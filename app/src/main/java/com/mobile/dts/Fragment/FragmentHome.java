package com.mobile.dts.Fragment;


import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;
import com.mobile.dts.activity.HomeActivity;


import java.util.ArrayList;
import java.util.HashMap;

import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURI;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURIPATH;
import static com.mobile.dts.utills.Constants.appPref;

public class FragmentHome extends Fragment {

    RecyclerView rv_category;
    private static final int REQUEST_PERMISSIONS = 102;

    String TAG="product";

    TextView tv_allow_storage;
    private SharedPreferences sharedpreferences;
    private FirebaseAnalytics mFirebaseAnalytics;
    ArrayList<HashMap<String, String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.permission1, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        isAccessGranted();
       // function();
         tv_allow_storage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 PermissionFragment1 fragment2 = new PermissionFragment1();
                 FragmentManager fragmentManager = getFragmentManager();
                 FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                 fragmentTransaction.replace(R.id.flContent, fragment2);
                 fragmentTransaction.addToBackStack(null);
                 fragmentTransaction.commit();
             }
         });


        return view;
    }

    private void initialisation(View view) {

       /* rv_category = view.findViewById(R.id.rv_product);
        */
        tv_allow_storage = view.findViewById(R.id.tv_allow_storage);
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getActivity().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getActivity().getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getActivity().getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if ((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getActivity())) {
               // showOverlayPermissionDialog();

            }
        }
        sharedpreferences = getActivity().getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        setScreenNameFirebaseAnalytics("Home Screen");
        /*Use to get SD card permission*/
        String sdcardUri = sharedpreferences.getString(SDCARDROOTDIRECTORYURI, null);
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (sdcardUri != null &&
                        DocumentFile.fromTreeUri(getActivity(), Uri.parse(sdcardUri)).canWrite()) {
                } else {
                  //  sdCardPermissionNaugot();
                }

            } else {
                if (sdcardUri != null &&
                        DocumentFile.fromTreeUri(getActivity(), Uri.parse(sdcardUri)).canWrite()) {
                    String sdcardUriPath = sharedpreferences.getString(SDCARDROOTDIRECTORYURIPATH, null);
                    if (sdcardUriPath != null && sdcardUriPath.indexOf(":") + 1 == sdcardUriPath.length()) {
                    } else {
                      //  sdCardPermission();
                    }

                } else {
                   // sdCardPermission();
                }

            }

        } catch (Exception e) {
           // sdCardPermissionNaugot();
        }

    }

    private void setScreenNameFirebaseAnalytics(String screenName) {
        mFirebaseAnalytics.setCurrentScreen(getActivity(), screenName, this.getClass().getSimpleName());
    }

}