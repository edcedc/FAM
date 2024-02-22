package com.spit.fam.SystemFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.Item;
import com.spit.fam.Entity.TempItem;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.InventoryRfidTask;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.Adapter.SimpleReaderListAdapter;
import com.spit.fam.fragments.ConnectionFragment;
import com.spit.fam.fragments.HomeFragment;
import com.spit.cs108library4a.Cs108Library4A;
import com.google.zxing.integration.android.IntentIntegrator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

public class NewStockTakeListItemFragment extends HomeFragment {
    public static String stockTakeListId;
    public static String stockTakeName;

    boolean scannerOpen = false;

    ListView listView, rfidListView;
    View noResult;
    public static int tabPosition;

    Button start;

    private InventoryRfidTask inventoryRfidTask;
    SimpleReaderListAdapter readerListAdapter;
    InventoryBarcodeTask inventoryBarcodeTask;

    HashMap<String, Integer> hashMap = new HashMap<>();

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        List<Item> list = Realm.getDefaultInstance().where(Item.class).equalTo("stocktakeno", "" + stockTakeListId).findAll();
        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().delete(TempItem.class);

        for(int i = 0; i < list.size(); i++) {

            Item item = list.get(i);

            TempItem tempItem = new TempItem();
            tempItem.setStocktakeno(item.getStocktakeno());
            tempItem.setAssetno(item.getAssetno());
            tempItem.setBrand(item.getBrand());
            tempItem.setCategory(item.getCategory());
            tempItem.setEpc(item.getEpc());

            tempItem.setId(item.getId());
            tempItem.setLocation(item.getLocation());
            tempItem.setModel(item.getModel());
            tempItem.setName(item.getName());
            tempItem.setPic(item.getPic());

            tempItem.setPk(item.getPk());
            tempItem.setRemarks(item.getRemarks());
            tempItem.setStatusid(item.getStatusid());

            Realm.getDefaultInstance().insertOrUpdate(tempItem);
        }
        Realm.getDefaultInstance().commitTransaction();

        for(int i = 0; i < list.size(); i++) {
            hashMap.put(list.get(i).getEpc(),i);
        }

        tabPosition = 0;

