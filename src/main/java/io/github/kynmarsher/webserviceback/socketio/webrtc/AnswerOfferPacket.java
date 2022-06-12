package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record AnswerOfferPacket(String roomId, String answerFromId, String answerToId, JsonNode answerBody) {
}
