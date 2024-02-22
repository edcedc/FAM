package com.spit.fam.Entity.RenewEntity;

public class ManualUpdateEvent {
    private String assetNo;
    private int statudID;

    public String getPic() {
        return pic;
    }

    public void setPic(String tempPic) {
        this.pic = tempPic;
    }

    private String pic;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    private String remark;

    public int getStatudID() {
        return statudID;
    }

    public void setStatudID(int statudID) {
        this.statudID = statudID;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }
}
