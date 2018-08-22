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
 *  * Created by 智光 on 2018/8/22 15:16
 *  
 */

public class ConnectThread extends Thread {
    private JSONObject mJson;
    private byte[] recvDate = null;
    private byte[] sendDate = null;
    private DatagramPacket recvDP;
    private DatagramSocket recvDS = null;
    private DatagramSocket sendDS = null;
    private String name;
    private String port;//假如创建房间，则作为房间服务端口
    private Handler mHandler;

    public ConnectThread(JSONObject json, String name, String port, Handler handler) {
        recvDate = new byte[256];
        recvDP = new DatagramPacket(recvDate, 0, recvDate.length);
        mHandler = handler;
        this.name = name;
        this.port = port;
        mJson = json;
    }

    @Override
    public void run() {
        try {
            if (mJson.getBoolean(JsonUtil.DATA_SERVER)) {
                Message message = Message.obtain();
                message.what = 1003;
                message.obj = mJson;
                mHandler.sendMessage(message);

            } else {
                mHandler.sendEmptyMessage(1006);
                recvDS = new DatagramSocket(54000);
                sendDS = new DatagramSocket();
                recvDS.setSoTimeout(5000);//超时5秒
                sendDate = JsonUtil.generateData(name, "connect", port, true);
                DatagramPacket sendDP = new DatagramPacket(sendDate, sendDate.length, (InetAddress) mJson.get(JsonUtil.DATA_ADDRESS), 53000);
                sendDS.send(sendDP);
                //等待回应
                recvDS.receive(recvDP);
                //收到回应
                mHandler.sendEmptyMessage(1004);
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
}
