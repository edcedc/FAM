package com.spit.fam.SystemFragment;

import static com.spit.fam.nike.ExtKt.POWER_OTHER;
import static com.spit.fam.nike.ExtKt.POWER_STOCKTAKE;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spit.fam.Event.DialogEvent;
import com.spit.fam.MainActivity;
import com.spit.fam.NewHandHeld.MyUtil;
import com.spit.fam.R;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import rfid.uhfapi_y2007.entities.AntennaPowerStatus;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgReaderCapabilityQuery;
import rfid.uhfapi_y2007.protocol.vrp.MsgRfidStatusQuery;

public class PowerSettingFragment extends BaseFragment{
    SeekBar seekBar, seekbarOther;
    TextView powerText, powerOther;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.power_setting_fragment_layout, null);
        powerText = view.findViewById(R.id.power);
        powerOther = view.findViewById(R.id.power_other);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)MainActivity.mContext).onBackPressed();
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)MainActivity.mContext).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        seekBar = view.findViewById(R.id.seekbar);
        seekbarOther = view.findViewById(R.id.seekbar_other);





        MsgReaderCapabilityQuery msg = new MsgReaderCapabilityQuery();
        if(MyUtil.reader.Send(msg)) {
            int antCount = msg.getReceivedMessage().getAntennaCount();
            int minPower = msg.getReceivedMessage().getMinPowerValue();
            int maxPower = msg.getReceivedMessage().getMaxPowerValue();

            if (antCount > 0) {
                String[] powers = new String[maxPower - minPower + 1];
                for (int j = minPower; j <= maxPower; j++) {
                    powers[j - minPower] = j + "";
                }

                MsgRfidStatusQuery msgState = new MsgRfidStatusQuery();
                if (MyUtil.reader.Send(msgState))
                {
                    AntennaPowerStatus[] aps = msgState.getReceivedMessage().getAntennas();
                    for (AntennaPowerStatus a : aps)
                    {
                        if(a.AntennaNO == 1) {
                           // for (int i = 0; i < spPower.getAdapter().getCount(); i++) {
                                //if (spPower.getAdapter().getItem(i).toString().equals(a.PowerValue + "")) {
                                //    spPower.setSelection(i);
                                //    break;
                                //}
                            //}
                            /*powerText.setText(" ( " + a.PowerValue + " ) ");
                            Log.i("power","power " + a.PowerValue);
                            seekBar.setProgress(a.PowerValue);*/
                            int s1 = Hawk.get(POWER_STOCKTAKE, -1);
                            int s2 = Hawk.get(POWER_OTHER, -1);
                            if (s1 != -1){
                                seekBar.setProgress(s1);
                                powerText.setText(" ( " + seekBar.getProgress() + " ) ");
                            }else {
                                seekBar.setProgress(a.PowerValue);
                                powerText.setText(" ( " + a.PowerValue + " ) ");
                            }
                            if (s2 != -1){
                                seekbarOther.setProgress(s2);
                                powerOther.setText(" ( " + seekbarOther.getProgress() + " ) ");
                            }else {
                                seekbarOther.setProgress(a.PowerValue);
                                powerOther.setText(" ( " + a.PowerValue + " ) ");
                            }
                            break;
                        }
                    }
                }
            }
        }
        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.home_settings));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.i("value", "value " + (int)(seekBar.getProgress() / 100f * 32));

                powerText.setText(" ( " + + progress + " ) ");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbarOther.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.i("value", "value " + (int)(seekBar.getProgress() / 100f * 32));

                powerOther.setText(" ( " + + progress + " ) ");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b){
        super.onCreateView(li,vg,b);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("value", "value " + seekBar.getProgress() + "---" + seekbarOther.getProgress());
                Hawk.put(POWER_STOCKTAKE, seekBar.getProgress());
                Hawk.put(POWER_OTHER, seekbarOther.getProgress());
                DialogEvent event = new DialogEvent(getString(R.string.app_name), getString(R.string.success));
                EventBus.getDefault().post(event);
                /*MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt(*//*"30"*//* (int)(seekBar.getProgress() / 100f * 32) + "")});
                if (!MyUtil.reader.Send(pMsg)) {
                    //errStr += "\r\n天线功率设置失败！" + pMsg.getErrorInfo().getErrMsg();
                    Log.i("error", "error");
                    DialogEvent event = new DialogEvent(getString(R.string.app_name), getString(R.string.fail));
                    EventBus.getDefault().post(event);
                } else {
                    DialogEvent event = new DialogEvent(getString(R.string.app_name), getString(R.string.success));
                    EventBus.getDefault().post(event);
                }*/
            }
        });

        return view;
    }
}
