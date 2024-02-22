package com.spit.fam.Request;

public class UploadPhotoRequest {
    private String photo;
    private int created_by;

    public int getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(int updated_by) {
        this.updated_by = updated_by;
    }

    private int updated_by;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getCreated_by() {
        return created_by;
    }

    public void setCreated_by(int created_by) {
        this.created_by = created_by;
    }

    public StockTakeListItemRemarkRequest getStock_take_asset_item_remark() {
        return stock_take_asset_item_remark;
    }

    public void setStock_take_asset_item_remark(StockTakeListItemRemarkRequest stock_take_asset_item_remark) {
        this.stock_take_asset_item_remark = stock_take_asset_item_remark;
    }

    private StockTakeListItemRemarkRequest stock_take_asset_item_remark;
}
