package com.spit.fam.WebService.P2Callback;

import android.util.Log;

import com.spit.fam.Entity.SPEntityP3.SearchNoEpcItem;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchNoEpcCallback implements Callback<List<SearchNoEpcItem>> {
    private int id = -1;

    public SearchNoEpcCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public SearchNoEpcCallback(int id) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.id = id;
    }

    @Override
    public void onResponse(Call<List<SearchNoEpcItem>> call, Response<List<SearchNoEpcItem>> response) {
        Log.i("onResponse", "onResponse" + response.toString());

        if(response.code() == 200) {
            if(id == -1) {
                EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
            } else {
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
                callbackResponseEvent.type = id;

                EventBus.getDefault().post(callbackResponseEvent);
            }
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<List<SearchNoEpcItem>> call, Throwable t) {

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}