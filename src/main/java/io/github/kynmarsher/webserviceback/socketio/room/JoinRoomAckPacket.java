package io.github.kynmarsher.webserviceback.socketio.room;

public record JoinRoomAckPacket(boolean exists, String sessionId, boolean isAdmin) {
}
