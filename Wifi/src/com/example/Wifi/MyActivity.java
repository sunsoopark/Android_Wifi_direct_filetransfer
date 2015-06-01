package com.example.Wifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.*;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.example.Wifi.down.FileTranfterActivity;
import com.example.Wifi.down.ServerService;

import java.io.File;


public class MyActivity extends BaseActivity implements WifiDirectWrapper.IwifiDirectInterface {
    private static final String TAG = MyActivity.class.getSimpleName();
    private Button btn_startSVC, btn_discover, btn_discover_svc;

    private Context mContext;
    private MyAdapter mPeerAdapter;
    private ListView mListView;
    private WifiDirectWrapper mWidiWrapper;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mWidiWrapper = new WifiDirectWrapper(this, TAG, this);
        btn_startSVC = (Button) findViewById(R.id.btn_first);
        btn_discover = (Button) findViewById(R.id.btn_second);
        btn_discover_svc = (Button) findViewById(R.id.btn_third);
        mListView = (ListView) findViewById(R.id.listView);
        mPeerAdapter = new MyAdapter(this, R.layout.listview_item);
        mProgress = new ProgressDialog(this);

        mListView.setAdapter(mPeerAdapter);

        btn_startSVC.setOnClickListener(mClickListener);
        btn_discover.setOnClickListener(mClickListener);
        btn_discover_svc.setOnClickListener(mClickListener);
        mListView.setOnItemClickListener(mOnitemClickListener);

        if (getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
            showMsg("support wifi direct");
        } else {
            showMsg("not support wifi direct");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWidiWrapper.registRCV(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWidiWrapper.unRegistRCV(mContext);
    }

    public void showProgress(){
        if(mProgress == null){
            mProgress = new ProgressDialog(mContext);
            mProgress.setMessage("탐색중");
        }
        mProgress.show();
    }
    public void hideProgress(){
        if(mProgress != null){
            mProgress.hide();
        }
    }

    private PopupMenu mPopMenu;

    private void showMenu(View anchorView, final WifiP2pDevice device) {
        if (mPopMenu == null) {
            mPopMenu = new PopupMenu(mContext, anchorView);
            mPopMenu.getMenuInflater().inflate(R.menu.menu, mPopMenu.getMenu());
            mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_connect:
                            mWidiWrapper.doConnect(device);
                            break;
                        case R.id.menu_disconnect:
                            mWidiWrapper.doDisconnect();
                            break;
                        case R.id.menu_transfer:
                            doTransfer(device);
                            break;
                    }
                    return false;
                }
            });
        } else {
            mPopMenu.dismiss();
        }
        mPopMenu.show();
    }


    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showProgress();
            if (v.equals(btn_startSVC)) {
                mWidiWrapper.addLocalService();
            } else if (v.equals(btn_discover)) {
                mWidiWrapper.discoverPeer();
            } else if (v.equals(btn_discover_svc)) {
                mWidiWrapper.discoverNearService();
            }
        }
    };


    private AdapterView.OnItemClickListener mOnitemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyAdapter adapter = (MyAdapter) parent.getAdapter();
            WifiP2pDevice item = (WifiP2pDevice) adapter.getItem(position);
            showMenu(view, item);
        }

    };


    private void doTransfer(WifiP2pDevice device) {
        Intent intent = new Intent(MyActivity.this, FileTranfterActivity.class);
        intent.setAction(FileTranfterActivity.ACTION_SEND_FILE);
        intent.putExtra("device", device);
        intent.putExtra("info", mWidiWrapper.getConnectedWifiP2pInfo());
        mContext.startActivity(intent);
    }

    @Override
    public void onSucess(String actionName) {
        Log.v(TAG, actionName);
        startServer();
        hideProgress();
    }

    @Override
    public void onFailure(int reason) {
        hideProgress();
    }

    @Override
    public void onConnectError(int reason) {
        hideProgress();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        Log.v(TAG, "peea available");
        mPeerAdapter.addAll(peerList.getDeviceList());
        mPeerAdapter.notifyDataSetChanged();
    }

    private static Intent serverIntent = null;

    private void startServer() {
        if (serverIntent == null) {
            serverIntent = new Intent(MyActivity.this, ServerService.class);
            serverIntent.putExtra("port", Constants.FILE_SERVICE_PORT);
        }
        mContext.startService(serverIntent);
    }

    private void stopServer() {
        mContext.stopService(serverIntent);
    }
}
