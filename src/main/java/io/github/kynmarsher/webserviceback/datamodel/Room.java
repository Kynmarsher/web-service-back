package io.github.kynmarsher.webserviceback.datamodel;

import com.devskiller.friendly_id.FriendlyId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room {
    private final UUID roomId;
    private final String adminId;
    private final List<RoomMember> memberList;

    public Room(String roomCreatorId) {
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
