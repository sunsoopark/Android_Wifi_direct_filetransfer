package com.example.Wifi.down;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.example.Wifi.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sunsoo on 2015-05-21.
 */
public class ServerService extends IntentService {
    public static final String SERVER_DOWNLOAD_SERVICE = "action.SERVER_DOWNLOAD_SERVICE";
    private boolean serviceEnabled = false;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public ServerService() {
        super("serversevice");
        serviceEnabled = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("test", "server SErvice ");
        String filePath = intent.getStringExtra("filePath");
        String fileName = intent.getStringExtra("fileName");
        ServerSocket welcomeSocket = null;
        Socket socket = null;
        InputStream is = null;
        InputStreamReader isr = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Intent sendIntent = new Intent();

        try {


            welcomeSocket = new ServerSocket(8988);

            while (true && serviceEnabled) {

                //Listen for incoming connections on specified port
                //Block thread until someone connects
                socket = welcomeSocket.accept();
                if(socket.isConnected()){
                    break;
                }
            }

            while (socket.isConnected() && serviceEnabled) {


                //signalActivity("TCP Connection Established: " + socket.toString() + " Starting file transfer");


                is = socket.getInputStream();
                isr = new InputStreamReader(is);

                File file = new File(filePath, fileName);

                byte[] buffer = new byte[4096];
                int bytesRead;

                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                sendIntent.setAction(SERVER_DOWNLOAD_SERVICE);
                sendIntent.putExtra("status",Constants.START_DOWNLOAD);
                sendIntent.putExtra("total",file.length());
                sendBroadcast(sendIntent);

                while (true) {
                    bytesRead = is.read(buffer, 0, buffer.length);
                    if (bytesRead == -1) {
                        break;
                    }
                    bos.write(buffer, 0, bytesRead);
                    bos.flush();
                    sendIntent.putExtra("status",Constants.DOWNLOADING);
                    sendIntent.putExtra("read",bytesRead);
                    sendOrderedBroadcast(sendIntent, null);
                    sendBroadcast(sendIntent);
                }

                bos.close();
                socket.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
            sendIntent.putExtra("status",Constants.FAIL_DOWNLOADING);
            sendOrderedBroadcast(sendIntent, null);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendIntent.putExtra("status",Constants.STOP_DOWNLOAD);
        sendOrderedBroadcast(sendIntent, null);
    }
}
