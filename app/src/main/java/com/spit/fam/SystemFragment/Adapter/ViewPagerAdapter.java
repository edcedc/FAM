package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.spit.fam.R;
import com.spit.fam.SystemFragment.AssetChangeFragment;
import com.spit.fam.SystemFragment.AssetDetailStockTakeItemRemarkFragment;
import com.spit.fam.SystemFragment.AssetRegisterFragment;
import com.spit.fam.SystemFragment.AssetSearchFragment;
import com.spit.fam.SystemFragment.AssetsDetailFragment;
import com.spit.fam.SystemFragment.AssetsDetailWithTabFragment;
import com.spit.fam.SystemFragment.LoginFragment;
import com.spit.fam.SystemFragment.StockTakeListItemFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    Context context;

    public ViewPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if( (((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK)) && StockTakeListItemFragment.stockTakeList == null && AssetsDetailWithTabFragment.asset != null && AssetsDetailWithTabFragment.asset.getStatus().id == 7) {
            return Fragment.instantiate(context, AssetsDetailFragment.class.getName());
        } else if(AssetListAdapter.WITH_EPC) {
            //if( ((MainActivity)context).isNetworkAvailable()) {
                if (position == 0) {
                    return Fragment.instantiate(context, AssetSearchFragment.class.getName());
                } else if (position == 1) {
                    return Fragment.instantiate(context, AssetsDetailFragment.class.getName());
                } else {

                    if ((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK) {
                        return Fragment.instantiate(context, AssetChangeFragment.class.getName());
                    } else {
                        AssetDetailStockTakeItemRemarkFragment.REMARK_STRING = "";
                        return Fragment.instantiate(context, AssetDetailStockTakeItemRemarkFragment.class.getName());
                    }
                }
            /*} else {

                if (position == 0) {
                    return Fragment.instantiate(context, AssetSearchFragment.class.getName());
                }  else {
                    Log.i("viewpageradapter", "viewpageradapter " + AssetsDetailWithTabFragment.assetRemark + " " + !AssetsDetailWithTabFragment.WITH_REMARK + " " + LoginFragment.SP_API + " " + AssetsDetailWithTabFragment.WITH_REMARK);

                    if ((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK)
                        return Fragment.instantiate(context, AssetChangeFragment.class.getName());
                    else {
                        return Fragment.instantiate(context, AssetDetailStockTakeItemRemarkFragment.class.getName());
                    }
                }
            }*/
        } else {
           // if(((MainActivity)context).isNetworkAvailable()) {
                if (position == 0) {
                    return Fragment.instantiate(context, AssetsDetailFragment.class.getName());
                } else if (position == 1) {
                    if(AssetsDetailWithTabFragment.WITH_REMARK) {
                        AssetDetailStockTakeItemRemarkFragment.REMARK_STRING = "";
                        return Fragment.instantiate(context, AssetDetailStockTakeItemRemarkFragment.class.getName());
                    }
                    return Fragment.instantiate(context, AssetRegisterFragment.class.getName());
                }
           // } else {
           //     return Fragment.instantiate(context, AssetRegisterFragment.class.getName());
          ///  }
        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.i("stockTakeList", "stockTakeList " + StockTakeListItemFragment.stockTakeList);
        if                       (  (((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK)) &&
             (StockTakeListItemFragment.stockTakeList == null && AssetsDetailWithTabFragment.asset != null && AssetsDetailWithTabFragment.asset.getStatus().id == 7)) {
            return context.getString(R.string.details);
        } else if(AssetListAdapter.WITH_EPC) {
           // if(((MainActivity)context).isNetworkAvailable()) {
                switch (position) {
                    case 0:
                        return context.getString(R.string.search);
                    case 1:
                        return context.getString(R.string.details);
                    case 2:
                        if ((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK)
                            return context.getString(R.string.change_epc);
                        else
                            return context.getString(R.string.remark);
                }
            /*} else {

                switch (position) {
                    case 0:
                        return context.getString(R.string.search);
                    case 1:
                        if ((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK)
                            return context.getString(R.string.change_epc);
                        else
                            return context.getString(R.string.remark);
                }
            }*/
        } else {
           // if(((MainActivity)context).isNetworkAvailable()) {

                switch (position) {
                    case 0:
                        return context.getString(R.string.details);
                    case 1:
                        if(AssetsDetailWithTabFragment.WITH_REMARK) {
                            return context.getString(R.string.remark);
                        }
                        return context.getString(R.string.bind_to);
                }
           // } else {
           //     return context.getString(R.string.bind_to);
           // }
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        Log.i("getCount", "getCount " + AssetsDetailWithTabFragment.asset + " " + StockTakeListItemFragment.stockTakeList);
        try {
            Log.i("getCount", "getCount " + AssetsDetailWithTabFragment.asset.getStatus().id + " ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //if(StockTakeListItemFragment.stockTakeList != null) {
        //    return 3.
        //}
        
        if((((!LoginFragment.SP_API && AssetsDetailWithTabFragment.assetRemark == null) || !AssetsDetailWithTabFragment.WITH_REMARK)) && StockTakeListItemFragment.stockTakeList == null && AssetsDetailWithTabFragment.asset != null && AssetsDetailWithTabFragment.asset.getStatus().id == 7){
            Log.i("case 1", "stockTakeList case 1");
            return 1;

        } else if(AssetListAdapter.WITH_EPC) {
            Log.i("case 2", "stockTakeList case 2");

            //if(((MainActivity)context).isNetworkAvailable()) {
                return 3;
           // } else {
           //     return 2;
           // }
        } else {
            Log.i("case 3", "stockTakeList case 3");

            // if(((MainActivity)context).isNetworkAvailable()) {
                return 2;
           // } else {
           //     return 1;
           // }
        }
    }
}
