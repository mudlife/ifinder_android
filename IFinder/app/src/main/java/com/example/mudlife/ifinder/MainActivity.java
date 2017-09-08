package com.example.mudlife.ifinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {


    RelativeLayout shouYeLayout = null;
    RelativeLayout paiZhaoLayout = null;
    RelativeLayout dingWeiLayout = null;
    RelativeLayout jieShaoLayout = null;

    ShouYe shouYeFragment = null;
    PaiZhao paiZhaoFragment = null;
    DingWei dingWeiFragment = null;
    JieShao jieShaoFragment = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shouYeLayout = (RelativeLayout) findViewById(R.id.first_layout);
        paiZhaoLayout = (RelativeLayout) findViewById(R.id.second_layout);
        dingWeiLayout = (RelativeLayout) findViewById(R.id.third_layout);
        jieShaoLayout = (RelativeLayout) findViewById(R.id.fourth_layout);



        if(shouYeFragment == null){
            shouYeFragment = new ShouYe();
        }
        getFragmentManager().beginTransaction().replace(R.id.bootmNav,shouYeFragment).commit();

        /**
         * 首页
         */
        shouYeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shouYeFragment == null){
                    shouYeFragment = new ShouYe();
                }
                getFragmentManager().beginTransaction().replace(R.id.bootmNav,shouYeFragment).commit();
            }
        });

        /**
         * 拍照
         */
        paiZhaoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(paiZhaoFragment == null){
                    paiZhaoFragment = new PaiZhao();
                }
                getFragmentManager().beginTransaction().replace(R.id.bootmNav,paiZhaoFragment).commit();
            }
        });

        /**
         * 定位
         */
        dingWeiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dingWeiFragment == null){
                    dingWeiFragment = new DingWei();
                }
                getFragmentManager().beginTransaction().replace(R.id.bootmNav,dingWeiFragment).commit();
            }
        });

        /**
         * 介绍
         */
        jieShaoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(jieShaoFragment == null){
                    jieShaoFragment = new JieShao();
                }
                getFragmentManager().beginTransaction().replace(R.id.bootmNav,jieShaoFragment).commit();
            }
        });
    }
}
