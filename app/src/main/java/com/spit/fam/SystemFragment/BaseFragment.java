package com.spit.fam.SystemFragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import com.spit.fam.Entity.Category;
import com.spit.fam.Entity.Location;
import com.spit.fam.Event.CallbackFailEvent;
import com.spit.fam.Event.CallbackResponseEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

public class BaseFragment extends Fragment {
    public View view;

    public void onPause() {
        super.onPause();
        MainActivity.isNetworkOK = false;

        ((MainActivity)MainActivity.mContext).stop();
    }

    public void onCreate(Bundle bundle) {
        changeLocale(getActivity(), Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"));

        super.onCreate(bundle);

        ((MainActivity)getActivity()).setLowBatteryIfNeeded();
    }

    public void changeLocale(Context context, String localeString) {
        Log.i("localeString", "localeString" + localeString);

        String languageToLoad  = localeString; // your language
        Locale locale = new Locale(languageToLoad);

        if(languageToLoad != null && languageToLoad.equals("zt")) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Log.i("hihi", "hihi battery " + MainActivity.mCs108Library4a.getBatteryCount() + " " + MainActivity.mCs108Library4a.getBatteryDisplay(false) + " " + MainActivity.mCs108Library4a.isBatteryLow());
    }


    @Override
    public void onStop() {
        super.onStop();
        MainActivity.nfcCardNumber = null;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackFailEvent failEvent) {
    }

    public void replaceFragment(Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getActivity().getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }


    public void changeFragment(Fragment fragment){

        String backStateName = fragment.getClass().getName();

        FragmentManager manager = ((MainActivity)MainActivity.mContext).getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
           // ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Category.class) {
            Hawk.put(InternalStorage.Application.CATEGORY, event.getResponse());
        }

        if(event.getResponse() instanceof List && ((List)event.getResponse()).size() > 0 && ((List)event.getResponse()).get(0).getClass() == Location.class) {
            Hawk.put(InternalStorage.Application.LOCATION, event.getResponse());
        }
    }
}

