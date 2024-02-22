package com.spit.fam.Entity.SPEntityP3;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DisposalListItem extends RealmObject {
/*{"id":74,"disposalNo":"0000000086","name":"SPIT/ST/FE/0000000092","applyDate":"26/04/2022","approvalDate":"26/04/2022","approvedby":"Admin","validDate":"30/04/2022","total":1,"disposed":0,"approved":1}]*/

    public Long getTimeString() {
        return timeString;
    }

    public void setTimeString(Long timeString) {
        this.timeString = timeString;
    }
    private Long timeString;

    private int id;
    private int total;
    private int disposed;
    private int approved;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type;
    private String disposalNo, name, applyDate, approvalDate, approvedby, validDate, companyid, userid;
    private Date approvalDateObj;
    private Date validDateObj;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @PrimaryKey
    private String pk;

    public Date getApprovalDateObj() {
        return approvalDateObj;
    }

    public void setApprovalDateObj(Date approvalDateObj) {
        this.approvalDateObj = approvalDateObj;
    }

    public Date getValidDateObj() {
        return validDateObj;
    }

    public void setValidDateObj(Date validDateObj) {
        this.validDateObj = validDateObj;
    }


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

    public int getDisposed() {
        return disposed;
    }

    public void setDisposed(int disposed) {
        this.disposed = disposed;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
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
