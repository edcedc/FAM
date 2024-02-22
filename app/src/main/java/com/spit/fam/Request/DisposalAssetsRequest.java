package com.spit.fam.Request;

import java.util.ArrayList;

public class DisposalAssetsRequest {
    public ArrayList<Integer> getList() {
        return list;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
    }

    private ArrayList<Integer> list = new ArrayList<>();
}
