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
public class LiftReceTypeCmd extends BaseIdreamCmdMsg {
    public byte set_lift_down;
    public byte set_lift_up;
    public byte reserve = 0x00;

    public LiftReceTypeCmd() {
        super(LIFT_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, set_lift_down);
        EndianUtil.writeByte(dos, set_lift_up);
        EndianUtil.writeByte(dos, reserve);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        set_lift_down = EndianUtil.readByte(dis);
        set_lift_up = EndianUtil.readByte(dis);
        reserve = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "LiftReceTypeCmd{" +
                "set_lift_down=" + set_lift_down +
                ", set_lift_up=" + set_lift_up +
                ", reserve=" + reserve +
                "} " + super.toString();
    }
}
