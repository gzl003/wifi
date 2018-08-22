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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wuguohao on 17/3/14.
 */

public class ServerActivity extends AppCompatActivity {
    @BindView(R.id.server_ip)
    TextView mServerIpTv;
    @BindView(R.id.client_ip)
    TextView mClientIpTv;
    @BindView(R.id.port)
    EditText mPortEt;
    @BindView(R.id.confirm)
    Button mConfirmBtn;
    @BindView(R.id.content_panel_lv)
    ListView mContentPanelLv;
    @BindView(R.id.input_box_et)
    EditText mInputBoxEt;
    @BindView(R.id.send_message_btn)
    ImageButton mSendMsgBtn;

    @BindView(R.id.set_layout)
    View mSetLayout;
    @BindView(R.id.msg_layout)
    View mMsgLayout;

    private int mPort;
    private MySocketServer mSocketServer = null;
    private ArrayList<MessageBean> mMsgDatas = new ArrayList<>();
    private MsgListAdapter mListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.bind(this);

        initView();
    }

    void initView() {
        mServerIpTv.setText(Utils.getWifiIp(this));
        mListAdapter = new MsgListAdapter(this, mMsgDatas);
        mContentPanelLv.setAdapter(mListAdapter);
        mContentPanelLv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portStr = mPortEt.getText().toString();
                if (portStr == null || portStr.equals("")) {
                    Toast.makeText(ServerActivity.this, "请输入端口号", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPort = Integer.parseInt(portStr);
                mSocketServer = new MySocketServer(mPort);
                mSocketServer.startSocketServer();

                mServerIpTv.setText(Utils.getWifiIp(ServerActivity.this) + ":" + mPort);
                mSetLayout.setVisibility(View.INVISIBLE);
            }
        });


        mSendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgStr = mInputBoxEt.getText().toString();
                if (msgStr == null || msgStr.equals("")) {
                    Toast.makeText(ServerActivity.this, "请输入你想发送的内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSocketServer.sendMessage(msgStr);
                mMsgDatas.add(new MessageBean(msgStr, MessageBean.SEND_MSG_TYPE));
                mListAdapter.notifyDataSetChanged();
            }
        });


        MySocketServer.mServerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 11:
                        mMsgLayout.setVisibility(View.VISIBLE);
                        mClientIpTv.setText(msg.obj.toString());
                        break;
                    case 12:
                        mMsgDatas.add(new MessageBean(msg.obj.toString(), MessageBean.RECEIVE_MSG_TYPE));
                        mListAdapter.notifyDataSetChanged();
                        break;
                    default:

                        break;
                }
            }
        };
    }

}
