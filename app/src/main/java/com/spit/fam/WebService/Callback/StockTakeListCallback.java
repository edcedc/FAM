package com.spit.fam.WebService.Callback;

import android.util.Log;

import com.spit.fam.Entity.SpEntity.StockTakeNoList;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockTakeListCallback implements Callback<StockTakeNoList> {
    private String id;

    public StockTakeListCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public StockTakeListCallback(String id) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.id = id;
    }

    @Override
    public void onResponse(Call<StockTakeNoList> call, Response<StockTakeNoList> response) {
        Log.i("onResponse", "onResponse" + response.toString());

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
    public void onFailure(Call<StockTakeNoList> call, Throwable t) {

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}