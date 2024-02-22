package com.spit.fam.RenewSystemFragment;

import static com.spit.fam.nike.ExtKt.POWER_OTHER;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomMediaPlayer;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.OfflineMode.ReturnAssets;
import com.spit.fam.Entity.SPEntityP3.ReturnAsset;
import com.spit.fam.Entity.SpinnerOnClickEvent;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.PendingToAdd;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.NewHandHeld.MyUtil;
import com.spit.fam.R;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BaseFragment;
import com.spit.fam.WebService.Callback.GetLevelDataCallback;
import com.spit.fam.WebService.Callback.GetListingCallback;
import com.spit.fam.WebService.Callback.UpdateAssetEpcCallback;
import com.spit.fam.WebService.P2Callback.ReturnAssetCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;

public class RenewReturnFragment extends BaseFragment {
    public static int RETURN_API = 10;

    private View noResult;
    private ListView listView, return_listview ;

    private ReturnListAdapter assetListAdapter;
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

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");

    public static int CONTINUOUS_RETURN_API = 100;

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        return view;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int power = Hawk.get(POWER_OTHER, -1);
        if (power == -1){
            power = 32;
        }
        byte[] bytes = new byte[]{(byte) power};
        MsgPowerConfig pMsg = new MsgPowerConfig(bytes);
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

        Realm.getDefaultInstance().beginTransaction();
        ArrayList<ReturnAsset> returnAssets = new ArrayList<>();

