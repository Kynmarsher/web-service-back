package io.github.ryrys202.videovstrechi.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record CreateOfferPacket(String roomId, String offerFrom, String name, String offerTo, JsonNode offerBody) {
}
