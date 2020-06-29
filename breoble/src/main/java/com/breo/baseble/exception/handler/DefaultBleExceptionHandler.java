package com.breo.baseble.exception.handler;

import com.breo.baseble.exception.GattException;
import com.breo.baseble.exception.OtherException;
import com.breo.baseble.exception.ConnectException;
import com.breo.baseble.exception.InitiatedException;
import com.breo.baseble.exception.TimeoutException;
import com.breo.baseble.utils.BreoLog;


/**
 *  异常默认处理
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
    @Override
    protected void onConnectException(ConnectException e) {
        BreoLog.e(e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
        BreoLog.e(e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        BreoLog.e(e.getDescription());
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        BreoLog.e(e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
        BreoLog.e(e.getDescription());
    }
}
