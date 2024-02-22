package com.spit.fam.Request;

import java.util.ArrayList;

public class UploadStockTakeRequest {

    public ArrayList<Integer> getList() {
        return list;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
    }

    public Integer getUserid() {
        return asset;
    }

    public void setAsset(Integer userid) {
        this.asset = userid;
    }

    private Integer asset;
    private ArrayList<Integer> list = new ArrayList<>();

}
