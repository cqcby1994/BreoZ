package com.breo.baseble.exception;

import com.breo.baseble.common.BleExceptionCode;

/**
 *  超时异常
 */
public class TimeoutException extends BleException {
    public TimeoutException() {
        super(BleExceptionCode.TIMEOUT, "Timeout Exception Occurred! ");
    }
}
