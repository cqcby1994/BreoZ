package com.breo.baseble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.breo.baseble.callback.IConnectCallback;
import com.breo.baseble.callback.scan.IScanCallback;
import com.breo.baseble.callback.scan.ScanCallback;
import com.breo.baseble.callback.scan.SingleFilterScanCallback;
import com.breo.baseble.common.BleConfig;
import com.breo.baseble.core.BreoDevice;
import com.breo.baseble.core.DeviceConnectionPool;
import com.breo.baseble.exception.TimeoutException;
import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.baseble.model.BluetoothLeDeviceGroup;
import com.breo.baseble.utils.BreoLog;


/**
 * BLE设备操作入口
 */
public class BreoBle {
    private Context context;//上下文
    private BluetoothManager bluetoothManager;//蓝牙管理
    private BluetoothAdapter bluetoothAdapter;//蓝牙适配器
    private DeviceConnectionPool deviceConnectionPool;//设备连接池
    private BreoDevice lastDevice;//上次操作设备镜像

    private static BreoBle instance;//入口操作管理
    private static BleConfig bleConfig = BleConfig.getInstance();

    private ScanCallback mScanCallback;

    public static boolean isDebug = true;

    /**
     * 单例方式获取蓝牙通信入口
     *
     * @return 返回ViseBluetooth
     */
    public static BreoBle getInstance() {
        if (instance == null) {
            synchronized (BreoBle.class) {
                if (instance == null) {
                    instance = new BreoBle();
                }
            }
        }
        return instance;
    }

    private BreoBle() {
    }

    /**
     * 获取配置对象，可进行相关配置的修改
     */
    public static BleConfig config() {
        return bleConfig;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        if (this.context == null && context != null) {
            this.context = context.getApplicationContext();
            bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
            deviceConnectionPool = new DeviceConnectionPool();
        }
    }

    /**
     * 开始扫描
     *
     * @param scanCallback 自定义回调
     */
    public void startScan(ScanCallback scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("ScanCallback is null");
        }
        mScanCallback = scanCallback;
        mScanCallback.setScan(true).scan();
    }

    /**
     * 停止扫描
     *
     * @param scanCallback 自定义回调
     */
    public void stopScan(ScanCallback scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("ScanCallback is null");
        }
        scanCallback.setScan(false).removeHandlerMsg().scan();
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (mScanCallback == null) {
            throw new IllegalArgumentException("ScanCallback is null");
        }
        mScanCallback.setScan(false).removeHandlerMsg().scan();
    }

    /**
     * 连接设备
     *
     * @param bluetoothLeDevice
     * @param connectCallback
     */
    public void connect(BluetoothLeDevice bluetoothLeDevice, IConnectCallback connectCallback) {
        if (bluetoothLeDevice == null || connectCallback == null) {
            BreoLog.e("This bluetoothLeDevice or connectCallback is null.");
            return;
        }

        if (deviceConnectionPool != null && !deviceConnectionPool.isContainDevice(bluetoothLeDevice)) {
            BreoDevice deviceMirror = new BreoDevice(bluetoothLeDevice);
            if (lastDevice != null && !TextUtils.isEmpty(lastDevice.getUniqueSymbol())
                    && lastDevice.getUniqueSymbol().equals(deviceMirror.getUniqueSymbol())) {
                deviceMirror = lastDevice;//防止重复创建设备镜像
            }
            deviceMirror.connect(connectCallback);
            lastDevice = deviceMirror;
        } else {
            BreoLog.i("This device is connected.");
        }
    }

    /**
     * 连接Breo设备
     *
     * @param bluetoothLeDevice
     * @param connectCallback
     */
    public void connectByBreo(BluetoothLeDevice bluetoothLeDevice, IConnectCallback connectCallback) {
        if (bluetoothLeDevice == null || connectCallback == null) {
            BreoLog.e("bluetoothLeDevice or connectCallback is null.");
            return;
        }
        if (deviceConnectionPool != null && !deviceConnectionPool.isContainDevice(bluetoothLeDevice)) {
            BreoDevice breoDevice = new BreoDevice(bluetoothLeDevice);
            breoDevice.isOnlyBreoDevice = true;
            if (lastDevice != null && !TextUtils.isEmpty(lastDevice.getUniqueSymbol())
                    && lastDevice.getUniqueSymbol().equals(breoDevice.getUniqueSymbol())) {
                breoDevice = lastDevice;//防止重复创建设备镜像
            }

            breoDevice.connect(connectCallback);
            lastDevice = breoDevice;
        } else {
            BreoLog.i("device is connected.");
        }
    }

    /**
     * 连接指定mac地址的设备
     *
     * @param mac             设备mac地址
     * @param connectCallback 连接回调
     */
    public void connectByMac(String mac, final IConnectCallback connectCallback) {
        if (mac == null || connectCallback == null) {
            BreoLog.e("This mac or connectCallback is null.");
            return;
        }
        startScan(new SingleFilterScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {

            }

            @Override
            public void onScanFinish(final BluetoothLeDeviceGroup bluetoothLeDeviceStore) {
                if (bluetoothLeDeviceStore.getDeviceList().size() > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            connect(bluetoothLeDeviceStore.getDeviceList().get(0), connectCallback);
                        }
                    });
                } else {
                    connectCallback.onConnectFailure(new TimeoutException());
                }
            }

            @Override
            public void onScanTimeout() {
                connectCallback.onConnectFailure(new TimeoutException());
            }

        }).setDeviceMac(mac));
    }

    /**
     * 连接指定设备名称的设备
     *
     * @param name            设备名称
     * @param connectCallback 连接回调
     */
    public void connectByName(String name, final IConnectCallback connectCallback) {
        if (name == null || connectCallback == null) {
            BreoLog.e("This name or connectCallback is null.");
            return;
        }
        startScan(new SingleFilterScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {

            }

            @Override
            public void onScanFinish(final BluetoothLeDeviceGroup bluetoothLeDeviceStore) {
                if (bluetoothLeDeviceStore.getDeviceList().size() > 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            connect(bluetoothLeDeviceStore.getDeviceList().get(0), connectCallback);
                        }
                    });
                } else {
                    connectCallback.onConnectFailure(new TimeoutException());
                }
            }

            @Override
            public void onScanTimeout() {
                connectCallback.onConnectFailure(new TimeoutException());
            }

        }).setDeviceName(name));
    }

    /**
     * 获取连接池中的设备镜像，如果没有连接则返回空
     */
    public BreoDevice getPoolDevice(BluetoothLeDevice bluetoothLeDevice) {
        if (deviceConnectionPool != null) {
            return deviceConnectionPool.getDeviceMirror(bluetoothLeDevice);
        }
        return null;
    }

    /**
     * 获取该设备连接状态
     *
     * @param bluetoothLeDevice
     * @return
     */
