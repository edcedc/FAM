package com.spit.fam.Event;

public class InsertEvent {
    private String assetNo;
    private String name;

    public InsertEvent(String assetNo, String name) {
        this.assetNo = assetNo;
        this.name = name;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
