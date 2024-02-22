package com.spit.fam.SystemFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spit.fam.BaseUtils;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.DatabaseHelper.DataBaseHandler;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Item;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP2.BriefBorrowedList;
import com.spit.fam.Entity.SPEntityP2.StockTakeListData;
import com.spit.fam.Entity.SPUser;
import com.spit.fam.Entity.SpEntity.StockTakeDetail;
import com.spit.fam.Entity.SpEntity.StockTakeNoList;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.Event.APICallbackZeroEvent;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.HideLoadingEvent;
import com.spit.fam.Event.LoginDownloadProgressEvent;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.Event.PendingToAdd;
import com.spit.fam.Event.ProgressEvent;
import com.spit.fam.Event.ShowLoadingEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.Response.UserListResponse;
import com.spit.fam.WebService.APIUtils;
import com.spit.fam.WebService.Callback.GetBorrowListAssetCallback;
import com.spit.fam.WebService.Callback.GetBriefAssetCallback;
import com.spit.fam.WebService.Callback.GetBriefBorrowedAssetCallback;
import com.spit.fam.WebService.Callback.GetLevelDataCallback;
import com.spit.fam.WebService.Callback.GetListingCallback;
import com.spit.fam.WebService.Callback.GetStockTakeListCallback;
import com.spit.fam.WebService.Callback.GetStockTakeListDataCallback;
import com.spit.fam.WebService.Callback.NewAssetDetailCallback;
import com.spit.fam.WebService.Callback.UserListCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.WebService.SPGetWebService;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadFragment extends BaseFragment {

    public static int ASSET_DETAIL_API = 12;
    public static int RETURN_API = 10;
    public static int STOCK_TAKE_API = 11;

    public static int BORROW_API_1 = 7;
    public static int BORROW_API_2 = 8;
    public static int BORROW_API_3 = 9;

    public static int DISPOSAL_API_1 = 6;
    public static int DISPOSAL_API_2 = 5;
    public static int DISPOSAL_API_3 = 4;

    public static int SEARCH_NO_EPC = 13;
    public static int USER_LIST_API = 14;
    public static int NFC_USER_LIST_API = 15;

    public static int CONTINUOUS_ASSET_LIST = 20;
    public static int CONTINUOUS_RETURN_API = 100;
    public static int CONTINUOUS_STOCK_TAKE_API = 110;
    public static int CONTINUOUS_BORROW_API_1 = 70;
    public static int CONTINUOUS_BORROW_API_2 = 80;
    public static int CONTINUOUS_BORROW_API_3 = 90;
    public static int CONTINUOUS_DISPOSAL_API_1 = 60;
    public static int CONTINUOUS_DISPOSAL_API_2 = 50;
    public static int CONTINUOUS_DISPOSAL_API_3 = 40;
    public static int CONTINUOUS_SEARCH_NO_EPC = 130;
    public static int CONTINUOUS_USER_LIST_API = 140;
    public static int CONTINUOUS_NFC_USER_LIST_API = 150;
    public static int CONTINUOUS_ASSET_DETAIL = 1000;
    public static int TRAY_LIST = 160;


    public static ArrayList<StockTakeList> stockTakeListData = new ArrayList<>();
    public static boolean CONTINUOUS_STOCK_TAKE_API_CALLED = false;
    public static boolean CONTINUOUS_STOCK_TAKE_API_CALLING = false;
    public static boolean CONTINUOUS_USER_LIST_API_CALLED = false;

    LinearLayout borrow_panel;
    LinearLayout stock_take_panel;
    LinearLayout disposal_panel;
    ProgressBar loadingDialog;

    public static boolean DOWNLOAD_ALL_FROM_LOGIN;
    public static boolean DOWNLOAD_ON_BACK_PRESS;
    public boolean dalay;

    TextView registrationDownload, borrowListDownload, stockTakeListDownload, disposalDownload;

    public boolean userAPIReady, assetListAPIReady, registerAPIReady, assetAPIReady, borrowListAPIReady, stockTakeListAPIReady, disposalListAPIReady;

    public boolean downloadAllRest = false;
    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String userId = Hawk.get(InternalStorage.Login.USER_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        spAssetListReady = false;
        userAPIReady = false;
        assetListAPIReady = false;
        registerAPIReady = false;
        borrowListAPIReady = false;
        stockTakeListAPIReady = false;
        disposalListAPIReady = false;
        CONTINUOUS_STOCK_TAKE_API_CALLED = false;
        CONTINUOUS_USER_LIST_API_CALLED = false;


        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                disposalDetailDownloadedCount = 0;
                borrowDetailDownloadedCount = 0;
                stockTakeDetailDownloadedCount = 0;
                registrationDownloadCount = 0;

                Log.i("serverId", "serverId " + serverId + " [" +Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")) ;
                //Log.i("data", "data " +Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, new ArrayList<BorrowList>()));

                Log.i("DOWNLOAD_ALL_FROM_LOGIN", "DOWNLOAD_ALL_FROM_LOGIN 1 " + DOWNLOAD_ALL_FROM_LOGIN + " " + ((MainActivity)MainActivity.mContext).isNetworkAvailable());


                if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {

                    if(DOWNLOAD_ALL_FROM_LOGIN) {
                        EventBus.getDefault().post(new ShowLoadingEvent());
                        RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());

                        //RetrofitClient.getSPGetWebService().userList(companyId, serverId,  Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, "")).enqueue(new UserListCallback(CONTINUOUS_USER_LIST_API));
                        //RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(CONTINUOUS_SEARCH_NO_EPC));
                        //RetrofitClient.getSPGetWebService().assetsList(companyId,serverId).enqueue(new GetBriefAssetCallback(CONTINUOUS_ASSET_LIST));
                    }

                    RetrofitClient.getSPGetWebService().returnList(companyId, serverId).enqueue(new GetBriefAssetCallback(CONTINUOUS_RETURN_API));
                 //   RetrofitClient.getSPGetWebService().trayList(companyId, serverId).enqueue(new TrayListCallBack(TRAY_LIST));/*
/*
            RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_DISPOSAL_API_2));
            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_BORROW_API_2));
            RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_DISPOSAL_API_1));
            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_BORROW_API_1));
            RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_DISPOSAL_API_3));
            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_BORROW_API_3));
            RetrofitClient.getSPGetWebService().newStockTakeList(companyId, serverId).enqueue(new GetStockTakeListCallback(CONTINUOUS_STOCK_TAKE_API));
*/

                } else {
                    changeFragment(new SearchListFragment());

                    //EventBus.getDefault().post(new CallbackResponseEvent( Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, new ArrayList<BorrowList>())) );
                    //EventBus.getDefault().post(new CallbackResponseEvent( Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, new ArrayList<BorrowList>())) );
                    //EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST,  new ArrayList<StockTakeNoList>())));
                }
            }
        }, 400);
    }

    public void onPause() {
        super.onPause();
        //handler.removeCallbacks(restRunnable);
        downloadAllRest = false;
        restRunnable = null;
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.download_fragment, null);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        loadingDialog = view.findViewById(R.id.loading_progressbar);

        ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.download));

        borrow_panel = view.findViewById(R.id.borrow_panel);
        stock_take_panel = view.findViewById(R.id.stock_take_panel);
        disposal_panel = view.findViewById(R.id.disposal_panel);

        registrationDownload = view.findViewById(R.id.registration_download);
        borrowListDownload = view.findViewById(R.id.borrow_download);
        stockTakeListDownload = view.findViewById(R.id.stock_take_download);
        disposalDownload = view.findViewById(R.id.disposal_download);

        view.findViewById(R.id.add).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setVisibility(View.GONE);

        view.findViewById(R.id.asset_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShowLoadingEvent());

                Log.i("callingAPI", "callingAPI assetsList");
                Log.i("callingAPI", "callingAPI assetsList");
                RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, "", Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", true, getActivity()));

            }
        });

        view.findViewById(R.id.user_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShowLoadingEvent());

                Log.i("callingAPI", "callingAPI userList " + companyId + " " + serverId + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, ""));
                RetrofitClient.getSPGetWebService().userList(companyId, serverId,  Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, "")).enqueue(new Callback<UserListResponse>() {
                    @Override
                    public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
                        EventBus.getDefault().post(new HideLoadingEvent());

                        if(response.code() == 200) {
                            if(response.body().getData().size() > 0) {

                                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                                schTaskEx.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, response.body().getThiscalldate());
                                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, response.body().getData());//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                                    }
                                });
                            }
                            response.body().setLocalUserId(Hawk.get(InternalStorage.Login.USER_ID));
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.user_list_downloaded)));
                        } else {
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail) + " 1 " +  " " + call.request().url() ));
                        }
                    }

                    @Override
                    public void onFailure(Call<UserListResponse> call, Throwable t) {
                        EventBus.getDefault().post(new HideLoadingEvent());

                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail) + " 2 " + call.request().url()));
                    }
                });
            }
        });

        registrationDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("callingAPI", "callingAPI searchnoepc");
                EventBus.getDefault().post(new ShowLoadingEvent());
                RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new Callback<List<BriefAsset>>() {
                    @Override
                    public void onResponse(Call<List<BriefAsset>> call, Response<List<BriefAsset>> response) {
                        EventBus.getDefault().post(new HideLoadingEvent());

                        if(response.code() == 200) {
                            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                            schTaskEx.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) response.body()));
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.registration_list_downloaded)));
                                }
                            });
                        } else {
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<BriefAsset>> call, Throwable t) {
                        EventBus.getDefault().post(new HideLoadingEvent());

                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                    }
                });
            }
        });

        stockTakeListDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User user = Hawk.get(InternalStorage.OFFLINE_CACHE.USER, new LoginResponse()).getUser();
                //RetrofitClient.getService().getStockTakeLists(user.getUser_group().getId()).enqueue(new GetStockTakeListCallback());
            }
        });


        view.findViewById(R.id.borrow_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((TextView) borrowListDownload).getText().equals("-")) {
                    ((TextView) borrowListDownload).setText("+");
                    borrow_panel.setVisibility(View.GONE);
                } else {
                    ((TextView) borrowListDownload).setText("-");
                    borrow_panel.setVisibility(View.VISIBLE);
                }
                //User user = Hawk.get(InternalStorage.OFFLINE_CACHE.USER, new LoginResponse()).getUser();
                //RetrofitClient.getService().getBorrowLists(user.getId()).enqueue(new GetBorrowListCallBack());
            }
        });

        view.findViewById(R.id.disposal_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((TextView) disposalDownload).getText().equals("-")) {
                    ((TextView) disposalDownload).setText("+");
                    disposal_panel.setVisibility(View.GONE);
                } else {
                    ((TextView) disposalDownload).setText("-");
                    disposal_panel.setVisibility(View.VISIBLE);
                }
            }
        });

        view.findViewById(R.id.stock_take_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((TextView) stockTakeListDownload).getText().equals("-")) {
                    ((TextView) stockTakeListDownload).setText("+");
                    stock_take_panel.setVisibility(View.GONE);
                } else {
                    ((TextView) stockTakeListDownload).setText("-");
                    stock_take_panel.setVisibility(View.VISIBLE);
                }
                //User user = Hawk.get(InternalStorage.OFFLINE_CACHE.USER, new LoginResponse()).getUser();
                //RetrofitClient.getService().getStockTakeLists(user.getUser_group().getId()).enqueue(new GetStockTakeListCallback());
            }
        });


        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        view.findViewById(R.id.download_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SP_DISPOSAL_2
                // SP_BORROW_2
                // SP_STOCK_TAKE_LIST
                if( ((MainActivity)getActivity()).isNetworkAvailable() ) {
                    //EventBus.getDefault().post(new ShowLoadingEvent());
                    //downloadAll();
                    DOWNLOAD_ALL_FROM_LOGIN = true;
                    onResume();
                } else {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.no_internet)));
                }
            }
        });

        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());
        return view;
    }

    public int disposalDetailCount = -1;
    public int borrowDetailCount = -1;
    public int stockTakeDetailCount = -1;
    public int registrationCount = -1;
    public int assetCount = -1;

    public int disposalDetailDownloadedCount = 0;
    public int borrowDetailDownloadedCount = 0;
    public int stockTakeDetailDownloadedCount = 0;
    public int registrationDownloadCount = 0;
    public int assetDownloadCount = 0;

    ArrayList<Asset> assetArrayList = null;// = Hawk.get(InternalStorage.OFFLINE_CACHE.REGISTRATION, new ArrayList<Asset>());
    ArrayList<Asset> assetArrayListWithEPC = null;//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<Asset>());

    public void downloadAll() {
        loadingDialog.setVisibility(View.VISIBLE);
        view.findViewById(R.id.download_percentage).setVisibility(View.VISIBLE);

        Log.i("callingAPI", "callingAPI listingLevel");
        RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());


        RetrofitClient.getSPGetWebService().assetsList(companyId,serverId).enqueue(new GetBriefAssetCallback(2));
        //RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(1));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 100);

        ArrayList<BriefBorrowedList> disposal = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, new ArrayList<BriefBorrowedList>());
        ArrayList<BriefBorrowedList> borrow = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, new ArrayList<BriefBorrowedList>());
        ArrayList<StockTakeList> stockTake = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST, new ArrayList<StockTakeList>());

        assetArrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.REGISTRATION, new ArrayList<Asset>());
        assetArrayListWithEPC = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<Asset>());

        disposalDetailCount = disposal.size();// + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, new ArrayList<BriefBorrowedList>()).size() + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, new ArrayList<BriefBorrowedList>()).size();
        borrowDetailCount = borrow.size();// + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, new ArrayList<BriefBorrowedList>()).size() + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, new ArrayList<BriefBorrowedList>()).size();
        stockTakeDetailCount = stockTake.size();

        registrationCount = assetArrayList.size();// + assetArrayListWithEPC.size();
        assetCount = assetArrayListWithEPC.size();

        Log.i("registrationCount", "registrationCount " + registrationCount);

        /*
        disposalDetailDownloadedCount = 0;
        borrowDetailDownloadedCount = 0;
        stockTakeDetailDownloadedCount = 0;
        registrationDownloadCount = 0;
*/

        for (int i = 0; i < disposal.size(); i++) {
            final int pos = i;
            Log.i("callingAPI", "callingAPI disposalListAssets 2" );
            RetrofitClient.getSPGetWebService().disposalListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), disposal.get(pos).getDisposalNo()).enqueue(new GetBorrowListAssetCallback(DISPOSAL_API_2 + ""));
        }

        ArrayList<BriefBorrowedList> disposal_1 = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, new ArrayList<BriefBorrowedList>());
        Log.i("disposal_1" , "disposal_1 " + disposal.size());

        for (int i = 0; i < disposal_1.size(); i++) {
            final int pos = i;
            Log.i("callingAPI", "callingAPI disposalListAssets 1");
            RetrofitClient.getSPGetWebService().disposalListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), disposal_1.get(pos).getDisposalNo()).enqueue(new GetBorrowListAssetCallback(DISPOSAL_API_1 + ""));
        }

        ArrayList<BriefBorrowedList> disposal_3 = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, new ArrayList<BriefBorrowedList>());
        for (int i = 0; i < disposal_3.size(); i++) {
            final int pos = i;
            Log.i("callingAPI", "callingAPI disposalListAssets 3");
            RetrofitClient.getSPGetWebService().disposalListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), disposal_3.get(pos).getDisposalNo()).enqueue(new GetBorrowListAssetCallback(DISPOSAL_API_3 + ""));
        }

        //TODO
        // try {
        for (int i = 0; i < borrow.size(); i++) {
            final int pos = i;
            Log.i("callingAPI", "callingAPI borrowListAssets 1");

            RetrofitClient.getSPGetWebService().borrowListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), borrow.get(pos).getBorrowNo()).enqueue(new GetBorrowListAssetCallback(BORROW_API_1 + ""));
        }
        //  } catch (Exception e) {
        //      e.printStackTrace();
        //   }

        // try {
        ArrayList<BriefBorrowedList> borrow1 = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, new ArrayList<BriefBorrowedList>());
        for (int i = 0; i < borrow1.size(); i++) {
            final int pos = i;
            Log.i("callingAPI", "callingAPI borrowListAssets 2");

            RetrofitClient.getSPGetWebService().borrowListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), borrow1.get(pos).getBorrowNo()).enqueue(new GetBorrowListAssetCallback(BORROW_API_2 + ""));
        }
        //  } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // try {
        ArrayList<BriefBorrowedList> borrow2 = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, new ArrayList<BriefBorrowedList>());
        for (int i = 0; i < borrow2.size(); i++) {
            final int pos = i;
            Log.i("callingAPI", "callingAPI borrowListAssets 3");

            RetrofitClient.getSPGetWebService().borrowListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), borrow2.get(pos).getBorrowNo()).enqueue(new GetBorrowListAssetCallback(BORROW_API_3 + ""));
        }
        //} catch (Exception e) {
        //     e.printStackTrace();
        // }

        for (int i = 0; i < stockTake.size(); i++) {
            final int pos = i;

            Log.i("callingAPI", "callingAPI stockTakeListDetail " + stockTake.get(pos).getStocktakeno());

            //RetrofitClient.getSPGetWebService().stockTakeListAsset(
             //       Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),serverId, stockTake.get(pos).getStocktakeno()).enqueue(new GetBorrowListAssetCallback());
            try {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date today = new Date();
                Date todayWithZeroTime = formatter.parse(formatter.format(today));


                Date date = formatter.parse(stockTake.get(i).getEndDate());
                Date dateWithZeroTime = formatter.parse(formatter.format(date));

                Log.i("date", "date " + todayWithZeroTime + " " + dateWithZeroTime);

                if (todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                    RetrofitClient.getSPGetWebService().stockTakeListAsset2(companyId, serverId, stockTake.get(pos).getStocktakeno()).enqueue(new GetStockTakeListDataCallback());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            /*RetrofitClient.getSPGetWebService().stockTakeListDetail(
                    Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),
                    Hawk.get(InternalStorage.Login.USER_ID, ""), stockTake.getTable().get(pos).getOrderNo()).enqueue(new GetStockTakeDetailCallback(
                    stockTake.getTable().get(pos).getOrderNo()
            ));*/
        }

        //for (int i = 0; i < assetArrayList.size(); i++) {
        String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
        //Log.i("callingAPI", "callingAPI assetDetail case 1 " + assetArrayList.get(0).getAssetno());

        //if(assetArrayList.size() > 0)
        Log.i("param", "param " + companyId + " " + serverId + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, ""));

        Log.i("callingAPI", "callingAPI assetDetail");

        RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, "", Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1"));
        // }

        //for (int i = 0; i < assetArrayListWithEPC.size(); i++) {
        //String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

        //   Log.i("callingAPI", "callingAPI assetDetail case 2 " + assetArrayListWithEPC.get(0).getAssetno());

        //if(assetArrayListWithEPC.size() > 0)
        //     RetrofitClient.getSPGetWebService().assetDetail(companyId, userid, assetArrayListWithEPC.get(0).getAssetno()).enqueue(new GetSPAssetListCallback("2"));
        //}

    }

    public void onResume() {
        super.onResume();
        //handler.postDelayed(restRunnable, 100);
    }


    List<StockTakeList> stockTakeList = new ArrayList<>();
    List<BorrowList> borrowLists = new ArrayList<>();
    List<BorrowList> disposalLists = new ArrayList<>();

    ArrayList<LinearLayout> stockTakePanelList = new ArrayList<>();
    ArrayList<LinearLayout> disposalList = new ArrayList<>();
    ArrayList<LinearLayout> borrowList = new ArrayList<>();

    public BriefAsset convertAssetToBriefAsset(AssetsDetail asset) {
        BriefAsset briefAsset = new BriefAsset();

        briefAsset.setName(asset.getName());
        briefAsset.setAssetNo(asset.getAssetNo());
        briefAsset.setBrand(asset.getBrand());
        briefAsset.setModel(asset.getModel());
        briefAsset.setEpc(asset.getEpc());

        briefAsset.setLocation(asset.getLocation());
        briefAsset.setCategory(asset.getCategory());


        briefAsset.setStatusid(asset.getStatusid());
        briefAsset.setStatusname(asset.getStatusname());

        //briefAsset.setLocation(asset.getLocationString());

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
        asset.setFound(briefAsset.getFound());

        Status status = new Status();

        if(briefAsset.getOverdue() != null && briefAsset.getOverdue()) {
            status.id = 9999;
        } else if(briefAsset.getStatusid() != null){
            try {
                status.id = Integer.parseInt(briefAsset.getStatusid());
            } catch (Exception e) {
            }
            //status.id = -1;
        }
        asset.setStatus(status);

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

    public ArrayList<Asset> getAssetListFromBriefAssetList(List<BriefAsset> briefAssets) {
        ArrayList<Asset> assetArrayList = new ArrayList<>();
        if(briefAssets == null) {
            return assetArrayList;
        }

        for(int i = 0; i < briefAssets.size(); i++) {
            boolean exist = false;

            for(int y = 0; y < assetArrayList.size(); y++) {
                if(assetArrayList.get(y).getAssetno().equals(briefAssets.get(i).getAssetNo())) {
                    exist = true;
                }
            }

            if(!exist)
                if(briefAssets.get(i) != null)
                    assetArrayList.add(convertBriefAssetToAsset(briefAssets.get(i)));
        }

        return assetArrayList;
    }

    ArrayList<BriefBorrowedList> disposalListData = new ArrayList<>(); //SP_DISPOSAL_2
    ArrayList<BriefBorrowedList> borrowedListData = new ArrayList<>(); //SP_BORROW_2
    ArrayList<StockTakeNoList> stockTakeNoLists = new ArrayList<>();   //SP_STOCK_TAKE_LIST

    private Handler handler = new Handler();
    private int timeCounter = 0;

    private Runnable restRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i("restRunnable", "restRunnable " + downloadAllRest + spAssetListReady);

            if(spAssetListReady) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DOWNLOAD_ALL_FROM_LOGIN = false;
                        changeFragment(new SearchListFragment());
                    }
                });
            }
            if(downloadAllRest) {
                int counter = 0;

                int percentage = 0;

                if (disposalDetailCount <= disposalDetailDownloadedCount) {
                    //percentage += (int)(disposalDetailCount / (float)disposalDetailDownloadedCount * 20);

                    int newIncrement =(int)( (float)disposalDetailDownloadedCount / disposalDetailCount* 20);

                    if(newIncrement >= 20 || disposalDetailCount == 0)
                        newIncrement = 20;

                    if(disposalDetailCount == -1) {
                        newIncrement = 0;
                    }
                    Log.i("newIncrement", "newIncrement 1 " + newIncrement + " " + disposalDetailDownloadedCount + " " + disposalDetailCount);
                    percentage += newIncrement;

                    counter++;
                }
                Log.i("hehe", "Hehe 0 "+ disposalDetailCount + " " + disposalDetailDownloadedCount + " " + percentage);


                if(borrowDetailCount <= borrowDetailDownloadedCount) {
                    //percentage += (int)(borrowDetailCount / (float)borrowDetailDownloadedCount * 20);

                    int newIncrement =  (int)( (float)borrowDetailDownloadedCount / borrowDetailCount * 20);

                    if(newIncrement >= 20 || borrowDetailCount == 0)
                        newIncrement = 20;

                    if(borrowDetailCount == -1) {
                        newIncrement = 0;
                    }
                    Log.i("newIncrement", "newIncrement 2 " + newIncrement);
                    percentage += newIncrement;

                    counter++;
                }
                Log.i("hehe", "Hehe 1 "+ borrowDetailCount + " " + borrowDetailDownloadedCount + " " + percentage);


                if(stockTakeDetailCount <= stockTakeDetailDownloadedCount) {
                    //percentage += (int)(stockTakeDetailCount / (float)stockTakeDetailDownloadedCount * 20);

                    int newIncrement =  (int)((float)stockTakeDetailDownloadedCount / stockTakeDetailCount * 20);

                    if(newIncrement >= 20|| stockTakeDetailCount== 0)
                        newIncrement = 20;

                    if(stockTakeDetailCount == -1) {
                        newIncrement = 0;
                    }
                    Log.i("newIncrement", "newIncrement 3 " + newIncrement);
                    percentage += newIncrement;

                    counter++;
                }
                Log.i("hehe", "Hehe 2 "+ stockTakeDetailCount + " " + stockTakeDetailDownloadedCount + " " + percentage);


                if(registrationCount <= registrationDownloadCount) {
                    //percentage += (int)(registrationCount / (float)registrationDownloadCount * 20);

                    int newIncrement =  (int)((float)registrationDownloadCount / registrationCount * 20);

                    if(newIncrement >= 20 || registrationCount == 0)
                        newIncrement = 20;

                    if(registrationCount == -1) {
                        newIncrement = 0;
                    }
                    Log.i("newIncrement", "newIncrement 4 " + newIncrement);
                    percentage += newIncrement;

                    counter++;
                }
                Log.i("hehe", "Hehe 3 "+ registrationCount + " " + registrationDownloadCount + " " + percentage);


                //if(assetCount <= assetDownloadCount) {
                //   int newIncrement = (int)( (float)assetDownloadCount / assetCount * 20);

                //    if(newIncrement >= 20 || assetCount == 0)
                //       newIncrement = 20;

                percentage += 20;

                counter++;
                // }

                Log.i("hehe", "Hehe 4 "+ assetCount + " " + assetDownloadCount + " " + percentage);

                Log.i("percentage", "percentage " + (disposalDetailCount == disposalDetailDownloadedCount) + " " + (borrowDetailCount == borrowDetailDownloadedCount) + " " + (stockTakeDetailCount == stockTakeDetailDownloadedCount) + " " + (registrationCount == registrationDownloadCount) + " " + (assetCount == assetDownloadCount));

                Log.i("dialog", "dialog " + ((float)counter / 5f) * 100 + " " + percentage + " " + counter + " " + registerAPIReady + " " + borrowListAPIReady + " " + stockTakeListAPIReady + " " + disposalListAPIReady + " " + (disposalDetailCount > 0 && disposalDetailCount == disposalDetailDownloadedCount) + " " +(borrowDetailCount > 0 && borrowDetailCount == borrowDetailDownloadedCount) + " " + (stockTakeDetailCount > 0 && stockTakeDetailCount <= stockTakeDetailDownloadedCount) + " " + (registrationCount > 0 && registrationCount <= registrationDownloadCount));
                Log.i("download", "download " + disposalDetailCount + " " +  disposalDetailDownloadedCount + " " + borrowDetailCount + " " +  borrowDetailDownloadedCount + " " + stockTakeDetailCount + " " + stockTakeDetailDownloadedCount);// + " " + borrowDetailCount + " " +  borrowDetailDownloadedCount);

                loadingDialog.setProgress(percentage);

                ((TextView)view.findViewById(R.id.download_percentage)).setText(percentage +"%");

                if(percentage == 100) {
                    ArrayList<LevelData> categoryList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + "" + "_0_" + 1,null);
                    ArrayList<LevelData> locationList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + "" + "_1_" + 1, null);

                    Log.i("DOWNLOAD_ALL_FROM_LOGIN", "DOWNLOAD_ALL_FROM_LOGIN 2 " + DOWNLOAD_ALL_FROM_LOGIN + " " + pendingToAdds.size());

                    if(DOWNLOAD_ALL_FROM_LOGIN) {
                        boolean data1 = getData(categoryList, 1, 0);
                        boolean data2 = getData(locationList, 1, 1);

                        Log.i("DOWNLOAD_ALL_FROM_LOGIN", "DOWNLOAD_ALL_FROM_LOGIN 3 SearchListFragment " + data1 + " " + data2 + " " +dalay );


                        if(dalay /*||percentage == 100 */) {
                            dalay = false;

                            if(DOWNLOAD_ON_BACK_PRESS) {
                                DOWNLOAD_ON_BACK_PRESS = false;
                                Log.i("onBackPress", "onBackPress");
                                getActivity().onBackPressed();
                            } else {
                                DOWNLOAD_ALL_FROM_LOGIN = false;
                                changeFragment(new SearchListFragment());
                            }

                            return;
                        }

                        if(pendingToAdds.size() == 0 ) {
                            EventBus.getDefault().post(new ShowLoadingEvent());

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dalay = true;
                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new CallbackResponseEvent(null));
                                }
                            }, 1000);
                        } else {
                            EventBus.getDefault().post(new CallbackResponseEvent(null));
                        }


                    } else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //EventBus.getDefault().post(new HideLoadingEvent());
                            }
                        }, 1000);

                        EventBus.getDefault().post(new HideLoadingEvent());

                        view.findViewById(R.id.download_percentage).setVisibility(View.GONE);
                        loadingDialog.setVisibility(View.GONE);
                    }

                }

            }

            Log.i("yoyo", "yoyo " + disposalDetailCount + " " + borrowDetailCount + " " + stockTakeDetailCount + " " + disposalDetailDownloadedCount + " " + borrowDetailDownloadedCount + " " + stockTakeDetailDownloadedCount);
            timeCounter += 100;
            if(timeCounter >= 3000) {
                try {
                    //onResume();
                    timeCounter = 0;
                } catch (Exception e) {
                    downloadAllRest = false;
                    restRunnable = null;
                }
            }
            handler.postDelayed( this, 100);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateFailEvent event) {
        changeFragment(new SearchListFragment());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginDownloadProgressEvent event) {
        Log.i("LoginDownloadProgressEvent", "LoginDownloadProgressEvent");
        setProgress(event.getProgress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProgressEvent event) {
        Log.i("ProgressEvent", "ProgressEvent");

        //setProgress(((float) event.getCount() / (float) event.getTotal() ));
        ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.loading) + " " + event.getCount() + "/" + event.getTotal() + "");
        ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
        ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
        ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(((float) event.getCount() / (float) event.getTotal() ) * 100) );

    }

    public void setProgress(Float progress) {

        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = (int)((progress * 10000)) + "";

                int count = 0;
                String result = "";
                for(int i = 0; i < data.length(); i++) {
                   char temp = data.charAt(data.length() - i - 1);
                    result = temp + result ;
                    count++;

                    if(count == 2) {
                        result = "." + result;
                    }
                }

                ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.downloading) + " " + result + " %");
                ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
                ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
                ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(progress * 100));
            }

        });
    }

    int downloadPercentage = 0;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PendingToAdd event) {

        Log.i("pendingToAdds", "pendingToAdds " + pendingToAdds.size());
        if(pendingToAdds.size() > 0 ) {
            Log.i("pendingToAdds", "callingAPI pendingToAdds " + pendingToAdds.get(0).type + " " + pendingToAdds.get(0).typeString + (pendingToAdds.get(0).typeString != null && pendingToAdds.get(0).typeString.contains(InternalStorage.OFFLINE_CACHE.SP_ASSET)));

            if (pendingToAdds.get(0).type == 0) {
                // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_CATEGORY_CACHE, pendingToAdds.get(0).type);
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (pendingToAdds.get(0).fatherNo != null ? pendingToAdds.get(0).fatherNo: "") + "_0_" + pendingToAdds.get(0).level, pendingToAdds.get(0).levelData);

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            } else if (pendingToAdds.get(0).type == 1) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (pendingToAdds.get(0).fatherNo != null ? pendingToAdds.get(0).fatherNo: "") + "_1_" + pendingToAdds.get(0).level, pendingToAdds.get(0).levelData);

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LOCATION_CACHE, pendingToAdds.get(0).levelData);
            }
            //InternalStorage.OFFLINE_CACHE.SP_ASSET
            pendingToAdds.remove(0);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post((new PendingToAdd()));
                }
            }, 100);

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("type", "CallbackResponseEvent type " + event.type);
        if(event.getResponse() instanceof StockTakeListData) {
            CONTINUOUS_STOCK_TAKE_API_CALLING = false;
        }
        if(event.type == TRAY_LIST) {
            Hawk.put("TRAY_LIST", event.getResponse());
        }
        if(event.type == SEARCH_NO_EPC) {

            Log.i("callingAPI", "callingAPI registerAPIReady true");
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    //Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                    registerAPIReady = true;
                    registrationDownloadCount =  getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).size();
                }
            });
        }

        if(event.type == STOCK_TAKE_API || event.type == CONTINUOUS_STOCK_TAKE_API) {


            stockTakeListAPIReady = true;

            ((ViewGroup) stock_take_panel).removeAllViews();

            //stock_take_list_title

            if(((List<BriefBorrowedList>) event.getResponse()).size() > 0) {
                ((TextView)view.findViewById(R.id.stock_take_list_title)).setText(getString(R.string.stock_take_list) + " (" + ((List<BriefBorrowedList>) event.getResponse()).size() + ")");
                ((TextView)view.findViewById(R.id.stock_take_download)).setText("+");
                view.findViewById(R.id.stock_take_panel).setVisibility(View.GONE);
            } else {
                ((TextView)view.findViewById(R.id.stock_take_list_title)).setText(getString(R.string.stock_take_list));
            }

            int count = 0;

            for (int i = 0; i < ((List<BriefBorrowedList>) event.getResponse()).size(); i++) {
                DataBaseHandler dataBaseHandler = new DataBaseHandler(MainActivity.mContext);
                dataBaseHandler.addStockTake( ((List<StockTakeList>) event.getResponse()).get(i) );

                final int pos = i;

                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText(( ((List<StockTakeList>) event.getResponse()).get(i).getStocktakeno() + " | " + ((List<StockTakeList>) event.getResponse()).get(i).getName()));
                (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("callingAPI", "callingAPI stockTakeListDetail");
                        EventBus.getDefault().post(new ShowLoadingEvent());
                        RetrofitClient.getSPGetWebService().stockTakeListAsset2( companyId ,serverId, ((List<StockTakeList>)event.getResponse()).get(pos).getStocktakeno()).enqueue( new Callback<StockTakeListData>() {
                            @Override
                            public void onResponse(Call<StockTakeListData> call, Response<StockTakeListData> response) {

                                if (response.code() == 200) {

                                    //ArrayList<Asset> assets = new ArrayList<>();

                                    String stocktakeno = (response.body()).getStocktakeno();

                                    for(int i = 0; i < ( (response.body()).getData()).size(); i++) {
                                        Item briefAsset = ((response.body()).getData()).get(i);
                                        //assets.add(convertBriefAssetToAsset(briefAsset));
                                    }

                                    for(int i = 0; i < ((response.body()).getData()).size(); i++ ){
                                        RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, ((response.body()).getData()).get(i).getAssetno(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", false, getActivity()));
                                    }


                                    ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                                    schTaskEx.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + stocktakeno, response.body());
                                        }
                                    });

                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.stock_take_list_downloaded) + " (" + stocktakeno + ")"));
                                    ((ViewGroup)v.getParent()).setVisibility(View.GONE);
                                    RetrofitClient.getSPGetWebService().newStockTakeList(companyId, serverId).enqueue(new GetStockTakeListCallback(STOCK_TAKE_API));
                                } else {
                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                                }
                            }

                            @Override
                            public void onFailure(Call<StockTakeListData> call, Throwable t) {
                                EventBus.getDefault().post(new HideLoadingEvent());
                                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));

                            }
                        });
                    }


                });



                boolean exist = false;
                try {
                    Log.i("bugbug", "bugbug exist " + ((List<StockTakeList>) event.getResponse()).get(i).getClass() + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + ((List<StockTakeList>) event.getResponse()).get(i).getStocktakeno()));
                    if (Hawk.get(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + ((List<StockTakeList>) event.getResponse()).get(i).getStocktakeno()) != null) {
                        exist = true;
                    } else {
                    }
                } catch (Exception e) {
                    Log.i("bugbug", "bugbug " + e.getMessage());
                    e.printStackTrace();
                }

                if(!exist) {
                    count++;
                    stockTakePanelList.add(linearLayout);
                    ((ViewGroup) stock_take_panel).addView(linearLayout);
                }
            }

            ((TextView)view.findViewById(R.id.stock_take_list_title)).setText(getString(R.string.stock_take_list) + " (" + count + ")");


            if(((ViewGroup) stock_take_panel).getChildCount() == 0) {
                (view.findViewById(R.id.stock_take_list_wrapper)).setVisibility(View.GONE);
            } else {
                (view.findViewById(R.id.stock_take_list_wrapper)).setVisibility(View.VISIBLE);
            }

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST, event.getResponse());
                }
            });
        }

        if (event.type == RETURN_API) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("RETURN_API", "RETURN_API " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                }
            });
        }

        if (event.type == CONTINUOUS_RETURN_API) {
            Log.i("CONTINUOUS", "CONTINUOUS case 1");

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("RETURN_API", "RETURN_API " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                    RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_DISPOSAL_API_2));
                }
            });
        }


        if(event.type == CONTINUOUS_DISPOSAL_API_2) {
            Log.i("CONTINUOUS", "CONTINUOUS case 2");

            disposalDetailDownloadedCount = ((List<BriefBorrowedList>) event.getResponse()).size();

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("DISPOSAL_API_2", "DISPOSAL_API_2 " + event.getResponse() + " " + ((List<BriefBorrowedList>) event.getResponse()).size());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, event.getResponse());
                    disposalListAPIReady = true;
                    RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_BORROW_API_2));

                    for (int i = 0; i < ((ArrayList<BriefBorrowedList>)event.getResponse()).size(); i++) {
                        final int pos = i;

                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = new Date();
                        try {

                            Date todayWithZeroTime = formatter.parse(formatter.format(today));
                            Date dateWithZeroTime = formatter.parse((((ArrayList<BriefBorrowedList>) event.getResponse()).get(i).getValidDate()));


                            if(todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                                RetrofitClient.getSPGetWebService().disposalListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), ((List<BriefBorrowedList>) event.getResponse()).get(pos).getDisposalNo()).enqueue(new Callback<BorrowListAssets>() {
                                    @Override
                                    public void onResponse(Call<BorrowListAssets> call, Response<BorrowListAssets> response) {

                                        if(response.code() == 200) {
                                            ArrayList<Asset> assets = new ArrayList<>();
                                            String DISPOSAL_NO = (response.body()).getDisposalNo();

                                            for(int i = 0; i < ( (response.body()).getData()).size(); i++) {
                                                BriefAsset briefAsset = ((response.body()).getData()).get(i);
                                                assets.add(convertBriefAssetToAsset(briefAsset));
                                            }


                                            if(DISPOSAL_NO != null) {
                                                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + DISPOSAL_NO, response.body());
                                            }
                                        } else {
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<BorrowListAssets> call, Throwable t) {
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.i("hihi", "hihi BriefBorrowedList 2 " + e.getLocalizedMessage());

                            e.printStackTrace();
                        }

                    }
                }
            });

        }


        if(event.type == CONTINUOUS_BORROW_API_2) {
            Log.i("CONTINUOUS", "CONTINUOUS case 3");

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("SP_BORROW_2", "SP_BORROW_2 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, event.getResponse());
                    borrowListAPIReady = true;
                    Log.i("callingAPI", "callingAPI borrowListAPIReady true");

                    for (int i = 0; i < ((ArrayList<BriefBorrowedList>)event.getResponse()).size(); i++) {

                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = new Date();
                        try {

                            Date todayWithZeroTime = formatter.parse(formatter.format(today));
                            Date dateWithZeroTime = formatter.parse((((ArrayList<BriefBorrowedList>) event.getResponse()).get(i).getValidDate()));

                            Log.i("test", "test " + todayWithZeroTime.equals(dateWithZeroTime) + " " + todayWithZeroTime.before(dateWithZeroTime));

                            if (todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                                final int pos = i;
                                RetrofitClient.getSPGetWebService().borrowListAssets(companyId, serverId, ((List<BriefBorrowedList>) event.getResponse()).get(pos).getBorrowNo()).enqueue(new Callback<BorrowListAssets>() {
                                    @Override
                                    public void onResponse(Call<BorrowListAssets> call, Response<BorrowListAssets> response) {

                                        if(response.code() == 200) {
                                            ArrayList<Asset> assets = new ArrayList<>();

                                            String BORROW_NO = (response.body()).getBorrowno();

                                            for(int i = 0; i < ( (response.body()).getData()).size(); i++) {
                                                BriefAsset briefAsset = ((response.body()).getData()).get(i);
                                                assets.add(convertBriefAssetToAsset(briefAsset));
                                            }


                                            if(BORROW_NO != null) {
                                                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO, response.body());
                                            }

                                        } else {
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<BorrowListAssets> call, Throwable t) {
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_DISPOSAL_API_1));
                }
            });

        }

        if(event.type == CONTINUOUS_DISPOSAL_API_1) {
            Log.i("CONTINUOUS", "CONTINUOUS case 4");

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("DISPOSAL_API_1", "DISPOSAL_API_1 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, event.getResponse());
                    RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_BORROW_API_1));
                }
            });

        }


        if(event.type == CONTINUOUS_BORROW_API_1) {
            Log.i("CONTINUOUS", "CONTINUOUS case 5");

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("SP_BORROW_1", "SP_BORROW_1 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, event.getResponse());
                    RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_DISPOSAL_API_3));

                }
            });
        }

        if(event.type == CONTINUOUS_DISPOSAL_API_3) {
            Log.i("CONTINUOUS", "CONTINUOUS case 6");

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("DISPOSAL_API_3", "DISPOSAL_API_3 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, event.getResponse());
                    RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(CONTINUOUS_BORROW_API_3));
                }
            });
        }

        if(event.type == CONTINUOUS_BORROW_API_3) {
            Log.i("CONTINUOUS", "CONTINUOUS case 7");

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("SP_BORROW_3", "SP_BORROW_3 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, event.getResponse());
                    RetrofitClient.getSPGetWebService().newStockTakeList(companyId, serverId).enqueue(new GetStockTakeListCallback(CONTINUOUS_STOCK_TAKE_API));
                }
            });

        }

        if(event.type == CONTINUOUS_STOCK_TAKE_API) {
            Log.i("CONTINUOUS", "CONTINUOUS case 8 " + DOWNLOAD_ALL_FROM_LOGIN);

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST, event.getResponse());
            CONTINUOUS_STOCK_TAKE_API_CALLED = true;

            //stockTakeListData = (ArrayList<StockTakeList>) event.getResponse();

            //for (int i = 0; i < ((List<StockTakeList>)event.getResponse()).size(); i++) {
            //    final int pos = i;
                //RetrofitClient.getSPGetWebService().stockTakeListAsset2( companyId ,serverId, ((List<StockTakeList>)event.getResponse()).get(pos).getStocktakeno()).enqueue(new GetStockTakeListDataCallback());
            //}

            //if(DOWNLOAD_ALL_FROM_LOGIN) {
            //    RetrofitClient.getSPGetWebService().userList(companyId, serverId,  Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, "")).enqueue(new UserListCallback(CONTINUOUS_USER_LIST_API));
           // }
        }

        if(CONTINUOUS_STOCK_TAKE_API_CALLED && stockTakeListData.size() > 0) {
            Log.i("CONTINUOUS", "CONTINUOUS case 8.1 " + stockTakeListData.size());

            //if(!CONTINUOUS_STOCK_TAKE_API_CALLING) {
                //RetrofitClient.getSPGetWebService().stockTakeListAsset2(companyId, serverId, stockTakeListData.get(0).getStocktakeno()).enqueue(new GetStockTakeListDataCallback());
            if(Realm.getDefaultInstance().where(Item.class).equalTo("stocktakeno",stockTakeListData.get(0).getStocktakeno()).findAll().size() == 0) {
                APIUtils.download2(stockTakeListData.get(0).getStocktakeno());
                CONTINUOUS_STOCK_TAKE_API_CALLING = true;
            } else {
                stockTakeListData.remove(0);
                EventBus.getDefault().post(new CallbackResponseEvent(""));
            }
          //  }
        }

        if(CONTINUOUS_STOCK_TAKE_API_CALLED && stockTakeListData.size() == 0) {
            Log.i("CONTINUOUS", "CONTINUOUS case 8.2 " + stockTakeListData.size());
            if(DOWNLOAD_ALL_FROM_LOGIN) {
                if(!CONTINUOUS_USER_LIST_API_CALLED) {
                    RetrofitClient.getSPGetWebService().userList(companyId, serverId, Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, "")).enqueue(new UserListCallback(CONTINUOUS_USER_LIST_API));
                    CONTINUOUS_USER_LIST_API_CALLED = true;
                }
            }
        }

        Log.i("REMARK", "REMARK case 8.3 " + CONTINUOUS_STOCK_TAKE_API_CALLED + " " +stockTakeListData.size());

        if(event.type == CONTINUOUS_USER_LIST_API) {
            Log.i("CONTINUOUS", "CONTINUOUS case 9");


            if(DOWNLOAD_ALL_FROM_LOGIN) {
                RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(CONTINUOUS_SEARCH_NO_EPC));
                //RetrofitClient.getSPGetWebService().assetsList(companyId,serverId).enqueue(new GetBriefAssetCallback(CONTINUOUS_ASSET_LIST));
            }
        }

        if(event.type == CONTINUOUS_SEARCH_NO_EPC) {
            Log.i("CONTINUOUS", "CONTINUOUS case 10");
/*
            PRDownloader.initialize(MainActivity.mContext);
            PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setConnectTimeout(60000000).setDatabaseEnabled(true).build();
            PRDownloader.initialize(MainActivity.mContext, config);

            String api = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");
            String downloadURL = ""+ (api.startsWith("http://") ? api : ("http://" + api)) + (api.endsWith("/") ? "" : "/") + "AMSWebService_EvidenceRoom/MobileWebService.asmx/assetsDetail?companyId="
                    + companyId + "&userId=" + serverId + "&assetno=&lastcalldate=";// + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "");

            Log.i("data", "dataURL" + downloadURL);

            int downloadId = PRDownloader.download("http://212.183.159.230/20MB.zip", "/sdcard/", "abc.json")
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
                            Log.i("onError", "onError " + error.getResponseCode());

                        }

                    });
*/
          //  ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
          //  schTaskEx.execute(new Runnable() {
             //   @Override
            //    public void run() {
                    //Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                    registerAPIReady = true;
                    Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
//                    registrationDownloadCount =  getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).size();
                    if(DOWNLOAD_ALL_FROM_LOGIN) {
                        //RetrofitClient.getSPGetWebService().assetsList(companyId,serverId).enqueue(new GetBriefAssetCallback(CONTINUOUS_ASSET_LIST));

                        //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, "", Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback(CONTINUOUS_ASSET_DETAIL));
                        //new Handler().postDelayed(new Runnable() {
                        //    @Override
                        //    public void run() {

                        //
                        //

                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                        String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""); //response.body().getData().get(i).getUserid());


                        String apiRoot = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");

                        if (apiRoot.endsWith("/")) {

                        } else {
                            apiRoot = apiRoot + "/";
                        }

                        downloadFileRetrofit(apiRoot + "MobileWebService.asmx/assetsDetail?userid=" + serverId + "&companyid=" + companyId + "&assetno=&lastcalldate=" + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, ""));

                        //new APIUtils().download();
                        //    }
                        //}, 2000);
                    }
            //    }
          //  });

        }

        if(event.type == CONTINUOUS_ASSET_LIST) {
            Log.i("CONTINUOUS", "CONTINUOUS case 11 start");

            Log.i("eventtype", "eventtype 2" );
            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {

                    Log.i("CONTINUOUS", "CONTINUOUS case 11 middle 1");
//22:47:22 22:49:25

                    registerAPIReady = true;

                    registerAPIReady = true;
                    ArrayList<Asset> myList = getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse());//.size();


                    registrationDownloadCount = myList.size();// getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).size();

                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, myList);

                    //04:52:45
                    assetListAPIReady = true;

                    EventBus.getDefault().post(new CallbackResponseEvent(null));
                    Log.i("CONTINUOUS", "CONTINUOUS case 11 end ");
                    //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, "", Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback(CONTINUOUS_ASSET_DETAIL));


                }
            });

        }

        if(event.type == CONTINUOUS_ASSET_DETAIL) {
            Log.i("CONTINUOUS", "CONTINUOUS case 1 start");

            spAssetListReady = true;

            registerAPIReady = true;

            registerAPIReady = true;

            assetListAPIReady = true;

            DOWNLOAD_ALL_FROM_LOGIN = false;
            changeFragment(new SearchListFragment());
        }

        if(event.type == DISPOSAL_API_1) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("DISPOSAL_API_1", "DISPOSAL_API_1 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, event.getResponse());
                }
            });
        }

        if(event.type == DISPOSAL_API_2 || event.type == CONTINUOUS_DISPOSAL_API_2) {
            disposalDetailDownloadedCount = ((List<BriefBorrowedList>)event.getResponse()).size();

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("DISPOSAL_API_2", "DISPOSAL_API_2 " + event.getResponse() + " " + ((List<BriefBorrowedList>)event.getResponse()).size());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, event.getResponse());
                    disposalListAPIReady = true;
                }
            });

            ((ViewGroup) disposal_panel).removeAllViews();

            int count = 0;

            if(((List<BriefBorrowedList>) event.getResponse()).size() > 0) {
                ((TextView)view.findViewById(R.id.disposal_list_title)).setText(getString(R.string.disposal_list) + " (" + ((List<BriefBorrowedList>) event.getResponse()).size() + ")");

                ((TextView)view.findViewById(R.id.disposal_download)).setText("+");
                view.findViewById(R.id.disposal_panel).setVisibility(View.GONE);
            } else {
                ((TextView)view.findViewById(R.id.disposal_list_title)).setText(getString(R.string.disposal_list));
            }


            for (int i = 0; i < ((List<BriefBorrowedList>) event.getResponse()).size(); i++) {
                final int pos = i;

                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText(((List<BriefBorrowedList>) event.getResponse()).get(i).getDisposalNo() + " | " + ((List<BriefBorrowedList>) event.getResponse()).get(i).getName());

                if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + ((List<BriefBorrowedList>) event.getResponse()).get(i).getDisposalNo(), null) == null) {

                    try {
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = new Date();
                        Date todayWithZeroTime = formatter.parse(formatter.format(today));


                        Date date = formatter.parse(((List<BorrowList>) event.getResponse()).get(i).getValid_date());
                        Date dateWithZeroTime = formatter.parse(formatter.format(date));

                        Log.i("date", "date disposal_panel " + todayWithZeroTime + " " + dateWithZeroTime);

                        if (todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {

                            ((ViewGroup) disposal_panel).addView(linearLayout);
                            disposalList.add(linearLayout);
                            count++;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    BorrowListAssets borrowListAssets = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + ((List<BriefBorrowedList>) event.getResponse()).get(i).getDisposalNo(), null);

                    for(int x = 0; x < borrowListAssets.getData().size(); x++){
                        List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + borrowListAssets.getData().get(x).getAssetNo(), null);
                        if(assetsDetail == null) {
                            //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, borrowListAssets.getData().get(x).getAssetNo(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", false, getActivity()));
                        }
                    }
                }

                (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("callingAPI", "callingAPI disposalListAssets " + ((List<BriefBorrowedList>) event.getResponse()).get(pos).getDisposalNo());

                        RetrofitClient.getSPGetWebService().disposalListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, ""), ((List<BriefBorrowedList>) event.getResponse()).get(pos).getDisposalNo()).enqueue(new Callback<BorrowListAssets>() {
                            @Override
                            public void onResponse(Call<BorrowListAssets> call, Response<BorrowListAssets> response) {

                                if(response.code() == 200) {

                                    ArrayList<Asset> assets = new ArrayList<>();
                                    String DISPOSAL_NO = (response.body()).getDisposalNo();

                                    for(int i = 0; i < ( (response.body()).getData()).size(); i++) {
                                        BriefAsset briefAsset = ((response.body()).getData()).get(i);
                                        assets.add(convertBriefAssetToAsset(briefAsset));
                                    }


                                    if(DISPOSAL_NO != null) {
                                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + DISPOSAL_NO, response.body());
                                    }

                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.disposal_list_downloaded) + " (" + DISPOSAL_NO + ")"));
                                    RetrofitClient.getSPGetWebService().disposalList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(DISPOSAL_API_2));

                                } else {
                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                                }
                            }

                            @Override
                            public void onFailure(Call<BorrowListAssets> call, Throwable t) {
                                EventBus.getDefault().post(new HideLoadingEvent());
                                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                            }
                        });
                    }
                });
            }

            ((TextView)view.findViewById(R.id.disposal_list_title)).setText(getString(R.string.disposal_list) + " (" + count + ")");



            if(((ViewGroup) disposal_panel).getChildCount() == 0) {
                (view.findViewById(R.id.disposal_list_wrapper)).setVisibility(View.GONE);
            } else {
                (view.findViewById(R.id.disposal_list_wrapper)).setVisibility(View.VISIBLE);
            }

        }

        if(event.type == DISPOSAL_API_3) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("DISPOSAL_API_3", "DISPOSAL_API_3 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, event.getResponse());
                }
            });

        }


        if(event.type == BORROW_API_1) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("SP_BORROW_1", "SP_BORROW_1 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, event.getResponse());
                }
            });

        }
        if(event.type == BORROW_API_2 || event.type == CONTINUOUS_BORROW_API_2) {


            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("SP_BORROW_2", "SP_BORROW_2 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, event.getResponse());
                    borrowListAPIReady = true;
                    Log.i("callingAPI", "callingAPI borrowListAPIReady true");
                }
            });

            ((ViewGroup) borrow_panel).removeAllViews();

            if(((List<BriefBorrowedList>) event.getResponse()).size() > 0) {
                ((TextView)view.findViewById(R.id.borrow_list_title)).setText(getString(R.string.borrow_list) + " (" + ((List<BriefBorrowedList>) event.getResponse()).size() + ")");

                ((TextView)view.findViewById(R.id.borrow_download)).setText("+");
                (view.findViewById(R.id.borrow_panel)).setVisibility(View.GONE);

            } else {
                ((TextView)view.findViewById(R.id.borrow_list_title)).setText(getString(R.string.borrow_list));
            }

            int count = 0;

            for (int i = 0; i < ((List<BriefBorrowedList>) event.getResponse()).size(); i++) {
                final int pos = i;
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText(((List<BriefBorrowedList>) event.getResponse()).get(i).getBorrowNo() + " | " + ((List<BriefBorrowedList>) event.getResponse()).get(i).getName());

                if( Hawk.get (InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ +((List<BriefBorrowedList>) event.getResponse()).get(i).getBorrowNo()) == null) {
                    ((ViewGroup) borrow_panel).addView(linearLayout);
                    Log.i("event", "event BriefBorrowedList " + event.type + " " + (event.type == 2));


                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date today = new Date();
                    try {

                        Date todayWithZeroTime = formatter.parse(formatter.format(today));
                        Date dateWithZeroTime = formatter.parse((((ArrayList<BriefBorrowedList>) event.getResponse()).get(i).getValidDate()));


                        if (todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                            Log.i("hihi", "hihi 369");

                            borrowList.add(linearLayout);
                            count++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    BorrowListAssets borrowListAsset = Hawk.get (InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ +((List<BriefBorrowedList>) event.getResponse()).get(i).getBorrowNo());

                    for(int x = 0; x < borrowListAsset.getData().size(); x++) {

                        List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + borrowListAsset.getData().get(x).getAssetNo(), null);
                        if(assetsDetail == null) {
                            //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, borrowListAsset.getData().get(x).getAssetNo(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", false, getActivity()));
                        }

                    }
                }

                ((TextView)view.findViewById(R.id.borrow_list_title)).setText(getString(R.string.borrow_list) + " (" + count + ")");

                (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("callingAPI", "callingAPI borrowListAssets");
                        final String borrowNo = ((List<BriefBorrowedList>) event.getResponse()).get(pos).getBorrowNo();

                        RetrofitClient.getSPGetWebService().borrowListAssets(companyId, serverId, ((List<BriefBorrowedList>) event.getResponse()).get(pos).getBorrowNo()).enqueue(new Callback<BorrowListAssets>() {
                            @Override
                            public void onResponse(Call<BorrowListAssets> call, Response<BorrowListAssets> response) {
                                EventBus.getDefault().post(new HideLoadingEvent());

                                if(response.code() == 200) {
                                    ArrayList<Asset> assets = new ArrayList<>();

                                    String BORROW_NO = (response.body()).getBorrowno();

                                    for(int i = 0; i < ( (response.body()).getData()).size(); i++) {
                                        BriefAsset briefAsset = ((response.body()).getData()).get(i);
                                        assets.add(convertBriefAssetToAsset(briefAsset));
                                    }


                                    for(int i = 0; i < ((response.body()).getData()).size(); i++ ){
                                        RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, ((response.body()).getData()).get(i).getAssetNo(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", false, getActivity()));
                                    }

                                    if(BORROW_NO != null) {
                                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO, response.body());
                                    }
                                    RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                                    //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + borrowNo, event.getResponse());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.borrow_list_downloaded) + " (" + BORROW_NO + ")"));
                                    RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_1));
                                } else {
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                                }
                            }

                            @Override
                            public void onFailure(Call<BorrowListAssets> call, Throwable t) {
                                EventBus.getDefault().post(new HideLoadingEvent());
                                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                            }
                        });
                    }
                });
            }

            if(((ViewGroup) borrow_panel).getChildCount() == 0) {
                (view.findViewById(R.id.borrow_list_wrapper)).setVisibility(View.GONE);
            } else {
                (view.findViewById(R.id.borrow_list_wrapper)).setVisibility(View.VISIBLE);
            }
        }

        if(event.type == BORROW_API_3) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("SP_BORROW_3", "SP_BORROW_3 " + event.getResponse());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, event.getResponse());
                }
            });

        }

        /*
        if(event.type == 1) {
            Log.i("eventType", "eventType " + event.type);
            if(((List) event.getResponse()).size() > 0){
                RetrofitClient.getSPGetWebService().assetsList(companyId,serverId).enqueue(new GetBriefAssetCallback(2));
                RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(1));
            }

            for(int i = 0; i < ((List) event.getResponse()).size(); i++) {
                List<AssetsDetail> assetsDetails = new ArrayList<>();
                assetsDetails.add((AssetsDetail) ((List) event.getResponse()).get(i));
                //convertAssetToBriefAsset((AssetsDetail) ((List) event.getResponse()).get(i));

                ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
                int finalI = i;
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        assetDownloadCount++;
                        Log.i("assetDownloadCount", "assetDownloadCount " + assetDownloadCount);
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((AssetsDetail) ((List) event.getResponse()).get(finalI)).getAssetNo(),assetsDetails );//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                    }
                });
            }

        } else*/
        if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof SPUser) {
            userAPIReady = true;
            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, event.getResponse());//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

            ArrayList<SPUser> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER, new ArrayList<SPUser>());
            ArrayList<SPUser> response = ((ArrayList<SPUser>) event.getResponse());

            for(int i = 0; i < arrayList.size(); i ++) {
                for(int y = 0 ; y < response.size(); y++) {
                    if(arrayList.get(i).getUserid().equals( response.get(y).getUserid()) ) {
                        arrayList.get(i).setPassword(response.get(y).getPassword());
                        arrayList.get(i).setLoginid(response.get(y).getLoginid());

                        response.remove(y);
                        break;
                    }
                }
            }

            arrayList.addAll(response);

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, arrayList);

                    /*PendingToAdd pendingToAdd = new PendingToAdd();
                    pendingToAdd.typeString = InternalStorage.OFFLINE_CACHE.SP_USER;
                    pendingToAdd.spUsers = (List<SPUser>) event.getResponse();
                    pendingToAdds.add(pendingToAdd);

                    EventBus.getDefault().post(pendingToAdd);*/
                }
            });
        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof AssetsDetail) {
            Log.i("assetDownloadCount", "assetDownloadCount " + assetDownloadCount);

            //assetDownloadCount = ((List) event.getResponse()).size();
            //Log.i("assetDownloadCount", "assetDownloadCount ");
            //assetDownloadCount
            if(((List) event.getResponse()).size() > 0){
                RetrofitClient.getSPGetWebService().assetsList(companyId,serverId).enqueue(new GetBriefAssetCallback(2));
                RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(1));
            }

            for(int i = 0; i < ((List) event.getResponse()).size(); i++) {
                List<AssetsDetail> assetsDetails = new ArrayList<>();
                assetsDetails.add((AssetsDetail) ((List) event.getResponse()).get(i));
                //convertAssetToBriefAsset((AssetsDetail) ((List) event.getResponse()).get(i));

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                int finalI = i;
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        assetDownloadCount++;
                        Log.i("assetDownloadCount", "assetDownloadCount " + assetDownloadCount);
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((AssetsDetail) ((List) event.getResponse()).get(finalI)).getAssetNo(),assetsDetails );//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                    }
                });
            }


        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BriefAsset) {

            Log.i("event.getResponse", " event.getResponse " + event.type);

            if(event.type == 1) {
                Log.i("callingAPI", "callingAPI registerAPIReady true");

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
                        registerAPIReady = true;
                        registrationDownloadCount =  getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).size();
                    }
                });


            } else if(event.type == 2) {
                Log.i("eventtype", "eventtype 2" );
                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {

                        ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                        schTaskEx.execute(new Runnable() {
                            @Override
                            public void run() {

                                registerAPIReady = true;
                                registrationDownloadCount =  getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).size();

                                ArrayList<Asset> myList = new ArrayList<>();

                                for(int i = 0; i < getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).size(); i++) {
                                    if(getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).get(i).getEPC().length() > 0 )
                                        myList.add(getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()).get(i));
                                }



                                Log.i("callingAPI", "callingAPI spAssetListReady " + spAssetListReady);


                                ArrayList<Asset> withEPC = new ArrayList<>();
                                ArrayList<Asset> withNoEpc = new ArrayList<>();

                                for(int i = 0; i < myList.size(); i++) {
                                    if(myList.get(i).getEPC() == null || myList.get(i).getEPC().length() == 0){
                                        withNoEpc.add(myList.get(i));
                                    } else {
                                        withEPC.add(myList.get(i));
                                    }
                                }
                                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, withEPC);
                                Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, withNoEpc);
                            }
                        });
                        spAssetListReady = true;

                        assetListAPIReady = true;

                        EventBus.getDefault().post(new CallbackResponseEvent(null));

                    }
                });


            } else if (event.type == 3) {
                Log.i("type", "CallbackResponseEvent type 3 " + event.getResponse());
                Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

            }

        } else if(event.getResponse() instanceof StockTakeListData){
            String STOCK_TAKE_NO = ((StockTakeListData)event.getResponse()).getStocktakeno();

            stockTakeDetailDownloadedCount++;

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + STOCK_TAKE_NO, event.getResponse());

            Log.i("putStockTake", "putStockTake " + InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + STOCK_TAKE_NO);

            for(int i = 0; i < ((StockTakeListData)event.getResponse()).getData().size(); i++ ) {
               List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((StockTakeListData) event.getResponse()).getData().get(i).getAssetno(), null);
                if (assetsDetail == null) {
                    //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, ((StockTakeListData) event.getResponse()).getData().get(i).getAssetno(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback(true));
                }
            }

            //DataBaseHandler dataBaseHandler = new DataBaseHandler(MainActivity.mContext);
            //dataBaseHandler.addStockTakeDetailBy((StockTakeListData)event.getResponse());

        } else if(event.getResponse() instanceof BorrowListAssets) {
            ArrayList<Asset> assets = new ArrayList<>();

            String BORROW_NO = ((BorrowListAssets)event.getResponse()).getBorrowno();
            String DISPOSAL_NO = ((BorrowListAssets)event.getResponse()).getDisposalNo();
            String STOCK_TAKE_NO = ((BorrowListAssets)event.getResponse()).getStocktakeno();

            for(int i = 0; i < ( ((BorrowListAssets)event.getResponse()).getData()).size(); i++) {
                BriefAsset briefAsset = (((BorrowListAssets)event.getResponse()).getData()).get(i);
                assets.add(convertBriefAssetToAsset(briefAsset));
            }


            if(BORROW_NO != null) {
                borrowDetailDownloadedCount++;
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO, event.getResponse());

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            } else if(DISPOSAL_NO != null) {
                if(event.getId()!= null && event.getId().equals(DISPOSAL_API_1 + "")) {
                    Log.i("yes", "yes yes");
                    disposalDetailDownloadedCount++;
                }
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + DISPOSAL_NO, event.getResponse());

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            } /*else if(STOCK_TAKE_NO != null) {
                stockTakeDetailDownloadedCount++;

                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + STOCK_TAKE_NO, event.getResponse());

                for(int i = 0; i < ((BorrowListAssets)event.getResponse()).getData().size(); i++ ) {
                    List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((BorrowListAssets) event.getResponse()).getData().get(i).getAssetNo(), null);
                    if (assetsDetail == null) {
                        //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, ((BorrowListAssets) event.getResponse()).getData().get(i).getAssetNo(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", false, getActivity()));
                    }
                }

                DataBaseHandler dataBaseHandler = new DataBaseHandler(MainActivity.mContext);
                dataBaseHandler.addStockTakeDetailBy((BorrowListAssets)event.getResponse());
            }*/

        } else  if(event.getResponse() instanceof StockTakeDetail) {
            //stockTakeDetailDownloadedCount++;

            ArrayList<Asset> assets = new ArrayList<>();
            for (int i = 0; i < ((StockTakeDetail) event.getResponse()).getTable().size(); i++) {
                assets.add(((StockTakeDetail) event.getResponse()).getTable().get(i).convertToAsset());
            }
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + event.getId(), event.getResponse());

            ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
        } else if( ( event.getResponse()) instanceof StockTakeNoList) {
            Log.i("callingAPI", "callingAPI stockTakeListAPIReady true");

            stockTakeListAPIReady = true;

            ((ViewGroup) stock_take_panel).removeAllViews();

            for (int i = 0; i < ((StockTakeNoList) event.getResponse()).getTable() .size(); i++) {
                final int pos = i;

                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText( ((StockTakeNoList) event.getResponse()).getTable().get(i).getOrderNo() + " | " + ((StockTakeNoList) event.getResponse()).getTable().get(i).getOrderName());
                ((ViewGroup) stock_take_panel).addView(linearLayout);
                (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("callingAPI", "callingAPI stockTakeListDetail");
                        EventBus.getDefault().post(new ShowLoadingEvent());

                        RetrofitClient.getSPGetWebService().stockTakeListDetail(companyId,serverId, ((StockTakeNoList) event.getResponse()).getTable().get(pos).getOrderNo()).enqueue(new Callback<StockTakeDetail>() {
                            @Override
                            public void onResponse(Call<StockTakeDetail> call, Response<StockTakeDetail> response) {

                                if(response.code() == 200) {

                                    ArrayList<Asset> assets = new ArrayList<>();
                                    for (int i = 0; i < ((StockTakeDetail) response.body()).getTable().size(); i++) {
                                        assets.add(((StockTakeDetail) response.body()).getTable().get(i).convertToAsset());
                                    }
                                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + event.getId(), response.body());

                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.success)));
                                } else {
                                    EventBus.getDefault().post(new HideLoadingEvent());
                                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));
                                }
                            }

                            @Override
                            public void onFailure(Call<StockTakeDetail> call, Throwable t) {
                                EventBus.getDefault().post(new HideLoadingEvent());
                                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.fail)));

                            }
                        });
                    }
                });

                stockTakePanelList.add(linearLayout);
            }

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST, event.getResponse());

            /*ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                }
            });*/
        } else  if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BorrowList) {


            if((((List<BorrowList>) event.getResponse()).get(0)).getBorrowno() != null) {
                ((ViewGroup) borrow_panel).removeAllViews();

                for (int i = 0; i < ((List<BorrowList>) event.getResponse()).size(); i++) {
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                    ((TextView) linearLayout.findViewById(R.id.text)).setText(((List<BorrowList>) event.getResponse()).get(i).getName());
                    // ((TextView) linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);

                    ((ViewGroup) borrow_panel).addView(linearLayout);
                    Log.i("hihi", "hihi 123");
                }
            }

        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BriefBorrowedList) {

            Log.i("BriefBorrowedList", "BriefBorrowedList " + event.type);

            //Log.i("hihi", "hihi " + (((List<BriefBorrowedList>) event.getResponse()).get(0)).getBorrowNo());


            if((((List<BriefBorrowedList>) event.getResponse()).get(0)).getBorrowNo() != null) {



                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            if((((List<BriefBorrowedList>) event.getResponse()).get(0)).getDisposalNo() != null) {
                Log.i("callingAPI", "callingAPI disposalListAPIReady true");

                if(event.type == 2)  {

                }
            }

        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof Asset) {
            Log.i("yoyoyo", "yoyoyo");
            EventBus.getDefault().post(new DialogEvent(getString(R.string.download), getString(R.string.registration_list) + " " + getString(R.string.download_success)));
        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof StockTakeList) {
            Log.i("CallbackResponseEvent", "CallbackResponseEvent " + ((List) event.getResponse()).size());
            Log.i("callingAPI", "callingAPI stockTakeListAPIReady true");
            if((((List<StockTakeList>) event.getResponse()).get(0)).getStocktakeno() != null) {
            }
        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BorrowList) {
            if ((((List<BorrowList>) event.getResponse()).get(0)).getDisposal_status() != null) {
                ((ViewGroup)disposal_panel).removeAllViews();
                disposalLists =  ((List<BorrowList>) event.getResponse());

                for(int i = 0; i < ((List<BorrowList>) event.getResponse()).size(); i++) {
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                    ((TextView)linearLayout.findViewById(R.id.text)).setText( ((List<BorrowList>) event.getResponse()).get(i).getName());
                    ((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);
                    try {
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = new Date();
                        Date todayWithZeroTime = formatter.parse(formatter.format(today));


                        Date date = formatter.parse(((List<BorrowList>) event.getResponse()).get(i).getValid_date());
                        Date dateWithZeroTime = formatter.parse(formatter.format(date));

                        Log.i("date", "date disposal_panel " + todayWithZeroTime + " " + dateWithZeroTime);

                        if (todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {


                            ((ViewGroup)disposal_panel).addView(linearLayout);


                            for(int x = 0; x < ((List<BorrowList>) event.getResponse()).get(i).getAssets().size(); x++ ){
                                RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, ((List<BorrowList>) event.getResponse()).get(i).getAssets().get(x).getAssetno(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1", true, getActivity()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                EventBus.getDefault().post(new DialogEvent(getString(R.string.download), getString(R.string.disposal_list) + " " + getString(R.string.download_success)));

            } else if ((((List<BorrowList>) event.getResponse()).get(0)).getBorrow_status() != null) {
                ((ViewGroup)borrow_panel).removeAllViews();
                borrowLists =  ((List<BorrowList>) event.getResponse());

                for(int i = 0; i < ((List<BorrowList>) event.getResponse()).size(); i++) {
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(DownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                    ((TextView)linearLayout.findViewById(R.id.text)).setText( ((List<BorrowList>) event.getResponse()).get(i).getName());
                    ((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);

                    ((ViewGroup)borrow_panel).addView(linearLayout);
                    Log.i("hihi", "hihi 246");
                }

                EventBus.getDefault().post(new DialogEvent(getString(R.string.download), getString(R.string.borrow_list) + " " + getString(R.string.download_success)));
            }
        } else if(event.getResponse() instanceof ListingResponse) {
            Log.i("SP_LISTING_LEVEL", "SP_LISTING_LEVEL save " + event.getResponse());
            if(event.getResponse() != null) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL, (ListingResponse) event.getResponse());

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }

            int categorySize = ((ListingResponse)event.getResponse()).getCatSize();
            int locationSize = ((ListingResponse)event.getResponse()).getLocSize();

            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

            if (categorySize > 0) {
                Log.i("callingAPI", "callingAPI listing 0" );

                RetrofitClient.getSPGetWebService().listing(companyId, "", "0").enqueue(new GetLevelDataCallback("", 0, 1));
            }

            if (locationSize > 0) {
                Log.i("callingAPI", "callingAPI listing 1" );

                RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("", 1, 1));
            }
        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == LevelData.class) {
            Log.i("LevelDataType", "LevelDataType " + event.type + " " + event.level);

            ArrayList<LevelData> levelData = (ArrayList<LevelData>)event.getResponse();

            if(event.empty) {
                levelData.clear();
            }

            //SP_LISTING_LEVEL_CACHE__0_1


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    PendingToAdd pendingToAdd = new PendingToAdd();
                    pendingToAdd.levelData = levelData;
                    pendingToAdd.level = event.level;
                    pendingToAdd.fatherNo = event.getFatherno();
                    pendingToAdd.type = event.type;

                    pendingToAdds.add(pendingToAdd);

                    EventBus.getDefault().post( (pendingToAdd));


                    for(int i = 0; i < levelData.size(); i++) {
                        // if(levelData.size() > 0) {

                        Log.i("callingAPI", "callingAPI listing " + event.type + " "  + levelData.get(i).getRono());
                        RetrofitClient.getSPGetWebService().listing(companyId, levelData.get(i).getRono(), event.type + "").enqueue(new GetLevelDataCallback(levelData.get(i).getRono(), event.type, event.level + 1));
                        // levelData.remove(0);
                        //}
                    }
                }
            }, 10);

            ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {

                    //getActivity().runOnUiThread(new Runnable() {
                    //    @Override
                    //    public void run() {



                }
                // });

            });

        }

        Log.i("downloadAll", "downloadAll  " + downloadAllRest + " " + assetListAPIReady + " " + registerAPIReady + " " +  borrowListAPIReady + " " + stockTakeListAPIReady + " " + disposalListAPIReady);

        if(!downloadAllRest && assetListAPIReady && registerAPIReady && borrowListAPIReady && stockTakeListAPIReady && disposalListAPIReady) {
            Log.i("callingAPI", "callingAPI downloadAll");
            /*
            registerAPIReady = false;
            borrowListAPIReady = false;
            stockTakeListAPIReady = false;
            disposalListAPIReady = false;
*/
            downloadAllRest = true;
            //downloadAll();

        }

        int counter = 0;
/*
        if(registerAPIReady) {
            counter++;
        }

        if(borrowListAPIReady) {
            counter++;
        }

        if(stockTakeListAPIReady) {
            counter++;
        }

        if(disposalListAPIReady) {
            counter++;
        }
*/

        int percentage = 0;

        if (disposalDetailCount <= disposalDetailDownloadedCount) {
            //percentage += (int)(disposalDetailCount / (float)disposalDetailDownloadedCount * 20);

            int newIncrement =(int)( (float)disposalDetailDownloadedCount / disposalDetailCount* 20);

            if(newIncrement >= 20 || disposalDetailCount == 0)
                newIncrement = 20;

            if(disposalDetailCount == -1) {
                newIncrement = 0;
            }
            Log.i("newIncrement", "newIncrement 1 " + newIncrement + " " + disposalDetailDownloadedCount + " " + disposalDetailCount);
            percentage += newIncrement;

            counter++;
        }
        Log.i("hehe", "Hehe 0 "+ disposalDetailCount + " " + disposalDetailDownloadedCount + " " + percentage);


        if(borrowDetailCount <= borrowDetailDownloadedCount) {
            //percentage += (int)(borrowDetailCount / (float)borrowDetailDownloadedCount * 20);

            int newIncrement =  (int)( (float)borrowDetailDownloadedCount / borrowDetailCount * 20);

            if(newIncrement >= 20 || borrowDetailCount == 0)
                newIncrement = 20;

            if(borrowDetailCount == -1) {
                newIncrement = 0;
            }
            Log.i("newIncrement", "newIncrement 2 " + newIncrement);
            percentage += newIncrement;

            counter++;
        }
        Log.i("hehe", "Hehe 1 "+ borrowDetailCount + " " + borrowDetailDownloadedCount + " " + percentage);


        if(stockTakeDetailCount <= stockTakeDetailDownloadedCount) {
            //percentage += (int)(stockTakeDetailCount / (float)stockTakeDetailDownloadedCount * 20);

            int newIncrement =  (int)((float)stockTakeDetailDownloadedCount / stockTakeDetailCount * 20);

            if(newIncrement >= 20|| stockTakeDetailCount== 0)
                newIncrement = 20;

            if(stockTakeDetailCount == -1) {
                newIncrement = 0;
            }
            Log.i("newIncrement", "newIncrement 3 " + newIncrement);
            percentage += newIncrement;

            counter++;
        }
        Log.i("hehe", "Hehe 2 "+ stockTakeDetailCount + " " + stockTakeDetailDownloadedCount + " " + percentage);


        if(registrationCount <= registrationDownloadCount) {
            //percentage += (int)(registrationCount / (float)registrationDownloadCount * 20);

            int newIncrement =  (int)((float)registrationDownloadCount / registrationCount * 20);

            if(newIncrement >= 20 || registrationCount == 0)
                newIncrement = 20;

            if(registrationCount == -1) {
                newIncrement = 0;
            }
            Log.i("newIncrement", "newIncrement 4 " + newIncrement);
            percentage += newIncrement;

            counter++;
        }
        Log.i("hehe", "Hehe 3 "+ registrationCount + " " + registrationDownloadCount + " " + percentage);


        //if(assetCount <= assetDownloadCount) {
        //   int newIncrement = (int)( (float)assetDownloadCount / assetCount * 20);

        //    if(newIncrement >= 20 || assetCount == 0)
        //       newIncrement = 20;

        percentage += 20;

        counter++;
        // }

        Log.i("hehe", "Hehe 4 "+ assetCount + " " + assetDownloadCount + " " + percentage);

        Log.i("percentage", "percentage " + (disposalDetailCount == disposalDetailDownloadedCount) + " " + (borrowDetailCount == borrowDetailDownloadedCount) + " " + (stockTakeDetailCount == stockTakeDetailDownloadedCount) + " " + (registrationCount == registrationDownloadCount) + " " + (assetCount == assetDownloadCount));

        Log.i("dialog", "dialog " + ((float)counter / 5f) * 100 + " " + percentage + " " + counter + " " + registerAPIReady + " " + borrowListAPIReady + " " + stockTakeListAPIReady + " " + disposalListAPIReady + " " + (disposalDetailCount > 0 && disposalDetailCount == disposalDetailDownloadedCount) + " " +(borrowDetailCount > 0 && borrowDetailCount == borrowDetailDownloadedCount) + " " + (stockTakeDetailCount > 0 && stockTakeDetailCount <= stockTakeDetailDownloadedCount) + " " + (registrationCount > 0 && registrationCount <= registrationDownloadCount));
        Log.i("download", "download " + disposalDetailCount + " " +  disposalDetailDownloadedCount + " " + borrowDetailCount + " " +  borrowDetailDownloadedCount + " " + stockTakeDetailCount + " " + stockTakeDetailDownloadedCount);// + " " + borrowDetailCount + " " +  borrowDetailDownloadedCount);

        loadingDialog.setProgress(percentage);

        ((TextView)view.findViewById(R.id.download_percentage)).setText(percentage +"%");

        if(percentage == 100) {
            ArrayList<LevelData> categoryList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + "" + "_0_" + 1,null);
            ArrayList<LevelData> locationList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + "" + "_1_" + 1, null);

            Log.i("DOWNLOAD_ALL_FROM_LOGIN", "DOWNLOAD_ALL_FROM_LOGIN 2 " + DOWNLOAD_ALL_FROM_LOGIN + " " + pendingToAdds.size());

            if(DOWNLOAD_ALL_FROM_LOGIN) {
                boolean data1 = getData(categoryList, 1, 0);
                boolean data2 = getData(locationList, 1, 1);

                Log.i("DOWNLOAD_ALL_FROM_LOGIN", "DOWNLOAD_ALL_FROM_LOGIN 3 SearchListFragment " + data1 + " " + data2 + " " +dalay );


                if(dalay /*||percentage == 100 */) {
                    dalay = false;

                    if(DOWNLOAD_ON_BACK_PRESS) {
                        DOWNLOAD_ON_BACK_PRESS = false;
                        Log.i("onBackPress", "onBackPress");
                        getActivity().onBackPressed();
                    } else {
                        DOWNLOAD_ALL_FROM_LOGIN = false;
                        changeFragment(new SearchListFragment());
                    }

                    return;
                }

                if(pendingToAdds.size() == 0 ) {
                    EventBus.getDefault().post(new ShowLoadingEvent());

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dalay = true;
                            EventBus.getDefault().post(new HideLoadingEvent());
                            EventBus.getDefault().post(new CallbackResponseEvent(null));
                        }
                    }, 1000);
                } else {
                    EventBus.getDefault().post(new CallbackResponseEvent(null));
                }
                    /*
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeFragment(new SearchListFragment());
                            DownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = false;

                        }
                    }, 1000);*/


            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //EventBus.getDefault().post(new HideLoadingEvent());
                    }
                }, 1000);

                EventBus.getDefault().post(new HideLoadingEvent());

                view.findViewById(R.id.download_percentage).setVisibility(View.GONE);
                loadingDialog.setVisibility(View.GONE);
            }
            //Log.i("levelData", "levelData" + categoryList.size() + " " + locationList.size());

            //if(categoryList == null || locationList == null || (categoryList.size() == 0 && locationList.size() == 0) ) {
            //    return;
            //}

            //Log.i("levelData", "levelData" +  getData(categoryList, 1, 0) + " " + getData(locationList, 1, 1));
            //for (int i = 0; i < categoryList.size(); i++) {
            //    ArrayList<LevelData> data = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_0_" + event.level, null);
            //}

            //for (int i = 0; i < locationList.size(); i++) {
            //    ArrayList<LevelData> data = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_1_" + event.level, null);
            //}


            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                        /*if( getData(categoryList, 1, 0) && getData(locationList, 1, 1)) {
                            if(DOWNLOAD_ALL_FROM_LOGIN) {
                                DownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = false;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeFragment(new SearchListFragment());
                                    }
                                });
                            } else {
                                EventBus.getDefault().post(new HideLoadingEvent());
                                runnable.run();
                                //getActivity().runOnUiThread(new Runnable() {
                                //    @Override
                                //    public void run() {
                                       // view.findViewById(R.id.download_percentage).setVisibility(View.GONE);
                                       // loadingDialog.setVisibility(View.GONE);
                               //     }
                              //  });
                            }
                        }*/
                }
            });

        }


        Log.i("yoyo", "yoyo " + disposalDetailCount + " " + borrowDetailCount + " " + stockTakeDetailCount + " " + disposalDetailDownloadedCount + " " + borrowDetailDownloadedCount + " " + stockTakeDetailDownloadedCount);
    }
    ArrayList<LevelData> levelData;
    ArrayList<PendingToAdd> pendingToAdds = new ArrayList<>();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.findViewById(R.id.download_percentage).setVisibility(View.GONE);
                    loadingDialog.setVisibility(View.GONE);
                }
            });

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkInventoryDoneEvent dialogEvent) {
        changeFragment(new SearchListFragment());
    }

    public boolean spAssetListReady = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(APICallbackZeroEvent dialogEvent) {
        Log.i("HideLoadingEvent", "HideLoadingEvent " + DOWNLOAD_ALL_FROM_LOGIN + " " + spAssetListReady);

        if(DOWNLOAD_ALL_FROM_LOGIN) {

            /*getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeFragment(new SearchListFragment());
                }
            });*/
            if(spAssetListReady) {
                //((MainActivity)getActivity()).findViewById(R.id.loading).setVisibility(View.GONE);


                //DownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = false;


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //changeFragment(new SearchListFragment());
                    }
                }, 1000);

            }
        } else {
            if(spAssetListReady) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //EventBus.getDefault().post(new HideLoadingEvent());
                    }
                }, 1000);
            }

            view.findViewById(R.id.download_percentage).setVisibility(View.GONE);
            loadingDialog.setVisibility(View.GONE);
        }
    }

    public boolean getData(ArrayList<LevelData> data , int level, int type) {
        if(data == null)
            return false;

        int count = 0;

        for (int i = 0; i < data.size(); i++) {
            ArrayList<LevelData> d = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (data.get(i).getRono() != null ? data.get(i).getRono(): "")  + "_" + type + "_" + (level + 1), null);
            if(d != null && d.size() == 0) {
                count++;
            } else {
                if(getData(d, level + 1, type)) {
                    count++;
                }
            }
        }


        if(count == data.size()) {
            Log.i("data", "data " + data.size() + " " + level + " " + type + " " + (count == data.size()));

            return true;
        }

        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        Log.i("event", "event " + event.getTitle());

        String filterText = event.getTitle().toLowerCase();

        if(filterText == null || filterText.length() == 0) {
            view.findViewById(R.id.registration_wrapper).setVisibility(View.VISIBLE);
            view.findViewById(R.id.borrow_list_wrapper).setVisibility(View.VISIBLE);
            view.findViewById(R.id.stock_take_list_wrapper).setVisibility(View.VISIBLE);
            view.findViewById(R.id.disposal_list_wrapper).setVisibility(View.VISIBLE);
        }

        int disposalCount = 0;

        for(int i = 0; i < disposalList.size(); i++) {
            if(filterText == null || filterText.length() == 0 || ((TextView)disposalList.get(i).findViewById(R.id.text)).getText().toString().toLowerCase().contains(filterText.toLowerCase())) {
                disposalList.get(i).setVisibility(View.VISIBLE);
                disposalCount++;
            } else {
                disposalList.get(i).setVisibility(View.GONE);
            }
        }

        if(disposalCount == 0) {
            view.findViewById(R.id.disposal_list_wrapper).setVisibility(View.GONE);
        }

        int borrowCount = 0;

        for(int i = 0; i < borrowList.size(); i++) {
            if(filterText == null || filterText.length() == 0 || ((TextView)borrowList.get(i).findViewById(R.id.text)).getText().toString().toLowerCase().contains(filterText.toLowerCase())) {
                borrowList.get(i).setVisibility(View.VISIBLE);
                borrowCount++;
            } else {
                borrowList.get(i).setVisibility(View.GONE);
            }
        }

        if(borrowCount == 0) {
            view.findViewById(R.id.borrow_list_wrapper).setVisibility(View.GONE);
        }

        int stockTakeCount = 0;

        for(int i = 0; i < stockTakePanelList.size(); i++) {
            Log.i("hihi", "hihi " +  ((TextView)stockTakePanelList.get(i).findViewById(R.id.text)).getText().toString().toLowerCase() + " " + (filterText.toLowerCase()));
            if(filterText == null || filterText.length() == 0 || ((TextView)stockTakePanelList.get(i).findViewById(R.id.text)).getText().toString().toLowerCase().contains(filterText.toLowerCase())) {
                stockTakePanelList.get(i).setVisibility(View.VISIBLE);
                stockTakeCount++;
            } else {
                stockTakePanelList.get(i).setVisibility(View.GONE);
            }
        }


        if(stockTakeCount == 0) {
            view.findViewById(R.id.stock_take_list_wrapper).setVisibility(View.GONE);
        }

