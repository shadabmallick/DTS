package com.mobile.dts.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.dts.R;
import com.mobile.dts.activity.FingerScan;


import java.util.ArrayList;
import java.util.HashMap;

public class FragmentTour1 extends Fragment {

    RecyclerView rv_category;

    String TAG="product";

    TextView tv_take_a_tour,may__be_later;

    ArrayList<HashMap<String,String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_tour, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        // function();
        tv_take_a_tour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentHomeTourSetting fragment2 = new FragmentHomeTourSetting();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, fragment2);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();

            }
        });
        may__be_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent finger=new Intent(getActivity(), FingerScan.class);
                getActivity().startActivity(finger);
            }
        });


        return view;
    }

    private void initialisation(View view) {

        /* rv_category = view.findViewById(R.id.rv_product);
         */
        tv_take_a_tour = view.findViewById(R.id.tv_take_a_tour);
        may__be_later = view.findViewById(R.id.may__be_later);
    }




}