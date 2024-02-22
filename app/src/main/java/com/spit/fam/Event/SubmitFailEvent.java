package com.spit.fam.Event;

public class SubmitFailEvent {
    String companyId;
    String userid;
    String firstLocation;
    String lastLocation;
    String returnList;

    public SubmitFailEvent(){

    }

    public SubmitFailEvent(String companyId,
            String userid,
            String firstLocation,
            String lastLocation,
            String returnList) {
        this.companyId = companyId;
        this.userid = userid;
        this.firstLocation = firstLocation;
        this.lastLocation = lastLocation;
        this.returnList = returnList;

    }

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
        return returnList;
    }

    public void setReturnList(String returnList) {
        this.returnList = returnList;
    }
}
