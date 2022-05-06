package io.github.kynmarsher.webserviceback.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    private final UUID roomId;
    private final UUID adminId;
    private final List<RoomMember> memberList;

    public Room(UUID roomCreatorId) {
        roomId = UUID.randomUUID();
        memberList = new ArrayList<>();
        adminId = roomCreatorId;
    }

    public UUID roomId() {
        return roomId;
    }

    public void addMember(RoomMember member) {
        memberList.add(member);
    }
}
