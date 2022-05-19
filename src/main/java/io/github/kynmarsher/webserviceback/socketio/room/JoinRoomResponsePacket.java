package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRoomResponsePacket {
    private boolean status;
    private String errorMessage;
}
