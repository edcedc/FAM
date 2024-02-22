package com.spit.fam.Request;

public class SPUpdateWaitingListRequest {
    private String userid;
    private String borrowList;// = new ArrayList<>();
    private String borrowno;

    public String getBorrowList() {
        return borrowList;
    }

    public void setBorrowList(String borrowList) {
        this.borrowList = borrowList;
    }

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    private String companyid;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
