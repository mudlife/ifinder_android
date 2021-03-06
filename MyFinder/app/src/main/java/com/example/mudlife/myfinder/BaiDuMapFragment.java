package com.example.mudlife.myfinder;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.BlockedNumberContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import com.example.mudlife.myfinder.iBeaconClass.iBeacon;

import java.util.List;


/**
 * Created by JX on 2017/9/11.
 */

public class BaiDuMapFragment extends Fragment {
    private MapView mapView = null;
    private BaiduMap baiduMap;
    private LocationManager locationManager;
    private String provider;
    boolean ifFrist = true;
    private OverlayOptions overlayOptions = null;
    private LatLng latLng = null;
    BaiDuMapReceiver baiDuMapReceiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        SDKInitializer.initialize(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.baidu_map, container, false);

        mapView = (MapView) view.findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();

//        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        baiduMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        List<String> list = locationManager.getAllProviders();
        if (list.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            Log.e("BaiDuMapFragment", "GPS_PROVIDER");

        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
            Log.e("BaiDuMapFragment", "NETWORK_PROVIDER");

        } else {
            Toast.makeText(getActivity(), "当前不能提供位置信息", Toast.LENGTH_LONG).show();
            Log.e("BaiDuMapFragment", "当前不能提供位置信息");
            return view;
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            Log.e("BaiDuMapFragment", "getLastKnownLocation");
            return view;
        }
//        addOverlay(FinderAdapter.myFinderList);
        Location location = locationManager.getLastKnownLocation(provider); //locationManager.getLastKnownLocation(provider);
        if (location != null) {
            navigateTo(location);
        }
        locationManager.requestLocationUpdates(provider, 1000, 10, locationListener);


        Log.e("BaiDuMapFragment", "onCreateView");
        return view;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //your implementations
            } else {
                Log.e("error", "權限");
            }
        }

    }

    private void navigateTo(Location location) {

        OverlayOptions options;

        baiduMap.clear();
        // 按照经纬度确定地图位置
//        if (ifFrist) {

        for (iBeacon ibeacon : FinderAdapter.myFinderList) {

            Log.e("BaiDu distance:",""+ibeacon.distance);
            options = new CircleOptions()
                    .center(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zIndex(9)
                    .radius((int) ibeacon.distance * 20)
                    .fillColor(Color.argb(0, 255, 0, 0))
                    .stroke(new Stroke(3, 0xAA00FF00));

            baiduMap.addOverlay(options);
        }
        Log.e("BaiDuMap", "la:" + location.getLatitude() + "Lo:" + location.getLongitude());
        latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
        // 移动到某经纬度
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomBy(5f);
        // 放大
        baiduMap.animateMapStatus(update);


//            ifFrist = false;
//        }
//        Log.e("BaiDuMap","la:"+location.getLatitude()+"Lo:"+location.getLongitude());
        // 显示个人位置图标
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData data = builder.build();
        baiduMap.setMyLocationData(data);
    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            navigateTo(location);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {


        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //1.创建广播接收对象
        baiDuMapReceiver = new BaiDuMapReceiver();

        //2.创建internt-filter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.mudlife.BaiDuUpdate");

        //3.注册广播接收者
        getActivity().registerReceiver(baiDuMapReceiver, filter);

    }


    private void addOverlay(List<iBeacon> list) {
        Log.e("BaiDuMap", "addOverlay");
        baiduMap.clear();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.tubiao);
        LatLng latLng = null;
        Marker marker;
        OverlayOptions options;
        iBeacon ibeacon = new iBeaconClass.iBeacon("钥匙", (short) 0xFF00, (short) 0xFF00, "BEAC1600-0000-0000-0000-112233445566",
                "112233445566", 0x8C, -59, 0.5);
//        for(iBeacon ibeacon:list){
        latLng = new LatLng(31.050659, 121.156179);
        options = new MarkerOptions()
                .position(latLng)
                .icon(bitmap)
                .zIndex(9)
                .draggable(true);
        marker = (Marker) baiduMap.addOverlay(options);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ibeacon", ibeacon);
        marker.setExtraInfo(bundle);
//        }
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        baiduMap.setMapStatus(msu);

    }


    public class BaiDuMapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider); //locationManager.getLastKnownLocation(provider);
            if (location != null) {
                navigateTo(location);
            }
        }
    }

}






















