package com.yunjic.mcsc.pojo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class SSHConfig {
    @Value("${ssh.host-os}")
    private String os;

    @Value("${ssh.host}")
    private String host;

    @Value("${ssh.port}")
    private int port = 22;

    @Value("${ssh.user.name}")
    private String userName;

    @Value("${ssh.user.password}")
    private String password;

    @Value("${ssh.timeout}")
    private int timeout;

    @Value("${ssh.server-alive-interval}")
    private int serverAliveInterval;
}
