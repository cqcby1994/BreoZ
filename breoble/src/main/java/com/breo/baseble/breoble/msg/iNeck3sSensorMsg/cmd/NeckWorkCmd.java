package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by breo on 2019/6/19.
 */

public class NeckWorkCmd extends BaseNeck3sCmd {
    public byte work_mode;//工作状态

    public NeckWorkCmd() {
        super(WORK_ONOFF);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 1);
        EndianUtil.writeByte(dos, work_mode);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        work_mode = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "WorkCmd{" +
                "work_mode=" + work_mode +
                '}';
    }
}
