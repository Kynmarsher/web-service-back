package io.github.kynmarsher.webserviceback.httpdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateRoomRequest {
    public String roomCreatorName;
}
