import static spark.Spark.*;

public class HelloWorld {
    public static void main(String[] args) {
        get("/hello/:name", (req, res) -> "Hello " + req.params(":name ")+req.queryParams("key"));
    }
}