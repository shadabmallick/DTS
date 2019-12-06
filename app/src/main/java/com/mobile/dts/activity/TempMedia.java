package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.mobile.dts.R;


public class TempMedia extends AppCompatActivity {
    RelativeLayout rel_center;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_media);
        rel_center=findViewById(R.id.rel_center);
        rel_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent codeagain=new Intent(getApplicationContext(),DtsGalleryActivity.class);
                startActivity(codeagain);
            }
        });
    }
}
