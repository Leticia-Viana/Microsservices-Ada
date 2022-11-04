package io.github.jtsato.moviesbattle.entrypoint.rest.domains.game.end;

import io.github.jtsato.moviesbattle.entrypoint.rest.common.HttpStatusConstants;
import io.github.jtsato.moviesbattle.entrypoint.rest.domains.game.GameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;

/**
 * @author Jorge Takeshi Sato
 */

@Tag(name = "Games")
@FunctionalInterface
public interface EndGameApiMethod {

    @Operation(operationId = "endGame", summary = "End the current game")

    @Parameter(name = "Accept-Language",
               example = "pt_BR",
               in = ParameterIn.HEADER,
               description = "Represents a specific geographical, political, or cultural region. Language & Country.")

    @Parameter(name = "PlayerId",
            example = "0",
            in = ParameterIn.HEADER,
            description = "Player Id: 0=John Smith, 1=Joe Doe, 2=Ashley Williams")

    @ApiResponses(value = {@ApiResponse(responseCode = HttpStatusConstants.OK_200, description = HttpStatusConstants.OK_200_MESSAGE),
                           @ApiResponse(responseCode = HttpStatusConstants.BAD_REQUEST_400, description = HttpStatusConstants.BAD_REQUEST_400_MESSAGE),
                           @ApiResponse(responseCode = HttpStatusConstants.UNAUTHORIZED_401, description = HttpStatusConstants.UNAUTHORIZED_401_MESSAGE),
                           @ApiResponse(responseCode = HttpStatusConstants.INTERNAL_SERVER_ERROR_500,
                                        description = HttpStatusConstants.INTERNAL_SERVER_ERROR_500_MESSAGE),})

    GameResponse execute();
}
