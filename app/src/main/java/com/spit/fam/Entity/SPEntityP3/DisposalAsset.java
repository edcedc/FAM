package com.spit.fam.Entity.SPEntityP3;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DisposalAsset extends RealmObject {
    private int id;
    private String assetNo;
    private String name;
    private String brand;
    private String model;
    private String category;
    private String location;
    private String epc;

    public String getLastAssetNo() {
        return LastAssetNo;
    }

    public void setLastAssetNo(String lastAssetNo) {
        LastAssetNo = lastAssetNo;
    }

    private String LastAssetNo;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;

    public long getTimeString() {
        return timeString;
    }

    public void setTimeString(long timeString) {
        this.timeString = timeString;
    }

    private long timeString;


    public boolean isTempAsset() {
        return tempAsset;
    }

    public void setTempAsset(boolean tempAsset) {
        this.tempAsset = tempAsset;
    }

    private boolean tempAsset;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @PrimaryKey
    private String pk;

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
    }

    private String disposalNo;
    private boolean disposed;

    private String companyid, userid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
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

    public boolean isDisposed() {
        return disposed;
    }

    public void setDisposed(boolean disposed) {
        this.disposed = disposed;
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

}
