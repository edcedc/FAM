package com.spit.fam.SystemFragment

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import com.orhanobut.hawk.Hawk
import com.spit.fam.*
import com.spit.fam.Entity.RenewEntity.ManualUpdateEvent
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset
import com.spit.fam.Entity.SPEntityP2.AssetsDetail
import com.spit.fam.Entity.SPEntityP2.BriefAsset
import com.spit.fam.Entity.SPEntityP2.PhotoUploadRequest
import com.spit.fam.Event.*
import com.spit.fam.Event.BarcodeScanEvent
import com.spit.fam.NewHandHeld.MyUtil
import com.spit.fam.fragments.InventoryRfidiMultiFragment
import com.spit.fam.nike.POWER_OTHER
import com.spit.fam.nike.POWER_STOCKTAKE
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rfid.uhfapi_y2007.entities.Flag
import rfid.uhfapi_y2007.entities.Session
import rfid.uhfapi_y2007.entities.SessionInfo
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig
import java.util.*
import java.util.concurrent.Executors

class RenewStockTakeFragment : InventoryRfidiMultiFragment() {

    companion object {
        lateinit var toolbar: String
        lateinit var stocktakeno: String
        lateinit var name: String

    }

    var isFirstRun : Boolean = false

    private var listViewAdapter : StockTakeAdapter? = null;

    private var fullList : Array<RealmStockTakeListAsset> = arrayOf<RealmStockTakeListAsset>()
    private var instockList : Array<RealmStockTakeListAsset> = arrayOf<RealmStockTakeListAsset>()
    private var missingList : Array<RealmStockTakeListAsset> = arrayOf<RealmStockTakeListAsset>()
    private var abnormalList : Array<RealmStockTakeListAsset> = arrayOf<RealmStockTakeListAsset>()

    var epcList: MutableList<String> = mutableListOf<String>()

    var tidList: MutableList<String> = mutableListOf<String>()
    var epcPosHashMap = HashMap<String, Integer>();
    var assetNoPosHashMap = HashMap<String, Integer>();

    var epcHashMap = HashMap<String, RealmStockTakeListAsset>();
    var tidHashMap = HashMap<String, RealmStockTakeListAsset>();
    var abnormalHashMap = HashMap<String, RealmStockTakeListAsset>();
    var instockHashMap = HashMap<String, RealmStockTakeListAsset>();

    var assetNoHashMap = HashMap<String, RealmStockTakeListAsset>();
    var locationHashMap = HashMap<String, String>();

    var instockEPCHashMap = HashMap<String, RealmStockTakeListAsset>();
    var instockTIDHashMap = HashMap<String, RealmStockTakeListAsset>();

    var instockEPCList: MutableList<String> = mutableListOf<String>()
    var instockTIDList: MutableList<String> = mutableListOf<String>()
    var abnormalEPCList: MutableList<String> = mutableListOf<String>()
    var abnormalTIDList: MutableList<String> = mutableListOf<String>()

    var requestedEPCList = mutableListOf<String>()

    private var tabLayout : TabLayout? = null;

    var playerO: CustomMediaPlayer? = null
    var playerN:CustomMediaPlayer? = null

    override fun onPause(){
        super.onPause()
        (view.findViewById(R.id.start) as Button).text = (MainActivity.mContext.getString(R.string.start))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*val pMsg = MsgPowerConfig(
            byteArrayOf(
                ((Hawk.get("power_stocktake", 100f) / 100f * 32).toInt()
                    .toString() + "").toInt().toByte()
            )
        )*/
        var power = Hawk.get(POWER_STOCKTAKE, -1)
        if (power == -1){
            power = 32
        }
        val bytes = byteArrayOf(power.toByte())
        val pMsg = MsgPowerConfig(bytes)
        MyUtil.reader.Send(pMsg)
        val si = SessionInfo()

        //si.Session = Session.values()[0];
        //si.Flag = Flag.values()[2];

        //si.Session = Session.values()[0];
        //si.Flag = Flag.values()[2];
        si.Session = Session.S0
        si.Flag = Flag.Flag_A_B

        val msgS = MsgSessionConfig(si)
        MyUtil.reader.Send(msgS)

        val q: Byte = 4
        val msg = MsgQValueConfig(q)
        MyUtil.reader.Send(msg, 500)

        playerO = MainActivity.sharedObjects.playerO
        playerN = MainActivity.sharedObjects.playerN

        MainActivity.sharedObjects.tagsList.clear()
        MainActivity.sharedObjects.tagsIndexList.clear()

        var data = ArrayList<String>();

        data.add("34152A84A000000000001801")
        data.add("400833B2DDD9014000000011")

        var rfid = RFIDDataUpdateEvent(data);


        Handler().postDelayed(Runnable {
            isFirstRun = true
            //EventBus.getDefault().post(rfid)
        }, 1000)

        view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.stock_take_list_item_fragment, null)

