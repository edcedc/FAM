package com.spit.fam.WebService.Callback;

import android.util.Log;

import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.ListingResponse;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetListingCallback implements Callback<ListingResponse> {
    public GetListingCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
        Log.i("onResponse", "onResponse" + response.toString());

        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else {
            EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));

            EventBus.getDefault().post(new UpdateFailEvent());

        }
    }

    @Override
    public void onFailure(Call<ListingResponse> call, Throwable t) {
        EventBus.getDefault().post(new UpdateFailEvent());

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
