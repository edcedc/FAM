package com.spit.fam.SystemFragment;

import static com.spit.fam.nike.ExtKt.POWER_OTHER;
import static com.spit.fam.nike.ExtKt.POWER_STOCKTAKE;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.CreateBy;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SpEntity.StockTakeDetail;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.TagType;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.MuteEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.NewHandHeld.MyUtil;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.ViewPagerAdapter;
import com.spit.fam.WebService.Callback.NewAssetDetailCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;

public class AssetsDetailWithTabFragment extends BaseFragment {
    public static int id = -1;
    public static String ASSET_NO = "";
    public static Asset asset;

    public static Integer assetRemark;
    public static Integer stockTakeListId;
    public static boolean WITH_REMARK;
    public static boolean IN_STOCK;
    public static String SOURCE;

    public static String REMARK_FROM_SERVER;
    public static ArrayList<String> PICTURE_LIST = new ArrayList<>();
    public static String PIC_SITE;

    public static RealmStockTakeListAsset realmStockTakeListAsset;

    public static int position;

    public static boolean SEARCH_ENABLED = false;

    public void onPause() {
        super.onPause();
        AssetsDetailWithTabFragment.this.SEARCH_ENABLED = false;

        id = -1;
        ASSET_NO = "";
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.assets_details_fragment_with_tabs, null);
        ((TextView)view.findViewById(R.id.toolbar_title)).setText(ASSET_NO);
        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("assetNo", ASSET_NO).findAll();// dataBaseHandler.searchAssetsDetail(ASSET_NO, "", "", "", "", "", "", "", "");//.size());//MainActivity.getAssetsDetailList(ASSET_NO);//Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);

        Log.i("assetsDetail", "assetsDetail  2 " + ASSET_NO + " " + assetsDetail.size());

