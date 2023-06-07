package com.yunjic.mcsc.controller;

import com.yunjic.mcsc.service.TerminalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import static com.yunjic.mcsc.constant.UserConstant.USER_NAME_KEY;

/**
 * 用于响应Web终端的WebSocket处理器类
 *
 * @author yunji
 */

@Component
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {
    @Autowired
    TerminalService terminalService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        terminalService.chatRegister(session);
        log.info(session.getAttributes().get(USER_NAME_KEY) + " 已连接到chat");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            if(session.getAttributes().get(USER_NAME_KEY).equals("visitor")){
                log.info("收到游客终端信息：" + message.getPayload());
                terminalService.sendChatFeedback(session, "【游客账号仅作演示用，无实际权限】");
                return;
            }
            //调用service发送聊天信息
            if(!terminalService.chat(
                    (String) session.getAttributes().get(USER_NAME_KEY),
                    ((TextMessage) message).getPayload()
            )){
                session.sendMessage(new TextMessage("Minecraft服务器未开启"));
            }
        } else if (message instanceof BinaryMessage) {

        } else if (message instanceof PongMessage) {

        } else {
            log.info("Unexpected WebSocket message type: " + message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("数据传输错误");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info(session.getAttributes().get(USER_NAME_KEY) + " 断开了chat连接");
        terminalService.closeChat(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
