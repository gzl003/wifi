package com.huanxi.gzlchat;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.Space;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wuguohao on 17/3/15.
 */

public class MsgListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    ArrayList<MessageBean> mMsgDatas = new ArrayList<>();

    public MsgListAdapter(Context context, ArrayList<MessageBean> datas) {
        this.context = context;
        this.mMsgDatas = datas;
        inflater = ((Activity) context).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mMsgDatas != null ? mMsgDatas.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mMsgDatas != null ? mMsgDatas.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MsgListAdapter.ViewHolder holder = null;
        if (view == null) {
            holder = new MsgListAdapter.ViewHolder();
            view = inflater.inflate(R.layout.item_msg, null);

            holder.textView = (TextView) view.findViewById(R.id.msg_item);
            holder.right = (Space) view.findViewById(R.id.right_space);
            holder.left = (Space) view.findViewById(R.id.left_space);
            view.setTag(holder);
        } else {
            holder = (MsgListAdapter.ViewHolder) view.getTag();
        }

        MessageBean mb = mMsgDatas.get(i);

        holder.textView.setText(mb.getMsg());
        if (mb.getSenderType() == MessageBean.SEND_MSG_TYPE) {
            holder.textView.setGravity(Gravity.RIGHT);
            holder.right.setVisibility(View.GONE);
            holder.left.setVisibility(View.VISIBLE);
        } else if (mb.getSenderType() == MessageBean.RECEIVE_MSG_TYPE) {
            holder.textView.setGravity(Gravity.LEFT);
            holder.right.setVisibility(View.VISIBLE);
            holder.left.setVisibility(View.GONE);
        }
        return view;
    }

    class ViewHolder {
        TextView textView;
        Space left;
        Space right;
    }
}
