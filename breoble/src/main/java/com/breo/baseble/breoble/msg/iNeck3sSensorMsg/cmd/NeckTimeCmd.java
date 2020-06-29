package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by breo on 2019/6/19.
 */

public class NeckTimeCmd extends BaseNeck3sCmd {
    public byte timing;//定时时间
    public byte time;//时间

    public NeckTimeCmd() {
        super(TIME_SET);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 2);
        EndianUtil.writeByte(dos, timing);
        EndianUtil.writeByte(dos, time);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        timing = EndianUtil.readByte(dis);
        time = EndianUtil.readByte(dis);

    }

    @Override
    public String toString() {
        return "NeckTimeCmd{" +
                "timing=" + timing +
                ", time=" + time +
                '}';
    }
}
