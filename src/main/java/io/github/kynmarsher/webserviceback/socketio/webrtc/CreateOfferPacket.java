package io.github.kynmarsher.webserviceback.socketio.webrtc;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;

@JsonPropertyOrder({"roomId", "offerFrom", "offerTo", "name", "offerBody"})
public record CreateOfferPacket(String roomId, String offerFrom, String name, String offerTo, JsonNode offerBody) {
}
