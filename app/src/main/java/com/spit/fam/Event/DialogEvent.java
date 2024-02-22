package com.spit.fam.Event;

public class DialogEvent {
    private String title;
    private String message;

    public DialogEvent(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
