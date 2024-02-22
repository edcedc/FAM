package com.spit.fam.Entity;

public class StockTakeListItem {
    private int id;
    private int asset;
    private int created_by;
    private int updated_by;
    private String created_at;
    private boolean found;
    private int stock_take_asset_item_remark;

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    private String updated_at;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAsset() {
        return asset;
    }

    public void setAsset(int asset) {
        this.asset = asset;
    }

    public int getCreated_by() {
        return created_by;
    }

    public void setCreated_by(int created_by) {
        this.created_by = created_by;
    }

    public int getUpdated_by() {
        return updated_by;
    }


    public int getStock_take_list() {
        return stock_take_list;
    }

    public void setStock_take_list(int stock_take_list) {
        this.stock_take_list = stock_take_list;
    }

    public void setUpdated_by(int updated_by) {
        this.updated_by = updated_by;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    private int stock_take_list;

    public int getStock_take_asset_item_remark() {
        return stock_take_asset_item_remark;
    }

    public void setStock_take_asset_item_remark(int stock_take_asset_item_remark) {
        this.stock_take_asset_item_remark = stock_take_asset_item_remark;
    }


    public int remarkDetails;
}
