package io.github.ryrys202.videovstrechi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.ryrys202.videovstrechi.datamodel.Room;
import io.github.ryrys202.videovstrechi.datamodel.RoomMember;
import io.github.ryrys202.videovstrechi.network.chat.IncomingChatMessagePacket;
import io.github.ryrys202.videovstrechi.network.room.*;
import io.github.ryrys202.videovstrechi.network.webrtc.CreateOfferPacket;
import io.github.ryrys202.videovstrechi.network.webrtc.IceCandidatePacket;
import io.github.ryrys202.videovstrechi.network.webrtc.OfferAnswerPacket;
import io.github.ryrys202.videovstrechi.util.Utils;
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

@Slf4j
@Accessors(fluent = true)
public class Videovstrechi {
    public static ObjectMapper RESPONSE_MAPPER;
    public static ObjectMapper STRICT_MAPPER;
    @Getter
    public Map<String, Room> roomList;
    public Map<UUID, UUID> sessionId;

    private final EngineIoServer mEngineIoServer;
    private final EngineIoServerOptions eioOptions;
    private @Getter final SocketIoServer mSocketIoServer;
    private final Server mServer;

    public static Videovstrechi INSTANCE;


    public Videovstrechi(String[] allowedCorsOrigins) {
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
        servletContextHandler.addFilter(RemoteAddressFilter.class, "/socket.io/*", EnumSet.of(DispatcherType.REQUEST));


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

            socket.on("createRoom", msgArgs -> {
                try {
                    final var createRoomRequest = Videovstrechi.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateRoomRequestPacket.class);
                    log.info("[Client %s] requested room creation".formatted(socket.getId()));
                    Room newRoom = new Room(socket.getId());
                    Videovstrechi.INSTANCE.roomList().put(newRoom.roomId(), newRoom);

                    final var responseObj = new CreateRoomResponsePacket(newRoom.roomId(), socket.getId(), createRoomRequest.name());

                    if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                        callback.sendAcknowledgement(Utils.dataToJson(responseObj));
                        log.info("[Client %s] received the room %s".formatted(socket.getId(), responseObj.roomId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("joinRoom", msgArgs -> {
                try {
                    final var joinRoomRequest = Videovstrechi.STRICT_MAPPER.readValue(msgArgs[0].toString(), JoinRoomRequestPacket.class);
                    log.info("[Client %s] requested to join the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    var responseObj = new GenericAnswerPacket(false, socket.getId(), "Room doesn't exist yet or expired");


                    if (roomList.containsKey(joinRoomRequest.roomId())) {
                        responseObj = new GenericAnswerPacket(true, socket.getId(), "success");

                        roomList.get(joinRoomRequest.roomId()).addMember(new RoomMember(joinRoomRequest.name(),
                                socket.getId(),
                                joinRoomRequest.useVideo(),
                                joinRoomRequest.useAudio()));
                        socket.joinRoom(joinRoomRequest.roomId());

                        socket.broadcast(joinRoomRequest.roomId(), "joinRoom", msgArgs[0]);
                    } else {
                        log.info("[Client %s] tried non existent room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    }
                    if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                        callback.sendAcknowledgement(Utils.dataToJson(responseObj));
                        log.info("[Client %s] joined the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // WebRTC
            socket.on("createOffer", msgArgs -> {
                try {
                    final var offerPacket = Videovstrechi.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateOfferPacket.class);

                    log.info("[Clinet %s] [Room %s] sent offer to client %s".formatted(offerPacket.offerFrom(), offerPacket.roomId(), offerPacket.offerTo()));
                    var clientOpt = Arrays.stream(mainNamespace.getAdapter().listClients(offerPacket.roomId()))
                            .filter(client -> client.getId().equals(offerPacket.offerTo()))
                            .reduce((a, b) -> null);
                    clientOpt.ifPresentOrElse(client -> client.send("createOffer", msgArgs[0]), () ->
                            log.info("[Clinet %s] don't know %s".formatted(socket.getId(), offerPacket.offerTo())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // WebRTC
            socket.on("answerOffer", msgArgs -> {
                try {
                    final var offerAnswer = Videovstrechi.STRICT_MAPPER.readValue(msgArgs[0].toString(), OfferAnswerPacket.class);
                    log.info("[Clinet %s] [Room %s] sent answer to %s".formatted(offerAnswer.answerFrom(), offerAnswer.roomId(), offerAnswer.answerTo()));
                    var clientOpt = Arrays.stream(mainNamespace.getAdapter().listClients(offerAnswer.roomId()))
                            .filter(client -> client.getId().equals(offerAnswer.answerTo()))
                            .reduce((a, b) -> null);
                    clientOpt.ifPresentOrElse(client -> client.send("answerOffer", msgArgs[0]), () -> {
                        log.info("[Clinet %s] don't know %s".formatted(socket.getId(), offerAnswer.answerTo()));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // WebRTC
            socket.on("iceCandidate", msgArgs -> {
                try {
                    final var iceCandidatePacket = Videovstrechi.STRICT_MAPPER.readValue(msgArgs[0].toString(), IceCandidatePacket.class);
                    socket.broadcast(iceCandidatePacket.roomId(), "iceCandidate", msgArgs[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("chatMessage", msgArgs -> {
                try {
                    final var chatMsg = Videovstrechi.STRICT_MAPPER.readValue(msgArgs[0].toString(), IncomingChatMessagePacket.class);
                    if ( chatMsg.message() != null && chatMsg.message().length() <= 128 ) {
                        socket.broadcast(chatMsg.roomId(), "chatMessage", msgArgs[0]);
                        if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                            final var responseObj = new GenericAnswerPacket(true, chatMsg.userId(), "Success");
                            callback.sendAcknowledgement(Utils.dataToJson(responseObj));
                            log.info("[Client %s] Room %s msg:  %s".formatted(socket.getId(), chatMsg.roomId(), chatMsg.message()));
                        }
                    } else {
                        if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                            final var responseObj = new GenericAnswerPacket(false, chatMsg.userId(), "Message is too big");
                            callback.sendAcknowledgement(Utils.dataToJson(responseObj));
                            log.info("[Client %s] Sent message too big".formatted(socket.getId()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
