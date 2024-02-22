package com.spit.fam.Entity.SpEntity;

import java.util.ArrayList;

public class StockTakeDetail {
    public String OrderNo;

    public String getOrderNo() {
        return OrderNo;
    }

    public void setOrderNo(String orderNo) {
        OrderNo = orderNo;
    }

    public ArrayList<StockTakeAsset> getTable() {
        if(Table == null)
            return  new ArrayList<>();
        return Table;
    }

    public void setTable(ArrayList<StockTakeAsset> table) {
        Table = table;
    }

    public ArrayList<StockTakeLocation> getLocation() {
        return Location;
    }

    public void setLocation(ArrayList<StockTakeLocation> location) {
        Location = location;
    }

    public ArrayList<StockTakeLocation> getCategory() {
        return Category;
    }

    public void setCategory(ArrayList<StockTakeLocation> category) {
        Category = category;
    }

    public ArrayList<StockTakeAsset> Table = new ArrayList<>();
    public ArrayList<StockTakeLocation> Location = new ArrayList<>();
    public ArrayList<StockTakeLocation> Category = new ArrayList<>();
}
