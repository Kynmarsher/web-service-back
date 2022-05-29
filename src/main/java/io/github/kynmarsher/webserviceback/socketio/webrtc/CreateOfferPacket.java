package io.github.kynmarsher.webserviceback.socketio.webrtc;

import java.util.UUID;

public record CreateOfferPacket(String roomId, String offerFrom, String offerBody) {
}
