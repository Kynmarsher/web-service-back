package io.github.ryrys202.videovstrechi.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record IceCandidatePacket(String roomId, String from, JsonNode iceCandidate) {
}
