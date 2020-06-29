package com.breo.breoz.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.breo.baseble.BreoBle;
import com.breo.baseble.breoble.msg.Protocol;
import com.breo.baseble.callback.IConnectCallback;
import com.breo.baseble.callback.INotifyCallback;
import com.breo.baseble.core.BreoDevice;
import com.breo.baseble.exception.BleException;
import com.breo.baseble.model.BluetoothLeDevice;
import com.breo.baseble.utils.HexUtil;
import com.breo.breoz.R;
import com.breo.breoz.event.ble.BleExceptionEvent;
import com.breo.breoz.event.ble.BreoBleConEvent;
import com.breo.breoz.event.ble.BreoBleSendData;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenqc on 2019/10/23 15:41
 */
public class BreoBleService extends Service {
    public static final int STATE_DISCONNECTED = -899;
    public static final int STATE_CONNECTING = -888;
    public static final int STATE_CONNECTED = -999;
    public static final int STATE_FAILED = -777;
    public static final int STATE_NOTIFY = -778;

    //防止使能失败
    private static final int STATE_SETNOTIFY = 4;

    private final IBinder mBinder = new LocalBinder();
    private BreoBleService.BleHandler mHandler;
    private BreoBleService.MsgSendLoopHandler msgSendLoopHandler;
    private HandlerThread msgSendLoopHandlerThread;

    private BreoDevice conDevice;

    private IConnectCallback mIConnectCallback;

    private INotifyCallback mINotifyCallback;

    private static final String TAG = "BreoBleService";

    private int mNotifyCount = 0;
    private ByteBuffer buffer;

    private BluetoothStateBroadcastReceive mReceive;

    private boolean isFirstCon = false;

    private String deviceName = "";

