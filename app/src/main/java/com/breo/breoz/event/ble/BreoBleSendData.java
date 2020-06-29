package com.breo.breoz.event.ble;

import androidx.annotation.NonNull;

import com.breo.baseble.utils.HexUtil;


/**
 * Created by chenqc on 2019/10/28 19:12
 */
public class BreoBleSendData {
    private byte[] sendData;

    public BreoBleSendData(byte[] sendData) {
        this.sendData = sendData;
    }

    public byte[] getSendData() {
        return sendData;
    }

    public void setSendData(byte[] sendData) {
        this.sendData = sendData;
    }

    @NonNull
    @Override
    public String toString() {
        return "BreoBleSendData="+ HexUtil.toHexString(sendData);
    }
}
