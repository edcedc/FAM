package com.spit.fam.SystemFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.spit.fam.Entity.Asset;
import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Entity.SpinnerOnClickEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.LevelData;
import com.spit.fam.Response.ListingResponse;
import com.spit.fam.WebService.Callback.GetListingCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class SearchFormFragment extends BaseFragment {
    public boolean WITH_EPC = false;

    public static Asset ASSET;
    public static int RETURN_API = 10;

    List<Location> lcoationList;
    List<Category> categoryList;

    ArrayList<View> locationViewList = new ArrayList<>();
    ArrayList<View> categoryViewList = new ArrayList<>();

    ArrayList<Spinner> locationSpinnerList = new ArrayList<>();
    ArrayList<Spinner> categorySpinnerList = new ArrayList<>();


    ArrayList<Integer> selectedLocation = new ArrayList<>();
    ArrayList<Integer> selectedCategory = new ArrayList<>();

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        view = LayoutInflater.from(getActivity()).inflate(R.layout.search_form_fragment, null);

        if(LoginFragment.SP_API) {
            view.findViewById(R.id.sp_display).setVisibility(View.VISIBLE);
            view.findViewById(R.id.original_category).setVisibility(View.GONE);
            view.findViewById(R.id.sp_category).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sp_location).setVisibility(View.VISIBLE);
            view.findViewById(R.id.location_spinner).setVisibility(View.INVISIBLE);

        }

        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.search));

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateDrawerStatus();
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        (view.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        (view.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        (view.findViewById(R.id.search)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Asset asset = new Asset();
                asset.setAssetno(((EditText) view.findViewById(R.id.asset_no)).getText().toString());
                asset.setName(((EditText) view.findViewById(R.id.asset_name)).getText().toString());
                asset.setBrand(((EditText) view.findViewById(R.id.brand)).getText().toString());
                asset.setModel(((EditText) view.findViewById(R.id.model)).getText().toString());
                if(LoginFragment.SP_API) {
                    String firstLocation = "";
                    String lastLocation = "";

                    String firstCategory = "";
                    String lastCategory = "";

                    for(int i = 0; i < locationViewList.size(); i++) {
                        Spinner current = (Spinner) locationViewList.get(i);

                        if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                            //firstLocation = locationSpinnerList.get(i).getSelectedItem().toString();
                            if(true) {
                                firstLocation = current.getSelectedItem().toString();//.toString();
                            } else {
                                firstLocation = current.getSelectedItem().toString();//.toString();
                            }
                            break;
                        }
                    }

                    for(int i = locationViewList.size() - 1; i > 0; i--) {
                        Spinner current = (Spinner) locationViewList.get(i);

                        Log.i("lastLoc", "lastLoc " + current.getSelectedItem());

                        if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                            //lastLocation = locationSpinnerList.get(i).getSelectedItem().toString();
                            try {
                                if(true) {
                                    lastLocation = current.getSelectedItem().toString();//locationLevelData.get(i).get(locationSpinnerList.get(i).getSelectedItemPosition() - 1).getName();//.toString();
                                } else {
                                    lastLocation = current.getSelectedItem().toString();//locationLevelData.get(i).get(locationSpinnerList.get(i).getSelectedItemPosition() - 1).getRono();//.toString();
                                }
                            } catch (Exception e){

                            }
                            break;
                        }
                    }

                    String locationFilter = "";

                    for(int i = 0; i < locationViewList.size(); i++) {
                        Spinner current = (Spinner) locationViewList.get(i);

                        if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                            //firstCategory = categorySpinnerList.get(i).getSelectedItem().toString();
                            Log.i("categorydemo", "categorydemo " + current.getSelectedItem().toString());
                            locationFilter += current.getSelectedItem().toString() + "->";
                        } else if (current.getSelectedItem().toString().equals("-")) {
                            break;
                        }
                    }

                    if(locationFilter.length() > 0) {
                        locationFilter = locationFilter.substring(0, locationFilter.length() - 2);
                    }

                    Log.i("data", "data " + locationFilter);

                    selectedLocation.clear();
                    for(int i = 0; i < locationSpinnerList.size(); i++) {
                        selectedLocation.add(locationSpinnerList.get(i).getSelectedItemPosition());
                    }

                    //locationViewList
                    for(int i = 0; i < categoryViewList.size(); i++) {
                        Spinner current = (Spinner) categoryViewList.get(i);
                        Log.i("firstCategory", "firstCategory " + current.getSelectedItem().toString());

                        if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                            //firstCategory = categorySpinnerList.get(i).getSelectedItem().toString();
                            if(true) {
                                firstCategory = current.getSelectedItem().toString();//.toString();
                            } else {
                                firstCategory = current.getSelectedItem().toString();//.toString();
                            }
                            break;
                        }
                    }


                    for(int i = categoryViewList.size() - 1; i > 0; i--) {
                        Spinner current = (Spinner) categoryViewList.get(i);

                        if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                            //lastCategory = categorySpinnerList.get(i).getSelectedItem().toString();
                            try {
                                if(true) {
                                    lastCategory = current.getSelectedItem().toString();//.toString();
                                } else {
                                    lastCategory = current.getSelectedItem().toString();//.toString();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Log.i("lastCategory", "lastCategory " + firstCategory + " " + lastCategory);
                            //Log.i("lastCategory", "lastCategory " + lastCategory + " " + categorySpinnerList.size());
                            break;
                        }
                    }

                    String categoryFilter = "";

                    for(int i = 0; i < categoryViewList.size(); i++) {
                        Spinner current = (Spinner) categoryViewList.get(i);

                        if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                            //firstCategory = categorySpinnerList.get(i).getSelectedItem().toString();
                            Log.i("categorydemo", "categorydemo " + current.getSelectedItem().toString());
                            categoryFilter += current.getSelectedItem().toString() + "->";
                        } else if (current.getSelectedItem().toString().equals("-")) {
                            break;
                        }
                    }

                    if(categoryFilter.length() > 0) {
                        categoryFilter = categoryFilter.substring(0, categoryFilter.length() - 2);
                    }

                    Log.i("data", "data " + categoryFilter);

                    selectedCategory.clear();
                    for(int i = 0; i < categorySpinnerList.size(); i++) {
                        selectedCategory.add(categorySpinnerList.get(i).getSelectedItemPosition());
                    }

                    asset.setFirstCat(categoryFilter);
                    //asset.setLastCat(lastCategory);

                    asset.setFirstLocation(locationFilter);
                    //asset.setLastLocation(lastLocation);

                    Log.i("firstLoc", "firstLoc " + firstLocation + " " + lastLocation + " " + firstCategory + " " + lastCategory);

                    ASSET = asset;

                    SearchResultFragment searchResultFragment = new SearchResultFragment();
                    searchResultFragment.WITH_EPC = WITH_EPC;
                    ((MainActivity)getActivity()).hideKeyboard(getActivity());

                    replaceFragment(searchResultFragment);

                } else {
                    Category category = new Category();
                    category.setName(((Spinner) view.findViewById(R.id.category_spinner1)).getSelectedItem().toString());

                    Category category1 = new Category();
                    category1.setName(((Spinner) view.findViewById(R.id.category_spinner2)).getSelectedItem().toString());

                    Category category2 = new Category();
                    category2.setName(((Spinner) view.findViewById(R.id.category_spinner3)).getSelectedItem().toString());

                    ArrayList<Category> categories = new ArrayList<>();
                    categories.add(category1);
                    categories.add(category2);
                    categories.add(category);
                    asset.setCategories(categories);


                    Location location = new Location();
                    location.setName(((Spinner) view.findViewById(R.id.location_spinner)).getSelectedItem().toString());
                    ArrayList<Location> locationArrayList = new ArrayList<>();
                    locationArrayList.add(location);
                    asset.setLocations(locationArrayList);

                    ASSET = asset;

                    ((MainActivity)getActivity()).hideKeyboard(getActivity());

                    SearchResultFragment searchResultFragment = new SearchResultFragment();
                    searchResultFragment.WITH_EPC = WITH_EPC;

                    replaceFragment(searchResultFragment);
                }
            }
        });


        //ListingResponse listingResponse = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL);
