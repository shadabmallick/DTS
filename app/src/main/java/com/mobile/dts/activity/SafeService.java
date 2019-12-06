package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.dts.R;


public class SafeService extends AppCompatActivity {
    TextView tv_buy_now;
    ImageView img_back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safe_service);
        tv_buy_now=findViewById(R.id.tv_buy_now);
        img_back=findViewById(R.id.img_back);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
/*
        tv_buy_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history=new Intent(getApplicationContext(),HistoryOrganised.class);
                startActivity(history);
            }
        });
*/
    }
}
