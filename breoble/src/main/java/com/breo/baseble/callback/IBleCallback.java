package com.breo.baseble.callback;

import com.breo.baseble.core.BluetoothGattChannel;
import com.breo.baseble.exception.BleException;
import com.breo.baseble.model.BluetoothLeDevice;

/**
 * 操作数据回调
 */
public interface IBleCallback {
    void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice);

    void onFailure(BleException exception);
}
