package com.spit.fam.SystemFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.WebService.Callback.GetAssetListCallback;
import com.spit.fam.WebService.Callback.GetBriefAssetCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.adapters.ReaderListAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class RegistrationFragment extends BaseFragment {
    private List<Asset> assetResponse;
    private View noResult;
    private ListView listView;
    private AssetListAdapter assetListAdapter;

    public static boolean RESET_TAB = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.registration_fragment, null);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, MainActivity.sharedObjects.barsList, true, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(((MainActivity)getActivity()).isURLReachable()) {
                    Log.i("callingAPI", "callingAPI @ RegistrationFragment");
                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                    RetrofitClient.getSPGetWebService().assetsList(Hawk.get(InternalStorage.Setting.COMPANY_ID,""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"")).enqueue(new GetBriefAssetCallback(DownloadFragment.SEARCH_NO_EPC));

                } else {
                    Log.i("localCache", "localCache @ RegistrationFragment");

                    Log.i("size", "size " + Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()).size());
                    EventBus.getDefault().post(new CallbackResponseEvent((Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()))) );
                }
            }
        });

        listView = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFormFragment searchFormFragment = new SearchFormFragment();
                searchFormFragment.WITH_EPC = false;
                replaceFragment(searchFormFragment);
            }
        });
        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        //view.findViewById(R.id.scan).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                    startStopHandler(false);
                }
            }
        });
        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration));
        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(LoginFragment.SP_API) {
                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");//.getUser();
                    String userid = Hawk.get(InternalStorage.Login.USER_ID, "");//.getUser();
                    if(!MainActivity.OFFLINE_MODE) {
                       // RetrofitClient.getSPGetWebService().disposalList(companyId, userid, tab.getPosition()).enqueue(new GetBriefBorrowedAssetCallback());
                    } else {
                        EventBus.getDefault().post(new CallbackResponseEvent((Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()))) );
                    }
                } else {
                    //setupListView(getData());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);
        return view;
    }

    Parcelable state;

    @Override
    public void onPause() {
        state = listView.onSaveInstanceState();

        if(scannerOpen) {
            scannerOpen = false;

            boolean started = false;
            if (inventoryBarcodeTask != null)
                if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING)
                    started = true;

            if (started != false) {
                MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: Stop Barcode inventory");
                inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
            }

        }
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {
        Log.i("BarcodeScanEvent", "BarcodeScanEvent " + event.getBarcode());

        if (MainActivity.mCs108Library4a.isBleConnected() == false) {
        } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
        } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
        } else {
            startStopHandler(false);
        }
        String filterText = event.getBarcode();
        //List<Asset> originalList = Hawk.get(InternalStorage.OFFLINE_CACHE.REGISTRATION, new ArrayList<>());

        boolean exist = false;

        //for(int i = 0; i < originalList.size(); i++) {
        //List<AssetsDetail> assetsDetail =   databaseHandler.searchAssetsDetail(event.getBarcode(), "", "", "", "", "", "", "", "");//.size());//MainActivity.getAssetsDetailList(ASSET_NO);//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);
        //String assetNo, String name, String startLoc, String endLoc, String brand, String model, String startCat, String endCat, String barcode
        List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class)
                .equalTo("assetNo", event.getBarcode())
                .findAll();

        if(assetsDetail.size() > 0) {
            AssetsDetailWithTabFragment.ASSET_NO = (assetsDetail.get(0).getAssetNo());

            if (AssetsDetailWithTabFragment.ASSET_NO == null || AssetsDetailWithTabFragment.ASSET_NO.length() == 0 || assetsDetail.get(0).getEpc().length() > 0) {

            } else if (assetsDetail == null) {
                exist = true;

                Log.i("replace2", "replace2");

                ((MainActivity) getActivity()).replaceFragment(new AssetsDetailWithTabFragment());
            }

            if (exist) {
            }
        }

        if(!exist) {
            Toast.makeText(getActivity(), getString(R.string.no_data),  Toast.LENGTH_LONG).show();
        }
    }
    public void onResume() {
        super.onResume();

        Log.i("onResume hihi", " onResume hihi " + RegistrationFragment.RESET_TAB);

        if(RegistrationFragment.RESET_TAB) {
            ((TabLayout)view.findViewById(R.id.tab_layout)).getTabAt(0).select();
            RegistrationFragment.RESET_TAB = false;
        }

        if(((MainActivity)getActivity()).isURLReachable()) {
            Log.i("callingAPI", "callingAPI @ RegistrationFragment");
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            RetrofitClient.getSPGetWebService().searchnoepc(companyId).enqueue(new GetBriefAssetCallback(DownloadFragment.SEARCH_NO_EPC));
            //RetrofitClient.getSPGetWebService().assetsList(Hawk.get(InternalStorage.Setting.COMPANY_ID,""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"")).enqueue(new GetBriefAssetCallback(DownloadFragment.SEARCH_NO_EPC));

            /*ArrayList<Asset> briefAssets = new ArrayList<>();
            briefAssets.add(new Asset());
            Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, briefAssets);

            EventBus.getDefault().post(new CallbackResponseEvent(getData((briefAssets) ) ) );
*/
        } else {
            Log.i("localCache", "localCache @ RegistrationFragment");
            //Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
            EventBus.getDefault().post(new CallbackResponseEvent((Hawk.get(InternalStorage.OFFLINE_CACHE.REGISTRATION, new ArrayList<>()))) );

            //Log.i("size", "size " + Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()).size());
            //EventBus.getDefault().post(new CallbackResponseEvent((Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()))) );
        }
        //removeAllFragments(getChildFragmentManager());
    }

    public void callAPI(){
        User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
        {
            Log.i("hihi", "hihi " + user.getUser_group().getId());
            if (user != null && user.getUser_group() != null && user.getUser_group().getId() >= 0)
                RetrofitClient.getService().getAssetListEPCEmpty(
                        user.getUser_group().getId()).enqueue(new GetAssetListCallback());
        }
    }

    public ArrayList<Asset> getData(ArrayList<Asset> dataList) {
        int selectedPosition = ((TabLayout)view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

        Log.i("selectedPosition", "selectedPosition " + selectedPosition);

        ArrayList<Asset> result = new ArrayList<>();

        for(int i = 0; i < dataList.size(); i++) {
            if(selectedPosition == 0) {
                if (dataList.get(i).getEPC() != null && dataList.get(i).getEPC().length() > 0) {
                    result.add(dataList.get(i));
                }
            } else {
                if (dataList.get(i).getEPC() == null || dataList.get(i).getEPC().length() == 0) {
                   // Log.i("length", "length " + dataList.get(i).getEPC().length() );
                    result.add(dataList.get(i));
                }
            }
        }
        Log.i("selectedPosition", "selectedPosition 2 " + result.size() + " " + dataList.size());

        return result;
    }

    public void handleNoResult(List<Asset> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            noResult.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private static void removeAllFragments(FragmentManager fragmentManager) {
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    public void setupListView(List<Asset> assetResponse) {
        AssetListAdapter.WITH_EPC = false;

        this.assetResponse = assetResponse;
        handleNoResult(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()));

        if(assetListAdapter == null) {
            assetListAdapter = new AssetListAdapter(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()), getActivity());
            listView.setAdapter(assetListAdapter);
        } else {
            assetListAdapter.setData(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()), getActivity());
            listView.setAdapter(assetListAdapter);
        }

        Log.i("assetResponse", "assetResponse " + assetResponse.size());

        if(assetResponse.size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration) + " (" + (getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString())).size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration));

        // Restore previous state (including selected item index and scroll position)
        if(state != null) {
            listView.onRestoreInstanceState(state);
        }
    }

    public List<Asset> getFilterList(String filterText) {
        List<Asset> myAsset = new ArrayList<>();

        if(filterText == null || filterText.length() == 0) {
            return assetResponse;
        }

        for(int i = 0; i < assetResponse.size(); i++) {
            boolean exist = false;

            if(assetResponse.get(i).getAssetno().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getName().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getBrand().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getModel().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getCategoryString().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getLocationString().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getEPC().toLowerCase().contains(filterText)) {
                exist = true;
            }

            if(exist)
                myAsset.add(assetResponse.get(i));
        }

        return myAsset;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {

        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Log.i("event", "event " + event.getResponse());

        if(event.type == DownloadFragment.SEARCH_NO_EPC) {
            List <BriefAsset> assets = ((List<BriefAsset>) event.getResponse());
            List <BriefAsset> newData = new ArrayList<>();

            for(int i = 0; i < assets.size(); i++) {
                if(assets.get(i).getEpc() == null || assets.get(i).getEpc().length() == 0) {
                    newData.add(assets.get(i));
                }
            }

            Log.i("newData", "newData " + newData.size());

            Hawk.put(InternalStorage.OFFLINE_CACHE.REGISTRATION, getAssetListFromBriefAssetList((newData)));
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(getAssetListFromBriefAssetList(newData));
            EventBus.getDefault().post(callbackResponseEvent);

            if(newData.size() == 0) {
                ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration));
            }

        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BriefAsset.class ) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
            EventBus.getDefault().post(callbackResponseEvent);

        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Asset.class ) {
            //Hawk.put(InternalStorage.Application.ASSET, event.getResponse());InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION
            Hawk.put(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, event.getResponse());
            setupListView(getData((ArrayList<Asset>) event.getResponse()));
        } else {
            handleNoResult(null);
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        Log.i("event", "event " + event.getTitle());

        String filterText = event.getTitle().toLowerCase();

        if(filterText == null || filterText.length() == 0) {
            setupListView(assetResponse);
            return;
        }

        if(assetResponse != null && assetResponse.size() > 0) {
            List<Asset> myAsset = new ArrayList<>();

            for(int i = 0; i < assetResponse.size(); i++) {
                boolean exist = false;

                //assetResponse.get(i).getAssetno().toLowerCase().contains(filterText)
                Log.i("data", "data " + assetResponse.get(i).getAssetno() + " " + filterText);

                if(assetResponse.get(i).getAssetno() != null && assetResponse.get(i).getAssetno().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(assetResponse.get(i).getName() != null && assetResponse.get(i).getName().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(assetResponse.get(i).getBrand() != null && assetResponse.get(i).getBrand().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(assetResponse.get(i).getModel() != null && assetResponse.get(i).getModel().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(assetResponse.get(i).getCategoryString() != null && assetResponse.get(i).getCategoryString().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(assetResponse.get(i).getLocationString() != null && assetResponse.get(i).getLocationString().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(assetResponse.get(i).getEPC() != null && assetResponse.get(i).getEPC().toLowerCase().contains(filterText)) {
                    exist = true;
                }

                if(exist)
                    myAsset.add(assetResponse.get(i));
            }

            if(myAsset.size() > 0)
                ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration) + " (" + myAsset.size() + ")");
            else
                ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration));

            handleNoResult(myAsset);
            assetListAdapter.setData(myAsset, getActivity());
            assetListAdapter.notifyDataSetChanged();
        }
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
        asset.setProsecutionNo(briefAsset.getProsecutionNo());
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
            assetArrayList.add(convertBriefAssetToAsset(briefAssets.get(i)));
        }

        return assetArrayList;
    }


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


    private ReaderListAdapter readerListAdapter;
    public boolean scannerOpen;
    InventoryBarcodeTask inventoryBarcodeTask;
    void startStopHandler(boolean buttonTrigger) {
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
}

