package commands;


public class CommandParser {
    public Command parser(String msg, String chatTarget){
        msg = msg.trim();
        if(msg.isEmpty()){
            return new ErrorCommand("【系统消息】 输入不能为空");
        }

        if(msg.startsWith("/login ")){
            String[] parts = msg.split("\\s+",3);
            if(parts.length<3){
                return new ErrorCommand("【系统消息】 错误，命令不正确，仔细检查输入的命名，例如'/login bingxunqing 123ABC'，需要三部分");
            }

            return new LoginCommand(parts[1],parts[2]);
        }else if(msg.startsWith("/register ")){
            String[] parts = msg.split("\\s+",3);
            if(parts.length<3){
                return new ErrorCommand("【系统消息】 错误，命令不正确，仔细检查输入的命名，例如'/register bingxunqing 123ABC'，需要三部分");
            }
            return new registerCommand(parts[1],parts[2]);
        }else if(msg.startsWith("/msg ")){
            String[] parts = msg.split("\\s+",2);
            if(parts.length<2){
                return new ErrorCommand("【系统消息】 错误，命令不正确，仔细检查输入的命名，例如'/msg bingxunqing'，需要两部分");
            }
            return new ChangeChatTarget(parts[1]);
        }else{
            if(chatTarget.equals("all")){
                return new PublicChat(msg);
            }else{
                return new PrivateChat(chatTarget,msg);
            }
        }
    }

}
