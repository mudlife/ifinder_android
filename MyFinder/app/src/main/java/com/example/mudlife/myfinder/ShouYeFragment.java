package com.example.mudlife.myfinder;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.mudlife.myfinder.iBeaconClass.iBeacon;


/**
 * Created by JX on 2017/9/12.
 */

public class ShouYeFragment extends ListFragment {
    FinderAdapter finderAdapter = null;
    FinderAdapterReceiver finderAdapterReceiver=null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shouye_fragment,null);

//        finderAdapter.addFinder(new iBeaconClass.iBeacon("钥匙",(short) 0xFF00,(short) 0xFF00,"BEAC1600-0000-0000-0000-112233445566",
//                "112233445566",0x8C,-59,"0.5"));
//
//        finderAdapter.addFinder(new iBeaconClass.iBeacon("钥匙",(short) 0xFF00,(short) 0xFF00,"BEAC1600-0000-0000-0000-AABBCCDDEEFF",
//                "AABBCCDDEEFF",0x8C,-59,"0.5"));

        setListAdapter(finderAdapter);


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        finderAdapter = new FinderAdapter(getActivity());

        //1.创建广播接收对象
        finderAdapterReceiver = new FinderAdapterReceiver();

        //2.创建internt-filter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.mudlife.FinderUpdata");

        //3.注册广播接收者
        getActivity().registerReceiver(finderAdapterReceiver,filter);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(finderAdapterReceiver);
    }

    public void addFinder(iBeacon ibeacon){
        finderAdapter.addFinder(new iBeacon(ibeacon));

    }

    public class FinderAdapterReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.example.mudlife.FinderUpdata") == true){
                finderAdapter.updateData();
            }

        }

    }

}
