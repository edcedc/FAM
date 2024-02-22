package com.spit.fam.SystemFragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.CreateBy;
import com.spit.fam.Entity.Item;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP2.PhotoUploadRequest;
import com.spit.fam.Entity.SPEntityP2.StockTakeListData;
import com.spit.fam.Entity.SpEntity.StockTakeAsset;
import com.spit.fam.Entity.SpEntity.StockTakeDetail;
import com.spit.fam.Entity.SpEntity.StrJson;
import com.spit.fam.Entity.SpEntity.UploadStockTakeData;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.Entity.TagType;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.HideLoadingEvent;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.Event.ShowLoadingEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.InventoryRfidTask;
import com.spit.fam.MainActivity;
import com.spit.fam.OnBackPressEvent;
import com.spit.fam.R;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.SimpleReaderListAdapter;
import com.spit.fam.WebService.Callback.GetAssetListCallback;
import com.spit.fam.WebService.Callback.GetBorrowListAssetCallback;
import com.spit.fam.WebService.Callback.GetBriefAssetObjectCallback;
import com.spit.fam.WebService.Callback.GetStockTakeListDataCallback;
import com.spit.fam.WebService.Callback.ImageReturnCallback;
import com.spit.fam.WebService.Callback.SPWebServiceCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.fragments.ConnectionFragment;
import com.spit.fam.fragments.HomeFragment;
import com.spit.cs108library4a.Cs108Library4A;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;

