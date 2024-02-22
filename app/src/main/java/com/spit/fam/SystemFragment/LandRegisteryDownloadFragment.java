package com.spit.fam.SystemFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.spit.fam.BaseUtils;
import com.spit.fam.Entity.OfflineMode.BorrowAssets;
import com.spit.fam.Entity.OfflineMode.ChangeEpc;
import com.spit.fam.Entity.OfflineMode.DisposalAssets;
import com.spit.fam.Entity.OfflineMode.ReturnAssets;
import com.spit.fam.Entity.PermissionCallbackEvent;
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset;
import com.spit.fam.Entity.RenewEntity.RenewFileToByte;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.StockTakeListData;
import com.spit.fam.Entity.SPEntityP3.BorrowAsset;
import com.spit.fam.Entity.SPEntityP3.BorrowDetailResponse;
import com.spit.fam.Entity.SPEntityP3.BorrowListItem;
import com.spit.fam.Entity.SPEntityP3.DisposalDetailResponse;
import com.spit.fam.Entity.SPEntityP3.DisposalListItem;
import com.spit.fam.Entity.SPEntityP3.ReturnAsset;
import com.spit.fam.Entity.SPEntityP3.SearchNoEpcItem;
import com.spit.fam.Entity.SPEntityP3.StocktakeList;
import com.spit.fam.Entity.SpEntity.StrJson;
import com.spit.fam.Event.FileLoadingCompletedEvent;
import com.spit.fam.Event.InsertEvent;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.Event.PendingToAdd;
import com.spit.fam.Event.ProgressEvent;
import com.spit.fam.Event.ReadFileCallbackEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.Response.UserListResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;

public class LandRegisteryDownloadFragment extends BaseFragment {
    public static boolean AUTO_SYNC = true;
    public static boolean BACK_PRESS = false;

    public static String userDefinedCompanyId = "LandRegistry";//Hawk.get(InternalStorage.Setting.COMPANY_ID, "LandRegistry") ;

    private static String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    private static String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

    public static String userList = "userList";
    public static String assetsDetail = "assetsDetail";
    public static String appsID = "appsID";

    public static String searchnoepc = "searchnoepc";

    public static String borrowList = "borrowList";
    public static String borrowListAssets = "borrowListAssets";

    public static String returnList = "returnList";

    public static String stockTakeList = "stockTakeList";
    public static String stockTakeListAsset = "stockTakeListAsset";

    public static String listingLevel = "listingLevel";
    public static String listing = "listing";

    public static String cardNo = "cardNo";

    public static int LEVEL = 1;
    public static String root = "";

    public static String disposalList = "disposalList";
    public static String disposalListAssets = "disposalListAssets";

    public static String borrowAssets = "borrowAssets";
    public static String returnAssets = "returnAssets";
    public static String disposalAssets = "disposalAssets";
    public static String UploadStockTake = "UploadStockTake";

    public static String FileToByte = "FileToByte";

    public static String setEpc = "setEpc";
    public static String changeEpc = "changeEpc";

    public static boolean userListReady, assetsDetailReady, searchnoepcReady, borrowList0Ready, borrowList1Ready, borrowList2Ready, disposalList0Ready, disposalList1Ready, disposalList2Ready, returnListReady, stockTakeListReady, listingLevelReady;

    ArrayList<LevelData> pendingToAdds = new ArrayList<>();

    private ArrayList<String> stockTakeNo = new ArrayList<>();
    private ArrayList<String> borrowNo = new ArrayList<>();
    private ArrayList<String> disposalNo = new ArrayList<>();

    private TextView loadingTextView = null;
    private ProgressBar progressbar = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");

