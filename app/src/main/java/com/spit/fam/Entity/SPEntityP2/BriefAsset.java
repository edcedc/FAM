package com.spit.fam.Entity.SPEntityP2;

import java.util.ArrayList;

public class BriefAsset {
    private int id;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setId(int id) {this.id = id;}

    private int ID;
    private String assetNo;
    private String assetno;

    private String name;
    private String model;
    private String brand;
    private String category;
    private String location;
    private String epc;
    private String statusid;
    private String statusname;
    private Boolean borrowed;
    private Boolean disposed;
    private Boolean overdue;

    public String getProsecutionNo() {
        return prosecutionNo;
    }

    public void setProsecutionNo(String prosecutionNo) {
        this.prosecutionNo = prosecutionNo;
    }

    private String prosecutionNo = "";

    public String getRono() {
        return rono;
    }

    public void setRono(String rono) {
        this.rono = rono;
    }

    private String rono;

    public Boolean getStockTake() {
        return stockTake;
    }

    public void setStockTake(Boolean stockTake) {
        this.stockTake = stockTake;
    }

    private Boolean stockTake;

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

    private String remarks;
    private String pic;

    public Boolean getOrverdue() {
        return orverdue;
    }

    public void setOrverdue(Boolean orverdue) {
        this.orverdue = orverdue;
    }

    private Boolean orverdue;

    private String returndate;
    private int type;
    private boolean found;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public boolean getFound() {
        return found;
    }

    public int getId() {
        return id;
    }

    public Boolean getOverdue() {
        if(orverdue != null)
            return orverdue;

        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.orverdue = orverdue;
        this.overdue = overdue;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Boolean getDisposed() {
        return disposed;
    }

    public void setDisposed(Boolean disposed) {
        this.disposed = disposed;
    }

    public String getReturnDate() {
        return returndate;
    }

    public void setReturnDate(String returnDate) {
        this.returndate = returnDate;
    }


    public Boolean getBorrowed() {
        return borrowed;
    }

    public void setBorrowed(Boolean borrowed) {
        this.borrowed = borrowed;
    }

    public String getStatusid() {
        return statusid;
    }

    public void setStatusid(String statusid) {
        this.statusid = statusid;
    }

    public String getStatusname() {
        return statusname;
    }

    public void setStatusname(String statusname) {
        this.statusname = statusname;
    }

    public int getCategorySize() {
        if(category == null || category.length() == 0)
            return 0;

        try {
            return category.split("->").length;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getLocationSize() {
        if(location == null || location.length() == 0) {
            return 0;
        }

        try {
            return location.split("->").length;
        } catch (Exception e) {
            return 0;
        }
    }

    public ArrayList<String> getLocations() {
        if(location == null || location.length() == 0) {
            return new ArrayList<>();
        }

        try {
            ArrayList<String> locationList = new ArrayList<>();

            for(int i = 0; i < location.split("->").length; i++){
                locationList.add(location.split("->")[i]);
            }

            return locationList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getCategorys() {
        if(category == null || category.length() == 0) {
            return new ArrayList<>();
        }

        try {
            ArrayList<String> categoryList = new ArrayList<>();

            for(int i = 0; i < category.split("->").length; i++){
                categoryList.add(category.split("->")[i]);
            }

            return categoryList;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getAssetNo() {
        if(assetno != null) {
            return assetno;
        }
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
}
