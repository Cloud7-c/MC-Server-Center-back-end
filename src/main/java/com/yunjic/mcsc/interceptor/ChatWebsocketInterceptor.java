package com.yunjic.mcsc.interceptor;

import com.yunjic.mcsc.pojo.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

import static com.yunjic.mcsc.constant.UserConstant.CURRENT_USER_SESSION_KEY;
import static com.yunjic.mcsc.constant.UserConstant.USER_NAME_KEY;
import static com.yunjic.mcsc.constant.UserConstant.VISITOR_ROLE;

@Slf4j
public class ChatWebsocketInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (serverHttpRequest instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest request = (ServletServerHttpRequest) serverHttpRequest;
            User currentUser = (User)request.getServletRequest().getSession().getAttribute(CURRENT_USER_SESSION_KEY);
            if(currentUser == null) return false;
            log.info("chat连接请求：" + currentUser);

            if (currentUser.getRole() == VISITOR_ROLE) {
                attributes.put(USER_NAME_KEY, "visitor");
            } else {
                attributes.put(USER_NAME_KEY, currentUser.getUserName());
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
