package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.Status;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BorrowListFragment;
import com.spit.fam.SystemFragment.BorrowListItemListFragment;
import com.spit.fam.SystemFragment.DisposalListFragment;
import com.spit.fam.SystemFragment.LoginFragment;

import java.util.ArrayList;
import java.util.List;

public class BriefAssetAdapter extends BaseAdapter {
    private List<BriefAsset> assetResponse;
    private Context context;
    private String type;
    private ArrayList<Integer> integerArrayList = new ArrayList<>();


    public BriefAssetAdapter(List<BriefAsset> assetResponse, Context context, String type) {
        this.assetResponse = assetResponse;
        this.context = context;
        setData(assetResponse, context);
        integerArrayList = new ArrayList<>();

        this.type = type;
    }


    public void setData(List<BriefAsset> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
    }

    @Override
    public int getCount() {
        return assetResponse.size();
    }

    @Override
    public BriefAsset getItem(int i) {
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


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.search_listview_cell, viewGroup, false);

        if(getItem(i) == null) return view;

        ((TextView)view.findViewById(R.id.search_cell_title)).setText(getItem(i).getAssetNo() + " | " + getItem(i).getName());


        ((TextView)view.findViewById(R.id.search_cell_brand_value)).setText (getItem(i).getBrand());
        ((TextView)view.findViewById(R.id.search_cell_model_value)).setText (getItem(i).getModel());
        ((TextView)view.findViewById(R.id.search_cell_category_value)).setText( getItem(i).getCategory());

        ((TextView)view.findViewById(R.id.search_cell_epc_value)).setText(getItem(i).getEpc());

        if(type != null) {
            ((ViewGroup)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.VISIBLE);
        } else {
            ((ViewGroup)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.GONE);
        }

        //TODO
        setTextView((TextView)view.findViewById(R.id.search_cell_new_epc_value), "");//(getItem(i).getNewEPC()));
        setTextView((TextView)view.findViewById(R.id.search_cell_return_date_value), "");//(getItem(i).getNewEPC()));

        setTextView ((TextView)view.findViewById(R.id.search_cell_location_value), getItem(i).getLocation());


        ((TextView)view.findViewById(R.id.search_cell_status_in_borrowed)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.search_cell_status_disposed)).setVisibility(View.GONE);


        if (getItem(i).getStatusid().length() > 0) {
            Log.i("case1", "case1");

            if (getItem(i).getStatusid().length()  > 0) {
                Status status = new Status();
                try {
                    status.id = Integer.parseInt(getItem(i).getStatusid());
                    String text = status.getStatusString();
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);
                } catch (Exception e ){}
                if(getItem(i).getStatusid().equals("2"))
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                else if(getItem(i).getStatusid().equals("3") || getItem(i).getStatusid().equals("4"))
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
                else if(getItem(i).getStatusid().equals("5") || getItem(i).getStatusid().equals("6") || getItem(i).getStatusid().equals("7")|| getItem(i).getStatusid().equals("8"))
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
                else {
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);
                }
            }
        }

        Log.i("demodemodemo", "demodemodemo " + BorrowListFragment.POSITION + " " + DisposalListFragment.POSITION);

        if(!(BorrowListFragment.POSITION == 1 || DisposalListFragment.POSITION == 1)) {
            view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
            view.findViewById(R.id.missing_take).setVisibility(View.GONE);
            view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
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

        return view;
    }



    public void performClick(int i) {
        //if(borrowList)
        //    return;

        if(LoginFragment.SP_API) {
            AssetsDetailWithTabFragment.ASSET_NO = getItem(i).getAssetNo();

            if(AssetsDetailWithTabFragment.ASSET_NO == null || AssetsDetailWithTabFragment.ASSET_NO.length() == 0)
                return;

            AssetsDetailWithTabFragment.WITH_REMARK = false;

            List<AssetsDetail> assetsDetail = MainActivity.getAssetsDetailList(AssetsDetailWithTabFragment.ASSET_NO);

            if(assetsDetail == null && !((MainActivity) context).isNetworkAvailable())
                return;

            AssetsDetailWithTabFragment assetsDetailWithTabFragment = new AssetsDetailWithTabFragment();

            AssetsDetailWithTabFragment.REMARK_FROM_SERVER = getItem(i).getRemarks();

            Log.i("replace6", "replace6");
            ((MainActivity) context).replaceFragment(assetsDetailWithTabFragment);
            return;
        }

        //Navigation.findNavController(((MainActivity)context).getSupportFragmentManager().getFragments().get(0).getView() ).navigate(R.id.action_searchListFragment_to_assetsDetailWithTabFragment);
    }
}
