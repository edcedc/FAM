package com.spit.fam.SystemFragment;

import static com.spit.fam.nike.ExtKt.POWER_OTHER;
import static com.spit.fam.nike.ExtKt.POWER_STOCKTAKE;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;
import com.spit.fam.CustomMediaPlayer;
import com.spit.fam.Entity.RFIDRssiDataUpdateEvent;
import com.spit.fam.Event.MuteEvent;
import com.spit.fam.Event.StopEvent;
import com.spit.fam.InventoryRfidTask;
import com.spit.fam.MainActivity;
import com.spit.fam.NewHandHeld.MyUtil;
import com.spit.fam.R;
import com.spit.fam.fragments.ConnectionFragment;
import com.spit.fam.fragments.HomeFragment;
import com.spit.cs108library4a.Cs108Connector;
import com.spit.cs108library4a.Cs108Library4A;
import com.spit.cs108library4a.ReaderDevice;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;

public class AssetSearchFragment extends HomeFragment {
    public static AssetSearchFragment newInstance() {
        return new AssetSearchFragment();
    }

    final double dBuV_dBm_constant = 106.98;
    final int labelMin = -80;
    final int labelMax = -30;

    private ProgressBar geigerProgress;
    private EditText editTextGeigerTagID;
    private CheckBox checkBoxGeigerTone;
    private SeekBar seekGeiger;
    private Spinner memoryBankSpinner;
    private EditText editTextRWSelectOffset, editTextGeigerAntennaPower;
    private TextView geigerThresholdView;
    private TextView geigerTagRssiView;
    private TextView geigerTagGotView;
    private TextView geigerRunTime, geigerVoltageLevelView;
    private TextView rfidYieldView;
    private TextView rfidRateView;
    public Button button;

    private boolean started = false;
    int thresholdValue = 0;

    private InventoryRfidTask geigerSearchTask;

    MediaPlayer mPlayer;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        playerO = MainActivity.sharedObjects.playerL;
        playerO.start();
        playerO.pause();

