package io.github.kynmarsher.webserviceback.httpdata;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;
// POJO
@Data
@Builder
public class CreateRoomResponse {
    public String name;
    public UUID userId;
    public UUID roomId;
}

