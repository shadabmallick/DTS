package com.mobile.dts.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.dts.R;


import java.util.ArrayList;
import java.util.HashMap;

public class PermissionFragment1  extends Fragment {

    RecyclerView rv_category;

    String TAG="product";

    TextView tv_allow;

    ArrayList<HashMap<String,String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.permission2, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        // function();
        tv_allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionFragment2 fragment2 = new PermissionFragment2();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, fragment2);
                fragmentTransaction.commit();
            }
        });


        return view;
    }

    private void initialisation(View view) {

        /* rv_category = view.findViewById(R.id.rv_product);
         */
        tv_allow = view.findViewById(R.id.tv_allow);
    }




}