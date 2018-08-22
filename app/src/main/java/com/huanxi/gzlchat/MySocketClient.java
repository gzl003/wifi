package com.huanxi.gzlchat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by wuguohao on 17/3/14.
 */

public class MySocketClient {
    private int mServerPort;
    private String mServerIp;
    private Socket mSocket;
    private boolean isClient = false;
    private Context mContext;
    private PrintWriter mPrintWrite;
    private InputStream mInput;

    private Thread mSocketThread;
    public static Handler mHandler;


    /**
     * 启动客户端socket
     */
    public void connectServer() {
        mSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(mServerIp, mServerPort);

                    if (mSocket != null) {
                        Message msg = Message.obtain();
                        msg.what = 14;
                        mHandler.sendMessage(msg);
                        isClient = true;
                        initOut();
                        initIn();
                    } else {
                        isClient = false;
                        Toast.makeText(mContext, "Socket网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocketThread.start();
    }


    /**
     * 给client socket设置参数
     */
    public void setClientValue(Context context, String ip, int port) {
        this.mContext = context;
        this.mServerIp = ip;
        this.mServerPort = port;
    }


    /**
     *
     * */
    public void initOut() {
        try {
            mPrintWrite = new PrintWriter(mSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * */
    private void initIn() {
        while (isClient) {
            String str = null;
            try {
                mInput = mSocket.getInputStream();
                byte[] bt = new byte[5000];
                mInput.read(bt);
                str = new String(bt, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str != null) {
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 13;
                mHandler.sendMessage(msg);
            }
        }
    }


    public void sendMessage(final String msg) {
        if (mSocket != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mPrintWrite.print(msg);
                    mPrintWrite.flush();
                }
            }).start();
        } else {
            Toast.makeText(mContext, "未连接服务器", Toast.LENGTH_SHORT).show();
            isClient = false;
        }
    }


}
