package com.spit.fam.Entity.SPEntityP3;

import java.util.Date;

import io.realm.RealmObject;

public class StocktakeList extends RealmObject {
    private int id, total,progress;
    private String stocktakeno, name, startDate, endDate, lastUpdate,remarks, companyid, userid;

    public Date getEndDateObj() {
        return endDateObj;
    }

    public void setEndDateObj(Date approvalDateObj) {
        this.endDateObj = approvalDateObj;
    }

    public Date getStartDateObj() {
        return endDateObj;
    }

    public void setStartDateObj(Date startDate) {
        this.startDateObj = startDate;
    }

    private Date endDateObj;
    private Date startDateObj;

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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
