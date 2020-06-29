package com.breo.breoz.ble.msg.iSeeKSensorMsg.cmd;


import com.breo.baseble.utils.HexUtil;
import com.breo.breoz.ble.msg.EndianUtil;
import com.breo.breoz.ble.msg.iSeeKSensorMsg.BaseISeeKCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by chenqc on 2019/10/17 14:57
 *
 * Byte[0]:状态，0x00 等待下载 0x01 开始下载 “0x02”下载中，“0x03”下载完成 ，“0x04”下载异常
 * Byte[1]:进度，“0x00”-“0x63”
 * Byte[2]--:json 格式(url+名字)
 */
public class MusicUrlCmd extends BaseISeeKCmdMsg {
    public byte musicStatus;
    public byte musicLodingProgress;
    public byte musicMsgLength;
    public byte[] musicMsg;

    /**
     * @  //命令体ID
     */
    public MusicUrlCmd() {
        super(MUSICMODE);
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) (musicMsg.length+2));
        EndianUtil.writeByte(dos, musicStatus);
        EndianUtil.writeByte(dos, musicLodingProgress);
        EndianUtil.writeByte(dos, musicMsg.length);
        for (byte b : musicMsg) {
            EndianUtil.writeByte(dos, b);
        }



    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {
        unpacksCmdLength(dis);
        musicStatus = EndianUtil.readByte(dis);
        musicLodingProgress = EndianUtil.readByte(dis);
        musicMsg = EndianUtil.readBytes(dis,bsCmdLength-2);



    }

    @Override
    public String toString() {
        return "MusicUrlCmd{" +
                "musicStatus=" + musicStatus +
                ", musicLodingProgress=" + musicLodingProgress +
                ", musicMsg=" + HexUtil.toHexString(musicMsg) +
                '}';
    }
}
