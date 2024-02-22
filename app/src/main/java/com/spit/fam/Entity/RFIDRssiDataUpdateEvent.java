package com.spit.fam.Entity;

import java.util.ArrayList;

public class RFIDRssiDataUpdateEvent {

    public RFIDRssiDataUpdateEvent(ArrayList<EpcWithRssi> arrayList) {
        setArrayList(arrayList);
    }

    public ArrayList<EpcWithRssi> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<EpcWithRssi> arrayList) {
        this.arrayList = arrayList;
    }

    private ArrayList<EpcWithRssi> arrayList = new ArrayList<>();
}
