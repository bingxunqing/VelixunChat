package Server;

import commands.Command;
import commands.CommandParser;
import db.DatabaseManager;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    // final表示常量，必须初始化
    private final Socket clientsocket;
    public ServerControl server;
    public PrintWriter pw;
    private String currentUsername=null;
    private String chatTarget = "all";
    CommandParser parser;

    public ClientHandler(Socket clientsocket,ServerControl server) {
        this.clientsocket = clientsocket;
        this.server = server;
        this.parser = new CommandParser();
    }

    @Override
    public void run() {
        try(
                BufferedReader br = new BufferedReader(new InputStreamReader(clientsocket.getInputStream(), StandardCharsets.UTF_8));
                ) {
                this.pw = new PrintWriter(new OutputStreamWriter(clientsocket.getOutputStream(), StandardCharsets.UTF_8), true);

                while(this.currentUsername==null){
                    pw.println("【系统消息】 欢迎! 请登录或注册。格式: /login <用户名> <密码> 或 /register <用户名> <密码>");
                    String commandLine = br.readLine();
                    if(commandLine == null){
                        return;
                    }
                    Command command = parser.parser(commandLine ,this.chatTarget);
                    if(command != null){
                        command.execute(this,server);
                    }
                }
                String line;
                while((line=br.readLine())!=null){
                    if("exit".equals(line)){
                        break;
                    }
                    Command command = parser.parser(line,this.chatTarget);
                    if(command != null){
                        command.execute(this,server);
                    }
                }
        } catch (IOException e) {
            System.out.println("【系统日志】 与客户端 " + clientsocket.getRemoteSocketAddress() + " 的连接发生异常: " + e.getMessage());
        }finally {
            if (this.currentUsername != null) {
                server.removeClientHandler(this);
                server.broadcastMessage("【系统消息】 [" + this.currentUsername + "] 已下线。");
            }
            try {
                clientsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void sendMessage(String message){
        if (pw != null) {
            pw.println(message);
            pw.flush();
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public Socket getClientsocket() {
        return clientsocket;
    }

    public void setChatTarget(String chatTarget) {
        this.chatTarget = chatTarget;
    }


}
//    public boolean dealUserMsg(String msg){
//        if(msg.equalsIgnoreCase("exit")){
//            return false;
//        }
//
//        if(msg.toLowerCase().startsWith("/msg ")){
//            String[] parts = msg.split("\\s+",2);
//            if(parts.length<2){
//                this.sendMessage("【系统消息】命令格式错误。正确格式例如 /msg bingxunqing 或 /msg all");
//                return true;
//            }
//
//            String newTarget = parts[1];
//
//            if("all".equalsIgnoreCase(newTarget)){
//                this.chatTarget = "all";
//                this.sendMessage("【系统消息】 已切换到公共聊天");
//            }else if(newTarget.equalsIgnoreCase(this.currentUsername)){
//                this.sendMessage("【系统消息】跟自己聊天吗？有点意思");
//            }else{
//                if(server.getClientByUserName(newTarget)!=null){
//                    this.chatTarget = newTarget;
//                    this.sendMessage("【系统消息】已切换到与[" + newTarget + "]的私聊。发送 '/msg all' 可返回公共频道");
//                }
//                else{
//                    this.sendMessage("【系统消息】 [" + newTarget +"] 不在线或不存在，无法切换.");
//                }
//            }
//            return true;
//        }else{
//            if("all".equalsIgnoreCase(this.chatTarget)){
//                server.broadcastMessage("【用户发言】["+ this.currentUsername + "] 说: " + msg);
//            }else{
//                ClientHandler receiver = server.getClientByUserName(this.chatTarget);
//
//                if(receiver!=null){
//                    receiver.sendMessage("【系统消息】【私聊消息】 ["+ this.currentUsername +"]说" + msg);
//                    this.sendMessage("【系统消息】 你已经发送成功私聊["+ this.chatTarget + "]消息"+msg);
//                }else{
//                    this.sendMessage("【系统消息】 发送失败，[" + this.chatTarget + "]已下线。");
//                    this.chatTarget = "all";
//                    this.sendMessage("【系统消息】 已自动切换回公共聊天");
//                }
//            }
//        }
//        return true;
//    }
//    public void handleAuthCommand(String commandLine){
//        // \s表示空白字符，+表示一个或者多个连续字符，3表示，最多分为三部分，剩下有空格也全部归为第三部分
//        String[] parts = commandLine.trim().split("\\s+",3);
//        String command = parts[0];
//
//        if(parts.length <3){
//            sendMessage("【系统消息】 错误，命令不正确，仔细检查输入的命名，例如'/login bingxunqing 123ABC'，需要三部分");
//            return;
//        }
//
//        String username = parts[1];
//        String password = parts[2];
//       sendMessage(username + "------------pas:---------" + password);
//        if(username.length()<3 || username.length()>16 ){
//            sendMessage("【系统消息】 注册失败：用户名长度必须在 3 到 16 个字符之间。");
//            return;
//        }
//
//        // 这个非常重要！如果没有这个，用户可以sql注入，以及脚本注入等！
//        // ^表示从第一个开始，$表示匹配至结束，前面[...]为必须有一个或多个
//        if (!username.matches("^[a-zA-Z0-9_]+$")) {
//            sendMessage("【系统消息】 注册失败：用户名只能包含字母、数字和下划线。");
//            return;
//        }
//
//        if (password.length() < 6) {
//            sendMessage("【系统消息】 注册失败：密码长度至少需要 6 个字符。");
//            return;
//        }
//
//
//        switch(command){
//            case "/register":
//                sendMessage("0----------");
//                // 从clientsocket获取链接信息（getRemoteSocketAddress），然后强转类型为java.net.InetSocketAddress，
//                // 然后获取地址（getAddress()，包含ip和端口），然后获取实际ip地址getHostAddress()（去掉端口）
//                String clientIp = ((java.net.InetSocketAddress)clientsocket.getRemoteSocketAddress()).getAddress().getHostAddress();
//                if(!ServerControl.canRegister(clientIp)){
//                    sendMessage("【系统消息】注册失败,注册过于频繁，请稍后再试。（5分钟内最多只能注册30个）");
//                    return;
//                }
//               sendMessage("1-----");
//                if(DatabaseManager.registerUser(username,password)){
//                    sendMessage("2---------");
//                    ServerControl.successRegister(clientIp);
//                   sendMessage("3------------");
//                    sendMessage("【系统消息】注册成功！请使用'/login'指令登录。");
//                }else {
//                    sendMessage("【系统消息】注册失败，可能是因为用户名占用");
//                }
//                break;
//                case "/login":
//                    if(DatabaseManager.checkLogin(username,password)){
//                        this.currentUsername=username;
//                        sendMessage("【系统消息】登录成功！欢迎, " + username);
//                    } else {
//                        sendMessage("【系统消息】登录失败：用户名或密码错误。");
//                    }
//                    break;
//            default:
//                sendMessage("【系统消息】错误：无效的命令。");
//                break;
//        }
//
//    }