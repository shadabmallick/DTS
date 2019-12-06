package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.dts.R;


public class TellFriend extends AppCompatActivity {
    TextView tv_spread_world;
    ImageView img_back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tell_a_friend);
        tv_spread_world=findViewById(R.id.tv_spread_world);
        img_back=findViewById(R.id.img_back);
        tv_spread_world.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rate_us=new Intent(TellFriend.this,RateUs.class);
                startActivity(rate_us);
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
