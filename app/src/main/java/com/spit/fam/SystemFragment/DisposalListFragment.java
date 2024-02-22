package com.spit.fam.SystemFragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Entity.BorrowListUser;
import com.spit.fam.Entity.SPEntityP2.BriefBorrowedList;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.BorrowListAdapter;
import com.spit.fam.WebService.Callback.GetAssetListCallback;
import com.spit.fam.WebService.Callback.GetBorrowListAssetCallback;
import com.spit.fam.WebService.Callback.GetBorrowListCallBack;
import com.spit.fam.WebService.Callback.GetBriefBorrowedAssetCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DisposalListFragment extends BaseFragment {
    private List<BorrowList> assetResponse;
    private View noResult;
    private ListView listView;

    private BorrowListAdapter assetListAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.disposal_list_fragment, null);

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

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.disposal_list).toUpperCase());

        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                POSITION = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                BorrowListFragment.POSITION = -1;

                if(LoginFragment.SP_API) {
                     if (((MainActivity)getActivity()).isURLReachable()) {
                        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                        String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
                         int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

                        Log.i("callingAPI", "callingAPI " + position);
                        RetrofitClient.getSPGetWebService().disposalList(companyId, userid,      (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition()) ).enqueue(new GetBriefBorrowedAssetCallback());;
                    } else {

                         Handler handler = new Handler();
                         handler.postDelayed(new Runnable() {
                             @Override
                             public void run() {

                                 int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                                 Log.i("callingCache", "callingCache " + position);

                                 if(position == 0) {
                                     EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, new ArrayList<BorrowList>())));
                                 }
                                 if(position == 1) {
                                     EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, new ArrayList<BorrowList>())));
                                 }
                                 if(position == 2) {
                                     EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, new ArrayList<BorrowList>())));
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


        if(position2 == 0) {
            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, new ArrayList<BorrowList>())));
        }
        if(position2 == 1) {
            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, new ArrayList<BorrowList>())));
        }
        if(position2 == 2) {
            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, new ArrayList<BorrowList>())));
        }

        if (((MainActivity)getActivity()).isURLReachable()) {
            if (LoginFragment.SP_API) {
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");
                int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

                Log.i("callingAPI", "callingAPI " + position);
                RetrofitClient.getSPGetWebService().disposalList(companyId, userid,      (((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition()) ).enqueue(new GetBriefBorrowedAssetCallback());;
            } else {
                User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
                RetrofitClient.getService().getAssetList(user.getUser_group().getId()).enqueue(new GetAssetListCallback());;
            }
        } else {
            if (LoginFragment.SP_API) {
            } else {
                ArrayList<BorrowList> borrowLists = Hawk.get(InternalStorage.OFFLINE_CACHE.DISPOSAL_LIST, new ArrayList<>());
                EventBus.getDefault().post(new CallbackResponseEvent(borrowLists));
            }
        }
    }


    public void handleNoResult(List<BorrowList> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            noResult.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    public void setupListView(List<BorrowList> assetResponse) {

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

    public List<BorrowList> convertBriefBorrowListArrayToBorrowListArray(ArrayList<BriefBorrowedList> briefBorrowedLists) {
        ArrayList<BorrowList> borrowLists = new ArrayList<>();

        if(briefBorrowedLists == null)
            return borrowLists;

        for(int i = 0; i < briefBorrowedLists.size(); i++) {
            borrowLists.add(convertBriefBorrowListToBorrowList(briefBorrowedLists.get(i)));
        }


        Log.i("onRes", "onRes " + borrowLists.size());
        return borrowLists;
    }

    public BorrowList convertBriefBorrowListToBorrowList(BriefBorrowedList briefBorrowedList) {
        BorrowList borrowList = new BorrowList();
        borrowList.setDisposalNo(briefBorrowedList.getDisposalNo());
        borrowList.setName(briefBorrowedList.getName());
        borrowList.setCreated_at(briefBorrowedList.getApplyDate());
        borrowList.setApproved_date(briefBorrowedList.getApprovalDate());
        borrowList.setApprovedString(briefBorrowedList.getApproved());
        borrowList.setValid_date(briefBorrowedList.getValidDate());
        BorrowListUser borrowListUser = new BorrowListUser();
        //borrowListUser.setUsername(briefBorrowedList.getApprovedby());
        borrowList.setApprovedby(briefBorrowedList.getApprovedby());
        borrowList.setBorrowed(briefBorrowedList.getDisposed() + "");
        borrowList.setTotal(briefBorrowedList.getTotal() + "");

        return borrowList;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("CallbackResponseEvent", "CallbackResponseEvent " + event);
        swipeRefreshLayout.setRefreshing(false);

        this.assetResponse = (List<BorrowList>) event.getResponse();
        if(event.getResponse() instanceof BorrowListAssets) {
            Log.i("hihi", "hihi BorrowListAssets");
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + ((BorrowListAssets)event.getResponse()).getBorrowno()  , event.getResponse());

        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BriefBorrowedList.class ) {
            Log.i("CallbackResponseEvent", "CallbackResponseEvent " + event.getResponse());

            //CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(convertBriefBorrowListArrayToBorrowListArray(((ArrayList<BriefBorrowedList>)event.getResponse())));
            //EventBus.getDefault().post(callbackResponseEvent);
            int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

            if(position == 0) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_1, (((ArrayList<BriefBorrowedList>) event.getResponse())));
            }

            if(position == 1) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_2, (((ArrayList<BriefBorrowedList>) event.getResponse())));
            }

            if(position == 2) {
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_3, (((ArrayList<BriefBorrowedList>) event.getResponse())));
            }

            if(((MainActivity)getActivity()).isURLReachable()) {
                ArrayList<BriefBorrowedList> arrayList = (((ArrayList<BriefBorrowedList>) event.getResponse()));
                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
                for(int i = 0; i < arrayList.size(); i++) {

                    SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        Date date = parser.parse(arrayList.get(i).getValidDate());

                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = new Date();
                        Date todayWithZeroTime = formatter.parse(formatter.format(today));


                        Date dateWithZeroTime = formatter.parse(formatter.format(date));

                        if(todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                            Log.i("disposal date", "disposal valid true");
                            if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ + arrayList.get(i).getDisposalNo(),null) == null) {
                                RetrofitClient.getSPGetWebService().disposalListAssets(companyId, userid, arrayList.get(i).getDisposalNo()).enqueue(new GetBorrowListAssetCallback());
                            }
                        } else {
                            Log.i("disposal date", "disposal valid false");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                 }
            }


            setupListView(convertBriefBorrowListArrayToBorrowListArray(((ArrayList<BriefBorrowedList>)event.getResponse())));
        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Asset.class ) {
            User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
            Hawk.put(InternalStorage.Application.ASSET, event.getResponse());
            RetrofitClient.getService().getDisposalLists(user.getId()).enqueue(new GetBorrowListCallBack());

        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BorrowList.class ) {
            Hawk.put(InternalStorage.OFFLINE_CACHE.DISPOSAL_LIST, event.getResponse());
            setupListView(getData());
        } else {
            handleNoResult(null);
        }
    }

    public List<BorrowList> getAppliedList() {
        ArrayList<BorrowList> lists = new ArrayList<>();

        try {
            if (assetResponse != null)
                for (int i = 0; i < assetResponse.size(); i++) {
                    if (assetResponse.get(i).getDisposal_status().getName().equals("Applied")) {
                        lists.add(assetResponse.get(i));
                    }
                }
        } catch (Exception e) {

        }
        return lists;
    }

    public List<BorrowList> getApprovedList() {
        ArrayList<BorrowList> lists = new ArrayList<>();
        try {
            if (assetResponse != null)
                for (int i = 0; i < assetResponse.size(); i++) {
                    if (assetResponse.get(i).getDisposal_status().getName().equals("Approved")) {
                        lists.add(assetResponse.get(i));
                    }
                }
        } catch (Exception e) {

        }
        return lists;
    }

    public List<BorrowList> getRejectedList() {
        ArrayList<BorrowList> lists = new ArrayList<>();

        try {

            if (assetResponse != null)
                for (int i = 0; i < assetResponse.size(); i++) {
                    if (assetResponse.get(i).getDisposal_status().getName().equals("Rejected")) {
                        lists.add(assetResponse.get(i));
                    }
                }
        } catch (Exception e) {

        }
        return lists;
    }

    public List<BorrowList> getData() {
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


    public List<BorrowList> filterArrayList(List<BorrowList> assetResponse) {

        List<BorrowList> filterResult = new ArrayList<>();

        for(int i = 0; i < assetResponse.size(); i++) {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date = parser.parse(assetResponse.get(i).getValid_date());

                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date today = new Date();
                Date todayWithZeroTime = formatter.parse(formatter.format(today));


                Date dateWithZeroTime = formatter.parse(formatter.format(date));

                if(todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                    Log.i("return", "return false");
                    filterResult.add(assetResponse.get(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return filterResult;
    }
}