//    public ConnectState getConnectState(BluetoothLeDevice bluetoothLeDevice) {
//        if (deviceMirrorPool != null) {
//            return deviceMirrorPool.getConnectState(bluetoothLeDevice);
//        }
//        return ConnectState.CONNECT_DISCONNECT;
//    }

    /**
     * 判断该设备是否已连接
     *
     * @param bluetoothLeDevice
     */
    public boolean isConnect(BluetoothLeDevice bluetoothLeDevice) {
        if (deviceConnectionPool != null) {
            return deviceConnectionPool.isContainDevice(bluetoothLeDevice);
        }
        return false;
    }

    public boolean isConnect() {
        try {
            if (deviceConnectionPool != null) {
                return deviceConnectionPool.getDeviceList().size() > 0;
            }
        }catch (Exception e){
            return false;
        }

        return false;
    }

    /**
     * 断开某一个设备
     *
     * @param bluetoothLeDevice
     */
//    public void disconnect(BluetoothLeDevice bluetoothLeDevice) {
//        if (deviceMirrorPool != null) {
//            deviceMirrorPool.disconnect(bluetoothLeDevice);
//        }
//    }

    /**
     * 断开所有设备
     */
    public void disconnect() {
        if (deviceConnectionPool != null) {
            deviceConnectionPool.disconnect();
        }
    }

    /**
     * 清除资源，在退出应用时调用
     */
    public void clear() {
        if (lastDevice != null) {
            lastDevice = null;
        }
        if (deviceConnectionPool != null) {
            deviceConnectionPool.clear();
        }
    }

    /**
     * 获取Context
     *
     * @return 返回Context
     */
    public Context getContext() {
        return context;
    }

    /**
     * 获取蓝牙管理
     *
     * @return 返回蓝牙管理
     */
    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    /**
     * 获取蓝牙适配器
     *
     * @return 返回蓝牙适配器
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * 获取设备镜像池
     */
    public DeviceConnectionPool getBreoDevicePool() {
        return deviceConnectionPool;
    }

    /**
     * 获取当前连接失败重试次数
     *
     * @return
     */
    public int getConnectRetryCount() {
        if (lastDevice == null) {
            return 0;
        }
        return lastDevice.getConnectRetryCount();
    }

//    /**
//     * 获取当前读取数据失败重试次数
//     *
//     * @return
//     */
//    public int getReadDataRetryCount() {
//        if (lastDeviceMirror == null) {
//            return 0;
//        }
//        return lastDeviceMirror.getReadDataRetryCount();
//    }

    /**
     * 获取当前使能数据失败重试次数
     *
     * @return
     */
//    public int getReceiveDataRetryCount() {
//        if (lastDeviceMirror == null) {
//            return 0;
//        }
//        return lastDeviceMirror.getReceiveDataRetryCount();
//    }

    /**
     * 获取当前写入数据失败重试次数
     *
     * @return
     */
//    public int getWriteDataRetryCount() {
//        if (lastDeviceMirror == null) {
//            return 0;
//        }
//        return lastDeviceMirror.getWriteDataRetryCount();
//    }
}
