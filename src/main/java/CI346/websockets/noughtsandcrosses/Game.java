package CI346.websockets.noughtsandcrosses;
/**
 * A POJO for a game, which contains two players and the associated sessions.
 */
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.websocket.api.Session;

@RequiredArgsConstructor
@Data
public class Game {
    @NonNull
    private final Player p1;
    @NonNull
    private final Player p2;
    @NonNull
    private final Session p1Session;
    @NonNull
    private final Session p2Session;

    public Session getOpponentSession(Session session) {
        return (session.equals(p1Session) ? p2Session : p1Session);
    }
}
