package com.spit.fam.RenewSystemFragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.SPEntityP3.StocktakeList;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.Event.LoginDownloadProgressEvent;
import com.spit.fam.Event.NetworkInventoryDoneEvent;
import com.spit.fam.Event.ProgressEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.StockTakeListAdapter;
import com.spit.fam.SystemFragment.BaseFragment;
import com.spit.fam.SystemFragment.RenewStockTakeFragment;
import com.spit.fam.SystemFragment.SearchFormFragment;
import com.spit.fam.WebService.P2Callback.StocktakeListCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class StocktakeFragment extends BaseFragment {
    public static int STOCK_TAKE_API = 11;
    public static String STOCK_TAKE_NO_EDITED = null;

    List<StocktakeList> data;
    StockTakeListAdapter stockTakeListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    ListView listView;
    View noResult;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.stock_take_fragment, null);

        view.findViewById(R.id.blocking).setOnClickListener(null);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAPI();
            }
        });

        ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.stock_take));

        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ((EditText) view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        listView = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);

        view.findViewById(R.id.add).setVisibility(View.GONE);
        view.findViewById(R.id.scan).setVisibility(View.GONE);

        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SearchFormFragment());
            }
        });
        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity) getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li, vg, b);
        return view;
    }

    Parcelable state;
    public void onPause() {
        super.onPause();
        //state = listView.onSaveInstanceState();
    }

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    public void callAPI(){
        List<StocktakeList> stockTakeNoList =
                (Realm.getDefaultInstance().copyFromRealm(
                        Realm.getDefaultInstance().where(StocktakeList.class)
                                .greaterThanOrEqualTo("endDateObj", new Date())
                                .lessThanOrEqualTo("startDateObj", new Date())

                                .equalTo("companyid", companyId)
                                .equalTo("userid", serverId)
                                .findAll()));

        Log.i("data", "data " + stockTakeNoList.size());

        if(stockTakeNoList != null)
            EventBus.getDefault().post(new CallbackResponseEvent(stockTakeNoList));

        if (((MainActivity)MainActivity.mContext).isURLReachable())  {
            RetrofitClient.getSPGetWebService().renewStockTakeList(companyId, serverId).enqueue(new StocktakeListCallback(this.hashCode()));
        } else {
        }
    }

    public void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callAPI();
            }
        }, 500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        swipeRefreshLayout.setRefreshing(false);

        Log.i("yoyo", "yoyo " + event.type + " " + this.hashCode());

        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == StocktakeList.class) {

            List<StocktakeList> res = ((List<StocktakeList>) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(StocktakeList.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            for (int i = 0; i < res.size(); i++) {

                try {
                    res.get(i).setStartDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(res.get(i).getStartDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    res.get(i).setEndDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(res.get(i).getEndDate()));
                    Date expected = res.get(i).getEndDateObj();
                    Calendar c = Calendar.getInstance();
                    c.setTime(expected);
                    c.add(Calendar.DATE, 1);  // number of days to add
                    expected = (c.getTime());  // dt is now the new date
                    res.get(i).setEndDateObj(expected);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                res.get(i).setCompanyid(companyId);
                res.get(i).setUserid(serverId);
                Realm.getDefaultInstance().insertOrUpdate(res.get(i));
            }

            Realm.getDefaultInstance().commitTransaction();

            List<StocktakeList> stockTakeNoList =
                    (Realm.getDefaultInstance().copyFromRealm(
                            Realm.getDefaultInstance().where(StocktakeList.class)
                                    .greaterThanOrEqualTo("endDateObj", new Date())
                                    .lessThanOrEqualTo("startDateObj", new Date())

                                    .equalTo("companyid", companyId)
                                    .equalTo("userid", serverId)
                                    .findAll()));

            setupListView(stockTakeNoList);

        } else if(event.type == this.hashCode()) {
            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(StocktakeList.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

            Realm.getDefaultInstance().commitTransaction();

            setupListView(new ArrayList<>());
            handleNoResult(new ArrayList<>());
        }

    }

    public List<StocktakeList> filterArrayList(List<StocktakeList> assetResponse) {
        List<StocktakeList> filterResult =
                (Realm.getDefaultInstance().copyFromRealm(
                        Realm.getDefaultInstance().where(StocktakeList.class)
                                .greaterThanOrEqualTo("endDateObj", new Date())
                                .lessThanOrEqualTo("startDateObj", new Date())
                                .equalTo("companyid", companyId)
                                .equalTo("userid", serverId)
                                .findAll()));

        return filterResult;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProgressEvent event) {
        Log.i("ProgressEvent", "ProgressEvent " +  getString(R.string.loading) + " " + event.getCount() + "/" + event.getTotal() + "");

        //setProgress(((float) event.getCount() / (float) event.getTotal() ));
        ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.loading) + " " + event.getCount() + "/" + event.getTotal() + "");
        ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
        ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
        ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(((float) event.getCount() / (float) event.getTotal() ) * 100) );

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginDownloadProgressEvent event) {
        setProgress(event.getProgress());
    }

    public void setProgress(Float progress) {
        ((MainActivity)MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if((int)(progress * 100) >= 100) {
                    //((LinearLayout) view.findViewById(R.id.blocking)).setVisibility(View.GONE);
                    return;
                }

                //Log.i("data", "data " + (int)(progress * 100));

                ((TextView)view.findViewById(R.id.download_progress) ).setText( getString(R.string.downloading) + " " + (int)(progress * 100) + "/100");
                ((TextView)view.findViewById(R.id.unauthoried_device)).setVisibility(View.GONE);
                ((LinearLayout)view.findViewById(R.id.blocking)).setVisibility(View.VISIBLE);
                ((ProgressBar)view.findViewById(R.id.progress)).setProgress((int)(progress * 100));
            }

        });
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doneEvent(UpdateFailEvent event) {
        ((LinearLayout) view.findViewById(R.id.blocking)).setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doneEvent(NetworkInventoryDoneEvent event) {
        ((LinearLayout) view.findViewById(R.id.blocking)).setVisibility(View.GONE);
        // RenewStockTakeFragment.toolbar = stockTakeList.getName();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                RenewStockTakeFragment.toolbar = event.getName();
                RenewStockTakeFragment.stocktakeno = event.getStocktakeno();
                RenewStockTakeFragment.name = event.getName();

                // ((MainActivity)MainActivity.mContext).changeFragment(new OldStockTakeFragment());
                ((MainActivity)MainActivity.mContext).replaceFragment(new RenewStockTakeFragment());
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable,500);
    }

    public void setupListView(List<StocktakeList> data) {
        //this.data = data;
        handleNoResult(filterArrayList(data));
        AssetListAdapter.WITH_EPC = true;

        if(stockTakeListAdapter == null) {
            stockTakeListAdapter = new StockTakeListAdapter(filterArrayList(data), getActivity());
            listView.setAdapter(stockTakeListAdapter);
        } else {
            stockTakeListAdapter.setData(filterArrayList(data), getActivity());
            stockTakeListAdapter.notifyDataSetChanged();
        }


        if(state != null) {
            listView.onRestoreInstanceState(state);
        }
    }

    public void handleNoResult(List<StocktakeList> data) {
        Log.i("handleNoResult", "handleNoResult " +data.size() );

        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        //Log.i("event", "event " + event.getTitle());

        String filterText = event.getTitle().toLowerCase();

        if(filterText == null || filterText.length() == 0) {
            setupListView(data);
            return;
        }

      //  if(data != null && data.size() > 0) {
            List<StocktakeList> stockTakeLists = new ArrayList<>();

           // for(int i = 0; i < data.size(); i++) {
                /*
                boolean exist = false;

                if(data.get(i).getName().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(data.get(i).getStartDate() != null && data.get(i).getStartDate().toLowerCase().contains(filterText)) {
                    exist = true;
                } else if(data.get(i).getEndDate() != null && data.get(i).getEndDate().toLowerCase().contains(filterText)) {
                    exist = true;
                }

                if(exist)
                    stockTakeLists.add(data.get(i));*/

                stockTakeLists =
                        (Realm.getDefaultInstance().copyFromRealm(
                                Realm.getDefaultInstance().where(StocktakeList.class)
                                        .greaterThanOrEqualTo("endDateObj", new Date())
                                        .lessThanOrEqualTo("startDateObj", new Date())
                                        .equalTo("companyid", companyId)
                                        .equalTo("userid", serverId)

                                        .beginGroup()
                                        .contains("stocktakeno", filterText)
                                        .or()
                                        .contains("name", filterText)
                                        .or()
                                        .contains("startDate", filterText)
                                        .or()
                                        .contains("endDate", filterText)
                                        .endGroup()

                                        .findAll()));

          //  }

            handleNoResult(stockTakeLists);
            stockTakeListAdapter.setData(stockTakeLists, getActivity());
            stockTakeListAdapter.notifyDataSetChanged();
       // }
    }


}
