package com.spit.fam.Entity.OfflineMode;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ReturnAssets extends RealmObject {
    private String userid;
    private String companyid;
    private String firstlocation;
    private String lastlocation;
    private String returnList;

    @PrimaryKey
    private String pk;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getFirstlocation() {
        return firstlocation;
    }

    public void setFirstlocation(String firstlocation) {
        this.firstlocation = firstlocation;
    }

    public String getLastlocation() {
        return lastlocation;
    }

    public void setLastlocation(String lastlocation) {
        this.lastlocation = lastlocation;
    }

    public String getReturnList() {
        return returnList;
    }

    public void setReturnList(String returnList) {
        this.returnList = returnList;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }
}
