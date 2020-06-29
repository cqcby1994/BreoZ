package com.breo.breoz.ble.msg.iSeeKSensorMsg.cmd;


import com.breo.breoz.ble.msg.EndianUtil;
import com.breo.breoz.ble.msg.iSeeKSensorMsg.BaseISeeKCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by chenqc on 2019/10/21 15:44
 */
public class RunCmd extends BaseISeeKCmdMsg {
    byte[] jsonBytes;
    /**
     * //命令体ID
     */
    public RunCmd() {
        super(RUN_MESS);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) jsonBytes.length);
        EndianUtil.writeBytes(dos,jsonBytes,0,jsonBytes.length);

    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        jsonBytes = EndianUtil.readBytes(dis,bsCmdLength);

    }
}
