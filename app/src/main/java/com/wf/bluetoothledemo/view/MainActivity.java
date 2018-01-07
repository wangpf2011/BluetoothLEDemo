package com.wf.bluetoothledemo.view;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.wf.bluetoothledemo.MainApplication;
import com.wf.bluetoothledemo.R;
import com.wf.bluetoothledemo.adapter.DeviceAdapter;
import com.wf.bluetoothledemo.bean.BleDevice;
import com.wf.bluetoothledemo.utils.BleServiceActivity;
import com.wf.bluetoothledemo.utils.PermissionsChecker;
import com.wf.bluetoothledemo.utils.UIHelper;

/**
 * 主界面--View
 *
 * Created by wangpf
 */
public class MainActivity extends BleServiceActivity implements IMainView, View.OnClickListener {
    private long exitTime = 0;
    //BLE扫描权限
    public final int MY_PERMISSIONS_BTSCAN_DEVICE = 1;
    //开启蓝牙功能
    private final int REQUEST_ENABLE_BT_LESCAN = 2;

    //蓝牙扫描相关
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceAdapter mDeviceAdapter;
    private boolean mScanning = false;
    // stop scan after 10 second
    private final long SCAN_PERIOD = 10000;

    //运行权限申请相关
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    //是否需要系统权限检测, 防止和系统提示框重叠
    private boolean isRequireCheck;
    // 所需的全部权限
    private String[] ACCESS_LOCATION = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private Button btn_scan;
    private ImageView imgLoading;
    private Animation operatingAnim;

    public static final int MSG_ALERT = 99;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ALERT:
                    if(msg.obj != null) {
                        UIHelper.ToastMessage(MainActivity.this, msg.obj.toString());
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionsChecker = new PermissionsChecker(this);
        isRequireCheck = true;

        initView();
        //请求权限--您的位置
        boolean isMinSdkM = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        if(!isMinSdkM) {
            if(mPermissionsChecker.lacksPermissions(ACCESS_LOCATION)) {
                ActivityCompat.requestPermissions(this, ACCESS_LOCATION, MY_PERMISSIONS_BTSCAN_DEVICE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isRequireCheck) {
            // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
            if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                UIHelper.ToastMessage(MainActivity.this, R.string.ble_not_supported);
                return;
            }

            if(mBluetoothAdapter == null) {
                // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                // 检查设备上是否支持蓝牙
                if (mBluetoothAdapter == null) {
                    UIHelper.ToastMessage(MainActivity.this, R.string.ble_not_supported1);
                    return;
                }
            }
        }else {
            isRequireCheck = true;
        }
    }

    @Override
    protected void onDestroy() {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
                    if(!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_LESCAN);
                    }else {
                        imgLoading.startAnimation(operatingAnim);
                        imgLoading.setVisibility(View.VISIBLE);
                        btn_scan.setText(getString(R.string.stop_scan));
                        scanLeDevice(true);
                    }
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    scanLeDevice(false);
                }
                break;
        }
    }

    private void initView() {
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        //img_loading动画
        imgLoading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.img_rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());

        mDeviceAdapter = new DeviceAdapter(this);
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);
        listView_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BleDevice bleDevice = mDeviceAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, BleConnectActivity.class);
                intent.putExtra("device", bleDevice);
                startActivity(intent);
            }
        });
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            BleDevice bleDevice = new BleDevice(device, rssi, scanRecord, 0);
            mDeviceAdapter.addDevice(bleDevice);
            mDeviceAdapter.notifyDataSetChanged();
        }
    };

    //Device startLeScan/stopLeScan
    private void scanLeDevice(final boolean enable) {
        if(enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    imgLoading.clearAnimation();
                    imgLoading.setVisibility(View.INVISIBLE);
                    btn_scan.setText(getString(R.string.start_scan));
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT_LESCAN:
                isRequireCheck = false;
                if(resultCode == Activity.RESULT_CANCELED) {
                    UIHelper.ToastMessage(MainActivity.this, R.string.ble_not_supported1);
                }else {
                    imgLoading.startAnimation(operatingAnim);
                    imgLoading.setVisibility(View.VISIBLE);
                    btn_scan.setText(getString(R.string.stop_scan));
                    scanLeDevice(true);
                }
                break;
        }
    }

    /**
     * 请求运行时权限回调函数
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_BTSCAN_DEVICE) {
            if(hasAllPermissionsGranted(grantResults)) {
                isRequireCheck = true;
                // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
                if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    UIHelper.ToastMessage(MainActivity.this, R.string.ble_not_supported);
                    return;
                }

                if(mBluetoothAdapter == null) {
                    // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
                    final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    // 检查设备上是否支持蓝牙
                    if (mBluetoothAdapter == null) {
                        UIHelper.ToastMessage(MainActivity.this, R.string.ble_not_supported1);
                        return;
                    }
                }
            }else {
                isRequireCheck = false;
            }
        }
    }

    //退出按钮
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN ) {
            if((System.currentTimeMillis()-exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else {
                mBluetoothLeService.close();
                MainApplication.getInstance().exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 发送消息至Handler
     * @param type
     */
    @Override
    public void sendHandler(int type, String msg) {
        mHandler.obtainMessage(type, msg).sendToTarget();
    }

    @Override
    public void sendEmptyHandler(int type) {
        mHandler.sendEmptyMessage(type);
    }
}
