package com.spit.fam.Entity.SPEntityP3;

import java.util.ArrayList;

public class BorrowDetailResponse {
    /*
    {
  "status": 0,
  "totalCount": 1,
  "name": "SPIT/ST/FE/0000000092",
  "borrowno": "0000000195",
  "borrowed": 1,
  "approved": 1,
  "data": [
    {
      "id": 22,
      "assetno": "SPIT/ST/FE/0000000092",
      "name": "9L 水劑滅火筒",
      "brand": "",
      "model": "",
      "category": "安全設備->滅火筒",
      "location": "香港->科學園->16W 205室",
      "epc": "E20210722170624000000001",
      "borrowed": true,
      "type": 1,
      "LastAssetNo" :""
    }
  ]
}
     */


    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
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

    public ArrayList<BorrowAsset> getData() {
        return data;
    }

    public void setData(ArrayList<BorrowAsset> data) {
        this.data = data;
    }

    private String borrowno;
    private int status, totalCount, borrowed, approved;
    private ArrayList<BorrowAsset> data = new ArrayList();

}
