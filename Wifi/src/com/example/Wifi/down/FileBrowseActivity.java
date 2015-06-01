package com.example.Wifi.down;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.Wifi.BaseActivity;
import com.example.Wifi.R;

import java.io.File;

/**
 * Created by sunsoo on 2015-05-26.
 */
public class FileBrowseActivity extends BaseActivity {

    private TextView txtPath;
    private ListView mDirList;
    private Button mMoveUp;
    private FileBrowseAdapter mDirListAdapter;
    private Context mContext;
    private File mCurrentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        mContext = this;
        txtPath = (TextView) findViewById(R.id.txt_dir_path);
        mDirList = (ListView) findViewById(R.id.file_list);
        mMoveUp = (Button) findViewById(R.id.up);
        mDirListAdapter = new FileBrowseAdapter(this, R.layout.listview_item_horizontal);
        mDirList.setAdapter(mDirListAdapter);
        mDirList.setOnItemClickListener(mOnItemClickListener);
        mMoveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDirInfo(mCurrentPath == null?null : new File(mCurrentPath.getParent()));
            }
        });
        initDirInfo(null);
    }

    private void initDirInfo(File root) {
        if (isExternalStorageReadable()) {
            mCurrentPath = root;
            File path = null;
            if (root == null) {
                path = Environment.getExternalStorageDirectory();
                mMoveUp.setEnabled(false);
            } else {
                path = root;
                mMoveUp.setEnabled(true);
            }
            String rootName = path.getAbsolutePath();
            txtPath.setText(rootName);
            File[] files = path.listFiles();
            mDirListAdapter.addAll(files);
            mDirListAdapter.notifyDataSetChanged();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           final File file = (File) mDirListAdapter.getItem(position);
            boolean isDir = file.isDirectory();
            if (isDir) {
                initDirInfo(file);
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle("파일전송").setMessage(file.getName() + " 전송 하시겠습니까?")
                        .setPositiveButton("전송", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("path",file.getAbsoluteFile());
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }
                        }).setNegativeButton("취소", null).show();
            }
        }
    };
}
