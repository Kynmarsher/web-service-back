package io.github.kynmarsher.webserviceback.socketio.room;

public record DeleteUserPacket(String roomId, String userToDelete) {
}
