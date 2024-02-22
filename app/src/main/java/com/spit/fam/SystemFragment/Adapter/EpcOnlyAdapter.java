package com.spit.fam.SystemFragment.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.MainActivity;
import com.spit.fam.R;

import java.util.ArrayList;

public class EpcOnlyAdapter extends BaseAdapter {
    public static int position = -1;

    public ArrayList<String> getEpcList() {
        return epcList;
    }

    public void setEpcList(ArrayList<String> epcList) {
        this.epcList = epcList;
    }

    private ArrayList<String> epcList;

    public EpcOnlyAdapter(ArrayList<String> epcList) {
        this.epcList = epcList;
    }

    @Override
    public int getCount() {
        return epcList.size();
    }

    @Override
    public String getItem(int position) {
        return epcList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return epcList.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.layout_epc_only, null);

        TextView textView = (TextView)view.findViewById(R.id.epc_data);

        textView.setText(getItem(position));

        if(EpcOnlyAdapter.position == position) {
            textView.setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.colorPrimary));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpcOnlyAdapter.position = position;
                Log.i("hihi", "hihi " + EpcOnlyAdapter.position);
                notifyDataSetChanged();
            }
        });

        return view;
    }
}
