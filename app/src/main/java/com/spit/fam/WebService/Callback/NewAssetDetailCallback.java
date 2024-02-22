package com.spit.fam.WebService.Callback;

import android.content.Context;
import android.util.Log;

import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Event.AssetQueueEvent;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.HideLoadingEvent;
import com.spit.fam.Event.ShowLoadingEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.spit.fam.SystemFragment.DownloadFragment.CONTINUOUS_ASSET_DETAIL;
import static com.spit.fam.SystemFragment.DownloadFragment.CONTINUOUS_ASSET_LIST;

public class NewAssetDetailCallback implements Callback<JsonElement> {
    public NewAssetDetailCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    private String type;
    private boolean showLoadingIcon;
    private Context context;
    private boolean realTimeApi;

    private ArrayList<AssetQueueEvent> queneList = new ArrayList<>();
    private String jsonRaw, withEPC, withoutEPC = "";
    public NewAssetDetailCallback(boolean realTimeApi) {
        this.realTimeApi = realTimeApi;
    }

    public static boolean occupied;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AssetQueueEvent event) {


        if(!occupied) {
            occupied = true;

            ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(queneList.size() == 0) {
                            if(event.key != null)
                                queneList.add(event);
                        }

                        if(queneList.size() == 0) {
                            return;
                        }

                        List<AssetsDetail> arraysList = new ArrayList<>();
                        AssetsDetail assetsDetail = new AssetsDetail();

                        for (int i = 0; i < ((JSONArray) queneList.get(0).value.get(event.key)).length(); i++) {
                            setAssetDetail(assetsDetail, ((JSONArray) queneList.get(0).value.get("title")).getString(i), ((JSONArray) queneList.get(0).value.get(event.key)).getString(i));
                        }


                        arraysList.add(assetsDetail);

                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((JSONArray) event.value.get(event.key)).getString(0), arraysList);//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

                        occupied = false;

                        queneList.remove(0);

                        Log.i("queneList", "queneList " + queneList.size());

                        if(queneList.size() > 0) {
                            //AssetQueueEvent assetQueueEvent = new AssetQueueEvent();
                            //assetQueueEvent.key = queneList.get(0).key;
                            //assetQueueEvent.value = queneList.get(0).value;
                            //EventBus.getDefault().post(assetQueueEvent);

                            return;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            queneList.add(event);
        }

    }

    public NewAssetDetailCallback(String type) {
        this.type = type;
        EventBus.getDefault().register(this);

        EventBus.getDefault().post(new CallbackStartEvent());
        Log.i("CONTINUOUS", "CONTINUOUS case 11 b start");
    }

    public NewAssetDetailCallback(String type, boolean showLoadingIcon, Context context) {
        this.type = type;
        this.showLoadingIcon =showLoadingIcon;
        this.context = context;
        EventBus.getDefault().register(this);

        if(showLoadingIcon) {
            EventBus.getDefault().post(new ShowLoadingEvent());
        }

        EventBus.getDefault().post(new CallbackStartEvent());
    }

    private int typeId;
    public NewAssetDetailCallback(int type) {
        Log.i("CONTINUOUS", "CONTINUOUS case 11 start");
        EventBus.getDefault().register(this);
        this.typeId = type;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
        Log.i("CONTINUOUS", "CONTINUOUS case 11 downloaded");

        Log.i("body", "body " + response.body());

        //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, thiscalldate);
        if(response.code() == 200) {

            if(showLoadingIcon) {
                EventBus.getDefault().post(new HideLoadingEvent());
                EventBus.getDefault().post(new DialogEvent(context.getString(R.string.app_name), context.getString(R.string.asset_list_downloaded)));
            }
            ArrayList<AssetsDetail> assetsDetails = new ArrayList<>();

            JsonObject rootJsonObject = response.body().getAsJsonObject();
            if(!realTimeApi) {
            }

            try {
                String cerpath = rootJsonObject.getAsJsonPrimitive("cerpath").getAsString();
                if(cerpath != null && cerpath.length() > 0)
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_CERT_PATH, cerpath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int count = rootJsonObject.getAsJsonPrimitive("count").getAsInt();

            ExecutorService schTaskEx = Executors.newFixedThreadPool(100000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {

                    if(count > 1 || CONTINUOUS_ASSET_LIST == typeId) {
                        String thiscalldate = rootJsonObject.getAsJsonPrimitive("thiscalldate").getAsString();
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, thiscalldate);

                        //DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
                        //db.deleteAssets();
                        Realm.getDefaultInstance().beginTransaction();
                        Realm.getDefaultInstance().delete(AssetsDetail.class);
                        Realm.getDefaultInstance().commitTransaction();
                    }

                    JsonObject jsonObjectRaw = rootJsonObject.getAsJsonObject("data");
                    if(jsonObjectRaw != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonObjectRaw.toString());
                            Iterator<String> keys = jsonObject.keys();

                            int counter = 0;

                            Realm.getDefaultInstance().beginTransaction();

                            while (keys.hasNext()) {
                                final String key = keys.next();
                                final JSONArray value = (JSONArray)jsonObject.get(key);

                                    AssetsDetail assetsDetail = new AssetsDetail();

                                    assetsDetail.setAssetNo(value.getString(0));
                                    assetsDetail.setName(value.getString(1));
                                    assetsDetail.setStatusid(value.getString(2));
                                    assetsDetail.setStatusname(value.getString(3));
                                    assetsDetail.setBrand(value.getString(4));
                                    assetsDetail.setModel(value.getString(5));
                                    assetsDetail.setSerialno(value.getString(6));
                                    assetsDetail.setUnit(value.getString(7));
                                    assetsDetail.setCategory(value.getString(8));
                                    assetsDetail.setLocation(value.getString(9));
                                    assetsDetail.setLastStockDate(value.getString(10));
                                    assetsDetail.setCreatedById(value.getString(11));
                                    assetsDetail.setCreatedByName(value.getString(12));
                                    assetsDetail.setCreatedDate(value.getString(13));
                                    assetsDetail.setPurchaseDate(value.getString(14));
                                    assetsDetail.setInvoiceDate(value.getString(15));
                                    assetsDetail.setInvoiceNo(value.getString(16));
                                    assetsDetail.setFundingSourceid(value.getString(17));
                                    assetsDetail.setFundingSourcename(value.getString(18));
                                    assetsDetail.setSupplier(value.getString(19));
                                    assetsDetail.setMaintenanceDate(value.getString(20));
                                    assetsDetail.setCost(value.getString(21));
                                    assetsDetail.setPraticalValue(value.getString(22));
                                    assetsDetail.setEstimatedLifetime(value.getString(23));
                                    assetsDetail.setTypeOfTag(value.getString(24));
                                    assetsDetail.setBarcode(value.getString(25));
                                    assetsDetail.setEpc(value.getString(26));
                                    assetsDetail.setCertType(value.getString(27));
                                    assetsDetail.setCertUrl(value.getString(28));
                                    assetsDetail.setCerstatus(value.getString(29));
                                    assetsDetail.setIsverified(Boolean.parseBoolean(value.getString(30)));
                                    assetsDetail.setStartdate(value.getString(31));
                                    assetsDetail.setEnddate(value.getString(32));
                                    assetsDetail.setRono(value.getString(33));
                                    assetsDetail.setPossessor(value.getString(34));
                                    assetsDetail.setUsergroup(value.getString(35));
                                    try {
                                        //assetsDetail.setExhibitsource(value.getString(36));
                                       // assetsDetail.setExhibitwitness(value.getString(37));
                                        assetsDetail.setLastassetno(value.getString(36));
                                    } catch (Exception e) {
                                        //e.printStackTrace();
                                    }

                                   // Log.i("yoyoyo", "yoyoyo " + value.getString(36) + " " + value.getString(37) + " " + value.getString(38));

                                    if(counter > 0 && counter < 2)
                                        assetsDetails.add(assetsDetail);
                                //DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);

                                if(!assetsDetail.getAssetNo().equals("assetNo") &&  (CONTINUOUS_ASSET_DETAIL == typeId)) {
                                    //db.addAssetsDetail(assetsDetail);
                                    Realm.getDefaultInstance().insertOrUpdate(assetsDetail);
                                }
                                
                                Log.i("CONTINUOUS", "CONX key " + assetsDetail.getAssetNo() + " " + assetsDetail.getAssetNo().equals("assetNo"));

                                counter++;
                            }

                            Realm.getDefaultInstance().commitTransaction();

                            Log.i("CON", "CON finish" );

                            EventBus.getDefault().post(new HideLoadingEvent());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("assetsDetails", "assetsDetails " + assetsDetails.size() + " " + type + " " + typeId);

                    if(typeId > 0) {
                        CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(assetsDetails);
                        callbackResponseEvent.type = typeId;
                        callbackResponseEvent.setResponse(assetsDetails);
                        EventBus.getDefault().post(callbackResponseEvent);
                    } else {
                        EventBus.getDefault().post(new CallbackResponseEvent(type, assetsDetails));
                    }
                }
            });



           /* for(int i = 0; i < assetsDetails.size(); i++) {
                List<AssetsDetail> arraysList = new ArrayList<>();
                arraysList.add((AssetsDetail) (assetsDetails.get(i)));

                ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
                int finalI = i;
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {

                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((AssetsDetail) (assetsDetails).get(finalI)).getAssetNo(), arraysList);//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                    }
                });
            }
*/
        } else {

            if(showLoadingIcon) {
                EventBus.getDefault().post(new HideLoadingEvent());
                EventBus.getDefault().post(new DialogEvent(context.getString(R.string.app_name), context.getString(R.string.fail)));
            }
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    public AssetsDetail setAssetDetail(AssetsDetail assetsDetail, String field,  String data) {
        if(data == null || data.equals("null")) {
            data = "";
        }
        if(field.equals("assetNo")) {
            assetsDetail.setAssetNo(data);
        } else if (field.equals("name")) {
            assetsDetail.setName(data);
        } else if (field.equals("statusid")) {
            assetsDetail.setStatusid(data);
        } else if (field.equals("statusname")) {
            assetsDetail.setStatusname(data);
        } else if (field.equals("brand")) {
            assetsDetail.setBrand(data);
        } else if (field.equals("model")) {
            assetsDetail.setModel(data);
        } else if (field.equals("serialno")) {
            assetsDetail.setSerialno(data);
        } else if (field.equals("unit")) {
            assetsDetail.setUnit(data);
        } else if (field.equals("category")) {
            Log.i("category", "category " + data);
            assetsDetail.setCategory(data);
        } else if (field.equals("location")) {
            Log.i("location", "location " + data);
            assetsDetail.setLocation(data);
        } else if (field.equals("lastStockDate")) {
            assetsDetail.setLastStockDate(data);
        } else if (field.equals("createdByid")) {
            assetsDetail.setCreatedById(data);
        } else if (field.equals("createdByname")) {
            assetsDetail.setCreatedByName(data);
        } else if (field.equals("createdDate")) {
            assetsDetail.setCreatedDate(data);
        } else if (field.equals("purchaseDate")) {
            assetsDetail.setPurchaseDate(data);
        } else if (field.equals("invoiceDate")) {
            assetsDetail.setInvoiceDate(data);
        } else if (field.equals("invoiceNo")) {
            assetsDetail.setInvoiceNo(data);
        } else if (field.equals("fundingSourceid")) {
            assetsDetail.setFundingSourceid(data);
        } else if (field.equals("fundingSourcename")) {
            assetsDetail.setFundingSourcename(data);
        } else if (field.equals("supplier")) {
            assetsDetail.setSupplier(data);
        } else if (field.equals("maintenanceDate")) {
            assetsDetail.setMaintenanceDate(data);
        } else if (field.equals("cost")) {
            assetsDetail.setCost(data);
        } else if (field.equals("praticalValue")) {
            assetsDetail.setPraticalValue(data);
        } else if (field.equals("estimatedLifetime")) {
            assetsDetail.setEstimatedLifetime(data);
        } else if (field.equals("typeOfTag")) {
            assetsDetail.setTypeOfTag(data);
        } else if (field.equals("barcode")) {
            assetsDetail.setBarcode(data);
        } else if (field.equals("epc")) {
            assetsDetail.setEpc(data);
        } else if (field.equals("certType")) {
            assetsDetail.setCertType(data);
        } else if (field.equals("certUrl")) {
            assetsDetail.setCertUrl(data);
        } else if (field.equals("cerstatus")) {
            assetsDetail.setCerstatus(data);
        } else if (field.equals("isverified")) {
            try {
                assetsDetail.setIsverified(Boolean.parseBoolean(data));
            } catch (Exception e) {
                assetsDetail.setIsverified(false);
                e.printStackTrace();
            }
        } else if (field.equals("startdate")) {
            assetsDetail.setStartdate(data);
        } else if (field.equals("enddate")) {
            assetsDetail.setEnddate(data);
        } else if (field.equals("rono")) {
            assetsDetail.setRono(data);
        } else if (field.equals("possessor")) {
            assetsDetail.setPossessor(data);
        } else if (field.equals("usergroup")) {
            assetsDetail.setUsergroup(data);
        }else if (field.equals("exhibitsource")) {
            assetsDetail.setExhibitsource(data);
        }else if (field.equals("exhibitwitness")) {
            assetsDetail.setExhibitwitness(data);
        }else if (field.equals("lastassetno")) {
            assetsDetail.setLastassetno(data);
        }

        return assetsDetail;
    }

    @Override
    public void onFailure(Call<JsonElement> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().url());
        if(showLoadingIcon) {
            EventBus.getDefault().post(new HideLoadingEvent());
            EventBus.getDefault().post(new DialogEvent(context.getString(R.string.app_name), context.getString(R.string.fail)));
        }

        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}
