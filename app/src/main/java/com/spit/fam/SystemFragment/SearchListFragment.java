package com.spit.fam.SystemFragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.DatabaseHelper.DataBaseHandler;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.Status;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CurrentAssetsCountEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.HideLoadingEvent;
import com.spit.fam.Event.LoginDownloadProgressEvent;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SaveList2ExternalTask;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.SearchListAdapter;
import com.spit.fam.WebService.APIUtils;
import com.spit.fam.adapters.ReaderListAdapter;
import com.spit.fam.fragments.HomeFragment;
import com.spit.cs108library4a.Cs108Connector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Case;
import io.realm.Realm;

import static com.spit.fam.SystemFragment.DownloadFragment.CONTINUOUS_ASSET_DETAIL;

public class SearchListFragment extends HomeFragment {
    private List<AssetsDetail> assetResponse;
    private View noResult;
    private ListView listView;
    private SearchListAdapter assetListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static int offset;
    public static String filterText = "";

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AssetsDetailWithTabFragment.ASSET_NO = "";

        ((MainActivity)MainActivity.mContext).updateDrawerStatus();

        //String realmStockTakeListAsset_json = "[" + sb.toString().substring(0, sb.toString().length() > 0 ? sb.toString().length() - 1 : 0) + "]";

        //Log.i("realmStockTakeListAsset_json", "realmStockTakeListAsset_json " + realmStockTakeListAsset_json);


        offset = 0;
        view = LayoutInflater.from(getActivity()).inflate(R.layout.search_list_fragment, null);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //callAPI();
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

