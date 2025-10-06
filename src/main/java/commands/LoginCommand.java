package commands;

import Server.ClientHandler;
import Server.ServerControl;
import db.DatabaseManager;

public class LoginCommand implements Command {
    private String username;
    private String password;

    public LoginCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginCommand() {

    }

    @Override
    public void execute(ClientHandler client, ServerControl server) {
        if(DatabaseManager.checkLogin(username, password)) {
            client.setCurrentUsername(username);
            client.sendMessage("【系统消息】登录成功！欢迎--" +username);
            server.addClientHandler(client);
            server.broadcastMessage("【系统消息】 ["+ username + "] 已上线。");
            client.sendMessage("【系统消息】您已进入公共聊天频道。发送 '/msg <用户名>' 可切换至私聊。");
        }else{
            client.sendMessage("【系统消息】登录失败,用户名或密码错误。");
        }
    }
}
