package nmu.sagrada.messages.server;

import androidx.annotation.NonNull;

import nmu.sagrada.messages.Message;

public class Left extends Message {
    private static final long serialVersionUID = 202L;

    public String name;

    @NonNull
    @Override
    public String toString() {
        return String.format("Left(%s)" , name);
    }
}
