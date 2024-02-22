package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.DatabaseHelper.DataBaseHandler;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.Status;
import com.spit.fam.Event.ModifyAssetRequest;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BorrowListFragment;
import com.spit.fam.SystemFragment.BorrowListItemListFragment;
import com.spit.fam.SystemFragment.DisposalListFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.ReturnFragment;
import com.spit.fam.SystemFragment.SearchFormFragment;
import com.spit.fam.SystemFragment.SearchListFragment;
import com.spit.fam.SystemFragment.SearchResultFragment;
import com.spit.fam.SystemFragment.StockTakeListItemFragment;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchListAdapter extends BaseAdapter {
    public static boolean WITH_EPC = true;
    public static int PAGE_SIZE = 50;

    public List<AssetsDetail> assetResponse;
    ArrayList<String> searchedEPCList = new ArrayList<>();
    public List<AssetsDetail> wishList = new ArrayList<>();

    private String type;

    Context context;
    public boolean borrowList;
    boolean returnList;
    boolean withRemark;

    private ArrayList<Integer> integerArrayList = new ArrayList<>();

    public ArrayList<Integer> getIntegerArrayList() {
        return integerArrayList;
    }

    public ArrayList<AssetsDetail> getSelectedAsset() {
        Log.i("getSelectedAsset", "getSelectedAsset " + integerArrayList.size());

        if(integerArrayList != null && integerArrayList.size() > 0) {
            ArrayList<AssetsDetail> assets = new ArrayList<>();

            for(int i = 0; i < integerArrayList.size(); i++) {
                assets.add(assetResponse.get(integerArrayList.get(i)));
            }
            return assets;
        }

        return new ArrayList<>();
    }

    public SearchListAdapter(List<AssetsDetail> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        integerArrayList = new ArrayList<>();
        setData(assetResponse, context);
    }

    HashMap<String, ModifyAssetRequest> s = new HashMap<String,ModifyAssetRequest>();

    public SearchListAdapter(List<AssetsDetail> assetResponse, Context context, String type) {
        this.assetResponse = assetResponse;
        this.context = context;
        setData(assetResponse, context);
        integerArrayList = new ArrayList<>();

        this.type = type;

        ArrayList<ModifyAssetRequest> bindEpcList = Hawk.get(InternalStorage.OFFLINE_CACHE.PENDING_CHANGE_EPC_REQUEST, new ArrayList<ModifyAssetRequest>());
        Log.i("bindEpcList", "bindEpcList " + bindEpcList.size());

        for (ModifyAssetRequest p: bindEpcList) {
            Log.i("bindEpcList", "bindEpcList " + p.getAssetno());
            s.put(p.getAssetno(), p);
        }
    }

    public SearchListAdapter(boolean withRemark, List<AssetsDetail> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.withRemark = withRemark;
        Log.i("withRemark", "withRemark" + withRemark);
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
    }

    public SearchListAdapter(boolean withRemark, List<AssetsDetail> assetResponse, List<AssetsDetail> wishList, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.withRemark = withRemark;
        this.wishList = wishList;
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
    }

    public SearchListAdapter(List<AssetsDetail> assetResponse, Context context, boolean borrowList) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.borrowList = true;
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
    }

    public SearchListAdapter(List<AssetsDetail> assetResponse, boolean returnList, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.returnList = returnList;
        integerArrayList = new ArrayList<>();

        setData(assetResponse, context);
    }

    public void setData(List<AssetsDetail> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;

        for(int i = 0; i < assetResponse.size(); i++) {
            List<AssetsDetail> arrayList = new ArrayList<>();
            //if (StockTakeListItemFragment.stockTakeList != null) {
            //    arrayList = StockTakeListItemFragment.stockTakeList.getAssets();

            //} else {
                arrayList = wishList;
            //}

            for (int y = 0; y < arrayList.size(); y++) {
                if (arrayList.get(y).getEpc().equals(getItem(i).getEpc())) {
                    //getItem(i).setFoundInStockTakeList(true);
                }
            }

            if (borrowList) {
                //getItem(i).setFoundInStockTakeList(true);
            }

            //Log.i("setFoundInSearchedEPCList", "setFoundInSearchedEPCList " + searchedEPCList + " " + getItem(i).getEPC());
            /*if(getItem(i).getEPC() != null && getItem(i).getEPC().length() > 0) {
                getItem(i).setFoundInSearchedEPCList(searchedEPCList.contains(getItem(i).getEPC()));
            } else {
                getItem(i).setFoundInSearchedEPCList(searchedEPCList.contains(getItem(i).getAssetno()));
            }*/
        }
    }

    public void setSearchedEPCList(ArrayList<String> searchedEPCList) {
        for(int i = 0; i < searchedEPCList.size(); i++) {
            if(this.searchedEPCList.contains(searchedEPCList.get(i))) {

            } else {
                this.searchedEPCList.add(searchedEPCList.get(i));
            }
        }

        ArrayList<String> newList = new ArrayList<>();
        for(int i = 0; i < searchedEPCList.size(); i++) {
            if(!newList.contains(searchedEPCList.get(i))) {
                newList.add(searchedEPCList.get(i));
            }
        }
        this.searchedEPCList = newList;
    }

    public void setSearchedEPCList(List<AssetsDetail> assetResponse) {
        this.assetResponse = assetResponse;
    }

    public ArrayList<String> getSearchedEPCList() {
        return searchedEPCList;
    }

    @Override
    public int getCount() {
        Log.i("getCount", "getCount " + assetResponse.size() + " " + hasMore);
        if(assetResponse.size() > 0) {
            return assetResponse.size();// + (hasMore && assetResponse.size() >= 50 ? 1 : 0);
        } else {
            return 0;
        }
    }

    @Override
    public AssetsDetail getItem(int i) {
        try {
            return assetResponse.get(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        try {
            return assetResponse.get(i).hashCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setTextView(TextView tv, String data) {
        if(data == null || data.length() == 0) {
            ((ViewGroup)tv.getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)tv.getParent()).setVisibility(View.VISIBLE);
            tv.setText(data);
        }
    }

    public void setWishList(ArrayList<AssetsDetail> wishList) {
        this.wishList = wishList;
    }

    private boolean request = false;
    public boolean hasMore = false;

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Log.i("assetDetail", "assetDetail " + i + " " + assetResponse.size());

        if(false) {

            view = LayoutInflater.from(context).inflate(R.layout.view_loading, viewGroup, false);
            if(!request) {
                request = true;

                List<Asset> assets = new ArrayList<>();


                if(SearchFormFragment.ASSET != null) {
                    SearchResultFragment.offset++;

                    Log.i("data", "data dd " +SearchResultFragment.offset);

                    DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
                    assets = db.searchAssetWithEPC(SearchFormFragment.ASSET.getAssetno().trim(), SearchFormFragment.ASSET.getName().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastCat().trim(), SearchFormFragment.ASSET.getBrand().trim(), SearchFormFragment.ASSET.getModel().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastLocation().trim(), SearchResultFragment.offset + "");
                    //assetResponse.addAll(assets);

                } else {
                    SearchListFragment.offset++;
                    Log.i("data", "data ddd " +SearchListFragment.offset);

                    DataBaseHandler db = new DataBaseHandler(MainActivity.mContext);
                    assets = db.getAssetWithEPC(SearchListFragment.filterText, SearchListFragment.offset * PAGE_SIZE + "");
                    //assetResponse.addAll(assets);
                }


                if(assets.size() == 0) {
                    final View tempView = view;

                    hasMore = false;
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            tempView.performClick();
                            tempView.invalidate();
                        }
                    };

                    final Handler handler = new Handler();
                    handler.postDelayed(runnable, 100);
                }

                notifyDataSetChanged();
                request = false;
            }
            return view;
        }

        view = LayoutInflater.from(context).inflate(R.layout.search_listview_cell, viewGroup, false);

        if(getItem(i) == null) return view;

        //if(getItem(i).isEPCOnly()) {
        //    ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getEPC());
        //    (view.findViewById(R.id.content_panel)).setVisibility(View.GONE);
        //} else {
            (view.findViewById(R.id.content_panel)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getAssetNo() + " | " + getItem(i).getName());
        //}

        //((TextView)view.findViewById(R.id.last_asset_no)).setText (getItem(i).getLastassetno());
        //((ViewGroup)((TextView)view.findViewById(R.id.last_asset_no)).getParent()).setVisibility(View.VISIBLE);

        //((LinearLayout)((TextView)view.findViewById(R.id.search_cell_prosecution_value)).getParent()).setVisibility(View.VISIBLE);

        //((TextView)view.findViewById(R.id.search_cell_prosecution_value)).setText (getItem(i).getProsecutionNo());
        ((TextView)view.findViewById(R.id.search_cell_brand_value)).setText (getItem(i).getBrand());
        ((TextView)view.findViewById(R.id.search_cell_model_value)).setText (getItem(i).getModel());
        ((TextView)view.findViewById(R.id.search_cell_category_value)).setText( getItem(i).getCategory());

        //setTextView((TextView)view.findViewById(R.id.search_cell_epc_value), (getItem(i).getEPC()));
        ((TextView)view.findViewById(R.id.search_cell_epc_value)).setText(getItem(i).getEpc());

        if(type != null) {
            ((ViewGroup)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.VISIBLE);
        } else {
            ((ViewGroup)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.GONE);
        }

        //if(s.get(getItem(i).getAssetno()) != null) {
        //    setTextView((TextView) view.findViewById(R.id.search_cell_new_epc_value), s.get(getItem(i).getAssetNo()).getEPC());
        //} else {
            setTextView((TextView) view.findViewById(R.id.search_cell_new_epc_value), "");
        //}
        setTextView ((TextView)view.findViewById(R.id.search_cell_location_value), getItem(i).getLocation() + " ");
        setTextView ((TextView)view.findViewById(R.id.search_cell_return_date_value), "");


        ((TextView)view.findViewById(R.id.search_cell_status_in_borrowed)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_disposed)).setVisibility(View.GONE);

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

            String status_key = StockTakeListItemFragment.stockTakeList != null ? InternalStorage.OFFLINE_CACHE.PENDING_STOCK_TAKE_STATUS_ + StockTakeListItemFragment.stockTakeList.getOrderNo() + "_" + getItem(i).getAssetNo() : "";
            int value = Hawk.get(status_key, -1);



            /*
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
            }*/
            //view.setBackgroundColor(context.getResources().getColor(R.color.ams_grey));//context.getResources().getColor(R.color.colorPrimary));

        } else {
            Log.i("case 5", "case 5");


            view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
            view.findViewById(R.id.missing_take).setVisibility(View.GONE);
            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);

            //Log.i("returnList", "returnList [" + getItem(i).getEPC() + "] " +  searchedEPCList.contains(getItem(i).getEPC()) + " " + searchedEPCList.size());

            /*
            for(int x = 0; x < searchedEPCList.size(); x++) {
                Log.i("searchedEPCList", "searchedEPCList [" + getItem(i).getEPC() + "] [" + searchedEPCList.get(x) + "] " + searchedEPCList.get(x).equals(getItem(i).getEPC()));
            }

             */

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

        Log.i("status", "status " +  getItem(i).getStatusid());
        String text = getItem(i).getStatusid();


        ((TextView) view.findViewById(R.id.search_cell_generic)).setText(new Status().getStatus(Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"), getItem(i).getStatusid()));

        if(getItem(i).getStatusid().equals("2"))
            ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        else if(getItem(i).getStatusid().equals("3") || getItem(i).getStatusid().equals("4"))
            ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
        else if(getItem(i).getStatusid().equals("5") || getItem(i).getStatusid().equals("6") || getItem(i).getStatusid().equals("7")|| getItem(i).getStatusid().equals("8") || getItem(i).getStatusid().equals("9999"))
            ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
        else {
            ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);
        }

        //!borrowList ||
        /*
        if(getItem(i).getStatusid() != null) {
            Log.i("getStatus", "getStatus " + getItem(i).getStatus().id);

            if (getItem(i).getStatus().id > 0) {
                Log.i("case1", "case1");

                if (getItem(i).getStatus().id > 0) {
                    String text = getItem(i).getStatus().getStatusString();
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
                }
            } else if(getItem(i).getStatus().getName() != null) {
                Log.i("case2", "case2");

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
                    Log.i("case3", "case3");

                    String text = getItem(i).getStatus().getStatusString();
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.VISIBLE);//setText(text);

                    if(getItem(i).getStatus().id == 9999) {
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    }

                } else {
                    Log.i("case4", "case4");

                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);//setText(text);

                }
            } else {
                Log.i("case5", "case5");

                ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);//setText(text);

            }
        }*/

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

        return view;
    }

    public void performClick(int i) {
        //if(borrowList)
        //    return;

        if(LoginFragment.SP_API) {
            AssetsDetailWithTabFragment.ASSET_NO = getItem(i).getAssetNo();

            if (withRemark)
                AssetsDetailWithTabFragment.WITH_REMARK = true;
            else
                AssetsDetailWithTabFragment.WITH_REMARK = false;

            List<AssetsDetail> assetsDetail = MainActivity.getAssetsDetailList(AssetsDetailWithTabFragment.ASSET_NO);


            if(assetsDetail == null && !((MainActivity) context).isNetworkAvailable())
                return;

            Log.i("replace9", "replace9");

            AssetsDetailWithTabFragment assetsDetailWithTabFragment = new AssetsDetailWithTabFragment();


            ((MainActivity) context).replaceFragment(assetsDetailWithTabFragment);
            return;
        }


        //Navigation.findNavController(((MainActivity)context).getSupportFragmentManager().getFragments().get(0).getView() ).navigate(R.id.action_searchListFragment_to_assetsDetailWithTabFragment);
    }


}