public class StockTakeListItemFragment extends HomeFragment {
    ArrayList<Asset> myasset = Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>());
    public static int stockTakeListId = 0;

    public static StockTakeList stockTakeList;

    private AssetListAdapter assetListAdapter;
    private List<Asset> data;

    private List<String> epcList = new ArrayList<>();

    ListView listView, rfidListView;
    View noResult;
    public static int tabPosition;
    Button start;

    private InventoryRfidTask inventoryRfidTask;
    SimpleReaderListAdapter readerListAdapter;

    public StrJson convertAssetToStrJson(Asset asset) {
        StrJson strJson = new StrJson();

        strJson.setAssetName(asset.getName());
        strJson.setAssetNo(asset.getAssetno());
        strJson.setBarcode(asset.getBarcode());
        strJson.setBrand(asset.getBrand());
        try {
            strJson.setCategoryName(asset.getCategories().get(0).getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            strJson.setLocationName(asset.getLocations().get(0).getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        strJson.setCost(asset.getCost());
        strJson.setEPC(asset.getEPC());
        strJson.setInvoiceDate(asset.getInvoiceDate())  ;
        strJson.setInvoiceNo(asset.getInvoiceNo());
        strJson.setLastAssetNo(asset.getLastAssetNo());
        strJson.setModelNo(asset.getModel());
        strJson.setSerialNo(asset.getSerialNo());
        strJson.setOrderNo(stockTakeList.getOrderNo());
        strJson.setGIAI_GRAI(asset.getGIAI_GRAI());

        strJson.setSupplier(asset.getSupplier());
        strJson.setUnit(asset.getUnit());
        strJson.setFundingSouce(asset.getFundingSource());
        strJson.setPurchaseDate(asset.getPurchaseDate());
        String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + asset.getAssetno();
        strJson.setRemarks(Hawk.get(key, ""));
        strJson.setQRCode("");

        return strJson;
    }

    boolean scannerOpen = false;
    InventoryBarcodeTask inventoryBarcodeTask;
    void startStopBarcodeHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.mCs108Library4a.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus()) || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false))) {
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                return;
            }
            if (MainActivity.mCs108Library4a.isBarcodeFailure()) {
                Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: Start Barcode inventory");
            started = true;
            inventoryBarcodeTask = new InventoryBarcodeTask(MainActivity.sharedObjects.barsList, readerListAdapter, null, null, null, null, null, null, null, false);
            inventoryBarcodeTask.execute();
        } else {
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: Stop Barcode inventory");
            if (buttonTrigger) inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
        }
    }

    private boolean saved;
    private ArrayList<String> keyList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {
        Log.i("EVENT", "EVENT " + event.getBarcode());

        if(stockTakeList != null) {
            keyList.clear();

            for(int i = 0; i < stockTakeList.getAssets().size(); i++) {
                //DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
                //myAsset = db.getAssetByAssetNo(filterText,  "0");

                //List<AssetsDetail> assetsDetail = db.getAssetByAssetNo(event.getBarcode(),  "0");
                List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("assetNo", event.getBarcode()).findAll();// dataBaseHandler.searchAssetsDetail(ASSET_NO, "", "", "", "", "", "", "", "");//.size());//MainActivity.getAssetsDetailList(ASSET_NO);//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);

                try {
                    Log.i("EVENT", "EVENT " + assetsDetail.get(0).getBarcode());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Log.i("hihi", "hihi "+ assetsDetail.get(0).getBarcode() + " " + (event.getBarcode()) +  assetsDetail.get(0).getAssetNo() + " " + (event.getBarcode()) );

                if(assetsDetail != null && assetsDetail.size() > 0 && assetsDetail.get(0).getBarcode() != null && (assetsDetail.get(0).getAssetNo().equals(stockTakeList.getAssets().get(i).getAssetno()) || assetsDetail.get(0).getBarcode().equals(stockTakeList.getAssets().get(i).getBarcode()))) {
                    //String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + stockTakeList.getAssets().get(i).getAssetno();
                    //Hawk.put(status_key, 2);
                    //assetListAdapter.notifyDataSetChanged();

                    ArrayList<String> arrayList = new ArrayList<>();
                    if(assetsDetail.get(0).getEpc().length() > 0) {
                        arrayList.add(assetsDetail.get(0).getEpc());
                    } else {
                        if(!epcList.contains(assetsDetail.get(0).getAssetNo()))
                            epcList.add(assetsDetail.get(0).getAssetNo());
                        arrayList.add(assetsDetail.get(0).getAssetNo());
                    }

                    RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(arrayList);
                    rfidDataUpdateEvent.setBarcode(true);

                    EventBus.getDefault().post(rfidDataUpdateEvent);
                } else if(stockTakeList.getAssets().get(i).getAssetno().equals(event.getBarcode())){

                    ArrayList<String> arrayList = new ArrayList<>();

                    if(stockTakeList.getAssets().get(i).getEPC().length() > 0) {
                        arrayList.add(stockTakeList.getAssets().get(i).getEPC());
                    } else {
                        arrayList.add(stockTakeList.getAssets().get(i).getAssetno());
                    }

                    RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(arrayList);
                    rfidDataUpdateEvent.setBarcode(true);

                    EventBus.getDefault().post(rfidDataUpdateEvent);
                }
            }
        }

        scannerOpen = false;
        startStopBarcodeHandler(false);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
        
        ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
        schTaskEx.execute(new Runnable() {
            @Override
            public void run() {
                /*
                BorrowListAssets borrowListAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno());//, event.getResponse());

                if(borrowListAssets != null) {
                    for (int i = 0; i < borrowListAssets.getData().size(); i++) {
                        String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo();
                        Log.i("onResume", "onResume status_key" + Hawk.get(status_key, -1) + " " + status_key);

                        String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo();
                        Log.i("onResume", "onResume remark" + Hawk.get(key, "") + " " + key);

                        int statusValue = Hawk.get(status_key, -1);
                        String remarkValue = Hawk.get(key, "");

                        Hawk.put(InternalStorage.OFFLINE_CACHE.SAVE_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo(), statusValue);
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SAVE_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo(), remarkValue);
                    }
                }

                ArrayList<PhotoUploadRequest> photoUploadRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());
                Log.i("onResume", "onResume SAVED_STOCK_TAKE_CACHE" + photoUploadRequests.size());
                Hawk.put(InternalStorage.OFFLINE_CACHE.SAVED_PHOTO_UPLOAD_REQUEST, photoUploadRequests);

                 */
            }
        });

        view = LayoutInflater.from(getActivity()).inflate(R.layout.stock_take_list_item_fragment, null);

        Log.i("stockTakeList", "stockTakeList " + stockTakeList);

        if (stockTakeList != null)
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName());

        ((EditText) view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        view.findViewById(R.id.add).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                boolean started = false;
                boolean delayNeeded = false;

                if (inventoryRfidTask != null)
                    if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING)
                        started = true;

                if(started) {
                    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
                    delayNeeded = true;
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                            Log.i("openScanner case 1", "openScanner case 1");
                            openScanner();
                        } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                            Log.i("openScanner case 2", "openScanner case 2");
                            openScanner();
                        } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                            Log.i("openScanner case 1", "openScanner case 3");
                            openScanner();
                        } else {
                            scannerOpen = true;
                            startStopBarcodeHandler(false);
                        }
                    }
                };

                Handler handler = new Handler();
                if(delayNeeded) {
                    handler.postDelayed(runnable, 1000);
                } else {
                    handler.post(runnable);
                }
                /*
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    openScanner();
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    openScanner();
                } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                    openScanner();
                } else {
                    scannerOpen = true;
                    startStopBarcodeHandler(false);
                }*/
            }
        });

        listView = view.findViewById(R.id.listview);
        rfidListView = view.findViewById(R.id.rfidlistview);
        noResult = view.findViewById(R.id.no_result);
        start = (Button) view.findViewById(R.id.start);

        boolean bSelect4detail = true;
        boolean needDupElim = true;

        boolean need4Extra1 = MainActivity.mCs108Library4a.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = ( false);

        readerListAdapter = new SimpleReaderListAdapter(getActivity(), R.layout.reader_list_item_clone, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);
        rfidListView.setAdapter(readerListAdapter);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopHandler(false);
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity) getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).onBackPressed();
            }
        });


        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stockTakeList.getScannedCount() == 0) {
                   EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.nothing_selected)));
                   return;
                }

                if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) {
                    startStopHandler(false);
                }

                saved = true;

                EventBus.getDefault().post(new ShowLoadingEvent());
                Hawk.put(stockTakeList.getStocktakeno(), stockTakeList);//, event.getResponse());

                Log.i("confirm", "confirm UploadStockTakeData" + LoginFragment.SP_API + " " + stockTakeList.getOrderNo());
                ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {

                        if ( LoginFragment.SP_API) {
                            EventBus.getDefault().post(new CallbackStartEvent());
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    //TODO your background code


                                    StrJson strJson = new StrJson();
                                    String result = "";

                                    Log.i("UploadStockTakeData", "UploadStockTakeData " + stockTakeList.getAssets().size());
                                    ArrayList<UploadStockTakeData> uploadStockTakeDataList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());

                                    ArrayList<UploadStockTakeData> newArrayList = new ArrayList<>();

                                    for(int i = 0; i < uploadStockTakeDataList.size(); i++) {
                                        if(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo().equals(stockTakeList.getOrderNo())) {

                                        } else {
                                            newArrayList.add(uploadStockTakeDataList.get(i));
                                        }
                                    }

                                    uploadStockTakeDataList = newArrayList;

                                    for(int i = 0; i < StockTakeListItemFragment.stockTakeList.getProccessedAbmoralAssets().size(); i++) {
                                        Asset asset = (StockTakeListItemFragment.stockTakeList.getProccessedAbmoralAssets().get(i));
                                        if(asset.getAssetno() == null) {
                                            continue;
/*

                                            strJson.setAssetName(null);
                                            strJson.setAssetNo(null);
                                            strJson.setBarcode(null);
                                            strJson.setBrand(null);
                                            strJson.setCategoryName(null);
                                            strJson.setLocationName(null);

                                            strJson.setCost(null);
                                            strJson.setEPC(asset.getEPC());
                                            strJson.setInvoiceDate(null) ;
                                            strJson.setInvoiceNo(null);
                                            strJson.setLastAssetNo(null);
                                            strJson.setModelNo(null);
                                            strJson.setSerialNo(null);
                                            strJson.setOrderNo(stockTakeList.getOrderNo());
                                            strJson.setGIAI_GRAI(null);

                                            strJson.setSupplier(null);
                                            strJson.setUnit(null);
                                            strJson.setFundingSouce(null);
                                            strJson.setPurchaseDate(null);
                                            strJson.setRemarks(null);
                                            strJson.setQRCode(null);*/

                                        } else {
                                            strJson = convertAssetToStrJson(asset);
                                        }

                                        Log.i("asset", "asset "+ asset.getAssetno());

                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        strJson.setScanDate(simpleDateFormat.format(asset.getScanDateTime()));
                                        strJson.setFoundStatus(116);
                                        strJson.setStatusID(2);
                                        strJson.setUserName(Hawk.get(InternalStorage.Login.USER_ID, ""));
                                        strJson.setLoginID(Hawk.get(InternalStorage.Login.USER_ID, ""));
                                        strJson.setUserid(Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""));

                                        UploadStockTakeData uploadStockTakeData = new UploadStockTakeData();
                                        uploadStockTakeData.setCompanyID(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                                        uploadStockTakeData.setStockTakeName(stockTakeList.getName());


                                        uploadStockTakeData.setStrJson(new GsonBuilder().disableHtmlEscaping().create().toJson(strJson));
                                        uploadStockTakeData.setStrJsonObject(strJson);



                                        boolean exist = false;
                                        int position = -1;

                                        //if(!exist) {
                                        Log.i("strjson", "strjson " + (stockTakeList.checkExist.get(asset.getEPC()) == null));
                                        if(stockTakeList.checkExist.get(asset.getEPC()) == null) {
                                            Log.i("strJson", "abnormal strJson " + uploadStockTakeData.getStrJson());
                                            uploadStockTakeDataList.add(uploadStockTakeData);
                                        }
                                        //} else if (position >= 0) {
                                        //    uploadStockTakeDataList.set(position, uploadStockTakeData);
                                        //}
                                    }

                                    Log.i("uploadStockTakeDataList", "uploadStockTakeDataList case 2 " + uploadStockTakeDataList.size());

                                    for (int i = 0; i < stockTakeList.getAssets().size(); i++) {
                                        Asset asset = stockTakeList.getAssets().get(i);

                                        strJson = convertAssetToStrJson(asset);
/*
                                        boolean found = false;
                                        for (int y = 0; y < StockTakeListItemFragment.stockTakeList.getAssets().size(); y++) {
                                            if (StockTakeListItemFragment.stockTakeList.getAssets().get(y).getEPC().equals(asset.getEPC())) {
                                                found = true;
                                            }
                                        }
*/
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        if ( searchedEPCList.contains(asset.getEPC())) {
                                            strJson.setScanDate(simpleDateFormat.format(asset.getScanDateTime()));
                                            strJson.setFoundStatus(116);
                                        } else {
                                            strJson.setScanDate(simpleDateFormat.format(asset.getScanDateTime()));
                                            strJson.setFoundStatus(118);
                                        }

                                        int  status = 0;
                                        if ( searchedEPCList.contains(asset.getEPC())) {
                                            status = 1;
                                        } else if ( !searchedEPCList.contains(asset.getEPC())) {
                                            status = 0;
                                        } else if ( !searchedEPCList.contains(asset.getEPC())) {
                                            status = 2;
                                        }

                                        String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + asset.getAssetno();

                                        int value = Hawk.get(status_key, -1);
                                        Log.i("assetNo", "assetNo " + value + " " + asset.getAssetno());
                                        if(value != -1)
                                            status = value;

                                        if(value == 2) {
                                            strJson.setFoundStatus(117);
                                            status = 1;
                                        }

                                        if(asset.getFindType().equals("barcode")) {
                                            Log.i("case1", "case1");
                                            strJson.setFoundStatus(117);
                                            status = 1;
                                        }

                                        if(asset.getFindType().equals("rfid")) {
                                            Log.i("case2", "case2");
                                            strJson.setFoundStatus(116);
                                            status = 1;
                                        }
                                        if(asset.getFindType().equals("manual")) {
                                            Log.i("case3", "case3");
                                            strJson.setFoundStatus(118);
                                            status = 1;
                                        }

                                        Log.i("asset", "asset " + asset.getAssetno() + " " + strJson.getFoundStatus());

                                        strJson.setStatusID(status);
                                        strJson.setUserName(Hawk.get(InternalStorage.Login.USER_ID, ""));
                                        strJson.setLoginID(Hawk.get(InternalStorage.Login.USER_ID, ""));
                                        strJson.setUserid(Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""));

                                        UploadStockTakeData uploadStockTakeData = new UploadStockTakeData();
                                        uploadStockTakeData.setCompanyID(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                                        uploadStockTakeData.setStockTakeName(stockTakeList.getName());

                                        uploadStockTakeData.setStrJson(new GsonBuilder().disableHtmlEscaping().create().toJson(strJson));
                                        uploadStockTakeData.setStrJsonObject(strJson);


                                        /*
                                        boolean exist = false;
                                        int position = -1;
                                        for(int x = 0; x < uploadStockTakeDataList.size(); x++) {
                                            if(uploadStockTakeDataList.get(x).getStrJsonObject().getOrderNo().equals(uploadStockTakeData.getStrJsonObject().getOrderNo()) && uploadStockTakeDataList.get(x).getStrJsonObject().getAssetNo().equals(uploadStockTakeData.getStrJsonObject().getAssetNo())) {
                                                exist = true;
                                                position = x;
                                                break;
                                            }
                                        }*/


                                        //if(!exist) {

                                        if(status > 0) {
                                            Log.i("strJson", "strJson " + uploadStockTakeData.getStrJson());
                                            uploadStockTakeDataList.add(uploadStockTakeData);
                                        }
                                        //} else if (position >= 0) {
                                        //    uploadStockTakeDataList.set(position, uploadStockTakeData);
                                        //}
                                        //StockTakeListItemFragment.this.getActivity().onBackPressed();
                                        Log.i("result", "result " +  Hawk.get(InternalStorage.Setting.COMPANY_ID, "") +  "[" + result + "]");

                                        //RetrofitClient.getSPGetWebService().UploadStockTake(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),  "[" + new GsonBuilder().disableHtmlEscaping().create().toJson(strJson) + "]").enqueue(new SPWebServiceCallback());

                                        //Result += new GsonBuilder().disableHtmlEscaping().create().toJson(strJson) + (getFoundMissList(true).size() - 1 == i ? "" : ",");
                                    }

                                    Log.i("uploadStockTakeDataList", "uploadStockTakeDataList case 3 " + uploadStockTakeDataList.size());

                                    if( /*((MainActivity)getActivity()).isNetworkAvailable()*/ false) {
                                        Log.i("upload", "upload " + "[" + new GsonBuilder().disableHtmlEscaping().create().toJson(strJson) + "]");
                                        ArrayList<String> uniqueStockTakeList = new ArrayList<>();
                                        ArrayList<String> uniqueStockTakeId = new ArrayList<>();

                                        for(int i = 0; i < uploadStockTakeDataList.size(); i++) {
                                            if(!uniqueStockTakeId.contains(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo())) {
                                                uniqueStockTakeId.add(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo());
                                                uniqueStockTakeList.add(uploadStockTakeDataList.get(i).getStockTakeName());
                                            }
                                        }

                                        for(int i = 0; i < uniqueStockTakeId.size(); i++) {
                                            final int tempPos = i;


                                            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                                            String strJsonString = "";

                                            ArrayList<UploadStockTakeData> data = new ArrayList<>();

                                            for(int y = 0; y < uploadStockTakeDataList.size(); y++) {
                                                if(uploadStockTakeDataList.get(y).getStrJsonObject().getOrderNo().equals(uniqueStockTakeId.get(tempPos))) {
                                                    data.add(uploadStockTakeDataList.get(y));
                                                }
                                            }

                                            for(int y = 0; y < data.size(); y++) {
                                                companyId = data.get(y).getCompanyID();
                                                if(y == 0) {
                                                    strJsonString = "[";
                                                }

                                                strJsonString += data.get(y).getStrJson();

                                                if(y != data.size() - 1) {
                                                    strJsonString += ",";
                                                } else {
                                                    strJsonString += "]";
                                                }
                                                //
                                            }


                                            Log.i("strJson", "strJson "  + strJsonString);
                                            RetrofitClient.getSPGetWebService().UploadStockTake(companyId,  strJsonString).enqueue(new SPWebServiceCallback(uniqueStockTakeId.get(tempPos)));

                                            ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                                            final int[] temp = {0};

                                            for(int x = 0; x < arrayList.size(); x++) {
                                                if(arrayList.get(x).getOrderNo().equals(uniqueStockTakeId.get(tempPos)) && arrayList.get(x).getAssetNo().equals(uploadStockTakeDataList.get(tempPos).getStrJsonObject().getAssetNo())) {
                                                    String finalCompanyId = companyId;
                                                    int finalI = x;

                                                    Handler handler = new Handler();
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Glide.with(StockTakeListItemFragment.this.getActivity())
                                                                    .asBitmap().load(arrayList.get(finalI).getFilePath()).into(new SimpleTarget<Bitmap>(500,500) {
                                                                @Override
                                                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                                                    String encodedString = "";

                                                                    File f = new File(StockTakeListItemFragment.this.getActivity().getCacheDir(), "asd.jpg");
                                                                    try {
                                                                        f.createNewFile();

                                                                        Bitmap bitmap = resource;
                                                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos);
                                                                        byte[] bitmapdata = bos.toByteArray();

                                                                        encodedString = Base64.encodeToString(bitmapdata, Base64.DEFAULT);

                                                                        FileOutputStream fos = null;

                                                                        fos = new FileOutputStream(f);

                                                                        fos.write(bitmapdata);
                                                                        fos.flush();
                                                                        fos.close();
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                                    Log.i("uploadFileToByte", "uploadFileToByte " + finalCompanyId + " " + arrayList.get(finalI).getAssetNo() + " " + arrayList.get(finalI).getFileLoc() + " " + arrayList.get(finalI).getUserId());
                                                                    RetrofitClient.getSPGetWebService().uploadFileToByte(finalCompanyId, encodedString, "jpg", arrayList.get(finalI).getRono(), arrayList.get(finalI).getFileLoc(), arrayList.get(finalI).getUserId(), uniqueStockTakeId.get(tempPos)).enqueue(new ImageReturnCallback());

                                                                    temp[0]++;

                                                                    if(temp[0] == arrayList.size()) {
                                                                        Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());
                                                                    }
                                                                }});

                                                        }
                                                    });

                                                }
                                            }
                                        }
                                    } else {

                                        //if(MainActivity.OFFLINE_MODE) {
                                        Log.i("localsave", "localsave " + uploadStockTakeDataList.size());
                                        Hawk.put(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, uploadStockTakeDataList);
                                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_SAVED_ + stockTakeList.getOrderNo(), true);

                                        /*
                                        BorrowListAssets borrowListAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno());//, event.getResponse());
                                        ArrayList<PhotoUploadRequest> photoUploadRequestArrayList = new ArrayList<>();

                                        for(int i = 0; i < borrowListAssets.getData().size(); i++) {
                                            String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo();
                                            Log.i("onPause", "onPause status_key" + Hawk.get(status_key, -1) + " " + status_key);

                                            String key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo();
                                            Log.i("onPause", "onPause remark" + Hawk.get(key, "") + " " + key);

                                            int statusValue =  Hawk.get(status_key, -1);
                                            String remarkValue = Hawk.get(key, "");

                                            Hawk.put(InternalStorage.OFFLINE_CACHE.SAVE_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo(), statusValue);
                                            Hawk.put(InternalStorage.OFFLINE_CACHE.SAVE_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo(), remarkValue);

                                        }*/

                                        ArrayList<PhotoUploadRequest> photoUploadRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                                        Hawk.put(InternalStorage.OFFLINE_CACHE.SAVED_PHOTO_UPLOAD_REQUEST, photoUploadRequests);

                                        if(getActivity() != null) {

                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((MainActivity) getActivity()).updateDrawerStatus();
                                                }
                                            });
                                            EventBus.getDefault().post(new CallbackResponseEvent(new Object()));

                                            EventBus.getDefault().post(new DialogEvent(getActivity().getString(R.string.app_name), getActivity().getString(R.string.upload_tips)));
                                            EventBus.getDefault().post(new OnBackPressEvent());
                                        }
                                    }
                                    EventBus.getDefault().post(new HideLoadingEvent());

                                }
                            });


                        }
                    }
                });


            }
        });

        ((TabLayout) view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                listView.clearFocus();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelection(0);
                    }
                });

                setupListView(filter(getData(), filterText));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setupListView(filter(getData(), filterText));
        Log.i("cccc", "cccc 9");

        callAPI();
    }


    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li, vg, b);
        return view;
    }

    public void callAPI() {
        Log.i("stockTakeListId", "stockTakeListId " + stockTakeListId + " " + stockTakeList + " " +
                Hawk.get(InternalStorage.Setting.COMPANY_ID, "") + " " +
                Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + " " + stockTakeList.getStocktakeno());

        StockTakeList stl = Hawk.get(this.stockTakeList.getStocktakeno(), null);//, event.getResponse());

        if(stl != null) {
            this.stockTakeList = stl;
        }

        if( ((MainActivity)getActivity()).isNetworkAvailable() ) {
                if(stockTakeList != null) {
                    /*RetrofitClient.getSPGetWebService().stockTakeListDetail(
                            Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),
                            Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""), stockTakeList.getOrderNo()).enqueue(new GetStockTakeDetailCallback());*/
                    RetrofitClient.getSPGetWebService().stockTakeListAsset2(
                            Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),
                            Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""), stockTakeList.getStocktakeno()).enqueue(new GetStockTakeListDataCallback());

                }

        } else {
            //if(LoginFragment.SP_API) {

                //StockTakeDetail stockTakeDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno());//, event.getResponse());
                StockTakeListData borrowListAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno());//, event.getResponse());

                Log.i("borrowListAssets", "borrowListAssets " + borrowListAssets + " " +borrowListAssets.getData().size());

                if(borrowListAssets == null) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.no_data)));
                    //getActivity().onBackPressed();
                    return;
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new CallbackResponseEvent(borrowListAssets));
                        }
                    }, 1000);
                }
                /*

            StockTakeDetail stockTakeDetail = new StockTakeDetail();
            stockTakeDetail.OrderNo = borrowListAssets.getStocktakeno();
            stockTakeList.setOrderNo(stockTakeDetail.OrderNo);
            Log.i("stockTakeDetail", "stockTakeDetail " + borrowListAssets.getStocktakeno() + " " + borrowListAssets.getData().size());
            stockTakeDetail.Table = new ArrayList<StockTakeAsset>();

            for(int i = 0; i < borrowListAssets.getData().size(); i++) {

                StockTakeAsset stockTakeAsset = new StockTakeAsset();
                StockTakeListData.Item briefAsset = borrowListAssets.getData().get(i);
                Log.i("borrowListAssets data", "borrowListAssets data " +briefAsset.getStatusid() + " " + borrowListAssets.getPicsite() + " " + borrowListAssets.getData().get(i).getRemarks() + " " + borrowListAssets.getData().get(i).getPic());

                stockTakeAsset.setAssetNo(briefAsset.getAssetno());
                stockTakeAsset.setAssetName(briefAsset.getName());
                stockTakeAsset.setBrand(briefAsset.getBrand());
                stockTakeAsset.setModelNo(briefAsset.getModel());
                stockTakeAsset.setEPC(briefAsset.getEpc());
                stockTakeAsset.setLocationName(briefAsset.getLocation());
                stockTakeAsset.setCategoryName(briefAsset.getCategory());
                stockTakeAsset.setPicsite(borrowListAssets.getPicsite());
                stockTakeAsset.setRemarks(briefAsset.getRemarks());
                stockTakeAsset.setFound(briefAsset.getStatusid() == (2) ? true : false);
                stockTakeAsset.setStatusid(briefAsset.getStatusid() + "");

                ArrayList<String> picArrayList = new ArrayList<>();

                try {
                    for (int x = 0; x < Arrays.asList(briefAsset.getPic().split(",")).size(); x++) {
                        if (Arrays.asList(briefAsset.getPic().split(",")).get(x).length() > 0) {
                            String result = (Arrays.asList(briefAsset.getPic().split(",")).get(x)).trim();

                            if (result.length() > 0) {
                                picArrayList.add(result);
                            }
                        }
                    }
                } catch (Exception e) {}

                stockTakeAsset.setPic(picArrayList);

                stockTakeDetail.Table.add(stockTakeAsset);
            }

            if(stockTakeDetail != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new CallbackResponseEvent(stockTakeDetail));
                    }
                };

                Handler handler = new Handler();
                handler.postDelayed(runnable, 500);
             }
            return;
            */
            //}

            /*
            ArrayList<Asset> assetArrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.ASSET, new ArrayList<Asset>());
            ArrayList<Asset> result = new ArrayList<>();

            if(stockTakeList != null) {
                for (int y = 0; y < stockTakeList.getAssetIds().size(); y++) {
                    for (int i = 0; i < assetArrayList.size(); i++) {
                        if (assetArrayList.get(i).getId().equals("" + stockTakeList.getAssetIds().get(y))) {
                            result.add(assetArrayList.get(i));
                        }
                    }
                }
            }
            EventBus.getDefault().post(new CallbackResponseEvent(result));*/
        }
    }

    private List<BriefAsset> abnormalAssetCache = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {

        if(event.getResponse() instanceof StockTakeListData) {
            StockTakeListData borrowListAssets = (StockTakeListData) event.getResponse();

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno(), borrowListAssets);//, event.getResponse());

            StockTakeDetail stockTakeDetail = new StockTakeDetail();
            stockTakeDetail.OrderNo = borrowListAssets.getStocktakeno();
            stockTakeList.setOrderNo(stockTakeDetail.OrderNo);
            Log.i("stockTakeDetail", "stockTakeDetail " + borrowListAssets.getStocktakeno() + " " );
            stockTakeDetail.Table = new ArrayList<StockTakeAsset>();

            for(int i = 0; i < borrowListAssets.getData().size(); i++) {

                StockTakeAsset stockTakeAsset = new StockTakeAsset();
                Item briefAsset = borrowListAssets.getData().get(i);
                Log.i("borrowListAssets data", "borrowListAssets data " +briefAsset.getStatusid() + " " + borrowListAssets.getPicsite() + " " + borrowListAssets.getData().get(i).getRemarks() + " " + borrowListAssets.getData().get(i).getPic());

                stockTakeAsset.setAssetNo(briefAsset.getAssetno());
                stockTakeAsset.setAssetName(briefAsset.getName());
                stockTakeAsset.setBrand(briefAsset.getBrand());
                stockTakeAsset.setModelNo(briefAsset.getModel());
                stockTakeAsset.setEPC(briefAsset.getEpc());
                stockTakeAsset.setLocationName(briefAsset.getLocation());
                stockTakeAsset.setCategoryName(briefAsset.getCategory());
                stockTakeAsset.setPicsite(borrowListAssets.getPicsite());
                stockTakeAsset.setRemarks(briefAsset.getRemarks());
                stockTakeAsset.setFound(briefAsset.getStatusid() == (2) ? true : false);
                stockTakeAsset.setStatusid(briefAsset.getStatusid() + "");
                stockTakeAsset.setProsecutionNo(briefAsset.getProsecutionNo());
                //Log.i("hihi", "hihi " + stockTakeAsset.getAssetNo() + " " + briefAsset.getStockTake() + " " + briefAsset.getPic());

                ArrayList<String> picArrayList = new ArrayList<>();

                try {
                    for (int x = 0; x < Arrays.asList(briefAsset.getPic().split(",")).size(); x++) {
                        if (Arrays.asList(briefAsset.getPic().split(",")).get(x).length() > 0) {
                            String result = (Arrays.asList(briefAsset.getPic().split(",")).get(x)).trim();

                            if (result.length() > 0) {
                                picArrayList.add(result);
                            }
                        }
                    }
                } catch (Exception e) {}

                stockTakeAsset.setPic(picArrayList);

                stockTakeDetail.Table.add(stockTakeAsset);
            }

            Log.i("tableSize", "tableSize " + stockTakeDetail.Table.size());


            if(stockTakeDetail != null)
                EventBus.getDefault().post(new CallbackResponseEvent(stockTakeDetail));
            return;
        }
        if(event.getResponse() instanceof BorrowListAssets) {

            BorrowListAssets borrowListAssets = (BorrowListAssets) event.getResponse();

            BorrowListAssets temp = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno());//, event.getResponse());

            //if(temp == null) {
                //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno(), borrowListAssets);//, event.getResponse());
            //}

            StockTakeDetail stockTakeDetail = new StockTakeDetail();
            stockTakeDetail.OrderNo = borrowListAssets.getStocktakeno();
            stockTakeList.setOrderNo(stockTakeDetail.OrderNo);
            Log.i("stockTakeDetail", "stockTakeDetail " + borrowListAssets.getStocktakeno() + " " );
            stockTakeDetail.Table = new ArrayList<StockTakeAsset>();

            for(int i = 0; i < borrowListAssets.getData().size(); i++) {
                Log.i("borrowListAssets data", "borrowListAssets data " + borrowListAssets.getPicsite() + " " + borrowListAssets.getData().get(i).getRemarks() + " " + borrowListAssets.getData().get(i).getPic());

                StockTakeAsset stockTakeAsset = new StockTakeAsset();
                BriefAsset briefAsset = borrowListAssets.getData().get(i);

                stockTakeAsset.setAssetNo(briefAsset.getAssetNo());
                stockTakeAsset.setAssetName(briefAsset.getName());
                stockTakeAsset.setBrand(briefAsset.getBrand());
                stockTakeAsset.setModelNo(briefAsset.getModel());
                stockTakeAsset.setEPC(briefAsset.getEpc());
                stockTakeAsset.setLocationName(briefAsset.getLocation());
                stockTakeAsset.setCategoryName(briefAsset.getCategory());
                stockTakeAsset.setPicsite(borrowListAssets.getPicsite());
                stockTakeAsset.setRemarks(briefAsset.getRemarks());
                stockTakeAsset.setFound(briefAsset.getStatusid().equals("2") ? true : false);

                Log.i("hihi", "hihi " + stockTakeAsset.getAssetNo() + " " + briefAsset.getStockTake() + " " + briefAsset.getPic());

                ArrayList<String> picArrayList = new ArrayList<>();

                try {
                    for (int x = 0; x < Arrays.asList(briefAsset.getPic().split(",")).size(); x++) {
                        if (Arrays.asList(briefAsset.getPic().split(",")).get(x).length() > 0) {
                            String result = (Arrays.asList(briefAsset.getPic().split(",")).get(x)).trim();

                            if (result.length() > 0) {
                                picArrayList.add(result);
                            }
                        }
                    }
                } catch (Exception e) {}

                stockTakeAsset.setPic(picArrayList);

                stockTakeDetail.Table.add(stockTakeAsset);
            }

            if(stockTakeDetail != null)
                EventBus.getDefault().post(new CallbackResponseEvent(stockTakeDetail));

        } else if(event.getResponse() instanceof BriefAsset) {
            Log.i("briefAsset", "briefAsset");
            Asset asset = null;

            asset = convertBriefAssetToAsset((BriefAsset) event.getResponse());
            Status status = new Status();
            status.id = 9;
            asset.setStatus(status);
            asset.setScanDateTime(new Date());

            stockTakeList.addAssets(asset);

            stockTakeList.getAbnormalMap().put(asset.getEPC(), asset);

            stockTakeList.proccessedAllAssets.clear();
            stockTakeList.proccessedReadAssets.clear();
            stockTakeList.proccessedUnReadAssets.clear();

            List<Asset> assets = filter(getData(), filterText);

            listView.setSelection(0);

            assetListAdapter.setData(assets, getActivity());
            handleNoResult(assets);
            assetListAdapter.notifyDataSetChanged();
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName() + " (" + assets.size() + ")");

        } else  if(event.getResponse() instanceof  StockTakeList) {
            Log.i("stockTakeList", "stockTakeList case 3 ");

            stockTakeList =  ((StockTakeList)event.getResponse());
            if(stockTakeList != null)
                ((TextView) view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName());


            RetrofitClient.getService().getAssets(stockTakeList.getAssetIds()).enqueue(new GetAssetListCallback());

        } else if (event.getResponse() instanceof APIResponse) {
            //getActivity().onBackPressed();
            Log.i("APIResponse", "APIResponse " + event.getResponse());

            if((((APIResponse)event.getResponse()).getStatusString()).equals("1")) {


                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.upload_success))

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().onBackPressed();
                            }
                        })
                        .show();
            } else {

                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.upload_fail))

                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        } else if (event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof APIResponse) {
            Log.i("yoyo", "yoyo " + (((List<APIResponse>) event.getResponse()).get(0)).getStatusString());
            Log.i("yoyo", "yoyo 1");
            if( (((List<APIResponse>) event.getResponse()).get(0)).getStatusString().equals(LoginFragment.SP_API ? "1" : "0")) {

                Log.i("uploadsuccess", "uploadsuccess");
                String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());
                RetrofitClient.getSPGetWebService().stockTakeListAsset(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),serverId, stockTakeList.getStocktakeno()).enqueue(new GetBorrowListAssetCallback());


                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.upload_success))

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            } else {


                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.upload_fail))

                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        } else if(event.getResponse() instanceof StockTakeDetail) {
            Log.i("yoyo", "yoyo 2");

            ArrayList<Asset> assets = new ArrayList<>();
            for(int i = 0; i < ((StockTakeDetail)event.getResponse()).getTable().size() ; i ++) {
                assets.add( ((StockTakeDetail)event.getResponse()).getTable().get(i).convertToAsset() );
            }

            Log.i("data", "data " + InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getOrderNo());

            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getOrderNo(), event.getResponse());

            stockTakeList.setAsset(assets);
            //Log.i("size", "size " + assets.size() + " " + ((StockTakeDetail)event.getResponse()).getTable().size());

            setupListView(filter(getData(), filterText));
            Log.i("cccc", "cccc 5");

        } else if (event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0).getClass() == Asset.class) {
            stockTakeList.setAsset((List<Asset>) event.getResponse());


            for(int i = 0; i < stockTakeList.getStockTakeListItems().size(); i++) {
                for(int y = 0; y < stockTakeList.getAssets().size(); y++) {

                    if(Integer.parseInt(stockTakeList.getAssets().get(y).getId()) == (stockTakeList.getStockTakeListItems().get(i).getAsset())) {

                        if(stockTakeList.getStockTakeListItems().get(i).isFound()) {
                            stockTakeList.getAssets().get(y).setFound(true);
                        }
                    }
                }
            }

            Log.i("assets", "assets " + stockTakeList.getAssets() + " " + ((List<Asset>) event.getResponse()).size()) ;
            setupListView(filter(getData(),filterText));
            Log.i("cccc", "cccc 6");

        } else {
            handleNoResult(null);
        }
    }

    public void handleNoResult(List<Asset> data) {
        //Log.i("data", "data " + data.size());

        if (data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }
    }

    public void setupListView(List<Asset> data) {
        //this.data = data;
        AssetListAdapter.WITH_EPC = true;
        //Log.i("yoyo", "yoyo " + data.size());

        handleNoResult(data);

        if (assetListAdapter == null) {
            for(int i = 0; i < data.size(); i++) {
                //Log.i("yoyo", "yoyo11 1 " + data.get(i).getStock_take_asset_item_remark() + " " + data.size() + " " + i);
            }
            assetListAdapter = new AssetListAdapter(true, data, getActivity());
            listView.setAdapter(assetListAdapter);
        } else {
            for(int i = 0; i < data.size(); i++) {
                //Log.i("yoyo", "yoyo11 2 " + data.get(i).getStock_take_asset_item_remark());
            }
            assetListAdapter.setData(data, getActivity());
            assetListAdapter.notifyDataSetChanged();
            //listView.setAdapter(assetListAdapter);
        }

        Log.i("setuplistview", "setuplistview" + stockTakeList.getName() + " (" + data.size() + ")");
        if(data.size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName() + " (" + data.size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName());

    }

    public List<Asset> getData() {
        if(stockTakeList != null) {
            if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
                Log.i("getData", "getData case 1");
                epcList.clear();
                for(int i = 0; i < stockTakeList.getAssets().size(); i++) {
                    epcList.add(stockTakeList.getAssets().get(i).getEPC() + "");
                    Log.i("epc", "epc " + epcList.get(i));
                }
                return (stockTakeList.getAssets());
            } else if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 1) {

                Log.i("getData", "getData case 2");
                return (stockTakeList.getReadAssets());

                //return (getFoundMissList(true));
            } else if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 2) {

                Log.i("getData", "getData case 3");
                return (stockTakeList.getUnReadAssets());

                //return (getFoundMissList(false));
            } else if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 3) {

                Log.i("getData", "getData case 4 " + stockTakeList.getProccessedAbmoralAssets().size());
                for(int i = 0; i < stockTakeList.getProccessedAbmoralAssets().size(); i++) {
                    Log.i("getData case 4", "getData case 4 " + stockTakeList.getProccessedAbmoralAssets().get(i).getEPC());
                }

                return (stockTakeList.getProccessedAbmoralAssets());

                //return (getAbnormalList());
            }
        }
        return new ArrayList<>();
    }

    ArrayList<String> searchedEPCList = new ArrayList<>();

    public void setSearchedEPCList(ArrayList<String> searchedEPCList) {

        for(int i = 0; i < searchedEPCList.size(); i++) {
            if(this.searchedEPCList.contains(searchedEPCList.get(i))) {

            } else {
                this.searchedEPCList.add(searchedEPCList.get(i));
            }
        }
        //this.searchedEPCList = searchedEPCList;
    }

    public List<Asset> getFoundMissList(boolean getFoundList) {
        List<Asset> raw = stockTakeList.getAssets();
        List<Asset> result = new ArrayList<>();
        List<Asset> rawNotFound = new ArrayList<>();
        List<Asset> stillMissing = new ArrayList<>();

        for(int i = 0; i < raw.size(); i++) {
            String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + raw.get(i).getAssetno();
            int value = Hawk.get(status_key, -1);

            if(raw.get(i).isFound() || searchedEPCList.contains(raw.get(i).getEPC()) || value == 1 || value == 2) {
                result.add(raw.get(i));
            } else {
                rawNotFound.add(raw.get(i));
            }
        }

        for(int i = 0; i < rawNotFound.size(); i ++) {
            for(int y = 0; y < searchedEPCList.size(); y++) {
                //Log.i("searchedEPCList", "searchedEPCList " + searchedEPCList.get(y) + " " + rawNotFound.get(i).getEPC() + " " + (rawNotFound.get(i).getEPC().equals(searchedEPCList)));
                if(rawNotFound.get(i).getEPC().equals(searchedEPCList.get(y))) {
                    rawNotFound.get(i).setFound(true);
                    result.add(rawNotFound.get(i));

                } else {
                    if(!result.contains(rawNotFound.get(i)))
                        stillMissing.add(rawNotFound.get(i));
                }
            }
        }

        if(searchedEPCList.size() == 0) {
            stillMissing = rawNotFound;
        }

        //Log.i("result", "getFoundList " + getFoundList + " " + result);

        if(getFoundList) {
            return result;
        } else {
            return rawNotFound;
        }
    }

    private List<String> requestedEPCList = new ArrayList<>();
    public List<Asset> getAbnormalList() {
        ArrayList<Asset> abnormalList = new ArrayList<>();

        ArrayList<Asset> temp = new ArrayList<>();

        temp.addAll(stockTakeList.getAbnormalAssets());

        List<Asset> waitingList = getFoundMissList(false);
        List<Asset> borrowedList = getFoundMissList(true);

        for(int i = 0; i < searchedEPCList.size(); i++) {
            boolean exist = false;
            for (int y = 0; y < waitingList.size(); y++) {
                if(searchedEPCList != null) {
                    if (waitingList.get(y).getEPC() != null && waitingList.get(y).getEPC().equals(searchedEPCList.get(i))) {
                        exist = true;
                    }
                }
            }


            for (int y = 0; y < borrowedList.size(); y++) {
                if(searchedEPCList != null) {
                    if (borrowedList.get(y).getEPC() != null && borrowedList.get(y).getEPC().equals(searchedEPCList.get(i))) {
                        exist = true;
                    }
                }
            }

            boolean cacheExist = false;
            boolean requested = false;
            int cachePos = -1;

            for(int y = 0; y < requestedEPCList.size(); y++) {
                if(requestedEPCList.get(y).equals(searchedEPCList.get(i))) {
                    requested = true;
                    break;
                }
            }

            for(int y = 0; y < abnormalAssetCache.size(); y++) {
                if(abnormalAssetCache.get(y).getEpc().equals(searchedEPCList.get(i))) {
                    cacheExist = true;
                    cachePos = y;
                    break;
                }
            }

            Log.i("request", "request " + requested + " " + exist);

            if(!requested && !exist) {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
                Log.i("request", "request2 " + requested + " " + exist + " " + companyId + " " + userid + " " + searchedEPCList.get(i));

                if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                    //RetrofitClient.getSPGetWebService().getBriefAssetInfo(companyId, userid, searchedEPCList.get(i)).enqueue(new GetBriefAssetObjectCallback(true));
                } else {

                    /*
                    final String search = searchedEPCList.get(i);
                    final boolean currentExist = exist;

                    ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
                    schTaskEx.execute(new Runnable() {
                                          @Override
                                          public void run() {
                                              DataBaseHandler databaseHandler = new DataBaseHandler(MainActivity.mContext);
                                              List<Asset> assets = databaseHandler.getAssetByEPC(search, "0");

                                              if(assets.size() > 0) {
                                                  BriefAsset briefAsset = convertAssetToBriefAsset(assets.get(0));

                                                  if (!currentExist) {
                                                      briefAsset.setFound(true);
                                                      abnormalAssetCache.add(briefAsset);
                                                      requestedEPCList.add(briefAsset.getEpc());
                                                  }
                                              }
                                          }
                                      });

                     */
                }
            }



            if(!exist) {
                if(cacheExist) {
                    Asset asset = convertBriefAssetToAsset(abnormalAssetCache.get(cachePos));
                    asset.setAbnormal(true);
                    temp.add(asset);

                } else {
                    Asset asset = new Asset();
                    asset.setEPCOnly(true);
                    asset.setEPC(searchedEPCList.get(i));
                    temp.add(asset);
                }
            }
        }

        Log.i("temp", "temp " + " " + searchedEPCList.size() + " " + temp.size());


        return temp;
    }


    public BriefAsset convertAssetToBriefAsset(Asset asset) {
        BriefAsset briefAsset = new BriefAsset();

        briefAsset.setName(asset.getName());
        briefAsset.setAssetNo(asset.getAssetno());
        briefAsset.setBrand(asset.getBrand());
        briefAsset.setModel(asset.getModel());
        briefAsset.setEpc(asset.getEPC());

        briefAsset.setLocation(asset.getLocationString());
        briefAsset.setCategory(asset.getCategoryString());

        return briefAsset;
    }

    public Asset convertBriefAssetToAsset(BriefAsset briefAsset) {
        Asset asset = new Asset();
        asset.setId(briefAsset.getId() + "");
        asset.setName(briefAsset.getName());
        asset.setAssetno(briefAsset.getAssetNo());
        asset.setBrand(briefAsset.getBrand());
        asset.setModel(briefAsset.getModel());
        asset.setEPC(briefAsset.getEpc());
        asset.setRemarks(briefAsset.getRemarks());
        try {
            asset.setFound(briefAsset.getStatusid().equals("2"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        asset.setRono(briefAsset.getRono());

        if(briefAsset.getBorrowed() != null)
            asset.setFound(briefAsset.getBorrowed());
        else if(briefAsset.getDisposed() != null)
            asset.setFound(briefAsset.getDisposed());

        asset.setReturndate("");

        ArrayList<Category> categoryArrayList = new ArrayList<>();

        if(briefAsset.getCategorys() != null) {
            for (int i = 0; i < briefAsset.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(briefAsset.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);


        ArrayList<Location> locationArrayList = new ArrayList<>();

        if(briefAsset.getLocations() != null) {
            for (int i = 0; i < briefAsset.getLocations().size(); i++) {
                Location location = new Location();
                location.setName(briefAsset.getLocations().get(i));
                locationArrayList.add(location);
            }
        }
        asset.setLocations(locationArrayList);
        return asset;
    }

    public List<Asset> getAbnormal() {
        List<Asset> result = new ArrayList<>();
        List<Asset> raw = stockTakeList.getAssets();
        List<Asset> found = getFoundMissList(true);
        List<Asset> missing = getFoundMissList(false);

        Log.i("myasset", "myasset " + myasset);

       /* if(myasset == null || myasset.size() == 0) {

            for (int i = 0; i < searchedEPCList.size(); i++) {
                Asset asset = new Asset();
                asset.setEPC(searchedEPCList.get(i));
                asset.setEPCOnly(true);

                boolean exist = false;
                for(int z = 0 ; z < result.size(); z++) {
                    if(result.get(z).getEPC().equals(searchedEPCList.get(i))) {
                        exist = true;
                    }
                }

                for(int y = 0; y < found.size(); y++) {
                    if(found.get(y).getEPC().equals(searchedEPCList.get(i))) {
                        exist = true;
                    }
                }

                for(int y = 0; y < missing.size(); y++) {
                    if(missing.get(y).getEPC().equals(searchedEPCList.get(i))) {
                        exist = true;
                    }
                }

                if(!exist)
                    result.add(asset);
            }
            return result;
        }
*/

        for(int i = 0; i < searchedEPCList.size(); i++) {
            boolean exist = false;

            for(int y = 0 ; y < myasset.size(); y++) {
                if(myasset.get(y).getEPC() != null && searchedEPCList.get(i) != null && myasset.get(y).getEPC().length() > 0 && searchedEPCList.get(i).length() > 0) {
                    if(myasset.get(y).getEPC().equals(searchedEPCList.get(i))) {
                        //esist case
                        exist = true;
                        result.add(myasset.get(y));
                        break;
                    } else {
                        Asset asset = new Asset();
                        asset.setEPC(searchedEPCList.get(i));
                        asset.setEPCOnly(true);

                        for(int x = 0; x < found.size(); x++) {
                            if(found.get(x).getEPC().equals(searchedEPCList.get(i))) {
                                exist = true;
                            }
                        }

                        for(int x = 0; x < missing.size(); x++) {
                            if(missing.get(x).getEPC().equals(searchedEPCList.get(i))) {
                                exist = true;
                            }
                        }

                        if(!exist)
                            result.add(asset);
                        break;
                    }
                }
            }

            boolean requested = false;

            for(int y = 0; y < requestedEPCList.size(); y++) {
                if(requestedEPCList.get(y).equals(searchedEPCList.get(i))) {
                    requested = true;
                    break;
                }
            }

            Log.i("requested", "requested " + requested + " " + exist);

            if(!requested && !exist) {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                String userid = Hawk.get(InternalStorage.Login.USER_ID,"");

                if(((MainActivity)getActivity()).isNetworkAvailable() ) {
                    RetrofitClient.getSPGetWebService().getBriefAssetInfo(companyId, userid, searchedEPCList.get(i)).enqueue(new GetBriefAssetObjectCallback(true));
                } else {
                    List<Asset> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<>());

                    for(int x = 0; x < arrayList.size(); x++) {
                        if(arrayList.get(x).getEPC().equals(searchedEPCList.get(i))) {
                            BriefAsset briefAsset = new BriefAsset();

                            briefAsset.setAssetNo(arrayList.get(x).getAssetno());
                            briefAsset.setName(arrayList.get(x).getName());
                            briefAsset.setBrand(arrayList.get(x).getBrand());
                            briefAsset.setModel(arrayList.get(x).getModel());
                            briefAsset.setEpc(arrayList.get(x).getEPC());
                            briefAsset.setCategory(arrayList.get(x).getCategoryString());
                            briefAsset.setLocation(arrayList.get(x).getLocationString());

                            EventBus.getDefault().post(new CallbackResponseEvent(briefAsset));
                        }
                    }
                }
            }
        }

        return result;
    }

    boolean started;
    private Handler handler = new Handler();
    public void stop() {
        started = false;

        //handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        //handler.postDelayed(runnable, 1000);
    }

    public void onResume() {
        super.onResume();
        Log.i("onResume", "onResume " +  stockTakeList.getStocktakeno());

        ArrayList<String> arrayList = new ArrayList<>();
        //arrayList.add("34152A84000000000000028F");
        //arrayList.add("34152A840000000000000DAF");

        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new RFIDDataUpdateEvent(arrayList));
            }
        };

        new Handler().postDelayed(runnable2, 5000);


        ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
        schTaskEx.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());
                ArrayList<PhotoUploadRequest> newArrayList = new ArrayList<>();

                for(int i = 0; i < arrayList.size(); i++) {
                    if(arrayList.get(i).isConfirm()) {
                        newArrayList.add(arrayList.get(i));
                    }
                }

                Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, newArrayList);
            }
        });

        try {
            tabPosition = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
        } catch (Exception e) {}

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int id = Hawk.get("TEMP_ID", -1);
                Asset asset = Hawk.get("TEMP_RESULT", new Asset());


                if( id != -1 && asset.getEPC() != null) {
                    AssetsDetailWithTabFragment.id = id;
                    //AssetsDetailWithTabFragment.asset = asset;
                    AssetsDetailWithTabFragment.ASSET_NO = asset.getAssetno();
                    Hawk.put("TEMP_ID", -1);
                    Hawk.put("TEMP_RESULT", new Asset());

                    String status_key = InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + AssetsDetailWithTabFragment.asset.getAssetno();
                    Hawk.put(status_key, 1);

                    Log.i("status_key", "status_key " + status_key);

                    //replaceFragment(new AssetsDetailWithTabFragment());
                }
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 1000);

        start();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "onDestroy");

        ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
        schTaskEx.execute(new Runnable() {
                              @Override
                              public void run() {
                                  try {
                                      BorrowListAssets borrowListAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stockTakeList.getStocktakeno());//, event.getResponse());
                                      ArrayList<PhotoUploadRequest> photoUploadRequestArrayList = new ArrayList<>();

                                      /*
                                      for (int i = 0; i < borrowListAssets.getData().size(); i++) {
                                          String status_key = InternalStorage.OFFLINE_CACHE.SAVE_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo();
                                          //Log.i("onDestroy", "onDestroy status_key" + Hawk.get(status_key, -1) + " " + status_key);

                                          String key = InternalStorage.OFFLINE_CACHE.SAVE_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo();
                                          //Log.i("onDestroy", "onDestroy remark" + Hawk.get(key, "") + " " + key);

                                          int statusValue = Hawk.get(status_key, -1);
                                          String remarkValue = Hawk.get(key, "");

                                          Log.i("onDestroy", "onDestroy PENDING_STOCK_TAKE_STATUS_ save" + InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo() + " " + statusValue);
                                          Log.i("onDestroy", "onDestroy PENDING_STOCK_TAKE_REMARK_ save" + InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo() + " " + remarkValue);

                                          Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo(), statusValue);
                                          Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REMARK_ + stockTakeList.getStocktakeno() + "_" + borrowListAssets.getData().get(i).getAssetNo(), remarkValue);

                                      }
                                       */

                                      ArrayList<PhotoUploadRequest> photoUploadRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.SAVED_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());
                                      Log.i("onDestroy", "onDestroy PENDING_PHOTO_UPLOAD_REQUEST " + photoUploadRequests.size());
                                      Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, photoUploadRequests);
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }
                              }
                          }
        );
    }

    Parcelable state;
    public void onPause() {
        super.onPause();

        Log.i("case 1", "onPause case 1");

        ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
        schTaskEx.execute(new Runnable() {
            @Override
            public void run() {
                if(!saved) {
                    for(int i = 0; i < keyList.size(); i++) {
                        Hawk.put(keyList.get(i), null);
                    }
                }
            }
        });
        Log.i("case 1", "onPause case 2");

        onStop();
        Log.i("case 1", "onPause case 3");

        stop();
        Log.i("case 1", "onPause case 4");

        boolean started = false;
        if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (  ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus()) || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false))) {
            return;
        }

        if(started) {
            startStopHandler(false);
        }

        //state = listView.onSaveInstanceState();
    }

    int count = 0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(started) {
                start();
                String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        + "0123456789"
                        + "abcdefghijklmnopqrstuvxyz";
                StringBuilder sb = new StringBuilder(24);
                ArrayList<String> listFromSet = new ArrayList<String>();

                /*
                for(int x = 0; x < 100; x++) {
                    sb = new StringBuilder(24);

                    for(int i = 0; i < 24;i++) {
                        int index = (int)(AlphaNumericString.length() * Math.random());
                        sb.append(AlphaNumericString.charAt(index));
                    }

                    listFromSet.add(sb.toString());
                }*/



                Log.i("data", "data " + sb.toString());

                if(count == 0)
                    listFromSet.add("34187890000000000000001D");

                if(count == 1)
                    listFromSet.add("34187890000000000000001A");

                if(count == 2)
                    listFromSet.add("34187890000000000000001B");

                if(count == 3)
                    listFromSet.add("E2801160600002083E167978");

                if(count == 4)
                    listFromSet.add("E2801160600002083E16E948");

                if(count == 5)
                    listFromSet.add("000000000000000000005566");

                count++;

                EventBus.getDefault().post(new RFIDDataUpdateEvent(listFromSet));

                if(!searchedEPCList.contains("34187 89000 00000 00496 9980")) {
                    //searchedEPCList.add("341878900000000004969980");
                }


                if(!searchedEPCList.contains("000000000000000000005566")) {
                   //searchedEPCList.add("000000000000000000005566");
                }

                if(!searchedEPCList.contains("A00000000000000000000005")) {
                    //searchedEPCList.add("D00000000000000000000007");
                }

                //setupListView(getData());

            }
        }
    };


    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger) MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.mCs108Library4a.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryBarcodeTask) {
            Toast.makeText(MainActivity.mContext, "Running barcode inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (buttonTrigger && ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus()) || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false))) {
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: trigger ignore");
            return;
        }
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                replaceFragment(new ConnectionFragment());
                return;
            } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                replaceFragment(new ConnectionFragment());
                return;
            } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                return;
            }
            //if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            //else rfidListView.setSelection(0);
            startInventoryTask();
        } else {
            MainActivity.mCs108Library4a.appendToLogView("CANCELLING. Set taskCancelReason");
            //if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            if (buttonTrigger) inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
        }
    }


    void startInventoryTask() {
        int extra1Bank = -1, extra2Bank = -1;
        int extra1Count = 0, extra2Count = 0;
        int extra1Offset = 0, extra2Offset = 0;
        String mDid = null;//this.mDid;

        if (mDid != null) {
            if (MainActivity.mDid != null && mDid.length() == 0) mDid = MainActivity.mDid;
            extra2Bank = 2;
            extra2Offset = 0;
            extra2Count = 2;
            if (mDid.matches("E200B0")) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 0x2d;
                extra2Count = 1;
            } else if (mDid.matches("E203510")) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 8;
                extra2Count = 2;
            } else if (mDid.matches("E280B12")) {
                extra1Bank = 2;
                extra1Offset = 0;
                extra1Count = 2;
                extra2Bank = 3;
                extra2Offset = 0x120;
                extra2Count = 1;
            } else if (mDid.matches("E282402")) {
                extra1Bank = 0;
                extra1Offset = 11;
                extra1Count = 1;
                extra2Bank = 0;
                extra2Offset = 13;
                extra2Count = 1;
            } else if (mDid.matches("E282403")) {
                extra1Bank = 0;
                extra1Offset = 12;
                extra1Count = 3;
                extra2Bank = 3;
                extra2Offset = 8;
                extra2Count = 4;
            } else if (mDid.matches("E282405")) {
                extra1Bank = 0;
                extra1Offset = 10;
                extra1Count = 5;
                extra2Bank = 3;
                extra2Offset = 0x12;
                extra2Count = 4;
            }
            if (mDid.matches("E280B12")) {
                if (MainActivity.mDid.matches("E280B12B")) {
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 5, 1, 0x220, "8321");
                    MainActivity.mCs108Library4a.appendToLog("Hello123: Set Sense at Select !!!");
                } else { //if (MainActivity.mDid.matches("E280B12A")) {
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
                    MainActivity.mCs108Library4a.appendToLog("Hello123: Set Sense at BOOT !!!");
                }
            } else if (mDid.matches("E203510")) {
                MainActivity.mCs108Library4a.setSelectCriteria(1, true, 7, 4, 0, 1, 32, mDid);
            } else if (mDid.matches("E28240")) {
                if (MainActivity.selectFor != 0) {
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = 0;
                }
            } else if (mDid.matches("E282402")) {
                if (MainActivity.selectFor != 2) {
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 2, 0, 3, 0xA0, "20");
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = 2;
                }
            } else if (mDid.matches("E282403")) {
                if (MainActivity.selectFor != 3) {
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 2, 0, 3, 0xE0, "");
                    MainActivity.mCs108Library4a.setSelectCriteria(2, true, 4, 2, 0, 3, 0xD0, "1F");
                    MainActivity.selectFor = 3;
                }
            } else if (mDid.matches("E282405")) {
                if (MainActivity.selectFor != 5) {
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 5, MainActivity.selectHold, 3, 0x3B0, "00");
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = 5;
                }
            } else {
                if (MainActivity.selectFor != -1) {
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(2);
                    MainActivity.selectFor = -1;
                }
            }
            String TAG = "TEST";
            boolean bNeedSelectedTagByTID = true;
            if (mDid.matches("E2806894")) {
                //Log.i(TAG, "HelloK: Find E2806894 with MainActivity.mDid = " + MainActivity.mDid);
                if (MainActivity.mDid.matches("E2806894A")) {
                    //Log.i(TAG, "HelloK: Find E2806894A");
                    MainActivity.mCs108Library4a.setInvBrandId(false);
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
                } else if (MainActivity.mDid.matches("E2806894B")) {
                    //Log.i(TAG, "HelloK: Find E2806894B");
                    MainActivity.mCs108Library4a.setInvBrandId(false);
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 1, 0x203, "1", true);
                    bNeedSelectedTagByTID = false;
                } else if (MainActivity.mDid.matches("E2806894C")) {
                    //Log.i(TAG, "HelloK: Find E2806894C");
                    MainActivity.mCs108Library4a.setInvBrandId(true);
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 1, 0x204, "1", true);
                    bNeedSelectedTagByTID = false;
                }
            } else if (mDid.indexOf("E28011") == 0) bNeedSelectedTagByTID = false;
            //Log.i(TAG, "HelloK: going to setSelectedTagByTID with mDid = " + mDid + " with extra1Bank = " + extra1Bank + ", extra2Bank = " + extra2Bank + ", bNeedSelectedTagByTID = " + bNeedSelectedTagByTID );// ", bMultiBank = " + bMultiBank);
            if (bNeedSelectedTagByTID) MainActivity.mCs108Library4a.setSelectedTagByTID(mDid, 300);
        }/* else if (bMultiBankInventory) {
            CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.accessInventoryBankTitle1);
            if (checkBox.isChecked()) {
                extra1Bank = spinnerBank1.getSelectedItemPosition();
                EditText editText = (EditText) getActivity().findViewById(R.id.accessInventoryOffset1);
                extra1Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) getActivity().findViewById(R.id.accessInventoryLength1);
                extra1Count = Integer.valueOf(editText.getText().toString());
            }
            checkBox = (CheckBox) getActivity().findViewById(R.id.accessInventoryBankTitle2);
            if (checkBox.isChecked()) {
                extra2Bank = spinnerBank2.getSelectedItemPosition();
                EditText editText = (EditText) getActivity().findViewById(R.id.accessInventoryOffset2);
                extra2Offset = Integer.valueOf(editText.getText().toString());
                editText = (EditText) getActivity().findViewById(R.id.accessInventoryLength2);
                extra2Count = Integer.valueOf(editText.getText().toString());
            }
        }*/
