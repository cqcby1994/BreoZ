package com.breo.breoz.ble.msg.iSeeKSensorMsg;


import com.breo.breoz.ble.msg.base.BaseSensorMsg;

/**
 * Created by chenqc on 2019/10/17 14:16
 */
public abstract class BaseISeeKCmdMsg extends BaseSensorMsg {
    public static final byte DEVICE_VERSION = 0x01;// 1byte 设备版本（默认握手指令） R+RP
    public static final byte RESET_DEVICE = 0x5B;//1byte 复位设置 W+RP
    public static final byte RUN_MESS = 0x69;// 设备运行状态 R+RP
    public static final byte BATTERY = 0x58;// 热敷温度 R/W+RP
    public static final byte MODE = 0x6b;// 热敷温度 R/W+RP
    public static final byte MUSICMODE = 0x6c;// 热敷温度 R/W+RP
    public static final byte EXPLORE = 0x6d;// 模式创作指令
    public static final byte JSON = (byte) 0xaa;// json指令

    public static final byte FACTORY = (byte) 0x05;// json指令



    /**
     * @param bsCmdId    //命令体ID
     */
    public BaseISeeKCmdMsg(byte bsCmdId) {
        super(SYS_ISEEK_SENSOR, bsCmdId);
    }


}
