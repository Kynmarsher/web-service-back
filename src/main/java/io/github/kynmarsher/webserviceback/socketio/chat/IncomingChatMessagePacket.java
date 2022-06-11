package io.github.kynmarsher.webserviceback.socketio.chat;

public record IncomingChatMessagePacket(String roomId, String userId, String name, String message) {
}
