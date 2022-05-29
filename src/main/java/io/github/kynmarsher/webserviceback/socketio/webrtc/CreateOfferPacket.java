package io.github.kynmarsher.webserviceback.socketio.webrtc;

public record CreateOfferPacket(String roomId, String offerFrom, String offerBody) {
}
