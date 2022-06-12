package io.github.kynmarsher.webserviceback.datamodel;

import io.github.kynmarsher.webserviceback.util.Utils;

import java.util.*;

public class Room {
    private final String roomId;
    private String adminId = null;
    private final Map<String, RoomMember> memberList;

    public Room() {
        roomId = Utils.roomId();
        memberList = new HashMap<>();
    }

    public String roomId() {
        return roomId;
    }

    public String adminId() {
        return adminId;
    }

    public void addMember(RoomMember member) {
        memberList.put(member.userId(), member);
    }

    public RoomMember getMember(String memberId) {
        return memberList.get(memberId);
    }

    public boolean isMember(String memberId) {
        return memberList.containsKey(memberId);
    }

    public boolean isAdminClaimable() {
        System.out.println(adminId);
        System.out.println(memberList);
        return adminId == null;
    }

    public void claimAdmin(String userId) {
        adminId = userId;
    }
}
