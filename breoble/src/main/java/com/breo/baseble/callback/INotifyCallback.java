package com.breo.baseble.callback;

import com.breo.baseble.breoble.msgbean.ID5SRunBean;
import com.breo.baseble.exception.BleException;

/**
 * Created by chenqc on 2019/6/26 15:51
 */
public interface INotifyCallback {
    void onNotify(byte[] data);
    void onError(BleException exception);

}
