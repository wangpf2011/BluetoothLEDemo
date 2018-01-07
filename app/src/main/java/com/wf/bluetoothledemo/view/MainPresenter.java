package com.wf.bluetoothledemo.view;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.wf.bluetoothledemo.service.BluetoothLeConstants;
import com.wf.bluetoothledemo.service.BluetoothLeService;

import java.util.List;

/**
 * 主界面--Presenter
 *
 * Created by wangpf
 */
public class MainPresenter {

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView
    // on the UI.
    public void displayGattServices(BluetoothLeService mBluetoothLeService) {
        if(mBluetoothLeService == null
                || mBluetoothLeService.getSupportedGattServices() == null
                || mBluetoothLeService.getSupportedGattServices().size() <=0) {
            return;
        }

        String uuid = null;
        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            //特定Service
            if(BluetoothLeConstants.UUIDSTR_ISSC_PROPRIETARY_SERVICE.equalsIgnoreCase(uuid)) {
                mBluetoothLeService.setService(gattService);

                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for(int i=0; i<gattCharacteristics.size(); i++) {
                    uuid = gattCharacteristics.get(i).getUuid().toString();
                    Log.e("console", "gatt Characteristic: " + uuid);
                    if(BluetoothLeConstants.UUIDSTR_ISSC_TRANS_TX.equalsIgnoreCase(uuid)) {
                        //读属性Characteristic
                        BluetoothGattCharacteristic characteristictx = gattCharacteristics.get(i);
                        mBluetoothLeService.setCharacteristictx(characteristictx);
                        //Notification Characteristic
                        mBluetoothLeService.setCharacteristicNotification(characteristictx, true);
                        List<BluetoothGattDescriptor> descriptorList = characteristictx.getDescriptors();
                        if (descriptorList != null && descriptorList.size() > 0) {
                            BluetoothGattDescriptor descriptor = descriptorList.get(0);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothLeService.wirteDescriptor(descriptor);
                        }
                    }else if(BluetoothLeConstants.UUIDSTR_ISSC_TRANS_RX.equalsIgnoreCase(uuid)) {
                        //写属性Characteristic
                        BluetoothGattCharacteristic characteristicrx = gattCharacteristics.get(i);
                        mBluetoothLeService.setCharacteristicrx(characteristicrx);
                    }
                }// end for
                break;
            }// end if
        }// end for
    }
}
