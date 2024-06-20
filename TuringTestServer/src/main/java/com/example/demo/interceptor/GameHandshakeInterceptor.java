package com.example.demo.interceptor;

import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GameHandshakeInterceptor implements HandshakeInterceptor{

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse
            response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        //将用户id放入socket处理器的会话(WebSocketSession)中
        ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
        //获取参数
        // String userId = serverHttpRequest.getServletRequest().getParameter("uid");
        HttpHeaders headers = serverHttpRequest.getHeaders();
        List<String> header = headers.get("uid");
        String userId = header.get(0);
        attributes.put("uid", userId);
        //可以在此处进行认证与授权操作，当用户权限验证通过后，进行握手成功操作，验证失败返回false
        // if (!userId.equals("caozz")) {
        //     System.out.println("握手失败!");
        //     return false;
        // }
        // System.out.println("开始握手-");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse
            response, WebSocketHandler wsHandler, Exception exception) {
        // System.out.println("握手成功-");
    }
}
