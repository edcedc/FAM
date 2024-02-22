package com.spit.fam.WebService.Callback;

import android.util.Log;

import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.Event.CallbackStartEvent;
import com.spit.fam.Event.SubmitFailEvent;
import com.spit.fam.Event.UpdateFailEvent;
import com.spit.fam.Response.APIResponse;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateAssetEpcCallback implements Callback<APIResponse> {
    //companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, pendingReturnAsset.getReturnList())
    String companyId;
    String userid;
    String firstLocation;
    String lastLocation;
    String returnList;

    public UpdateAssetEpcCallback(String companyId, String userid, String firstLocation, String lastLocation, String returnList) {
        this.companyId = companyId;
        this.userid = userid;
        this.firstLocation = firstLocation;
        this.lastLocation = lastLocation;
        this.returnList = returnList;
    }

    public UpdateAssetEpcCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    int type;
    public UpdateAssetEpcCallback(int type) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.type = type;
    }

    @Override
    public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {
        Log.i("onResponse123", "onResponse123" + response.toString() + " " + call.request());
        if(response.code() == 200) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
            callbackResponseEvent.type = type;
            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            EventBus.getDefault().post(new UpdateFailEvent());
            try {
                EventBus.getDefault().post(new SubmitFailEvent(companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, returnList));
            } catch (Exception e){

            }
            if(type == 3){
                EventBus.getDefault().post(new CallbackFailEvent(response.message()));
            }
            //EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<APIResponse> call, Throwable t) {
        EventBus.getDefault().post(new UpdateFailEvent());
        try {
            EventBus.getDefault().post(new SubmitFailEvent(companyId, userid, firstLocation, lastLocation.isEmpty() ? firstLocation : lastLocation, returnList));
        } catch (Exception e) {

        }
        if(type == 3){
            EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
        }
        //EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
