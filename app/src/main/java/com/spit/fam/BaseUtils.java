package com.spit.fam;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.realm.Realm;

import com.spit.fam.Entity.Item;
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.StockTakeListData;
import com.spit.fam.Event.InsertEvent;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.Event.ProgressEvent;
import com.spit.fam.Event.ReadFileCallbackEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.SystemFragment.LandRegisteryDownloadFragment;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.orhanobut.hawk.Hawk;

public class BaseUtils {
    public static int count = 0;
    private static String thiscalldate = "";
    public static int serverCount = 0;
    private static String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());
    private static String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

    public static void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)MainActivity.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void parseStockTakeJson(String path) {
        Realm.getDefaultInstance().beginTransaction();

        StockTakeListData stockTakeListData = new StockTakeListData();
        ArrayList<Item> list = new ArrayList<>();

        try {
            try (JsonParser jParser = new JsonFactory().createParser(new File(path));) {
                JsonToken current;
                current = jParser.nextToken();

                if (current != JsonToken.START_OBJECT) {
                    System.out.println("Error: root should be object: quiting.");
                    return;
                }


                while (jParser.nextToken() != JsonToken.END_OBJECT) {
                    String fn = jParser.getCurrentName();
                    jParser.nextToken();
                    Log.i("hihi", "hihi cerpath " + fn + " " + jParser.getText());

                    if (fn.equals("status")) {
                        stockTakeListData.setStatus((jParser.getText()));
                        Log.i("status", "status " + jParser.getText());
                    }

                    if (fn.equals("totalCount")) {
                        stockTakeListData.setTotalCount(Integer.parseInt((jParser.getText())));
                        Log.i("totalCount", "totalCount " + jParser.getText());
                    }

                    if (fn.equals("name")) {
                        stockTakeListData.setName((jParser.getText()));
                        Log.i("name", "name " + jParser.getText());
                    }

                    if (fn.equals("stocktakeno")) {
                        stockTakeListData.setStocktakeno((jParser.getText()));
                        Log.i("stocktakeno", "stocktakeno " + jParser.getText());
                    }

                    if (fn.equals("picsite")) {
                        stockTakeListData.setPicsite((jParser.getText()));
                        Log.i("picsite", "picsite " + jParser.getText());
                        Hawk.put(InternalStorage.PIC_SITE, jParser.getText());
                    }

                    if (fn.equals("data")) {
                        // String fn2 = jParser.getCurrentName();
                        // jParser.nextToken();

                        Item item = null;// new StockTakeListData.Item();
                        RealmStockTakeListAsset realmStockTakeListAsset = null;

                        // Log.i("fn2", "fn2 " + fn2 + " " +jParser.getText());
                        //while (jParser.nextToken() != JsonToken.END_ARRAY) {

                        while (jParser.nextToken() != JsonToken.END_ARRAY) {
                            while (jParser.nextToken() != JsonToken.END_OBJECT) {

                                String fn3 = jParser.getCurrentName();
                                jParser.nextToken();

                                Log.i("fn3", "fn3 " + fn3 + " " + jParser.getText());

                                try {
                                    if (fn3.equals("id")) {
                                        item = new Item();
                                        realmStockTakeListAsset = new RealmStockTakeListAsset();

                                        item.setStocktakeno(stockTakeListData.getStocktakeno());
                                        realmStockTakeListAsset.setStocktakeno(stockTakeListData.getStocktakeno());
                                        realmStockTakeListAsset.setStocktakename(stockTakeListData.getName());
                                        realmStockTakeListAsset.setId(Integer.parseInt(jParser.getText()));

                                        Log.i("stocktakedetail", "stocktakedetail " + realmStockTakeListAsset.getStocktakeno() + " " + realmStockTakeListAsset.getId());
                                        //item.setId(Integer.parseInt(jParser.getText()));
                                    }

                                    if (fn3.equals("assetno")) {
                                        item.setAssetno(jParser.getText());
                                        realmStockTakeListAsset.setAssetno(jParser.getText());
                                    }

                                    if (fn3.equals("LastAssetNo")) {
                                        //item.setAssetno(jParser.getText());
                                        realmStockTakeListAsset.setLastAssetNo(jParser.getText());
                                    }

                                    if (fn3.equals("name")) {
                                        item.setName(jParser.getText());
                                        realmStockTakeListAsset.setName(jParser.getText());
                                    }

                                    if (fn3.equals("brand")) {
                                        item.setBrand(jParser.getText());
                                        realmStockTakeListAsset.setBrand(jParser.getText());
                                    }

                                    if (fn3.equals("model")) {
                                        item.setModel(jParser.getText());
                                        realmStockTakeListAsset.setModel(jParser.getText());
                                    }

                                    if (fn3.equals("category")) {
                                        item.setCategory(jParser.getText());
                                        realmStockTakeListAsset.setCategory(jParser.getText());
                                    }

                                    if (fn3.equals("location")) {
                                        item.setLocation(jParser.getText());
                                        realmStockTakeListAsset.setLocation(jParser.getText());
                                    }

                                    if (fn3.equals("epc")) {
                                        item.setEpc(jParser.getText());
                                        realmStockTakeListAsset.setEpc(jParser.getText());
                                    }

                                    if (fn3.equals("statusid")) {
                                        item.setStatusid(Integer.parseInt(jParser.getText()));
                                        realmStockTakeListAsset.setStatusid(Integer.parseInt(jParser.getText()));

                                        if(item.getStatusid() == 2 || item.getStatusid() == 10) {
                                            count++;
                                        }
                                    }

                                    if (fn3.equals("remarks")) {
                                        item.setRemarks(jParser.getText());
                                        realmStockTakeListAsset.setRemarks((jParser.getText()));
                                    }

                                    if (fn3.equals("pic")) {
                                        item.setPic(jParser.getText());
                                        realmStockTakeListAsset.setPic((jParser.getText()));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (fn3.equals("rono")) {
                                    item.setRono(jParser.getText());
                                    realmStockTakeListAsset.setRono((jParser.getText()));

                                    list.add(item);
                                    stockTakeListData.setData(list);

                                    item.setPk(item.getStocktakeno() + item.getAssetno());
                                    realmStockTakeListAsset.setPk(companyId + item.getStocktakeno() + item.getAssetno());
                                    realmStockTakeListAsset.setUserId(serverId);
                                    realmStockTakeListAsset.setCompanyId(companyId);
                                    Realm.getDefaultInstance().insertOrUpdate(realmStockTakeListAsset);

                                     EventBus.getDefault().post(new ProgressEvent(count, serverCount));
                                    EventBus.getDefault().post(new InsertEvent("StockTake " + item.getStocktakeno(), item.getAssetno()));

                                    Log.i("size", "size " + stockTakeListData.getData().size());
                                }
                            }
                        }
                        //}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Realm.getDefaultInstance().commitTransaction();

                EventBus.getDefault().post(new UpdateFailEvent());
                return;
            }
        } catch (Exception b) {
            b.printStackTrace();
            Realm.getDefaultInstance().commitTransaction();

            EventBus.getDefault().post(new UpdateFailEvent());

            return;
        }

        Log.i("stockTakeListData", "stockTakeListData " + stockTakeListData.getStocktakeno() + " " +stockTakeListData.getData().size());
        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeListData.getStocktakeno(), stockTakeListData);//, event.getResponse());
        Realm.getDefaultInstance().commitTransaction();
        EventBus.getDefault().post(new NetworkInventoryDoneEvent(stockTakeListData.getName(), stockTakeListData.getStocktakeno()));

        ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
        readFileCallbackEvent.setFileName(LandRegisteryDownloadFragment.stockTakeListAsset + "_" + stockTakeListData.getStocktakeno());
        EventBus.getDefault().post(readFileCallbackEvent);

        //FileUtils.deleteFileByRawPath(path);
    }

    public static void parseLargeJson(String  path) {
        count = 0;
        serverCount = 0;

        List<String> title = new LinkedList<>();

        Realm.getDefaultInstance().beginTransaction();

        try (JsonParser jParser = new JsonFactory().createParser(new File(path));) {
            JsonToken current;
            current = jParser.nextToken();

            if (current != JsonToken.START_OBJECT) {
                System.out.println("Error: root should be object: quiting.");
                return;
            }


            while (jParser.nextToken() != JsonToken.END_OBJECT) {
                String fn = jParser.getCurrentName();
                jParser.nextToken();
                Log.i("hihi", "hihi cerpath " + jParser.getText());

                if(fn.equals("cerpath")) {
                    Log.i("hihi", "hihi cerpath " + jParser.getText());
                } else if(fn.equals("count")) {
                    Log.i("hihi", "hihi count " + jParser.getText());

                    serverCount = Integer.parseInt(jParser.getText());
                    if(serverCount > 0) {
                        Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("userid", serverId).equalTo("companyid", companyId).findAll().deleteAllFromRealm();
                    } else {
                       // EventBus.getDefault().post(new AddAssetEvent(0, 0));

                    }
                } else if(fn.equals("thiscalldate")) {
                    Log.i("hihi", "hihi thiscalldate " + jParser.getText());
                    thiscalldate = jParser.getText();
                    if(serverCount > 0) {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, thiscalldate);
                    }
                } else if(fn.equals("data")) {

                    //  jParser.nextToken();
                    //Log.i("hihi", "hihi data " + jParser.getText());

                    while (jParser.nextToken() != JsonToken.END_OBJECT) {
                        Log.i("hihi", "Hihi 1 " + fn + " " + jParser.getText());

                        if (jParser.getText().equals("title")) {
                            jParser.nextToken();
                            // Log.i("hihi", "hihi title " + jParser.getText());

                            int position = 0;
                            while (jParser.nextToken() != JsonToken.END_ARRAY) {
                                title.add(jParser.getText());
                                Log.i("hihi", "Hihi header " + jParser.getText() + " " + position);
                                position++;
                            }
                        } else {
                            jParser.nextToken();
                            Log.i("hihi", "hihi other " + jParser.getText());

                            int position = 0;

                            AssetsDetail assetsDetail = new AssetsDetail();
                            while (jParser.nextToken() != JsonToken.END_ARRAY) {
                                String myTitle = "";

                                try {
                                    myTitle = title.get(position);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Log.i("hihi", "hihi other " + myTitle + " " + jParser.getText() + " " + position + " " + title.size());

                                if(myTitle.equals("ID")){
                                    //assetsDetail.setId(Integer.parseInt(jParser.getText()));
                                }
                                if(myTitle.equals("assetNo")){
                                    assetsDetail.setAssetNo(jParser.getText());
                                }
                                if(myTitle.equals("name")){
                                    assetsDetail.setName(jParser.getText());
                                }
                                if(myTitle.equals("statusid")){
                                    assetsDetail.setStatusid(jParser.getText());
                                }
                                if(myTitle.equals("statusname")){
                                    assetsDetail.setStatusname(jParser.getText());
                                }
                                if(myTitle.equals("brand")){
                                    assetsDetail.setBrand(jParser.getText());
                                }
                                if(myTitle.equals("model")){
                                    assetsDetail.setModel(jParser.getText());
                                }
                                if(myTitle.equals("serialno")){
                                    assetsDetail.setSerialno(jParser.getText());
                                }
                                if(myTitle.equals("unit")){
                                    assetsDetail.setUnit(jParser.getText());
                                }
                                if(myTitle.equals("category")){
                                    assetsDetail.setCategory(jParser.getText());
                                }
                                if(myTitle.equals("location")){
                                    assetsDetail.setLocation(jParser.getText());
                                }
                                if(myTitle.equals("lastStockDate")){
                                    assetsDetail.setLastStockDate(jParser.getText());
                                }
                                if(myTitle.equals("createdByid")){
                                    assetsDetail.setCreatedById(jParser.getText());
                                }
                                if(myTitle.equals("createdByname")){
                                    assetsDetail.setCreatedByName(jParser.getText());
                                }
                                if(myTitle.equals("createdDate")){
                                    assetsDetail.setCreatedDate(jParser.getText());
                                }
                                if(myTitle.equals("purchaseDate")){
                                    assetsDetail.setPurchaseDate(jParser.getText());
                                }
                                if(myTitle.equals("invoiceDate")){
                                    assetsDetail.setInvoiceDate(jParser.getText());
                                }
                                if(myTitle.equals("invoiceNo")){
                                    assetsDetail.setInvoiceNo(jParser.getText());
                                }
                                if(myTitle.equals("fundingSourceid")){
                                    assetsDetail.setFundingSourceid(jParser.getText());
                                }
                                if(myTitle.equals("fundingSourcename")){
                                    assetsDetail.setFundingSourcename(jParser.getText());
                                }
                                if(myTitle.equals("supplier")){
                                    assetsDetail.setSupplier(jParser.getText());
                                }
                                if(myTitle.equals("maintenanceDate")){
                                    assetsDetail.setMaintenanceDate(jParser.getText());
                                }
                                if(myTitle.equals("cost")){
                                    assetsDetail.setCost(jParser.getText());
                                }
                                if(myTitle.equals("praticalValue")){
                                    assetsDetail.setPraticalValue(jParser.getText());
                                }
                                if(myTitle.equals("estimatedLifetime")){
                                    assetsDetail.setEstimatedLifetime(jParser.getText());
                                }
                                if(myTitle.equals("typeOfTag")){
                                    assetsDetail.setTypeOfTag(jParser.getText());
                                }
                                if(myTitle.equals("epc")){
                                    assetsDetail.setEpc(jParser.getText());
                                }
                                if(myTitle.equals("certType")){
                                    assetsDetail.setCertType(jParser.getText());
                                }
                                if(myTitle.equals("certUrl")){
                                    assetsDetail.setCertUrl(jParser.getText());
                                }
                                if(myTitle.equals("cerstatus")){
                                    assetsDetail.setCerstatus(jParser.getText());
                                }
                                if(myTitle.equals("isverified")){
                                    try {
                                        assetsDetail.setIsverified(Boolean.parseBoolean(jParser.getText()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(myTitle.equals("startdate")){
                                    assetsDetail.setStartdate(jParser.getText());
                                }
                                if(myTitle.equals("enddate")){
                                    assetsDetail.setEnddate(jParser.getText());
                                }
                                if(myTitle.equals("rono")){
                                    assetsDetail.setRono(jParser.getText());
                                }

                                if(myTitle.equals("possessor")){
                                    assetsDetail.setPossessor(jParser.getText());
                                }
                                if(myTitle.equals("usergroup")){
                                    assetsDetail.setUsergroup(jParser.getText());
                                }
                                if(myTitle.equals("LastAssetNo")){
                                    assetsDetail.setLastassetno(jParser.getText());
                                }



                                if(position == title.size() - 1) {

                                    assetsDetail.setUserid(serverId);
                                    assetsDetail.setCompanyid(companyId);
                                    assetsDetail.setPk(companyId+serverId+assetsDetail.getAssetNo());
                                    assetsDetail.setOrdering(count);

                                    Realm.getDefaultInstance().insertOrUpdate(assetsDetail);

                                    count++;
                                    EventBus.getDefault().post(new ProgressEvent(count, serverCount));
                                    EventBus.getDefault().post(new InsertEvent(assetsDetail.getAssetNo(), assetsDetail.getName()));

                                }

                                position++;

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Realm.getDefaultInstance().commitTransaction();
        //

        EventBus.getDefault().post(new NetworkInventoryDoneEvent("inventory", ""));

    }
}
