package com.spit.fam.Entity;

import android.util.Log;

import com.spit.fam.Entity.SpEntity.UploadStockTakeData;
import com.spit.fam.InternalStorage;
import com.spit.fam.SystemFragment.LoginFragment;
import com.orhanobut.hawk.Hawk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class StockTakeList {
    private int id;

    public String getOrderNo() {
        return OrderNo;
    }

    public void setOrderNo(String orderNo) {
        OrderNo = orderNo;
    }

    public String getStocktakeno() {
        return stocktakeno;
    }

    public void setStocktakeno(String stocktakeno) {
        this.stocktakeno = stocktakeno;
    }

    private String stocktakeno;

    private String OrderNo;
    private String stocktakeid;
    private String name;
    private String startDate;
    private String endDate;
    public List<Asset> myAsset;
    private String lastUpdateTime;
    private String lastUpdate;

    public String getPicsite() {
        return picsite;
    }

    public void setPicsite(String picsite) {
        this.picsite = picsite;
    }

    private String picsite;

    public boolean isStockTake() {
        return stockTake;
    }

    public void setStockTake(boolean stockTake) {
        this.stockTake = stockTake;
    }

    private boolean stockTake;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getHasRemark() {
        return hasRemark;
    }

    public void setHasRemark(String hasRemark) {
        this.hasRemark = hasRemark;
    }

    private int total = -1;
    private int progress = -1;
    private String hasRemark;

    private ArrayList<StockTakeListItem> stock_take_list_items = new ArrayList<>();

    public String getLastUpdateTime() {
        return lastUpdate;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }



    public Date getStartDateObject(){
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");//2020-09-07

        try {
            Date result = parser.parse(startDate);
            return result;
        } catch (Exception e) {
            return  null;
        }

    }

    public Date getEndDateObject(){
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");//2020-09-07
        try {
            Date result = parser.parse(endDate);
            return result;
        } catch (Exception e) {
            return  null;
        }

    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStocktakeid() {
        return stocktakeid;
    }

    public void setStocktakeid(String stocktakeid) {
        this.stocktakeid = stocktakeid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public ArrayList<StockTakeListItem> getStockTakeListItems() {
        return stock_take_list_items;
    }

    public void setStockTakeListItems(ArrayList<StockTakeListItem> stockTakeListItems) {
        this.stock_take_list_items = stockTakeListItems;
    }

    public int getProgress() {
        if(progress >= 0)
            return progress;

        int count = 0;

        for(int i = 0; i < stock_take_list_items.size(); i++) {
            if(stock_take_list_items.get(i).isFound()) {
                count++;
            }
        }

        return count;
    }

    public int getTotalCount() {
        if(total >= 0)
            return total;
        return stock_take_list_items.size();
    }

    public List<Long> getAssetIds() {
        //String ids = "";
        List<Long> ids = new ArrayList<>();

        for(int i = 0; i < stock_take_list_items.size(); i++) {
            //ids += stock_take_list_items.get(i).getId() + ",";
            ids.add((long)stock_take_list_items.get(i).getAsset());
        }

        return ids;
    }

    public void setAsset(List<Asset> myAsset) {
        this.myAsset = myAsset;
    }

    public Asset getAssetByStockTakeListItem(StockTakeListItem stockTakeListItem) {

        if(myAsset != null && myAsset.size() > 0) {
            for (int i = 0; i < myAsset.size(); i++) {
                Log.i("compare", "compare " + myAsset.get(i).getId() + " " + stockTakeListItem.getAsset());

                if (myAsset.get(i).getId().equals(stockTakeListItem.getAsset() + "")) {
                    myAsset.get(i).setFound(stockTakeListItem.isFound());
                    int value = stockTakeListItem.getStock_take_asset_item_remark();
                    myAsset.get(i).setStock_take_asset_item_remark(value);
                    myAsset.get(i).setStockTakeId(stockTakeListItem.getId());
                    Log.i("found", "found " + myAsset.get(i).getName() + " " + stockTakeListItem.getStock_take_asset_item_remark());
                    return myAsset.get(i);
                }
            }

        }
        return null;
    }

    public List<Asset> proccessedAllAssets = new ArrayList();
    public List<Asset> proccessedReadAssets = new ArrayList();
    public List<Asset> proccessedUnReadAssets = new ArrayList();
    public List<Asset> proccessedAbmoralAssets = new ArrayList();

    private HashMap<String, Asset> readMap = new HashMap<>();
    public HashMap<String, String> checkExist = new HashMap<>();
    private HashMap<String, Asset> abnormalMap = new HashMap<>();

    public HashMap<String, Asset> getReadMap() {
        if(readMap.size() == 0) {
            getAssets();
        }
        return readMap;
    }

    public HashMap<String, Asset> getAbnormalMap() {
        if(abnormalMap.size() == 0) {
            getProccessedAbmoralAssets();
        }
        return abnormalMap;
    }

    public void addAssets(Asset asset) {
        myAsset.add(asset);

        proccessedAbmoralAssets.clear();
    }

    public int getScannedCount() {
        int x = 0;
        for(int i = 0; i < myAsset.size();i ++) {
            if(myAsset.get(i).getScanDateTime() != null) {
                x++;
            }
        }
        return x;
    }

    public Asset getAssetByEPC(String epc) {
        for(int i = 0; i < myAsset.size(); i++) {
            if(myAsset.get(i).getEPC().equals(epc)) {
                return myAsset.get(i);
            }
        }
        return null;
    }

    public List<Asset> getAssets() {
        if(proccessedAllAssets.size() > 0) {
            return proccessedAllAssets;
        }

        if(myAsset != null) {
            List<Asset> newAsset = new ArrayList<>();

            for(int i = 0; i < myAsset.size(); i++){
                if(myAsset.get(i).getStatus().id == 2 || myAsset.get(i).getStatus().id == 10) {
                    newAsset.add(myAsset.get(i));
                }

                if(myAsset.get(i).getStatus().id == 2 ||  myAsset.get(i).getStatus().id == 10) {
                    readMap.put(myAsset.get(i).getEPC(), myAsset.get(i));
                    checkExist.put(myAsset.get(i).getEPC(), myAsset.get(i).getAssetno());
                }
            }

            proccessedAllAssets = newAsset;

            return newAsset;
        }
        return new ArrayList<>();
    }

    public List<Asset> getReadAssets() {

        if(proccessedReadAssets.size() > 0) {
            return proccessedReadAssets;
        }

        if(myAsset != null) {
            List<Asset> newAsset = new ArrayList<>();

            for(int i = 0; i < myAsset.size(); i++){
                if(myAsset.get(i).getStatus().id == 2 ) {
                    newAsset.add(myAsset.get(i));
                }
            }

            proccessedReadAssets = newAsset;

            return newAsset;
        }
        return new ArrayList<>();
    }

    public List<Asset> getUnReadAssets() {

        if(proccessedUnReadAssets.size() > 0) {
            return proccessedUnReadAssets;
        }

        if(myAsset != null) {
            List<Asset> newAsset = new ArrayList<>();

            for(int i = 0; i < myAsset.size(); i++){
                if(myAsset.get(i).getStatus().id == 10 ) {
                    newAsset.add(myAsset.get(i));
                }
            }

            proccessedUnReadAssets = newAsset;

            return newAsset;
        }
        return new ArrayList<>();
    }


    public List<Asset> getProccessedAbmoralAssets() {

        if(proccessedAbmoralAssets.size() > 0) {
            Log.i("getProccessedAbmoralAssets", "getProccessedAbmoralAssets case 1");
            return proccessedAbmoralAssets;
        }

        if(myAsset != null) {
            List<Asset> newAsset = new ArrayList<>();

            for(int i = 0; i < myAsset.size(); i++){
                if(myAsset.get(i).getStatus().id == 9 && readMap.get(myAsset.get(i).getEPC()) == null) {

                    if(myAsset.get(i).getEPC() != null && myAsset.get(i).getEPC().length() > 0) {
                        Log.i("abnormal", "abnormal " + myAsset.get(i).getEPC());
                        newAsset.add(myAsset.get(i));
                        abnormalMap.put(myAsset.get(i).getEPC(), myAsset.get(i));
                    }
                }
            }

            proccessedAbmoralAssets = newAsset;


            Log.i("getProccessedAbmoralAssets", "getProccessedAbmoralAssets case 2 " + newAsset.size() + " " +myAsset.size());

            return newAsset;
        }
        return new ArrayList<>();
    }

    public List<Asset> getAbnormalAssets() {
        Log.i("myAsset", "myAsset " + myAsset + " " + stock_take_list_items);

        if(myAsset != null) {

            if(!LoginFragment.SP_API) {
                ArrayList<UploadStockTakeData> uploadStockTakeDataList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
                if (uploadStockTakeDataList != null && uploadStockTakeDataList.size() > 0) {
                    for (int y = 0; y < myAsset.size(); y++) {
                        for (int i = 0; i < uploadStockTakeDataList.size(); i++) {
                            Log.i("newAsset", "newAsset " + myAsset.get(y).getAssetno() + " " + uploadStockTakeDataList.get(i).getStrJsonObject().getAssetNo());

                            if (myAsset.get(y).getAssetno() != null && uploadStockTakeDataList.get(i).getStrJsonObject().getAssetNo() != null) {
                                if (myAsset.get(y).getAssetno().equals(uploadStockTakeDataList.get(i).getStrJsonObject().getAssetNo())) {
                                    myAsset.get(y).setFound(true);
                                    Log.i("newAsset", "newAsset " + myAsset.get(y).getAssetno());
                                }
                            }
                        }
                    }
                }
            }
            for(int i = 0; i < stock_take_list_items.size(); i++) {
                for(int y = 0; y < myAsset.size(); y++) {
                    Asset asset = getAssetByStockTakeListItem(stock_take_list_items.get(i));

                    if (asset != null) {
                        int myValue = asset.getStock_take_asset_item_remark();

                        if(asset.getId() == myAsset.get(y).getId()) {
                            myAsset.get(y).setStock_take_asset_item_remark(stock_take_list_items.get(i).remarkDetails);
                            break;
                        }
                    }
                }
            }

            List<Asset> newAsset = new ArrayList<>();
            Log.i("data", "datadatadata " + myAsset.size());

            for(int i = 0; i < myAsset.size(); i++){
                Log.i("data", "datadata " + myAsset.get(i).getStatus().id);
                if(myAsset.get(i).getStatus().id == 9 && myAsset.get(i).getEPC() != null && myAsset.get(i).getEPC().length() > 0 ) {
                    newAsset.add(myAsset.get(i));
                }
            }
            return newAsset;
        }

        List<Asset> newAsset = new ArrayList<>();

        for(int i = 0; i < stock_take_list_items.size(); i++) {
            Asset asset = getAssetByStockTakeListItem(stock_take_list_items.get(i));
            if(asset != null) {
                int myValue = asset.getStock_take_asset_item_remark();
                asset.setStock_take_asset_item_remark(myValue);

                newAsset.add(asset);
            }
        }

        return newAsset;
    }

}
