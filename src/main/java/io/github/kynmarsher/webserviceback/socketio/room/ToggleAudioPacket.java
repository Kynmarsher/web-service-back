package io.github.kynmarsher.webserviceback.socketio.room;

public record ToggleAudioPacket(String roomId, String userId, Boolean activeAudio) {
}
