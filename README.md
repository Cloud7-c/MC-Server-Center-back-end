# MC-Server-Center-back-end

Minecraft服务器交互中心（后端）

后端使用SpringBoot+Mybatis-Plus+MySQL，前端使用Ant Design Pro(React)脚手架

前端项目地址：[MC-Server-Center-front-end](https://github.com/Cloud7-c/MC-Server-Center-front-end)

### 功能

1. 提供基本的登录注册功能（注册功能可配置开启/关闭）；
2. 用户分权管理；
3. 管理员可在管理页面对用户进行管理操作；
4. 使用 SSH 连接实现无侵入式管理 Minecraft 服务器，项目服务端与 Minecraft 服务端完全分离；
5. 目标 SSH 服务器兼容 Windows 和 Linux（需在配置文件中设置）；
6. 提供一个仿真终端，直接对接 Minecraft 服务器后台；
7. 提供聊天功能，实现游戏内聊天信息与网页端聊天信息互通。

### 关于权限

管理员用户将能看到所有界面，并进行实际操作；  
普通用户将不能看到与管理相关的页面，能对可见页面进行实际操作；  
游客账户仅作演示用，能看到所有界面，但不能进行实际操作。  
