package io.github.jtsato.moviesbattle.core.domains.quiz.usecase;

import io.github.jtsato.moviesbattle.core.common.GetLocalDateTime;
import io.github.jtsato.moviesbattle.core.domains.game.models.Game;
import io.github.jtsato.moviesbattle.core.domains.game.models.Status;
import io.github.jtsato.moviesbattle.core.domains.game.xcutting.GetGameByPlayerIdAndStatusGateway;
import io.github.jtsato.moviesbattle.core.domains.game.xcutting.GetUnansweredQuizzesByGateway;
import io.github.jtsato.moviesbattle.core.domains.movie.model.Movie;
import io.github.jtsato.moviesbattle.core.domains.movie.xcutting.GetAllMoviesCountGateway;
import io.github.jtsato.moviesbattle.core.domains.movie.xcutting.GetRandomMovieGateway;
import io.github.jtsato.moviesbattle.core.domains.player.model.Player;
import io.github.jtsato.moviesbattle.core.domains.player.usecase.RegisterPlayerCommand;
import io.github.jtsato.moviesbattle.core.domains.player.usecase.RegisterPlayerUseCase;
import io.github.jtsato.moviesbattle.core.domains.player.xcutting.GetPlayerByEmailGateway;
import io.github.jtsato.moviesbattle.core.domains.quiz.model.Quiz;
import io.github.jtsato.moviesbattle.core.exception.InvalidActionException;
import io.github.jtsato.moviesbattle.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.inject.Named;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * A Use Case follows these steps:
 * - Takes input
 * - Validates business rules
 * - Manipulates the model state
 * - Returns output
 */

/**
 * @author Jorge Takeshi Sato
 */

@Named
@RequiredArgsConstructor
public class RequestQuizUseCaseImpl implements RequestQuizUseCase {

    private final GetPlayerByEmailGateway getPlayerByEmailGateway;
    private final RegisterPlayerUseCase registerPlayerUseCase;
    private final GetGameByPlayerIdAndStatusGateway getGameByPlayerIdAndStatusGateway;
    private final GetUnansweredQuizzesByGateway getUnansweredQuizzesByGateway;
    private final GetRandomMovieGateway getRandomMovieGateway;
    private final GetAllMoviesCountGateway getAllMoviesCountGateway;
    private final GetAllQuizzesByGameIdGateway getAllQuizzesByGameIdGateway;
    private final GetLocalDateTime getLocalDateTime;
    private final RegisterQuizGateway registerQuizGateway;

    @Override
    public Quiz execute(final RequestQuizCommand command) {

        final Player player = getPlayerOrRegister(command.getPlayerEmail(), command.getPlayerName());
        final Game game = getGameInProgressByPlayerId(player.getId());
        final Optional<Quiz> optional = getUnansweredQuizByGameId(game.getId());

        if (optional.isPresent()) {
            return optional.get();
        }

        final List<Quiz> quizzes = getAllQuizzesByGameIdGateway.execute(game.getId());

        checkIfReachedTheLimitOfLosses(game.getId(), quizzes);

        checkIfTheCombinationsIsOver(quizzes);

        ImmutablePair<Movie, Movie> immutablePair = getNewMoviesPair(quizzes);

        Movie movieOne = immutablePair.getLeft();
        Movie movieTwo = immutablePair.getRight();

        final String optionOneId = movieOne.getImdbId();
        final String optionOneTitle = movieOne.getTitle();
        final String optionOneYear = movieOne.getYear();
        final String optionTwoId = movieTwo.getImdbId();
        final String optionTwoTitle = movieTwo.getTitle();
        final String optionTwoYear = movieTwo.getYear();
        final LocalDateTime createdAt = getLocalDateTime.now();
        final LocalDateTime updatedAt = getLocalDateTime.now();

        final Quiz quiz = new Quiz(null,
                                   game,
                                   null,
                                   optionOneId,
                                   optionOneTitle,
                                   optionOneYear,
                                   optionTwoId,
                                   optionTwoTitle,
                                   optionTwoYear,
                                   createdAt,
                                   updatedAt);

        return registerQuizGateway.execute(quiz);
    }

    private Player getPlayerOrRegister(final String email, String name) {
        final Optional<Player> optional = getPlayerByEmailGateway.execute(email);
        return optional.orElseGet(() -> registerNewPlayer(email, name));
    }

    private Player registerNewPlayer(final String email, String name) {
        final RegisterPlayerCommand registerPlayerCommand = new RegisterPlayerCommand(email, name);
        return registerPlayerUseCase.execute(registerPlayerCommand);
    }

    private Game getGameInProgressByPlayerId(final Long playerId) {
        Optional<Game> optionalGame = getGameByPlayerIdAndStatusGateway.execute(playerId, Status.IN_PROGRESS);
        return optionalGame.orElseThrow(() -> new NotFoundException("validation.game.in.progress.notfound", String.valueOf(playerId)));
    }

    private Optional<Quiz> getUnansweredQuizByGameId(final Long gameId){
        return getUnansweredQuizzesByGateway.execute(gameId);
    }

    private ImmutablePair<Movie, Movie> getNewMoviesPair(List<Quiz> quizzes) {

        Movie movieOne = getRandomMovie();
        Movie movieTwo = getRandomMovie();

        while (movieOne.getId().equals(movieTwo.getId())) {
            movieTwo = getRandomMovie();
        }

        while (isTheQuizRepeated(quizzes, movieOne.getImdbId(), movieTwo.getImdbId())) {
            movieOne = getRandomMovie();
            movieTwo = getRandomMovie();
            while (movieOne.getId().equals(movieTwo.getId())) {
                movieTwo = getRandomMovie();
            }
        }

        return ImmutablePair.of(movieOne, movieTwo);
    }

    private static boolean isTheQuizRepeated(final List<Quiz> quizzes, final String movieOneImdbId, final String movieTwoImdbId) {
        return quizzes.stream().anyMatch(quiz ->
                (quiz.getOptionOneId().equals(movieOneImdbId) && quiz.getOptionTwoId().equals(movieTwoImdbId)) ||
                (quiz.getOptionOneId().equals(movieTwoImdbId) && quiz.getOptionTwoId().equals(movieOneImdbId))
        );
    }

    private Movie getRandomMovie() {
        final Optional<Movie> optional = getRandomMovieGateway.execute();
        return optional.orElseThrow(() -> new NotFoundException("validation.movies.database.empty"));
    }

    private void checkIfReachedTheLimitOfLosses(final Long gameId, final List<Quiz> quizzes) {
        final long losses = quizzes.stream().map(Quiz::getBet).filter(bet -> !bet.getWinTheBet()).count();
        if (losses >= 3) {
            throw new InvalidActionException("validation.game.losses.limit.reached", String.valueOf(gameId));
        }
    }

    private void checkIfTheCombinationsIsOver(List<Quiz> quizzes) {
        final Long count = getAllMoviesCountGateway.execute();
        final BigInteger combinationsCount = combinationWithRepetition(Math.toIntExact(count), 2);
        if (combinationsCount.compareTo(BigInteger.valueOf(quizzes.size())) <= 0) {
            throw new NotFoundException("validation.movie.combinations.over");
        }
    }

    private static BigInteger combinationWithRepetition(final int n, final int r) {
        return factorial(n).divide(factorial(r).multiply(factorial(n - r)));
    }

    private static BigInteger factorial(final int n) {
        BigInteger result = BigInteger.valueOf(n);
        for (int index = n - 1; index > 1; index--) {
            result = result.multiply(BigInteger.valueOf(index));
        }
        return result;
    }
}
