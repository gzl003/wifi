package com.huanxi.gzlchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;



import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wuguohao on 17/3/14.
 */

public class ClientActivity extends AppCompatActivity {
    @BindView(R.id.server_ip)
    EditText mServerIpEt;
    @BindView(R.id.server_port)
    EditText mServerPortEt;
    @BindView(R.id.connect)
    Button mConnectBtn;
    @BindView(R.id.content_panel_lv)
    ListView mContentPanelLv;
    @BindView(R.id.input_box_et)
    EditText mInputBoxEt;
    @BindView(R.id.send_msg_btn)
    ImageButton mSendMsgBtn;

    @BindView(R.id.set_layout)
    View mSetLayout;
    @BindView(R.id.msg_layout)
    View mMsgLayout;

    MySocketClient mSocketClient;
    ArrayList<MessageBean> mMsgDatas = new ArrayList<>();
    MsgListAdapter mListAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ButterKnife.bind(this);

        initView();
    }

    void initView() {
        mSocketClient = new MySocketClient();
        mListAdapter = new MsgListAdapter(this, mMsgDatas);
        mContentPanelLv.setAdapter(mListAdapter);
        mContentPanelLv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipString = mServerIpEt.getText().toString();
                String portString = mServerPortEt.getText().toString();
                if (ipString == null || ipString.equals("")) {
                    Toast.makeText(ClientActivity.this, "请输入服务器IP地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (portString == null || portString.equals("")) {
                    Toast.makeText(ClientActivity.this, "请输入服务器端口号", Toast.LENGTH_SHORT).show();
                    return;
                }

                mSocketClient.setClientValue(ClientActivity.this, ipString, Integer.parseInt(portString));
                mSocketClient.connectServer();
            }
        });

        mSendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mInputBoxEt.getText().toString();
                if (msg == null || msg.equals("")) {
                    Toast.makeText(ClientActivity.this, "请输入你要发送的内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSocketClient.sendMessage(msg);
                mMsgDatas.add(new MessageBean(msg, MessageBean.SEND_MSG_TYPE));
                mListAdapter.notifyDataSetChanged();
            }
        });

        MySocketClient.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 13:
                        mMsgDatas.add(new MessageBean(msg.obj.toString(), MessageBean.RECEIVE_MSG_TYPE));
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case 14:
                        mSetLayout.setVisibility(View.INVISIBLE);
                        mMsgLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(ClientActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        break;
                    default:

                        break;
                }
            }
        };

    }
}
