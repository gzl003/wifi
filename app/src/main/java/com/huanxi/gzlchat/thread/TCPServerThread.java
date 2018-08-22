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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *  * Created by 智光 on 2018/8/22 15:18
 *  
 */

public class TCPServerThread extends Thread {
    private ServerSocket ss;
    private int port;//约定的端口
    private Map<String,SocketThread> clients;//保存加入房间的客户端
    private Handler mHandler;//用于通知UI更新
    private boolean isRun;//标记 线程是否已经运行，避免重复启动

    public TCPServerThread(String port,Handler handler) {
        this.port = Integer.valueOf(port);
        clients = new HashMap<>();
        mHandler = handler;
    }

    @Override
    public void run() {
        isRun=true;
        try {
            ss = new ServerSocket(Integer.valueOf(port));
            ss.setReuseAddress(true);//设置端口重用，避免TIME_WAIT引起Address already in use异常
            mHandler.sendEmptyMessage(1007);//通知服务已经创建成功
            for(;;) {
                Socket client = ss.accept();
                //不得重复加入
                if(clients.containsKey(client.getInetAddress().toString())){
                    sendMessage(client.getInetAddress().toString(),"您已经在房间中");
                    continue;
                }

                SocketThread st = new SocketThread(client);
                st.start();
                clients.put(client.getInetAddress().toString(),st);
                //通知某某已经加入
                Message message=Message.obtain();
                message.obj=client.getInetAddress().toString();
                message.what = 1008;
                mHandler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Message message =Message.obtain();
            message.what=1009;
            message.obj = "创建服务异常:"+e.getMessage();
            mHandler.sendMessage(message);
            isRun=false;
        }
    }

    @Override
    public synchronized void start() {
        if (!isRun) {
            super.start();
        }
    }

    //发送消息
    public void sendMessage(String address,String msg){
        if (clients.containsKey(address)){
            clients.get(address).sendMessage(msg);
        }
    }
    //移除一个已经链接的哭护短
    public void removeClient(String address){
        if(clients.containsKey(address)){
            clients.remove(address);
        }
    }


    //每一个客户新开一个线程
    class SocketThread extends Thread {
        private Socket socket;
        private boolean flag = true;
        private PrintWriter writer;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("链接成功！");
            BufferedReader reader = null;
            try {
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(os,"UTF-8"), true);
                while (flag) {
                    String content = reader.readLine();
                    if (content == null || content.equals("bye")) {
                        flag = false;
                        System.out.println(socket.getPort() + "已经断开链接");
                    } else {
                        System.out.println("收到消息：" + content);
                        //通知UI，并携带ip地址,携带ip是为了服务端判断是谁发来消息
                        Message message =Message.obtain();
                        message.obj =new String[]{content,socket.getInetAddress().toString()};
                        message.what=10010;
                        mHandler.sendMessage(message);

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(socket.getPort() + "已经断开链接");
                //应通知UI以便从集合中移除
                flag = false;

                //
                Message message =Message.obtain();
                message.what=1009;
                message.obj = "客户线程:"+e.getMessage();
                mHandler.sendMessage(message);
            } finally {
                try {
                    reader.close();
                    writer.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
}
