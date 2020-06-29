package com.breo.breoz.ble.msg.base;


import com.breo.baseble.utils.HexUtil;
import com.breo.breoz.ble.msg.EndianUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by gtt on 2017/4/28.
 */

public abstract class BaseSensorMsg extends PackageMsg {
    public static final byte SYS_TEMPER_SENSOR = 0x20;
    public static final byte SYS_IDREAM_SENSOR = 0x41;
    public static final byte SYS_WOWO_SENSOR = 0x01;
    public static final byte SYS_WOWO2_SENSOR = 0x05;
    public static final byte SYS_ISEEM_SENSOR = 0X21;
    public static final byte SYS_ISEE4M_SENSOR = 0x22;
    public static final byte SYS_ISEE4MS_SENSOR = 0x23;

    public static final byte SYS_INECK3S_SENSOR = 0x12;

    public static final byte SYS_ISEEK_SENSOR = 0x26;
    public static final byte SYS_IGUN_SENSOR = 0x27;


    public byte bsCmdId;//命令体参数名ID
    public byte bsCmdLength;//命令体参数长度

    /**
     * @param sensorType //设备类型标志
     * @param bsCmdId    //命令体ID
     */
    public BaseSensorMsg(byte sensorType, byte bsCmdId) {
        super(sensorType);
        this.bsCmdId = bsCmdId;
    }

    protected abstract void packCmd(DataOutputStream dos) throws IOException;

    protected abstract void unpackCmd(DataInputStream dis) throws IOException;

    @Override
    public final void packsSensor(DataOutputStream dos) throws IOException {
        if (bsCmdId != -1) {
            EndianUtil.writeByte(dos, bsCmdId);
        }
        packCmd(dos);
    }

    @Override
    public final void unpacksSensor(DataInputStream dis) throws IOException {
        if (bsCmdId != -1) {
            bsCmdId = EndianUtil.readByte(dis);
        }
        unpackCmd(dis);
    }

    protected final void packsCmdLength(DataOutputStream dos, byte commandLength) throws IOException {
        EndianUtil.writeByte(dos, this.bsCmdLength = commandLength);
    }

    protected final void unpacksCmdLength(DataInputStream dis) throws IOException {
        bsCmdLength = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "BaseSensorMsg{" +
                "bsCmdId=" + HexUtil.toHex16(bsCmdId) +
                ", bsCmdLength=" + HexUtil.toHex16(bsCmdLength) +
                "} " + super.toString();
    }


}
