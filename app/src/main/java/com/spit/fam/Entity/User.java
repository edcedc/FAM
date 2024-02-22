package com.spit.fam.Entity;

import java.util.ArrayList;

public class User {
    public ArrayList<BorrowListAsset> getBorrowed_assets() {
        return borrowed_assets;
    }

    public void setBorrowed_assets(ArrayList<BorrowListAsset> borrowed_assets) {
        this.borrowed_assets = borrowed_assets;
    }

    private ArrayList<BorrowListAsset> borrowed_assets = new ArrayList<>();
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    private int id;
    private String username;
    private String firstname,lastname;

    private String email;
    private UserGroup user_group;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserGroup getUser_group() {
        return user_group;
    }

    public void setUser_group(UserGroup user_group) {
        this.user_group = user_group;
    }
}
