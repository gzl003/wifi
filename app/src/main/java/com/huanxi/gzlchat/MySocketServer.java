package com.huanxi.gzlchat;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wuguohao on 17/3/14.
 */

public class MySocketServer {
    private ServerSocket mServerSocket = null;
    private Socket mSocket = null;
    private InputStream mInputStream = null;
    public static Handler mServerHandler;

    public MySocketServer(int port) {
        try {
            mServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSocketServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = mServerSocket.accept();

                    Message msg = Message.obtain();
                    msg.what = 11;
                    msg.obj = mSocket.getRemoteSocketAddress();
                    mServerHandler.sendMessage(msg);

                    try {
                        mInputStream = mSocket.getInputStream();

                        while (!mSocket.isClosed()) {
                            byte[] bt = new byte[5000];
                            mInputStream.read(bt);
                            String str = new String(bt, "UTF-8");
                            if (str != null && !str.equals("exit")) {
                                returnMessage(str);
                            } else if (str == null || str.equals("exit")) {
                                break;
                            }
                            System.out.println(str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        mSocket.isClosed();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    mSocket.isClosed();
                }
            }
        }).start();
    }


    public void returnMessage(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        msg.what = 12;
        mServerHandler.sendMessage(msg);
    }


    public void sendMessage(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter out = new PrintWriter(mSocket.getOutputStream());
                    out.print(str);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
