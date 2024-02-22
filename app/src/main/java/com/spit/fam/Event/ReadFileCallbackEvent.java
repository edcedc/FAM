package com.spit.fam.Event;

public class ReadFileCallbackEvent {
    public String getType() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName;
}
