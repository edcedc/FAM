package com.spit.fam.WebService.Callback;

import com.spit.fam.Entity.StockTakeListItemRemark;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetRemarkCallback implements Callback<StockTakeListItemRemark> {
    public GetRemarkCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<StockTakeListItemRemark> call, Response<StockTakeListItemRemark> response) {
        //Timber.d("onResponse" + response.toString());
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
    }

    @Override
    public void onFailure(Call<StockTakeListItemRemark> call, Throwable t) {
        //Timber.d("onFailure" + t.toString());
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
        //AssetDetailStockTakeItemRemarkFragment.updateCase = false;
    }
}