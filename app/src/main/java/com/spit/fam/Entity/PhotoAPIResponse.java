package com.spit.fam.Entity;

import com.google.gson.annotations.SerializedName;

public class PhotoAPIResponse {

    @SerializedName("kode")
    String kode;
    @SerializedName("pesan")
    String pesan;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    String filename;

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }
}
