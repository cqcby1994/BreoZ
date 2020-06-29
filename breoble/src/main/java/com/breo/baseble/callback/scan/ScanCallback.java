package com.breo.baseble.callback.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.breo.baseble.BreoBle;
import com.breo.baseble.common.BleConfig;
import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.baseble.model.BluetoothLeDeviceGroup;

/**
 *  扫描设备回调
 */
public class ScanCallback implements BluetoothAdapter.LeScanCallback, IScanFilter {
    protected Handler handler = new Handler(Looper.myLooper());
    protected boolean isScan = true;//是否开始扫描
    protected boolean isScanning = false;//是否正在扫描
    protected BluetoothLeDeviceGroup bluetoothLeDeviceStore;//用来存储扫描到的设备
    protected IScanCallback scanCallback;//扫描结果回调

    public ScanCallback(IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        if (scanCallback == null) {
            throw new NullPointerException("this scanCallback is null!");
        }
        bluetoothLeDeviceStore = new BluetoothLeDeviceGroup();
    }

    public ScanCallback setScan(boolean scan) {
        isScan = scan;
        return this;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void scan() {
        if (isScan) {
            if (isScanning) {
                return;
            }
            bluetoothLeDeviceStore.clear();
            if (BleConfig.getInstance().getScanTimeout() > 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScanning = false;

                        if (BreoBle.getInstance().getBluetoothAdapter() != null) {
                            BreoBle.getInstance().getBluetoothAdapter().stopLeScan(ScanCallback.this);
                        }

                        if (bluetoothLeDeviceStore.getDeviceMap() != null
                                && bluetoothLeDeviceStore.getDeviceMap().size() > 0) {
                            scanCallback.onScanFinish(bluetoothLeDeviceStore);
                        } else {
                            scanCallback.onScanTimeout();
                        }
                    }
                }, BleConfig.getInstance().getScanTimeout());
            }else if (BleConfig.getInstance().getScanRepeatInterval() > 0){
                //如果超时时间设置为一直扫描（即 <= 0）,则判断是否设置重复扫描间隔
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScanning = false;

                        if (BreoBle.getInstance().getBluetoothAdapter() != null) {
                            BreoBle.getInstance().getBluetoothAdapter().stopLeScan(ScanCallback.this);
                        }

                        if (bluetoothLeDeviceStore.getDeviceMap() != null
                                && bluetoothLeDeviceStore.getDeviceMap().size() > 0) {
                            scanCallback.onScanFinish(bluetoothLeDeviceStore);
                        } else {
                            scanCallback.onScanTimeout();
                        }
                        isScanning = true;
                        if (BreoBle.getInstance().getBluetoothAdapter() != null) {
                            BreoBle.getInstance().getBluetoothAdapter().startLeScan(ScanCallback.this);
                        }
                        handler.postDelayed(this,BleConfig.getInstance().getScanRepeatInterval());
                    }
                }, BleConfig.getInstance().getScanRepeatInterval());
            }
            isScanning = true;
            if (BreoBle.getInstance().getBluetoothAdapter() != null) {
                BreoBle.getInstance().getBluetoothAdapter().startLeScan(ScanCallback.this);
            }
        } else {
            isScanning = false;
            if (BreoBle.getInstance().getBluetoothAdapter() != null) {
                BreoBle.getInstance().getBluetoothAdapter().stopLeScan(ScanCallback.this);
            }
        }
    }

    public ScanCallback removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
        bluetoothLeDeviceStore.clear();
        return this;
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(bluetoothDevice, rssi, scanRecord, System.currentTimeMillis());
        BluetoothLeDevice filterDevice = onFilter(bluetoothLeDevice);
        if (filterDevice != null) {
            if (!bluetoothLeDeviceStore.checkDevice(filterDevice)) {
                scanCallback.onDeviceFound(filterDevice);
            }
            bluetoothLeDeviceStore.addDevice(filterDevice);

        }
    }

    @Override
    public BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice) {
        return bluetoothLeDevice;
    }

}
