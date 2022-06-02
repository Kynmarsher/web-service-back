package io.github.kynmarsher.webserviceback.socketio.room;

public record ChatMessagePacket(String roomId, String authorId, String message) {
}
