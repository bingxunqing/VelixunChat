package commands;

import Server.ClientHandler;
import Server.ServerControl;

public class PrivateChat implements Command {
    private String message;
    private String chatTarget;
    private String username;
    public  PrivateChat(String chatTarget , String message){
        this.message = message;
        this.chatTarget = chatTarget;
    }

    public PrivateChat(){

    }


    @Override
    public void execute(ClientHandler client, ServerControl server) {
        username = client.getCurrentUsername();
        ClientHandler receiver = server.getClientByUserName(chatTarget);
        if(receiver!=null){
            receiver.sendMessage("【系统消息】【私聊消息】 ["+ this.username +"]说" + message);
            client.sendMessage("【系统消息】 你已经向["+ this.chatTarget + "]发送成功私聊消息:"+message);
        }else {
            // 应该在执行前判断好，不可能为null，否则此处无法处理null
            client.sendMessage("【系统消息】 发送失败，[" + this.chatTarget + "]已下线。");
        }


    }
}
