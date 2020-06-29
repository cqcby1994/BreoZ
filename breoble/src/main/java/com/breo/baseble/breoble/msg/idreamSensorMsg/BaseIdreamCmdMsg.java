package com.breo.baseble.breoble.msg.idreamSensorMsg;


import com.breo.baseble.breoble.msg.base.BaseSensorMsg;

/**
 * 说明：
 *
 * @author ： gtt
 *
 */
public abstract class BaseIdreamCmdMsg extends BaseSensorMsg {
    public static final byte TEMP_RECE_TYPE = 0x51;
    public static final byte AIR_RECE_TYPE = 0x52;
    public static final byte MUSIC_RECE_TYPE = 0x53;
    //备用;
    public static final byte CMD_RECE_TYPE = 0x54;
    //备用;
    public static final byte SEND_TYPE = 0x55;
    public static final byte MODE_RECE_TYPE = 0x56;
    public static final byte LIFT_RECE_TYPE = 0x57;
    public static final byte BATTERY_SEND_TYPE = 0x58;
    public static final byte MOTOR_RECE_TYPE = 0x59;
    //备用;
    public static final byte MP3_MUSIC_NAME = 0x5A;
    public static final byte APP_UPDATA_FIRMWORK = 0x5B;
    public static final byte AIR_MODE_TYPE_A = 0x5D;
    public static final byte AIR_MODE_TYPE_B = 0x60;
    public static final byte AIR_MODE_TYPE_C = 0x61;
    public static final byte IDREAM5S_SHAKE_HAND = 0x01;
    //备用
    public static final byte PRESSURE_DATA = 0x62;
    public static final byte VERSION_RECE_CMD = 0x63;
    public static final byte HARDVERSION_SEND_TYPE = 0x64;
    public static final byte VERSION_SEND_TYPE = 0x65;
    public static final byte OXLIBVERSION_SEND_TYPE = 0x66;
    public static final byte RUNMESS_TYPE=0x69;
    public BaseIdreamCmdMsg(byte commandId) {
        super(SYS_IDREAM_SENSOR, commandId);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
