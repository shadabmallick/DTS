package com.mobile.dts.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.dts.R;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentTourRestoreSetting extends Fragment {

    RecyclerView rv_category;

    String TAG="product";

    TextView tv_next;
    ImageView img_back;

    ArrayList<HashMap<String,String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tour_restore_two, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        // function();
        tv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentMessage1 fragment2 = new FragmentMessage1();
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
                FragmentTourkeepSafeSetting fragment2 = new FragmentTourkeepSafeSetting();
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

        tv_next = view.findViewById(R.id.tv_end_tour);
    }




}