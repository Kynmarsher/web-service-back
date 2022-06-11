package io.github.kynmarsher.webserviceback.socketio.room;

public record KickUserPacket(String roomId, String userToKick) {
}
