package io.github.ryrys202.videovstrechi.network.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record OfferAnswerPacket(String roomId, String answerFrom, String answerTo, JsonNode answerBody) {
}
