package com.spit.fam.SystemFragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomMediaPlayer;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.OfflineMode.ReturnAssets;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP2.PendingReturnAsset;
import com.spit.fam.Entity.SpinnerOnClickEvent;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.Event.SubmitFailEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.InventoryRfidTask;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Request.LoginRequest;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.SimpleReaderListAdapter;
import com.spit.fam.WebService.Callback.GetBriefAssetCallback;
import com.spit.fam.WebService.Callback.GetLevelDataCallback;
import com.spit.fam.WebService.Callback.GetListingCallback;
import com.spit.fam.WebService.Callback.LoginCallback;
import com.spit.fam.WebService.Callback.UpdateAssetEpcCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.adapters.ReaderListAdapter;
import com.spit.fam.fragments.CommonFragment;
import com.spit.fam.fragments.ConnectionFragment;
import com.spit.cs108library4a.Cs108Library4A;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class ReturnFragment extends CommonFragment {
    private InventoryRfidTask inventoryRfidTask;
    public static int RETURN_API = 10;

    private View noResult;
    private ListView listView, rfidListView;
    private ListView return_listview;
    private ReaderListAdapter readerListAdapter;

    private AssetListAdapter assetListAdapter;
    private Button start;

    private ArrayList<Spinner> spinnerArrayList = new ArrayList<>();
    private ArrayList<ArrayList<LevelData>> locationLevelData = new ArrayList<>();

    public static int tabPosition = 0;

    String firstLocation = "";
    String lastLocation = "";

    String firstLocationString = "";
    String lastLocationString = "";

    private SwipeRefreshLayout swipeRefreshLayout;

    CustomMediaPlayer playerO, playerN;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        playerO = MainActivity.sharedObjects.playerO;
        playerN = MainActivity.sharedObjects.playerN;

        tabPosition = 0;

        view = LayoutInflater.from(getActivity()).inflate(R.layout.return_fragment, null);
        view.findViewById(R.id.add).setVisibility(View.GONE);

        view.findViewById(R.id.confirm).setVisibility(View.GONE);

        listView = view.findViewById(R.id.listview);
        rfidListView = view.findViewById(R.id.rfidlistview);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
                if(((MainActivity)getActivity()).isURLReachable() ){
                    RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
                    RetrofitClient.getSPGetWebService().returnList(companyId, userid).enqueue(new GetBriefAssetCallback(RETURN_API));
                } else {
                    ArrayList<Asset> myAsset =  Hawk.get(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());
                    EventBus.getDefault().post(new CallbackResponseEvent(myAsset));

                    ListingResponse listingResponse = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL);

                    Log.i("listing case", "listing case 3 " + listingResponse.getCatSize() + " " + listingResponse.getLocSize() + " " + myAsset.size());

                    ListingResponse newListingResponse = new ListingResponse();
                    newListingResponse.setCatSize(listingResponse.getCatSize());
                    newListingResponse.setLocSize(listingResponse.getLocSize());

                    EventBus.getDefault().post(new CallbackResponseEvent(newListingResponse));
                    //handleLocCat(listingResponse.getCatSize(), listingResponse.getLocSize());

                }
            }
        });

        boolean bSelect4detail = true;
        boolean needDupElim = true;

        boolean need4Extra1 = MainActivity.mCs108Library4a.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = ( false);

        readerListAdapter = new SimpleReaderListAdapter(getActivity(), R.layout.reader_list_item_clone, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);
        rfidListView.setAdapter(readerListAdapter);

        noResult = view.findViewById(R.id.no_result);
        start = view.findViewById(R.id.start);
        return_listview = view.findViewById(R.id.return_listview);

        view.findViewById(R.id.select_location_list).setOnClickListener(null);

        start.setVisibility(View.GONE);
        view.findViewById(R.id.confirm).setVisibility(View.GONE);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase());

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startStopHandler(false);
                if(start.getText().toString().equals(getString(R.string.start))) {
                    start.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();
                } else {
                    start.setText(getString(R.string.start));
                    ((MainActivity) MainActivity.mContext).stop();
                }
            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(getSelectedList() != null && getSelectedList().size() == 0) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.return_string), getString(R.string.nothing_selected)));
                    return;
                }

                if( ((MainActivity)getActivity()).isURLReachable() ) {
                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                    String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");
                    RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
                    //RetrofitClient.getSPGetWebService().returnList(companyId, userid).enqueue(new GetBriefAssetCallback(RETURN_API));
                }

                boolean started = false;
                if (inventoryRfidTask != null)
                    if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING)
                        started = true;

                if (started != false) {
                    MainActivity.mCs108Library4a.appendToLogView("CANCELLING. Set taskCancelReason");
                    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
                }

                view.findViewById(R.id.select_location_list).setVisibility(View.VISIBLE);

                //if(((MainActivity)getActivity()).isNetworkAvailable()) {
                Log.i("getSelectedList", "getSelectedList " + getSelectedList().size());

                if(getSelectedListString(getSelectedList()) == null && getSelectedListString(getSelectedList()).size() == 0) {
                   // Toast.makeText(getActivity(), "NOT SCANNED RECORD", Toast.LENGTH_LONG);
                }

                SimpleListAdapter simpleListAdapter = new SimpleListAdapter( getSelectedListString(getSelectedList()));
                ColorDrawable sage = new ColorDrawable(getActivity().getResources().getColor(android.R.color.darker_gray));
                return_listview.setDivider(sage);
                return_listview.setDividerHeight(1);

                return_listview.setAdapter(simpleListAdapter);
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });


        view.findViewById(R.id.borrow_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.select_location_list).setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.borrow_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firstLocation.length() == 0) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.return_string), getString(R.string.no_location_selected)));
                    return;
                }

                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");
                ArrayList<String> returnArrayList = new ArrayList<>();
                ArrayList<Asset> selectedList = getSelectedList();

                ArrayList<Asset> newResult = new ArrayList<>();
                for(int y = 0; y < result.size(); y++) {
                    newResult.add(result.get(y));
                }

                for(int i = 0; i < getSelectedList().size(); i++) {
                    returnArrayList.add(getSelectedList().get(i).getId());
                }

                for(int y = 0; y < result.size(); y++) {
                    for(int i = 0; i < getSelectedList().size(); i++) {
                        if(result.get(y).getAssetno().equals(getSelectedList().get(i).getAssetno())) {
                            resultEpc.remove(getSelectedList().get(i).getEPC());
                            newResult.remove(result.get(y));
                            Log.i("result", "result case " +  i + " " + y + " " + + newResult.size());

                        }
                    }
                }

                Log.i("result", "result new " + newResult.size());

                result.clear();
                for(int i = 0; i < newResult.size(); i++) {
                    result.add(newResult.get(i));
                }
                //result = newResult;
                Log.i("result", "result new " + result.size());

                String filterText = ((TextView)view.findViewById(R.id.edittext)).getText().toString();
                ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase() + " (" + filter(result, filterText).size() + ")");

                assetListAdapter.notifyDataSetChanged();

                Log.i("firstLocation", "firstLocation "+ firstLocation + " " + lastLocation + " " + firstLocationString + " " + returnArrayList);

                assetListAdapter.integerArrayList.clear();

                if(true) {

                    ArrayList<PendingReturnAsset> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, new ArrayList<PendingReturnAsset>());

                    PendingReturnAsset pendingReturnAsset = new PendingReturnAsset();
                    pendingReturnAsset.setCompanyId(companyId);
                    pendingReturnAsset.setUserid(userid);
                    pendingReturnAsset.setFirstLocation(firstLocation);
                    pendingReturnAsset.setLastLocation(lastLocation.isEmpty() ? firstLocation : lastLocation);
                    pendingReturnAsset.setReturnArrayList(returnArrayList);


                    for(int x = 0; x < arrayList.size(); x++) {
                        for(int y = 0; y < selectedList.size(); y++) {
                            if(arrayList.get(x).getReturnArrayList().contains(selectedList.get(y).getId())) {
                                arrayList.get(x).getReturnArrayList().remove(selectedList.get(y).getId());
                            }
                        }
                    }

                    arrayList.add(pendingReturnAsset);


                    view.findViewById(R.id.select_location_list).setVisibility(View.GONE);

                    if( ((MainActivity)getActivity()).isURLReachable() ) {
                        //Log.i("firstLoc", "firstLoc " + pendingReturnAsset.getFirstLocation());
                        //Log.i("lastLoc", "lastLoc " + pendingReturnAsset.getLastLocation());


                        Log.i("firstLoc", "firstLoc " + firstLocation);
                        Log.i("lastLoc", "lastLoc " + (lastLocation.isEmpty() ? firstLocation : lastLocation));

                        RetrofitClient.getSPGetWebService().returnAsset(companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, pendingReturnAsset.getReturnList()).enqueue(new UpdateAssetEpcCallback(companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, pendingReturnAsset.getReturnList()));//returnBorrowedAssetRequest).enqueue(new UpdateAssetEpcCallback());
                    } else {
                        //Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_RETURN_REQUEST, arrayList);
                        ReturnAssets returnAssets = new ReturnAssets();
                        returnAssets.setCompanyid(companyId);
                        returnAssets.setUserid(userid);
                        returnAssets.setFirstlocation(firstLocation);
                        returnAssets.setLastlocation(lastLocation.isEmpty() ? firstLocation : lastLocation);

                        String returnList = "";
                        for(int i = 0; i < returnArrayList.size(); i++) {
                            returnList += returnArrayList.get(i) + (i != returnArrayList.size() - 1 ? "," : "");
                        }
                        returnAssets.setReturnList(returnList);

                        returnAssets.setPk(companyId+userid+firstLocation+(lastLocation.isEmpty() ? firstLocation : lastLocation)+returnList);

                        Realm.getDefaultInstance().beginTransaction();
                        Realm.getDefaultInstance().insertOrUpdate(returnAssets);
                        Realm.getDefaultInstance().commitTransaction();

                        ((MainActivity)getActivity()).updateDrawerStatus();
                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_tips)));
                    }

                }

            }
        });

        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();

                if(tabPosition == 0) {
                    Log.i("getAsset", "getAsset " + getAsset());
                    view.findViewById(R.id.scan).setVisibility(View.GONE);
                    setupListView(getAsset());
                    //ArrayList<Asset> myAsset =  Hawk.get(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());
                    //EventBus.getDefault().post(new CallbackResponseEvent(myAsset));

                    start.setVisibility(View.GONE);
                    view.findViewById(R.id.confirm).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.scan).setVisibility(View.VISIBLE);
                    //setupReturnList();
                    setupListView(result);
                    view.findViewById(R.id.start).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.confirm).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        view.findViewById(R.id.scan).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });

        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());


        setLocationList(Hawk.get(InternalStorage.Search.LOCATION, new ArrayList()));

        ArrayList<Asset> myAsset =  Hawk.get(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());

        Log.i("side", "side " + myAsset.size());
        CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(myAsset);
        callbackResponseEvent.type = RETURN_API;

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                EventBus.getDefault().post(callbackResponseEvent);

                ListingResponse listingResponse = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL);

                if(listingResponse != null) {
                    Log.i("listing case", "listing case 3 " + listingResponse.getCatSize() + " " + listingResponse.getLocSize() + " " + myAsset.size());

                    ListingResponse newListingResponse = new ListingResponse();
                    newListingResponse.setCatSize(listingResponse.getCatSize());
                    newListingResponse.setLocSize(listingResponse.getLocSize());

                    EventBus.getDefault().post(new CallbackResponseEvent(newListingResponse));
                }

                setupListView(getAsset());
            }
        }, 10);

        if( ((MainActivity)getActivity()).isURLReachable() ) {
            //Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());

            //if(LoginFragment.SP_API) {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
            String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
            RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());

            RetrofitClient.getSPGetWebService().returnList(companyId, userid).enqueue(new GetBriefAssetCallback(RETURN_API));
            /*} else {
                User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
                String password = Hawk.get(InternalStorage.Login.PASSWORD, "");
                RetrofitClient.getService().login(new LoginRequest(user.getEmail(), password)).enqueue(new LoginCallback());
                RetrofitClient.getService().getLocationList().enqueue(new GetLocationListCallback());
            }*/
        } else {
            //handleLocCat(listingResponse.getCatSize(), listingResponse.getLocSize());

        }
    }

    public void setupReturnList() {
        String filterText = ((TextView)view.findViewById(R.id.edittext)).getText().toString();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        Log.i("event", "event 2314123123");
        if(((TabLayout)view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
            setupListView(getAsset());
        } else {
            setupReturnList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SubmitFailEvent event) {
        ReturnAssets returnAssets = new ReturnAssets();
        returnAssets.setCompanyid(event.getCompanyId());
        returnAssets.setUserid(event.getUserid());
        returnAssets.setFirstlocation(event.getFirstLocation());
        returnAssets.setLastlocation(event.getLastLocation().isEmpty() ? event.getFirstLocation() : event.getLastLocation());

        returnAssets.setReturnList(event.getReturnList());

        returnAssets.setPk(event.getCompanyId()+event.getUserid()+event.getFirstLocation()+(event.getLastLocation().isEmpty() ? event.getFirstLocation() : event.getLastLocation())+event.getReturnList());

        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().insertOrUpdate(returnAssets);
        Realm.getDefaultInstance().commitTransaction();

        ((MainActivity)getActivity()).updateDrawerStatus();
        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_tips)));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {
        Log.i("BarcodeScanEvent", "BarcodeScanEvent " + event.getBarcode());

        if (MainActivity.mCs108Library4a.isBleConnected() == false) {
        } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
        } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
        } else {
            startStopBarcodeHandler(false);
        }


        List<Asset> assets = getAsset();

        for(int i = 0; i < assets.size(); i++) {
            List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + assets.get(i).getAssetno(), null);

            Log.i("hihi", "hihi " + assetsDetail);

            if(assetsDetail != null && ( assetsDetail.get(0).getBarcode().equals(event.getBarcode()) || assetsDetail.get(0).getEpc().equals(event.getBarcode()) ) ) {

                Log.i("hihi", "hihi case 1 ");

                if(!resultEpc.contains(assets.get(i).getEPC()) && !resultEpc.contains(assets.get(i).getAssetno())) {
                    if(assets.get(i).getEPC() == null || assets.get(i).getEPC().length() == 0) {
                        resultEpc.add(assets.get(i).getAssetno());
                    } else {
                        resultEpc.add(assets.get(i).getEPC());
                    }
                    result.add(assets.get(i));

                    break;
                }
            } else if(assets.get(i).getAssetno().equals(event.getBarcode())) {

                Log.i("hihi", "hihi case 2 ");

                if(assets.get(i).getEPC() == null || assets.get(i).getEPC().length() == 0) {
                    resultEpc.add(assets.get(i).getAssetno());
                } else {
                    resultEpc.add(assets.get(i).getEPC());
                }
                result.add(assets.get(i));
                handleNoResult(result);
                assetListAdapter.notifyDataSetChanged();
                /*if(!searchedEPCList.contains(assets.get(i).getEPC()) && !searchedEPCList.contains(assets.get(i).getAssetno())) {
                    if(assets.get(i).getEPC() == null || assets.get(i).getEPC().length() == 0) {
                        searchedEPCList.add(assets.get(i).getAssetno());
                    } else {
                        searchedEPCList.add(assets.get(i).getEPC());
                    }
                    break;
                }*/
            }
        }
    }

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

    public boolean scannerOpen;
    public void openScanner() {
        new IntentIntegrator(getActivity())
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .setPrompt("")
                .setCameraId(0)
                .setBeepEnabled(true)
                .setBarcodeImageEnabled(true)
                .setCaptureActivity(CaptureActivityPortrait.class)
                .initiateScan();
    }


    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);


        if(LoginFragment.SP_API) {
            view.findViewById(R.id.sp_location).setVisibility(View.VISIBLE);
            view.findViewById(R.id.location_spinner).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.sp_location).setVisibility(View.GONE);
            view.findViewById(R.id.location_spinner).setVisibility(View.VISIBLE);
        }

        /*
        //0920A0000000000000000004
        ArrayList<String> arrayList = new ArrayList<>();
        //arrayList.add("0920A0000000000000000004");

        RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(arrayList);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(rfidDataUpdateEvent);
            }
        };

        new Handler().postDelayed(runnable, 2000);

         */

        return view;
    }

    public void setupListView(List<Asset> myasset) {
        String filterText = ((TextView)view.findViewById(R.id.edittext)).getText().toString();

        handleNoResult(filter(myasset, filterText));
        AssetListAdapter.WITH_EPC = true;

        if(assetListAdapter == null) {
            assetListAdapter = new AssetListAdapter(filter(myasset, filterText), true, getActivity());
            listView.setAdapter(assetListAdapter);
        } else {
            assetListAdapter.setData(filter(myasset, filterText), getActivity());
            listView.setAdapter(assetListAdapter);
        }

        if(filter(myasset, filterText).size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase() + " (" + filter(myasset, filterText).size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase());
    }

    public List<Asset> getAsset() {
        if(assets != null && assets.size() > 0) {
            return assets;
        }

        ArrayList<Asset> result = new ArrayList<>();
         result =  Hawk.get(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());

        assets = result;

         for(int i = 0; i < result.size(); i++) {
             if(result.get(i).getEPC().length() > 0)
                assetsEpc.add(result.get(i).getEPC());
         }

        /*
        if(!LoginFragment.SP_API) {
            User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
            ArrayList<BorrowListAsset> borrowLists = user.getBorrowed_assets();

            Log.i("getAsset", "getAsset " + borrowLists.size());

            for (int i = 0; i < borrowLists.size(); i++) {
                for (int y = 0; y < myasset.size(); y++) {
                    if (borrowLists.get(i).getId().equals(myasset.get(y).getId())) {
                        result.add(myasset.get(y));
                    }
                }
            }
        }
*/


        return result;
    }

    public void handleNoResult(List<Asset> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }

        if(data.size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase() + " (" + data.size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase());

    }

    public void onResume() {
        super.onResume();
        start();
        //if(!LoginFragment.SP_API)
        //    setupListView(getAsset());


        if(tabPosition == 0) {
            //searchedEPCList = new ArrayList<>();
        }

    }



    public Asset convertBriefAssetToAsset(BriefAsset briefAsset) {
        Asset asset = new Asset();

        asset.setId("" + briefAsset.getId());
        asset.setName(briefAsset.getName());
        asset.setAssetno(briefAsset.getAssetNo());
        asset.setBrand(briefAsset.getBrand());
        asset.setModel(briefAsset.getModel());
        asset.setEPC(briefAsset.getEpc());
        asset.setProsecutionNo(briefAsset.getProsecutionNo());

        asset.setReturndate(briefAsset.getReturnDate());

        Status status = new Status();

        Log.i("convert", "convert " + briefAsset.getOrverdue());

        if(briefAsset.getOverdue() != null && briefAsset.getOverdue()) {
            status.id = 9999;
        } else {
            status.id = -1;
        }
        asset.setStatus(status);

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
            assetArrayList.add(convertBriefAssetToAsset(briefAssets.get(i)));
        }

        return assetArrayList;
    }

    public void onPause(){
        super.onPause();
        onStop();
        stop();
        runnable = null;
        ((MainActivity)getActivity()).hideKeyboard(getActivity());
    }

    private boolean started = false;
    ArrayList<String> searchedEPCList = new ArrayList<>();

    private Handler handler = new Handler();
    public void stop() {
        started = false;
        //handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        //handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(started) {
                start();

                Log.i("tabPosition", "tabPosition " + tabPosition);

                Runnable r = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // your code here

                            if(assetListAdapter != null) {

                                ArrayList<Asset> result = new ArrayList<>();

                                for(int i = 0; i < searchedEPCList.size(); i++) {
                                    for(int y = 0; y < getAsset().size(); y++) {
                                        try {
                                            Log.i("data", "data " + getAsset().get(y).getEPC() + " " + searchedEPCList.get(i));

                                            if(getAsset().get(y).getEPC() != null && getAsset().get(y).getEPC().length() > 0) {
                                                if (getAsset().get(y).getEPC().equals(searchedEPCList.get(i))) {
                                                    result.add(getAsset().get(y));
                                                }
                                            } else {
                                                if (getAsset().get(y).getAssetno().equals(searchedEPCList.get(i))) {
                                                    result.add(getAsset().get(y));
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                //if( getActivity() != null)
                                try {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //setupListView(result);
                                            String filterText = ((TextView)view.findViewById(R.id.edittext)).getText().toString();


                                            if(tabPosition == 1) {

                                                if (result.size() > 0) {
                                                    handleNoResult(result);
                                                } else {
                                                    handleNoResult(result);
                                                }
                                                
                                                assetListAdapter.setData(filter(result, filterText), getActivity());

                                                if (assetListAdapter != null)
                                                    assetListAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                    }
                };

                Thread t = new Thread(r);
                t.start();
            }
        }
    };

    public void setSearchedEPCList(ArrayList<String> searchedEPCList) {
        for(int i = 0; i < searchedEPCList.size(); i++) {
            if(this.searchedEPCList.contains(searchedEPCList.get(i))) {

            } else {
                this.searchedEPCList.add(searchedEPCList.get(i));
            }
        }
        //this.searchedEPCList = searchedEPCList;
    }


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
                Log.i(TAG, "HelloK: Find E2806894 with MainActivity.mDid = " + MainActivity.mDid);
                if (MainActivity.mDid.matches("E2806894A")) {
                    Log.i(TAG, "HelloK: Find E2806894A");
                    MainActivity.mCs108Library4a.setInvBrandId(false);
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
                } else if (MainActivity.mDid.matches("E2806894B")) {
                    Log.i(TAG, "HelloK: Find E2806894B");
                    MainActivity.mCs108Library4a.setInvBrandId(false);
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 1, 0x203, "1", true);
                    bNeedSelectedTagByTID = false;
                } else if (MainActivity.mDid.matches("E2806894C")) {
                    Log.i(TAG, "HelloK: Find E2806894C");
                    MainActivity.mCs108Library4a.setInvBrandId(true);
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 1, 0x204, "1", true);
                    bNeedSelectedTagByTID = false;
                }
            } else if (mDid.indexOf("E28011") == 0) bNeedSelectedTagByTID = false;
            Log.i(TAG, "HelloK: going to setSelectedTagByTID with mDid = " + mDid + " with extra1Bank = " + extra1Bank + ", extra2Bank = " + extra2Bank + ", bNeedSelectedTagByTID = " + bNeedSelectedTagByTID );// ", bMultiBank = " + bMultiBank);
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


    @Override
    public void onDestroy() {
        MainActivity.mCs108Library4a.setNotificationListener(null);
        if (inventoryRfidTask != null) {
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onDestory(): VALID inventoryRfidTask");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
        }
        resetSelectData();
        MainActivity.mCs108Library4a.setVibrateTime(MainActivity.mCs108Library4a.getVibrateTime());
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onDestory(): onDestory()");
        super.onDestroy();
    }

    ArrayList<Asset> result = new ArrayList<>();
    ArrayList<String> resultEpc = new ArrayList<>();

    ArrayList<Asset> assets = new ArrayList<>();
    ArrayList<String> assetsEpc = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {

        if(playerN != null)
            playerN.start();
        if(playerO != null)
            playerO.start();

        for(int x = 0; x < event.getData().size(); x++) {

            if(!resultEpc.contains(event.getData().get(x)) && assetsEpc.contains(event.getData().get(x)) ) {
                for(int y = 0; y < assets.size(); y++) {
                    if(assets.get(y).getEPC().equals(event.getData().get(x))) {
                        result.add(assets.get(y));
                        resultEpc.add(assets.get(y).getEPC());
                    }
                }

                if(((TabLayout)view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 1) {
                    handleNoResult(result);
                }
                assetListAdapter.notifyDataSetChanged();
            }
        }

        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
    }

    public void setSpinner(List<LevelData> categoryList, Spinner spinner, int layer, int type) {
        List<String> location = new ArrayList<>();
        location.add("-");

        for(int i = 0; i < categoryList.size(); i++)
            location.add(categoryList.get(i).getName());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                for (int i = layer; i < spinnerArrayList.size(); i++) {
                    setSpinner(new ArrayList<>(), spinnerArrayList.get(i), i, type);
                }

                if(position != 0) {
                    EventBus.getDefault().post(new SpinnerOnClickEvent(layer, type, categoryList.get(position - 1).getRono()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        /*
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new SpinnerOnClickEvent(layer, type, categoryList.get(position).getRono()));
            }
        });*/
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SpinnerOnClickEvent event) {

        if(!MainActivity.OFFLINE_MODE) {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            RetrofitClient.getSPGetWebService().listing(companyId, event.getFatherno(), event.getType() + "").enqueue(new GetLevelDataCallback(event.getType(), event.getLayer() + 1));
        } else {
            if(event.getLayer() == 1) {
                firstLocation = event.getFatherno();
                lastLocation = "";
            } else {
                lastLocation = event.getFatherno();
            }
            Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent " + event.getLayer() + " " + firstLocation + " " + lastLocation + " " + event.getLayer());

            if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1)) != null) {
                //EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1))));
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1)));
                callbackResponseEvent.type = event.getType();
                callbackResponseEvent.level = (event.getLayer() + 1);
                callbackResponseEvent.setFatherno(event.getFatherno());

                EventBus.getDefault().post(callbackResponseEvent);
            }
        }
    }

    public  float convertDpToPixel(float dp){
        return dp * ((float) getActivity().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    List<Location> lcoationList;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        swipeRefreshLayout.setRefreshing(false);

        if (event.type == RETURN_API) {
            Log.i("RETURN_API", "RETURN_API " + event.getResponse());
            Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
        }

        Log.i("event", "event " + event.getResponse().toString());
        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == LevelData.class) {
            ArrayList<LevelData> levelData = (ArrayList<LevelData>) event.getResponse();

            for(int i = event.level ; i < spinnerArrayList.size() - 1; i++) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ReturnFragment.this.getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i).setAdapter(arrayAdapter);
            }

            try {
                locationLevelData.subList(event.level, locationLevelData.size()).clear();
            } catch (Exception e) {
            }
            locationLevelData.add( levelData);

            setSpinner(levelData, spinnerArrayList.get(event.level - 1), event.level, event.type);

        } else if(event.getResponse() instanceof ListingResponse) {
            int locationSize = ((ListingResponse)event.getResponse()).getLocSize();
            Log.i("locationSize", "locationSize " + locationSize);
            ViewGroup spinnerRoot = (ViewGroup) view.findViewById(R.id.sp_location);
            spinnerRoot.removeAllViews();
            spinnerArrayList.clear();

            for(int i = 0; i < locationSize; i++) {
                Spinner spinner = new Spinner(ReturnFragment.this.getActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)convertDpToPixel(10);
                layoutParams.bottomMargin = (int)convertDpToPixel(10);

                spinner.setLayoutParams(layoutParams);

                spinnerRoot.addView(spinner);

                LinearLayout ll = new LinearLayout(getActivity());
                ll.setBackgroundColor(Color.parseColor("#C9CACA"));
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(1));
                layoutParams.topMargin = (int)convertDpToPixel(2);

                if(i + 1 < locationSize)
                    layoutParams.bottomMargin = (int)convertDpToPixel(2);

                ll.setLayoutParams(layoutParams);

                spinnerRoot.addView(ll);

                spinnerArrayList.add(spinner);
            }
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

            if(!MainActivity.OFFLINE_MODE) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback(1, 1));
            } else {

                if (Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1") != null) {
                    CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1"));
                    callbackResponseEvent.type = 1;
                    callbackResponseEvent.level = 1;
                    callbackResponseEvent.setFatherno("");
                    callbackResponseEvent.setResponse((Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1")));

                    EventBus.getDefault().post(callbackResponseEvent);

                }
            }
        } else if(event.getResponse() instanceof APIResponse) {
            Log.i("APIResponse", "APIResponse 1 " + ((APIResponse) event.getResponse()).getStatus());

            if(LoginFragment.SP_API) {
                onResume();

                if (((APIResponse) event.getResponse()).getStatus() == 0) {
                    searchedEPCList.clear();
                    setupReturnList();

                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                    String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
                    RetrofitClient.getSPGetWebService().returnList(companyId, userid).enqueue(new GetBriefAssetCallback(RETURN_API));
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.return_to_location), getString(R.string.return_success)));
                } else {
                    if(((APIResponse) event.getResponse()).getReturnCount() > 0) {
                        searchedEPCList.clear();
                        setupReturnList();

                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                        String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
                        RetrofitClient.getSPGetWebService().returnList(companyId, userid).enqueue(new GetBriefAssetCallback(RETURN_API));
                    }

                    EventBus.getDefault().post(new DialogEvent(getString(R.string.return_to_location), getString(R.string.return_some).replace("x", ((APIResponse) event.getResponse()).getReturnCount() +"" )));
                }
            } else {
                if (((APIResponse) event.getResponse()).getStatus() == 0) {
                    User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
                    String password = Hawk.get(InternalStorage.Login.PASSWORD, "");

                    if (((MainActivity) getActivity()).isURLReachable()) {
                        Log.i("APIResponse", "APIResponse 2");
                        RetrofitClient.getService().login(new LoginRequest(user.getEmail(), password)).enqueue(new LoginCallback());
                    } else {
                        User u = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();

                        Log.i("APIResponse", "APIResponse 3 " + u.getBorrowed_assets().size());
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.Login.USER, new LoginResponse())));
                    }
                }
            }
        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Location.class) {
            lcoationList = (List<Location>) event.getResponse();
            Hawk.put(InternalStorage.Search.LOCATION, lcoationList);
            setLocationList(lcoationList);
        } else if(event.getResponse().getClass() == LoginResponse.class) {
            Hawk.put(InternalStorage.Login.USER, (LoginResponse)event.getResponse());
            //Hawk.put(InternalStorage.Login.PASSWORD, ((EditText)view.findViewById(R.id.password)).getText().toString());
            view.findViewById(R.id.select_location_list).setVisibility(View.GONE);

            setupListView(getAsset());
        } else if(RETURN_API == event.type || (event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BriefAsset.class)) {
            Log.i("hihi", "hihi " + ((List)event.getResponse()).size() );
            assets = new ArrayList<>();
            getAsset();

            if(((TabLayout)view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
                setupListView(getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
            }

            Hawk.put(InternalStorage.OFFLINE_CACHE.RETURN, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
        }else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Asset.class) {
            Log.i("hihi", "hihi 2 " + ((List)event.getResponse()).size() + " " +  ((List<Asset>)event.getResponse()).get(0).getAssetno() + " " + ((List<Asset>)event.getResponse()).get(0).getId());
            setupListView(((List<Asset>) event.getResponse()));
        }
    }

    public void setLocationList(List<Location> lcoationList){
        List<String> location = new ArrayList<>();

        for(int i = 0; i < lcoationList.size(); i++)
            location.add(lcoationList.get(i).getName());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)view.findViewById(R.id.location_spinner)).setAdapter(dataAdapter);
    }

    public ArrayList<Asset> getSelectedList() {
        ArrayList<Asset> result = new ArrayList<>();/*
        for(int i = 0; i < getAsset().size(); i ++) {
            for(int y = 0; y < searchedEPCList.size(); y++) {
                if(getAsset().get(i).getEPC().equals(searchedEPCList.get(y))) {
                    result.add(getAsset().get(i));
                    Log.i("result", "result " + result.size());
                }
            }
        }*/

        return assetListAdapter.getSelectedAsset();
    }

    public ArrayList<String> getSelectedListString(ArrayList<Asset> getSelectedList) {
        ArrayList<String> result = new ArrayList<>();

        for(int i = 0; i < getSelectedList.size(); i ++) {
            result.add(getSelectedList.get(i).getAssetno() + "\n" + getSelectedList.get(i).getName());
        }

        return result;
    }

    public class SimpleListAdapter extends BaseAdapter {
        private ArrayList<String> data;

        public SimpleListAdapter(ArrayList<String> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_listview_simpletext, null);
            ((TextView)view.findViewById(R.id.textview)).setText("" + getItem(position));
            return view;
        }
    }



    public List<Asset> filter(List<Asset> data, String filterText) {
        if(filterText == null || filterText.length() == 0 ) return data;

        List<Asset> assetResponse = data;
        List<Asset> myAsset = new ArrayList<>();

        if (assetResponse != null && assetResponse.size() > 0) {
            for (int i = 0; i < assetResponse.size(); i++) {
                boolean exist = false;

                if (assetResponse.get(i).getAssetno() != null && assetResponse.get(i).getAssetno().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if (assetResponse.get(i).getName() != null && assetResponse.get(i).getName().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if (assetResponse.get(i).getBrand() != null && assetResponse.get(i).getBrand().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if (assetResponse.get(i).getModel() != null && assetResponse.get(i).getModel().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if (assetResponse.get(i).getCategoryString() != null && assetResponse.get(i).getCategoryString().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if (assetResponse.get(i).getLocationString() != null && assetResponse.get(i).getLocationString().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if (assetResponse.get(i).getEPC() != null && assetResponse.get(i).getEPC().toLowerCase().contains(filterText)) {
                    exist = true;
                }

                if (exist)
                    myAsset.add(assetResponse.get(i));
            }
        }

        return myAsset;
    }
}
