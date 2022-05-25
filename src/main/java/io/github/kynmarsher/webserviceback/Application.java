package io.github.kynmarsher.webserviceback;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.socketio.room.CreateRoomRequestPacket;
import io.github.kynmarsher.webserviceback.socketio.room.CreateRoomResponsePacket;
import io.socket.engineio.server.Emitter;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoSocket;
import lombok.extern.slf4j.Slf4j;

import static io.github.kynmarsher.webserviceback.util.Utils.dataToJson;

@Slf4j
public class Application {
    public static void main(String[] args){
        WebServiceBack server = new WebServiceBack();
        try {
            server.startServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final var socketServer = server.mSocketIoServer();
        final var mainNamespace = socketServer.namespace("/");
        mainNamespace.on("connection", arguments -> {
            SocketIoSocket socket = (SocketIoSocket) arguments[0];
            System.out.println("Client " + socket.getId() + " (" + socket.getInitialHeaders().get("remote_addr") + ") has connected.");

            socket.on("message", msgArgs -> {
                System.out.println("[Client " + socket.getId() + "] " + msgArgs);
                socket.send("message", "test message", 1);
            });

            socket.on("createRoom", msgArgs -> {
                try {
                    final var incomingRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateRoomRequestPacket.class);
                    System.out.println("[Client " + socket.getId() + "] " + incomingRequest.roomCreatorName());
                    Room newRoom = new Room(socket.getId());
                    // Сохраняем новую СВОЙ ОБЪЕКТ комнаты в список комнат
                    WebServiceBack.INSTANCE.roomList().put(newRoom.roomId(), newRoom);

                    final var responseObj = CreateRoomResponsePacket.builder()
                            .name(incomingRequest.roomCreatorName())
                            .roomId(newRoom.roomId())
                            .userId(socket.getId())
                            .build();


                    socket.send("createRoom", dataToJson(responseObj));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });
    }
}
