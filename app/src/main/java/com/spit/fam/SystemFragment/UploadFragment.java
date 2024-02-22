package com.spit.fam.SystemFragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.OfflineMode.BorrowAssets;
import com.spit.fam.Entity.OfflineMode.ChangeEpc;
import com.spit.fam.Entity.OfflineMode.DisposalAssets;
import com.spit.fam.Entity.OfflineMode.ReturnAssets;
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BorrowListRequest;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP2.BriefBorrowedList;
import com.spit.fam.Entity.SPEntityP2.PendingReturnAsset;
import com.spit.fam.Entity.SPEntityP2.PhotoUploadRequest;
import com.spit.fam.Entity.SpEntity.StockTakeDetail;
import com.spit.fam.Entity.SpEntity.StrJson;
import com.spit.fam.Entity.SpEntity.UploadStockTakeData;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.LoginDownloadProgressEvent;
import com.spit.fam.Event.ModifyAssetRequest;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Request.UpdateWaitingListRequest;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.WebService.Callback.GetBorrowListAssetCallback;
import com.spit.fam.WebService.Callback.GetBriefAssetCallback;
import com.spit.fam.WebService.Callback.GetBriefBorrowedAssetCallback;
import com.spit.fam.WebService.Callback.GetSPAssetListCallback;
import com.spit.fam.WebService.Callback.GetStockTakeListCallback;
import com.spit.fam.WebService.Callback.ImageReturnCallback;
import com.spit.fam.WebService.Callback.NewAssetDetailCallback;
import com.spit.fam.WebService.Callback.SPWebServiceCallback;
import com.spit.fam.WebService.Callback.UpdateAssetEpcCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.spit.fam.SystemFragment.DownloadFragment.BORROW_API_2;
import static com.spit.fam.SystemFragment.DownloadFragment.STOCK_TAKE_API;

public class UploadFragment extends BaseFragment {
    public HashMap<String, Integer> tempCountHashMap = new HashMap<>();

    public static int ASSET_DETAIL_API = 12;
    public static int RETURN_API = 10;

    LinearLayout borrow_panel;
    LinearLayout stock_take_panel;
    LinearLayout disposal_panel;

    TextView registrationUpload, borrowListUpload, stockTakeListUpload, disposalUpload;

    int registrationCount = 0;
    int changeCount = 0;

    int registrationAPIReturn = 0;
    int changeAPIReturn = 0;

    int registrationSuccess = 0;
    int changeSuccess = 0;

    String userId = Hawk.get(InternalStorage.Login.USER_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());
    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

    int apiReturnCount = 0;

    public boolean isMatchCount(ArrayList<BorrowListRequest> pendingDisposalRequest){
        Log.i("pendingDisposalRequest", "pendingDisposalRequest " + pendingDisposalRequest.size() + " " + apiReturnCount);

        if(pendingDisposalRequest.size() == apiReturnCount) {
            return true;
        }

        return false;
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

                ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.uploading) + " " + result + " %");
                ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
                ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
                ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(progress * 100));
            }

        });
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.upload_fragment, null);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.upload));

        borrow_panel = view.findViewById(R.id.borrow_panel);
        stock_take_panel = view.findViewById(R.id.stock_take_panel);
        disposal_panel = view.findViewById(R.id.disposal_panel);

        registrationUpload = view.findViewById(R.id.registration_download);
        borrowListUpload = view.findViewById(R.id.borrow_download);
        stockTakeListUpload = view.findViewById(R.id.stock_take_download);
        disposalUpload = view.findViewById(R.id.disposal_download);

        view.findViewById(R.id.add).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setVisibility(View.GONE);

        if(LoginFragment.SP_API) {
            //stockTakeListUpload.setVisibility(View.GONE);
        } else {
            stockTakeListUpload.setVisibility(View.VISIBLE);
        }

        //ArrayList<PendingReturnAsset> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, new ArrayList<PendingReturnAsset>());
        RealmResults<ReturnAssets> arrayList = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

        Log.i("ReturnAssets", "ReturnAssets " + arrayList.size());

        if(arrayList.size() > 0) {
            view.findViewById(R.id.return_list_wrapper).setVisibility(View.VISIBLE);

            view.findViewById(R.id.return_list_upload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    returnCount = 0;
                    originalReturnCount = arrayList.size();
                    Log.i("arrayList", "arrayList " +arrayList.size());

                    for(int i = 0; i < arrayList.size(); i++) {
                        Log.i("loop", "loop " + arrayList.get(i).getCompanyid() + " " + arrayList.get(i).getUserid() + " " + arrayList.get(i).getFirstlocation() + " " + arrayList.get(i).getLastlocation()+ " " + arrayList.get(i).getReturnList() );

                        RetrofitClient.getSPGetWebService().returnAsset(arrayList.get(i).getCompanyid(), arrayList.get(i).getUserid(),arrayList.get(i).getFirstlocation(),arrayList.get(i).getLastlocation(),arrayList.get(i).getReturnList()).enqueue(new UpdateAssetEpcCallback(3));//returnBorrowedAssetRequest).enqueue(new UpdateAssetEpcCallback());
                    }
                }
            });

            view.findViewById(R.id.return_list_wrapper).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {


                    new AlertDialog.Builder(getActivity())
                            .setTitle(getActivity().getString(R.string.app_name))
                            .setMessage(getString(R.string.confirm_deleting) + " " + getString(R.string.return_string)  + "?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //RealmResults<BorrowAssets> borrowListRequest = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("borrowno", orderno).equalTo("userid", userid).equalTo("companyid", companyId).findAll();
                                    RealmResults<ReturnAssets> arrayList = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

                                    //RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();
                                    Realm.getDefaultInstance().beginTransaction();
                                    arrayList.deleteAllFromRealm();
                                    Realm.getDefaultInstance().commitTransaction();

                                    Realm.getDefaultInstance().refresh();
                                    hideUIIfNeeded();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                    return false;
                }
            });
        } else {

            view.findViewById(R.id.return_list_wrapper).setVisibility(View.GONE);
        }

        view.findViewById(R.id.change_epc_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmResults<ChangeEpc> modifyAssetRequests = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll();

                //   final ArrayList<ModifyAssetRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                int count = modifyAssetRequests.size();

                for(int i = 0; i < modifyAssetRequests.size(); i++) {
                    final int pos = i;
                    String assetNo = modifyAssetRequests.get(i).getAssetno();

                    if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                        RetrofitClient.getSPGetWebService().changeEpc(modifyAssetRequests.get(i).getCompanyid(), modifyAssetRequests.get(i).getUserid(), modifyAssetRequests.get(i).getAssetno(), modifyAssetRequests.get(i).getEpc()).enqueue(
                                new Callback<List<APIResponse>>() {

                                    @Override
                                    public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                                        if(response.code() == 200) {
                                            if(response.body() != null && response.body().size() > 0) {
                                                //if(response.body().get(0).getStatus() == 0) {

                                                if(response.body().get(0).getStatus() == 0) {
                                                    RetrofitClient.getSPGetWebService().searchnoepc(Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).enqueue(new GetBriefAssetCallback(1));
                                                    RetrofitClient.getSPGetWebService().assetsList(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, "")).enqueue(new GetBriefAssetCallback(2));
                                                    RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, assetNo, "").enqueue(new NewAssetDetailCallback("1"));

                                                    //RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),  Hawk.get(InternalStorage.Login.USER_ID, ""), assetNo).enqueue(new GetSPAssetListCallback());
                                                }

                                                //}
                                                Log.i("pospos", "pospos " + pos + " " + modifyAssetRequests.size());
                                                if (pos == modifyAssetRequests.size() -1 ){
                                                    Realm.getDefaultInstance().beginTransaction();
                                                    modifyAssetRequests.deleteAllFromRealm();
                                                    Realm.getDefaultInstance().commitTransaction();
                                                }
                                            }
                                            hideUIIfNeeded();
                                        }
                                        ((MainActivity)getActivity()).updateDrawerStatus();
                                    }

                                    @Override
                                    public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                                    }
                                });
                    } else {
                        EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_internet)));
                    }
                }
            }
        });

        registrationUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ArrayList<ModifyAssetRequest> another = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                for(int i = 0; i < another.size(); i++) {
                    String assetNo = another.get(i).getAssetno();

                    RetrofitClient.getSPGetWebService().setEpc(another.get(i).getCompanyid(), another.get(i).getUserid(), another.get(i).getAssetno(), another.get(i).getEPC()).enqueue(new Callback<List<APIResponse>>() {

                        @Override
                        public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                            if(response.code() == 200) {
                                if(response.body() != null && response.body().size() > 0) {
                                    //if(response.body().get(0).getStatus() == 0) {

                                    if(response.body().get(0).getStatus() == 0) {

                                        RetrofitClient.getSPGetWebService().searchnoepc(Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).enqueue(new GetBriefAssetCallback(1));
                                        RetrofitClient.getSPGetWebService().assetsList(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, "")).enqueue(new GetBriefAssetCallback(2));

                                        //RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),  Hawk.get(InternalStorage.Login.USER_ID, ""), assetNo).enqueue(new GetSPAssetListCallback());
                                        RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, assetNo, "").enqueue(new NewAssetDetailCallback("1"));

                                    }

                                    for(int x = 0; x < another.size(); x++) {

                                        if(another.get(x).getAssetno().equals(assetNo) && another.get(x).setEPC) {
                                            another.remove(x);
                                            Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, another);
                                            hideUIIfNeeded();

                                            break;
                                        }
                                    }
                                    //}
                                }
                                ((MainActivity)getActivity()).updateDrawerStatus();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                        }
                    });
                }

                /*
                if(LoginFragment.SP_API) {

                    ArrayList<ModifyAssetRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                    registrationCount = modifyAssetRequests.size();


                    for(int i = 0; i < modifyAssetRequests.size(); i++) {
                        RetrofitClient.getSPGetWebService().changeEpc(modifyAssetRequests.get(i).getCompanyid(), modifyAssetRequests.get(i).getUserid(), modifyAssetRequests.get(i).getAssetno(), modifyAssetRequests.get(i).getEPC()).enqueue(new SPWebServiceCallback("2", modifyAssetRequests.get(i).getAssetno()));
                    }

                    ArrayList<ModifyAssetRequest> modifyAssetRequests2 = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                    changeCount = modifyAssetRequests2.size();


                    for(int i = 0; i < modifyAssetRequests2.size(); i++) {
                        RetrofitClient.getSPGetWebService().setEpc(modifyAssetRequests2.get(i).getCompanyid(), modifyAssetRequests2.get(i).getUserid(), modifyAssetRequests2.get(i).getAssetno(), modifyAssetRequests2.get(i).getEPC()).enqueue(new SPWebServiceCallback("1", modifyAssetRequests2.get(i).getAssetno()));
                    }

                } else {
                    ArrayList<ModifyAssetRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                    for(int i = 0; i < modifyAssetRequests.size(); i++) {
                        RetrofitClient.getAPIService().modifyAsset("" + modifyAssetRequests.get(i).getId(), modifyAssetRequests.get(i).getEPC()).enqueue(new UpdateAssetEpcCallback());
                    }

                    ArrayList<ModifyAssetRequest> modifyAssetRequests2 = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                    for(int i = 0; i < modifyAssetRequests2.size(); i++) {
                        RetrofitClient.getAPIService().modifyAsset("" + modifyAssetRequests2.get(i).getId(), modifyAssetRequests2.get(i).getEPC()).enqueue(new UpdateAssetEpcCallback());
                    }

                    EventBus.getDefault().post(new DialogEvent(getString(R.string.upload), getString(R.string.registration_list) + " " + getString(R.string.upload_success)));
                }

                // User user = Hawk.get(InternalStorage.OFFLINE_CACHE.USER, new LoginResponse()).getUser();
                //  RetrofitClient.getService().getAssetList(user.getUser_group().getId(), "").enqueue(new GetAssetListCallback());
            }*/
            }
        });

        borrowListUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<UpdateWaitingListRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BORROW_REQUEST, new ArrayList<UpdateWaitingListRequest>());

                for(int i = 0; i < modifyAssetRequests.size(); i++) {
                    RetrofitClient.getAPIService().borrowAsset(modifyAssetRequests.get(i)).enqueue(new UpdateAssetEpcCallback());
                }

                EventBus.getDefault().post(new DialogEvent(getString(R.string.upload), getString(R.string.borrow_list) + " " + getString(R.string.upload_success)));

            }
        });

        ArrayList<UploadStockTakeData> stockTakeList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
        int count = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).findAll().size();
        int distinctCount = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).distinct("stocktakeno").findAll().size();

        if(count > 0) {
            Log.i("distinctCount", "distinctCount " + distinctCount);

            view.findViewById(R.id.stock_take_list).setVisibility(View.VISIBLE);
            stockTakeListUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ArrayList<UploadStockTakeRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_REQUEST, new ArrayList<UploadStockTakeRequest>());
                    String strJson = "";

                    for (int i = 0; i < distinctCount; i++) {
                        //UploadStockTakeRequest uploadStockTakeRequest = new UploadStockTakeRequest();

                        //RetrofitClient.getAPIService().stockTakeAssets(modifyAssetRequests.get(i)).enqueue(new UpdateAssetEpcCallback());
                    }

                    EventBus.getDefault().post(new DialogEvent(getString(R.string.upload), getString(R.string.stock_take_list) + " " + getString(R.string.upload_success)));
                }

            });


        } else {
            view.findViewById(R.id.stock_take_list).setVisibility(View.GONE);
        }

        /*
        disposalUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<DisposalAssetsRequest> disposalAssetsRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_DISPOSAL, new ArrayList<DisposalAssetsRequest>());
                for(int i = 0; i < disposalAssetsRequests.size(); i++) {
                    RetrofitClient.getAPIService().disposalAssets(disposalAssetsRequests.get(i)).enqueue(new Callback<APIResponse>(){

                        @Override
                        public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                            Log.i("call", "call " + call.request().toString());

                            if(response.code() == 200) {
                                if(response.body().getStatus() == 0) {
                                }
                            } else {
                                EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                            }
                        }

                        @Override
                        public void onFailure(Call<APIResponse> call, Throwable t) {
                            Log.i("call", "call " + call.request().toString());
                            EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                        }
                    });
                }

                EventBus.getDefault().post(new DialogEvent(getString(R.string.upload), getString(R.string.disposal_list) + " " + getString(R.string.upload_success)));
            }
        });*/

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        //ArrayList<BorrowListRequest> borrowListRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST,new ArrayList<>());
        handleBorrowPanel();
        handleDisposalPanel();
        //ArrayList<BorrowListRequest> borrowListRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST,new ArrayList<>());



        view.findViewById(R.id.borrow_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((TextView) borrowListUpload).getText().equals("-")) {
                    ((TextView) borrowListUpload).setText("+");
                    borrow_panel.setVisibility(View.GONE);
                } else {
                    ((TextView) borrowListUpload).setText("-");
                    borrow_panel.setVisibility(View.VISIBLE);
                }
                //User user = Hawk.get(InternalStorage.OFFLINE_CACHE.USER, new LoginResponse()).getUser();
                //RetrofitClient.getService().getBorrowLists(user.getId()).enqueue(new GetBorrowListCallBack());
            }
        });

        view.findViewById(R.id.disposal_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((TextView) disposalUpload).getText().equals("-")) {
                    ((TextView) disposalUpload).setText("+");
                    disposal_panel.setVisibility(View.GONE);
                } else {
                    ((TextView) disposalUpload).setText("-");
                    disposal_panel.setVisibility(View.VISIBLE);
                }
            }
        });
        view.findViewById(R.id.stock_take_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((TextView) stockTakeListUpload).getText().equals("-")) {
                    ((TextView) stockTakeListUpload).setText("+");
                    stock_take_panel.setVisibility(View.GONE);
                } else {
                    ((TextView) stockTakeListUpload).setText("-");
                    stock_take_panel.setVisibility(View.VISIBLE);
                }
            }
        });

        view.findViewById(R.id.upload_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                    EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_internet)));
                    return;
                }
                upload();
