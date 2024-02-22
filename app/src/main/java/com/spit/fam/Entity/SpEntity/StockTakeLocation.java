package com.spit.fam.Entity.SpEntity;

public class StockTakeLocation {
    private String ID;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getFatherRoNo() {
        return FatherRoNo;
    }

    public void setFatherRoNo(String fatherRoNo) {
        FatherRoNo = fatherRoNo;
    }

    public String getPathID() {
        return PathID;
    }

    public void setPathID(String pathID) {
        PathID = pathID;
    }

    public String getOrderByID() {
        return OrderByID;
    }

    public void setOrderByID(String orderByID) {
        OrderByID = orderByID;
    }

    public String getRoNo() {
        return RoNo;
    }

    public void setRoNo(String roNo) {
        RoNo = roNo;
    }

    private String FatherRoNo,PathID, OrderByID, RoNo;

}
