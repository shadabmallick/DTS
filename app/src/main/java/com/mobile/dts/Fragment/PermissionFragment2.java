package com.mobile.dts.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.dts.R;


import java.util.ArrayList;
import java.util.HashMap;

public class PermissionFragment2 extends Fragment {

    RecyclerView rv_category;

    String TAG="product";

    TextView tv_allow_ok;
    public final static int REQUEST_CODE = 10101;

    ArrayList<HashMap<String,String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.permission3, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        // function();
        tv_allow_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                checkDrawOverlayPermission();
            }
        });


        return view;
    }

    private void initialisation(View view) {

        /* rv_category = view.findViewById(R.id.rv_product);
         */
        tv_allow_ok = view.findViewById(R.id.tv_allow_ok);
    }


    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            FragmentTour1 fragment2 = new FragmentTour1();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, fragment2);
            fragmentTransaction.commit();
            return true;
        }
    }

}