package io.github.kynmarsher.webserviceback;
import java.util.HashMap;

import static spark.Spark.*;

public class WebServiceBack {
   public HashMap<String, Room> roomList;
    public WebServiceBack(){
        roomList=new HashMap<>();
        port(3100);
        get("/room/:roomid", (request, response) -> {
            return roomList.get(request.params(":roomid")).roomId;
        });
       post("/room", (request, response) -> {
            roomList.put("123", new Room("sirop1234"));
            return 200;
        });
       put("/room:roomid", (request, response)-> {

        });


    }
}
