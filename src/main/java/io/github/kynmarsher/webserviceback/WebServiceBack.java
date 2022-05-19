package io.github.kynmarsher.webserviceback;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.datamodel.RoomMember;
import io.github.kynmarsher.webserviceback.socketio.room.CreateRoomRequestPacket;
import io.github.kynmarsher.webserviceback.socketio.room.CreateRoomResponsePacket;
import io.github.kynmarsher.webserviceback.socketio.room.JoinRoomRequestPacket;
import io.github.kynmarsher.webserviceback.socketio.room.JoinRoomResponsePacket;
import io.github.kynmarsher.webserviceback.socketio.webrtc.CreateOfferPacket;
import io.github.kynmarsher.webserviceback.socketio.webrtc.OfferAnswerPacket;
import io.github.kynmarsher.webserviceback.socketio.webrtc.StartCallPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        initializeSocket();
    }

    private void initializeSocket() {
        final var socketIOConfig = new Configuration();
        socketIOConfig.setPort(3200);
        socketIOServer = new SocketIOServer(socketIOConfig);

        socketIOServer.addEventListener("createRoom", CreateRoomRequestPacket.class, (client, data, ackSender) -> {
            Room newRoom = new Room(client.getSessionId());
            // Сохраняем новую СВОЙ ОБЪЕКТ комнаты в список комнат
            roomList.put(newRoom.roomId(), newRoom);

            final var responseObj = CreateRoomResponsePacket.builder()
                    .name(data.getRoomCreatorName())
                    .roomId(newRoom.roomId())
                    .userId(client.getSessionId())
                    .build();


            client.sendEvent("createRoom", responseObj);
        });
        socketIOServer.addEventListener("joinRoom", JoinRoomRequestPacket.class, (client, data, ackSender) -> {
            final var responseObj = JoinRoomResponsePacket.builder()
                    .errorMessage("Room doesn't exist yet or expired")
                    .status(false);


            if (roomList.containsKey(data.getRoomId())) {
                responseObj.status(true);
                responseObj.errorMessage("Success");
                // Присоединяем в своих комнатах
                roomList.get(data.getRoomId()).addMember(new RoomMember(data.getName(), client.getSessionId(), data.isUseVideo(), data.isUseAudio()));
                // Присоединяем к сокет комнате
                client.joinRoom(data.getRoomId().toString());
            }

            client.sendEvent("joinRoom", responseObj.build());
        });

        socketIOServer.addEventListener("startCall", StartCallPacket.class, (client, data, ackSender) -> {
            socketIOServer.getRoomOperations(data.getRoomId().toString()).sendEvent("startCall", client, data);
        });
        socketIOServer.addEventListener("createOffer", CreateOfferPacket.class, (client, data, ackSender) -> {
            socketIOServer.getRoomOperations(data.getRoomId().toString()).sendEvent("createOffer", client, data);
        });
        socketIOServer.addEventListener("answerOffer", OfferAnswerPacket.class, (client, data, ackSender) -> {
            socketIOServer.getClient(data.getAnswerTo()).sendEvent("answerOffer", data);
        });
        socketIOServer.start();
    }
}
