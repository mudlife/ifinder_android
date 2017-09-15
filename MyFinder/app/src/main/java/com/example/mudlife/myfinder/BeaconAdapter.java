package com.example.mudlife.myfinder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import com.example.mudlife.myfinder.iBeaconClass.iBeacon;
/**
 * Created by JX on 2017/9/12.
 */

public class BeaconAdapter extends BaseAdapter {

    public interface OnAddFinderListener{
        void onAddFinder(iBeacon ibeacon);
    }

    private static final String TAG = "BeaconAdapter";
    Context mContext;

    OnAddFinderListener onAddFinderListener=null;
    public void setOnAddFinderListener(OnAddFinderListener onAddFinderListener){
        this.onAddFinderListener = onAddFinderListener;
    }

    public static List<iBeacon> iBeaconList = new ArrayList<iBeacon>();

    public  BeaconAdapter(Context context){
        mContext = context;
    }
//
//    public void binData(List<iBeacon> list){
//        Log.e(TAG,"binData");
//        iBeaconList = list;
//
//    }
    public void addData(iBeacon ibeacon){
        //判断设备是否存在
//        Log.e(TAG,"添加");

        for(iBeacon ib:iBeaconList){
//            Log.e(TAG,ib.getProximityUuid());
//            Log.e(TAG,ibeacon.getProximityUuid());
            if(ib.proximityUuid.equals(ibeacon.proximityUuid) == true){

                return;
            }

        }
        if(ibeacon.isPairStatuse() == true){
            Log.e(TAG,"添加ibeacon");
            iBeaconList.add(ibeacon);
            updateData();
        }

//        iBeaconList.add(ibeacon);
//        updateData();

    }

    public void updateData(){
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return iBeaconList.size();
    }

    @Override
    public Object getItem(int position) {
        return iBeaconList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = null;
        ViewHolder holder;
        if(convertView == null){
            view = LayoutInflater.from(this.mContext).inflate(R.layout.ibeacon_info,null);

        }else{
            view = convertView;
        }

        holder = new ViewHolder();
        holder.Uuid = (TextView) view.findViewById(R.id.ibeacon_uuid);
        holder.Rssi = (TextView) view.findViewById(R.id.ibeacon_rssi);
        holder.add = (LinearLayout) view.findViewById(R.id.ibeacon_add);

        holder.Uuid.setText(iBeaconList.get(position).bluetoothAddress);
        holder.Rssi.setText(""+iBeaconList.get(position).rssi);

        //添加按键点击事件
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddFinderListener.onAddFinder(iBeaconList.get(position));
                iBeaconList.remove(position);
                notifyDataSetChanged();
            }
        });



        return view;
    }

    static class ViewHolder{
        public TextView Uuid;
        public TextView Rssi;
        public LinearLayout add;
    }
}























