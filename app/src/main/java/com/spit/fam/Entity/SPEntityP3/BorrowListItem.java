package com.spit.fam.Entity.SPEntityP3;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BorrowListItem extends RealmObject {
    public Long getTimeString() {
        return timeString;
    }

    public void setTimeString(Long timeString) {
        this.timeString = timeString;
    }
    private Long timeString;

    /*{"id":26,"borrowno":"0000000055","name":"borrow","applyDate":"02/12/2020","approvalDate":"02/12/2020","approvedby":"Admin","validDate":"31/12/2020","total":4,"borrowed":4,"approved":4},*/

    private String companyid;
    private String userid;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @PrimaryKey
    String pk;

    private int id;
    private int total;
    private int borrowed;
    private int approved;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;
    private String borrowno,name,applyDate,approvalDate,approvedby,validDate;

    public Date getApprovalDateObj() {
        return approvalDateObj;
    }

    public void setApprovalDateObj(Date approvalDateObj) {
        this.approvalDateObj = approvalDateObj;
    }

    private Date approvalDateObj;
    private Date validDateObj;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getBorrowed() {
        return borrowed;
    }

    public void setBorrowed(int borrowed) {
        this.borrowed = borrowed;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(String applyDate) {
        this.applyDate = applyDate;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getApprovedby() {
        return approvedby;
    }

    public void setApprovedby(String approvedby) {
        this.approvedby = approvedby;
    }

    public String getValidDate() {
        return validDate;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }


    public Date getValidDateObj() {
        return validDateObj;
    }

    public void setValidDateObj(Date validDateObj) {
        this.validDateObj = validDateObj;
    }


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


}
