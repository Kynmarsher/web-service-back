package io.github.kynmarsher.webserviceback.socketio.chat;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"roomId", "userId", "message"})
public record IncomingChatMessagePacket(String roomId, String userId, String message) {
}
