package com.breo.breoz.ble.msg;

import android.util.Log;

import com.breo.breoz.ble.msg.base.BaseSensorMsg;
import com.breo.breoz.ble.msg.base.PackageMsg;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;



/**
 * 创建者： gtt
 * 创建时间：2017/4/27
 * 说明：
 */
public class MsgParser {
    public static PackageMsg parseMessage(byte[] data) {
        PackageMsg packageMsg = null;
        Log.i("ppp","data="+ Arrays.toString(data));

        try {
            //如果是iseem且是69包且长度超过18,说明是粘包
            if (data != null && (data[2] == BaseSensorMsg.SYS_ISEEM_SENSOR ||
                    data[2] == BaseSensorMsg.SYS_IDREAM_SENSOR) && data[6] == 0x69 && data.length > 18) {
                if (data[18] == (byte) 0xAA && data[19] == (byte) 0xBB) {
                    int songNameLength = (int) data[23];
                    int musicianLength = (int) data[24];
                    byte[] songNameBytes = new byte[songNameLength];
                    byte[] musicianBytes = new byte[musicianLength];
                    for (int i = 0; i < songNameLength; i++) {
                        songNameBytes[i] = data[25 + i];
                    }
                    for (int j = 0; j < musicianLength; j++) {
                        musicianBytes[j] = data[25 + songNameLength + j];
                    }
                    try {
                        String songName = new String(songNameBytes, "UTF-8");
                        String musician = new String(musicianBytes, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }


            if (data != null && data.length >= 9) {
                byte[] localData = Protocol.unescapeMsg(data, 1, data.length - 3);
                //判断是不是设备上报
                if (localData[2] == PackageMsg.COMM_READ || localData[2] == PackageMsg.COMM_UPLOAD) {
                    switch (localData[1]) {

                        default:
                            return null;
                    }


                }
            }
            return packageMsg;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("ppp",e.toString());
            return null;
        }
    }




}
