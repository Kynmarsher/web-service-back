package io.github.kynmarsher.webserviceback.socketio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferObject {
    private String roomId;
    private UUID offerFrom;
    private String offer;
}
