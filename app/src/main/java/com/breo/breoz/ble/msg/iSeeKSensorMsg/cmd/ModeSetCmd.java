package com.breo.breoz.ble.msg.iSeeKSensorMsg.cmd;


import com.breo.breoz.ble.msg.EndianUtil;
import com.breo.breoz.ble.msg.iSeeKSensorMsg.BaseISeeKCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by chenqc on 2019/10/17 14:52
 *
 * Byte[0]:按摩模式，“0x00”护眼模式，“0x01”美眼 “0x02”睡眠 “0x03”舒曼波，“0x04”抚触模式，“0x05”明目，“0x06”养眼“0x07”眼保健操 +
 * Byte[1]:模式状态，“0x00”停止，“0x01”工作
 */
public class ModeSetCmd extends BaseISeeKCmdMsg {
    public byte modeId;
    public byte modeState;


    public ModeSetCmd() {
        super(MODE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 2);
        EndianUtil.writeByte(dos, modeId);
        EndianUtil.writeByte(dos, modeState);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {

    }
}
