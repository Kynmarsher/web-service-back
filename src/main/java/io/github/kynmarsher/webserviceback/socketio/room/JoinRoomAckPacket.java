package io.github.kynmarsher.webserviceback.socketio.room;

public record JoinRoomAckPacket(boolean roomStatus, String userId, boolean isAdmin, String message) {
}
