package com.wf.bluetoothledemo.service;

import com.wf.bluetoothledemo.utils.ByteUtils;
import com.wf.bluetoothledemo.utils.CRC16;
import com.wf.bluetoothledemo.utils.StringUtils;

/**
 * 封装通讯协议工具类
 *
 * Created by wangpf
 */
public class CommandRequestUtils {

    /**
     * 接收数据--解析设备返回的数据，返回命令号
     * @param bytes
     * @return
     */
    public static String getFrameOrder(byte[] bytes) {
        if(!checkFrame(bytes)) return "";

        String hexStr = ByteUtils.bytesToHexString(bytes).toLowerCase();
        String command = hexStr.substring(16, 18);

        return command;
    }

    /**
     * 接收数据--数据帧有效性校验
     * @param bytes
     * @return
     */
    public static boolean checkFrame(byte[] bytes) {
        if(bytes == null || bytes.length <= 13) return false;

        String hexStr = ByteUtils.bytesToHexString(bytes).toLowerCase();
        //帧头、帧尾校验
        if(!hexStr.startsWith("eb90eb90") || !hexStr.endsWith("90eb")) return false;
        //CRC16校验
        String check = hexStr.substring(8, hexStr.lastIndexOf("90eb")-4);
        String crc = hexStr.substring(hexStr.lastIndexOf("90eb")-4, hexStr.lastIndexOf("90eb"));
        int i = CRC16.calcCrc16(ByteUtils.hexStringToBytes(check));
        int j = ByteUtils.byteToInt(ByteUtils.convertBytes(ByteUtils.hexStringToBytes(crc)));
        if(i != j) return false;

        return true;
    }

    /**
     * 召唤实时数据--a6
     * @param deviceNo
     * @return
     */
    public static byte[] obtainRealData(String deviceNo) {
        if(StringUtils.isEmpty(deviceNo)) return null;
        byte[] data = deviceNo.getBytes();
        return packageBytes("a6", data);
    }

    /**
     * 封装发送报文--命令通用
     * @param cmd
     * @param data
     * @return
     */
    public static byte[] packageBytes(String cmd, byte[] data) {
        //协议头（包含帧头、目的地址、源地址）
        byte[] head = ByteUtils.hexStringToBytes("eb90eb9001fe");
        //目的地址
        byte dest = (byte)Integer.parseInt("01", 16);
        //源地址
        byte src = (byte)Integer.parseInt("fe", 16);
        //命令号
        byte command = (byte)Integer.parseInt(cmd, 16);
        //协议尾（帧尾）
        byte[] tail = ByteUtils.hexStringToBytes("90eb");

        //数据长度
        byte[] dataLen = ByteUtils.convertBytes(ByteUtils.intToBytes(data.length + 3, 2));
        //CRC校验
        byte[] crcCheck = new byte[data.length + 5];
        crcCheck[0] = dest;
        crcCheck[1] = src;
        System.arraycopy(dataLen, 0, crcCheck, 2, dataLen.length);
        crcCheck[4] = command;
        System.arraycopy(data, 0, crcCheck, 5, data.length);
        byte[] crc = ByteUtils.convertBytes(ByteUtils.intToBytes(CRC16.calcCrc16(crcCheck), 2));

        //组装协议字节数组
        //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        byte[] result = new byte[head.length+tail.length+data.length+5];
        int i = 0;
        System.arraycopy(head, 0, result, i, head.length);
        i += head.length;
        System.arraycopy(dataLen, 0, result, i, dataLen.length);
        i += dataLen.length;
        result[i] = command;
        i += 1;
        System.arraycopy(data, 0, result, i, data.length);
        i += data.length;
        System.arraycopy(crc, 0, result, i, crc.length);
        i += crc.length;
        System.arraycopy(tail, 0, result, i, tail.length);

        return result;
    }
}
