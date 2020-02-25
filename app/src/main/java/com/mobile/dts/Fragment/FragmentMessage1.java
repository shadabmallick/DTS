package com.mobile.dts.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.mobile.dts.activity.DtsGalleryActivity;
import com.mobile.dts.activity.FingerScan;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentMessage1 extends Fragment {

    RecyclerView rv_category;

    String TAG="product";

    TextView tv_take_a_tour;
    ImageView img_back;

    ArrayList<HashMap<String,String>> list_names;
    ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_one, container, false);


        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Loading...");


        initialisation(view);
        // function();
        tv_take_a_tour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent codeagain=new Intent(getActivity(), DtsGalleryActivity.class);
                startActivity(codeagain);
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTourRestoreSetting fragment2 = new FragmentTourRestoreSetting();
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

        tv_take_a_tour = view.findViewById(R.id.tv_take_a_tour);
    }




}