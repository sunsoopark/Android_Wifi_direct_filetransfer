package com.example.Wifi.down;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.Wifi.Constants;
import com.example.Wifi.R;

import java.io.File;

/**
 * Created by sunsoo on 2015-05-22.
 */
public class FileTranfterActivity extends Activity implements IFileTransfer {
    private TextView txtPath, txtTargetName;
    private Button btnFind, btnStart, btnStop;
    private ProgressBar mProgress;
    private RelativeLayout llProgressLayout;
    public static final String ACTION_SEND_FILE = "action.send_file";
    public static final String ACTION_RECEIVE_FILE = "action.receive_file";
    private Handler mUiUpdater = new Handler();
    private WifiP2pInfo mWifiP2pInfo = null;

    private ServerBroadCastReceiver mServerBroadCastRCV= null;
    private ClientBroadCastReceiver mClientBroadCastRCV= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        txtTargetName = (TextView) findViewById(R.id.txt_device);
        txtPath = (TextView) findViewById(R.id.txt_path);
        btnFind = (Button) findViewById(R.id.btn_find);
        btnStart = (Button) findViewById(R.id.btn_start_transfer);
        btnStop = (Button) findViewById(R.id.btn_stop_transfer);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        llProgressLayout = (RelativeLayout) findViewById(R.id.ll_transfer);

        Intent intent = getIntent();
        if (intent != null) {
            if (ACTION_RECEIVE_FILE.equals(intent.getAction())) {
                Bundle data = intent.getBundleExtra("data");
                Log.i("test","request download");
                mServerBroadCastRCV = new ServerBroadCastReceiver(this);
                registerReceiver(mServerBroadCastRCV,new IntentFilter(ServerService.SERVER_DOWNLOAD_SERVICE));
            } else if (ACTION_SEND_FILE.equals(intent.getAction())) {
                mClientBroadCastRCV = new ClientBroadCastReceiver(this);
                registerReceiver(mClientBroadCastRCV,new IntentFilter(ClientService.CLIENT_DOWNLOAD_SERVICE));
                WifiP2pDevice device = intent.getParcelableExtra("device");
                mWifiP2pInfo = intent.getParcelableExtra("info");
                if (device != null) {
                    txtTargetName.setText(device.deviceName);
                }

            }
//            else if(intent.getAction().equals("action.REQUEST_DOWNLOAD")){
//                Log.i("test","request download");
//
//            }

        }
        btnFind.setOnClickListener(mOnClickListener);
        btnStart.setOnClickListener(mOnClickListener);
        btnStop.setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mServerBroadCastRCV != null){
            unregisterReceiver(mServerBroadCastRCV);
        }
        if(mClientBroadCastRCV != null){
            unregisterReceiver(mClientBroadCastRCV);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(btnFind)) {
                showFileChooser();
            } else if (v.equals(btnStart)) {
                startDownload();
            } else if (v.equals(btnStop)) {
                stopDownload();
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.REQUEST_FILE_BROWSE){
            Log.e("text","return file " );
            if(resultCode == Activity.RESULT_OK){
                File file = (File) data.getExtras().get("path");
                if(file != null){
                    txtPath.setText(file.getAbsolutePath());
                }else {
                    Log.e("text","return file name is null" );
                }
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(FileTranfterActivity.this, FileBrowseActivity.class);
        startActivityForResult(intent, Constants.REQUEST_FILE_BROWSE);
    }

    private void updateProgress(final int current, final int total) {
        if (mProgress == null) return;
        mUiUpdater.post(new Runnable() {
            @Override
            public void run() {
                if (total != -1) {
                    mProgress.setMax(total);
                }
                mProgress.setProgress(current);
            }
        });
    }
    Intent transeIntent = null;
    private void startDownload() {
        transeIntent= new Intent(FileTranfterActivity.this, ClientService.class);
        Log.v("test","start downlaod ::"+txtPath.getText().toString());
        File file = new File(txtPath.getText().toString());
        transeIntent.putExtra("filePath",file.getAbsolutePath());
        transeIntent.putExtra("fileName",file.getName());
        transeIntent.putExtra("host", mWifiP2pInfo);
        startService(transeIntent);

    }

    private void stopDownload() {
    stopService(transeIntent);
    }

    @Override
    public void onDownloading(Bundle msg) {
        String name = (String) msg.get("name");
        int total = (int) msg.get("total");
        int current = (int) msg.get("current");
        updateProgress(current, -1);
    }

    @Override
    public void onStartDownload(Bundle msg) {
        llProgressLayout.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.GONE);
    }

    @Override
    public void onStopDownload(Bundle msg) {
        String name = (String) msg.get("name");
        int total = (int) msg.get("total");
        int current = (int) msg.get("current");
        updateProgress(current, -1);
        llProgressLayout.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDownloadFail(Bundle msg) {
        llProgressLayout.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishDownload(Bundle msg) {
        String name = (String) msg.get("name");
        int total = (int) msg.get("total");
        int current = (int) msg.get("current");
        updateProgress(current, -1);
    }
}
