package io.github.ryrys202.videovstrechi.network.chat;

public record IncomingChatMessagePacket(String roomId, String userId, String name, String message) {
}
