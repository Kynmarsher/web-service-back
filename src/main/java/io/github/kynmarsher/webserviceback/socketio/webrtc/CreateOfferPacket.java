package io.github.kynmarsher.webserviceback.socketio.webrtc;

import java.util.UUID;

public record CreateOfferPacket(UUID roomId, String offerFrom, String offerBody) {
}
