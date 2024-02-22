package com.spit.fam.SystemFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spit.fam.Entity.TempItem;
import com.spit.fam.MainActivity;
import com.spit.fam.R;

import java.util.ArrayList;
import java.util.List;

public class NewStockTakeListAdapter extends BaseAdapter {
    List<TempItem> list = new ArrayList<>();

    public NewStockTakeListAdapter(List<TempItem> list) {
        this.list = list;
    }

    public void setData(List<TempItem> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public TempItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.search_listview_cell, null, false);
           // view = LayoutInflater.from(context).inflate(R.layout.stock_take_cell_item, viewGroup, false);
        }


        if(getItem(position).getAssetno().length() > 0) {
            ((TextView) view.findViewById(R.id.search_cell_title)).setText(getItem(position).getAssetno() + " " + getItem(position).getName());
        } else {
            ((TextView) view.findViewById(R.id.search_cell_title)).setText(getItem(position).getEpc());
        }

        Log.i("assetNo", "assetNo " + getItem(position).getAssetno() + " " + getItem(position).getEpc());
        if(getItem(position).getAssetno() == null || getItem(position).getAssetno().length() == 0) {
            ((LinearLayout)view.findViewById(R.id.search_cell_brand_value).getParent()).setVisibility(View.GONE);
            ((LinearLayout)view.findViewById(R.id.search_cell_model_value).getParent()).setVisibility(View.GONE);
            ((LinearLayout)view.findViewById(R.id.search_cell_category_value).getParent()).setVisibility(View.GONE);
            ((LinearLayout)view.findViewById(R.id.search_cell_location_value).getParent()).setVisibility(View.GONE);
            ((LinearLayout)view.findViewById(R.id.search_cell_epc_value).getParent()).setVisibility(View.GONE);
        } else {
            ((LinearLayout)view.findViewById(R.id.search_cell_brand_value).getParent()).setVisibility(View.VISIBLE);
            ((LinearLayout)view.findViewById(R.id.search_cell_model_value).getParent()).setVisibility(View.VISIBLE);
            ((LinearLayout)view.findViewById(R.id.search_cell_category_value).getParent()).setVisibility(View.VISIBLE);
            ((LinearLayout)view.findViewById(R.id.search_cell_location_value).getParent()).setVisibility(View.VISIBLE);
            ((LinearLayout)view.findViewById(R.id.search_cell_epc_value).getParent()).setVisibility(View.VISIBLE);
        }

        ((TextView)view.findViewById(R.id.search_cell_brand_value)).setText(getItem(position).getBrand());
        ((TextView)view.findViewById(R.id.search_cell_model_value)).setText(getItem(position).getModel());
        ((TextView)view.findViewById(R.id.search_cell_category_value)).setText(getItem(position).getCategory());
        ((TextView)view.findViewById(R.id.search_cell_location_value)).setText(getItem(position).getLocation());
        ((TextView)view.findViewById(R.id.search_cell_epc_value)).setText(getItem(position).getEpc());

        ((LinearLayout)((TextView)view.findViewById(R.id.search_cell_return_date_value)).getParent()).setVisibility(View.GONE);//.setText(getItem(position).getEpc());

        view.findViewById(R.id.search_cell_status_in_library).setVisibility(View.GONE);
        view.findViewById(R.id.search_cell_status_disposed).setVisibility(View.GONE);
        view.findViewById(R.id.search_cell_status_in_borrowed).setVisibility(View.GONE);
        view.findViewById(R.id.search_cell_generic).setVisibility(View.GONE);

        view.findViewById(R.id.borrow_tick).setVisibility(View.GONE);
        view.findViewById(R.id.missing_take).setVisibility(View.GONE);
        view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);

        if(NewStockTakeListItemFragment.tabPosition == 1) {
            view.findViewById(R.id.borrow_tick).setVisibility(View.VISIBLE);
        } else if(NewStockTakeListItemFragment.tabPosition == 2) {
            view.findViewById(R.id.missing_take).setVisibility(View.VISIBLE);
        } else if(NewStockTakeListItemFragment.tabPosition == 3) {
            if(getItem(position).getAssetno().length() > 0) {
                view.findViewById(R.id.abnormal_take).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.abnormal_take).setVisibility(View.GONE);
            }
        } else if(NewStockTakeListItemFragment.tabPosition == 0) {

            if(getItem(position).getStatusid() == 2 ){//|| getItem(position).getTempStatusId() == 2) {
                view.findViewById(R.id.borrow_tick).setVisibility(View.VISIBLE);
            } else if(getItem(position).getStatusid() == 10){// || getItem(position).getTempStatusId() == 10) {
                view.findViewById(R.id.missing_take).setVisibility(View.VISIBLE);
            } else if(getItem(position).getStatusid() == 9){// && getItem(position).getAssetno().isEmpty()){// || getItem(position).getTempStatusId() == 9) {
                view.findViewById(R.id.abnormal_take).setVisibility(View.VISIBLE);
            }
        }
        Log.i("view",  "view " + position +" " +getItem(position).getStatusid());

        return view;
    }
}
