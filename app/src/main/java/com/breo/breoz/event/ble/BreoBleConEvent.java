package com.breo.breoz.event.ble;

import java.util.Arrays;


/**
 * Created by chenqc on 2019/10/28 11:11
 */
public class BreoBleConEvent {
    private int connectStatus;
    private String statusMsg;
    private String deviceName;
    private String deviceAddress;

    private byte[] notifyByte;

    private String notifyData;

    private boolean isFirstCon = false;

    public BreoBleConEvent(int connectStatus, String statusMsg) {
        this.connectStatus = connectStatus;
        this.statusMsg = statusMsg;
    }

    public BreoBleConEvent(int connectStatus, String statusMsg, boolean isFirstCon) {
        this.connectStatus = connectStatus;
        this.statusMsg = statusMsg;
        this.isFirstCon = isFirstCon;
    }
    public BreoBleConEvent(int connectStatus, String statusMsg, String device, String address, boolean isFirstCon) {
        this.connectStatus = connectStatus;
        this.statusMsg = statusMsg;
        this.isFirstCon = isFirstCon;
        this.deviceName = device;
        this.deviceAddress = address;
    }
    public BreoBleConEvent() {

    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public boolean isFirstCon() {
        return isFirstCon;
    }

    public void setFirstCon(boolean firstCon) {
        isFirstCon = firstCon;
    }

    public BreoBleConEvent(byte[] notifyByte, String notifyData) {
        this.notifyByte = notifyByte;
        this.notifyData = notifyData;
    }
    public BreoBleConEvent(int state,byte[] notifyByte, String notifyData) {
        this.connectStatus = state;
        this.notifyByte = notifyByte;
        this.notifyData = notifyData;
    }

    public BreoBleConEvent(byte[] notifyByte) {
        this.notifyByte = notifyByte;
    }
    public BreoBleConEvent(int state,byte[] notifyByte) {
        this.connectStatus = state;
        this.notifyByte = notifyByte;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public byte[] getNotifyByte() {
        return notifyByte;
    }

    public void setNotifyByte(byte[] notifyByte) {
        this.notifyByte = notifyByte;
    }

    public String getNotifyData() {
        return notifyData;
    }

    public void setNotifyData(String notifyData) {
        this.notifyData = notifyData;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "BreoBleConEvent{" +
                "connectStatus=" + connectStatus +
                ", statusMsg='" + statusMsg + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", notifyByte=" + Arrays.toString(notifyByte) +
                ", notifyData='" + notifyData + '\'' +
                ", isFirstCon=" + isFirstCon +
                '}';
    }
}
