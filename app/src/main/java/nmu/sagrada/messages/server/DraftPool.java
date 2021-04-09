package nmu.sagrada.messages.server;

import java.util.ArrayList;

import nmu.sagrada.messages.Message;

public class DraftPool extends Message {
    private static final long serialVersionUID = 200L;

    public ArrayList<String> draftPool;
    public DraftPool(ArrayList<String> dratPool) {
        this.draftPool = dratPool;
    }
}
