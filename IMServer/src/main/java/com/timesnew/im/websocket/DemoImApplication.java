package com.timesnew.im.websocket;

import com.timesnew.im.socket.core.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoImApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoImApplication.class, args);
        Server server = new Server();
        server.start();
    }
}
