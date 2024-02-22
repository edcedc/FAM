package com.spit.fam.WebService.Callback;

import android.util.Log;
import android.widget.Toast;

import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.Response.UserListResponse;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListCallback implements Callback<UserListResponse> {
    public UserListCallback() {
    }

    private int type;
    public UserListCallback(int type) {
        this.type = type;
    }

    @Override
    public void onResponse(Call<UserListResponse> call, Response<UserListResponse> response) {
        Log.i("case 0", "case 0 " + call.request().url() + " " + type);

        if(response.code() == 200) {
            Hawk.put(InternalStorage.OFFLINE_CACHE.SP_USER_CALLED_DATE, response.body().getThiscalldate());
            for(int i = 0; i < response.body().getData().size(); i++) {
                if(response.body().getData().get(i).getLoginid().toLowerCase().equals(Hawk.get(InternalStorage.Login.USER_ID, "").toLowerCase())) {
                    Log.i("UserListResponse", "UserListResponse " + Hawk.get(InternalStorage.Login.USER_ID, "") + " " + Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, response.body().getData().get(i).getUserid()));
                    Log.i("UserListResponse", "UserListResponse USER_ID " + response.body().getData().get(i).getUserid());
                    Hawk.put(InternalStorage.OFFLINE_CACHE.USER_ID, response.body().getData().get(i).getUserid());

                    InternalStorage.resetStaticPath();
                }
            }

            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(type + "", response.body().getData());
            callbackResponseEvent.type = type;

            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            Log.i("case 1", "case 1 " + call.request().url());
            EventBus.getDefault().post(new CallbackFailEvent(response.message()) + " " + call.request().url());
        }
    }

    @Override
    public void onFailure(Call<UserListResponse> call, Throwable t) {
        Log.i("case 2", "case 2 " + call.request().url() + " " + t.getMessage());
        Toast.makeText(MainActivity.mContext, MainActivity.mContext.getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()) + " " + call.request().url());

    }
}
