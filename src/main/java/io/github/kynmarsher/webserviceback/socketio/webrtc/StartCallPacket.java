package io.github.kynmarsher.webserviceback.socketio.webrtc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartCallPacket {
    private String name;
    private UUID roomId;
    private boolean video;
    private boolean audio;
}
