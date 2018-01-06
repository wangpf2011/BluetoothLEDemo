package com.wf.bluetoothledemo.utils;

/**
 * 字节工具类
 * Created by wangpf
 */
public class ByteUtils {

    public static byte[] intToBytesReversal(int n) {
        byte[] b = new byte[2];

        for(int i = 0;i < 2;i++) {
            b[1-i]=(byte)(n>>(8-i*8));
        }
        return b;
    }

    /**
     * int转byte数组（len个字节）
     * @param n
     * @param len 转换目标字节数
     * @return
     */
    public static byte[] intToBytes(int n, int len) {
        if(len <= 1) {
            byte[] byteArr = {(byte)n};
            return byteArr;
        }
        byte[] b = new byte[len];

        for(int i = 0;i < len;i++) {
            b[i]=(byte)(n>>((len-1)*8-i*8));
        }
        return b;
    }

    /**
     * byte转换为int
     * @param b
     * @return
     */
    public static int byteToInt(byte[] b) {
        int mask=0xff;
        int temp=0;
        int n=0;
        for(int i=0;i<b.length;i++) {
            n<<=8;
            temp=b[i]&mask;
            n|=temp;
        }
        return n;
    }

    /**
     * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 单字节与16进账字符串转换
     * @param src
     * @return
     */
    public static String byteToHexString(byte src){
        int v = src & 0xFF;
        String hv = Integer.toHexString(v);
        if (hv.length() < 2) {
            return "0"+hv;
        }
        return hv;
    }

    public static byte hexStringToByte(String hexString) {
        if(StringUtils.isEmpty(hexString) || hexString.length() !=2) return 0;

        return hexStringToBytes(hexString)[0];
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * 字节数组倒序
     * @param bytes
     * @return
     */
    public static byte[] convertBytes(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for(int i=0; i< bytes.length; i++) {
            result[bytes.length - i -1] = bytes[i];
        }

        return result;
    }

    /**
     * 截取指定长度字符串
     * @param str
     * @param len
     * @return
     */
    public static String substring(String str, int len) {
        if(StringUtils.isEmpty(str)) {
            str = String.format("%0"+len+"d", 0);
        }

        if(str.length() < len) {
            while(str.length() < len) {
                str = "0" + str;
            }
        }else if(str.length() > len){
            str = str.substring(0, len);
        }

        return str;
    }

    /**
     * 充电桩实时数据，温度特殊处理
     *
     * 如果温度相关字节第一位是1，则进行减1取反操作
     * @param temp
     * @return
     */
    public static int dealTemp(String temp) {
        if(StringUtils.isEmpty(temp) || temp.length() != 4) return 0;

        byte[] bytes = ByteUtils.convertBytes(ByteUtils.hexStringToBytes(temp));
        int s = (bytes[0] & 0x80);
        if(s != 0) {
            int num = ByteUtils.byteToInt(bytes) - 1;
            bytes = ByteUtils.intToBytes(num, 2);
            System.out.println(ByteUtils.bytesToHexString(bytes));

            byte tmp;
            for(int i=0;i<bytes.length;i++) {
                tmp = bytes[i];
                bytes[i] = (byte)(~tmp);
            }
            return ByteUtils.byteToInt(bytes);
        }else {
            return ByteUtils.byteToInt(bytes);
        }
    }
}
