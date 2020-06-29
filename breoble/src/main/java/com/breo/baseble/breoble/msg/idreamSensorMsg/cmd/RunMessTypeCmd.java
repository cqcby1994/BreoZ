package com.breo.baseble.breoble.msg.idreamSensorMsg.cmd;



import com.breo.baseble.breoble.msg.EndianUtil;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * idream5s设备上报
 * Created by Administrator on 2018/8/23.
 * [长度] 8
 * [数据]
 * Byte[0]:工作模式， “ 0x00” 模式 1,“ 0x01” 模式二,“ 0x02” 自定义,
 * Byte[1]:MP3 播放状态
 * Byte[2]:热敷温度， “ 0x00” 关闭， “ 0x01” 打开
 * Byte[3]:气压强度， “ 0x00”
 * Byte[4]:按摩时间， “ 0x00~0xFF” 分钟
 * Byte[5]:电池电量， 五个等级“ 0x00~0x04”
 * Byte[6]:音频连接状态， “ 0x01” 音频连接 “ 0x00” 音频没有连接
 * Byte[7]:预留
 */

public class RunMessTypeCmd extends BaseIdreamCmdMsg {
    public byte massage_mode;//按摩模式
    public byte mp3_state;//mp3状态
    public byte hot_compress;//热敷温度
    public byte air_strength;//气压强度
    public byte time;//按摩时间
    public byte battery;//电池电量
    public byte music_conn_state;//音频连接状态
    public byte tmp;//预留

    public RunMessTypeCmd() {
        super(RUNMESS_TYPE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 8);
        EndianUtil.writeByte(dos, massage_mode);
        EndianUtil.writeByte(dos, mp3_state);
        EndianUtil.writeByte(dos, hot_compress);
        EndianUtil.writeByte(dos, air_strength);
        EndianUtil.writeByte(dos, time);
        EndianUtil.writeByte(dos, battery);
        EndianUtil.writeByte(dos, music_conn_state);
        EndianUtil.writeByte(dos, tmp);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        massage_mode = EndianUtil.readByte(dis);
        mp3_state = EndianUtil.readByte(dis);
        hot_compress = EndianUtil.readByte(dis);
        air_strength = EndianUtil.readByte(dis);
        time = EndianUtil.readByte(dis);
        battery = EndianUtil.readByte(dis);
        music_conn_state = EndianUtil.readByte(dis);
        tmp = EndianUtil.readByte(dis);
    }

    @Override
    public String toString() {
        return "RunMessTypeCmd{" +
                "massage_mode=" + massage_mode +
                ", mp3_state=" + mp3_state +
                ", hot_compress=" + hot_compress +
                ", air_strength=" + air_strength +
                ", time=" + time +
                ", battery=" + battery +
                ", music_conn_state=" + music_conn_state +
                '}';
    }
}
