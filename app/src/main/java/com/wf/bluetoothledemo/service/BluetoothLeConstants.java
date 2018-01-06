package com.wf.bluetoothledemo.service;

/**
 * BLE常量--Service、Characteristic的UUID
 *
 * Created by wangpf
 */

public class BluetoothLeConstants {
    //指定Service的UUID
    public static final String UUIDSTR_ISSC_PROPRIETARY_SERVICE = "49535343-FE7D-4AE5-8FA9-9FAFD205E455";

    //读取数据BluetoothGattCharacteristic的UUID
    public static final String UUIDSTR_ISSC_TRANS_TX = "49535343-1E4D-4BD9-BA61-23C647249616";
    //写数据BluetoothGattCharacteristic的UUID
    public static final String UUIDSTR_ISSC_TRANS_RX = "49535343-8841-43F4-A8D4-ECBE34729BB3";

    //获取监控数据
    public static final String BLUETOOTH_CHARGE_MONITOR = "com.wf.action.BLUETOOTH_CHARGE_MONITOR";
}