                if(((MainActivity)MainActivity.mContext).isURLReachable()) {
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "");
                    //RetrofitClient.getSPGetWebService().assetDetail(companyId, serverId, "", Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_CALLED_DATE, "")).enqueue(new NewAssetDetailCallback(CONTINUOUS_ASSET_DETAIL));
                    new APIUtils().download();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        listView = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);
        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, MainActivity.sharedObjects.barsList, true, false);
        ((ListView)view.findViewById(R.id.listview_1)).setAdapter(readerListAdapter);

        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFormFragment searchFormFragment = new SearchFormFragment();
                searchFormFragment.WITH_EPC = true;
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

        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search));

        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());



        ExecutorService schTaskEx = Executors.newFixedThreadPool(100000);
        schTaskEx.execute(new Runnable() {
            @Override
            public void run() {



            }
        });


        DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
        int count =  Realm.getDefaultInstance().where(AssetsDetail.class)
                .equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                .equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))
                //.isNotNull("epc")
                //.isNotEmpty("epc")
                .findAll()
                .size();
        //db.getAssetWithEPCCount("", "" + offset);

        Log.i("count", "count " + count);

        List<AssetsDetail> assets =  Realm.getDefaultInstance().where(AssetsDetail.class)
                .equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                .equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))
                //.isNotNull("epc")
                //.isNotEmpty("epc")
                .findAll()
                .sort("ordering")
                ;

        //db.getAssetWithEPC("", "0");
        handleNoResult(assets);
        setupListView(assets);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new CurrentAssetsCountEvent(count));
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 200);

        EventBus.getDefault().post(new HideLoadingEvent());
    }

    public boolean scannerOpen;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CurrentAssetsCountEvent event) {
        if(event.getAssetsCount() > 0) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search) + " (" + event.getAssetsCount() + ")");
        } else {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search));// + " (" + event.getAssetsCount() + ")");
        }
    }

    Parcelable state;
    public void onPause() {
        super.onPause();
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

        state = listView.onSaveInstanceState();
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);

        StockTakeListItemFragment.stockTakeList = null;

        //ArrayList<SPUser> spUsers = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER, new ArrayList<SPUser>());//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

        //Log.i("SP_USERS", "SP_USERS " + spUsers.size());

       // if(!((MainActivity)getActivity()).isNetworkAvailable()) {
        //    view.findViewById(R.id.scan).setVisibility(View.GONE);
         //   view.findViewById(R.id.add).setVisibility(View.GONE);
       // }
        Log.i("datadata", "datadata " + Hawk.get("TEMP_ID", -1) + " " + Hawk.get("TEMP_RESULT", new Asset()));

        return view;
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

    boolean userVisibleHint = false;

    public void changeLocale(Context context, String localeString) {
        Log.i("localeString", "localeString" + localeString);

        String languageToLoad  = localeString; // your language
        Locale locale = new Locale(languageToLoad);

        if(languageToLoad != null && languageToLoad.equals("zt")) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
    }

    public void onResume() {
        super.onResume();
/*
        DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
        int count = db.getAssetWithEPCCount("", "" + offset);

        Log.i("count", "count " + count);

                List<Asset> assets = db.getAssetWithEPC("", "0");
                handleNoResult(assets);
                setupListView(assets);
                EventBus.getDefault().post(new CurrentAssetsCountEvent(count));

        EventBus.getDefault().post(new HideLoadingEvent());
*/
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

                    Log.i("replace1", "replace1");
                    replaceFragment(new AssetsDetailWithTabFragment());
                }
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 1000);




        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
            }
        };

        Thread thread = new Thread(runnable1);
        thread.run();

        if (userVisibleHint) {
            MainActivity.mCs108Library4a.setAutoBarStartSTop(true);
            setNotificationListener();
        }

        //removeAllFragments(getChildFragmentManager());
    }

    void setNotificationListener() {
        MainActivity.mCs108Library4a.setNotificationListener(new Cs108Connector.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    public void callAPI(){
        /*if (((MainActivity) getActivity()).isNetworkAvailable()) {
            Log.i("callingAPI", "callingAPI @ SearchListFragment" + Hawk.get(InternalStorage.Setting.COMPANY_ID,"") + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,""));
            RetrofitClient.getSPGetWebService().assetsList(Hawk.get(InternalStorage.Setting.COMPANY_ID,""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"")).enqueue(new GetBriefAssetCallback(2));
        } else {
            Log.i("localCache", "localCache @ SearchListFragment");
            List<Asset> originalList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<>());
            EventBus.getDefault().post(new CallbackResponseEvent(originalList));
        }*/

        /*
        if(!LoginFragment.SP_API) {
            if (((MainActivity) getActivity()).isNetworkAvailable()) {
                User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
                if (user != null && user.getUser_group() != null && user.getUser_group().getId() >= 0)
                    RetrofitClient.getService().getAssetList(user.getUser_group().getId()).enqueue(new GetAssetListCallback());
            } else {
                ArrayList arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.ASSET, new ArrayList<>());
                EventBus.getDefault().post(new CallbackResponseEvent(arrayList));
            }
        }*/
    }


    public void handleNoResult(List<AssetsDetail> data) {
        Log.i("case 0", "case handleNoResult 0");
        if(data == null || data.size() == 0) {

            Log.i("case 0", "case handleNoResult 1");
            noResult.setVisibility(View.VISIBLE);
        } else {
            Log.i("case 0", "case handleNoResult 2");

            noResult.setVisibility(View.GONE);
        }
    }

    private static void removeAllFragments(FragmentManager fragmentManager) {
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    public void setupListView(List<AssetsDetail> assetResponse) {
        swipeRefreshLayout.setRefreshing(false);

        AssetListAdapter.WITH_EPC = true;

        this.assetResponse = assetResponse;
        handleNoResult(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()));

        //if(assetListAdapter == null) {
            assetListAdapter = new SearchListAdapter(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()), getActivity(), "");
            assetListAdapter.hasMore = true;
            listView.setAdapter(assetListAdapter);
       // } else {
            assetListAdapter.setData(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()), getActivity());
            //listView.setAdapter(assetListAdapter);
            assetListAdapter.notifyDataSetChanged();
       // }

        EventBus.getDefault().post(new CurrentAssetsCountEvent(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()).size()));

        //if(state != null) {
        //    listView.onRestoreInstanceState(state);
        //}
    }

    public List<AssetsDetail> getFilterList(String filterText) {
        List<AssetsDetail> myAsset = new ArrayList<>();

        if(filterText == null || filterText.length() == 0) {
            return assetResponse;
        }

        for(int i = 0; i < assetResponse.size(); i++) {
            boolean exist = false;

            if(assetResponse.get(i).getAssetNo().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getName().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getBrand().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getModel().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getCategory().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getLocation().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getEpc().toLowerCase().contains(filterText)) {
                exist = true;
            } else if(assetResponse.get(i).getLastassetno().toLowerCase().contains(filterText)) {
                exist = true;
            }

            if(exist)
                myAsset.add(assetResponse.get(i));
        }

        return myAsset;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.spit.fam.BarcodeScanEvent event) {
        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
        Log.i("BarcodeScanEvent", "BarcodeScanEvent");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        if(event.type == CONTINUOUS_ASSET_DETAIL) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if(event.type == CONTINUOUS_ASSET_DETAIL) {
            swipeRefreshLayout.setRefreshing(false);
            offset = 0;

            ExecutorService schTaskEx = Executors.newFixedThreadPool(100000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {

                    DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
                    //int count = db.getAssetWithEPCCount("", "" + offset);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<AssetsDetail> assets =  Realm.getDefaultInstance().where(AssetsDetail.class)
                                    .equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                                    .equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))
                                    //.isNotNull("epc")
                                    //.isNotEmpty("epc")
                                    .findAll()
                                    .sort("ordering")
                                    ;
                            int count = assets.size();
                            Log.i("count", "count " + count);

                            setupListView(assets);
                            EventBus.getDefault().post(new CurrentAssetsCountEvent(count));
                        }
                    });
                }
            });
        }

        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BriefAsset.class ) {
        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Asset.class ) {
        } else {
        }
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
        //List<Asset> originalList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<>());

        boolean exist = false;
