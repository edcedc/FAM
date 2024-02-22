package com.spit.fam.WebService.Callback;

import com.spit.fam.Entity.Status;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetStatusCallback implements Callback<List<Status>> {
    public GetStatusCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<List<Status>> call, Response<List<Status>> response) {
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<List<Status>> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
/*
public class GetCategoryListCallback implements Callback<List<Category>> {
    public GetCategoryListCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
        Timber.d("onResponse" + response.toString() + " " + response.body().size());
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<List<Category>> call, Throwable t) {
        Timber.d("onFailure" + t.toString());
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}

 */