package com.spit.fam.SystemFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.fragments.HomeFragment;

import java.util.ArrayList;

public class UnitSelectFragment extends HomeFragment {
    private Spinner spinner;
    private ArrayList<String> stringList = new ArrayList<>();

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_select_unit, null);
        spinner = view.findViewById(R.id.unit_spinner);

        for (int i = 0; i < 10; i++) {
            stringList.add(new String("Name_" + i + "Id_" + i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),  android.R.layout.simple_spinner_dropdown_item, stringList);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        view.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).changeFragment(new DownloadFragment());
            }
        });
    }


    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b){
        super.onCreateView(li, vg, b);
        return view;
    }
}
