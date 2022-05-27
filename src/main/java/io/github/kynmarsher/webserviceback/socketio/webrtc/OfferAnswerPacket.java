package io.github.kynmarsher.webserviceback.socketio.webrtc;

import java.util.UUID;

public record OfferAnswerPacket(UUID roomId, String answerTo, String answerBody) {
}
