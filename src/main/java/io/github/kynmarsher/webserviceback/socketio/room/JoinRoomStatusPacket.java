package io.github.kynmarsher.webserviceback.socketio.room;

public record JoinRoomStatusPacket(boolean status, String userId, String message) {
}
