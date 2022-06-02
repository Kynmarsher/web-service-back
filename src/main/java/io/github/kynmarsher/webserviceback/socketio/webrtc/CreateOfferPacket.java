package io.github.kynmarsher.webserviceback.socketio.webrtc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"roomId", "offerFrom", "offerBody"})
public record CreateOfferPacket(String roomId, String offerFrom, String offerBody) {

    @JsonIgnore
    public String offerBody() {
        return this.offerBody;
    }
}
