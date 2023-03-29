package io.github.jtsato.moviesbattle.infra.domains.movie;

import io.github.jtsato.moviesbattle.core.domains.movie.model.Movie;
import io.github.jtsato.moviesbattle.core.domains.movie.xcutting.GetMovieByImdbIdGateway;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Jorge Takeshi Sato
 */

@RequiredArgsConstructor
@Service
public class GetMovieByImdbIdProvider implements GetMovieByImdbIdGateway {
    private static final MovieResponseMapper MOVIE_RESPONSE_MAPPER = Mappers.getMapper(MovieResponseMapper.class);
    private final MovieClient movieClient;

    @Override
    public Optional<Movie> execute(String ImdbId) {
        return Optional.of(MOVIE_RESPONSE_MAPPER.of(movieClient.getMovieByImdbId(ImdbId)));
    }
}
