package com.wf.bluetoothledemo.view;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wf.bluetoothledemo.MainApplication;
import com.wf.bluetoothledemo.R;
import com.wf.bluetoothledemo.adapter.DeviceAdapter;
import com.wf.bluetoothledemo.bean.BleDevice;
import com.wf.bluetoothledemo.service.BluetoothLeService;
import com.wf.bluetoothledemo.service.CommandRequestUtils;
import com.wf.bluetoothledemo.utils.BleServiceActivity;
import com.wf.bluetoothledemo.utils.ByteUtils;
import com.wf.bluetoothledemo.utils.PermissionsChecker;
import com.wf.bluetoothledemo.utils.UIHelper;

/**
 * BLE连接--View
 *
 * BLE蓝牙连接，与设备进行信息交互
 *
 * Created by wangpf
 */
public class BleConnectActivity extends BleServiceActivity implements View.OnClickListener {
    private BleConnectPresenter presenter;

    private TextView txt_ble_name;
    private TextView txt_ble_mac;
    private TextView txt_ble_status;
    private EditText txt_realdata;

    private ImageView imgLoading;
    private Animation operatingAnim;
    private BleDevice bleDevice;

    public static final int MSG_ALERT = 99;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ALERT:
                    if(msg.obj != null) {
                        UIHelper.ToastMessage(BleConnectActivity.this, msg.obj.toString());
                    }
                    break;
            }
        }
    };

    //广播接收器
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//BLE蓝牙连接成功
                imgLoading.clearAnimation();
                imgLoading.setVisibility(View.INVISIBLE);
                txt_ble_status.setText("BLE Status： Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {//BLE蓝牙断开
                imgLoading.clearAnimation();
                imgLoading.setVisibility(View.INVISIBLE);
                txt_ble_status.setText("BLE Status： Disconnect");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {//发现BLE服务
                presenter.displayGattServices(mBluetoothLeService);
            } else if(BluetoothLeService.BLUETOOTH_DEVICE_MONITOR.equals(action)) {//读取设备实时数据
                String msg = intent.getStringExtra("msg");
                // 实时显示
                txt_realdata.setText(txt_realdata.getText()+"响应："+ msg);
            }
        }
    };

    //配置Intent过滤器
    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.BLUETOOTH_DEVICE_MONITOR);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_connect);
        presenter = new BleConnectPresenter();

        initView();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private void initView() {
        bleDevice = this.getIntent().getParcelableExtra("device");
        txt_ble_name = (TextView) findViewById(R.id.txt_ble_name);
        txt_ble_mac = (TextView) findViewById(R.id.txt_ble_mac);
        txt_ble_status = (TextView) findViewById(R.id.txt_ble_status);
        txt_ble_name.setText("BLE Name： "+bleDevice.getName());
        txt_ble_mac.setText("BLE MAC： "+bleDevice.getMac());
        txt_ble_status.setText("BLE Status： Disconnect");
        txt_realdata = (EditText)findViewById(R.id.txt_realdata);

        findViewById(R.id.btn_clear_realdata).setOnClickListener(this);
        findViewById(R.id.btn_read_realdata).setOnClickListener(this);

        //img_loading动画
        imgLoading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.img_rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
    }

    //Activity绑定Service时执行该方法
    public void serviceConnected() {
        //建立连接
        boolean result = mBluetoothLeService.connect(bleDevice.getMac());
        imgLoading.startAnimation(operatingAnim);
        imgLoading.setVisibility(View.VISIBLE);
        if(!result) {
            UIHelper.ToastMessage(BleConnectActivity.this, getString(R.string.ble_connect_fail));
            mBluetoothLeService.disconnect();
            imgLoading.clearAnimation();
            imgLoading.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        if(mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //读取数据
            case R.id.btn_read_realdata:
                try {
                    if(mBluetoothLeService != null
                            && mBluetoothLeService.isConnect()) {
                        //命令请求--获取桩当前版本号
                        txt_realdata.setText(txt_realdata.getText()+"请求："+ ByteUtils.bytesToHexString(CommandRequestUtils.obtainRealData("1011700001")));
                        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getCharacteristicrx();
                        characteristic.setValue(CommandRequestUtils.obtainRealData("1011700001"));
                        mBluetoothLeService.wirteCharacteristic(characteristic);
                    }else {
                        mHandler.obtainMessage(MSG_ALERT, "请先连接蓝牙，然后重试").sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //清除页面数据
            case R.id.btn_clear_realdata:
                txt_realdata.setText("");
                break;
        }
    }
}
