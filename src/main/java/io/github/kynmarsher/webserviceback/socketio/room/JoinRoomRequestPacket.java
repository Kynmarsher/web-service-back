package io.github.kynmarsher.webserviceback.socketio.room;

public record JoinRoomRequestPacket(String roomId, String name, boolean useVideo, boolean useAudio) {
}
