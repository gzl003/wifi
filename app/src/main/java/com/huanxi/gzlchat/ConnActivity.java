package com.huanxi.gzlchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huanxi.gzlchat.thread2.ResponseThread;
import com.huanxi.gzlchat.thread2.SearchThread;

public class ConnActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout logContainer;
    private SearchThread searchThread;
    private ResponseThread responseThread;
    private boolean in_searching, in_response;
    private TextView device_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conn);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.online).setOnClickListener(this);
        findViewById(R.id.offline).setOnClickListener(this);
        logContainer = (LinearLayout) findViewById(R.id.log);
        device_ip = findViewById(R.id.device_ip);
        device_ip.setText(Utils.getWifiIp(this));

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showLog((String) msg.obj);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if (!in_searching) {
                    searchThread = new SearchThread(mHandler, 20);
                    searchThread.startSearch();
                    in_searching = true;
                } else {
                    Toast.makeText(this, "线程已经启动", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stop:
                if (in_searching) {
                    searchThread.stopSearch();
                    in_searching = false;
                } else {
                    Toast.makeText(this, "线程未启动", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.online:
                if (!in_response) {
                    responseThread = new ResponseThread(mHandler);
                    responseThread.startResponse();
                    in_response = true;
                } else {
                    Toast.makeText(this, "线程已经启动", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.offline:
                if (in_response) {
                    responseThread.stopResponse();
                    in_response = false;
                } else {
                    Toast.makeText(this, "线程未启动", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void showLog(final String msg) {
        TextView tv = new TextView(ConnActivity.this);
        tv.setText(msg);
        logContainer.addView(tv);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
