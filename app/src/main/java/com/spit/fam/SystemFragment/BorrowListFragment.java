package com.spit.fam.SystemFragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.spit.fam.Entity.BorrowList;
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

public class BorrowListFragment extends BaseFragment {
    private List<BorrowList> assetResponse;
    private View noResult;
    private ListView listView;
    private BorrowListAdapter assetListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.borrow_list_fragment, null);


        ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(POSITION).select();

        if(LoginFragment.SP_API) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

                    if(position == 0) {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, new ArrayList<BorrowList>())));
                    }
                    if(position == 1) {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, new ArrayList<BorrowList>())));
                    }
                    if(position == 2) {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, new ArrayList<BorrowList>())));
                    }
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, 100);

        } else {
            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.BORROW, new ArrayList<>())));
        }
        if (((MainActivity)getActivity()).isURLReachable()) {
            //RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_1));
            Log.i("borrowList api", "borrowList api");
            int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

            if(position == 0) {
                RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_1));
            }

            if(position == 1) {
                RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));
            }

            if(position == 2) {
                RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_3));
            }
        } else {
        }

        listView = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (((MainActivity)getActivity()).isURLReachable()) {
                    //RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_1));
                    int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                    Log.i("borrowList api", "borrowList api " + position);

                    if(position == 0) {
                        RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_1));
                    }

                    if(position == 1) {
                        RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));
                    }

                    if(position == 2) {
                        RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_3));
                    }
                } else {
                    if(LoginFragment.SP_API) {
                        int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

                        if(position == 0) {
                            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, new ArrayList<BorrowList>())));
                        }
                        if(position == 1) {
                            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, new ArrayList<BorrowList>())));
                        }
                        if(position == 2) {
                            EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, new ArrayList<BorrowList>())));
                        }
                    } else {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.BORROW, new ArrayList<>())));
                    }
                }
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.borrow_list).toUpperCase());

        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                POSITION = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                DisposalListFragment.POSITION = -1;

                if(LoginFragment.SP_API) {
                    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                    String userid = Hawk.get(InternalStorage.Login.USER_ID, "");


                    int position2 = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

                    if(position2 == 0) {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, new ArrayList<BorrowList>() )));//convertBriefBorrowListArrayToBorrowListArray(((ArrayList<BriefBorrowedList>) event.getResponse())));
                    } else if(position2 == 1) {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, new ArrayList<BorrowList>() )));//convertBriefBorrowListArrayToBorrowListArray(((ArrayList<BriefBorrowedList>) event.getResponse())));
                    } else if(position2 == 2) {
                        EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_3,new ArrayList<BorrowList>()  )));//convertBriefBorrowListArrayToBorrowListArray(((ArrayList<BriefBorrowedList>) event.getResponse())));
                    }


                    if(((MainActivity)getActivity()).isURLReachable()) {
                        int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
                        listView.setVisibility(View.GONE);

                        if(position == 0) {
                            Log.i("borrowList api", "borrowList api 0 ");

                            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 0).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_1));
                        } else if (position == 1) {
                            Log.i("borrowList api", "borrowList api 1 ");

                            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 1).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_2));
                        } else if (position == 2) {
                            Log.i("borrowList api", "borrowList api 2 ");

                            RetrofitClient.getSPGetWebService().borrowList(companyId, serverId, 2).enqueue(new GetBriefBorrowedAssetCallback(BORROW_API_3));
                        }
                        //RetrofitClient.getSPGetWebService().borrowList(companyId, userid, ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition()).enqueue(new GetBriefBorrowedAssetCallback());
                    } else {
                        //EventBus.getDefault().post(new CallbackResponseEvent(myData));
                    }
                } else {
                    //setupListView(getData());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);
        return view;
    }

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());
    public static int BORROW_API_1 = 7;
    public static int BORROW_API_2 = 8;
    public static int BORROW_API_3 = 9;

    public void onResume() {
        super.onResume();
    }

    public void callAPI(){
        if(LoginFragment.SP_API) {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
            Log.i("borrowList api", "borrowList api");

            RetrofitClient.getSPGetWebService().borrowList(companyId, userid, ((TabLayout)view.findViewById(R.id.tab_layout)).getSelectedTabPosition()).enqueue(new GetBriefBorrowedAssetCallback());
        } else {
            User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
            if(user != null)
                RetrofitClient.getService().getBorrowLists(user.getId()).enqueue(new GetBorrowListCallBack());
        }
    }


    public void handleNoResult(List<BorrowList> data) {
        Log.i("handleNoResult", "handleNoResult " + data);
        if(data == null || data.size() == 0) {
            listView.setVisibility(View.GONE);
            noResult.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            noResult.setVisibility(View.GONE);
        }
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

    public void setupListView(List<BorrowList> assetResponse) {
        //filterArrayList(filterArrayList(assetResponse));

        handleNoResult(filterArrayList(assetResponse));
        AssetListAdapter.WITH_EPC = true;

        if(assetListAdapter == null) {
            assetListAdapter = new BorrowListAdapter(filterArrayList(assetResponse), getActivity(), true,     ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition() ) ;
            listView.setAdapter(assetListAdapter);
        } else {
            assetListAdapter.setData(filterArrayList(assetResponse), getActivity(), ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition());
            assetListAdapter.notifyDataSetChanged();
            //listView.setAdapter(assetListAdapter);
        }


        //if(state != null) {
        //    listView.onRestoreInstanceState(state);
        //}
    }

    Parcelable state;
    public void onPause() {
        super.onPause();
        state = listView.onSaveInstanceState();
    }

    public static int POSITION;


    ArrayList<BriefBorrowedList> arrayList = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        swipeRefreshLayout.setRefreshing(false);
        if(event.getResponse() instanceof BorrowListAssets) {
            Log.i("hihi", "hihi BorrowListAssets");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + ((BorrowListAssets)event.getResponse()).getBorrowno()  , event.getResponse());
                }
            };

            Thread thread = new Thread(runnable);
            thread.run();

        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BriefBorrowedList.class ) {

            POSITION = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();
            DisposalListFragment.POSITION = -1;

            int position = POSITION;

            Log.i("CallbackResponseEvent", "CallbackResponseEvent " + position);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(position == 0) {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_1, (((ArrayList<BriefBorrowedList>) event.getResponse())));
                    } else  if(position == 1) {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_2, (((ArrayList<BriefBorrowedList>) event.getResponse())));
                    }else if(position == 2) {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.SP_BORROW_3, (((ArrayList<BriefBorrowedList>) event.getResponse())));
                    }

                    if(((MainActivity)getActivity()).isURLReachable()) {
                        ArrayList<BriefBorrowedList> arrayList = (((ArrayList<BriefBorrowedList>) event.getResponse()));

                        String userid = Hawk.get(InternalStorage.Login.USER_ID, "");
                        for(int i = 0; i < arrayList.size(); i++) {

                        //if(arrayList.size() > 0) {
                            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
                            try {

                                Date date = parser.parse(arrayList.get(arrayList.size() - 1).getValidDate());

                                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                Date today = new Date();
                                Date todayWithZeroTime = formatter.parse(formatter.format(today));
                                Date dateWithZeroTime = formatter.parse(formatter.format(date));

                                if (todayWithZeroTime.equals(dateWithZeroTime) || todayWithZeroTime.before(dateWithZeroTime)) {
                                    Log.i("return", "return false");
                                    if (Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + arrayList.get(arrayList.size() - 1).getBorrowNo(), null) == null) {
                                        RetrofitClient.getSPGetWebService().borrowListAssets(companyId, userid, arrayList.get(arrayList.size() - 1).getBorrowNo()).enqueue(new GetBorrowListAssetCallback());
                                        arrayList.remove(arrayList.size() - 1);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                       // }
                            //if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ + arrayList.get(i).getBorrowNo()  , null) == null)
                            //    RetrofitClient.getSPGetWebService().borrowListAssets(companyId, userid, arrayList.get(i).getBorrowNo()).enqueue(new GetBorrowListAssetCallback());
                        }
                    }
                }
            };

            Thread thread = new Thread(runnable);
            thread.run();


            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(convertBriefBorrowListArrayToBorrowListArray(((ArrayList<BriefBorrowedList>)event.getResponse())));
            EventBus.getDefault().post(callbackResponseEvent);

        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == BorrowList.class ) {
            this.assetResponse = (List<BorrowList>) event.getResponse();
            Log.i("handleNoResult", "handleNoResult assetResponse " + assetResponse.size());

            //Hawk.put(InternalStorage.OFFLINE_CACHE.BORROW, event.getResponse());
            setupListView(getData());

        } else {
            handleNoResult(null);
        }
    }

    public BorrowList convertBriefBorrowListToBorrowList(BriefBorrowedList briefBorrowedList) {
        BorrowList borrowList = new BorrowList();
        borrowList.setBorrowno(briefBorrowedList.getBorrowNo());

        borrowList.setName(briefBorrowedList.getName());
        borrowList.setCreated_at(briefBorrowedList.getApplyDate());
        borrowList.setApproved_date(briefBorrowedList.getApprovalDate());
        borrowList.setValid_date(briefBorrowedList.getValidDate());
        borrowList.setApprovedby(briefBorrowedList.getApprovedby());
        borrowList.setBorrowed(briefBorrowedList.getBorrowed());
        borrowList.setTotal(briefBorrowedList.getTotal() + "");
        borrowList.setApprovedString(briefBorrowedList.getApproved());
        return borrowList;
    }

    public List<BorrowList> convertBriefBorrowListArrayToBorrowListArray(ArrayList<BriefBorrowedList> briefBorrowedLists) {
        ArrayList<BorrowList> borrowLists = new ArrayList<>();

        if(briefBorrowedLists == null)
            return borrowLists;

        Log.i("briefBorrowedLists", "briefBorrowedLists " + briefBorrowedLists.size());

        for(int i = 0; i < briefBorrowedLists.size(); i++) {
            borrowLists.add(convertBriefBorrowListToBorrowList(briefBorrowedLists.get(i)));
        }
        return borrowLists;
    }


    public List<BorrowList> getData() {
        return assetResponse;
    }
}
