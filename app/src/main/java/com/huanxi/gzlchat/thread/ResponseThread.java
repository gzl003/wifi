package com.huanxi.gzlchat.thread;

import android.os.Handler;
import android.os.Message;

import com.huanxi.gzlchat.JsonUtil;
import com.huanxi.gzlchat.SerchepActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *  * Created by 智光 on 2018/8/22 14:26
 *  搜索响应线程
 */
public class ResponseThread extends Thread {
    private byte[] recvDate = null;
    private byte[] sendDate = null;
    private DatagramPacket recvDP;
    private DatagramSocket recvDS = null;
    private DatagramSocket sendDS = null;
    private boolean flag = true;
    private Handler mHandler;
    private String name;//本机名字
    private boolean server;//是否已经开启服务器

    public ResponseThread(Handler handler,String name) {
        recvDate = new byte[256];
        recvDP = new DatagramPacket(recvDate, 0, recvDate.length);
        mHandler = handler;
        this.name=name;
    }
    public void setServer(boolean server) {
        this.server = server;
    }

    public void run() {
        try {
            sendMsg("设备已经开启，等待其他设备搜索...");
            recvDS = new DatagramSocket(53000);
            sendDS = new DatagramSocket();
            while (flag) {
                recvDS.receive(recvDP);
                JSONObject jsonObject=JsonUtil.parseData(recvDP.getData());
                if (jsonObject.getString(JsonUtil.DATA_TYPE).equals("response")) {
                    sendMsg("确认收到回应");
                } else if (jsonObject.getString(JsonUtil.DATA_TYPE).equals("stop")) {
                    sendMsg("下线：" + flag);
                } else if (jsonObject.getString(JsonUtil.DATA_TYPE).equals("broadcast")){
                    sendMsg("收到：" + recvDP.getAddress() + ":" + recvDP.getPort() + " 发来搜索请求：" + jsonObject.toString());
                    sendDate = JsonUtil.generateData(name,"response",server? SerchepActivity.PORT:"received",server);
                    sendMsg("回应>>");
                    DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, recvDP.getAddress(), 54000);
                    sendDS.send(sendDP);
                }else if(jsonObject.getString(JsonUtil.DATA_TYPE).equals("connect")){
                    sendMsg("收到连接请求");
                    jsonObject.put(JsonUtil.DATA_ADDRESS, recvDP.getAddress());
                    Message msg = Message.obtain();
                    msg.obj = jsonObject;
                    msg.what=1005;
                    mHandler.sendMessage(msg);

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

    public void startResponse() {
        flag = true;
        start();
        sendMsg("上线");
    }

    public void stopResponse() {
        flag = false;
        //为了避免用户在UI线程调用，所以新建一个线程
        new Thread() {
            @Override
            public void run() {
                if (sendDS != null) {
                    sendDate = JsonUtil.generateData(name,"stop","stop_resp",server);
                    try {
                        DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, InetAddress.getByName("localhost"), 53000);
                        sendDS.send(sendDP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }
    //发送同意加入信号
    public void sendAgreeMessage() {
        //为了避免用户在UI线程调用，所以新建一个线程
        new Thread() {
            @Override
            public void run() {
                if (sendDS != null) {
                    sendDate = JsonUtil.generateData(name,"agree","agree_resp",server);
                    try {
                        DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, recvDP.getAddress(), 54000);
                        sendDS.send(sendDP);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
