package com.spit.fam.Entity;


import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;

public class Photo {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return  url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public PhotoFormat getFormats() {
        return formats;
    }

    public void setFormats(PhotoFormat formats) {
        this.formats = formats;
    }

    private String url;
    private int width, height;
    private PhotoFormat formats;

    public String getPhoto() {
        return AssetsDetailWithTabFragment.PIC_SITE + photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    private String photo;
    private String encodedString;

    public String getEncodedString() {
        return encodedString;
    }

    public void setEncodedString(String encodedString) {
        this.encodedString = encodedString;
    }


    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    private boolean deletable;
}
