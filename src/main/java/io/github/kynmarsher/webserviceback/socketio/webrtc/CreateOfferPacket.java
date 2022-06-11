package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record CreateOfferPacket(String roomId, String offerFrom, String name, String offerTo, JsonNode offerBody) {
}
