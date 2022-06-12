package io.github.kynmarsher.webserviceback.socketio.room;

public record StartCallPacket(String roomId, String userId, String name) {
}
