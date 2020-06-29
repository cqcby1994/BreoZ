package com.breo.breoz.event.ble;

/**
 * Created by chenqc on 2019/10/28 14:14
 */
public class BleExceptionEvent  {
    private String msg ;

    public BleExceptionEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
