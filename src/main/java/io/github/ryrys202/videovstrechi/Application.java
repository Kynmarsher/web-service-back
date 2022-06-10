package io.github.ryrys202.videovstrechi;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args){
        var server = new Videovstrechi(null);
        try {
            server.startServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        server.initializeListeners();
    }
}
