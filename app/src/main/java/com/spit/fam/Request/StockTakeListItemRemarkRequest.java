package com.spit.fam.Request;

import com.spit.fam.Entity.Photo;

import java.util.ArrayList;

public class StockTakeListItemRemarkRequest {
    /*
    {
        "remark" : "remark",
        "asset" : 1,
        "created_by" : 1,
        "updated_by" : 1,
        "stock_take_list" : 1,
        "status" : 2,
        "stock_take_list_item" : 3
    }
     */
    private Integer id;
    private String remark;
    private int stock_take_list_item,stock_take_list;
    private int status;
    private int created_by, updated_by;

    public ArrayList<Photo> getRemarkPhoto() {
        return remarkPhoto;
    }

    public void setRemarkPhoto(ArrayList<Photo> remarkPhoto) {
        this.remarkPhoto = remarkPhoto;
    }

    private ArrayList<Photo> remarkPhoto = new ArrayList<>();

    public int getStock_take_list() {
        return stock_take_list;
    }

    public void setStock_take_list(int stock_take_list) {
        this.stock_take_list = stock_take_list;
    }

    public int getAsset() {
        return asset;
    }

    public void setAsset(int asset) {
        this.asset = asset;
    }

    private int asset;

    public int getCreated_by() {
        return created_by;
    }

    public void setCreated_by(int created_by) {
        this.created_by = created_by;
    }

    public int getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(int updated_by) {
        this.updated_by = updated_by;
    }


    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getStock_take_list_item() {
        return stock_take_list_item;
    }

    public void setStock_take_list_item(int stock_take_list_item) {
        this.stock_take_list_item = stock_take_list_item;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
