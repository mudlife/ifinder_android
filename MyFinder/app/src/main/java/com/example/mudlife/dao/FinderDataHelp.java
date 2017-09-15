package com.example.mudlife.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mudlife.myfinder.FinderAdapter;
import com.example.mudlife.myfinder.iBeaconClass.iBeacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JX on 2017/9/15.
 */

public class FinderDataHelp extends SQLiteOpenHelper {
    private static final String TAG = "FinderDataHelp";
    private static final String name = "finderdb";
    private static final int version = 1;


    public FinderDataHelp(Context context){
        super(context,name,null,version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG,"onCreate");

        db.execSQL("CREATE TABLE IF NOT EXISTS finderinfo(finderid integer primary key autoincrement," +
                "name varchar(40)," +
                "uuid varchar(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 添加iBeacon
     * @param ibeacon
     */
    public void insert(iBeacon ibeacon){
        SQLiteDatabase db = getWritableDatabase();

        Log.e(TAG,"insert:"+ibeacon.proximityUuid);
        ContentValues cv = new ContentValues();
        cv.put("name",ibeacon.name);
        cv.put("uuid",ibeacon.proximityUuid);
        db.insert("finderinfo",null,cv);
        db.close();
    }

    /**
     * 查找iBeacon
     * @return
     */
    public void getFinders(List<iBeacon> list){

        SQLiteDatabase db = getReadableDatabase();
//        db.execSQL("DROP TABLE finderinfo");
//        db.close();

        Cursor cursor = db.rawQuery("select name,uuid from finderinfo",null);
        while (cursor.moveToNext()){

            iBeacon ibeacon = new iBeacon();
            ibeacon.name = cursor.getString(0);
            ibeacon.proximityUuid = cursor.getString(1);
            Log.e(TAG,"getFinders:"+ibeacon.proximityUuid);
            list.add(ibeacon);
        }
        Log.e(TAG,"getFinders OK");
        cursor.close();
        db.close();

    }

    /**
     * 删除Finder
     * @param uuid
     * @return
     */
    public int deleteFinder(String uuid){
        SQLiteDatabase db = getWritableDatabase();
        Log.e(TAG,"delete:"+uuid);
       int result = db.delete("finderinfo","uuid=?",new String[]{uuid});
        db.close();
        Log.e(TAG,"result:"+result);
        return result;

    }
}

























