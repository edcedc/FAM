package com.spit.fam.NewHandHeld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.spit.fam.R;

import java.util.ArrayList;

public class TagMsgBaseAdapter extends BaseAdapter {
    // 定义信息的集合体
    private ArrayList<TagMsgEntity> mTagMsgEntitys;
    // 定义上下文
    private Context mCtx;
    // 动态加载Layout布局文件
    private LayoutInflater mLayoutInflater;

    public TagMsgBaseAdapter(ArrayList<TagMsgEntity> mTagMsgEntitys, Context mCtx) {
        if(mTagMsgEntitys == null)
            mTagMsgEntitys = new ArrayList<>();
        this.mTagMsgEntitys = mTagMsgEntitys;
        this.mCtx = mCtx;
        // 动态加载器mLayoutInflater由调用者activity获取
        mLayoutInflater = LayoutInflater.from(this.mCtx);
    }

    @Override
    public int getCount() {
        return mTagMsgEntitys == null ? 0 : mTagMsgEntitys.size();
    }

    @Override
    public Object getItem(int position) {
        return mTagMsgEntitys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        // 根据listview的位置提取聊天信息
        TagMsgEntity tagMsgEntity = mTagMsgEntitys.get(position);
        // 每条聊天信息显示的布局组件
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_inventory, null);
            // 存储每条标签信息显示的组件
            mViewHolder = new ViewHolder();
            mViewHolder.mTagType = (TextView) convertView.findViewById(R.id.item_inventory_lblType);
            mViewHolder.mRssi = (TextView) convertView.findViewById(R.id.item_inventory_rssi);
            mViewHolder.mAntenna = (TextView) convertView.findViewById(R.id.item_inventory_antenna);
            mViewHolder.mEPC = (TextView) convertView.findViewById(R.id.item_inventory_epc);
            mViewHolder.mTID = (TextView) convertView.findViewById(R.id.item_inventory_tid);
            mViewHolder.mUser = (TextView) convertView.findViewById(R.id.item_inventory_user);
            convertView.setTag(mViewHolder);
        } else {
            // 不是第一次显示的直接获取上次的布局的组件信息
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        // 对布局中的组件内容进行设置
        if (!tagMsgEntity.getTagType().equals(mViewHolder.mTagType.getText()))
            mViewHolder.mTagType.setText(tagMsgEntity.getTagType());
        if (!tagMsgEntity.getRssi().equals(mViewHolder.mRssi.getText()))
            mViewHolder.mRssi.setText(tagMsgEntity.getRssi());
        if (!tagMsgEntity.getAntenna().equals(mViewHolder.mAntenna.getText()))
            mViewHolder.mAntenna.setText(tagMsgEntity.getAntenna());
        if (!tagMsgEntity.getEPC().equals(mViewHolder.mEPC.getText()))
            mViewHolder.mEPC.setText(tagMsgEntity.getEPC());
        if (!tagMsgEntity.getTID().equals(mViewHolder.mTID.getText()))
            mViewHolder.mTID.setText(tagMsgEntity.getTID());
        if (!tagMsgEntity.getUser().equals(mViewHolder.mUser.getText()))
            mViewHolder.mUser.setText(tagMsgEntity.getUser());
        System.out.println("EPC：" + mViewHolder.mEPC.getText());
        return convertView;
    }

    public void setChangeItem(TagMsgEntity item,int position) {
        TagMsgEntity tag = (TagMsgEntity)mTagMsgEntitys.get(position);
        if(!tag.getAntenna().equals("") && !tag.getAntenna().equals(item.getAntenna()))
            tag.setAntenna("" + Integer.parseInt(item.getAntenna()));
        tag.setRssi(item.getRssi());
    }

    public void addItem(TagMsgEntity item) {
        mTagMsgEntitys.add(item);
    }

//    public int reflashData(TagMsgEntity item) {
//        boolean isAdd = true;
//        int p = -1;
//        for (int i = 0; i < mTagMsgEntitys.size(); i++) {
//            if (mTagMsgEntitys.get(i).getEPC().equals(item.getEPC())) {
//                isAdd = false;
//                if (!item.getAntenna().equals("") || !item.getRssi().equals(""))
//                    setChangeItem(item, i);
//                p = i;
//                break;
//            }
//        }
//        if (isAdd) {
//            // TODO:
//            addItem(item);
//            p = mTagMsgEntitys.size() - 1;
//        }
//        notifyDataSetChanged();
//        return p;
//    }

    public int reflashData(TagMsgEntity item) {
        boolean isAdd = true;
        int p = -1;
        String epc = "";
        String tid = "";
        String epc1="";
        String tid1="";
        for (int i = 0; i < mTagMsgEntitys.size(); i++) {
            epc = mTagMsgEntitys.get(i).getEPC();
            tid = mTagMsgEntitys.get(i).getTID();
            epc1=item.getEPC();
            tid1 = item.getTID();
            if(tid == null || tid.equals("") || tid1 == null || tid1.equals("")){
                if(epc != null && !epc.equals("") && epc1 != null && !epc1.equals("")){
                    if(epc.equals(epc1)){
                        isAdd = false;
                        setChangeItem(item, i);
                        p = i;
                        break;
                    }
                }
            }else if(epc != null && !epc.equals("") && epc1 != null && !epc1.equals(""))
            {
                if(tid != null && !tid.equals("") && tid1 != null && !tid1.equals("")){
                    if(tid.equals(tid1)){
                        isAdd = false;
                        setChangeItem(item, i);
                        p = i;
                        break;
                    }
                }
            }else {
                if (epc.equals(epc1) && tid.equals(tid1)) {
                    isAdd = false;
                    setChangeItem(item, i);
                    p = i;
                    break;
                }
            }
        }
        if (isAdd) {
            addItem(item);
            p = mTagMsgEntitys.size() - 1;
        }
        notifyDataSetChanged();
        return p;
    }

    public void cleanItem() {
        if (mTagMsgEntitys != null)
            mTagMsgEntitys.clear();
        notifyDataSetChanged();
    }

    class ViewHolder{
        private TextView mTagType;// 标签类型
        private TextView mRssi; // RSSI
        private TextView mAntenna;// 天线
        private TextView mEPC; // EPC
        private TextView mTID;// TID
        private TextView mUser; // User
    }
}

