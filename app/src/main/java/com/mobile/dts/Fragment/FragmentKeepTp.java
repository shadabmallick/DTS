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
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.dts.R;


import java.util.ArrayList;
import java.util.HashMap;

public class FragmentKeepTp extends Fragment {

    RecyclerView rv_category;

    String TAG="product";

    TextView tv_front;
    ImageView img_back;

    ArrayList<HashMap<String,String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tour_keep_safe, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        // function();
        tv_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTourkeepSafe fragment2 = new FragmentTourkeepSafe();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, fragment2);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();

            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHomeTour fragment2 = new FragmentHomeTour();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, fragment2);

                fragmentTransaction.commit();

            }
        });



        return view;
    }

    private void initialisation(View view) {

        img_back = view.findViewById(R.id.img_back);

        tv_front = view.findViewById(R.id.tv_next);
    }




}