/*
                final ArrayList<ModifyAssetRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                int count = modifyAssetRequests.size();

                for(int i = 0; i < modifyAssetRequests.size(); i++) {
                    int pos = i;
                    String assetNo = modifyAssetRequests.get(i).getAssetno();

                    ArrayList<ModifyAssetRequest> finalModifyAssetRequests = modifyAssetRequests;
                    RetrofitClient.getSPGetWebService().changeEpc(modifyAssetRequests.get(i).getCompanyid(), modifyAssetRequests.get(i).getUserid(), modifyAssetRequests.get(i).getAssetno(), modifyAssetRequests.get(i).getEPC()).enqueue(
                            new Callback<List<APIResponse>>() {

                                @Override
                                public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                                    if(response.code() == 200) {
                                        if(response.body() != null && response.body().size() > 0) {
                                            //if(response.body().get(0).getStatus() == 0) {
                                            ((MainActivity)getActivity()).updateDrawerStatus();

                                            for(int x = 0; x < modifyAssetRequests.size(); x++) {
                                                if(modifyAssetRequests.get(x).getAssetno().equals(assetNo) && modifyAssetRequests.get(x).changeEPC) {
                                                    finalModifyAssetRequests.remove(x);
                                                    Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, finalModifyAssetRequests);
                                                    hideUIIfNeeded();
                                                    break;
                                                }
                                            }
                                            //}
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                                }
                            });
                }
*/
                /*
                final ArrayList<ModifyAssetRequest> another = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                for(int i = 0; i < another.size(); i++) {
                    String assetNo = another.get(i).getAssetno();

                    RetrofitClient.getSPGetWebService().setEpc(another.get(i).getCompanyid(), another.get(i).getUserid(), another.get(i).getAssetno(), another.get(i).getEPC()).enqueue(new Callback<List<APIResponse>>() {

                        @Override
                        public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                            if(response.code() == 200) {
                                if(response.body() != null && response.body().size() > 0) {
                                    //if(response.body().get(0).getStatus() == 0) {
                                    ((MainActivity)getActivity()).updateDrawerStatus();

                                    for(int x = 0; x < another.size(); x++) {
                                        Log.i("another assetno", "another assetno " + another.get(x).getAssetno().equals(assetNo) + " " + another.get(x).setEPC);

                                        if(another.get(x).getAssetno().equals(assetNo) && another.get(x).setEPC) {
                                            another.remove(x);
                                            Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, another);
                                            hideUIIfNeeded();

                                            break;
                                        }
                                    }
                                    //}
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                        }
                    });
                }
*/
/*
                ArrayList<BorrowListRequest> borrowListRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST,new ArrayList<>());

                for(int i = 0; i < borrowListRequests.size(); i++) {
                    final int position = 0;
                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                    String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                    RetrofitClient.getSPGetWebService().borrowAssets(companyId, userid, borrowListRequests.get(position).getWaitiList(), borrowListRequests.get(position).getBorrowno()).enqueue(new Callback<APIResponse>(){

                        @Override
                        public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                            if(response.code() == 200) {
                                ((MainActivity)getActivity()).updateDrawerStatus();

                                if(response.body().getStatus() == 0) {

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<APIResponse> call, Throwable t) {

                        }
                    });
                }

*/
                /*
                ArrayList<UploadStockTakeData> uploadStockTakeDataList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
                ArrayList<String> uniqueStockTakeList = new ArrayList<>();
                ArrayList<String> uniqueStockTakeId = new ArrayList<>();

                EventBus.getDefault().post(new ShowLoadingEvent());

                ExecutorService schTaskEx = Executors.newFixedThreadPool(5000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {


                        for(int i = 0; i < uploadStockTakeDataList.size(); i++) {
                            if(!uniqueStockTakeId.contains(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo())) {
                                uniqueStockTakeId.add(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo());
                                uniqueStockTakeList.add(uploadStockTakeDataList.get(i).getStockTakeName());
                            }
                        }

                        for(int i = 0; i < uniqueStockTakeId.size(); i++) {
                            final int tempPos = i;

                            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                            String strJson = "";

                            ArrayList<UploadStockTakeData> data = new ArrayList<>();

                            for(int y = 0; y < uploadStockTakeDataList.size(); y++) {
                                if(uploadStockTakeDataList.get(y).getStrJsonObject().getOrderNo().equals(uniqueStockTakeId.get(tempPos))) {
                                    data.add(uploadStockTakeDataList.get(y));
                                }
                            }

                            for(int y = 0; y < data.size(); y++) {
                                companyId = data.get(y).getCompanyID();
                                if(y == 0) {
                                    strJson = "[";
                                }

                                strJson += data.get(y).getStrJson();

                                if(y != data.size() - 1) {
                                    strJson += ",";
                                } else {
                                    strJson += "]";
                                }
                                //
                            }


                            Log.i("strJson", "strJson "  + strJson);
                            RetrofitClient.getSPGetWebService().UploadStockTake(companyId,  strJson).enqueue(new SPWebServiceCallback(uniqueStockTakeId.get(tempPos)));

                            ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                            for(int x = 0; x < arrayList.size(); x++) {
                                if(arrayList.get(x).getOrderNo().equals(uniqueStockTakeId.get(tempPos)) && arrayList.get(x).getAssetNo().equals(uploadStockTakeDataList.get(tempPos).getStrJsonObject().getAssetNo())) {
                                    String finalCompanyId = companyId;
                                    int finalI = x;
                                    Glide.with(UploadFragment.this.getActivity())
                                            .asBitmap().load(arrayList.get(x).getFilePath()).into(new SimpleTarget<Bitmap>(500,500) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                            File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.png");
                                            try {
                                                f.createNewFile();

                                                Bitmap bitmap = resource;
                                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 70 , bos);
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

                                            Log.i("uploadFileToByte", "uploadFileToByte case5 " + finalCompanyId + " " + arrayList.get(finalI).getRono() + " " + arrayList.get(finalI).getFileLoc() + " " + arrayList.get(finalI).getUserId() + " "+  uniqueStockTakeId.get(tempPos));
                                            RetrofitClient.getSPGetWebService().uploadFileToByte(finalCompanyId, encodedString, "png", arrayList.get(finalI).getRono(), arrayList.get(finalI).getFileLoc(), arrayList.get(finalI).getUserId(), uniqueStockTakeId.get(tempPos)).enqueue(new ImageReturnCallback());

                                        }});

                                }
                            }
                        }
                        EventBus.getDefault().post(new HideLoadingEvent());
                    }
                });
,*/
            }
        });


        hideUIIfNeeded();

        return view;
    }

    public boolean uploadAll = false;
    public boolean uploadOne = false;

    public void upload() {
        Realm.getDefaultInstance().refresh();
        RealmResults<BorrowAssets> borrowListRequests = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll();
        RealmResults<DisposalAssets> disposalListRequests = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll();
        RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).distinct("stocktakeno").findAll();
        RealmResults<ChangeEpc> modifyAssetRequests = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll();
        RealmResults<ReturnAssets> returnAssets = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("userid", userid).findAll();

        int distinctCount = realmResults.size();

        Log.i("distinctCount", "distinctCount " + distinctCount);

        final int tempPos = 0;
        if(returnAssets.size() > 0) {
            RetrofitClient.getSPGetWebService().returnAsset(returnAssets.get(0).getCompanyid(), returnAssets.get(0).getUserid(), returnAssets.get(0).getFirstlocation(), returnAssets.get(0).getLastlocation(), returnAssets.get(0).getReturnList()).enqueue(new Callback<APIResponse>() {
                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                    if(response.code() == 200) {

                        Realm.getDefaultInstance().refresh();

                        Realm.getDefaultInstance().beginTransaction();
                        returnAssets.get(0).deleteFromRealm();
                        Realm.getDefaultInstance().commitTransaction();
                        Realm.getDefaultInstance().refresh();
                        upload();

                        hideUIIfNeeded();
                    }
                    ((MainActivity)getActivity()).updateDrawerStatus();

                }

                @Override
                public void onFailure(Call<APIResponse> call, Throwable t) {

                }
            });
                    /*.enqueue(
                    new Callback<List<APIResponse>>() {

                        @Override
                        public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                            if(response.code() == 200) {
                                if(response.body() != null && response.body().size() > 0) {
                                    if(response.body().get(0).getStatus() == 0) {
                                        Realm.getDefaultInstance().beginTransaction();
                                        //changeEpcClone.deleteFromRealm();
                                        Realm.getDefaultInstance().commitTransaction();
                                        upload();
                                    }
                                }
                                hideUIIfNeeded();
                            }
                            ((MainActivity)getActivity()).updateDrawerStatus();
                        }

                        @Override
                        public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                        }
                    });*/
            return;
        }

        if(modifyAssetRequests.size() > 0) {
            final ChangeEpc changeEpcClone =  (modifyAssetRequests.get(0));

            RetrofitClient.getSPGetWebService().changeEpc(modifyAssetRequests.get(0).getCompanyid(), modifyAssetRequests.get(0).getUserid(), modifyAssetRequests.get(0).getAssetno(), modifyAssetRequests.get(0).getEpc()).enqueue(
                    new Callback<List<APIResponse>>() {

                        @Override
                        public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                            if(response.code() == 200) {
                                if(response.body() != null && response.body().size() > 0) {
                                    if(response.body().get(0).getStatus() == 0) {
                                        Realm.getDefaultInstance().beginTransaction();
                                        changeEpcClone.deleteFromRealm();
                                        Realm.getDefaultInstance().commitTransaction();
                                        upload();
                                    }
                                }
                                hideUIIfNeeded();
                            }
                            ((MainActivity)getActivity()).updateDrawerStatus();
                        }

                        @Override
                        public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                        }
                    });

            return;
        }
         if(borrowListRequests.size() > 0) {

                final int position = 0;
                final int orderno = borrowListRequests.get(0).hashCode();
                final BorrowAssets uploadRequest =  borrowListRequests.get(0);

                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                Log.i("position", "position" + position + " " + borrowListRequests.size());

                BorrowAssets borrowAssets = null;
                for(int x = 0; x < borrowListRequests.size(); x++) {
                    if(borrowListRequests.get(x).hashCode() == (orderno)) {
                        borrowAssets = borrowListRequests.get(x);
                    }
                }

                BorrowAssets finalBorrowAssets = borrowAssets;
                RetrofitClient.getSPGetWebService().borrowAssets(companyId, serverId, borrowAssets.getBorrowList(), borrowAssets.getBorrowno()).enqueue(new Callback<APIResponse>() {

                    @Override
                    public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                        try {} catch (Exception e) {e.printStackTrace();}

                        Log.i("responseCode", "responseCode " + response.code() + " " + response.body().getStatus() );

                        if(response.code() == 200) {
                            Realm.getDefaultInstance().beginTransaction();
                            Log.i("position", "position" + position + " " + borrowListRequests.size());
                            try {
                                uploadRequest.deleteFromRealm();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Realm.getDefaultInstance().commitTransaction();
                            hideUIIfNeeded();
                            upload();

                        } else {
                            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                        }
                    }

                    @Override
                    public void onFailure(Call<APIResponse> call, Throwable t) {
                        Log.i("call", "call " + call.request().toString());
                        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                    }
                });
        } else if(disposalListRequests.size() > 0) {



                final int position = 0;
                final int orderno = disposalListRequests.get(position).hashCode();
                 final DisposalAssets uploadRequest =  disposalListRequests.get(0);


                        DisposalAssets borrowAssets = null;
                        for(int x = 0; x < disposalListRequests.size(); x++) {
                            if(disposalListRequests.get(x).hashCode() == (orderno)) {
                                borrowAssets = disposalListRequests.get(x);
                            }
                        }

                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                        String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                        DisposalAssets finalBorrowAssets = borrowAssets;
                        RetrofitClient.getSPGetWebService().disposalAssets(companyId, serverId, borrowAssets.getDisposalList(), borrowAssets.getDisposalNo()).enqueue(new Callback<APIResponse>() {

                            @Override
                            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                                if(response.code() == 200) {

                                    Realm.getDefaultInstance().beginTransaction();
                                    uploadRequest.deleteFromRealm();
                                    Realm.getDefaultInstance().commitTransaction();
                                    hideUIIfNeeded();

                                    upload();
                                } else {
                                    EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                                }
                            }

                            @Override
                            public void onFailure(Call<APIResponse> call, Throwable t) {
                                EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                            }
                        });

        } else if(distinctCount > 0) {
             final String stocktakeno = realmResults.get(tempPos).getStocktakeno();

             uploadAll = true;

             ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
             schTaskEx.execute(new Runnable() {
                 @Override
                 public void run() {

                     if(!((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                         EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_internet)));
                         return;
                     }
                     if(tempCountHashMap.get(stocktakeno) == null)
                         tempCountHashMap.put(stocktakeno, 0);

                     RealmResults<RealmStockTakeListAsset> upload = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
                     String strJson = "[";
                     ArrayList<StrJson> data = new ArrayList<>();

                     int lastPos = -1;

                     Log.i("ypos", "ypos " + tempCountHashMap.get(stocktakeno));

                     for(int y = tempCountHashMap.get(stocktakeno); y < upload.size() ; y++) {
                         StrJson object = new StrJson();
                         object.setAssetName(upload.get(y).getName());
                         object.setAssetNo(upload.get(y).getAssetno());
                         object.setBrand(upload.get(y).getBrand());
                         object.setCategoryName(upload.get(y).getCategory());
                         object.setEPC(upload.get(y).getEpc());
                         Log.i("foundType", "foundType " + upload.get(y).getFindType());


                         LoginDownloadProgressEvent loginDownloadProgressEvent = new LoginDownloadProgressEvent(y * 1.0f / upload.size() * 1.0f);
                         EventBus.getDefault().post(loginDownloadProgressEvent);

                         if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                             object.setFoundStatus(116);
                         }
                         if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                             object.setFoundStatus(117);
                         }
                         if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                             object.setFoundStatus(118);
                         }

                         object.setLocationName(upload.get(y).getLocation());
                         object.setModelNo(upload.get(y).getModel());
                         object.setQRCode("");
                         object.setRemarks(upload.get(y).getRemarks());

                         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                         try {
                             Log.i("scandate", "scandate " + object.getScanDate());

                             object.setScanDate(df.format(upload.get(y).getScanDateTime()));
                         } catch (Exception e) {
                             Log.i("scandate", "scandate2error " + e.getMessage());

                             e.printStackTrace();
                         }
                         object.setLoginID(upload.get(y).getUserName());
                         object.setOrderNo(upload.get(y).getStocktakeno());
                         if(upload.get(y).getStatusid() == 2) {
                             object.setStatusID(1);

                             if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                 object.setFoundStatus(116);
                             }
                             if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                                 object.setFoundStatus(117);
                             }
                             if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                                 object.setFoundStatus(118);
                             }
                         } else if(upload.get(y).getStatusid() == 10) {

                             if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                 object.setFoundStatus(116);
                             }

                         } else if(upload.get(y).getStatusid() == 9) {
                             object.setStatusID(2);
                         }

                         object.setUserName(upload.get(y).getUserName());
                         object.setUserid(upload.get(y).getUserId());



                         strJson += new Gson().toJson(object).toString().replace("\\u003e", ">");
                         Log.i("data", "datadata " + strJson);
                         strJson += ",";

                         Log.i("pic", "pic " + upload.size() + " "  + upload.get(y).getPic());

                         String rono = upload.get(y).getRono();

                         if(upload.get(y).getPic() != null && upload.get(y).getPic().length() > 0)
                             for(int z = 0; z < upload.get(y).getPic().split(",").length; z++) {
                                 String path = upload.get(y).getPic().split(",")[z];
                                 Log.i("path", "path " + path + "  " + path.startsWith("/storage"));

                                 if(path != null && path.startsWith("/storage")) {
                                     int finalZ = z;
                                     int finalY = y;

                                     ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             Glide.with(UploadFragment.this.getActivity())
                                                     .asBitmap().load(path).into(new SimpleTarget<Bitmap>(1000,1000) {
                                                 @Override
                                                 public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                                     File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.jpg");
                                                     try {
                                                         f.createNewFile();

                                                         Bitmap bitmap = resource;
                                                         ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                         bitmap.compress(Bitmap.CompressFormat.JPEG, 70 , bos);
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

                                                     Log.i("uploadFileToByte", "uploadFileToByte case3 " + companyId + " " + rono  + " " + finalZ + " " + object.getUserName() + " " + object.getOrderNo() + " " + encodedString);

                                                     RetrofitClient.getSPGetWebService().uploadFileToByte(companyId, encodedString, "png", rono, finalZ, object.getUserName(), object.getOrderNo()).enqueue(new ImageReturnCallback());

                                                 }
                                             });
                                         }
                                     });
                                 }
                             }

                         lastPos = y;
                         if(y % 500 == 0) {
                             // if(strJson.length() > 1) {
                             //  strJson = strJson.substring(0, strJson.length() - 1);
                             tempCountHashMap.put(stocktakeno, y+1);
                             break;
                             //}
                             // strJson += "]";
                             //RetrofitClient.getSPGetWebService().UploadStockTake(companyId, strJson).enqueue(new SPWebServiceCallback(lastPos + "_UPLOAD_STOCK_TAKE_" + stocktakeno));
                             //strJson = "[";
                         }
                     }

                     if(strJson.length() > 1)
                         strJson = strJson.substring(0, strJson.length() - 1);

                     strJson += "]";
                     Log.i("strJson","strJson " +lastPos  + " " + upload.size() + " " +strJson);
                     RealmResults<RealmStockTakeListAsset> upload22 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();

                     if(strJson.length() > 2) {
                         RetrofitClient.getSPGetWebService().UploadStockTake(companyId, strJson).enqueue(new SPWebServiceCallback(lastPos + "_UPLOAD_STOCK_TAKE_" + stocktakeno));
                     }
                     Log.i("strJson","strJson333 " + upload22.size());


                     Realm.getDefaultInstance().refresh();

                     RealmResults<RealmStockTakeListAsset> upload33 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
                     Log.i("strJson","strJson333 " + upload33.size());

                     Runnable runnable = new Runnable() {
                         @Override
                         public void run() {
                             handleStockTakePanel();

                         }
                     };

                     Handler handler = new Handler(Looper.getMainLooper());
                     handler.postDelayed(runnable, 0);


                     ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                     Log.i("arrayList", "arrayList " + arrayList.size());

                     for(int x = 0; x < arrayList.size(); x++) {
                         Log.i("arrayList", "arrayList " + arrayList.get(x).getOrderNo() + " " +stocktakeno);

                         if(arrayList.get(x).getOrderNo().equals(stocktakeno) ) {
                             String finalCompanyId = companyId;
                             int finalI = x;
                             Glide.with(UploadFragment.this.getActivity())
                                     .asBitmap().load(arrayList.get(x).getFilePath()).into(new SimpleTarget<Bitmap>(500,500) {
                                 @Override
                                 public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                     File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.png");
                                     try {
                                         f.createNewFile();

                                         Bitmap bitmap = resource;
                                         ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                         bitmap.compress(Bitmap.CompressFormat.PNG, 70 /*ignored for PNG*/, bos);
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

                                     RetrofitClient.getSPGetWebService().uploadFileToByte(finalCompanyId, encodedString, "png", arrayList.get(finalI).getRono(), arrayList.get(finalI).getFileLoc(), arrayList.get(finalI).getUserId(), arrayList.get(finalI).getOrderNo() ).enqueue(new ImageReturnCallback());

                                 }});

                         }
                     }

                 }
             });
         }

