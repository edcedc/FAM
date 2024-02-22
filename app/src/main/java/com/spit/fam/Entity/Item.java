package com.spit.fam.Entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public  class Item extends RealmObject {
    private int id;

    public int getTempStatusid() {
        return tempsid;
    }

    public void setTempStatusid(int tempStatusid) {
        this.tempsid = tempStatusid;
    }

    private int tempsid;


    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }

    private String stocktakeno;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @PrimaryKey
    private String pk;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatusid() {
        return statusid;
    }

    public void setStatusid(int statusid) {
        this.statusid = statusid;
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

    private int statusid;

    public int getTempStatusId() {
        return tempStatusId;
    }

    public void setTempStatusId(int tempStatusId) {
        this.tempStatusId = tempStatusId;
    }

    private int tempStatusId;

    private String assetno, name, brand, model, category, location, epc, remarks, pic, rono;

    public String getProsecutionNo() {
        return prosecutionNo;
    }

    public void setProsecutionNo(String prosecutionNo) {
        this.prosecutionNo = prosecutionNo;
    }

    private String prosecutionNo;

}