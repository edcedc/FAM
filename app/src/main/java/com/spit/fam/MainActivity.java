package com.spit.fam;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.DrawerListContent.DrawerPositions;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.CreateBy;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.OfflineMode.BorrowAssets;
import com.spit.fam.Entity.OfflineMode.ChangeEpc;
import com.spit.fam.Entity.OfflineMode.DisposalAssets;
import com.spit.fam.Entity.OfflineMode.ReturnAssets;
import com.spit.fam.Entity.PermissionCallbackEvent;
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.TagType;
import com.spit.fam.Event.APICallbackZeroEvent;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.FileLoadingCompletedEvent;
import com.spit.fam.Event.HideLoadingEvent;
import com.spit.fam.Event.NFCCardEvent;
import com.spit.fam.Event.ShowLoadingEvent;
import com.spit.fam.RenewSystemFragment.RenewBorrowListFragment;
import com.spit.fam.RenewSystemFragment.RenewDisposalListFragment;
import com.spit.fam.RenewSystemFragment.RenewRegistrationFragment;
import com.spit.fam.RenewSystemFragment.RenewReturnFragment;
import com.spit.fam.RenewSystemFragment.StocktakeFragment;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BorrowListFragment;
import com.spit.fam.SystemFragment.DepositRecieveFragment;
import com.spit.fam.SystemFragment.DisposalListFragment;
import com.spit.fam.SystemFragment.FileChosenEvent;
import com.spit.fam.SystemFragment.FileUtils;
import com.spit.fam.SystemFragment.LandRegisteryDownloadFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.PowerSettingFragment;
import com.spit.fam.RenewSystemFragment.RevampDownloadFragment;
import com.spit.fam.SystemFragment.SearchListFragment;
import com.spit.fam.SystemFragment.StockTakeListItemFragment;
import com.spit.fam.SystemFragment.UploadFragment;
import com.spit.fam.fragments.*;
import com.spit.fam.nfc.WritableTag;
import com.spit.cs108library4a.Cs108Library4A;
import com.spit.cs108library4a.ReaderDevice;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mindorks.paracamera.Camera;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends NewMainActivity {
    public static boolean OFFLINE_MODE = true;
    public static String CARD_NUMBER = "9A05254A";

    final boolean DEBUG = false; final String TAG = "Hello";
    public static boolean activityActive = false;

    //Tag to identify the currently displayed fragment
    Fragment fragment = null;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";

    public static TextView mLogView;
    public DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;

    public static Context mContext;
    public static Cs108Library4A mCs108Library4a;
    public static SharedObjects sharedObjects;
    public static SensorConnector mSensorConnector;
    public static ReaderDevice tagSelected;
    Handler mHandler = new Handler();

    public static String mDid; public static int selectHold; public static int selectFor;

    public static class Config {
        public String configPassword, configPower, config0, config1, config2;
    };
    public static Config config  = new Config();

    public static boolean SKIP_DOWNLOAD_ONCE;

    public static int callback = 0;

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
        }

        return assetsDetail;
    }

    public static List<AssetsDetail> getAssetsDetailList(String assetsNo) {
        List<AssetsDetail> assetsDetails = new ArrayList<>();

        //assetsDetails.add(getAssetsDetail(assetsNo));

        //DataBaseHandler dataBaseHandler = new DataBaseHandler(MainActivity.mContext);
        //assetsDetails = dataBaseHandler.getAssetByAssetNo(assetsNo, "0");
        assetsDetails = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("assetNo", assetsNo).findAll();

        return assetsDetails;
    }


    public static AssetsDetail getAssetsDetail(String assetsNo) {
        String jsonString = Hawk.get(Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") + "CON", "");
        try {
            JSONArray arr = new JSONArray(jsonString);
            List<String> list = new ArrayList<String>();
            Log.i("arr", "arr " + arr.length());
            for (int i = 0; i < arr.length(); i++) {
                String refAssetNo = arr.getJSONObject(i).getString("assetNo");

                if(assetsNo.equals(refAssetNo)) {

                    AssetsDetail assetsDetail = new AssetsDetail();
                    assetsDetail.setAssetNo(arr.getJSONObject(i).getString("assetNo"));
                    assetsDetail.setName(arr.getJSONObject(i).getString("name"));
                    assetsDetail.setStatusid(arr.getJSONObject(i).getString("statusid"));
                    assetsDetail.setStatusname(arr.getJSONObject(i).getString("statusname"));
                    assetsDetail.setBrand(arr.getJSONObject(i).getString("brand"));
                    assetsDetail.setModel(arr.getJSONObject(i).getString("model"));
                    assetsDetail.setSerialno(arr.getJSONObject(i).getString("serialno"));
                    assetsDetail.setUnit(arr.getJSONObject(i).getString("unit"));
                    assetsDetail.setCategory(arr.getJSONObject(i).getString("category"));
                    assetsDetail.setLocation(arr.getJSONObject(i).getString("location"));
                    assetsDetail.setLastStockDate(arr.getJSONObject(i).getString("lastStockDate"));
                    assetsDetail.setCreatedById(arr.getJSONObject(i).getString("createdById"));
                    assetsDetail.setCreatedByName(arr.getJSONObject(i).getString("createdByName"));
                    assetsDetail.setCreatedDate(arr.getJSONObject(i).getString("createdDate"));
                    assetsDetail.setPurchaseDate(arr.getJSONObject(i).getString("purchaseDate"));
                    assetsDetail.setInvoiceDate(arr.getJSONObject(i).getString("invoiceDate"));
                    assetsDetail.setInvoiceNo(arr.getJSONObject(i).getString("invoiceNo"));
                    assetsDetail.setFundingSourceid(arr.getJSONObject(i).getString("fundingSourceid"));
                    assetsDetail.setFundingSourcename(arr.getJSONObject(i).getString("fundingSourcename"));
                    assetsDetail.setSupplier(arr.getJSONObject(i).getString("supplier"));
                    assetsDetail.setMaintenanceDate(arr.getJSONObject(i).getString("maintenanceDate"));
                    assetsDetail.setCost(arr.getJSONObject(i).getString("cost"));
                    assetsDetail.setPraticalValue(arr.getJSONObject(i).getString("praticalValue"));
                    assetsDetail.setEstimatedLifetime(arr.getJSONObject(i).getString("estimatedLifetime"));
                    assetsDetail.setTypeOfTag(arr.getJSONObject(i).getString("typeOfTag"));
                    assetsDetail.setBarcode(arr.getJSONObject(i).getString("barcode"));
                    assetsDetail.setEpc(arr.getJSONObject(i).getString("epc"));
                    assetsDetail.setCertType(arr.getJSONObject(i).getString("certType"));
                    assetsDetail.setCertUrl(arr.getJSONObject(i).getString("certUrl"));
                    assetsDetail.setCerstatus(arr.getJSONObject(i).getString("cerstatus"));
                    assetsDetail.setIsverified(arr.getJSONObject(i).getBoolean("isverified"));
                    assetsDetail.setStartdate(arr.getJSONObject(i).getString("startdate"));
                    assetsDetail.setEnddate(arr.getJSONObject(i).getString("enddate"));
                    assetsDetail.setRono(arr.getJSONObject(i).getString("rono"));
                    assetsDetail.setPossessor(arr.getJSONObject(i).getString("possessor"));
                    assetsDetail.setUsergroup(arr.getJSONObject(i).getString("usergroup"));

                    return assetsDetail;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackStartEvent startEvent) {
        Log.i("CallbackStartEvent", "callback [" + callback );
        Log.i("HideLoadingEvent", "HideLoadingEvent show");

        //findViewById(R.id.loading).setVisibility(View.VISIBLE);
        callback++;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnBackPressEvent startEvent) {
        onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FileLoadingCompletedEvent startEvent) {
        Log.i("FileLoadingCompletedEvent", "FileLoadingCompletedEvent");
        replaceFragment(new LandRegisteryDownloadFragment());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent dialogEvent) {
        Log.i("CallbackResponseEvent", "callback [" + callback);

        if(callback > 0) {
            callback--;
        }

        if(callback == 0) {
            EventBus.getDefault().post(new APICallbackZeroEvent());

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 10);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ShowLoadingEvent dialogEvent) {
        //if(dialogEvent.hide)
        Log.i("HideLoadingEvent", "HideLoadingEvent show");

        findViewById(R.id.loading).setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HideLoadingEvent dialogEvent) {
        //if(dialogEvent.hide)
        Log.i("HideLoadingEvent", "HideLoadingEvent");

        findViewById(R.id.loading).setVisibility(View.GONE);
    }

    public boolean isDialogShown() {
        return dialogShown;
    }

    public void setDialogShown(boolean dialogShown) {
        this.dialogShown = dialogShown;
    }

    private boolean dialogShown = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DialogEvent dialogEvent)
    {
        Log.i("DialogEvent", "DialogEvent " + dialogShown);

        //if(dialogShown)
        //    return;

        dialogShown = true;
        new AlertDialog.Builder(this)
                .setTitle(dialogEvent.getTitle())
                .setMessage(dialogEvent.getMessage())

                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogShown = false;
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogShown = false;
            }
        })
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackFailEvent failEvent) {
        Log.i("CallbackFailEvent", "callback " + callback + " " + dialogShown);

        callback--;

        if(callback == 0) {
            EventBus.getDefault().post(new APICallbackZeroEvent());
        }

        //dialogShown = true;

        //if(dialogShown)
        //    return;

        if(isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(failEvent.getMessage())

                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialogShown = false;

                        }
                    })
                    .show();
        } else {

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.no_internet))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialogShown = false;

                        }
                    })
                    .show();
        }
    }


    private NfcAdapter adapter = null;
    WritableTag tag =  null;
    String tagId =  null;

    private void initNfcAdapter() {
        NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        adapter = nfcManager.getDefaultAdapter();
    }

    private void enableNfcForegroundDispatch() {
        try {
            //Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            //adapter.enableForegroundDispatch(this, nfcPendingIntent, null, null);
        } catch (Exception ex) {
            Log.e("MainActivity", "Error enabling NFC foreground dispatch", ex);
        }
    }

    private void disableNfcForegroundDispatch() {
        try {
            //adapter.disableForegroundDispatch(this);
        } catch (Exception ex) {
            Log.e("MainActivity", "Error disabling NFC foreground dispatch", ex);
        }
    }

    public static String nfcCardNumber = null;
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        try {
            tag = new WritableTag(tagFromIntent);
        } catch (FormatException e) {
            Log.e("MainActivity", "Unsupported tag tapped", e);
            return;
        }

        Log.i("MainActivity", "Unsupported tag tapped");

        if(tag != null)
            tagId = tag.getTagId();
        else
            tagId = null;

        if(tagId != null && tagId.length() > 0) {
            Log.i("nfcCardEvent", "nfcCardEvent activity main " + tagId);

            NFCCardEvent nfcCardEvent = new NFCCardEvent();
            nfcCardEvent.setCardNo(tagId);
            nfcCardNumber = tagId;
            EventBus.getDefault().post(nfcCardEvent);
        }
        //showToast("Tag tapped: " + tagId);
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void reload() {
        doRestart(this);
    }

    public static void doRestart(Context c) {
        try {
            // check if the context is given
            if (c != null) {
                // fetch the package manager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                // check if we got the PackageManager
                if (pm != null) {
                    // create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(c.getPackageName());
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        c.getApplicationContext().startActivity(mStartActivity);
                        // kill the application
                        System.exit(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("restart", "Could not Restart");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //AppCenter.start(getApplication(), "dfc84ad1-1ed8-4f52-abbe-38ee123c5942", Analytics.class, Crashes.class);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);

        FirebaseApp.initializeApp(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        if (savedInstanceState == null)
            Log.i(TAG, "MainActivity.onCreate: NULL savedInstanceState");
        else
            Log.i(TAG, "MainActivity.onCreate: VALID savedInstanceState");

        Hawk.init(this).build();
        EventBus.getDefault().register(this);

        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.low_battery);

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FileUtils.writeFromFile(new Gson().toJson(appsID), "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/", LandRegisteryDownloadFragment.appsID);

                FileUtils.deleteFileByRawPath( "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/Upload/" + LandRegisteryDownloadFragment.appsID + ".json");

                Hawk.put(InternalStorage.Login.USER, null);
                Hawk.put(InternalStorage.Login.PREVIOUS_ID, Hawk.get(InternalStorage.Login.USER_ID, ""));
                Hawk.put(InternalStorage.Login.USER_ID, null);
                Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID, null);
                updateDrawerStatus();

               // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "");

                /*
                File dir = new File("/sdcard/LandRegistry/Download");
                if (dir.isDirectory())
                {
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++)
                    {
                        new File(dir, children[i]).delete();
                    }
                }
*/
                changeFragment(new LoginFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                reload();
            }
        });


        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("stockTakeList", "stockTakeList null 2");

                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new SearchListFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.registration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("stockTakeList", "stockTakeList null 3");

                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new RenewRegistrationFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.borrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("stockTakeList", "stockTakeList null 4");

                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new RenewBorrowListFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.deposit_receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new DepositRecieveFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.disposal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("stockTakeList", "stockTakeList null 5");

                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new RenewDisposalListFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LandRegisteryDownloadFragment.writeData();

                //replaceFragment(new LandRegisteryDownloadFragment());

                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.bluetooth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    replaceFragment(new ConnectionFragment(true));
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                    replaceFragment(new ConnectionFragment(true));
                } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                    replaceFragment(new ConnectionFragment(true));

                    //Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                } else {
                    replaceFragment(new ConnectionFragment(true));

                    //Toast.makeText(MainActivity.mContext, R.string.bluetooth_already_connected, Toast.LENGTH_SHORT).show();
                }

                //changeFragment(new DisposalListFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.power_setting).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                replaceFragment(new PowerSettingFragment());
                                                                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                                                            }
                                                        });
        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changeFragment(new DownloadFragment());
                RevampDownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = false;
                RevampDownloadFragment.DOWNLOAD_ON_BACK_PRESS = false;
                if( ((MainActivity)MainActivity.mContext).isNetworkAvailable() ){
                    RevampDownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = true;

                    replaceFragment(new RevampDownloadFragment());
                } else {
                    LandRegisteryDownloadFragment.writeData();
                }

                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new UploadFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.return_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("stockTakeList", "stockTakeList null 6");

                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new RenewReturnFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.stock_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("stockTakeList", "stockTakeList null 7");

                StockTakeListItemFragment.stockTakeList = null;

                changeFragment(new StocktakeFragment());
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        findViewById(R.id.right_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mLogView = (TextView) findViewById(R.id.log_view);

        // set up the drawer's list view with items and click listener
        //mDrawerList.setAdapter(new DrawerListAdapter(this, R.layout.drawer_list_item, DrawerListContent.ITEMS));
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mContext = this;
        sharedObjects = new SharedObjects(mContext);
        mCs108Library4a = new Cs108Library4A(mContext, mLogView);
        mSensorConnector = new SensorConnector(mContext);

        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> lst = imeManager.getInputMethodList();
        for (InputMethodInfo info : lst) {
//            MainActivity.mCs108Library4a.appendToLog(info.getId() + " " + info.loadLabel(getPackageManager()).toString());
        }
//        Intent intent = new Intent(MainActivity.this, CustomIME.class);
 //       startService(intent);
//        savedInstanceState = null;
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            //selectItem(DrawerPositions.MAIN);
            if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER) == null) {
                Log.i("case1", "case1");
                if(isNetworkAvailable()) {
                    changeFragment(new LoginFragment());
                } else {
                    changeFragment(new LandRegisteryDownloadFragment());
                }
            } else {
                Log.i("case2", "case2");
                changeFragment(new LoginFragment());
            }
        }
        Log.i(TAG, "MainActivity.onCreate.onCreate: END");

        //Intent intent = new Intent(this,ScanConfigActivity.class);
        //startActivity(intent);
    }


    public void changeLocale(Context context, String localeString) {
        Log.i("MainActivity localeString", "MainActivity localeString" + localeString);

        String languageToLoad  = localeString; // your language
        Locale locale = new Locale(languageToLoad);

        if(languageToLoad != null && languageToLoad.equals("zt")) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MainActivity.mCs108Library4a.connect(null);
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onStart()");
    }

    public static Camera camera;
    private ImageView imageView;

    public void setLowBatteryIfNeeded() {
        if(MainActivity.mCs108Library4a.isBatteryLow() != null) {
            findViewById(R.id.low_battery).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.low_battery).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(Hawk.get(InternalStorage.PUSH_TOKEN, "").isEmpty()) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Log and toast
                            String msg = getString(R.string.msg_token_fmt, token);
                            Log.d(TAG +" Case 0", msg);
                            Hawk.put(InternalStorage.PUSH_TOKEN, token);

                            //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            //((TextView)findViewById(R.id.informationTextView)).setText(msg);
                        }
                    });
        } else {
            Log.d(TAG +" Case 1", Hawk.get(InternalStorage.PUSH_TOKEN, ""));
        }
