package com.mobile.dts.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.dts.R;


public class CodeAgain extends AppCompatActivity {
    String TAG="";
    ImageView img_back;
    TextView tv_or;
    EditText button_enter4digit;
    SharedPreferences.Editor editor;
    String code;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourdigit_code);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.title_color));
        }
        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

      code=getIntent().getStringExtra("code");
        Log.d(TAG, "CodeAgain: "+code);

        tv_or=findViewById(R.id.tv_or);
        img_back=findViewById(R.id.img_back);

        button_enter4digit=findViewById(R.id.button_enter4digit);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.title_color));
        }
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_or.setText("Enter 4 digit code again");
        button_enter4digit.setTransformationMethod(new AsteriskPasswordTransformationMethod());
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
                return mSource.length(); // Return default
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
            { if(code.equals(button_enter4digit.getText().toString())) {
                Log.d(TAG, "onTextChanged: ");
                editor.putString("code", button_enter4digit.getText().toString());
                Intent codeagain = new Intent(getApplicationContext(), DtsGalleryActivity.class);
                codeagain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                codeagain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                editor.apply();
                startActivity(codeagain);
            }
            else {
                Toast.makeText(getApplicationContext(),"Code doesn't matches",Toast.LENGTH_LONG).show();
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