        if(assetsDetail != null  ) {
            Log.i("assetsDetail", "assetsDetail  2 " + assetsDetail);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(assetsDetail.size() == 0) {
                        List<AssetsDetail> assetsDetail2 = new ArrayList<>();
                        AssetsDetail assetsDetail1 = new AssetsDetail();
                        assetsDetail1.setAssetNo(ASSET_NO);
                        assetsDetail2.add(assetsDetail1);

                        EventBus.getDefault().post(new CallbackResponseEvent(assetsDetail2));
                    } else {
                        EventBus.getDefault().post(new CallbackResponseEvent(assetsDetail));
                    }
                }
            };

            Handler handler = new Handler();
            handler.postDelayed(runnable, 50);

        } else {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    //AssetsDetailWithTabFragment.this.asset = new Asset();

                    viewPagerAdapter = new ViewPagerAdapter( getChildFragmentManager(), getActivity());
                    ((ViewPager)view.findViewById(R.id.viewpager)).setAdapter(viewPagerAdapter);
                    ((TabLayout)view.findViewById(R.id.tab_layout)).setupWithViewPager(((ViewPager)view.findViewById(R.id.viewpager)));
                    ((ViewPager)view.findViewById(R.id.viewpager)).setOffscreenPageLimit(3);

                    ((ViewPager)view.findViewById(R.id.viewpager)).setCurrentItem(1);
                }
            };

            Handler handler = new Handler();
            handler.postDelayed(runnable, 50);

        }

        if( ((MainActivity)getActivity()).isURLReachable() ) {
            RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID,""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,""),ASSET_NO, "").enqueue(new NewAssetDetailCallback("1"));

            /*
            if(LoginFragment.SP_API) {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

                String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
                RetrofitClient.getSPGetWebService().assetDetail(companyId, userid, ASSET_NO).enqueue(new GetSPAssetListCallback());

            } else {
                Log.i("case 3", "case 3");
                RetrofitClient.getService().getAsset(id).enqueue(new GetAssetCallback());
            }*/
        } else {
            /*
            if(LoginFragment.SP_API) {
                viewPagerAdapter = new ViewPagerAdapter( getChildFragmentManager(), getActivity());
                ((ViewPager)view.findViewById(R.id.viewpager)).setAdapter(viewPagerAdapter);
                ((TabLayout)view.findViewById(R.id.tab_layout)).setupWithViewPager(((ViewPager)view.findViewById(R.id.viewpager)));
                ((ViewPager)view.findViewById(R.id.viewpager)).setOffscreenPageLimit(3);

            } else {
                ArrayList<Asset> arrayList = Hawk.get(InternalStorage.OFFLINE_CACHE.ASSET, new ArrayList<>());
                for(int i = 0; i < arrayList.size(); i++) {
                    if(Integer.parseInt(arrayList.get(i).getId()) == id) {

                        final int position = i;

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new CallbackResponseEvent((Asset) arrayList.get(position)));
                            }
                        };
                        Handler handler = new Handler();
                        handler.postDelayed(runnable, 300);
                    }
                }
            }*/
        }
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        AssetsDetailWithTabFragment.this.SEARCH_ENABLED = true;

        (view.findViewById(R.id.asset_detail_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        return view;
    }


    public void onResume(){
        super.onResume();
/*
        if(((MainActivity)getActivity()).isNetworkAvailable()) {
            Log.i("callingAPI", "callingAPI @ AssetsDetailWithTabFragment " + Hawk.get(InternalStorage.Setting.COMPANY_ID,"") + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"") + " " + ASSET_NO);

            RetrofitClient.getSPGetWebService().assetDetail(Hawk.get(InternalStorage.Setting.COMPANY_ID,""), Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,""),ASSET_NO, "").enqueue(new NewAssetDetailCallback("1"));
        } else {

            Log.i("localCache", "localCache @ AssetsDetailWithTabFragment ");

            List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, null);

            //Log.i("AssetsDetailWithTabFragment assetsDetail", " AssetsDetailWithTabFragment assetsDetail " + assetsDetail);// + " " + assetsDetail.getClass());

            if(assetsDetail != null) {
                //Log.i("AssetsDetailWithTabFragment assetsDetail", " AssetsDetailWithTabFragment assetsDetail " + assetsDetail.getClass());
                EventBus.getDefault().post(new CallbackResponseEvent(assetsDetail));
            }
        }

 */
    }

    public static ViewPagerAdapter viewPagerAdapter;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("CallbackResponseEvent", "CallbackResponseEvent " + event.getResponse().getClass());

        if(event.getResponse() instanceof List && ((List) event.getResponse()).size() > 0 && ((List) event.getResponse()).get(0) instanceof AssetsDetail) {
            Asset asset = convertAssetDetailToAsset((AssetsDetail)((List) event.getResponse()).get(0));
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(asset);
            EventBus.getDefault().post(callbackResponseEvent);

            //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_ASSET + ASSET_NO, event.getResponse());

        } else if(event.getResponse() instanceof StockTakeDetail) {
            StockTakeDetail apiResponse = ((StockTakeDetail)event.getResponse());

            ArrayList<Asset> list = new ArrayList<>();

            ArrayList<Asset> assets = new ArrayList<>();
            for(int i = 0; i < ((StockTakeDetail)event.getResponse()).getTable().size() ; i ++) {
                assets.add( ((StockTakeDetail)event.getResponse()).getTable().get(i).convertToAsset() );
            }

            try {
                this.asset = assets.get(0);
            } catch (Exception e) {
                getActivity().onBackPressed();
                return;
            }


            viewPagerAdapter = new ViewPagerAdapter( getChildFragmentManager(), getActivity());
            ((ViewPager)view.findViewById(R.id.viewpager)).setAdapter(viewPagerAdapter);
            ((TabLayout)view.findViewById(R.id.tab_layout)).setupWithViewPager(((ViewPager)view.findViewById(R.id.viewpager)));
            ((ViewPager)view.findViewById(R.id.viewpager)).setOffscreenPageLimit(3);

            if(AssetListAdapter.WITH_EPC)
                ((ViewPager)view.findViewById(R.id.viewpager)).setCurrentItem(1);

            ((ViewPager)view.findViewById(R.id.viewpager)).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    position = i;
                    if(i == 1 || i == 2) {
                        //EventBus.getDefault().post(new StopEvent());
                        //if (((AssetSearchFragment) viewPagerAdapter.getItem(0)).button.getText().toString().toLowerCase().equals("stop"))
                        //     ((AssetSearchFragment) viewPagerAdapter.getItem(0)).button.performClick();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
        } else if(event.getResponse() instanceof Asset) {
            Log.i("instanceof", "instanceof asset");
            this.asset = (Asset)event.getResponse();


            Log.i("status", "status " + AssetsDetailWithTabFragment.asset.getStatus().id);

            if(StockTakeListItemFragment.stockTakeList == null && AssetsDetailWithTabFragment.asset.getStatus() != null && AssetsDetailWithTabFragment.asset.getStatus().id == 7) {
                //((TabLayout)view.findViewById(R.id.tab_layout)).setVisibility(View.GONE);
            } else {
                ((TabLayout)view.findViewById(R.id.tab_layout)).setVisibility(View.VISIBLE);
            }

            viewPagerAdapter = new ViewPagerAdapter( getChildFragmentManager(), getActivity());
            ((ViewPager)view.findViewById(R.id.viewpager)).setAdapter(viewPagerAdapter);
            ((TabLayout)view.findViewById(R.id.tab_layout)).setupWithViewPager(((ViewPager)view.findViewById(R.id.viewpager)));
            ((ViewPager)view.findViewById(R.id.viewpager)).setOffscreenPageLimit(3);

            if(AssetListAdapter.WITH_EPC)
                ((ViewPager)view.findViewById(R.id.viewpager)).setCurrentItem(1);

            ((ViewPager)view.findViewById(R.id.viewpager)).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    position = i;
                    if(i == 0) {
                        /*
                        try {
                            MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt((int) (100f / 100f * 32) + "")});
                            MyUtil.reader.Send(pMsg);
                            SessionInfo si = new SessionInfo();

                            si.Session = Session.values()[0];
                            si.Flag = Flag.values()[2];

                            MsgSessionConfig msgS = new MsgSessionConfig(si);
                            MyUtil.reader.Send(msgS);

                            byte q = 0;
                            MsgQValueConfig msg = new MsgQValueConfig(q);
                            MyUtil.reader.Send(msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
*/

                        try {
                            int power = Hawk.get(POWER_STOCKTAKE, -1);
                            if (power == -1){
                                power = 32;
                            }
                            byte[] bytes = new byte[]{(byte) power};
                            MsgPowerConfig pMsg = new MsgPowerConfig(bytes);
//                            MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt((int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
                            MyUtil.reader.Send(pMsg);
                            SessionInfo si = new SessionInfo();

                            //si.Session = Session.values()[0];
                            //si.Flag = Flag.values()[2];
                            si.Session = Session.values()[0];
                            si.Flag = Flag.values()[2];

                            MsgSessionConfig msgS = new MsgSessionConfig(si);
                            MyUtil.reader.Send(msgS);

                            byte q = 4;
                            MsgQValueConfig msg = new MsgQValueConfig(q);
                            MyUtil.reader.Send(msg);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if(i == 2) {
                        EventBus.getDefault().post(new MuteEvent());

                        try {
                            int power = Hawk.get(POWER_STOCKTAKE, -1);
                            if (power == -1){
                                power = 32;
                            }
                            byte[] bytes = new byte[]{(byte) power};
                            MsgPowerConfig pMsg = new MsgPowerConfig(bytes);
//                            MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt(/*"30"*/ (int) (100f / 100f * 32) + "")});
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

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        EventBus.getDefault().post(new MuteEvent());
                    }
                    if(i == 1 || i == 2) {
                        //EventBus.getDefault().post(new StopEvent());
                        //if (((AssetSearchFragment) viewPagerAdapter.getItem(0)).button.getText().toString().toLowerCase().equals("stop"))
                        //     ((AssetSearchFragment) viewPagerAdapter.getItem(0)).button.performClick();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
        } else {
        }
    }

    public Asset convertAssetDetailToAsset(AssetsDetail assetDetail) {
        Asset asset = new Asset();
        asset.setAssetno(assetDetail.getAssetNo());
        asset.setName(assetDetail.getName());

        if(assetDetail.getStatusid() != null) {
            Status status = new Status();
            String language = Hawk.get(InternalStorage.Setting.LANGUAGE, "en");

            status.setName(status.getStatus(language, assetDetail.getStatusid()));
            try {
                status.id = Integer.parseInt(assetDetail.getStatusid());
            } catch (Exception e) {e.printStackTrace();}
            asset.setStatus(status);
        }

        asset.setBrand(assetDetail.getBrand());
        asset.setModel(assetDetail.getModel());
        asset.setSerialNo(assetDetail.getSerialno());
        asset.setUnit(assetDetail.getUnit());
        asset.setNewEPC(assetDetail.getNewEpc());
        asset.setRono(assetDetail.getRono());
        asset.setPossessor(assetDetail.getPossessor());
        asset.setUsergroup(assetDetail.getUsergroup());
        asset.setExhibitsource(assetDetail.getExhibitsource());
        asset.setExhibitwitness(assetDetail.getExhibitwitness());
        Log.i("LastAssetNo", "LastAssetNo " + assetDetail.getLastassetno());
        asset.setLastassetno(assetDetail.getLastassetno());


        ArrayList<Category> categoryArrayList = new ArrayList<>();

        Log.i("categories", "categories" + assetDetail.getCategorys());

        if(assetDetail.getCategorys() != null) {
            for (int i = 0; i < assetDetail.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(assetDetail.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);

        ArrayList<Location> locationArrayList = new ArrayList<>();

        if(assetDetail.getLocations() != null) {
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

        asset.setCertType(assetDetail.getCertType());
        asset.setCertUrl(assetDetail.getCertUrl());
        asset.setCerstatus(assetDetail.getCerstatus());
        asset.setIsverified(assetDetail.isIsverified());
        asset.setStartdate(assetDetail.getStartdate());
        asset.setEnddate(assetDetail.getEnddate());

        return asset;
    }
}

