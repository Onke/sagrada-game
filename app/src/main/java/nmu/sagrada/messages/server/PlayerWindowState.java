package nmu.sagrada.messages.server;

import java.util.ArrayList;

import nmu.sagrada.messages.Message;

public class PlayerWindowState extends Message {
    private static final long serialVersionUID = 214L;

    public ArrayList<String> dicePostions;
}
