package com.huanxi.gzlchat;

/**
 * Created by wuguohao on 17/3/15.
 */

public class MessageBean {
    public static final int SEND_MSG_TYPE = 1;
    public static final int RECEIVE_MSG_TYPE = 2;

    private String msg;
    private int senderType;

    public MessageBean(String msg, int senderType) {
        this.msg = msg;
        this.senderType = senderType;
    }

    public String getMsg() {
        return msg;
    }

    public int getSenderType() {
        return senderType;
    }

}
