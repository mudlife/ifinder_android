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
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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


    /*接口*/
    public interface OnBleScanListener{
        void onAddBeacon(String name,short major,short minor,String uuid,String addr,int power,int rssi,String distance);
    }


    private static final String TAG = "BleService";
    private static boolean bleLeScanFlag = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdertiser;
    Timer timer=null;

    private OnBleScanListener onBleScanListener;


    BleReceiver bleReceiver=null;

    private Vibrator vibrator;

    long[] pattern = {100,400,100,400};

    public void setOnBleScanListener(OnBleScanListener onBleScanListener){
        this.onBleScanListener = onBleScanListener;
    }

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
        bleReceiver = new BleReceiver();

        //2.创建internt-filter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.mudlife.BleAdv");

        //3.注册广播接收者
        registerReceiver(bleReceiver,filter);

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


    }


    /**
     * iBeaon 掃描
     */
    public void iBeaconStartLeScan(){
        Log.e(TAG,"iBeaconStartLeScan");
        if(bleLeScanFlag == false){
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            bleLeScanFlag = true;

        }


    }


    public  void iBeacnStopLeSan(){
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

                        //更新距离
                        ib.distance = ibeacon.distance;

                        Intent intent = new Intent();
                        intent.setAction("com.example.mudlife.FinderUpdata");
                        sendBroadcast(intent);

                        if(Float.parseFloat(ibeacon.distance) > 1.0){//距离超出1m

                            //震动
                            vibrator.vibrate(pattern,2);

                            //蜂鸣器
                            iBeaconAdv(ibeacon.proximityUuid,(short)0xFB04,(short)0xFF00);


                        }else{
                            vibrator.cancel();
                            iBeaconAdv(ibeacon.proximityUuid,(short)0xFF00,(short)0xFF00);
                        }
                        if ((ib.statuse == iBeaconClass.FINDER_FANGDIU_ON && ibeacon.statuse == iBeaconClass.FINDER_FANGDIU_ON) ||
                                (ib.statuse == iBeaconClass.FINDER_FANGDIU_OFF && ibeacon.statuse == iBeaconClass.FINDER_IDLE) ) {
                            Log.e(TAG,"状态："+ib.statuse+":"+ibeacon.statuse);
                            iBeaconAdv(ib.proximityUuid, (short) 0xFF00, (short) 0xFF00);
                            ib.statuse = iBeaconClass.FINDER_IDLE;
                        }
                        ib.statuse = ibeacon.statuse;
                        return;
                    }
                }
                onBleScanListener.onAddBeacon(ibeacon.name,ibeacon.major,ibeacon.minor,ibeacon.proximityUuid.toString(),
                        ibeacon.bluetoothAddress.toString(),ibeacon.txPower,ibeacon.rssi,ibeacon.distance);
            }

        }
    };



    /**
     * BLE 廣播數據
     * @param data
     */
    public void iBeaconAdv(String uuid,short major,short minor){


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
                Log.e(TAG,"onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + "mode" + settingsInEffect.getMode());
            }else{
                Log.e(TAG,"onStartSuccess ,settingsInEffect is null");
            }



            timer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    mBluetoothLeAdertiser.stopAdvertising(mAdvertiseCallback);

                    iBeaconStartLeScan();
                }
            },3000);

        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.e(TAG,"onStartFailure ");
        }
    };





    /****广播接收者****/
    public class BleReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("com.example.mudlife.BleAdv")){
                iBeaconAdv(intent.getStringExtra("uuid"),
                        intent.getShortExtra("major",(short)0xFF00),
                        intent.getShortExtra("mnor",(short)0xFF00));

            }
            Log.e(TAG,"BleReceiver");
        }
    }
}

























