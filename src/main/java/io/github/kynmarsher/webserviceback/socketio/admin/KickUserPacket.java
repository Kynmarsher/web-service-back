package io.github.kynmarsher.webserviceback.socketio.admin;

public record KickUserPacket(String roomId, String userId, String adminSecret, String userToKick) {
}
