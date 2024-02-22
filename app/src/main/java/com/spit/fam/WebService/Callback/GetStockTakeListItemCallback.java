package com.spit.fam.WebService.Callback;

import com.spit.fam.Entity.StockTakeListItem;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetStockTakeListItemCallback implements Callback<List<StockTakeListItem>> {
    public GetStockTakeListItemCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<List<StockTakeListItem>> call, Response<List<StockTakeListItem>> response) {
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<List<StockTakeListItem>> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}

