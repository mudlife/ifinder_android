package com.example.mudlife.myfinder;

import android.bluetooth.BluetoothDevice;

/**
 * Created by JX on 2017/9/12.
 */
public class iBeaconClass {

    public static final short FINDER_IDLE = 0x0002;
    public static final short FINDER_PAIRING = 0x0001;
    public static final short FINDER_FANGDIU_ON = 0x0003;
    public static final short FINDER_FANGDIU_OFF = 0x0006;
    public static final short FINDER_XUNZHAO_ON = 0x0007;
    public static final short FINDER_XUNZHAO_OFF = 0x0008;
    /**
     * iBeacon静态内部实体类
     *
     * @author cheny
     *
     */
    static public class iBeacon {


        public String name;
        public short major;
        public short minor;
        public String proximityUuid;
        public String bluetoothAddress;
        public int txPower;
        public int rssi;
        public String distance;

        public int statuse;

        public iBeacon(){
            this.name = "未知";
            this.major = (short)0xFF00;
            this.minor = (short)0xFF00;
            this.proximityUuid = "";
            this.bluetoothAddress = "";
            this.txPower = 0xC8;
            this.rssi = 0;
            this.distance = "0.0";
            this.statuse = FINDER_IDLE;
        }
        public iBeacon(String name, short major, short minor, String proximityUuid, String bluetoothAddress, int txPower, int rssi, String distance) {

            this.name = name;
            this.major = major;
            this.minor = minor;
            this.proximityUuid = proximityUuid;
            this.bluetoothAddress = bluetoothAddress;
            this.txPower = txPower;
            this.rssi = rssi;
            this.distance = distance;
            if((this.minor &0x00FF) == FINDER_IDLE){
                statuse = FINDER_IDLE;
            }else if((this.minor &0x00FF) == FINDER_PAIRING){
                statuse = FINDER_PAIRING;
            }else if((this.minor &0x00FF) == FINDER_FANGDIU_ON){
                statuse = FINDER_FANGDIU_ON;
            }
        }

        public iBeacon(iBeacon ibeacon){
            this.name = ibeacon.name;
            this.major = ibeacon.major;
            this.minor = ibeacon.minor;
            this.proximityUuid = ibeacon.proximityUuid;
            this.bluetoothAddress = ibeacon.bluetoothAddress;
            this.txPower = ibeacon.txPower;
            this.rssi = ibeacon.rssi;
            this.distance = ibeacon.distance;

            if((this.minor &0x00FF) == FINDER_IDLE){
                statuse = FINDER_IDLE;
            }else if((this.minor &0x00FF) == FINDER_PAIRING){
                statuse = FINDER_PAIRING;
            }else if((this.minor &0x00FF) == FINDER_FANGDIU_ON){
                statuse = FINDER_FANGDIU_ON;
            }
        }

        public boolean isPairStatuse(){
            if(minor == (short) 0xFE01){
                return true;
            }else{
                return false;
            }
        }

    }

    /**
     * 解析iBeacon信息
     *
     * @param device
     * @param rssi
     * @param scanData
     * @return
     */
    public static iBeacon fromScanData(BluetoothDevice device, int rssi, byte[] scanData) {

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanData[startByte + 2] & 0xff) == 0x02 && ((int) scanData[startByte + 3] & 0xff) == 0x15) {
                // 这是 iBeacon
                patternFound = true;
                break;
            } else if (((int) scanData[startByte] & 0xff) == 0x2d && ((int) scanData[startByte + 1] & 0xff) == 0x24
                    && ((int) scanData[startByte + 2] & 0xff) == 0xbf
                    && ((int) scanData[startByte + 3] & 0xff) == 0x16) {
                iBeacon iBeacon = new iBeacon();
                iBeacon.major = 0;
                iBeacon.minor = 0;
                iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                iBeacon.txPower = -55;
                iBeacon.distance = String.format("%.2f", calculateAccuracy(iBeacon.txPower, rssi));
                return iBeacon;
            } else if (((int) scanData[startByte] & 0xff) == 0xad && ((int) scanData[startByte + 1] & 0xff) == 0x77
                    && ((int) scanData[startByte + 2] & 0xff) == 0x00
                    && ((int) scanData[startByte + 3] & 0xff) == 0xc6) {

                iBeacon iBeacon = new iBeacon();
                iBeacon.major = 0;
                iBeacon.minor = 0;
                iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                iBeacon.txPower = -55;
                iBeacon.distance = String.format("%.2f", calculateAccuracy(iBeacon.txPower, rssi));
                return iBeacon;
            }
            startByte++;
        }

        if (patternFound == false) {

            // 这不是iBeacon
//            System.out.println("这不是iBeacon:" );
            return null;
        }

        iBeacon iBeacon = new iBeacon();

        iBeacon.major = (short)((scanData[startByte + 20] & 0xff) * 0x100 + (scanData[startByte + 21] & 0xff));
        iBeacon.minor = (short)((scanData[startByte + 22] & 0xff) * 0x100 + (scanData[startByte + 23] & 0xff));
        iBeacon.txPower = (int) scanData[startByte + 24];
        iBeacon.rssi = rssi;

        // 格式化UUID
        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
        String hexString = bytesToHexString(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0, 8));
        sb.append("-");
        sb.append(hexString.substring(8, 12));
        sb.append("-");
        sb.append(hexString.substring(12, 16));
        sb.append("-");
        sb.append(hexString.substring(16, 20));
        sb.append("-");
        sb.append(hexString.substring(20, 32));
        iBeacon.proximityUuid = sb.toString();

        if (device != null) {
            iBeacon.bluetoothAddress = device.getAddress();
            iBeacon.name = device.getName();
        }
        iBeacon.distance = String.format("%.2f", calculateAccuracy(iBeacon.txPower, rssi));

        if((iBeacon.minor &0x00FF) == FINDER_IDLE){
            iBeacon.statuse = FINDER_IDLE;
        }else if((iBeacon.minor &0x00FF) == FINDER_PAIRING){
            iBeacon.statuse = FINDER_PAIRING;
        }else if((iBeacon.minor &0x00FF) == FINDER_FANGDIU_ON){
            iBeacon.statuse = FINDER_FANGDIU_ON;
        }

        return iBeacon;
    }

    /**
     * 转换十进制
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
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
     * 估算用户设备到ibeacon的距离
     *
     * @param txPower
     * @param rssi
     * @return
     */
    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}

