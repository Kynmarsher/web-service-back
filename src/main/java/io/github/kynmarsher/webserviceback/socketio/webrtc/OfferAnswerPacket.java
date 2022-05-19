package io.github.kynmarsher.webserviceback.socketio.webrtc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAnswerPacket {
    private UUID roomId;
    private UUID answerTo;
    private String answerBody;
}
