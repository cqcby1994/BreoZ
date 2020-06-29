package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by breo on 2019/6/19.
 */

public class NeckMotoCmd extends BaseNeck3sCmd {
    public byte space;//速度
    public byte motor;//电机选择
    public byte path;//方向
    public NeckMotoCmd() {
        super(MOTO_SET);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, space);
        EndianUtil.writeByte(dos, motor);
        EndianUtil.writeByte(dos, path);

    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        space = EndianUtil.readByte(dis);
        motor = EndianUtil.readByte(dis);
        path = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "NeckMotoCmd{" +
                "space=" + space +
                ", motor=" + motor +
                ", path=" + path +
                '}';
    }
}
