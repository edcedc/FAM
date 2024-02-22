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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.Tray;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.InventoryBarcodeTask;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.Adapter.SimpleReaderListAdapter;
import com.spit.fam.WebService.Callback.NewAssetDetailCallback;
import com.spit.fam.WebService.Callback.TrayListCallBack;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.adapters.ReaderListAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

public class DepositRecieveFragment extends BaseFragment {
    private ReaderListAdapter readerListAdapter;
    private HashMap<String,AssetsDetail> recieveList = new HashMap<String,AssetsDetail>();
    private HashMap<String,AssetsDetail> depositList = new HashMap<String, AssetsDetail>();

    private AssetListAdapter depositAdapter;
    private AssetListAdapter recieveAdapter;
    private String tray = null;
    private String tempBarcode = null;

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String userId = Hawk.get(InternalStorage.Login.USER_ID, "");

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.deposit_fragment, null);

        boolean bSelect4detail = true;
        boolean needDupElim = true;
        boolean need4Extra1 = MainActivity.mCs108Library4a.getPortNumber() > 1 ? true : false;
        boolean need4Extra2 = false;
        readerListAdapter = new SimpleReaderListAdapter(getActivity(), R.layout.reader_list_item_clone, MainActivity.sharedObjects.tagsList, bSelect4detail, true, needDupElim, need4Extra1, need4Extra2);

        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.add).setVisibility(View.GONE);

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).updateDrawerStatus();
                ((MainActivity) getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.receive_deposit).toUpperCase());

        ((TabLayout) view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    ((LinearLayout) view.findViewById(R.id.deposit_bar)).setVisibility(View.GONE);
                    recieveAdapter = new AssetListAdapter(recieveList);
                    ((ListView) view.findViewById(R.id.listview)).setAdapter(recieveAdapter);
                } else if (tab.getPosition() == 0) {
                    ((LinearLayout) view.findViewById(R.id.deposit_bar)).setVisibility(View.VISIBLE);
                    depositAdapter = new AssetListAdapter(depositList);
                    ((ListView) view.findViewById(R.id.listview)).setAdapter(depositAdapter);
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

        view.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempBarcode = null;
                openBarcodeScanner();
            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
                    if(tray == null) {
                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.tray_not_set)));
                        return;
                    }

                    if(depositList.size() == 0) {
                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.no_asset_selected)));
                        return;
                    }
                } else {
                    if(recieveList.size() == 0) {
                        EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.no_asset_selected)));
                        return;
                    }
                }
            }
        });

        if(depositAdapter == null) {
            depositAdapter = new AssetListAdapter(depositList);
        }
        ((ListView) view.findViewById(R.id.listview)).setAdapter(depositAdapter);
    }

    public void openBarcodeScanner() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                    openScanner();
                } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                    openScanner();
                } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                    openScanner();
                } else {
                    scannerOpen = true;
                    startStopBarcodeHandler(false);
                }
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 1000);
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li, vg, b);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //EventBus.getDefault().post(new BarcodeScanEvent("SPIT/IT/HW/00000001132"));
            }
        }, 1000);

        return view;
    }

    public class AssetListAdapter extends BaseAdapter {
        private HashMap<String, AssetsDetail> assetArrayList = new HashMap<String, AssetsDetail>();

        public AssetListAdapter(HashMap<String, AssetsDetail> assetArrayList) {
            this.assetArrayList = assetArrayList;
        }

        public void setAssetArrayList(HashMap<String, AssetsDetail> assetArrayList) {
            this.assetArrayList = assetArrayList;
        }

        @Override
        public int getCount() {
            return assetArrayList.size();
        }

        @Override
        public AssetsDetail getItem(int position) {
            return (AssetsDetail) assetArrayList.values().toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return ((AssetsDetail) assetArrayList.values().toArray()[position]).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null)
                    convertView = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.deposit_recieve_cell_item, parent, false);

                ((TextView) convertView.findViewById(R.id.deposit_asset_details)).setText(getItem(position).getAssetNo());
                ((TextView) convertView.findViewById(R.id.deposit_asset_brand_value)).setText(getItem(position).getBrand());
                ((TextView) convertView.findViewById(R.id.deposit_asset_model_value)).setText(getItem(position).getModel());
                ((TextView) convertView.findViewById(R.id.deposit_asset_category_value)).setText(getItem(position).getCategory());
                ((TextView) convertView.findViewById(R.id.deposit_asset_location_value)).setText(getItem(position).getLocation());
                ((TextView) convertView.findViewById(R.id.deposit_asset_epc_value)).setText(getItem(position).getEpc());
            } catch (Exception e) {
                e.printStackTrace();
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AssetsDetailWithTabFragment.ASSET_NO = getItem(position).getAssetNo();

                    AssetsDetailWithTabFragment assetsDetailWithTabFragment = new AssetsDetailWithTabFragment();


                    Log.i("replace3", "replace3");

                    ((MainActivity)MainActivity.mContext).replaceFragment(assetsDetailWithTabFragment);
                }
            });

            return convertView;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {
        tempBarcode = event.getBarcode();

        if(((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0 && event.getBarcode().length() == 3) {
            boolean found = false;
            if( ((MainActivity)getActivity()).isNetworkAvailable() ) {
                RetrofitClient.getSPGetWebService().trayList(companyId, userId).enqueue(new TrayListCallBack(DownloadFragment.TRAY_LIST));
            } else {
                ArrayList<Tray> trayArrayList = Hawk.get("TRAY_LIST", new ArrayList<>());
                for (int i = 0; i < trayArrayList.size(); i++) {
                    if (trayArrayList.get(i).getId() != null && trayArrayList.get(i).getId().equals(event.getBarcode())) {
                        ((TextView) view.findViewById(R.id.location_tag)).setText(getString(R.string.location) + " : " + trayArrayList.get(i).getTrayName());
                        tray = trayArrayList.get(i).getTrayName();
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.tray_not_found)));
                }
            }
        } else {
            if( ((MainActivity)getActivity()).isNetworkAvailable() ) {
                RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""), event.getBarcode(), "").enqueue(new NewAssetDetailCallback("1"));
            } else {
                //DataBaseHandler dataBaseHandler = new DataBaseHandler(MainActivity.mContext);
                //List<AssetsDetail> assetsDetail = dataBaseHandler.searchAssetsDetail(event.getBarcode());//.size());//MainActivity.getAssetsDetailList(ASSET_NO);//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);
                List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).beginGroup().equalTo("assetNo", event.getBarcode()).or().equalTo("barcode", event.getBarcode()).endGroup().findAll();

                Log.i("assetDetails", "assetDetails" + assetsDetail.size());

                if (assetsDetail != null && assetsDetail.size() > 0) {
                    if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
                        if (depositList.get(assetsDetail.get(0).getAssetNo()) == null) {
                            depositList.put(assetsDetail.get(0).getAssetNo(), assetsDetail.get(0));

                            if (depositAdapter == null) {
                                depositAdapter = new AssetListAdapter(depositList);
                            } else {
                                depositAdapter.setAssetArrayList(depositList);
                            }
                            depositAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (recieveList.get(assetsDetail.get(0).getAssetNo()) == null) {
                            recieveList.put(assetsDetail.get(0).getAssetNo(), assetsDetail.get(0));

                            Log.i("recieveList", "recieveList " + recieveList.size());

                            if (recieveAdapter == null) {
                                recieveAdapter = new AssetListAdapter(recieveList);
                            } else {
                                recieveAdapter.setAssetArrayList(recieveList);
                            }
                            recieveAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.asset_not_found)));
                }
            }
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        if(event.type == 1){
            List<AssetsDetail> assetsDetail = ((List<AssetsDetail>)event.getResponse());

            if(assetsDetail== null || assetsDetail.size() == 0) {
                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.asset_not_found)));
                return;
            }

            if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
                if (depositList.get(assetsDetail.get(0).getAssetNo()) == null) {
                    depositList.put(assetsDetail.get(0).getAssetNo(), assetsDetail.get(0));

                    if (depositAdapter == null) {
                        depositAdapter = new AssetListAdapter(depositList);
                    } else {
                        depositAdapter.setAssetArrayList(depositList);
                    }
                    depositAdapter.notifyDataSetChanged();
                }
            } else {
                if (recieveList.get(assetsDetail.get(0).getAssetNo()) == null) {
                    recieveList.put(assetsDetail.get(0).getAssetNo(), assetsDetail.get(0));

                    Log.i("recieveList", "recieveList " + recieveList.size());

                    if (recieveAdapter == null) {
                        recieveAdapter = new AssetListAdapter(recieveList);
                    } else {
                        recieveAdapter.setAssetArrayList(recieveList);
                    }
                    recieveAdapter.notifyDataSetChanged();
                }
            }
        } else if(event.type == DownloadFragment.TRAY_LIST){
            Hawk.put("TRAY_LIST", ((ArrayList<Tray>)event.getResponse()));

            boolean found = false;

            ArrayList<Tray> trayArrayList = Hawk.get("TRAY_LIST", new ArrayList<>());
            for (int i = 0; i < trayArrayList.size(); i++) {
                if (trayArrayList.get(i).getId() != null && trayArrayList.get(i).getId().equals(tempBarcode)) {
                    ((TextView) view.findViewById(R.id.location_tag)).setText(getString(R.string.location) + " : " + trayArrayList.get(i).getTrayName());
                    tray = trayArrayList.get(i).getTrayName();
                    found = true;
                    break;
                }
            }

            if (!found) {
                EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.tray_not_found)));
            }
        }
    }
}