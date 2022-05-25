package io.github.kynmarsher.webserviceback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.kynmarsher.webserviceback.datamodel.Room;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoServer;
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
    private @Getter final SocketIoServer mSocketIoServer;
    private final Server mServer;

    public static WebServiceBack INSTANCE;


    public WebServiceBack() {
        INSTANCE = this;
        STRICT_MAPPER = new ObjectMapper();
        RESPONSE_MAPPER = new ObjectMapper();
        RESPONSE_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);

        roomList = new HashMap<>();
        // Jetty and Socket.io init
        this.mServer = new Server(new InetSocketAddress(3200));
        this.mEngineIoServer = new EngineIoServer();
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
    }
//    TODO start_call delete and offers naoborot
}
