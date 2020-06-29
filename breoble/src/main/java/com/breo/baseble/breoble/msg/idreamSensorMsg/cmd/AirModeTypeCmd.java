package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;

import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by chenqc on 2019/7/15 09:24
 */
public class AirModeTypeCmd extends BaseIdreamCmdMsg {
    private byte[] datas;
    public AirModeTypeCmd(byte[] bytes) {
        super(bytes[0]);
        this.datas = bytes;
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        for (int i =0;i<datas.length;i++){
            if (i==0){

            }else {
                EndianUtil.writeByte(dos, datas[i]);
            }
        }
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {

    }
}
