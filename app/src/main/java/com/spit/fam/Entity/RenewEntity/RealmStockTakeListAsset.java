package com.spit.fam.Entity.RenewEntity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmStockTakeListAsset extends RealmObject {
    private String stocktakeno;

    public String getStocktakename() {
        return stocktakename;
    }

    public void setStocktakename(String stocktakename) {
        this.stocktakename = stocktakename;
    }

    private String stocktakename;
    private int id;
    private String assetno;
    private String name;
    private String brand;
    private String model;
    private String category;
    private String location;
    private String epc;
    private boolean stockTake;
    private int statusid;
    private String remarks;
    private String pic;
    private String rono;

    public String getLastAssetNo() {
        return LastAssetNo;
    }

    public void setLastAssetNo(String lastAssetNo) {
        LastAssetNo = lastAssetNo;
    }

    private String LastAssetNo;

    public String getOtherRono() {
        return otherRono;
    }

    public void setOtherRono(String otherRono) {
        this.otherRono = otherRono;
    }

    private String otherRono;

    private String findType;
    @PrimaryKey
    private String pk;

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getPk() {
        return pk;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userId;
    private String companyId;

    private String userName;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getFindType() {
        return findType;
    }

    public boolean isFoundByScan() {
        if(findType.equals("uploaded"))
            return false;

        return !findType.equals("rfid");
    }

    public void setFoundByScan(boolean foundByScan) {
        if(foundByScan)
            findType = "barcode";
        else
            findType = "rfid";
    }

    public void setFindType(String findType) {
        this.findType = findType;
    }

    public Date getScanDateTime() {
        return scanDateTime;
    }

    public void setScanDateTime(Date scanDateTime) {
        this.scanDateTime = scanDateTime;
    }

    private Date scanDateTime = null;

    public boolean getTempStockTake() {
        return tempStockTake;
    }

    public void setTempStockTake(boolean tempStockTake) {
        this.tempStockTake = tempStockTake;
    }

    private boolean tempStockTake;

    public String getAssetno() {
        return assetno;
    }

    public void setAssetno(String assetno) {
        this.assetno = assetno;
    }
    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public boolean isStockTake() {
        return stockTake;
    }

    public void setStockTake(boolean stockTake) {
        this.stockTake = stockTake;
    }

    public int getStatusid() {
        return statusid;
    }

    public void setStatusid(int statusid) {
        this.statusid = statusid;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getRono() {
        return rono;
    }

    public void setRono(String rono) {
        this.rono = rono;
    }


}
