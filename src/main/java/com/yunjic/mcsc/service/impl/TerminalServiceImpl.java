package com.yunjic.mcsc.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.yunjic.mcsc.common.ResponseCode;
import com.yunjic.mcsc.exception.BusinessException;
import com.yunjic.mcsc.pojo.MinecraftServerConfig;
import com.yunjic.mcsc.pojo.MinecraftServerConnection;
import com.yunjic.mcsc.pojo.SSHConfig;
import com.yunjic.mcsc.pojo.model.ChatMessage;
import com.yunjic.mcsc.service.TerminalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

import static com.yunjic.mcsc.constant.UserConstant.USER_NAME_KEY;

@Service
@Slf4j
public class TerminalServiceImpl implements TerminalService {
    //SSH连接设置
    @Autowired
    SSHConfig sshConfig;
    //MC服务器设置
    @Autowired
    MinecraftServerConfig minecraftServerConfig;

    //服务器连接
    private static volatile MinecraftServerConnection connection;
    //终端WebSocket连接
    private static final List<WebSocketSession> webSocketSessions = new CopyOnWriteArrayList<>();
    //聊天WebSocket连接
    private static final List<WebSocketSession> chatSessions = new CopyOnWriteArrayList<>();

    //服务器消息缓存
    private static final Queue<String> messageList = new ConcurrentLinkedQueue<>();
    //聊天消息缓存
    private static final Queue<ChatMessage> chatList = new ConcurrentLinkedQueue<>();
    //消息缓存最大数量
    @Value("${max-message-list-size:100}")
    private int maxMessageListSize;

    //是否已设置pty大小，当第一个终端连接后该值置为true，并设置pty大小
    private static boolean ptySizeFlag = false;

    @Override
    public void terminalRegister(WebSocketSession session) throws IOException{
        if(connection == null) initConnection();

        sendMessage(session, "\u001b[1;32m连接成功！\u001b[0m\r\n");
        for(String message: messageList){
            sendMessage(session, message);
        }
        webSocketSessions.add(session);
    }

