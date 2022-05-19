package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateRoomResponsePacket {
    private String name;
    private UUID userId;
    private UUID roomId;
}
