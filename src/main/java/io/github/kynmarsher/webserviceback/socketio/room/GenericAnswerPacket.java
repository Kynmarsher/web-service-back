package io.github.kynmarsher.webserviceback.socketio.room;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"status", "userId", "message"})
public record GenericAnswerPacket(boolean status, String userId, String message) {

}
