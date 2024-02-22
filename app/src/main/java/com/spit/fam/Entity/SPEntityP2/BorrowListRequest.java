package com.spit.fam.Entity.SPEntityP2;

import java.util.ArrayList;

public class BorrowListRequest {
    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getWaitiList() {
        return waitiList;
    }

    public void setWaitiList(String waitiList) {
        this.waitiList = waitiList;
    }

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    //companyId, userid, waitingListIdString, BORROW_NO
    private String companyid;
    private String userid;
    private String waitiList;
    private String borrowno;

    public ArrayList<String> getAssetNoList() {
        return assetNoList;
    }

    public void setAssetNoList(ArrayList<String> assetNoList) {
        this.assetNoList = assetNoList;
    }

    private ArrayList<String> assetNoList = new ArrayList<String>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;
}
