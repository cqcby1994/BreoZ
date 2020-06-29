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
public class TempReceTypeCmd extends BaseIdreamCmdMsg {
    public byte set_temp;
    public byte heat_work;
    public byte reserve = 0x00;

    public TempReceTypeCmd() {
        super(TEMP_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, set_temp);
        EndianUtil.writeByte(dos, heat_work);
        EndianUtil.writeByte(dos, reserve);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        set_temp = EndianUtil.readByte(dis);
        heat_work = EndianUtil.readByte(dis);
        reserve = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "TempReceTypeCmd{" +
                "set_temp=" + set_temp +
                ", heat_work=" + heat_work +
                ", reserve=" + reserve +
                "} " + super.toString();
    }
}
