package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by breo on 2019/6/20.
 */

public class NeckHotCmd extends BaseNeck3sCmd {
    public byte temperature;
    public NeckHotCmd() {
        super(HOT_SET);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 1);
        EndianUtil.writeByte(dos, temperature);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        temperature = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "NeckHotCmd{" +
                "temperature=" + temperature +
                '}';
    }
}
