package io.github.kynmarsher.webserviceback.socketio.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record CreateRoomRequestPacket(String roomCreatorName) {

}