/*
        if(getString(R.string.registration_list).contains(filterText)) {
            view.findViewById(R.id.registration_wrapper).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.registration_wrapper).setVisibility(View.GONE);
        }

        if(getString(R.string.borrow_list).contains(filterText)) {
           view.findViewById(R.id.borrow_list_wrapper).setVisibility(View.VISIBLE);
        } else {
           view.findViewById(R.id.borrow_list_wrapper).setVisibility(View.GONE);
        }

        if(getString(R.string.stock_take_list).contains(filterText)) {
          view.findViewById(R.id.stock_take_list_wrapper).setVisibility(View.VISIBLE);
        } else {
          view.findViewById(R.id.stock_take_list_wrapper).setVisibility(View.GONE);
        }

        if(getString(R.string.disposal_list).contains(filterText)) {
            view.findViewById(R.id.disposal_list_wrapper).setVisibility(View.VISIBLE);
        } else {
           view.findViewById(R.id.disposal_list_wrapper).setVisibility(View.GONE);
        }

 */
    }

    void downloadFileRetrofit(String fileUrl) {
        SPGetWebService downloadService = RetrofitClient.getSPGetWebClient().create(SPGetWebService.class);

        Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync(fileUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("TAG", "server contacted and has file");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                            if(writtenToDisk) {

                                try {
                                    BaseUtils.parseLargeJson(MainActivity.mContext.getFilesDir().toString() + "/" + ("master.json"));

                                } catch (Exception e) {
                                }
                            }
                            Log.d("TAG", "file download was a success? " + writtenToDisk);
                            return null;
                        }
                    }.execute();
                } else {
                    Log.d("TAG", "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "error");
            }
        });
    }
    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(MainActivity.mContext.getFilesDir().toString() + "/" + ("master.json") );

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);

                    float progress = (float)fileSizeDownloaded / (float)fileSize;
                    Log.d("TAG", "file download: 2 " + progress);

                    DownloadFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new LoginDownloadProgressEvent(progress));
                        }
                    });
                }

                outputStream.flush();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}
