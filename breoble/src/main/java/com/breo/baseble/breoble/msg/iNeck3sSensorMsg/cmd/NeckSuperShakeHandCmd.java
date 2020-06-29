package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by breo on 2019/6/20.
 */

public class NeckSuperShakeHandCmd extends BaseNeck3sCmd {
    //    Byte[0]:设备类别；
//    Byte[1]:硬件版本；
//    Byte[2]: Bootloader 大版本；
//    Byte[3]:Bootloader 小版本；
//    Byte[4]:设备类别；
//    Byte[5]:硬件版本；
//    Byte[6]:主控软件大版本；
//    Byte[7]:主控软件小版本；
    public byte deviceType;//设备类别
    public byte hardwareVersion;//硬件版本
    public byte bigBootloader;//大版本
    public byte smallBootloader;//小版本
    public byte deviceType2;//设备类别
    public byte hardwareVersion2;//硬件版本
    public byte mainBigBootloader;//主控软件大版本
    public byte mainSmallBootloader;//主控软件小版本
    public NeckSuperShakeHandCmd() {
        super(DEVICE_VERSION);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 8);
        EndianUtil.writeByte(dos, deviceType);
        EndianUtil.writeByte(dos, hardwareVersion);
        EndianUtil.writeByte(dos, bigBootloader);
        EndianUtil.writeByte(dos, smallBootloader);
        EndianUtil.writeByte(dos, deviceType2);
        EndianUtil.writeByte(dos, hardwareVersion2);
        EndianUtil.writeByte(dos, mainBigBootloader);
        EndianUtil.writeByte(dos, mainSmallBootloader);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        deviceType = EndianUtil.readByte(dis);
        hardwareVersion = EndianUtil.readByte(dis);
        bigBootloader = EndianUtil.readByte(dis);
        smallBootloader = EndianUtil.readByte(dis);
        deviceType2 = EndianUtil.readByte(dis);
        hardwareVersion2 = EndianUtil.readByte(dis);
        mainBigBootloader = EndianUtil.readByte(dis);
        mainSmallBootloader = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "NeckSuperShakeHandCmd{" +
                "deviceType=" + deviceType +
                ", hardwareVersion=" + hardwareVersion +
                ", bigBootloader=" + bigBootloader +
                ", smallBootloader=" + smallBootloader +
                ", deviceType2=" + deviceType2 +
                ", hardwareVersion2=" + hardwareVersion2 +
                ", mainBigBootloader=" + mainBigBootloader +
                ", mainSmallBootloader=" + mainSmallBootloader +
                '}';
    }
}
