package com.breo.breoz.ble.msg.iSeeKSensorMsg.cmd;


import com.breo.breoz.ble.msg.EndianUtil;
import com.breo.breoz.ble.msg.iSeeKSensorMsg.BaseISeeKCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by chenqc on 2019/11/25 17:12
 *
 *
 */
public class ISeeKFactoryCmd extends BaseISeeKCmdMsg {
    public byte clean;
    public byte factoryMode;


    public ISeeKFactoryCmd() {
        super(FACTORY);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 2);
        EndianUtil.writeByte(dos, clean);
        EndianUtil.writeByte(dos, factoryMode);

    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {

    }
}
