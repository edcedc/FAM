package com.spit.fam.Entity.OfflineMode;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DisposalAssets extends RealmObject {

    private String userid;
    private String companyid;
    private String disposalNo;
    private String disposalList;
    private String name;

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

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
    }

    public String getDisposalList() {
        return disposalList;
    }

    public void setDisposalList(String disposalList) {
        this.disposalList = disposalList;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
