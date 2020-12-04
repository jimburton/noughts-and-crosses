# Noughts and Crosses

This project demonstrates how to produce a simple WebSocket server using the [Spark](http://sparkjava.com) framework 
for web development, along with a JavaScript client. Clients connect to the server and are presented with a list of players 
online. The client selects an opponent then the two clients play a game of Noughts and Crosses. The JavaScript for the 
board comes from [Vasanth Krishnamoorthy](https://codepen.io/vasanthkay/details/KVzYzG).

Fetch the code, then build and run it with Maven:

    $ git clone https://github.com/jimburton/sparktest
    $ cd sparktest
    $ mvn compile && mvn exec:java

This starts the websocket class running on `ws://localhost:4567` and the webpage that provides the UI running
on `http://localhost:4567`. Open two copies of the webpage in your browser. Enter a name for each player then 
click on the name of the other player in one window.

For more information on how to use Spark, see http://sparkjava.com/tutorials/.
    
## Using the Lombok annotations

We are using the [Lombok](https://projectlombok.org) library to reduce the amount of boilerplate 
we have to write. For instance, in the `Game` class the `@Data` annotation generates the getters 
and setters, `@ToString` generates a custom `toString` method, and `@AllArgsConstructor` saves us
from writing a constructor that requires all three fields:

```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import java.util.Collection;

@Data
@ToString(exclude = "userList")
@AllArgsConstructor
public class Message {
    private NACWebSocket.MsgType msgType;
    private String userMessage;
    private Collection<String> userList;
}

```

Elsewhere, we use Lombok's `val` declaration for type-inferred local final variables:

```java
public class NACWebSocket {
    //...
    private static void setNameOrRequestAgain(Session session, Message msg) {   
        val names = userMap.values();    // names: Collection<Player>
        //...
    }
    //...
}
```
           
Note that your IDE will complain about the missing methods until you make it aware of Lombok, 
e.g. by installing a [plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin). Your 
favourite IDE probably supports Lombok, but if it doesn't you can build and run the project 
on the command line using Maven. See https://projectlombok.org/features/all for an explanation 
of all annotations.

## Encoding and Decoding JSON

All messages are sent between clients and server as `JSON` objects. On the server side, we use
the [`Gson`](https://github.com/google/gson) library to bind JSON directly to POJO classes. Note 
the use of `fromJson` and `toJson` below:

```java
public class NACWebSocket {
    //...
    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException { 
        log.info("Received: "+message.toString());
        val msg = gson.fromJson(message, Message.class); //decodes JSON and packs it into an instance of Message
        //...
    }
    
    private static void send(Session session, MsgType type,
                                 String theMsg, Collection<String> list) {
        val msg = new Message(type, theMsg, list);
        try {
            log.info("Sending: "+gson.toJson(msg));//encodes msg as a JSON string
            session.getRemote().sendString(gson.toJson(msg)); 
        } catch (Exception e) {
            e.printStackTrace(); 
        } 
    }
}
```

One the client side, we use the `parse` and `stringify` methods of the `JSON` object:

```javascript
function handleMessage(msg) {
    var data = JSON.parse(msg.data);//msg.data is a String, data is a JS object
    //...
}

function send(type, msg) {
    var data = {"msgType": type, "userMessage": msg};
    websocket.send(JSON.stringify(data));//turn data into a String
}
```

## Exercises

Add the functionality to allow players to chat with each other during a game. On the server side,
add a `MsgType` called `CHAT` and a clause to the switch statement in the `onMessage` handler that
sends a message to the opponent. You can expect the message to be stored in the `userMessage` field
of the `Message` that arrives.

On the client side, you will be adding code to the file `script.js`. You need to enable the form 
with the id `form_chat` when a game begins, and disable it when a game ends. If you have a form 
field called `"foo"` you can enable it like this:

    id("foo").disabled = false;
    
Note that this is using our helper function, `id`, to get a reference to the element.

Add the enabling and disabling code to the functions `setupLeave` (called when a game begins) and
`setupJoin` (called when the page loads and when a game ends). When the chat form is submitted, 
a function called `chat` is invoked -- edit this function to grab the contents of the field
`form_chat_text` and use the `send` function to send a message to the server with the string 
`"CHAT"` as its type and the contents of the field as its `userMessage`.

Finally for the client side, add a clause to the switch statement in the `handleMessage` to respond
when a message with the type `"CHAT"` arrives. You should append the contents of `data.userMessage`
to the `div` with the id `chat_area`.