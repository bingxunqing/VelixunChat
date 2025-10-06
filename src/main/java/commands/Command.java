package commands;

import Server.ClientHandler;
import Server.ServerControl;

public interface Command {

    public void execute(ClientHandler client, ServerControl server);
}
