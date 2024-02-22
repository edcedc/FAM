package com.spit.fam.Entity;

public class SpinnerOnClickEvent {
    int layer;
    int type;
    String fatherno;

    public SpinnerOnClickEvent(int layer, int type, String fatherno) {
        this.layer = layer;
        this.type = type;
        this.fatherno = fatherno;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFatherno() {
        return fatherno;
    }

    public void setFatherno(String fatherno) {
        this.fatherno = fatherno;
    }
}

