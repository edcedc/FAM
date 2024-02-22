package com.spit.fam.SystemFragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
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

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomMediaPlayer;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Entity.BorrowListAsset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.CreateBy;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.OfflineMode.BorrowAssets;
import com.spit.fam.Entity.OfflineMode.DisposalAssets;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.TagType;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.InventoryRfidTask;
import com.spit.fam.MainActivity;
import com.spit.fam.OnBackPressEvent;
import com.spit.fam.R;
import com.spit.fam.Request.LoginRequest;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.SaveList2ExternalTask;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.DisposalListAdapter;
import com.spit.fam.SystemFragment.Adapter.SimpleReaderListAdapter;
import com.spit.fam.WebService.Callback.GetBorrowListAssetCallback;
import com.spit.fam.WebService.Callback.GetBriefAssetObjectCallback;
import com.spit.fam.WebService.Callback.LoginCallback;
import com.spit.fam.WebService.Callback.UpdateAssetEpcCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.adapters.ReaderListAdapter;
import com.spit.fam.fragments.ConnectionFragment;
import com.spit.fam.fragments.HomeFragment;
import com.spit.cs108library4a.Cs108Library4A;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmResults;

public class BorrowListItemListFragment extends HomeFragment {
    public static boolean BORROW_LIST;
    public static boolean ABNORMAL = false;
    public static String BORROW_NO = null;

    public static String DISPOSAL_NO = null;
    public static boolean DISPOSAL_LIST = false;

    private InventoryRfidTask inventoryRfidTask;

    private List<BorrowList> assetResponse;
    private View noResult;
    private ListView listView, rfidListView;
    private AssetListAdapter assetListAdapter;
    private DisposalListAdapter disposalListAdapter;
    private Button start;

    public static BorrowList borrowList;
    private int tabPosition = 0;


    private ReaderListAdapter readerListAdapter;
    public static boolean UPDATE_VALUE;

    public static int type;

    TabItem tabItem;

    public static String SELECTED;

    CustomMediaPlayer playerO, playerN;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        playerO = MainActivity.sharedObjects.playerO;
        playerN = MainActivity.sharedObjects.playerN;

        POSITITON = 1;

        clearTagsList();
        SELECTED = null;

        view = LayoutInflater.from(getActivity()).inflate(R.layout.borrow_list_item_list_fragment, null);

        view.findViewById(R.id.add).setVisibility(View.GONE);

        Log.i("data", "data " + BorrowListFragment.POSITION + " " + DisposalListFragment.POSITION);

        if (BorrowListFragment.POSITION == 1 || DisposalListFragment.POSITION == 1) {
            view.findViewById(R.id.tab_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.button_panel).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.tab_layout).setVisibility(View.GONE);
            view.findViewById(R.id.button_panel).setVisibility(View.GONE);
        }

        listView = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);
        rfidListView = view.findViewById(R.id.rfidlistview);

        boolean bSelect4detail = true;
        boolean needDupElim = true;

        boolean need4Extra1 = MainActivity.mCs108Library4a.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = (false);

        readerListAdapter = new SimpleReaderListAdapter(getActivity(), R.layout.reader_list_item_clone, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);
        rfidListView.setAdapter(readerListAdapter);

        start = view.findViewById(R.id.borrow_start);

        if (BORROW_LIST) {
            ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).setText(getString(R.string.borrowed));
        } else {
            ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).setText(getString(R.string.disposed));
        }

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getWaitingListId().size() == 0) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.disposal), getString(R.string.nothing_selected)));
                    return;
                }

                //view.findViewById(R.id.nfc_card_confirm).setVisibility(View.VISIBLE);
                confirmBorrowDisposal();

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startStopHandler(false);
                if (start.getText().toString().equals(getString(R.string.start))) {
                    start.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();
                } else {
                    start.setText(getString(R.string.start));
                    ((MainActivity) MainActivity.mContext).stop();
                }
            }
        });

        view.findViewById(R.id.borrow_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onBackPress", "onBackPress 3");

                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).updateDrawerStatus();
                ((MainActivity) getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        if (!LoginFragment.SP_API)
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(borrowList.getName().toUpperCase());

        ((TabLayout) view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((MainActivity) getActivity()).hideKeyboard(getActivity());

                ((EditText) view.findViewById(R.id.edittext)).setText("");

                tabPosition = tab.getPosition();
                setupListView(getData());
                if (tabPosition != 1) {
                    view.findViewById(R.id.scan).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.scan).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean started = false;
                boolean delayNeeded = false;

                if (inventoryRfidTask != null)
                    if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING)
                        started = true;

                if (started) {
                    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
                    delayNeeded = true;
                }

                Log.i("started", "started " + started);

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
                if (delayNeeded) {
                    handler.postDelayed(runnable, 1000);
                } else {
                    handler.post(runnable);
                }
            }
        });
        setupListView(getData());

        ((EditText) view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());


        if (LoginFragment.SP_API) {
            CallbackResponseEvent callbackResponseEvent = null;
            if (BORROW_NO != null) {
                callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO, new ArrayList<>()));
            } else if (DISPOSAL_NO != null) {
                callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + DISPOSAL_NO, new ArrayList<>()));
            }


            CallbackResponseEvent finalCallbackResponseEvent = callbackResponseEvent;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(finalCallbackResponseEvent);
                }
            };

            Handler handler = new Handler();
            handler.postDelayed(runnable, 10);

        } else {
            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.Login.USER, new LoginResponse())));
        }

        if (((MainActivity) getActivity()).isURLReachable()) {
            if (LoginFragment.SP_API) {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

                if (BORROW_NO != null) {
                    RetrofitClient.getSPGetWebService().borrowListAssets(companyId, userid, BORROW_NO).enqueue(new GetBorrowListAssetCallback());
                } else if (DISPOSAL_NO != null) {
                    RetrofitClient.getSPGetWebService().disposalListAssets(companyId, userid, DISPOSAL_NO).enqueue(new GetBorrowListAssetCallback());
                }
            } else {
                //callAPI();
            }
        } else {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        setupListView(getData());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent barcodeScanEvent) {
        Log.i("BarcodeScanEvent", "BarcodeScanEvent " + barcodeScanEvent.getBarcode());

        if (MainActivity.mCs108Library4a.isBleConnected() == false) {
        } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
        } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
        } else {
            startStopBarcodeHandler(false);
        }

        ArrayList<String> s = new ArrayList<>();

        for (int x = 0; x < waitingList.size(); x++) {
            if (waitingList.get(x).getAssetno().equals(barcodeScanEvent.getBarcode()) || waitingList.get(x).getEPC().equals(barcodeScanEvent.getBarcode())) {

                waitingList.get(x).setFound(true);
                waitingListEPC.add(waitingList.get(x).getEPC());
                handleNoResult(waitingList);
                assetListAdapter.notifyDataSetChanged();
            }
        }
    }

    InventoryBarcodeTask inventoryBarcodeTask;

    void startStopBarcodeHandler(boolean buttonTrigger) {
        if (buttonTrigger)
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.mCs108Library4a.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryBarcodeTask != null)
            if (inventoryBarcodeTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
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
            if (buttonTrigger)
                inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE;
            else
                inventoryBarcodeTask.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP;
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

    void clearTagsList() {
        MainActivity.mCs108Library4a.appendToLog("runningInventoryRfidTask = " + MainActivity.sharedObjects.runningInventoryRfidTask + ", readerListAdapter" + (readerListAdapter != null ? " tagCount = " + String.valueOf(readerListAdapter.getCount()) : " = NULL"));
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        MainActivity.tagSelected = null;
        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
        MainActivity.mLogView.setText("");
        try {
            readerListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    void sortTagsList() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        Collections.sort(MainActivity.sharedObjects.tagsList);
        readerListAdapter.notifyDataSetChanged();
    }

    void saveTagsList() {
        if (MainActivity.sharedObjects.runningInventoryRfidTask) return;
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.tagsList);
        saveExternalTask.execute();
    }

    void shareTagsList() {
        SaveList2ExternalTask saveExternalTask = new SaveList2ExternalTask(MainActivity.sharedObjects.tagsList);
        String stringOutput = saveExternalTask.createStrEpcList();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, stringOutput);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Sharing to"));
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li, vg, b);

