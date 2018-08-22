package com.huanxi.gzlchat;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huanxi.gzlchat.thread.ConnectThread;
import com.huanxi.gzlchat.thread.ResponseThread;
import com.huanxi.gzlchat.thread.SearchThread;
import com.huanxi.gzlchat.thread.TCPClientThread;
import com.huanxi.gzlchat.thread.TCPServerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索WiFi内的设备IP
 * <p>
 * 参考文档
 * https://blog.csdn.net/qq_36009027/article/details/76572681
 * https://blog.csdn.net/qq_36009027/article/details/76685975
 */
public class SerchepActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout logContainer;
    private SearchThread searchThread;//搜索线程
    private ResponseThread responseThread;//响应线程
    private boolean in_searching, in_response;//是否正在搜索,已经上线
    private DeviceAdapter adapter;//
    private ListView devList;//显示设备列表
    private List<JSONObject> mList;//搜索设出来的区域内设备列表
    public static final String PORT = "45403";//约定端口号
    private TCPServerThread mServerThread;//服务端线程
    private TCPClientThread mClientThread;//客户端线程
    private boolean isClient;//当前是客户端还是服务端
    private String curClientAddress = "";//当前聊天的客户端
    private ScrollView scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serchep);
        initUI();
        initList();

    }

    private void initList() {
        //提前创建服务端 但是不开启
        mServerThread = new TCPServerThread(PORT, mHandler);

        devList = (ListView) findViewById(R.id.devices_list);
        adapter = new DeviceAdapter(this);
        devList.setAdapter(adapter);
        mList = new ArrayList<>();
        itemClick();
    }

    private void initUI() {
        logContainer = (LinearLayout) findViewById(R.id.log);
        scrollview = findViewById(R.id.scrollview);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.online).setOnClickListener(this);
        findViewById(R.id.offline).setOnClickListener(this);
        findViewById(R.id.online).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final EditText pwdEt = new EditText(SerchepActivity.this);
                //弹出输入聊天内容对话框
                new AlertDialog.Builder(SerchepActivity.this)
                        .setView(pwdEt)
                        .setTitle("请输入内容")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isClient) {
                                    showLog("客户端发消息：" + pwdEt.getText().toString());
                                    mClientThread.sendMessage(pwdEt.getText().toString());
                                } else {
                                    showLog("服务端给" + curClientAddress + "发消息：" + pwdEt.getText().toString());
                                    mServerThread.sendMessage(curClientAddress, pwdEt.getText().toString());
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();
                return true;
            }
        });
    }

    private void itemClick() {
        devList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //发起联机请求时必须关闭搜索线程，否则会占用端口
                if (in_searching) {
                    searchThread.stopSearch();
                    in_searching = false;
                }
                JSONObject jo = mList.get(position);
                try {
                    curClientAddress = jo.get(JsonUtil.DATA_ADDRESS).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //启动发起连接请求线程
                ConnectThread t = new ConnectThread(jo, "测试", PORT, mHandler);
                t.start();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message message) {
            switch (message.what) {
                case 1001://日志打印
                    showLog((String) message.obj);
                    break;
                case 1002://搜索到设备、更新列表
                    JSONObject jsonObject = (JSONObject) message.obj;
                    mList.add(jsonObject);
                    adapter.addDevice(jsonObject);
                    break;
                case 1003://对方已经开启服务，是否链接
                    final JSONObject jo0 = (JSONObject) message.obj;
                    Toast.makeText(SerchepActivity.this, "对方已经开启服务，是否链接", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(SerchepActivity.this)
                            .setTitle("对方已经创建房间，是否加入？")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("加入", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        //TODO 还应做取消服务线程操作
                                        mClientThread = new TCPClientThread(jo0.getString(JsonUtil.DATA_ADDRESS).replace("/", ""), jo0.getString(JsonUtil.DATA_MESSAGE), mHandler);
                                        mClientThread.start();
                                        isClient = true;
                                        showLog("建立客户端成功");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .create().show();
                    break;
                case 1004://收到对方回应，正在建立TCP
                    Toast.makeText(SerchepActivity.this, "对方已经回应，正在建立TCP", Toast.LENGTH_SHORT).show();
                    break;
                case 1005://收到连接邀请，是否同意
                    final JSONObject jo1 = (JSONObject) message.obj;
                    Toast.makeText(SerchepActivity.this, "收到邀请连接请求，是否同意", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(SerchepActivity.this)
                            .setTitle("收到连接请求，是否同意加入？")
                            .setNegativeButton("拒绝", null)
                            .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showLog("发送同意请求");
                                    responseThread.sendAgreeMessage();//发送同意请求

                                    //连接到房间
                                    try {
                                        //TODO 取消服务线程
                                        mClientThread = new TCPClientThread(jo1.getString(JsonUtil.DATA_ADDRESS).replace("/", ""), jo1.getString(JsonUtil.DATA_MESSAGE), mHandler);
                                        mClientThread.start();
                                        isClient = true;
                                        showLog("已经建立客户端：" + jo1.getString(JsonUtil.DATA_ADDRESS).replace("/", ""));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        showLog("建立连接失败：" + e.getMessage());
                                    }
                                }
                            })
                            .create().show();
                    break;
                case 1006://对方没有创建房间，是否创建房间并邀请对方加入
                    Toast.makeText(SerchepActivity.this, "对方没有创建房间，已经创建房间并邀请对方加入", Toast.LENGTH_SHORT).show();
                    mServerThread.start();
                    isClient = false;
                    if (searchThread != null) {
                        searchThread.setServer(true);
                    }
                    if (responseThread != null) {
                        responseThread.setServer(true);
                    }
                    break;
                case 1007://房间创建成功
                    showLog("房间已经创建 ，等待加入..");
                    break;
                case 1008://有人加入房间
                    showLog(message.obj + " 加入房间");
                    break;
                case 1009://异常消息获取
                    showErrLog(message.obj + "");
                    break;
                case 10010://收到消息
                    String[] msg = (String[]) message.obj;
                    showLog("收到" + msg[1] + "的消息:" + msg[0]);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if (!in_searching) {
                    mList.clear();
                    adapter.clearDevices();
                    searchThread = new SearchThread(mHandler, "测试" + System.currentTimeMillis(), 20);
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
                    responseThread = new ResponseThread(mHandler, "测试" + System.currentTimeMillis());
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
        TextView tv = new TextView(SerchepActivity.this);
        tv.setText(msg);
        logContainer.addView(tv);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void showErrLog(final String msg) {
        TextView tv = new TextView(SerchepActivity.this);
        tv.setText(msg);
        tv.setTextColor(Color.RED);
        logContainer.addView(tv);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

}
