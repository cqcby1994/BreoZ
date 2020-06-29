package com.breo.baseble.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.breo.baseble.breoble.GattAttributes;
import com.breo.baseble.breoble.msg.MsgParser;
import com.breo.baseble.breoble.msg.base.BaseSensorMsg;
import com.breo.baseble.breoble.msg.base.PackageMsg;
import com.breo.baseble.breoble.msg.iNeck3sSensorMsg.cmd.NeckHotCmd;
import com.breo.baseble.breoble.msg.idreamSensorMsg.cmd.RunMessTypeCmd;
import com.breo.baseble.breoble.msgbean.ID5SRunBean;
import com.breo.baseble.breoble.msgbean.MsgFactory;
import com.breo.baseble.callback.IMusicMsgChangeCallback;
import com.breo.baseble.callback.INotifyCallback;
import com.breo.baseble.callback.ISendCallback;
import com.breo.baseble.exception.GattException;
import com.breo.baseble.BreoBle;
import com.breo.baseble.callback.IBleCallback;
import com.breo.baseble.callback.IConnectCallback;
import com.breo.baseble.callback.IRssiCallback;
import com.breo.baseble.common.BleConfig;
import com.breo.baseble.common.BleConstant;
import com.breo.baseble.common.ConnectState;
import com.breo.baseble.common.PropertyType;
import com.breo.baseble.exception.BleException;
import com.breo.baseble.exception.ConnectException;
import com.breo.baseble.exception.TimeoutException;
import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.baseble.utils.BreoLog;
import com.breo.baseble.utils.HexUtil;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_BREDR;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static com.breo.baseble.common.BleConstant.MSG_BREO_SHAKE_TIMEOUT;
import static com.breo.baseble.common.BleConstant.MSG_CONNECT_RETRY;
import static com.breo.baseble.common.BleConstant.MSG_CONNECT_TIMEOUT;
import static com.breo.baseble.common.BleConstant.MSG_READ_DATA_RETRY;
import static com.breo.baseble.common.BleConstant.MSG_READ_DATA_TIMEOUT;
import static com.breo.baseble.common.BleConstant.MSG_RECEIVE_DATA_RETRY;
import static com.breo.baseble.common.BleConstant.MSG_RECEIVE_DATA_TIMEOUT;
import static com.breo.baseble.common.BleConstant.MSG_WRITE_DATA_RETRY;
import static com.breo.baseble.common.BleConstant.MSG_WRITE_DATA_TIMEOUT;
import static com.breo.baseble.common.BleExceptionCode.PARAMETER_ERR;

/**
 * 设备镜像（设备连接成功后返回的设备信息模型）
 * .
 */
public class BreoDevice {
    private final BreoDevice breoDevice;
    private final String uniqueSymbol;//唯一符号
    private final BluetoothLeDevice bluetoothLeDevice;//设备基础信息

    private BluetoothGatt bluetoothGatt;//蓝牙GATT
    private IRssiCallback rssiCallback;//获取信号值回调
    private IConnectCallback connectCallback;//连接回调
    private int connectRetryCount = 0;//当前连接重试次数
    private int writeDataRetryCount = 0;//当前写入数据重试次数
    private int readDataRetryCount = 0;//当前读取数据重试次数
    private int receiveDataRetryCount = 0;//当前接收数据重试次数
    private boolean isActiveDisconnect = false;//是否主动断开连接
    private boolean isIndication;//是否是指示器方式
    private boolean enable;//是否设置使能
    private byte[] writeData;//写入数据
    private ConnectState connectState = ConnectState.CONNECT_INIT;//设备状态描述
    private volatile HashMap<String, BluetoothGattChannel> writeInfoMap = new HashMap<>();//写入数据GATT信息集合
    private volatile HashMap<String, BluetoothGattChannel> readInfoMap = new HashMap<>();//读取数据GATT信息集合
    private volatile HashMap<String, BluetoothGattChannel> enableInfoMap = new HashMap<>();//设置使能GATT信息集合
    private volatile HashMap<String, IBleCallback> bleCallbackMap = new HashMap<>();//数据操作回调集合
    private volatile HashMap<String, IBleCallback> receiveCallbackMap = new HashMap<>();//数据接收回调集合


    private ISendCallback sendCallback;
    private INotifyCallback notifyCallback;
    private IMusicMsgChangeCallback musicMsgChangeCallback;
    private boolean isWriteInit = false;
    private boolean isNotifyInit = false;

