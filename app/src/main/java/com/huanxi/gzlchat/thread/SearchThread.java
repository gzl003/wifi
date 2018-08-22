package com.huanxi.gzlchat.thread;

import android.os.Handler;
import android.os.Message;

import com.huanxi.gzlchat.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *  * Created by 智光 on 2018/8/22 14:26
 *  搜索线程
 */

public class SearchThread extends Thread {
    private boolean flag = true;
    private byte[] recvDate = null;
    private byte[] sendDate = null;
    private DatagramPacket recvDP = null;
    private DatagramSocket recvDS = null;
    private DatagramSocket sendDS = null;
    private Handler mHandler;
    private StateChangeListener onStateChangeListener;
    private int state;
    private int maxDevices;//防止广播攻击，设置最大搜素数量
    public static final int STATE_INIT_FINISH = 0;
    public static final int STATE_SEND_BROADCAST = 1;
    public static final int STATE_WAITE_RESPONSE = 2;
    public static final int STATE_HANDLE_RESPONSE = 3;

    private String name;//本机名字
    private boolean server;//是否已经开启服务器

    public SearchThread(Handler handler, String name,int max) {
        recvDate = new byte[256];
        recvDP = new DatagramPacket(recvDate, 0, recvDate.length);
        mHandler = handler;
        this.name =name;
        maxDevices = max;

    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public void setOnStateChangeListener(StateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    public void run() {
        try {
            recvDS = new DatagramSocket(54000);
            sendDS = new DatagramSocket();

            changeState(STATE_INIT_FINISH);
            //发送一次广播:广播地址255.255.255.255和组播地址224.0.1.140 --  为了防止丢包，理应多次发送
            sendDate = JsonUtil.generateData(name,"broadcast","search",server);
            DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("255.255.255.255"), 53000);
            sendDS.send(sendDP);
            changeState(STATE_SEND_BROADCAST);
            sendMsg("等待接收-----");
            int curDevices = 0;//当前搜索到的设备数量
            while (flag) {
                changeState(STATE_WAITE_RESPONSE);
                recvDS.receive(recvDP);
                changeState(STATE_HANDLE_RESPONSE);
                JSONObject jsonObject = JsonUtil.parseData(recvDP.getData());
                //判断是不是本机发起的结束搜索请求
                if (jsonObject.getString(JsonUtil.DATA_TYPE).equals("stop")) {
                    sendMsg("停止搜索：" + flag);
                } else if (jsonObject.getString(JsonUtil.DATA_TYPE).equals("response")){
                    if (curDevices >= maxDevices) {break;}
                    sendMsg("收到：" + recvDP.getAddress() + ":" + recvDP.getPort() + " 发来：" + jsonObject.toString());
                    //通知ui更新
                    jsonObject.put(JsonUtil.DATA_ADDRESS, recvDP.getAddress());
                    Message msg = Message.obtain(mHandler);
                    msg.obj = jsonObject;
                    msg.what=1002;
                    mHandler.sendMessage(msg);
                    //回应
                    sendDate = JsonUtil.generateData(name,"response","response",server);
                    DatagramPacket responseDP = new DatagramPacket(sendDate, sendDate.length, recvDP.getAddress(), 53000);
                    sendDS.send(responseDP);
                    curDevices++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (recvDS != null)
                recvDS.close();
            if (sendDS != null)
                sendDS.close();
        }
    }

    private void sendMsg(String string) {
        Message msg = Message.obtain(mHandler);
        msg.obj = string;
        msg.what=1001;
        mHandler.sendMessage(msg);
    }

    public void stopSearch() {
        flag = false;
        //由于在等待接收数据包时阻塞，无法达到关闭线程效果，因此给本机发送一个消息取消阻塞状态
        //为了避免用户在UI线程调用，所以新建一个线程
        new Thread() {
            @Override
            public void run() {
                if (sendDS != null) {
                    sendDate = JsonUtil.generateData(name,"stop","search_finish",server);
                    try {
                        DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("localhost"), 54000);
                        sendDS.send(sendDP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void startSearch() {
        flag = true;
        start();
        sendMsg("开始搜索");
    }

    private void changeState(int state) {
        this.state = state;
        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChanged(this.state);
        }
    }

    public interface StateChangeListener {
        void onStateChanged(int state);
    }
}
