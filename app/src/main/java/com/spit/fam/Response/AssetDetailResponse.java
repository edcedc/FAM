package com.spit.fam.Response;

import com.spit.fam.Entity.SPEntityP2.AssetsDetail;

import java.util.ArrayList;

public class AssetDetailResponse {

    private int count;
    private String thiscalldate;
    private ArrayList<AssetsDetail> myData = new ArrayList<>();
    
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getThiscalldate() {
        return thiscalldate;
    }

    public void setThiscalldate(String thiscalldate) {
        this.thiscalldate = thiscalldate;
    }

    public ArrayList<AssetsDetail> getMyData() {
        return myData;
    }

    public void setMyData(ArrayList<AssetsDetail> myData) {
        this.myData = myData;
    }
}
