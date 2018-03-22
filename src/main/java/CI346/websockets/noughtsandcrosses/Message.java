package CI346.websockets.noughtsandcrosses;

import java.util.Collection;

public class Message {
    private NACWebSocket.MsgType msgType;
    private String userMessage;
    private Collection<String> userList;

    public Message(NACWebSocket.MsgType msgType, String userMessage, Collection<String> userList) {
        this.msgType = msgType;
        this.userMessage = userMessage;
        this.userList = userList;
    }

    public NACWebSocket.MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(NACWebSocket.MsgType msgType) {
        this.msgType = msgType;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public Collection<String> getUserList() {
        return userList;
    }

    public void setUserList(Collection<String> userList) {
        this.userList = userList;
    }

    public String toString() {
        return msgType+": "+userMessage;
    }
}
