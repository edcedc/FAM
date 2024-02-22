package com.spit.fam.WebService.Callback;

import android.util.Log;

import com.spit.fam.Entity.SPEntityP2.StockTakeListData;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetStockTakeListDataCallback implements Callback<StockTakeListData> {
    private String id;

    public GetStockTakeListDataCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public GetStockTakeListDataCallback(String id) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.id = id;
    }

    @Override
    public void onResponse(Call<StockTakeListData> call, Response<StockTakeListData> response) {
        Log.i("onResponse", "onResponse " + response.raw().message().toString() + " " + call.request().toString());
        if(response.code() == 200) {
            if(id == null) {
                EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
            } else {
                EventBus.getDefault().post(new CallbackResponseEvent(id, null, response.body()));
            }
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<StockTakeListData> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().url());
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
