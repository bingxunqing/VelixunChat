package commands;

import Server.ClientHandler;
import Server.ServerControl;
import db.DatabaseManager;

import java.net.InetSocketAddress;

public class registerCommand implements Command {
    private String username;
    private String password;

    public registerCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public registerCommand() {

    }


    @Override
    public void execute(ClientHandler client, ServerControl server) {



        if(username.length()<3 || username.length()>16){
            client.sendMessage("【系统消息】 注册失败：用户名长度必须在 3 到 16 个字符之间。");
            return;
        }

        if(password.length()<6 || password.length()>100){
            client.sendMessage("【系统消息】 注册失败：密码长度至少需要 6 个字符,不能超过 100 个字符");
            return;
        }

        if(!username.matches("^[a-zA-Z0-9_]+$")) {
            client.sendMessage("【系统消息】 注册失败：用户名只能包含字母、数字和下划线。");
            return;
        }

        String clientIp = ((InetSocketAddress) client.getClientsocket().getRemoteSocketAddress()).getAddress().getHostAddress();
        if(!ServerControl.canRegister(clientIp)){
            client.sendMessage("【系统消息】 注册失败,注册过于频繁，请稍后再试。（5分钟内最多只能注册30个）");
            return;
        }

        if(DatabaseManager.registerUser(username, password)){
            ServerControl.successRegister(clientIp);
            client.sendMessage("【系统消息】 注册成功！请使用'/login'指令登录。");
        }else{
            client.sendMessage("【系统消息】 注册失败，可能是因为用户名占用");
        }
    }
}
