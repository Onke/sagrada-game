package nmu.sagrada.messages.server;

import nmu.sagrada.messages.Message;

public class PlacedDieReceived extends Message {
    private static final long serialVersionUID = 203L;

    public int draftPoolSelection;
    public int windowBoxSelection;

    public PlacedDieReceived(int draftPoolSelection, int windowBoxSelection) {
        this.draftPoolSelection = draftPoolSelection;
        this.windowBoxSelection = windowBoxSelection;
    }
}
