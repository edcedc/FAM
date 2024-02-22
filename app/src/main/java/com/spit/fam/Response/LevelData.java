package com.spit.fam.Response;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LevelData extends RealmObject {
    public boolean isNewData() {
        return isNewData;
    }

    public void setNewData(boolean newData) {
        isNewData = newData;
    }

    private boolean isNewData;

    private String rono;

    public String getRono() {
        return rono;
    }

    public void setRono(String rono) {
        this.rono = rono;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    @PrimaryKey
    private String pk;

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    private String searchKey;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private int level;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFatherNo() {
        return fatherNo;
    }

    public void setFatherNo(String fatherNo) {
        this.fatherNo = fatherNo;
    }

    private String fatherNo;

    private String companyid;

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

    private String userid;

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    private int ordering;

}
