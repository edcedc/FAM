package com.spit.fam.Entity;

import java.util.ArrayList;

public class StockTakeListItemRemark {
    private int id;
    private String remark;
    private StockTakeListItem stock_take_list_item;
    private Status status;
    public ArrayList<Photo> photo = new ArrayList<>();

    public ArrayList<Photo> getStock_take_asset_item_remark_photos() {
        if(remarkPhoto == null)
            return new ArrayList<>();
        return remarkPhoto;
    }

    public void setStock_take_asset_item_remark_photos(ArrayList<Photo> stock_take_asset_item_remark_photos) {
        this.remarkPhoto = stock_take_asset_item_remark_photos;
    }

    public ArrayList<Photo> remarkPhoto = new ArrayList<>();

    public Status getStatus() {
        if(status == null)
            return new Status();

        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ArrayList<Photo> getPhotos() {
        return photo;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photo = photos;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRemark() {
        if(remark == null)
            return "";
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
