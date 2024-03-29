package com.spit.fam.SystemFragment;

import static com.spit.fam.nike.ExtKt.POWER_OTHER;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.CustomMediaPlayer;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.OfflineMode.ChangeEpc;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.ModifyAssetRequest;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryRfidTask;
import com.spit.fam.MainActivity;
import com.spit.fam.NewHandHeld.MyUtil;
import com.spit.fam.R;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.SaveList2ExternalTask;
import com.spit.fam.SystemFragment.Adapter.EpcOnlyAdapter;
import com.spit.fam.SystemFragment.Adapter.SimpleReaderListAdapter;
import com.spit.fam.WebService.Callback.NewAssetDetailCallback;
import com.spit.fam.WebService.Callback.SPWebServiceCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.adapters.ReaderListAdapter;
import com.spit.fam.fragments.ConnectionFragment;
import com.spit.fam.fragments.HomeFragment;
import com.spit.fam.fragments.InventoryRfidiMultiFragment;
import com.spit.cs108library4a.Cs108Connector;
import com.spit.cs108library4a.Cs108Library4A;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;

public class AssetChangeFragment extends HomeFragment {
    public static AssetChangeFragment newInstance() {
        return  new AssetChangeFragment();
    }

    final private boolean bAdd2End = false;
    private boolean bMultiBank = false, bMultiBankInventory = false, bBapInventory = false, bctesiusInventory = false;
    private String mDid = null;
    int vibrateTimeBackup = 0;

    private CheckBox checkBoxDupElim;
    private Spinner spinnerBank1, spinnerBank2;
    private ListView rfidListView;
    private TextView rfidEmptyView;
    private TextView rfidRunTime, rfidVoltageLevel;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    private Button button;

    private ReaderListAdapter readerListAdapter;
    private EpcOnlyAdapter epcOnlyAdapter;

    private InventoryRfidTask inventoryRfidTask;

    public static String SELECTED = null;
    CustomMediaPlayer playerO, playerN;

