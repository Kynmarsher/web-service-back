package io.github.kynmarsher.webserviceback;
import static spark.Spark.*;

public class WebServiceBack {
    public WebServiceBack(){
        port(3100);
        get("/hello", (request, response) -> {
            System.out.println("Suck");
            return "Cock";
        });
    }
}
