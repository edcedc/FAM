package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BorrowListFragment;
import com.spit.fam.SystemFragment.BorrowListItemListFragment;
import com.spit.fam.SystemFragment.DisposalListFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.ReturnFragment;
import com.spit.fam.SystemFragment.StockTakeListItemFragment;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class AssetListAdapter extends BaseAdapter {
    public static boolean WITH_EPC = true;

    public List<Asset> assetResponse;
    ArrayList<String> searchedEPCList = new ArrayList<>();
    public List<Asset> wishList = new ArrayList<>();

    private String type;

    Context context;
    public boolean borrowList;
    boolean returnList;
    boolean withRemark;

    public ArrayList<Integer> integerArrayList = new ArrayList<>();

    public ArrayList<Integer> getIntegerArrayList() {
        return integerArrayList;
    }

    public ArrayList<Asset> getSelectedAsset() {
        Log.i("getSelectedAsset", "getSelectedAsset " + integerArrayList.size() + " " + assetResponse.size());

        if(integerArrayList != null && integerArrayList.size() > 0) {
            ArrayList<Asset> assets = new ArrayList<>();

            for(int i = 0; i < integerArrayList.size(); i++) {
                Log.i("yoyo", "yoyo " + integerArrayList.get(i));
                assets.add(assetResponse.get(integerArrayList.get(i)));
            }
            return assets;
        }

        return new ArrayList<>();
    }

    public AssetListAdapter(List<Asset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        integerArrayList = new ArrayList<>();
        setData(assetResponse, context);
        EventBus.getDefault().register(this);
    }


    public AssetListAdapter(List<Asset> assetResponse, Context context, String type) {
        this.assetResponse = assetResponse;
        this.context = context;
        setData(assetResponse, context);
        integerArrayList = new ArrayList<>();

        this.type = type;
        EventBus.getDefault().register(this);
    }

    public AssetListAdapter(boolean withRemark, List<Asset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.withRemark = withRemark;
        Log.i("withRemark", "withRemark" + withRemark);
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
        EventBus.getDefault().register(this);
    }

    public AssetListAdapter(boolean withRemark, List<Asset> assetResponse, List<Asset> wishList, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.withRemark = withRemark;
        this.wishList = wishList;
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
        EventBus.getDefault().register(this);
    }

    public AssetListAdapter(List<Asset> assetResponse, Context context, boolean borrowList) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.borrowList = true;
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
        EventBus.getDefault().register(this);
    }

    public AssetListAdapter(List<Asset> assetResponse, boolean returnList, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.returnList = returnList;
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
        EventBus.getDefault().register(this);
    }

    public void setData(List<Asset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;


        for(int i = 0; i < assetResponse.size(); i++) {
            List<Asset> arrayList = new ArrayList<>();
            if (StockTakeListItemFragment.stockTakeList != null) {
                arrayList = StockTakeListItemFragment.stockTakeList.getAssets();

            } else {
                arrayList = wishList;
            }

            try {
                for (int y = 0; y < arrayList.size(); y++) {
                    if (arrayList.get(y).getEPC().equals(getItem(i).getEPC())) {
                        getItem(i).setFoundInStockTakeList(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (borrowList) {
                getItem(i).setFoundInStockTakeList(true);
            }

            Log.i("setFoundInSearchedEPCList", "setFoundInSearchedEPCList " + searchedEPCList + " " + getItem(i).getEPC());
            if(getItem(i).getEPC() != null && getItem(i).getEPC().length() > 0) {
                getItem(i).setFoundInSearchedEPCList(searchedEPCList.contains(getItem(i).getEPC()));
            } else {
                getItem(i).setFoundInSearchedEPCList(searchedEPCList.contains(getItem(i).getAssetno()));
            }
        }
    }

    public void setSearchedEPCList(ArrayList<String> searchedEPCList) {
        for(int i = 0; i < searchedEPCList.size(); i++) {
            if(this.searchedEPCList.contains(searchedEPCList.get(i))) {

            } else {
                this.searchedEPCList.add(searchedEPCList.get(i));
            }
        }
/*
        ArrayList<String> newList = new ArrayList<>();
        for(int i = 0; i < searchedEPCList.size(); i++) {
            if(!newList.contains(searchedEPCList.get(i))) {
                newList.add(searchedEPCList.get(i));
            }
        }
        this.searchedEPCList = newList;*/
    }

    public void setSearchedEPCList(List<Asset> assetResponse) {
        this.assetResponse = assetResponse;
    }

    public ArrayList<String> getSearchedEPCList() {
        return searchedEPCList;
    }

    @Override
    public int getCount() {
        Log.i("getCount", "getCount " + assetResponse.size());
        return assetResponse.size();
    }

    @Override
    public Asset getItem(int i) {
        return assetResponse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return assetResponse.get(i).hashCode();
    }

    public void setTextView(TextView tv, String data) {
        if(data == null || data.length() == 0) {
            ((ViewGroup)tv.getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)tv.getParent()).setVisibility(View.VISIBLE);
            tv.setText(data);
        }
    }

    public void setWishList(ArrayList<Asset> wishList) {
        this.wishList = wishList;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
        view = LayoutInflater.from(context).inflate(R.layout.search_listview_cell, viewGroup, false);
        view.setOnClickListener(null);

        ((TextView)view.findViewById(R.id.search_cell_status_in_borrowed)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_disposed)).setVisibility(View.GONE);
        view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
        view.findViewById(R.id.missing_take).setVisibility(View.GONE);
        view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);

        if( getItem(i).getAssetno() == null) {
            setTextView((TextView)view.findViewById(R.id.search_cell_brand_value), "");
            setTextView((TextView)view.findViewById(R.id.search_cell_model_value), "");
            setTextView((TextView)view.findViewById(R.id.search_cell_category_value), "");
            setTextView((TextView)view.findViewById(R.id.search_cell_title), getItem(i).getEPC());
            setTextView((TextView)view.findViewById(R.id.search_cell_epc_value), "");
            setTextView((TextView)view.findViewById(R.id.search_cell_location_value), "");
            setTextView((TextView)view.findViewById(R.id.search_cell_return_date_value), "");
            //setTextView((TextView)view.findViewById(R.id.search_cell_prosecution_value), "");

            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);

            return view;
        } else {
            ((TextView)view.findViewById(R.id.search_cell_title)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_brand_value)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_model_value)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_category_value)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_epc_value)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_location_value)).setVisibility(View.VISIBLE);
        }

        if(getItem(i) == null) return view;

        Log.i("adapter", "adapter " + getItem(i).getEPC() + " "+ getItem(i).isEPCOnly() + " " + (getItem(i).getAssetno() == null || getItem(i).getAssetno().length() == 0));

        if(getItem(i).isEPCOnly() && (getItem(i).getAssetno() == null || getItem(i).getAssetno().length() == 0)) {
            ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getEPC());
            (view.findViewById(R.id.content_panel)).setVisibility(View.GONE);
        } else {
            (view.findViewById(R.id.content_panel)).setVisibility(View.VISIBLE);
            ((LinearLayout)((TextView)view.findViewById(R.id.search_cell_title)).getParent()).setVisibility(View.VISIBLE);
            Log.i("adapter", "adapter 2 " +getItem(i).getAssetno() + " | " + getItem(i).getName());

            ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getAssetno() + " | " + getItem(i).getName());
        }

        //setTextView((TextView)view.findViewById(R.id.search_cell_prosecution_value),  getItem(i).getProsecutionNo() != null && getItem(i).getProsecutionNo().length() > 0 ? getItem(i).getProsecutionNo() : " ");
        setTextView((TextView)view.findViewById(R.id.search_cell_brand_value) , getItem(i).getBrand() != null && getItem(i).getBrand().length() > 0 ? getItem(i).getBrand() : " ");
        setTextView((TextView)view.findViewById(R.id.search_cell_model_value), getItem(i).getModel() != null && getItem(i).getModel().length() > 0 ? getItem(i).getModel() : " ");
        setTextView((TextView)view.findViewById(R.id.search_cell_category_value), getItem(i).getCategoryString() != null && getItem(i).getCategoryString().length() > 0 ? getItem(i).getCategoryString() : " ");
        setTextView((TextView)view.findViewById(R.id.search_cell_epc_value), getItem(i).getEPC() != null && getItem(i).getEPC().length() > 0 ? getItem(i).getEPC() : " ");
        setTextView((TextView)view.findViewById(R.id.search_cell_location_value), getItem(i).getLocationString() != null && getItem(i).getLocationString().length() > 0 ? getItem(i).getLocationString() : " ");


        if(type != null) {
            ((ViewGroup)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.VISIBLE);
        } else {
            ((ViewGroup)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.GONE);
        }
        setTextView((TextView)view.findViewById(R.id.search_cell_new_epc_value), (getItem(i).getNewEPC()));

        setTextView ((TextView)view.findViewById(R.id.search_cell_return_date_value), getItem(i).getReturndate());


        //if(StockTakeListItemFragment.stockTakeList != null && StockTakeListItemFragment.stockTakeList.getAssets() != null)
        //    Log.i("yoyo", "yoyo stockTakeList " + StockTakeListItemFragment.stockTakeList.getAssets().size());

        //if(wishList != null )
        //    Log.i("yoyo", "yoyo wishList " + wishList.size());


        if(returnList) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
        /*
         null [] true false
        false true false
         */
        if( (StockTakeListItemFragment.stockTakeList != null || (wishList != null && wishList.size() > 0) || borrowList) && !returnList) {

            String status_key = StockTakeListItemFragment.stockTakeList != null ? InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + getItem(i).getAssetno() : "";
            int value = Hawk.get(status_key, -1);



            if (getItem(i).isFoundInSearchedEPCList() || value == 1 || value == 2 || getItem(i).isFound() || (getItem(i).isFoundInStockTakeList() && getItem(i).isFoundInSearchedEPCList())) {
                Log.i("case 1", "case 1 " + getItem(i).isFoundInSearchedEPCList() + " " + getItem(i).isFound() + " " + getItem(i).isFoundInStockTakeList() + " " + getItem(i).isFoundInSearchedEPCList() + " " + value);
                view.findViewById(R.id.borrow_tick).setVisibility(View.VISIBLE);
                view.findViewById(R.id.missing_take).setVisibility(View.GONE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
            } else if (getItem(i).isFoundInStockTakeList() && !getItem(i).isFoundInSearchedEPCList()  ) {
                Log.i("case 2", "case 2");
                view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
                view.findViewById(R.id.missing_take).setVisibility(View.VISIBLE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
            } else if (!getItem(i).isFoundInStockTakeList() && !getItem(i).isFoundInSearchedEPCList() ) {
                Log.i("case 3", "case 3");
                view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
                view.findViewById(R.id.missing_take).setVisibility(View.GONE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.VISIBLE);
            }

            if(getItem(i).isAbnormal()) {
                Log.i("case 4", "case 4");

                view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
                view.findViewById(R.id.missing_take).setVisibility(View.GONE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.VISIBLE);
            }
            //view.setBackgroundColor(context.getResources().getColor(R.color.ams_grey));//context.getResources().getColor(R.color.colorPrimary));

        } else {
            Log.i("case 5", "case 5");


            view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
            view.findViewById(R.id.missing_take).setVisibility(View.GONE);
            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);

            Log.i("returnList", "returnList [" + getItem(i).getEPC() + "] " +  searchedEPCList.contains(getItem(i).getEPC()) + " " + searchedEPCList.size());

            for(int x = 0; x < searchedEPCList.size(); x++) {
                Log.i("searchedEPCList", "searchedEPCList [" + getItem(i).getEPC() + "] [" + searchedEPCList.get(x) + "] " + searchedEPCList.get(x).equals(getItem(i).getEPC()));
            }

            view.findViewById(R.id.background).setBackgroundColor(context.getResources().getColor(R.color.ams_grey));

            if(integerArrayList.contains(i) && ReturnFragment.tabPosition == 1) {
                view.findViewById(R.id.background).setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            } else {
                view.findViewById(R.id.background).setBackgroundColor(context.getResources().getColor(R.color.ams_grey));
            }

            /*
            if(returnList && searchedEPCList.contains(getItem(i).getEPC())) {
                view.findViewById(R.id.background).setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            } else {
                view.findViewById(R.id.background).setBackgroundColor(context.getResources().getColor(R.color.ams_grey));
            }*/
        }

        Log.i("demodemodemo", "demodemodemo " + BorrowListFragment.POSITION + " " + DisposalListFragment.POSITION);

        if(!(BorrowListFragment.POSITION == 1 || DisposalListFragment.POSITION == 1)) {
            view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
            view.findViewById(R.id.missing_take).setVisibility(View.GONE);
            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
        }

        // Log.i("status", "status " +  getItem(i).getStatus().id);
        //!borrowList ||
        if(getItem(i).getStatus() != null) {
            Log.i("getStatus", "getStatus " + getItem(i).getStatus().id);

            if (getItem(i).getStatus().id > 0) {

                if (getItem(i).getStatus().id > 0) {
                    String text = getItem(i).getStatus().getStatusString();

                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);

                    if(getItem(i).getStatus().id == 2)
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                    else if(getItem(i).getStatus().id == 3 || getItem(i).getStatus().id == 4)
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
                    else if(getItem(i).getStatus().id == 5 || getItem(i).getStatus().id == 6 || getItem(i).getStatus().id == 7 || getItem(i).getStatus().id == 8 || getItem(i).getStatus().id == 9999)
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    else {
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);
                    }

                    Log.i("case1", "case1 " + text);
                }

            } else if(getItem(i).getStatus().getName() != null) {
                Log.i("case2", "case2 " + getItem(i).getName() + " " + getItem(i).getStatus().getName() );

                if (getItem(i).getStatus().getName().equals("In Library")) {
                    ((TextView) view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.VISIBLE);
                } else if (getItem(i).getStatus().getName().equals("In Borrowed")) {
                    ((TextView) view.findViewById(R.id.search_cell_status_in_borrowed)).setVisibility(View.VISIBLE);
                } else if (getItem(i).getStatus().getName().equals("Disposed")) {
                    ((TextView) view.findViewById(R.id.search_cell_status_disposed)).setVisibility(View.VISIBLE);
                }
            } else {
                ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);//setText(text);
            }
        } else {
            if (getItem(i).getStatus() != null && getItem(i).getStatus().id > 0) {
                if (getItem(i).getStatus().id > 0) {

                    String text = getItem(i).getStatus().getStatusString();
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.VISIBLE);//setText(text);

                    if(getItem(i).getStatus().id == 9999) {
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    }
                    Log.i("case3", "case3 " + text);

                } else {
                    Log.i("case4", "case4");

                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);//setText(text);

                }
            } else {
                Log.i("case5", "case5");

                ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);//setText(text);

            }
        }

        if(StockTakeListItemFragment.stockTakeList != null) {
            ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!((MainActivity)context).isNetworkAvailable()) {

                    if (BorrowListItemListFragment.BORROW_NO != null || BorrowListItemListFragment.DISPOSAL_NO != null) {
                    //    return;
                    }
                }
                performClick(i);
            }
        });


        if(getItem(i).getAssetno() == null) {

        }
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doneEvent(NetworkInventoryDoneEvent event) {
        if(event.getName() == null || !event.getName().equals("inventory")) {
            //performClick(pos);
        }
    }

    public static int pos = -1;
    public void performClick(int i) {
        pos = i;
        //if(borrowList)
        //    return;
        AssetsDetailWithTabFragment.SOURCE = "";

        if(LoginFragment.SP_API) {
            AssetsDetailWithTabFragment.ASSET_NO = "";
            AssetsDetailWithTabFragment.ASSET_NO = getItem(i).getAssetno();

            //if(!((MainActivity)context).isNetworkAvailable() && (AssetsDetailWithTabFragment.ASSET_NO == null || AssetsDetailWithTabFragment.ASSET_NO.length() == 0))
            //    return;

            Log.i("withRemark", "withRemark " + withRemark + " " + getItem(i).isFound());

            if (withRemark)
                AssetsDetailWithTabFragment.WITH_REMARK = true;
            else
                AssetsDetailWithTabFragment.WITH_REMARK = false;

            List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + AssetsDetailWithTabFragment.ASSET_NO, null);

            Log.i("assetsDetail", "assetsDetail " + InternalStorage.OFFLINE_CACHE.SP_ASSET + AssetsDetailWithTabFragment.ASSET_NO + " " + assetsDetail + " " + getItem(i).getStatus().id);

            //if(assetsDetail == null && !((MainActivity) context).isNetworkAvailable())
            //    return;

            if(assetsDetail == null) {
                AssetsDetailWithTabFragment.asset = new Asset();
                AssetsDetailWithTabFragment.asset.setAssetno(getItem(i).getAssetno());
                AssetsDetailWithTabFragment.asset.setName(getItem(i).getName());
                AssetsDetailWithTabFragment.asset.setBrand(getItem(i).getBrand());
                AssetsDetailWithTabFragment.asset.setModel(getItem(i).getModel());
                AssetsDetailWithTabFragment.asset.setEPC(getItem(i).getEPC());
            }

            Log.i("replace992", "replace992");

            AssetsDetailWithTabFragment assetsDetailWithTabFragment = new AssetsDetailWithTabFragment();

            try {
                //searchedEPCList.contains(searchedEPCList.get(i));
                if (getItem(i).getStatus().id == 2) {
                    AssetsDetailWithTabFragment.IN_STOCK = getItem(i).isFound() ? getItem(i).isFound() : searchedEPCList.contains(assetsDetail.get(0).getEpc());

                    if(getItem(i).getFindType().equals("rfid") || getItem(i).getFindType().equals("barcode")  || getItem(i).getFindType().equals("uploaded"))
                        AssetsDetailWithTabFragment.SOURCE = "FORCE";

                    Log.i("getItem", "getItem " + getItem(i) + " " + getItem(i).isFound() + " " + searchedEPCList.contains(assetsDetail.get(0).getEpc()) + " " + getItem(i).isFoundByScan());

                } else {
                    AssetsDetailWithTabFragment.IN_STOCK = getItem(i).isFound();
                    AssetsDetailWithTabFragment.SOURCE = "FORCE";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i("RAW", "RAW " + AssetsDetailWithTabFragment.IN_STOCK + " " + AssetsDetailWithTabFragment.SOURCE + " " + getItem(i).isFound());

            AssetsDetailWithTabFragment.PICTURE_LIST = getItem(i).getPic();
            AssetsDetailWithTabFragment.REMARK_FROM_SERVER = getItem(i).getRemarks();
            String api = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");
            //AssetsDetailWithTabFragment.PIC_SITE = (api.startsWith("http://") ? api : ("http://" + api)) + (api.endsWith("/") ? "" : "/") +"GS1AMS_Second";////Hawk.get(InternalStorage.PIC_SITE, "");

            Log.i("hihihi","hihihi" + AssetsDetailWithTabFragment.PIC_SITE);
            //"http://icloud.securepro.com.hk/Exhibit/";//getItem(i).getPicsite();

            Log.i("LIST", "LIST " + AssetsDetailWithTabFragment.PICTURE_LIST + " " + AssetsDetailWithTabFragment.REMARK_FROM_SERVER + " " + AssetsDetailWithTabFragment.PIC_SITE);

            if(returnList && ReturnFragment.tabPosition == 1) {
                if(!integerArrayList.contains(i)) {
                    Log.i("integerArrayList", "integerArrayList add");
                    integerArrayList.add(new Integer(i));
                } else {
                    Log.i("integerArrayList", "integerArrayList remove");
                    integerArrayList.remove(new Integer(i));
                }
                notifyDataSetChanged();
                return;
            }

            if (getItem(i).getEPC() != null && getItem(i).getEPC().length() > 0) {
                WITH_EPC = true;
            } else {
                WITH_EPC = false;
            }

            ((MainActivity) context).replaceFragment(assetsDetailWithTabFragment);
            return;
        }

        try {
            AssetsDetailWithTabFragment.id = Integer.parseInt(getItem(i).getId());
            AssetsDetailWithTabFragment.asset = (getItem(i));

            Log.i("write asset remark", "write asset remark " + getItem(i).getStock_take_asset_item_remark());

            AssetsDetailWithTabFragment.assetRemark = getItem(i).getStock_take_asset_item_remark();
            AssetsDetailWithTabFragment.stockTakeListId = getItem(i).getStockTakeId();

            if (withRemark)
                AssetsDetailWithTabFragment.WITH_REMARK = true;
            else
                AssetsDetailWithTabFragment.WITH_REMARK = false;

            if (getItem(i).getEPC() != null && getItem(i).getEPC().length() > 0) {
                WITH_EPC = true;
            } else {
                WITH_EPC = false;
            }

            Log.i("yoyo ", "yoyo " + withRemark + " " + AssetsDetailWithTabFragment.WITH_REMARK + " " + WITH_EPC);
            Log.i("replace5", "replace5");

            ((MainActivity) context).replaceFragment(new AssetsDetailWithTabFragment());
        } catch (Exception e){
        }
        //Navigation.findNavController(((MainActivity)context).getSupportFragmentManager().getFragments().get(0).getView() ).navigate(R.id.action_searchListFragment_to_assetsDetailWithTabFragment);
    }


}
