package io.github.kynmarsher.webserviceback.socketio.room;

import java.util.UUID;

public record JoinRoomRequestPacket(String name, String memberId, UUID roomId, boolean useVideo, boolean useAudio) {
}
