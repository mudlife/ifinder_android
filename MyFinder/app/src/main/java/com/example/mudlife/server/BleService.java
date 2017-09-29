package com.example.mudlife.server;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.mudlife.myfinder.FinderAdapter;
import com.example.mudlife.myfinder.iBeaconClass;
import com.example.mudlife.myfinder.iBeaconClass.iBeacon;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by JX on 2017/9/12.
 */

public class BleService extends android.app.Service {





    private static final String TAG = "BleService";
    private static boolean bleLeScanFlag = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdertiser;
    Timer timer=null;




    static BleReceiver bleReceiver=null;

    private Vibrator vibrator;

    long[] pattern = {100,400,100,400};



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG,"onBind");
        return new MyBinder();
    }

    public class MyBinder extends Binder{
        public BleService getService(){
            return BleService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");



        //1.创建广播接收对象
        if(bleReceiver == null) {
            bleReceiver = new BleReceiver();

            //2.创建internt-filter对象
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.example.mudlife.BleAdv");

            //3.注册广播接收者
            registerReceiver(bleReceiver,filter);
        }
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"ble not supported",Toast.LENGTH_SHORT).show();
        }



        mBluetoothLeAdertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if(mBluetoothLeAdertiser == null){
            Toast.makeText(this,"the device not support peripheral",Toast.LENGTH_SHORT).show();
            System.out.println("the device not support peripheral");
            Log.e(TAG,"the device not support peripheral");
        }


        timer = new Timer();
        //开启蓝牙
        mBluetoothAdapter.enable();

        //震动
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//        iBeaconAdv("00000000-0000-0000-0000-000000000000", (short) 0xFF00, (short) 0xFF00);

    }


    /**
     * iBeaon 掃描
     */
    public void iBeaconStartLeScan(){

        Log.e(TAG,"掃描");
        if(bleLeScanFlag == false){
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            bleLeScanFlag = true;

        }


    }


    public  void iBeacnStopLeSan(){
        Log.e(TAG,"停止掃描");
        if(bleLeScanFlag == true){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            bleLeScanFlag = false;
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        vibrator.cancel();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * iBeacon 掃描回調函數
     */
    @SuppressLint("NewApi")
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {



            final iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);

//            Log.e(TAG,"有数据");
//            onBleScanListener.onAddBeacon(ibeacon);

            if(ibeacon !=null) {
                Log.i(TAG,"name:"+ibeacon.name);
                Log.i(TAG,"addresss:"+ibeacon.bluetoothAddress.toString());
                Log.i(TAG,"uuid:"+ibeacon.proximityUuid.toString());
                Log.i(TAG,"major:"+Integer.toHexString(ibeacon.major));
                Log.i(TAG,"minor:"+Integer.toHexString(ibeacon.minor));
                Log.i(TAG,"txPower:"+Integer.toHexString(ibeacon.txPower));
                Log.i(TAG,"distance"+ibeacon.distance);

                for(iBeacon ib: FinderAdapter.myFinderList){

                    if(ib.proximityUuid.equals(ibeacon.proximityUuid) == true) {

                        if(ib.send_minor == ibeacon.minor){
                            Log.e(TAG,"send_minor:"+Integer.toHexString(ib.send_minor)+":"+Integer.toHexString(ibeacon.minor));
                            mBluetoothLeAdertiser.stopAdvertising(mAdvertiseCallback);
                            iBeaconStartLeScan();
                            ib.tx = false;
                        }

                        //更新距离
                        ib.distance = ibeacon.distance;
                        ib.addDistance(ibeacon.distance);
//                        ib.major = ibeacon.major;
                        ib.minor = ibeacon.minor;
                        ib.statuse = ibeacon.minor&0x0f;

                        Intent intent = new Intent();
                        intent.setAction("com.example.mudlife.FinderUpdata");
                        sendBroadcast(intent);


                            if (ib.outDistance()) {//距离超出1m

                                //震动
                                vibrator.vibrate(pattern, 2);
//                            Log.e(TAG,"setCmd 0x04");
                                ib.tx = true;
                                ib.setCmd((byte) 0x04);
                                ib.send_minor = (short) 0xFB04;

                                //蜂鸣器
                                iBeaconAdv(ibeacon.proximityUuid, ibeacon.send_major, (short) 0xFF00);

                            } else {
                                vibrator.cancel();
                                if(ibeacon.minor == (short)0xFB04 && ib.xunzhao == false){
                                    ib.setCmd((byte) 0x06);
                                    ib.send_minor = (short)0xF906;
                                    ib.tx = true;
                                    iBeaconAdv(ib.proximityUuid, ib.send_major, ib.minor);
                                }
//                                if (ib.xunzhao == true) {
//                                    ib.setCmd((byte) 0x04);
//                                }
//
//                                ib.send_minor = (short) 0xFF00;
//                                iBeaconAdv(ibeacon.proximityUuid, ibeacon.send_major, ibeacon.send_minor);

                            }
//


                        return;
                    }
                }

                //发送广播 添加设备
                Intent intent = new Intent();
                intent.setAction("com.example.mudlife.iBeaconAdd");
                Bundle bundle = new Bundle();
                bundle.putSerializable("ibeacon",ibeacon);

                intent.putExtra("bundle",bundle);
                sendBroadcast(intent);


//                onBleScanListener.onAddBeacon(ibeacon.name,ibeacon.major,ibeacon.minor,ibeacon.proximityUuid.toString(),
//                        ibeacon.bluetoothAddress.toString(),ibeacon.txPower,ibeacon.rssi,ibeacon.distance);
            }

        }
    };



    /**
     * BLE 廣播數據
     * @param data
     */
    public void iBeaconAdv(String uuid,short major,short minor){



        Log.e(TAG,"major:"+Integer.toHexString(major));
            mBluetoothLeAdertiser.stopAdvertising(mAdvertiseCallback);
            iBeacnStopLeSan();
            mBluetoothLeAdertiser.startAdvertising(createAdvSettings(true, 0), createAdvertiseData(uuid,major, minor, (byte) 0xC8), mAdvertiseCallback);


    }

    public static AdvertiseSettings createAdvSettings(boolean connectable,int timeoutMillis){
       AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();

        if(mAdvertiseSettings == null){
            Log.e(TAG,"mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }

    public static AdvertiseData createAdvertiseData(String proximityUuid,short major,short minor,byte txPower){
       String[] uuidstr = proximityUuid.replaceAll("-","").toLowerCase().split("");
        byte[] uuidBytes = new byte[16];
        for(int i=1,x =0;i< uuidstr.length;x++){
            uuidBytes[x] = (byte) ((Integer.parseInt(uuidstr[i++],16) << 4 | Integer.parseInt(uuidstr[i++],16)));
        }
        byte[] majorBytes = {(byte)(major >> 8),(byte)(major & 0xff)};
        byte[] minorBytes = {(byte)(minor >> 8),(byte)(minor & 0xff)};
        byte[] mPowerBytes = {txPower};
        byte[] manufacturerData = new byte[0x17];
        byte[] flagibeacon = {0x02,0x15};
        System.arraycopy(flagibeacon,0x0,manufacturerData,0x0,0x2);
        System.arraycopy(uuidBytes,0x0,manufacturerData,0x2,0x10);
        System.arraycopy(majorBytes,0x0,manufacturerData,0x12,0x2);
        System.arraycopy(minorBytes,0x0,manufacturerData,0x14,0x2);
        System.arraycopy(mPowerBytes,0x0,manufacturerData,0x16,0x1);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();

        builder.addManufacturerData(0x004c,manufacturerData);
//        builder.setIncludeDeviceName(false);

//        builder.addServiceData(ParcelUuid.fromString(proximityUuid),manufacturerData);
        AdvertiseData adv = builder.build();
        return adv;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if(settingsInEffect != null){
//                Log.e(TAG,"onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + "mode" + settingsInEffect.getMode());
            }else{
//                Log.e(TAG,"onStartSuccess ,settingsInEffect is null");
            }



            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mBluetoothLeAdertiser.stopAdvertising(mAdvertiseCallback);
                    Log.e(TAG,"开始扫描");
                    iBeaconStartLeScan();

                }
            },1000);

        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

//            Log.e(TAG,"onStartFailure ");
        }
    };





    /****广播接收者****/
    public class BleReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("com.example.mudlife.BleAdv")){
                short major = intent.getShortExtra("major",(short)0xFF00);
                short minor = intent.getShortExtra("minor",(short)0xFF00);

//                Log.e(TAG,"BleReceiver "+Integer.toHexString(major)+":"+Integer.toHexString(minor));


                if(major == (short)0xFF00){
                    return;
                }
//                Log.e(TAG,"BleReceiver");
                iBeaconAdv(intent.getStringExtra("uuid"),
                        major,
                        minor);

            }

        }
    }
}

























