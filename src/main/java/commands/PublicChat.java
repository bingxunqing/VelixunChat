package commands;

import Server.ClientHandler;
import Server.ServerControl;

public class PublicChat implements Command {
    String username;
    String message;

    public PublicChat( String message) {
        this.message = message;
    }

    public PublicChat(){

    }

    @Override
    public void execute(ClientHandler client, ServerControl server) {
        username = client.getCurrentUsername();
        server.broadcastMessage("【用户发言】["+ username + "] 说: " +message);
    }
}
