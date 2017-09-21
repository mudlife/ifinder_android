package com.example.mudlife.myfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by JX on 2017/9/13.
 */

public class FinderActivity extends AppCompatActivity {
    EditText finderName = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finder_activity);

        finderName = (EditText) findViewById(R.id.finder_item_name);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("name");

        finderName.setText(bundle.getString("finder_name"));
//        Log.e("FinderActivity",bundle.getString("finder_name"));

    }
}
