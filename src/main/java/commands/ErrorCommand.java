package commands;

import Server.ClientHandler;
import Server.ServerControl;

public class ErrorCommand implements Command{
    String msg;

    public ErrorCommand(String msg){
        this.msg = msg;
    }

    @Override
    public void execute(ClientHandler client, ServerControl server) {
        client.sendMessage(msg);
    }
}
