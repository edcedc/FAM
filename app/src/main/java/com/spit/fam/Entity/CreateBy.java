package com.spit.fam.Entity;

public class CreateBy {
    private int id;

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    private String createdById;
    private String firstname;
    private String lastname;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        if(firstname == null)
            return "";
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        if(lastname == null)
            return "";
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }


    public String getName() {
        if(name != null) {
            return  name;
        }
        return getFirstname() + getLastname();
    }

    public void setName(String name) {
        this.name = name;
    }
}