    @Override
    public void chatRegister(WebSocketSession session) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        for(ChatMessage message: chatList){
            String jsonString = mapper.writeValueAsString(message);
            sendMessage(session, jsonString);
        }
        chatSessions.add(session);
    }

    @Override
    public void close(WebSocketSession session){
        webSocketSessions.remove(session);
    }

    @Override
    public void closeChat(WebSocketSession session) {
        chatSessions.remove(session);
    }

    @Override
    public boolean getServerState() {
        if(connection == null) return false;
        return connection.serverIsRunning();
    }

    //初始化SSH连接，设置Minecraft服务器运行状态为false
    private void initConnection(){
        if(connection == null){
            synchronized (TerminalServiceImpl.class){
                if(connection == null){
                    try {
                        //初始化SSH连接
                        log.info("开始初始化SSH连接");
                        JSch jSch = new JSch();
                        Session session = jSch.getSession(sshConfig.getUserName(),sshConfig.getHost(),sshConfig.getPort());
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.setPassword(sshConfig.getPassword());
                        session.setTimeout(sshConfig.getTimeout());
                        session.setServerAliveInterval(sshConfig.getServerAliveInterval());
                        session.connect();
                        Channel channel = session.openChannel("shell");
                        ((ChannelShell)channel).setPty(true);
                        channel.connect();
                        log.info("SSH连接成功！");

                        //初始化Minecraft服务器连接信息
                        connection = new MinecraftServerConnection();
                        connection.setChannel(channel);
                        connection.setRunning(false);

                        //新建一个线程监听来自SSH连接的消息
                        startListenerThread(session, channel);
                    } catch (JSchException e) {
                        log.info("SSH连接失败");
                        throw new BusinessException(ResponseCode.MC_SERVER_CONNECTION_ERROR);
                    }
                }
            }
        }
    }

    private void startListenerThread(Session session, Channel channel){
        //新建线程监听信息
        new Thread(()->{
            log.info("建立监听线程");
            InputStream inputStream = null;
            try {
                inputStream = channel.getInputStream();
                byte[] buffer = new byte[1024];
                int i = 0;
                String data = "";
                while(true){
                    if(inputStream.available() > 0){
                        //读取channel输入流
                        i = inputStream.read(buffer);
                        //如果服务器正在运行才做处理，将输入流按行分割存入队列中并向客户端广播
                        if(connection.serverIsRunning()){
                            data = data + new String(buffer, 0, i);
                            if(data.contains("\r\n")) {
                                String[] lines = data.split("\r\n|\n");
                                for(int j = 0; j < lines.length-1; j++){
                                    //向终端连接广播
                                    broadcastMessage(lines[j] + "\r\n");
                                    //向聊天连接广播
                                    if(chatPattern.matcher(lines[j]).find()){
                                        broadcastChat(lines[j]);
                                    }
                                    log.info(lines[j]);
                                }
                                if (data.endsWith("\r\n") || data.endsWith("\n")) {
                                    //向终端连接广播
                                    broadcastMessage(lines[lines.length-1] + "\r\n");
                                    //向聊天连接广播
                                    if(chatPattern.matcher(lines[lines.length-1]).find()){
                                        broadcastChat(lines[lines.length-1]);
                                    }
                                    log.info(lines[lines.length-1]);
                                    data = "";
                                }
                                else {
                                    data = lines[lines.length - 1];
                                }
                            }
                        }
                    }
                    //轮询间隔时间
                    try {
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        log.error("Listener thread fail to sleep");
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                log.error("获取输入流失败");
                e.printStackTrace();
            } finally {
                if(connection.serverIsRunning()){
                    try {
                        stopMinecraftServer();
                    } catch (IOException e) {
                        log.info("服务器已关闭");
                    }
                }
                //断开连接后关闭会话
                log.info("断开连接");
                session.disconnect();
                channel.disconnect();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("关闭服务器连接输入流失败");
                        e.printStackTrace();
                    }
                }
            }
        }, "ConnectionListenerThread").start();
    }

    @Override
    public boolean startMinecraftServer() throws IOException{
        //若服务器已经在运行，不执行命令，直接返回
        if(connection.serverIsRunning()){
            return false;
        }
        //切换目录，根据系统的不同使用不同的命令
        log.info("OS: " + sshConfig.getOs());
        if(sshConfig.getOs().equals("linux")){
            executeServerCommand("cd " + minecraftServerConfig.getPath());
        }
        else if(sshConfig.getOs().equals("windows")){
            String[] path = minecraftServerConfig.getPath().split(":", 2);
            if(path.length != 2) return false;
            executeServerCommand(path[0] + ':');
            executeServerCommand("cd " + minecraftServerConfig.getPath());
        }
        else {
            log.error("无法识别操作系统配置");
            return false;
        }

        //拼接启动命令，包含Java虚拟机参数为空的判断
        String startCommand = minecraftServerConfig.getJavaPath() + ' '
                + ("".equals(minecraftServerConfig.getArguments()) ? "" : (minecraftServerConfig.getArguments()+' '))
                + "-jar " + minecraftServerConfig.getServer()
                + " nogui";
        executeServerCommand(startCommand);
        connection.setRunning(true);
        //广播消息
        broadcastMessage(SERVER_START_MESSAGE);
        for(WebSocketSession session : chatSessions){
            session.sendMessage(new TextMessage(SERVER_START_MESSAGE));
        }
        return true;
    }

    @Override
    public boolean stopMinecraftServer() throws IOException{
        //如果服务器没在运行，不执行命令，直接返回
        if(!connection.serverIsRunning()){
            return false;
        }

        executeServerCommand("stop");
        broadcastMessage("服务器已关闭\r\n");
        connection.setRunning(false);
        //广播消息
        broadcastMessage(SERVER_STOP_MESSAGE);
        for(WebSocketSession session : chatSessions){
            session.sendMessage(new TextMessage(SERVER_STOP_MESSAGE));
        }
        return true;
    }

    private void executeServerCommand(String command) throws IOException {
        log.info("execute server command: " + command);
        if(connection == null || connection.getChannel() == null || command == null) return;

        PrintWriter printWriter = new PrintWriter(connection.getChannel().getOutputStream());
        printWriter.println(command);
        printWriter.flush();
    }

    @Override
    public boolean executeCommand(String userName, String command) throws IOException {
        log.info(userName + " execute command: " + command);
        if(connection == null || connection.getChannel() == null) return false;
        if(command == null || command.trim().equals("")) return false;

        //判断是否是关闭服务器命令
        if(command.trim().equals("stop") || command.trim().startsWith("stop\r\n") || command.trim().startsWith("stop\n")){
            return true;
        }
        //监听修改pty大小的命令
        //todo 直接修改pty大小意味着只有最后打开的页面能自适应大小，
        // 前面已发送的数据也不会根据新页面修改，待改进。
        // 更新：
        // ptySize修改后会使得最近的数据重新刷入channel的inputStream中，
        // 导致来自Minecraft服务器的聊天信息重复发送，
        // 暂时通过设置一个ptySizeFlag修复，
        // 更新后，只有第一个终端传入的size是有效的，后续不再根据终端大小自适应修改，
        // 前端因此将终端设为了固定大小
        if(command.startsWith("%command%resize:")){
            String[] data = command.split("%command%resize:")[1].split(",");
            log.info("col:" + data[0]+ ",row: " + data[1]);
            if(connection != null && !ptySizeFlag){
                ptySizeFlag = true;
                ((ChannelShell)connection.getChannel()).setPtySize(Integer.parseInt(data[0]),Integer.parseInt(data[1]),640,480);
            }
            return true;
        }

        //确定服务器是否开启，能否执行命令
        if(!connection.serverIsRunning()) return false;
        broadcastMessage(userName + ": " + command + "\r\n");
        //执行命令
        //todo 修复linux不适配println问题
        PrintWriter printWriter = new PrintWriter(connection.getChannel().getOutputStream());
        printWriter.println(command);
        printWriter.flush();
        return true;
    }

    @Override
    public void broadcastMessage(byte[] buffer) throws IOException {
        messageList.offer(new String(buffer));
        if(messageList.size() > maxMessageListSize){
            messageList.poll();
        }
        for(WebSocketSession session : webSocketSessions){
            session.sendMessage(new TextMessage(buffer));
        }
    }

    @Override
    public void broadcastMessage(String message) throws IOException {
        messageList.offer(message);
        if(messageList.size() > maxMessageListSize){
            messageList.poll();
        }
        for(WebSocketSession session : webSocketSessions){
            session.sendMessage(new TextMessage(message));
        }
    }

    @Override
    public void broadcastChat(ChatMessage chatMessage) throws IOException {
        chatList.offer(chatMessage);
        if(chatList.size() > maxMessageListSize){
            chatList.poll();
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(chatMessage);
        log.info("发送聊天信息: "+jsonString);
        for(WebSocketSession session : chatSessions){
            session.sendMessage(new TextMessage(jsonString));
        }
    }

    @Override
    public void broadcastChat(String serverMessage) throws IOException {
        log.info("server message"+serverMessage);
        //封装服务器信息
        Matcher matcher = chatPattern.matcher(serverMessage);
        ChatMessage chatMessage = new ChatMessage();
        if(matcher.find()){
            log.info("group count:"+matcher.groupCount());
            chatMessage.setUserName(matcher.group(2));
            chatMessage.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            chatMessage.setData(matcher.group(3));

            broadcastChat(chatMessage);
        }
        else{
            log.info("Did not match any chat message.");
        }
    }

    @Override
    public void broadcastChat(String userName, String message) throws IOException {
        //封装网页端发送的信息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserName(userName);
        chatMessage.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        chatMessage.setData(message);

        broadcastChat(chatMessage);
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void sendMessage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    @Override
    public boolean chat(String userName, String text){
        try {
            log.info("receive massage from " + userName + ": " + text);
            broadcastChat("[Web] "+userName, text);

            if(connection == null || !connection.serverIsRunning()){
                return false;
            }
            String chatUnicode = stringToUnicode("tellraw @a [{\"text\":\"[Web] <"+userName+"> "+ text +"\",\"color\":\"white\"}]");
            executeServerCommand(chatUnicode);

            return true;
        } catch (IOException e) {
            log.error("执行聊天命令出错");
            throw new BusinessException(ResponseCode.SYSTEM_ERROR);
        }
    }

    @Override
    public boolean sendChatFeedback(WebSocketSession session, String message){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserName((String) session.getAttributes().get(USER_NAME_KEY));
        chatMessage.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        chatMessage.setData(message);

        try {
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(chatMessage)));
        } catch (IOException e) {
            log.info("发送聊天反馈时转换json失败");
            throw new RuntimeException(e);
        }
        return true;
    }


    //将字符串中的常用中文文字和字符转换为unicode格式，中文以外的字符将保持不变
    private static String stringToUnicode(String str) {
        StringBuilder unicode = new StringBuilder();
        char[] specialChar = new char[]{'。', '￥', '…', '—', 0x2018, 0x201C, 0x2019, 0x201D, '《', '》', '【', '】', '、'};
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 0x4e00 && c <= 0x9fa5) { // 判断是否为中文字符
                unicode.append("\\u").append(Integer.toHexString(c));
            }
            else if (c >= 0xff01 && c <= 0xff5e) { // 判断是否为中文全角符号
                unicode.append("\\u").append(Integer.toHexString(c));
            }
            else {
                boolean flag = false;
                for(char ch : specialChar){
                    if(c == ch){
                        unicode.append("\\u").append(Integer.toHexString(c));
                        flag = true;
                        break;
                    }
                }
                if(!flag) unicode.append(c);
            }
        }
        return unicode.toString();
    }
}