        view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.gov_download_fragment, null);
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        ((TextView)view.findViewById(R.id.toolbar_title)).setText(R.string.sync);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)MainActivity.mContext).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        loadingTextView = view.findViewById(R.id.loading);
        progressbar = view.findViewById(R.id.progressbar);

       // FileUtils.writeFromFile("[" +new Gson().toJson(appsID) +"]", "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/", "startSync");

        FileUtils.writeFromFile("[]", "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/", "startSync");
        return view;
    }

    public void onPause() {
        super.onPause();
        FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/startSync.json");

        if(runnable != null) {
            if(handler != null) {
                handler.removeCallbacks(runnable);
                runnable = null;
            }
        }
    }

    public void onResume() {
        super.onResume();

        syncData();

        /*
        ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
        schTaskEx.execute(new Runnable() {
            @Override
            public void run() {

                //writeData();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    }
                }, 500);

            }
        });*/

    }

    private Handler handler;
    private Runnable runnable;

    public void syncData() {
        //BaseUtils.parseStockTakeJson("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + stockTakeListAsset + "_0000000174.json");
        serverId =  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

        userListReady = false;
        assetsDetailReady = false;
        searchnoepcReady = false;
        borrowList0Ready = false;
        borrowList0Ready = false;
        borrowList1Ready = false;
        borrowList2Ready = false;
        disposalList0Ready = false;
        disposalList1Ready = false;
        disposalList2Ready = false;
        returnListReady = false;
        stockTakeListReady = false;
        listingLevelReady = false;

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/" + userDefinedCompanyId + "/");
        folder.mkdirs();

        File folder2 = new File(Environment.getExternalStorageDirectory().toString() + "/" + userDefinedCompanyId + "/Download");
        folder2.mkdirs();

        File folder3 = new File(Environment.getExternalStorageDirectory().toString() + "/" + userDefinedCompanyId + "/Upload");
        folder3.mkdirs();

        //File file = new File(Environment.getExternalStorageDirectory().toString()+ "/" + userDefinedCompanyId + "/Download/" +  assetsDetail + ".json");

        //Hawk.put(InternalStorage.Setting.COMPANY_ID, userDefinedCompanyId);
        //Hawk.put(InternalStorage.Setting.HOST_ADDRESS, "icloud.securepro.com.hk");
        InternalStorage.resetStaticPath();

        requestPermissionIfNeeded();

        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingTextView.setText(MainActivity.mContext.getString(R.string.waiting) + " ...");
            }
        });

        if(Hawk.get(InternalStorage.OFFLINE_CACHE.USER_LIST) != null && Hawk.get(InternalStorage.Login.USER_ID) != null /*FileUtils.checkFileExist(appsID)*/) {

            handler = new Handler(Looper.getMainLooper());

            runnable = new Runnable() {
                @Override
                public void run() {

                    boolean isExist = FileUtils.isFileExist("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");
                    Log.i("isExist", "isExist "+ isExist);

                     if(!isExist) {
                        handler.postDelayed(this, 500);
                     } else {

                         ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                         schTaskEx.execute(new Runnable() {
                             @Override
                             public void run() {
                                 Log.i("appsID", "appsID case 1 ");

                                 ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                                 schTaskEx.execute(new Runnable() {
                                     @Override
                                     public void run() {

                                         Realm.getDefaultInstance().beginTransaction();
                                         Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("userid",Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")).findAll().deleteAllFromRealm();
                                         Realm.getDefaultInstance().commitTransaction();

                                         parseDataToDatabase(FileUtils.readFromFile(listingLevel), listingLevel);
                                         BaseUtils.parseLargeJson("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + assetsDetail + ".json");
                                     }
                                 });
                                 FileUtils.deleteFile("success");

                                 String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

                                 RealmResults<ChangeEpc> changeEpcs = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("userid", userid).findAll();

                                 RealmResults<BorrowAssets> borrowAssets = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("userid", userid).findAll();

                                 RealmResults<DisposalAssets> disposalAssets = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("userid", userid).findAll();

                                 RealmResults<RealmStockTakeListAsset> realmStockTakeListAsset = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).equalTo("userId", userid).findAll();

                                 RealmResults<ReturnAssets> returnAssets = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).findAll();

                                 Realm.getDefaultInstance().beginTransaction();

                                 changeEpcs.deleteAllFromRealm();
                                 borrowAssets.deleteAllFromRealm();
                                 disposalAssets.deleteAllFromRealm();
                                 realmStockTakeListAsset.deleteAllFromRealm();
                                 returnAssets.deleteAllFromRealm();
                                 Realm.getDefaultInstance().commitTransaction();

                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + changeEpc + ".json");
                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + LandRegisteryDownloadFragment.borrowAssets + ".json");
                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + LandRegisteryDownloadFragment.disposalAssets + ".json");
                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + LandRegisteryDownloadFragment.returnAssets + ".json");
                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + UploadStockTake + ".json");
                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/" + FileToByte + ".json");

                                 FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");

                             }
                         });

                     }
                }
            };

            handler.post(runnable);

        } else if(Hawk.get(InternalStorage.OFFLINE_CACHE.USER_LIST) != null) {
            Log.i("appsID", "appsID case 2 ");
            ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeFragment(new LoginFragment());
                    FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");

                }
            });
        } else {
            Log.i("appsID", "appsID case 3 ");
            parseDataToDatabase(FileUtils.readFromFile(userList), userList);
            //empty app, new install sync all data
        }
    }

    public void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_PERMISSION_STORAGE = 100;
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for (String str : permissions) {
                if (MainActivity.mContext.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, REQUEST_CODE_PERMISSION_STORAGE);
                    return;
                }
            }
        }
    }


    public static void writeData() {
        String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");

        Log.i("user", "userid " + userid);

        RealmResults<ChangeEpc> changeEpcs = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("userid", userid).findAll();

        RealmResults<BorrowAssets> borrowAssets = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("userid", userid).findAll();

        RealmResults<DisposalAssets> disposalAssets = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("userid", userid).findAll();

        RealmResults<RealmStockTakeListAsset> realmStockTakeListAsset = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).equalTo("userId", userid).findAll();

        RealmResults<ReturnAssets> returnAssets = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).findAll();

        Gson gson = new Gson();
        StringBuilder sb = new StringBuilder();
        for(ChangeEpc d : changeEpcs) {
            ChangeEpc temp = Realm.getDefaultInstance().copyFromRealm(d);
            temp.setPk(null);
            sb.append(gson.toJson(temp));
            sb.append(",");
        }
        if(changeEpcs.size() > 0) {
            String changeEpcs_json = "[" + sb.toString().substring(0, sb.toString().length() > 0 ? sb.toString().length() - 1 : 0) + "]";

            Log.i("change_json", "change_json " + changeEpcs_json + " " + sb.toString().length());
            FileUtils.writeToFile(changeEpcs_json, "changeEpc");
        }

        sb = new StringBuilder();
        for(DisposalAssets d : disposalAssets) {
            DisposalAssets temp = Realm.getDefaultInstance().copyFromRealm(d);
            temp.setPk(null);
            sb.append(gson.toJson(temp));
            sb.append(",");
        }

        if(disposalAssets.size() > 0) {
            String disposalAssets_json = "[" + sb.toString().substring(0, sb.toString().length() > 0 ? sb.toString().length() - 1 : 0) + "]";

            Log.i("disposalAssets_json", "disposalAssets_json " + disposalAssets_json);
            FileUtils.writeToFile(disposalAssets_json, "disposalAssets");
        }

        sb = new StringBuilder();
        for(BorrowAssets d : borrowAssets) {
            BorrowAssets temp = Realm.getDefaultInstance().copyFromRealm(d);
            temp.setPk(null);
            sb.append(gson.toJson(Realm.getDefaultInstance().copyFromRealm(d)));
            sb.append(",");
        }

        if(borrowAssets.size() > 0) {
            String borrowAssets_json = "[" + sb.toString().substring(0, sb.toString().length() > 0 ? sb.toString().length() - 1 : 0) + "]";

            Log.i("borrowAssets_json", "borrowAssets_json " + borrowAssets_json);
            FileUtils.writeToFile(borrowAssets_json, "borrowAssets");
        }

        sb = new StringBuilder();

        String strJson = "";

        for(RealmStockTakeListAsset d : realmStockTakeListAsset) {
            RealmStockTakeListAsset temp = Realm.getDefaultInstance().copyFromRealm(d);
            temp.setPk(null);
            sb.append(gson.toJson(temp));
            sb.append(",");


            StrJson object = new StrJson();
            object.setAssetName(d.getName());
            object.setAssetNo(d.getAssetno());
            object.setBrand(d.getBrand());
            object.setCategoryName(d.getCategory());
            object.setEPC(d.getEpc());

            if(d.getFindType() != null && d.getFindType().equals("rfid")) {
                object.setFoundStatus(116);
            }
            if(d.getFindType() != null && d.getFindType().equals("barcode")) {
                object.setFoundStatus(117);
            }
            if(d.getFindType() != null && d.getFindType().equals("manual")) {
                object.setFoundStatus(118);
            }

            object.setLocationName(d.getLocation());
            object.setModelNo(d.getModel());
            object.setQRCode("");
            object.setRemarks(d.getRemarks());

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                object.setScanDate(df.format(d.getScanDateTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            object.setLoginID(d.getUserName());
            object.setOrderNo(d.getStocktakeno());
            if(d.getStatusid() == 2) {
                object.setStatusID(1);

                if(d.getFindType() != null && d.getFindType().equals("rfid")) {
                    object.setFoundStatus(116);
                }
                if(d.getFindType() != null && d.getFindType().equals("barcode")) {
                    object.setFoundStatus(117);
                }
                if(d.getFindType() != null && d.getFindType().equals("manual")) {
                    object.setFoundStatus(118);
                }
            } else if(d.getStatusid() == 10) {

                if(d.getFindType() != null && d.getFindType().equals("rfid")) {
                    object.setFoundStatus(116);
                }

            } else if(d.getStatusid() == 9) {
                object.setStatusID(2);
            }

            object.setUserName(d.getUserName());
            object.setUserid(d.getUserId());


            strJson += new Gson().toJson(object).toString().replace("\\u003e", ">");
            strJson += ",";

            String rono = d.getRono();
        }

        Log.i("ssssss", "ssssssss" + realmStockTakeListAsset.size());

        if(realmStockTakeListAsset.size() > 0) {
            String realmStockTakeListAsset_json = "[" + strJson.substring(0, strJson.length() > 0 ? strJson.length() - 1 : 0) + "]";
            FileUtils.writeToFile(realmStockTakeListAsset_json, "UploadStockTake");
        }

        sb = new StringBuilder();
        for(ReturnAssets d : returnAssets) {
            ReturnAssets temp = Realm.getDefaultInstance().copyFromRealm(d);
            temp.setPk(null);
            sb.append(gson.toJson(Realm.getDefaultInstance().copyFromRealm(d)));
            sb.append(",");
        }

        if(returnAssets.size() > 0) {
            String returnAssets_json = "[" + sb.toString().substring(0, sb.toString().length() > 0 ? sb.toString().length() - 1 : 0) + "]";
            FileUtils.writeToFile(returnAssets_json, "returnAssets");
        }

        sb = new StringBuilder();
        ArrayList<RenewFileToByte> arrayList = new ArrayList<>();//gson.fromJson(data,new TypeToken<List<RenewFileToByte>>(){}.getType());
        ArrayList<RenewFileToByte> fileToBytes = new ArrayList<>();//gson.fromJson(data,new TypeToken<List<RenewFileToByte>>(){}.getType());

        for(RealmStockTakeListAsset d : realmStockTakeListAsset) {
            sb.append(gson.toJson(Realm.getDefaultInstance().copyFromRealm(d)));
            String[] arr = null;
            try {
                arr = d.getPic().split(",");

                ArrayList<String> data = new ArrayList<>();

                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].startsWith("http")) {

                    } else {
                        data.add(arr[i]);
                    }
                }
                arr = data.toArray(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String assetNo = d.getRono();
            String userName = d.getUserName();
            String stockTakeNo = d.getStocktakeno();


            if(arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    int tempPos = i;
                    String picdata = arr[i];
                    Log.i("picdata", "picdata  " + picdata);

                    RenewFileToByte renewFileToByte = new RenewFileToByte();
                    renewFileToByte.setCompanyID(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                    renewFileToByte.setFileLoc(tempPos);
                    renewFileToByte.setiCode(assetNo);
                    renewFileToByte.setLoginID(userName);
                    renewFileToByte.setPassCode(stockTakeNo);
                    renewFileToByte.setStr(picdata);
                    renewFileToByte.setSuffix("png");
                    renewFileToByte.setOrderNo(stockTakeNo);

                    fileToBytes.add(renewFileToByte);
                }
            }
        }

        if(fileToBytes.size() > 0) {
            for (int i = 0; i < fileToBytes.size(); i++) {
                int finalI = i;
                ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.mContext)
                                .asBitmap().load(fileToBytes.get(finalI).getStr()).into(new SimpleTarget<Bitmap>(500, 500) {

                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {


                                File f = new File(MainActivity.mContext.getCacheDir(), "asd.jpg");
                                try {
                                    f.createNewFile();


                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                                    String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);


                                    String data = FileUtils.readFromFile("/sdcard/LandRegistry/Upload", "FileToByte");

                                    RenewFileToByte renewFileToByte = new RenewFileToByte();
                                    renewFileToByte.setCompanyID(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                                    renewFileToByte.setFileLoc(fileToBytes.get(finalI).getFileLoc());
                                    renewFileToByte.setiCode(fileToBytes.get(finalI).getiCode());
                                    renewFileToByte.setLoginID(fileToBytes.get(finalI).getLoginID());
                                    renewFileToByte.setPassCode(fileToBytes.get(finalI).getPassCode());
                                    renewFileToByte.setStr(encodedString);
                                    renewFileToByte.setSuffix("png");
                                    renewFileToByte.setOrderNo(fileToBytes.get(finalI).getPassCode());
                                    arrayList.add(renewFileToByte);


                                    if (finalI == arrayList.size() - 1) {
                                        String result = "[";

                                        for (RenewFileToByte de : arrayList) {
                                            result += "{";
                                            result += "'companyID' : \"" + de.getCompanyID() + "\",";
                                            result += "'fileLoc' : \"" + de.getFileLoc() + "\",";
                                            result += "'iCode' : \"" + de.getiCode() + "\",";
                                            result += "'loginID' : \"" + de.getLoginID() + "\",";
                                            result += "'passCode' : \"" + de.getPassCode() + "\",";
                                            result += "'str' : \"" + de.getStr() + "\",";
                                            result += "'Suffix' : \"" + de.getSuffix() + "\",";
                                            result += "'orderNo' : \"" + de.getOrderNo() + "\"";

                                            result += "},";
                                        }

                                        if (arrayList.size() > 0) {
                                            result = result.substring(0, result.length() - 1);

                                            result += "]";

                                            Log.i("renewFileToByte_json", "renewFileToByte_json " + result + " ");
                                            FileUtils.writeToFile(result, "FileToByte");
                                        }

                                        FileOutputStream fos = null;

                                        fos = new FileOutputStream(f);

                                        fos.write(byteArray);
                                        fos.flush();
                                        fos.close();

                                        //Log.i("FileLoadingCompletedEvent", "FileLoadingCompletedEvent 1");

                                        //EventBus.getDefault().post(new FileLoadingCompletedEvent());

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        } else {
            //Log.i("FileLoadingCompletedEvent", "FileLoadingCompletedEvent 2");
            //.getDefault().post(new FileLoadingCompletedEvent());
        }
        EventBus.getDefault().post(new FileLoadingCompletedEvent());
    }


    /*

                ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.mContext)
                                .asBitmap().load(picdata ).into(new SimpleTarget<Bitmap>(500,500) {

                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {


                                File f = new File(MainActivity.mContext.getCacheDir(), "asd.jpg");
                                try {
                                    f.createNewFile();


                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                                    String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);


                                    String data = FileUtils.readFromFile("/sdcard/LandRegistry/Upload", "FileToByte");

                                    RenewFileToByte renewFileToByte = new RenewFileToByte();
                                    renewFileToByte.setCompanyID( Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                                    renewFileToByte.setFileLoc(tempPos);
                                    renewFileToByte.setiCode(assetNo);
                                    renewFileToByte.setLoginID(userName);
                                    renewFileToByte.setPassCode(stockTakeNo);
                                    renewFileToByte.setStr(encodedString);
                                    renewFileToByte.setSuffix("png");

                                    arrayList.add(renewFileToByte);



                                    String result = "[";

                                    for(RenewFileToByte de : arrayList) {
                                        result += "{";
                                        result += "'companyID' : \"" + de.getCompanyID() + "\",";
                                        result += "'fileLoc' : \"" + de.getFileLoc() + "\",";
                                        result += "'iCode' : \"" + de.getiCode() + "\",";
                                        result += "'loginID' : \"" + de.getLoginID() + "\",";
                                        result += "'passCode' : \"" + de.getPassCode() + "\",";
                                        result += "'str' : \"" + de.getStr() + "\",";
                                        result += "'Suffix' : \"" + de.getSuffix() + "\"";

                                        result += "},";
                                    }

                                    if(arrayList.size() > 0) {
                                        result = result.substring(0, result.length() - 1);
                                    }

                                    result += "]";

                                    Log.i("renewFileToByte_json", "renewFileToByte_json " + result + " " );
                                    FileUtils.writeToFile(result, "FileToByte");


                                    FileOutputStream fos = null;

                                    fos = new FileOutputStream(f);

                                    fos.write(byteArray);
                                    fos.flush();
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProgressEvent dialogEvent) {
        int nowPercentage = (int)((float)dialogEvent.getCount() / (float) dialogEvent.getTotal() * 100);
        progressbar.setProgress(nowPercentage);
        if(nowPercentage != 100) {
            progressbar.setVisibility(View.VISIBLE);
        } else {
            progressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(InsertEvent dialogEvent) {
        Log.i("InsertEvent", "InsertEvent" + dialogEvent.getAssetNo() + " " + dialogEvent.getName());
        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingTextView.setText(MainActivity.mContext.getString(R.string.transfering) + " " + dialogEvent.getAssetNo() + " " + dialogEvent.getName());
            }
        });
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PermissionCallbackEvent dialogEvent) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        if(Hawk.get("first") == null) {

            Hawk.put("first", "first");
            FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");
            FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/userList.json");
            FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload/appsID.json");

            deleteRecursive(new File("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download"));
            deleteRecursive(new File("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Upload"));

            FileUtils.writeFromFile("[" +new Gson().toJson(appsID) +"]", "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/", "startSync");
        }
        syncData();
        /*
        ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
        schTaskEx.execute(new Runnable() {
                              @Override
                              public void run() {
                                  //writeData();


                                  Handler handler = new Handler();
                                  handler.postDelayed(new Runnable() {
                                      @Override
                                      public void run() {
                                          //syncData();
                                      }
                                  }, 500);
                              }
                          });*/

     }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkInventoryDoneEvent event) {
        if(event.getName().equals("inventory")) {
            //Log.i("NetworkInventoryDoneEvent", "NetworkInventoryDoneEvent case 0");
            parseDataToDatabase(FileUtils.readFromFile(searchnoepc + "_" + serverId), searchnoepc);
        } else {
            if(stockTakeNo.size() > 0) {
                Log.i("stockTakeNo11", "stockTakeNo13 " + stockTakeNo.size());
                //Log.i("NetworkInventoryDoneEvent", "NetworkInventoryDoneEvent case 1");

                BaseUtils.parseStockTakeJson("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + stockTakeListAsset + "_" + serverId + "_" + stockTakeNo.get(stockTakeNo.size() - 1) + ".json");
                stockTakeNo.remove(stockTakeNo.size() - 1);

            } else {
                //Log.i("NetworkInventoryDoneEvent", "NetworkInventoryDoneEvent case 2");
                Log.i("SearchListFragment", "SearchListFragment case 3");

                changeStaticFragment(new SearchListFragment());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReadFileCallbackEvent event) {
        Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent" + event.getType() + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID));

        if (event.getType().equals(userList)) {
            if (Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID) == null) {
                changeStaticFragment(new LoginFragment());
                FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");
            } else {
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent searchnoepc");
                parseDataToDatabase(FileUtils.readFromFile(searchnoepc + "_" + serverId), searchnoepc);
            }
        } else if (event.getType().equals(searchnoepc)) {
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent borrowList_0");
                    parseDataToDatabase(FileUtils.readFromFile(borrowList  + "_" + serverId + "_0"), borrowList + "_0");
                }
            });
        } else if (event.getType().equals(borrowList + "_" + 0)) {
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent borrowList_1");
                    parseDataToDatabase(FileUtils.readFromFile(borrowList  + "_" + serverId + "_1"), borrowList + "_1");
                }
            });
        } else if (event.getType().equals(borrowList + "_" + 1)) {
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent borrowList_2");
                    parseDataToDatabase(FileUtils.readFromFile(borrowList  + "_" + serverId + "_2"), borrowList + "_2");
                }
            });
        } else if (event.getType().equals(borrowList + "_" + 2)) {
            /*ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    parseDataToDatabase(FileUtils.readFromFile(disposalList + "_0"), disposalList + "_0");
                }
            });*/
            //DialogEvent dialogEvent = new DialogEvent("borrowNo" + FileUtils.readFromFile(MainActivity.mContext, borrowListAssets  + "_" + serverId + "_" + borrowNo.get(borrowNo.size() - 1)), borrowNo.size() +" " + borrowListAssets  + "_" + serverId + "_" + borrowNo.get(borrowNo.size() - 1));
            //EventBus.getDefault().post(dialogEvent);
            if(borrowNo.size() > 0) {
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent borrowListAssets");
                parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, borrowListAssets  + "_" + serverId + "_" + borrowNo.get(borrowNo.size() - 1)), borrowListAssets + "_" + borrowNo.get(borrowNo.size() - 1));
            } else {
                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent disposalList_0");
                        parseDataToDatabase(FileUtils.readFromFile(disposalList  + "_" + serverId + "_0"), disposalList + "_0");
                    }
                });
            }
        } else if (event.getType().contains(borrowListAssets)){
            Log.i("borrowNo", "borrowNo " + borrowNo.size());
            if(borrowNo.size() > 0) {
                //disposalNo.remove(disposalNo.size() - 1);
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent borrowListAssets");
                parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, borrowListAssets + "_" + serverId + "_" + borrowNo.get(borrowNo.size() - 1)), borrowListAssets + "_" + borrowNo.get(borrowNo.size() - 1));
            } else {
                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent disposalList_0");
                        parseDataToDatabase(FileUtils.readFromFile(disposalList + "_" + serverId + "_0"), disposalList + "_0");
                    }
                });
            }
        } else if (event.getType().equals(disposalList + "_" + 0)) {
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent disposalList_1");
                    parseDataToDatabase(FileUtils.readFromFile(disposalList  + "_" + serverId + "_1"), disposalList + "_1");
                }
            });
        } else if (event.getType().equals(disposalList + "_" + 1)) {
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent disposalList_2");
                    parseDataToDatabase(FileUtils.readFromFile(disposalList  + "_" + serverId + "_2"), disposalList + "_2");
                }
            });
        } else if (event.getType().equals(disposalList + "_" + 2)) {
            if(disposalNo.size() > 0) {
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent disposalListAssets");
                parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets  + "_" + serverId + "_" + disposalNo.get(disposalNo.size() - 1)), disposalListAssets + "_" + disposalNo.get(disposalNo.size() - 1));
            } else {
                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent returnList");
                        parseDataToDatabase(FileUtils.readFromFile(returnList + "_" + serverId), returnList);
                    }
                });
            }
        } else if (event.getType().contains(disposalListAssets)){
            Log.i("disposalNo", "disposalNo " + disposalNo.size());
            if(disposalNo.size() > 0) {
                //disposalNo.remove(disposalNo.size() - 1);
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent disposalListAssets");
                parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets  + "_" + serverId + "_" + disposalNo.get(disposalNo.size() - 1)), disposalListAssets + "_" + disposalNo.get(disposalNo.size() - 1));
            } else {
                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent returnList");
                        parseDataToDatabase(FileUtils.readFromFile(returnList + "_" + serverId), returnList);
                    }
                });
            }
        } else if(event.getType().equals(returnList)) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent stockTakeList");
                    parseDataToDatabase(FileUtils.readFromFile(stockTakeList + "_" + serverId), stockTakeList);
                }
            });
        } else if(event.getType().contains(stockTakeListAsset)) {
            Log.i("stockTakeNo", "stockTakeNo " + stockTakeNo.size());

            if(stockTakeNo.size() == 0) {
                Log.i("SearchListFragment", "SearchListFragment case 1");

                changeStaticFragment(new SearchListFragment());
            } else {
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent stockTakeListAsset");

                BaseUtils.parseStockTakeJson("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + stockTakeListAsset + "_" + serverId + "_" + stockTakeNo.get(stockTakeNo.size() - 1) + ".json");
                stockTakeNo.remove(stockTakeNo.size() - 1);
            }
        } else if(event.getType().contains(stockTakeList)) {

            if(stockTakeNo.size() > 0) {
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent stockTakeListAsset");

                BaseUtils.parseStockTakeJson("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + stockTakeListAsset + "_" + serverId + "_" + stockTakeNo.get(stockTakeNo.size() - 1) + ".json");

                try {
                    stockTakeNo.remove(stockTakeNo.size() - 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Log.i("ReadFileCallbackEvent", "ReadFileCallbackEvent SearchListFragment");
                Log.i("SearchListFragment", "SearchListFragment case 2");

                changeStaticFragment(new SearchListFragment());
            }
        } /*else if(borrowNo.size() > 0) {
            parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, borrowListAssets + "_" + borrowNo.get(borrowNo.size() - 1)), borrowListAssets + "_" + borrowNo.get(borrowNo.size() - 1));
        } else if(disposalNo.size() > 0) {
            parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets + "_" + disposalNo.get(disposalNo.size() - 1)), disposalListAssets + "_" + disposalNo.get(disposalNo.size() - 1));
        }*/
    }
    public void parseDataToDatabase(String raw, String type) {
        Log.i("parseDataToDatabase", "parseDataToDatabase " + raw + " " + type);
        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingTextView.setText(getString(R.string.transfering) + " " + type + "...");
            }
        });

        if(type == null || raw == null) {
            return;
        }

        Gson gson = new Gson();
        if(type.equals(cardNo)) {


        } else if(type.equals(userList)) {

            handler = new Handler(Looper.getMainLooper());

            runnable = new Runnable() {
                @Override
                public void run() {
                    Log.i("userList", "userList start");

                    UserListResponse userListResponse = gson.fromJson(FileUtils.readFromFile(userList), UserListResponse.class);
                    if(userListResponse != null) {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, userListResponse.getThiscalldate());
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, userListResponse.getData());
                        Hawk.put(InternalStorage.OFFLINE_CACHE.USER_LIST, userListResponse.getData());

                        for (int i = 0; i < userListResponse.getData().size(); i++) {
                            if (userListResponse.getData().get(i).getNfcCardNo().equals(Hawk.get(InternalStorage.Login.CARD_NUMBER) /*Hawk.get(InternalStorage.Login.USER_ID, "").toLowerCase()*/)) {
                                Log.i("UserListResponse", "UserListResponse USER_ID " + userListResponse.getData().get(i).getUserid());

                                Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID, userListResponse.getData().get(i).getUserid());
                                Hawk.put(InternalStorage.Login.USER_ID, userListResponse.getData().get(i).getUserid());
                                Hawk.put(InternalStorage.Login.PASSWORD, userListResponse.getData().get(i).getPassword());

                                InternalStorage.resetStaticPath();
                            }
                        }
                    }

                    if(FileUtils.readFromFile(userList) != null && FileUtils.readFromFile(userList).length() > 0) {
                        userListReady = true;
                        Log.i("userListReady", "userListReady ");
                        if(userListResponse != null && userListResponse.getData().size() > 0)
                            Hawk.put(InternalStorage.OFFLINE_CACHE.USER_LIST, userListResponse);

                        ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
                        readFileCallbackEvent.setFileName(type);
                        EventBus.getDefault().post(readFileCallbackEvent);
                    } else {
                        handler.postDelayed(this, 100);
                    }
                }
            };

            handler.post(runnable);

        } else if(type.equals(assetsDetail)) {
            if(raw != null && raw.length() > 0) {
                //assetsDetailReady = true;
                Log.i("assetsDetailReady", "assetsDetailReady ");
            }
        } else if(type.equals(searchnoepc)) {
            String data = FileUtils.readFromFile(searchnoepc + "_" + serverId);

            /*
            List<BriefAsset> response = gson.fromJson(data , new TypeToken<List<BriefAsset>>(){}.getType());
            Hawk.put(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, response);

            if(data!= null && data.length() > 0) {
                searchnoepcReady = true;
                Log.i("searchnoepcReady", "searchnoepcReady " + response.size());
            }*/
            List<SearchNoEpcItem> res = gson.fromJson(data , new TypeToken<List<SearchNoEpcItem>>(){}.getType());//((List<SearchNoEpcItem>) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < res.size(); i++) {
                res.get(i).setCompanyid(companyId);
                res.get(i).setUserid(serverId);
                Realm.getDefaultInstance().insertOrUpdate(res.get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            if(data!= null && data.length() > 0) {
                searchnoepcReady = true;
                Log.i("searchnoepcReady", "searchnoepcReady " + res.size());
            }

            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

        } else if(type.equals(borrowList + "_0")) {


            ArrayList<BorrowListItem> response = gson.fromJson(FileUtils.readFromFile(MainActivity.mContext, borrowList  + "_" + serverId + "_" + 0), new TypeToken<List<BorrowListItem>>(){}.getType());

            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String bo = response.get(i).getBorrowno();
                    borrowNo.add(bo);
                }
            }
            Realm.getDefaultInstance().beginTransaction();

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setValidDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getValidDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Log.i("approvaldate", "approvaldate" + arrayList.get(i).getApprovalDate());
                    response.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.get(i).setType(0);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                response.get(i).setPk(companyId + serverId + "" + type + response.get(i).getBorrowno());
                response.get(i).setTimeString((long)i);

                Log.i("data", "data " + response.get(i).getPk());

                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }

            Realm.getDefaultInstance().commitTransaction();

            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, response);

            if(FileUtils.readFromFile(MainActivity.mContext, borrowList  + "_" + serverId + "_" + 0) != null && FileUtils.readFromFile(MainActivity.mContext, borrowList + "_" + serverId + "_" + 0).length() > 0) {
                borrowList0Ready = true;
                Log.i("borrowList0Ready", "borrowList0Ready ");
                //FileUtils.deleteFile(type);
            }

            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

        } else if(type.equals(borrowList  + "_1")) {

            String data = FileUtils.readFromFile(MainActivity.mContext, borrowList + "_" + serverId + "_" + 1);

            ArrayList<BorrowListItem> response = gson.fromJson(data, new TypeToken<List<BorrowListItem>>(){}.getType());

            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String bo = response.get(i).getBorrowno();
                    borrowNo.add(bo);
                }
            }

            Realm.getDefaultInstance().beginTransaction();

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setValidDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getValidDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Log.i("approvaldate", "approvaldate" + arrayList.get(i).getApprovalDate());
                    response.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.get(i).setType(1);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                response.get(i).setPk(companyId + serverId + "" + type + response.get(i).getBorrowno());
                response.get(i).setTimeString((long)i);

                Log.i("data", "data " + response.get(i).getPk());

                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            if(data != null && data.length() > 0) {
                borrowList1Ready = true;
                Log.i("borrowList1Ready", "borrowList1Ready ");
                //FileUtils.deleteFile(type);
            }

            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);
        } else if(type.equals(borrowList + "_2")) {
            String data = FileUtils.readFromFile(MainActivity.mContext, borrowList + "_" + serverId + "_" + 2);

            ArrayList<BorrowListItem> response = gson.fromJson(data, new TypeToken<List<BorrowListItem>>(){}.getType());

            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String bo = response.get(i).getBorrowno();
                    borrowNo.add(bo);
                }
            }

            Realm.getDefaultInstance().beginTransaction();

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setValidDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getValidDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Log.i("approvaldate", "approvaldate" + arrayList.get(i).getApprovalDate());
                    response.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.get(i).setType(2);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                response.get(i).setPk(companyId + serverId + "" + type + response.get(i).getBorrowno());
                response.get(i).setTimeString((long)i);

                Log.i("data", "data " + response.get(i).getPk());

                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            if(data != null && data.length() > 0) {
                borrowList2Ready = true;
                Log.i("borrowList2Ready", "borrowList2Ready ");
            }


            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);
        } else if (type.contains(borrowListAssets)) {
            /*BorrowListAssets response = gson.fromJson(raw, BorrowListAssets.class);
            if(response != null) {
                Log.i("response", "response" + response.getBorrowno());
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + response.getBorrowno(), response);

                //FileUtils.deleteFile(type);
            }*/

            BorrowDetailResponse res =  gson.fromJson(raw, BorrowDetailResponse.class);

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(BorrowAsset.class).equalTo("borrowno", res.getBorrowno()).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < res.getData().size(); i++) {
                res.getData().get(i).setBorrowno(res.getBorrowno());
                res.getData().get(i).setCompanyid(companyId);
                res.getData().get(i).setUserid(serverId);
                res.getData().get(i).setTimeString((long)i);

                Realm.getDefaultInstance().insert(res.getData().get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            borrowNo.remove(borrowNo.size() - 1);

            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

        } else if(type.equals(disposalList + "_0")) {
            //List<BriefBorrowedList> response = gson.fromJson(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 0), new TypeToken<List<BriefBorrowedList>>(){}.getType());
            List<DisposalListItem> response = gson.fromJson(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 0), new TypeToken<List<DisposalListItem>>(){}.getType());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(DisposalListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("type", type).findAll().deleteAllFromRealm();

            //ArrayList<DisposalListItem> arrayList = ((ArrayList<DisposalListItem>) event.getResponse());

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setValidDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getValidDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Log.i("approvaldate", "approvaldate" + response.get(i).getApprovalDate());
                    response.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.get(i).setType(0);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                response.get(i).setPk(companyId + serverId + "SP_DISPOSAL_" + type + response.get(i).getDisposalNo());

                response.get(i).setTimeString((long)i);
                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }


            Realm.getDefaultInstance().commitTransaction();

            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String diso = response.get(i).getDisposalNo();
                    disposalNo.add(diso);
                    //parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets + "_" +disposalNo), disposalListAssets + "_" +disposalNo);
                }
            }

           // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, response);

            if(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 0) != null && FileUtils.readFromFile(disposalList  + "_" + serverId + "_" + 0).length() > 0) {
                disposalList0Ready = true;
                Log.i("disposalList0Ready", "disposalList0Ready ");

                //FileUtils.deleteFile(type);
            }



            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

        } else if(type.equals(disposalList + "_1")) {
            //List<BriefBorrowedList> response = gson.fromJson(FileUtils.readFromFile(disposalList  + "_" + serverId+ "_" + 1), new TypeToken<List<BriefBorrowedList>>(){}.getType());

            List<DisposalListItem> response = gson.fromJson(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 1), new TypeToken<List<DisposalListItem>>(){}.getType());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(DisposalListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("type", type).findAll().deleteAllFromRealm();

            //ArrayList<DisposalListItem> arrayList = ((ArrayList<DisposalListItem>) event.getResponse());

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setValidDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getValidDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Log.i("approvaldate", "approvaldate" + response.get(i).getApprovalDate());
                    response.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.get(i).setType(1);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                response.get(i).setPk(companyId + serverId + "SP_DISPOSAL_" + type + response.get(i).getDisposalNo());

                response.get(i).setTimeString((long)i);
                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }


            Realm.getDefaultInstance().commitTransaction();
            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String diso = response.get(i).getDisposalNo();
                    disposalNo.add(diso);
                    //parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets + "_" +disposalNo), disposalListAssets + "_" +disposalNo);
                }
            }

            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, response);

            if(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 1) != null && FileUtils.readFromFile(disposalList  + "_" + serverId+ "_" + 1).length() > 0) {
                disposalList1Ready = true;
                Log.i("disposalList1Ready", "disposalList1Ready ");

                //FileUtils.deleteFile(type);
            }


            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);
        } else if(type.equals(disposalList + "_2")) {

            List<DisposalListItem> response = gson.fromJson(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 2), new TypeToken<List<DisposalListItem>>(){}.getType());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(DisposalListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("type", type).findAll().deleteAllFromRealm();

            //ArrayList<DisposalListItem> arrayList = ((ArrayList<DisposalListItem>) event.getResponse());

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setValidDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getValidDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Log.i("approvaldate", "approvaldate" + response.get(i).getApprovalDate());
                    response.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.get(i).setType(2);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                response.get(i).setPk(companyId + serverId + "SP_DISPOSAL_" + type + response.get(i).getDisposalNo());

                response.get(i).setTimeString((long)i);
                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }


            Realm.getDefaultInstance().commitTransaction();
            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String diso = response.get(i).getDisposalNo();
                    disposalNo.add(diso);
                    //parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets + "_" +disposalNo), disposalListAssets + "_" +disposalNo);
                }
            }
            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, response);

            if(FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 2) != null && FileUtils.readFromFile(disposalList + "_" + serverId + "_" + 2).length() > 0) {
                disposalList2Ready = true;
                Log.i("disposalList2Ready", "disposalList2Ready ");
                //FileUtils.deleteFile(type);
            }


            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

        } else if(type.contains(disposalListAssets)) {
            //BorrowListAssets response = gson.fromJson(raw, BorrowListAssets.class);
            //if(response != null)
            //    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + response.getDisposalNo(), response);
            DisposalDetailResponse res = gson.fromJson(raw, DisposalDetailResponse.class);
            Realm.getDefaultInstance().beginTransaction();

            for (int i = 0; i < res.getData().size(); i++) {
                res.getData().get(i).setDisposalNo(res.getDisposalNo());
                res.getData().get(i).setCompanyid(companyId);
                res.getData().get(i).setUserid(serverId);
                //res.getData().get(i).setPk(companyId + serverId + "SP_DISPOSAL_" + res.getData().get(i).getDisposalNo());
                res.getData().get(i).setPk(companyId + serverId + "SP_DISPOSAL_" + res.getDisposalNo() + res.getData().get(i).getAssetNo());
                res.getData().get(i).setTimeString((long)i);

                Realm.getDefaultInstance().insertOrUpdate(res.getData().get(i));
            }

            Realm.getDefaultInstance().commitTransaction();

            disposalNo.remove(disposalNo.size() - 1);

            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);
            //
            //parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, disposalListAssets + "_" +disposalNo), disposalListAssets + "_" +disposalNo);
        } else if(type.equals(returnList)) {
            //List<BriefAsset> response = gson.fromJson(FileUtils.readFromFile(returnList + "_" + serverId), new TypeToken<List<BriefAsset>>(){}.getType());

            ArrayList<ReturnAsset> response = gson.fromJson(FileUtils.readFromFile(returnList + "_" + serverId), new TypeToken<List<ReturnAsset>>(){}.getType());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(ReturnAsset.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < response.size(); i++) {
                String pk = companyId + serverId + "RETURN" + response.get(i).getAssetno();
                response.get(i).setPk(pk);
                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);

                response.get(i).setTimeStamp(new Date().getTime() + "");
                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            try {
                Log.i("response", "response " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, response);

            if(FileUtils.readFromFile(returnList + "_" + serverId) != null && FileUtils.readFromFile(returnList + "_" + serverId).length() > 0) {
                returnListReady = true;
                Log.i("returnListReady", "returnListReady ");
                //FileUtils.deleteFile(type);
            }

            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

        } else if(type.equals(stockTakeList)) {
            //List<StockTakeList> response =  gson.fromJson(FileUtils.readFromFile(stockTakeList + "_" + serverId), new TypeToken<List<StockTakeList>>(){}.getType());
            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST, response);

            List<StocktakeList> response = gson.fromJson(FileUtils.readFromFile(stockTakeList + "_" + serverId), new TypeToken<List<StocktakeList>>(){}.getType());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(StocktakeList.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < response.size(); i++) {

                try {
                    response.get(i).setStartDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getStartDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    response.get(i).setEndDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(response.get(i).getEndDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                response.get(i).setCompanyid(companyId);
                response.get(i).setUserid(serverId);
                Realm.getDefaultInstance().insertOrUpdate(response.get(i));
            }

            Realm.getDefaultInstance().commitTransaction();
            Log.i("stockTakeList", "stockTakeList " + response);

            if(response != null) {
                for (int i = 0; i < response.size(); i++) {
                    String stocktakeno = response.get(i).getStocktakeno();
                    stockTakeNo.add(stocktakeno);
                    //parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, stockTakeListAsset + "_" + stocktakeno), stockTakeListAsset + stocktakeno);
                }
            }

            if(FileUtils.readFromFile(stockTakeList + "_" + serverId) != null && FileUtils.readFromFile(stockTakeList + "_" + serverId).length() > 0) {
                stockTakeListReady = true;
                Log.i("stockTakeListReady", "stockTakeListReady ");
                //FileUtils.deleteFile(type);
            }


            ReadFileCallbackEvent readFileCallbackEvent = new ReadFileCallbackEvent();
            readFileCallbackEvent.setFileName(type);
            EventBus.getDefault().post(readFileCallbackEvent);

            /*
            if(stockTakeNo.size() > 0) {
                BaseUtils.parseStockTakeJson("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/Download/" + stockTakeListAsset + "_" + serverId + "_" + stockTakeNo.get(stockTakeNo.size() - 1) + ".json");
            } else {
                changeStaticFragment(new SearchListFragment());
            }*/
        } else if(type.contains(stockTakeListAsset)) {
            StockTakeListData response = gson.fromJson(raw, StockTakeListData.class);

            BaseUtils.parseLargeJson(MainActivity.mContext.getFilesDir().toString() + "/" + ("master.json"));

            if(response != null) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + response.getStocktakeno(), response);
                //FileUtils.deleteFile(type);//stockTakeListAsset + "_" + response.getStocktakeno());


            }
        } else if(type.equals(listingLevel)) {
            ListingResponse listingResponse = gson.fromJson(raw, ListingResponse.class);
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL, listingResponse);

            Realm.getDefaultInstance().beginTransaction();
            listingResponse.setPk(companyId + serverId + "SP_LISTING_LEVEL");
            Realm.getDefaultInstance().insertOrUpdate(listingResponse);
            Realm.getDefaultInstance().commitTransaction();

            parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, listing + "__1"), listing + "__1");


            if(raw != null && raw.length() > 0) {
                listingLevelReady = true;
                //FileUtils.deleteFile(type);
            }
        } else if(type.contains(listing)) {
            int LEVEL = 0;
            String fatherno = "";

            if(type.equals(listing + "__1")) {
                LEVEL = 1;
            } else {
                LEVEL = Integer.parseInt(type.split("_")[3]);
                fatherno = (type.split("_")[2]);

                Log.i("level", "levellevellevel " + LEVEL + " " + type);
            }

            ArrayList<LevelData> levelData = gson.fromJson(raw, new TypeToken<List<LevelData>>(){}.getType());
            root = type.split("_")[1];
            Log.i("root", "root " + InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + root + "_1_" + LEVEL);

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + fatherno + "_1_" + LEVEL, levelData);

            PendingToAdd pendingToAdd = new PendingToAdd();
            pendingToAdd.levelData = levelData;//
            pendingToAdd.level = LEVEL;//
            pendingToAdd.fatherNo = fatherno;
            pendingToAdd.type = 1;//

            pendingToAdds.addAll(levelData);


            Realm.getDefaultInstance().beginTransaction();
            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_1_" + event.level, event.getResponse());

            for (int i = 0; i < pendingToAdd.levelData.size(); i++) {
                //Log.i("inert", "insert " + levelData.get(i).getName() + " " + event.level + " " + levelData.get(i).getRono() + " " + event.getFatherno() + " " + event.type);
                levelData.get(i).setNewData(true);
                levelData.get(i).setFatherNo(fatherno);
                levelData.get(i).setType(1);
                levelData.get(i).setLevel(LEVEL);
                levelData.get(i).setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_1_" + pendingToAdd.level);
                levelData.get(i).setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_1_" + pendingToAdd.level + i);
                levelData.get(i).setOrdering(i);

                levelData.get(i).setCompanyid(companyId);
                levelData.get(i).setUserid(serverId);

                Realm.getDefaultInstance().insertOrUpdate(pendingToAdd.levelData.get(i));
            }

        Realm.getDefaultInstance().commitTransaction();
            //LEVEL++;

            Log.i("listing", "listing " + type);

            if(levelData != null) {
                for (int i = 0; i < levelData.size(); i++) {
                    if(type.equals(listing + "__1")) {
                        parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, listing + "_" + levelData.get(i).getRono() + "_1_" + (LEVEL+ 1)), listing + "_" + levelData.get(i).getRono() + "_1_" + (LEVEL+ 1));
                    } else {
                        parseDataToDatabase(FileUtils.readFromFile(MainActivity.mContext, listing + "_" + levelData.get(i).getRono() + "_1_" + (LEVEL+ 1)), listing + "_" + levelData.get(i).getRono() + "_1_" + (LEVEL + 1));
                    }

                    Log.i("data", "data " + listing + "_" + levelData.get(i).getRono() + "_1_" + (LEVEL+ 1));
                }
            }
            //FileUtils.deleteFile(type);

            if(type.equals(listing + "__1")) {
                ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*
                        if (!BACK_PRESS) {
                            Log.i("toLoginFragment", "tologinFragment");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                   // changeStaticFragment(new LoginFragment());
                                }
                            }, 1000);

                        } else {
                            Log.i("onBackPressed", "onBackPressed");

                            ((MainActivity) MainActivity.mContext).onBackPressed();
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                File folder2 = new File(Environment.getExternalStorageDirectory().toString()+"/" + userDefinedCompanyId + "/Download");
                                folder2.mkdirs();

                            }
                        }, 1000);

                         */
                    }
                });
            }
        }

    }


    public static void changeStaticFragment(Fragment fragment){

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = ((MainActivity)MainActivity.mContext).getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            // ft.addToBackStack(backStateName);
            ft.commit();
        }
    }


}
