package io.github.kynmarsher.webserviceback.datamodel;

import io.github.kynmarsher.webserviceback.util.Utils;
import io.socket.socketio.server.SocketIoSocket;

public record RoomMember(String name, String userId, String socketId, boolean video, boolean audio) {
}
