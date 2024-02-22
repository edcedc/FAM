package com.spit.fam.SystemFragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.spit.fam.CustomTextWatcher;
import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPEntityP2.BriefAsset;
import com.spit.fam.Entity.SPEntityP3.SearchNoEpcItem;
import com.spit.fam.Entity.Status;
import com.spit.fam.Entity.User;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CurrentAssetsCountEvent;
import com.spit.fam.Event.CustomTextWatcherEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.SystemFragment.Adapter.AssetListAdapter;
import com.spit.fam.SystemFragment.Adapter.SearchListAdapter;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Case;
import io.realm.Realm;

public class SearchResultFragment extends BaseFragment {
    public static boolean WITH_EPC = false;

    private List<AssetsDetail> assetResponse;
    private SearchListAdapter assetListAdapter;

    private ListView listview;
    private TextView noResult;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static int offset;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        offset = 0;

        view = LayoutInflater.from(getActivity()).inflate(R.layout.search_list_fragment, null);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAPI();
            }
        });

        //view.findViewById(R.id.search_bar).setVisibility(View.GONE);

        view.findViewById(R.id.add).setVisibility(View.GONE);
        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        swipeRefreshLayout.setEnabled(false);

        listview = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
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
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result));
        ((EditText)view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li, vg, b);
        return view;
    }

    public void onResume(){
        super.onResume();
        callAPI();
    }

    public void callAPI(){
        User user = Hawk.get(InternalStorage.Login.USER, new LoginResponse()).getUser();
        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
        String userid =  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

        if(LoginFragment.SP_API) {
            if(WITH_EPC) {
                Log.i("EPC", "EPC case 1 " + SearchFormFragment.ASSET.getAssetno().trim() + " " + SearchFormFragment.ASSET.getName().trim() + " " + SearchFormFragment.ASSET.getBrand().trim() + " " + SearchFormFragment.ASSET.getFirstCat().trim() + " "  +SearchFormFragment.ASSET.getLastCat().trim() + " " +SearchFormFragment.ASSET.getFirstLocation().trim() + " " + SearchFormFragment.ASSET.getLastLocation().trim() );

                ExecutorService schTaskEx = Executors.newFixedThreadPool(100000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        /*DataBaseHandler db = new DataBaseHandler(getActivity());
                        List<Asset> assets = db.searchAssetWithEPC(SearchFormFragment.ASSET.getAssetno().trim(), SearchFormFragment.ASSET.getName().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastLocation().trim(), SearchFormFragment.ASSET.getBrand().trim(), SearchFormFragment.ASSET.getModel().trim(), SearchFormFragment.ASSET.getFirstCat().trim(), SearchFormFragment.ASSET.getLastCat().trim(), 0 + "");
                        EventBus.getDefault().post(new CallbackResponseEvent(assets));


                        int count = db.searchAssetWithEPCCount(SearchFormFragment.ASSET.getAssetno().trim(), SearchFormFragment.ASSET.getName().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastLocation().trim(), SearchFormFragment.ASSET.getBrand().trim(), SearchFormFragment.ASSET.getModel().trim(), SearchFormFragment.ASSET.getFirstCat().trim(), SearchFormFragment.ASSET.getLastCat().trim(), 0 + "");
                        EventBus.getDefault().post(new CurrentAssetsCountEvent(count));

                        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + count + ")");*/

                        List<AssetsDetail> assets =
                                Realm.getDefaultInstance().where(AssetsDetail.class)//.isNotNull("epc").isNotEmpty("epc")
                                        .equalTo("companyid", companyId).equalTo("userid", userid)

                                        .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                                        .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                                        .contains("brand", SearchFormFragment.ASSET.getBrand().trim(), Case.INSENSITIVE)
                                        .contains("model", SearchFormFragment.ASSET.getModel().trim(), Case.INSENSITIVE)

                                        .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                                        //.contains("category", SearchFormFragment.ASSET.getLastCat().trim(),Case.INSENSITIVE)

                                        .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                                        //.contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)
                                        .findAll();

                        EventBus.getDefault().post(new CallbackResponseEvent(assets));

                        EventBus.getDefault().post(new CurrentAssetsCountEvent(assets.size()));

                        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + assets.size() + ")");

                    }
                });
                return;
