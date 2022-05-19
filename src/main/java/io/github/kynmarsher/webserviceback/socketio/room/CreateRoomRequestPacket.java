package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequestPacket {
    private String roomCreatorName;
}
