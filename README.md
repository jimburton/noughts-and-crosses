# Noughts and Crosses

This project demonstrates how to produce a simple WebSocket server using the Spark framework for web development,
along with a JavaScript client. Clients connect to server and are presented with a list of players online. The client
selects an opponent then the two clients play a game of Noughts and Crosses. The JavaScript for the game itself comes
from [Vasanth Krishnamoorthy](https://codepen.io/vasanthkay/details/KVzYzG).

Fetch the code then build and run it with Maven:

    $ git clone https://github.com/jimburton/sparktest
    $ cd sparktest
    $ mvn compile && mvn exec:java
    
For more information on how to use Spark, see http://sparkjava.com/tutorials/.
    
## Using the Lombok annotations

We are using the Lombok library to reduce the amount of boilerplate we have to write. 
For instance, in the `Game` class the `@Data` annotation generates the getters and 
setters, `@ToString` generates a custom `toString` method, and `@AllArgsConstructor` generates 
a constructor that requires all three fields:

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
        val name = msg.getUserMessage(); // name: String
        //...
    }
    //...
}
```
           
Note that your IDE will complain about the missing methods until you make it aware of Lombok, 
e.g. by installing a [plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin). Your favourite IDE probably 
supports Lombok, but if it doesn't you can build and run the project on the command line using Maven. 
See https://projectlombok.org/features/all.