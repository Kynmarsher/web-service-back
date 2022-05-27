package io.github.kynmarsher.webserviceback.socketio.room;

import java.util.UUID;

public record JoinRoomRequestPacket(String name, UUID roomId, boolean useVideo, boolean useAudio) {
}
