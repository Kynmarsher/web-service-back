package io.github.kynmarsher.webserviceback.datamodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class RoomMember {
    public String name;
    public UUID memberId;
    public boolean video;
    public boolean audio;
}
