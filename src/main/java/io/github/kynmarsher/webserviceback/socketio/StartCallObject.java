package io.github.kynmarsher.webserviceback.socketio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartCallObject {
    private UUID roomId;
    private String name;
    private boolean video;
    private boolean audio;
}