        playerO.getPlayer().setVolume(0f, 0f);

    }

    Runnable runnable4;
    Handler handler4 = new Handler();

    int value = 50;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState, true);

        runnable4 = new Runnable() {
            @Override
            public void run() {
                Log.i("second", "second");

                /*
                long diff = new Date().getTime() - lastupdateTime.getTime();

                long diffSeconds = diff / 1000;
                long diffMinutes = diff / (60 * 1000);

                Log.i("diff", "diff " + diff + " " + diffSeconds);

                if(diff >= 500) {
                    ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
                    ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);
                    mHandler.removeCallbacks(mAlertRunnable2);
                    playerO.getPlayer().setVolume(0f, 0f);
                    playerO.pause();
                    Log.i("diff", "pause");

                    ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            signal5.setVisibility(View.GONE);
                            signal4.setVisibility(View.GONE);
                            signal3.setVisibility(View.GONE);
                            signal2.setVisibility(View.GONE);
                            signal1.setVisibility(View.GONE);
                            signal0.setVisibility(View.VISIBLE);
                        }
                    });
                }
                */
                if(lastupdateTime != null) {
                    long diff = new Date().getTime() - lastupdateTime.getTime();

                    long diffSeconds = diff / 1000;
                    long diffMinutes = diff / (60 * 1000);

                    Log.i("second", "second diff " + diff + " " + diffSeconds);

                    if (diffSeconds >= 1 ) {
                        tempProgressValue = 0;//Float.parseFloat(getRSSI((int)tempProgressValue).substring(1));

                        mHandler.removeCallbacks(mAlertRunnable2);
 
                        playerO.getPlayer().setVolume(0f, 0f);
                        playerO.pause();
                        Log.i("diff", "pause");
                        ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
                        ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);

                        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                signal5.setVisibility(View.GONE);
                                signal4.setVisibility(View.GONE);
                                signal3.setVisibility(View.GONE);
                                signal2.setVisibility(View.GONE);
                                signal1.setVisibility(View.GONE);
                                signal0.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                handler4.postDelayed(this, 1000);
            }
        };
        handler4 = new Handler();
        handler4.postDelayed(runnable4, 1000);
/*
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ArrayList<EpcWithRssi> epcWithRssiArrayList = new ArrayList<>();

                EpcWithRssi epcWithRssi = new EpcWithRssi();
                epcWithRssi.setEpc("000000000000000000000001");
                epcWithRssi.setRssi("" + value);
                value += 5;

                Log.i("value", "value " + value);
                epcWithRssiArrayList.clear();;
                epcWithRssiArrayList.add(epcWithRssi);

                RFIDRssiDataUpdateEvent RFIDRssiDataUpdateEvent = new RFIDRssiDataUpdateEvent(epcWithRssiArrayList);
                EventBus.getDefault().post(RFIDRssiDataUpdateEvent);

                if(value < 130)
                    handler.postDelayed(this, 1000);
            }
        };

        //Handler handler = new Handler();
        //handler.postDelayed(runnable, 1000);
*/
        view = inflater.inflate(R.layout.assets_search_fragment, container, false);
        ((TextView) view.findViewById(R.id.text_view_progress)).setText((int) 0 + "");
        // mPlayer = MediaPlayer.create(getContext(), R.raw.zxing_beep);
        // mPlayer.setLooping(true);
        //  mPlayer.setVolume(0f, 0f);
        // mPlayer.start();
        signal5 = view.findViewById(R.id.signal_5);
        signal4 = view.findViewById(R.id.signal_4);
        signal3 = view.findViewById(R.id.signal_3);
        signal2 = view.findViewById(R.id.signal_2);
        signal1 = view.findViewById(R.id.signal_1);
        signal0 = view.findViewById(R.id.signal_0);

        return view;
    }

    ImageView signal0, signal1, signal2, signal3, signal4, signal5;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //android.support.v7.app.ActionBar actionBar;
        //actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        //actionBar.setIcon(R.drawable.dl_loc);
        //actionBar.setTitle(R.string.title_activity_geiger);
        getActivity().findViewById(R.id.signal_4).setVisibility(View.GONE);
        getActivity().findViewById(R.id.signal_3).setVisibility(View.GONE);
        getActivity().findViewById(R.id.signal_2).setVisibility(View.GONE);
        getActivity().findViewById(R.id.signal_1).setVisibility(View.GONE);
        getActivity().findViewById(R.id.signal_0).setVisibility(View.VISIBLE);

        TableRow tableRowProgressLabel;
        getActivity().findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ConnectionFragment());
            }
        });
        TextView textViewProgressLabelMin = (TextView) getActivity().findViewById(R.id.geigerProgressLabelMin);
        TextView textViewProgressLabelMid = (TextView) getActivity().findViewById(R.id.geigerProgressLabelMid);
        TextView textViewProgressLabelMax = (TextView) getActivity().findViewById(R.id.geigerProgressLabelMax);
        textViewProgressLabelMin.setText(String.format("%.0f", MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 ? labelMin : labelMin + dBuV_dBm_constant));
        textViewProgressLabelMid.setText(String.format("%.0f", MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 ? labelMin + (labelMax - labelMin) / 2 : labelMin + (labelMax - labelMin) / 2 + dBuV_dBm_constant));
        textViewProgressLabelMax.setText(String.format("%.0f", MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 ? labelMax : labelMax + dBuV_dBm_constant));

        geigerProgress = (ProgressBar) getActivity().findViewById(R.id.geigerProgress);
        editTextGeigerTagID = (EditText) getActivity().findViewById(R.id.selectTagID);
        if (AssetsDetailWithTabFragment.asset != null)
            editTextGeigerTagID.setText(AssetsDetailWithTabFragment.asset.getEPC());
        checkBoxGeigerTone = (CheckBox) getActivity().findViewById(R.id.geigerToneCheck);

        final ReaderDevice tagSelected = MainActivity.tagSelected;
        if (tagSelected != null) {
            if (tagSelected.getSelected() == true) {
                editTextGeigerTagID.setText(tagSelected.getAddress());
            }
        }

        seekGeiger = (SeekBar) getActivity().findViewById(R.id.geigerSeek);
        seekGeiger.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == seekGeiger && fromUser == true) {
                    thresholdValue = progress;
                    geigerThresholdView.setText(String.format("%.2f", MainActivity.mCs108Library4a.getRssiDisplaySetting() == 0 ? thresholdValue : thresholdValue - dBuV_dBm_constant));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        memoryBankSpinner = (Spinner) getActivity().findViewById(R.id.selectMemoryBank);
        ArrayAdapter<CharSequence> memoryBankAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        memoryBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memoryBankSpinner.setAdapter(memoryBankAdapter);
        memoryBankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: //if EPC
                        if (tagSelected != null)
                            editTextGeigerTagID.setText(tagSelected.getAddress());
                        editTextRWSelectOffset.setText("32");
                        break;
                    case 1:
                        if (tagSelected != null) {
                            if (tagSelected.getTid() != null)
                                editTextGeigerTagID.setText(tagSelected.getTid());
                        }
                        editTextRWSelectOffset.setText("0");
                        break;
                    case 2:
                        if (tagSelected != null) {
                            if (tagSelected.getUser() != null)
                                editTextGeigerTagID.setText(tagSelected.getUser());
                        }
                        editTextRWSelectOffset.setText("0");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editTextRWSelectOffset = (EditText) getActivity().findViewById(R.id.selectMemoryOffset);

        TableRow tableRowSelectPassword = (TableRow) getActivity().findViewById(R.id.selectPasswordRow);
        tableRowSelectPassword.setVisibility(View.GONE);

        editTextGeigerAntennaPower = (EditText) getActivity().findViewById(R.id.selectAntennaPower);
        editTextGeigerAntennaPower.setText(String.valueOf(300));

        geigerThresholdView = (TextView) getActivity().findViewById(R.id.geigerThreshold);
        geigerTagRssiView = (TextView) getActivity().findViewById(R.id.geigerTagRssi);
        geigerTagRssiView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (alertRssiUpdateTime < 0) return;
                double rssi = Double.parseDouble(geigerTagRssiView.getText().toString());
                if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0)
                    rssi += dBuV_dBm_constant;

                double progressPos = geigerProgress.getMax() * (rssi - labelMin - dBuV_dBm_constant) / (labelMax - labelMin);

                if(progressPos > 100) {
                    progressPos = 100;
                }

                if (progressPos < 0) progressPos = 0;
                if (progressPos > geigerProgress.getMax()) progressPos = geigerProgress.getMax();
                geigerProgress.setProgress((int) (progressPos));


                Log.i("progress", "progress " + progressPos);

                //mPlayer.setVolume((float) (progressPos / 100f),(float) (progressPos / 100f));
