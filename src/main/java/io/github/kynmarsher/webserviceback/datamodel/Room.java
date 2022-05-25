package io.github.kynmarsher.webserviceback.datamodel;

import java.util.*;

public class Room {
    private final UUID roomId;
    private final String adminId;
    private final Map<UUID,RoomMember> memberList;

    public Room(String clientSocketId) {
        roomId = UUID.randomUUID();
        memberList = new HashMap<>();
        adminId = clientSocketId;
    }

    public UUID roomId() {
        return roomId;
    }

    public void addMember(RoomMember member) {
        memberList.put(member.getMemberId(), member);
    }

    public RoomMember getMember(UUID memberUUID) {
        return memberList.get(memberUUID);
    }
}
