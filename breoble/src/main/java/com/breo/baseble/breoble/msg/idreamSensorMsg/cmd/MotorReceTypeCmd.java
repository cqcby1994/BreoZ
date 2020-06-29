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
public class MotorReceTypeCmd extends BaseIdreamCmdMsg {
    public byte reserve = 0x00;
    public byte motor_work;
    public byte motor_dir;

    public MotorReceTypeCmd() {
        super(MOTOR_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, reserve);
        EndianUtil.writeByte(dos, motor_work);
        EndianUtil.writeByte(dos, motor_dir);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        reserve = EndianUtil.readByte(dis);
        motor_work = EndianUtil.readByte(dis);
        motor_dir = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "MotorReceTypeCmd{" +
                "reserve=" + reserve +
                ", motor_work=" + motor_work +
                ", motor_dir=" + motor_dir +
                "} " + super.toString();
    }
}
