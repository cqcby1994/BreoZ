package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.APP_UPDATA_FIRMWORK;

/**
 * 说明：
 *
 * @author ： gtt
 * @date ： 2017/11/30
 */
public class APPUpdatafirmworkCmd extends BaseIdreamCmdMsg {
    public byte reserve1 = 0x00;
    public byte reserve2 = 0x00;
    public byte reserve3 = 0x00;

    public APPUpdatafirmworkCmd() {
        super(APP_UPDATA_FIRMWORK);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, reserve1);
        EndianUtil.writeByte(dos, reserve2);
        EndianUtil.writeByte(dos, reserve3);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        reserve1 = EndianUtil.readByte(dis);
        reserve2 = EndianUtil.readByte(dis);
        reserve3 = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "APPUpdatafirmworkCmd{" +
                "bootloaderLittleVersion=" + reserve1 +
                ", deviceType2=" + reserve2 +
                ", reserve3=" + reserve3 +
                "} " + super.toString();
    }
}
