package com.spit.fam.WebService.Callback;

import android.util.Log;

import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Response.APIResponse;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SPWebServiceCallback implements Callback<List<APIResponse>> {
    public SPWebServiceCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public SPWebServiceCallback(String id) {
        Log.i("SPWebServiceCallback", "SPWebServiceCallback " + id);
        this.id = id;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public SPWebServiceCallback(String id, String assetNo) {
        this.id = id;
        this.assetNo = assetNo;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    private String id;
    private String assetNo;

    @Override
    public void onResponse(Call<List<APIResponse>> call, Response<List<APIResponse>> response) {
        Log.i("success", "success" + response.toString() + " " + call.toString());
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(id, assetNo, response.raw().request().url().toString(), response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
    }

    @Override
    public void onFailure(Call<List<APIResponse>> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().toString());

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
