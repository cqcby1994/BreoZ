package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 播放网络音乐,让蜂鸣器滴一声
 *
 * @author ： yjl
 * @date ： 2018/8/15
 */
public class BuzzerCmd extends BaseIdreamCmdMsg {
    public byte buzzer = 0x00;//0x00短鸣 0x01长鸣
    public byte count = 0x01;// 次数
    public byte reserve = 0x00;

    public BuzzerCmd() {
        super(MUSIC_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, buzzer);
        EndianUtil.writeByte(dos, count);
        EndianUtil.writeByte(dos, reserve);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        buzzer = EndianUtil.readByte(dis);
        count = EndianUtil.readByte(dis);
        reserve = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "BuzzerCmd{" +
                "buzzer=" + buzzer +
                ", count=" + count +
                ", reserve=" + reserve +
                "} " + super.toString();
    }
}
