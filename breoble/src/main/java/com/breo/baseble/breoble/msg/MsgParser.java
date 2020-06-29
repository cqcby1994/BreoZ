package com.breo.baseble.breoble.msg;

import android.util.Log;


import com.breo.baseble.breoble.msg.base.BaseSensorMsg;
import com.breo.baseble.breoble.msg.base.PackageMsg;
import com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.APPUpdatafirmworkCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeACmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeBCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirModeTypeCCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.AirReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.BatterySendTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.HardVersionSendTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.LiftReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.ModeReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.MotorReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.MusicReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.OxlibVersionSendTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.RunMessTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.ShakeHandTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.TempReceTypeCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.VersionReceCmdCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.VersionSendTypeCmd;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.AIR_MODE_TYPE_A;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.AIR_MODE_TYPE_B;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.AIR_MODE_TYPE_C;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.AIR_RECE_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.APP_UPDATA_FIRMWORK;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.BATTERY_SEND_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.HARDVERSION_SEND_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.IDREAM5S_SHAKE_HAND;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.LIFT_RECE_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.MODE_RECE_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.MOTOR_RECE_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.MUSIC_RECE_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.OXLIBVERSION_SEND_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.RUNMESS_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.TEMP_RECE_TYPE;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.VERSION_RECE_CMD;
import static com.breo.baseble.breoble.msg.idreamSensorMsg.BaseIdreamCmdMsg.VERSION_SEND_TYPE;


/**
 * 创建者： gtt
 * 创建时间：2017/4/27
 * 说明：
 */
public class MsgParser {
    public static PackageMsg parseMessage(byte[] data) {
        PackageMsg packageMsg = null;
        try {
            //如果是iseem且是69包且长度超过18,说明是粘包
            if (data != null && (data[2] == BaseSensorMsg.SYS_IDREAM_SENSOR) && data[6] == 0x69 && data.length > 18) {
                if (data[18] == (byte) 0xAA && data[19] == (byte) 0xBB) {
                    int songNameLength = (int) data[23];
                    int musicianLength = (int) data[24];
                    byte[] songNameBytes = new byte[songNameLength];
                    byte[] musicianBytes = new byte[musicianLength];
                    for (int i = 0; i < songNameLength; i++) {
                        songNameBytes[i] = data[25 + i];
                    }
                    for (int j = 0; j < musicianLength; j++) {
                        musicianBytes[j] = data[25 + songNameLength + j];
                    }
                    try {
                        String songName = new String(songNameBytes, "UTF-8");
                        String musician = new String(musicianBytes, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }


            if (data != null && data.length >= 9) {
                byte[] localData = Protocol.unescapeMsg(data, 1, data.length - 3);
                //判断是不是设备上报
                if (localData[2] == PackageMsg.COMM_READ || localData[2] == PackageMsg.COMM_UPLOAD) {
                    switch (localData[1]) {
                        case BaseSensorMsg.SYS_IDREAM_SENSOR:
                            packageMsg = getIdreamCmdMsg(localData[5]);
                            break;

                        default:
                            return null;
                    }

                    if (packageMsg != null) {
                        if (localData[2] == PackageMsg.COMM_UPLOAD) {
                            packageMsg.unpacksPackage(data);
                        } else if (localData[2] == PackageMsg.COMM_READ) {
                            packageMsg.unpacksPackageWithShakeHand(data);
                        }
                    } else {
                    }
                }
            }
            return packageMsg;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("ppp",e.toString());
            return null;
        }
    }


    public static BaseIdreamCmdMsg getIdreamCmdMsg(byte cmdId) {
        BaseIdreamCmdMsg baseIdreamCmdMsg = null;
        switch (cmdId) {
            case AIR_MODE_TYPE_A:
                baseIdreamCmdMsg = new AirModeTypeACmd();
                break;
            case AIR_MODE_TYPE_B:
                baseIdreamCmdMsg = new AirModeTypeBCmd();
                break;
            case AIR_MODE_TYPE_C:
                baseIdreamCmdMsg = new AirModeTypeCCmd();
                break;
            case AIR_RECE_TYPE:
                baseIdreamCmdMsg = new AirReceTypeCmd();
                break;
            case APP_UPDATA_FIRMWORK:
                baseIdreamCmdMsg = new APPUpdatafirmworkCmd();
                break;
            case BATTERY_SEND_TYPE:
                baseIdreamCmdMsg = new BatterySendTypeCmd();
                break;
            case HARDVERSION_SEND_TYPE:
                baseIdreamCmdMsg = new HardVersionSendTypeCmd();
                break;
            case LIFT_RECE_TYPE:
                baseIdreamCmdMsg = new LiftReceTypeCmd();
                break;
            case MODE_RECE_TYPE:
                baseIdreamCmdMsg = new ModeReceTypeCmd();
                break;
            case MOTOR_RECE_TYPE:
                baseIdreamCmdMsg = new MotorReceTypeCmd();
                break;
            case MUSIC_RECE_TYPE:
                baseIdreamCmdMsg = new MusicReceTypeCmd();
                break;
            case OXLIBVERSION_SEND_TYPE:
                baseIdreamCmdMsg = new OxlibVersionSendTypeCmd();
                break;
            case TEMP_RECE_TYPE:
                baseIdreamCmdMsg = new TempReceTypeCmd();
                break;
            case VERSION_RECE_CMD:
                baseIdreamCmdMsg = new VersionReceCmdCmd();
                break;
            case VERSION_SEND_TYPE:
                baseIdreamCmdMsg = new VersionSendTypeCmd();
                break;
            case IDREAM5S_SHAKE_HAND:
                baseIdreamCmdMsg = new ShakeHandTypeCmd();
                break;
            case RUNMESS_TYPE:
                baseIdreamCmdMsg=new RunMessTypeCmd();
                break;
            default:
                break;
        }
        return baseIdreamCmdMsg;
    }

}
