package nmu.sagrada.messages.client;

import androidx.annotation.NonNull;

import nmu.sagrada.messages.Message;

public class Join extends Message {
    private static final long serialVersionUID = 100L;

    private String name;

    public Join(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Join(%s)", name);
    }

}
