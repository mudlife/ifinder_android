package com.example.mudlife.myfinder;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by JX on 2017/9/12.
 */

public class PaiZhaoActivity extends AppCompatActivity{
    private static int REQUEST_THUMBNAIL =1;
    private static int REQUEST_ORIGINAL = 2;

    private ImageView camearImage;
    private String sdPath;
    private String picPath;

    private Camera mCamera;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //your implementations
            }else{
                Log.e("error","權限");
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paizhao_fragment);
        camearImage = (ImageView) findViewById(R.id.camear_image);

        sdPath = Environment.getExternalStorageDirectory().getPath();
        picPath = sdPath + "/" + "temp.png";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            requestPermissions(new String[]{Manifest.permission.CAMERA},1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            Log.e("BaiDuMapFragment", "getLastKnownLocation");

        }


        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent1,REQUEST_THUMBNAIL);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_THUMBNAIL){
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                camearImage.setImageBitmap(bitmap);

            }else if(requestCode == REQUEST_ORIGINAL){
                FileInputStream fis = null;
                try{
                    fis = new FileInputStream(picPath);

                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    camearImage.setImageBitmap(bitmap);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }finally {
                    try{
                        if(fis !=null){
                            fis.close();
                        }

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
