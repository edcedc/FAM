package com.spit.fam.RenewSystemFragment;

import android.os.AsyncTask;
import android.os.Bundle;
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
import com.spit.fam.Entity.SPEntityP3.ReturnAsset;
import com.spit.fam.Entity.SPEntityP3.BorrowAsset;
import com.spit.fam.Entity.SPEntityP3.BorrowDetailResponse;
import com.spit.fam.Entity.SPEntityP3.BorrowListItem;
import com.spit.fam.Entity.SPEntityP3.DisposalAsset;
import com.spit.fam.Entity.SPEntityP3.DisposalDetailResponse;
import com.spit.fam.Entity.SPEntityP3.DisposalListItem;
import com.spit.fam.Entity.SPEntityP3.SearchNoEpcItem;
import com.spit.fam.Entity.SPEntityP3.StocktakeList;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.HideLoadingEvent;
import com.spit.fam.Event.LoginDownloadProgressEvent;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.Event.PendingToAdd;
import com.spit.fam.Event.ProgressEvent;
import com.spit.fam.Event.ShowLoadingEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.Response.UserListResponse;
import com.spit.fam.SystemFragment.Adapter.StockTakeListAdapter;
import com.spit.fam.SystemFragment.BaseFragment;
import com.spit.fam.SystemFragment.SearchListFragment;
import com.spit.fam.WebService.Callback.GetLevelDataCallback;
import com.spit.fam.WebService.Callback.GetListingCallback;
import com.spit.fam.WebService.P2Callback.BorrowDetailCallback;
import com.spit.fam.WebService.P2Callback.BorrowListCallback;
import com.spit.fam.WebService.P2Callback.DisposalDetailCallback;
import com.spit.fam.WebService.P2Callback.DisposalListCallback;
import com.spit.fam.WebService.P2Callback.ReturnAssetCallback;
import com.spit.fam.WebService.P2Callback.SearchNoEpcCallback;
import com.spit.fam.WebService.P2Callback.StocktakeListCallback;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RevampDownloadFragment extends BaseFragment {
    public boolean spAssetListReady, userAPIReady, assetListAPIReady, registerAPIReady, assetAPIReady, borrowListAPIReady, stockTakeListAPIReady, disposalListAPIReady, categoryReady, locationReady;
    public static boolean CONTINUOUS_STOCK_TAKE_API_CALLED = false;
    public static boolean CONTINUOUS_STOCK_TAKE_API_CALLING = false;
    public static boolean CONTINUOUS_USER_LIST_API_CALLED = false;
    public int disposalDetailDownloadedCount = 0;
    public int borrowDetailDownloadedCount = 0;
    public int stockTakeDetailDownloadedCount = 0;
    public int registrationDownloadCount = 0;
    public int assetDownloadCount = 0;

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    public static boolean DOWNLOAD_ALL_FROM_LOGIN = true;
    public static boolean DOWNLOAD_ON_BACK_PRESS;


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

    public static int CONTINUOUS_DISPOSAL_DETAIL = 444;
    public static int CONTINUOUS_BORROW_DETAIL = 555;

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.download_fragment, null);

        view.findViewById(R.id.add).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setVisibility(View.GONE);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.download));

        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                if( ((MainActivity)getActivity()).isNetworkAvailable() ) {
                    DOWNLOAD_ALL_FROM_LOGIN = true;
                    EventBus.getDefault().post(new ShowLoadingEvent());
                    Log.i("RetrofitClient", "listingLevel");

                    RealmResults<LevelData> levelDataOld = (Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
                    Realm.getDefaultInstance().beginTransaction();
                    levelDataOld.deleteAllFromRealm();
                    Realm.getDefaultInstance().commitTransaction();

                    RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
                } else {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.no_internet)));
                }
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


        view.findViewById(R.id.asset_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("callingAPI", "callingAPI searchnoepc");
                EventBus.getDefault().post(new ShowLoadingEvent());
                RetrofitClient.getSPGetWebService().newSearchnoepc(companyId).enqueue(new SearchNoEpcCallback(CONTINUOUS_SEARCH_NO_EPC));
            }
        });
        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        return view;
    }

    @Override
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

        disposalDetailDownloadedCount = 0;
        borrowDetailDownloadedCount = 0;
        stockTakeDetailDownloadedCount = 0;
        registrationDownloadCount = 0;

        if (((MainActivity) MainActivity.mContext).isNetworkAvailable()) {
            if (DOWNLOAD_ALL_FROM_LOGIN) {
                EventBus.getDefault().post(new ShowLoadingEvent());
                Log.i("RetrofitClient", "listingLevel");

                RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
            } else {
                Log.i("RetrofitClient", "returnList");
                RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, 1).enqueue(new BorrowListCallback(CONTINUOUS_BORROW_API_2));

                //RetrofitClient.getSPGetWebService().newReturnList(companyId, serverId).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
            }
        } else {

            if(DOWNLOAD_ALL_FROM_LOGIN) {
                changeFragment(new SearchListFragment());
            } else {
                ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);
                EventBus.getDefault().post(new HideLoadingEvent());
            }
        }
    }

    ArrayList<LevelData> pendingToAdds = new ArrayList<>();
    ArrayList<DisposalListItem> pendingToDisposalApi = new ArrayList<>();
    ArrayList<BorrowListItem> pendingToBorrowApi = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {

        if (event.getResponse() instanceof ListingResponse) {

           // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL, event.getResponse());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findAll().deleteAllFromRealm();

            ListingResponse listingResponse = ((ListingResponse) event.getResponse());
            listingResponse.setPk(companyId + serverId + "SP_LISTING_LEVEL");
            Realm.getDefaultInstance().insertOrUpdate(listingResponse);
            Realm.getDefaultInstance().commitTransaction();

            ListingResponse l = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();

            Log.i("Retrofit", "Retrofit ListingResponse " + l);

            int categorySize = ((ListingResponse) event.getResponse()).getCatSize();
            int locationSize = ((ListingResponse) event.getResponse()).getLocSize();

            if (categorySize > 0) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "0").enqueue(new GetLevelDataCallback("", 0, 1));
            } else if (locationSize > 0) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("", 1, 1));
            } else {
                Realm.getDefaultInstance().beginTransaction();
                //Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("isNewData", false).findAll().deleteAllFromRealm();
                Realm.getDefaultInstance().commitTransaction();

                if (DOWNLOAD_ALL_FROM_LOGIN) {
                    RetrofitClient.getSPGetWebService().newReturnList(companyId, serverId).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
                }
            }
        }

        if (event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0).getClass() == LevelData.class) {

            ArrayList<LevelData> levelData = (ArrayList<LevelData>) event.getResponse();

            if (event.empty) {
                levelData.clear();
            }

            PendingToAdd pendingToAdd = new PendingToAdd();
            pendingToAdd.levelData = levelData;//
            pendingToAdd.level = event.level;//
            pendingToAdd.fatherNo = event.getFatherno();
            pendingToAdd.type = event.type;//

            pendingToAdds.addAll(levelData);

            Realm.getDefaultInstance().beginTransaction();

            Log.i("data", "downloadsize " + levelData.size());

            if (pendingToAdd.type == 0) {
               // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_0_" + event.level, event.getResponse());
                for (int i = 0; i < levelData.size(); i++) {
                    Log.i("inert", "insert " + levelData.get(i).getName() + " " + event.level + " " + levelData.get(i).getRono() + " " + event.getFatherno() + " " + event.type);
                    levelData.get(i).setNewData(true);

                    levelData.get(i).setFatherNo(event.getFatherno());
                    levelData.get(i).setType(event.type);
                    levelData.get(i).setLevel(event.level);
                    levelData.get(i).setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_0_" + pendingToAdd.level);
                    levelData.get(i).setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_0_" + pendingToAdd.level + i);
                    levelData.get(i).setOrdering(i);

                    levelData.get(i).setCompanyid(companyId);
                    levelData.get(i).setUserid(serverId);

                    Realm.getDefaultInstance().insertOrUpdate(pendingToAdd.levelData.get(i));
                }
            } else if (pendingToAdd.type == 1) {
                //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_1_" + event.level, event.getResponse());

                for (int i = 0; i < pendingToAdd.levelData.size(); i++) {
                    Log.i("inert", "insert " + levelData.get(i).getName() + " " + event.level + " " + levelData.get(i).getRono() + " " + event.getFatherno() + " " + event.type);
                    levelData.get(i).setNewData(true);
                    levelData.get(i).setFatherNo(event.getFatherno());
                    levelData.get(i).setType(event.type);
                    levelData.get(i).setLevel(event.level);
                    levelData.get(i).setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_1_" + pendingToAdd.level);
                    levelData.get(i).setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_1_" + pendingToAdd.level + i);
                    levelData.get(i).setOrdering(i);

                    levelData.get(i).setCompanyid(companyId);
                    levelData.get(i).setUserid(serverId);

                    Realm.getDefaultInstance().insertOrUpdate(pendingToAdd.levelData.get(i));
                }
            }
            Realm.getDefaultInstance().commitTransaction();

            if (pendingToAdds.size() > 0) {
                Log.i("RetrofitClient", "listing " + event.type);

                RetrofitClient.getSPGetWebService().listing(companyId, pendingToAdds.get(0).getRono(), event.type + "").enqueue(new GetLevelDataCallback(pendingToAdds.get(0).getRono(), event.type, pendingToAdds.get(0).getLevel() + 1));
                pendingToAdds.remove(0);
            } else {
                if (event.type == 0) {
                    Log.i("RetrofitClient", "listing " + event.type);

                    RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("", 1, 1));
                } else {

                    Realm.getDefaultInstance().beginTransaction();

                   // Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("isNewData", false).findAll().deleteAllFromRealm();

                    RealmResults<LevelData> result = Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().sort("ordering");

                    for (int i = 0; i < result.size(); i++) {
                        result.get(i).setNewData(false);
                        Realm.getDefaultInstance().insertOrUpdate((result.get(i)));
                    }

                    Realm.getDefaultInstance().commitTransaction();
                    int count = Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().size();

                    Log.i("RetrofitClient", "LevelData done " + count);
                    if (DOWNLOAD_ALL_FROM_LOGIN) {
                        RetrofitClient.getSPGetWebService().newReturnList(companyId, serverId).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
                    }
                }
            }
        }


        if (event.type == CONTINUOUS_RETURN_API) {
            Log.i("CONTINUOUS", "CONTINUOUS case 1");
            ArrayList<ReturnAsset> arrayList = ((ArrayList<ReturnAsset>) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(ReturnAsset.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < arrayList.size(); i++) {
                String pk = companyId + serverId + "RETURN" + arrayList.get(i).getAssetno();
                arrayList.get(i).setPk(pk);
                arrayList.get(i).setCompanyid(companyId);
                arrayList.get(i).setUserid(serverId);

                arrayList.get(i).setTimeStamp(new Date().getTime() + "");
                Realm.getDefaultInstance().insertOrUpdate(arrayList.get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            Log.i("RetrofitClient", "RETURN_API size " + Realm.getDefaultInstance().where(ReturnAsset.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().size() + " " + arrayList.size());

            if (DOWNLOAD_ALL_FROM_LOGIN) {
                RetrofitClient.getSPGetWebService().newDisposalList(companyId, serverId, 1).enqueue(new DisposalListCallback(CONTINUOUS_DISPOSAL_API_2));
            }
        }

        if (event.type == CONTINUOUS_DISPOSAL_API_1 || event.type == CONTINUOUS_DISPOSAL_API_2 || event.type == CONTINUOUS_DISPOSAL_API_3) {
            Realm.getDefaultInstance().beginTransaction();

            int type = -1;

            if (event.type == CONTINUOUS_DISPOSAL_API_1) {
                type = 0;
            }
            if (event.type == CONTINUOUS_DISPOSAL_API_2) {
                type = 1;
            }
            if (event.type == CONTINUOUS_DISPOSAL_API_3) {
                type = 2;
            }
            Realm.getDefaultInstance().where(DisposalListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("type", type).findAll().deleteAllFromRealm();

            ArrayList<DisposalListItem> arrayList = ((ArrayList<DisposalListItem>) event.getResponse());
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < arrayList.size(); i++) {

                try {
                    arrayList.get(i).setValidDateObj(format.parse(arrayList.get(i).getValidDate()));
                    Date expected = arrayList.get(i).getValidDateObj();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();
                    c.setTime(expected);
                    c.add(Calendar.DATE, 1);  // number of days to add
                    expected = (c.getTime());  // dt is now the new date
                    arrayList.get(i).setValidDateObj(expected);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Log.i("approvaldate", "approvaldate" + arrayList.get(i).getApprovalDate());
                    arrayList.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(arrayList.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                arrayList.get(i).setType(type);
                arrayList.get(i).setCompanyid(companyId);
                arrayList.get(i).setUserid(serverId);
                arrayList.get(i).setPk(companyId + serverId + "SP_DISPOSAL_" + type + arrayList.get(i).getDisposalNo());

                arrayList.get(i).setTimeString((long)i);
                Realm.getDefaultInstance().insertOrUpdate(arrayList.get(i));
            }


            Realm.getDefaultInstance().commitTransaction();


            int count = Realm.getDefaultInstance().where(DisposalListItem.class)
                    .equalTo("companyid", companyId)
                    .equalTo("userid", serverId)
                    .equalTo("type", type)
                    .greaterThanOrEqualTo("validDateObj", new Date())
                    .lessThanOrEqualTo("approvalDateObj", new Date())
                    .findAll().size();

            Log.i("RetrofitClient", "DisposalListItem size " + type + " " + count + " " + arrayList.size());

            pendingToDisposalApi.addAll(
                    Realm.getDefaultInstance().copyFromRealm(
                            Realm.getDefaultInstance().where(DisposalListItem.class)
                                    .equalTo("companyid", companyId)
                                    .equalTo("userid", serverId)
                                    .equalTo("type", type)
                                    .greaterThanOrEqualTo("validDateObj", new Date())
                                    .lessThanOrEqualTo("approvalDateObj", new Date())
                                    .findAll()));


            if (event.type == CONTINUOUS_DISPOSAL_API_2) {
                List<DisposalListItem> pendingToDisposalApi = Realm.getDefaultInstance().where(DisposalListItem.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)
                        .equalTo("type", 1)
                        .greaterThanOrEqualTo("validDateObj", new Date())
                        .lessThanOrEqualTo("approvalDateObj", new Date())
                        .findAll();

                for (int i = 0; i < pendingToDisposalApi.size(); i++) {
                   // Log.i("CONTINUOUS_DISPOSAL_API_2", "CONTINUOUS_DISPOSAL_API_2 " + i);
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.mContext).inflate(R.layout.download_upload_list_row, null);
                    ((TextView) linearLayout.findViewById(R.id.text)).setText(pendingToDisposalApi.get(i).getDisposalNo() + " | " + pendingToDisposalApi.get(i).getName());

                    final int finalI = i;

                    (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new ShowLoadingEvent());

                            RetrofitClient.getSPGetWebService().newDisposalListAssets(companyId, serverId, pendingToDisposalApi.get(finalI).getDisposalNo()).enqueue(new DisposalDetailCallback(CONTINUOUS_DISPOSAL_DETAIL));
                        }
                    });

                    ((ViewGroup) view.findViewById(R.id.disposal_panel)).addView(linearLayout);
                }
                ((TextView)view.findViewById(R.id.disposal_list_title)).setText(getString(R.string.disposal_list) + " (" + pendingToDisposalApi.size() + ")");
                ((TextView) view.findViewById(R.id.disposal_download)).setText("+");

                ((ViewGroup) view.findViewById(R.id.disposal_panel)).setVisibility(View.GONE);

                if(!DOWNLOAD_ALL_FROM_LOGIN) {
                    RetrofitClient.getSPGetWebService().renewStockTakeList(companyId, serverId).enqueue(new StocktakeListCallback(CONTINUOUS_STOCK_TAKE_API));
                    //EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.download_success)));
                }

                if(pendingToDisposalApi.size() == 0) {
                    ((ViewGroup)((ViewGroup) view.findViewById(R.id.disposal_panel)).getParent()).setVisibility(View.GONE);
                }
            }


            view.findViewById(R.id.disposal_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((TextView) view.findViewById(R.id.disposal_download)).getText().equals("-")) {
                        ((TextView) view.findViewById(R.id.disposal_download)).setText("+");
                        ((ViewGroup) view.findViewById(R.id.disposal_panel)).setVisibility(View.GONE);
                    } else {
                        ((TextView) view.findViewById(R.id.disposal_download)).setText("-");
                        ((ViewGroup) view.findViewById(R.id.disposal_panel)).setVisibility(View.VISIBLE);
                    }
                }
            });

            if (DOWNLOAD_ALL_FROM_LOGIN) {

                if (event.type == CONTINUOUS_DISPOSAL_API_1) {
                    RetrofitClient.getSPGetWebService().newDisposalList(companyId, serverId, 2).enqueue(new DisposalListCallback(CONTINUOUS_DISPOSAL_API_3));
                }
                if (event.type == CONTINUOUS_DISPOSAL_API_2) {
                    RetrofitClient.getSPGetWebService().newDisposalList(companyId, serverId, 0).enqueue(new DisposalListCallback(CONTINUOUS_DISPOSAL_API_1));
                }

                if (event.type == CONTINUOUS_DISPOSAL_API_3) {
                    if (pendingToDisposalApi.size() > 0) {
                        Realm.getDefaultInstance().beginTransaction();
                        Realm.getDefaultInstance().where(DisposalAsset.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();
                        Realm.getDefaultInstance().commitTransaction();

                        RetrofitClient.getSPGetWebService().newDisposalListAssets(companyId, serverId, pendingToDisposalApi.get(0).getDisposalNo()).enqueue(new DisposalDetailCallback(CONTINUOUS_DISPOSAL_DETAIL));
                    } else {

                        Log.i("RetrofitClient", "newDisposalListAssets done 1");
                        RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, 1).enqueue(new BorrowListCallback(CONTINUOUS_BORROW_API_2));
                    }
                }
            }
        }

        if (event.type == CONTINUOUS_DISPOSAL_DETAIL) {
            if(pendingToDisposalApi.size() > 0)
                pendingToDisposalApi.remove(0);
            DisposalDetailResponse res = ((DisposalDetailResponse) event.getResponse());

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
            int count = Realm.getDefaultInstance().where(DisposalAsset.class).equalTo("disposalNo", res.getDisposalNo()).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().size();
            Log.i("RetrofitClient", "newDisposalListAssets   " + res.getDisposalNo() + " " + count);

            if (DOWNLOAD_ALL_FROM_LOGIN) {
                if (pendingToDisposalApi.size() > 0) {
                    RetrofitClient.getSPGetWebService().newDisposalListAssets(companyId, serverId, pendingToDisposalApi.get(0).getDisposalNo()).enqueue(new DisposalDetailCallback(CONTINUOUS_DISPOSAL_DETAIL));
                } else {
                    Log.i("RetrofitClient", "newDisposalListAssets done 2");
                    RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, 1).enqueue(new BorrowListCallback(CONTINUOUS_BORROW_API_2));
                }
            }

            if(!DOWNLOAD_ALL_FROM_LOGIN) {
                EventBus.getDefault().post(new HideLoadingEvent());
                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.download_success)));
            }
        }

        if (event.type == CONTINUOUS_BORROW_API_1 || event.type == CONTINUOUS_BORROW_API_2 || event.type == CONTINUOUS_BORROW_API_3) {

            Realm.getDefaultInstance().beginTransaction();

            int type = -1;

            if (event.type == CONTINUOUS_BORROW_API_1) {
                type = 0;
            }
            if (event.type == CONTINUOUS_BORROW_API_2) {
                type = 1;
            }
            if (event.type == CONTINUOUS_BORROW_API_3) {
                type = 2;
            }

            Realm.getDefaultInstance().where(BorrowListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("type", type).findAll().deleteAllFromRealm();

            ArrayList<BorrowListItem> arrayList = ((ArrayList<BorrowListItem>) event.getResponse());
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < arrayList.size(); i++) {

                try {
                    arrayList.get(i).setValidDateObj(format.parse(arrayList.get(i).getValidDate()));
                    Date expected = arrayList.get(i).getValidDateObj();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();
                    c.setTime(expected);
                    c.add(Calendar.DATE, 1);  // number of days to add
                    expected = (c.getTime());  // dt is now the new date
                    arrayList.get(i).setValidDateObj(expected);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Log.i("approvaldate", "approvaldate" + arrayList.get(i).getApprovalDate());
                    arrayList.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(arrayList.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                arrayList.get(i).setType(type);
                arrayList.get(i).setCompanyid(companyId);
                arrayList.get(i).setUserid(serverId);
                arrayList.get(i).setPk(companyId + serverId + "" + type + arrayList.get(i).getBorrowno());
                arrayList.get(i).setTimeString((long)i);

                Log.i("data", "data " + arrayList.get(i).getPk());

                Realm.getDefaultInstance().insertOrUpdate(arrayList.get(i));
            }


            Realm.getDefaultInstance().commitTransaction();


            int count = Realm.getDefaultInstance().where(BorrowListItem.class)
                    .equalTo("companyid", companyId)
                    .equalTo("userid", serverId)
                    .equalTo("type", type)
                    .greaterThanOrEqualTo("validDateObj", new Date())
                    .lessThanOrEqualTo("approvalDateObj", new Date())
                    .findAll().size();

            Log.i("RetrofitClient", "BorrowListItem size " + type + " " + count + " " + arrayList.size() + " " + pendingToBorrowApi.size());

            pendingToBorrowApi.addAll(
                    Realm.getDefaultInstance().copyFromRealm(
                            Realm.getDefaultInstance().where(BorrowListItem.class)
                                    .equalTo("companyid", companyId)
                                    .equalTo("userid", serverId)
                                    .equalTo("type", type)
                                    .greaterThanOrEqualTo("validDateObj", new Date())
                                    .lessThanOrEqualTo("approvalDateObj", new Date())
                                    .findAll()));

            if (event.type == CONTINUOUS_BORROW_API_2) {
                List<BorrowListItem> pendingToBorrowApi = Realm.getDefaultInstance().where(BorrowListItem.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)
                        .equalTo("type", 1)
                        .greaterThanOrEqualTo("validDateObj", new Date())
                        .lessThanOrEqualTo("approvalDateObj", new Date())
                        .findAll();

                for (int i = 0; i < pendingToBorrowApi.size(); i++) {
                    Log.i("CONTINUOUS_BORROW_API_2", "CONTINUOUS_BORROW_API_2 " + i);
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(RevampDownloadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                    ((TextView) linearLayout.findViewById(R.id.text)).setText(pendingToBorrowApi.get(i).getBorrowno() + " | " + pendingToBorrowApi.get(i).getName());

                    final int finalI = i;

                    (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EventBus.getDefault().post(new ShowLoadingEvent());
                            Log.i("CONTINUOUS_BORROW_API_2", "CONTINUOUS_BORROW_API_2 " + finalI);

                            RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, serverId, pendingToBorrowApi.get(finalI).getBorrowno()).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
                        }
                    });

                    ((ViewGroup) view.findViewById(R.id.borrow_panel)).addView(linearLayout);
                }
                ((TextView)view.findViewById(R.id.borrow_list_title)).setText(getString(R.string.borrow_list) + " (" + pendingToBorrowApi.size() + ")");
                ((TextView) view.findViewById(R.id.borrow_download)).setText("+");

                ((ViewGroup) view.findViewById(R.id.borrow_panel)).setVisibility(View.GONE);

                if(pendingToBorrowApi.size() == 0) {
                    ((ViewGroup)((ViewGroup) view.findViewById(R.id.borrow_panel)).getParent()).setVisibility(View.GONE);
                }

                if(!DOWNLOAD_ALL_FROM_LOGIN) {
                    //EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.download_success)));
                }

            }

            view.findViewById(R.id.borrow_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((TextView) view.findViewById(R.id.borrow_download)).getText().equals("-")) {
                        ((TextView) view.findViewById(R.id.borrow_download)).setText("+");
                        ((ViewGroup) view.findViewById(R.id.borrow_panel)).setVisibility(View.GONE);
                    } else {
                        ((TextView) view.findViewById(R.id.borrow_download)).setText("-");
                        ((ViewGroup) view.findViewById(R.id.borrow_panel)).setVisibility(View.VISIBLE);
                    }
                }
            });

            if (DOWNLOAD_ALL_FROM_LOGIN) {
                if (event.type == CONTINUOUS_BORROW_API_1) {
                    RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, 2).enqueue(new BorrowListCallback(CONTINUOUS_BORROW_API_3));
                }
                if (event.type == CONTINUOUS_BORROW_API_2) {
                    RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, 0).enqueue(new BorrowListCallback(CONTINUOUS_BORROW_API_1));
                }
                if (event.type == CONTINUOUS_BORROW_API_3) {
                    if (pendingToBorrowApi.size() > 0) {
                        Realm.getDefaultInstance().beginTransaction();
                        //Realm.getDefaultInstance().where(BorrowListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();
                        Realm.getDefaultInstance().commitTransaction();

                        RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, serverId, pendingToBorrowApi.get(0).getBorrowno()).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
                    } else {
                        Log.i("RetrofitClient", "newBorrowListAssets done 3");
                        Log.i("RetrofitClient", "renewStockTakeList");
                        RetrofitClient.getSPGetWebService().renewStockTakeList(companyId, serverId).enqueue(new StocktakeListCallback(CONTINUOUS_STOCK_TAKE_API));
                    }
                }
            } else {
                if (event.type == CONTINUOUS_BORROW_API_2) {
                    RetrofitClient.getSPGetWebService().newDisposalList(companyId, serverId, 1).enqueue(new DisposalListCallback(CONTINUOUS_DISPOSAL_API_2));
                }
            }
        }


        if (event.type == CONTINUOUS_BORROW_DETAIL) {
            if(pendingToBorrowApi.size() > 0)
                pendingToBorrowApi.remove(0);

            BorrowDetailResponse res = ((BorrowDetailResponse) event.getResponse());

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

            int count = Realm.getDefaultInstance().where(BorrowAsset.class).equalTo("borrowno", res.getBorrowno()).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().size();
            Log.i("RetrofitClient", "newBorrowListAssets   " + res.getBorrowno() + " " + count);
            if (DOWNLOAD_ALL_FROM_LOGIN) {

                if (pendingToBorrowApi.size() > 0) {
                    RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, serverId, pendingToBorrowApi.get(0).getBorrowno()).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
                } else {
                    Log.i("RetrofitClient", "newBorrowListAssets done ");
                    if (DOWNLOAD_ALL_FROM_LOGIN) {
                        RetrofitClient.getSPGetWebService().renewStockTakeList(companyId, serverId).enqueue(new StocktakeListCallback(CONTINUOUS_STOCK_TAKE_API));
                    }
                }
            }

            if(!DOWNLOAD_ALL_FROM_LOGIN) {
                EventBus.getDefault().post(new HideLoadingEvent());
                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.download_success)));
            }
        }

        if (event.type == CONTINUOUS_STOCK_TAKE_API) {
            List<StocktakeList> res = ((List<StocktakeList>) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(StocktakeList.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < res.size(); i++) {

                try {
                    res.get(i).setStartDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(res.get(i).getStartDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    res.get(i).setEndDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(res.get(i).getEndDate()));
                    Date expected = res.get(i).getEndDateObj();
                    Calendar c = Calendar.getInstance();
                    c.setTime(expected);
                    c.add(Calendar.DATE, 1);  // number of days to add
                    expected = (c.getTime());  // dt is now the new date
                    res.get(i).setEndDateObj(expected);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                res.get(i).setCompanyid(companyId);
                res.get(i).setUserid(serverId);
                Realm.getDefaultInstance().insertOrUpdate(res.get(i));
            }

            Realm.getDefaultInstance().commitTransaction();


            List<StocktakeList> stocktakeLists =
                    Realm.getDefaultInstance().where(StocktakeList.class)
                            .greaterThanOrEqualTo("endDateObj", new Date())
                            .lessThanOrEqualTo("startDateObj", new Date())
                            .equalTo("companyid", companyId)
                            .equalTo("userid", serverId)
                            .findAll();

            for (int i = 0; i < stocktakeLists.size(); i++) {
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.mContext).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText(stocktakeLists.get(i).getStocktakeno() + " | " + stocktakeLists.get(i).getName());

                final int finalI = i;

                (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new ShowLoadingEvent());

                        String apiRoot =  (Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").endsWith("/")) ? Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") : Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").toString() + "/";
                        //RetrofitClient.api;//(if (RetrofitClient.api.startsWith("http://")) RetrofitClient.api else "http://" + RetrofitClient.api) +
                        if (apiRoot.endsWith("/")) {

                        } else {
                            apiRoot = apiRoot + "/";
                        }
                        String path = apiRoot + "MobileWebService.asmx/stockTakeListAsset?userid=" + serverId + "&companyid=" + companyId + "&orderno=" + stocktakeLists.get(finalI).getStocktakeno();
                        BaseUtils.serverCount = stocktakeLists.get(finalI).getTotal();
                        BaseUtils.count = 0;

                        Log.i("download", "stocktake " + path);

                        StockTakeListAdapter.downloadFileRetrofit(path,stocktakeLists.get(finalI).getStocktakeno() );

                        //RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, serverId, stocktakeLists.get(finalI).getStocktakeno()).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
                    }
                });

                ((ViewGroup) view.findViewById(R.id.stock_take_panel)).addView(linearLayout);
            }

            if(stocktakeLists.size() == 0) {
                ((ViewGroup)((ViewGroup) view.findViewById(R.id.stock_take_panel)).getParent()).setVisibility(View.GONE);
            }

            ((TextView)view.findViewById(R.id.stock_take_list_title)).setText(getString(R.string.stock_take_list) + " (" + stocktakeLists.size() + ")");
            ((TextView) view.findViewById(R.id.stock_take_download)).setText("+");

            ((ViewGroup) view.findViewById(R.id.stock_take_panel)).setVisibility(View.GONE);

            view.findViewById(R.id.stock_take_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((TextView) view.findViewById(R.id.stock_take_download)).getText().equals("-")) {
                        ((TextView) view.findViewById(R.id.stock_take_download)).setText("+");
                        ((ViewGroup) view.findViewById(R.id.stock_take_panel)).setVisibility(View.GONE);
                    } else {
                        ((TextView) view.findViewById(R.id.stock_take_download)).setText("-");
                        ((ViewGroup) view.findViewById(R.id.stock_take_panel)).setVisibility(View.VISIBLE);
                    }
                }
            });

            if (DOWNLOAD_ALL_FROM_LOGIN) {
                Log.i("RetrofitClient", "newSearchnoepc");

                RetrofitClient.getSPGetWebService().newSearchnoepc(companyId).enqueue(new SearchNoEpcCallback(CONTINUOUS_SEARCH_NO_EPC));
            }
        }

        if (event.type == CONTINUOUS_SEARCH_NO_EPC) {
            List<SearchNoEpcItem> res = ((List<SearchNoEpcItem>) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < res.size(); i++) {
                res.get(i).setCompanyid(companyId);
                res.get(i).setUserid(serverId);
                Realm.getDefaultInstance().insertOrUpdate(res.get(i));
            }

            if (!DOWNLOAD_ALL_FROM_LOGIN) {
                EventBus.getDefault().post(new HideLoadingEvent());
            }

            Realm.getDefaultInstance().commitTransaction();
            if (DOWNLOAD_ALL_FROM_LOGIN) {
                String apiRoot = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");

                if (apiRoot.endsWith("/")) {

                } else {
                    apiRoot = apiRoot + "/";
                }
                Log.i("RetrofitClient", "downloadFileRetrofit");

                downloadFileRetrofit(apiRoot + "MobileWebService.asmx/assetsDetail?userid=" + serverId + "&companyid=" + companyId + "&assetno=&lastcalldate=" + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, ""));

            } else {
                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.registration_list_downloaded)));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        Log.i("event", "event " + event.getTitle());
        setupLayout(event.getTitle());
    }

    public void setupLayout(String filter) {

        List<StocktakeList> stocktakeLists =
                Realm.getDefaultInstance().where(StocktakeList.class)
                        .greaterThanOrEqualTo("endDateObj", new Date())
                        .lessThanOrEqualTo("startDateObj", new Date())
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)
                        .beginGroup()
                        .contains("stocktakeno", filter)
                        .or()
                        .contains("name", filter)
                        .endGroup()
                        .findAll();

        ((ViewGroup) view.findViewById(R.id.stock_take_panel)).removeAllViews();

        for (int i = 0; i < stocktakeLists.size(); i++) {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.mContext).inflate(R.layout.download_upload_list_row, null);
            ((TextView) linearLayout.findViewById(R.id.text)).setText(stocktakeLists.get(i).getStocktakeno() + " | " + stocktakeLists.get(i).getName());

            final int finalI = i;

            (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new ShowLoadingEvent());

                    String apiRoot =  (Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").endsWith("/")) ? Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") : Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").toString() + "/";
                    //RetrofitClient.api;//(if (RetrofitClient.api.startsWith("http://")) RetrofitClient.api else "http://" + RetrofitClient.api) +
                    if (apiRoot.endsWith("/")) {

                    } else {
                        apiRoot = apiRoot + "/";
                    }
                    String path = apiRoot + "MobileWebService.asmx/stockTakeListAsset?userid=" + serverId + "&companyid=" + companyId + "&orderno=" + stocktakeLists.get(finalI).getStocktakeno();
                    BaseUtils.serverCount = stocktakeLists.get(finalI).getTotal();
                    BaseUtils.count = 0;

                    Log.i("download", "stocktake " + path);

                    StockTakeListAdapter.downloadFileRetrofit(path,stocktakeLists.get(finalI).getStocktakeno() );

                    //RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, serverId, stocktakeLists.get(finalI).getStocktakeno()).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
                }
            });

            ((ViewGroup) view.findViewById(R.id.stock_take_panel)).addView(linearLayout);
        }

        ((TextView)view.findViewById(R.id.stock_take_list_title)).setText(getString(R.string.stock_take_list) + " (" + stocktakeLists.size() + ")");
        ((TextView) view.findViewById(R.id.stock_take_download)).setText("+");

        ((ViewGroup) view.findViewById(R.id.stock_take_panel)).setVisibility(View.GONE);

        view.findViewById(R.id.stock_take_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((TextView) view.findViewById(R.id.stock_take_download)).getText().equals("-")) {
                    ((TextView) view.findViewById(R.id.stock_take_download)).setText("+");
                    ((ViewGroup) view.findViewById(R.id.stock_take_panel)).setVisibility(View.GONE);
                } else {
                    ((TextView) view.findViewById(R.id.stock_take_download)).setText("-");
                    ((ViewGroup) view.findViewById(R.id.stock_take_panel)).setVisibility(View.VISIBLE);
                }
            }
        });


        List<BorrowListItem> pendingToBorrowApi = Realm.getDefaultInstance().where(BorrowListItem.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", serverId)
                .equalTo("type", 1)
                .greaterThanOrEqualTo("validDateObj", new Date())
                .lessThanOrEqualTo("approvalDateObj", new Date())
                .beginGroup()
                .contains("borrowno", filter)
                .or()
                .contains("name", filter)
                .endGroup()
                .findAll();

        ((ViewGroup) view.findViewById(R.id.borrow_panel)).removeAllViews();
        for (int i = 0; i < pendingToBorrowApi.size(); i++) {
            Log.i("CONTINUOUS_BORROW_API_2", "CONTINUOUS_BORROW_API_2 " + i);
            final LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.mContext).inflate(R.layout.download_upload_list_row, null);
            ((TextView) linearLayout.findViewById(R.id.text)).setText(pendingToBorrowApi.get(i).getBorrowno() + " | " + pendingToBorrowApi.get(i).getName());

            final int finalI = i;

            (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new ShowLoadingEvent());

                    RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, serverId, pendingToBorrowApi.get(finalI).getBorrowno()).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
                }
            });

            ((ViewGroup) view.findViewById(R.id.borrow_panel)).addView(linearLayout);
        }
        ((TextView)view.findViewById(R.id.borrow_list_title)).setText(getString(R.string.borrow_list) + " (" + pendingToBorrowApi.size() + ")");
        ((TextView) view.findViewById(R.id.borrow_download)).setText("+");

        ((ViewGroup) view.findViewById(R.id.borrow_panel)).setVisibility(View.GONE);

        if(!DOWNLOAD_ALL_FROM_LOGIN) {
            //EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.download_success)));
        }



        view.findViewById(R.id.borrow_list).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((TextView) view.findViewById(R.id.borrow_download)).getText().equals("-")) {
                        ((TextView) view.findViewById(R.id.borrow_download)).setText("+");
                        ((ViewGroup) view.findViewById(R.id.borrow_panel)).setVisibility(View.GONE);
                    } else {
                        ((TextView) view.findViewById(R.id.borrow_download)).setText("-");
                        ((ViewGroup) view.findViewById(R.id.borrow_panel)).setVisibility(View.VISIBLE);
                    }
                }
            });

        List<DisposalListItem> pendingToDisposalApi = Realm.getDefaultInstance().where(DisposalListItem.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", serverId)
                .equalTo("type", 1)
                .greaterThanOrEqualTo("validDateObj", new Date())
                .lessThanOrEqualTo("approvalDateObj", new Date())

                .beginGroup()
                .contains("disposalNo", filter)
                .or()
                .contains("name", filter)
                .endGroup()
                .findAll();


        ((ViewGroup) view.findViewById(R.id.disposal_panel)).removeAllViews();
       for (int i = 0; i < pendingToDisposalApi.size(); i++) {
            // Log.i("CONTINUOUS_DISPOSAL_API_2", "CONTINUOUS_DISPOSAL_API_2 " + i);
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.mContext).inflate(R.layout.download_upload_list_row, null);
            ((TextView) linearLayout.findViewById(R.id.text)).setText(pendingToDisposalApi.get(i).getDisposalNo() + " | " + pendingToDisposalApi.get(i).getName());

            final int finalI = i;

            (linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new ShowLoadingEvent());

                    RetrofitClient.getSPGetWebService().newDisposalListAssets(companyId, serverId, pendingToDisposalApi.get(finalI).getDisposalNo()).enqueue(new DisposalDetailCallback(CONTINUOUS_DISPOSAL_DETAIL));
                }
            });

            ((ViewGroup) view.findViewById(R.id.disposal_panel)).addView(linearLayout);
        }
       ((TextView)view.findViewById(R.id.disposal_list_title)).setText(getString(R.string.disposal_list) + " (" + pendingToDisposalApi.size() + ")");
       ((TextView) view.findViewById(R.id.disposal_download)).setText("+");

       ((ViewGroup) view.findViewById(R.id.disposal_panel)).setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkInventoryDoneEvent dialogEvent) {
        /*
        RealmResults<LevelData> levelDataOld = (Realm.getDefaultInstance().where(LevelData.class).equalTo("type", 0).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());

        RealmResults<AssetsDetail> arrayList = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("userid", serverId).equalTo("companyid",companyId).distinct("category").sort("category").findAll();

        Realm.getDefaultInstance().beginTransaction();
        levelDataOld.deleteAllFromRealm();

        for(int i = 0; i < arrayList.size(); i++) {
            String[] value = arrayList.get(i).getCategory().split("->");
            String name = value[value.length - 1];
            String fatherNo = "";
            if(value.length > 1) {
                fatherNo = value[value.length - 2];
            }


            if(value.length == 3) {
                String greatFather = value[0];
                Log.i("original" , "original " + value[0] + " " + value[1] + " " + value[2]);

                LevelData levelData = new LevelData();
                levelData.setName(greatFather);
                levelData.setFatherNo("");
                levelData.setRono(greatFather);
                levelData.setType(0);
                levelData.setLevel(1);
                levelData.setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + greatFather + "_0_" + 1);
                levelData.setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + greatFather + "_0_" + 1);
                levelData.setOrdering(i);
                levelData.setCompanyid(companyId);
                levelData.setUserid(serverId);

                Realm.getDefaultInstance().insertOrUpdate(levelData);
                Log.i("rono" , "rono 1 " + greatFather + "[" + "]");


                LevelData levelData2 = new LevelData();
                levelData2.setName(fatherNo);
                levelData2.setFatherNo(greatFather);
                levelData2.setRono(fatherNo);
                levelData2.setType(0);
                levelData2.setLevel(2);
                levelData2.setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (greatFather != null ? greatFather + fatherNo : fatherNo) + "_0_" + 2);
                levelData2.setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (greatFather != null ? greatFather + fatherNo : fatherNo) + "_0_" + 2);
                levelData2.setOrdering(i);
                levelData2.setCompanyid(companyId);
                levelData2.setUserid(serverId);

                Log.i("rono" , "rono 2 " + fatherNo  + "[" + greatFather + "]");

                Realm.getDefaultInstance().insertOrUpdate(levelData2);
            }
            if(value.length == 2) {
                LevelData levelData = new LevelData();
                levelData.setName(fatherNo);
                levelData.setFatherNo("");
                levelData.setRono(fatherNo);
                levelData.setType(0);
                levelData.setLevel(1);
                levelData.setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + fatherNo + "_0_" + 1);
                levelData.setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + fatherNo + "_0_" + 1);
                levelData.setOrdering(i);
                levelData.setCompanyid(companyId);
                levelData.setUserid(serverId);

                Log.i("rono" , "rono " + 1 + " " + fatherNo + "[" + "]");

                Realm.getDefaultInstance().insertOrUpdate(levelData);
            }

            LevelData levelData = new LevelData();
            levelData.setName(name);
            levelData.setFatherNo(fatherNo);
            levelData.setRono(name);
            levelData.setType(0);
            levelData.setLevel(value.length);
            levelData.setSearchKey(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (fatherNo != null ? fatherNo + name : name) + "_0_" + value.length);
            levelData.setPk(companyId + serverId + "SP_LISTING_LEVEL_CACHE" + "_" + (fatherNo != null ? fatherNo + name : name) + "_0_" + value.length);
            levelData.setOrdering(i);
            levelData.setCompanyid(companyId);
            levelData.setUserid(serverId);

            Realm.getDefaultInstance().insertOrUpdate(levelData);

            Log.i("rono" , "rono " + value.length + "* " + name + "[" + fatherNo + "]");
        }
        Realm.getDefaultInstance().commitTransaction();
*/
        RealmResults<LevelData> levelDataOld = (Realm.getDefaultInstance().where(LevelData.class).equalTo("isNewData", false).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());

        for(int i = 0; i< levelDataOld.size(); i++) {
            Log.i("levelDataOld", "levelDataOld " + levelDataOld.get(i).getName() + " " + levelDataOld.get(i).isNewData());
        }
        Realm.getDefaultInstance().beginTransaction();
        //levelDataOld.deleteAllFromRealm();
        Realm.getDefaultInstance().commitTransaction();

        if(DOWNLOAD_ALL_FROM_LOGIN) {
            changeFragment(new SearchListFragment());
        } else {
            ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);
            EventBus.getDefault().post(new HideLoadingEvent());
        }
    }


    public void onPause() {
        super.onPause();
        //Realm.getDefaultInstance().commitTransaction();
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

                    if(DOWNLOAD_ALL_FROM_LOGIN) {
                        changeFragment(new SearchListFragment());
                    } else {
                        ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);
                        EventBus.getDefault().post(new HideLoadingEvent());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "error");

                if(DOWNLOAD_ALL_FROM_LOGIN) {
                    changeFragment(new SearchListFragment());
                } else {
                    ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);
                    EventBus.getDefault().post(new HideLoadingEvent());
                }
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

                    ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProgressEvent event) {
        Log.i("ProgressEvent", "ProgressEvent");
        ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.loading)+ " " + event.getCount() + "/" + event.getTotal() + "");
        ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
        ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
        ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(((float) event.getCount() / (float) event.getTotal() ) * 100) );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginDownloadProgressEvent event) {
        setProgress(event.getProgress());
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
}

