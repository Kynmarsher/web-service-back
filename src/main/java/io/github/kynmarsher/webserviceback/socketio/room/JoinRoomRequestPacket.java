package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequestPacket {
    private String name;
    private UUID roomId;
    private boolean useVideo;
    private boolean useAudio;
}
