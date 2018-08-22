package com.huanxi.gzlchat.thread;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *  * Created by 智光 on 2018/8/22 15:17
 *  
 */

public class TCPClientThread extends Thread {
    private String mAddr;
    private int mPort;
    private PrintWriter writer;
    private Handler mHandler;
    private boolean isRun;
    public TCPClientThread(String address ,String port,Handler handler){
        mAddr =address;
        mPort = Integer.valueOf(port);
        mHandler = handler;
    }

    @Override
    public void run() {
        isRun = true;
        try {
            boolean flag = true;
            Socket socket = new Socket(mAddr, mPort);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(os,"UTF-8"), true);
            while (flag) {
                String  content = reader.readLine();//阻塞接收消息
                if (content==null){
                    //已经断开链接 应通知UI
                    break;
                }
                //将消息通知UI
                Message msg = Message.obtain();
                msg.obj =new String[]{content,"/"+mAddr};
                msg.what=10010;
                mHandler.sendMessage(msg);
            }
            reader.close();
            writer.close();
            socket.close();

        } catch (UnknownHostException e) {
            isRun=false;
            e.printStackTrace();
            Message message =Message.obtain();
            message.what=1009;
            message.obj = "客户端异常:"+e.getMessage();
            mHandler.sendMessage(message);
        } catch (IOException e) {
            isRun=false;
            e.printStackTrace();
            Message message =Message.obtain();
            message.what=1009;
            message.obj = "客户端异常:"+e.getMessage();
        }
    }

    @Override
    public synchronized void start() {
        if (!isRun) {
            super.start();
        }
    }

    //发送消息
    public void sendMessage(final String msg){
        new Thread(){
            @Override
            public void run() {
                if (writer!=null){
                    writer.println(msg);
                    writer.flush();
                }
            }
        }.start();
    }
}
