package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;


import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 说明：
 *
 * @author ： gtt
 * @date ： 2017/11/30
 */
public class VersionReceCmdCmd extends BaseIdreamCmdMsg {
    public byte reserve = 0x00;

    public VersionReceCmdCmd() {
        super(VERSION_RECE_CMD);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 1);
        EndianUtil.writeByte(dos, reserve);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        reserve = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "VersionReceCmdCmd{" +
                "reserve=" + reserve +
                "} " + super.toString();
    }
}
