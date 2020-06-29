package com.breo.baseble.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备信息集合

 */
public class BluetoothLeDeviceGroup {

    private final Map<String, BluetoothLeDevice> mDeviceMap;

    public BluetoothLeDeviceGroup() {
        mDeviceMap = new HashMap<>();
    }

    public void addDevice(BluetoothLeDevice device) {
        if (device == null) {
            return;
        }
        if (mDeviceMap.containsKey(device.getAddress())) {
            mDeviceMap.get(device.getAddress()).updateRssiReading(device.getTimestamp(), device.getRssi());
        } else {
            mDeviceMap.put(device.getAddress(), device);
        }
    }

    public void removeDevice(BluetoothLeDevice device) {
        if (device == null) {
            return;
        }
        mDeviceMap.remove(device.getAddress());
    }

    public boolean checkDevice(BluetoothLeDevice device){
        return mDeviceMap.containsKey(device.getAddress());
    }
    public void clear() {
        mDeviceMap.clear();
    }

    public Map<String, BluetoothLeDevice> getDeviceMap() {
        return mDeviceMap;
    }

    public List<BluetoothLeDevice> getDeviceList() {
        final List<BluetoothLeDevice> methodResult = new ArrayList<>(mDeviceMap.values());

        Collections.sort(methodResult, new Comparator<BluetoothLeDevice>() {

            @Override
            public int compare(final BluetoothLeDevice arg0, final BluetoothLeDevice arg1) {
                return arg0.getAddress().compareToIgnoreCase(arg1.getAddress());
            }
        });

        return methodResult;
    }

    @Override
    public String toString() {
        return "BluetoothLeDeviceGroup{" +
                "mDeviceMap=" + mDeviceMap +
                '}';
    }
}
