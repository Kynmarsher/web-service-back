package io.github.ryrys202.videovstrechi.socketio.chat;

public record IncomingChatMessagePacket(String roomId, String userId, String name, String message) {
}
