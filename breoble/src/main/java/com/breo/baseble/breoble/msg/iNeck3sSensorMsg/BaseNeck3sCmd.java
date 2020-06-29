package com.breo.baseble.breoble.msg.iNeck3sSensorMsg;


import com.breo.baseble.breoble.msg.base.BaseSensorMsg;

/**
 * Created by chenqc on 2019/6/19.
 */

public abstract class BaseNeck3sCmd extends BaseSensorMsg {
    /***************** 消息命令标志 ********************/
    public static final byte DEVICE_VERSION = 0x01;// 1byte 设备版本（默认握手指令） R+RP
    public static final byte RESET_DEVICE = 0x5B;//1byte 复位设置 W+RP
    public static final byte  WORK_ONOFF= 0x09;//设备开关
    public static final byte  TIME_SET= 0x40;//后颈按摩时间
    public static final byte  MODE_SET= 0x56;//模式
    public static final byte  MOTO_SET= 0x59;//转向
    public static final byte  HOT_SET= 0x51;//热敷

    public static final byte  UPDATA= 0x5B;//更新


    public BaseNeck3sCmd(byte bsCmdId) {
        super(SYS_INECK3S_SENSOR, bsCmdId);
    }


}
