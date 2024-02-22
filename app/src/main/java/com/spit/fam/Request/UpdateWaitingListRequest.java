package com.spit.fam.Request;

import java.util.ArrayList;

public class UpdateWaitingListRequest {
    public ArrayList<Integer> getList() {
        return list;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    private Integer userid;
    private ArrayList<Integer> list = new ArrayList<>();

}
