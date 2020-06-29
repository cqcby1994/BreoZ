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
public class AirReceTypeCmd extends BaseIdreamCmdMsg {
    public byte air_mode;
    public byte air_level;
    public byte press_work;

    public AirReceTypeCmd() {
        super(AIR_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, air_mode);
        EndianUtil.writeByte(dos, air_level);
        EndianUtil.writeByte(dos, press_work);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        air_mode = EndianUtil.readByte(dis);
        air_level = EndianUtil.readByte(dis);
        press_work = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "AirReceTypeCmd{" +
                "air_mode=" + air_mode +
                ", air_level=" + air_level +
                ", press_work=" + press_work +
                "} " + super.toString();
    }
}
