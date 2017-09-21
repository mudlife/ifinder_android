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
//                bleService.iBeaconAdv(ibeacon.proximityUuid,(short) 0xFC03,(short) 0XFF00);

                Log.e(TAG,"setOnAddFinderListener");
                onAddFinderListener.onAddFinder(ibeacon);
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy");

        beaconAdapter.destroy();

        super.onDestroy();
    }


}
