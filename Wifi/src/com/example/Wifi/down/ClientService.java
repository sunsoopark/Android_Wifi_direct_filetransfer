package com.example.Wifi.down;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;
import com.example.Wifi.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by sunsoo on 2015-05-21.
 */
public class ClientService extends IntentService {
    public static final String CLIENT_DOWNLOAD_SERVICE = "action.CLIENT_DOWNLOAD_SERVICE";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public ClientService() {
        super("client service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("test", "client SErvice ");
        String filePath = intent.getStringExtra("filePath");
        String fileName = intent.getStringExtra("fileName");
        Socket socket = new Socket();
        byte buf[] = new byte[1024];
        int len = 0;
        WifiP2pInfo wifiInfo = (WifiP2pInfo) intent.getParcelableExtra("host");
        InetAddress targetIP = null;
        Intent sendIntent = new Intent();
        sendIntent.setAction(CLIENT_DOWNLOAD_SERVICE);
        if (!wifiInfo.isGroupOwner) {
            //targetDevice.
            targetIP = wifiInfo.groupOwnerAddress;
        }
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
            socket.connect(new InetSocketAddress(targetIP, 8988), 5000);

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            OutputStream outputStream = socket.getOutputStream();
            File file = new File(filePath);
            FileInputStream fi = new FileInputStream(file);
            sendIntent.putExtra("status",Constants.START_DOWNLOAD);
            sendIntent.putExtra("total",file.length());
            sendBroadcast(sendIntent);

            while ((len = fi.read(buf)) != -1) {
                outputStream.write(buf, 0, len);

                sendIntent.putExtra("status",Constants.DOWNLOADING);
                sendIntent.putExtra("read",len);
                sendBroadcast(sendIntent);
            }
            outputStream.close();
            fi.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }

/**
 * Clean up any open sockets when done
 * transferring or if an exception occurred.
 */ finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
        sendIntent.putExtra("status",Constants.STOP_DOWNLOAD);
        sendBroadcast(sendIntent);
    }
}
