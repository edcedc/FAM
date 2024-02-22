package com.spit.fam.Entity;

public class StockTakeItemRemarkPhotoRequest {
    private String photo;
    private int created_by;
    private StockTakeListItemRemark stockTakeListItemRemark;

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

    public StockTakeListItemRemark getStockTakeListItemRemark() {
        return stockTakeListItemRemark;
    }

    public void setStockTakeListItemRemark(StockTakeListItemRemark stockTakeListItemRemark) {
        this.stockTakeListItemRemark = stockTakeListItemRemark;
    }
}
