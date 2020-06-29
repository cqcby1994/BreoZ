package com.breo.baseble.exception;

import com.breo.baseble.common.BleExceptionCode;

/**
 * 初始化异常
 */
public class InitiatedException extends BleException {
    public InitiatedException() {
        super(BleExceptionCode.INITIATED_ERR, "Initiated Exception Occurred! ");
    }
}
