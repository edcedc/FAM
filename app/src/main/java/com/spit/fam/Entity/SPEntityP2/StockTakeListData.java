package com.spit.fam.Entity.SPEntityP2;

import com.spit.fam.Entity.Item;

import java.util.ArrayList;

public class StockTakeListData {
    public ArrayList<Item> getData() {
        return data;
    }

    public void setData(ArrayList<Item> data) {
        this.data = data;
    }

    private ArrayList<Item> data = new ArrayList<>();

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }

    public String getPicsite() {
        return picsite;
    }

    public void setPicsite(String picsite) {
        this.picsite = picsite;
    }

    private int totalCount;
    private String status, name,stocktakeno,picsite;

    public String getLastAssetNo() {
        return LastAssetNo;
    }

    public void setLastAssetNo(String lastAssetNo) {
        LastAssetNo = lastAssetNo;
    }

    private String LastAssetNo;


    /*
          "id": 997,
      "assetno": "0000015143",
      "name": "资产test",
      "brand": "123",
      "model": "23",
      "category": "巧克力->1",
      "location": "青岛",
      "epc": "E2801160600002083E167978",
      "stockTake": true,
      "statusid": 2,
      "remarks": "",
      "pic": "",
      "rono": "050583170AD24F08A91C3B017344E13A"

     */
}
