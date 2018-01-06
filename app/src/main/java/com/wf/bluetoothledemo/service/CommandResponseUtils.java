package com.wf.bluetoothledemo.service;

import android.content.Intent;
import android.util.Log;
import org.json.JSONObject;

import java.text.DecimalFormat;
import com.wf.bluetoothledemo.utils.ByteUtils;

/**
 * 接收设备上返回数据并解析
 *
 * Created by wangpf
 */

public class CommandResponseUtils {
    /**
     * 解析处理蓝牙接收到的数据
     * @param packet
     */
    public static Intent dealReceiveData(byte[] packet) {
        Intent msgSend = null;
        try {
            //广播接收到的数据
            String hexStr = ByteUtils.bytesToHexString(packet).toLowerCase();
            //eb90eb90fe010c00c32016082596001000006c5790eb
            if(!hexStr.startsWith("eb90eb90fe01") || !hexStr.endsWith("90eb")) {
                Log.e("bluetooth(drop)==", hexStr);
                return null;
            }

            Log.e("bluetooth==", hexStr);
            String order = CommandRequestUtils.getFrameOrder(packet);
            if("b6".equals(order)) {//获取设备实时监控数据
                msgSend = new Intent(BluetoothLeConstants.BLUETOOTH_CHARGE_MONITOR);
                if(hexStr.length() >= 98) {
                    JSONObject jo = new JSONObject();
                    //0：空闲，1：充电，2：故障
                    String status = hexStr.substring(18, 20);
                    if("00".equals(status)) jo.put("status", "0");
                    else if("01".equals(status)) jo.put("status", "1");
                    else if("02".equals(status)) jo.put("status", "2");
                    //电压
                    String dy = hexStr.substring(20, 24);
                    int dyval = ByteUtils.byteToInt(ByteUtils.convertBytes(ByteUtils.hexStringToBytes(dy)));
                    DecimalFormat df = new DecimalFormat("######0.0");
                    jo.put("dy", df.format(dyval/10.0));
                    //电流
                    String dl = hexStr.substring(24, 28);
                    int dlval = ByteUtils.byteToInt(ByteUtils.convertBytes(ByteUtils.hexStringToBytes(dl)));
                    df = new DecimalFormat("######0.00");
                    jo.put("dl", df.format(dlval/100.0));
                    //导引电压
                    String ddy = hexStr.substring(28, 32);
                    int ddyval = ByteUtils.byteToInt(ByteUtils.convertBytes(ByteUtils.hexStringToBytes(ddy)));
                    df = new DecimalFormat("######0.0");
                    jo.put("ddy", df.format(ddyval/10.0));
                    //温度1
                    String wd1 = hexStr.substring(32, 36);
                    int wd1val = ByteUtils.dealTemp(wd1);
                    jo.put("wd1", df.format(wd1val/10.0));
                    //温度2
                    String wd2 = hexStr.substring(36, 40);
                    int wd2val = ByteUtils.dealTemp(wd2);
                    jo.put("wd2", df.format(wd2val/10.0));

                    msgSend.putExtra("msg", jo.toString());
                }else {
                    msgSend.putExtra("msg", "{}");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return msgSend;
    }
}
