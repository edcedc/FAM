package com.spit.fam.Response;

import com.spit.fam.Entity.SPUser;
import com.spit.fam.InternalStorage;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

public class UserListResponse {
    private int count;
    private String thiscalldate;
    private ArrayList<SPUser> data = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getThiscalldate() {
        return thiscalldate;
    }

    public void setThiscalldate(String thiscalldate) {
        this.thiscalldate = thiscalldate;
    }

    public ArrayList<SPUser> getData() {
        if(data == null) {
            return new ArrayList<>();
        }
        return data;
    }

    public void setData(ArrayList<SPUser> data) {
        this.data = data;
    }

    public void setLocalUserId(String userName) {
        for(int i = 0; i < data.size(); i++) {
            if(data.get(i).getLoginid().toLowerCase().equals(userName.toLowerCase())) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID, data.get(i).getUserid());
            }
        }
    }
}
