package nmu.sagrada.messages.server;

import java.util.ArrayList;

import nmu.sagrada.messages.Message;

public class LobbyPlayersListed extends Message {
    private static final long serialVersionUID = 205L;

    public ArrayList<String> playerNames;

}
