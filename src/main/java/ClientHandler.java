import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    // final表示常量，必须初始化
    private final Socket clientsocket;
    public PrintWriter pw;
    private String currentUsername=null;
    private String chatTarget = "all";
    public ClientHandler(Socket clientsocket) {
        this.clientsocket = clientsocket;
    }

    @Override
    public void run() {

//        System.out.println("!!!!!!!!!! 正在运行最终修复版代码 [时间: " + java.time.LocalDateTime.now() + "] !!!!!!!!!!");
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
                    handleAuthCommand(commandLine);
                }
                System.out.println("【系统消息】 登录成功！输入文字即可聊天，输入'exit'即可退出");
                ServerControl.addClientHandler(this);
                ServerControl.broadcastMessage("【系统消息】 ["+ this.currentUsername + "] 已上线。");
//            Thread.currentThread().getId() +
            this.sendMessage("【系统消息】您已进入公共聊天频道。发送 '/msg <用户名>' 可切换至私聊。");
            String line;
            while ((line = br.readLine()) != null) {
                boolean isBreak = dealUserMsg(line);
                if(!isBreak){
                    break;
                }
            }
        } catch (Throwable e) {
            System.err.println("!!!!!!!!!! 线程发生致命错误 !!!!!!!!!!");
            System.out.println("【系统日志】客户端 " + clientsocket.getRemoteSocketAddress() + " 的连接已断开: " + e.getMessage());
            e.printStackTrace();
        }finally {
            if (this.currentUsername != null) {
                ServerControl.removeClientHandler(this);
                ServerControl.broadcastMessage("【系统消息】 [" + this.currentUsername + "] 已下线。");
            }
            try {
                clientsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean dealUserMsg(String msg){
        if(msg.equalsIgnoreCase("exit")){
            return false;
        }

        if(msg.toLowerCase().startsWith("/msg ")){
            String[] parts = msg.split("\\s+",2);
            if(parts.length<2){
                this.sendMessage("【系统消息】命令格式错误。正确格式例如 /msg bingxunqing 或 /msg all");
                return true;
            }

            String newTarget = parts[1];

            if("all".equalsIgnoreCase(newTarget)){
                this.chatTarget = "all";
                this.sendMessage("【系统消息】 已切换到公共聊天");
            }else if(newTarget.equalsIgnoreCase(this.currentUsername)){
                this.sendMessage("【系统消息】跟自己聊天吗？有点意思");
            }else{
                if(ServerControl.getClientByUserName(newTarget)!=null){
                    this.chatTarget = newTarget;
                    this.sendMessage("【系统消息】已切换到与[" + newTarget + "]的私聊。发送 '/msg all' 可返回公共频道");
                }
                else{
                    this.sendMessage("【系统消息】 [" + newTarget +"] 不在线或不存在，无法切换.");
                }
            }
            return true;
        }else{
            if("all".equalsIgnoreCase(this.chatTarget)){
                ServerControl.broadcastMessage("【用户发言】["+ this.currentUsername + "] 说: " + msg);
            }else{
                ClientHandler receiver = ServerControl.getClientByUserName(this.chatTarget);

                if(receiver!=null){
                    receiver.sendMessage("【系统消息】【私聊消息】 ["+ this.currentUsername +"]说" + msg);
                    this.sendMessage("【系统消息】 你已经发送成功私聊["+ this.chatTarget + "]消息"+msg);
                }else{
                    this.sendMessage("【系统消息】 发送失败，[" + this.chatTarget + "]已下线。");
                    this.chatTarget = "all";
                    this.sendMessage("【系统消息】 已自动切换回公共聊天");
                }
            }
        }
        return true;
    }

    public void sendMessage(String message){
        if (pw != null) {
            pw.println(message);
            pw.flush();
        }
    }

    public void handleAuthCommand(String commandLine){
        // \s表示空白字符，+表示一个或者多个连续字符，3表示，最多分为三部分，剩下有空格也全部归为第三部分
        String[] parts = commandLine.trim().split("\\s+",3);
        String command = parts[0];

        if(parts.length <3){
            sendMessage("【系统消息】 错误，命令不正确，仔细检查输入的命名，例如'/login bingxunqing 123ABC'，需要三部分");
            return;
        }

        String username = parts[1];
        String password = parts[2];
//        sendMessage(username + "------------pas:---------" + password);
        if(username.length()<3 || username.length()>16 ){
            sendMessage("【系统消息】 注册失败：用户名长度必须在 3 到 16 个字符之间。");
            return;
        }

        // 这个非常重要！如果没有这个，用户可以sql注入，以及脚本注入等！
        // ^表示从第一个开始，$表示匹配至结束，前面[...]为必须有一个或多个
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            sendMessage("【系统消息】 注册失败：用户名只能包含字母、数字和下划线。");
            return;
        }

        if (password.length() < 6) {
            sendMessage("【系统消息】 注册失败：密码长度至少需要 6 个字符。");
            return;
        }


        switch(command){
            case "/register":
//                sendMessage("0----------");
                // 从clientsocket获取链接信息（getRemoteSocketAddress），然后强转类型为java.net.InetSocketAddress，
                // 然后获取地址（getAddress()，包含ip和端口），然后获取实际ip地址getHostAddress()（去掉端口）
                String clientIp = ((java.net.InetSocketAddress)clientsocket.getRemoteSocketAddress()).getAddress().getHostAddress();
                if(!ServerControl.canRegister(clientIp)){
                    sendMessage("【系统消息】注册失败,注册过于频繁，请稍后再试。（5分钟内最多只能注册30个）");
                    return;
                }
//                sendMessage("1-----");
                if(DatabaseManager.registerUser(username,password)){
//                    sendMessage("2---------");
                    ServerControl.sucessRegister(clientIp);
//                    sendMessage("3------------");
                    sendMessage("【系统消息】注册成功！请使用'/login'指令登录。");
                }else {
                    sendMessage("【系统消息】注册失败，可能是因为用户名占用");
                }
                break;
                case "/login":
                    if(DatabaseManager.checkLogin(username,password)){
                        this.currentUsername=username;
                        sendMessage("【系统消息】登录成功！欢迎, " + username);
                    } else {
                        sendMessage("【系统消息】登录失败：用户名或密码错误。");
                    }
                    break;
            default:
                sendMessage("【系统消息】错误：无效的命令。");
                break;
        }

    }

    public String getCurrentUsername() {
        return currentUsername;
    }

}
