package com.spit.fam.Entity;

import android.util.Log;

import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.InternalStorage;
import com.spit.fam.Response.LoginResponse;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

public class BorrowList {
    private int id;
    private String borrowno;

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
    }

    public String getDisposalName() {
        return getDisposalNo() + " | " + getName();
    }

    private String disposalNo;

    private String created_at, updated_at;
    private User created_by;
    private User updated_by;
    private String valid_date;
    private String approved_date;
    private BorrowListUser approved;

    public String getApprovedby() {
        return approvedby;
    }

    public void setApprovedby(String approvedby) {
        this.approvedby = approvedby;
    }

    private String approvedby;

    public String getApprovedString() {
        return approvedString;
    }

    public void setApprovedString(String approvedString) {
        this.approvedString = approvedString;
    }

    private String approvedString;

    private Status borrow_status;
    private Status disposal_status;
    private ArrayList<BorrowListAsset> assets = new ArrayList<>();
    private String name;

    private String total;
    private String borrowed;

    public String getBorrowno() {
        return borrowno;
    }

    public void setBorrowno(String borrowno) {
        this.borrowno = borrowno;
    }

    public String getTotal() {
        return total;
    }

    public int getTotalCount() {
        return Integer.parseInt(total);
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getBorrowed() {
        return borrowed;
    }

    public void setBorrowed(String borrowed) {
        this.borrowed = borrowed;
    }

    public String getCreated_at() {
        return created_at;
    }
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public User getCreated_by() {
        return created_by;
    }

    public void setCreated_by(User created_by) {
        this.created_by = created_by;
    }

    public User getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(User updated_by) {
        this.updated_by = updated_by;
    }

    public Status getBorrow_status() {
        return borrow_status;
    }

    public void setBorrow_status(Status borrow_status) {
        this.borrow_status = borrow_status;
    }

    public ArrayList<BorrowListAsset> getAssets() {
        return assets;
    }

    public ArrayList<Asset> convertToAssetsList() {
        if(assets == null) {
            return new ArrayList<>();
        }

        ArrayList<Asset> myAssets = new ArrayList<>();

        for(int i = 0; i < assets.size(); i++) {
            if(Hawk.get(InternalStorage.Application.ASSET, new ArrayList<>()).size() > 0) {
                for(int y = 0; y < Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>()).size(); y++) {
                    if(assets.get(i).getId().equals( Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>()).get(y).getId() ) ) {
                        myAssets.add(Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>()).get(y));
                    }
                }
            }
        }

        return myAssets;
    }


    public static ArrayList<Asset> convertToAssetsList(ArrayList<BorrowListAsset> assets) {
        if(assets == null) {
            return new ArrayList<>();
        }

        ArrayList<Asset> myAssets = new ArrayList<>();

        for(int i = 0; i < assets.size(); i++) {
            if(Hawk.get(InternalStorage.Application.ASSET, new ArrayList<>()).size() > 0) {
                for(int y = 0; y < Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>()).size(); y++) {
                    if(assets.get(i).getId().equals( Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>()).get(y).getId() ) ) {
                        myAssets.add(Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>()).get(y));
                    }
                }
            }
        }

        return myAssets;
    }

    public void setAssets(ArrayList<BorrowListAsset> assets) {
        this.assets = assets;
    }

    //private User applied_by;

    public String getValid_date() {
        return valid_date;
    }

    public void setValid_date(String valid_date) {
        this.valid_date = valid_date;
    }


    public String getApproved_date() {
        return approved_date;
    }

    public void setApproved_date(String approved_date) {
        this.approved_date = approved_date;
    }

    public BorrowListUser getApproved() {
        return approved;
    }

    public void setApproved(BorrowListUser approved) {
        this.approved = approved;
    }

    public Status getDisposal_status() {
        return disposal_status;
    }

    public void setDisposal_status(Status disposal_status) {
        this.disposal_status = disposal_status;
    }

    public String getName() {
        if(borrowno != null && borrowno.length() > 0) {
            return borrowno + " | " + name;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<Asset> getBorrowedItem() {
         ArrayList<BorrowListAsset> borrowLists = new ArrayList<>();

        if(Hawk.get(InternalStorage.Login.USER, new User()) != null) {
            User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();

            if(user != null)
            if(user.getBorrowed_assets() != null && user.getBorrowed_assets().size() > 0) {
                for(int i = 0; i < user.getBorrowed_assets().size(); i++) {
                    for(int y = 0; y < assets.size(); y++) {
                        if(assets.get(y).getId().equals(user.getBorrowed_assets().get(i).getId())) {
                            borrowLists.add(assets.get(y));
                        }
                    }
                }
            }
        }

        ArrayList<Asset> list =  convertToAssetsList(borrowLists);

        for(int i = 0; i < list.size(); i++) {
            list.get(i).setFound(true);
        }

        return list;
    }

    public int getBorrowCount() {
        if (borrowed != null && borrowed.length() > 0) {
            return Integer.parseInt(borrowed);
        }
        return 0;
    }

    public int getBorrowTotal() {
        int count = 0;

        BorrowListAssets borrownoData = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + borrowno, new BorrowListAssets());

        for(int i = 0; i < borrownoData.getData().size(); i++) {
            if(borrownoData.getData().get(i).getType() == 1) {
                count ++;
            }
        }

        return count;
    }


    public int getDisposalTotal() {
        int count = 0;

        BorrowListAssets borrownoData = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + disposalNo, new BorrowListAssets());

        for(int i = 0; i < borrownoData.getData().size(); i++) {
            if(borrownoData.getData().get(i).getType() == 1) {
                count ++;
            }
        }

        return count;
    }

    public String getBorrowedCountString() {
        if(/*total != null && total.length() > 0 &&*/ borrowed != null && borrowed.length() > 0){

            int count = 0;

            BorrowListAssets borrownoData = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + borrowno, new BorrowListAssets());

            for(int i = 0; i < borrownoData.getData().size(); i++) {
                if(borrownoData.getData().get(i).getType() == 1) {
                    count ++;
                }
            }

            if(borrownoData.getData().size() == 0) {
                count = Integer.parseInt(total);
            }

            Log.i("count", "count " + count + " borrownoData " + borrownoData.getData().size() + " " + total);
            return borrowed + "/" + getApprovedString();
        }


        return getBorrowedItem().size() + "/" + assets.size();
    }


    public ArrayList<Asset> getDisposalItem() {
        ArrayList<BorrowListAsset> borrowLists = new ArrayList<>();


            ArrayList<Asset> myasset = Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>());

            Log.i("asset", "asset " + getAssets());

            for(int i = 0; i < getAssets().size(); i++) {
                for(int x = 0; x < myasset.size(); x++) {

                    Log.i("asset status check", "asset status check " + (myasset.get(x).getStatus().id == 3) + " " + myasset.get(x).getId());

                    if(myasset.get(x).getStatus().id == 3 && myasset.get(x).getId().equals(getAssets().get(i).getId())) {
                        borrowLists.add(assets.get(x));
                    }
                }
            }

            /*
            if(user.getBorrowed_assets() != null && user.getBorrowed_assets().size() > 0) {
                for(int i = 0; i < user.getBorrowed_assets().size(); i++) {
                    for(int y = 0; y < assets.size(); y++) {
                        if(user.getBorrowed_assets().get(i).getStatus() == 3 && assets.get(y).getId().equals(user.getBorrowed_assets().get(i).getId())) {
                            borrowLists.add(assets.get(y));
                        }
                    }
                }
            }
            */


        return convertToAssetsList(borrowLists);
    }


    public String getDisposalCountString() {

        if(total != null && total.length() > 0 && borrowed != null && borrowed.length() > 0){


            int count = 0;

            BorrowListAssets borrownoData = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + disposalNo, new BorrowListAssets());

            for(int i = 0; i < borrownoData.getData().size(); i++) {
                if(borrownoData.getData().get(i).getType() == 1) {
                    count ++;
                }
            }

            if(borrownoData.getData().size() == 0) {
                count = Integer.parseInt(total);
            }

            Log.i("count", "count " + count + " " + borrownoData.getData().size() + " " + disposalNo);
            return borrowed + "/" + approvedString;
           // return borrowed + "/" + total;
        }
        return getDisposalItem().size() + "/" + assets.size();
    }
}
