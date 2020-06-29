package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 说明：
 *
 * @author ： gtt
 * @date ： 2017/11/30
 */
public class HardVersionSendTypeCmd extends BaseIdreamCmdMsg {
    public byte[] versions = new byte[5];

    public HardVersionSendTypeCmd() {
        super(HARDVERSION_SEND_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 5);
        EndianUtil.writeBytes(dos, versions, 0, versions.length);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        versions = EndianUtil.readBytes(dis, 5);
    }

    @Override
    public String toString() {
        return "HardVersionSendTypeCmd{" +
                "versions=" + Arrays.toString(versions) +
                "} " + super.toString();
    }
}
