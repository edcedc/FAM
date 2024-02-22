package com.spit.fam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.spit.fam.Entity.EpcWithRssi;
import com.spit.fam.Entity.RFIDRssiDataUpdateEvent;
import com.spit.fam.Event.RFIDDataUpdateEvent;
import com.spit.fam.NewHandHeld.MyUtil;
import com.spit.fam.NewHandHeld.TagMsgBaseAdapter;
import com.spit.fam.NewHandHeld.TagMsgEntity;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rfid.uhfapi_y2007.ApiApplication;
import rfid.uhfapi_y2007.IPort;
import rfid.uhfapi_y2007.Rs232Port;
import rfid.uhfapi_y2007.core.ErrInfo;
import rfid.uhfapi_y2007.core.Util;
import rfid.uhfapi_y2007.entities.ConnectResponse;
import rfid.uhfapi_y2007.entities.InventoryConfig;
import rfid.uhfapi_y2007.entities.MemoryBank;
import rfid.uhfapi_y2007.entities.ReadTagParameter;
import rfid.uhfapi_y2007.entities.RxdActiveTag;
import rfid.uhfapi_y2007.entities.RxdTagData;
import rfid.uhfapi_y2007.entities.TagParameter;
import rfid.uhfapi_y2007.protocol.vrp.Msg6CTagFieldConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgActiveTagInventory;
import rfid.uhfapi_y2007.protocol.vrp.MsgActiveTagInventoryStop;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerOff;
import rfid.uhfapi_y2007.protocol.vrp.MsgTagInventory;
import rfid.uhfapi_y2007.protocol.vrp.MsgTagRead;
import rfid.uhfapi_y2007.protocol.vrp.Reader;
import rfid.uhfapi_y2007.utils.Event;

import static rfid.uhfapi_y2007.core.Util.ConvertByteArrayToHexWordString;

public class NewMainActivity extends AppCompatActivity {

    Button btnConn;
    Button btnDisconn;
    Button btnClean;
    Button btnScanConfig;
    Button btnScanEpc;
    Button btnScanTid;
    Button btnScan;
    Button btnStop;
    Button btnTagRead;
    Button btnTagWrite;
    Button btnTagLock;
    Button btnTagKill;

    Button btnScan_Activity;
    Button btnStop_Activity;

