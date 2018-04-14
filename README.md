# Noughts and Crosses

This project demonstrates how to produce a simple WebSocket server using the [Spark](http://sparkjava.com) framework 
for web development, along with a JavaScript client. Clients connect to the server and are presented with a list of players 
online. The client selects an opponent then the two clients play a game of Noughts and Crosses. 

Fetch the code, then build and run it with Maven:

    $ git clone https://github.com/jimburton/noughts-and-crosses
    $ cd noughts-and-crosses
    $ mvn compile && mvn exec:java
    
This starts the server running. Now you can open a client by visiting http://localhost:4567. Open 
several clients in different tabs and connect to the server using different names. As users join 
the server, their names will be listed as links -- clicking on a link will start a game with that 
user.

For information on how to use Spark, see http://sparkjava.com/tutorials/.
    
## Using the Lombok annotations

We are using the [Lombok](https://projectlombok.org) library to reduce the amount of boilerplate 
we have to write. For instance, in the `Message` class the `@Data` annotation generates the getters 
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
you need to edit the class `NACWebSocket`. Add a `MsgType` called `CHAT` and a clause to the 
switch statement in the `onMessage` handler that sends a message to both opponents. Note the
method `getOpponentSession`. You can expect the message to be stored in the `userMessage` field 
of the `Message` that arrives. (It is pretty inefficient to send the chat message back to
the user who wrote it but treating both users in the same way makes the client side simpler.)

On the client side, you will be adding code to the file `script.js` in the `resources` folder. 
Although you aren't expected to be a JavaScript expert, you should be able to work out how to
add the chat functionality by calling existing functions and changing the properties of some
HTML elements. You need to enable the form fields relating to chat when a game begins, and disable 
them when a game ends. Read the contents of `index.html` to find the identifiers of the fields, 
which is given by the `id` attribute in a tag. For instance, `foo` in `<button id="foo"...`. 
Then you can enable `foo` like this:

    id("foo").disabled = false;
    
Note that this is using our helper function, `id`, to get a reference to the DOM element. In addition,
when disabling the chat controls, you should clear the `div` with the id `chat_area` by setting its
`innerHTML` property to the empty string.

Put the enabling and disabling code into two functions, `enableChat` and `disableChat`. Add calls
to these functions in `setNameAndPlayer` (called when a game begins) and
`setupJoin` (called when the page loads and when a game ends). When the chat form is submitted, 
a function called `chat`, defined in `script.js`, is invoked -- edit this function to grab 
the contents of the field `form_chat_text` and prepend the name of the current player before it 
(e.g. if your username is `"bob"` and you type `"Hi"` into the field, the text that should be sent is
`"bob: Hi"`). The username is available in the field `name`. Use the `send` function to send the 
message to the server with the string `"CHAT"` as its type and the text as its `userMessage`. 
The contents of the field can be accessed like this:

    var txt = id("form_chat_text").value;

Finally for the client side, add a clause to the switch statement in the `handleMessage` to respond
when a message with the type `"CHAT"` arrives. You should append the contents of `data.userMessage`
to the `div` with the id `chat_area`. You can do that using the `insertAdjacentHTML` function:

    id("chat_area").insertAdjacentHTML("beforeend", text);

In order to make messages appear on a new line, wrap the text in `<p>...</p>` tags before inserting
it.
