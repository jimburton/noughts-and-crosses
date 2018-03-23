# Playing with Spark

http://sparkjava.com/tutorials/

http://velocity.apache.org/engine/2.0/user-guide.html

https://sparktutorials.github.io/2015/11/08/spark-websocket-chat.html

    $ mvn compile && mvn exec:java
    
Using the Lombok annotations
to avoid writing getters and setters. Note that your IDE may complain about
the missing methods until you make it aware of Lombok, e.g. by installing
a plugin. If your IDE doesn't support Lombok, build and run the project on
the command line using Maven.

Note that we are using Lombok's `val' declaration for type-inferred local final variables.