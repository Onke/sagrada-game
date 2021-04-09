package nmu.sagrada.messages.client;

import nmu.sagrada.messages.Message;

public class WindowSelected extends Message {
    private static final long serialVersionUID = 105L;
    private String window;

    public WindowSelected(String window) {
        this.window = window;
    }
}
