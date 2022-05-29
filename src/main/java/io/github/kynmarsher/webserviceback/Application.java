package io.github.kynmarsher.webserviceback;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args){
        WebServiceBack server = new WebServiceBack(null);
        try {
            server.startServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        server.initializeListeners();
    }
}
