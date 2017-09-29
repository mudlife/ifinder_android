package com.example.mudlife.myfinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mudlife.dao.FinderDataHelp;
import com.example.mudlife.myfinder.iBeaconClass.iBeacon;
import com.example.mudlife.server.BleService;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by JX on 2017/9/13.
 */

public class FinderAdapter extends BaseAdapter {
    private static FinderDataHelp finderDataHelp = null;

    private static float oldx=(float)0.0;
    private static float newx=(float)0.0;
    private static boolean isDown = false;

    public static Lock finderLock;



    private static final String TAG = "FinderAdapter";
    private static final String[] statuses= new String[]{
          "无状态",
            "配对",
            "闲置",
            "防丢中",
            "警報",
            "防丟中"
    };



    Context mContext;
    public static List<iBeacon> myFinderList = new ArrayList<iBeacon>();

    public FinderAdapter(Context mContext) {
        this.mContext = mContext;
        if(finderDataHelp == null){

            finderLock = new ReentrantLock();

            finderDataHelp = new FinderDataHelp(mContext);

            finderDataHelp.getFinders(myFinderList);
        }


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){

                    for(iBeacon ibeacon:myFinderList){
                        finderLock.lock();
//                        Log.e(TAG,ibeacon.proximityUuid);
                        if(ibeacon.tx == false)
                            continue;

                        //防丟
                        if(ibeacon.fangdiu == true){

                        }else{

                        }
                        Log.e(TAG,"THREAD");
//
                        Intent intent = new Intent();
                        intent.setAction("com.example.mudlife.BleAdv");
                        intent.putExtra("uuid",ibeacon.proximityUuid);
                        intent.putExtra("major",ibeacon.send_major);

                        intent.putExtra("minor",(short)0xFF00);
                        sendBc(intent);


                        //更新地图
                        Intent mapIntent = new Intent();
                        mapIntent.setAction("com.example.mudlife.BaiDuUpdate");
                        sendBc(intent);
                        finderLock.unlock();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        });

        thread.start();
    }

    public void sendBc(Intent intent){
        mContext.sendBroadcast(intent);
    }

    public void addFinder(iBeacon ibeacon){
        //判断设备是否存在


        for(iBeacon ib:myFinderList){
            if(ib.proximityUuid.equals(ibeacon.proximityUuid) == true){
                return;
            }

        }
            ibeacon.setCmd((byte)0x01);//配對指令
            ibeacon.send_minor = (short)0xFC03;//正確的返回結果
            myFinderList.add(ibeacon);
            finderDataHelp.insert(ibeacon);
            updateData();
//        Log.e(TAG,"addFinder");

    }


    public void deleteFinder(int position){
        finderDataHelp.deleteFinder(myFinderList.get(position).proximityUuid);
        myFinderList.remove(position);
        notifyDataSetChanged();
    }

    public  void updateData() {

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return myFinderList.size();
    }

    @Override
    public Object getItem(int position) {
        return myFinderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        final ViewHolder holder;
        if(convertView == null){
            view = LayoutInflater.from(this.mContext).inflate(R.layout.finder_info,null);

        }else{
            view = convertView;
        }

        holder = new ViewHolder();

        holder.finder_image = (ImageView) view.findViewById(R.id.finder_image);
        holder.finder_name = (TextView) view.findViewById(R.id.finder_name);
        holder.finder_statuse = (TextView) view.findViewById(R.id.finder_statuse);
        holder.finder_distance = (TextView) view.findViewById(R.id.finder_distance);

        holder.finder_fangdiu = (Switch) view.findViewById(R.id.finder_fangdiu);
        holder.finder_xunzhao = (Switch) view.findViewById(R.id.finder_xunzhao);
        holder.deleteLayout = (LinearLayout) view.findViewById(R.id.deleteLayout);
        holder.delete = (Button) view.findViewById(R.id.delete);

        holder.finder_distance.setText("距离:"+String.format("%.2f",myFinderList.get(position).distance)+"m");
        if(myFinderList.get(position).statuse<6)
        holder.finder_statuse.setText("状态:"+statuses[myFinderList.get(position).statuse]);



        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e(TAG,"onClick");
                Intent intent = new Intent();
                intent.setAction("com.example.mudlife.FinderActivity");

                Bundle bundle = new Bundle();
                bundle.putString("finder_name","钥匙1");

                intent.putExtra("name",bundle);

                mContext.startActivity(intent);

            }
        });

        //防丢SWitch
        holder.finder_fangdiu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Intent intent = new Intent();
                Log.e(TAG,"防丟swicth adv");
                intent.setAction("com.example.mudlife.BleAdv");
                intent.putExtra("uuid",myFinderList.get(position).proximityUuid);

                intent.putExtra("minor",(short)0xFF00);
                if(isChecked){
                    //开启防丢
                    myFinderList.get(position).setCmd((byte)0x01);
                    myFinderList.get(position).send_minor = (short)0xFC03;
                    myFinderList.get(position).fangdiu = true;

                }else{
                    //关闭防丢
                    myFinderList.get(position).setCmd((byte)0x02);
                    myFinderList.get(position).send_minor = (short)0xFD02;
                    myFinderList.get(position).fangdiu = false;


                }
                myFinderList.get(position).tx = true;
                intent.putExtra("major",myFinderList.get(position).major);
                sendBc(intent);

            }
        });

        holder.finder_xunzhao.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Intent intent = new Intent();
                Log.e(TAG,"尋找swicth adv");
                intent.setAction("com.example.mudlife.BleAdv");
                intent.putExtra("uuid",myFinderList.get(position).proximityUuid);

                intent.putExtra("minor",(short)0xFF00);
                if(isChecked){
                    Log.e(TAG,"尋找");
                    //寻找
                    myFinderList.get(position).setCmd((byte)0x04);
                    myFinderList.get(position).send_minor = (short)0xFB04;
                    myFinderList.get(position).xunzhao = true;

                }else{
                    //关闭
                    myFinderList.get(position).statuse = iBeaconClass.FINDER_XUNZHAO_OFF;
                    myFinderList.get(position).setCmd((byte)0x06);
                    myFinderList.get(position).send_minor = (short)0xF906;
                    myFinderList.get(position).xunzhao = false;

                }
                myFinderList.get(position).tx = true;
                intent.putExtra("major",myFinderList.get(position).major);
                sendBc(intent);
            }
        });



//        Log.e(TAG,"超出距离？"+myFinderList.get(position).distance);
        if(myFinderList.get(position).outDistance()){

            view.findViewById(R.id.finder_item).setBackgroundColor(Color.RED);

        }else {
            view.findViewById(R.id.finder_item).setBackgroundColor(Color.WHITE);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        oldx = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        newx = event.getX();
                        if(newx - oldx > 5.0){
                            holder.deleteLayout.setVisibility(View.GONE);
//                            holder.finder_image.setVisibility(View.VISIBLE);
                        }else if(newx - oldx < -5.0){
                            holder.deleteLayout.setVisibility(View.VISIBLE);
//                            holder.finder_image.setVisibility(View.GONE);
                        }
                        oldx = newx;
                        break;
                    case MotionEvent.ACTION_UP:

                        break;
                }
                return false;
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.deleteLayout.setVisibility(View.GONE);
                deleteFinder(position);
            }
        });
        return view;
    }





    static class ViewHolder{
        ImageView finder_image;
        TextView finder_name;
        TextView finder_statuse;
        TextView finder_distance;
        Switch finder_fangdiu;
        Switch finder_xunzhao;
        LinearLayout deleteLayout;
        Button delete;

    }


}
