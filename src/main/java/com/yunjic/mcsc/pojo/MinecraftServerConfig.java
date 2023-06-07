package com.yunjic.mcsc.pojo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class MinecraftServerConfig {
    @Value("${minecraft.path}")
    private String path;

    @Value("${minecraft.server}")
    private String server;

    @Value("${java.path}")
    private String javaPath;

    @Value("${java.arguments}")
    private String arguments;
}
