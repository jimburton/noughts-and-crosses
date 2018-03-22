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
    static Map<Session, String> userMap = new ConcurrentHashMap<>();
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

    static Logger logger = LoggerFactory.getLogger(NACWebSocket.class);

    @OnWebSocketConnect
    public void connected(Session session) {
        //session.getRemote().sendString();
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        //sessions.remove(session);
        String username = userMap.get(session);
        userMap.remove(session);
        //broadcastMessage("Server", MsgType.INFO, username + " left the club");
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        logger.info("Received: "+message.toString());
        Message msg = gson.fromJson(message, Message.class);
        logger.info("Received: "+msg.toString());
        switch (msg.getMsgType()) {
            case NAME:
                Collection<String> names = userMap.values();
                String name = msg.getUserMessage();
                if(names.contains(name)) {
                    logger.info("name clash!");
                    send(session, MsgType.NAME);
                } else {
                    logger.info("name is free: "+name);
                    userMap.put(session, name);
                    logger.info("Number of names: "+userMap.keySet().size());
                    send(session, MsgType.NAME_ACK,
                            name, userMap.values());
                }
                break;
            case NAME_ACK:
            case INFO:
            case JOIN:
            case MOVE:
            case TEXT:
            case LEAVE:
                break;
        }
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, MsgType type, String message) {

        Collection<String> vals = userMap.values();
        userMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                send(session, type, "", vals);
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

    private static void send(Session session, MsgType type) {
        send(session, type, "", null);
    }

    private static void send(Session session, MsgType type, String theMsg) {
        send(session, type, theMsg, null);
    }

    private static void send(Session session, MsgType type,
                             String theMsg, Collection<String> list) {
        Message msg = new Message(type,
                theMsg,
                list);
        try {
            logger.info("Sending: "+gson.toJson(msg));
            session.getRemote().sendString(gson.toJson(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MsgType stringToMsgType(String str) {
        MsgType[] vals = MsgType.values();
        for(MsgType t: vals) {
            if(t.name().equals(str)) {
                return t;
            }
        }
        return null;
    }

}
