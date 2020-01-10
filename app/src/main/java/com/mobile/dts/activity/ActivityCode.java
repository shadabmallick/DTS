package com.mobile.dts.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.mobile.dts.R;
import com.shashank.sony.fancytoastlib.FancyToast;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.mobile.dts.activity.CodeAgain.MY_PREFS_NAME;


public class ActivityCode extends AppCompatActivity {
    String TAG="ActivityCode";
    ImageView img_back;
    EditText button_enter4digit;
    String code,ifsound;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourdigit_code);
        img_back=findViewById(R.id.img_back);
        button_enter4digit=findViewById(R.id.button_enter4digit);
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        ifsound  = prefs.getString("code","");

       Bundle bundle = getIntent().getExtras();
        Log.d(TAG, "code2 : "+bundle);
       if (bundle != null){
           code = bundle.getString("code");
           Log.d(TAG, "code2 : "+code);
           Log.d(TAG, "code2 : "+ifsound);
       }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.title_color));
        }

        button_enter4digit.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return '*'; // This is the important part
            }
            public int length() {
                Log.d(TAG, "length: "+mSource.length());// Return default
                return mSource.length();

            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
          //  EditText button_enter4digit = (EditText) getCurrentFocus();

            if (button_enter4digit != null && button_enter4digit.length() ==4)
            {
             //   button_enter4digit.setText("");

                if(code.equals("No name defined")){
                    Intent codeagain=new Intent(getApplicationContext(),CodeAgain.class);
                    codeagain.putExtra("code",button_enter4digit.getText().toString());

                    codeagain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    codeagain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(codeagain);
                }
                else {

                    if(button_enter4digit.getText().toString().equals(ifsound)){
                        Log.d(TAG, "onTextChanged: ");
                        Intent codeagain=new Intent(getApplicationContext(),DtsGalleryActivity.class);
                        codeagain.putExtra("code",button_enter4digit.getText().toString());

                        codeagain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        codeagain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(codeagain);
                    }
                    else {
                        FancyToast.makeText(getApplicationContext(), "Code not match", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    }

                }



            }
        }

        // afterTextChanged
        @Override
        public void afterTextChanged(Editable s) {}

        // beforeTextChanged
        @Override
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
