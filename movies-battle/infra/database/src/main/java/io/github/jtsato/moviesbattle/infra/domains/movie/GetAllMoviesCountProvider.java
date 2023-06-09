package io.github.jtsato.moviesbattle.infra.domains.movie;

import io.github.jtsato.moviesbattle.core.domains.movie.xcutting.GetAllMoviesCountGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jorge Takeshi Sato
 */

@RequiredArgsConstructor
@Service
public class GetAllMoviesCountProvider implements GetAllMoviesCountGateway {

    private final MovieClient movieClient;

    @Override
    public Long execute() {
        return movieClient.getAllMoviesCount().count();
    }
}