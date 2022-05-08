package io.github.kynmarsher.webserviceback.socketio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAnswerObject {
    private UUID roomId;
    private UUID answerTo;
    private String answer;
}