        MainActivity.sharedObjects.tagsList.clear();
        MainActivity.sharedObjects.tagsIndexList.clear();
        view = LayoutInflater.from(getActivity()).inflate(R.layout.stock_take_list_item_fragment, null);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("34152A84A000000000001501");
        arrayList.add("34152A84A00000000000028F");
        //arrayList.add("D00000000000ASDD00000009");

        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {

                //EventBus.getDefault().post(new RFIDDataUpdateEvent(arrayList));
            }
        };

        new Handler().postDelayed(runnable2, 2000);

        boolean bSelect4detail = true;
        boolean needDupElim = true;
        boolean need4Extra1 = MainActivity.mCs108Library4a.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = ( false);
        listView = view.findViewById(R.id.listview);
        rfidListView = view.findViewById(R.id.rfidlistview);

        readerListAdapter = new SimpleReaderListAdapter(getActivity(), R.layout.reader_list_item_clone, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);
        rfidListView.setAdapter(readerListAdapter);


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

                if (started) {
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
                if (delayNeeded) {
                    handler.postDelayed(runnable, 1000);
                } else {
                    handler.post(runnable);
                }
            }
        });

        listView = view.findViewById(R.id.listview);
        rfidListView = view.findViewById(R.id.rfidlistview);
        noResult = view.findViewById(R.id.no_result);
        start = (Button) view.findViewById(R.id.start);



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
                ((MainActivity) getActivity()).updateDrawerStatus();
                ((MainActivity) getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).onBackPressed();
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

                setupListView(getData());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setupListView(getData());
    }

    private List<Item> all, read, unread, abnormal;

    public List<TempItem> getData() {
        Log.i("position" , "position " + tabPosition);

        List<TempItem> result = null;

        if (tabPosition == 0) {
            result = Realm.getDefaultInstance().where(TempItem.class).equalTo("stocktakeno", "" + stockTakeListId)
                    .beginGroup()
                    .equalTo("statusid", 2)
                    .or()
                    .equalTo("statusid", 10)
                    .endGroup()
                    .findAll();
        } else if (tabPosition == 1) {
            result = Realm.getDefaultInstance().where(TempItem.class).equalTo("stocktakeno", "" + stockTakeListId)
                    .equalTo("statusid", 2)
                    .findAll();
        } else if (tabPosition == 2) {
            result = Realm.getDefaultInstance().where(TempItem.class).equalTo("stocktakeno", "" + stockTakeListId)
                    .equalTo("statusid", 10)
                    .findAll();
        } else if (tabPosition == 3) {
            result = Realm.getDefaultInstance().where(TempItem.class).equalTo("stocktakeno", "" + stockTakeListId)
                    .equalTo("statusid", 9)
                    .findAll();
        }

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(stockTakeName + " (" + result.size() + ")");


        return result;//new ArrayList<>();
    }

    NewStockTakeListAdapter newStockTakeListAdapter = null;
    public void setupListView(List<TempItem> data) {
        Log.i("listView", "listView "+  stockTakeListId + " " + data.size());
        if(newStockTakeListAdapter == null) {
            newStockTakeListAdapter = new NewStockTakeListAdapter(data);
            listView.setAdapter(newStockTakeListAdapter);
        } else {
            newStockTakeListAdapter.setData(data);
            newStockTakeListAdapter.notifyDataSetChanged();
        }
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li, vg, b);
        return view;
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

    private boolean needResetData;

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

            Log.i("case 2", "case 2");
            inventoryRfidTask = new InventoryRfidTask(getContext(), extra1Bank, extra2Bank, extra1Count, extra2Count, extra1Offset, extra2Offset,
                    false, MainActivity.mCs108Library4a.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, mDid,
                    null/*rfidRunTime*/, null, null/*rfidVoltageLevel*/, null/*rfidYieldView*/, start, null/*rfidRateView*/);
        }
        inventoryRfidTask.execute();
    }


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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {

        int exist = -1;
        for(int i = 0; i < event.getData().size(); i++) {

            // 2
            // 10
            // 9

            Log.i("found", "found " + Realm.getDefaultInstance().where(TempItem.class).equalTo("epc", event.getData().get(i)).beginGroup().equalTo("statusid", 10).or().equalTo("statusid", 2).endGroup().findAll().size());

            if(Realm.getDefaultInstance().where(TempItem.class).equalTo("epc", event.getData().get(i)).equalTo("statusid", 10).findAll().size() > 0) {
                List<TempItem> arrayList = Realm.getDefaultInstance().where(TempItem.class).equalTo("epc", event.getData().get(i)).equalTo("statusid", 10).findAll();

                Log.i("arrayList", "arrayList " + arrayList.size());

                View view = listView.getChildAt(hashMap.get(arrayList.get(0).getEpc()));
                listView.getAdapter().getView(hashMap.get(arrayList.get(0).getEpc()), view, listView);
               // view.invalidate();
                //newStockTakeListAdapter.notifyDataSetInvalidated();

                exist = hashMap.get(arrayList.get(0).getEpc());

                Realm.getDefaultInstance().beginTransaction();
                arrayList.get(0).setStatusid(2);
                Realm.getDefaultInstance().commitTransaction();

            } else if(Realm.getDefaultInstance().where(TempItem.class).equalTo("epc", event.getData().get(i)).beginGroup().equalTo("statusid", 10).or().equalTo("statusid", 2).endGroup().findAll().size() == 0)  {
                TempItem item = new TempItem();
                item.setAssetno("");
                item.setPk(stockTakeListId + event.getData().get(i));
                item.setEpc(event.getData().get(i));
                item.setStocktakeno(stockTakeListId + "");
                item.setStatusid(9);

                Log.i("abnormal", "abnormal " +event.getData().get(i) + " " + stockTakeListId);

                Realm.getDefaultInstance().insertOrUpdate(item);
            }
        }




        for(int i = 0; i < event.getData().size(); i++) {

            try {
                View view = listView.getChildAt(hashMap.get(event.getData().get(i)));
                listView.getAdapter().getView(hashMap.get(event.getData().get(i)), view, listView);
                view.invalidate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(exist == -1)
            setupListView(getData());
    }


    public void onPause(){
        super.onPause();
        boolean started = false;
        if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        if (  ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus()) || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false))) {
            return;
        }

        if(started) {
            startStopHandler(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {

    }
}
