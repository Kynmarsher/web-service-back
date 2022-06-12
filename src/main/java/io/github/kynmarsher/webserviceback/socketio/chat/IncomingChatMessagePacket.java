package io.github.kynmarsher.webserviceback.socketio.chat;

public record IncomingChatMessagePacket(String roomId, String fromId, String name, String message) {
}
