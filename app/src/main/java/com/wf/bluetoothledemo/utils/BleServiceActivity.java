package com.wf.bluetoothledemo.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wf.bluetoothledemo.R;
import com.wf.bluetoothledemo.service.BluetoothLeService;

/**
 * 绑定BluetoothLeService的Activity
 *
 * client之间蓝牙通讯
 *
 * Created by wangpf
 */
public class BleServiceActivity extends BaseActivity {
    //蓝牙连接相关
    protected BluetoothLeService mBluetoothLeService;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                UIHelper.ToastMessage(BleServiceActivity.this, getString(R.string.ble_connect_fail));
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            //mBluetoothLeService.connect(Common.bluetoothMac);
            serviceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Activity绑定Service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    //是否已连接蓝牙
    public boolean isConnect() {
        if(mBluetoothLeService != null && mBluetoothLeService.isConnect()) {
            return true;
        }

        return false;
    }

    //Activity绑定Service时执行该方法
    public void serviceConnected() {
        Log.d("BleServiceActivity", "ServiceConnected~~");
    }

    // 含有全部的权限
    protected boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        //Activity销毁时解除Service绑定
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
