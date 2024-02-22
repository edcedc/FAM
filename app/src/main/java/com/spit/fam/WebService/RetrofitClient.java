package com.spit.fam.WebService;

import android.util.Log;

import com.spit.fam.InternalStorage;
import com.spit.fam.UnsafeOkHttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.hawk.Hawk;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Retrofit apiretrofit = null;
    private static Retrofit spgetretrofit = null;

    private static StrapiService service = null;
    private static APIService apiService = null;
    public static com.spit.fam.WebService.SPGetWebService SPGetWebService = null;

    public static String STATIC_BASE = "http://34.123.213.205:1337";
    public static String BASE = "http://34.123.213.205:1337/";

    private static String BASE_80 = "http://34.82.136.182/";
    private static String SP_GET_WEB_SERVICE = "http://icloud.securepro.com.hk/AMSWebService_Template/MobileWebService.asmx/";

    public static Retrofit getClient() {
        if (retrofit==null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(1);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(700, TimeUnit.SECONDS)
                    .readTimeout(700, TimeUnit.SECONDS)
                    .writeTimeout(700, TimeUnit.SECONDS).dispatcher(dispatcher)
                    .build();


            retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static StrapiService getService() {
        if(service == null) {
            service = getClient().create(StrapiService.class);
        }

        return service;
    }

    public static Retrofit get80Client() {
        if (apiretrofit == null) {
            apiretrofit = new Retrofit.Builder()
                    .baseUrl(BASE_80)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return apiretrofit;
    }

    public static APIService getAPIService() {
        if(apiService == null) {
            apiService = get80Client().create(APIService.class);
        }

        return apiService;
    }


    public static String api;
    public static Retrofit getSPGetWebClient() {
       // if (spgetretrofit == null) {

        Log.i("hihi", "hihi" +
                (Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").startsWith("http://") ? Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") : "http://" + Hawk.get(InternalStorage.Setting.HOST_ADDRESS, ""))

                );
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        //if(api == null) {
            api = Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").endsWith("/") ? Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") : Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") + "/";
            Log.i("api", "api " + api + " " + (api.startsWith("http://") ? api : ("http://" + api))  +  (api.endsWith("/") ? "" : "/"));
        //}
        Log.i("api", "api " + api + " " + (api.startsWith("http://") ? api : ("http://" + api))  +  (api.endsWith("/") ? "" : "/"));

        Log.i("result" , "result " + api + " " + (api.startsWith("http://") ? api : ("http://" + api)) + (api.endsWith("/") ? "" : "/"));

        try {
            spgetretrofit = new Retrofit.Builder()
                    .baseUrl(api)
                    .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
      //  }
        return spgetretrofit;
    }

    public static com.spit.fam.WebService.SPGetWebService getSPGetWebService() {
        //if(SPGetWebService == null) {
            SPGetWebService = getSPGetWebClient().create(com.spit.fam.WebService.SPGetWebService.class);
        //}

        return SPGetWebService;
    }
}
