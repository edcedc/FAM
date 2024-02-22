package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.BaseUtils;
import com.spit.fam.Entity.RenewEntity.RealmStockTakeListAsset;
import com.spit.fam.Entity.SPEntityP3.StocktakeList;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.Event.LoginDownloadProgressEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.DownloadFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.RenewStockTakeFragment;
import com.spit.fam.WebService.RetrofitClient;
import com.spit.fam.WebService.SPGetWebService;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockTakeListAdapter extends BaseAdapter {
    List<StocktakeList> stockTakeListResponse;
    Context context;

    public StockTakeListAdapter(List<StocktakeList> stockTakeListResponse, Context context) {
        this.context = context;
        this.stockTakeListResponse = stockTakeListResponse;
    }


    public void setData(List<StocktakeList> stockTakeListResponse, Context context) {
        this.context = context;
        this.stockTakeListResponse = stockTakeListResponse;
    }

    @Override
    public int getCount() {
        return stockTakeListResponse.size();
    }

    @Override
    public StocktakeList getItem(int position) {
        return stockTakeListResponse.get(position);
    }

    @Override
    public long getItemId(int position) {
        return stockTakeListResponse.get(position).hashCode();
    }

    public void setTextView (TextView tv, String data) {
        if((data == null || data.length() == 0 ) && tv != null) {
            ((ViewGroup)tv.getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)tv.getParent()).setVisibility(View.VISIBLE);
            tv.setText(data);
        }
    }
    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""); //response.body().getData().get(i).getUserid());

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.stock_take_cell_item, viewGroup, false);


        StocktakeList stockTakeList = getItem(i);

        ((TextView)view.findViewById(R.id.cell_title)).setText( (stockTakeList.getStocktakeno() != null ? stockTakeList.getStocktakeno() : stockTakeList.getStocktakeno()) + " | " + stockTakeList.getName());

        setTextView ((TextView)view.findViewById(R.id.start_date_value), stockTakeList.getStartDate());
        setTextView((TextView)view.findViewById(R.id.end_date_value), (stockTakeList.getEndDate()));

        ((ViewGroup)((TextView) view.findViewById(R.id.progress_value)).getParent()).setVisibility(View.VISIBLE);

        /*int count = Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", serverId).equalTo("companyId", companyId).equalTo("stocktakeno", stockTakeList.getStocktakeno())
                .beginGroup()
                .equalTo("tempStockTake", true).or()
                .equalTo("statusid", 2)
                .endGroup()
                .findAll().size();
        if(Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", serverId).equalTo("companyId", companyId).equalTo("stocktakeno", stockTakeList.getStocktakeno())
                .equalTo("tempStockTake", true)
                .findAll().size() > 0){
            setTextView((TextView) view.findViewById(R.id.progress_value), count + " / " + stockTakeList.getTotal());
        } else {*/
            setTextView((TextView) view.findViewById(R.id.progress_value), stockTakeList.getProgress() + " / " + stockTakeList.getTotal());
        //}
        setTextView((TextView) view.findViewById(R.id.remark_value), stockTakeList.getRemarks() );

         ((TextView)view.findViewById(R.id.last_update_value)).setText(  stockTakeList.getLastUpdate());

        (view.findViewById(R.id.cell_status_new)).setVisibility(View.GONE);
        (view.findViewById(R.id.cell_status_processing)).setVisibility(View.GONE);
        (view.findViewById(R.id.cell_status_expired)).setVisibility(View.GONE);

            //(view.findViewById(R.id.search_cell_generic))

        /*
        if(stockTakeList.getStartDateObject() != null && stockTakeList.getEndDateObject() != null) {
            if (isSameDate(stockTakeList.getStartDateObject(), new Date())) {
                //Log.i("case 1", "case 1");
                (view.findViewById(R.id.cell_status_new)).setVisibility(View.VISIBLE);
            } else if ((stockTakeList.getStartDateObject().before(new Date())) && new Date().before(stockTakeList.getEndDateObject())) {
                //Log.i("case 2", "case 2");
                (view.findViewById(R.id.cell_status_processing)).setVisibility(View.VISIBLE);
            } else if (new Date().after(stockTakeList.getEndDateObject())) {
                //Log.i("case 3", "case 3");
                (view.findViewById(R.id.cell_status_expired)).setVisibility(View.VISIBLE);
            }
        }
         */

        if(Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", serverId).equalTo("companyId", companyId).equalTo("stocktakeno", stockTakeList.getStocktakeno()).findAll().size() == 0) {
            (view.findViewById(R.id.cell_status_new)).setVisibility(View.GONE);
        } else {
            (view.findViewById(R.id.cell_status_new)).setVisibility(View.VISIBLE);

            ((TextView)view.findViewById(R.id.cell_status_new)).setText(context.getString(R.string.download_success));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginFragment.SP_API) {
                AssetsDetailWithTabFragment.ASSET_NO = "";

                Log.i("data", "data " + getItem(i) + " " + getItem(i).getStocktakeno() + " " );//+ StockTakeListItemFragment.stockTakeList.getOrderNo());


              //  Log.i("stockTakeList", "stockTakeList adapter setOnClickListener" + stockTakeList.getStocktakeno() + " " + Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).findAll().size());

                if(Realm.getDefaultInstance().where(RealmStockTakeListAsset.class).equalTo("userId", serverId).equalTo("companyId", companyId).equalTo("stocktakeno", stockTakeList.getStocktakeno()).findAll().size() == 0) {
                    if(((MainActivity)context).isURLReachable()) {
                        LoginDownloadProgressEvent event = new LoginDownloadProgressEvent(0.0f);
                        EventBus.getDefault().post(event);


                        String apiRoot =  (Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").endsWith("/")) ? Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "") : Hawk.get(InternalStorage.Setting.HOST_ADDRESS, "").toString() + "/";
                        //RetrofitClient.api;//(if (RetrofitClient.api.startsWith("http://")) RetrofitClient.api else "http://" + RetrofitClient.api) +
                        if (apiRoot.endsWith("/")) {

                        } else {
                            apiRoot = apiRoot + "/";
                        }

                        String path = apiRoot + "MobileWebService.asmx/stockTakeListAsset?userid=" + serverId + "&companyid=" + companyId + "&orderno=" + stockTakeList.getStocktakeno();
                        BaseUtils.serverCount = stockTakeList.getTotal();
                        BaseUtils.count = 0;

                        downloadFileRetrofit(path,stockTakeList.getStocktakeno() );

                        //APIUtils.download2(stockTakeList.getStocktakeno());
                    } else {
                        DialogEvent dialogEvent = new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.no_data));
                        EventBus.getDefault().post(dialogEvent);
                    }
                } else {
                    RenewStockTakeFragment.toolbar = stockTakeList.getName();
                    RenewStockTakeFragment.stocktakeno = stockTakeList.getStocktakeno();

                    ((MainActivity)context).replaceFragment(new RenewStockTakeFragment());
                }
            }
        }});

        return view;
    }

    public boolean isSameDate(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        if(date1 == null || date2 == null)
            return  false;

        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return sameDay;
    }



    public static void downloadFileRetrofit(String fileUrl, String orderno) {
        SPGetWebService downloadService = RetrofitClient.getSPGetWebClient().create(SPGetWebService.class);

        Call<ResponseBody> call = downloadService.downloadFileWithDynamicUrlSync(fileUrl);


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("TAG", "server contacted and has file");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {

                            boolean writtenToDisk = writeResponseBodyToDisk(response.body(), orderno);
                            if(writtenToDisk) {

                                try {

                                    BaseUtils.parseStockTakeJson(MainActivity.mContext.getFilesDir().toString() + "/" + ("stockTakeListAsset" + orderno + ".json"));

                                    if (DownloadFragment.stockTakeListData.size() > 0) {
                                        DownloadFragment.stockTakeListData.remove(0);
                                        EventBus.getDefault().post(new CallbackResponseEvent(""));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.i("UpdateFailEvent", "UpdateFailEvent case 0 ");
                                    EventBus.getDefault().post(new UpdateFailEvent());
                                }
                            }
                            Log.d("TAG", "file download was a success? " + writtenToDisk);
                            return null;
                        }
                    }.execute();
                } else {
                    EventBus.getDefault().post(new UpdateFailEvent());
                    Log.d("TAG", "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                EventBus.getDefault().post(new UpdateFailEvent());

                Log.e("TAG", "error");
            }
        });
    }
    private static boolean writeResponseBodyToDisk(ResponseBody body, String orderno) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(MainActivity.mContext.getFilesDir().toString() + "/" + (("stockTakeListAsset" + orderno + ".json") ));
            futureStudioIconFile.delete();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    if(fileSize == -1){
                        Log.i("UpdateFailEvent", "UpdateFailEvent case 1 ");
                        EventBus.getDefault().post(new UpdateFailEvent());
                        break;
                    }

                    Log.d("TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);

                    float progress = (float)fileSizeDownloaded / (float)fileSize;
                    Log.d("TAG", "file download: 2 " + progress);

                    ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new LoginDownloadProgressEvent(progress));
                        }
                    });
                }

                outputStream.flush();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
