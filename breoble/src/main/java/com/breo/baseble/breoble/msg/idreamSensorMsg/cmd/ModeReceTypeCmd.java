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
public class ModeReceTypeCmd extends BaseIdreamCmdMsg {
    public byte set_mode;
    public byte work_time;
    public byte reserve = 0x00;

    public ModeReceTypeCmd() {
        super(MODE_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, set_mode);
        EndianUtil.writeByte(dos, work_time);
        EndianUtil.writeByte(dos, reserve);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        set_mode = EndianUtil.readByte(dis);
        work_time = EndianUtil.readByte(dis);
        reserve = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "ModeReceTypeCmd{" +
                "set_mode=" + set_mode +
                ", work_time=" + work_time +
                ", reserve=" + reserve +
                "} " + super.toString();
    }
}
