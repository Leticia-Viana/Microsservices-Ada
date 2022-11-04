package io.github.jtsato.moviesbattle.core.domains.game.usecases.start;

import io.github.jtsato.moviesbattle.core.common.validation.SelfValidating;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Jorge Takeshi Sato
 */

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class StartGameCommand extends SelfValidating<StartGameCommand> implements Serializable {

    private static final long serialVersionUID = -3480489082836991592L;

    @NotBlank(message = "validation.player.email.blank")
    private final String playerEmail;

    @NotBlank(message = "validation.player.name.blank")
    private final String playerName;

    public StartGameCommand(final String playerEmail, final String playerName) {
        this.playerEmail = playerEmail;
        this.playerName = playerName;
        this.validateSelf();
    }
}
