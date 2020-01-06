package com.mobile.dts.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mobile.dts.R;


public class HistoryOrganised extends AppCompatActivity {
    TextView rel_center;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_organisec);
        rel_center=findViewById(R.id.tv_see_you);
        rel_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
               // System.exit(0);
            }
        });
    }
}
