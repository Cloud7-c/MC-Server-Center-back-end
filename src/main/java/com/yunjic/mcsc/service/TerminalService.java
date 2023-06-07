package com.yunjic.mcsc.service;

import com.yunjic.mcsc.pojo.model.ChatMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.regex.Pattern;

public interface TerminalService {
    /**
     * 用于正则匹配服务器聊天信息
     */
    Pattern chatPattern = Pattern.compile("^\\[\\d\\d:\\d\\d:\\d\\d] \\[Server thread/INFO]: (\\[Not Secure] )?<([a-zA-Z]+\\w*)> (.+)$");

    /**
     * 服务器启动
     */
    String SERVER_START_MESSAGE = "%COMMAND%SERVER_START";

    /**
     * 服务器关闭
     */
    String SERVER_STOP_MESSAGE = "%COMMAND%SERVER_STOP";

    /**
     * 记录终端WebSocket session
     *
     * @param session 需要记录的WebSocket session
     * @throws IOException IO异常
     */
    void terminalRegister(WebSocketSession session) throws IOException;

    /**
     * 记录聊天WebSocket session
     *
     * @param session 需要记录的WebSocket session
     * @throws IOException IO异常
     */
    void chatRegister(WebSocketSession session) throws IOException;

    /**
     * 终端WebSocket断开连接时执行的操作
     *
     * @param session 断开连接的WebSocket session
     */
    void close(WebSocketSession session);

    /**
     * 聊天WebSocket断开连接时执行的操作
     *
     * @param session 断开连接的WebSocket session
     */
    void closeChat(WebSocketSession session);

    /**
     *
     * @return Minecraft服务器是否正在运行
     */
    boolean getServerState();

    /**
     * 启动Minecraft服务器
     *
     * @return 是否能够执行启动命令
     */
    boolean startMinecraftServer() throws IOException;

    /**
     * 关闭Minecraft服务器
     *
     * @return 是否能够执行关闭命令
     */
    boolean stopMinecraftServer() throws IOException;

    /**
     * 执行命令
     *
     * @param userName 执行命令的用户
     * @param command 要执行的命令
     * @return 是否能够执行命令
     * @throws IOException IO异常
     */
    boolean executeCommand(String userName, String command) throws IOException;

    /**
     * 向所有终端连接发送消息
     *
     * @param buffer 需要返回给前端的字符串的字节流
     * @throws IOException IO异常
     */
    void broadcastMessage(byte[] buffer) throws IOException;

    /**
     * 向所有终端连接发送消息
     *
     * @param message 要发送的信息
     * @throws IOException IO异常
     */
    void broadcastMessage(String message) throws IOException;

    /**
     * 向所有聊天连接发送消息
     *
     * @param chatMessage 聊天信息
     * @throws IOException IO异常
     */
    void broadcastChat(ChatMessage chatMessage) throws IOException;

    /**
     * 处理服务器原始信息，并向所有聊天连接发送消息
     *
     * @param serverMessage 服务器原始信息
     * @throws IOException IO异常
     */
    void broadcastChat(String serverMessage) throws IOException;

    /**
     * 处理网页发送的信息，并向所有聊天连接发送消息
     *
     * @param userName 发送聊天信息的用户名
     * @param message 发送的信息
     * @throws IOException IO异常
     */
    void broadcastChat(String userName, String message) throws IOException;

    /**
     * 向指定终端连接返回消息
     *
     * @param session 要发送到的WebSocketSession
     * @param buffer 需要发送的字符串的字节流
     * @throws IOException IO异常
     */
    void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    /**
     * 向指定终端连接返回消息
     *
     * @param session 要发送到的WebSocketSession
     * @param message 需要发送的字符串
     * @throws IOException IO异常
     */
    void sendMessage(WebSocketSession session, String message) throws IOException;

    /**
     * 向Minecraft服务器和所有聊天连接发送聊天信息
     * 如果Minecraft服务器处于关闭状态，返回false，但仍会向网页发送信息
     *
     * @param userName 发送聊天信息的用户名
     * @param text 聊天信息的内容
     */
    boolean chat(String userName, String text);

    /**
     * 向一个chat session发送反馈信息
     *
     * @param session 要发送到的session
     * @param message 反馈信息的内容
     * @return 是否发送成功
     */
    boolean sendChatFeedback(WebSocketSession session, String message);
}
