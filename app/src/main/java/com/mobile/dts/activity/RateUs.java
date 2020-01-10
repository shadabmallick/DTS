package com.mobile.dts.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mobile.dts.R;

public class RateUs extends AppCompatActivity {

    String TAG="Rate";
    TextView tv_take_a_tour;
    ImageView img_back;
    RatingBar ratingbar;
    SharedPreferences.Editor editor;
    String rating;
    private SharedPreferences sharedpreferences;

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_us);
        tv_take_a_tour=findViewById(R.id.tv_take_a_tour);
        ratingbar=findViewById(R.id.ratingBar);

        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        rating = sharedpreferences.getString("rating", "");

        Log.d(TAG, "onCreate: "+rating);
        if (rating.equals("")){
            ratingbar.setRating(0);

        }
        else {
            ratingbar.setRating(Float.parseFloat(rating));
        }

        img_back=findViewById(R.id.img_back);
        tv_take_a_tour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RateUs=new Intent(getApplicationContext(),SafeService.class);
                startActivity(RateUs);
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                String rating1 = String.valueOf(ratingBar.getRating());
                Log.d(TAG, "onRatingChanged: "+rating1);
               // editor.clear();
                editor.putString("rating", rating1);
                editor.apply();

            }
        });
    }

}
