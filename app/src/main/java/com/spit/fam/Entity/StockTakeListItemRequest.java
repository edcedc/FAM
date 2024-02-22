package com.spit.fam.Entity;

public class StockTakeListItemRequest {
    private boolean found;
    private int asset;
    private int created_by;
    private int updated_by;

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
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

    public void setUpdated_by(int updated_by) {
        this.updated_by = updated_by;
    }

    public int getStock_take_list() {
        return stock_take_list;
    }

    public void setStock_take_list(int stock_take_list) {
        this.stock_take_list = stock_take_list;
    }

    private int stock_take_list;


}
