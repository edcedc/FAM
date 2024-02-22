package com.spit.fam.SystemFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.orhanobut.hawk.Hawk;

import java.util.Locale;

public class SettingFragment extends BaseFragment {

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b){
        super.onCreateView(li, vg, b);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.setting_fragment, null);

        //                android:background="@drawable/app_logo"
        Glide.with(getActivity()).load(R.drawable.app_logo).into((ImageView)view.findViewById(R.id.add_logo));

        ((TextView)view.findViewById(R.id.language)).setText(Hawk.get(InternalStorage.Setting.LANGUAGE, "zh"));
        ((TextView)view.findViewById(R.id.host_address)).setText(Hawk.get(InternalStorage.Setting.HOST_ADDRESS, ""));
        ((TextView)view.findViewById(R.id.company_id)).setText(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);

                //arrayAdapter.add(getString(R.string.chinese));
                arrayAdapter.add(getString(R.string.t_chinese));
                arrayAdapter.add(getString(R.string.s_chinese));
                arrayAdapter.add(getString(R.string.english));

                builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0) {
                            ((TextView) view.findViewById(R.id.language)).setText(getString(R.string.t_chinese));
                        } else if(which == 1) {
                            ((TextView) view.findViewById(R.id.language)).setText(getString(R.string.s_chinese));
                        } else if(which == 2) {
                            ((TextView) view.findViewById(R.id.language)).setText(getString(R.string.english));
                        }
                    }
                });
                builderSingle.show();
            }
        });

        (view.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });


        ((TextView)view.findViewById(R.id.host_address)).setText(Hawk.get(InternalStorage.Setting.HOST_ADDRESS, ""));
        ((TextView)view.findViewById(R.id.company_id)).setText(Hawk.get(InternalStorage.Setting.COMPANY_ID, ""));


        String localeString = Hawk.get(InternalStorage.Setting.LANGUAGE, "");

        if(localeString.length() == 0) {
            try {
                Locale current = getResources().getConfiguration().locale;
                localeString = current.getLanguage();
                Log.i("languageCode", "languageCode " + current.getLanguage() + " " + current.getDisplayLanguage());
            } catch (Exception e) {

            }
            if (localeString.length() == 0) {
                localeString = "en";// current.getLanguage();
            }
        }

        if(localeString.equals("en")) {
            ((TextView)view.findViewById(R.id.language)).setText(getString(R.string.english));
        } else if(localeString.equals("zh")) {
            ((TextView)view.findViewById(R.id.language)).setText(getString(R.string.t_chinese));
        } else if(localeString.equals("zt")) {
            ((TextView)view.findViewById(R.id.language)).setText(getString(R.string.s_chinese));
        }

        return view;
    }

    public void save() {

        if(((TextView)view.findViewById(R.id.language)).getText().toString().equals("English")) {
            Hawk.put(InternalStorage.Setting.LANGUAGE, "en");
        } else if(((TextView)view.findViewById(R.id.language)).getText().toString().equals(getString(R.string.t_chinese))) {
            Hawk.put(InternalStorage.Setting.LANGUAGE, "zh");
        }  else {
            Hawk.put(InternalStorage.Setting.LANGUAGE, "zt");
        }

        String result =  ((TextView)view.findViewById(R.id.host_address)).getText().toString().trim();

        if(!result.startsWith("http://") &&  !result.startsWith("https://")) {
            result = "http://" + result;
        }

        Hawk.put(InternalStorage.Setting.HOST_ADDRESS, result);

        Hawk.put(InternalStorage.Setting.COMPANY_ID, ((TextView)view.findViewById(R.id.company_id)).getText().toString().trim());
        InternalStorage.resetStaticPath();

        Intent intent = getActivity().getIntent();
        getActivity().onBackPressed();
    }

    public void onPause() {
        super.onPause();
        MainActivity.hideKeyboard(getActivity());
    }
}
