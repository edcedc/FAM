package com.spit.fam.Request;

import java.util.ArrayList;

public class ReturnBorrowedAssetRequest {
    private Integer userid;

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getLocationId() {
        return location;
    }

    public void setLocationId(String locationId) {
        this.location = locationId +"";
    }

    public ArrayList<Integer> getList() {
        return list;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
    }

    private String location;
    private ArrayList<Integer> list = new ArrayList<>();
}