/*
                if(((MainActivity)getActivity()).isNetworkAvailable() ) {
                    Log.i("callingAPI", "callingAPI search @ SearchResultFragment " + userid + (companyId + " " + userid + " " + SearchFormFragment.ASSET.getAssetno() + " " + SearchFormFragment.ASSET.getName() + " " + "1" + " " + SearchFormFragment.ASSET.getBrand() + " "+  SearchFormFragment.ASSET.getModel() + " " + SearchFormFragment.ASSET.getFirstCat() + " " + SearchFormFragment.ASSET.getLastCat() + " " + SearchFormFragment.ASSET.getFirstLocation() + " " + SearchFormFragment.ASSET.getLastLocation()));
                    RetrofitClient.getSPGetWebService().search(companyId, userid, SearchFormFragment.ASSET.getAssetno().trim(), SearchFormFragment.ASSET.getName().trim(), "1", SearchFormFragment.ASSET.getBrand().trim(), SearchFormFragment.ASSET.getModel().trim(), SearchFormFragment.ASSET.getFirstCat().trim(), SearchFormFragment.ASSET.getLastCat().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastLocation().trim()).enqueue(new GetBriefAssetCallback());
                } else {
                    Log.i("localCache", "localCache search @ SearchResultFragment");

                    List<Asset> originalList = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_ASSET_LIST, new ArrayList<>());
                    List<Asset> dataList = new ArrayList<>();

                    for(int i = 0; i < originalList.size(); i++) {
                        boolean exist = true;

                        if(originalList.get(i).getAssetno().toLowerCase() != null && originalList.get(i).getAssetno().toLowerCase().contains(SearchFormFragment.ASSET.getAssetno().toLowerCase())) {
                            //exist = true;
                        } else {
                            exist = false;
                        }

                        Log.i("exist", "exist case 1 " + exist);

                        if(originalList.get(i).getNameForSearch() != null &&  originalList.get(i).getNameForSearch().toLowerCase().contains(SearchFormFragment.ASSET.getName().toLowerCase())) {
                            //exist = true;
                        } else {
                            exist = false;
                        }
                        Log.i("exist", "exist case 2 " + exist);

                        if(originalList.get(i).getBrandForSearch() != null && originalList.get(i).getBrandForSearch().toLowerCase().contains(SearchFormFragment.ASSET.getBrand().toLowerCase())) {
                            //exist = true;
                        } else {
                            exist = false;
                        }


                        if(originalList.get(i).getModelForSearch() != null &&  originalList.get(i).getModelForSearch().toLowerCase().contains(SearchFormFragment.ASSET.getModel().toLowerCase())) {
                            //exist = true;
                        } else {
                            exist = false;
                        }

                        String location = originalList.get(i).getLocationString();

                        String [] locationArray = null;
                        if(location.contains("/")) {
                            locationArray = location.split("/");
                        } else {
                            locationArray = new String[1];
                            locationArray[0] = location;
                        }


                        boolean locationExist = false;
                        if(SearchFormFragment.ASSET.getFirstLocation().length() > 0 && SearchFormFragment.ASSET.getLastLocation().length() > 0) {
                            if(location.contains(SearchFormFragment.ASSET.getFirstLocation()) && location.contains(SearchFormFragment.ASSET.getLastLocation())) {
                                locationExist = true;
                            }

                        } else if(SearchFormFragment.ASSET.getFirstLocation().length() > 0) {
                            if(locationArray[0].equals(SearchFormFragment.ASSET.getFirstLocation())) {
                                locationExist = true;
                            }
                        }

                        if(locationExist || (SearchFormFragment.ASSET.getFirstLocation().length() == 0 && SearchFormFragment.ASSET.getLastLocation().length() == 0)) {

                        } else {
                            exist = false;
                        }

                        Log.i("exist", "exist case 5 " + exist);

                        String category = originalList.get(i).getCategoryString();
                        Log.i("category", "category " + category);

                        String [] categoryArray = null;
                        if(category.contains("/")) {
                            categoryArray = category.split("/");
                        } else {
                            categoryArray = new String[1];
                            categoryArray[0] = category;
                        }

                        boolean categoryExist = false;

                        if(SearchFormFragment.ASSET.getFirstCat().length() > 0 && SearchFormFragment.ASSET.getLastCat().length() > 0) {
                            Log.i("categoryArray", "categoryArray " + categoryArray[0] + " " + SearchFormFragment.ASSET.getFirstCat() + " "  );

                            if(category.contains(SearchFormFragment.ASSET.getFirstCat()) && category.contains(SearchFormFragment.ASSET.getLastCat()) ) {
                                categoryExist = true;
                            }

                        } else if(SearchFormFragment.ASSET.getFirstCat().length() > 0) {
                            Log.i("categoryArray", "categoryArray " + categoryArray[0] + " " + SearchFormFragment.ASSET.getFirstCat() + " "  );

                            if(categoryArray[0].equals(SearchFormFragment.ASSET.getFirstCat())) {
                                categoryExist = true;
                            }
                        }

                        if(categoryExist || (SearchFormFragment.ASSET.getFirstCat().length() == 0 && SearchFormFragment.ASSET.getLastCat().length() == 0)) {

                        } else {
                            exist = false;
                        }

                        Log.i("exist", "exist case 6 " + exist);


                        if(exist) {
                            dataList.add(originalList.get(i));
                        }
                    }
                    EventBus.getDefault().post(new CallbackResponseEvent(dataList));
                }*/
            } else {
                Log.i("EPC", "EPC case 1 " + SearchFormFragment.ASSET.getAssetno().trim() + " " + SearchFormFragment.ASSET.getName().trim() + " " + SearchFormFragment.ASSET.getBrand().trim() + " " + SearchFormFragment.ASSET.getFirstCat().trim() + " "  +SearchFormFragment.ASSET.getLastCat().trim() + " " +SearchFormFragment.ASSET.getFirstLocation().trim() + " " + SearchFormFragment.ASSET.getLastLocation().trim() );

                ExecutorService schTaskEx = Executors.newFixedThreadPool(100000);
                schTaskEx.execute(new Runnable() {
                    @Override
                    public void run() {
                        /*DataBaseHandler db = new DataBaseHandler(getActivity());
                        List<Asset> assets = db.searchAssetWithEPC(SearchFormFragment.ASSET.getAssetno().trim(), SearchFormFragment.ASSET.getName().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastLocation().trim(), SearchFormFragment.ASSET.getBrand().trim(), SearchFormFragment.ASSET.getModel().trim(), SearchFormFragment.ASSET.getFirstCat().trim(), SearchFormFragment.ASSET.getLastCat().trim(), 0 + "");
                        EventBus.getDefault().post(new CallbackResponseEvent(assets));


                        int count = db.searchAssetWithEPCCount(SearchFormFragment.ASSET.getAssetno().trim(), SearchFormFragment.ASSET.getName().trim(), SearchFormFragment.ASSET.getFirstLocation().trim(), SearchFormFragment.ASSET.getLastLocation().trim(), SearchFormFragment.ASSET.getBrand().trim(), SearchFormFragment.ASSET.getModel().trim(), SearchFormFragment.ASSET.getFirstCat().trim(), SearchFormFragment.ASSET.getLastCat().trim(), 0 + "");
                        EventBus.getDefault().post(new CurrentAssetsCountEvent(count));

                        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + count + ")");*/

                        List<SearchNoEpcItem> data = (Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", userid)
                                .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                                .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                                .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                                //.contains("category", SearchFormFragment.ASSET.getLastCat().trim(), Case.INSENSITIVE)
                                .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                                //.contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)
                                .findAll());
                        List<AssetsDetail> assets = new ArrayList<>();

                        //    private String assetNo, name, model, brand, category, location, epc, statusid, companyid, userid;
                        for(int i = 0; i < data.size(); i++) {
                            SearchNoEpcItem searchNoEpcItem = data.get(i);

                            AssetsDetail assetsDetail = new AssetsDetail();
                            assetsDetail.setAssetNo(searchNoEpcItem.getAssetNo());
                            assetsDetail.setName(searchNoEpcItem.getName());
                            assetsDetail.setModel(searchNoEpcItem.getModel());
                            assetsDetail.setBrand(searchNoEpcItem.getBrand());
                            assetsDetail.setCategory(searchNoEpcItem.getCategory());
                            assetsDetail.setLocation(searchNoEpcItem.getLocation());
                            assetsDetail.setEpc(searchNoEpcItem.getEpc());
                            assetsDetail.setStatusid(searchNoEpcItem.getStatusid());

                            assets.add(assetsDetail);
                        }
                        EventBus.getDefault().post(new CallbackResponseEvent(assets));

                        EventBus.getDefault().post(new CurrentAssetsCountEvent(assets.size()));

                        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + assets.size() + ")");

                        /*
                        List<AssetsDetail> assets =
                                Realm.getDefaultInstance().where(AssetsDetail.class)//.isNotNull("epc").isNotEmpty("epc")
                                        .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                                        .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                                        .contains("brand", SearchFormFragment.ASSET.getBrand().trim(), Case.INSENSITIVE)
                                        .contains("model", SearchFormFragment.ASSET.getModel().trim(), Case.INSENSITIVE)

                                        .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                                        .contains("category", SearchFormFragment.ASSET.getLastCat().trim(),Case.INSENSITIVE)

                                        .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                                        .contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)
                                        .findAll();

                        */
                    }
                });
                return;
            }
        }

    }


    public void handleNoResult(List<Asset> data) {
        if(data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        Log.i("CustomTextWatcherEvent", "CustomTextWatcherEvent " + event.getTitle());

        if(WITH_EPC) {
            List<AssetsDetail> assets =
                    Realm.getDefaultInstance().where(AssetsDetail.class)//.isNotNull("epc").isNotEmpty("epc")
                            .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                            .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                            .contains("brand", SearchFormFragment.ASSET.getBrand().trim(), Case.INSENSITIVE)
                            .contains("model", SearchFormFragment.ASSET.getModel().trim(), Case.INSENSITIVE)

                            .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                           // .contains("category", SearchFormFragment.ASSET.getLastCat().trim(), Case.INSENSITIVE)

                            .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                            //.contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)

                            .beginGroup()
                            .contains("assetNo", event.getTitle().trim(), Case.INSENSITIVE).or()
                            .contains("name", event.getTitle().trim(), Case.INSENSITIVE).or()
                            .contains("brand", event.getTitle().trim(), Case.INSENSITIVE).or()
                            .contains("model", event.getTitle().trim(), Case.INSENSITIVE).or()
                            .contains("category", event.getTitle().trim(), Case.INSENSITIVE).or()
                            .contains("location", event.getTitle(), Case.INSENSITIVE).or()
                            .contains("lastassetno", event.getTitle(), Case.INSENSITIVE)
                            .endGroup()

                            .findAll();


            setupListView(assets);

            EventBus.getDefault().post(new CurrentAssetsCountEvent(assets.size()));

            ((TextView) view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + assets.size() + ")");
        } else {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            String userid =  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

            List<SearchNoEpcItem> data = (Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", userid)
                    .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                    .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                    .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                   // .contains("category", SearchFormFragment.ASSET.getLastCat().trim(), Case.INSENSITIVE)
                    .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                   // .contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)

                    .beginGroup()
                    .contains("assetNo", event.getTitle().trim(), Case.INSENSITIVE).or()
                    .contains("name", event.getTitle().trim(), Case.INSENSITIVE).or()
                    .contains("brand", event.getTitle().trim(), Case.INSENSITIVE).or()
                    .contains("model", event.getTitle().trim(), Case.INSENSITIVE).or()
                    .contains("category", event.getTitle().trim(), Case.INSENSITIVE).or()
                    .contains("location", event.getTitle(), Case.INSENSITIVE)
                    .endGroup()

                    .findAll());
            List<AssetsDetail> assets = new ArrayList<>();

            //    private String assetNo, name, model, brand, category, location, epc, statusid, companyid, userid;
            for(int i = 0; i < data.size(); i++) {
                SearchNoEpcItem searchNoEpcItem = data.get(i);

                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo(searchNoEpcItem.getAssetNo());
                assetsDetail.setName(searchNoEpcItem.getName());
                assetsDetail.setModel(searchNoEpcItem.getModel());
                assetsDetail.setBrand(searchNoEpcItem.getBrand());
                assetsDetail.setCategory(searchNoEpcItem.getCategory());
                assetsDetail.setLocation(searchNoEpcItem.getLocation());
                assetsDetail.setEpc(searchNoEpcItem.getEpc());
                assetsDetail.setStatusid(searchNoEpcItem.getStatusid());

                assets.add(assetsDetail);
            }
            //EventBus.getDefault().post(new CallbackResponseEvent(assets));

            //EventBus.getDefault().post(new CurrentAssetsCountEvent(assets.size()));
            setupListView(assets);

            ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + assets.size() + ")");

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        swipeRefreshLayout.setRefreshing(false);


        if(WITH_EPC) {
            setupListView(Realm.getDefaultInstance().where(AssetsDetail.class)//.isNotNull("epc").isNotEmpty("epc")
                    .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                    .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                    .contains("brand", SearchFormFragment.ASSET.getBrand().trim(), Case.INSENSITIVE)
                    .contains("model", SearchFormFragment.ASSET.getModel().trim(), Case.INSENSITIVE)
                    .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                    //.contains("category", SearchFormFragment.ASSET.getLastCat().trim(), Case.INSENSITIVE)
                    .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                   // .contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)

                    //.contains("category", filterText)

                    //.contains("location", filterText)

                    .findAll());
        } else {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            String userid =  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

            List<SearchNoEpcItem> data = (Realm.getDefaultInstance().where(SearchNoEpcItem.class).equalTo("companyid", companyId).equalTo("userid", userid)
                    .contains("assetNo", SearchFormFragment.ASSET.getAssetno().trim(), Case.INSENSITIVE)
                    .contains("name", SearchFormFragment.ASSET.getName().trim(), Case.INSENSITIVE)
                    .contains("category", SearchFormFragment.ASSET.getFirstCat().trim(), Case.INSENSITIVE)
                    //.contains("category", SearchFormFragment.ASSET.getLastCat().trim(), Case.INSENSITIVE)
                    .contains("location", SearchFormFragment.ASSET.getFirstLocation().trim(), Case.INSENSITIVE)
                   // .contains("location", SearchFormFragment.ASSET.getLastLocation().trim(), Case.INSENSITIVE)
                    .findAll());
            List<AssetsDetail> assets = new ArrayList<>();

            //    private String assetNo, name, model, brand, category, location, epc, statusid, companyid, userid;
            for(int i = 0; i < data.size(); i++) {
                SearchNoEpcItem searchNoEpcItem = data.get(i);

                AssetsDetail assetsDetail = new AssetsDetail();
                assetsDetail.setAssetNo(searchNoEpcItem.getAssetNo());
                assetsDetail.setName(searchNoEpcItem.getName());
                assetsDetail.setModel(searchNoEpcItem.getModel());
                assetsDetail.setBrand(searchNoEpcItem.getBrand());
                assetsDetail.setCategory(searchNoEpcItem.getCategory());
                assetsDetail.setLocation(searchNoEpcItem.getLocation());
                assetsDetail.setEpc(searchNoEpcItem.getEpc());
                assetsDetail.setStatusid(searchNoEpcItem.getStatusid());

                assets.add(assetsDetail);
            }
            setupListView(assets);

            Log.i("datadata", "datadata " + data.size() + " " + assets.size());
            //((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search_result) + " (" + assets.size() + ")");
        }
    }

    public void setupListView(List<AssetsDetail> assetResponse) {
        swipeRefreshLayout.setRefreshing(false);

        AssetListAdapter.WITH_EPC = true;

        this.assetResponse = assetResponse;
        //handleNoResult(assetResponse);

        assetListAdapter = new SearchListAdapter(assetResponse, MainActivity.mContext);
        assetListAdapter.hasMore = true;
        listview.setAdapter(assetListAdapter);
        assetListAdapter.setData(assetResponse, getActivity());
        assetListAdapter.notifyDataSetChanged();

        Log.i("data", "data " + assetResponse.size());

        //ventBus.getDefault().post(new CurrentAssetsCountEvent(getFilterList(((EditText)view.findViewById(R.id.edittext)).getText().toString()).size()));
    }

    Parcelable state;

    @Override
    public void onPause() {
        state = listview.onSaveInstanceState();
        super.onPause();
    }

    public Asset convertBriefAssetToAsset(BriefAsset briefAsset) {
        Asset asset = new Asset();
        asset.setAssetno(briefAsset.getAssetNo());
        asset.setName(briefAsset.getName());
        asset.setBrand(briefAsset.getBrand());
        asset.setModel(briefAsset.getModel());
        asset.setEPC(briefAsset.getEpc());

        Status status = new Status();
        status.id  = Integer.parseInt(briefAsset.getStatusid());
        asset.setStatus(status);

        ArrayList<Category> categoryArrayList = new ArrayList<>();

        if(briefAsset.getCategorys() != null) {
            for (int i = 0; i < briefAsset.getCategorys().size(); i++) {
                Category category = new Category();
                category.setName(briefAsset.getCategorys().get(i));
                categoryArrayList.add(category);
            }
        }
        asset.setCategories(categoryArrayList);


        ArrayList<Location> locationArrayList = new ArrayList<>();

        if(briefAsset.getLocations() != null) {
            for (int i = 0; i < briefAsset.getLocations().size(); i++) {
                Location location = new Location();
                location.setName(briefAsset.getLocations().get(i));
                locationArrayList.add(location);
            }
        }
        asset.setLocations(locationArrayList);
        return asset;
    }

    public ArrayList<Asset> getAssetListFromBriefAssetList(List<BriefAsset> briefAssets) {
        ArrayList<Asset> assetArrayList = new ArrayList<>();
        if(briefAssets == null) {
            return assetArrayList;
        }

        for(int i = 0; i < briefAssets.size(); i++) {
            assetArrayList.add(convertBriefAssetToAsset(briefAssets.get(i)));
        }

        return assetArrayList;
    }
}
