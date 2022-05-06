package io.github.kynmarsher.webserviceback;

import com.devskiller.friendly_id.FriendlyId;
import com.devskiller.friendly_id.jackson.FriendlyIdModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.requests.CreateRoomRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;

public class WebServiceBack {
    // Эти поля - мапперы которые конвертируют объект в JSON и обратно в объект
    public static ObjectMapper RESPONSE_MAPPER;
    public static ObjectMapper STRICT_MAPPER;

    public Map<UUID, Room> roomList;

    public WebServiceBack() {
        roomList = new HashMap<>();

        // Настройка JSON мапперов
        RESPONSE_MAPPER = new ObjectMapper();
        RESPONSE_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        RESPONSE_MAPPER.registerModule(new FriendlyIdModule());
        STRICT_MAPPER = new ObjectMapper();
        STRICT_MAPPER.registerModule(new FriendlyIdModule());


        port(3100);

        // Получение комнаты
        get("/room/:roomid", (request, response) -> {
            // Получаем предполагаемый номер комнаты из запроса
            final String roomId = request.params(":roomid");
            // Проверяем существует ли такая комната
            return roomList.get(UUID.fromString(roomId));
        });

        // Создание комнаты
        post("/room", (request, response) -> {
            // Читаем JSON реквеста и прверащаем его в объект CreateRoomRequest
            CreateRoomRequest incomingRequest = STRICT_MAPPER.readValue(request.body(), CreateRoomRequest.class);
            // Создаем новый ID из имени которое введет пользователь
            String creatorId = FriendlyId.toFriendlyId(UUID.fromString(incomingRequest.roomCreatorName));
            // Создаем объект Room c автором запроса как с админом
            Room newRoom = new Room(creatorId);
            // Сохраняем новую комнату в список комнат
            roomList.put(newRoom.roomId(), newRoom);
            // Возвращаем Id комнаты
            return newRoom.roomId();
        });
        put("/room/:roomid", (request, response)-> {
            return 200;
        });


    }
}
