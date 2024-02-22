package com.spit.fam.Entity.SpEntity;

import android.util.Log;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.Status;

import java.util.ArrayList;
import java.util.List;

public class StockTakeAsset {
    private String AssetNo;
    private String LastAssetNo;
    private String AssetName;
    private String Brand;
    private String ModelNo;
    private String SerialNo;
    private String InvoiceNo;
    private String InvoiceDate;
    private String Unit;
    private String CategoryName;

    public String getProsecutionNo() {
        return prosecutionNo;
    }

    public void setProsecutionNo(String prosecutionNo) {
        this.prosecutionNo = prosecutionNo;
    }

    private String prosecutionNo;

    public String getStatusid() {
        return statusid;
    }

    public void setStatusid(String statusid) {
        this.statusid = statusid;
    }

    private String statusid;

    public ArrayList<String> getPic() {
        return pic;
    }

    public void setPic(ArrayList<String> pic) {
        this.pic = pic;
    }

    public void setPic(List<String> pic) {
        this.pic.clear();
        for(int i = 0; i < pic.size(); i++) {
            this.pic.add(pic.get(i));
        }
    }

    public String getPicsite() {
        return picsite;
    }

    public void setPicsite(String picsite) {
        this.picsite = picsite;
    }

    private String remarks;
    private ArrayList<String> pic = new ArrayList<>();
    private String picsite;

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    private boolean found;

    public String getAssetNo() {
        return AssetNo;
    }

    public void setAssetNo(String assetNo) {
        AssetNo = assetNo;
    }

    public String getLastAssetNo() {
        return LastAssetNo;
    }

    public void setLastAssetNo(String lastAssetNo) {
        LastAssetNo = lastAssetNo;
    }

    public String getAssetName() {
        return AssetName;
    }

    public void setAssetName(String assetName) {
        AssetName = assetName;
    }

    public String getBrand() {
        return Brand;
    }

    public void setBrand(String brand) {
        Brand = brand;
    }

    public String getModelNo() {
        return ModelNo;
    }

    public void setModelNo(String modelNo) {
        ModelNo = modelNo;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public String getInvoiceNo() {
        return InvoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        InvoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return InvoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        InvoiceDate = invoiceDate;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }

    public String getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(String categoryID) {
        CategoryID = categoryID;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public String getFundingSource() {
        return FundingSource;
    }

    public void setFundingSource(String fundingSource) {
        FundingSource = fundingSource;
    }

    public String getCost() {
        return Cost;
    }

    public void setCost(String cost) {
        Cost = cost;
    }

    public String getSupplier() {
        return Supplier;
    }

    public void setSupplier(String supplier) {
        Supplier = supplier;
    }

    public String getPurchaseDate() {
        return PurchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        PurchaseDate = purchaseDate;
    }

    public String getGLN() {
        return GLN;
    }

    public void setGLN(String GLN) {
        this.GLN = GLN;
    }

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public String getGIAI_GRAI() {
        return GIAI_GRAI;
    }

    public void setGIAI_GRAI(String GIAI_GRAI) {
        this.GIAI_GRAI = GIAI_GRAI;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String remarks) {
        Remarks = remarks;
    }

    public String getScanDate() {
        return ScanDate;
    }

    public void setScanDate(String scanDate) {
        ScanDate = scanDate;
    }

    public String getFirst_Cat_RoNo() {
        return First_Cat_RoNo;
    }

    public void setFirst_Cat_RoNo(String first_Cat_RoNo) {
        First_Cat_RoNo = first_Cat_RoNo;
    }

    public String getLast_Cat_RoNo() {
        return Last_Cat_RoNo;
    }

    public void setLast_Cat_RoNo(String last_Cat_RoNo) {
        Last_Cat_RoNo = last_Cat_RoNo;
    }

    public String getFirst_Loc_RoNo() {
        return First_Loc_RoNo;
    }

    public void setFirst_Loc_RoNo(String first_Loc_RoNo) {
        First_Loc_RoNo = first_Loc_RoNo;
    }

    public String getLast_Loc_RoNo() {
        return Last_Loc_RoNo;
    }

    public void setLast_Loc_RoNo(String last_Loc_RoNo) {
        Last_Loc_RoNo = last_Loc_RoNo;
    }

    private String LocationName, CategoryID, LocationID, FundingSource, Cost, Supplier, PurchaseDate,GLN,EPC,Barcode, GIAI_GRAI,Remarks,ScanDate,First_Cat_RoNo,Last_Cat_RoNo,First_Loc_RoNo,Last_Loc_RoNo;

    public Asset convertToAsset() {
        Log.i("asset", "asset " + getAssetNo() + " " + getAssetName() + " " + getLastAssetNo() + " " + getBrand() + " " + getModelNo() + " "
                + getSerialNo() + " " + getInvoiceNo() + " " + getInvoiceDate() + " " + getUnit() + " " + getFundingSource() + " " + getLocationName() + " " + getLocationID()
                + " " + getCategoryName() + " " + getCost() + " " + getSupplier() + " " + getPurchaseDate() + " " + getBarcode() + " " + getEPC());

        Asset asset = new Asset();
        asset.setRemarks(getRemarks());
        asset.setPic(getPic());
        asset.setPicsite(getPicsite());

        asset.setAssetno(getAssetNo());
        asset.setName(getAssetName());
        asset.setLastAssetNo(getLastAssetNo());
        asset.setBrand(getBrand());
        asset.setModel(getModelNo());
        asset.setSerialNo(getSerialNo());
        asset.setInvoiceNo(getInvoiceNo());
        asset.setInvoiceDate(getInvoiceDate());
        asset.setUnit(getUnit());
        asset.setGIAI_GRAI(getGIAI_GRAI());
        asset.setFundingSource(getFundingSource());
        asset.setProsecutionNo(getProsecutionNo());
        asset.setFound(isFound());

        Location location = new Location();
        location.setName(getLocationName());
        location.setLocationId(getLocationID());
        ArrayList<Location> locationArrayList = new ArrayList<>();
        locationArrayList.add(location);
        asset.setLocations(locationArrayList);


        Category category = new Category();

        category.setName(getCategoryName());
        category.setLocationId(getCategoryID());
        ArrayList<Category> categoryArrayList = new ArrayList<>();
        categoryArrayList.add(category);
        asset.setCategories(categoryArrayList);

        //LOCATION / CATEGORY / FUNDING SOURCE
        asset.setCost(getCost());
        asset.setSupplier(getSupplier());
        asset.setPurchaseDate(getPurchaseDate());
        //GLN // EPC //
        asset.setBarcode(getBarcode());
        asset.setEPC(getEPC());

        Status status = new Status();
        try {
            status.id = Integer.parseInt(statusid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        asset.setStatus(status);

        if(status.id == 2){
            asset.setFindType("uploaded");
        }

        //GIAI GRAI
        //REMARK
        //SCAN DATE + 4
        return asset;
    }
}