/*
        //for(int i = 0; i < originalList.size(); i++) {
            DataBaseHandler databaseHandler = new DataBaseHandler(MainActivity.mContext);
            List<AssetsDetail> assetsDetail =   databaseHandler.searchAssetsDetail(event.getBarcode(), "", "", "", "", "", "", "", "");//.size());//MainActivity.getAssetsDetailList(ASSET_NO);//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);

            Log.i("assetsDetail", "assetsDetail " + event.getBarcode() + " " + assetsDetail.size());
            //List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + originalList.get(i).getAssetno(), null);

           // if(originalList.get(i).getAssetno().equals(event.getBarcode()) || assetsDetail != null && (assetsDetail.get(0).getAssetNo().equals(filterText) || assetsDetail.get(0).getBarcode().equals(filterText))) {
            if(assetsDetail.size() > 0) {
                exist = true;
                AssetsDetailWithTabFragment.ASSET_NO = (assetsDetail.get(0).getAssetNo()); 0
      
                if(AssetsDetailWithTabFragment.ASSET_NO == null || AssetsDetailWithTabFragment.ASSET_NO.length() == 0 || assetsDetail.get(0).getEpc().length() == 0) {

                } else if(assetsDetail == null) {

                } else {
                    ((MainActivity) getActivity()).replaceFragment(new AssetsDetailWithTabFragment());
                }

            }

            if(exist) {

            }*/
       // }

        List<AssetsDetail> assets =  Realm.getDefaultInstance().where(AssetsDetail.class)
                .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                //.isNotNull("epc")
                //.isNotEmpty("epc")
                .beginGroup()
                .equalTo("assetNo", event.getBarcode(), Case.INSENSITIVE)
                .or()
                .equalTo("epc", event.getBarcode(), Case.INSENSITIVE)
                .endGroup()
                .equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                .equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))

                .findAll()
                .sort("ordering");

        if(assets.size() > 0) {
            exist = true;
            AssetsDetailWithTabFragment.ASSET_NO = (assets.get(0).getAssetNo());

            if(assets.size() == 0) {

            } else {
                Log.i("replace99", "replace99");
                ((MainActivity) getActivity()).replaceFragment(new AssetsDetailWithTabFragment());
            }

        }
        if(!exist) {
            Toast.makeText(getActivity(), getString(R.string.no_data),  Toast.LENGTH_LONG).show();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        Log.i("CustomTextWatcherEvent", "CustomTextWatcherEvent " + event.getTitle());

        //if(!MainActivity.OFFLINE_MODE && LoginFragment.SP_API) {
        //    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
        //    String userid = Hawk.get(InternalStorage.Login.USER, "");

        //    RetrofitClient.getSPGetWebService().search(companyId, userid, "",event.getTitle().toLowerCase(), "1", "", "","", "", "", "").enqueue(new GetBriefAssetCallback());
        //} else {
            filterText = event.getTitle().toLowerCase();

            //if (filterText == null || filterText.length() == 0) {
            //    setupListView(assetResponse);
            //    return;
            //}

            offset = 0;

            //if (assetResponse != null && assetResponse.size() > 0) {
                List<AssetsDetail> myAsset = new ArrayList<>();

                /*
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
                }*/

                //DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
                //myAsset = db.getAssetWithEPC(filterText, offset + "");
        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
        String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

        List<AssetsDetail> assets =  Realm.getDefaultInstance().where(AssetsDetail.class)
                        .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                        //.isNotNull("epc")
                        //.isNotEmpty("epc")
                        .beginGroup()
                        .contains("assetNo", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("name", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("brand", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("model", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("category", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("location", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("epc", event.getTitle(), Case.INSENSITIVE)
                        .or()
                        .contains("lastassetno", event.getTitle(), Case.INSENSITIVE)
                        .endGroup()
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)

                        .findAll()
                        .sort("ordering")
                        ;

                int count = assets.size();// db.getAssetWithEPCCount(filterText, "" + offset);

                Log.i("textChanged", "textChanged " + count + " " + myAsset.size());

                EventBus.getDefault().post(new CurrentAssetsCountEvent(count));

                handleNoResult(assets);
                if(count > SearchListAdapter.PAGE_SIZE)
                    assetListAdapter.hasMore = true;
                assetListAdapter.setData(assets, getActivity());
                assetListAdapter.notifyDataSetChanged();
            //}
        //}
    }

    private ReaderListAdapter readerListAdapter;
    void clearTagsList() {
        MainActivity.sharedObjects.barsList.clear();
        readerListAdapter.notifyDataSetChanged();
    }
    void sortTagsList() {
        Collections.sort(MainActivity.sharedObjects.barsList);
        readerListAdapter.notifyDataSetChanged();
    }
    void saveTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.barsList);
        saveExternalTask.execute();
    }
    void shareTagsList() {
        MainActivity.mCs108Library4a.appendToLog("Share BUTTON is pressed.");
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginDownloadProgressEvent event) {
        setProgress(event.getProgress());
    }



    public void setProgress(Float progress) {
        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if((int)(progress * 100) >= 100) {
                    //((LinearLayout) view.findViewById(R.id.blocking)).setVisibility(View.GONE);
                    return;
                }

                //Log.i("data", "data " + (int)(progress * 100));

                ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.downloading) + " " + (int)(progress * 100) + "/100");
                ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
                ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
                ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(progress * 100));
            }

        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doneEvent(NetworkInventoryDoneEvent event) {
        ((LinearLayout) view.findViewById(R.id.blocking)).setVisibility(View.GONE);

        List<AssetsDetail> assets =  Realm.getDefaultInstance().where(AssetsDetail.class)
                .equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                .equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))

                //.isNotNull("epc")
                //.isNotEmpty("epc")
                .findAll()
                .sort("ordering")
                ;

        if(assets.size() > 0) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search) + " (" + assets.size() + ")");
        } else {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search));// + " (" + event.getAssetsCount() + ")");
        }
        //db.getAssetWithEPC("", "0");
        handleNoResult(assets);
        setupListView(assets);
    }
}
