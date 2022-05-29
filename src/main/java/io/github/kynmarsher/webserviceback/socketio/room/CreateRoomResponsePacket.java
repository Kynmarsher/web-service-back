package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

public record CreateRoomResponsePacket(String roomId, String userId, String name) {
}