/*
        ArrayList<String> s = new ArrayList<>();

        s.add("510833B2DDD9014000003456");

        for(int x = 0; x < 100; x++) {
        }
        RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(s);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
               EventBus.getDefault().post(rfidDataUpdateEvent);
            }
        };

        new Handler().postDelayed(runnable, 200);
*/

        return view;
    }

    private boolean started = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean calling = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (started)
                start();

            Log.i("cardNumber", "cardNumber " + MainActivity.nfcCardNumber);

            if (Hawk.get(InternalStorage.Login.CARD_NUMBER, null) != null) {
                Log.i("cardNumber", "cardNumber compare " + "9F4F1B2D".equals(MainActivity.nfcCardNumber));

                if (Hawk.get(InternalStorage.Login.CARD_NUMBER, null).equals(MainActivity.nfcCardNumber)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (view.findViewById(R.id.nfc_card_confirm).getVisibility() == (View.VISIBLE)) {
                                view.findViewById(R.id.nfc_card_confirm).setVisibility(View.GONE);
                                confirmBorrowDisposal();
                                MainActivity.nfcCardNumber = null;
                            }
                        }
                    });
                }
            }
            //34148F936400000000003B83
            //34187890000000000000000E
            //B00000000000000000000001
            //E2801160600002083E16E948
            if (!searchedEPCList.contains("0920A0000000000000000010")) {
                //searchedEPCList.add("0920A0000000000000000010");
            }

            if (!searchedEPCList.contains("E2801160600002083E1679BB")) {
                //searchedEPCList.add("E2801160600002083E1679BB");
            }
            if (!searchedEPCList.contains("E2801160600002083E16E918")) {
                //searchedEPCList.add("E2801160600002083E16E918");
            }
           /*
            if(!searchedEPCList.contains("34148F936400000000003B86")) {
                searchedEPCList.add("34148F936400000000003B86");
            }
            if(!searchedEPCList.contains("34148F936400000000003B87")) {
                searchedEPCList.add("34148F936400000000003B87");
            }

            if(!searchedEPCList.contains("34187890000000000000000E")) {
                searchedEPCList.add("34187890000000000000000E");
            }*/
            //if(!searchedEPCList.contains("341878900000000000000005")) {
            //    searchedEPCList.add("341878900000000000000005");
            //}
            //if(!searchedEPCList.contains("34187890000000000000001A")) {
            //    searchedEPCList.add("34187890000000000000001A");
            //}

            //if(!searchedEPCList.contains("34187890000000000000001A")) {
            //    searchedEPCList.add("34187890000000000000001A");
            //}
            ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
            schTaskEx.execute(new Runnable() {
                @Override
                public void run() {
                    calling = true;

                    List<Asset> assets = getData();

                    //if( getActivity() != null)
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (assetListAdapter != null) {
                                    assetListAdapter.setSearchedEPCList(searchedEPCList);
                                    assetListAdapter.setData(assets, getActivity());
                                    handleNoResult(assets);
                                    assetListAdapter.notifyDataSetChanged();
                                } else if (disposalListAdapter != null) {
                                    disposalListAdapter.setSearchedEPCList(searchedEPCList);
                                    disposalListAdapter.setData(assets, getActivity());
                                    handleNoResult(assets);
                                    disposalListAdapter.notifyDataSetChanged();
                                }
                                calling = false;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });


        }
    };

    public void stop() {
        started = false;
        //handler.removeCallbacks(runnable);
        //handler = null;
        //runnable = null;
    }

    public void start() {
        started = true;
        //handler.postDelayed(runnable, 1000);
    }

    private static int POSITITON = 1;

    public void onResume() {
        super.onResume();
        Log.i("dataonResume ", "dataonResume " + BorrowListFragment.POSITION + " " + DisposalListFragment.POSITION);

        ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(POSITITON).select();

        start();
    }

    public void callAPI() {
        User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
        String password = Hawk.get(InternalStorage.Login.PASSWORD, "");
        RetrofitClient.getService().login(new LoginRequest(user.getEmail(), password)).enqueue(new LoginCallback());
    }

    public void onPause() {
        super.onPause();


        if (start.getText().toString().equals(getString(R.string.start))) {
            start.setText(getString(R.string.start));
        }

        ((MainActivity) getActivity()).hideKeyboard(getActivity());

        POSITITON = (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition());

        boolean started = false;

        if (inventoryRfidTask != null)
            if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) {
                started = true;
            }

        if (started != false) {
            MainActivity.mCs108Library4a.appendToLogView("CANCELLING. Set taskCancelReason");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
        }

        onStop();
        stop();
    }

    public void handleNoResult(List<Asset> data) {
        if (data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }


        if (title != null && data.size() > 0) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(title + " (" + data.size() + ")");
        } else {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(title);// + " (" + assetResponse.size() + ")");
        }
    }

    public void setupListView(List<Asset> assetResponse) {
        handleNoResult(assetResponse);
        AssetListAdapter.WITH_EPC = true;
        DisposalListAdapter.WITH_EPC = true;

        Log.i("setupListView", "setupListView " + assetListAdapter + " " + disposalListAdapter + " " + BORROW_LIST + " " + assetResponse.size());

        if (disposalListAdapter == null && assetListAdapter == null) {
            //if(BORROW_LIST) {
            Log.i("setupListView", "setupListView case 1");
            assetListAdapter = new AssetListAdapter(assetResponse, getActivity(), true);
            assetListAdapter.setSearchedEPCList(searchedEPCList);
            assetListAdapter.setData(assetResponse, getActivity());

            listView.setAdapter(assetListAdapter);
            /*} else {
                Log.i("setupListView", "setupListView case 2");
                disposalListAdapter = new DisposalListAdapter(assetResponse, getActivity(), true);
                disposalListAdapter.setSearchedEPCList(searchedEPCList);
                disposalListAdapter.setData(assetResponse, getActivity());
                listView.setAdapter(disposalListAdapter);
            }*/
        } else {
            //if(BORROW_LIST) {
            Log.i("setupListView", "setupListView case 3");
            assetListAdapter.setSearchedEPCList(searchedEPCList);
            assetListAdapter.setData(assetResponse, getActivity());
            assetListAdapter.notifyDataSetChanged();
            //listView.setAdapter(assetListAdapter);
            /*} else {
                Log.i("setupListView", "setupListView case 4");

                disposalListAdapter.setSearchedEPCList(searchedEPCList);
                disposalListAdapter.setData(assetResponse, getActivity());
                disposalListAdapter.notifyDataSetChanged();
                //listView.setAdapter(disposalListAdapter);
            }*/
        }

        //if(assetResponse.size() > 0)
        //    ((TextView) view.findViewById(R.id.toolbar_title)).setText(borrowList.getName().toUpperCase() + " (" + assetResponse.size() + ")");
    }


    /*
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        this.assetResponse = (List<BorrowList>) event.getResponse();

        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BorrowList.class ) {
            setupListView(getData());
        } else {
            handleNoResult(null);
        }
    }*/

    public List<Asset> getBorrowedList() {
        if (LoginFragment.SP_API) {
            ArrayList<Asset> myAsset = new ArrayList<>();
            Log.i("apiData", "apiData " + apiData.size());
            for (int i = 0; i < apiData.size(); i++) {
                Log.i("borrowed", "borrowed " + apiData.get(i).isFound() + " " + apiData.get(i).getType());
                //345134513451345134513451
                if (apiData.get(i).isFound() && apiData.get(i).getType() == 1) {
                    myAsset.add(apiData.get(i));
                }// else if(searchedEPCList.contains(apiData.get(i).getEPC()) ){
                //   myAsset.add(apiData.get(i));
                //}
            }

            Log.i("myAsset", "myAsset " + myAsset.size());

            return myAsset;
        } else if (BORROW_LIST) {
            return borrowList.getBorrowedItem();
        } else {
            return borrowList.getDisposalItem();
        }
    }

    public List<Asset> getWaitingList() {
        if (LoginFragment.SP_API) {

            if (view.findViewById(R.id.tab_layout).getVisibility() == View.GONE) {
                return apiData;
            }

            ArrayList<Asset> myAsset = new ArrayList<>();
            for (int i = 0; i < apiData.size(); i++) {
                if (!apiData.get(i).isFound() && apiData.get(i).getType() == 1) {
                    if (BORROW_NO != null || DISPOSAL_NO != null) {
                        if (apiData.get(i).getStatus() != null && apiData.get(i).getStatus().id == 9997) {
                            Asset asset = apiData.get(i);
                            asset.setFoundInSearchedEPCList(searchedEPCList.contains(asset.getEPC()));
                            Log.i("setFoundInSearchedEPCList", "setFoundInSearchedEPCList " + asset.isFoundInSearchedEPCList() + " " + apiData.get(i).getType());
                            if (apiData.get(i).getType() == 1) {
                                myAsset.add(apiData.get(i));
                            } else if (type != 1) {
                                myAsset.add(apiData.get(i));
                            }
                        }
                    } else {
                        Asset asset = apiData.get(i);
                        asset.setFoundInSearchedEPCList(searchedEPCList.contains(asset.getEPC()));
                        Log.i("setFoundInSearchedEPCList", "setFoundInSearchedEPCList " + asset.isFoundInSearchedEPCList());

                        myAsset.add(asset);
                    }
                }
            }

            try {
                Log.i("searchedEPCList", "searchedEPCList " + searchedEPCList + " " + myAsset.get(0).getEPC().equals(searchedEPCList.get(0)));
            } catch (Exception e) {
                Log.i("searchedEPCList", "searchedEPCList " + searchedEPCList);

                e.printStackTrace();
            }

            for (int i = 0; i < myAsset.size(); i++) {
                for (int y = 0; y < searchedEPCList.size(); y++) {
                    if (myAsset.get(i).getEPC().equals(searchedEPCList.get(y))) {
                        //myAsset.get(i).setFoundInSearchedEPCList(true);
                        Log.i("setFoundInSearchedEPCList", "setFoundInSearchedEPCList true");
                    }
                }
            }

            return myAsset;
        }

        ArrayList<Asset> lists = new ArrayList<>();
        ArrayList<BorrowListAsset> temp = new ArrayList<>();
        int count = 0;

        if (BORROW_LIST) {
            for (int i = 0; i < borrowList.getAssets().size(); i++) {
                boolean exist = false;
                for (int y = 0; y < borrowList.getBorrowedItem().size(); y++) {
                    if (borrowList.getAssets().get(i).getId().equals(borrowList.getBorrowedItem().get(y).getId())) {
                        exist = true;
                        Log.i("count", "count " + count++);
                        break;
                    }
                }

                Log.i("count", "count " + exist + " " + borrowList.getAssets().get(i).getId() + " ");

                if (!exist) {
                    temp.add(borrowList.getAssets().get(i));
                }
            }
        } else {

            ArrayList<Asset> assets = Hawk.get(InternalStorage.Application.ASSET, new ArrayList<>());

            for (int i = 0; i < borrowList.getAssets().size(); i++) {
                boolean exist = false;
                for (int y = 0; y < borrowList.getDisposalItem().size(); y++) {
                    if (assets.get(i).getId().equals(borrowList.getDisposalItem().get(y).getId())) {
                        exist = true;
                        Log.i("count", "count " + count++);
                        break;
                    }
                }


                if (!exist) {
                    temp.add(borrowList.getAssets().get(i));
                }
            }
        }

        Log.i("getWaitingList", "getWaitingList " + BorrowList.convertToAssetsList(temp).size());
        ArrayList<Asset> assets = BorrowList.convertToAssetsList(temp);

        return assets;
    }

    public ArrayList<Integer> getWaitingListId() {
        /*
        List<Asset> myAsset =  getWaitingList();
        ArrayList<Integer> list = new ArrayList<>();

        for(int i = 0; i < myAsset.size(); i ++) {
            for(int y = 0; y < searchedEPCList.size(); y++) {
                if(myAsset.get(i).getEPC() != null && myAsset.get(i).getEPC().length() > 0) {
                    if(searchedEPCList.get(y).equals(myAsset.get(i).getEPC())) {
                        Log.i("myAsset", "myAsset " + myAsset.get(i).getId());
                        list.add(Integer.parseInt(myAsset.get(i).getId()));
                    }
                } else {
                    if(searchedEPCList.get(y).equals(myAsset.get(i).getAssetno())) {
                        Log.i("myAsset", "myAsset " + myAsset.get(i).getId());
                        list.add(Integer.parseInt(myAsset.get(i).getId()));
                    }
                }
            }
        }

         */
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).getFound()) {
                list.add(Integer.parseInt(waitingList.get(i).getId()));
            }
        }
        return list;
    }

    public ArrayList<String> getWaitingListAssetNo() {
        List<Asset> myAsset = getWaitingList();
        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < myAsset.size(); i++) {
            for (int y = 0; y < searchedEPCList.size(); y++) {
                if (searchedEPCList.get(y).equals(myAsset.get(i).getEPC())) {
                    Log.i("myAsset", "myAsset " + myAsset.get(i).getId());
                    list.add((myAsset.get(i).getAssetno()));
                }
            }
        }
        return list;
    }

    ArrayList<Asset> myasset = Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>());

    private final class LongOperation extends AsyncTask<Void, Void, List<Asset>> {

        @Override
        protected List<Asset> doInBackground(Void... params) {
            List<Asset> data = getData();
            assetListAdapter.setData(data, getActivity());
            for (int i = 0; i < assetListAdapter.assetResponse.size(); i++) {
                List<Asset> arrayList = new ArrayList<>();
                if (StockTakeListItemFragment.stockTakeList != null) {
                    arrayList = StockTakeListItemFragment.stockTakeList.getAssets();

                } else {
                    arrayList = assetListAdapter.wishList;
                }

                for (int y = 0; y < arrayList.size(); y++) {
                    if (arrayList.get(y).getEPC().equals(assetListAdapter.getItem(i).getEPC())) {
                        assetListAdapter.getItem(i).setFoundInStockTakeList(true);
                    }
                }

                if (assetListAdapter.borrowList) {
                    assetListAdapter.getItem(i).setFoundInStockTakeList(true);
                }


                assetListAdapter.getItem(i).setFoundInSearchedEPCList(searchedEPCList.contains(assetListAdapter.getItem(i).getEPC()));
            }

            return assetListAdapter.assetResponse;
        }

        @Override
        protected void onPostExecute(List<Asset> result) {

            Log.i("yoyo", "yoyo " + assetListAdapter + " " + disposalListAdapter);

            if (assetListAdapter != null) {
                assetListAdapter.setSearchedEPCList(searchedEPCList);

                Log.i("debug", "debug case 1");

                if (ABNORMAL) {
                    Log.i("debug", "debug case 2");

                    assetListAdapter.setData(result, getActivity());
                } else {
                    Log.i("debug", "debug case 3");

                    assetListAdapter.setData(result, getActivity());
                }

                if (BorrowListItemListFragment.this.getActivity() != null) {
                    BorrowListItemListFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("debug", "debug case 4");

                            if (assetListAdapter != null) {
                                Log.i("debug", "debug case 5 " + assetListAdapter.getCount());

                                assetListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }


            if (disposalListAdapter != null) {
                disposalListAdapter.setSearchedEPCList(searchedEPCList);

                if (ABNORMAL) {
                    disposalListAdapter.setData(result, getActivity());
                } else {
                    disposalListAdapter.setData(result, getActivity());
                }


                if (BorrowListItemListFragment.this.getActivity() != null) {
                    BorrowListItemListFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (disposalListAdapter != null)
                                disposalListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            //TextView txt = (TextView) findViewById(R.id.output);
            //txt.setText("Executed"); // txt.setText(result);
        }
    }

    private List<BriefAsset> abnormalAssetCache = new ArrayList<>();
    private List<Asset> newAbnormalAssetCache = new ArrayList<>();

    private List<String> requestedEPCList = new ArrayList<>();

    List<Asset> abnormalListCache = new ArrayList<>();

    public List<Asset> getAbnormalList() {
        ArrayList<Asset> abnormalList = new ArrayList<>();

        ArrayList<Asset> temp = new ArrayList<>();
        List<Asset> waitingList = getWaitingList();
        List<Asset> borrowedList = getBorrowedList();


        ArrayList<String> mySearchedEPCList = new ArrayList<>();

        for (int i = 0; i < abnormalList.size(); i++) {
            if (searchedEPCList.contains(abnormalList.get(i).getEPC())) {

            } else {
                mySearchedEPCList.add(abnormalList.get(i).getEPC());
            }
        }

        if (abnormalList.size() == 0) {
            mySearchedEPCList = searchedEPCList;
        }

        for (int i = 0; i < mySearchedEPCList.size(); i++) {
            boolean exist = false;
            for (int y = 0; y < waitingList.size(); y++) {
                if (mySearchedEPCList != null) {
                    if (waitingList.get(y).getEPC() != null && waitingList.get(y).getEPC().equals(mySearchedEPCList.get(i))) {
                        exist = true;
                    }
                }
            }


            for (int y = 0; y < borrowedList.size(); y++) {
                if (mySearchedEPCList != null) {
                    if (borrowedList.get(y).getEPC() != null && borrowedList.get(y).getEPC().equals(mySearchedEPCList.get(i))) {
                        exist = true;
                    }
                }
            }

            boolean cacheExist = false;
            boolean requested = false;
            int cachePos = -1;

            for (int y = 0; y < requestedEPCList.size(); y++) {
                if (requestedEPCList.get(y).equals(mySearchedEPCList.get(i))) {
                    requested = true;
                    break;
                }
            }

            for (int y = 0; y < abnormalAssetCache.size(); y++) {
                if (abnormalAssetCache.get(y).getEpc().equals(mySearchedEPCList.get(i))) {
                    cacheExist = true;
                    cachePos = y;
                    break;
                }
            }

            Log.i("cacheExist", "cacheExist " + cacheExist + " " + cachePos);

            if (!requested && !exist) {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
                //ArrayList<Asset> assetArrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<Asset>());

                /*
                for(int x = 0; x < assetArrayList.size(); x++) {
                    String epc = assetArrayList.get(x).getEPC();
                    Log.i("assetArrayList", "assetArrayList epc " + epc + " " + " " + searchedEPCList.get(i) + " " + epc.equals(searchedEPCList.get(i)) );
                    if(epc.equals(searchedEPCList.get(i))) {
                        EventBus.getDefault().post(new CallbackResponseEvent(assetArrayList.get(x)));
                    }
                    //new GetBriefAssetObjectCallback()
                }

                 */
                //TODO
                //if(((MainActivity)getActivity()).isNetworkAvailable()) {
                // RetrofitClient.getSPGetWebService().getBriefAssetInfo(companyId, userid, searchedEPCList.get(i)).enqueue(new GetBriefAssetObjectCallback(true));
                //}

                if (false) {
                    RetrofitClient.getSPGetWebService().getBriefAssetInfo(companyId, userid, mySearchedEPCList.get(i)).enqueue(new GetBriefAssetObjectCallback(true));
                } else {

                    final String search = searchedEPCList.get(i);
                    final boolean currentExist = exist;

                    ExecutorService schTaskEx = Executors.newFixedThreadPool(10000);
                    schTaskEx.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("epc", search).findAll();

                            Log.i("assets", "assets " + search + " " + assetsDetail.size());

                            if (assetsDetail.size() > 0) {
                                BriefAsset briefAsset = convertAssetToBriefAsset(assetsDetail.get(0));

                                if (!currentExist) {
                                    briefAsset.setFound(true);
                                    abnormalAssetCache.add(briefAsset);
                                    requestedEPCList.add(briefAsset.getEpc());

                                    Log.i("assets", "assets add case");
                                }
                            }
                        }
                    });
                }
            }


            if (!exist) {
                if (cacheExist) {
                    Asset asset = convertBriefAssetToAsset(abnormalAssetCache.get(cachePos));
                    asset.setAbnormal(true);

                    temp.add(asset);

                } else {
                    Asset asset = new Asset();
                    asset.setEPCOnly(true);
                    asset.setEPC(mySearchedEPCList.get(i));
                    temp.add(asset);
                }
            }
        }

        Log.i("temp", "temp " + " " + mySearchedEPCList.size() + " " + temp.size());


        if (abnormalListCache.size() == 0) {
            abnormalListCache = (temp);
        } else {
            abnormalListCache.addAll(temp);
        }

        return temp;
    }


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
        asset.setFound(briefAsset.getFound());
        asset.setType(briefAsset.getType());
        asset.setProsecutionNo(briefAsset.getProsecutionNo());

        Log.i("DISPOSAL_NO", "DISPOSAL_NO " + DISPOSAL_NO + " " + briefAsset.getType() + " " + (BORROW_NO != null || DISPOSAL_NO != null));

        if (BORROW_NO != null || DISPOSAL_NO != null) {
            Status status = new Status();
            Log.i("DISPOSAL_NO case1", "DISPOSAL_NO case1");
            if (briefAsset.getType() == 0) {
                status.id = 9997;
                Log.i("DISPOSAL_NO case2", "DISPOSAL_NO case2");
            } else if (briefAsset.getType() == 1) {
                status.id = 9997;
                Log.i("DISPOSAL_NO case3", "DISPOSAL_NO case3");
            } else if (briefAsset.getType() == 2) {
                status.id = 9997;
                Log.i("DISPOSAL_NO case4", "DISPOSAL_NO case4");
            }

            if (briefAsset.getBorrowed() != null && briefAsset.getBorrowed()) {
                Log.i("DISPOSAL_NO case5", "DISPOSAL_NO case5");
                status.id = 9995;
            }

            Log.i("DISPOSAL_NO case6", "DISPOSAL_NO case6");

            Log.i("DISPOSAL_NO", "DISPOSAL_NO status " + status.id);
            //status.id = 0;
            asset.setStatus(status);
        }

        if (briefAsset.getBorrowed() != null)
            asset.setFound(briefAsset.getBorrowed());
        else if (briefAsset.getDisposed() != null)
            asset.setFound(briefAsset.getDisposed());

        asset.setReturndate("");

        ArrayList<Category> categoryArrayList = new ArrayList<>();

        if (briefAsset.getCategorys() != null) {
            for (int i = 0; i < briefAsset.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(briefAsset.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);


        ArrayList<Location> locationArrayList = new ArrayList<>();

        if (briefAsset.getLocations() != null) {
            for (int i = 0; i < briefAsset.getLocations().size(); i++) {
                Location location = new Location();
                location.setName(briefAsset.getLocations().get(i));
                locationArrayList.add(location);
            }
        }
        asset.setLocations(locationArrayList);
        return asset;
    }

    private List<Asset> selectedList = new ArrayList<>();
    private List<Asset> waitingList = new ArrayList<>();
    private List<Asset> abnomalList = new ArrayList<>();

    private List<String> waitingListEPC = new ArrayList<>();
    private List<String> abnomalListEPC = new ArrayList<>();

    public List<Asset> getData() {
        Log.i("data", "data " + (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition()));

        String filterText = ((TextView) view.findViewById(R.id.edittext)).getText().toString();

        if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
            ABNORMAL = false;

            if (selectedList.isEmpty()) {
                List<Asset> myasset = getBorrowedList();

                for (int i = 0; i < myasset.size(); i++) {
                    myasset.get(i).setFound(true);
                }
                selectedList = myasset;
            }

            return filter(selectedList, filterText);
        }

        if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 1) {
            ABNORMAL = false;
            if (waitingList.isEmpty()) {
                waitingList = getWaitingList();
                for (int i = 0; i < waitingList.size(); i++) {
                    if (waitingList.get(i).getEPC() != null && !waitingList.get(i).getEPC().isEmpty())
                        waitingListEPC.add(waitingList.get(i).getEPC());
                }
            }
            return filter(waitingList, filterText);
        }

        if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 2) {
            ABNORMAL = true;
            return filter(abnomalList, filterText);
        }
        return null;
    }


    public List<Asset> filter(List<Asset> data, String filterText) {
        if (filterText == null || filterText.length() == 0) return data;

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

    void startStopHandler(boolean buttonTrigger) {
        if (buttonTrigger)
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.mCs108Library4a.getTriggerButtonStatus());
        if (MainActivity.sharedObjects.runningInventoryBarcodeTask) {
            Toast.makeText(MainActivity.mContext, "Running barcode inventory", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean started = false;
        if (inventoryRfidTask != null)
            if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
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
            if (buttonTrigger)
                inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
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
            Log.i(TAG, "HelloK: going to setSelectedTagByTID with mDid = " + mDid + " with extra1Bank = " + extra1Bank + ", extra2Bank = " + extra2Bank + ", bNeedSelectedTagByTID = " + bNeedSelectedTagByTID);// ", bMultiBank = " + bMultiBank);
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
        } else */
        {
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
            if (DEBUG)
                MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onDestory(): VALID inventoryRfidTask");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
        }
        resetSelectData();
        MainActivity.mCs108Library4a.setVibrateTime(MainActivity.mCs108Library4a.getVibrateTime());
        if (DEBUG)
            MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onDestory(): onDestory()");
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {
        Log.i("event", "event " + event.getData());


        if (playerN != null)
            playerN.start();
        if (playerO != null)
            playerO.start();

        for (int i = 0; i < event.getData().size(); i++) {
            boolean contains = false;
            if (waitingListEPC.contains(event.getData().get(i))) {
                contains = true;

                int position = waitingListEPC.indexOf(event.getData().get(i));
                waitingList.get(position).setFound(true);
                waitingListEPC.add(event.getData().get(i));
                handleNoResult(waitingList);
                assetListAdapter.notifyDataSetChanged();
            }

            if (contains == false) {
                if (!abnomalListEPC.contains(event.getData().get(i))) {
                    List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("epc", event.getData().get(i)).findAll();

                    if (assetsDetail.size() > 0) {
                        convertAssetDetailToAsset(assetsDetail.get(0)).setAbnormal(true);

                        abnomalList.add(convertAssetDetailToAsset(assetsDetail.get(0)));
                        abnomalListEPC.add(event.getData().get(i));
                    } else {
                        Asset assets1 = new Asset();// convertAssetDetailToAsset(assetsDetail.get(0));
                        assets1.setEPC(event.getData().get(i));

                        abnomalList.add(assets1);
                        abnomalListEPC.add(event.getData().get(i));
                    }
                    handleNoResult(abnomalList);
                    assetListAdapter.notifyDataSetChanged();
                }
            }
        }


        int tabSelectedPosition = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

        if (tabSelectedPosition == 1) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(title + " (" + getData().size() + ")");
        } else if (tabSelectedPosition == 2) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(title + " (" + getData().size() + ")");
        }

        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();

        //setSearchedEPCList(event.getData());
        //if(!calling)
        //runnable.run();
        //assetListAdapter.setSearchedEPCList(event.getData());
        //new LongOperation().execute();

        if (tabPosition == 2) {
            //assetListAdapter.setSearchedEPCList(getAbnormalList());
        }
    }

    ArrayList<String> searchedEPCList = new ArrayList<>();

    public Asset convertAssetDetailToAsset(AssetsDetail assetDetail) {
        Asset asset = new Asset();
        asset.setAssetno(assetDetail.getAssetNo());
        asset.setName(assetDetail.getName());

        if (assetDetail.getStatusid() != null) {
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

        if (assetDetail.getCategorys() != null) {
            for (int i = 0; i < assetDetail.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(assetDetail.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);

        ArrayList<Location> locationArrayList = new ArrayList<>();

        if (assetDetail.getLocations() != null) {
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

    public void setSearchedEPCList(ArrayList<String> searchedEPCList) {
        for (int i = 0; i < searchedEPCList.size(); i++) {
            if (this.searchedEPCList.contains(searchedEPCList.get(i))) {

            } else {
                this.searchedEPCList.add(searchedEPCList.get(i));
            }
        }
        //this.searchedEPCList = searchedEPCList;
    }

    ArrayList<Asset> apiData = new ArrayList<>();

    private String title;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("CallbackResponseEvent", "CallbackResponseEvent " + event.getResponse() + " " + event.getResponse().getClass());

        if (event.getResponse() instanceof BorrowListAssets) {
            ArrayList<Asset> assets = new ArrayList<>();
            selectedList.clear();
            waitingList.clear();
            waitingListEPC.clear();
            abnomalList.clear();
            abnomalListEPC.clear();

            title = ((BorrowListAssets) event.getResponse()).getName();

            ((TextView) view.findViewById(R.id.toolbar_title)).setText(((BorrowListAssets) event.getResponse()).getName() + " (" + ((BorrowListAssets) event.getResponse()).getData().size() + ")");

            for (int i = 0; i < (((BorrowListAssets) event.getResponse()).getData()).size(); i++) {
                BriefAsset briefAsset = (((BorrowListAssets) event.getResponse()).getData()).get(i);
                assets.add(convertBriefAssetToAsset(briefAsset));
            }

            Log.i("CallbackResponseEvent", "CallbackResponseEvent " + assets.size());

            apiData = assets;

            //Log.i("data", "data " + BORROW_NO);

            if (BORROW_NO != null) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO, event.getResponse());
            } else if (DISPOSAL_NO != null) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + DISPOSAL_NO, event.getResponse());
            }

            setupListView(getData());

            BorrowListAssets borrowListAssets = ((BorrowListAssets) event.getResponse());

            int count = 0;
            for (int i = 0; i < borrowListAssets.getData().size(); i++) {
                if (BORROW_NO != null && borrowListAssets.getData().get(i).getBorrowed()) {
                    count++;
                }


                if (DISPOSAL_NO != null && borrowListAssets.getData().get(i).getDisposed()) {
                    count++;
                }
            }

            int total = 0;

            for (int i = 0; i < borrowListAssets.getData().size(); i++) {
                if (borrowListAssets.getData().get(i).getType() == 1) {
                    total++;
                }
            }

            if (total == count) {
                POSITITON = 0;
                if (type == 1) {
                    ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).select();
                } else {
                    ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(1).select();
                }
                view.findViewById(R.id.tab_layout).setVisibility(View.GONE);
                view.findViewById(R.id.button_panel).setVisibility(View.GONE);
                view.findViewById(R.id.scan).setVisibility(View.GONE);
            }

            //EventBus.getDefault().post(new CallbackResponseEvent(assets));
        } else if (event.getResponse() instanceof BriefAsset) {
            BriefAsset briefAsset = (BriefAsset) event.getResponse();

            boolean exist = false;

            for (int i = 0; i < abnormalAssetCache.size(); i++) {
                if (briefAsset != null && briefAsset.getEpc() != null) {
                    if (briefAsset.getEpc().equals(abnormalAssetCache.get(i).getEpc())) {
                        exist = true;
                    }
                }
            }

            if (!exist) {
                briefAsset.setFound(true);
                abnormalAssetCache.add(briefAsset);
            }

        } else if (event.getResponse() instanceof Asset) {
            Asset briefAsset = (Asset) event.getResponse();

            boolean exist = false;

            for (int i = 0; i < newAbnormalAssetCache.size(); i++) {
                if (briefAsset != null && briefAsset.getEPC() != null) {
                    if (briefAsset.getEPC().equals(newAbnormalAssetCache.get(i).getEPC())) {
                        exist = true;
                    }
                }
            }

            if (!exist) {
                briefAsset.setFound(true);
                newAbnormalAssetCache.add(briefAsset);
            }

        } else if (event.getResponse().getClass() == LoginResponse.class) {
            Hawk.put(InternalStorage.Login.USER, (LoginResponse) event.getResponse());
            if (ON_BACK_PRESS) {
                ON_BACK_PRESS = false;
                Log.i("onBackPress", "onBackPress 3");

                getActivity().onBackPressed();
            }
            //getActivity().onBackPressed();
        } else if (event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0).getClass() == String.class) {
            if (assetListAdapter != null) {
                assetListAdapter.setSearchedEPCList(new ArrayList<String>((List<String>) event.getResponse()));
                assetListAdapter.notifyDataSetChanged();
            }

            if (disposalListAdapter != null) {
                disposalListAdapter.setSearchedEPCList(new ArrayList<String>((List<String>) event.getResponse()));
                disposalListAdapter.notifyDataSetChanged();
            }

            ArrayList<Asset> myasset = Hawk.get(InternalStorage.Application.ASSET, new ArrayList<Asset>());
            ArrayList<String> notyetfoundlist = new ArrayList<>();

            for (int y = 0; y < ((List) event.getResponse()).size(); y++) {
                boolean exist = false;
                for (int x = 0; x < getAbnormalList().size(); x++) {
                    if (getAbnormalList().get(x).getEPC().equals(((List) event.getResponse()).get(y))) {
                        exist = true;
                    }
                }

                if (!exist)
                    notyetfoundlist.add(((List) event.getResponse()).get(y).toString());
            }

            for (int y = 0; y < ((List) event.getResponse()).size(); y++) {
                for (int i = 0; i < myasset.size(); i++) {
                    if (((List) event.getResponse()).get(y).equals(myasset.get(i).getEPC())) {
                        getAbnormalList().add(myasset.get(i));
                    }
                }
            }
        } else if (UPDATE_VALUE) {
            UPDATE_VALUE = false;
            ON_BACK_PRESS = true;
            if (((MainActivity) getActivity()).isURLReachable()) {
                User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
                String password = Hawk.get(InternalStorage.Login.PASSWORD, "");

                RetrofitClient.getService().login(new LoginRequest(user.getEmail(), password)).enqueue(new LoginCallback());
            }
        } else if (event.getResponse() instanceof APIResponse) {
            APIResponse apiResponse = (APIResponse) event.getResponse();

            String title = null;

            if (borrowList != null && borrowList.getName() != null) {
                title = borrowList.getName();
            }

            if (apiResponse.getStatus() == 0) {
                Log.i("onBackPress", "onBackPress 4");


                EventBus.getDefault().post(new DialogEvent(BORROW_NO != null ? getString(R.string.borrow_list) : getString(R.string.disposal_list), BORROW_NO != null ? getString(R.string.borrow_success) : getString(R.string.disposal_success)));
                getActivity().onBackPressed();
            } else if (apiResponse.getStatus() == 1 && apiResponse.getBorrowCount() > 0) {
                EventBus.getDefault().post(new DialogEvent(BORROW_NO != null ? getString(R.string.borrow_list) : getString(R.string.disposal_list), getString(R.string.lend_some).replace("x", apiResponse.getBorrowCount() + "")));
                getActivity().onBackPressed();
            } else {
                EventBus.getDefault().post(new DialogEvent(BORROW_NO != null ? getString(R.string.borrow_list) : getString(R.string.disposal_list), getString(R.string.fail)));//getString(R.string.lend_some).replace("x", apiResponse.getBorrowCount() + "")));

            }
        }
    }

    public static boolean ON_BACK_PRESS;

    public void confirmBorrowDisposal() {

        if (((MainActivity) getActivity()).isURLReachable()) {

            if (LoginFragment.SP_API) {

                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");
                String waitingListIdString = "";

                for (int i = 0; i < getWaitingListId().size(); i++) {
                    waitingListIdString += getWaitingListId().get(i) + (i == getWaitingListId().size() - 1 ? "" : ",");
                }

                Log.i("waitingList", "waitingList " + waitingListIdString + " " + BORROW_LIST);

                if (BORROW_LIST) {
                    RetrofitClient.getSPGetWebService().borrowAssets(companyId, userid, waitingListIdString, BORROW_NO).enqueue(new UpdateAssetEpcCallback());
                } else {
                    RetrofitClient.getSPGetWebService().disposalAssets(companyId, userid, waitingListIdString, DISPOSAL_NO).enqueue(new UpdateAssetEpcCallback());
                }
            }
        } else {
            //UpdateFailEvent
            savelocal();
        }
    }

    public void savelocal() {
        if (LoginFragment.SP_API) {

            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");
            String waitingListIdString = "";
            ArrayList<String> assetNoList = getWaitingListAssetNo();

            for (int i = 0; i < getWaitingListId().size(); i++) {
                waitingListIdString += getWaitingListId().get(i) + (i == getWaitingListId().size() - 1 ? "" : ",");
            }

            Log.i("waitingList", "waitingList " + waitingListIdString + " " + BORROW_LIST + " " + InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + BORROW_NO);

            //for internet access
            if (BORROW_LIST) {
                RealmResults<BorrowAssets> borrowAssets = Realm.getDefaultInstance().where(BorrowAssets.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .equalTo("borrowno", BORROW_NO).findAll();

                if (borrowAssets.size() == 0) {
                    BorrowAssets ba = new BorrowAssets();
                    ba.setCompanyid(companyId);
                    ba.setUserid(userid);
                    ba.setBorrowno(BORROW_NO);

                    ba.setPk(ba.getCompanyid() + ba.getUserid() + ba.getBorrowno());
                    ba.setBorrowList(waitingListIdString);

                    Realm.getDefaultInstance().beginTransaction();
                    Realm.getDefaultInstance().insertOrUpdate(ba);
                    Realm.getDefaultInstance().commitTransaction();
                } else {
                    Realm.getDefaultInstance().beginTransaction();

                    BorrowAssets ba = Realm.getDefaultInstance().copyFromRealm(borrowAssets.get(0));

                    String data = "";

                    Set<String> foo = new HashSet<String>(Arrays.asList(ba.getBorrowList().split(",")));

                    for (int i = 0; i < getWaitingListId().size(); i++) {
                        foo.add(getWaitingListId().get(i) + "");
                    }


                    for (String s : foo) {
                        data = data + s + ",";
                    }

                    if (data.length() > 0) {
                        data = data.substring(0, data.length() - 1);
                    }

                    ba.setBorrowList(data);
                    Realm.getDefaultInstance().insertOrUpdate(ba);
                    Realm.getDefaultInstance().commitTransaction();

                }

                ((MainActivity) getActivity()).updateDrawerStatus();
                EventBus.getDefault().post(new DialogEvent(getActivity().getString(R.string.app_name), getActivity().getString(R.string.upload_tips)));
                EventBus.getDefault().post(new OnBackPressEvent());
            } else {
                    /*BorrowListRequest borrowListRequest = new BorrowListRequest();
                    borrowListRequest.setCompanyid(companyId);
                    borrowListRequest.setUserid(userid);
                    borrowListRequest.setWaitiList(waitingListIdString);
                    borrowListRequest.setAssetNoList(assetNoList);
                    borrowListRequest.setBorrowno(DISPOSAL_NO);
                    borrowListRequest.setTitle( borrowList.getName());

                    ArrayList<BorrowListRequest> borrowListRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_BORROW_REQUEST, new ArrayList<BorrowListRequest>());
                    borrowListRequests.add(borrowListRequest);

                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_PENDING_DISPOSAL_REQUEST,borrowListRequests);*/
                //Log.i("onBackPress", "onBackPress 2");

                RealmResults<DisposalAssets> borrowAssets = Realm.getDefaultInstance().where(DisposalAssets.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .equalTo("disposalNo", DISPOSAL_NO).findAll();

                Realm.getDefaultInstance().beginTransaction();

                if (borrowAssets.size() == 0) {
                    DisposalAssets ba = new DisposalAssets();
                    ba.setCompanyid(companyId);
                    ba.setUserid(userid);
                    ba.setDisposalNo(DISPOSAL_NO);

                    ba.setPk(ba.getCompanyid() + ba.getUserid() + ba.getDisposalNo());
                    ba.setDisposalList(waitingListIdString);

                    Realm.getDefaultInstance().insertOrUpdate(ba);
                } else {
                    DisposalAssets ba = borrowAssets.get(0);

                    String data = "";

                    Set<String> foo = new HashSet<String>(Arrays.asList(ba.getDisposalList().split(",")));

                    for (int i = 0; i < getWaitingListId().size(); i++) {
                        foo.add(getWaitingListId().get(i) + "");
                    }


                    for (String s : foo) {
                        data = data + s + ",";
                    }

                    if (data.length() > 0) {
                        data = data.substring(0, data.length() - 1);
                    }

                    ba.setDisposalList(data);
                    Realm.getDefaultInstance().insertOrUpdate(ba);
                }

                Realm.getDefaultInstance().commitTransaction();

                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_tips)));

                ((MainActivity) getActivity()).updateDrawerStatus();
                getActivity().onBackPressed();
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateFailEvent event) {
        savelocal();

    }
}