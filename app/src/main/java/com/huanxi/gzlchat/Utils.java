package com.huanxi.gzlchat;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by wuguohao on 17/3/14.
 */

public class Utils {
    /**
     * 获取本机wifi IP地址
     */
    public static String getWifiIp(Context context) {
        String wifiStr = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()
                && wifiManager.getWifiState() == wifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                int ipAddress = wifiInfo.getIpAddress();
                if (ipAddress == 0) {
                    wifiStr = "";
                } else {
                    wifiStr = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff)
                            + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
                }
            }
        }
        return wifiStr;
    }
}
