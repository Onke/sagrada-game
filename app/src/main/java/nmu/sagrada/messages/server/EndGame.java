package nmu.sagrada.messages.server;

import nmu.sagrada.messages.Message;

import java.util.Map;

public class EndGame extends Message {
    private static final long serialVersionUID = 215L;

    public Map<String , Integer> playerPoints;
    public String winner;

}