/*
        ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
        // schTaskEx.execute(new Runnable() {
        //     @Override
        //     public void run() {
        RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).distinct("stocktakeno").findAll();
        int distinctCount = realmResults.size();//Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).distinct("stocktakeno").findAll().size();

        for(int m = 0; m < distinctCount; m++) {
            String stocktakeno = realmResults.get(m).getStocktakeno();

            RealmResults<RealmStockTakeListAsset> upload = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
            String strJson = "[";
            ArrayList<StrJson> data = new ArrayList<>();

            for(int y = 0; y < upload.size(); y++) {
                StrJson object = new StrJson();
                object.setAssetName(upload.get(y).getName());
                object.setAssetNo(upload.get(y).getAssetno());
                object.setBrand(upload.get(y).getBrand());
                object.setCategoryName(upload.get(y).getCategory());
                object.setEPC(upload.get(y).getEpc());
                Log.i("foundType", "foundType " + upload.get(y).getFindType());


                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                    object.setFoundStatus(116);
                }
                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                    object.setFoundStatus(117);
                }
                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                    object.setFoundStatus(118);
                }

                object.setLocationName(upload.get(y).getLocation());
                object.setModelNo(upload.get(y).getModel());
                object.setQRCode("");
                object.setRemarks(upload.get(y).getRemarks());

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Log.i("scandate", "scandate " + object.getScanDate());

                    object.setScanDate(df.format(upload.get(y).getScanDateTime()));
                } catch (Exception e) {
                    Log.i("scandate", "scandate2error " + e.getMessage());

                    e.printStackTrace();
                }
                object.setLoginID(upload.get(y).getUserName());
                object.setOrderNo(upload.get(y).getStocktakeno());
                if(upload.get(y).getStatusid() == 2) {
                    object.setStatusID(1);

                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                        object.setFoundStatus(116);
                    }
                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                        object.setFoundStatus(117);
                    }
                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                        object.setFoundStatus(118);
                    }
                } else if(upload.get(y).getStatusid() == 10) {

                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                        object.setFoundStatus(116);
                    }

                } else if(upload.get(y).getStatusid() == 9) {
                    object.setStatusID(2);
                    object.setAssetName("");
                }

                object.setUserName(upload.get(y).getUserName());
                object.setUserid(upload.get(y).getUserId());


                if(object.getScanDate() == null) {
                    //object.setScanDate(df.format(new Date()));
                }
                strJson += new Gson().toJson(object).toString().replace("\\u003e", ">");
                Log.i("data", "datadata " + strJson);
                strJson += ",";

                Log.i("pic", "picpicpic "  + upload.get(y).getOtherRono());

                if(upload.get(y).getPic() != null && upload.get(y).getPic().length() > 0) {
                    Log.i("pic", "picpicpic "  + upload.get(y).getOtherRono());
                }

                String rono = upload.get(y).getOtherRono();

                if(upload.get(y).getPic() != null && upload.get(y).getPic().length() > 0)
                    for(int z = 0; z < upload.get(y).getPic().split(",").length; z++) {
                        String path = upload.get(y).getPic().split(",")[z];
                        Log.i("path", "path " + path + "  " + path.startsWith("/storage"));

                        if(path != null && path.startsWith("/storage")) {
                            int finalZ = z;
                            int finalY = y;

                            ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(UploadFragment.this.getActivity())
                                            .asBitmap().load(path).into(new SimpleTarget<Bitmap>(1000,1000) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                            File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.jpg");
                                            try {
                                                f.createNewFile();

                                                Bitmap bitmap = resource;
                                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70 , bos);
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
                                            Log.i("uploadFileToByte", "uploadFileToByte  case1 " + companyId + " " + rono  + " " + finalZ + " " + object.getUserName() + " " + object.getOrderNo());
                                            RetrofitClient.getSPGetWebService().uploadFileToByte(companyId, encodedString, "png", rono, finalZ, object.getUserName(), object.getOrderNo()).enqueue(new ImageReturnCallback());

                                        }
                                    });
                                }
                            });
                        }
                    }

                //object.setFoundStatus(upload.get(y).getFindType());

                if(y > 0 && y % 500 == 0) {

                    if(strJson.length() > 1)
                        strJson = strJson.substring(0, strJson.length() - 1);

                    strJson += "]";

                    String uploadStockTake = "";//""UPLOAD_STOCK_TAKE";
                    if(y == upload.size() - 1) {
                        uploadStockTake = "UPLOAD_STOCK_TAKE";
                    }
                    RetrofitClient.getSPGetWebService().UploadStockTake(companyId,  strJson).enqueue(new SPWebServiceCallback("UPLOAD_STOCK_TAKE_" + stocktakeno ));

                    strJson = "[";
                }
            }

            if(strJson.length() > 1)
                strJson = strJson.substring(0, strJson.length() - 1);

            strJson += "]";
            Log.i("strJson","strJson " + strJson);

            RealmResults<RealmStockTakeListAsset> upload22 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("stocktakeno", stocktakeno).findAll();


            RetrofitClient.getSPGetWebService().UploadStockTake(companyId,  strJson).enqueue(new SPWebServiceCallback("UPLOAD_STOCK_TAKE_" + stocktakeno));
            //RetrofitClient.getSPGetWebService().newStockTakeList(companyId, serverId).enqueue(new GetStockTakeListCallback(STOCK_TAKE_API));

            Log.i("strJson","strJson333 " + upload22.size());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().commitTransaction();

            //Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).equalTo("stocktakeno", realmResults.get(tempPos).getStocktakeno()).findAll().deleteAllFromRealm();
            Realm.getDefaultInstance().refresh();

            RealmResults<RealmStockTakeListAsset> upload33 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
            Log.i("strJson","strJson333 " + upload33.size());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    handleStockTakePanel();

                }
            };

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(runnable, 0);


            ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

            Log.i("arrayList", "arrayList " + arrayList.size());

            for(int x = 0; x < arrayList.size(); x++) {
                Log.i("arrayList", "arrayList " + arrayList.get(x).getOrderNo() + " " +stocktakeno);

                if(arrayList.get(x).getOrderNo().equals(stocktakeno) ) {
                    String finalCompanyId = companyId;
                    int finalI = x;
                    Glide.with(UploadFragment.this.getActivity())
                            .asBitmap().load(arrayList.get(x).getFilePath()).into(new SimpleTarget<Bitmap>(500,500) {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                            File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.png");
                            try {
                                f.createNewFile();

                                Bitmap bitmap = resource;
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 70, bos);
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

                            Log.i("uploadFileToByte", "uploadFileToByte case2 " + finalCompanyId + " " + arrayList.get(finalI).getAssetNo() + " " + arrayList.get(finalI).getFileLoc() + " " + arrayList.get(finalI).getUserId() + " "+  stocktakeno);
                            RetrofitClient.getSPGetWebService().uploadFileToByte(finalCompanyId, encodedString, "png", arrayList.get(finalI).getRono(), arrayList.get(finalI).getFileLoc(), arrayList.get(finalI).getUserId(), stocktakeno).enqueue(new ImageReturnCallback());

                        }});

                }
            }
        }

        RealmResults<ChangeEpc> modifyAssetRequests = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("userid", userid).findAll();

        for(int i = 0; i < modifyAssetRequests.size(); i++) {
            final int pos = i;
            String assetNo = modifyAssetRequests.get(i).getAssetno();

            if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                RetrofitClient.getSPGetWebService().changeEpc(modifyAssetRequests.get(i).getCompanyid(), modifyAssetRequests.get(i).getUserid(), modifyAssetRequests.get(i).getAssetno(), modifyAssetRequests.get(i).getEpc()).enqueue(
                        new Callback<List<APIResponse>>() {

                            @Override
                            public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
                                if(response.code() == 200) {
                                    if(response.body() != null && response.body().size() > 0) {
                                        //if(response.body().get(0).getStatus() == 0) {

                                        if(response.body().get(0).getStatus() == 0) {
                                            RetrofitClient.getSPGetWebService().searchnoepc(Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).enqueue(new GetBriefAssetCallback(1));
                                            RetrofitClient.getSPGetWebService().assetsList(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.Login.USER_ID, "")).enqueue(new GetBriefAssetCallback(2));
                                            RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, assetNo, "").enqueue(new NewAssetDetailCallback("1"));

                                            //RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),  Hawk.get(InternalStorage.Login.USER_ID, ""), assetNo).enqueue(new GetSPAssetListCallback());
                                        }

                                        //}
                                        Log.i("pospos", "pospos " + pos + " " + modifyAssetRequests.size());
                                        if (pos == modifyAssetRequests.size() -1 ){
                                            Realm.getDefaultInstance().beginTransaction();
                                            modifyAssetRequests.deleteAllFromRealm();
                                            Realm.getDefaultInstance().commitTransaction();
                                        }
                                    }
                                    hideUIIfNeeded();
                                }
                                ((MainActivity)getActivity()).updateDrawerStatus();
                            }

                            @Override
                            public void onFailure(Call<List<APIResponse>> call, Throwable t) {

                            }
                        });
            } else {
                EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_internet)));
            }
            //    }

        }

        RealmResults<ReturnAssets> arrayList = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).findAll();

        returnCount = 0;
        originalReturnCount = arrayList.size();
        for(int i = 0; i < arrayList.size(); i++) {
            RetrofitClient.getSPGetWebService().returnAsset(arrayList.get(i).getCompanyid(), arrayList.get(i).getUserid(),arrayList.get(i).getFirstlocation(),arrayList.get(i).getLastlocation(),arrayList.get(i).getReturnList()).enqueue(new UpdateAssetEpcCallback(3));//returnBorrowedAssetRequest).enqueue(new UpdateAssetEpcCallback());
        }

        RealmResults<BorrowAssets> borrowListRequests = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("userid", userid).findAll();

        for (int i = 0; i < borrowListRequests.size(); i++) {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
            final int position = i;
            RetrofitClient.getSPGetWebService().borrowAssets(companyId, serverId, borrowListRequests.get(i).getBorrowList(), borrowListRequests.get(i).getBorrowno()).enqueue(new Callback<APIResponse>() {

                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.i("responseCode", "responseCode " + response.code() + " " + response.body().getStatus());

                    if (response.code() == 200) {
                        //  RetrofitClient.getSPGetWebService().borrowListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), serverId, borrowListRequests.get(position).getBorrowno()).enqueue(new GetBorrowListAssetCallback());
                        EventBus.getDefault().post(new CallbackResponseEvent(response.body()));

                        Realm.getDefaultInstance().beginTransaction();
                        //borrowListRequests.remove(position);
                        if(position == borrowListRequests.size() - 1) {
                            borrowListRequests.deleteAllFromRealm();
                        }

                        Realm.getDefaultInstance().commitTransaction();
                        // Hawk.put(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST, borrowListRequests);
                        hideUIIfNeeded();

                        if (response.body().getStatus() == 0) {
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_success)));
                            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                        } else {
                            if (response.body().getStatus() == 1 && response.body().getBorrowCount() == 0) {

                                EventBus.getDefault().post(new DialogEvent(getString(R.string.fail), getString(R.string.lend_some).replace("x", "" + response.body().getBorrowCount())));
                            } else {
                                //RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                                //RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, assetNo, "").enqueue(new NewAssetDetailCallback("1"));
                                EventBus.getDefault().post(new CallbackFailEvent(getString(R.string.lend_some).replace("x", "" + response.body().getBorrowCount())));
                            }
                        }
                    } else {

                        EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                    }
                }

                @Override
                public void onFailure(Call<APIResponse> call, Throwable t) {
                    Log.i("call", "call " + call.request().toString());
                    EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                }
            });
        }

        RealmResults<DisposalAssets> disposalListRequests = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("userid", userid).findAll();

        for (int i = 0; i < disposalListRequests.size(); i++) {
            int finalI = i;
            RetrofitClient.getSPGetWebService().disposalAssets(companyId, serverId, disposalListRequests.get(i).getDisposalList(), disposalListRequests.get(i).getDisposalNo()).enqueue(new Callback<APIResponse>() {
                final int position = finalI;

                @Override
                public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.i("responseCode", "responseCode " + response.code() + " " + response.body().getStatus());

                    if (response.code() == 200) {
                        EventBus.getDefault().post(new CallbackResponseEvent(response.body()));

                        Realm.getDefaultInstance().beginTransaction();
                        if(position == disposalListRequests.size() - 1) {
                            disposalListRequests.deleteAllFromRealm();
                        }
                        Realm.getDefaultInstance().commitTransaction();
                        hideUIIfNeeded();

                        if (response.body().getStatus() == 0) {
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_success)));
                            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                        } else {
                            if (response.body().getStatus() == 1 && response.body().getBorrowCount() == 0) {
                                EventBus.getDefault().post(new DialogEvent(getString(R.string.fail), getString(R.string.lend_some).replace("x", "" + response.body().getBorrowCount())));
                            } else {
                                EventBus.getDefault().post(new CallbackFailEvent(getString(R.string.lend_some).replace("x", "" + response.body().getBorrowCount())));
                            }
                        }
                    } else {

                        EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                    }
                }

                @Override
                public void onFailure(Call<APIResponse> call, Throwable t) {
                    Log.i("call", "call " + call.request().toString());
                    EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                }
            });
        }*/
        // });
    }

    String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");

    public void hideUIIfNeeded() {
        RealmResults<ReturnAssets> arrayList = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

        if(arrayList.size() > 0) {
            view.findViewById(R.id.return_list_wrapper).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.return_list_wrapper).setVisibility(View.GONE);
        }


        ArrayList<UploadStockTakeData> stockTakeList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
        int count = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).findAll().size();

        if(count > 0) {
            view.findViewById(R.id.stock_take_list).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.stock_take_list).setVisibility(View.GONE);
        }

        //ArrayList<BorrowListRequest> borrowListRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST,new ArrayList<>());
        RealmResults<BorrowAssets> borrowListRequests = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

        if(borrowListRequests.size() > 0) {
            view.findViewById(R.id.borrow_list).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.borrow_list).setVisibility(View.GONE);
        }


        RealmResults<DisposalAssets> pendingDisposalRequest = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

        if(pendingDisposalRequest.size() == 0) {
            view.findViewById(R.id.disposal_list_wrapper).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.disposal_list_wrapper).setVisibility(View.VISIBLE);
        }
        RealmResults<ChangeEpc> changeEpcs = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll();

        ArrayList changeEpcList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<>());
        ArrayList bindEpcList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<>());
        Log.i("PENDING_CHANGE_EPC_REQUEST", "PENDING_CHANGE_EPC_REQUEST " + InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST + " " + changeEpcList.size());

        Log.i("changeEpcList", "changeEpcList " + changeEpcList.size() + " " + bindEpcList.size());
        view.findViewById(R.id.registration).setVisibility(View.GONE);