/*
        if (bMultiBank == false) {
            MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
            inventoryRfidTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0,
                    false, MainActivity.mCs108Library4a.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, null,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
        } else */{
            if ((extra1Bank != -1 && extra1Count != 0) || (extra2Bank != -1 && extra2Count != 0)) {
                if (extra1Bank == -1 || extra1Count == 0) {
                    extra1Bank = extra2Bank;
                    extra2Bank = 0;
                    extra1Count = extra2Count;
                    extra2Count = 0;
                    extra1Offset = extra2Offset;
                    extra2Offset = 0;
                }
                if (extra1Bank == 1) extra1Offset += 2;
                if (extra2Bank == 1) extra2Offset += 2;
                MainActivity.mCs108Library4a.setTagRead(extra2Count != 0 && extra2Count != 0 ? 2 : 1);
                MainActivity.mCs108Library4a.setAccessBank(extra1Bank, extra2Bank);
                MainActivity.mCs108Library4a.setAccessOffset(extra1Offset, extra2Offset);
                MainActivity.mCs108Library4a.setAccessCount(extra1Count, extra2Count);
                needResetData = true;
            } else resetSelectData();
            MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY);

            Log.i("case 2", "case 2");
            inventoryRfidTask = new InventoryRfidTask(getContext(), extra1Bank, extra2Bank, extra1Count, extra2Count, extra1Offset, extra2Offset,
                    false, MainActivity.mCs108Library4a.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, mDid,
                    null/*rfidRunTime*/, null, null/*rfidVoltageLevel*/, null/*rfidYieldView*/, start, null/*rfidRateView*/);
        }
        inventoryRfidTask.execute();
    }


    private boolean needResetData;
    void resetSelectData() {
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        if (needResetData) {
            MainActivity.mCs108Library4a.setTagRead(0);
            MainActivity.mCs108Library4a.setAccessBank(1);
            MainActivity.mCs108Library4a.setAccessOffset(0);
            MainActivity.mCs108Library4a.setAccessCount(0);
            needResetData = false;
        }
    }

    static boolean resetNeeded = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {
        Log.i("event", "event " + event.getData());
        resetNeeded = false;

        setSearchedEPCList(event.getData());


        ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
        schTaskEx.execute(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < event.getData().size(); i++) {
                    Asset assetFromTable = null;// stockTakeList.getReadMap().get(event.getData().get(i));

                    Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 0 value " + event.getData().get(i) + " " + epcList.contains(event.getData().get(i)));

                    if(epcList.contains(event.getData().get(i))) {
                        //Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 1 value " + assetFromTable.getStatus().id + " " + event.getData().get(i));
                        //if(assetFromTable.getStatus().id != 2 ) {
                        Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 2 " + event.isBarcode());

                        for(int x = 0; x < stockTakeList.myAsset.size(); x++) {
                            if(stockTakeList.myAsset.get(x).getAssetno().equals(event.getData().get(i)) || stockTakeList.myAsset.get(x).getEPC().equals(event.getData().get(i))) {
                                Asset asset = stockTakeList.myAsset.get(x);

                                //Log.i("asset", "asset RFIDDataUpdateEvent " + asset.isFoundByScan() + " " + event.isManually() + " " +asset.getFindType() );

                                if(event.isManually()) {
                                    asset.setFindType("manual");
                                    asset.setScanDateTime(new Date());
                                    if(event.isFound()) {
                                        asset.setFound(true);
                                        asset.getStatus().id = 2;
                                    } else {
                                        asset.setFound(false);
                                        //asset.getStatus().id = 10;
                                        Status status = new Status();
                                        status.id = 10;
                                        asset.setStatus(status);
                                    }
                                } else if(event.isBarcode() && asset.isFoundByScan()) {
                                    asset.setFoundByScan(true);
                                    asset.setScanDateTime(new Date());
                                    asset.setFound(true);
                                    asset.getStatus().id = 2;
                                } else {
                                    asset.setFoundByScan(false);
                                    asset.setScanDateTime(new Date());
                                    asset.setFound(true);
                                    asset.getStatus().id = 2;
                                }

                                Log.i("asset", "asset RFIDDataUpdateEvent " + asset.isFoundByScan() + " " + event.isManually() + " " +asset.getFindType() );

                                if(asset != null)
                                    stockTakeList.myAsset.set(x, asset);

                                resetNeeded = true;
                                break;
                            }
                        }
                        //}
                    } else if(stockTakeList.getAbnormalMap().get(event.getData().get(i)) == null && event.getData().get(i).length() > 0) {

                        // DataBaseHandler dataBaseHandler = new DataBaseHandler(MainActivity.mContext);
                        //List<Asset> assets = dataBaseHandler.getAssetWithEPC(event.getData().get(i), "0");
                        List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("epc", event.getData().get(i)).findAll();

                        Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 3 value " + assetsDetail.size() + " " + event.getData().get(i) + " " + stockTakeList.getAbnormalMap().size());
                        Asset asset = null;

                        if(assetsDetail.size() > 0) {
                            asset = convertAssetDetailToAsset(assetsDetail.get(0));
                            asset.setScanDateTime(new Date());
                            Status status = new Status();
                            status.id = 9;
                            asset.setStatus(status);
                            asset.setScanDateTime(new Date());

                            stockTakeList.addAssets(asset);

                            resetNeeded = true;

                            Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 4 " + assetsDetail.get(0).getAssetNo());
                        } else {
                            Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 5");

                            asset = new Asset();
                            asset.setEPC(event.getData().get(i));
                            Status status = new Status();
                            status.id = 9;
                            asset.setStatus(status);
                            asset.setScanDateTime(new Date());
                            asset.setEPCOnly(true);

                            stockTakeList.addAssets(asset);

                            resetNeeded = true;

                            if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                                if (!requestedEPCList.contains(event.getData().get(i))) {
                                    RetrofitClient.getSPGetWebService().getBriefAssetInfo(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),
                                            Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""), event.getData().get(i)).enqueue(new GetBriefAssetObjectCallback(true));
                                    requestedEPCList.add(event.getData().get(i));
                                    Log.i("requested", "requested " + event.getData().get(i));
                                }
                            } else {

                            }
                        }

                        if(asset != null && stockTakeList.checkExist.get(event.getData()) == null) {
                            Log.i("asset", "RFIDDataUpdateEvent case 6 " + asset.getAssetno() + " " + asset.getEPC());
                            stockTakeList.getAbnormalMap().put(event.getData().get(i), asset);
                        }
                    }
                }
            }
        });

        if(resetNeeded) {

//            stockTakeList.proccessedAllAssets.clear();
            stockTakeList.proccessedReadAssets.clear();
            stockTakeList.proccessedUnReadAssets.clear();

            resetNeeded = false;

            List<Asset> assets = filter(getData(), filterText);

            //listView.setSelection(0);

            assetListAdapter.setData(assets, getActivity());
            handleNoResult(assets);
            //assetListAdapter.notifyDataSetChanged();
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName() + " (" + assets.size() + ")");

            Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent case 6");
        }
        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
    }

    public Asset convertAssetDetailToAsset(AssetsDetail assetDetail) {
        Asset asset = new Asset();
        asset.setAssetno(assetDetail.getAssetNo());
        asset.setName(assetDetail.getName());

        if(assetDetail.getStatusid() != null) {
            Status status = new Status();
            String language = Hawk.get(InternalStorage.Setting.LANGUAGE, "en");

            status.setName(status.getStatus(language, assetDetail.getStatusid()));
            asset.setStatus(status);
        }

        asset.setBrand(assetDetail.getBrand());
        asset.setModel(assetDetail.getBrand());
        asset.setSerialNo(assetDetail.getSerialno());
        asset.setUnit(assetDetail.getUnit());

        asset.setRono(assetDetail.getRono());
        asset.setStartdate(assetDetail.getStartdate());
        asset.setEnddate(assetDetail.getEnddate());
        asset.setCertUrl(assetDetail.getCertUrl());
        asset.setCertType(assetDetail.getCertType());
        asset.setIsverified(assetDetail.isIsverified());

        ArrayList<Category> categoryArrayList = new ArrayList<>();

        if(assetDetail.getCategorys() != null) {
            for (int i = 0; i < assetDetail.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(assetDetail.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);

        ArrayList<Location> locationArrayList = new ArrayList<>();

        if(assetDetail.getLocations() != null) {
            for (int i = 0; i < assetDetail.getLocations().size(); i++) {
                Location location = new Location();
                location.setName(assetDetail.getLocations().get(i));
                locationArrayList.add(location);
            }
        }
        asset.setLocations(locationArrayList);

        asset.setLastStockDate(assetDetail.getLastStockDate());

        CreateBy createBy = new CreateBy();
        createBy.setCreatedById(assetDetail.getCreatedById());
        createBy.setName(assetDetail.getCreatedByName());

        asset.setCreated_by(createBy);

        asset.setCreateDate(assetDetail.getCreatedDate());
        asset.setPurchaseDate(assetDetail.getPurchaseDate());
        asset.setInvoiceDate(assetDetail.getInvoiceDate());
        asset.setInvoiceNo(assetDetail.getInvoiceNo());
        asset.setFundingSource(assetDetail.getFundingSourcename());
        asset.setSupplier(assetDetail.getSupplier());
        asset.setMaintenanceDate(assetDetail.getMaintenanceDate());
        asset.setCost(assetDetail.getCost());
        asset.setPracticalValue(assetDetail.getPraticalValue());

        try {
            int estimatedLifeTime = Integer.parseInt(assetDetail.getEstimatedLifetime());
            asset.setEstimatedLifeTime(estimatedLifeTime);
        } catch (Exception e) {
            asset.setEstimatedLifeTime(-99999);
        }

        TagType tagType = new TagType();
        tagType.setName(assetDetail.getTypeOfTag());
        asset.setTag_type(tagType);

        asset.setBarcode(assetDetail.getBarcode());
        asset.setEPC(assetDetail.getEpc());
        return asset;
    }

    private String filterText;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {

        filterText = event.getTitle().toLowerCase();
        Log.i("filterText", "filterText " + filterText);

        if(filterText == null || filterText.length() == 0) {
            Log.i("cccc", "cccc");
            //setupListView(getData());
            return;
        } else {
            Log.i("cccc", "cccc 1 ");
            List<Asset> data = filter(getData(), filterText);
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(stockTakeList.getName() + " (" + data.size() + ")");

            setupListView(data);
        }
    }

    public List<Asset> filter(List<Asset> raw, String filterText) {
        if(raw == null) return new ArrayList<>();
        if(filterText == null || filterText.length() == 0 ) return raw;

        if(raw != null && raw.size() > 0) {
            List<Asset> myAsset = new ArrayList<>();

            for(int i = 0; i < raw.size(); i++) {
                boolean exist = false;

                if(raw.get(i).getAssetno() != null && raw.get(i).getAssetno().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(raw.get(i).getName() != null && raw.get(i).getName().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(raw.get(i).getBrand() != null && raw.get(i).getBrand().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(raw.get(i).getModel() != null && raw.get(i).getModel().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(raw.get(i).getCategoryString() != null && raw.get(i).getCategoryString().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(raw.get(i).getLocationString() != null && raw.get(i).getLocationString().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(raw.get(i).getEPC() != null && raw.get(i).getEPC().toLowerCase().contains(filterText)) {
                    exist = true;
                }

                if(exist)
                    myAsset.add(raw.get(i));
            }


            return myAsset;
        }

        return raw;
    }


    public void openScanner() {
        MainActivity.SKIP_DOWNLOAD_ONCE = true;
        new IntentIntegrator(getActivity())
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .setPrompt("")
                .setCameraId(0)
                .setBeepEnabled(true)
                .setBarcodeImageEnabled(true)
                .setCaptureActivity(CaptureActivityPortrait.class)
                .initiateScan();
    }
}
