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
public class AirModeTypeCCmd extends BaseIdreamCmdMsg {
    public byte air_value;
    public byte reserve1 = 0x00;
    public byte reserve2 = 0x00;

    public AirModeTypeCCmd() {
        super(AIR_MODE_TYPE_C);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, air_value);
        EndianUtil.writeByte(dos, reserve1);
        EndianUtil.writeByte(dos, reserve2);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        air_value = EndianUtil.readByte(dis);
        reserve1 = EndianUtil.readByte(dis);
        reserve2 = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "AirModeTypeCCmd{" +
                "air_d_value=" + air_value +
                ", bootloaderLittleVersion=" + reserve1 +
                ", deviceType2=" + reserve2 +
                "} " + super.toString();
    }
}