/*
                getActivity().findViewById(R.id.signal_4).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_3).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_2).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_1).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_0).setVisibility(View.INVISIBLE);

                if (((TextView) getActivity().findViewById(R.id.geigerTagGot)).getText().length() > 0) {
                    if (progressPos > 80) {
                        getActivity().findViewById(R.id.signal_5).setVisibility(View.VISIBLE);
                    } else if (progressPos > 70) {
                        getActivity().findViewById(R.id.signal_4).setVisibility(View.VISIBLE);
                    } else if (progressPos > 50) {
                        getActivity().findViewById(R.id.signal_3).setVisibility(View.VISIBLE);
                    } else if (progressPos > 30) {
                        getActivity().findViewById(R.id.signal_2).setVisibility(View.VISIBLE);
                    } else if (progressPos > 10) {
                        getActivity().findViewById(R.id.signal_1).setVisibility(View.VISIBLE);
                    } else if (progressPos > 0) {
                        getActivity().findViewById(R.id.signal_0).setVisibility(View.VISIBLE);
                    }
                }
*/
                ((TextView) view.findViewById(R.id.text_view_progress)).setText((int) progressPos + "");

                ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress((int) progressPos);

                alertRssiUpdateTime = System.currentTimeMillis();
                alertRssi = rssi;
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("afterTextChanged(): alerting = " + alerting + ", alertRssi = " + alertRssi);

                Log.i("progress", "progress afterTextChanged(): alerting = " + alerting + ", alertRssi = " + alertRssi);


                if (rssi > thresholdValue && checkBoxGeigerTone.isChecked()) {
                    if (alerting == false) {
                        alerting = true;
                        mHandler.removeCallbacks(mAlertRunnable);
                        mHandler.post(mAlertRunnable);
                        if (DEBUG)
                            MainActivity.mCs108Library4a.appendToLog("afterTextChanged(): mAlertRunnable starts");
                    }
                }
            }
        });
        geigerRunTime = (TextView) getActivity().findViewById(R.id.geigerRunTime);
        geigerTagGotView = (TextView) getActivity().findViewById(R.id.geigerTagGot);
        geigerVoltageLevelView = (TextView) getActivity().findViewById(R.id.geigerVoltageLevel);
        rfidYieldView = (TextView) getActivity().findViewById(R.id.geigerYield);
        rfidRateView = (TextView) getActivity().findViewById(R.id.geigerRate);
        button = (Button) getActivity().findViewById(R.id.geigerStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startStopHandler(false);

                int power = Hawk.get(POWER_STOCKTAKE, -1);
                if (power == -1){
                    power = 32;
                }
                byte[] bytes = new byte[]{(byte) power};
                MsgPowerConfig pMsg = new MsgPowerConfig(bytes);
//        MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt(/*"30"*/ (int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
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

                if (button.getText().toString().equals(getString(R.string.start))) {
                    button.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();

                                                                                                                                                                                                                                     ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
                    ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);

                    playerO.getPlayer().setVolume(1f, 1f);

                    signal5.setVisibility(View.GONE);
                    signal4.setVisibility(View.GONE);
                    signal3.setVisibility(View.GONE);
                    signal2.setVisibility(View.GONE);
                    signal1.setVisibility(View.GONE);
                    signal0.setVisibility(View.VISIBLE);

                } else {
                    button.setText(getString(R.string.start));
                    ((MainActivity) MainActivity.mContext).stop();
                    playerO.getPlayer().setVolume(0f, 0f);
                    playerO.pause();

                    mHandler.removeCallbacks(mAlertRunnable2);


                    ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
                    ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);

                    ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            signal5.setVisibility(View.GONE);
                            signal4.setVisibility(View.GONE);
                            signal3.setVisibility(View.GONE);
                            signal2.setVisibility(View.GONE);
                            signal1.setVisibility(View.GONE);
                            signal0.setVisibility(View.VISIBLE);
                        }
                    });


                }
            }
        });

        //playerN = MainActivity.sharedObjects.playerL;
    }

    Runnable timerRunnable = null;
    Handler timerHandler = new Handler();

    @Override
    public void onResume() {
        super.onResume();
        onResume = true;

        if (AssetsDetailWithTabFragment.asset != null && AssetsDetailWithTabFragment.asset.getEPC() != null)
            editTextGeigerTagID.setText(AssetsDetailWithTabFragment.asset.getEPC());

        setNotificationListener();
    }

    @Override
    public void onPause() {



        playerO.getPlayer().setVolume(0f, 0f);
        playerO.pause();

        ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
        ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);

        handler4.removeCallbacks(runnable4);
        mHandler.removeCallbacks(mAlertRunnable2);

        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                signal5.setVisibility(View.GONE);
                signal4.setVisibility(View.GONE);
                signal3.setVisibility(View.GONE);
                signal2.setVisibility(View.GONE);
                signal1.setVisibility(View.GONE);
                signal0.setVisibility(View.VISIBLE);
            }
        });

        if (mPlayer != null) {
            mPlayer.pause();
        }

        if(playerO != null) {
            playerO.pause();
            playerO.getPlayer().setVolume(0f, 0f);
        }

        PlaybackParams params = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            //params = new PlaybackParams();
            //params.setPitch(1);
            //playerO.getPlayer().setPlaybackParams(params);
        }

        mPlayer = null;
        onResume = false;

        editTextGeigerTagID.setText("");
        // MainActivity.mCs108Library4a.setNotificationListener(null);
        super.onPause();
        //playerN.pause();

        boolean started = false;
        if (geigerSearchTask != null)
            if (geigerSearchTask.getStatus() == AsyncTask.Status.RUNNING) started = true;

        if (started) {
            geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
        }
    }

    @Override
    public void onDestroy() {
        // MainActivity.mCs108Library4a.setNotificationListener(null);
        if (geigerSearchTask != null) {
            geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.DESTORY;
        }
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();

    }

    boolean userVisibleHint = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
        } else {
            userVisibleHint = false;
            MainActivity.mCs108Library4a.appendToLog("InventoryRfidiMultiFragment is now INVISIBLE");

            try {
                button.setText(getString(R.string.start));
                ((MainActivity) MainActivity.mContext).stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    double alertRssi;
    boolean alerting = false;
    long alertRssiUpdateTime;
    CustomMediaPlayer playerN;
    private final Runnable mAlertRunnable2 = new Runnable() {
        @Override
        public void run() {
            boolean alerting1 = true;
            final int toneLength = 50;

            mHandler.removeCallbacks(mAlertRunnable2);

            String progressPosString = ((TextView) view.findViewById(R.id.text_view_progress)).getText().toString();

            int progressPos = (int) ((Integer.parseInt(progressPosString) ) / 70f * 100);

            int tonePause = 0;

            /*
            if (progressPos > 100) {
                tonePause = 50;
            } else if (progressPos > 80) {
                tonePause = 100;
            } else if (progressPos > 60) {
                tonePause = 200;
            } else if (progressPos > 40) {
                tonePause = 400;
            } else if (progressPos > 30) {
                tonePause = 450;
            } else if (progressPos > 20) {
                tonePause = 500;
            } else if (progressPos > 10) {
                tonePause = 550;
            }

                                     if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 30) {
                            signal5.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 50) {
                            signal4.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 60) {
                            signal3.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 70) {
                            signal2.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) > 0) {
                            signal1.setVisibility(View.VISIBLE);
                        }/* else if (finalProgressValue > 0) {

            */

            if (tempProgressValue < 35) {
                tonePause = 100;
            } else if (tempProgressValue < 50) {
                tonePause = 300;
            } else if (tempProgressValue < 60) {
                tonePause = 500;
            } else if (tempProgressValue < 70) {
                tonePause = 600;
            } else if (tempProgressValue > 0) {
                tonePause = 700;
            }
            Log.i("tonePause", "tonePause "  +tonePause);

            if(tonePause > 0) {
                if (playerO.isPlaying()) {
                    playerO.getPlayer().setVolume(0f, 0f);
                    playerO.pause();
                } else {
                    playerO.getPlayer().setVolume(1f, 1f);
                    playerO.start();
                }
            }

            if(progressPos > 0) {
                mHandler.postDelayed(mAlertRunnable2, tonePause);
            } else {
                mHandler.removeCallbacks(mAlertRunnable2);
            }
        }
    };

    /*
    private final Runnable mAlertRunnable = new Runnable() {
        @Override
        public void run() {
            boolean alerting1 = true;
            final int toneLength = 50;
            mHandler.removeCallbacks(mAlertRunnable);
            if (alertRssi < 20 || alertRssi < thresholdValue || checkBoxGeigerTone.isChecked() == false || alertRssiUpdateTime < 0 || System.currentTimeMillis() - alertRssiUpdateTime > 200) {
                alerting1 = false;
            }
            if (alerting1 == false) {
                playerN.pause();
                ((TextView)view.findViewById(R.id.text_view_progress)).setText((int)0 + "");
                ((ProgressBar)view.findViewById(R.id.progress_bar)).setProgress((int)0);
                getActivity().findViewById(R.id.signal_4).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_3).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_2).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_1).setVisibility(View.GONE);
                getActivity().findViewById(R.id.signal_0).setVisibility(View.VISIBLE);
                alerting = false;
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): ENDS with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
            } else if (playerN.isPlaying() == false) {
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): TONE starts");
                mHandler.postDelayed(mAlertRunnable, toneLength);
                playerN.start();
            } else {
                int tonePause = 0;
                double rssi = Double.parseDouble(geigerTagRssiView.getText().toString());
                if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0)
                    rssi += dBuV_dBm_constant;
                double progressPos = geigerProgress.getMax() * (rssi - labelMin - dBuV_dBm_constant) / (labelMax - labelMin);
                 tonePause = 0;
                 rssi = Double.parseDouble(geigerTagRssiView.getText().toString());
                if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0)
                    rssi += dBuV_dBm_constant;
                progressPos = geigerProgress.getMax() * (rssi - labelMin - dBuV_dBm_constant) / (labelMax - labelMin);
                if (progressPos > 70) {
                    tonePause = 2000 - toneLength;
                } else if (progressPos > 50) {
                    tonePause = 1000 - toneLength;
                } else if (progressPos > 30) {
                    tonePause = 500 - toneLength;
                } else if (progressPos > 10) {
                    tonePause = 250 - toneLength;
                } else if (progressPos > 0) {
                    tonePause = toneLength;
                }
                 Log.i("alertRssi", "alertRssi " + alertRssi + " " + tonePause);
                if(tonePause > 0) {
                    playerN.pause();
                    mHandler.postDelayed(mAlertRunnable, tonePause);
                } else {
                }
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): START with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
                alerting = tonePause > 0 ? true : false;
                tonePause = 0;
                if (alertRssi >= 60) tonePause = toneLength;
                else if (alertRssi >= 50) tonePause = 250 - toneLength;
                else if (alertRssi >= 40) tonePause = 500 - toneLength;
                else if (alertRssi >= 30) tonePause = 1000 - toneLength;
                else if (alertRssi >= 20) tonePause = 2000 - toneLength;
                if (tonePause > 0)
                    mHandler.postDelayed(mAlertRunnable, tonePause);
                if (tonePause <= 0 || alertRssi < 60) {
                    playerN.pause(); if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Pause");
                }
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): START with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
                alerting = tonePause > 0 ? true : false;
            }
        }
    };*/
    private final Runnable mAlertRunnable = new Runnable() {
        @Override
        public void run() {
            boolean alerting1 = true;
            final int toneLength = 50;

            mHandler.removeCallbacks(mAlertRunnable);

            if (alertRssi < 20 || alertRssi < thresholdValue || checkBoxGeigerTone.isChecked() == false || alertRssiUpdateTime < 0 || System.currentTimeMillis() - alertRssiUpdateTime > 200)
                alerting1 = false;
            if (alerting1 == false) {
                playerN.pause();
                alerting = false;

                //if(System.currentTimeMillis() - alertRssiUpdateTime > 1000) {
                ((TextView) view.findViewById(R.id.text_view_progress)).setText((int) 0 + "");
                ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress((int) 0);
                //}

                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): ENDS with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
            } else if (playerN.isPlaying() == false) {
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): TONE starts");
                mHandler.postDelayed(mAlertRunnable, toneLength);

                playerN.start();
            } else {
                int tonePause = 0;
                if (alertRssi >= 80) tonePause = toneLength;
                else if (alertRssi >= 70) tonePause = 250 - toneLength;
                else if (alertRssi >= 60) tonePause = 500 - toneLength;
                else if (alertRssi >= 50) tonePause = 1000 - toneLength;
                else if (alertRssi >= 40) tonePause = 2000 - toneLength;
                if (tonePause > 0) mHandler.postDelayed(mAlertRunnable, tonePause);
                if (tonePause <= 0 || alertRssi < 60) {
                    playerN.pause();
                    if (DEBUG) MainActivity.mCs108Library4a.appendToLog("Pause");
                }
                if (DEBUG)
                    MainActivity.mCs108Library4a.appendToLog("mAlertRunnable(): START with new alerting1 = " + alerting1 + ", alertRssi = " + alertRssi);
                alerting = tonePause > 0 ? true : false;
            }
        }
    };

    void setNotificationListener() {
        MainActivity.mCs108Library4a.setNotificationListener(new Cs108Connector.NotificationListener() {
            @Override
            public void onChange() {
                startStopHandler(true);
            }
        });
    }

    void startStopHandler() {
        boolean started = false;
        if (geigerSearchTask != null) {
            if (geigerSearchTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        }
        if (
                ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus())
                        || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false)))
            return;
        if (started == false) {
        } else {
            geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            alertRssiUpdateTime = -1;
        }
    }

    void startStopHandler(boolean buttonTrigger) {
        boolean started = false;
        if (geigerSearchTask != null) {
            if (geigerSearchTask.getStatus() == AsyncTask.Status.RUNNING) started = true;
        }
        if (buttonTrigger == true &&
                ((started && MainActivity.mCs108Library4a.getTriggerButtonStatus())
                        || (started == false && MainActivity.mCs108Library4a.getTriggerButtonStatus() == false)))
            return;
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected() == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show();
                replaceFragment(new ConnectionFragment());
                return;
            } else if (MainActivity.mCs108Library4a.isRfidFailure()) {
                Toast.makeText(MainActivity.mContext, "Rfid is disabled", Toast.LENGTH_SHORT).show();
                replaceFragment(new ConnectionFragment());
                return;
            } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                Toast.makeText(MainActivity.mContext, R.string.toast_not_ready, Toast.LENGTH_SHORT).show();
                //replaceFragment(new ConnectionFragment());
                return;
            }
            startInventoryTask();
            alertRssiUpdateTime = 0;
        } else {
            if (buttonTrigger)
                geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.BUTTON_RELEASE;
            else geigerSearchTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP;
            alertRssiUpdateTime = -1;
        }
    }

    void startInventoryTask() {
        started = true;
        boolean invalidRequest = false;
        int memorybank = memoryBankSpinner.getSelectedItemPosition();
        int powerLevel = Integer.valueOf(editTextGeigerAntennaPower.getText().toString());

        Log.d("memorybank", "memorybank " + memorybank + " " + powerLevel);

        if (powerLevel < 0 || powerLevel > 330) invalidRequest = true;
        else if (MainActivity.mCs108Library4a.setSelectedTag(editTextGeigerTagID.getText().toString(), memorybank + 1, powerLevel) == false) {
            invalidRequest = true;
        } else {
            MainActivity.mCs108Library4a.startOperation(Cs108Library4A.OperationTypes.TAG_SEARCHING);
        }
        geigerSearchTask = new InventoryRfidTask(getContext(), -1, -1, 0, 0, 0, 0, invalidRequest, true,
                null, null, geigerTagRssiView, null,
                geigerRunTime, geigerTagGotView, geigerVoltageLevelView, null, button, rfidRateView);
        geigerSearchTask.execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MuteEvent event) {
        playerO.getPlayer().setVolume(0f, 0f);
        playerO.pause();

        ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
        ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);

         mHandler.removeCallbacks(mAlertRunnable2);

        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                signal5.setVisibility(View.GONE);
                signal4.setVisibility(View.GONE);
                signal3.setVisibility(View.GONE);
                signal2.setVisibility(View.GONE);
                signal1.setVisibility(View.GONE);
                signal0.setVisibility(View.VISIBLE);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(StopEvent event) {
        if (geigerSearchTask != null) {
            if (button.getText().toString().toLowerCase().equals("stop"))
                button.performClick();
            geigerSearchTask.cancel(true);
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

        }
    };
    ;
    CustomMediaPlayer playerO;
    private boolean onResume;
    Date lastupdateTime = null;

    float count = 0;
    float total = 0;

    public static String getRSSI(int ri)
    {
        byte rssi = 0;
        rssi = (byte)ri;
        int mantissa = (rssi & 0x07);
        int exponent = (rssi >> 3);
        double r = (20 * Math.log10(Math.pow(2, exponent) * (1 + mantissa / Math.pow(2, 3)))) - 110;
        return String.format("%.1f", r);
    }
    float tempProgressValue;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDRssiDataUpdateEvent event) {
         if(AssetsDetailWithTabFragment.position != 0) {
            return;
        }

        String epc = "";
        String myrssi = "";

        for(int i = 0; i < event.getArrayList().size(); i++) {
            if(event.getArrayList().get(i).getEpc().equals(AssetsDetailWithTabFragment.asset.getEPC())){
                epc = AssetsDetailWithTabFragment.asset.getEPC();
                myrssi = event.getArrayList().get(i).getRssi();
                break;
            }
        }

        Log.i("data", "RFIDRssiDataUpdateEvent " +  myrssi + " " + epc);

        if(epc.length() > 0){

        //if(event.getArrayList().get(0).getEpc().equals(AssetsDetailWithTabFragment.asset.getEPC())) {
            Log.i("assetSearch", "assetSearch " + epc);

            Log.i("RFIDRssiDataUpdateEvent", "RFIDRssiDataUpdateEvent " + myrssi);
            long diffSeconds = 1;

            count++;
            total += (int)Float.parseFloat(myrssi);

            if(lastupdateTime != null) {
                long diff = new Date().getTime() - lastupdateTime.getTime();
                diffSeconds = diff / 500;
            }

            if(diffSeconds > 0.5 && myrssi.length() > 0) {

                int progressValue = (int)(( (total / count ) - 45) / 70f * 100);
                 tempProgressValue = (total / count );

                Log.i("progressPos", "progressPos "  +(total / count ));

                total = 0f;
                count = 0f;
                ((TextView) view.findViewById(R.id.text_view_progress)).setText("" +progressValue);
                ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress((int) Integer.parseInt(progressValue + ""));

                progressValue = (int) (progressValue / 70f * 100);

                int finalProgressValue = progressValue;
                ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

/*
                    getActivity().findViewById(R.id.signal_4).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.signal_3).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.signal_2).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.signal_1).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.signal_0).setVisibility(View.INVISIBLE);
*/
                        signal5.setVisibility(View.GONE);
                        signal4.setVisibility(View.GONE);
                        signal3.setVisibility(View.GONE);
                        signal2.setVisibility(View.GONE);
                        signal1.setVisibility(View.GONE);
                        signal0.setVisibility(View.INVISIBLE);

                        //  if (((TextView) getActivity().findViewById(R.id.geigerTagGot)).getText().length() > 0) {
                        Log.i("rssi", "rssi " + Float.parseFloat(getRSSI((int)tempProgressValue).substring(1))  + " " + tempProgressValue);

                         if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 35) {
                            signal5.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 50) {
                            signal4.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 60) {
                            signal3.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) < 70) {
                            signal2.setVisibility(View.VISIBLE);
                        } else if (Float.parseFloat(getRSSI((int)tempProgressValue).substring(1)) > 0) {
                            signal1.setVisibility(View.VISIBLE);
                        }/* else if (finalProgressValue > 0) {
                            signal0.setVisibility(View.VISIBLE);
                        }*/
                        // }
                        tempProgressValue = Float.parseFloat(getRSSI((int)tempProgressValue).substring(1));

                        Log.i("progressPos", "progressPos "  + finalProgressValue);

                    }
                });
                lastupdateTime = new Date();

                handler.removeCallbacks(runnable);

                if(playerO != null && onResume) {
                    playerO.start();
                    mHandler.postDelayed(mAlertRunnable2, 0);
                }

                PlaybackParams params = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    params = new PlaybackParams();
                    int rssi = (int) Integer.parseInt(myrssi);
                    params.setPitch(2f * (rssi / 110f));
                    //playerO.getPlayer().setPlaybackParams(params);
                }

                long new_diff = new Date().getTime() - dateObj.getTime();
                long new_diffSeconds = new_diff / 1000;

                /*
                if(!singleMode && finalProgressValue > 80 && new_diffSeconds >= 1) {
                    try {
                        dateObj = new Date();

                        MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt( (int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
                        MyUtil.reader.Send(pMsg);
                        SessionInfo si = new SessionInfo();

                        si.Session = Session.values()[0];
                        si.Flag = Flag.values()[2];
                        //si.Session = Session.S0;
                        //si.Flag = Flag.Flag_A_B;

                        MsgSessionConfig msgS = new MsgSessionConfig(si);
                        MyUtil.reader.Send(msgS);

                        byte q = 0;
                        MsgQValueConfig msg = new MsgQValueConfig(q);
                        MyUtil.reader.Send(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    singleMode = true;
                    Log.i("singleMode", "[0]singleMode " + singleMode + " " + new_diffSeconds);
                }
                 */
                
                /*
                runnable = new Runnable() {
                    @Override
                    public void run() {

                        long diff = new Date().getTime() - lastupdateTime.getTime();

                        long diffSeconds = diff / 1000;
                        long diffMinutes = diff / (60 * 1000);

                        Log.i("diff", "diff " + diff + " " + diffSeconds);

                        if(diff >= 500) {
                            ((TextView) view.findViewById(R.id.text_view_progress)).setText("0");
                            ((ProgressBar) view.findViewById(R.id.progress_bar)).setProgress(0);
                             mHandler.removeCallbacks(mAlertRunnable2);
                             playerO.getPlayer().setVolume(0f, 0f);
                             playerO.pause();
                            Log.i("diff", "pause");

                            ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    signal5.setVisibility(View.GONE);
                                    signal4.setVisibility(View.GONE);
                                    signal3.setVisibility(View.GONE);
                                    signal2.setVisibility(View.GONE);
                                    signal1.setVisibility(View.GONE);
                                    signal0.setVisibility(View.VISIBLE);
                                }
                            });
                         }
                    }
                };

                handler.postDelayed(runnable, 500);

                 */
            }


            ((MainActivity)MainActivity.mContext).clean();
        } else {
            long new_diff = new Date().getTime() - dateObj.getTime();
            long new_diffSeconds = new_diff / 1000;
/*
            if(singleMode && new_diffSeconds >= 1) {
                dateObj = new Date();

                try {
                    MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt( (int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
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
                    MyUtil.reader.Send(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                singleMode = false;
                Log.i("singleMode", "[1]singleMode " + singleMode + " " + new_diffSeconds);
            }
            */
        }
    }

    boolean singleMode = true;
    private Date dateObj = new Date();
}
