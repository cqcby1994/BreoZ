/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.breo.breoz.ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    /**
     * 读的服务
     */
    public final static String BLE_IDREAM5S_SPP_SERVICE_READ = "00006666-0000-1000-8000-00805f9b34fb";
    public final static String BLE_SPP_SERVICE_READ = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    /**
     * 写的服务
     */
    public final static String BLE_IDREAM5S_SPP_SERVICE_WRITE = "00007777-0000-1000-8000-00805f9b34fb";
    public final static String BLE_SPP_SERVICE_WRITE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    /**
     * 通知的描述符
     */
    public final static String BLE_IDREAM5S_SPP_NOTIFY_CHARACTERISTIC = "00008888-0000-1000-8000-00805f9b34fb";
    public final static String BLE_SPP_NOTIFY_CHARACTERISTIC = "49535343-1e4d-4bd9-ba61-23c647249616";
    /**
     * 写的描述符
     */
    public final static String BLE_IDREAM5S_SPP_WRITE_CHARACTERISTIC = "00008877-0000-1000-8000-00805f9b34fb";
    public final static String BLE_SPP_WRITE_CHARACTERISTIC = "49535343-8841-43f4-a8d4-ecbe34729bb3";
    public final static String BLE_SPP_AT_CHARACTERISTIC = "0000fee2-0000-1000-8000-00805f9b34fb";
    public static final String HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String DEVICE_INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
    /**
     * 标注自己的UUID
     */
    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static HashMap<String, String> attributes = new HashMap();

    static {
        // Sample Services.
        attributes.put(HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");
        // Sample Characteristics.
        attributes.put(MANUFACTURER_NAME_STRING, "Manufacturer Name String");

        attributes.put(BLE_SPP_SERVICE_READ, "BLE SPP Service");
        attributes.put(BLE_SPP_SERVICE_WRITE, "BLE SPP Service");
        attributes.put(BLE_SPP_NOTIFY_CHARACTERISTIC, "BLE SPP Notify Characteristic");
        attributes.put(BLE_SPP_WRITE_CHARACTERISTIC, "BLE SPP Write Characteristic");
        attributes.put(BLE_SPP_AT_CHARACTERISTIC, "BLE SPP AT Characteristic");
//        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "BLE SPP AT Characteristic");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
