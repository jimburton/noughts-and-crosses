import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static j2html.TagCreator.*;

@WebSocket
public class EchoWebSocket {

    // Store sessions if you want to, for example, broadcast a message to all users
    //private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    // this map is shared between sessions and threads, so it needs to be thread-safe
    // (http://stackoverflow.com/a/2688817)
    static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    static int nextUserNumber = 1;

    @OnWebSocketConnect
    public void connected(Session session) {
        //sessions.add(session);
        String username = "User" + nextUserNumber++;
        userUsernameMap.put(session, username);
        broadcastMessage("Server", username + " joined the club");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        //sessions.remove(session);
        String username = userUsernameMap.get(session);
        userUsernameMap.remove(session);
        broadcastMessage("Server", username + " left the chat");
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Got: " + message);   // Print message
        session.getRemote().sendString(message); // and send it back
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("userMessage", createHtmlMessageFromSender(sender, message))
                        .put("userlist", userUsernameMap.values())
                ));
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

}
