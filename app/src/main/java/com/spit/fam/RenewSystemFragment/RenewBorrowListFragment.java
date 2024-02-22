package com.spit.fam.RenewSystemFragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Entity.SPEntityP3.BorrowListItem;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.NetworkRecallEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.BaseFragment;
import com.spit.fam.SystemFragment.BorrowListFragment;
import com.spit.fam.SystemFragment.BorrowListItemListFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.WebService.P2Callback.BorrowListCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class RenewBorrowListFragment  extends BaseFragment {
    private List<BorrowList> assetResponse;
    private View noResult;
    private ListView listView;

    private BorrowListAdapter assetListAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    public static int CONTINUOUS_DISPOSAL_API_1 = 70;
    public static int CONTINUOUS_DISPOSAL_API_2 = 80;
    public static int CONTINUOUS_DISPOSAL_API_3 = 90;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.borrow_list_fragment, null);

        Log.i("serverId", "serverId " + serverId);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callAPI();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 100);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        callAPI();
                    }
                }
        );

        listView = view.findViewById(R.id.listview);

        noResult = view.findViewById(R.id.no_result);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.borrow_list).toUpperCase());

        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                POSITION = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                BorrowListFragment.POSITION = -1;

                if(LoginFragment.SP_API) {
                    if (((MainActivity)getActivity()).isURLReachable()) {
                        int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

                        Log.i("callingAPI", "callingAPI " + position);
                        int callback = -1;
                        if(position == 0) {
                            callback = CONTINUOUS_DISPOSAL_API_1;
                        } else if(position == 1) {
                            callback = CONTINUOUS_DISPOSAL_API_2;
                        } else if(position == 2) {
                            callback = CONTINUOUS_DISPOSAL_API_3;
                        }
                        RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, position).enqueue(new BorrowListCallback(callback));
                    } else {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                                Log.i("callingCache", "callingCache " + position);

                                if(position == 0) {
                                    setupListView(getData());
                                }
                                if(position == 1) {
                                    setupListView(getData());
                                }
                                if(position == 2) {
                                    setupListView(getData());
                                }
                            }
                        },300);


                    }
                } else {
                    setupListView(getData());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(1).select();

    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);

        Log.i("disposalList", "disposalList");

        return view;
    }

    public static int POSITION = 0;

    public void onResume() {
        super.onResume();
        /*
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(POSITION).select();
                //callAPI();
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 200);*/
        Log.i("onResume", "onResume");
    }

    public void callAPI(){

        int position2 = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
        Log.i("position", "position " + position2);

        //gs135BB5E39D4DD4C0FB4C4307413C07B8420000000186
        if(position2 == 0) {
            setupListView(getData());
        }
        if(position2 == 1) {
            setupListView(getData());
        }
        if(position2 == 2) {
            setupListView(getData());
        }

        if (((MainActivity)getActivity()).isURLReachable()) {
            int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

            Log.i("callingAPI", "callingAPI " + position);
            int callback = -1;
            if(position == 0) {
                callback = CONTINUOUS_DISPOSAL_API_1;
            } else if(position == 1) {
                callback = CONTINUOUS_DISPOSAL_API_2;
            } else if(position == 2) {
                callback = CONTINUOUS_DISPOSAL_API_3;
            }
            RetrofitClient.getSPGetWebService().newBorrowList(companyId, serverId, position).enqueue(new BorrowListCallback(callback));

        }
    }


    public void handleNoResult(List<BorrowListItem> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            noResult.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    public void setupListView(List<BorrowListItem> assetResponse) {

        handleNoResult(filterArrayList(assetResponse));
        Log.i("assetResponse" , "assetResponse " + assetResponse.size() + " " + filterArrayList(assetResponse).size() + " " + listView.getVisibility());

        AssetListAdapter.WITH_EPC = true;

        if(assetListAdapter == null) {
            assetListAdapter = new BorrowListAdapter(filterArrayList(assetResponse), getActivity(), false, ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() );
            listView.setAdapter(assetListAdapter);
        } else {
            assetListAdapter.setData(filterArrayList(assetResponse), getActivity(),  ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition());
            assetListAdapter.notifyDataSetChanged();
        }
    }
    class BorrowListAdapter extends BaseAdapter {
        List<BorrowListItem> assetResponse;
        Context context;
        boolean borrowList;
        int type = -1;

        public BorrowListAdapter(List<BorrowListItem> assetResponse, Context context) {
            this.assetResponse = assetResponse;
            this.context = context;
        }

        public BorrowListAdapter(List<BorrowListItem> assetResponse, Context context, boolean borrowList) {
            this.assetResponse = assetResponse;
            this.context = context;
            this.borrowList = borrowList;
        }

        public BorrowListAdapter(List<BorrowListItem> assetResponse, Context context, boolean borrowList, int type) {
            this.assetResponse = assetResponse;
            this.context = context;
            this.borrowList = borrowList;
            this.type = type;
            Log.i("type", "type " + type);
        }

        public void setData(List<BorrowListItem> assetResponse, Context context) {
            this.assetResponse = assetResponse;
            this.context = context;
        }

        public void setData(List<BorrowListItem> assetResponse, Context context, int type) {
            this.assetResponse = assetResponse;
            this.context = context;
            this.type = type;
        }

        @Override
        public int getCount() {
            return assetResponse.size();
        }

        @Override
        public BorrowListItem getItem(int i) {
            return assetResponse.get(i);
        }

        @Override
        public long getItemId(int i) {
            return assetResponse.get(i).hashCode();
        }

        public void setTextView(TextView textView, String data) {
            if(textView == null)
                return;

            if(data == null) {
                ((ViewGroup)textView.getParent()).setVisibility(View.GONE);
            } else {
                ((ViewGroup)textView.getParent()).setVisibility(View.VISIBLE);
                textView.setText(data);
            }
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(context).inflate(R.layout.borrow_listview_cell, viewGroup, false);
            setTextView (((TextView)view.findViewById(R.id.cell_title)), ((getItem(i).getBorrowno()  != null ?  getItem(i).getBorrowno() + " | " : "") + getItem(i).getName()));

            setTextView( ((TextView)view.findViewById(R.id.approval_date)), (getItem(i).getApprovalDate()));
            setTextView( ((TextView)view.findViewById(R.id.apply_date)), (getItem(i).getApplyDate()));

            setTextView( ((TextView)view.findViewById(R.id.valid_date)), (getItem(i).getValidDate()));
            try {
                ((ViewGroup) view.findViewById(R.id.approved_by).getParent()).setVisibility(View.VISIBLE);

                ((TextView) view.findViewById(R.id.approved_by)).setVisibility(View.VISIBLE);
                setTextView( ((TextView) view.findViewById(R.id.approved_by)), (getItem(i).getApprovedby()));
            } catch (Exception e) {
                ((ViewGroup) view.findViewById(R.id.approved_by).getParent()).setVisibility(View.GONE);
            }

            ((TextView)view.findViewById(R.id.cell_status_applied)).setVisibility(View.GONE);
            ((TextView)view.findViewById(R.id.cell_status_approved)).setVisibility(View.GONE);
            ((TextView)view.findViewById(R.id.cell_status_rejected)).setVisibility(View.GONE);

            Log.i("case1", "case 1 " + type);
            if(type != -1) {

                if(type == 2) {
                    ((TextView)view.findViewById(R.id.cell_status_rejected)).setVisibility(View.VISIBLE);
                } else if(type == 0) {
                    ((TextView)view.findViewById(R.id.cell_status_applied)).setVisibility(View.VISIBLE);
                } else if(type == 1) {
                    ((TextView)view.findViewById(R.id.cell_status_approved)).setVisibility(View.VISIBLE);
                }
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //BorrowListItemListFragment.borrowList = getItem(i);
                    BorrowListItemListFragment.type = type;

                    RenewBorrowListItemFragment borrowListItemListFragment = new RenewBorrowListItemFragment();


                    if(getItem(i).getBorrowno() != null && getItem(i).getBorrowno().length() > 0) {
                        RenewBorrowListItemFragment.BORROW_NO = getItem(i).getBorrowno();
                        RenewBorrowListItemFragment.BORROW_NAME = getItem(i).getName();

                        RenewBorrowListItemFragment.BORROW_LIST = true;
                    } else {
                        RenewBorrowListItemFragment.DISPOSAL_NO = null;
                        RenewBorrowListItemFragment.DISPOSAL_LIST = false;
                    }

                    Log.i("BORROW_LIST", "BORROW_LIST " + BorrowListItemListFragment.BORROW_LIST);
                    Log.i("DISPOSAL_LIST", "DISPOSAL_LIST " + BorrowListItemListFragment.DISPOSAL_LIST);

                    /*
                    BorrowListAssets disposalno = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ +  getItem(i).getBorrowno());


                    if(!((MainActivity)context).isNetworkAvailable()){
                        if(RenewBorrowListItemFragment.DISPOSAL_LIST && disposalno == null) {
                            EventBus.getDefault().post(new DialogEvent(context.getString(R.string.app_name), context.getString(R.string.no_data)));
                            return;
                        }
                    }
                    */

                    ((MainActivity) context).replaceFragment(borrowListItemListFragment);
                }
            });

            ((TextView) view.findViewById(R.id.borrow_disposal_progress)).setText(context.getString(R.string.borrowed_item));


            if((type == 2 || type == 0) &&  ((getItem(i).getTotal()) != (getItem(i).getApproved()))) {
                Log.i("case 1" , "disposal case 1");

                setTextView(((TextView) view.findViewById(R.id.borrowed_item)), ((getItem(i).getTotal()) - (getItem(i).getApproved())) + "/" + ((getItem(i).getTotal()) - (getItem(i).getApproved())) );
                ((TextView) view.findViewById(R.id.borrow_disposal_progress)).setText(context.getString(R.string.not_approved));
            } else {
                Log.i("case 1" , "disposal case 2");

                setTextView(((TextView) view.findViewById(R.id.borrowed_item)), (getItem(i).getBorrowed()) + "/" + ((getItem(i).getApproved())));
            }



            return view;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkRecallEvent event) {
        callAPI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("CallbackResponseEvent", "CallbackResponseEvent " + event);
        swipeRefreshLayout.setRefreshing(false);

        if (event.type == CONTINUOUS_DISPOSAL_API_1 || event.type == CONTINUOUS_DISPOSAL_API_2 || event.type == CONTINUOUS_DISPOSAL_API_3) {
            Realm.getDefaultInstance().beginTransaction();

            int type = -1;

            if (event.type == CONTINUOUS_DISPOSAL_API_1) {
                type = 0;
            }
            if (event.type == CONTINUOUS_DISPOSAL_API_2) {
                type = 1;
            }
            if (event.type == CONTINUOUS_DISPOSAL_API_3) {
                type = 2;
            }
            Realm.getDefaultInstance().where(BorrowListItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("type", type).findAll().deleteAllFromRealm();

            ArrayList<BorrowListItem> arrayList = ((ArrayList<BorrowListItem>) event.getResponse());

            Log.i("arrayList", "arrayList " + arrayList.size() + " type " + type);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            for (int i = 0; i < arrayList.size(); i++) {
                Log.i("data", "data " + arrayList.get(i).getBorrowno());

                try {
                    arrayList.get(i).setValidDateObj(format.parse(arrayList.get(i).getValidDate()));
                    Date expected = arrayList.get(i).getValidDateObj();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();
                    c.setTime(expected);
                    c.add(Calendar.DATE, 1);  // number of days to add
                    expected = (c.getTime());  // dt is now the new date
                    arrayList.get(i).setValidDateObj(expected);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Log.i("approvaldate", "approvaldate" + arrayList.get(i).getApprovalDate());
                    arrayList.get(i).setApprovalDateObj(new SimpleDateFormat("dd/MM/yyyy").parse(arrayList.get(i).getApprovalDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                arrayList.get(i).setType(type);
                arrayList.get(i).setCompanyid(companyId);
                arrayList.get(i).setUserid(serverId);
                arrayList.get(i).setPk(companyId + serverId + "" + type + arrayList.get(i).getBorrowno());

                arrayList.get(i).setTimeString((long)i);

                Log.i("data", "data " + arrayList.get(i).getTimeString());
                Realm.getDefaultInstance().insertOrUpdate(arrayList.get(i));
            }

            Realm.getDefaultInstance().commitTransaction();

            Realm.getDefaultInstance().refresh();
            setupListView(getData());

        }
    }

    public List<BorrowListItem> getAppliedList() {
        List<BorrowListItem> lists = Realm.getDefaultInstance().copyFromRealm(
                Realm.getDefaultInstance().where(BorrowListItem.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)
                        .equalTo("type", 0)
                        .greaterThanOrEqualTo("validDateObj", new Date())
                        .sort("timeString")
                        .findAll());

        Log.i("getAppliedList", "getAppliedList " + lists.size());

        return lists;
    }

    public List<BorrowListItem> getApprovedList() {
        List<BorrowListItem> lists = Realm.getDefaultInstance().copyFromRealm(
                Realm.getDefaultInstance().where(BorrowListItem.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)
                        .equalTo("type", 1)
                        .greaterThanOrEqualTo("validDateObj", new Date())
                        .lessThanOrEqualTo("approvalDateObj", new Date())
                        .sort("timeString")
                        .findAll());
        return lists;
    }

    public List<BorrowListItem> getRejectedList() {
        List<BorrowListItem> lists = Realm.getDefaultInstance().copyFromRealm(
                Realm.getDefaultInstance().where(BorrowListItem.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", serverId)
                        .equalTo("type", 2)
                        .greaterThanOrEqualTo("validDateObj", new Date())
                        .lessThanOrEqualTo("approvalDateObj", new Date())
                        .sort("timeString")
                        .findAll());
        return lists;
    }

    public List<BorrowListItem> getData() {
        try {
            if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 0) {
                return getAppliedList();
            }

            if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 1) {
                return getApprovedList();
            }

            if (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() == 2) {
                return getRejectedList();
            }
        } catch (Exception e) {

        }
        return null;
    }


    public List<BorrowListItem> filterArrayList(List<BorrowListItem> assetResponse) {

        return assetResponse;
    }
}