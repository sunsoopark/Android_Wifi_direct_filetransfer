package com.example.Wifi.down;

import android.os.Bundle;

/**
 * Created by sunsoo on 2015-05-26.
 */
public interface IFileTransfer {

    void onDownloading(Bundle msg);
    void onStartDownload(Bundle msg);
    void onStopDownload(Bundle msg);
    void onDownloadFail(Bundle msg);
    void onFinishDownload(Bundle msg);

}
