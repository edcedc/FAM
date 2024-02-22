package com.spit.fam.Entity;

public class SPUser {
    private String LoginID;
    private String Password;
    private String userid;

    public String getNfcCardNo() {
        if (staffcard == null)
            return "";

        return staffcard;
    }

    public void setNfcCardNo(String nfcCardNo) {
        this.staffcard = nfcCardNo;
    }

    private String staffcard;

    public String getLoginid() {
        return LoginID;
    }

    public void setLoginid(String loginid) {
        this.LoginID = loginid;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
