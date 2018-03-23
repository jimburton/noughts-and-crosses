package CI346.websockets.noughtsandcrosses;
/**
 * A POJO that represents a player.
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(exclude="inGame")
@RequiredArgsConstructor
@Data
public class Player {
    @NonNull
    private final String name;
    private boolean inGame;
}
