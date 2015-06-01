package com.example.Wifi;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sunsoo on 2015-05-12.
 */


public class MyAdapter extends ArrayAdapter {

    public class viewHolder {
        public TextView text_type;
        public TextView text_name;

    }

    ArrayList<WifiP2pDevice> list = new ArrayList<>();
    private Context mContext = null;

    public MyAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @Override
    public void notifyDataSetInvalidated() {
        list.clear();

        super.notifyDataSetInvalidated();
    }

    public void add(WifiP2pDevice object) {
        if (!list.contains(object)) {
            list.add(object);
        }
    }

    @Override
    public void addAll(Collection collection) {
        list.clear();
        list.addAll(collection);
    }

    @Override
    public int getCount() {
        if (list == null) {
            return -1;
        }
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder = null;
        WifiP2pDevice item = list.get(position);
        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.listview_item, null);
            holder = new viewHolder();
            holder.text_type = (TextView) convertView.findViewById(R.id.device_type);
            holder.text_name = (TextView) convertView.findViewById(R.id.device_name);
        } else {
            holder = (viewHolder) convertView.getTag();
        }
        holder.text_type.setText(getName(item.deviceName,item.status));
        holder.text_name.setText("[" + item.deviceAddress + "]");
        convertView.setTag(holder);
        return convertView;
    }
    private String getName(String name, int state){
        if(state == WifiP2pDevice.AVAILABLE){
            name += " 연결가능";
        }else if (state == WifiP2pDevice.CONNECTED){
            name += " 연결됨";
        }else if (state == WifiP2pDevice.INVITED){
            name += " 연결중";
        }else if (state == WifiP2pDevice.FAILED){
            name += " FAILED";
        }
        return name;
    }

}