    private String deviceAddress = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
        mHandler = new BreoBleService.BleHandler(this);
        msgSendLoopHandlerThread = new HandlerThread("BreoBleService");
        msgSendLoopHandlerThread.start();
        msgSendLoopHandler = new BreoBleService.MsgSendLoopHandler(msgSendLoopHandlerThread.getLooper(), this);
        initCallback();
        registerBluetoothReceiver();
        //服务保活
        //addServiceBodyguard();
       // startForeground();
    }
    private void addServiceBodyguard(){
        Intent intent = new Intent(this, BreoBleService.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSubText("蓝牙服务已开启");
//        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        // 启动前台服务通知
        startForeground(1, builder.build());

    }

    private void initCallback() {
        mIConnectCallback = new IConnectCallback() {
            @Override
            public void onConnectSuccess(BreoDevice deviceMirror) {
                conDevice = deviceMirror;
                mHandler.sendEmptyMessage(STATE_CONNECTED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    boolean result = conDevice.getBluetoothGatt().requestMtu(128);
                    Log.d(TAG, "requestMtu=" + result);
                }
            }

            @Override
            public void onConnectFailure(BleException exception) {
                if (conDevice != null) {
                    conDevice = null;
                }
                mHandler.sendEmptyMessage(STATE_FAILED);
            }

            @Override
            public void onDisconnect() {
                if (conDevice != null) {
                    conDevice = null;
                }
                mHandler.sendEmptyMessage(STATE_DISCONNECTED);
            }

            @Override
            public void onNotBreoDevice(String s) {

            }
        };
        mINotifyCallback = new INotifyCallback() {
            @Override
            public void onNotify(byte[] data) {
                //     data[0]           帧头 1个字节 0x55
//     data[1]         所有帧⻓长度 1个字节
//     data[2]         当前帧序号 1个字节
//                数据内容 最⼤大15个字节
//                帧尾 1个字节 0xaa
                Log.d(TAG, data.length + "收到数据包" + HexUtil.toHexString(data));
                broadcastUpdate(data);
            }

            @Override
            public void onError(BleException exception) {
                EventBus.getDefault().post(new BleExceptionEvent(exception.getDescription()));

                Log.d(TAG, "BleException="+exception);

            }
        };

    }

    public void setCanNotify() {
        Log.d(TAG, "mINotifyCallback =" + mINotifyCallback);
        if (conDevice == null) {
            return;
        }
        if (mINotifyCallback != null) {
            conDevice.setBreoNotifyListener(mINotifyCallback);
        } else {
            mINotifyCallback = new INotifyCallback() {
                @Override
                public void onNotify(byte[] data) {
                    //     data[0]           帧头 1个字节 0x55
//     data[1]         所有帧⻓长度 1个字节
//     data[2]         当前帧序号 1个字节
//                数据内容 最⼤大15个字节
//                帧尾 1个字节 0xaa
                    Log.d(TAG, data.length + "收到数据包" + Arrays.toString(data));
                    broadcastUpdate(data);
                }

                @Override
                public void onError(BleException exception) {
                    Log.d(TAG, "BleException="+exception);
                    EventBus.getDefault().post(new BleExceptionEvent(exception.getDescription()));


                }
            };
            conDevice.setBreoNotifyListener(mINotifyCallback);


        }
    }

    public class LocalBinder extends Binder {
        public BreoBleService getService() {
            return BreoBleService.this;
        }
    }

    public void connect(BluetoothLeDevice device) {
        if (conDevice != null) {
            conDevice.disconnect();
            conDevice = null;
        }
        removeAllMsg();
        mHandler.sendEmptyMessage(STATE_CONNECTING);
        deviceName = device.getName();
        deviceAddress = device.getAddress();
        BreoBle.getInstance().connect(device, mIConnectCallback);
    }

    public void connect(BluetoothLeDevice device, boolean isFirst) {
        if (conDevice != null) {
            conDevice.disconnect();

            conDevice = null;
        }
        isFirstCon = isFirst;
        removeAllMsg();
        mHandler.sendEmptyMessage(STATE_CONNECTING);
        deviceName = device.getName();
        deviceAddress = device.getAddress();
        BreoBle.getInstance().connect(device, mIConnectCallback);
    }

    public void connect(String address) {
        if (conDevice != null) {
            conDevice.disconnect();

            conDevice = null;
        }
        isFirstCon = false;
        removeAllMsg();
        mHandler.sendEmptyMessage(STATE_CONNECTING);
        BreoBle.getInstance().connectByMac(address, mIConnectCallback);
    }

    public void disconnect() {
        if (conDevice != null) {
            conDevice.disconnect();
            conDevice.clear();
            conDevice = null;
        }
        removeAllMsg();

        BreoBle.getInstance().disconnect();
        BreoBle.getInstance().clear();
    }

    public void resetCallBack() {
        mINotifyCallback = null;
        mIConnectCallback = null;
        initCallback();
    }

    public void sendDataForISeeK(byte[] data) {
        Message message = Message.obtain();
        if (data.length > 0 && data[0] == 0x4e) {
            message.what = MsgSendLoopHandler.WHAT_SEND_ISEEK_MSG_BREO;

        } else {
            message.what = MsgSendLoopHandler.WHAT_SEND_ISEEK_MSG;
        }
        message.obj = data;
        //Log.d(TAG,"sendDataForISeeK="+conDevice)
        msgSendLoopHandler.handleMessage(message);
    }

    private void broadcastUpdate(byte[] data) {
        if (conDevice.getBluetoothLeDevice().getName() != null && conDevice.getBluetoothLeDevice().getName().toLowerCase().contains("iseek")||conDevice.getBluetoothLeDevice().getName().toLowerCase().contains("igun")) {
            if (data[0] == 0x4e && data.length > 1) {
                if (buffer != null) {
                    buffer.clear();
                }
                buffer = null;
                buffer = ByteBuffer.allocate(5 * 1024);
                mNotifyCount = 0;
            }
            buffer.put(data);
            mNotifyCount = mNotifyCount + data.length;
            if (data[data.length - 1] == 0x4e) {
                //最后一包
                byte[] dataTemp = buffer.array();
                byte[] dataNotify = new byte[mNotifyCount];
                System.arraycopy(dataTemp, 0, dataNotify, 0, mNotifyCount);
                byte[] localData = Protocol.unescapeMsg(dataNotify, 1, dataNotify.length - 2);
                Log.d(TAG, "localData=" + HexUtil.toHexString(localData));
                if (localData.length > 5 && localData[5] == (byte) 0xAA) {
                    byte[] jsonData = new byte[localData.length - 9];
                    System.arraycopy(localData, 8, jsonData, 0, jsonData.length);
                    String s = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        s = new String(jsonData, StandardCharsets.UTF_8);
                    } else {
                        try {
                            s = new String(jsonData, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    EventBus.getDefault().post(new BreoBleConEvent(STATE_NOTIFY, localData, s));
                    Log.d(TAG, "broadcastUpdate=" + s);
                } else {
                    EventBus.getDefault().post(new BreoBleConEvent(STATE_NOTIFY, localData));

                }
                // value= (a<< 8) | (b & 0xFF);

            }


        }
    }

    /**
     * 处理连接状态的Handler,发送广播更新状态
     */
    private class BleHandler extends Handler {


        boolean isDisconnecting = false;
        private WeakReference<BreoBleService> bluetoothDeviceWeakReference;

        BleHandler(BreoBleService breoBleService) {
            bluetoothDeviceWeakReference = new WeakReference<>(breoBleService);
        }

        @Override
        public void handleMessage(Message msg) {
            BreoBleService breoBleService = bluetoothDeviceWeakReference.get();
            if (breoBleService == null) {
                return;
            }
            switch (msg.what) {
                case STATE_DISCONNECTED:
                    //breoBleService.resetCallBack();
                    EventBus.getDefault().post(new BreoBleConEvent(STATE_DISCONNECTED, "DISCONNECTED"));
                    break;
                case STATE_CONNECTED:
                    EventBus.getDefault().post(new BreoBleConEvent(STATE_CONNECTED, "CONNECTED", deviceName, deviceAddress, isFirstCon));
//                    setCanNotify();
                    mHandler.sendEmptyMessageDelayed(STATE_SETNOTIFY, 500);
                    break;
                case STATE_FAILED:
                    EventBus.getDefault().post(new BreoBleConEvent(STATE_FAILED, "FAILED"));

                    break;
                case STATE_CONNECTING:
                    EventBus.getDefault().post(new BreoBleConEvent(STATE_CONNECTING, "CONNECTING"));

                    break;
                case STATE_SETNOTIFY:
                    setCanNotify();
                    break;
                default:
                    break;
            }
        }
    }

    public boolean isConnected() {
        if (conDevice != null) {
            return conDevice.isConnected();
        }
        return false;
    }

    private static class MsgSendLoopHandler extends Handler {
        public static final int WHAT_SEND_MSG = 0;
        public static final int WHAT_START_BLE_NOTIFICATION = 1;
        public static final int WHAT_SEND_ISEEK_MSG = 2;
        public static final int WHAT_SEND_ISEEK_MSG_BREO = 3;
        WeakReference<BreoBleService> weakReference;

        MsgSendLoopHandler(Looper looper, BreoBleService BreoBleService) {
            super(looper);
            weakReference = new WeakReference<>(BreoBleService);
        }

        @Override
        public void handleMessage(Message msg) {
            BreoBleService breoBleService = weakReference.get();
            switch (msg.what) {
                case WHAT_SEND_MSG:
                    break;
                case WHAT_SEND_ISEEK_MSG:
                    byte[] jsonBytes = (byte[]) msg.obj;
                    List<byte[]> sendList = new ArrayList<>();
                    int count = jsonBytes.length / 15;
                    int last = jsonBytes.length % 15;
                    if (last > 0) {
                        count++;
                    }
                    for (int i = 0; i < count; i++) {
                        byte[] sendData;
                        if (i == count - 1) {
                            //最后一包
                            sendData = new byte[last + 4];
                            sendData[0] = 0x55;
                            sendData[1] = (byte) count;
                            sendData[2] = (byte) i;

                            System.arraycopy(jsonBytes, i * 15, sendData, 3, last);
                        } else {
                            sendData = new byte[19];
                            sendData[0] = 0x55;
                            sendData[1] = (byte) count;
                            sendData[2] = (byte) i;
                            System.arraycopy(jsonBytes, i * 15, sendData, 3, 15);
                        }
                        sendData[sendData.length - 1] = (byte) 0xAA;

                        Log.d(TAG, i + "->sendData=" + HexUtil.toHexString(sendData));

                        sendList.add(sendData);
                    }
                    Log.d(TAG, "sendList=" + sendList);
                    if (breoBleService.conDevice != null) {
                        breoBleService.conDevice.sendListData(sendList);
                    }
                    break;
                case WHAT_SEND_ISEEK_MSG_BREO:
                    byte[] data = (byte[]) msg.obj;
                    Log.d(TAG, "WHAT_SEND_ISEEK_MSG_BREO=" + HexUtil.toHexString(data));
                    if (breoBleService.conDevice != null) {
                        breoBleService.conDevice.sendDataForLongPkg(data);
                    }
                    break;
            }
        }

    }

    @Subscribe
    public void getEvent(BreoBleConEvent bleConEvent) {
        Log.d(TAG, "getEvent=" + bleConEvent.toString());
    }

    @Subscribe
    public void getSendEvent(BreoBleSendData breoBleSendData) {
        Log.d(TAG, "getSendEvent=" + breoBleSendData.toString());
        if (breoBleSendData.getSendData() != null && breoBleSendData.getSendData().length > 0) {
            sendDataForISeeK(breoBleSendData.getSendData());
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");

        return super.onUnbind(intent);
    }
    private void startForeground() {
        String CHANNEL_ONE_ID = "com.breo.luson.ble";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(notificationChannel);
            startForeground(1, new NotificationCompat.Builder(this, CHANNEL_ONE_ID).build());
        }
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "onDestroy");
        msgSendLoopHandlerThread.quit();
        if (mReceive != null) {
            unregisterReceiver(mReceive);
            mReceive = null;
        }
        if (BreoBle.getInstance().isConnect()){
            if (Build.VERSION.SDK_INT >= 26) {
                //startForegroundService(new Intent(getApplication(),BreoBleService.class));
            } else {
                // Pre-O behavior.
                startService(new Intent(getApplication(),BreoBleService.class));
            }
        }
        super.onDestroy();
    }

    private void registerBluetoothReceiver() {
        if (mReceive == null) {
            mReceive = new BluetoothStateBroadcastReceive();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        registerReceiver(mReceive, intentFilter);
    }

    class BluetoothStateBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action != null) {
                switch (action) {
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        Log.d(TAG, "BreoReceiver蓝牙设备:" + device.getName() + "已链接");
                        //Toast.makeText(context , "蓝牙设备:" + device.getName() + "已链接", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Log.d(TAG, "BreoReceiver蓝牙设备:" + device.getName() + "已断开");
                        //Toast.makeText(context , "蓝牙设备:" + device.getName() + "已断开", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        switch (blueState) {
                            case BluetoothAdapter.STATE_OFF:
                                Log.d(TAG, "BreoReceiver蓝牙已关闭");
                                EventBus.getDefault().post(new BreoBleConEvent(STATE_DISCONNECTED, "DISCONNECTED"));
                                BreoBle.getInstance().disconnect();

                                //Toast.makeText(context , "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                                break;
                            case BluetoothAdapter.STATE_ON:
                                Log.d(TAG, "BreoReceiver蓝牙已开启");
                                //Toast.makeText(context , "蓝牙已开启"  , Toast.LENGTH_SHORT).show();
                                break;
                        }
                        break;
                }
            }
        }
    }

    //    public static final int STATE_DISCONNECTED = -899;
//    public static final int STATE_CONNECTING = -888;
//    public static final int STATE_CONNECTED = -999;
//    public static final int STATE_FAILED = -777;
//    public static final int STATE_NOTIFY = -778;
    private void removeAllMsg() {
        mHandler.removeMessages(STATE_DISCONNECTED);
        mHandler.removeMessages(STATE_CONNECTING);
        mHandler.removeMessages(STATE_CONNECTED);
        mHandler.removeMessages(STATE_FAILED);
        mHandler.removeMessages(STATE_NOTIFY);

    }
}
