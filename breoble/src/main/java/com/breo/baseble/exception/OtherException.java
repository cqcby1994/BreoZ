package com.breo.baseble.exception;

import com.breo.baseble.common.BleExceptionCode;

/**
 *  其他异常
 */
public class OtherException extends BleException {
    public OtherException(String description) {
        super(BleExceptionCode.OTHER_ERR, description);
    }
}