    ListView tagList;
    TagMsgBaseAdapter mTagMsgBaseAdapter;
    private Event inventoryReceived = new Event(this, "reader_OnInventoryReceived");
    private Event brokenNetwork = new Event(this, "reader_OnBrokenNetwork");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.new_activity_main);

        new ApiApplication().init(getApplicationContext());
        Reader.OnBrokenNetwork.addEvent(brokenNetwork);

        btnConn = findViewById(R.id.btnConn);
        btnDisconn = findViewById(R.id.btnDisconn);
        btnScanEpc = findViewById(R.id.btnScanEPC);
        btnScanTid = findViewById(R.id.btnScanTID);
        btnScan = findViewById(R.id.btnScan);
        btnStop = findViewById(R.id.btnScanStop);
        btnClean = findViewById(R.id.btnClean);
        btnScanConfig = findViewById(R.id.btnScanConfig);
        btnTagRead = findViewById(R.id.btnTagRead);
        btnTagWrite = findViewById(R.id.btnTagWrite);
        btnTagLock = findViewById(R.id.btnTagLock);
        btnTagKill = findViewById(R.id.btnTagKill);
        btnScan_Activity = findViewById(R.id.btnScan_ActivityTag);
        btnStop_Activity = findViewById(R.id.btnScanStop_ActivityTag);

        btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conn();
            }
        });

        btnDisconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconn();
            }
        });

        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clean();
            }
        });

        btnScanConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanConfig();
            }
        });

        btnScanEpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanEpc();
            }
        });

        btnScanTid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanTid();
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        btnTagRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagRead();
            }
        });

        btnTagWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagWrite();
            }
        });

        btnTagLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagLock();
            }
        });

        btnTagKill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagKill();
            }
        });

        btnScan_Activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanActivity();
            }
        });

        btnStop_Activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopActivity();
            }
        });

        tagList = findViewById(R.id.tagList);
        mTagMsgBaseAdapter = new TagMsgBaseAdapter(null, this);
        tagList.setAdapter(mTagMsgBaseAdapter);// 设置listview的适配器

        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @SuppressWarnings("unchecked")
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TagMsgEntity tag = (TagMsgEntity)mTagMsgBaseAdapter.getItem(position);
                setSelectedTagParam(tag);
                setSelectedEpcTagParam(tag);
            }
        });

        MyUtil.inventoryConfig = new InventoryConfig();

        setCtrlEnable("断开");
    }

    private void conn() {
       disconn();
        IPort iPort = new Rs232Port("COM13,115200");
//        IPort iPort = new TcpClientPort("192.168.0.110:9090");
        MyUtil.reader = new Reader("reader1",iPort);
        ConnectResponse res = MyUtil.reader.Connect();
        if(res.IsSucessed){
            MyUtil.reader.OnInventoryReceived.addEvent(inventoryReceived);
            setCtrlEnable("连接");

            Msg6CTagFieldConfig msg = new Msg6CTagFieldConfig(false, true);
            MyUtil.reader.Send(msg);

            //Toast.makeText(this,"连接成功!",Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "连接失败：" + res.ErrorInfo.getErrMsg(), Toast.LENGTH_SHORT).show();
        }

    }

    private void disconn() {
        if(MyUtil.reader != null && MyUtil.reader.getIsConnected()){
            MyUtil.reader.OnInventoryReceived.removeEvent(inventoryReceived);
            MyUtil.reader.Disconnect();
            setCtrlEnable("断开");
        }
        MyUtil.reader = null;
    }

    public void clean() {
        mTagMsgBaseAdapter.cleanItem();
        MyUtil.selectParam = null;
        MyUtil.selectEpcParam = null;
    }

    private void scanConfig() {
        //Intent intent = new Intent(this,ScanConfigActivity.class);
        //startActivity(intent);
    }

    public void scanEpc() {
        stop();
        clean();
        if (MyUtil.reader != null && MyUtil.reader.getIsConnected()) {
            if(MyUtil.reader.Send(new MsgTagInventory())) {
                setCtrlEnable("扫描");
                //Toast.makeText(this, "正在扫描标签...", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(this, "RFID模块已断开连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanTid() {
        stop();
        clean();
        if (MyUtil.reader != null && MyUtil.reader.getIsConnected()) {
            if(MyUtil.reader.Send(new MsgTagRead())) {
                setCtrlEnable("扫描");
                //Toast.makeText(this, "正在扫描标签...", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(this, "RFID模块已断开连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void scan() {
        stop();
        clean();
        if (MyUtil.reader != null && MyUtil.reader.getIsConnected()) {
            ReadTagParameter param = new ReadTagParameter();
            param.IsLoop = true;
            param.ReadTime = MyUtil.inventoryConfig.ScanTime;
            param.ReadCount  = MyUtil.inventoryConfig.ScanCount;
            param.IsReturnEPC = MyUtil.inventoryConfig.IsScanEpc;
            param.IsReturnTID = MyUtil.inventoryConfig.IsScanTid;
            if(MyUtil.inventoryConfig.IsScanUser) {
                param.UserPtr = MyUtil.inventoryConfig.UserPtr;
                param.UserLen = MyUtil.inventoryConfig.UserLen;
            }
            MsgTagRead msg = new MsgTagRead(param);
            if(MyUtil.reader.Send(msg)){
                setCtrlEnable("扫描");
               //Toast.makeText(this,"正在扫描标签...",Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(this, "RFID模块已断开连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void reader_OnInventoryReceived(Reader sender, RxdTagData tagData)
    {
        if(tagData == null)
            return;
        String epc = ConvertByteArrayToHexWordString(tagData.getEPC());
        String tid = ConvertByteArrayToHexWordString(tagData.getTID());
        String user = ConvertByteArrayToHexWordString(tagData.getUser());
        String ant = (tagData.getAntenna() == 0) ? "" : "" + tagData.getAntenna();
        String rssi = (tagData.getRSSI() == 0) ? "" : "" + tagData.getRSSI();

        final TagMsgEntity tag = new TagMsgEntity("6C",rssi, ant, epc, tid, user);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dis(tag);
            }
        });
    }

    private void dis(TagMsgEntity tag) {
        //if(index >= orginalCount)
        //   Log.i("TagMsgEntity", "TagMsgEntity " + tag.getEPC() + " " + tag.getRssi() + " " + index + " "+ mTagMsgBaseAdapter.getCount());

             ArrayList<String> newArrayList = new ArrayList<>();
            newArrayList.add(tag.getEPC().replace(" ", "").toUpperCase());

            RFIDDataUpdateEvent rfidDataUpdateEvent = new RFIDDataUpdateEvent(newArrayList);
            EventBus.getDefault().post(rfidDataUpdateEvent);

            if(AssetsDetailWithTabFragment.SEARCH_ENABLED) {
                ArrayList<EpcWithRssi> epcWithRssiArrayList = new ArrayList<>();
                EpcWithRssi epcWithRssi = new EpcWithRssi();
                epcWithRssi.setEpc(tag.getEPC().replace(" ", "").toUpperCase());
                epcWithRssi.setRssi(tag.getRssi());

                epcWithRssiArrayList.add(epcWithRssi);

                RFIDRssiDataUpdateEvent RFIDRssiDataUpdateEvent = new RFIDRssiDataUpdateEvent(epcWithRssiArrayList);
                EventBus.getDefault().post(RFIDRssiDataUpdateEvent);
            }


    }

    public void stop() {
        if (MyUtil.reader != null)
            MyUtil.reader.Send(new MsgPowerOff());
        setCtrlEnable("停止");
    }

    private void tagRead() {
        if(MyUtil.selectParam == null){
            //Toast.makeText(this,"请选择标签",Toast.LENGTH_SHORT).show();
            return;
        }
        //Intent intent = new Intent(this,TagReadActivity.class);
        //startActivity(intent);
    }

    private void tagWrite() {
        if(MyUtil.selectParam == null){
            //Toast.makeText(this,"请选择标签",Toast.LENGTH_SHORT).show();
            return;
        }
        //Intent intent = new Intent(this,TagWriteActivity.class);
        //startActivity(intent);
    }

    private void tagLock() {
        if(MyUtil.selectParam == null){
            //Toast.makeText(this,"请选择标签",Toast.LENGTH_SHORT).show();
            return;
        }
       // Intent intent = new Intent(this,TagLockActivity.class);
       // startActivity(intent);
    }

    private void tagKill() {
        if(MyUtil.selectEpcParam == null){
            //Toast.makeText(this,"请选择标签EPC",Toast.LENGTH_SHORT).show();
            return;
        }
        //Intent intent = new Intent(this,TagKillActivity.class);
        //startActivity(intent);
    }

    // region 有源标签
    private void scanActivity() {
        stop();
        clean();
        if (MyUtil.reader != null && MyUtil.reader.getIsConnected()) {
            MsgActiveTagInventory msg = new MsgActiveTagInventory();
            if (MyUtil.reader.Send(msg)){
                setCtrlEnable("扫描");
                //Toast.makeText(this,"正在扫描标签...",Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(this, "RFID模块已断开连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void reader_OnActiveTagInventoryReceived(Reader sender, List<RxdActiveTag> tagData) {
        if (tagData == null)
            return;
        for (RxdActiveTag tag : tagData) {
            String tagid = Util.ConvertByteArrayToHexString(tag.TagData.TagID.Data);
            String rssi = (tag.RSSI == 0) ? "" : "" + tag.RSSI;
            String port = (tag.Port == 0) ? "" : "" + tag.Port;
            // TODO:
        }
    }

    private void reader_OnBrokenNetwork(String readerName, ErrInfo errInfo)
    {
        final String err = errInfo.getErrMsg();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(NewMainActivity.this,"网络异常断开:" + err,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void stopActivity() {
        if (MyUtil.reader != null)
            MyUtil.reader.Send(new MsgActiveTagInventoryStop());
        setCtrlEnable("停止");
    }
    // endregion

    private void setSelectedTagParam(TagMsgEntity tag) {

        MyUtil.selectParam = null;
        if(tag != null) {
            MyUtil.selectParam = new TagParameter();
            String tid = tag.getTID();
            String epc = tag.getEPC();
            if (tid != null && !tid.equals("")) {
                MyUtil.selectParam.TagData = Util.ConvertHexStringToByteArray(tid);
                MyUtil.selectParam.MemoryBank = MemoryBank.TIDMemory;
            } else if (epc != null && !epc.equals("")) {
                MyUtil.selectParam.TagData = Util.ConvertHexStringToByteArray(epc);
                MyUtil.selectParam.MemoryBank = MemoryBank.EPCMemory;
            }
        }
    }

    private void setSelectedEpcTagParam(TagMsgEntity tag) {

        MyUtil.selectEpcParam = null;
        if(tag != null) {
            MyUtil.selectEpcParam = new TagParameter();
            String epc = tag.getEPC();
            if(epc != null && !epc.equals("")) {
                MyUtil.selectEpcParam.TagData = Util.ConvertHexStringToByteArray(epc);
                MyUtil.selectEpcParam.MemoryBank = MemoryBank.EPCMemory;
            }
        }
    }

    private void setCtrlEnable(String state){

        switch (state){
            case "连接":
            case "停止":
                btnConn.setEnabled(false);
                btnDisconn.setEnabled(true);
                btnClean.setEnabled(true);
                btnScanConfig.setEnabled(true);
                btnScanEpc.setEnabled(true);
                btnScanTid.setEnabled(true);
                btnScan.setEnabled(true);
                btnStop.setEnabled(false);
                btnTagRead.setEnabled(true);
                btnTagWrite.setEnabled(true);
                btnTagLock.setEnabled(true);
                btnTagKill.setEnabled(true);
                btnScan_Activity.setEnabled(true);
                btnStop_Activity.setEnabled(false);
                break;
            case "断开":
                btnConn.setEnabled(true);
                btnDisconn.setEnabled(false);
                btnClean.setEnabled(true);
                btnScanConfig.setEnabled(false);
                btnScanEpc.setEnabled(false);
                btnScanTid.setEnabled(false);
                btnScan.setEnabled(false);
                btnStop.setEnabled(false);
                btnTagRead.setEnabled(false);
                btnTagWrite.setEnabled(false);
                btnTagLock.setEnabled(false);
                btnTagKill.setEnabled(false);
                btnScan_Activity.setEnabled(false);
                btnStop_Activity.setEnabled(false);
                break;
            case "扫描":
                btnConn.setEnabled(false);
                btnDisconn.setEnabled(false);
                btnClean.setEnabled(false);
                btnScanConfig.setEnabled(false);
                btnScanEpc.setEnabled(false);
                btnScanTid.setEnabled(false);
                btnScan.setEnabled(false);
                btnStop.setEnabled(true);
                btnTagRead.setEnabled(false);
                btnTagWrite.setEnabled(false);
                btnTagLock.setEnabled(false);
                btnTagKill.setEnabled(false);
                btnScan_Activity.setEnabled(false);
                btnStop_Activity.setEnabled(true);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconn();
        Reader.OnBrokenNetwork.removeEvent(brokenNetwork);
    }

    public void onResume() {
        super.onResume();
        conn();
    }

    public void onPause() {
        super.onPause();
        disconn();
    }
}
