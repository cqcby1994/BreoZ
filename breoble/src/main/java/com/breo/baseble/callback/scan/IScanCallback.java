package com.breo.baseble.callback.scan;

import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.baseble.model.BluetoothLeDeviceGroup;


/**
 *  扫描回调
 */
public interface IScanCallback {
    //发现设备
    void onDeviceFound(BluetoothLeDevice bluetoothLeDevice);

    //扫描完成
    void onScanFinish(BluetoothLeDeviceGroup bluetoothLeDeviceGroup);

    //扫描超时
    void onScanTimeout();

}