/*
        if(listingResponse != null) {
            Log.i("listing case", "listing case 3 " + listingResponse.getCatSize() + " " + listingResponse.getLocSize());

            ListingResponse newListingResponse = new ListingResponse();
            newListingResponse.setCatSize(listingResponse.getCatSize());
            newListingResponse.setLocSize(listingResponse.getLocSize());

            handleLocCat(listingResponse.getCatSize(), listingResponse.getLocSize());
        }
        if(LoginFragment.SP_API) {
            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            Log.i("listing case", "listing case 1 ");
            if( ((MainActivity)getActivity()).isNetworkAvailable() ) {
                Log.i("listing case", "listing case 2 ");

                RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
            } else if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL) != null) {
            }

            Log.i("listing case", "listing case 4 ");
        } else {
            setCategoryList(Hawk.get(InternalStorage.Search.CATEGORY, new ArrayList()));
            setLocationList(Hawk.get(InternalStorage.Search.LOCATION, new ArrayList()));

            RetrofitClient.getService().getLocationList().enqueue(new GetLocationListCallback());
            RetrofitClient.getService().getCategoryList().enqueue(new GetCategoryListCallback());
        }*/


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                if(((MainActivity)getActivity()).isURLReachable() ){
                    RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
                } else {
                    ArrayList<Asset> myAsset =  Hawk.get(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());
                    EventBus.getDefault().post(new CallbackResponseEvent(myAsset));

                    //ListingResponse listingResponse = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL);
                    ListingResponse listingResponse = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();

                    Log.i("listing case", "listing case 3 " + listingResponse.getCatSize() + " " + listingResponse.getLocSize() + " " + myAsset.size());

                    ListingResponse newListingResponse = new ListingResponse();
                    newListingResponse.setCatSize(listingResponse.getCatSize());
                    newListingResponse.setLocSize(listingResponse.getLocSize());

                    EventBus.getDefault().post(new CallbackResponseEvent(newListingResponse));
                }
            }
        }, 300);
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b){
        super.onCreateView(li, vg, b);


        return view;
    }

    public void onResume(){
        super.onResume();

        for(int i = 0; i < categorySpinnerList.size(); i++) {
            //selectedCategory.add(categorySpinnerList.get(i).getSelectedItemPosition());
            if(selectedCategory.size() > i)
                categorySpinnerList.get(i).setSelection(selectedCategory.get(i));
        }

        for(int i = 0; i < locationSpinnerList.size(); i++) {
            //selectedCategory.add(categorySpinnerList.get(i).getSelectedItemPosition());
            if(selectedLocation.size() > i)
                locationSpinnerList.get(i).setSelection(selectedLocation.get(i));
        }
    }

    public void setCategoryList(List<Category> categoryList) {
        List<String> location = new ArrayList<>();

        for(int i = 0; i < categoryList.size(); i++)
            location.add(categoryList.get(i).getName());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)view.findViewById(R.id.category_spinner1)).setAdapter(dataAdapter);
        ((Spinner)view.findViewById(R.id.category_spinner2)).setAdapter(dataAdapter);
        ((Spinner)view.findViewById(R.id.category_spinner3)).setAdapter(dataAdapter);
    }

    public void setSpinner(List<LevelData> categoryList, Spinner spinner, int layer, int type) {
        List<String> location = new ArrayList<>();
        location.add("-");

        for(int i = 0; i < categoryList.size(); i++) {
            if(categoryList.get(i).getName() != null)
                location.add(categoryList.get(i).getName());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(type == 0) {
                    for (int i = layer; i < categoryViewList.size(); i++) {
                        setSpinner(new ArrayList<>(), (Spinner)categoryViewList.get(i), i, type);
                    }
                } else if(type == 1) {
                    for (int i = layer; i < locationViewList.size(); i++) {
                        setSpinner(new ArrayList<>(), (Spinner)locationViewList.get(i), i, type);
                    }
                }

                if(position != 0) {
                    Log.i("type", "type " + layer + " " + type + " " + categoryList.size());
                    EventBus.getDefault().post(new SpinnerOnClickEvent(layer, type, categoryList.get(position - 1).getRono()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SpinnerOnClickEvent event) {

        List<LevelData> levelZero = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("fatherNo", event.getFatherno()).equalTo("level", event.getLayer() + 1).equalTo("type", event.getType()).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
        Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent " + levelZero.size() + " " + event.getLayer()  + " " + event.getType() + " " + event.getFatherno() +  " " + companyId);

        try {
            if(event.getType() == 1) {
                setSpinner(levelZero, ((Spinner) locationViewList.get(event.getLayer())), event.getLayer() + 1, 1);
            } else {
                setSpinner(levelZero, ((Spinner) categoryViewList.get(event.getLayer())), event.getLayer() + 1, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

        Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent" + event.getLayer()  + " " + event.getType() + " " + event.getFatherno() +  " " + companyId);

        Log.i("localCache", "localCache @ SearchFormFragment");

        Log.i("SP_LISTING_LEVEL_CACHE", "onMessageEvent SP_LISTING_LEVEL_CACHE " + (InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "") + "_" + event.getType() + "_" + (event.getLayer() + 1)));

        if(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1)) != null) {
            //EventBus.getDefault().post(new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1))));
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_" + event.getType() + "_" + (event.getLayer() + 1)));
            callbackResponseEvent.type = event.getType();
            callbackResponseEvent.level = (event.getLayer() + 1);
            callbackResponseEvent.setFatherno(event.getFatherno());

            EventBus.getDefault().post(callbackResponseEvent);
        }
        if(((MainActivity)getActivity()).isNetworkAvailable()) {
            Log.i("callingAPI", "callingAPI @ SearchFormFragment " + companyId + " " + event.getFatherno() + " " + event.getType() + " ");
            RetrofitClient.getSPGetWebService().listing(companyId, event.getFatherno(), event.getType() + "").enqueue(new GetLevelDataCallback(event.getFatherno(), event.getType(), event.getLayer() + 1));
        } else {
        }*/
    }

    public void setLocationList(List<Location> lcoationList) {
        /*
        List<String> location = new ArrayList<>();

        for(int i = 0; i < lcoationList.size(); i++)
            location.add(lcoationList.get(i).getName());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)view.findViewById(R.id.location_spinner)).setAdapter(dataAdapter);*/
    }

    private ArrayList<ArrayList<LevelData>> locationLevelData = new ArrayList<>();
    private ArrayList<ArrayList<LevelData>> categoryLevelData = new ArrayList<>();

    ArrayList<LevelData> pendingToAdds = new ArrayList<>();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("CallbackResponseEvent", "CallbackResponseEvent " );
        if (event.getResponse() instanceof ListingResponse) {
            if(((MainActivity)getActivity()).isURLReachable() ) {
                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findAll().deleteAllFromRealm();

                ListingResponse listingResponse = ((ListingResponse) event.getResponse());
                listingResponse.setPk(companyId + serverId + "SP_LISTING_LEVEL");
                Realm.getDefaultInstance().insertOrUpdate(listingResponse);
                Realm.getDefaultInstance().commitTransaction();
            }

            ListingResponse l = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();

            Log.i("Retrofit", "Retrofit ListingResponse " + l);

            int categorySize = ((ListingResponse) event.getResponse()).getCatSize();
            int locationSize = ((ListingResponse) event.getResponse()).getLocSize();

            /*
            if (categorySize > 0) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "0").enqueue(new GetLevelDataCallback("", 0, 1));
            } else if (locationSize > 0) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "0").enqueue(new GetLevelDataCallback("", 1, 1));
            } else {
                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).equalTo("isNewData", false).findAll().deleteAllFromRealm();
                Realm.getDefaultInstance().commitTransaction();
            }*/
            int count = Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().size();

            Log.i("RetrofitClient", "LevelData done " + count);

            ListingResponse l2 = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();
            categorySize = (l2).getCatSize();
            locationSize = (l2).getLocSize();

            ViewGroup spinnerRoot = (ViewGroup) view.findViewById(R.id.sp_category);
            spinnerRoot.removeAllViews();
            categoryViewList.clear();

            for(int i = 0; i < categorySize; i++) {
                Log.i("category", "category" + i);

                Spinner spinner = new Spinner(SearchFormFragment.this.getActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)convertDpToPixel(10);
                layoutParams.bottomMargin = (int)convertDpToPixel(10);

                spinner.setLayoutParams(layoutParams);

                spinnerRoot.addView(spinner);

                LinearLayout ll = new LinearLayout(getActivity());
                ll.setBackgroundColor(Color.parseColor("#C9CACA"));
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(1));
                layoutParams.topMargin = (int)convertDpToPixel(2);

                if(i + 1 < locationSize)
                    layoutParams.bottomMargin = (int)convertDpToPixel(2);

                ll.setLayoutParams(layoutParams);

                spinnerRoot.addView(ll);

                categoryViewList.add(spinner);
            }

            List<LevelData> levelZero = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("level", 1).equalTo("type", 0).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
            setSpinner(levelZero, ((Spinner)categoryViewList.get(0)), 1, 0);

            ViewGroup spinnerRoot_loc = (ViewGroup) view.findViewById(R.id.sp_location);
            spinnerRoot_loc.removeAllViews();
            locationViewList.clear();

            for(int i = 0; i < locationSize; i++) {
                Log.i("location", "location" + i);

                Spinner spinner = new Spinner(SearchFormFragment.this.getActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)convertDpToPixel(10);
                layoutParams.bottomMargin = (int)convertDpToPixel(10);

                spinner.setLayoutParams(layoutParams);

                spinnerRoot_loc.addView(spinner);

                LinearLayout ll = new LinearLayout(getActivity());
                ll.setBackgroundColor(Color.parseColor("#C9CACA"));
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(1));
                layoutParams.topMargin = (int)convertDpToPixel(2);

                if(i + 1 < locationSize)
                    layoutParams.bottomMargin = (int)convertDpToPixel(2);

                ll.setLayoutParams(layoutParams);

                spinnerRoot_loc.addView(ll);

                locationViewList.add(spinner);
            }

            List<LevelData> lz = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("level", 1).equalTo("type", 1).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
            setSpinner(lz, ((Spinner)locationViewList.get(0)), 1, 1);

        }

/*
        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == LevelData.class) {
            Log.i("leveldata", "leveldata " + event.getResponse());
            ArrayList<LevelData> levelData = (ArrayList<LevelData>)event.getResponse();
            int level = event.level;
            int row = (int)Math.floor((event.level - 1 )/ 3); // 5 / 3
            int pos = level ;
            // 1 2 3
            // 1 2 3

            if(event.type == 0) {
                for(int i = event.level; i < categorySpinnerList.size() - 1; i ++) { //1 2 3 4 5
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchFormFragment.this.getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinnerList.get(i).setAdapter(arrayAdapter);
                }

                try {
                    categoryLevelData.subList(event.level - 1, categoryLevelData.size()).clear();
                } catch (Exception e) {
                }
                categoryLevelData.add(levelData);

                Log.i("leveldata", "leveldata type case " + event.type + " " + event.level + " " + categoryLevelData.size());

                if(pos == 1) {
                    setSpinner(levelData, categoryViewList.get(row).findViewById(R.id.category_spinner1), event.level, event.type);
                } else if(pos == 2) {
                    setSpinner(levelData, categoryViewList.get(row).findViewById(R.id.category_spinner2), event.level, event.type);
                } else if(pos == 3) {
                    setSpinner(levelData, categoryViewList.get(row).findViewById(R.id.category_spinner3), event.level, event.type);
                }

                Log.i("SP_LISTING_LEVEL_CACHE", "CallbackResponseEvent SP_LISTING_LEVEL_CACHE " + InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "") + "_0_" + event.level);

                //Hawk.put(InternalStorage.OFFLINE_CACHE.SP_CATEGORY_CACHE, event.getResponse());
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "")  + "_0_" + event.level, event.getResponse());
            } else if (event.type == 1){
                Log.i("leveldata", "leveldata type case " + event.type + " " + pos);
                for(int i = event.level; i < locationViewList.size() - 1; i ++) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SearchFormFragment.this.getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>());
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    locationSpinnerList.get(i).setAdapter(arrayAdapter);
                }

                try {
                    locationLevelData.subList(event.level, locationLevelData.size()).clear();
                } catch (Exception e) {
                }
                locationLevelData.add( levelData);

                Log.i("pos", "pos " + pos + " " + event.level + " " + locationLevelData.size() + " " + row);

                if(pos == 1) {
                    setSpinner(levelData, locationViewList.get(row).findViewById(R.id.category_spinner1), event.level, event.type);
                } else if(pos == 2) {
                    setSpinner(levelData, locationViewList.get(row).findViewById(R.id.category_spinner2), event.level, event.type);
                } else if(pos == 3) {
                    setSpinner(levelData, locationViewList.get(row).findViewById(R.id.category_spinner3), event.level, event.type);
                }

                Log.i("SP_LISTING_LEVEL_CACHE", "CallbackResponseEvent SP_LISTING_LEVEL_CACHE " + InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "") + "_1_" + event.level);

                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "_" + (event.getFatherno() != null ? event.getFatherno() : "") + "_1_" + event.level, event.getResponse());
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LOCATION_CACHE, event.getResponse());
            }
        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Category.class) {
            categoryList = (List<Category>) event.getResponse();

            Hawk.put(InternalStorage.Search.CATEGORY, categoryList);
            setCategoryList(categoryList);
        } else if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Location.class) {
            lcoationList = (List<Location>) event.getResponse();

            Hawk.put(InternalStorage.Search.LOCATION, lcoationList);
            setLocationList(lcoationList);
        } else if(event.getResponse() instanceof ListingResponse) {
            Log.i("SP_LISTING_LEVEL", "SP_LISTING_LEVEL save " + event.getResponse());
            if(event.getResponse() != null)
                Hawk.put(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL, (ListingResponse)event.getResponse());

            int categorySize = ((ListingResponse)event.getResponse()).getCatSize();
            int locationSize = ((ListingResponse)event.getResponse()).getLocSize();

            handleLocCat(categorySize, locationSize);
        }*/
    }

    public  float convertDpToPixel(float dp){
        return dp * ((float) getActivity().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public void handleLocCat(int categorySize, int locationSize ) {
        /*
        locationViewList.clear();
        categoryViewList.clear();
        categorySpinnerList.clear();
        locationSpinnerList.clear();

        int categoryLevel = (int)Math.ceil(categorySize / 3f);
        int locationLevel = (int)Math.ceil(locationSize / 3f);

        LinearLayout category = view.findViewById(R.id.sp_category);
        LinearLayout location = view.findViewById(R.id.sp_location);

        Log.i("category", "category " + Math.ceil(categorySize / 3f) + " "+ categoryLevel + " " + locationLevel);
        category.removeAllViews();
        location.removeAllViews();

        for(int i = 0; i < categoryLevel; i++) {
            int count = categoryLevel * 3;
            int hiddenNeeded = 0;

            if(count > categorySize) {
                hiddenNeeded = count - categorySize;
            }

            View linearLayout = LayoutInflater.from(getActivity()).inflate(R.layout.category_row, null);
            category.addView(linearLayout);
            categoryViewList.add(linearLayout);

            Log.i("hiddenNeeded", "hiddenNeeded " + count + " " + categorySize + " " + hiddenNeeded);

            if(hiddenNeeded == 1) {
                categorySpinnerList.add(linearLayout.findViewById(R.id.category_spinner1));
                categorySpinnerList.add(linearLayout.findViewById(R.id.category_spinner2));
                linearLayout.findViewById(R.id.category_spinner3).setVisibility(View.GONE);
            } else if(hiddenNeeded == 2) {
                categorySpinnerList.add(linearLayout.findViewById(R.id.category_spinner1));
                linearLayout.findViewById(R.id.category_spinner2).setVisibility(View.GONE);
                linearLayout.findViewById(R.id.category_spinner3).setVisibility(View.GONE);
            } else {
                categorySpinnerList.add(linearLayout.findViewById(R.id.category_spinner1));
                categorySpinnerList.add(linearLayout.findViewById(R.id.category_spinner2));
                categorySpinnerList.add(linearLayout.findViewById(R.id.category_spinner3));
            }
        }
        String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");

        if (categorySize > 0) {
            Log.i("category", "category " + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__0_1"));

            if (Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__0_1") != null) {
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__0_1"));
                callbackResponseEvent.type = 0;
                callbackResponseEvent.level = 1;
                callbackResponseEvent.setFatherno("");
                callbackResponseEvent.setResponse((Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__0_1")));

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(callbackResponseEvent);
                    }
                };

                Handler handler = new Handler();

                handler.postDelayed(runnable, 300);

            }
        }

        if (locationSize > 0) {
            Log.i("location", "location " + Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1"));

            if (Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1") != null) {
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1"));
                callbackResponseEvent.type = 1;
                callbackResponseEvent.level = 1;
                callbackResponseEvent.setFatherno("");
                callbackResponseEvent.setResponse((Hawk.get(InternalStorage.OFFLINE_CACHE.SP_LISTING_LEVEL_CACHE + "__1_1")));

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(callbackResponseEvent);
                    }
                };

                Handler handler = new Handler();

                handler.postDelayed(runnable, 300);

            }
        }
        if( ((MainActivity)getActivity()).isNetworkAvailable()) {
            if (categorySize > 0) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "0").enqueue(new GetLevelDataCallback("",0, 1));
            }

            if (locationSize > 0) {
                RetrofitClient.getSPGetWebService().listing(companyId, "", "1").enqueue(new GetLevelDataCallback("",1, 1));
            }
        } else {
        }

        for(int i = 0; i < locationLevel; i++) {
            int count = locationLevel * 3;
            int hiddenNeeded = 0;

            if(count > locationSize) {
                hiddenNeeded = count - locationSize;
            }

            View linearLayout = LayoutInflater.from(getActivity()).inflate(R.layout.category_row, null);
            location.addView(linearLayout);
            locationViewList.add(linearLayout);

            //Log.i("hiddenNeeded", "hiddenNeeded " + count + " " + locationSize);
            if(hiddenNeeded == 1) {
                locationSpinnerList.add(linearLayout.findViewById(R.id.category_spinner1));
                locationSpinnerList.add(linearLayout.findViewById(R.id.category_spinner2));
                linearLayout.findViewById(R.id.category_spinner3).setVisibility(View.INVISIBLE);
            } else if(hiddenNeeded == 2) {
                locationSpinnerList.add(linearLayout.findViewById(R.id.category_spinner1));
                linearLayout.findViewById(R.id.category_spinner2).setVisibility(View.INVISIBLE);
                linearLayout.findViewById(R.id.category_spinner3).setVisibility(View.INVISIBLE);
            } else {
                locationSpinnerList.add(linearLayout.findViewById(R.id.category_spinner1));
                locationSpinnerList.add(linearLayout.findViewById(R.id.category_spinner2));
                locationSpinnerList.add(linearLayout.findViewById(R.id.category_spinner3));
            }
        }*/
    }
}
