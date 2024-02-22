package com.spit.fam.WebService.Callback;

import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Request.StockTakeListItemRemarkRequest;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadStockTakeListRemarkCallback implements Callback<StockTakeListItemRemarkRequest> {
    public UploadStockTakeListRemarkCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<StockTakeListItemRemarkRequest> call, Response<StockTakeListItemRemarkRequest> response) {
         if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<StockTakeListItemRemarkRequest> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}