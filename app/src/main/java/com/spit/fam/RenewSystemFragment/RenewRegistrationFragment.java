package com.spit.fam.RenewSystemFragment;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.CaptureActivityPortrait;
import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.OfflineMode.ChangeEpc;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP3.SearchNoEpcItem;
import com.spit.fam.Event.BarcodeScanEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.BaseFragment;
import com.spit.fam.SystemFragment.SearchFormFragment;
import com.spit.fam.WebService.P2Callback.SearchNoEpcCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class RenewRegistrationFragment extends BaseFragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    public static int CONTINUOUS_SEARCH_NO_EPC = 130;

    private ListView listview;
    private ListAdapter listAdapter;
    public  int tabPosition = 0;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.registration_fragment, null);

        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFormFragment searchFormFragment = new SearchFormFragment();
                searchFormFragment.WITH_EPC = false;
                replaceFragment(searchFormFragment);
            }
        });

        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(getActivity())
                        .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        .setPrompt("")
                        .setCameraId(0)
                        .setBeepEnabled(true)
                        .setBarcodeImageEnabled(true)
                        .setCaptureActivity(CaptureActivityPortrait.class)
                        .initiateScan();
            }
        });

        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        noResult = view.findViewById(R.id.no_result);

        ((EditText) view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        listview = (ListView) view.findViewById(R.id.listview);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(((MainActivity)getActivity()).isURLReachable()) {
                    //Log.i("callingAPI", "callingAPI @ RegistrationFragment");
                    //String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                    RetrofitClient.getSPGetWebService().newSearchnoepc(companyId).enqueue(new SearchNoEpcCallback(CONTINUOUS_SEARCH_NO_EPC));

                } else {
                    Log.i("localCache", "localCache @ RegistrationFragment");
                    //Log.i("size", "size " + Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()).size());
                    //EventBus.getDefault().post(new CallbackResponseEvent((Hawk.get(InternalStorage.OFFLINE_CACHE.LOCAL_REGISTRATION, new ArrayList<>()))) );
                }
            }
        });

        ((TabLayout)view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();

                if(tabPosition == 0) {

                    handleNoResult(getData());

                    if(listAdapter == null) {
                        listAdapter = new ListAdapter(getData());
                        listview.setAdapter(listAdapter);
                    } else {
                        listAdapter.setData(getData());
                        listAdapter.notifyDataSetChanged();
                    }
                 } else {

                    handleNoResult(getData());

                    if(listAdapter == null) {
                        listAdapter = new ListAdapter(getData());
                        listview.setAdapter(listAdapter);
                    } else {
                        listAdapter.setData(getData());
                        listAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        handleNoResult(getData());

        if(listAdapter == null) {
            listAdapter = new ListAdapter(getData());
            listview.setAdapter(listAdapter);
        } else {
            listAdapter.setData(getData());
            listAdapter.notifyDataSetChanged();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(((MainActivity)getActivity()).isURLReachable()) {
                    RetrofitClient.getSPGetWebService().newSearchnoepc(companyId).enqueue(new SearchNoEpcCallback(CONTINUOUS_SEARCH_NO_EPC));
                }
            }
        },400);
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);

        if(listAdapter == null) {
            listAdapter = new ListAdapter(getData());
            listview.setAdapter(listAdapter);
        } else {
            listAdapter.setData(getData());
            listAdapter.notifyDataSetChanged();
        }

        return view;
    }

    private View noResult;

    public List<SearchNoEpcItem> getData(){
        List<SearchNoEpcItem> result = new ArrayList<>();

        List<SearchNoEpcItem> data = (Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", serverId)


                .beginGroup()
                .contains("assetNo", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                .or()
                .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                .or()
                .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                .or()
                .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                .or()
                .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                .endGroup()
                .findAll());

        for(int i = 0; i < data.size(); i++) {
            //List<AssetsDetail> assetsDetail = Realm.getDefaultInstance().where(AssetsDetail.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("assetNo",data.get(i).getAssetNo()).findAll();
            RealmResults<ChangeEpc> changeEpcs = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("assetno",data.get(i).getAssetNo()).equalTo("userid", serverId).equalTo("companyid", companyId).findAll();
            //assetno
            if(changeEpcs.size() > 0 && tabPosition == 0 ) {
                result.add(data.get(i));
            }  else if(tabPosition == 1 && (changeEpcs.size() == 0 )) {
                result.add(data.get(i));
            }
        }

        return result;
    }
    public void handleNoResult(List<SearchNoEpcItem> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }

        if(data.size() > 0)
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration).toUpperCase() + " (" + data.size() + ")");
        else
            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.registration).toUpperCase());


        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {

        handleNoResult(getData());

        if(listAdapter == null) {
            listAdapter = new ListAdapter(getData());
            listview.setAdapter(listAdapter);
        } else {
            listAdapter.setData(getData());
            listAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        swipeRefreshLayout.setRefreshing(false);

        List<SearchNoEpcItem> res = ((List<SearchNoEpcItem>) event.getResponse());

        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().deleteAllFromRealm();

        for (int i = 0; i < res.size(); i++) {
            res.get(i).setCompanyid(companyId);
            res.get(i).setUserid(serverId);
            Realm.getDefaultInstance().insertOrUpdate(res.get(i));
        }
        Realm.getDefaultInstance().commitTransaction();

        handleNoResult(getData());

        if(listAdapter == null) {
            listAdapter = new ListAdapter(getData());
            listview.setAdapter(listAdapter);
        } else {
            listAdapter.setData(getData());
            listAdapter.notifyDataSetChanged();
        }
    }

    public class ListAdapter extends BaseAdapter {
        List<SearchNoEpcItem> data = new ArrayList<>();

        public ListAdapter(List<SearchNoEpcItem> data) {
            this.data = data;
        }

        public void setData(List<SearchNoEpcItem> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public SearchNoEpcItem getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return data.hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null)
                view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.search_listview_cell, viewGroup, false);

            SearchNoEpcItem asset = getItem(i);

            ((TextView)view.findViewById(R.id.search_cell_title)).setText(asset.getAssetNo() + " | " + asset.getName());
            ((TextView)view.findViewById(R.id.search_cell_brand_value)).setText(asset.getBrand());
            ((TextView)view.findViewById(R.id.search_cell_model_value)).setText(asset.getModel());
            ((TextView)view.findViewById(R.id.search_cell_category_value)).setText(asset.getCategory());
            ((TextView)view.findViewById(R.id.search_cell_location_value)).setText(asset.getLocation());
            ((TextView)view.findViewById(R.id.search_cell_epc_value)).setText(asset.getEpc());
            (((TextView)view.findViewById(R.id.search_cell_return_date_value))).setVisibility(View.GONE);

            RealmResults<ChangeEpc> changeEpcs = Realm.getDefaultInstance().where(ChangeEpc.class).equalTo("assetno",asset.getAssetNo()).equalTo("userid", serverId).equalTo("companyid", companyId).findAll();

            if(changeEpcs.size() > 0) {
                ((LinearLayout)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.VISIBLE);
                ((TextView)view.findViewById(R.id.search_cell_new_epc_value)).setText(changeEpcs.get(0).getEpc());
            } else {
                ((LinearLayout)((TextView)view.findViewById(R.id.search_cell_new_epc_value)).getParent()).setVisibility(View.GONE);
            }
            //((ViewGroup)(view.findViewById(R.id.search_cell_generic)).getParent()).setVisibility(View.GONE);
            //((ViewGroup)(view.findViewById(R.id.search_cell_status_in_library)).getParent()).setVisibility(View.GONE);
           // ((ViewGroup)(view.findViewById(R.id.search_cell_status_in_borrowed)).getParent()).setVisibility(View.GONE);
            //((ViewGroup)(view.findViewById(R.id.search_cell_status_disposed)).getParent()).setVisibility(View.GONE);

            Log.i("position", "position " + view);


            (((view.findViewById(R.id.borrow_tick)))).setVisibility(View.GONE);
            (((view.findViewById(R.id.missing_take)))).setVisibility(View.GONE);
            ((view.findViewById(R.id.abnormal_take))).setVisibility(View.GONE);

            /*
            ((ViewGroup)view.findViewById(R.id.search_cell_title).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_brand_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_model_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_category_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_location_value).getParent()).setVisibility(View.GONE);
 */
            //}

            Log.i("data" ,"data " + getItem(i).getStatusid().equals("2"));
            if(getItem(i).getStatusid().equals("2")) {
                ((TextView) view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.VISIBLE);

                ((view.findViewById(R.id.search_cell_status_in_borrowed))).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_status_disposed))).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_generic))).setVisibility(View.GONE);

            } else if(getItem(i).getStatusid().equals("3") || getItem(i).getStatusid().equals("4")) {
                ((TextView) view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_status_in_borrowed))).setVisibility(View.VISIBLE);
                ((view.findViewById(R.id.search_cell_status_disposed))).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_generic))).setVisibility(View.GONE);
            } else if(getItem(i).getStatusid().equals("5") || getItem(i).getStatusid().equals("6") || getItem(i).getStatusid().equals("7") || getItem(i).getStatusid().equals("8") || getItem(i).getStatusid().equals("9999")) {

                ((TextView) view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_status_in_borrowed))).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_status_disposed))).setVisibility(View.VISIBLE);
                ((view.findViewById(R.id.search_cell_generic))).setVisibility(View.GONE);
            } else {
                ((TextView) view.findViewById(R.id.search_cell_status_in_library)).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_status_in_borrowed))).setVisibility(View.GONE);
                (((view.findViewById(R.id.search_cell_status_disposed)))).setVisibility(View.GONE);
                ((view.findViewById(R.id.search_cell_generic))).setVisibility(View.GONE);
            }

            ((ViewGroup)(view.findViewById(R.id.search_cell_return_date_value)).getParent()).setVisibility(View.GONE);

            final int tempI = i;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("setOnClickListener", "setOnClickListener ");

                    AssetsDetailWithTabFragment.ASSET_NO = (getItem(tempI).getAssetNo());

                        Log.i("replace99", "replace99");
                        ((MainActivity) getActivity()).replaceFragment(new AssetsDetailWithTabFragment());


                }
            });
            return view;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BarcodeScanEvent event) {
        boolean exist = false;
        List<SearchNoEpcItem> SearchNoEpcItem = Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", serverId)
                .beginGroup()
                .contains("assetNo", event.getBarcode())
                .or()
                .contains("epc", event.getBarcode())
                .endGroup()
                .findAll();

        if (SearchNoEpcItem.size() > 0) {
            List<AssetsDetail> assets = Realm.getDefaultInstance().where(AssetsDetail.class)
                    .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                    .beginGroup()
                    .contains("assetNo", event.getBarcode(), Case.INSENSITIVE)
                    .or()
                    .contains("epc", event.getBarcode(), Case.INSENSITIVE)
                    .endGroup()
                    .findAll();

            if (assets.size() > 0) {
                exist = true;
                AssetsDetailWithTabFragment.ASSET_NO = (assets.get(0).getAssetNo());

                if (assets.size() == 0) {

                } else {
                    Log.i("replace99", "replace99");
                    ((MainActivity) getActivity()).replaceFragment(new AssetsDetailWithTabFragment());
                }

            }
        }
        if(!exist) {
            Toast.makeText(getActivity(), getString(R.string.no_data),  Toast.LENGTH_LONG).show();
        }
    }
}
