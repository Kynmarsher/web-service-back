package io.github.kynmarsher.webserviceback.socketio.room;

import java.util.UUID;

public record JoinRoomRequestPacket(String roomId, String userId, String name, boolean useVideo, boolean useAudio) {
}
