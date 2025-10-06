package commands;

import Server.ClientHandler;
import Server.ServerControl;

public class ChangeChatTarget implements Command {
    private String newTarget;
    private String username;
    public ChangeChatTarget(String newTarget){
        this.newTarget = newTarget;
    }
    public ChangeChatTarget(){

    }

    @Override
    public void execute(ClientHandler client, ServerControl server) {
        username = client.getCurrentUsername();
        if("all".equalsIgnoreCase(newTarget)){
            client.setChatTarget("all");
            client.sendMessage("【系统消息】 已切换到公共聊天");
        }else if(username.equalsIgnoreCase(newTarget)){
            client.sendMessage("【系统消息】跟自己聊天吗？有点意思");
        }else{
            if(server.getClientByUserName(newTarget)!=null){
                client.setChatTarget(newTarget);
                client.sendMessage("【系统消息】已切换到与[" + newTarget + "]的私聊。发送 '/msg all' 可返回公共频道");
            }else{
                client.sendMessage("【系统消息】无法切换，用户[" + newTarget + "]不存在或不在线。");
            }

        }
    }
}
