package com.yunjic.mcsc.pojo;

import com.jcraft.jsch.Channel;
import lombok.Data;

@Data
public class MinecraftServerConnection {
    private Channel channel = null;
    private boolean running;

    public boolean serverIsRunning(){
        return running;
    }
}
