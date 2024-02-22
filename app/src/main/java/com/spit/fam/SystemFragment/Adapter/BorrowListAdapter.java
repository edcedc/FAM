package com.spit.fam.SystemFragment.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.Entity.BorrowList;
import com.spit.fam.Event.BorrowListAssets;
import com.spit.fam.Event.DialogEvent;
import com.spit.fam.InternalStorage;
import com.spit.fam.MainActivity;
import com.spit.fam.R;
import com.spit.fam.SystemFragment.BorrowListItemListFragment;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class BorrowListAdapter extends BaseAdapter {
    List<BorrowList> assetResponse;
    Context context;
    boolean borrowList;
    int type = -1;

    public BorrowListAdapter(List<BorrowList> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
    }

    public BorrowListAdapter(List<BorrowList> assetResponse, Context context, boolean borrowList) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.borrowList = borrowList;
    }

    public BorrowListAdapter(List<BorrowList> assetResponse, Context context, boolean borrowList, int type) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.borrowList = borrowList;
        this.type = type;
        Log.i("type", "type " + type);
    }

    public void setData(List<BorrowList> assetResponse, Context context) {
        this.assetResponse = assetResponse;
        this.context = context;
    }

    public void setData(List<BorrowList> assetResponse, Context context, int type) {
        this.assetResponse = assetResponse;
        this.context = context;
        this.type = type;
    }

    @Override
    public int getCount() {
        return assetResponse.size();
    }

    @Override
    public BorrowList getItem(int i) {
        return assetResponse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return assetResponse.get(i).hashCode();
    }

    public void setTextView(TextView textView, String data) {
        if(textView == null)
            return;

        if(data == null) {
            ((ViewGroup)textView.getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)textView.getParent()).setVisibility(View.VISIBLE);
            textView.setText(data);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.borrow_listview_cell, viewGroup, false);
        setTextView (((TextView)view.findViewById(R.id.cell_title)), ((getItem(i).getDisposalNo()  != null ?  getItem(i).getDisposalNo() + " | " : "") + getItem(i).getName()));

        setTextView( ((TextView)view.findViewById(R.id.approval_date)), (getItem(i).getApproved_date()));
        setTextView( ((TextView)view.findViewById(R.id.apply_date)), (getItem(i).getCreated_at()));

        setTextView( ((TextView)view.findViewById(R.id.valid_date)), (getItem(i).getValid_date()));
        try {
            ((ViewGroup) view.findViewById(R.id.approved_by).getParent()).setVisibility(View.VISIBLE);

            ((TextView) view.findViewById(R.id.approved_by)).setVisibility(View.VISIBLE);
            setTextView( ((TextView) view.findViewById(R.id.approved_by)), (getItem(i).getApprovedby()));
        } catch (Exception e) {
            ((ViewGroup) view.findViewById(R.id.approved_by).getParent()).setVisibility(View.GONE);
        }

        ((TextView)view.findViewById(R.id.cell_status_applied)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.cell_status_approved)).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.cell_status_rejected)).setVisibility(View.GONE);

        Log.i("case1", "case 1 " + type);
        if(getItem(i).getBorrow_status() != null) {
            if(getItem(i).getBorrow_status().getName().equals("Rejected")) {
                ((TextView)view.findViewById(R.id.cell_status_rejected)).setVisibility(View.VISIBLE);
            } else if(getItem(i).getBorrow_status().getName().equals("Applied")) {
                ((TextView)view.findViewById(R.id.cell_status_applied)).setVisibility(View.VISIBLE);
            } else if(getItem(i).getBorrow_status().getName().equals("Approved")) {
                ((TextView)view.findViewById(R.id.cell_status_approved)).setVisibility(View.VISIBLE);
            }
        } else if(type != -1) {

            if(type == 2) {
                ((TextView)view.findViewById(R.id.cell_status_rejected)).setVisibility(View.VISIBLE);
            } else if(type == 0) {
                ((TextView)view.findViewById(R.id.cell_status_applied)).setVisibility(View.VISIBLE);
            } else if(type == 1) {
                ((TextView)view.findViewById(R.id.cell_status_approved)).setVisibility(View.VISIBLE);
            }
        }

        if(getItem(i).getDisposal_status() != null) {
            if(getItem(i).getDisposal_status().getName().equals("Rejected")) {
                ((TextView)view.findViewById(R.id.cell_status_rejected)).setVisibility(View.VISIBLE);
            } else if(getItem(i).getDisposal_status().getName().equals("Applied")) {
                ((TextView)view.findViewById(R.id.cell_status_applied)).setVisibility(View.VISIBLE);
            } else if(getItem(i).getDisposal_status().getName().equals("Approved")) {
                ((TextView)view.findViewById(R.id.cell_status_approved)).setVisibility(View.VISIBLE);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(type == 0 || type == 2)
                    return;

                if(getItem(i).getBorrow_status() != null && !getItem(i).getBorrow_status().getName().equals("Approved")) {
                    return;
                }

                if(getItem(i).getDisposal_status() != null && !getItem(i).getDisposal_status().getName().equals("Approved")) {
                    return;
                }*/

                BorrowListItemListFragment.borrowList = getItem(i);
                BorrowListItemListFragment.type = type;

                BorrowListItemListFragment borrowListItemListFragment = new BorrowListItemListFragment();

                if(getItem(i).getBorrowno() != null && getItem(i).getBorrowno().length() > 0) {
                    BorrowListItemListFragment.BORROW_NO = getItem(i).getBorrowno();
                    BorrowListItemListFragment.BORROW_LIST = true;
                } else {
                    BorrowListItemListFragment.BORROW_NO = null;
                    BorrowListItemListFragment.BORROW_LIST = false;
                }

                if(getItem(i).getDisposalNo() != null && getItem(i).getDisposalNo().length() > 0) {
                    BorrowListItemListFragment.DISPOSAL_NO = getItem(i).getDisposalNo();
                    BorrowListItemListFragment.DISPOSAL_LIST = true;
                } else {
                    BorrowListItemListFragment.DISPOSAL_NO = null;
                    BorrowListItemListFragment.DISPOSAL_LIST = false;
                }



                Log.i("BORROW_LIST", "BORROW_LIST " + BorrowListItemListFragment.BORROW_LIST);
                Log.i("DISPOSAL_LIST", "DISPOSAL_LIST " + BorrowListItemListFragment.DISPOSAL_LIST);

                BorrowListAssets borrowno = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_BORROW_NO_ +  getItem(i).getBorrowno());
                BorrowListAssets disposalno = Hawk.get(InternalStorage.OFFLINE_CACHE.SP_DISPOSAL_NO_ +  getItem(i).getDisposalNo());

                //if(!((MainActivity)context).isNetworkAvailable()) {
                   // if(borrowno == null && disposalno == null) {

                    //} else {
                Log.i("yoyo", "yoyo " + borrowno + " " +  getItem(i).getBorrowCount() + " " + getItem(i).getBorrowTotal());
                /*if(borrowno != null && getItem(i).getBorrowCount() == getItem(i).getBorrowTotal()) {
                    return;
                }

                if(disposalno != null && getItem(i).getBorrowCount() == getItem(i).getDisposalTotal()) {
                    return;
                }*/

                if(!((MainActivity)context).isNetworkAvailable()){
                    if(BorrowListItemListFragment.BORROW_LIST && borrowno == null) {
                        EventBus.getDefault().post(new DialogEvent(context.getString(R.string.app_name), context.getString(R.string.no_data)));
                        return;
                    }

                    if(BorrowListItemListFragment.DISPOSAL_LIST && disposalno == null) {
                        EventBus.getDefault().post(new DialogEvent(context.getString(R.string.app_name), context.getString(R.string.no_data)));
                        return;
                    }
                }


                ((MainActivity) context).replaceFragment(borrowListItemListFragment);
                    //}
                /*} else {
                    ((MainActivity) context).replaceFragment(borrowListItemListFragment);
                }*/

                //AssetsDetailWithTabFragment.id = Integer.parseInt(getItem(i).getId());
                //AssetsDetailWithTabFragment.asset = (getItem(i));
                //((MainActivity)context).replaceFragment(new AssetsDetailWithTabFragment());
                //Navigation.findNavController(((MainActivity)context).getSupportFragmentManager().getFragments().get(0).getView() ).navigate(R.id.action_searchListFragment_to_assetsDetailWithTabFragment);
            }
        });

        if(borrowList) {
            ((TextView) view.findViewById(R.id.borrow_disposal_progress)).setText(context.getString(R.string.borrowed_item));
        } else {
            ((TextView) view.findViewById(R.id.borrow_disposal_progress)).setText(context.getString(R.string.disposed_item));
        }

        if(borrowList) {

            if((type == 2 || type == 0) &&  (Integer.parseInt(getItem(i).getTotal()) != Integer.parseInt(getItem(i).getApprovedString()))) {
                setTextView(((TextView) view.findViewById(R.id.borrowed_item)), (Integer.parseInt(getItem(i).getTotal()) - Integer.parseInt(getItem(i).getApprovedString())) + "/" + getItem(i).getTotal());
                ((TextView) view.findViewById(R.id.borrow_disposal_progress)).setText(context.getString(R.string.not_approved));
            } else {
                setTextView(((TextView) view.findViewById(R.id.borrowed_item)), (getItem(i).getBorrowedCountString()));
            }
        } else {

            if((type == 2 || type == 0) &&  (Integer.parseInt(getItem(i).getTotal()) != Integer.parseInt(getItem(i).getApprovedString()))) {
                Log.i("case 1" , "disposal case 1");

                setTextView(((TextView) view.findViewById(R.id.borrowed_item)), (Integer.parseInt(getItem(i).getTotal()) - Integer.parseInt(getItem(i).getApprovedString())) + "/" + getItem(i).getTotal());
                ((TextView) view.findViewById(R.id.borrow_disposal_progress)).setText(context.getString(R.string.not_approved));
            } else {
                Log.i("case 1" , "disposal case 2");

                setTextView(((TextView) view.findViewById(R.id.borrowed_item)), (getItem(i).getBorrowedCountString()));
            }
            //setTextView (((TextView) view.findViewById(R.id.borrowed_item)), (getItem(i).getDisposalCountString()));
        }


        return view;
    }
}
