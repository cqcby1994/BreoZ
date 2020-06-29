package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 说明：
 *
 * @author ： gtt
 * @date ： 2017/11/30
 */
public class MusicReceTypeCmd extends BaseIdreamCmdMsg {
    public byte set_mp3_cmd;//设置音乐命令， “ 0x01” 设定播放状态， “ 0x02” 音量设置,“ 0x03” 查询音乐播放状态， “ 0x04” 下一首， “ 0x05” 上一首
    public byte mp3_work;// Byte[0]为 01 时， 设定播放状态， “ 0x01” 暂停， “ 0x02” 播放；
    //    Byte[0]为 02 时， 设定具体的音量数值“ 0x00~0x0A”
//    Byte[0]为 03 时， 查询设备当前音乐播放状态， “ 0x00” 暂停状态， “ 0x01” 播放状态。
    public byte reserve = 0x00;

    public MusicReceTypeCmd() {
        super(MUSIC_RECE_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 3);
        EndianUtil.writeByte(dos, set_mp3_cmd);
        EndianUtil.writeByte(dos, mp3_work);
        EndianUtil.writeByte(dos, reserve);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        set_mp3_cmd = EndianUtil.readByte(dis);
        mp3_work = EndianUtil.readByte(dis);
        reserve = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "MusicReceTypeCmd{" +
                "set_mp3_cmd=" + set_mp3_cmd +
                ", mp3_work=" + mp3_work +
                ", reserve=" + reserve +
                "} " + super.toString();
    }
}