        var schTaskEx = Executors.newFixedThreadPool(500000);
        schTaskEx.execute(Runnable {
            view.findViewById<LinearLayout>(R.id.loading).setOnClickListener(null)
            view.findViewById<LinearLayout>(R.id.loading).visibility = View.VISIBLE
            initList()
            (MainActivity.mContext as MainActivity).runOnUiThread {
                setupListViewOnBackground()
            }
        })

        (view.findViewById(R.id.blocking) as LinearLayout).setOnClickListener {  }
        (view.findViewById<View>(R.id.edittext) as EditText).addTextChangedListener(CustomTextWatcher())
        (view.findViewById<View>(R.id.add) as ImageView).visibility = View.GONE
        //(view.findViewById<View>(R.id.back) as ImageView).visibility = View.INVISIBLE

        tabLayout = (view.findViewById<TabLayout>(R.id.tab_layout))

        (view.findViewById<TabLayout>(R.id.tab_layout)).setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                //setupListView(getData().toList())
                Log.i("case6", "Case6")

                setupListViewOnBackground()
                BaseUtils.hideKeyboard(view)
            }
        })


        (view.findViewById(R.id.start) as Button).setOnClickListener {
            // if (MainActivity.mCs108Library4a.isBleConnected() === false) {
            //    (MainActivity.mContext as MainActivity).replaceFragment(ConnectionFragment())
            //} else {


            //startStopHandler(false);
            var power = Hawk.get(POWER_STOCKTAKE, -1)
            if (power == -1) {
                power = 32
            }
            val bytes = byteArrayOf(power.toByte())
            val pMsg = MsgPowerConfig(bytes)
            MyUtil.reader.Send(pMsg)
            val si = SessionInfo()
            si.Session = Session.S0
            si.Flag = Flag.Flag_A_B
            val msgS = MsgSessionConfig(si)
            MyUtil.reader.Send(msgS)

            val q: Byte = 4
            val msg = MsgQValueConfig(q)
            MyUtil.reader.Send(msg, 500)

            if((view.findViewById(R.id.start) as Button).text.toString().equals((MainActivity.mContext.getString(R.string.start)))) {
                (view.findViewById(R.id.start) as Button).text = (MainActivity.mContext.getString(R.string.stop))

                (MainActivity.mContext as MainActivity).scanEpc()
                // (view.findViewById(R.id.inventoryRfidButton1) as Button).performClick()
            } else {
                (view.findViewById(R.id.start) as Button).text = (MainActivity.mContext.getString(R.string.start))

                (MainActivity.mContext as MainActivity).stop()
                // (view.findViewById(R.id.inventoryRfidButton1) as Button).performClick()
            }
            //}
        }

        (view.findViewById(R.id.confirm) as Button).setOnClickListener {
            save()
        }


        view.findViewById<View>(R.id.scan).setOnClickListener {
            var started = false
            var delayNeeded = false
            if (inventoryRfidTask != null) if (inventoryRfidTask.status == AsyncTask.Status.RUNNING) started = true
            if (started) {
                inventoryRfidTask.taskCancelReason = InventoryRfidTask.TaskCancelRReason.STOP
                delayNeeded = true
            }
            val runnable = Runnable {
                if (MainActivity.mCs108Library4a.isBleConnected == false) {
                    Log.i("openScanner case 1", "openScanner case 1")
                    openScanner()
                } else if (MainActivity.mCs108Library4a.isRfidFailure) {
                    Log.i("openScanner case 2", "openScanner case 2")
                    openScanner()
                } else if (MainActivity.mCs108Library4a.mrfidToWriteSize() != 0) {
                    Log.i("openScanner case 1", "openScanner case 3")
                    openScanner()
                } else {
                    scannerOpen = true
                    startStopBarcodeHandler(false)
                }
            }
            val handler = Handler()
            if (delayNeeded) {
                handler.postDelayed(runnable, 1000)
            } else {
                handler.post(runnable)
            }
        }



        view.findViewById<View>(R.id.st_back).setOnClickListener {
            (MainActivity.mContext as MainActivity).onBackPressed()
        }


        view.findViewById<View>(R.id.menu).setOnClickListener {
            (activity as MainActivity?)!!.updateDrawerStatus()
            (activity as MainActivity?)!!.mDrawerLayout.openDrawer(Gravity.RIGHT)
        }
    }

    fun openScanner() {
        MainActivity.SKIP_DOWNLOAD_ONCE = true
        IntentIntegrator(activity)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .setPrompt("")
                .setCameraId(0)
                .setBeepEnabled(true)
                .setBarcodeImageEnabled(true)
                .setCaptureActivity(CaptureActivityPortrait::class.java)
                .initiateScan()
    }


    var scannerOpen = false
    var inventoryBarcodeTask: InventoryBarcodeTask? = null
    fun startStopBarcodeHandler(buttonTrigger: Boolean) {
        if (buttonTrigger) MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: getTriggerButtonStatus = " + MainActivity.mCs108Library4a.triggerButtonStatus)
        if (MainActivity.sharedObjects.runningInventoryRfidTask) {
            Toast.makeText(MainActivity.mContext, "Running RFID inventory", Toast.LENGTH_SHORT).show()
            return
        }
        var started = false
        if (inventoryBarcodeTask != null) if (inventoryBarcodeTask!!.status == AsyncTask.Status.RUNNING) started = true
        if (buttonTrigger && (started && MainActivity.mCs108Library4a.triggerButtonStatus || started == false && MainActivity.mCs108Library4a.triggerButtonStatus == false)) {
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: trigger ignore")
            return
        }
        if (started == false) {
            if (MainActivity.mCs108Library4a.isBleConnected == false) {
                Toast.makeText(MainActivity.mContext, R.string.toast_ble_not_connected, Toast.LENGTH_SHORT).show()
                return
            }
            if (MainActivity.mCs108Library4a.isBarcodeFailure) {
                Toast.makeText(MainActivity.mContext, "Barcode is disabled", Toast.LENGTH_SHORT).show()
                return
            }
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: Start Barcode inventory")
            started = true
            inventoryBarcodeTask = InventoryBarcodeTask(MainActivity.sharedObjects.barsList, readerListAdapter, null, null, null, null, null, null, null, false)
            inventoryBarcodeTask!!.execute()
        } else {
            MainActivity.mCs108Library4a.appendToLog("BARTRIGGER: Stop Barcode inventory")
            if (buttonTrigger) inventoryBarcodeTask!!.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.BUTTON_RELEASE else inventoryBarcodeTask!!.taskCancelReason = InventoryBarcodeTask.TaskCancelRReason.STOP
        }
    }

    var serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")

    fun save() {

        AsyncTask.execute {
            Realm.getDefaultInstance().beginTransaction()

            var totalCount = (fullList.size + abnormalHashMap.values.size) * 1.0f
            var positionCount = 0 * 1.0f;

            for (i in fullList) {
                i.userName = Hawk.get(InternalStorage.Login.USER_ID, "")
                i.userId = serverId
                i.companyId = companyId
                Realm.getDefaultInstance().insertOrUpdate(i)

                (MainActivity.mContext as MainActivity).runOnUiThread {
                    view.findViewById<RelativeLayout>(R.id.save_progress).visibility = View.VISIBLE
                    view.findViewById<ProgressBar>(R.id.progress).progress = (positionCount as Float / totalCount as Float * 100).toInt()
                    positionCount += 1;
                }

            }

            for (value in abnormalHashMap.values) {
                value.userName = Hawk.get(InternalStorage.Login.USER_ID, "")
                value.userId = serverId
                value.assetno = ""
                value.pk = value.epc + value.stocktakeno
                value.name = ""
                value.brand = ""
                value.category = ""
                value.findType = "rfid"
                value.model = ""
                value.companyId = companyId
                value.userId = userId
                //qrcode
                if (value.remarks == null)
                    value.remarks = ""
                value.statusid = 9


                value.stocktakename = toolbar

                if (value.otherRono != null && value.otherRono.isNotEmpty()) {
                    Log.i("abnormal", "abnormal hihi " + value.otherRono);
                }
                value.tempStockTake = true
                Realm.getDefaultInstance().insertOrUpdate(value)

                (MainActivity.mContext as MainActivity).runOnUiThread {
                    view.findViewById<RelativeLayout>(R.id.save_progress).visibility = View.VISIBLE
                    view.findViewById<ProgressBar>(R.id.progress).progress = (positionCount as Float / totalCount as Float * 100).toInt()
                    positionCount += 1;
                }

            }


            Realm.getDefaultInstance().commitTransaction()

            var count = Realm.getDefaultInstance().where(RealmStockTakeListAsset::class.java)
                .equalTo("tempStockTake", true).findAll().count();

            val arrayList = Hawk.get(
                    InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST,
                    java.util.ArrayList<PhotoUploadRequest>()
            )
            Log.i(
                    "count",
                    "savecount " + count + " " + arrayList.size + "　" + InternalStorage.OFFLINE_CACHE.PENDING_PHOTO_UPLOAD_REQUEST
            );

            (MainActivity.mContext as MainActivity).runOnUiThread {

                val handler = Handler()
                handler.postDelayed(
                        Runnable {
                            (MainActivity.mContext as MainActivity).onBackPressed()

                            (MainActivity.mContext as MainActivity).updateDrawerStatus()

                            EventBus.getDefault().post(
                                    DialogEvent(
                                            (MainActivity.mContext as MainActivity)!!.getString(R.string.app_name),
                                            (MainActivity.mContext as MainActivity)!!.getString(R.string.upload_tips)
                                    )
                            )
                        }, 500)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        view.findViewById<RelativeLayout>(R.id.save_progress).setOnClickListener {

        }
        return view;
    }

    fun initList() {
        if(fullList.isNullOrEmpty()) {
            Log.i("assetno", "assetno " + RenewStockTakeFragment.stocktakeno);

            fullList = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(RealmStockTakeListAsset::class.java)
                    .equalTo("stocktakeno", RenewStockTakeFragment.stocktakeno)
                    .notEqualTo("statusid", (9).toInt())
                    .equalTo("userId", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                    .equalTo("companyId", Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))
                    .findAll()).toTypedArray();
            var position = 0;

            for(stockTake in fullList) {
                if(!stockTake.epc.isNullOrEmpty()) {
                    epcList.add(stockTake.epc)

                    epcHashMap.put(stockTake.epc, stockTake)
                    assetNoHashMap.put(stockTake.assetno, stockTake)

                    epcPosHashMap.put(stockTake.epc, position as Integer)
                    assetNoPosHashMap.put(stockTake.assetno, position as Integer)

                    position++;
                } else {
                }
            }
        }

        if(instockList.isNullOrEmpty()) {
            val list: MutableList<RealmStockTakeListAsset> = instockList.toMutableList();

            for(inventory in fullList) {
                if(inventory.statusid == 2) {
                    list.add(inventory)
                }
            }

            instockList = list.toTypedArray()
        }


        if(missingList.isNullOrEmpty()) {
            val list: MutableList<RealmStockTakeListAsset> = missingList.toMutableList();

            for(inventory in fullList) {
                if(inventory.statusid == 10) {
                    list.add(inventory)
                }
            }
            missingList = list.toTypedArray()
        }


        if(abnormalList.isNullOrEmpty()) {
            //abnormalList = RealmUtils.realmInstance.copyFromRealm((RealmUtils.getInventoryList("", "", "", "", "2"))).toTypedArray()
        }

        //Log.i("abnormalList", "abnormalList " + abnormalList.size + " " + RealmUtils.realmInstance.copyFromRealm((RealmUtils.getInventoryList("", "", "", "", "2"))).toTypedArray().size)
    }


    fun getData(): Array<RealmStockTakeListAsset> {
        var filterText = view.findViewById<TextView>(R.id.edittext).text.toString()
        var selectedPos = (view.findViewById<TabLayout>(R.id.tab_layout)).selectedTabPosition

        //Log.i("filterText", "filterText " + filterText + " " + (RealmUtils.getInventoryList( )).size + " " + RealmUtils.realmInstance.copyFromRealm((RealmUtils.getInventoryList(category, classDesc, location, filterText, ""))).toTypedArray().size + " " +  RealmUtils.realmInstance.copyFromRealm((RealmUtils.getInventoryList(category, classDesc, location, filterText, "2"))).toTypedArray().size);


        if (selectedPos == 0) {
            return filter(filterText, fullList.toList());
        }


        if (selectedPos == 1) {
            if(instockList.isNullOrEmpty()) {
                val list: MutableList<RealmStockTakeListAsset> = instockList.toMutableList();

                for(inventory in fullList) {
                    if(inventory.statusid == 2) {
                        list.add(inventory)
                    }
                }

                instockList = list.toTypedArray()
                Log.i("instockList", "instockList " + instockList.size)
            }

            return filter(filterText, instockList.toList());
        }

        if (selectedPos == 2) {

            if(missingList.isNullOrEmpty()) {
                val list: MutableList<RealmStockTakeListAsset> = missingList.toMutableList();

                for(inventory in fullList) {
                    if(inventory.statusid == 10) {
                        list.add(inventory)
                    }
                }
                missingList = list.toTypedArray()
                Log.i("missingList", "missingList " + missingList.size)
            }
            return filter(filterText, missingList.toList());
        }

        if (selectedPos == 3) {
            return filter(filterText, abnormalList.toList());
        }

        return arrayOf<RealmStockTakeListAsset>()
    }

    open fun setupListViewOnBackground() {
        //instockList = arrayOf<Inventory>()//.clear()
        //missingList = arrayOf<Inventory>()//.clear()

        view.findViewById<LinearLayout>(R.id.loading).setOnClickListener(null)
        view.findViewById<LinearLayout>(R.id.loading).visibility = View.VISIBLE

        //AsyncTask.execute(Runnable {
        var schTaskEx = Executors.newFixedThreadPool(500000);
        schTaskEx.execute(Runnable() {

            var inventory = ArrayList(getData().toList());
            try {
                (MainActivity.mContext as MainActivity).runOnUiThread {
                    view.findViewById<LinearLayout>(R.id.loading).visibility = View.GONE
                    setupListView(inventory)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                (MainActivity.mContext as MainActivity).runOnUiThread {
                    view.findViewById<LinearLayout>(R.id.loading).visibility = View.GONE
                }
            }
        })//.start()
    }

    fun setupListView(inventory: List<RealmStockTakeListAsset>) {
        (view!!.findViewById<View>(R.id.toolbar_title) as TextView)!!.text = toolbar + " (" + inventory.size + ")"

        (view.findViewById<View>(R.id.toolbar_title) as TextView).setOnClickListener { }
        listViewAdapter = StockTakeAdapter(inventory)
        (view!!.findViewById<View>(R.id.listview) as ListView)!!.adapter = listViewAdapter
        view!!.findViewById<LinearLayout>(R.id.loading).visibility = View.GONE
    }

    fun existIn(inventory: List<RealmStockTakeListAsset>, epc: String?) : Int {
        var exist : Int = -1;
        var count : Int = 0;

        for(i in inventory) {
            if( i.epc.equals(epc) || i.assetno.equals(epc)) {
                exist = count
                break;
            }
            count++;
       }

        Log.i("exist", "exist 3 " + exist);

        return exist;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: BarcodeScanEvent?) {

        instockList = arrayOf<RealmStockTakeListAsset>()
        missingList = arrayOf<RealmStockTakeListAsset>()


        Log.i("barcode", "barcode " + event!!.barcode)

        Handler().postDelayed(Runnable() {
            if (existIn(fullList.toList(), event!!.barcode) != -1) {
                var pos = existIn(fullList.toList(), event!!.barcode)

                if(fullList[pos].statusid != 2 ||  fullList[pos].findType.isNullOrEmpty() || fullList[pos].findType == "manual") {
                    fullList[pos].statusid = 2
                    fullList[pos].isFoundByScan = true
                    fullList[pos].tempStockTake = true
                    fullList[pos].findType = "barcode"
                    fullList[pos].scanDateTime = Date()
                }
                /*
                if(fullList[pos].statusID.isNullOrEmpty() || fullList[pos].scanDate.isNullOrEmpty() || fullList[pos].statusID != "1" || fullList[pos].foundStatus == "Manually") {
                    Log.i("case a", "case a 2 " + pos)

                    fullList[pos].statusID = "1"
                    fullList[pos].scanDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
                    fullList[pos].tempScanDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
                    fullList[pos].foundStatus = "QRCode"
                    fullList[pos].passCode = SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "PASSCODE")
                    fullList[pos].userName = SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "LOGIN_RECORD").split(",")[0]
                    fullList[pos].locationCode = SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "LOCATION")
                    fullList[pos].locationDescription = locationHashMap[SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "LOCATION")]

                    if(fullList[pos].locationDescription == null) {
                        fullList[pos].locationDescription = ""
                    }
                }*/

            }

            (MainActivity.mContext as MainActivity).runOnUiThread {
                try {
                    (view!!.findViewById<View>(R.id.toolbar_title) as TextView)!!.text = toolbar + " (" + listViewAdapter!!.inventory.size + ")"
                    listViewAdapter!!.inventory = getData().toList()
                    listViewAdapter!!.notifyDataSetChanged()
                    openScanner()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 10);

        scannerOpen = false
        //startStopBarcodeHandler(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ManualUpdateEvent?) {
        Log.i("ManualUpdateEvent", "ManualUpdateEvent assetNo " + event!!.assetNo);

        instockList = arrayOf<RealmStockTakeListAsset>()
        missingList = arrayOf<RealmStockTakeListAsset>()
        var schTaskEx = Executors.newFixedThreadPool(500000)
        schTaskEx.execute(Runnable {

            var schTaskEx = Executors.newFixedThreadPool(500000);
            // schTaskEx.execute( Runnable() {
            schTaskEx.execute(Runnable {
                var pos = existIn(fullList.toList(), event!!.assetNo)

                if (pos == -1) {
                    pos = existIn(abnormalList.toList(), event!!.assetNo)
                    abnormalList[pos].statusid = event.statudID
                    abnormalList[pos].remarks = event.remark

                    Log.i("statudID", "statudID " + event.statudID + " " + event.pic + " " + event.remark)

                    if (event.statudID == 2) {
                        abnormalList[pos].setFindType("manual")
                        abnormalList[pos].statusid = 2
                        abnormalList[pos].scanDateTime = Date()
                        abnormalList[pos].tempStockTake = true;
                    } else {
                        abnormalList[pos].statusid = event.statudID
                        abnormalList[pos].remarks = event.remark
                        //abnormalList[pos].tempStockTake = false;
                        // abnormalList[pos].scanDateTime = null
                    }
                    abnormalList[pos].pic = event.pic

                    listViewAdapter!!.inventory = getData().toList()

                    (MainActivity.mContext as MainActivity).runOnUiThread {
                        try {

                            (view!!.findViewById<View>(R.id.toolbar_title) as TextView)!!.text = toolbar + " (" + listViewAdapter!!.inventory.size + ")"

                            //listViewAdapter!!.inventory = getData().toList()
                            listViewAdapter!!.notifyDataSetChanged()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    fullList[pos].statusid = event.statudID
                    fullList[pos].remarks = event.remark

                    Log.i("statudID", "statudID " + event.statudID + " " + event.pic)

                    if (event.statudID == 2) {
                        if(fullList[pos].findType.isNullOrEmpty()) {
                            fullList[pos].setFindType("manual")
                        }
                        fullList[pos].statusid = 2
                        fullList[pos].scanDateTime = Date()
                        fullList[pos].tempStockTake = true;
                    } else {
                        fullList[pos].statusid = event.statudID
                        fullList[pos].tempStockTake = false;
                        fullList[pos].scanDateTime = null
                    }
                    fullList[pos].pic = event.pic

                    listViewAdapter!!.inventory = getData().toList()

                    (MainActivity.mContext as MainActivity).runOnUiThread {
                        try {

                            (view!!.findViewById<View>(R.id.toolbar_title) as TextView)!!.text = toolbar + " (" + listViewAdapter!!.inventory.size + ")"

                            //listViewAdapter!!.inventory = getData().toList()
                            listViewAdapter!!.notifyDataSetChanged()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })


        });
    }

    fun filter(filterText: String, inventory: List<RealmStockTakeListAsset>) : Array<RealmStockTakeListAsset> {
        var list = mutableListOf<RealmStockTakeListAsset>();
        if(filterText == null || filterText.length == 0) {
            return inventory.toTypedArray()
        } else {
            for (asset in inventory) {
                if (
                        (asset.assetno != null && asset.assetno.toLowerCase().contains(filterText.toLowerCase())) ||
                        (asset.name != null && asset.name.toLowerCase().contains(filterText.toLowerCase())) ||
                        (asset.brand != null && asset.brand.toLowerCase().contains(filterText.toLowerCase())) ||
                        (asset.category != null && asset.category.toLowerCase().contains(filterText.toLowerCase())) ||
                        (asset.location != null && asset.location.toLowerCase().contains(filterText.toLowerCase())) ||
                        (asset.epc != null && asset.epc.toLowerCase().contains(filterText.toLowerCase()))||
                        (asset.lastAssetNo != null && asset.lastAssetNo.toLowerCase().contains(filterText.toLowerCase()))
                ) {
                    list.add(asset)
                }
            }
        }
        return list.toTypedArray()
    }

    var companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")
    var userId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RFIDDataUpdateEvent?) {
        Log.i("RFIDDataUpdateEvent", "RFIDDataUpdateEvent " + event!!.data.size);

        if (playerN != null)
            playerN!!.start()
        //if (playerO != null)
        //    playerO!!.start()

        instockList = arrayOf<RealmStockTakeListAsset>()
        missingList = arrayOf<RealmStockTakeListAsset>()
        var changed : Boolean = false;

        // Handler().postDelayed( Runnable() {
        var schTaskEx = Executors.newFixedThreadPool(500000)
        schTaskEx.execute(Runnable {

            for (i in 0..event!!.data.size - 1) {

                Log.i("case a", "case a " + event!!.data[i] + " ")

                if (instockHashMap[event!!.data[i]] != null) {

                } else if (event!!.data[i].isNotEmpty() && epcHashMap[event!!.data[i]] != null) {
                    //var pos = existIn(fullList.toList(), event!!.data[i])
                    var data = epcHashMap[event!!.data[i]]

                    if (data!!.statusid == 10) {
                        data.statusid = 2
                        data.isFoundByScan = false
                        data.tempStockTake = true
                        data.scanDateTime = (Date())

                        //fullList[pos].scanDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
                        //fullList[pos].tempStockTake =  true//SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
                        //fullList[pos].foundStatus = "Scan"
                        //fullList[pos].passCode = SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "PASSCODE")
                        //fullList[pos].userName = SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "LOGIN_RECORD").split(",")[0]
                        //fullList[pos].locationCode = SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "LOCATION")
                        //fullList[pos].locationDescription = locationHashMap[SharedPrefsUtils.getStringPreference(LauncherActivity.mContext, "LOCATION")]

                        //if(fullList[pos].locationDescription == null) {
                        //    fullList[pos].locationDescription = ""
                        //}

                        changed = true;
                        //instockEPCList.add(event!!.data[i])
                        instockHashMap[event!!.data[i]] = data
                    }
                } else if (((event!!.data[i] != null && event!!.data[i].isNotEmpty())) && abnormalHashMap[event!!.data[i]] == null) {//(existIn(abnormalList.toList(), event!!.data[i].tid, event!!.data[i].epc) == -1)) {
                    val list: MutableList<RealmStockTakeListAsset> = (abnormalList).toMutableList();

                    var inventory = RealmStockTakeListAsset()

                    var editCase = false;

                    if (Realm.getDefaultInstance().where(AssetsDetail::class.java).equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("epc", event!!.data[i]).findAll().isNotEmpty()) {
                        var rawData = Realm.getDefaultInstance().where(AssetsDetail::class.java).equalTo("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")).equalTo("companyid", Hawk.get(InternalStorage.Setting.COMPANY_ID, "")).equalTo("epc", event!!.data[i]).findAll()[0]!!

                        inventory.statusid = 9
                        inventory.scanDateTime = Date()
                        inventory.stocktakeno = stocktakeno
                        inventory.tempStockTake = true
                        inventory.isFoundByScan = false
                        inventory.assetno = rawData.assetNo
                        inventory.model = rawData.model
                        inventory.category = rawData.category
                        inventory.location = rawData.location
                        inventory.epc = rawData.epc
                        inventory.name = rawData.name
                        inventory.remarks = ""
                        inventory.brand = rawData.brand
                        inventory.rono = rawData.rono
                        inventory.scanDateTime = Date()

                        Log.i("case1", "case1 " + inventory.scanDateTime);
                    } else {
                        inventory.epc = event!!.data[i]
                        inventory.statusid = 9
                        //inventory.scanDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())

                        inventory.scanDateTime = Date()
                        inventory.stocktakeno = stocktakeno

                        Log.i("case2", "case2 " + inventory.scanDateTime);
                    }

                    // inventory.tempStockTake = true//SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())

                    list.add(inventory)

                    abnormalList = list.toTypedArray()
                    //abnormalList = list.toTypedArray()
                    changed = true;

                    if (event!!.data[i] != null) {
                        abnormalEPCList.add(event!!.data[i])
                        abnormalHashMap[event!!.data[i]] = inventory
                    }
                    //TODO
                    //RetrofitClient.getSPGetWebService().getBriefAssetInfo(companyId, userId, event.data[i]).enqueue(GetBriefAssetObjectCallback(true))
                }

            }


            if (changed) {
                    (MainActivity.mContext as MainActivity).runOnUiThread {
                        try {
                             listViewAdapter!!.inventory = getData().toList()
                            (view!!.findViewById<View>(R.id.toolbar_title) as TextView)!!.text = toolbar + " (" + listViewAdapter!!.inventory.size + ")"

                            listViewAdapter!!.notifyDataSetChanged()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }

            schTaskEx.shutdown()
        })
        // } , 10);

        // }

        //sharedObjects.tagsList.clear()
        //sharedObjects.tagsIndexList.clear()

    }

    private var filterText: String? = null
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: CustomTextWatcherEvent?) {
        filterText = event!!.title.toLowerCase()

        if (filterText == null || filterText!!.length == 0) {
            //return
        }
        listViewAdapter!!.inventory = getData().toList()
        listViewAdapter!!.notifyDataSetChanged()

        (view!!.findViewById<View>(R.id.toolbar_title) as TextView)!!.text = toolbar + " (" + listViewAdapter!!.inventory.size + ")"
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: CallbackResponseEvent?)
    {
        if (event!!.response is BriefAsset) {

        }
    }

    inner class StockTakeAdapter(var inventory: List<RealmStockTakeListAsset>) : BaseAdapter() {
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            var v = p1

            if(v == null) {
                v = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.system_stock_take_listview_cell, null)
            }


            (v!!.findViewById(R.id.search_cell_title) as TextView).text = getItem(p0).assetno + " | " + getItem(p0).name
            (v!!.findViewById(R.id.search_cell_brand_value) as TextView).text = getItem(p0).brand
            (v!!.findViewById(R.id.search_cell_model_value) as TextView).text = getItem(p0).model
            (v!!.findViewById(R.id.search_cell_category_value) as TextView).text = getItem(p0).category
            (v!!.findViewById(R.id.search_cell_location_value) as TextView).text = getItem(p0).location
            (v!!.findViewById(R.id.search_cell_epc_value) as TextView).text = getItem(p0).epc


            (v!!.findViewById<TextView>(R.id.tick)).visibility = View.GONE
            (v!!.findViewById<TextView>(R.id.cross)).visibility = View.GONE
            (v!!.findViewById<TextView>(R.id.question_mark)).visibility = View.GONE

            v!!.setOnClickListener(View.OnClickListener {
                if (!getItem(p0).assetno.isNullOrEmpty()) {
                    AssetsDetailWithTabFragment.ASSET_NO = getItem(p0).assetno
                    AssetsDetailWithTabFragment.WITH_REMARK = true;
                    AssetsDetailWithTabFragment.realmStockTakeListAsset = getItem(p0)
                    AssetsDetailWithTabFragment.PIC_SITE = "http://" + Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") + "/GS1AMS_Second/"
                    Log.i("hihihi", "hihihi" + AssetsDetailWithTabFragment.PIC_SITE)
                    Log.i("replace3", "replace3")

                    (MainActivity.mContext as MainActivity).replaceFragment(AssetsDetailWithTabFragment())
                } else {
                }
            })


            if(tabLayout!!.selectedTabPosition == 3) {
                (v!!.findViewById<TextView>(R.id.question_mark)).visibility = View.VISIBLE
            } else if (getItem(p0).statusid == 2) {
                (v!!.findViewById<TextView>(R.id.tick)).visibility = View.VISIBLE
            } else if (getItem(p0).statusid == 10) {
                (v!!.findViewById<TextView>(R.id.cross)).visibility = View.VISIBLE
            } else if (getItem(p0).statusid == 9) {
                (v!!.findViewById<TextView>(R.id.question_mark)).visibility = View.VISIBLE
            } else {
                (v!!.findViewById<TextView>(R.id.cross)).visibility = View.VISIBLE
            }

            if(tabLayout!!.selectedTabPosition == 3) {
                (v!!.findViewById<TextView>(R.id.tick)).visibility = View.GONE
                (v!!.findViewById<TextView>(R.id.cross)).visibility = View.GONE
                (v!!.findViewById<TextView>(R.id.question_mark)).visibility = View.VISIBLE
            }

            if(getItem(p0).assetno.isNullOrEmpty() || getItem(p0).assetno.contains(",")) {
                ((v!!.findViewById(R.id.search_cell_title) as TextView).parent as ViewGroup).visibility = View.GONE
                ((v!!.findViewById(R.id.search_cell_location_value) as TextView).parent as ViewGroup).visibility = View.GONE
                ((v!!.findViewById(R.id.search_cell_brand_value) as TextView).parent as ViewGroup).visibility = View.GONE
                ((v!!.findViewById(R.id.search_cell_model_value) as TextView).parent as ViewGroup).visibility = View.GONE
                ((v!!.findViewById(R.id.search_cell_category_value) as TextView).parent as ViewGroup).visibility = View.GONE
            } else {
                ((v!!.findViewById(R.id.search_cell_title) as TextView).parent as ViewGroup).visibility = View.VISIBLE
                ((v!!.findViewById(R.id.search_cell_location_value) as TextView).parent as ViewGroup).visibility = View.VISIBLE
                ((v!!.findViewById(R.id.search_cell_brand_value) as TextView).parent as ViewGroup).visibility = View.VISIBLE
                ((v!!.findViewById(R.id.search_cell_model_value) as TextView).parent as ViewGroup).visibility = View.VISIBLE
                ((v!!.findViewById(R.id.search_cell_category_value) as TextView).parent as ViewGroup).visibility = View.VISIBLE
            }
           // (v.findViewById<View>(R.id.last_asset_no) as TextView).setText(getItem(p0).getLastAssetNo())
           // ((v.findViewById<View>(R.id.last_asset_no) as TextView).parent as ViewGroup).visibility =
            //    View.VISIBLE

            return v
        }

        override fun getItem(p0: Int): RealmStockTakeListAsset {
            return inventory!![p0]
        }

        override fun getItemId(p0: Int): Long {
            return inventory!![p0].hashCode().toLong()
        }

        override fun getCount(): Int {
            return inventory!!.size
        }
    }
}