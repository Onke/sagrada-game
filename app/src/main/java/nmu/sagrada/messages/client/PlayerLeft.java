package nmu.sagrada.messages.client;

import nmu.sagrada.messages.Message;

public class PlayerLeft extends Message {
    private static final long serialVersionUID = 108L;

    public String name;

    public PlayerLeft(String name) {
        this.name = name;
    }
}
