package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRoomStatusPacket {
    private boolean status;
    private String message;
}