    public boolean isOnlyBreoDevice = false;
    //数据包的队列
    private Queue<byte[]> dataInfoQueue = new LinkedList<>();

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CONNECT_TIMEOUT) {
                connectFailure(new TimeoutException());
            } else if (msg.what == MSG_CONNECT_RETRY) {
                connect();
            } else if (msg.what == MSG_WRITE_DATA_TIMEOUT) {
                writeFailure(new TimeoutException(), true);
            } else if (msg.what == MSG_WRITE_DATA_RETRY) {
                write(writeData);
//            } else if (msg.what == MSG_READ_DATA_TIMEOUT) {
//                readFailure(new TimeoutException(), true);
//            } else if (msg.what == MSG_READ_DATA_RETRY) {
//                read();
            } else if (msg.what == MSG_RECEIVE_DATA_TIMEOUT) {
                enableFailure(new TimeoutException(), true);
            } else if (msg.what == MSG_RECEIVE_DATA_RETRY) {
                enable(enable, isIndication);
            } else if (msg.what == MSG_BREO_SHAKE_TIMEOUT) {
                handFailure(new TimeoutException());
            }
        }
    };

    /**
     * 蓝牙所有相关操作的核心回调类
     */
    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {

        /**
         * 连接状态改变，主要用来分析设备的连接与断开
         * @param gatt GATT
         * @param status 改变前状态
         * @param newState 改变后状态
         */
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            BreoLog.i("onConnectionStateChange  status: " + status + "," + newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        gatt.discoverServices();
//
//                    }
//                }, 600);
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
//                close();
                    clear();
                if (connectCallback != null) {
                    handler.removeCallbacksAndMessages(null);
                    BreoBle.getInstance().getBreoDevicePool().removeDeviceMirror(breoDevice);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        connectState = ConnectState.CONNECT_DISCONNECT;
                        connectCallback.onDisconnect();
                    } else {
                        connectState = ConnectState.CONNECT_FAILURE;
                        connectCallback.onConnectFailure(new ConnectException(gatt, status));
                    }
                }
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                connectState = ConnectState.CONNECT_PROCESS;
            }
        }

        /**
         * 发现服务，主要用来获取设备支持的服务列表
         * @param gatt GATT
         * @param status 当前状态
         */
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            BreoLog.i("onServicesDiscovered  status: " + status + "  ,thread: " + Thread.currentThread());
            handler.removeMessages(MSG_CONNECT_TIMEOUT);
            if (status == 0) {
                bluetoothGatt = gatt;
                BreoLog.i("connectSuccess" + isOnlyBreoDevice+"bluetoothGatt="+bluetoothGatt);
                if (isOnlyBreoDevice) {
                    //初始化Breo通知和写入服务
                    initBreoBleNotifyService();
                    initBreoBleWriteService();
                    writeData(MsgFactory.getHelloData());
                    handler.removeMessages(MSG_BREO_SHAKE_TIMEOUT);
                    handler.sendEmptyMessageDelayed(MSG_BREO_SHAKE_TIMEOUT, BleConfig.getInstance().getConnectTimeout());
                } else {
                    connectState = ConnectState.CONNECT_SUCCESS;

                    if (connectCallback != null) {
                        isActiveDisconnect = false;
                        BreoBle.getInstance().getBreoDevicePool().addBreoDevice(breoDevice);
                        //Log.i("cqc", "deviceMirror=" + breoDevice.toString());

                        connectCallback.onConnectSuccess(breoDevice);
                    }
                }
            } else {
                connectFailure(new ConnectException(gatt, status));
            }
        }

        /**
         * 读取特征值，主要用来读取该特征值包含的可读信息
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            BreoLog.i("onCharacteristicRead  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()) +
                    "  ,thread: " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSuccessData(readInfoMap, characteristic.getValue(), status, true);
            } else {
                readFailure(new GattException(status), true);
            }
        }

        /**
         * 写入特征值，主要用来发送数据到设备
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            BreoLog.i("onCharacteristicWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()) +
                    "  ,thread: " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSuccessData(writeInfoMap, characteristic.getValue(), status, false);
            } else {
                writeFailure(new GattException(status), true);
            }
        }

        /**
         * 特征值改变，主要用来接收设备返回的数据信息
         * @param gatt GATT
         * @param characteristic 特征值
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            BreoLog.i("onCharacteristicChanged data:" + HexUtil.encodeHexStr(characteristic.getValue()) +
                    "  ,thread: " + Thread.currentThread());
            BreoLog.i("receiveCallbackMap=" + receiveCallbackMap.toString());
            BreoLog.i("enableInfoMap=" + enableInfoMap.size());

            for (Map.Entry<String, IBleCallback> receiveEntry : receiveCallbackMap.entrySet()) {
                String receiveKey = receiveEntry.getKey();
                IBleCallback receiveValue = receiveEntry.getValue();
                // TODO: 2019/11/20  enableInfoMap可能会为空，后期查找优化
                if (enableInfoMap.size() > 0) {
                    for (Map.Entry<String, BluetoothGattChannel> gattInfoEntry : enableInfoMap.entrySet()) {
                        String bluetoothGattInfoKey = gattInfoEntry.getKey();
                        BluetoothGattChannel bluetoothGattInfoValue = gattInfoEntry.getValue();
                        if (receiveKey.equals(bluetoothGattInfoKey)) {
                            receiveValue.onSuccess(characteristic.getValue(), bluetoothGattInfoValue, bluetoothLeDevice);
                        }
                    }
                } else {
                    receiveValue.onSuccess(characteristic.getValue(), null, bluetoothLeDevice);

                }
            }
        }

        /**
         * 读取属性描述值，主要用来获取设备当前属性描述的值
         * @param gatt GATT
         * @param descriptor 属性描述
         * @param status 当前状态
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            BreoLog.i("onDescriptorRead  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()) +
                    "  ,thread: " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSuccessData(readInfoMap, descriptor.getValue(), status, true);
            } else {
                readFailure(new GattException(status), true);
            }
        }

        /**
         * 写入属性描述值，主要用来根据当前属性描述值写入数据到设备
         * @param gatt GATT
         * @param descriptor 属性描述值
         * @param status 当前状态
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            BreoLog.i("onDescriptorWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()) +
                    "  ,thread: " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSuccessData(writeInfoMap, descriptor.getValue(), status, false);
            } else {
                writeFailure(new GattException(status), true);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleSuccessData(enableInfoMap, descriptor.getValue(), status, false);
            } else {
                enableFailure(new GattException(status), true);
            }
        }

        /**
         * 阅读设备信号值
         * @param gatt GATT
         * @param rssi 设备当前信号
         * @param status 当前状态
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            BreoLog.i("onReadRemoteRssi  status: " + status + ", rssi:" + rssi +
                    "  ,thread: " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (rssiCallback != null) {
                    rssiCallback.onSuccess(rssi);
                }
            } else {
                if (rssiCallback != null) {
                    rssiCallback.onFailure(new GattException(status));
                }
            }
        }
    };
    private long time = 0;

    public BreoDevice(BluetoothLeDevice bluetoothLeDevice) {
        breoDevice = this;
        this.bluetoothLeDevice = bluetoothLeDevice;
        this.uniqueSymbol = bluetoothLeDevice.getAddress() + bluetoothLeDevice.getName();
    }

    /**
     * 连接设备
     *
     * @param connectCallback
     */
    public synchronized void connect(IConnectCallback connectCallback) {
        if (connectState == ConnectState.CONNECT_SUCCESS || connectState == ConnectState.CONNECT_PROCESS
                || (connectState == ConnectState.CONNECT_INIT && connectRetryCount != 0)) {
            BreoLog.e("this connect state is connecting, connectSuccess or current retry count less than config connect retry count.");
            return;
        }
        handler.removeCallbacksAndMessages(null);
        this.connectCallback = connectCallback;
        connectRetryCount = 0;
        connect();
    }

    /**
     * 绑定一个具备读写或可通知能力的通道，设置需要操作数据的相关信息，包含：数据操作回调，数据操作类型，数据通道建立所需的UUID。
     *
     * @param bleCallback
     * @param bluetoothGattChannel
     */
    public synchronized void bindChannel(IBleCallback bleCallback, BluetoothGattChannel bluetoothGattChannel) {
        if (bleCallback != null && bluetoothGattChannel != null) {
            String key = bluetoothGattChannel.getGattInfoKey();
            PropertyType propertyType = bluetoothGattChannel.getPropertyType();
            if (!bleCallbackMap.containsKey(key)) {
                bleCallbackMap.put(key, bleCallback);
            }
            if (propertyType == PropertyType.PROPERTY_READ) {
                if (!readInfoMap.containsKey(key)) {
                    readInfoMap.put(key, bluetoothGattChannel);
                }
            } else if (propertyType == PropertyType.PROPERTY_WRITE) {
                if (!writeInfoMap.containsKey(key)) {
                    writeInfoMap.put(key, bluetoothGattChannel);
                }
            } else if (propertyType == PropertyType.PROPERTY_NOTIFY) {
                if (!enableInfoMap.containsKey(key)) {
                    enableInfoMap.put(key, bluetoothGattChannel);
                }
            } else if (propertyType == PropertyType.PROPERTY_INDICATE) {
                if (!enableInfoMap.containsKey(key)) {
                    enableInfoMap.put(key, bluetoothGattChannel);
                }
            }
        }
    }

    /**
     * 解绑通道
     *
     * @param bluetoothGattChannel
     */
    public synchronized void unbindChannel(BluetoothGattChannel bluetoothGattChannel) {
        if (bluetoothGattChannel != null) {
            String key = bluetoothGattChannel.getGattInfoKey();
            bleCallbackMap.remove(key);
            if (readInfoMap.containsKey(key)) {
                readInfoMap.remove(key);
            } else if (writeInfoMap.containsKey(key)) {
                writeInfoMap.remove(key);
            } else enableInfoMap.remove(key);
        }
    }

    /**
     * 写入数据
     *
     * @param data
     */
    public boolean writeData(byte[] data) {

        if (!checkBluetoothGattInfo(writeInfoMap)) {
            return false;
        }
        handler.removeMessages(MSG_WRITE_DATA_TIMEOUT);
        handler.removeMessages(MSG_WRITE_DATA_RETRY);
        writeDataRetryCount = 0;
        writeData = data;
        boolean s = write(data);
        Log.d("cqc writeData", s + "--与上一包的间隔：" + (System.currentTimeMillis() - time) + "，-->" + HexUtil.toHexString(data));

        time = System.currentTimeMillis();
//        if (!s){
//            Log.d("cqc writeData", s + "--第一次发送失败，重新绑定通道再试"+ "，-->" + HexUtil.toHexString(data));
//
//            isWriteInit = false;
//            initBreoBleWriteService();
//            s = write(data);
//        }

        return s;

    }

    /**
     * 读取数据
     */
    public void readData() {
        if (!checkBluetoothGattInfo(readInfoMap)) {
            return;
        }
        if (handler != null) {
            handler.removeMessages(MSG_READ_DATA_TIMEOUT);
            handler.removeMessages(MSG_READ_DATA_RETRY);
        }
        readDataRetryCount = 0;
        read();
    }

    /**
     * 获取设备信号值
     *
     * @param rssiCallback
     */
    public void readRemoteRssi(IRssiCallback rssiCallback) {
        this.rssiCallback = rssiCallback;
        if (bluetoothGatt != null) {
            bluetoothGatt.readRemoteRssi();
        }
    }

    /**
     * 注册获取数据通知
     *
     * @param isIndication
     */
    public void registerNotify(boolean isIndication) {
        if (!checkBluetoothGattInfo(enableInfoMap)) {
            return;
        }
        if (handler != null) {
            handler.removeMessages(MSG_RECEIVE_DATA_TIMEOUT);
            handler.removeMessages(MSG_RECEIVE_DATA_RETRY);
        }
        receiveDataRetryCount = 0;
        enable = true;
        this.isIndication = isIndication;
        enable(true, this.isIndication);
    }

    /**
     * 取消获取数据通知
     *
     * @param isIndication
     */
    public void unregisterNotify(boolean isIndication) {
        if (!checkBluetoothGattInfo(enableInfoMap)) {
            return;
        }
        if (handler != null) {
            handler.removeMessages(MSG_RECEIVE_DATA_TIMEOUT);
            handler.removeMessages(MSG_RECEIVE_DATA_RETRY);
        }
        enable = false;
        this.isIndication = isIndication;
        enable(enable, this.isIndication);
    }

    /**
     * 设置接收数据监听
     *
     * @param key             接收数据回调key，由serviceUUID+characteristicUUID+descriptorUUID组成
     * @param receiveCallback 接收数据回调
     */
    public void setNotifyListener(String key, IBleCallback receiveCallback) {
        receiveCallbackMap.put(key, receiveCallback);
    }

    /**
     * 获取当前连接失败重试次数
     *
     * @return //
     */
    public int getConnectRetryCount() {
        return connectRetryCount;
    }

    /**
     * 获取当前读取数据失败重试次数
     *
     * @return
     */
    public int getReadDataRetryCount() {
        return readDataRetryCount;
    }

    /**
     * 获取当前使能数据失败重试次数
     *
     * @return
     */
    public int getReceiveDataRetryCount() {
        return receiveDataRetryCount;
    }

    /**
     * 获取当前写入数据失败重试次数
     *
     * @return
     */
    public int getWriteDataRetryCount() {
        return writeDataRetryCount;
    }

    /**
     * 获取设备唯一标识
     */
    public String getUniqueSymbol() {
        return uniqueSymbol;
    }

    /**
     * 获取蓝牙GATT
     *
     * @return 返回蓝牙GATT
     */
    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    /**
     * 获取设备连接状态
     *
     * @return 返回设备连接状态
     */
    public ConnectState getConnectState() {
        return connectState;
    }

    /**
     * 获取服务列表
     */
    public List<BluetoothGattService> getGattServiceList() {
        if (bluetoothGatt != null) {
            return bluetoothGatt.getServices();
        }
        return null;
    }

    /**
     * 根据服务UUID获取指定服务
     *
     * @param serviceUuid
     */
    public BluetoothGattService getGattService(UUID serviceUuid) {
        if (bluetoothGatt != null && serviceUuid != null) {
            return bluetoothGatt.getService(serviceUuid);
        }
        return null;
    }

    /**
     * 获取某个服务的特征值列表
     */
    public List<BluetoothGattCharacteristic> getGattCharacteristicList(UUID serviceUuid) {
        if (getGattService(serviceUuid) != null && serviceUuid != null) {
            return getGattService(serviceUuid).getCharacteristics();
        }
        return null;
    }

    /**
     * 根据特征值UUID获取某个服务的指定特征值
     */
    public BluetoothGattCharacteristic getGattCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
        if (getGattService(serviceUuid) != null && serviceUuid != null && characteristicUuid != null) {
            return getGattService(serviceUuid).getCharacteristic(characteristicUuid);
        }
        return null;
    }

    /**
     * 获取某个特征值的描述属性列表
     *
     * @param serviceUuid
     * @param characteristicUuid
     * @return
     */
    public List<BluetoothGattDescriptor> getGattDescriptorList(UUID serviceUuid, UUID characteristicUuid) {
        if (getGattCharacteristic(serviceUuid, characteristicUuid) != null && serviceUuid != null && characteristicUuid != null) {
            return getGattCharacteristic(serviceUuid, characteristicUuid).getDescriptors();
        }
        return null;
    }

    /**
     * 根据描述属性UUID获取某个特征值的指定属性值
     *
     * @param serviceUuid
     * @param characteristicUuid
     * @param descriptorUuid
     * @return
     */
    public BluetoothGattDescriptor getGattDescriptor(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
        if (getGattCharacteristic(serviceUuid, characteristicUuid) != null && serviceUuid != null && characteristicUuid != null && descriptorUuid != null) {
            return getGattCharacteristic(serviceUuid, characteristicUuid).getDescriptor(descriptorUuid);
        }
        return null;
    }

    /**
     * 获取设备详细信息
     *
     * @return
     */
    public BluetoothLeDevice getBluetoothLeDevice() {
        return bluetoothLeDevice;
    }

    /**
     * 设备是否连接
     *
     * @return
     */
    public boolean isConnected() {
        return connectState == ConnectState.CONNECT_SUCCESS;
    }

    /**
     * 移除数据操作回调
     *
     * @param key
     */
    public synchronized void removeBleCallback(String key) {
        if (bleCallbackMap.containsKey(key)) {
            bleCallbackMap.remove(key);
        }
    }

    /**
     * 移除接收数据回调
     *
     * @param key
     */
    public synchronized void removeReceiveCallback(String key) {
        if (receiveCallbackMap.containsKey(key)) {
            receiveCallbackMap.remove(key);
        }
    }

    /**
     * 移除所有回调
     */
    public synchronized void removeAllCallback() {
        bleCallbackMap.clear();
        receiveCallbackMap.clear();
        if (musicMsgChangeCallback != null) {
            musicMsgChangeCallback = null;
        }
        if (sendCallback != null) {
            sendCallback = null;
        }
        if (notifyCallback != null) {
            notifyCallback = null;
        }

    }

    /**
     * 刷新设备缓存
     *
     * @return 返回是否刷新成功
     */
    public synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && bluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                BreoLog.i("Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            BreoLog.e("An exception occured while refreshing device" + e);
        }
        return false;
    }


    public synchronized void removeForSystem(){
//        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice("另一个ble 或者 蓝牙设备mac值");
//        if (remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
//            try {
//                Method removeBond = remoteDevice.getClass().getDeclaredMethod("removeBond");
//                removeBond.invoke(remoteDevice);
//                RLog.d(TAG , "成功移除系统bug");
//            } catch (Exception e) {
//                RLog.e(TAG , "反射异常");
//            }
//        }
    }

    /**
     * 主动断开设备连接
     */
    public synchronized void disconnect() {
        connectState = ConnectState.CONNECT_INIT;
        connectRetryCount = 0;
        if (bluetoothGatt != null) {
            isActiveDisconnect = true;
            BreoLog.i("bluetoothGatt.disconnect");

            bluetoothGatt.disconnect();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

    }

    /**
     * 关闭GATT
     */
    public synchronized void close() {
        if (bluetoothGatt != null) {
            BreoLog.i("bluetoothGatt.close");

            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }


    /**
     * 清除设备资源，在不使用该设备时调用
     */
    public synchronized void clear() {
        BreoLog.i("device clean");
        disconnect();
        refreshDeviceCache();
        close();
        if (bleCallbackMap != null) {
            bleCallbackMap.clear();
        }
        if (receiveCallbackMap != null) {
            receiveCallbackMap.clear();
        }
        if (writeInfoMap != null) {
            writeInfoMap.clear();
        }
        if (readInfoMap != null) {
            readInfoMap.clear();
        }
        if (enableInfoMap != null) {
            enableInfoMap.clear();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (musicMsgChangeCallback != null) {
            musicMsgChangeCallback = null;
        }
        if (sendCallback != null) {
            sendCallback = null;
        }
        if (notifyCallback != null) {
            notifyCallback = null;
        }
    }

    /**
     * UUID转换
     *
     * @param uuid
     * @return 返回UUID
     */
    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    /**
     * 检查BluetoothGattChannel集合是否有值
     *
     * @param bluetoothGattInfoHashMap
     * @return
     */
    private boolean checkBluetoothGattInfo(HashMap<String, BluetoothGattChannel> bluetoothGattInfoHashMap) {
        if (bluetoothGattInfoHashMap == null || bluetoothGattInfoHashMap.size() == 0) {
            BreoLog.e("this bluetoothGattInfo map is not value.");
            return false;
        }
        return true;
    }


    /**
     * 连接设备
     */
    private synchronized void connect() {
        if (handler != null) {
            handler.removeMessages(MSG_CONNECT_TIMEOUT);
            handler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, BleConfig.getInstance().getConnectTimeout());
        }
        connectState = ConnectState.CONNECT_PROCESS;
        if (bluetoothLeDevice != null && bluetoothLeDevice.getDevice() != null) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    bluetoothLeDevice.getDevice().connectGatt(BreoBle.getInstance().getContext(), false, coreGattCallback);
                }
            }, 600);

        }
    }

    /**
     * 设置使能
     *
     * @param enable       是否具备使能
     * @param isIndication 是否是指示器方式
     * @return
     */
    private synchronized boolean enable(boolean enable, boolean isIndication) {
        if (handler != null) {
            handler.removeMessages(MSG_RECEIVE_DATA_TIMEOUT);
            handler.sendEmptyMessageDelayed(MSG_RECEIVE_DATA_TIMEOUT, BleConfig.getInstance().getOperateTimeout());
        }
        boolean success = false;
        for (Map.Entry<String, BluetoothGattChannel> entry : enableInfoMap.entrySet()) {
            String bluetoothGattInfoKey = entry.getKey();
            BluetoothGattChannel bluetoothGattInfoValue = entry.getValue();
            if (bluetoothGatt != null && bluetoothGattInfoValue.getCharacteristic() != null) {
                BreoLog.d("enable-" + bluetoothGattInfoValue.getCharacteristic().getUuid().toString());

                success = bluetoothGatt.setCharacteristicNotification(bluetoothGattInfoValue.getCharacteristic(), enable);
            }
            BluetoothGattDescriptor bluetoothGattDescriptor = null;
            if (bluetoothGattInfoValue.getCharacteristic() != null && bluetoothGattInfoValue.getDescriptor() != null) {
                bluetoothGattDescriptor = bluetoothGattInfoValue.getDescriptor();
            } else if (bluetoothGattInfoValue.getCharacteristic() != null && bluetoothGattInfoValue.getDescriptor() == null) {
                if (bluetoothGattInfoValue.getCharacteristic().getDescriptors() != null
                        && bluetoothGattInfoValue.getCharacteristic().getDescriptors().size() == 1) {
                    bluetoothGattDescriptor = bluetoothGattInfoValue.getCharacteristic().getDescriptors().get(0);
                } else {
                    bluetoothGattDescriptor = bluetoothGattInfoValue.getCharacteristic()
                            .getDescriptor(UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG));
                }
            }
            if (bluetoothGattDescriptor != null) {
                bluetoothGattInfoValue.setDescriptor(bluetoothGattDescriptor);
                if (isIndication) {
                    if (enable) {
                        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    } else {
                        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                } else {
                    if (enable) {
                        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else {
                        bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                }
                if (bluetoothGatt != null) {
                    bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
                }
            }
        }

        if (!success) {
//            if (notifyCallback !=null){
//                BleException exception = new BleException(PARAMETER_ERR,"enable_err");
//                notifyCallback.onError(exception);
//            }
        }
        BreoLog.d("enable-" + success);
        return success;
    }

    /**
     * 读取数据
     *
     * @return
     */
    private synchronized boolean read() {
        if (handler != null) {
            handler.removeMessages(MSG_READ_DATA_TIMEOUT);
            handler.sendEmptyMessageDelayed(MSG_READ_DATA_TIMEOUT, BleConfig.getInstance().getOperateTimeout());
        }
        boolean success = false;
        for (Map.Entry<String, BluetoothGattChannel> entry : readInfoMap.entrySet()) {
            String bluetoothGattInfoKey = entry.getKey();
            BluetoothGattChannel bluetoothGattInfoValue = entry.getValue();
            if (bluetoothGatt != null && bluetoothGattInfoValue.getCharacteristic() != null && bluetoothGattInfoValue.getDescriptor() != null) {
                success = bluetoothGatt.readDescriptor(bluetoothGattInfoValue.getDescriptor());
            } else if (bluetoothGatt != null && bluetoothGattInfoValue.getCharacteristic() != null && bluetoothGattInfoValue.getDescriptor() == null) {
                success = bluetoothGatt.readCharacteristic(bluetoothGattInfoValue.getCharacteristic());
            }
        }
        return success;
    }

    /**
     * 写入数据
     *
     * @param data
     * @return
     */
    private synchronized boolean write(byte[] data) {
        if (handler != null) {
            handler.removeMessages(MSG_WRITE_DATA_TIMEOUT);
            handler.sendEmptyMessageDelayed(MSG_WRITE_DATA_TIMEOUT, BleConfig.getInstance().getOperateTimeout());
        }
        boolean success = false;
        for (Map.Entry<String, BluetoothGattChannel> entry : writeInfoMap.entrySet()) {
            String bluetoothGattInfoKey = entry.getKey();
            BluetoothGattChannel bluetoothGattInfoValue = entry.getValue();
            if (bluetoothGatt != null && bluetoothGattInfoValue.getCharacteristic() != null && bluetoothGattInfoValue.getDescriptor() != null) {
                bluetoothGattInfoValue.getDescriptor().setValue(data);
                Log.d("zkx", "111111111111");
                success = bluetoothGatt.writeDescriptor(bluetoothGattInfoValue.getDescriptor());
            } else if (bluetoothGatt != null && bluetoothGattInfoValue.getCharacteristic() != null && bluetoothGattInfoValue.getDescriptor() == null) {
                bluetoothGattInfoValue.getCharacteristic().setValue(data);
                success = bluetoothGatt.writeCharacteristic(bluetoothGattInfoValue.getCharacteristic());
                Log.d("zkx", "2222222222222" + bluetoothGatt + "," + bluetoothGattInfoValue.toString() + "success=" + success);

            }
        }
        return success;
    }

    /**
     * 连接失败处理
     *
     * @param bleException 回调异常
     */
    private void connectFailure(BleException bleException) {
        if (connectRetryCount < BleConfig.getInstance().getConnectRetryCount()) {
            connectRetryCount++;
            if (handler != null) {
                handler.removeMessages(MSG_CONNECT_TIMEOUT);
                handler.sendEmptyMessageDelayed(MSG_CONNECT_RETRY, BleConfig.getInstance().getConnectRetryInterval());
            }
            //BreoLog.i("connectFailure connectRetryCount is " + connectRetryCount);
        } else {
            if (bleException instanceof TimeoutException) {
                connectState = ConnectState.CONNECT_TIMEOUT;
            } else {
                connectState = ConnectState.CONNECT_FAILURE;
            }
            close();
            if (connectCallback != null) {
                connectCallback.onConnectFailure(bleException);
            }
            BreoLog.i("connectError" + bleException);
        }
    }

    /**
     * 握手失败处理
     *
     * @param bleException 回调异常
     */
    private void handFailure(BleException bleException) {
        if (connectRetryCount < BleConfig.getInstance().getConnectRetryCount()) {
            connectRetryCount++;
            if (handler != null) {
                handler.removeMessages(MSG_CONNECT_TIMEOUT);
                handler.sendEmptyMessageDelayed(MSG_CONNECT_RETRY, BleConfig.getInstance().getConnectRetryInterval());
            }
            //BreoLog.i("connectFailure connectRetryCount is " + connectRetryCount);
        } else {
            if (bleException instanceof TimeoutException) {
                connectState = ConnectState.CONNECT_TIMEOUT;
            } else {
                connectState = ConnectState.CONNECT_FAILURE;
            }
            disconnect();
            if (connectCallback != null) {
                connectCallback.onNotBreoDevice("Not a Breo device");
            }
            //BreoLog.i("connectFailure " + bleException);
        }
    }

    /**
     * 使能失败
     *
     * @param bleException
     * @param isRemoveCall
     */
    private void enableFailure(BleException bleException, boolean isRemoveCall) {
        if (receiveDataRetryCount < BleConfig.getInstance().getOperateRetryCount()) {
            receiveDataRetryCount++;
            if (handler != null) {
                handler.removeMessages(MSG_RECEIVE_DATA_TIMEOUT);
                handler.sendEmptyMessageDelayed(MSG_RECEIVE_DATA_RETRY, BleConfig.getInstance().getOperateRetryInterval());
            }
            //BreoLog.i("enableFailure receiveDataRetryCount is " + receiveDataRetryCount);
        } else {
            handleFailureData(enableInfoMap, bleException, isRemoveCall);
            // BreoLog.i("enableFailure " + bleException);
        }
    }

    /**
     * 读取数据失败
     *
     * @param bleException
     * @param isRemoveCall
     */
    private void readFailure(BleException bleException, boolean isRemoveCall) {
        if (readDataRetryCount < BleConfig.getInstance().getOperateRetryCount()) {
            readDataRetryCount++;
            if (handler != null) {
                handler.removeMessages(MSG_READ_DATA_TIMEOUT);
                handler.sendEmptyMessageDelayed(MSG_READ_DATA_RETRY, BleConfig.getInstance().getOperateRetryInterval());
            }
        } else {
            handleFailureData(readInfoMap, bleException, isRemoveCall);
            BreoLog.i("readError" + bleException);
        }
    }

    /**
     * 写入数据失败
     *
     * @param bleException
     * @param isRemoveCall
     */
    private void writeFailure(BleException bleException, boolean isRemoveCall) {
        if (writeDataRetryCount < BleConfig.getInstance().getOperateRetryCount()) {
            writeDataRetryCount++;
            if (handler != null) {
                handler.removeMessages(MSG_WRITE_DATA_TIMEOUT);
                handler.sendEmptyMessageDelayed(MSG_WRITE_DATA_RETRY, BleConfig.getInstance().getOperateRetryInterval());
            }
        } else {
            handleFailureData(writeInfoMap, bleException, isRemoveCall);
            BreoLog.i("writeError" + bleException);
        }
    }

    /**
     * 处理数据发送成功
     *
     * @param bluetoothGattInfoHashMap
     * @param value                    待发送数据
     * @param status                   发送数据状态
     * @param isRemoveCall             是否需要移除回调
     */
    private synchronized void handleSuccessData(HashMap<String, BluetoothGattChannel> bluetoothGattInfoHashMap, byte[] value, int status,
                                                boolean isRemoveCall) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        String removeBleCallbackKey = null;
        String removeBluetoothGattInfoKey = null;
        for (Map.Entry<String, IBleCallback> callbackEntry : bleCallbackMap.entrySet()) {
            String bleCallbackKey = callbackEntry.getKey();
            IBleCallback bleCallbackValue = callbackEntry.getValue();
            for (Map.Entry<String, BluetoothGattChannel> gattInfoEntry : bluetoothGattInfoHashMap.entrySet()) {
                String bluetoothGattInfoKey = gattInfoEntry.getKey();
                BluetoothGattChannel bluetoothGattInfoValue = gattInfoEntry.getValue();
                if (bleCallbackKey.equals(bluetoothGattInfoKey)) {
                    bleCallbackValue.onSuccess(value, bluetoothGattInfoValue, bluetoothLeDevice);
                    removeBleCallbackKey = bleCallbackKey;
                    removeBluetoothGattInfoKey = bluetoothGattInfoKey;
                }
            }
        }
        synchronized (bleCallbackMap) {
            if (isRemoveCall && removeBleCallbackKey != null && removeBluetoothGattInfoKey != null) {
                bleCallbackMap.remove(removeBleCallbackKey);
                bluetoothGattInfoHashMap.remove(removeBluetoothGattInfoKey);
            }
        }
    }

    /**
     * 处理数据发送失败
     *
     * @param bluetoothGattInfoHashMap
     * @param bleExceprion             回调异常
     * @param isRemoveCall             是否需要移除回调
     */
    private synchronized void handleFailureData(HashMap<String, BluetoothGattChannel> bluetoothGattInfoHashMap, BleException bleExceprion,
                                                boolean isRemoveCall) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        String removeBleCallbackKey = null;
        String removeBluetoothGattInfoKey = null;
        for (Map.Entry<String, IBleCallback> callbackEntry : bleCallbackMap.entrySet()) {
            String bleCallbackKey = callbackEntry.getKey();
            IBleCallback bleCallbackValue = callbackEntry.getValue();
            for (Map.Entry<String, BluetoothGattChannel> gattInfoEntry : bluetoothGattInfoHashMap.entrySet()) {
                String bluetoothGattInfoKey = gattInfoEntry.getKey();
                if (bleCallbackKey.equals(bluetoothGattInfoKey)) {
                    bleCallbackValue.onFailure(bleExceprion);
                    removeBleCallbackKey = bleCallbackKey;
                    removeBluetoothGattInfoKey = bluetoothGattInfoKey;
                }
            }
        }
        synchronized (bleCallbackMap) {
            if (isRemoveCall && removeBleCallbackKey != null && removeBluetoothGattInfoKey != null) {
                bleCallbackMap.remove(removeBleCallbackKey);
                bluetoothGattInfoHashMap.remove(removeBluetoothGattInfoKey);
            }
        }
    }

    public void setBreoSendListener(ISendCallback iSendCallback) {
        this.sendCallback = iSendCallback;
    }

    public void setBreoNotifyListener(INotifyCallback iNotifyCallback) {
        BreoLog.i("setBreoNotifyListener=" + enableInfoMap.size());
        enableInfoMap.clear();
        this.notifyCallback = iNotifyCallback;
        if (enableInfoMap.size() == 0 || !isNotifyInit) {
            initBreoBleNotifyService();
        }
    }

    public void setBreoNotifyListener(INotifyCallback iNotifyCallback, int type) {
        this.notifyCallback = iNotifyCallback;
        if (enableInfoMap.size() == 0 || !isNotifyInit) {
            if (type == BreoBle.config().DEVICE_ISEEK) {
                initBreoBleNotifyService(UUID.fromString(GattAttributes.BLE_SPP_ISEEK_SERVICE_READ), UUID.fromString(GattAttributes.BLE_SPP_ISEEK_NOTIFY_CHARACTERISTIC));
            } else {
                initBreoBleNotifyService();
            }
        }
    }

    public void setMusicMsgChangeListener(IMusicMsgChangeCallback musicMsgChangeListener) {
        this.musicMsgChangeCallback = musicMsgChangeListener;

    }

    public void sayHello() {
        NeckHotCmd neckWorkCmd = new NeckHotCmd();
        neckWorkCmd.temperature = 0x2d;
        writeData(neckWorkCmd.packsPackage());
    }

    public void hotCompressController(boolean open) {
        //设置热敷开关  true:开   false：关
        sendData(MsgFactory.getTempMsg(open));

    }

    public void hotCompressController(int temp) {
        if (temp > 44 || temp < 38) {
            return;
        }
        //设置热敷温度
        sendData(MsgFactory.getTempMsg(temp));
    }

    public void kneadController(boolean open) {
        //设置穴位揉捏开关 true:开   false：关
        sendData(MsgFactory.getMotorKnead(open));
    }

    public void airModeController(int flag) {
        if (flag < 0 || flag > 3) {
            return;
        }
        //设置气压模式的开关  0:循环 1：高 2：中 3：低
        sendData(MsgFactory.getAirMode(flag));
    }

