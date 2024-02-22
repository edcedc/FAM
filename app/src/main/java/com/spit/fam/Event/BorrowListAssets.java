package com.spit.fam.Event;

import com.spit.fam.Entity.SPEntityP2.BriefAsset;

import java.util.ArrayList;

public class BorrowListAssets {
    private String name;
    private String borrowno;
    private String stocktakeno;

    public String getPicsite() {
        return picsite;
    }

    public void setPicsite(String picsite) {
        this.picsite = picsite;
    }

    private String picsite;

    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }


    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
    }

    private String disposalNo;
    private ArrayList<BriefAsset> data = new ArrayList<>();

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

    public ArrayList<BriefAsset> getData() {
        return data;
    }

    public void setData(ArrayList<BriefAsset> data) {
        this.data = data;
    }

}
