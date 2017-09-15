package com.example.mudlife.myfinder;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.mudlife.server.BleService;

import com.example.mudlife.myfinder.iBeaconClass.iBeacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JX on 2017/9/12.
 */

public class LeScanFragment extends ListFragment {

    public interface OnAddFinderListener{
        void onAddFinder(iBeacon ibeacon);
    }

    private static final String TAG = "LeScanFragment";
    private static boolean bleServerFlag = false;
    MyServiceConn myServiceConn;
    BleService.MyBinder myBinder = null;

    BleService bleService = null;

    private Intent bleIntent=null;

    BeaconAdapter beaconAdapter;
    private OnAddFinderListener onAddFinderListener;

    public void setOnAddFinderListener(OnAddFinderListener onAddFinderListener){
        this.onAddFinderListener = onAddFinderListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");

        View v = inflater.inflate(R.layout.lescan_list,null);



        setListAdapter(beaconAdapter);

        beaconAdapter.updateData();
        bleBindService();


        return v;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e(TAG,"onCreate");

        super.onCreate(savedInstanceState);





        beaconAdapter = new BeaconAdapter(getActivity());

//        beaconAdapter.addData(new iBeacon("钥匙",(short)0xFF00,(short)0xFF00,"BEAC1600-0000-0000-0000-112233aabbcc","112233aabbcc",0x8c,-59,"0.5"));


        beaconAdapter.setOnAddFinderListener(new BeaconAdapter.OnAddFinderListener() {
            @Override
            public void onAddFinder(iBeacon ibeacon) {
                bleService.iBeaconAdv(ibeacon.proximityUuid,(short) 0xFC03,(short) 0XFF00);

                Log.e(TAG,"setOnAddFinderListener");
                onAddFinderListener.onAddFinder(ibeacon);
            }
        });


          /*开启蓝牙服务*/
        bleIntent = new Intent(getActivity(), BleService.class);
        bleIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        getActivity().startService(bleIntent);

        myServiceConn = new MyServiceConn();



    }


    public void bleBindService(){
        if(bleServerFlag == false){
            getActivity().bindService(bleIntent,myServiceConn, Context.BIND_ABOVE_CLIENT);
            bleServerFlag = true;
        }

    }

    public void bleUnBindService(){
        bleService.iBeacnStopLeSan();
        if(bleServerFlag == true){
            getActivity().unbindService(myServiceConn);
            bleServerFlag = false;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy");
        bleUnBindService();

        super.onDestroy();
    }

    class  MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (BleService.MyBinder) service;
            bleService = myBinder.getService();

            Log.e(TAG,"onServiceConnected");
            bleService.iBeaconStartLeScan();


            bleService.setOnBleScanListener(new BleService.OnBleScanListener() {
                @Override
                public void onAddBeacon(String name,short major,short minor,String uuid,String addr,int power,int rssi,String distance) {

                    beaconAdapter.addData(new iBeacon(name,major,minor, uuid, addr, power,rssi,distance));

                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
