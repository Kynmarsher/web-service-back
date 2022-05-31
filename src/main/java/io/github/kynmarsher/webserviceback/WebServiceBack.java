package io.github.kynmarsher.webserviceback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.datamodel.RoomMember;
import io.github.kynmarsher.webserviceback.socketio.room.*;
import io.github.kynmarsher.webserviceback.socketio.webrtc.CreateOfferPacket;
import io.github.kynmarsher.webserviceback.socketio.webrtc.OfferAnswerPacket;
import io.socket.engineio.server.Emitter;
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
import java.util.*;

import static io.github.kynmarsher.webserviceback.util.Utils.dataToJson;

@Slf4j
@Accessors(fluent = true)
public class WebServiceBack {
    // Эти поля - мапперы которые конвертируют объект в JSON и обратно в объект
    public static ObjectMapper RESPONSE_MAPPER;
    public static ObjectMapper STRICT_MAPPER;
    @Getter
    public Map<String, Room> roomList;
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
            log.info("[Client %s] connected".formatted(socket.getId()));

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

                    final var responseObj = new CreateRoomResponsePacket(newRoom.roomId(), socket.getId(), createRoomRequest.name());
                    socket.send("createRoom", dataToJson(responseObj));
                    log.info("[Client %s] received the room %s".formatted(socket.getId(), responseObj.roomId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("joinRoom", msgArgs -> {

                try {
                    final var joinRoomRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), JoinRoomRequestPacket.class);
                    log.info("[Client %s] requested to join the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    var responseObj = new JoinRoomStatusPacket(false, socket.getId(), "Room doesn't exist yet or expired");


                    if (roomList.containsKey(joinRoomRequest.roomId())) {
                        responseObj = new JoinRoomStatusPacket(true, socket.getId(), "success");
                        // Присоединяем в своих комнатах
                        roomList.get(joinRoomRequest.roomId()).addMember(new RoomMember(joinRoomRequest.name(), socket.getId(), joinRoomRequest.useVideo(), joinRoomRequest.useAudio()));
                        // Присоединяем к сокет комнате
                        socket.joinRoom(joinRoomRequest.roomId());
                        // Отправляем данные всем в комнате кроме самого клиента
                        socket.broadcast(joinRoomRequest.roomId(), "joinRoom", msgArgs[0]);
                        log.info("[Client %s] joined the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                        socket.send("joinRoom",dataToJson(responseObj));
                    } else {
                        log.info("[Client %s] tried non existent room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("roomInfo", msgArgs -> {
                try {
                    final var roomInfoRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), RoomInfoRequestPacket.class);
                    log.info("[Client %s] requested to data about room %s".formatted(socket.getId(), roomInfoRequest.roomId()));
                    final var responseObj = new RoomInfoResponsePacket(roomList.containsKey(roomInfoRequest.roomId()));
                    socket.send("roomInfo", dataToJson(responseObj));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("createOffer", msgArgs -> {
                try {
                    final var offerPacket = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateOfferPacket.class);
                    // Send Create offer to everyone in the room
                    socket.broadcast(offerPacket.roomId(), "createOffer", msgArgs[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("answerOffer", msgArgs -> {
                try {
                    final var offerAnswer = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), OfferAnswerPacket.class);
                    Optional<SocketIoSocket> clientOpt = Arrays.stream(mainNamespace.getAdapter().listClients(offerAnswer.roomId()))
                            .filter(client -> client.getId().equals(offerAnswer.answerTo()))
                            .reduce((a, b) -> null);

                    clientOpt.ifPresentOrElse(client -> client.send("answerOffer", msgArgs[0]), () -> { throw new RuntimeException(); });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