/*
        PRDownloader.initialize(getApplicationContext());
        // Enabling database for resume support even after the application is killed:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        int downloadId = PRDownloader.download("http://icloud.securepro.com.hk/AMSWebService_EvidenceRoom/MobileWebService.asmx/assetsDetail?companyid=gs1&userid=35BB5E39D4DD4C0FB4C4307413C07B84&assetno=&lastcalldate=", "/sdcard/", "abc.json")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        Log.i("onStartOrResume", "onStartOrResume");

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        Log.i("onPause", "onPause");

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        Log.i("onCancel", "onCancel");

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        Log.i("progress", "progress " + progress.currentBytes + " " + progress.totalBytes);
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Log.i("onDownloadComplete", "onDownloadComplete");

                    }

                    @Override
                    public void onError(Error error) {
                        Log.i("onError", "onError " + error.getServerErrorMessage());

                    }

                });
*/
        updateDrawerStatus();
        initNfcAdapter();
        enableNfcForegroundDispatch();

        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        ((TextView)findViewById(R.id.app_version)).setText("V :" +sdf.format(buildDate).toString()+"");

        Log.i("USERNAME", "USERNAME " + Hawk.get(InternalStorage.Login.USER_ID, ""));
        //((TextView)findViewById(R.id.username)).setText(Hawk.get(InternalStorage.Login.USER_ID, ""));
        //((TextView)findViewById(R.id.companyid)).setText(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));

        Log.i("Handler case 0", "Handler case 0" + Hawk.get(InternalStorage.Login.USER));

       // if(Hawk.get(InternalStorage.Login.USER) != null) {
       //     Log.i("Handler case 1", "Handler case 1" + Hawk.get(InternalStorage.Login.USER));

        if(!SKIP_DOWNLOAD_ONCE && Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").length()> 0  && Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "").length() > 0 ) {
            if (isNetworkAvailable()) {
                Log.i("Handler case 2", "Handler case 2");

                //DownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = true;
                //DownloadFragment.DOWNLOAD_ON_BACK_PRESS = true;

                //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DOWNLOAD_AFTER_LOGIN, true);
                try {
                    //replaceFragment(new DownloadFragment());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            SKIP_DOWNLOAD_ONCE = false;
        }
      //  }
        try {
            camera = new Camera.Builder()
                    .resetToCorrectOrientation(true)// it will rotate the camera bitmap to the correct orientation from meta data
                    .setTakePhotoRequestCode(1)
                    .setDirectory("pics")
                    .setName("ali_" + System.currentTimeMillis())
                    .setImageFormat(Camera.IMAGE_JPEG)
                    .setCompression(75)
                    .setImageHeight(1000)// it will try to achieve this height as close as possible maintaining the aspect ratio;
                    .build(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        changeLocale(this, Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"));

        ((TextView) findViewById(R.id.search)).setText(R.string.search);
        try {
            ((TextView) findViewById(R.id.registration)).setText(R.string.registration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.borrow)).setText(R.string.borrow);
        ((TextView) findViewById(R.id.deposit_receive)).setText(R.string.receive_deposit);
        ((TextView) findViewById(R.id.return_menu)).setText(R.string.return_string);
        ((TextView) findViewById(R.id.stock_take)).setText(R.string.stock_take);
        ((TextView) findViewById(R.id.disposal)).setText(R.string.disposal);
        ((TextView) findViewById(R.id.download)).setText(R.string.download);
        ((TextView) findViewById(R.id.upload)).setText(R.string.upload);
        ((TextView) findViewById(R.id.bluetooth)).setText(R.string.bluetooth);
        ((TextView) findViewById(R.id.power_setting)).setText(R.string.home_settings);

        ((TextView) findViewById(R.id.sync)).setText(R.string.sync);

        ((TextView) findViewById(R.id.logout)).setText(R.string.logout);

        activityActive = true; wedged = false;
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onResume()");
    }


    public void resetTitle() {
        changeLocale(this, Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"));

        ((TextView) findViewById(R.id.search)).setText(R.string.search);
        try {
            ((TextView) findViewById(R.id.registration)).setText(R.string.registration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView) findViewById(R.id.borrow)).setText(R.string.borrow);
        ((TextView) findViewById(R.id.return_menu)).setText(R.string.return_string);
        ((TextView) findViewById(R.id.stock_take)).setText(R.string.stock_take);
        ((TextView) findViewById(R.id.disposal)).setText(R.string.disposal);
        ((TextView) findViewById(R.id.download)).setText(R.string.download);
        ((TextView) findViewById(R.id.upload)).setText(R.string.upload);
        ((TextView) findViewById(R.id.bluetooth)).setText(R.string.bluetooth);
        ((TextView) findViewById(R.id.sync)).setText(R.string.sync);

        ((TextView) findViewById(R.id.logout)).setText(R.string.logout);
    }

    @Override
    public void onPause() {
        isNetworkOK = false;

        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onPause()");
        activityActive = false;
        super.onPause();
        disableNfcForegroundDispatch();
    }

    @Override
    protected void onStop() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) mCs108Library4a.appendToLog("MainActivity.onDestroy()");
        if (true) { mCs108Library4a.disconnect(true); }
        super.onDestroy();
    }

    public void updateDrawerStatus() {
        ((TextView)findViewById(R.id.username)).setText(Hawk.get(InternalStorage.Login.USER_ID, ""));
        ((TextView)findViewById(R.id.companyid)).setText(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));

        /*
        ArrayList<UploadStockTakeData> pendingStockTakeData = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
        ArrayList<BorrowListRequest> borrowListRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST,new ArrayList<>());
        ArrayList<PendingReturnAsset> pendingReturnRequest = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, new ArrayList<PendingReturnAsset>());

        ArrayList<String> uniqueStockTakeList = new ArrayList<>();
        ArrayList<String> uniqueStockTakeId = new ArrayList<>();

        for(int i = 0; i < pendingStockTakeData.size(); i++) {
            if(!uniqueStockTakeId.contains(pendingStockTakeData.get(i).getStrJsonObject().getOrderNo())) {
                uniqueStockTakeId.add(pendingStockTakeData.get(i).getStrJsonObject().getOrderNo());
                uniqueStockTakeList.add(pendingStockTakeData.get(i).getStockTakeName());
            }
        }

        ArrayList<PendingReturnAsset> pendingReturnAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, new ArrayList<PendingReturnAsset>());
        ArrayList<ModifyAssetRequest> pendingModifyAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
        ArrayList pendingBindAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<>());

        ArrayList<UpdateWaitingListRequest> pendingBorrowAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BORROW_REQUEST, new ArrayList<UpdateWaitingListRequest>());
        ArrayList<UploadStockTakeRequest> pendingStockTakeAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REQUEST, new ArrayList<UploadStockTakeRequest>());
        ArrayList<BorrowListRequest> pendingDisposalAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_PENDING_DISPOSAL_REQUEST,new ArrayList<>());

        ArrayList<PhotoUploadRequest> pendingPhotoUploadAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());
        int distinctCount = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).distinct("stocktakeno").findAll().size();
*/

        String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

        RealmResults<ChangeEpc> changeEpcs = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("userid", userid).findAll();

        RealmResults<BorrowAssets> borrowAssets = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("userid", userid).findAll();

        RealmResults<DisposalAssets> disposalAssets = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("userid", userid).findAll();

        RealmResults<RealmStockTakeListAsset> realmStockTakeListAsset = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).equalTo("companyId", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("userId", userid).distinct("stocktakeno").findAll();

        RealmResults<ReturnAssets> returnAssets = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("userid", userid).findAll();

        Log.i("changeEpcs", "changeEpcs" + changeEpcs.size());

        int totalCount = changeEpcs.size() + borrowAssets.size() + disposalAssets.size() + realmStockTakeListAsset.size() + returnAssets.size();// distinctCount+ pendingReturnRequest.size() + borrowListRequests.size() + uniqueStockTakeId.size() + pendingModifyAssets.size() +  pendingBindAssets.size() + pendingBorrowAssets.size() + pendingStockTakeAssets.size() + pendingDisposalAssets.size();// + pendingStockTakeData.size();// + pendingPhotoUploadAssets.size();

        ((TextView) findViewById(R.id.required_to_upload)).setText(totalCount + "");
        ((TextView) findViewById(R.id.required_to_upload_menu)).setText(totalCount + "");

        if(totalCount > 0) {
            ((TextView) findViewById(R.id.required_to_upload)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.required_to_upload_menu)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) findViewById(R.id.required_to_upload)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.required_to_upload_menu)).setVisibility(View.GONE);
        }
    }

    public void setBadgeVisibility(int visibility){
        ((TextView) findViewById(R.id.required_to_upload)).setVisibility(visibility);
    }

    boolean configureDisplaying = false;
    Toast configureToast;
    private final Runnable configureRunnable = new Runnable() {
        @Override
        public void run() {
            MainActivity.mCs108Library4a.appendToLog("AAA: mrfidToWriteSize = " + mCs108Library4a.mrfidToWriteSize());
            if (mCs108Library4a.mrfidToWriteSize() != 0) {
                MainActivity.mCs108Library4a.mrfidToWritePrint();
                configureDisplaying = true;
                mHandler.postDelayed(configureRunnable, 500);
            } else {
                configureDisplaying = false;
                progressDialog.dismiss();
            }
        }
    };

    CustomProgressDialog progressDialog;
    private void selectItem(DrawerPositions position) {
        Log.i(TAG, "MainActivity.selectItem: position = " + position);
        if (false && position != DrawerPositions.MAIN && position != DrawerPositions.ABOUT &&  position != DrawerPositions.CONNECT && mCs108Library4a != null) {
            if (MainActivity.mCs108Library4a.isRfidFailure() == false && mCs108Library4a.mrfidToWriteSize() != 0) {
                if (configureDisplaying == false) {
                    progressDialog = new CustomProgressDialog(this, getString(R.string.initial_reader));
                    progressDialog.show();
                    mHandler.post(configureRunnable);
                }
                return;
            }
        }
        if (true && position != DrawerPositions.MAIN && position != DrawerPositions.ABOUT && position != DrawerPositions.CONNECT && mCs108Library4a.isBleConnected() == false) {
            Toast.makeText(MainActivity.mContext, "Bluetooth Disconnected.  Please Connect.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (position) {
            case MAIN:
                fragment = new HomeFragment();
                break;
            case SPECIAL:
                fragment = new HomeSpecialFragment();
                break;
            case ABOUT:
                fragment = new AboutFragment();
                break;
            case CONNECT:
                fragment = new ConnectionFragment();
                break;
            case INVENTORY:
                fragment = new InventoryFragment();
                break;
            case SEARCH:
                fragment = new InventoryRfidSearchFragment();
                break;
            case MULTIBANK:
                mDid = null;
                fragment = InventoryRfidiMultiFragment.newInstance(true, null);
                break;
            case SETTING:
                fragment = new SettingFragment();
                break;
            case FILTER:
                fragment = new SettingFilterFragment();
                break;
            case READWRITE:
                fragment = new AccessReadWriteFragment();
                break;
            case SECURITY:
                fragment = new AccessSecurityFragment();
                break;
            case REGISTER:
                fragment = new AccessRegisterFragment();
                break;
            case COLDCHAIN:
                fragment = new ColdChainFragment();
                break;
            case BAPCARD:
                fragment = InventoryRfidiMultiFragment.newInstance(true, "E200B0");
                break;
            case CTESIUS:
                fragment = InventoryRfidiMultiFragment.newInstance(true, "E203510");
                break;
            case AURASENSE:
                fragment = new AuraSenseFragment();
                break;
            case AXZON:
                fragment = AxzonSelectorFragment.newInstance(true);
                break;
            case RFMICRON:
                fragment = AxzonSelectorFragment.newInstance(false);
                break;
            case UCODE:
                fragment = new UcodeFragment();
                break;
            case UCODE8:
                fragment = new Ucode8Fragment();
                break;
            case IMPINVENTORY:
                fragment = new ImpinjFragment();
                break;
            case WEDGE:
                fragment = new HomeSpecialFragment();
                break;
            case BLANK:
//                fragment = new BlankFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (position == DrawerPositions.MAIN) {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Don't add the transaction to back stack since we are navigating to the first fragment
            //being displayed and adding the same to the backstack will result in redundancy
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        } else {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Add the transaction to the back stack since we want the state to be preserved in the back stack
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        }
        //mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        //mDrawerList.setItemChecked(0, true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        super.onBackPressed();
    }

    public static boolean permissionRequesting;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MainActivity.mCs108Library4a.appendToLog("onRequestPermissionsResult ====");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequesting = false;

        EventBus.getDefault().post(new PermissionCallbackEvent());
    }


    public void sfnClicked(View view) {
        selectItem(DrawerPositions.SPECIAL);
    }

    public void connectClicked(View view) {
        selectItem(DrawerPositions.CONNECT);
    }

    public void invClicked(View view) { selectItem(DrawerPositions.INVENTORY); }

    public void locateClicked(View view) {
        selectItem(DrawerPositions.SEARCH);
    }

    public void multiBankClicked(View view) {
        selectItem(DrawerPositions.MULTIBANK);
    }

    public void settClicked(View view) {
        selectItem(DrawerPositions.SETTING);
    }

    public void filterClicked(View view) {
        selectItem(DrawerPositions.FILTER);
    }

    public void rrClicked(View view) {
        selectItem(DrawerPositions.READWRITE);
    }

    public void accessClicked(View view) {
        selectItem(DrawerPositions.SECURITY);
    }

    public void regClicked(View view) {
        selectItem(DrawerPositions.REGISTER);
    }

    public void coldChainClicked(View view) { selectItem(DrawerPositions.COLDCHAIN); }
    public void bapCardClicked(View view) { selectItem(DrawerPositions.BAPCARD); }
    public void ctesiusClicked(View view) { selectItem(DrawerPositions.CTESIUS); }

    public void axzonClicked(View view) { selectItem(DrawerPositions.AXZON); }
    public void rfMicronClicked(View view) { selectItem(DrawerPositions.RFMICRON); }

    public void uCodeClicked(View view) { selectItem(DrawerPositions.UCODE); }
    public void uCode8Clicked(View view) { selectItem(DrawerPositions.UCODE8); }

    public void impInventoryClicked(View view) { selectItem(DrawerPositions.IMPINVENTORY); }
    public void aurasenseClicked(View view) { selectItem(DrawerPositions.AURASENSE); }

    static boolean wedged = false;
    public void wedgeClicked(View view) {
        if (true) {
            wedged = true;
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        }
    }

    public void blankClicked(View view) {
//        selectItem(DrawerPositions.BLANK);
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "MainActivity.onItemClick: position = " + position + ", id = " + id);
            selectItem(DrawerListContent.DrawerPositions.toDrawerPosition(position));
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void replaceFragment(Fragment fragment){
        dialogShown = false;

        findViewById(R.id.loading).setVisibility(View.GONE);

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    public static void showKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static Fragment currentFragment;

    public void changeFragment(Fragment fragment) {
        try {
            Log.i("changeFragment", "changeFragment " + fragment.getClass());

            if( true) {

            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.loading).setVisibility(View.GONE);

        Log.i("changeFragment", "changeFragment");
        BorrowListFragment.POSITION = 1;
        DisposalListFragment.POSITION = 1;

        this.currentFragment = fragment;

        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            //ft.addToBackStack(backStateName);
            ft.commit();
        }
    }
    public static boolean isNetworkOK = false;

    public boolean isURLReachable() {
        return isNetworkAvailable();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
            (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
/*
        if(isNetworkOK) {
            return  isNetworkOK;
        }


        if(isConnected) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://www.google.com")
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    Log.i("asdas", "asdasd" + response.isSuccessful());
                    isNetworkOK = response.isSuccessful();
                    return response.isSuccessful();
                }

            } catch (Exception e) {
                Log.e("Error", "Error checking internet connection", e);
                Log.i("isNetworkAvailable", "isNetworkAvailable " + isConnected + "false");
                return false;
            }
        }
        Log.i("isNetworkAvailable", "isNetworkAvailable 3 " + isConnected + "");
*/
        return isConnected;
        //return false;
    }

    public  File savebitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + System.currentTimeMillis() + ".png");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
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

    String part_image;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<Asset> myAsset =  Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<Asset>());
        Log.i("onActivityResult", "onActivityResult requestCode resultCode data");
        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            Bitmap bitmap = camera.getCameraBitmap();
            if(bitmap != null) {
                try {
                    File f = savebitmap(bitmap);

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i("absolutePath", "absolutePath " + f.getAbsolutePath());
                            EventBus.getDefault().post(new FileChosenEvent(f.getAbsolutePath()));
                        }
                    };

                    Handler handler = new Handler();
                    handler.postDelayed(runnable, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //picFrame.setImageBitmap(bitmap);
            }else{
                //Toast.makeText(this.getApplicationContext(),"Picture not taken!",Toast.LENGTH_SHORT).show();
            }
            return;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.i("hihi", "hihi 1 onActivityResult " + result);

        if(result != null) {
            if(result.getContents() == null) {
                // Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                Log.i("result", "result " + result.getContents());

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        com.spit.fam.Event.BarcodeScanEvent barcodeScanEvent = new com.spit.fam.Event.BarcodeScanEvent(result.getContents());
                        EventBus.getDefault().post(barcodeScanEvent);
                    }
                }, 400);

                //  Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
               // if(LoginFragment.SP_API) {

               // } else {
                    for (int i = 0; i < myAsset.size(); i++) {
                        List<AssetsDetail> assetsDetails = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + myAsset.get(i).getAssetno());

                        if(assetsDetails != null)
                        Log.i("myAsset", "myAsset " + assetsDetails.get(0).getBarcode() + " " + assetsDetails.get(0).getBarcode().equals(result.getContents()));

                        if (myAsset.get(i).getAssetno() != null && myAsset.get(i).getAssetno().equals(result.getContents())) {
                            AssetsDetailWithTabFragment.id = Integer.parseInt(myAsset.get(i).getId());
                            AssetsDetailWithTabFragment.asset = myAsset.get(i);

                            List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET +  myAsset.get(i).getAssetno(), null);
                            if(assetsDetail != null)
                                AssetsDetailWithTabFragment.asset = convertAssetDetailToAsset(assetsDetail.get(0));

                            //EventBus.getDefault().post(new BarcodeScanEvent());
                            //Hawk.put("TEMP_ID", AssetsDetailWithTabFragment.id);
                            //Hawk.put("TEMP_RESULT", AssetsDetailWithTabFragment.asset);
                            break;
                        } else if (assetsDetails != null && assetsDetails.get(0).getBarcode().equals(result.getContents())) {
                            AssetsDetailWithTabFragment.id = Integer.parseInt(myAsset.get(i).getId());
                            AssetsDetailWithTabFragment.asset = myAsset.get(i);

                            List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET +  myAsset.get(i).getAssetno(), null);
                            AssetsDetailWithTabFragment.asset = convertAssetDetailToAsset(assetsDetail.get(0));

                            //EventBus.getDefault().post(new BarcodeScanEvent());
                            //Hawk.put("TEMP_ID", AssetsDetailWithTabFragment.id);
                            //Hawk.put("TEMP_RESULT", AssetsDetailWithTabFragment.asset);
                            break;
                        }
                    }
                //}
            }
        }
        Log.i("IntentResult", "IntentResult " + result);

        if (resultCode == RESULT_OK)
        {
            Log.i("hihi", "hihi 2 onActivityResult " + RESULT_OK + " " + requestCode + " " );

            if(requestCode == 9555)
            {
                Log.i("hihi", "hihi 3 onActivityResult " + 9544);

                Uri dataimage = data.getData();
                String[] imageprojection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(dataimage,imageprojection,null,null,null);

                Log.i("yoyo ", "yoyo case 1");
                if (cursor != null)
                {

                    Log.i("yoyo ", "yoyo case 2");

                    cursor.moveToFirst();
                    int indexImage = cursor.getColumnIndex(imageprojection[0]);
                    part_image = cursor.getString(indexImage);

                    Log.i("yoyo ", "yoyo case 3 " + part_image);

                    if(part_image != null)
                    {
                        Log.i("yoyo ", "yoyo case 4");

                        File image = new File(part_image);
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new FileChosenEvent(part_image));
                            }
                        };

                        Handler handler = new Handler();
                        handler.postDelayed(runnable, 1000);
                        //imgHolder.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
                    }
                }

                return;
            }
        }

        if(result != null) {
            /*
            if(result.getContents() == null) {
               // Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
              //  Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if(LoginFragment.SP_API) {

                } else {
                    for (int i = 0; i < myAsset.size(); i++) {
                        Log.i("myAsset", "myAsset " + myAsset.get(i).getBarcode().equals(result.getContents()));

                        if (myAsset.get(i).getAssetno() != null && myAsset.get(i).getAssetno().equals(result.getContents())) {
                            AssetsDetailWithTabFragment.id = Integer.parseInt(myAsset.get(i).getId());
                            AssetsDetailWithTabFragment.asset = myAsset.get(i);

                            EventBus.getDefault().post(new BarcodeScanEvent());
                            Hawk.put("TEMP_ID", AssetsDetailWithTabFragment.id);
                            Hawk.put("TEMP_RESULT", AssetsDetailWithTabFragment.asset);
                            break;
                        } else if (myAsset.get(i).getBarcode() != null && myAsset.get(i).getBarcode().equals(result.getContents())) {
                            AssetsDetailWithTabFragment.id = Integer.parseInt(myAsset.get(i).getId());
                            AssetsDetailWithTabFragment.asset = myAsset.get(i);

                            EventBus.getDefault().post(new BarcodeScanEvent());
                            Hawk.put("TEMP_ID", AssetsDetailWithTabFragment.id);
                            Hawk.put("TEMP_RESULT", AssetsDetailWithTabFragaewament.asset);
                            break;
                        }
                    }
                }
            }*/
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public boolean isConnected() {
        Intent intent = registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE"));
        return intent.getExtras().getBoolean("connected");
    }
}
