package com.spit.fam.Entity.SPEntityP2;

import java.util.ArrayList;

public class AssetListFromBorrowList {
    private String name;
    private int id;
    private String type;
    private ArrayList<BriefAsset> data = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<BriefAsset> getData() {
        return data;
    }

    public void setData(ArrayList<BriefAsset> data) {
        this.data = data;
    }
}
