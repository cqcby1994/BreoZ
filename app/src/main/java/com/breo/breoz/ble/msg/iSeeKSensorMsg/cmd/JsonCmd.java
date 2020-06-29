package com.breo.breoz.ble.msg.iSeeKSensorMsg.cmd;


import com.breo.breoz.ble.msg.EndianUtil;
import com.breo.breoz.ble.msg.iSeeKSensorMsg.BaseISeeKCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by chenqc on 2019/11/7 14:41
 */
public class JsonCmd extends BaseISeeKCmdMsg {
    public byte[] jsonData;

    /**
     * //命令体ID
     */
    public JsonCmd() {
        super(JSON);
    }

    //    int big = (value & 0xFF00) >> 8;
//4 int little = value & 0xFF;
    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) ((jsonData.length & 0xFF00) >> 8));
        packsCmdLength(dos, (byte) (jsonData.length & 0xFF));
        for (byte jsonDatum : jsonData) {
            EndianUtil.writeByte(dos, jsonDatum);
        }


    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {

    }
}
