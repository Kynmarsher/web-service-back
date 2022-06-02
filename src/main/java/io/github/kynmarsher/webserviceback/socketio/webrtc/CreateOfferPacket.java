package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

// @JsonPropertyOrder({"roomId", "offerFrom", "offerBody"})
public record CreateOfferPacket(String roomId, String offerFrom, @JsonRawValue String offerBody) {
}
