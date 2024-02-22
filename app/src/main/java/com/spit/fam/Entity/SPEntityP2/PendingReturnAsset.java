package com.spit.fam.Entity.SPEntityP2;

import java.util.ArrayList;

public class PendingReturnAsset {
    private String companyId;
    private String userid;
    private String firstLocation;
    private String lastLocation;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
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

    public String getReturnList() {
        String returnList = "";

        for(int i = 0; i < getReturnArrayList().size(); i++) {
            returnList += getReturnArrayList().get(i) + (i != getReturnArrayList().size() - 1 ? "," : "");
        }

        return returnList;
    }

    public void setReturnList(String returnList) {
        this.returnList = returnList;
    }

    private String returnList;

    public ArrayList<String> getReturnArrayList() {
        return returnArrayList;
    }

    public void setReturnArrayList(ArrayList<String> returnArrayList) {
        this.returnArrayList = returnArrayList;
    }

    private ArrayList<String> returnArrayList = new ArrayList<>();


}
