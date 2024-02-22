package com.spit.fam.Entity.SPEntityP3;

import java.util.ArrayList;

public class DisposalDetailResponse  {
    /*
      "totalCount": 1,
  "name": "SPIT/ST/FE/0000000092",
  "disposalNo": "0000000086",

     */
    public ArrayList<DisposalAsset> getData() {
        return data;
    }

    public void setData(ArrayList<DisposalAsset> data) {
        this.data = data;
    }

    ArrayList<DisposalAsset> data = new ArrayList<>();

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
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
    private String companyid;

    private String userid;
    private int totalCount;
    private String name;
    private String disposalNo;

 }
