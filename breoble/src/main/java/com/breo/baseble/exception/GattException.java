package com.breo.baseble.exception;

import com.breo.baseble.common.BleExceptionCode;

/**
 *  Gatt异常
 */
public class GattException extends BleException {
    private int gattStatus;

    public GattException(int gattStatus) {
        super(BleExceptionCode.GATT_ERR, "Gatt Exception Occurred! ");
        this.gattStatus = gattStatus;
    }

    public int getGattStatus() {
        return gattStatus;
    }

    public GattException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    @Override
    public String toString() {
        return "GattException{" +
                "gattStatus=" + gattStatus +
                '}' + super.toString();
    }
}
