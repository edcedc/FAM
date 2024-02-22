package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BorrowListFragment;
import com.spit.fam.SystemFragment.DisposalListFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.StockTakeListItemFragment;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import static com.spit.fam.SystemFragment.BorrowListItemListFragment.ABNORMAL;

public class DisposalListAdapter extends BaseAdapter {
    public static boolean WITH_EPC = true;

    List<Asset> assetResponse;
    ArrayList<String> searchedEPCList = new ArrayList<>();
    List<Asset> wishList = new ArrayList<>();

    Context context;
    boolean borrowList;
    boolean withRemark;

    public DisposalListAdapter(List<Asset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
    }

    public DisposalListAdapter(boolean withRemark, List<Asset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.withRemark = withRemark;
    }

    public DisposalListAdapter(boolean withRemark, List<Asset> assetResponse, List<Asset> wishList, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.withRemark = withRemark;
        this.wishList = wishList;
    }

    public DisposalListAdapter(List<Asset> assetResponse, Context context, boolean borrowList) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.borrowList = true;
    }

    public void setData(List<Asset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
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

    public void setSearchedEPCList(List<Asset> assetResponse) {
        this.assetResponse = assetResponse;
    }

    public ArrayList<String> getSearchedEPCList() {
        return searchedEPCList;
    }

    @Override
    public int getCount() {
        if(ABNORMAL) {
            int originalCount = 0;
            if(assetResponse != null) {
                originalCount = assetResponse.size();
            }

            if(originalCount > searchedEPCList.size()) {
                return  originalCount;
            } else {
                searchedEPCList.size();
            }
        }
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

        view.findViewById(R.id.search_cell_generic).setVisibility(View.GONE);

        if(getItem(i) == null)
            return view;

        if(getItem(i).isEPCOnly()) {
            ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getEPC());
            (view.findViewById(R.id.content_panel)).setVisibility(View.GONE);
        } else {
            (view.findViewById(R.id.content_panel)).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getAssetno() + " | " + getItem(i).getName());
        }

        setTextView  ((TextView)view.findViewById(R.id.search_cell_brand_value), (getItem(i).getBrand()));
        setTextView((TextView)view.findViewById(R.id.search_cell_model_value), (getItem(i).getModel()));
        setTextView((TextView)view.findViewById(R.id.search_cell_category_value), getItem(i).getCategoryString());
        setTextView((TextView)view.findViewById(R.id.search_cell_epc_value), (getItem(i).getEPC()));
        setTextView ((TextView)view.findViewById(R.id.search_cell_location_value), getItem(i).getLocationString());

        ((TextView)view.findViewById(R.id.search_cell_status_in_borrowed)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_disposed)).setVisibility(View.GONE);
        setTextView ((TextView)view.findViewById(R.id.search_cell_return_date_value), "");

        boolean found = false;

        //if(StockTakeListItemFragment.stockTakeList != null && StockTakeListItemFragment.stockTakeList.getAssets() != null)
        //    Log.i("yoyo", "yoyo " + StockTakeListItemFragment.stockTakeList.getAssets().size());

        //if(wishList != null )
        //    Log.i("yoyo", "yoyo " + wishList.size());

        if(StockTakeListItemFragment.stockTakeList != null || (wishList != null && wishList.size() > 0) || borrowList) {
            List<Asset> arrayList = new ArrayList<>();
            if(StockTakeListItemFragment.stockTakeList != null) {
                arrayList = StockTakeListItemFragment.stockTakeList.getAssets();
            } else {
                arrayList = wishList;
            }

            for (int y = 0; y < arrayList.size(); y++) {
                if (arrayList.get(y).getEPC().equals(getItem(i).getEPC())) {
                    found = true;
                }
            }

            if(borrowList) {
                found = true;
            }

            //Log.i("yoyoyo", "yoyoyo " + found +  " " + i +  getItem(i).isFound() + " " + arrayList.size());

            if ( (getItem(i).getStatus() != null && getItem(i).getStatus().id == 3) ||  getItem(i).isFoundInSearchedEPCList() ||   getItem(i).isFound() || (found && (searchedEPCList.contains(getItem(i).getEPC()) || searchedEPCList.contains(getItem(i).getAssetno()))) ) {
                view.findViewById(R.id.borrow_tick).setVisibility(View.VISIBLE);
                view.findViewById(R.id.missing_take).setVisibility(View.GONE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
            } else if (found && !searchedEPCList.contains(getItem(i).getEPC())) {
                view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
                view.findViewById(R.id.missing_take).setVisibility(View.VISIBLE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
            } else if (!found && !searchedEPCList.contains(getItem(i).getEPC())) {
                view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
                view.findViewById(R.id.missing_take).setVisibility(View.GONE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.VISIBLE);
            }

            if(getItem(i).isAbnormal()) {
                view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
                view.findViewById(R.id.missing_take).setVisibility(View.GONE);
                view.findViewById(R.id.abnormal_take).setVisibility(View.VISIBLE);
            }
        } else {
            view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
            view.findViewById(R.id.missing_take).setVisibility(View.GONE);
            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);

        }

        if(!(BorrowListFragment.POSITION == 1 || DisposalListFragment.POSITION == 1)) {
            view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
            view.findViewById(R.id.missing_take).setVisibility(View.GONE);
            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
        }

        if(!borrowList && getItem(i).getStatus() != null) {
            if(getItem(i).getStatus().getName() != null) {
                if (getItem(i).getStatus().getName().equals("In Library")) {
                    ((TextView) view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.VISIBLE);
                } else if (getItem(i).getStatus().getName().equals("In Borrowed")) {
                    ((TextView) view.findViewById(R.id.search_cell_status_in_borrowed)).setVisibility(View.VISIBLE);
                } else if (getItem(i).getStatus().getName().equals("Disposed")) {
                    ((TextView) view.findViewById(R.id.search_cell_status_disposed)).setVisibility(View.VISIBLE);
                }
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performClick(i);
            }
        });

        return view;
    }

    public void performClick(int i) {
        //if(borrowList)
        //    return;

        if(LoginFragment.SP_API) {
            AssetsDetailWithTabFragment.ASSET_NO = getItem(i).getAssetno();

            if(AssetsDetailWithTabFragment.ASSET_NO == null || AssetsDetailWithTabFragment.ASSET_NO.length() == 0)
                return;

            if (withRemark)
                AssetsDetailWithTabFragment.WITH_REMARK = true;
            else
                AssetsDetailWithTabFragment.WITH_REMARK = false;


            List<AssetsDetail> assetsDetail = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET + AssetsDetailWithTabFragment.ASSET_NO, null);

            try {
                if (!((MainActivity) context).isNetworkAvailable()) {
                    if (assetsDetail == null)
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i("replace7", "replace7");

            ((MainActivity) context).replaceFragment(new AssetsDetailWithTabFragment());
            return;
        }

        try {
            AssetsDetailWithTabFragment.id = Integer.parseInt(getItem(i).getId());
            AssetsDetailWithTabFragment.asset = (getItem(i));

            AssetsDetailWithTabFragment.assetRemark = getItem(i).getStock_take_asset_item_remark();

            if (withRemark)
                AssetsDetailWithTabFragment.WITH_REMARK = true;
            else
                AssetsDetailWithTabFragment.WITH_REMARK = false;

            if (getItem(i).getEPC() != null && getItem(i).getEPC().length() > 0) {
                WITH_EPC = true;
            } else {
                WITH_EPC = false;
            }
            Log.i("replace8", "replace8");

            ((MainActivity) context).replaceFragment(new AssetsDetailWithTabFragment());
        } catch (Exception e){
        }
        //Navigation.findNavController(((MainActivity)context).getSupportFragmentManager().getFragments().get(0).getView() ).navigate(R.id.action_searchListFragment_to_assetsDetailWithTabFragment);
    }


}

