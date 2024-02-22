package com.spit.fam.Event;

public class CallbackFailEvent {
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public CallbackFailEvent(String message) {
        this.message = message;
    }
}
