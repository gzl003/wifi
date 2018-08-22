package com.huanxi.gzlchat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  * Created by 智光 on 2018/8/22 15:23
 *  
 */
public class DeviceAdapter extends BaseAdapter {

    LayoutInflater inflater;
    private Context mContext;
    private List<JSONObject> list = new ArrayList<>();

    public DeviceAdapter(Context mContext) {
        this.mContext = mContext;
        inflater = ((Activity) mContext).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.device_item, null);

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.ip = (TextView) convertView.findViewById(R.id.ip);
            holder.group = (TextView) convertView.findViewById(R.id.group);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        JSONObject jsonObject = list.get(position);
        try {
            holder.name.setText(jsonObject.getString(JsonUtil.DATA_NAME));
            holder.ip.setText(jsonObject.getString(JsonUtil.DATA_ADDRESS));
            holder.group.setText(jsonObject.getString(JsonUtil.DATA_SERVER));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    static class ViewHolder {
        public TextView name;
        public TextView ip;
        public TextView group;
    }

    public void addDevice(JSONObject jsonObject) {
        list.add(jsonObject);
        notifyDataSetChanged();
    }

    public void clearDevices() {
        list.clear();
        notifyDataSetChanged();
    }
}
