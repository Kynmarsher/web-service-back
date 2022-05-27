package io.github.kynmarsher.webserviceback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.datamodel.RoomMember;
import io.github.kynmarsher.webserviceback.socketio.room.CreateRoomRequestPacket;
import io.github.kynmarsher.webserviceback.socketio.room.CreateRoomResponsePacket;
import io.github.kynmarsher.webserviceback.socketio.room.JoinRoomRequestPacket;
import io.github.kynmarsher.webserviceback.socketio.room.JoinRoomStatusPacket;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.kynmarsher.webserviceback.util.Utils.dataToJson;

@Slf4j
@Accessors(fluent = true)
public class WebServiceBack {
    // Эти поля - мапперы которые конвертируют объект в JSON и обратно в объект
    public static ObjectMapper RESPONSE_MAPPER;
    public static ObjectMapper STRICT_MAPPER;
    @Getter
    public Map<UUID, Room> roomList;
    public Map<UUID, UUID> sessionId;

    private final EngineIoServer mEngineIoServer;
    private final EngineIoServerOptions eioOptions;
    private @Getter final SocketIoServer mSocketIoServer;
    private final Server mServer;

    public static WebServiceBack INSTANCE;


    public WebServiceBack(String[] allowedCorsOrigins) {
        INSTANCE = this;
        STRICT_MAPPER = new ObjectMapper();
        RESPONSE_MAPPER = new ObjectMapper();
        RESPONSE_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);

        roomList = new HashMap<>();
        // Jetty and Socket.io init
        this.mServer = new Server(new InetSocketAddress(3200));
        eioOptions = EngineIoServerOptions.newFromDefault();
        eioOptions.setAllowedCorsOrigins(allowedCorsOrigins);
        this.mEngineIoServer = new EngineIoServer(eioOptions);
        this.mSocketIoServer = new SocketIoServer(mEngineIoServer);

        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        final var servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addFilter(RemoteAddrFilter.class, "/socket.io/*", EnumSet.of(DispatcherType.REQUEST));


        servletContextHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                mEngineIoServer.handleRequest(new HttpServletRequestWrapper(request) {
                    @Override
                    public boolean isAsyncSupported() {
                        return false;
                    }
                }, response);
            }
        }), "/socket.io/*");

        try {
            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(servletContextHandler);
            webSocketUpgradeFilter.addMapping(
                    new ServletPathSpec("/socket.io/*"),
                    (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(mEngineIoServer));
        } catch (ServletException ex) {
            ex.printStackTrace();
        }

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { servletContextHandler });
        mServer.setHandler(handlerList);
    }

    public void startServer() throws Exception {
        mServer.start();
    }

    public void stopServer() throws Exception {
        mServer.stop();
    }

    public void initializeListeners() {
        final var mainNamespace = mSocketIoServer.namespace("/");
        mainNamespace.on("connection", arguments -> {
            SocketIoSocket socket = (SocketIoSocket) arguments[0];
            System.out.println("Client " + socket.getId() + " (" + socket.getInitialHeaders().get("remote_addr") + ") has connected.");

            socket.on("message", msgArgs -> {
                System.out.println("[Client " + socket.getId() + "] " + msgArgs);
                socket.send("message", "test message", 1);
            });

            socket.on("createRoom", msgArgs -> {
                try {
                    final var createRoomRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateRoomRequestPacket.class);
                    log.info("[Client %s] requested room creation".formatted(socket.getId()));
                    Room newRoom = new Room(socket.getId());
                    // Сохраняем новую СВОЙ ОБЪЕКТ комнаты в список комнат
                    WebServiceBack.INSTANCE.roomList().put(newRoom.roomId(), newRoom);

                    final var responseObj = CreateRoomResponsePacket.builder()
                            .name(createRoomRequest.roomCreatorName())
                            .roomId(newRoom.roomId())
                            .userId(socket.getId())
                            .build();
                    socket.send("createRoom", dataToJson(responseObj));
                    log.info("[Client %s] recived the room %s".formatted(socket.getId(), responseObj.getRoomId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("joinRoom", msgArgs -> {
                try {
                    final var joinRoomRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), JoinRoomRequestPacket.class);
                    log.info("[Client %s] requested to join the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    final var responseObj = JoinRoomStatusPacket.builder()
                            .message("Room doesn't exist yet or expired")
                            .status(false);


                    if (roomList.containsKey(joinRoomRequest.roomId())) {
                        responseObj.status(true);
                        responseObj.message("Success");
                        // Присоединяем в своих комнатах
                        roomList.get(joinRoomRequest.roomId()).addMember(new RoomMember(joinRoomRequest.name(), socket.getId(), joinRoomRequest.useVideo(), joinRoomRequest.useAudio()));
                        // Присоединяем к сокет комнате
                        socket.joinRoom(joinRoomRequest.roomId().toString());
                        // Отправляем данные всем в комнате кроме самого клиента
                        socket.broadcast(joinRoomRequest.roomId().toString(), "joinRoom", msgArgs[0]);
                        log.info("[Client %s] joined the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    } else {
                        log.info("[Client %s] tried non existent room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    }

                    socket.send("joinRoom", dataToJson(responseObj.build()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // socket.on("createOffer", )
        });
    }

   /* private void initializeSocket() {
        final var socketIOConfig = new Configuration();
        socketIOConfig.setPort(3200);
        socketIOServer = new SocketIOServer(socketIOConfig);

        socketIOServer.addEventListener("createRoom", CreateRoomRequestPacket.class, (client, data, ackSender) -> {
            log.info("hui");
            log.info("PavelDown");
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
    }*/

//TODO: start_call delete and offers naoborot
}
