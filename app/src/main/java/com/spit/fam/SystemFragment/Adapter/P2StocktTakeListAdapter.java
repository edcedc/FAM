package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.StockTakeListItemFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class P2StocktTakeListAdapter extends BaseAdapter {
    List<StockTakeList> stockTakeListResponse;
    Context context;

    public P2StocktTakeListAdapter(List<StockTakeList> stockTakeListResponse, Context context) {
        this.context = context;
        this.stockTakeListResponse = stockTakeListResponse;
    }


    public void setData(List<StockTakeList> stockTakeListResponse, Context context) {
        this.context = context;
        this.stockTakeListResponse = stockTakeListResponse;
    }

    @Override
    public int getCount() {
        return stockTakeListResponse.size();
    }

    @Override
    public StockTakeList getItem(int position) {
        return stockTakeListResponse.get(position);
    }

    @Override
    public long getItemId(int position) {
        return stockTakeListResponse.get(position).hashCode();
    }

    public void setTextView (TextView tv, String data) {
        if((data == null || data.length() == 0 ) && tv != null) {
            ((ViewGroup)tv.getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)tv.getParent()).setVisibility(View.VISIBLE);
            tv.setText(data);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.stock_take_cell_item, viewGroup, false);

        StockTakeList stockTakeList = getItem(i);

        ((TextView)view.findViewById(R.id.cell_title)).setText( (stockTakeList.getOrderNo() != null ? stockTakeList.getOrderNo() : stockTakeList.getId()) + " | " + stockTakeList.getName());

        setTextView ((TextView)view.findViewById(R.id.start_date_value), stockTakeList.getStartDate());
        setTextView((TextView)view.findViewById(R.id.end_date_value), (stockTakeList.getEndDate()));
        if(stockTakeList.getProgress() == 0 && stockTakeList.getProgress() == 0) {
            ((ViewGroup)((TextView) view.findViewById(R.id.progress_value)).getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)((TextView) view.findViewById(R.id.progress_value)).getParent()).setVisibility(View.VISIBLE);
            setTextView((TextView) view.findViewById(R.id.progress_value), stockTakeList.getProgress() + " / " + stockTakeList.getTotalCount());
        }
        setTextView ((TextView)view.findViewById(R.id.last_update_value), stockTakeList.getLastUpdateTime());

        (view.findViewById(R.id.cell_status_new)).setVisibility(View.GONE);
        (view.findViewById(R.id.cell_status_processing)).setVisibility(View.GONE);
        (view.findViewById(R.id.cell_status_expired)).setVisibility(View.GONE);


        if(stockTakeList.getStartDateObject() != null && stockTakeList.getEndDateObject() != null) {
            if (isSameDate(stockTakeList.getStartDateObject(), new Date())) {
                //Log.i("case 1", "case 1");
                (view.findViewById(R.id.cell_status_new)).setVisibility(View.VISIBLE);
            } else if ((stockTakeList.getStartDateObject().before(new Date())) && new Date().before(stockTakeList.getEndDateObject())) {
                //Log.i("case 2", "case 2");
                (view.findViewById(R.id.cell_status_processing)).setVisibility(View.VISIBLE);
            } else if (new Date().after(stockTakeList.getEndDateObject())) {
                //Log.i("case 3", "case 3");
                (view.findViewById(R.id.cell_status_expired)).setVisibility(View.VISIBLE);
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginFragment.SP_API) {
                    Log.i("stockTakeList", "stockTakeList case 1 ");

                    StockTakeListItemFragment.stockTakeList = stockTakeList;//.getId();
                } else {
                    StockTakeListItemFragment.stockTakeListId = stockTakeList.getId();
                }
                if(((MainActivity)context).isNetworkAvailable()) {
                } else {
                    Log.i("stockTakeList", "stockTakeList case 2 ");

                    StockTakeListItemFragment.stockTakeList = stockTakeList;// getItem(i);
                }
                ((MainActivity)context).replaceFragment(new StockTakeListItemFragment());
            }
        });

        return view;
    }

    public boolean isSameDate(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        if(date1 == null || date2 == null)
            return  false;

        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return sameDay;
    }
}
