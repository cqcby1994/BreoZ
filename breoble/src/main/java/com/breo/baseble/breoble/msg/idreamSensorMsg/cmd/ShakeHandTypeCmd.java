package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2018/5/22.
 */

public class ShakeHandTypeCmd extends BaseIdreamCmdMsg {
    public byte b1;//
    public byte b2;//
    public byte b3;//
    public byte b4;//
    public byte b5;//
    public byte b6;//
    public byte b7;//
    public byte b8;//

    public ShakeHandTypeCmd() {
        super(IDREAM5S_SHAKE_HAND);

    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 8);
        EndianUtil.writeByte(dos, b1);
        EndianUtil.writeByte(dos,b2);
        EndianUtil.writeByte(dos, b3);
        EndianUtil.writeByte(dos, b4);
        EndianUtil.writeByte(dos, b5);
        EndianUtil.writeByte(dos, b6);
        EndianUtil.writeByte(dos, b7);
        EndianUtil.writeByte(dos, b8);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        b1 = EndianUtil.readByte(dis);
        b2=EndianUtil.readByte(dis);
        b3 = EndianUtil.readByte(dis);
        b4 = EndianUtil.readByte(dis);
        b5 = EndianUtil.readByte(dis);
        b6 = EndianUtil.readByte(dis);
        b7 = EndianUtil.readByte(dis);
        b8 = EndianUtil.readByte(dis);
    }
}
