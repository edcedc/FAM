package com.spit.fam.SystemFragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import com.spit.fam.Entity.StockTakeList;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.Adapter.StockTakeListAdapter;

import java.util.List;

public class OldStockTakeFragment extends BaseFragment {
    public static int STOCK_TAKE_API = 11;
    public static String STOCK_TAKE_NO_EDITED = null;

    List<StockTakeList> data;
    StockTakeListAdapter stockTakeListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    ListView listView;
    View noResult;



}
