package com.example.mudlife.myfinder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.mudlife.myfinder.iBeaconClass.iBeacon;
import com.example.mudlife.server.BleService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int SHOUYE=0;
    private static final int BAIDUMAP=1;
    private static final int JIESHAO=2;
    private static final int LESCAN=3;

    BleService bleService = null;
    BleService.MyBinder myBinder = null;
    MyServiceConn myServiceConn;
    Intent bleIntent;
    private static boolean bleServerFlag = false;


    ShouYeFragment shouYeFragment = null;
    BaiDuMapFragment baiDuMapFragment = null;

    JieShaoFragment jieShaoFragment = null;
    LeScanFragment leScanFragment = null;
    PaiZhaoActivity paiZhaoActivity = null;


    private int fragmentCurentStatuse=SHOUYE;
    private int fragmentOldStatuse=SHOUYE;


    RelativeLayout shouYeLayout = null;
    RelativeLayout paiZhaoLayout = null;
    RelativeLayout dingWeiLayout = null;
    RelativeLayout jieShaoLayout = null;

    TextView fanHuiTextView = null;
    TextView souSuoTextView = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //权限判断
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_CONTACTS);

        }


            /*开启蓝牙服务*/
        bleIntent = new Intent(this, BleService.class);
        bleIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        this.startService(bleIntent);




        myServiceConn = new MyServiceConn();


        shouYeLayout = (RelativeLayout) findViewById(R.id.first_layout);
        paiZhaoLayout = (RelativeLayout) findViewById(R.id.second_layout);
        dingWeiLayout = (RelativeLayout) findViewById(R.id.third_layout);
        jieShaoLayout = (RelativeLayout) findViewById(R.id.fourth_layout);

        fanHuiTextView = (TextView) findViewById(R.id.fanhui);
        souSuoTextView = (TextView) findViewById(R.id.sousuo);



        if(shouYeFragment == null){
            shouYeFragment = new ShouYeFragment();
        }


        getFragmentManager().beginTransaction().replace(R.id.bootmNav,shouYeFragment).commit();

        shouYeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getFragmentManager().beginTransaction().replace(R.id.bootmNav,shouYeFragment).commit();
                fragmentOldStatuse = fragmentCurentStatuse;
                fragmentCurentStatuse = SHOUYE;
            }
        });

        paiZhaoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(paiZhaoActivity == null){
                    paiZhaoActivity = new PaiZhaoActivity();
                }

                Intent intent = new Intent();
                intent.setAction("com.example.mudlife.PaiZhaoActivity");
                startActivity(intent);

            }
        });

        dingWeiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(baiDuMapFragment == null){
                    baiDuMapFragment = new BaiDuMapFragment();
                }

//                if(!baiDuMapFragment.isAdded()){
//                    getFragmentManager().beginTransaction().add(baiDuMapFragment,"baidumap").commit();
//                }

                getFragmentManager().beginTransaction().replace(R.id.bootmNav,baiDuMapFragment).commit();
                fragmentOldStatuse = fragmentCurentStatuse;
                fragmentCurentStatuse = BAIDUMAP;
                Log.e("Main","dingwei");
            }
        });


        jieShaoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(jieShaoFragment == null){
                    jieShaoFragment = new JieShaoFragment();
                }



                getFragmentManager().beginTransaction().replace(R.id.bootmNav,jieShaoFragment).commit();
                fragmentOldStatuse = fragmentCurentStatuse;
                fragmentCurentStatuse = JIESHAO;
            }
        });

        fanHuiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragmentOldStatuse == SHOUYE){

                    getFragmentManager().beginTransaction().replace(R.id.bootmNav,shouYeFragment).commit();
                    fragmentOldStatuse = fragmentCurentStatuse;
                    fragmentCurentStatuse = SHOUYE;
                }else if(fragmentOldStatuse == BAIDUMAP){
                    getFragmentManager().beginTransaction().replace(R.id.bootmNav,baiDuMapFragment).commit();
                    fragmentOldStatuse = fragmentCurentStatuse;
                    fragmentCurentStatuse = BAIDUMAP;
                }else if(fragmentOldStatuse == JIESHAO){
                    getFragmentManager().beginTransaction().replace(R.id.bootmNav,jieShaoFragment).commit();
                    fragmentOldStatuse = fragmentCurentStatuse;
                    fragmentCurentStatuse = JIESHAO;
                }
                fanHuiTextView.setVisibility(View.GONE);
                souSuoTextView.setText("搜索设备");


            }
        });

        souSuoTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(leScanFragment == null){
                    leScanFragment = new LeScanFragment();

                    leScanFragment.setOnAddFinderListener(new LeScanFragment.OnAddFinderListener() {
                        @Override
                        public void onAddFinder(iBeacon ibeacon) {

                            Log.e(TAG,"tianjia");
                            shouYeFragment.addFinder(ibeacon);

                            getFragmentManager().beginTransaction().replace(R.id.bootmNav,shouYeFragment).commit();
                            fragmentOldStatuse = fragmentCurentStatuse;
                            fragmentCurentStatuse = SHOUYE;
                            fanHuiTextView.setVisibility(View.GONE);
                            souSuoTextView.setText("搜索设备");

                        }
                    });
//                    if(!leScanFragment.isAdded()) {
//                        getFragmentManager().beginTransaction().add(leScanFragment, "leScan").commit();
//                    }

                }


                getFragmentManager().beginTransaction().replace(R.id.bootmNav,leScanFragment).commit();

                fragmentOldStatuse = fragmentCurentStatuse;
                fragmentCurentStatuse = LESCAN;
                fanHuiTextView.setVisibility(View.VISIBLE);
                souSuoTextView.setText("搜索中");
                if(leScanFragment != null){
//                    leScanFragment.bleBindService();
                }

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        bleUnBindService();
    }

    public void bleBindService(){
        if(bleServerFlag == false){
            this.bindService(bleIntent,myServiceConn, Context.BIND_ABOVE_CLIENT);
            bleServerFlag = true;
        }

    }

    public void bleUnBindService(){
        bleService.iBeacnStopLeSan();
        if(bleServerFlag == true){
            this.unbindService(myServiceConn);
            bleServerFlag = false;
        }

    }


    class  MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (BleService.MyBinder) service;
            bleService = myBinder.getService();



            Log.e(TAG,"onServiceConnected");
            bleService.iBeaconStartLeScan();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
