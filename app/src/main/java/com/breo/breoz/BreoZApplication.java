package com.breo.breoz;

import android.app.Application;

import com.breo.baseble.BreoBle;

public class BreoZApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BreoBle.config()
                .setScanTimeout(10 * 1000)//扫描超时时间
                .setConnectTimeout(10 * 1000);//连接超时时间
        BreoBle.getInstance().init(this);
    }
}
