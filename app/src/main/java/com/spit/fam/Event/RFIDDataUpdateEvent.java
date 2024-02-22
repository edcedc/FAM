package com.spit.fam.Event;

import java.util.ArrayList;

public class RFIDDataUpdateEvent {
    public RFIDDataUpdateEvent(ArrayList<String> data) {
        setData(data);
    }

    public ArrayList<String> getData() {
        return data;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
    }

    private ArrayList<String> data = new ArrayList<>();

    public boolean isBarcode() {
        return barcode;
    }

    public void setBarcode(boolean barcode) {
        this.barcode = barcode;
    }

    private boolean barcode;

    public boolean isManually() {
        return manually;
    }

    public void setManually(boolean manually) {
        this.manually = manually;
    }

    private boolean manually;

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    private boolean found;
}
