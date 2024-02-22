package com.spit.fam.SystemFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spit.fam.BuildConfig;
import com.spit.fam.Entity.AppsID;
import com.spit.fam.Entity.SPEntityP2.AssetsDetail;
import com.spit.fam.Entity.SPUser;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CancelSyncEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.NFCCardEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.RenewSystemFragment.RevampDownloadFragment;
import com.spit.fam.Response.APIResponse;
import com.spit.fam.Response.LoginResponse;
import com.spit.fam.Response.UserListResponse;
import com.spit.fam.WebService.Callback.SPWebServiceCallback;
import com.spit.fam.WebService.Callback.UserListCallback;
import com.spit.fam.WebService.RetrofitClient;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class LoginFragment extends BaseFragment {
    public static boolean SP_API = true;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        changeLocale(getActivity(), Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"));
    }

    public void changeLocale(Context context, String localeString) {
        Log.i("localeString", "localeString" + localeString);

        String languageToLoad  = localeString; // your language
        Locale locale = new Locale(languageToLoad);

        if(languageToLoad != null && languageToLoad.equals("zt")) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        UserListResponse userListResponse = new Gson().fromJson(FileUtils.readFromFile("userList"), UserListResponse.class);
        if(userListResponse != null && userListResponse.getData() != null && userListResponse.getData().size() > 0) {
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, userListResponse.getThiscalldate());
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, userListResponse.getData());
            Hawk.put(InternalStorage.OFFLINE_CACHE.USER_LIST, userListResponse.getData());
        }

        changeLocale(getActivity(), Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"));
        /*if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                requestPermissions(  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            }
        }*/
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_login, null);

        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        ((TextView)view.findViewById(R.id.app_version)).setText("V: " +sdf.format(buildDate).toString()+"");

        //((EditText)view.findViewById(R.id.account)).setText("admin");
        //((EditText)view.findViewById(R.id.password)).setText("123");

        companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
        api = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");

        if(companyId.length() == 0) {
           // Hawk.put(InternalStorage.Setting.COMPANY_ID, "icloud.securepro.com.hk");
        }

        if(api.length() == 0) {
            //Hawk.put(InternalStorage.Setting.COMPANY_ID, "gs1");
        }
        view.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(true);
            }
        });

        view.findViewById(R.id.sync_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });

        view.findViewById(R.id.sync_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });

        view.findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting();
            }
        });

        view.findViewById(R.id.setting_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting();
            }
        });

        view.findViewById(R.id.wrapper_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.wrapper).setVisibility(View.VISIBLE);
                view.findViewById(R.id.wrapper_2).setVisibility(View.GONE);
                MainActivity.hideKeyboard(getActivity());
                view.findViewById(R.id.version_text_space).setVisibility(View.VISIBLE);

            }
        });

        view.findViewById(R.id.space_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.wrapper).setVisibility(View.VISIBLE);
                view.findViewById(R.id.wrapper_2).setVisibility(View.GONE);
                MainActivity.hideKeyboard(getActivity());
                view.findViewById(R.id.version_text_space).setVisibility(View.VISIBLE);

            }
        });

        ((EditText)view.findViewById(R.id.account)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                view.findViewById(R.id.wrapper).setVisibility(View.GONE);
                view.findViewById(R.id.wrapper_2).setVisibility(View.VISIBLE);
                ((EditText)view.findViewById(R.id.account)).requestFocus();
                view.findViewById(R.id.version_text_space).setVisibility(View.GONE);

                return false;
            }
        });

        ((EditText)view.findViewById(R.id.password)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                view.findViewById(R.id.wrapper).setVisibility(View.GONE);
                view.findViewById(R.id.wrapper_2).setVisibility(View.VISIBLE);
                view.findViewById(R.id.version_text_space).setVisibility(View.GONE);

                ((EditText)view.findViewById(R.id.password)).requestFocus();

                return false;
            }
        });

        ((EditText)view.findViewById(R.id.account)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.hasFocus()) {
                    view.findViewById(R.id.wrapper).setVisibility(View.GONE);
                    view.findViewById(R.id.wrapper_2).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.version_text_space).setVisibility(View.GONE);
                    ((EditText)view.findViewById(R.id.account)).requestFocus();


                } else if (!((EditText)view.findViewById(R.id.password)).hasFocus()){
                    hideKeyboard(getActivity(), (EditText)view.findViewById(R.id.account) );
                }
            }
        });

        ((EditText)view.findViewById(R.id.password)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.hasFocus()) {
                    view.findViewById(R.id.wrapper).setVisibility(View.GONE);
                    view.findViewById(R.id.wrapper_2).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.version_text_space).setVisibility(View.GONE);

                } else if(!((EditText)view.findViewById(R.id.account)).hasFocus()) {
                    hideKeyboard(getActivity(), (EditText)view.findViewById(R.id.account) );
                }
            }
        });


        view.findViewById(R.id.show_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.show_password).setVisibility(View.GONE);
                view.findViewById(R.id.hide_password).setVisibility(View.VISIBLE);
                ((EditText)view.findViewById(R.id.password)).setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ((EditText)view.findViewById(R.id.password)).setSelection(((EditText)view.findViewById(R.id.password)).getText().length());
            }
        });


        view.findViewById(R.id.hide_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.show_password).setVisibility(View.VISIBLE);
                view.findViewById(R.id.hide_password).setVisibility(View.GONE);
                ((EditText)view.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ((EditText)view.findViewById(R.id.password)).setSelection(((EditText)view.findViewById(R.id.password)).getText().length());
            }
        });

        /*.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.wrapper).setVisibility(View.GONE);
            }
        });
        */

        //String username = Hawk.get(InternalStorage.Login.USER_ID, "");//((EditText) view.findViewById(R.id.account)).getText().toString() );
        //String password = Hawk.get(InternalStorage.Login.PASSWORD, "");//((EditText) view.findViewById(R.id.password)).getText().toString() );


        return view;
    }

    String companyId = "";
    String api = "";
    public void onResume() {
        super.onResume();

        companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
        api = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "");

        ((ImageView)view.findViewById(R.id.sync_2)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_popup_sync));
        ((ImageView)view.findViewById(R.id.sync_1)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_popup_sync));

        //http://34.123.213.205:1337
        //etrofitClient.BASE = "http://" + Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "34.123.213.205:1337") + "/";

        //Hawk.put(InternalStorage.Login.USER_ID);

        Log.i("yoyo", "yoyoy " + Hawk.get(InternalStorage.Login.USER) + " " +  Hawk.get(InternalStorage.Login.USER_ID, "")  + " " + Hawk.get(InternalStorage.Login.PASSWORD, ""));

        if(Hawk.get(InternalStorage.Login.USER) != null || (Hawk.get(InternalStorage.Login.USER_ID, "").length() > 0 && Hawk.get(InternalStorage.Login.PASSWORD, "").length() > 0 )) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Hawk.get(InternalStorage.Login.USER) != null)
                        LoginFragment.SP_API = false;
                    else
                        LoginFragment.SP_API = true;

                    if(((MainActivity)getActivity()) != null) {
                        boolean firstDownload = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DOWNLOAD_AFTER_LOGIN, false);

                        Log.i("hihi", "hihi " +  Hawk.get(InternalStorage.Setting.COMPANY_ID, "")  + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""));

                        InternalStorage.resetStaticPath();

                        if(((MainActivity)getActivity()).isURLReachable()) {
                            RevampDownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = true;
                            RevampDownloadFragment.DOWNLOAD_ON_BACK_PRESS = false;
                            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_DOWNLOAD_AFTER_LOGIN, true);
                            Log.i("DownloadFragment", "DownloadFragment case 1");
                            ((MainActivity) getActivity()).changeFragment(new RevampDownloadFragment());
                        } else {
                            RevampDownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = true;

                            Log.i("SearchListFragment", "SearchListFragment case 1");
                            ((MainActivity) getActivity()).changeFragment(new SearchListFragment());


                            List<AssetsDetail> assets = Realm.getDefaultInstance().where(AssetsDetail.class)
                                    .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")).findAll();

                            if(assets.size() == 0) {
                                //Log.i("DownloadFragment", "DownloadFragment case 2");
                                //LandRegisteryDownloadFragment.writeData();
                                ((MainActivity) getActivity()).changeFragment(new SearchListFragment());

                                //((MainActivity) getActivity()).changeFragment(new LandRegisteryDownloadFragment());
                            } else {
                                Log.i("SearchListFragment", "SearchListFragment case 2");
                                ((MainActivity) getActivity()).changeFragment(new SearchListFragment());
                            }

                            nfcCardNumber = null;
                        }
                    }
                }
            }, 10);
        }
    }

    public boolean login(boolean login) {
        Log.i("login", "login " + login);

        ((MainActivity)getActivity()).resetTitle();

        //((MainActivity)getActivity()).onResume();
        if(Hawk.get(InternalStorage.Setting.COMPANY_ID, "").length() == 0) {

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.companyid_empty))

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
            return false;
        }

        if(Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").length() == 0) {

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.host_address_empty))

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
            return false;
        }

        ArrayList<SPUser> spUsers = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER, new ArrayList<SPUser>());//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

        if(!((MainActivity)getActivity()).isNetworkAvailable() && spUsers.size() == 0) {
            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.no_internet) + spUsers.size() ));
            return false;
        }

        if(!login) {
            return true;
        }

        if(((EditText)view.findViewById(R.id.account)).getText().toString().length() == 0) {
            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.account_empty)));
            return false;
        }

        if(((EditText)view.findViewById(R.id.password)).getText().toString().length() == 0) {
            EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.password_empty)));
            return false;

        }

        Log.i("internet", "internet " + ((MainActivity)getActivity()).isNetworkAvailable());


        Log.i("SP_USER", "SP_USER " + spUsers.size());


        String userName = ((EditText) view.findViewById(R.id.account)).getText().toString();
        String password = ((EditText) view.findViewById(R.id.password)).getText().toString();

        boolean success = false;
        if(spUsers != null && spUsers.size() > 0 && !((MainActivity)getActivity()).isURLReachable() ) {
            for(int i = 0; i < spUsers.size(); i++) {
                if(spUsers.get(i).getLoginid().toLowerCase().equals(userName.toLowerCase())) {
                    try {
                        if (Encryption.decrypt(spUsers.get(i).getPassword()).equals(password)) {
                            Log.i("UserListResponse", "UserListResponse USER_ID " + spUsers.get(i).getUserid());

                            Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID,spUsers.get(i).getUserid() );

                            Log.i("account", "account " + spUsers.get(i).getUserid());

                            success = true;
                            ArrayList<APIResponse> apiResponses = new ArrayList<>();
                            APIResponse apiResponse = new APIResponse();
                            apiResponse.setStatus("2");

                            AppsID appsID = new AppsID();
                            appsID.setUserid(spUsers.get(i).getUserid());
                            FileUtils.writeFromFile("[" +new Gson().toJson(appsID) +"]", "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/Upload/", LandRegisteryDownloadFragment.appsID);

                            apiResponses.add(apiResponse);

                            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(apiResponses);


                            EventBus.getDefault().post(callbackResponseEvent);
                            break;
                        }
                    } catch (Exception e) {

                    }
                }
            }

            if(!success) {
                /*
                LandRegisteryDownloadFragment.parseDataToDatabase(FileUtils.readFromFile(LandRegisteryDownloadFragment.userList), LandRegisteryDownloadFragment.userList);
                for(int i = 0; i < spUsers.size(); i++) {
                    if(spUsers.get(i).getLoginid().toLowerCase().equals(userName.toLowerCase())) {
                        try {
                            if (Encryption.decrypt(spUsers.get(i).getPassword()).equals(password)) {
                                Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID,spUsers.get(i).getUserid() );

                                Log.i("account", "account " + spUsers.get(i).getUserid());

                                success = true;
                                ArrayList<APIResponse> apiResponses = new ArrayList<>();
                                APIResponse apiResponse = new APIResponse();
                                apiResponse.setStatus("2");

                                apiResponses.add(apiResponse);

                                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(apiResponses);


                                EventBus.getDefault().post(callbackResponseEvent);
                                break;
                            }
                        } catch (Exception e) {

                        }
                    }
                }*/
            }

            if(!success) {
                ArrayList<APIResponse> apiResponses = new ArrayList<>();
                APIResponse apiResponse = new APIResponse();
                apiResponse.setStatus("0");

                apiResponses.add(apiResponse);

                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(apiResponses);
                EventBus.getDefault().post(callbackResponseEvent);
            }

        } else {
            SP_API = true;
            RetrofitClient.api = api;
            RetrofitClient.getSPGetWebService().login(companyId, ((EditText) view.findViewById(R.id.account)).getText().toString(), ((EditText) view.findViewById(R.id.password)).getText().toString()).enqueue(new SPWebServiceCallback());
        }
        return true;
    }

    public void onPause(){
        super.onPause();
        FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/startSync.json");
    }

    public  String userList = "userList";
    Handler handler;
    Runnable runnable;

    public void sync() {
        FileUtils.writeFromFile("[]", "/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/", "startSync");
        try {
            ((ImageView)view.findViewById(R.id.sync_2)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
            ((ImageView)view.findViewById(R.id.sync_1)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));

            if (runnable != null) {


                ((ImageView)view.findViewById(R.id.sync_2)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_popup_sync));
                ((ImageView)view.findViewById(R.id.sync_1)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_popup_sync));
                if(runnable != null) {
                    if(handler != null) {
                        handler.removeCallbacks(runnable);
                        runnable = null;
                    }
                }
                FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/startSync.json");
                return;
            }

            handler = new Handler(Looper.getMainLooper());
            runnable = new Runnable() {
                @Override
                public void run() {

                    boolean isExist = FileUtils.isFileExist("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");
                    Log.i("isExist", "isExist " + isExist);

                    if (isExist) {
                        Log.i("userList", "userList start");

                        String rawData = FileUtils.readFromFile(userList);
                        UserListResponse userListResponse = new Gson().fromJson(rawData, UserListResponse.class);
                        if(userListResponse != null) {
                            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, userListResponse.getThiscalldate());
                            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, userListResponse.getData());
                            Hawk.put(InternalStorage.OFFLINE_CACHE.USER_LIST, userListResponse.getData());

                            for (int i = 0; i < userListResponse.getData().size(); i++) {
                                if (userListResponse.getData().get(i).getNfcCardNo().equals(Hawk.get(InternalStorage.Login.CARD_NUMBER) /*Hawk.get(InternalStorage.Login.USER_ID, "").toLowerCase()*/)) {
                                    Log.i("UserListResponse", "UserListResponse USER_ID " + userListResponse.getData().get(i).getUserid());

                                    Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID, userListResponse.getData().get(i).getUserid());
                                    Hawk.put(InternalStorage.Login.USER_ID, userListResponse.getData().get(i).getUserid());
                                    Hawk.put(InternalStorage.Login.PASSWORD, userListResponse.getData().get(i).getPassword());

                                    InternalStorage.resetStaticPath();
                                }
                            }
                        }
                        FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId + "/success.json");

                        if(rawData != null && rawData.length() > 0) {
                            if(userListResponse != null && userListResponse.getData().size() > 0)
                                Hawk.put(InternalStorage.OFFLINE_CACHE.USER_LIST, userListResponse);

                            EventBus.getDefault().post(new CancelSyncEvent());

                            if(runnable != null) {
                                if(handler != null) {
                                    handler.removeCallbacks(runnable);
                                    runnable = null;
                                }
                            }
                            FileUtils.deleteFileByRawPath("/sdcard/" + LandRegisteryDownloadFragment.userDefinedCompanyId +"/startSync.json");
                        }
                    } else {
                        handler.postDelayed(this, 500);
                    }
                }
            };
            handler.post(runnable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setting() {
        replaceFragment(new SettingFragment());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CancelSyncEvent event) {
        //Glide.with(MainActivity.mContext).load(android.R.drawable.ic_popup_sync).into((ImageView)view.findViewById(R.id.sync_2));
        //Glide.with(MainActivity.mContext).load(android.R.drawable.ic_popup_sync).into((ImageView)view.findViewById(R.id.sync_1));

        ((ImageView)view.findViewById(R.id.sync_2)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_popup_sync));
        ((ImageView)view.findViewById(R.id.sync_1)).setImageDrawable(getResources().getDrawable(android.R.drawable.ic_popup_sync));
    }

    private String nfcCardNumber = null;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NFCCardEvent event) {
        nfcCardNumber = event.getCardNo();


        if(login(false)) {
            if(((MainActivity)getActivity()).isNetworkAvailable()) {
                Log.i("hihi", "hihi case 1 "  + nfcCardNumber + " " + companyId);

                RetrofitClient.getSPGetWebService().userList(companyId, "", "").enqueue(new UserListCallback(DownloadFragment.NFC_USER_LIST_API));
            } else {
                Log.i("hihi", "hihi case 2");
                ArrayList<SPUser> spUsers = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER, new ArrayList<SPUser>());

                for(int i = 0; i < spUsers.size(); i++) {
                    //TODO
                    Log.i("getNfcCardNo", "getNfcCardNo " + spUsers.get(i).getNfcCardNo() + " " + nfcCardNumber);
                    if(spUsers.get(i).getNfcCardNo() != null && spUsers.get(i).getNfcCardNo().equals(nfcCardNumber)) {
                        try {
                            Hawk.put(InternalStorage.Login.CARD_NUMBER, nfcCardNumber);
                            ((EditText) view.findViewById(R.id.account)).setText(spUsers.get(i).getLoginid());
                            ((EditText) view.findViewById(R.id.password)).setText(Encryption.decrypt(spUsers.get(i).getPassword()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //exist = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                login(true);
                            }
                        });
                        break;
                    }
                }

                /*
                UserListResponse userListResponse = new UserListResponse();
                userListResponse.setData(Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER, new ArrayList<SPUser>()));

                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(userListResponse);
                callbackResponseEvent.setId ("" +DownloadFragment.NFC_USER_LIST_API);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.i("hihi", "hihi case 2.0 " + userListResponse.getData().size());

                        EventBus.getDefault().post(callbackResponseEvent);
                    }
                };

                new Handler().postDelayed(runnable, 100);*/
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("hihi", "hihi case 2.1 " + event.getId() + " " + DownloadFragment.NFC_USER_LIST_API + " " + (List<SPUser>)event.getResponse());


        if(event.getId() != null && event.getId().equals(DownloadFragment.NFC_USER_LIST_API +"")) {
            List<SPUser> spUsers = (List<SPUser>)event.getResponse();
            Log.i("hihi", "hihi case 3 " + spUsers.size());

            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, spUsers);//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

            boolean exist = false;

            for(int i = 0; i < spUsers.size(); i++) {
                //TODO
                Log.i("getNfcCardNo", "getNfcCardNo " + spUsers.get(i).getNfcCardNo() + " " + nfcCardNumber);
                if(spUsers.get(i).getNfcCardNo() != null && spUsers.get(i).getNfcCardNo().equals(nfcCardNumber)) {
                    try {
                        Hawk.put(InternalStorage.Login.CARD_NUMBER, nfcCardNumber);
                        ((EditText) view.findViewById(R.id.account)).setText(spUsers.get(i).getLoginid());
                        ((EditText) view.findViewById(R.id.password)).setText(Encryption.decrypt(spUsers.get(i).getPassword()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    exist = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            login(true);
                        }
                    });
                    break;
                }
            }

            if(!exist) {
                Toast.makeText(getActivity(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (event.getResponse().getClass() == LoginResponse.class) {
            Hawk.put(InternalStorage.Login.USER, (LoginResponse) event.getResponse());
            Hawk.put(InternalStorage.Login.PASSWORD, ((EditText) view.findViewById(R.id.password)).getText().toString());
            Log.i("SearchListFragment", "SearchListFragment case 1 ");
            //replaceFragment(new SearchListFragment());
        }

        if(event.getResponse() instanceof  List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0) instanceof SPUser) {
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER, (event.getResponse()));//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));
            Log.i("DownloadFragment", "DownloadFragment case 3");
            DownloadFragment.DOWNLOAD_ON_BACK_PRESS = false;
            if( ((MainActivity)MainActivity.mContext).isURLReachable() ){
                replaceFragment(new RevampDownloadFragment());
            } else {
                LandRegisteryDownloadFragment.writeData();
            }
            //((MainActivity) getActivity()).changeFragment(new LandRegisteryDownloadFragment());
        } else if (event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0) instanceof APIResponse) {
            if(((APIResponse) ((List)event.getResponse()).get(0)).getStatusString().equals("2")) {

                String original = Hawk.get(InternalStorage.Login.PREVIOUS_ID,"" );

                Hawk.put(InternalStorage.Login.USER_ID, ((EditText) view.findViewById(R.id.account)).getText().toString() );
                Hawk.put(InternalStorage.Login.PASSWORD, ((EditText) view.findViewById(R.id.password)).getText().toString() );
                DownloadFragment.DOWNLOAD_ALL_FROM_LOGIN = true;

                ArrayList<SPUser> spUsers = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_USER, new ArrayList<SPUser>());//((AssetsDetail) ((List) event.getResponse()).get(0)));//getAssetListFromBriefAssetList((List<BriefAsset>) event.getResponse()));

                for(int i = 0; i < spUsers.size(); i++) {
                    if(spUsers.get(i).getLoginid().toLowerCase().equals(((EditText) view.findViewById(R.id.account)).getText().toString().toLowerCase())) {
                        Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID, spUsers.get(i).getUserid());
                    }
                }

                InternalStorage.resetStaticPath();

                if( (!((MainActivity) MainActivity.mContext).isNetworkAvailable()) && spUsers.size() > 0 && original.equals(((EditText) view.findViewById(R.id.account)).getText().toString())) {
                    if(((MainActivity) getActivity()).isURLReachable()) {
                        ((MainActivity) getActivity()).changeFragment(new RevampDownloadFragment());
                    } else {

                        List<AssetsDetail> assets = Realm.getDefaultInstance().where(AssetsDetail.class)
                                .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")).findAll();

                        if(assets.size() == 0) {
                            Log.i("SearchListFragment", "SearchListFragment case 2222 " +  Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""));

                            //LandRegisteryDownloadFragment.writeData();
                            ((MainActivity) getActivity()).changeFragment(new SearchListFragment());

                            //((MainActivity) getActivity()).changeFragment(new LandRegisteryDownloadFragment());
                        } else {
                            Log.i("SearchListFragment", "SearchListFragment case 2");
                            ((MainActivity) getActivity()).changeFragment(new SearchListFragment());
                        }
                    }
                } else {
                    //((MainActivity) getActivity()).changeFragment(new DownloadFragment());
                    Log.i("callingAPI", "callingAPI userList");
                    if(((MainActivity) getActivity()).isNetworkAvailable()) {
                        RetrofitClient.getSPGetWebService().userList(companyId, ((EditText) view.findViewById(R.id.account)).getText().toString(), "").enqueue(new UserListCallback());
                    } else {
                        //Log.i("SearchListFragment", "SearchListFragment case 3");
                        //((MainActivity) getActivity()).changeFragment(new SearchListFragment());


                        List<AssetsDetail> assets = Realm.getDefaultInstance().where(AssetsDetail.class)
                                .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "")).findAll();

                        if(assets.size() == 0) {
                            ((MainActivity) getActivity()).changeFragment(new SearchListFragment());

                            //LandRegisteryDownloadFragment.writeData();
                            Log.i("SearchListFragment", "SearchListFragment case 22221 " + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""));

                            //((MainActivity) getActivity()).changeFragment(new LandRegisteryDownloadFragment());
                        } else {
                            Log.i("SearchListFragment", "SearchListFragment case 3");
                            ((MainActivity) getActivity()).changeFragment(new SearchListFragment());
                        }
                    }
                }
                Log.i("SearchListFragment", "SearchListFragment case 2 ");

                //replaceFragment(new SearchListFragment());
            } else {
                if(((APIResponse) ((List)event.getResponse()).get(0)).getStatusString().equals("1")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.incorrect_credential))

                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.app_name))
                            .setMessage(getString(R.string.username_not_exist))

                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            }
        }
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