    public AssetChangeFragment() {
        clearTagsList();
        SELECTED = null;
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
            rfidYieldView.setText("");
            rfidRateView.setText("");
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, bMultiBankInventory | bBapInventory | bctesiusInventory);
        return inflater.inflate(R.layout.assets_change_fragment, container, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAction_1:
                clearTagsList();
                return true;
            case R.id.menuAction_2:
                sortTagsList();
                return true;
            case R.id.menuAction_3:
                saveTagsList();
                return true;
            case R.id.menuAction_4:
                shareTagsList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerO = MainActivity.sharedObjects.playerO;
        playerN = MainActivity.sharedObjects.playerN;



        ArrayList<String> strings = new ArrayList<>();
        //strings.add("12sadasd312");
        //strings.add("12sadsad311");
        //strings.add("12sadsad310");

        //RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(strings);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //EventBus.getDefault().post(rfidDataUpdateEvent);
            }
        }, 2000);


        if (getArguments() != null) {
            bMultiBank = getArguments().getBoolean("bMultiBank");
            mDid = getArguments().getString("mDid");
            if (bMultiBank && mDid == null) {
                bMultiBankInventory = true;
            } else if (bMultiBank && mDid != null) {
                if (mDid.matches("E200B0")) {
                    bBapInventory = true;
                } else if (mDid.matches("E203510")) {
                    bctesiusInventory = true;
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        (getActivity().findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SELECTED", "SELECTED " + SimpleReaderListAdapter.SELECTED);
                if(EpcOnlyAdapter.position == -1) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.change_epc), getString(R.string.nothing_selected)));
                    return;
                } else {

                    String changedEPC = epcOnlyAdapter.getItem(EpcOnlyAdapter.position);
                    Log.i("API SELECTED", "API SELECTED " + SimpleReaderListAdapter.SELECTED);

                    if( ((MainActivity)getActivity()).isNetworkAvailable())  {
                        Log.i("callingAPI", "callingAPI @ AssetChangeFragment");
                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                        String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");


                        RetrofitClient.getSPGetWebService().changeEpc(companyId, userid, AssetsDetailWithTabFragment.asset.getAssetno(), epcOnlyAdapter.getItem(EpcOnlyAdapter.position)).enqueue(new SPWebServiceCallback());

                    } else {
                        Log.i("localCache", "localCache @ AssetChangeFragment");

                        List<Asset> registrationList = Hawk.get(InternalStorage.OFFLINE_CACHE.REGISTRATION, new ArrayList<>());
                        List<Asset> originalList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<>());

                        boolean alreadyExist = false;
                        int pos = -1;

                        for(int i = 0; i < originalList.size(); i ++) {
                            if (originalList.get(i).getEPC().equals(changedEPC) || (originalList.get(i).getNewEPC() != null && originalList.get(i).getNewEPC().equals(changedEPC))) {
                                alreadyExist = true;
                                break;
                            }


                            if(originalList.get(i).getAssetno().equals(AssetsDetailWithTabFragment.asset.getAssetno())){
                                pos = i;
                            }
                        }

                        for(int i = 0; i < registrationList.size(); i ++) {
                            if (registrationList.get(i).getEPC().equals(changedEPC) || (registrationList.get(i).getNewEPC() != null && registrationList.get(i).getNewEPC().equals(changedEPC))) {
                                alreadyExist = true;
                                //pos = i;
                                break;
                            }

                        }

                        Log.i("alreadyExist", "alreadyExist " + alreadyExist);


                        if(alreadyExist) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.app_name))
                                    .setMessage(getString(R.string.epc_already_exist))

                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();

                            return;
                        } else {
                            if(pos != -1) {
                                originalList.get(pos).setNewEPC(changedEPC);
                                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, originalList);
                            }
                        }

                        //List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + AssetsDetailWithTabFragment.asset.getAssetno(), null);
                        List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("assetNo", AssetsDetailWithTabFragment.asset.getAssetno()).findAll();// dataBaseHandler.searchAssetsDetail(ASSET_NO, "", "", "", "", "", "", "", "");//.size());//MainActivity.getAssetsDetailList(ASSET_NO);//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);

                        AssetsDetail ad = Realm.getDefaultInstance().copyFromRealm(assetsDetail.get(0));
                        ad.setNewEpc(changedEPC);

                        assetsDetail = new ArrayList<>();
                        assetsDetail.add(ad);

                        Log.i("hihi", "hihi " + assetsDetail.get(0).getNewEpc() + " " + AssetsDetailWithTabFragment.asset.getAssetno());

                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + AssetsDetailWithTabFragment.asset.getAssetno(), assetsDetail);

                        Log.i("hihi", "hihi " + assetsDetail.get(0).getNewEpc() + " " + AssetsDetailWithTabFragment.asset.getAssetno() + " " +  Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + AssetsDetailWithTabFragment.asset.getAssetno(), new ArrayList<AssetsDetail>()).get(0).getNewEpc());

                        ArrayList<ModifyAssetRequest> modifyAssetRequests = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                        ModifyAssetRequest modifyAssetRequest = new ModifyAssetRequest();

                        for(int i = 0; i < modifyAssetRequests.size(); i++) {
                            if(modifyAssetRequests.get(i).getAssetno().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                                modifyAssetRequest = modifyAssetRequests.get(i);
                            }
                        }

                        modifyAssetRequest.setAssetno((AssetsDetailWithTabFragment.asset.getAssetno()));
                        modifyAssetRequest.setEPC(changedEPC);
                        modifyAssetRequest.setCompanyid(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                        modifyAssetRequest.setUserid(Hawk.get(InternalStorage.Login.USER_ID, ""));
                        modifyAssetRequest.changeEPC = true;

                        Log.i("PUT", "PUT PENDING_CHANGE_EPC_REQUEST " + AssetsDetailWithTabFragment.asset.getAssetno());

                        boolean exist = false;
                        for(int i = 0; i < modifyAssetRequests.size(); i++) {
                            if(modifyAssetRequests.get(i).getAssetno().equals(AssetsDetailWithTabFragment.asset.getAssetno())) {
                                exist = true;
                                modifyAssetRequests.get(i).setAssetno((AssetsDetailWithTabFragment.asset.getAssetno()));
                                modifyAssetRequests.get(i).setEPC(changedEPC);
                                modifyAssetRequests.get(i).setCompanyid(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                                modifyAssetRequests.get(i).setUserid(Hawk.get(InternalStorage.Login.USER_ID, ""));
                                modifyAssetRequests.get(i).changeEPC = true;
                            }
                        }

                        if(!exist)
                            modifyAssetRequests.add(modifyAssetRequest);

                        //Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, modifyAssetRequests);

                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_tips)));

                        String data = FileUtils.readFromFile("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/Upload/", "changeEpc");

                        ChangeEpc changeEpc = new ChangeEpc();
                        changeEpc.setEpc(changedEPC);
                        changeEpc.setCompanyid(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));
                        changeEpc.setAssetno(AssetsDetailWithTabFragment.asset.getAssetno());
                        changeEpc.setUserid(Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""));
                        changeEpc.setPk(changeEpc.getCompanyid()+changeEpc.getUserid()+changeEpc.getAssetno());

                        Realm.getDefaultInstance().beginTransaction();
                        Realm.getDefaultInstance().insertOrUpdate(changeEpc);
                        Realm.getDefaultInstance().commitTransaction();

                        ((MainActivity)getActivity()).updateDrawerStatus();

                        getActivity().onBackPressed();
                        /*
                        ArrayList<Asset> assets = Hawk.get(InternalStorage.OFFLINE_CACHE.ASSET, new ArrayList<>());
                        Asset result = null;

                        for(int i = 0; i < assets.size(); i++) {
                            if(assets.get(i).equals(AssetsDetailWithTabFragment.asset.getEPC())) {
                                result = assets.get(i);
                                result.setEPC(SELECTED);

                                break;
                            }
                        }

                        if(result != null) {
                            Hawk.put(InternalStorage.OFFLINE_CACHE.ASSET, result);//new ArrayList<>());
                        }

                        EventBus.getDefault().post(new CallbackResponseEvent(new APIResponse()));

                        ArrayList<ModifyAssetRequest> data = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());

                        ModifyAssetRequest modifyAssetRequest = new ModifyAssetRequest();
                        modifyAssetRequest.setId(Integer.parseInt(AssetsDetailWithTabFragment.asset.getId()));
                        modifyAssetRequest.setEPC(SimpleReaderListAdapter.SELECTED);

                        data.add(modifyAssetRequest);

                        Hawk.put(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, data);// new ArrayList<ModifyAssetRequest>());*/
                    }

                }
            }
        });

        ((TextView)getActivity().findViewById(R.id.old_epc)).setText(AssetsDetailWithTabFragment.asset.getEPC());

        MainActivity.selectFor = -1;
        if (bMultiBankInventory | bBapInventory | bctesiusInventory) {
            android.support.v7.app.ActionBar actionBar;
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setIcon(R.drawable.dl_inv);
            if (bMultiBankInventory) actionBar.setTitle("M"); //"Multibank");
            else if (bBapInventory) actionBar.setTitle("B"); //"BAP Inventory");
            else if (bctesiusInventory) actionBar.setTitle("C"); //"CTESIUS Inventory");
        } else if (bMultiBankInventory) {
            LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.inventoryMultibankSetting);
            linearLayout.setVisibility(View.VISIBLE);
            checkBoxDupElim = (CheckBox) getActivity().findViewById(R.id.accessInventoryDupElim);
            checkBoxDupElim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBoxDupElim.isChecked()) readerListAdapter.setSelectDupElim(true);
                    else readerListAdapter.setSelectDupElim(false);
                }
            });
        }

        ArrayAdapter<CharSequence> lockAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.memoryBank_options, R.layout.custom_spinner_layout);
        lockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBank1 = (Spinner) getActivity().findViewById(R.id.accessInventoryBank1);
        if (getActivity() == null) Log.i("Hello", "InventoryRfidMultiFragment.onActivityCreated: NULL getActivity()");
        else Log.i("Hello", "InventoryRfidMultiFragment.onActivityCreated: VALID getActivity()");
        if (spinnerBank1 == null) Log.i("Hello", "InventoryRfidMultiFragment.onActivityCreated: NULL spinnerBank1");
        else Log.i("Hello", "InventoryRfidMultiFragment.onActivityCreated: VALID spinnerBank1");
        if (lockAdapter == null) Log.i("Hello", "InventoryRfidMultiFragment.onActivityCreated: NULL lockAdapter");
        else Log.i("Hello", "InventoryRfidMultiFragment.onActivityCreated: VALID lockAdapter");
        spinnerBank1.setAdapter(lockAdapter); spinnerBank1.setSelection(2);
        spinnerBank2 = (Spinner) getActivity().findViewById(R.id.accessInventoryBank2);
        spinnerBank2.setAdapter(lockAdapter); spinnerBank2.setSelection(3);

        rfidListView = (ListView) getActivity().findViewById(R.id.inventoryRfidList1);
        rfidEmptyView = (TextView) getActivity().findViewById(R.id.inventoryRfidEmpty1);
        rfidListView.setEmptyView(rfidEmptyView);
        boolean bSelect4detail = true;
        if (bMultiBankInventory) bSelect4detail = false;
        boolean needDupElim = true;

        boolean need4Extra1 = MainActivity.mCs108Library4a.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = (mDid != null ? true : false);

        readerListAdapter = new SimpleReaderListAdapter(getActivity(), R.layout.reader_list_item_clone, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);
        epcOnlyAdapter = new EpcOnlyAdapter(new ArrayList<>());

        rfidListView.setAdapter(epcOnlyAdapter);

        rfidRunTime = (TextView) getActivity().findViewById(R.id.inventoryRfidRunTime1);
        rfidVoltageLevel = (TextView) getActivity().findViewById(R.id.inventoryRfidVoltageLevel1);
        TextView rfidFilterOn = (TextView) getActivity().findViewById(R.id.inventoryRfidFilterOn1);
        if (mDid != null || (MainActivity.mCs108Library4a.getSelectEnable() == false && MainActivity.mCs108Library4a.getInvMatchEnable() == false))
            rfidFilterOn.setVisibility(View.INVISIBLE);

        rfidYieldView = (TextView) getActivity().findViewById(R.id.inventoryRfidYield1);
        rfidRateView = (TextView) getActivity().findViewById(R.id.inventoryRfidRate1);
        button = (Button) getActivity().findViewById(R.id.inventoryRfidButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startStopHandler(false);

                int power = Hawk.get(POWER_OTHER, -1);
                if (power == -1){
                    power = 32;
                }
                byte[] bytes = new byte[]{(byte) power};
                MsgPowerConfig pMsg = new MsgPowerConfig(bytes);
//        MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt(/*"30"*/ (int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
                MyUtil.reader.Send(pMsg);
                SessionInfo si = new SessionInfo();

                //si.Session = Session.values()[0];
                //si.Flag = Flag.values()[2];
                si.Session = Session.S0;
                si.Flag = Flag.Flag_A_B;

                MsgSessionConfig msgS = new MsgSessionConfig(si);
                MyUtil.reader.Send(msgS);

                byte q = 4;
                MsgQValueConfig msg = new MsgQValueConfig(q);
                MyUtil.reader.Send(msg, 500);

                if(button.getText().toString().equals(getString(R.string.start))) {
                    button.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();
                } else {
                    button.setText(getString(R.string.start));
                    ((MainActivity) MainActivity.mContext).stop();
                }
            }
        });

        vibrateTimeBackup = MainActivity.mCs108Library4a.getVibrateTime();
        final Button buttonT1 = (Button) getActivity().findViewById(R.id.inventoryRfidButtonT1);
        buttonT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = buttonT1.getText().toString().trim();
                if (buttonText.toUpperCase().matches("BUZ")) {
                    MainActivity.mCs108Library4a.setVibrateTime(0); MainActivity.mCs108Library4a.setVibrateOn(1);
                    buttonT1.setText("STOP");
                }
                else {
                    MainActivity.mCs108Library4a.setVibrateOn(0);
                    buttonT1.setText("BUZ");
                }
            }
        });
    }

    private boolean onResume;

    @Override
    public void onResume() {
        super.onResume();
        onResume = true;
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onResume(): userVisibleHint = " + userVisibleHint);
        if (userVisibleHint) {
            setNotificationListener();
        }
    }

    @Override
    public void onPause() {
        onResume = false;
        ((MainActivity) MainActivity.mContext).stop();

        // MainActivity.mCs108Library4a.setNotificationListener(null);
        super.onPause();
        EpcOnlyAdapter.position = -1;

        boolean started = false;
        if (inventoryRfidTask != null) if (inventoryRfidTask.getStatus() == AsyncTask.Status.RUNNING) started = true;

        if(started) {
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            try {
                button.setText(getString(R.string.start));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        // MainActivity.mCs108Library4a.setNotificationListener(null);
        if (inventoryRfidTask != null) {
            if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onDestory(): VALID inventoryRfidTask");
            inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
        }
        resetSelectData();
        MainActivity.mCs108Library4a.setVibrateTime(vibrateTimeBackup);
        if (DEBUG) MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment().onDestory(): onDestory()");
        super.onDestroy();
    }

    boolean userVisibleHint = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment is now VISIBLE");
            setNotificationListener();
        } else {
            userVisibleHint = false;
            MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment is now INVISIBLE");
            //MainActivity.mCs108Library4a.setNotificationListener(null);
            try {
                button.setText(getString(R.string.start));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (inventoryRfidTask != null) {
                inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            }
        }
    }

    public static InventoryRfidiMultiFragment newInstance(boolean bMultiBank, String mDid) {
        InventoryRfidiMultiFragment myFragment = new InventoryRfidiMultiFragment();

        Bundle args = new Bundle();
        args.putBoolean("bMultiBank", bMultiBank);
        args.putString("mDid", mDid);
        myFragment.setArguments(args);

        return myFragment;
    }


    void setNotificationListener() {
        MainActivity.mCs108Library4a.setNotificationListener(new Cs108Connector.NotificationListener() {
            @Override
            public void onChange() {
                MainActivity.mCs108Library4a.appendToLog("TRIGGER key is pressed.");
                startStopHandler(true);
            }
        });
    }

    boolean needResetData = false;
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
                //replaceFragment(new ConnectionFragment());
                return;
            }
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            else rfidListView.setSelection(0);
            startInventoryTask();
        } else {
            MainActivity.mCs108Library4a.appendToLogView("CANCELLING. Set taskCancelReason");
            if (bAdd2End) rfidListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            if (buttonTrigger) inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else    inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
        }
    }

    void startInventoryTask() {
        int extra1Bank = -1, extra2Bank = -1;
        int extra1Count = 0, extra2Count = 0;
        int extra1Offset = 0, extra2Offset = 0;
        String mDid = this.mDid;

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
            boolean bNeedSelectedTagByTID = true;
            if (mDid.matches("E2806894")) {
                if (MainActivity.mDid.matches("E2806894A")) {
                    MainActivity.mCs108Library4a.setInvBrandId(false);
                    MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
                } else if (MainActivity.mDid.matches("E2806894B")) {
                    MainActivity.mCs108Library4a.setInvBrandId(false);
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 1, 0x203, "1", true);
                    bNeedSelectedTagByTID = false;
                } else if (MainActivity.mDid.matches("E2806894C")) {
                    MainActivity.mCs108Library4a.setInvBrandId(true);
                    MainActivity.mCs108Library4a.setSelectCriteria(1, true, 4, 0, 1, 0x204, "1", true);
                    bNeedSelectedTagByTID = false;
                }
            } else if (mDid.indexOf("E28011") == 0) bNeedSelectedTagByTID = false;
            if (bNeedSelectedTagByTID) MainActivity.mCs108Library4a.setSelectedTagByTID(mDid, 300);
        } else if (bMultiBankInventory) {
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
        }

        if (bMultiBank == false) {
            MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT);
            Log.i("case 1", "case 1");
            inventoryRfidTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0,
                    false, MainActivity.mCs108Library4a.getInventoryBeep(),
                    MainActivity.sharedObjects.tagsList, readerListAdapter, null, null,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
        } else {
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
                    MainActivity.sharedObjects.tagsList, null, null, mDid,
                    rfidRunTime, null, rfidVoltageLevel, rfidYieldView, button, rfidRateView);
        }
        //new Handler().postDelayed(new Runnable() {
        //    @Override
        //    public void run() {
        inventoryRfidTask.execute();
        //    }
        //}, 1000);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {
        Log.i("RFIDDataUpdateEvent","RFIDDataUpdateEvent " +AssetsDetailWithTabFragment.position);

        if(AssetsDetailWithTabFragment.position == 2) {
            if (playerN != null)
                playerN.start();
            //if (playerO != null)
            //    playerO.start();

            ArrayList<String> epcList = epcOnlyAdapter.getEpcList();

            for (int i = 0; i < event.getData().size(); i++) {
                if (!epcList.contains(event.getData().get(i))) {
                    epcList.add(event.getData().get(i));
                }
            }

            epcOnlyAdapter.setEpcList(epcList);
            epcOnlyAdapter.notifyDataSetChanged();
        }
        //readerListAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {

        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0) instanceof APIResponse) {
            APIResponse apiResponse = (APIResponse) ((List)event.getResponse()).get(0);
            if(apiResponse.getStatus() == 0) {

                EventBus.getDefault().post(new DialogEvent(getString(R.string.change_epc), getString(R.string.success_binding)));
                if( ((MainActivity)getActivity()).isNetworkAvailable()) {
                    RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID,""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,""), AssetsDetailWithTabFragment.ASSET_NO, "").enqueue(new NewAssetDetailCallback("1"));
                } else {
                    getActivity().onBackPressed();
                }
                //getActivity().onBackPressed();
            } else {
                EventBus.getDefault().post(new DialogEvent(getString(R.string.change_epc), getString(R.string.binding_fail)));
            }
        } else {
            /*new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.app_name))
                    .setMessage("Pair Success")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().onBackPressed();
                        }
                    })
                    .show();*/
        }
        //getActivity().onBackPressed();
        //   readerListAdapter.notifyDataSetChanged();
    }


    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        handler.postDelayed(runnable, 1000);
    }

    private boolean started = false;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(started) {
                start();
            }
        }
    };
}