//    public void airTypeProgressController(int type, int progress) {
//        if (type < 1 || type > 3 || progress < 0 || progress > 11) {
//            return;
//        }
//        sendData(MsgFactory.getAirType(type, progress));
//    }

    public void musicController(int flag) {
        if (flag > 3 || flag < 0) {
            return;
        }
        sendData(MsgFactory.getMusicCmd(flag));

    }

    public void timeController(int time) {
        if (time == 5 || time == 10 || time == 15) {
            sendData(MsgFactory.getTimeCmd(time));
        }
    }

    public void airTypeProgressController(int values_a, int values_b, int values_c) {

        if (values_a < 0 || values_a > 11 || values_b < 0 || values_b > 11 || values_c < 0 || values_c > 11) {

            return;
        }
        sendDataForLongPkg(MsgFactory.getAllAirModeTypeMsg(values_a, values_b, values_c));

    }

    public synchronized void sendDataForLongPkg(PackageMsg packageMsg) {
        if (writeInfoMap.size() <= 0 || !isWriteInit) {
            initBreoBleWriteService();
        }
        byte[] data = packageMsg.packsPackage();
        assert data != null;


        writeData(data);

    }

    public synchronized void sendDataForLongPkg(byte[] data) {
        if (writeInfoMap.size() <= 0 || !isWriteInit) {
            initBreoBleWriteService();
        }
        writeData(data);

    }

    private synchronized void sendData(PackageMsg packageMsg) {
        if (writeInfoMap.size() <= 0 || !isWriteInit) {
            initBreoBleWriteService();
        }
        byte[] data = packageMsg.packsPackage();
        if (dataInfoQueue != null) {
            dataInfoQueue.clear();
            dataInfoQueue = splitPacketFor20Byte(data);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendSpliteData();
                }
            });
        }

    }

    public synchronized void sendData(byte[] data) {
        Log.i("cqc", "size = " + data.length);
        if (writeInfoMap.size() <= 0 || !isWriteInit) {
            initBreoBleWriteService();
        }
        if (dataInfoQueue != null) {
            dataInfoQueue.clear();
            dataInfoQueue = splitPacketFor20Byte(data);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendSpliteData();
                }
            });
        }

    }

    public synchronized void sendListData(List<byte[]> listData) {

        if (writeInfoMap.size() <= 0 || !isWriteInit) {
            initBreoBleWriteService();
        }
        if (dataInfoQueue != null) {
            dataInfoQueue.clear();
            //dataInfoQueue = splitPacketFor20Byte(data);
            for (int i = 0; i < listData.size(); i++) {
                dataInfoQueue.offer(listData.get(i));

            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendSpliteListData();
                }
            });
        }

    }

    private void sendSpliteData() {
        if (dataInfoQueue != null && !dataInfoQueue.isEmpty()) {
            if (dataInfoQueue.peek() != null) {
                writeData(dataInfoQueue.poll());
            }
            if (dataInfoQueue.peek() != null) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendSpliteData();
                    }
                }, 25);
            }
        }
    }

    private void sendSpliteListData() {
        if (dataInfoQueue != null && !dataInfoQueue.isEmpty()) {
            int retry = 0;
            if (dataInfoQueue.peek() != null) {
                byte[] data = dataInfoQueue.poll();
                boolean is_ok = false;
                is_ok = writeData(data);
                if (!is_ok) {
                    //触发重发
                    do {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            Log.d("cqc", "InterruptedException=" + e);
                            e.printStackTrace();
                        }
                        is_ok = writeData(data);
                        retry++;

                        if (retry > 3) {
                            break;
                        }
                    } while (!is_ok);
                }

            }
            if (dataInfoQueue.peek() != null) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendSpliteListData();
                    }
                }, 80);
            }
        }
    }

    public void initBreoBleWriteService() {
        //绑定写入服务
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(breoDevice.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(UUID.fromString(GattAttributes.BLE_SPP_SERVICE_WRITE))
                .setCharacteristicUUID(UUID.fromString(GattAttributes.BLE_SPP_WRITE_CHARACTERISTIC))
                .setDescriptorUUID(null)
                .builder();
        List<BluetoothGattService> tt = breoDevice.getBluetoothGatt().getServices();
        for (int i = 0; i < tt.size(); i++) {
            BreoLog.d(i + "initBreoBleWriteService=" + tt.get(i).getUuid());

        }
        if (tt.size() < 4) {
            if (notifyCallback != null) {
                BleException exception = new BleException(PARAMETER_ERR, "GattService < 4,GattService=" + tt.size());
                notifyCallback.onError(exception);
            }
        }

        breoDevice.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                BreoLog.d("initBreoBleWriteService绑定成功");
                if (sendCallback != null) {
                    sendCallback.onSuccess();
                }
            }

            @Override
            public void onFailure(BleException exception) {
                BreoLog.d("initBreoBleWriteService绑定失败" + exception);
                if (sendCallback != null) {
                    sendCallback.onError(exception);
                }

            }
        }, bluetoothGattChannel);
        isWriteInit = true;
    }

    public void initISeeKWriteService(int type) {
        if (type == BreoBle.config().DEVICE_ISEEK) {
            //绑定写入服务
            BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                    .setBluetoothGatt(breoDevice.getBluetoothGatt())
                    .setPropertyType(PropertyType.PROPERTY_WRITE)
                    .setServiceUUID(UUID.fromString(GattAttributes.BLE_SPP_ISEEK_SERVICE_WRITE))
                    .setCharacteristicUUID(UUID.fromString(GattAttributes.BLE_SPP_ISEEK_WRITE_CHARACTERISTIC))
                    .setDescriptorUUID(null)
                    .builder();
            breoDevice.bindChannel(new IBleCallback() {
                @Override
                public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                    if (sendCallback != null) {
                        sendCallback.onSuccess();
                    }
                }

                @Override
                public void onFailure(BleException exception) {
                    if (sendCallback != null) {
                        sendCallback.onError(exception);
                    }

                }
            }, bluetoothGattChannel);
            isWriteInit = true;
        }
    }

    public void initBreoBleNotifyService() {
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(breoDevice.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setServiceUUID(UUID.fromString(GattAttributes.BLE_SPP_SERVICE_READ))
                .setCharacteristicUUID(UUID.fromString(GattAttributes.BLE_SPP_NOTIFY_CHARACTERISTIC))
                .setDescriptorUUID(null)
                .builder();
        List<BluetoothGattService> tt = breoDevice.getBluetoothGatt().getServices();
        for (int i = 0; i < tt.size(); i++) {
            BreoLog.d(i + "initBreoBleNotifyService=" + tt.get(i).getUuid());

        }
        if (tt.size() < 4) {
            if (notifyCallback != null) {
                BleException exception = new BleException(PARAMETER_ERR, "GattService < 4,GattService=" + tt.size());
                notifyCallback.onError(exception);
            }
        }
        breoDevice.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {

            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, bluetoothGattChannel);
        breoDevice.registerNotify(false);
        breoDevice.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.d("cqc", "原始数据" + HexUtil.toHexString(data));
                broadcastUpdate(data);
//                PackageMsg notifyPackage = MsgParser.parseMessage(data);
//
//                if (notifyPackage != null && notifyPackage.pDeviceType == BaseSensorMsg.SYS_IDREAM_SENSOR && connectState != ConnectState.CONNECT_SUCCESS) {
//                    //握手成功 回调连接成功接口
//                    connectState = ConnectState.CONNECT_SUCCESS;
//                    handler.removeMessages(MSG_BREO_SHAKE_TIMEOUT);
//                    if (connectCallback != null) {
//                        isActiveDisconnect = false;
//                        BreoBle.getInstance().getBreoDevicePool().addBreoDevice(breoDevice);
//                        BreoLog.d("连接Breo设备成功");
//                        connectCallback.onConnectSuccess(breoDevice);
//                    }
//                } else {
//                    //系统其他上报通知
//                    broadcastUpdate(data);
//
//                }
            }

            @Override
            public void onFailure(BleException exception) {
                if (notifyCallback != null) {
                    notifyCallback.onError(exception);
                }
            }
        });
        isNotifyInit = true;
    }

    public void initBreoBleNotifyService(UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(breoDevice.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setServiceUUID(serviceUUID)
                .setCharacteristicUUID(characteristicUUID)
                .setDescriptorUUID(null)
                .builder();
        breoDevice.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {

            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, bluetoothGattChannel);
        breoDevice.registerNotify(false);
        breoDevice.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {

                PackageMsg notifyPackage = MsgParser.parseMessage(data);

                if (notifyPackage != null && notifyPackage.pDeviceType == BaseSensorMsg.SYS_IDREAM_SENSOR && connectState != ConnectState.CONNECT_SUCCESS) {
                    //握手成功 回调连接成功接口
                    connectState = ConnectState.CONNECT_SUCCESS;
                    handler.removeMessages(MSG_BREO_SHAKE_TIMEOUT);
                    if (connectCallback != null) {
                        isActiveDisconnect = false;
                        BreoBle.getInstance().getBreoDevicePool().addBreoDevice(breoDevice);
                        BreoLog.d("连接Breo设备成功");
                        connectCallback.onConnectSuccess(breoDevice);
                    }
                } else {
                    //系统其他上报通知
                    broadcastUpdate(data);

                }
            }

            @Override
            public void onFailure(BleException exception) {
                if (notifyCallback != null) {
                    notifyCallback.onError(exception);
                }
            }
        });
        isNotifyInit = true;
    }

    /**
     * 数据分包
     *
     * @param data
     * @return
     */
    private Queue<byte[]> splitPacketFor20Byte(byte[] data) {
        Queue<byte[]> dataInfoQueue = new LinkedList<>();
        if (data != null) {
            int index = 0;
            do {
                byte[] surplusData = new byte[data.length - index];
                byte[] currentData;
                System.arraycopy(data, index, surplusData, 0, data.length - index);
                if (surplusData.length <= 20) {
                    currentData = new byte[surplusData.length];
                    System.arraycopy(surplusData, 0, currentData, 0, surplusData.length);
                    index += surplusData.length;
                } else {
                    currentData = new byte[20];
                    System.arraycopy(data, index, currentData, 0, 20);
                    index += 20;
                }
                dataInfoQueue.offer(currentData);
            } while (index < data.length);
        }
        return dataInfoQueue;
    }

    private void broadcastUpdate(byte[] data) {
        if (notifyCallback != null) {
            notifyCallback.onNotify(data);
            return;

        }

        ID5SRunBean id5SRunBean = new ID5SRunBean();
//  AA BB 5B 24    01(播放状态) 0C(歌名字节数) 06(艺术家字节数) E5 BF AB E4 B9 90 E7 8E AF E5 B2 9B(歌名) 54 46 42 4F 59 53(艺术家)
        if ((byte) 0xAA == data[0] && (byte) 0xBB == data[1]) {
            String[] songMsg = MsgFactory.getSongMsg(data);
            if (songMsg != null && songMsg.length == 2) {
                if (musicMsgChangeCallback != null) {
                    musicMsgChangeCallback.change(songMsg[0], songMsg[1]);
                }

            }

        } else if (data[0] == 0x55) {
            if (notifyCallback != null) {
                notifyCallback.onNotify(data);
            }
        } else {
            PackageMsg packageMsg = MsgParser.parseMessage(data);
            if (packageMsg instanceof RunMessTypeCmd) {
                RunMessTypeCmd runMessTypeCmd = (RunMessTypeCmd) packageMsg;
                id5SRunBean.setMusic_conn_state(runMessTypeCmd.music_conn_state == 0x01);
                id5SRunBean.setTime(runMessTypeCmd.time);
                id5SRunBean.setBattery(runMessTypeCmd.battery);
                if (notifyCallback != null) {
                    notifyCallback.onNotify(data);
                }
            } else {
                BreoLog.d("broadcastUpdate_packageMsg=null");

            }

        }
    }

    @Override
    public String toString() {
        return "BreoDevice{" +
                "bluetoothLeDevice=" + bluetoothLeDevice.toString() +
                '}';
    }
}
