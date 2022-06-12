package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.databind.JsonNode;

public record CreateOfferTranslatePacket(String roomId, String name, String offerFromId, JsonNode offerBody) {
}
