package io.github.kynmarsher.webserviceback.socketio.room;

public record JoinRoomBroadcastPacket(String roomId, String userId, String name, boolean isAdmin, boolean useVideo, boolean useAudio) {
}
