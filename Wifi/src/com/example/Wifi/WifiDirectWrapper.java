package com.example.Wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunsoo on 2015-05-22.
 */
public class WifiDirectWrapper {
    private static String TAG = "";

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private Context mContext;
    private IwifiDirectInterface mCallback;

    public interface IwifiDirectInterface {
        void onSucess(String actionName);

        void onFailure(int reason);

        void onConnectError(int reason);

        void onPeersAvailable(WifiP2pDeviceList peerList);

    }

    public WifiDirectWrapper(Context context, String tagName, IwifiDirectInterface callBack) {
        this.mContext = context;
        this.TAG = tagName;
        mCallback = callBack;
        initWifiSVC();
        requestPeers();
    }


    public void registRCV(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        context.registerReceiver(wifiP2PReceiver, intentFilter);

    }

    public void unRegistRCV(Context context) {
        context.unregisterReceiver(wifiP2PReceiver);
    }

    private void initWifiSVC() {
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), null);
    }

    public void addLocalService() {
        Log.v(TAG, "addLocalService");
        Map record = new HashMap<String, String>();
        record.put("listenport", Constants.FILE_SERVICE_PORT);
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");


        WifiP2pServiceInfo info = WifiP2pDnsSdServiceInfo.newInstance("_tester", "ip_tcp", record);
        mWifiP2pManager.addLocalService(p2pChannel, info, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mCallback.onSucess("addLocalService success");
                Log.v(TAG, "success");
            }

            @Override
            public void onFailure(int reason) {
                mCallback.onFailure(reason);
                Log.v(TAG, "fail ::" + reason);
            }
        });

    }

    public void discoverPeer() {
        Log.v(TAG, "discoverPeers");
        mWifiP2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "discover success");
                mCallback.onSucess("discover peer success");
                requestPeers();
            }

            @Override
            public void onFailure(int reason) {
                mCallback.onFailure(reason);
                Log.i(TAG, "discover fail >>" + reason);
            }
        });
    }

    final HashMap<String, String> buddies = new HashMap<String, String>();

    public void discoverNearService() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            ((BaseActivity) mContext).showMsg("서비스 검색중");
            return;
        }
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(device.deviceAddress, (String) record.get("buddyname"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                Log.d(TAG, "service res available [" + instanceName + "/type:" + registrationType);
            }
        };
        mWifiP2pManager.setDnsSdResponseListeners(p2pChannel, serviceResponseListener, txtListener);
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mWifiP2pManager.addServiceRequest(p2pChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mCallback.onSucess("add service success");
                        Log.v(TAG, "sevice success");
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                        Log.v(TAG, "sevice fail :: " + code);
                        mCallback.onFailure(code);
                    }
                });

        mWifiP2pManager.discoverServices(p2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mCallback.onSucess("discover service success");
            }

            @Override
            public void onFailure(int reason) {
                mCallback.onFailure(reason);
            }
        });

    }

    private WifiP2pInfo mWifiP2pInfo = null;

    public WifiP2pInfo getConnectedWifiP2pInfo() {
        return mWifiP2pInfo;
    }

    BroadcastReceiver wifiP2PReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == -1) return;
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.i(TAG, "WIFI_P2P_STATE_ENABLED");
                    requestPeers();
                } else {
                    Log.i(TAG, "WIFI_P2P_STATE_DISABLED");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.i(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
                //do request peer
                requestPeers();
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (netInfo != null && netInfo.isConnected()) {
                    Log.i(TAG, "device connected:: " + netInfo.isConnected());
                    mWifiP2pManager.requestConnectionInfo(p2pChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            mWifiP2pInfo = info;
                        }
                    });
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.i(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.i(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            }
        }
    };

    public void requestPeers() {
        mWifiP2pManager.requestPeers(p2pChannel, peerListListener);
    }


    public void doConnect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (device.status == WifiP2pDevice.CONNECTED) {
            Log.v(TAG, "disconnect");

            return;
        }
        mWifiP2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "connect success");
                mCallback.onSucess("connect success");
                requestPeers();
            }

            @Override
            public void onFailure(int reason) {
                Log.v(TAG, "connect fail >>" + reason);
            }
        });
    }

    public void doDisconnect() {
        mWifiP2pManager.removeGroup(p2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                requestPeers();
                mCallback.onSucess("disconnect success");
            }

            @Override
            public void onFailure(int reason) {
                requestPeers();
            }
        });
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            mCallback.onPeersAvailable(peerList);
        }
    };


}
