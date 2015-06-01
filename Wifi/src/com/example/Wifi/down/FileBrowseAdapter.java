package com.example.Wifi.down;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.Wifi.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sunsoo on 2015-05-12.
 */


public class FileBrowseAdapter extends ArrayAdapter {

    public class viewHolder {
        public TextView text_type;
        public TextView text_name;

    }

    ArrayList<Object> list = new ArrayList<>();
    private Context mContext = null;

    public FileBrowseAdapter(Context context, int resource) {
        super(context, resource);
        this.mContext = context;
    }

    @Override
    public void notifyDataSetInvalidated() {
        list.clear();

        super.notifyDataSetInvalidated();
    }

    public void addAll(File[] collection) {
        list.clear();
        for (File file : collection) {
            list.add(file);
        }
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
        File file = (File) list.get(position);
        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.listview_item_horizontal, null);
            holder = new viewHolder();
            holder.text_type = (TextView) convertView.findViewById(R.id.device_type);
            holder.text_name = (TextView) convertView.findViewById(R.id.device_name);
        } else {
            holder = (viewHolder) convertView.getTag();
        }
        holder.text_type.setText(file.isDirectory() ? "디렉토리" : "파일");
        holder.text_name.setText("[" + file.getName() + "]");
        convertView.setTag(holder);
        return convertView;
    }
}