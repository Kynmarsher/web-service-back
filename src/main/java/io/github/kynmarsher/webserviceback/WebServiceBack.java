package io.github.kynmarsher.webserviceback;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.devskiller.friendly_id.FriendlyId;
import com.devskiller.friendly_id.jackson.FriendlyIdModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.datamodel.RoomMember;
import io.github.kynmarsher.webserviceback.httpdata.CreateRoomRequest;
import io.github.kynmarsher.webserviceback.httpdata.CreateRoomResponse;
import io.github.kynmarsher.webserviceback.socketio.OfferAnswerObject;
import io.github.kynmarsher.webserviceback.socketio.CreateOfferObject;
import io.github.kynmarsher.webserviceback.socketio.StartCallObject;
import io.github.kynmarsher.webserviceback.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;

@Slf4j
public class WebServiceBack {
    // Эти поля - мапперы которые конвертируют объект в JSON и обратно в объект
    public static ObjectMapper RESPONSE_MAPPER;
    public static ObjectMapper STRICT_MAPPER;

    public Map<UUID, Room> roomList;

    public Map<UUID, UUID> sessionId;

    public SocketIOServer socketIOServer;

    public WebServiceBack() {
        roomList = new HashMap<>();

        // Настройка JSON мапперов
        RESPONSE_MAPPER = new ObjectMapper();
        RESPONSE_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        RESPONSE_MAPPER.registerModule(new FriendlyIdModule());
        STRICT_MAPPER = new ObjectMapper();
        STRICT_MAPPER.registerModule(new FriendlyIdModule());
        initializeSocket();


        port(3100);

        // Получение комнаты
        get("/room/:roomid", (request, response) -> {
            // Получаем предполагаемый номер комнаты из запроса
            final String roomId = request.params(":roomid");
            // Проверяем существует ли такая комната
            if (roomList.containsKey(FriendlyId.toUuid(roomId))) {
                return roomList.get(FriendlyId.toUuid(roomId));
            } else {
                response.status(404);
                return 404;
            }
        });

        // Создание комнаты
        post("/room", (request, response) -> {
            // Читаем JSON реквеста и прверащаем его в объект CreateRoomRequest
            CreateRoomRequest incomingRequest = STRICT_MAPPER.readValue(request.body(), CreateRoomRequest.class);
            // Создаем новый ID из имени которое введет пользователь
            UUID creatorId = UUID.nameUUIDFromBytes(incomingRequest.getRoomCreatorName().getBytes());
            // Создаем объект Room c автором запроса как с админом
            Room newRoom = new Room(creatorId);
            // Сохраняем новую комнату в список комнат
            roomList.put(newRoom.roomId(), newRoom);

            // Подготавливаем ответ клиенту
            // Создаем CreateRoomResponse используюя паттерн Builder
            CreateRoomResponse responseObj = CreateRoomResponse.builder()
                    .name(incomingRequest.getRoomCreatorName())
                    .roomId(newRoom.roomId())
                    .userId(creatorId)
                    .build();

            // Выставляем статус, что все прошло успешно
            response.status(200);
            // Конвертируем объект ответа в Json
            response.body(Utils.dataToJson(responseObj));
            // Даем понять что мы будем передавать json
            response.type("application/json");
            return response.body();
        });


    }

    private void initializeSocket() {
        final var socketIOConfig = new Configuration();
        socketIOConfig.setPort(3200);
        socketIOServer = new SocketIOServer(socketIOConfig);
        socketIOServer.addEventListener("startCall", StartCallObject.class, (client, data, ackSender) -> {
            final var roomId = FriendlyId.toFriendlyId(data.getRoomId());
            client.joinRoom(roomId);
            roomList.get(data.getRoomId()).addMember(new RoomMember(data.getName(), client.getSessionId(), data.isVideo(), data.isAudio()));
            socketIOServer.getRoomOperations(roomId).sendEvent("startCall", client, data);
        });
        socketIOServer.addEventListener("createOffer", CreateOfferObject.class, (client, data, ackSender) -> {
            final var roomId = FriendlyId.toFriendlyId(data.getRoomId());
            socketIOServer.getRoomOperations(roomId).sendEvent("createOffer", client, data);
        });
        socketIOServer.addEventListener("answerOffer", OfferAnswerObject.class, (client, data, ackSender) -> {
            final var roomId = FriendlyId.toFriendlyId(data.getRoomId());
            socketIOServer.getClient(data.getAnswerTo()).sendEvent("answerOffer", data);
        });
        socketIOServer.start();
    }
}
