package com.spit.fam.Entity.SPEntityP3;

import io.realm.RealmObject;

public class BorrowAsset extends RealmObject {
    public long getTimeString() {
        return timeString;
    }

    public void setTimeString(long timeString) {
        this.timeString = timeString;
    }

    /*

        {
          "id": 22,
          "assetno": "SPIT/ST/FE/0000000092",
          "name": "9L 水劑滅火筒",
          "brand": "",
          "model": "",
          "category": "安全設備->滅火筒",
          "location": "香港->科學園->16W 205室",
          "epc": "E20210722170624000000001",
          "borrowed": true,
          "type": 1
        }
         */
    private long timeString;

    private int id;

    public boolean isTempAsset() {
        return tempAsset;
    }

    public void setTempAsset(boolean tempAsset) {
        this.tempAsset = tempAsset;
    }

    private boolean tempAsset = false;

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAssetno() {
        return assetno;
    }

    public void setAssetno(String assetno) {
        this.assetno = assetno;
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

    public boolean isBorrowed() {
        return borrowed;
    }

    public void setBorrowed(boolean borrowed) {
        this.borrowed = borrowed;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLastAssetNo() {
        return LastAssetNo;
    }

    public void setLastAssetNo(String lastAssetNo) {
        LastAssetNo = lastAssetNo;
    }

    private String LastAssetNo;
    private int type;
    private String assetno, name, brand, model, category, location,epc,borrowno;
    private boolean borrowed;
    private String companyid, userid;

}
