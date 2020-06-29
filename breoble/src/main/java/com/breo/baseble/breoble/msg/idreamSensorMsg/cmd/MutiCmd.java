package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;


import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 说明：
 *
 * @author ： gtt
 * @date ： 2017/11/30
 */
public class MutiCmd extends BaseIdreamCmdMsg {
    public List<BaseIdreamCmdMsg> baseIdreamCmdMsgs = new ArrayList<>();

    public MutiCmd() {
        super((byte) 0xFF);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        for (BaseIdreamCmdMsg baseIdreamCmdMsg : baseIdreamCmdMsgs) {
            baseIdreamCmdMsg.packsSensor(dos);
        }
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        for (BaseIdreamCmdMsg baseIdreamCmdMsg : baseIdreamCmdMsgs) {
            baseIdreamCmdMsg.unpacksSensor(dis);
        }
    }


}
