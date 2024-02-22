package com.spit.fam.Entity;

public class PhotoFormat {
    public PhotoSize getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(PhotoSize thumbnail) {
        this.thumbnail = thumbnail;
    }

    public PhotoSize getMedium() {
        return medium;
    }

    public void setMedium(PhotoSize medium) {
        this.medium = medium;
    }

    public PhotoSize getSmall() {
        return small;
    }

    public void setSmall(PhotoSize small) {
        this.small = small;
    }

    private PhotoSize thumbnail;
    private PhotoSize medium;
    private PhotoSize small;

}
