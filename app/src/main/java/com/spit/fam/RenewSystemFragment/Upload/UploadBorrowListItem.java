package com.spit.fam.RenewSystemFragment.Upload;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UploadBorrowListItem extends RealmObject {
    //companyId, userid, waitingListIdString, BORROW_NO
    private String companyid;
    private String userid;
    private String waitinglist;
    private String borrowno;

    @PrimaryKey
    private String pk;

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

    public String getWaitinglist() {
        return waitinglist;
    }

    public void setWaitinglist(String waitinglist) {
        this.waitinglist = waitinglist;
    }

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }
}
