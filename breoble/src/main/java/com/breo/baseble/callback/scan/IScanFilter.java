package com.breo.baseble.callback.scan;

import com.breo.baseble.model.BluetoothLeDevice;

/**
 * 扫描过滤接口，根据需要实现过滤规则
 */
public interface IScanFilter {
    BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice);
}
