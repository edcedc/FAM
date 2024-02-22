package com.spit.fam.Entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public class Asset {
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ArrayList<String> getPic() {
        return pic;
    }

    public void setPic(ArrayList<String> pic) {
        this.pic = pic;
    }

    public String getPicsite() {
        return picsite;
    }

    public void setPicsite(String picsite) {
        this.picsite = picsite;
    }

    String picsite;
    String remarks;
    ArrayList<String> pic = new ArrayList<>();

    public String getLastAssetNo() {
        return LastAssetNo;
    }

    public void setLastAssetNo(String lastAssetNo) {
        LastAssetNo = lastAssetNo;
    }


    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public boolean getFound() {
        return found;
    }

    public boolean isEPCOnly() {
        return EPCOnly;
    }

    public void setEPCOnly(boolean EPCOnly) {
        this.EPCOnly = EPCOnly;
    }


    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public CreateBy getCreated_by() {
        return created_by;
    }

    public void setCreated_by(CreateBy created_by) {
        this.created_by = created_by;
    }


    public String getBrand() {
        return brand;
    }

    public String getBrandForSearch() {
        if(brand == null) {
            return "";
        }
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }


    @SerializedName("id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    public int getIntegerId() {
        return Integer.parseInt(id);
    }

    public void setId(String id) {
        this.id = id ;
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

    public String getNameForSearch() {
        if(name == null)
            return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return Model;
    }

    public String getModelForSearch() {
        if(Model == null) {
            return "";
        }
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getLastStockDate() {
        return LastStockDate;
    }

    public void setLastStockDate(String lastStockDate) {
        LastStockDate = lastStockDate;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public String getPurchaseDate() {
        return PurchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        PurchaseDate = purchaseDate;
    }

    public String getInvoiceDate() {
        return InvoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        InvoiceDate = invoiceDate;
    }

    public String getInvoiceNo() {
        return InvoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        InvoiceNo = invoiceNo;
    }

    public String getSupplier() {
        return Supplier;
    }

    public void setSupplier(String supplier) {
        Supplier = supplier;
    }

    public String getMaintenanceDate() {
        return MaintenanceDate;
    }

    public void setMaintenanceDate(String maintenanceDate) {
        MaintenanceDate = maintenanceDate;
    }

    public String getCost() {
        return Cost;
    }

    public void setCost(String cost) {
        Cost = cost;
    }

    public String getPracticalValue() {
        return PracticalValue;
    }

    public void setPracticalValue(String practicalValue) {
        PracticalValue = practicalValue;
    }

    public String getEstimatedLifeTime() {
        if(EstimatedLifeTime == -99999)
            return "";
        return "" +EstimatedLifeTime;
    }

    public void setEstimatedLifeTime(int estimatedLifeTime) {
        EstimatedLifeTime = estimatedLifeTime;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public TagType getTag_type() {
        if(tag_type == null)
            return new TagType();

        return tag_type;
    }

    public void setTag_type(TagType tag_type) {
        this.tag_type = tag_type;
    }

    public Status getStatus() {
        if(status == null)
            return new Status();

        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCategoryString() {
        String categoryString = "";

        for(int i = 0; i < getCategories().size(); i++) {
            categoryString += getCategories().get(i).getName() + (i == getCategories().size() - 1 ? "" : "/");
        }

        return categoryString;
    }

    public String getLocationString() {
        String LocationString = "";

        for(int i = 0; i < getLocations().size(); i++) {
            LocationString += getLocations().get(i).getName() + (i == getLocations().size() - 1 ? "" : "/");
        }

        return LocationString;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public ArrayList<UserGroup> getUser_groups() {
        return user_groups;
    }

    public void setUser_groups(ArrayList<UserGroup> user_groups) {
        this.user_groups = user_groups;
    }


    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }


    public String getFundingSource() {
        return FundingSource;
    }

    public void setFundingSource(String fundingSource) {
        FundingSource = fundingSource;
    }

    private String FundingSource;

    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Location> locations = new ArrayList<>();
    private ArrayList<UserGroup> user_groups = new ArrayList<>();

    public Integer getStock_take_asset_item_remark() {
        return stock_take_asset_item_remark;
        //return stock_take_asset_item_remark;
    }

    public void setStock_take_asset_item_remark(Integer stock_take_asset_item_remark) {
        this.stock_take_asset_item_remark = stock_take_asset_item_remark;
    }


    public Integer getStockTakeId() {
        return stockTakeId;
    }

    public void setStockTakeId(Integer stockTakeId) {
        this.stockTakeId = stockTakeId;
    }


    public String getGIAI_GRAI() {
        return GIAI_GRAI;
    }

    public void setGIAI_GRAI(String GIAI_GRAI) {
        this.GIAI_GRAI = GIAI_GRAI;
    }

    private String GIAI_GRAI;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    private String orderNo;

    public boolean isFoundInStockTakeList() {
        return foundInStockTakeList;
    }

    public void setFoundInStockTakeList(boolean foundInStockTakeList) {
        this.foundInStockTakeList = foundInStockTakeList;
    }


    public boolean isFoundInSearchedEPCList() {
        return isFoundInSearchedEPCList;
    }

    public void setFoundInSearchedEPCList(boolean foundInSearchedEPCList) {
        isFoundInSearchedEPCList = foundInSearchedEPCList;
    }

    public String getNewEPC() {
        return newEPC;
    }

    public void setNewEPC(String newEPC) {
        this.newEPC = newEPC;
    }

    private String LastAssetNo;
    private boolean found;

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

    public void setFoundByManual(){
        findType = "manual";
    }

    public String getFindType() {
        return findType;
    }

    public void setFindType(String findType) {
        this.findType = findType;
    }

    private String findType ="";

    public boolean EPCOnly;
    public String EPC;
    private String newEPC;
    public CreateBy created_by;
    private String brand;
    private String assetno;
    private String name;
    private String Model;
    private String SerialNo;
    private String Unit;
    private String LastStockDate;
    private String CreateDate;
    private String created_at;
    private String updated_at;
    private String PurchaseDate;
    private String InvoiceDate;
    private String InvoiceNo;
    private String Supplier;
    private String MaintenanceDate;
    private String Cost;
    private String PracticalValue;
    private int EstimatedLifeTime;
    private String Barcode;
    private TagType tag_type;
    private Status status;
    private Integer stock_take_asset_item_remark = 0;
    private Integer stockTakeId;
    private boolean foundInStockTakeList;
    private boolean isFoundInSearchedEPCList = false;
    private String firstCat = "";
    private String lastCat = "";
    private String firstLocation = "";
    private String lastLocation = "";

    public String getProsecutionNo() {
        return prosecutionNo;
    }

    public void setProsecutionNo(String prosecutionNo) {
        this.prosecutionNo = prosecutionNo;
    }

    private String prosecutionNo = "";

    public Date getScanDateTime() {
        if(scanDateTime == null) {
            return new Date();
        }

        return scanDateTime;
    }

    public void setScanDateTime(Date scanDateTime) {
        this.scanDateTime = scanDateTime;
    }

    private Date scanDateTime = null;

    public String getUsergroup() {
        return usergroup;
    }

    public void setUsergroup(String usergroup) {
        this.usergroup = usergroup;
    }

    public String getPossessor() {
        return possessor;
    }

    public void setPossessor(String possessor) {
        this.possessor = possessor;
    }

    private String usergroup;
    private String possessor;

    public String getRono() {
        return rono;
    }

    public void setRono(String rono) {
        this.rono = rono;
    }

    private String rono;

    public boolean isAbnormal() {
        return abnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.abnormal = abnormal;
    }

    private boolean abnormal;

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    private boolean overdue = false;

    public String getFirstCat() {
        return firstCat;
    }

    public void setFirstCat(String firstCat) {
        this.firstCat = firstCat;
    }

    public String getLastCat() {
        return lastCat;
    }

    public void setLastCat(String lastCat) {
        this.lastCat = lastCat;
    }

    public String getFirstLocation() {
        return firstLocation;
    }

    public void setFirstLocation(String firstLocation) {
        this.firstLocation = firstLocation;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getReturndate() {
        return returndate;
    }

    public void setReturndate(String overdueDate) {
        this.returndate = returndate;
    }

    private String returndate;


    public String getCertType() {
        if(certType == null)
            return "";

        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    private String certType, certUrl, startdate, enddate;

    public String getCerstatus() {
        return cerstatus;
    }

    public void setCerstatus(String cerstatus) {
        this.cerstatus = cerstatus;
    }

    public boolean isIsverified() {
        return isverified;
    }

    public void setIsverified(boolean isverified) {
        this.isverified = isverified;
    }

    private String cerstatus;
    private boolean isverified;

    private String exhibitsource;

    public String getExhibitsource() {
        return exhibitsource;
    }

    public void setExhibitsource(String exhibitsource) {
        this.exhibitsource = exhibitsource;
    }

    public String getExhibitwitness() {
        return exhibitwitness;
    }

    public void setExhibitwitness(String exhibitwitness) {
        this.exhibitwitness = exhibitwitness;
    }

    public String getLastassetno() {
        return lastassetno;
    }

    public void setLastassetno(String lastassetno) {
        this.lastassetno = lastassetno;
    }

    private String exhibitwitness;
    private String lastassetno;


}
