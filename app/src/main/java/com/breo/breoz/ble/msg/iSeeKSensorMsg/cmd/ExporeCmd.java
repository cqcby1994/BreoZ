package com.breo.breoz.ble.msg.iSeeKSensorMsg.cmd;

import android.util.Log;

import com.breo.breoz.ble.msg.EndianUtil;
import com.breo.breoz.ble.msg.iSeeKSensorMsg.BaseISeeKCmdMsg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by chenqc on 2019/11/6 12:05
 * <p>
 * Byte[0] : 探索 基本模式 “0x00 0x00 ”内旋 ，“ 0x01 0x01 ”外旋 “0x020x020x02 ” 扩散 “0x03 0x03 ” 收敛 ，“ 0x04 0x04 ” 回环 ，“ 0x05 0x05 ” 环绕 ，“ 0x060x060x06 ”对绕 “0x070x070x07 ”齐震
 * Byte[1] :快慢 “0x00 0x00 ” 快速 0x01 0x01 ”中速 “0x020x020x02 ” 快速“0x03 0x03 ” 极快 ，
 * Byte[2] ：圈数 “0x00 0x00 ”-“0x ff ”1圈-255 圈
 * Byte[3]：每个 模式 的时间 间隔 ，“0x00 0x00 ”不间隔 “0x01 ”间隔 0.5 秒 “0x02 ” 间隔 1秒，“0x03”间隔
 */
public class ExporeCmd extends BaseISeeKCmdMsg {
    public byte modeId;
    public byte modeSpeed;
    public byte modeTurns;
    public byte modeTime;
    //用于模式存储
    private int[] playModulus = {720, 720, 360, 360, 1440, 720, 1440, 200};

    public int durationTime;

    public String modeName;


    /**
     *
     */
    public ExporeCmd() {
        super(EXPLORE);
    }

    public ExporeCmd(byte modeId, byte modeSpeed, byte modeTurns, byte modeTime) {
        super(EXPLORE);
        this.modeId = modeId;
        this.modeSpeed = modeSpeed;
        this.modeTurns = modeTurns;
        this.modeTime = modeTime;
    }

    @Override
    protected void packCmd(DataOutputStream dos) throws IOException {
        packsCmdLength(dos, (byte) 4);
        EndianUtil.writeByte(dos, modeId);
        EndianUtil.writeByte(dos, modeSpeed);
        EndianUtil.writeByte(dos, modeTurns);
        EndianUtil.writeByte(dos, modeTime);
    }

    @Override
    protected void unpackCmd(DataInputStream dis) throws IOException {

    }

    public int getDurationTime() {
        Log.d("cqc","ExporeCmd="+toString());

        Log.d("cqc","getDurationTime="+playModulus[modeId ] * modeSpeed * modeTurns);
        return playModulus[modeId ] * modeSpeed * modeTurns;
    }

    @Override
    public String toString() {
        return "ExporeCmd{" +
                "modeId=" + modeId +
                ", modeSpeed=" + modeSpeed +
                ", modeTurns=" + modeTurns +
                ", modeTime=" + modeTime +
                ", durationTime=" + durationTime +
                ", modeName='" + modeName + '\'' +
                '}';
    }

    public String toPostString(){
        return
                "\"modeId\":\"" + modeId +
                "\", \"modeSpeed\":\"" + modeSpeed +
                "\", \"modeTurns\":\"" + modeTurns +
                "\", \"modeTime\":\"" + modeTime +
                "\", \"durationTime\":\"" + durationTime +
                "\"" ;
    }


}
