package com.yunjic.mcsc.controller;

import com.yunjic.mcsc.common.ResponseCode;
import com.yunjic.mcsc.exception.BusinessException;
import com.yunjic.mcsc.pojo.model.User;
import com.yunjic.mcsc.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import static com.yunjic.mcsc.constant.UserConstant.CURRENT_USER_SESSION_KEY;

/**
 * 处理管理类命令的接口
 */
@RestController
@RequestMapping("api/manage")
public class ManageController {
    @Autowired
    TerminalService terminalService;

    //获取Minecraft服务器状态
    @GetMapping("/server/state")
    public boolean serverState(HttpServletRequest request){
        return terminalService.getServerState();
    }

    //启动Minecraft服务器
    @GetMapping("/server/start")
    public boolean startServer(HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ResponseCode.NO_AUTH);
        }
        try {
            return terminalService.startMinecraftServer();
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.SYSTEM_ERROR);
        }
    }

    //关闭Minecraft服务器
    @GetMapping("/server/stop")
    public boolean stopServer(HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ResponseCode.NO_AUTH);
        }
        try {
            return terminalService.stopMinecraftServer();
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.SYSTEM_ERROR);
        }
    }

    private boolean isAdmin(HttpServletRequest request){
        User currentUser = (User)request.getSession().getAttribute(CURRENT_USER_SESSION_KEY);
        return currentUser != null && currentUser.getRole() == 0;
    }
}
