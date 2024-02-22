package com.spit.fam.Event;

public class ProgressEvent {
    public ProgressEvent(int count, int total) {
        this.count = count;
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int total;
    private int count;
}
