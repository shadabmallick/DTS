package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.mobile.dts.R;


public class BackupCode extends AppCompatActivity {
    EditText button_enter4digit;
    ImageView img_back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backupcode1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.title_color));
        }
        img_back=findViewById(R.id.img_back);
        button_enter4digit=findViewById(R.id.button_enter4digit);
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
            {
               // button_enter4digit.setText("");
                Intent codeagain=new Intent(getApplicationContext(),backupAgain.class);
                codeagain.putExtra("code",button_enter4digit.getText().toString());
                codeagain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                codeagain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(codeagain);


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


