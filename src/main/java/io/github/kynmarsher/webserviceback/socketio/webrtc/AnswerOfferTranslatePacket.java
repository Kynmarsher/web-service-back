package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record AnswerOfferTranslatePacket(String roomId, String answerFromId, JsonNode answerBody) {
}
