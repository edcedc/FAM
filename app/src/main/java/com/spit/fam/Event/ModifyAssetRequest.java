package com.spit.fam.Event;

public class ModifyAssetRequest {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEPC() {
        return EPC;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    private String EPC;

    public String getOriginalEPC() {
        return originalEPC;
    }

    public void setOriginalEPC(String originalEPC) {
        this.originalEPC = originalEPC;
    }

    private String originalEPC;

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

    public String getAssetno() {
        return assetno;
    }

    public void setAssetno(String assetno) {
        this.assetno = assetno;
    }

    private String assetno;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    private String companyId;

    public boolean changeEPC = false;
    public boolean setEPC = false;
}
