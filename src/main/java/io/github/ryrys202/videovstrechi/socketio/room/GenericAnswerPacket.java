package io.github.ryrys202.videovstrechi.socketio.room;

public record GenericAnswerPacket(boolean status, String userId, String message) {}
