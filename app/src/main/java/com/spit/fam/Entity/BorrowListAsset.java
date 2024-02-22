package com.spit.fam.Entity;

import java.util.ArrayList;

public class BorrowListAsset {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getEstimatedLifeTime() {
        return EstimatedLifeTime;
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

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private String id;
    private String assetno;
    private String name;
    private String brand;
    private String Model;
    private String SerialNo;
    private String Unit;
    private String LastStockDate;
    private String CreateDate;
    private String PurchaseDate;
    private String InvoiceDate;
    private String InvoiceNo;
    private String Supplier;
    private String MaintenanceDate;
    private String Cost;
    private String PracticalValue;
    private int EstimatedLifeTime;
    private String Barcode;
    private String EPC;
    private int status;
    private int location;
    private int category;

    public static ArrayList<BorrowListAsset> convertToBorrowListAssetList(ArrayList<Asset> asset) {
        ArrayList<BorrowListAsset> temp = new ArrayList<>();

        for(int i = 0; i < asset.size(); i++) {
            temp.add(convertToBorrowListAsset(asset.get(i)));
        }

        return temp;
    }

    public static BorrowListAsset convertToBorrowListAsset(Asset asset) {
        BorrowListAsset borrowListAsset = new BorrowListAsset();
        borrowListAsset.id = asset.getId();
        borrowListAsset.assetno = asset.getAssetno();
        borrowListAsset.name = asset.getName();
        borrowListAsset.brand = asset.getBrand();
        borrowListAsset.Model = asset.getModel();
        borrowListAsset.SerialNo = asset.getSerialNo();
        borrowListAsset.Unit = asset.getUnit();
        borrowListAsset.LastStockDate = asset.getLastStockDate();
        borrowListAsset.CreateDate = asset.getCreateDate();
        borrowListAsset.PurchaseDate = asset.getPurchaseDate();
        borrowListAsset.InvoiceNo = asset.getInvoiceNo();
        borrowListAsset.InvoiceDate = asset.getInvoiceDate();
        borrowListAsset.Supplier = asset.getSupplier();
        borrowListAsset.MaintenanceDate = asset.getMaintenanceDate();
        borrowListAsset.Cost = asset.getCost();
        borrowListAsset.PurchaseDate = asset.getPracticalValue();
        try {
            int count = Integer.parseInt(asset.getEstimatedLifeTime());
            borrowListAsset.EstimatedLifeTime = count;
        } catch (Exception e) {

        }

        borrowListAsset.Barcode = asset.getBarcode();
        borrowListAsset.EPC = asset.getEPC();

        if(asset.getStatus() != null)
            borrowListAsset.status = asset.getStatus().id;
        try {
            if (asset.getLocations() != null)
                borrowListAsset.status = asset.getLocations().get(0).getId();//.id;
        } catch (Exception e ){}

        try {
            if (asset.getStatus() != null)
                borrowListAsset.status = asset.getStatus().id;
        } catch (Exception e) {}

        return borrowListAsset;
    }
}
