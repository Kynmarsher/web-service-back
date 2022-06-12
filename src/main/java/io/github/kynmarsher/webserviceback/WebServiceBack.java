package io.github.kynmarsher.webserviceback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.github.kynmarsher.webserviceback.datamodel.RoomMember;
import io.github.kynmarsher.webserviceback.socketio.chat.IncomingChatMessagePacket;
import io.github.kynmarsher.webserviceback.socketio.room.*;
import io.github.kynmarsher.webserviceback.socketio.webrtc.*;
import io.github.kynmarsher.webserviceback.util.Utils;
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
    public Map<String, RoomMember> sessionList;

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
        final var adminNamespace = mSocketIoServer.namespace("admin");
        adminNamespace.on("connection", arguments -> {
            SocketIoSocket socket = (SocketIoSocket) arguments[0];

            socket.on("kickUser", msgArgs -> {
                try {
                    final var kickUserPacket = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), KickUserPacket.class);
                    var responseObj = new GenericAnswerPacket(false, socket.getId(), "No such user");

                    if (socket.getId().equals(roomList.get(kickUserPacket.roomId()).adminId())) {
                        responseObj = new GenericAnswerPacket(true, socket.getId(), "User is kicked");
                        socket.send("kickUser", msgArgs[0]);
                    }
                    if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                        callback.sendAcknowledgement(dataToJson(responseObj));
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            });
        });
        mainNamespace.on("connection", arguments -> {
            SocketIoSocket socket = (SocketIoSocket) arguments[0];
            log.info("[Socket %s] connected".formatted(socket.getId()));

            socket.on("createRoom", msgArgs -> {
                try {
                    // final var createRoomRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateRoomRequestPacket.class);
                    log.info("[Socket %s] requested room creation".formatted(socket.getId()));
                    final var newRoom = new Room();

                    roomList.put(newRoom.roomId(), newRoom);

                    final var responseObj = new CreateRoomResponsePacket(newRoom.roomId());

                    if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                        callback.sendAcknowledgement(dataToJson(responseObj));
                        log.info("[Socket %s] received the room %s".formatted(socket.getId(), responseObj.roomId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("joinRoom", msgArgs -> {
                try {
                    final var joinRoomRequest = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), JoinRoomRequestPacket.class);
                    log.info("[Client %s] requested to join the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    // ACK ответ на случай если комнаты не существует
                    var ackPacket = new JoinRoomAckPacket(false, "none", false);

                    // Комната существует
                    if (roomList.containsKey(joinRoomRequest.roomId())) {
                        // ACK ответ на существование комнаты
                        ackPacket = new JoinRoomAckPacket(true,
                                Utils.sessionId(),
                                roomList.get(joinRoomRequest.roomId()).isAdminClaimable());

                        // Создаем нового пользователя
                        final var newMember = new RoomMember(joinRoomRequest.name(),
                                socket.getId(),
                                joinRoomRequest.useVideo(),
                                joinRoomRequest.useAudio());

                        // Добавляем пользователя в сессию
                        sessionList.put(ackPacket.sessionId(), newMember);
                        // Присоединяем его сокет к комнате как и самого пользователя
                        roomList.get(joinRoomRequest.roomId()).addMember(newMember);
                        socket.joinRoom(joinRoomRequest.roomId());
                        // Отправляем данные всем в комнате кроме самого клиента
                        // Создаем пакет для трансляции
                        final var broadcastPacket = new JoinRoomBroadcastPacket(joinRoomRequest.roomId(), newMember.userId(), newMember.name(), newMember.video(), newMember.audio());
                        // Отправляем
                        socket.broadcast(joinRoomRequest.roomId(), "joinRoom", dataToJson(broadcastPacket));
                    } else {
                        log.info("[Client %s] tried non existent room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    }

                    if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                        callback.sendAcknowledgement(dataToJson(ackPacket));
                        log.info("[Client %s] joined the room %s".formatted(socket.getId(), joinRoomRequest.roomId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // WebRTC
            socket.on("createOffer", msgArgs -> {
                try {
                    final var offerPacket = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), CreateOfferPacket.class);
                    final var userOfferFrom = Optional.of(sessionList.get(offerPacket.sessionId()));
                    userOfferFrom.ifPresent(user -> {
                        // Отправляем пакет конкретному пользователю
                        log.info("[Socket %s] [Room %s] %s sent offer to client %s".formatted(socket.getId(), offerPacket.roomId(), user.userId(), offerPacket.offerToId()));

                        // Найдем айди его сокета по user id, TODO: свернуть в одну функцию?
                        final var socketId = roomList.get(offerPacket.roomId()).getMember(offerPacket.offerToId()).socketId();
                        // Отправим ему сообщение
                        Utils.userBySocketId(mainNamespace, offerPacket.roomId(), socketId).ifPresentOrElse(foundSocket -> {
                            final var offerTranslate = new CreateOfferTranslatePacket(offerPacket.roomId(), offerPacket.name(), user.userId(), offerPacket.offerBody());
                            foundSocket.send("createOffer", dataToJson(offerTranslate));
                        }, () -> log.warn("No socket for requested user %s".formatted(socketId)));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // WebRTC
            socket.on("answerOffer", msgArgs -> {
                try {
                    final var answerPacket = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), AnswerOfferPacket.class);
                    final var userAnswerFrom = Optional.of(sessionList.get(answerPacket.sessionId()));
                    userAnswerFrom.ifPresent(user -> {
                        log.info("[Socket %s] [Room %s] User %s sent answer to %s".formatted(socket.getId(), answerPacket.roomId(), userAnswerFrom, answerPacket.answerToId()));
                        // Найдем айди его сокета по user id, TODO: свернуть в одну функцию?
                        final var socketId = roomList.get(answerPacket.roomId()).getMember(answerPacket.answerToId()).socketId();
                        Utils.userBySocketId(mainNamespace, answerPacket.roomId(), socketId).ifPresentOrElse(foundSocket -> {
                            final var answerTranslate = new AnswerOfferTranslatePacket(answerPacket.roomId(), user.userId(), answerPacket.answerBody());
                            foundSocket.send("createOffer", dataToJson(answerTranslate));
                        }, () -> log.warn("No socket for requested user %s".formatted(socketId)));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // WebRTC
            socket.on("iceCandidate", msgArgs -> {
                try {
                    final var iceCandidatePacket = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), IceCandidatePacket.class);
                    final var userIceFrom = Optional.of(sessionList.get(iceCandidatePacket.sessionId()));
                    userIceFrom.ifPresent(user -> {
                        final var iceTranslatePacket = new IceCandidateTranslatePacket(iceCandidatePacket.roomId(), user.userId(), iceCandidatePacket.iceCandidate());
                        socket.broadcast(iceCandidatePacket.roomId(), "iceCandidate", dataToJson(iceTranslatePacket));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            socket.on("chatMessage", msgArgs -> {
                try {
                    final var chatMsg = WebServiceBack.STRICT_MAPPER.readValue(msgArgs[0].toString(), IncomingChatMessagePacket.class);
                    var responseObj = new GenericAnswerPacket(true, chatMsg.userId(), "Success");

                    if ( chatMsg.message() != null && chatMsg.message().length() <= 128 ) {
                        socket.broadcast(chatMsg.roomId(), "chatMessage", msgArgs[0]);
                    } else {
                        responseObj = new GenericAnswerPacket(false, chatMsg.userId(), "Message is too big");
                    }

                    if (msgArgs[msgArgs.length - 1] instanceof SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
                        callback.sendAcknowledgement(dataToJson(responseObj));
                        log.info("[Client %s] Room %s msg:  %s".formatted(socket.getId(), chatMsg.roomId(), chatMsg.message()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
