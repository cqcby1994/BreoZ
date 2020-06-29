package com.breo.baseble.callback;

import com.breo.baseble.core.BreoDevice;
import com.breo.baseble.exception.BleException;

/**
 *  连接设备回调
 */
public interface IConnectCallback {
    //连接成功
    void onConnectSuccess(BreoDevice deviceMirror);

    //连接失败
    void onConnectFailure(BleException exception);

    //连接断开
    void onDisconnect();
    //不是Breo设备
    void onNotBreoDevice(String s);
}
