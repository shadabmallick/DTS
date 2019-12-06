package com.mobile.dts.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobile.dts.Fragment.FragmentHome;
import com.mobile.dts.R;
import com.mobile.dts.database.DTSPreferences;


import java.util.Calendar;
import java.util.Date;

public class IntroductionAfterLogin extends AppCompatActivity {


    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    Date on_pause,on_open,on_back;
    String TAG="Drawer";
    Fragment fragment = null;
    TextView header_text,tv_mail;
    RelativeLayout rl_edit;
    TextView tooltext,toolbar_edit,toolbar_save,tv_profile;

    ImageView toolbar_image,toolbar_notification;
    FragmentTransaction transaction;
    Typeface typeface;
  //  GlobalClass global_class;
  //  Shared_Preference shared_prefrence;
    ProgressDialog progressBar;

    boolean doubleBackToExitPressedOnce = false;

    String device_id;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    // BottomNavigation bottomNavigation;
    RelativeLayout rl_cart_notification;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.introduction_whole);
        on_open = Calendar.getInstance().getTime();
        Log.d(TAG, "currentTime: "+on_open);


/*
        shared_prefrence = new Shared_Preference(MainActivity.this);

        shared_prefrence.loadPrefrence();
        global_class = (GlobalClass)getApplicationContext();*/
        toolbar =  findViewById(R.id.toolbar);
      //  setSupportActionBar(toolbar);
     //   typeface = ResourcesCompat.getFont(this, R.font.orbitron_light);
      // getSupportActionBar().hide();

      //  getSupportActionBar().setHomeAsUpIndicator(R.mipmap.menu_new);
        tooltext = toolbar.findViewById(R.id.toolbar_title);


        if (savedInstanceState == null) {
           /* tooltext.setText("Product Catalogue");
            tooltext.setTextSize(18);
            tooltext.setTypeface(typeface);
            tooltext.setTextColor(Color.parseColor("#202020"));*/
            FragmentHome frag_name = new FragmentHome();
            FragmentManager manager = this.getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.flContent, frag_name, frag_name.getTag()).commit();
        }

        progressBar = new ProgressDialog(IntroductionAfterLogin.this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Loading...");

        device_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        // Find our drawer view
        mDrawer =  findViewById(R.id.drawer_layout);





        // toggle.syncState();


        if (DTSPreferences.SharedInstance(IntroductionAfterLogin.this).FirstLaunchApps()) {

            DTSPreferences.SharedInstance(IntroductionAfterLogin.this).setFirstTimeLaunchApps(false);

        }else {

            Intent intent = new Intent(IntroductionAfterLogin.this, FingerScan.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }














    public void checkLocationPermission(){


        // Asking user if explanation is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //Prompt the user once explanation has been shown
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);


        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
