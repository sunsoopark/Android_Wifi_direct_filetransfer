package com.example.Wifi;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by sunsoo on 2015-05-22.
 */
public class BaseActivity extends Activity {

    private static Toast msgToast;

    void showMsg(String msg) {
        if (msgToast == null) {
            msgToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        } else {
            msgToast.setText(msg);
        }
        msgToast.show();
    }
}
