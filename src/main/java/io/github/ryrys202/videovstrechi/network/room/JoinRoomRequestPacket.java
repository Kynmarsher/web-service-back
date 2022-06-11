package io.github.ryrys202.videovstrechi.network.room;

public record JoinRoomRequestPacket(String roomId, String userId, String name, boolean useVideo, boolean useAudio) { }
