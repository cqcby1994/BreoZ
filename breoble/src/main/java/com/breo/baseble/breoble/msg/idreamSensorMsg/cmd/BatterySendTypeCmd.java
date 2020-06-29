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
public class BatterySendTypeCmd extends BaseIdreamCmdMsg {
    public byte battery_alarm;
    public byte battery_charge;
    public byte battery_voltage;
    public byte reserve1 = 0x00;
    public byte reserve2 = 0x00;

    public BatterySendTypeCmd() {
        super(BATTERY_SEND_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 5);
        EndianUtil.writeByte(dos, battery_alarm);
        EndianUtil.writeByte(dos, battery_charge);
        EndianUtil.writeByte(dos, battery_voltage);
        EndianUtil.writeByte(dos, reserve1);
        EndianUtil.writeByte(dos, reserve2);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        battery_alarm = EndianUtil.readByte(dis);
        battery_charge = EndianUtil.readByte(dis);
        battery_voltage = EndianUtil.readByte(dis);
        reserve1 = EndianUtil.readByte(dis);
        reserve2 = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "BatterySendTypeCmd{" +
                "battery_alarm=" + battery_alarm +
                ", battery_charge=" + battery_charge +
                ", battery_voltage=" + battery_voltage +
                ", bootloaderLittleVersion=" + reserve1 +
                ", deviceType2=" + reserve2 +
                "} " + super.toString();
    }
}
