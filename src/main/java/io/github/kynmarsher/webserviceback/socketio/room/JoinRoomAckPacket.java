package io.github.kynmarsher.webserviceback.socketio.room;

public record JoinRoomAckPacket(boolean exists, String userId, boolean isAdmin, String adminSecret) {
}
