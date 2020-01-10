package com.mobile.dts.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.RelativeLayout;


import com.mobile.dts.R;
import com.mobile.dts.adapter.MyRecyclerViewAdapter;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.utills.Constants;


import java.util.ArrayList;

import static com.mobile.dts.utills.Constants.appPref;

public class KeepSafeMultiple extends AppCompatActivity {
    String TAG="KeepSafeMultiple";
    RecyclerView recyclerView;
  RelativeLayout rel_fourth_bottom,layoutforprofileimage,rl_folder,rel_first_bottom;
   // ArrayList<ImageBean> sinceInstallList = new Utils().fetchFolderList(KeepSafeMultiple.this);
    private SharedPreferences sharedpreferences, settingsPref;

    String tempDate = null;
    // Array of integers points to images stored in /res/drawable-ldpi/
    private SqlLiteHelper dtsDataBase;
    ArrayList<ImageBean> imageArrayList;
    MyRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keep_safe_multiple);
        dtsDataBase = new SqlLiteHelper(this);
        sharedpreferences = getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        layoutforprofileimage=findViewById(R.id.layoutforprofileimage);
        rel_first_bottom=findViewById(R.id.rel_first_bottom);
        rel_fourth_bottom=findViewById(R.id.rel_fourth_bottom);
        rl_folder=findViewById(R.id.rl_folder);
        recyclerView=findViewById(R.id.rv_folder);
        ArrayList<String> savedImageList = dtsDataBase.getSavedImageList();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new MyRecyclerViewAdapter(this, savedImageList);
        recyclerView.setAdapter(adapter);
        layoutforprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rl_folder.setVisibility(rl_folder.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        rel_first_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DtsGalleryActivity.class);
                //tv_badge.setText(Integer.toString(tempLength));
                intent.putExtra(Constants.galleryType, Constants.delete);
                startActivity(intent);
                finish();
            }
        });
        rel_fourth_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                //tv_badge.setText(Integer.toString(tempLength));
               // intent.putExtra(Constants.galleryType, Constants.delete);
                startActivity(intent);
                finish();
            }
        });

    }

}
