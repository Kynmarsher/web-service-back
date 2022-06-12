package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record IceCandidatePacket(String roomId, String iceFromId, JsonNode iceCandidateBody) {
}
