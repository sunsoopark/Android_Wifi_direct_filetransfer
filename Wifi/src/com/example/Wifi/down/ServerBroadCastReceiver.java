package com.example.Wifi.down;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by sunsoo on 2015-05-21.
 */
public class ServerBroadCastReceiver extends BroadcastReceiver {
    IFileTransfer mCallback;
    public ServerBroadCastReceiver(IFileTransfer callback) {
        super();
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
        if(action.equals(ServerService.SERVER_DOWNLOAD_SERVICE)){
        }
    }
}
