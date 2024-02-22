package com.spit.fam.Entity.OfflineMode;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BorrowAssets extends RealmObject {
    private String userid;
    private String companyid;
    private String borrowno;
    private String borrowList;
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

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    public String getBorrowList() {
        return borrowList;
    }

    public void setBorrowList(String borrowList) {
        this.borrowList = borrowList;
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
