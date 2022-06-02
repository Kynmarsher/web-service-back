package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record OfferAnswerPacket(String roomId, String answerTo, JsonNode answerBody) {
}
