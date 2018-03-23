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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static CI346.websockets.noughtsandcrosses.NACWebSocket.MsgType.*;
import static j2html.TagCreator.*;

@WebSocket
public class NACWebSocket {

    // Store sessions if you want to, for example, broadcast a message to all users
    //private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    // this map is shared between sessions and threads, so it needs to be thread-safe
    // (http://stackoverflow.com/a/2688817)
    static Map<Session, Player> userMap = new ConcurrentHashMap<>();
    static List<Game> games = new ArrayList<>();
    static Gson gson = new Gson();


    enum MsgType {
        JOIN
        , LEAVE
        , LIST
        , MOVE
        , NAME
        , NAME_ACK
        , PLAYER_1
        , PLAYER_2
    }

    static Logger logger = LoggerFactory.getLogger(NACWebSocket.class);

    @OnWebSocketConnect
    public void connected(Session session) {
        //session.getRemote().sendString();
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        //sessions.remove(session);
        //String username = userMap.get(session).getName();
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
                setNameOrRequestAgain(session, msg);
                break;
            case LEAVE:
                leave(session);
                break;
            case JOIN:
                join(session, msg);
                break;
            case MOVE:
                break;
        }
    }

    private static void join(Session session, Message msg) {
        Player p1 = userMap.get(session);
        Optional<Player> p2Opt = userMap.values().stream()
                .filter(p -> p.getName().equals(msg.getUserMessage()))
                .findFirst();
        if(p2Opt.isPresent() && !p2Opt.get().isInGame()) {
            Player p2 = p2Opt.get();
            logger.info("new game between "+p1.getName()+" and "+p2.getName());
            Game g = new Game(p1, p2);
            games.add(g);
            Session p2session = getSession(p2);
            send(session, PLAYER_1);
            send(p2session, PLAYER_2);
        } else {
            send(session, JOIN);
        }
    }

    private static Session getSession(Player p) {
        for (Entry<Session, Player> entry : userMap.entrySet()) {
            if (p.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static void setNameOrRequestAgain(Session session, Message msg) {
        Collection<Player> names = userMap.values();
        String name = msg.getUserMessage();
        if(names.stream().map(Player::getName).anyMatch(n -> n.equals(name))) {
            logger.info("name clash!");
            send(session, MsgType.NAME);
        } else {
            logger.info("name is free: "+name);
            userMap.put(session, new Player(name));
            logger.info("Number of names: "+userMap.keySet().size());
            send(session, MsgType.NAME_ACK,
                    name, null);
            broadcastMessage(name, LIST, "");
        }
    }

    private static void leave(Session session) {
        Player left = userMap.get(session);
        userMap.remove(session);
        games = games.stream()
                .filter(g -> !(g.getP1().equals(left) && !(g.getP2().equals(left))))
                .collect(Collectors.toList());
        broadcastMessage("", LIST, "");
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public static void broadcastMessage(String sender, MsgType type, String msg) {

        Collection<String> names = userMap.values().stream()
                .filter(p -> !p.isInGame())
                .map(p -> p.getName())
                .collect(Collectors.toList());
        Collection<String> gameStrings = games.stream()
                .map(g -> g.getP1().getName()+" vs "+g.getP1().getName())
                .collect(Collectors.toList());
        gameStrings.addAll(names);
        userMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                send(session, type, msg, gameStrings);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
}
