package com.breo.baseble.utils;

import android.util.Log;

import com.breo.baseble.BreoBle;

/**
 * Created by chenqc on 2019/7/1 18:16
 */
public class BreoLog {
    private static String TAG = "BreoApp";
    public static void i(String s){
        if (BreoBle.isDebug) {
            Log.i(TAG, s);
        }
    }
    public static void d(String s){
        if (BreoBle.isDebug) {
            Log.d(TAG, s);
        }
    }

    public static void e(String s){
        if (BreoBle.isDebug) {
            Log.e(TAG, s);
        }
    }
}

