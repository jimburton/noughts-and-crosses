package CI346.websockets.noughtsandcrosses;
/**
 * The WebSocket handler for playing the game.
 *
 * Includes examples of
 * + simple logging
 * + using Gson for encoding/decoding between JSON and POJOs
 *
 */

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static CI346.websockets.noughtsandcrosses.NACWebSocket.MsgType.*;

@Slf4j
@WebSocket
public class NACWebSocket {

    // this map is shared between sessions and threads, so it needs to be thread-safe
    // (http://stackoverflow.com/a/2688817)
    static Map<Session, Player> userMap = new ConcurrentHashMap<>();
    static Gson gson = new Gson();
    static Game game;

    /**
     * The Noughts and Crosses message protocol.
     */
    enum MsgType {
        JOIN        //CLIENT -> SERVER. Player requests to join a game
        , LEAVE     //CLIENT -> SERVER. Player is disconnecting
        , NAME      //CLIENT -> SERVER. Player is requesting a name.
        , NAME_ACK  //CLIENT <- SERVER. Name is accepted.
        , LIST      //CLIENT <- SERVER. Sending list of players not in a game
        , PLAYER_1  //CLIENT <- SERVER. Player is Player 1.
        , PLAYER_2  //CLIENT <- SERVER. Player is Player 2
        , MOVE      //CLIENT <-> SERVER. Message containing a move
    }

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

    /**
     * Handle incoming messages.
     * @param session
     * @param message
     * @throws IOException
     */
    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        log.info("Received: "+message.toString());
        Message msg = gson.fromJson(message, Message.class);
        log.info("Received: "+msg.toString());
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

    /**
     * Handle player starting a new game.
     * @param session
     * @param msg
     */
    private static void join(Session session, Message msg) {
        val p1 = userMap.get(session);
        val p2Opt = userMap.values().stream()
                .filter(p -> p.getName().equals(msg.getUserMessage()))
                .findFirst();
        if(p2Opt.isPresent() && !p2Opt.get().isInGame()) {
            val p2 = p2Opt.get();
            log.info("new game between "+p1.getName()+" and "+p2.getName());
            game = new Game(p1, p2);
            game.setInPlay(p1);
            val p2session = getSession(p2);
            send(session, PLAYER_1);
            send(p2session, PLAYER_2);
        } else {
            send(session, JOIN);
        }
    }

    /**
     * Set the player's name if the requested name is unique. Otherwise, request again.
     * @param session
     * @param msg
     */
    private static void setNameOrRequestAgain(Session session, Message msg) {
        val names = userMap.values();
        val name = msg.getUserMessage();
        if(names.stream()
                .map(Player::getName)
                .anyMatch(n -> n.equals(name))) {
            log.info("name clash!");
            send(session, MsgType.NAME);
        } else {
            log.info("name is free: "+name);
            userMap.put(session, new Player(name));
            log.info("Number of names: "+userMap.keySet().size());
            send(session, MsgType.NAME_ACK,
                    name, null);
            broadcastMessage(name, LIST, "");
        }
    }

    /**
     * Player is leaving.
     * @param session
     */
    private static void leave(Session session) {
        //Player left = userMap.get(session);
        userMap.remove(session);
        broadcastMessage("", LIST, "");
    }

    /**
     * Sends a message from one user to all users, along with a list of current usernames.
     * @param sender
     * @param type
     * @param msg
     */
    public static void broadcastMessage(String sender, MsgType type, String msg) {

        val names = userMap.values().stream()
                .filter(p -> !p.isInGame())
                .map(p -> p.getName())
                .collect(Collectors.toList());
        userMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                send(session, type, msg, names);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Helper method to find a session associated with Player p.
     * @param p
     * @return
     */
    private static Session getSession(Player p) {
        val sessionOpt = userMap.entrySet().stream()
                .filter(e -> e.getValue().equals(p))
                .map(Entry::getKey).findFirst();
        return (sessionOpt.isPresent() ? sessionOpt.get() : null);
    }

    /**
     * Send a message which consists only of a Message type.
     * @param session
     * @param type
     */
    private static void send(Session session, MsgType type) {
        send(session, type, "", null);
    }

    /**
     * Send a message with a Message type and a string forming the message itself.
     * @param session
     * @param type
     * @param theMsg
     */
    private static void send(Session session, MsgType type, String theMsg) {
        send(session, type, theMsg, null);
    }

    /**
     * Send a message with a type, some content and the list of available users.
     * @param session
     * @param type
     * @param theMsg
     * @param list
     */
    private static void send(Session session, MsgType type,
                             String theMsg, Collection<String> list) {
        val msg = new Message(type, theMsg, list);
        try {
            log.info("Sending: "+gson.toJson(msg));
            session.getRemote().sendString(gson.toJson(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
