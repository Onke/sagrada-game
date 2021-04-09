package nmu.sagrada.messages.client;

import nmu.sagrada.messages.Message;

public class PlaceDie extends Message {
    private static final long serialVersionUID = 102L;
    private  int draftPoolSelection, windowSelection;

    public PlaceDie(int draftPoolSelection, int windowSelection) {
        this.draftPoolSelection = draftPoolSelection;
        this.windowSelection = windowSelection;

    }
}
