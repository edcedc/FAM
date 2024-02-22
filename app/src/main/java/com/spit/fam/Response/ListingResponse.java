package com.spit.fam.Response;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ListingResponse extends RealmObject {
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @PrimaryKey
    private String pk;

    private int catSize;
    private int locSize;

    public int getCatSize() {
        return catSize;
    }

    public void setCatSize(int catSize) {
        this.catSize = catSize;
    }

    public int getLocSize() {
        return locSize;
    }

    public void setLocSize(int locSize) {
        this.locSize = locSize;
    }
}
