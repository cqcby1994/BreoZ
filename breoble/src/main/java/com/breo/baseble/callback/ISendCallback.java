package com.breo.baseble.callback;

import com.breo.baseble.exception.BleException;

/**
 * Created by chenqc on 2019/6/26 14:28
 */
public interface ISendCallback {
    void onSuccess();
    void onError(BleException exception);
}
