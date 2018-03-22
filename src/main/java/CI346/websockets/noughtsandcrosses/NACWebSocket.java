package CI346.websockets.noughtsandcrosses;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
//import org.json.JSONObject;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static CI346.websockets.noughtsandcrosses.NACWebSocket.MsgType.NAME;
import static j2html.TagCreator.*;

@WebSocket
public class NACWebSocket {

    // Store sessions if you want to, for example, broadcast a message to all users
    //private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    // this map is shared between sessions and threads, so it needs to be thread-safe
    // (http://stackoverflow.com/a/2688817)
    static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    static int nextUserNumber = 1;
    static Gson gson = new Gson();


    enum MsgType {
        JOIN
        , LEAVE
        , TEXT
        , INFO
        , MOVE
        , NAME
        , NAME_ACK
    }

    Logger logger = LoggerFactory.getLogger(NACWebSocket.class);

    @OnWebSocketConnect
    public void connected(Session session) {
        //session.getRemote().sendString();
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        //sessions.remove(session);
        String username = userUsernameMap.get(session);
        userUsernameMap.remove(session);
        broadcastMessage("Server", MsgType.INFO, username + " left the club");
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        logger.info("Received: "+message.toString());
        Message[] msgs = gson.fromJson(message, Message[].class);
        logger.info("Received: "+msgs[0].toString());
        if(message.startsWith(NAME.toString())) {
            Collection<String> names = userUsernameMap.values();
            String name = message.substring(NAME.toString().length());
            if(names.contains(name)) {
                send(session, MsgType.NAME);
            } else {
                userUsernameMap.put(session, name);
                send(session, MsgType.NAME_ACK, String.valueOf(userUsernameMap.values()));
            }
        }
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, MsgType type, String message) {

        Message msg = new Message(type.toString(),
                createHtmlMessageFromSender(sender, message),
                userUsernameMap.values());
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(gson.toJson(msg));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    private void send(Session session, MsgType type) {
        send(session, type, "");
    }

    private void send(Session session, MsgType type, String theMsg) {
        Message msg = new Message(type.toString(),
                theMsg,
                null);
        try {
            session.getRemote().sendString(gson.toJson(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
