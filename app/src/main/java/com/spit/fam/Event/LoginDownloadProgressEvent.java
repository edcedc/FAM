package com.spit.fam.Event;

public class LoginDownloadProgressEvent {

    public LoginDownloadProgressEvent(float progress) {
        this.progress = progress;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    private float progress;
}
