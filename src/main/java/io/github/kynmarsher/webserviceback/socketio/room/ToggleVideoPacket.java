package io.github.kynmarsher.webserviceback.socketio.room;

public record ToggleVideoPacket(String roomId, String userId, Boolean activeVideo) {
}
