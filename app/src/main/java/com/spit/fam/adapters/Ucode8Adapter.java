package com.spit.fam.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.spit.fam.fragments.AccessUcode8Fragment;
import com.spit.fam.fragments.InventoryRfidiMultiFragment;
import com.spit.fam.fragments.UtraceFragment;

public class Ucode8Adapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 2;
    public Fragment fragment0, fragment1, fragment2;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = new AccessUcode8Fragment();
                fragment0 = fragment;
                break;
            case 1:
                fragment = InventoryRfidiMultiFragment.newInstance(true,"E2806894");
                fragment1 = fragment;
                break;
            case 2:
                fragment = new UtraceFragment();
                fragment2 = fragment;
                break;
            default:
                fragment = null;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return NO_OF_TABS;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public Ucode8Adapter(FragmentManager fm) {
        super(fm);
    }
}