/*
        if(changeEpcs.size() > 0 || bindEpcList.size() > 0) {
            view.findViewById(R.id.registration).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.registration_list)).setText(getString(R.string.registration_list) + " (" + bindEpcList.size() + ")");

            view.findViewById(R.id.register_onclick).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(view.findViewById(R.id.register_panel).getVisibility() == View.VISIBLE) {
                        view.findViewById(R.id.register_panel).setVisibility(View.GONE);
                        ((TextView)view.findViewById(R.id.register_list_btn)).setText("+");
                    } else {
                        view.findViewById(R.id.register_panel).setVisibility(View.VISIBLE);
                        ((TextView)view.findViewById(R.id.register_list_btn)).setText("-");
                    }
                }
            });


            for (int i = 0; i < changeEpcs.size(); i++) {
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText((changeEpcs.get(i)).getAssetno() + " | " + (changeEpcs.get(i)).getEpc());
                ((TextView) linearLayout.findViewById(R.id.text)).setTextSize(14);
                ((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);

                ((ViewGroup) view.findViewById(R.id.register_panel)).addView(linearLayout);
            }
        } else {
            view.findViewById(R.id.registration).setVisibility(View.GONE);
        }*/

        if(changeEpcs.size() > 0) {
            ((TextView)view.findViewById(R.id.change_epc_text)).setText(getString(R.string.change_epc) + " (" + changeEpcs.size() + ")");

            view.findViewById(R.id.change_onclick).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(view.findViewById(R.id.change_panel).getVisibility() == View.VISIBLE) {
                        view.findViewById(R.id.change_panel).setVisibility(View.GONE);
                        ((TextView)view.findViewById(R.id.modify_bind_btn)).setText("+");
                    } else {
                        view.findViewById(R.id.change_panel).setVisibility(View.VISIBLE);
                        ((TextView)view.findViewById(R.id.modify_bind_btn)).setText("-");
                    }
                }
            });

            ((ViewGroup) view.findViewById(R.id.change_panel)).removeAllViews();

            for (int i = 0; i < changeEpcs.size(); i++) {
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText((changeEpcs.get(i)).getAssetno() + " | " + (changeEpcs.get(i)).getEpc());
                ((TextView) linearLayout.findViewById(R.id.text)).setTextSize(14);
                ((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);

                ((ViewGroup) view.findViewById(R.id.change_panel)).addView(linearLayout);
            }
            view.findViewById(R.id.change_epc).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.change_epc).setVisibility(View.GONE);
        }

        if(view.findViewById(R.id.change_epc).getVisibility() == View.GONE && view.findViewById(R.id.stock_take_list).getVisibility() == (View.GONE) && view.findViewById(R.id.registration).getVisibility() == (View.GONE) && view.findViewById(R.id.borrow_list).getVisibility() == (View.GONE) && view.findViewById(R.id.disposal_list_wrapper).getVisibility() == (View.GONE) && view.findViewById(R.id.return_list_wrapper).getVisibility() == (View.GONE)) {
            view.findViewById(R.id.upload_all).setVisibility(View.GONE);
            view.findViewById(R.id.no_data).setVisibility(View.VISIBLE);
            ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);

        } else {
            view.findViewById(R.id.no_data).setVisibility(View.GONE);
            view.findViewById(R.id.upload_all).setVisibility(View.VISIBLE);
            ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);

        }

        ((MainActivity)getActivity()).updateDrawerStatus();
    }

    public void onResume() {
        super.onResume();
        callAPI();
    }

    String encodedString = null;

    public void uploadStocktakeByNo(String stocktakeno, int lp) {
        Log.i("uploadStocktakeByNo", "uploadStocktakeByNo " + lp);
        ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
        schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {

                        if(!((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                            EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_internet)));
                            return;
                        }
                        RealmResults<RealmStockTakeListAsset> upload = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
                        String strJson = "[";
                        ArrayList<StrJson> data = new ArrayList<>();

                        int lastPos = -1;

                        for(int y = lp; y < upload.size() && y < lp + 500; y++) {
                            StrJson object = new StrJson();
                            object.setAssetName(upload.get(y).getName());
                            object.setAssetNo(upload.get(y).getAssetno());
                            object.setBrand(upload.get(y).getBrand());
                            object.setCategoryName(upload.get(y).getCategory());
                            object.setEPC(upload.get(y).getEpc());
                            Log.i("foundType", "foundType " + upload.get(y).getFindType());


                            LoginDownloadProgressEvent loginDownloadProgressEvent = new LoginDownloadProgressEvent(y * 1.0f / upload.size() * 1.0f);
                            EventBus.getDefault().post(loginDownloadProgressEvent);

                            if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                object.setFoundStatus(116);
                            }
                            if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                                object.setFoundStatus(117);
                            }
                            if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                                object.setFoundStatus(118);
                            }

                            object.setLocationName(upload.get(y).getLocation());
                            object.setModelNo(upload.get(y).getModel());
                            object.setQRCode("");
                            object.setRemarks(upload.get(y).getRemarks());

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Log.i("scandate", "scandate " + object.getScanDate());

                                object.setScanDate(df.format(upload.get(y).getScanDateTime()));
                            } catch (Exception e) {
                                Log.i("scandate", "scandate2error " + e.getMessage());

                                e.printStackTrace();
                            }
                            object.setLoginID(upload.get(y).getUserName());
                            object.setOrderNo(upload.get(y).getStocktakeno());
                            if(upload.get(y).getStatusid() == 2) {
                                object.setStatusID(1);

                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                    object.setFoundStatus(116);
                                }
                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                                    object.setFoundStatus(117);
                                }
                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                                    object.setFoundStatus(118);
                                }
                            } else if(upload.get(y).getStatusid() == 10) {

                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                    object.setFoundStatus(116);
                                }

                            } else if(upload.get(y).getStatusid() == 9) {
                                object.setStatusID(2);
                            }

                            object.setUserName(upload.get(y).getUserName());
                            object.setUserid(upload.get(y).getUserId());



                            strJson += new Gson().toJson(object).toString().replace("\\u003e", ">");
                            Log.i("data", "datadata " + strJson);
                            strJson += ",";

                            Log.i("pic", "pic " + upload.size() + " "  + upload.get(y).getPic());

                            String rono = upload.get(y).getRono();

                            if(upload.get(y).getPic() != null && upload.get(y).getPic().length() > 0)
                                for(int z = 0; z < upload.get(y).getPic().split(",").length; z++) {
                                    String path = upload.get(y).getPic().split(",")[z];
                                    Log.i("path", "path " + path + "  " + path.startsWith("/storage"));

                                    if(path != null && path.startsWith("/storage")) {
                                        int finalZ = z;
                                        int finalY = y;

                                        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Glide.with(UploadFragment.this.getActivity())
                                                        .asBitmap().load(path).into(new SimpleTarget<Bitmap>(1000,1000) {
                                                    @Override
                                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                                        File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.jpg");
                                                        try {
                                                            f.createNewFile();

                                                            Bitmap bitmap = resource;
                                                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70 , bos);
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

                                                        Log.i("uploadFileToByte", "uploadFileToByte case3 " + companyId + " " + rono  + " " + finalZ + " " + object.getUserName() + " " + object.getOrderNo() + " " + encodedString);

                                                        RetrofitClient.getSPGetWebService().uploadFileToByte(companyId, encodedString, "png", rono, finalZ, object.getUserName(), object.getOrderNo()).enqueue(new ImageReturnCallback());

                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                            lastPos = y;
                            if(y % 500 == 0) {
                                if(strJson.length() > 1)
                                    strJson = strJson.substring(0, strJson.length() - 1);

                                strJson += "]";
                                RetrofitClient.getSPGetWebService().UploadStockTake(companyId, strJson).enqueue(new SPWebServiceCallback(lastPos + "_UPLOAD_STOCK_TAKE_" + stocktakeno));
                                strJson = "[";
                            }
                        }

                        if(strJson.length() > 1)
                            strJson = strJson.substring(0, strJson.length() - 1);

                        strJson += "]";
                        Log.i("strJson","strJson " +lastPos  + " " + upload.size() + " " +strJson);
                        RealmResults<RealmStockTakeListAsset> upload22 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();

                        if(strJson.length() > 2) {
                            RetrofitClient.getSPGetWebService().UploadStockTake(companyId, strJson).enqueue(new SPWebServiceCallback(lastPos + "_UPLOAD_STOCK_TAKE_" + stocktakeno));
                        }
                        Log.i("strJson","strJson333 " + upload22.size());


                        Realm.getDefaultInstance().refresh();

                        RealmResults<RealmStockTakeListAsset> upload33 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
                        Log.i("strJson","strJson333 " + upload33.size());

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                handleStockTakePanel();

                            }
                        };

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(runnable, 0);


                        ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                        Log.i("arrayList", "arrayList " + arrayList.size());

                        for(int x = 0; x < arrayList.size(); x++) {
                            Log.i("arrayList", "arrayList " + arrayList.get(x).getOrderNo() + " " +stocktakeno);

                            if(arrayList.get(x).getOrderNo().equals(stocktakeno) ) {
                                String finalCompanyId = companyId;
                                int finalI = x;
                                Glide.with(UploadFragment.this.getActivity())
                                        .asBitmap().load(arrayList.get(x).getFilePath()).into(new SimpleTarget<Bitmap>(500,500) {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                        File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.png");
                                        try {
                                            f.createNewFile();

                                            Bitmap bitmap = resource;
                                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 70 /*ignored for PNG*/, bos);
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

                                        RetrofitClient.getSPGetWebService().uploadFileToByte(finalCompanyId, encodedString, "png", arrayList.get(finalI).getRono(), arrayList.get(finalI).getFileLoc(), arrayList.get(finalI).getUserId(), arrayList.get(finalI).getOrderNo() ).enqueue(new ImageReturnCallback());

                                    }});

                            }
                        }

                    }
                });
    }

    public void handleDisposalPanel() {
        RealmResults<DisposalAssets> disposalListRequests = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();


        Log.i("disposalListRequests", "disposalListRequests " + disposalListRequests.size());

        if(disposalListRequests.size() > 0) {
            view.findViewById(R.id.disposal_list).setVisibility(View.VISIBLE);

            ((ViewGroup) disposal_panel).removeAllViews();

            for (int i = 0; i < disposalListRequests.size(); i++) {
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.download)).setText(MainActivity.mContext.getString(R.string.upload));
                ((TextView) linearLayout.findViewById(R.id.text)).setText(disposalListRequests.get(i).getDisposalNo() + " | " + disposalListRequests.get(i).getName());
                //((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);
                // ((TextView) linearLayout.findViewById(R.id.download)).setText(getString(R.string.upload));
                final int position = i;
                final int orderno = disposalListRequests.get(i).hashCode();
                final String real_orderno = disposalListRequests.get(i).getDisposalNo();

                ((TextView) linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DisposalAssets borrowAssets = null;
                        for(int x = 0; x < disposalListRequests.size(); x++) {
                            if(disposalListRequests.get(x).hashCode() == (orderno)) {
                                borrowAssets = disposalListRequests.get(x);
                            }
                        }

                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                        String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                        DisposalAssets finalBorrowAssets = borrowAssets;
                        RetrofitClient.getSPGetWebService().disposalAssets(companyId, serverId, borrowAssets.getDisposalList(), borrowAssets.getDisposalNo()).enqueue(new Callback<APIResponse>() {

                            @Override
                            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                                try {
                                } catch (Exception e) {e.printStackTrace();}

                                Log.i("responseCode", "responseCode " + response.code() + " " + response.body().getStatus() );

                                if(response.code() == 200) {


                                    //RetrofitClient.getSPGetWebService().borrowListAssets(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), serverId, borrowListRequests.get(position).getBorrowno()).enqueue(new GetBorrowListAssetCallback());
                                    ((ViewGroup)((TextView) linearLayout.findViewById(R.id.download)).getParent()).setVisibility(View.GONE);
                                    EventBus.getDefault().post(new CallbackResponseEvent(response.body()));

                                    Realm.getDefaultInstance().beginTransaction();
                                    //borrowListRequests.remove(position);
                                    finalBorrowAssets.deleteFromRealm();
                                    Realm.getDefaultInstance().commitTransaction();
                                    //  Hawk.put(InternalStorage.OFFLINE_CACHE.SP_PENDING_BORROW_REQUEST, borrowListRequests);
                                    hideUIIfNeeded();

                                    if(response.body().getStatus() == 0) {
                                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_success)));
                                        RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                                    } else {
                                        if(response.body().getStatus() == 1 && response.body().getBorrowCount() == 0) {

                                            EventBus.getDefault().post(new DialogEvent(getString(R.string.fail), getString(R.string.disposal_some).replace("x", "" + response.body().getBorrowCount())));
                                        } else {
                                            //  RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                                            //RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, assetNo, "").enqueue(new NewAssetDetailCallback("1"));
                                            EventBus.getDefault().post(new CallbackFailEvent(getString(R.string.disposal_some).replace("x", "" + response.body().getBorrowCount())));
                                        }
                                    }
                                } else {

                                    EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                                }
                            }

                            @Override
                            public void onFailure(Call<APIResponse> call, Throwable t) {
                                Log.i("call", "call " + call.request().toString());
                                EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                            }
                        });
                    }
                });

                linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        new AlertDialog.Builder(getActivity())
                                .setTitle(getActivity().getString(R.string.app_name))
                                .setMessage(getString(R.string.confirm_deleting) + " " + getString(R.string.disposal) + " (" + real_orderno + ") ?")

                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //RealmResults<BorrowAssets> borrowListRequest = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("borrowno", orderno).equalTo("userid", userid).equalTo("companyid", companyId).findAll();
                                        RealmResults<DisposalAssets> disposalListRequest = Realm.getDefaultInstance().where(DisposalAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).equalTo("disposalNo", real_orderno).findAll();

                                        //RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();
                                        Realm.getDefaultInstance().beginTransaction();
                                        disposalListRequest.deleteAllFromRealm();
                                        Realm.getDefaultInstance().commitTransaction();

                                        Realm.getDefaultInstance().refresh();
                                        hideUIIfNeeded();
                                        handleDisposalPanel();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        return false;
                    }
                });
                ((ViewGroup) disposal_panel).addView(linearLayout);
            }
        } else {
            view.findViewById(R.id.disposal_list).setVisibility(View.GONE);
        }
    }
    public void handleBorrowPanel() {
        RealmResults<BorrowAssets> borrowListRequests = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

        Log.i("borrow", "borrow " + borrowListRequests.size() + " " + Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "") );

        if(borrowListRequests.size() > 0) {
            view.findViewById(R.id.borrow_list).setVisibility(View.VISIBLE);

            ((ViewGroup) borrow_panel).removeAllViews();

            for (int i = 0; i < borrowListRequests.size(); i++) {
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                ((TextView) linearLayout.findViewById(R.id.text)).setText(borrowListRequests.get(i).getBorrowno() + " | " + borrowListRequests.get(i).getName());
                //((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);
                ((TextView) linearLayout.findViewById(R.id.download)).setText(MainActivity.mContext.getString(R.string.upload));

                //((TextView) linearLayout.findViewById(R.id.download)).setText(getString(R.string.upload));
                final int position = i;
                final int orderno = borrowListRequests.get(i).hashCode();
                final String orderno_string = borrowListRequests.get(i).getBorrowno();

                ((TextView) linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                        String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                        Log.i("position", "position" + position + " " + borrowListRequests.size());

                        BorrowAssets borrowAssets = null;
                        for(int x = 0; x < borrowListRequests.size(); x++) {
                            if(borrowListRequests.get(x).hashCode() == (orderno)) {
                                borrowAssets = borrowListRequests.get(x);
                            }
                        }

                        BorrowAssets finalBorrowAssets = borrowAssets;
                        RetrofitClient.getSPGetWebService().borrowAssets(companyId, serverId, borrowAssets.getBorrowList(), borrowAssets.getBorrowno()).enqueue(new Callback<APIResponse>() {

                            @Override
                            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
                                try {
                                } catch (Exception e) {e.printStackTrace();}

                                Log.i("responseCode", "responseCode " + response.code() + " " + response.body().getStatus() );

                                if(response.code() == 200) {

                                    ((ViewGroup)((TextView) linearLayout.findViewById(R.id.download)).getParent()).setVisibility(View.GONE);
                                    EventBus.getDefault().post(new CallbackResponseEvent(response.body()));

                                    Realm.getDefaultInstance().beginTransaction();
                                    Log.i("position", "position" + position + " " + borrowListRequests.size());
                                    try {
                                        finalBorrowAssets.deleteFromRealm();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Realm.getDefaultInstance().commitTransaction();
                                    hideUIIfNeeded();

                                    if(response.body().getStatus() == 0) {
                                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_success)));
                                        RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));

                                    } else {
                                        if(response.body().getStatus() == 1 && response.body().getBorrowCount() == 0) {

                                            EventBus.getDefault().post(new DialogEvent(getString(R.string.fail), getString(R.string.lend_some).replace("x", "" + response.body().getBorrowCount())));
                                        } else {
                                            EventBus.getDefault().post(new CallbackFailEvent(getString(R.string.lend_some).replace("x", "" + response.body().getBorrowCount())));
                                        }
                                    }
                                } else {

                                    EventBus.getDefault().post(new CallbackFailEvent(response.message()));
                                }
                            }

                            @Override
                            public void onFailure(Call<APIResponse> call, Throwable t) {
                                Log.i("call", "call " + call.request().toString());
                                EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
                            }
                        });
                    }
                });

                linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        new AlertDialog.Builder(getActivity())
                                .setTitle(getActivity().getString(R.string.app_name))
                                .setMessage(getString(R.string.confirm_deleting) + " " + getString(R.string.borrow_list) + " (" + orderno_string + ") ?")

                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        RealmResults<BorrowAssets> borrowListRequest = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("borrowno", orderno_string).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

                                        //RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();
                                        Realm.getDefaultInstance().beginTransaction();
                                        borrowListRequest.deleteAllFromRealm();
                                        Realm.getDefaultInstance().commitTransaction();

                                        Realm.getDefaultInstance().refresh();
                                        hideUIIfNeeded();
                                        handleBorrowPanel();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                        return false;
                    }
                });
                ((ViewGroup) borrow_panel).addView(linearLayout);
            }
        } else {
            view.findViewById(R.id.borrow_list).setVisibility(View.GONE);
        }
    }
    public void handleStockTakePanel() {

        ((ViewGroup)stock_take_panel).removeAllViews();

        ArrayList<UploadStockTakeData> uploadStockTakeDataList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
        ArrayList<String> uniqueStockTakeList = new ArrayList<>();
        ArrayList<String> uniqueStockTakeId = new ArrayList<>();

        for(int i = 0; i < uploadStockTakeDataList.size(); i++) {
            if(!uniqueStockTakeId.contains(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo())) {
                uniqueStockTakeId.add(uploadStockTakeDataList.get(i).getStrJsonObject().getOrderNo());
                uniqueStockTakeList.add(uploadStockTakeDataList.get(i).getStockTakeName());
            }
        }

        RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).distinct("stocktakeno").findAll();
        int distinctCount = realmResults.size();//Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("tempStockTake", true).distinct("stocktakeno").findAll().size();

        Log.i("distinctCount", "distinctCount " + distinctCount);

        for(int i = 0; i < distinctCount; i++) {
            final int tempPos = i;
            final String stocktakeno = realmResults.get(i).getStocktakeno();

            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
            ((TextView)linearLayout.findViewById(R.id.text)).setText(realmResults.get(i).getStocktakeno() + " | " + realmResults.get(i).getStocktakename()/* uniqueStockTakeId.get(i) + " | " + uploadStockTakeDataList.get(i).getStockTakeName() */);

            ((TextView)linearLayout.findViewById(R.id.download)).setText(getString(R.string.upload));
            ((TextView)linearLayout.findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //uploadStocktakeByNo(stocktakeno, 0);


                    uploadAll = true;
                    uploadOne = true;

                    ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
                    schTaskEx.execute(new Runnable() {
                        @Override
                        public void run() {

                            if(!((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                                EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_internet)));
                                return;
                            }
                            if(tempCountHashMap.get(stocktakeno) == null)
                                tempCountHashMap.put(stocktakeno, 0);

                            RealmResults<RealmStockTakeListAsset> upload = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
                            String strJson = "[";
                            ArrayList<StrJson> data = new ArrayList<>();

                            int lastPos = -1;

                            Log.i("ypos", "ypos " + tempCountHashMap.get(stocktakeno));

                            for(int y = tempCountHashMap.get(stocktakeno); y < upload.size() ; y++) {
                                StrJson object = new StrJson();
                                object.setAssetName(upload.get(y).getName());
                                object.setAssetNo(upload.get(y).getAssetno());
                                object.setBrand(upload.get(y).getBrand());
                                object.setCategoryName(upload.get(y).getCategory());
                                object.setEPC(upload.get(y).getEpc());
                                Log.i("foundType", "foundType " + upload.get(y).getFindType());


                                LoginDownloadProgressEvent loginDownloadProgressEvent = new LoginDownloadProgressEvent(y * 1.0f / upload.size() * 1.0f);
                                EventBus.getDefault().post(loginDownloadProgressEvent);

                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                    object.setFoundStatus(116);
                                }
                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                                    object.setFoundStatus(117);
                                }
                                if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                                    object.setFoundStatus(118);
                                }

                                object.setLocationName(upload.get(y).getLocation());
                                object.setModelNo(upload.get(y).getModel());
                                object.setQRCode("");
                                object.setRemarks(upload.get(y).getRemarks());

                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                try {
                                    Log.i("scandate", "scandate " + object.getScanDate());

                                    object.setScanDate(df.format(upload.get(y).getScanDateTime()));
                                } catch (Exception e) {
                                    Log.i("scandate", "scandate2error " + e.getMessage());

                                    e.printStackTrace();
                                }
                                object.setLoginID(upload.get(y).getUserName());
                                object.setOrderNo(upload.get(y).getStocktakeno());
                                if(upload.get(y).getStatusid() == 2) {
                                    object.setStatusID(1);

                                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                        object.setFoundStatus(116);
                                    }
                                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("barcode")) {
                                        object.setFoundStatus(117);
                                    }
                                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("manual")) {
                                        object.setFoundStatus(118);
                                    }
                                } else if(upload.get(y).getStatusid() == 10) {

                                    if(upload.get(y).getFindType() != null && upload.get(y).getFindType().equals("rfid")) {
                                        object.setFoundStatus(116);
                                    }

                                } else if(upload.get(y).getStatusid() == 9) {
                                    object.setStatusID(2);
                                }

                                object.setUserName(upload.get(y).getUserName());
                                object.setUserid(upload.get(y).getUserId());



                                strJson += new Gson().toJson(object).toString().replace("\\u003e", ">");
                                Log.i("data", "datadata " + strJson);
                                strJson += ",";

                                Log.i("pic", "pic " + upload.size() + " "  + upload.get(y).getPic());

                                String rono = upload.get(y).getRono();

                                if(upload.get(y).getPic() != null && upload.get(y).getPic().length() > 0)
                                    for(int z = 0; z < upload.get(y).getPic().split(",").length; z++) {
                                        String path = upload.get(y).getPic().split(",")[z];
                                        Log.i("path", "path " + path + "  " + path.startsWith("/storage"));

                                        if(path != null && path.startsWith("/storage")) {
                                            int finalZ = z;
                                            int finalY = y;

                                            ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Glide.with(UploadFragment.this.getActivity())
                                                            .asBitmap().load(path).into(new SimpleTarget<Bitmap>(1000,1000) {
                                                                @Override
                                                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                                                    File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.jpg");
                                                                    try {
                                                                        f.createNewFile();

                                                                        Bitmap bitmap = resource;
                                                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70 , bos);
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

                                                                    Log.i("uploadFileToByte", "uploadFileToByte case3 " + companyId + " " + rono  + " " + finalZ + " " + object.getUserName() + " " + object.getOrderNo() + " " + encodedString);

                                                                    RetrofitClient.getSPGetWebService().uploadFileToByte(companyId, encodedString, "png", rono, finalZ, object.getUserName(), object.getOrderNo()).enqueue(new ImageReturnCallback());

                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    }

                                lastPos = y;
                                if(y % 500 == 0) {
                                    // if(strJson.length() > 1) {
                                    //  strJson = strJson.substring(0, strJson.length() - 1);
                                    tempCountHashMap.put(stocktakeno, y+1);
                                    break;
                                    //}
                                    // strJson += "]";
                                    //RetrofitClient.getSPGetWebService().UploadStockTake(companyId, strJson).enqueue(new SPWebServiceCallback(lastPos + "_UPLOAD_STOCK_TAKE_" + stocktakeno));
                                    //strJson = "[";
                                }
                            }

                            if(strJson.length() > 1)
                                strJson = strJson.substring(0, strJson.length() - 1);

                            strJson += "]";
                            Log.i("strJson","strJson " +lastPos  + " " + upload.size() + " " +strJson);
                            RealmResults<RealmStockTakeListAsset> upload22 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();

                            if(strJson.length() > 2) {
                                RetrofitClient.getSPGetWebService().UploadStockTake(companyId, strJson).enqueue(new SPWebServiceCallback(lastPos + "_UPLOAD_STOCK_TAKE_" + stocktakeno));
                            }
                            Log.i("strJson","strJson333 " + upload22.size());


                            Realm.getDefaultInstance().refresh();

                            RealmResults<RealmStockTakeListAsset> upload33 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", stocktakeno).findAll();
                            Log.i("strJson","strJson333 " + upload33.size());

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    handleStockTakePanel();

                                }
                            };

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(runnable, 0);


                            ArrayList<PhotoUploadRequest> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST, new ArrayList<PhotoUploadRequest>());

                            Log.i("arrayList", "arrayList " + arrayList.size());

                            for(int x = 0; x < arrayList.size(); x++) {
                                Log.i("arrayList", "arrayList " + arrayList.get(x).getOrderNo() + " " +stocktakeno);

                                if(arrayList.get(x).getOrderNo().equals(stocktakeno) ) {
                                    String finalCompanyId = companyId;
                                    int finalI = x;
                                    Glide.with(UploadFragment.this.getActivity())
                                            .asBitmap().load(arrayList.get(x).getFilePath()).into(new SimpleTarget<Bitmap>(500,500) {
                                                @Override
                                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                                    File f = new File(UploadFragment.this.getActivity().getCacheDir(), "asd.png");
                                                    try {
                                                        f.createNewFile();

                                                        Bitmap bitmap = resource;
                                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                        bitmap.compress(Bitmap.CompressFormat.PNG, 70 /*ignored for PNG*/, bos);
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

                                                    RetrofitClient.getSPGetWebService().uploadFileToByte(finalCompanyId, encodedString, "png", arrayList.get(finalI).getRono(), arrayList.get(finalI).getFileLoc(), arrayList.get(finalI).getUserId(), arrayList.get(finalI).getOrderNo() ).enqueue(new ImageReturnCallback());

                                                }});

                                }
                            }

                        }
                    });
                }
            });
            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(getActivity().getString(R.string.app_name))
                            .setMessage(getString(R.string.confirm_deleting) + " " + getString(R.string.stock_take) + " (" + stocktakeno + ") ?")

                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    RealmResults<RealmStockTakeListAsset> realmResults = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", stocktakeno).findAll();
                                    Realm.getDefaultInstance().beginTransaction();
                                    realmResults.deleteAllFromRealm();
                                    Realm.getDefaultInstance().commitTransaction();

                                    Realm.getDefaultInstance().refresh();
                                    hideUIIfNeeded();
                                    handleStockTakePanel();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                    return false;
                }
            });
            ((ViewGroup)stock_take_panel).addView(linearLayout);
        }
    }

    public void callAPI() {
        User user = Hawk.get(InternalStorage.OFFLINE_CACHE.USER, new LoginResponse()).getUser();
        if(LoginFragment.SP_API) {
            handleStockTakePanel();
        } else {
        }
    }


    public ArrayList<Asset> getAssetListFromBriefAssetList(List<BriefAsset> briefAssets) {
        ArrayList<Asset> assetArrayList = new ArrayList<>();
        if(briefAssets == null) {
            return assetArrayList;
        }

        for(int i = 0; i < briefAssets.size(); i++) {
            assetArrayList.add(convertBriefAssetToAsset(briefAssets.get(i)));
        }

        return assetArrayList;
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

    int returnCount = 0;
    int originalReturnCount = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackFailEvent failEvent) {
        ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("case", "case 1");

        if(event.getId() != null && event.getId().contains("UPLOAD_STOCK_TAKE_") ) {
            int lastPos = Integer.parseInt(event.getId().split("_")[0]);

            String result = (event.getId().split("_")[4]).replace("UPLOAD_STOCK_TAKE_", "");

            RealmResults<RealmStockTakeListAsset> upload22 = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("tempStockTake", true).equalTo("stocktakeno", result).findAll();

            Log.i("lastPos", "lastPos " + lastPos + " " + upload22.size() + " " +result);

            if(lastPos +1 == upload22.size()) {
                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", userid).equalTo("companyId", companyId).equalTo("stocktakeno", result).findAll().deleteAllFromRealm();
                upload22.deleteAllFromRealm();
                Realm.getDefaultInstance().commitTransaction();
                ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.GONE);

                Realm.getDefaultInstance().refresh();

                if(uploadOne){
                    uploadAll = false;
                    uploadOne = false;
                }
            }

            if(uploadAll) {
                upload();
            }
            //uploadStocktakeByNo(result, lastPos);

        }

        if(event.type == BORROW_API_2) {

            ExecutorService schTaskEx = Executors.newFixedThreadPool(1);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, event.getResponse());
                }
            });
        }

        if(event.type == STOCK_TAKE_API) {
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST, event.getResponse());

            handleStockTakePanel();
        }

        if (event.type == RETURN_API) {
            Log.i("RETURN_API", "RETURN_API " + event.getResponse());
            Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()) );

            for(int i = 0 ; i < ((List<BriefAsset>) event.getResponse()).size() ; i++) {
                RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, ((List<BriefAsset>) event.getResponse()).get(i).getAssetNo(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1"));
            }
        }

        if (event.type == 4) {
            Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
        }

        if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BriefBorrowedList) {
            if(event.type == 2) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, event.getResponse());
            }
        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof AssetsDetail) {
            Log.i("assetsDetail", "assetDetail " + ((List) event.getResponse()).size());

            for(int i = 0; i < ((List) event.getResponse()).size(); i++) {
                ArrayList<AssetsDetail> assetsDetails = new ArrayList<>();
                assetsDetails.add((AssetsDetail) ((List) event.getResponse()).get(i));

                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((AssetsDetail) ((List) event.getResponse()).get(i)).getAssetNo(), assetsDetails);
            }

            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ((AssetsDetail) ((List) event.getResponse()).get(0)).getAssetNo(), event.getResponse());
        } else if(event.getResponse() instanceof  APIResponse) {
            if(event.type == 3) {
                returnCount++;
                ((MainActivity)getActivity()).updateDrawerStatus();

                Log.i("returnCount", "returnCount " + returnCount + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, new ArrayList<PendingReturnAsset>()).size());
                RealmResults<ReturnAssets> arrayList = Realm.getDefaultInstance().where(ReturnAssets.class).equalTo("userid", userid).equalTo("companyid", companyId).findAll();

                if(returnCount == arrayList.size()) {
                    Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, new ArrayList<PendingReturnAsset>());

                    Realm.getDefaultInstance().beginTransaction();
                    arrayList.deleteAllFromRealm();
                    Realm.getDefaultInstance().commitTransaction();
                    hideUIIfNeeded();
                    ((MainActivity)getActivity()).updateDrawerStatus();

                    RetrofitClient.getSPGetWebService().returnList(companyId, serverId).enqueue(new GetBriefAssetCallback(RETURN_API));
                }

                if(returnCount == originalReturnCount) {
                    returnCount = 0;
                    originalReturnCount = 0;
                }
            }
        } else if(event.getResponse() instanceof StockTakeDetail) {

            ArrayList<Asset> assets = new ArrayList<>();
            for (int i = 0; i < ((StockTakeDetail) event.getResponse()).getTable().size(); i++) {
                assets.add(((StockTakeDetail) event.getResponse()).getTable().get(i).convertToAsset());
            }

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + event.getId(), event.getResponse());
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
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO, event.getResponse());
            } else if(DISPOSAL_NO != null) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + DISPOSAL_NO, event.getResponse());
            } else if(STOCK_TAKE_NO != null) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_STOCK_TAKE_LIST_NO_ + STOCK_TAKE_NO, event.getResponse());
            }

        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BriefAsset) {

            Log.i("event.getResponse", " event.getResponse " + event.type);

            if(event.type == 1) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
            } else if(event.type == 2) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
            }

        } else
        if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof APIResponse) {
            Log.i("case", "case 2 " + event.getId() + " " + event.getUrl());


            if(event.getUrl() != null) {
                Log.i("case", "case 3");

                if(event.getUrl().contains("UploadStockTake")) {
                    Log.i("case", "case 4");

                    ArrayList<UploadStockTakeData> uploadStockTakeDataList = Hawk.get(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, new ArrayList<UploadStockTakeData>());
                    ArrayList<UploadStockTakeData> newRaw = new ArrayList<>();


                    for(int x = 0; x < uploadStockTakeDataList.size(); x++) {
                        Log.i("data", "data " + uploadStockTakeDataList.get(x).getStrJsonObject().getOrderNo() + " " + event.getId());
                        if(uploadStockTakeDataList.get(x).getStrJsonObject().getOrderNo().equals(event.getId())) {

                        } else {
                            newRaw.add(uploadStockTakeDataList.get(x));
                        }
                    }

                    Hawk.put(InternalStorage.LocalStockTake.LOCAL_STOCK_TAKE_RECORD, newRaw);//new ArrayList<UploadStockTakeData>());
                    hideUIIfNeeded();

                    RetrofitClient.getSPGetWebService().stockTakeListAsset(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),serverId,  event.getId()).enqueue(new GetBorrowListAssetCallback());
                    RetrofitClient.getSPGetWebService().newStockTakeList(companyId, serverId).enqueue(new GetStockTakeListCallback(STOCK_TAKE_API));

                    /*String orderNo = uploadStockTakeDataList.get(Integer.parseInt(event.getId())).getStrJsonObject().getOrderNo();

                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

                    RetrofitClient.getSPGetWebService().stockTakeListDetail(
                            Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),
                            Hawk.get(InternalStorage.Login.USER_ID, ""),orderNo).enqueue(new GetStockTakeDetailCallback(
                            orderNo
                    ));*/
                } else if(event.getId().equals("2") || event.getId().equals("1")) {

                    if(event.getId().equals("2")) {
                        registrationAPIReturn++;

                        if(((ArrayList<APIResponse>)event.getResponse()).get(0).getStatus() == 0) {
                            registrationSuccess++;
                            RetrofitClient.getSPGetWebService().newAssetDetail(companyId,  serverId, event.getAssetNo(), Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1"));
                            //RetrofitClient.getSPGetWebService().assetDetail(companyId, userid, "", Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback("1"));

                        } else if(((ArrayList<APIResponse>)event.getResponse()).get(0).getStatus() == 1) {

                        }


                        if(registrationAPIReturn == registrationCount ) {
                            Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                            Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                            registrationAPIReturn = 0;
                            registrationCount = 0;
                            changeAPIReturn = 0;
                            changeCount = 0;

                            hideUIIfNeeded();

                            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

                            String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                            RetrofitClient.getSPGetWebService().assetsList(companyId,userid).enqueue(new GetBriefAssetCallback(2));
                            RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(1));

                            //changeSuccess  + " items, successfully change " + registrationSuccess
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.result), getString(R.string.binding_result).replace("x", changeSuccess + "").replace("y", registrationSuccess + "") ));
                        }

                    } else if(event.getId().equals("1")) {
                        changeAPIReturn++;

                        if(((ArrayList<APIResponse>)event.getResponse()).get(0).getStatus() == 0) {
                            changeSuccess++;
                            RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""),  Hawk.get(InternalStorage.Login.USER_ID, ""), event.getAssetNo()).enqueue(new GetSPAssetListCallback());

                        } else if(((ArrayList<APIResponse>)event.getResponse()).get(0).getStatus() == 1) {

                        }


                        if( changeAPIReturn == changeCount) {
                            Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
                            Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_BIND_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                            registrationAPIReturn = 0;
                            registrationCount = 0;
                            changeAPIReturn = 0;
                            changeCount = 0;

                            hideUIIfNeeded();

                            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

                            String userid = Hawk.get(InternalStorage.Login.USER_ID, "");

                            RetrofitClient.getSPGetWebService().assetsList(companyId,userid).enqueue(new GetBriefAssetCallback(2));
                            RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(1));

                            //changeSuccess  + " items, successfully change " + registrationSuccess
                            EventBus.getDefault().post(new DialogEvent(getString(R.string.result), getString(R.string.binding_result).replace("x", changeSuccess + "").replace("y", registrationSuccess + "") ));
                        }
                    }


                    //Log.i("registrationAPIReturn", "registrationAPIReturn " + registrationAPIReturn + " "+ changeAPIReturn + " " + ((ArrayList<APIResponse>)event.getResponse()).get(0).getStatus());
                    //Log.i("result", "result "+ registrationAPIReturn + " " + registrationCount + " " + changeAPIReturn + " " + changeCount);

                }
            } else {

            }
        }
        else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof Asset) {
            //EventBus.getDefault().post(new DialogEvent(getString(R.string.download), getString(R.string.registration_list) + " " + getString(R.string.download_success)));
        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof StockTakeList) {

            //((ViewGroup)stock_take_panel).removeAllViews();

            //for(int i = 0; i < ((List<StockTakeList>) event.getResponse()).size(); i++) {
            //   LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
            // ((TextView)linearLayout.findViewById(R.id.text)).setText(((List<StockTakeList>) event.getResponse()).get(i).getOrderNo() + " | "+ ((List<StockTakeList>) event.getResponse()).get(i).getName());
            //((TextView)linearLayout.findViewById(R.id.download)).setVisibility(View.GONE);
            // ((TextView)linearLayout.findViewById(R.id.download)).setText(getString(R.string.upload));

            //((ViewGroup)stock_take_panel).addView(linearLayout);
            //}

            //EventBus.getDefault().post(new DialogEvent(getString(R.string.download), getString(R.string.stock_take_list) + " " + getString(R.string.download_success)));
        } else if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof BorrowList) {
            if ((((List<BorrowList>) event.getResponse()).get(0)).getDisposal_status() != null) {

                ((ViewGroup)disposal_panel).removeAllViews();

                for(int i = 0; i < ((List<BorrowList>) event.getResponse()).size(); i++) {
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(UploadFragment.this.getActivity()).inflate(R.layout.download_upload_list_row, null);
                    ((TextView)linearLayout.findViewById(R.id.text)).setText( ((List<BorrowList>) event.getResponse()).get(i).getName());
                    ((TextView)linearLayout.findViewById(R.id.download)).setText(getString(R.string.upload));

                    ((ViewGroup)disposal_panel).addView(linearLayout);
                }

                //EventBus.getDefault().post(new DialogEvent(getString(R.string.download), getString(R.string.disposal_list) + " " + getString(R.string.download_success)));

            }
        }
    }
}

