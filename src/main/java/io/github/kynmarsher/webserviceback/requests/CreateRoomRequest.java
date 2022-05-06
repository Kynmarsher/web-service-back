package io.github.kynmarsher.webserviceback.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CreateRoomRequest {
    public String roomCreatorName;
}
