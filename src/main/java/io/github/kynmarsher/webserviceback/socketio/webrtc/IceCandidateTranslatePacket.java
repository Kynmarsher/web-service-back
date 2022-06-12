package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record IceCandidateTranslatePacket(String roomId, String fromId, JsonNode iceCandidate) {
}
