package nmu.sagrada.messages.client;

import androidx.annotation.NonNull;

import nmu.sagrada.messages.Message;

public class Quit extends Message {
    private static final long serialVersionUID = 103L;

    @NonNull
    @Override
    public String toString() {
        return "Quit()";
    }
}
