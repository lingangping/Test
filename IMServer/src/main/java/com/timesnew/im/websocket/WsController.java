package com.timesnew.im.websocket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WsController {
    @RequestMapping("/websocket")
    public String webSocket() {
            return "chat";
    }
}
