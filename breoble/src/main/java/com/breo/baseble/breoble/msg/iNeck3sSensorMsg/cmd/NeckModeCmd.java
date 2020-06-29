package com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.BaseNeck3sCmd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by breo on 2019/6/19.
 */

public class NeckModeCmd extends BaseNeck3sCmd {
    public byte mode;//模式

    public NeckModeCmd() {
        super(MODE_SET);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 1);
        EndianUtil.writeByte(dos, mode);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        mode = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "NeckModeCmd{" +
                "mode=" + mode +
                '}';
    }
}
