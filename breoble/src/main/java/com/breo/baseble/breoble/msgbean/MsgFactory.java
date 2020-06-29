package com.breo.baseble.breoble.msgbean;


import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeACmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeBCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeCCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.ModeReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.MotorReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.MusicReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.MutiCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.TempReceTypeCmd;

import java.io.UnsupportedEncodingException;


/**
 * Created by chenqc on 2019/6/27 15:21
 */
public class MsgFactory {

    public static byte[] getHelloData() {
        return new byte[]{0x4E, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFB, 0x4E};
    }

    public static TempReceTypeCmd getTempMsg(boolean open) {
        TempReceTypeCmd tempReceTypeCmd = new TempReceTypeCmd();
        tempReceTypeCmd.set_temp = (42 & 0xFF);
        if (open) {
            tempReceTypeCmd.heat_work = 0x01;
        } else {
            tempReceTypeCmd.heat_work = 0x00;
        }
        return tempReceTypeCmd;
    }
    public static TempReceTypeCmd getTempMsg(int temp) {
        TempReceTypeCmd tempReceTypeCmd = new TempReceTypeCmd();
        tempReceTypeCmd.set_temp = (byte) (temp & 0xFF);

        tempReceTypeCmd.heat_work = 0x01;

        return tempReceTypeCmd;
    }

    public static MotorReceTypeCmd getMotorKnead(boolean open) {
        MotorReceTypeCmd motorReceTypeCmd = new MotorReceTypeCmd();
        motorReceTypeCmd.motor_dir = 0x00;
        if (open) {
            motorReceTypeCmd.motor_work = 0x01;
        } else {
            motorReceTypeCmd.motor_work = 0x00;
        }
        return motorReceTypeCmd;
    }

    public static AirReceTypeCmd getAirMode(int flag) {
        AirReceTypeCmd airReceTypeCmd = new AirReceTypeCmd();
        airReceTypeCmd.air_level = 0x00;
        airReceTypeCmd.press_work = 0x01;
        switch (flag) {
            case 0:
                //循环
                airReceTypeCmd.air_mode = 0x00;
                break;
            case 1:
                //强
                airReceTypeCmd.air_mode = 0x01;
                break;
            case 2:
                //中
                airReceTypeCmd.air_mode = 0x02;
                break;
            case 3:
                //弱
                airReceTypeCmd.air_mode = 0x03;
                break;
        }
        return airReceTypeCmd;
    }

    public static MutiCmd getAirType(int type, int progress) {

        MutiCmd mutiCmd = new MutiCmd();
        AirReceTypeCmd airReceTypeCmd = new AirReceTypeCmd();
        airReceTypeCmd.air_mode = 0x04;
        airReceTypeCmd.air_level = 0x00;
        airReceTypeCmd.press_work = 0x01;
        mutiCmd.baseIdreamCmdMsgs.add(airReceTypeCmd);
        switch (type) {
            case 1:
                AirModeTypeACmd aCmd = new AirModeTypeACmd();
                aCmd.air_value = (byte) progress;
                mutiCmd.baseIdreamCmdMsgs.add(aCmd);
                break;
            case 2:
                AirModeTypeBCmd bCmd = new AirModeTypeBCmd();
                bCmd.air_value = (byte) progress;
                mutiCmd.baseIdreamCmdMsgs.add(bCmd);

                break;
            case 3:
                AirModeTypeCCmd cCmd = new AirModeTypeCCmd();
                cCmd.air_value = (byte) progress;
                mutiCmd.baseIdreamCmdMsgs.add(cCmd);
                break;
        }
        return mutiCmd;
    }

    public static MusicReceTypeCmd getMusicCmd(int flag) {
        MusicReceTypeCmd musicReceTypeCmd = new MusicReceTypeCmd();
        musicReceTypeCmd.set_mp3_cmd = 0x01;
        switch (flag) {
            case 0:
                //暂停
                musicReceTypeCmd.mp3_work = 0x01;
                break;
            case 1:
                musicReceTypeCmd.mp3_work = 0x02;
                //播放
                break;
            case 2:
                musicReceTypeCmd.set_mp3_cmd = 0x05;
                musicReceTypeCmd.mp3_work = 0x01;
                //上一首
                break;
            case 3:
                //下一首
                musicReceTypeCmd.set_mp3_cmd = 0x04;
                musicReceTypeCmd.mp3_work = 0x01;
                break;
        }

        return musicReceTypeCmd;
    }

    public static ModeReceTypeCmd getTimeCmd(int time) {
        ModeReceTypeCmd modeReceTypeCmd = new ModeReceTypeCmd();
        modeReceTypeCmd.set_mode = 0x00;
        modeReceTypeCmd.work_time = 0x00;
        if (time == 5) {
            modeReceTypeCmd.work_time = 0x05;
        } else if (time == 10) {
            modeReceTypeCmd.work_time = 0x0a;

        } else if (time == 15) {
            modeReceTypeCmd.work_time = 0x0f;
        }
        return modeReceTypeCmd;
    }

    public static String[] getSongMsg(byte[] data) {
        int songNameLength = (int) data[5];
        int musicianLength = (int) data[6];
        byte[] songNameBytes = new byte[songNameLength];
        byte[] musicianBytes = new byte[musicianLength];
        for (int i = 0; i < songNameLength; i++) {
            songNameBytes[i] = data[7 + i];
        }
        for (int j = 0; j < musicianLength; j++) {
            musicianBytes[j] = data[7 + songNameLength + j];
        }
        try {
            String songName = new String(songNameBytes, "UTF-8");
            String musician = new String(musicianBytes, "UTF-8");
            return new String[]{songName, musician};
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static AirModeTypeCmd getAllAirModeTypeMsg(int mode_a, int mode_b, int mode_c) {
        byte[] data = new byte[]{0x5D, 0x03, (byte) mode_a, 0x00, 0x00, 0x60, 0x03, (byte) mode_b, 0x00, 0x00, 0x61, 0x03, (byte) mode_c, 0x00, 0x00};
        return new AirModeTypeCmd(data);
    }
}
