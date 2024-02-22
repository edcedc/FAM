package com.spit.fam.Entity.SpEntity;

public class UploadStockTakeData {
    private String companyID;
    private String strJson;
    private StrJson strJsonObject;
    private String stockTakeListName;

    public String getStockTakeName() {
        return stockTakeListName;
    }

    public void setStockTakeName(String stockTakeName) {
        this.stockTakeListName = stockTakeName;
    }


    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getStrJson() {
        return strJson;
    }

    public void setStrJson(String strJson) {
        this.strJson = strJson;
    }
    public StrJson getStrJsonObject() {
        return strJsonObject;
    }

    public void setStrJsonObject(StrJson strJsonObject) {
        this.strJsonObject = strJsonObject;
    }

}
