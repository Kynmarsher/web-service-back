package io.github.ryrys202.videovstrechi.datamodel;

import io.github.ryrys202.videovstrechi.util.Utils;

import java.util.*;

public class Room {
    private final String roomId;
    private final String adminId;
    private final Map<String,RoomMember> memberList;

    public Room(String clientSocketId) {
        roomId = Utils.nanoId();
        memberList = new HashMap<>();
        adminId = clientSocketId;
    }

    public String roomId() {
        return roomId;
    }

    public void addMember(RoomMember member) {
        memberList.put(member.memberId(), member);
    }

    public RoomMember getMember(String memberUUID) {
        return memberList.get(memberUUID);
    }
}
