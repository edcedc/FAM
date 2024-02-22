package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spit.fam.Entity.Asset;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.adapters.ReaderListAdapter;
import com.spit.cs108library4a.ReaderDevice;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class SimpleReaderListAdapter extends ReaderListAdapter {
    final boolean DEBUG = false;
    private final Context context;
    private final int resourceId;
    private final ArrayList<ReaderDevice> readersList;
    private boolean select4detail, select4Rssi, selectDupElim, select4Extra1, select4Extra2;
    public static String SELECTED = null;

    List<Asset> originalList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<>());

    public SimpleReaderListAdapter(Context context, int resourceId, ArrayList<ReaderDevice> readersList, boolean select4detail, boolean select4Rssi) {
        super(context, resourceId, readersList,select4detail, select4Rssi);
        this.context = context;
        this.resourceId = resourceId;
        this.readersList = readersList;
        this.select4detail = select4detail;
        this.select4Rssi = select4Rssi;
        select4Extra1 = false;
        select4Extra2 = false;
    }

    public SimpleReaderListAdapter(Context context, int resourceId, ArrayList<ReaderDevice> readersList, boolean select4detail, boolean select4Rssi, boolean selectDupElim, boolean select4Extra1, boolean select4Extra2) {
        super(context, resourceId, readersList, select4detail, select4Rssi, selectDupElim, select4Extra1, select4Extra2);
        this.context = context;
        this.resourceId = resourceId;
        this.readersList = readersList;
        this.select4detail = select4detail;
        this.select4Rssi = select4Rssi;
        this.selectDupElim = selectDupElim;
        this.select4Extra1 = select4Extra1;
        this.select4Extra2 = select4Extra2;
        SELECTED = null;//"48150012345678901191038392395";
        MainActivity.mCs108Library4a.appendToLog("select4Extra1 = " + select4Extra1 + ", select4Extra2 = " + select4Extra2);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReaderDevice reader = readersList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(resourceId, null);
        }

        TextView checkedTextView = (TextView) convertView.findViewById(R.id.reader_checkedtextview);
        //checkedTextView.setCheckMarkDrawable(null);
        checkedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SELECTED = checkedTextView.getText().toString();
                notifyDataSetChanged();
            }
        });

        String text1 = "";
        if (reader.getName() != null) {
            if (reader.getName().length() != 0) {
                text1 += reader.getName();
            }
        }
        if (reader.getAddress() != null) {
            if (reader.getAddress().length() != 0) {
                if (text1.length() != 0) text1 += "\n";
                text1 += reader.getAddress();
            }
        }

        boolean exist = false;
        for(int i = 0; i < originalList.size(); i++) {
            if(originalList.get(i).getEPC().equals(text1)) {
                exist = true;
            }
        }

        if(exist) {
            checkedTextView.setVisibility(View.GONE);
        } else {
            checkedTextView.setVisibility(View.VISIBLE);
        }

        checkedTextView.setText(text1);

        if(SELECTED != null && SELECTED.toLowerCase().equals(text1.toLowerCase())) {
            checkedTextView.setTextColor(Color.WHITE);
            checkedTextView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            checkedTextView.setTextColor(Color.BLACK);
            checkedTextView.setBackgroundColor(Color.WHITE);
        }

        if (reader.getSelected()) {
            //checkedTextView.setChecked(true);
        } else {
          //  checkedTextView.setChecked(false);
        }

        TextView countTextView = (TextView) convertView.findViewById(R.id.reader_count);
        if (reader.getCount() != 0) {
            countTextView.setText(String.valueOf(reader.getCount()));
        } else {
            countTextView.setVisibility(View.GONE);
        }

        if (select4Rssi) {
            TextView rssiTextView = (TextView) convertView.findViewById(R.id.reader_rssi);
            //rssiTextView.setVisibility(View.VISIBLE);
            double rssiValue = reader.getRssi();
            if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 && rssiValue > 0)
                rssiValue -= 106.98;
            rssiTextView.setText(String.format("%.1f", rssiValue));
        }

        if (select4Extra1) {
            TextView portTextView = (TextView) convertView.findViewById(R.id.reader_extra1);
            portTextView.setVisibility(View.VISIBLE);
            int portValue = reader.getPort() + 1;
            portTextView.setText(String.valueOf(portValue));
        }

        if (select4Extra2) {
            TextView portTextView = (TextView) convertView.findViewById(R.id.reader_extra2);
            portTextView.setVisibility(View.VISIBLE);
            int codeStatus = reader.getStatus();
            int codeSensor = reader.getCodeSensor(); int codeRssi = reader.getCodeRssi(); float codeTempC = reader.getCodeTempC();
            String brand = reader.getBrand();
            String strExtra = "";
            if (codeStatus > reader.INVALID_STATUS) { //for Bap tags
                int portstatus = reader.getStatus(); if (portstatus > reader.INVALID_STATUS) {
                    if ((portstatus & 2) == 0) strExtra += "Bat OK";
                    else strExtra += "Bat NG";
                    if ((portstatus & 4) != 0) strExtra += "\nTemper NG";
                }
            } else if (codeSensor > reader.INVALID_CODESENSOR && codeRssi > reader.INVALID_CODERSSI) { //for Axzon/Magnus tags
                strExtra = "SC=" + String.format("%d", codeSensor);
                int ocrssiMin = -1; int ocrssiMax = -1; boolean bValidOcrssi = false;
                ocrssiMax = Integer.parseInt(MainActivity.config.config1);
                ocrssiMin = Integer.parseInt(MainActivity.config.config2);
                if (ocrssiMax > 0 && ocrssiMin > 0 && (codeRssi > ocrssiMax || codeRssi < ocrssiMin)) strExtra += ("\n<font color=red>OCRSSI=" + String.format("%d", codeRssi) + "</font>");
                else {
                    bValidOcrssi = true; strExtra += ("\nOCRSSI=" + String.format("%d", codeRssi));
                }
                if (codeTempC > reader.INVALID_CODETEMPC) {
                    if (bValidOcrssi || portTextView.getText().toString().indexOf("T=") >= 0)
                        strExtra += ("\nT=" + String.format("%.1f", codeTempC) + (char) 0x00B0 + "C");
                }
                int backport = reader.getBackport1(); if (backport > reader.INVALID_BACKPORT) strExtra += String.format("\nBP1=%d", backport);
                backport = reader.getBackport2(); if (backport > reader.INVALID_BACKPORT) strExtra += String.format("\nBP2=%d", backport);
            } else if (codeTempC > reader.INVALID_CODETEMPC) { //for Ctesius tags
                strExtra = ("T=" + String.format("%.1f", codeTempC) + (char) 0x00B0 + "C");
            } else if (reader.getDetails().contains("E2806894")) { //for code8 tags
                strExtra = ((brand != null) ? ("Brand=" + brand) : "");
            } else if (reader.getSensorData() < reader.INVALID_SENSORDATA) {
                strExtra = "SD=" + String.valueOf(reader.getSensorData());
            }
            if (strExtra.length() != 0) portTextView.setText(Html.fromHtml(strExtra));
        }

        TextView readerDetailA = (TextView) convertView.findViewById(R.id.reader_detailA);
        TextView readerDetailB = (TextView) convertView.findViewById(R.id.reader_detailB);
        if (reader.isConnected() ||   select4detail == false) {
            /*
            readerDetailA.setText(reader.getDetails());
            readerDetailB.setText("");
            if (reader.isConnected()) {
                readerDetailB.setText("Connected");
            } else {
                int channel = reader.getChannel();
                int phase = reader.getPhase();
                String stringDetailB = null;
                if (channel != 0 || phase != 0) {
                    double dChannel = MainActivity.mCs108Library4a.getLogicalChannel2PhysicalFreq(reader.getChannel());
                    stringDetailB = "Phase=" + phase + "\n" + dChannel + "MHz";
                }
                if (stringDetailB != null) readerDetailB.setText(stringDetailB);
            }
            if (readerDetailA.getText().toString().length() != 0 || readerDetailB.getText().toString().length() != 0) {
                readerDetailA.setVisibility(View.VISIBLE);
                readerDetailB.setVisibility(View.VISIBLE);
            } else {
                readerDetailA.setVisibility(View.GONE);
                readerDetailB.setVisibility(View.GONE);
            }*/
        } else {
            readerDetailA.setVisibility(View.GONE);
            readerDetailB.setVisibility(View.GONE);
        }
        return convertView;
    }

    public boolean getSelectDupElim() { return selectDupElim; }
    public void setSelectDupElim(boolean selectDupElim) { this.selectDupElim = selectDupElim; }
}