        returnAssets.addAll(Realm.getDefaultInstance().where(ReturnAsset.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", userid).findAll());

        for(int i = 0; i < returnAssets.size(); i++) {
            returnAssets.get(i).setScanned(false);
            returnAssets.get(i).setClicked(false);

            Realm.getDefaultInstance().insertOrUpdate(returnAssets.get(i));
        }

        Realm.getDefaultInstance().commitTransaction();

        playerO = MainActivity.sharedObjects.playerO;
        playerN = MainActivity.sharedObjects.playerN;

        tabPosition = 0;

        view = LayoutInflater.from(getActivity()).inflate(R.layout.return_fragment, null);
        start = view.findViewById(R.id.start);

        return_listview = view.findViewById(R.id.return_listview);

        ((EditText) view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        view.findViewById(R.id.add).setVisibility(View.GONE);
        noResult = view.findViewById(R.id.no_result);

        view.findViewById(R.id.start).setVisibility(View.GONE);
        view.findViewById(R.id.confirm).setVisibility(View.GONE);

        view.findViewById(R.id.borrow_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RenewReturnFragment.this.view.findViewById(R.id.select_location_list).setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstLocation = "";
                lastLocation = "";

                for(int i = 0; i < spinnerArrayList.size(); i++) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<>());
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerArrayList.get(i).setAdapter(dataAdapter);
                    spinnerArrayList.get(i).setEnabled(false);
                }

                ArrayList<ReturnAsset> returnAssets = new ArrayList<>();

                returnAssets.addAll(
                Realm.getDefaultInstance().where(ReturnAsset.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .equalTo("scanned", true)
                        .equalTo("clicked", true)
                        .findAll()
                );

                if(returnAssets.size() == 0) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.return_string), getString(R.string.nothing_selected)));
                    return;
                }

                RenewReturnFragment.this.view.findViewById(R.id.location_spinner).setVisibility(View.GONE);

                RenewReturnFragment.this.view.findViewById(R.id.select_location_list).setVisibility(View.VISIBLE);

                RenewReturnFragment.this.view.findViewById(R.id.select_location_list).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                SimpleListAdapter simpleListAdapter = new SimpleListAdapter( getSelectedListString());
                ColorDrawable sage = new ColorDrawable(getActivity().getResources().getColor(android.R.color.darker_gray));
                return_listview.setDivider(sage);
                return_listview.setDividerHeight(1);
                return_listview.setAdapter(simpleListAdapter);

                String searchKey = (companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_"  + "_" + 1 + "_" + 1);

                List<LevelData> arrayList = Realm.getDefaultInstance().where(LevelData.class).equalTo("searchKey", searchKey).findAll().sort("ordering");
                List<LevelData> result = new ArrayList<>();

                for(int i = 0; i < arrayList.size(); i++) {
                    //{"rono":"6FCCC4EEB9A440DBBF7E7FDC91980E41","name":"香港","fatherrono":"29E114E6D7124D7198A13CECD2CA88D5"}

                    LevelData levelData = new LevelData();
                    levelData.setRono(arrayList.get(i).getRono());
                    levelData.setName(arrayList.get(i).getName());
                    levelData.setOrdering(i);

                    result.add(levelData);
                }
               Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent " + arrayList.size() + " " + searchKey + " " );


                if(((MainActivity)MainActivity.mContext).isNetworkAvailable()){
                    RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("", 1, 1));
                } else {

                    CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(result);
                    callbackResponseEvent.type = 1;
                    callbackResponseEvent.level = (1);
                    callbackResponseEvent.setFatherno("");

                    EventBus.getDefault().post(callbackResponseEvent);
                }


            }
        });

        listView = view.findViewById(R.id.listview);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (((MainActivity) getActivity()).isURLReachable()) {
                    RetrofitClient.getSPGetWebService().newReturnList(companyId, userid).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start.getText().toString().equals(getString(R.string.start))) {
                    start.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();
                } else {
                    start.setText(getString(R.string.start));
                    ((MainActivity) MainActivity.mContext).stop();
                }
            }
        });
        view.findViewById(R.id.scan).setVisibility(View.GONE);

        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(getActivity())
                        .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        .setPrompt("")
                        .setCameraId(0)
                        .setBeepEnabled(true)
                        .setBarcodeImageEnabled(true)
                        .setCaptureActivity(CaptureActivityPortrait.class)
                        .initiateScan();
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();

                if(tabPosition == 0) {
                    view.findViewById(R.id.scan).setVisibility(View.GONE);

                    setupListView(getData());

                    view.findViewById(R.id.start).setVisibility(View.GONE);
                    view.findViewById(R.id.confirm).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.scan).setVisibility(View.VISIBLE);
                    setupListView(getData());
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


        ListingResponse l = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + userid + "SP_LISTING_LEVEL").findFirst();

        ViewGroup spinnerRoot = (ViewGroup) RenewReturnFragment.this.view.findViewById(R.id.sp_location);
        spinnerRoot.removeAllViews();
        spinnerArrayList.clear();

        for(int i = 0; i < l.getLocSize(); i++) {
            Spinner spinner = new Spinner(RenewReturnFragment.this.getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = (int)convertDpToPixel(10);
            layoutParams.bottomMargin = (int)convertDpToPixel(10);

            spinner.setLayoutParams(layoutParams);

            spinnerRoot.addView(spinner);

            LinearLayout ll = new LinearLayout(getActivity());
            ll.setBackgroundColor(Color.parseColor("#C9CACA"));
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(1));
            layoutParams.topMargin = (int)convertDpToPixel(2);

            if(i + 1 < l.getLocSize())
                layoutParams.bottomMargin = (int)convertDpToPixel(2);

            ll.setLayoutParams(layoutParams);

            spinnerRoot.addView(ll);

            spinnerArrayList.add(spinner);
        }
        view.findViewById(R.id.borrow_confirm).setOnClickListener(new View.OnClickListener() {
                                                                      @Override
                                                                      public void onClick(View view) {

                                                                          if(firstLocation.length() == 0) {
                                                                              EventBus.getDefault().post(new DialogEvent(getString(R.string.return_string), getString(R.string.no_location_selected)));
                                                                              return;
                                                                          }

                                                                          String returnString = "";

                                                                          ArrayList<ReturnAsset> returnAssetArrayList = new ArrayList<>();

                                                                          returnAssetArrayList.addAll(
                                                                                  Realm.getDefaultInstance().where(ReturnAsset.class)
                                                                                          .equalTo("companyid", companyId)
                                                                                          .equalTo("userid", userid)
                                                                                          .equalTo("scanned" , true)
                                                                                          .equalTo("clicked" , true)
                                                                                          .findAll()
                                                                          );

                                                                          for(int i = 0; i < returnAssetArrayList.size(); i++) {
                                                                              //returnArrayList.add(returnAssetArrayList.get(i).getId() + "");
                                                                              returnString += returnAssetArrayList.get(i).getId() + ",";
                                                                          }

                                                                          returnString = (returnString.substring(0, returnString.length() - 1)) ;
                                                                          if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                                                                              RetrofitClient.getSPGetWebService().returnAsset(companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, returnString).enqueue(new UpdateAssetEpcCallback(companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, returnString));//returnBorrowedAssetRequest).enqueue(new UpdateAssetEpcCallback());
                                                                          } else {
                                                                              ReturnAssets returnAssets = new ReturnAssets();
                                                                              returnAssets.setCompanyid(companyId);
                                                                              returnAssets.setUserid(userid);
                                                                              returnAssets.setFirstlocation(firstLocation);
                                                                              returnAssets.setLastlocation(lastLocation.isEmpty() ? firstLocation : lastLocation);

                                                                               returnAssets.setReturnList(returnString);

                                                                              returnAssets.setPk(companyId+userid+firstLocation+lastLocation+returnString);

                                                                              Realm.getDefaultInstance().beginTransaction();
                                                                              Realm.getDefaultInstance().insertOrUpdate(returnAssets);
                                                                              Realm.getDefaultInstance().commitTransaction();

                                                                              ((MainActivity)getActivity()).updateDrawerStatus();
                                                                              EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.upload_tips)));
                                                                              RenewReturnFragment.this.view.findViewById(R.id.select_location_list).setVisibility(View.GONE);
                                                                              ((MainActivity) MainActivity.mContext).replaceFragment(new RenewReturnFragment());


                                                                          }
                                                                      }
                                                                  });
        setupListView(getData());

        if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
            RetrofitClient.getSPGetWebService().newReturnList(companyId, userid).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
        } else {

        }
    }

    public  float convertDpToPixel(float dp){
        return dp * ((float) getActivity().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public List<ReturnAsset> getAsset() {
        return Realm.getDefaultInstance().where(ReturnAsset.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll();

    }

    public void setupListView(List<ReturnAsset> myasset) {
        String filterText = ((TextView)view.findViewById(R.id.edittext)).getText().toString();

        handleNoResult(filter(myasset, filterText));
        AssetListAdapter.WITH_EPC = true;


        Log.i("myasset", "myasset " + myasset.size() + " " + assetListAdapter);

         if(assetListAdapter == null) {
             assetListAdapter = new ReturnListAdapter(myasset);
            listView.setAdapter(assetListAdapter);
        } else {
             assetListAdapter.setData(myasset);
             assetListAdapter.notifyDataSetChanged();
        }

        if(filter(myasset, filterText).size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase() + " (" + filter(myasset, filterText).size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase());
    }

    ReturnListAdapter adapter = null;

    public class ReturnListAdapter extends BaseAdapter {
        List<ReturnAsset> data = new ArrayList<>();

        public ReturnListAdapter(List<ReturnAsset> data) {
            this.data = data;
            Log.i("ReturnListAdapter", "ReturnListAdapter " + this.data.size());
        }

        public void setData(List<ReturnAsset> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            Log.i("getCount", "getCount " + data.size());
            return data.size();
        }

        @Override
        public ReturnAsset getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return data.hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null)
                view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.search_listview_cell, null);

            view.findViewById(R.id.background).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AssetsDetailWithTabFragment.ASSET_NO = getItem(i).getAssetno();
                    Log.i("ASSET_NO", "ASSET_NO " );

                    AssetsDetailWithTabFragment.WITH_REMARK = false;

                    AssetsDetailWithTabFragment assetsDetailWithTabFragment = new AssetsDetailWithTabFragment();
                    ((MainActivity) MainActivity.mContext).replaceFragment(assetsDetailWithTabFragment);
                }
            });

            Log.i("listview", "listview position " + i);
            ReturnAsset borrowAsset = getItem(i);

            ((TextView)view.findViewById(R.id.search_cell_title)).setText(borrowAsset.getAssetno() + " | " + borrowAsset.getName());
            ((TextView)view.findViewById(R.id.search_cell_brand_value)).setText(borrowAsset.getBrand());
            ((TextView)view.findViewById(R.id.search_cell_model_value)).setText(borrowAsset.getModel());
            ((TextView)view.findViewById(R.id.search_cell_category_value)).setText(borrowAsset.getCategory());
            ((TextView)view.findViewById(R.id.search_cell_location_value)).setText(borrowAsset.getLocation());
            ((TextView)view.findViewById(R.id.search_cell_epc_value)).setText(borrowAsset.getEpc());
            (((TextView)view.findViewById(R.id.search_cell_return_date_value))).setVisibility(View.GONE);

            ((ViewGroup)(view.findViewById(R.id.search_cell_generic)).getParent()).setVisibility(View.GONE);
            ((ViewGroup)(view.findViewById(R.id.search_cell_status_in_library)).getParent()).setVisibility(View.GONE);
            ((ViewGroup)(view.findViewById(R.id.search_cell_status_in_borrowed)).getParent()).setVisibility(View.GONE);
            ((ViewGroup)(view.findViewById(R.id.search_cell_status_disposed)).getParent()).setVisibility(View.GONE);

            ((ViewGroup)(view.findViewById(R.id.search_cell_return_date_value)).getParent()).setVisibility(View.GONE);

            Log.i("position", "position " + view);

            int position = ((TabLayout) RenewReturnFragment.this.view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

            (((view.findViewById(R.id.borrow_tick)))).setVisibility(View.GONE);
            (((view.findViewById(R.id.missing_take)))).setVisibility(View.GONE);
            ((view.findViewById(R.id.abnormal_take))).setVisibility(View.GONE);
            if(borrowAsset.isClicked()) {
                ((LinearLayout)(view.findViewById(R.id.background))).setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.colorPrimary));
            } else {
                ((LinearLayout)(view.findViewById(R.id.background))).setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.ams_grey));
            }


            if(tabPosition == 1) {

                ((LinearLayout)(view.findViewById(R.id.background))).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Realm.getDefaultInstance().beginTransaction();

                        List<ReturnAsset> returnAssets = Realm.getDefaultInstance().where(ReturnAsset.class)
                                .equalTo("companyid", companyId)
                                .equalTo("userid", userid)
                                .contains("assetno", borrowAsset.getAssetno())
                                .findAll();

                        returnAssets.get(0).setClicked(!returnAssets.get(0).isClicked());

                        if(returnAssets.get(0).isClicked()) {
                            ((LinearLayout)(view.findViewById(R.id.background))).setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.colorPrimary));
                        } else {
                            ((LinearLayout)(view.findViewById(R.id.background))).setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.ams_grey));
                        }

                        Realm.getDefaultInstance().insertOrUpdate(returnAssets.get(0));
                        Realm.getDefaultInstance().commitTransaction();

                    }
                });
            } else {
                ((LinearLayout)(view.findViewById(R.id.background))).setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.ams_grey));

                ((LinearLayout)(view.findViewById(R.id.background))).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
            }

            return view;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SpinnerOnClickEvent event) {

        if(event.getLayer() == 1) {
            firstLocation = event.getFatherno();
            lastLocation = "";
        } else {
            lastLocation = event.getFatherno();
        }
        Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent location " + event.getLayer() + " " + firstLocation + " " + lastLocation + " " + event.getLayer());

        if(true) {

            String searchKey = (companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_" + event.getFatherno() + "_" + 1 + "_" + (event.getLayer() + 1));

            List<LevelData> arrayList = Realm.getDefaultInstance().where(LevelData.class).equalTo("searchKey", searchKey).findAll().sort("ordering");
            List<LevelData> result = new ArrayList<>();

            for (int i = 0; i < arrayList.size(); i++) {
                //{"rono":"6FCCC4EEB9A440DBBF7E7FDC91980E41","name":"香港","fatherrono":"29E114E6D7124D7198A13CECD2CA88D5"}

                LevelData levelData = new LevelData();
                levelData.setRono(arrayList.get(i).getRono());
                levelData.setName(arrayList.get(i).getName());
                levelData.setOrdering(i);

                result.add(levelData);
            }
            try {
                Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent searchKey " + searchKey);

                //Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent data " + arrayList.size() + " " + searchKey + " " + arrayList.get(0).getClass());

                /*
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(result);
                callbackResponseEvent.type = event.getType();
                callbackResponseEvent.level = (event.getLayer() + 1);
                callbackResponseEvent.setFatherno(event.getFatherno());

                EventBus.getDefault().post(callbackResponseEvent);

                 */
                if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                    RetrofitClient.getSPGetWebService().listing(companyId, event.getFatherno(), event.getType() + "").enqueue(new GetLevelDataCallback(event.getFatherno(), event.getType(), event.getLayer() + 1));
                } else {
                    CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(result);
                    callbackResponseEvent.type = event.getType();
                    callbackResponseEvent.level = (event.getLayer() + 1);
                    callbackResponseEvent.setFatherno(event.getFatherno());

                    EventBus.getDefault().post(callbackResponseEvent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //}
        /*
        if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1)) != null) {
            //EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1))));
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1)));
            callbackResponseEvent.type = event.getType();
            callbackResponseEvent.level = (event.getLayer() + 1);
            callbackResponseEvent.setFatherno(event.getFatherno());

            EventBus.getDefault().post(callbackResponseEvent);
        }
        */
    }
    public void setSpinner(List<LevelData> categoryList, Spinner spinner, int layer, int type) {
        try {
            List<String> location = new ArrayList<>();
            location.add("-");

            try {
                for (int i = 0; i < categoryList.size(); i++) {
                    if(categoryList.get(i).getName() != null) {
                        Log.i("data", "data " + categoryList.get(i).getName());

                        location.add(categoryList.get(i).getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("location", "location " + spinner.hashCode() + " " + location.size() + " " + categoryList.size());

             ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);

            if(location.size() == 1) {
                spinner.setEnabled(false);
                for (int i = layer; i < spinnerArrayList.size(); i++) {
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerArrayList.get(i).setAdapter(dataAdapter);
                    spinnerArrayList.get(i).setEnabled(false);
                }
            } else {
                spinner.setEnabled(true);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        if (position != 0) {
                            EventBus.getDefault().post(new SpinnerOnClickEvent(layer, type, categoryList.get(position - 1).getRono()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }
        /*
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new SpinnerOnClickEvent(layer, type, categoryList.get(position).getRono()));
            }
        });*/
    }


    public void handleNoResult(List<ReturnAsset> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }

        if(data.size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase() + " (" + data.size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.return_string).toUpperCase());

        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public List<ReturnAsset> filter(List<ReturnAsset> data, String filterText) {
        if(filterText == null || filterText.length() == 0 )
            return data;

        return data;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        setupListView(getData());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {

        boolean isChanged = false;
        Realm.getDefaultInstance().beginTransaction();
        if(barcodeIsScanned.get(event.getBarcode()) != null) {
            if (barcodeIsScanned.get(event.getBarcode()).equals(new Boolean(false)) ) {
                barcodeIsScanned.put(event.getBarcode(), new Boolean(true));
                isChanged = true;

                List<ReturnAsset> returnAssets = Realm.getDefaultInstance().where(ReturnAsset.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .beginGroup()
                        .contains("assetno", event.getBarcode())
                        .or()
                        .contains("epc", event.getBarcode())
                        .endGroup()
                        .findAll();

                returnAssets.get(0).setScanned(true);
                Realm.getDefaultInstance().insertOrUpdate(returnAssets);
            }
        }
        Realm.getDefaultInstance().commitTransaction();

        setupListView(getData());

    }

    ArrayList<LevelData> pendingToAdds = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {

        if(event.getResponse() instanceof APIResponse) {
            Log.i("APIResponse", "APIResponse 1 " + ((APIResponse) event.getResponse()).getStatus());
            RenewReturnFragment.this.view.findViewById(R.id.select_location_list).setVisibility(View.GONE);
            ((TabLayout)view.findViewById(R.id.tab_layout)).getTabAt(0).select();

            if (((APIResponse) event.getResponse()).getStatus() == 0) {
                barcodeIsScanned.clear();
                RetrofitClient.getSPGetWebService().newReturnList(companyId, userid).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
                EventBus.getDefault().post(new DialogEvent(getString(R.string.return_to_location), getString(R.string.return_success)));
            } else {
                if (((APIResponse) event.getResponse()).getReturnCount() > 0) {
                    RetrofitClient.getSPGetWebService().newReturnList(companyId, userid).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
                }

                EventBus.getDefault().post(new DialogEvent(getString(R.string.return_to_location), getString(R.string.return_some).replace("x", ((APIResponse) event.getResponse()).getReturnCount() + "")));
            }

        }
        if (event.type == CONTINUOUS_RETURN_API) {
            swipeRefreshLayout.setRefreshing(false);

            Log.i("CONTINUOUS", "CONTINUOUS case 1");
            ArrayList<ReturnAsset> arrayList = ((ArrayList<ReturnAsset>) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(ReturnAsset.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll().deleteAllFromRealm();

            for (int i = 0; i < arrayList.size(); i++) {
                String pk = companyId + userid + "RETURN" + arrayList.get(i).getAssetno();
                arrayList.get(i).setPk(pk);
                arrayList.get(i).setCompanyid(companyId);
                arrayList.get(i).setUserid(userid);
                arrayList.get(i).setTimeStamp(new Date().getTime() + "");
                Realm.getDefaultInstance().insertOrUpdate(arrayList.get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            Log.i("RetrofitClient", "RETURN_API size " + Realm.getDefaultInstance().where(ReturnAsset.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll().size() + " " + arrayList.size());

            barcodeIsScanned.clear();
            isScanned.clear();

            setupListView(getData());

            if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
            } else {
                String searchKey = (companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_"  + "_" + 1 + "_" + 1);

                List<LevelData> aa = Realm.getDefaultInstance().where(LevelData.class).equalTo("searchKey", searchKey).findAll().sort("ordering");
                List<LevelData> result = new ArrayList<>();

                for(int i = 0; i < aa.size(); i++) {
                    //{"rono":"6FCCC4EEB9A440DBBF7E7FDC91980E41","name":"香港","fatherrono":"29E114E6D7124D7198A13CECD2CA88D5"}

                    LevelData levelData = new LevelData();
                    levelData.setRono(aa.get(i).getRono());
                    levelData.setName(aa.get(i).getName());
                    levelData.setOrdering(i);

                    result.add(levelData);
                }
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(result);
                callbackResponseEvent.type = 1;
                callbackResponseEvent.level = (1);
                callbackResponseEvent.setFatherno("");

                EventBus.getDefault().post(callbackResponseEvent);

            }
        }


        if (event.getResponse() instanceof ListingResponse) {
            Realm.getDefaultInstance().beginTransaction();
            ListingResponse listingResponse = ((ListingResponse) event.getResponse());
            listingResponse.setPk(companyId + userid + "SP_LISTING_LEVEL");
            Realm.getDefaultInstance().insertOrUpdate(listingResponse);
            Realm.getDefaultInstance().commitTransaction();

            ListingResponse l = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + userid + "SP_LISTING_LEVEL").findFirst();

            Log.i("Retrofit", "Retrofit ListingResponse " + l);

            int categorySize = ((ListingResponse) event.getResponse()).getCatSize();
            int locationSize = ((ListingResponse) event.getResponse()).getLocSize();

            if (locationSize > 0) {
                if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                    RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("", 1, 1));
                }
            } else {
                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", userid).equalTo("isNewData", false).findAll().deleteAllFromRealm();
                Realm.getDefaultInstance().commitTransaction();

                //if (DOWNLOAD_ALL_FROM_LOGIN) {
                  //  RetrofitClient.getSPGetWebService().newReturnList(companyId, userid).enqueue(new ReturnAssetCallback(CONTINUOUS_RETURN_API));
                //}
            }
        }
        if (event.type == 1) {

            ArrayList<LevelData> levelData = (ArrayList<LevelData>) event.getResponse();

            for(int i = event.level ; i < spinnerArrayList.size() - 1; i++) {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.mContext, android.R.layout.simple_list_item_1, new ArrayList<>());
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i).setAdapter(arrayAdapter);
            }

            try {
                locationLevelData.subList(event.level, locationLevelData.size()).clear();
            } catch (Exception e) {
            }
            locationLevelData.add( levelData);

           // Log.i("data", "data " + event.getFatherno() + " " + levelData.get(0).getName() + " " + event.level + " " + event.type);

            try {
                setSpinner(levelData, spinnerArrayList.get(event.level - 1), event.level, event.type);
            } catch (Exception e) {
                e.printStackTrace();
            }

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


            if (pendingToAdd.type == 0) {
                for (int i = 0; i < levelData.size(); i++) {
                    Log.i("inert", "insert " + levelData.get(i).getName() + " " + event.level + " " + levelData.get(i).getRono() + " " + event.getFatherno() + " " + event.type);
                    levelData.get(i).setNewData(true);

                    levelData.get(i).setFatherNo(event.getFatherno());
                    levelData.get(i).setType(event.type);
                    levelData.get(i).setLevel(event.level);
                    levelData.get(i).setSearchKey(companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_" + 1 + "_" + pendingToAdd.level);
                    levelData.get(i).setPk(companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_" + 1 + "_" + pendingToAdd.level + i);

                    levelData.get(i).setCompanyid(companyId);
                    levelData.get(i).setUserid(userid);
                    levelData.get(i).setOrdering(i);
                    Realm.getDefaultInstance().insertOrUpdate(pendingToAdd.levelData.get(i));
                }
            } else if (pendingToAdd.type == 1) {
                for (int i = 0; i < pendingToAdd.levelData.size(); i++) {
                    Log.i("inert", "insert " + levelData.get(i).getName() + " " + event.level + " " + levelData.get(i).getRono() + " " + event.getFatherno() + " " + event.type);
                    levelData.get(i).setNewData(true);
                    levelData.get(i).setFatherNo(event.getFatherno());
                    levelData.get(i).setType(event.type);
                    levelData.get(i).setLevel(event.level);
                    levelData.get(i).setSearchKey(companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_1_" + pendingToAdd.level);
                    levelData.get(i).setPk(companyId + userid + "SP_LISTING_LEVEL_CACHE" + "_" + (pendingToAdd.fatherNo != null ? pendingToAdd.fatherNo : "") + "_1_" + pendingToAdd.level + i);

                    levelData.get(i).setCompanyid(companyId);
                    levelData.get(i).setUserid(userid);
                    levelData.get(i).setOrdering(i);

                    Realm.getDefaultInstance().insertOrUpdate(pendingToAdd.levelData.get(i));
                }
            }
            Realm.getDefaultInstance().commitTransaction();

            if (pendingToAdds.size() > 0) {
                Log.i("RetrofitClient", "listing " + event.type);
                if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                    //RetrofitClient.getSPGetWebService().listing(companyId, pendingToAdds.get(0).getRono(), event.type + "").enqueue(new GetLevelDataCallback(pendingToAdds.get(0).getRono(), event.type, pendingToAdds.get(0).getLevel() + 1));
                }
                pendingToAdds.remove(0);
            } else {
                if (event.type == 0) {
                    Log.i("RetrofitClient", "listing " + event.type);
                    if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
                        //RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("", 1, 1));
                    }
                } else {

                    Realm.getDefaultInstance().beginTransaction();

                    Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", userid).equalTo("isNewData", false).findAll().deleteAllFromRealm();

                    RealmResults<LevelData> result = Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll().sort("ordering");

                    for (int i = 0; i < result.size(); i++) {
                        result.get(i).setNewData(false);
                        Realm.getDefaultInstance().insertOrUpdate((result.get(i)));
                    }

                    Realm.getDefaultInstance().commitTransaction();
                    int count = Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", userid).findAll().size();


                    Log.i("RetrofitClient", "LevelData done " + count);
                }
            }
        }

    }

    HashMap<String, Boolean> isScanned = new HashMap<>();
    HashMap<String, Boolean> barcodeIsScanned = new HashMap<>();

    public ArrayList<ReturnAsset> getData() {
        ArrayList<ReturnAsset> returnAssets = new ArrayList<>();
        if(tabPosition == 0) {
            returnAssets.addAll(Realm.getDefaultInstance().where(ReturnAsset.class)
                    .equalTo("companyid", companyId)
                    .equalTo("userid", userid)

                    .beginGroup()
                    .contains("assetno", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .endGroup()

                    .beginGroup()
                    .contains("assetno", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .endGroup()
                    .findAll()
                    .sort("timeStamp")
            );

            if(isScanned.isEmpty()) {
                for (int i = 0; i < returnAssets.size(); i++) {
                    isScanned.put(returnAssets.get(i).getEpc(), new Boolean(false));
                }
            }
            if(barcodeIsScanned.isEmpty()) {

                for (int i = 0; i < returnAssets.size(); i++) {
                    barcodeIsScanned.put(returnAssets.get(i).getAssetno(), new Boolean(false));
                    barcodeIsScanned.put(returnAssets.get(i).getEpc(), new Boolean(false));
                }
            }
        } else {

            returnAssets.addAll(Realm.getDefaultInstance().where(ReturnAsset.class)
                    .equalTo("companyid", companyId)
                    .equalTo("userid", userid)
                    .equalTo("scanned" , true)
                    .beginGroup()
                    .contains("assetno", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .or()
                    .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                    .endGroup()
                    .findAll()
                    .sort("timeStamp")
            );
        }
        return returnAssets;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {
        Log.i("event", "event " + event.getData());
        if (playerN != null)
            playerN.start();
        //if (playerO != null)
        //    playerO.start();

        Realm.getDefaultInstance().beginTransaction();

        boolean isChanged = false;
        for(int i = 0; i < event.getData().size(); i++){
            if(isScanned.get(event.getData().get(i)) != null) {
                if (isScanned.get(event.getData().get(i)).equals(new Boolean(false)) ) {
                    isScanned.put(event.getData().get(i), new Boolean(true));
                    isChanged = true;

                    List<ReturnAsset> returnAssets = Realm.getDefaultInstance().where(ReturnAsset.class)
                            .equalTo("companyid", companyId)
                            .equalTo("userid", userid)
                            .contains("epc", event.getData().get(i))
                            .findAll();

                    returnAssets.get(0).setScanned(true);
                    Realm.getDefaultInstance().insertOrUpdate(returnAssets);
                }
            }
        }

        Realm.getDefaultInstance().commitTransaction();
         setupListView(getData());

    }


    public ArrayList<String> getSelectedListString() {
        ArrayList<String> result = new ArrayList<>();

        ArrayList<ReturnAsset> returnAssetArrayList = new ArrayList<>();

        returnAssetArrayList.addAll(
        Realm.getDefaultInstance().where(ReturnAsset.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", userid)
                .equalTo("scanned" , true)
                .equalTo("clicked" , true)
                .findAll()
        );

        for(int i = 0; i < returnAssetArrayList.size(); i ++) {
            result.add(returnAssetArrayList.get(i).getAssetno() + "\n" + returnAssetArrayList.get(i).getName());
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

}
