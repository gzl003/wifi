package com.huanxi.gzlchat;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  * Created by 智光 on 2018/8/22 15:01
 *  
 */

public class JsonUtil {

    public static final String DATA_NAME = "name";
    public static final String DATA_TYPE = "type";
    public static final String DATA_MESSAGE = "msg";
    public static final String DATA_SERVER = "server";
    public static final String DATA_ADDRESS = "address";

    public static JSONObject parseData(byte[] date) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new String(date));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    @NonNull
    public static byte[] generateData(String name, String type, String msg, boolean server) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DATA_NAME, name);
            jsonObject.put(DATA_TYPE, type);
            jsonObject.put(DATA_MESSAGE, msg);
            jsonObject.put(DATA_SERVER, server);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString().getBytes();
    }

}
