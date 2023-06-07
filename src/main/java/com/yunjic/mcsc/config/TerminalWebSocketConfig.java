package com.yunjic.mcsc.config;

import com.yunjic.mcsc.controller.TerminalWebSocketHandler;
import com.yunjic.mcsc.interceptor.TerminalWebsocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 用于Web端显示终端的WebSocket配置
 *
 * @author yunji
 */
@Configuration
@EnableWebSocket
public class TerminalWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    TerminalWebSocketHandler terminalWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(terminalWebSocketHandler, "/ws/terminal")
                .addInterceptors(new TerminalWebsocketInterceptor())
                .setAllowedOrigins("*");
    }
}
