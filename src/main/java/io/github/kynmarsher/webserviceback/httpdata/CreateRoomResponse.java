package io.github.kynmarsher.webserviceback.httpdata;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateRoomResponse {
    private String name;
    private UUID userId;
    private UUID roomId;
}

