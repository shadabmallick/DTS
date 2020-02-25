package com.mobile.dts.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.mobile.dts.R;

import static com.mobile.dts.utills.GooglePlusLogin.MY_PREFS_NAME;


public class SafeSetting extends AppCompatActivity {
    ImageView img_back,img_code;
    TextInputEditText email,name;
    TextView txthelp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.safe_setting);
        img_back=findViewById(R.id.img_back);
        email=findViewById(R.id.email);
        name=findViewById(R.id.full_name);
        img_code=findViewById(R.id.img_code);
        txthelp=findViewById(R.id.txthelp);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String nametxt = prefs.getString("name", "");//"No name defined" is the default value.
        String  lastname = prefs.getString("lastname", ""); //0
        String  email_txt = prefs.getString("email", ""); //0

        email.setText(email_txt);
        name.setText(nametxt+ " "+lastname);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        img_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent code=new Intent(getApplicationContext(),ActivityCode.class);
                code.putExtra("code","No name defined");
                startActivity(code);
            }
        });
        txthelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HelpSupport.class);
                startActivity(intent);

            }
        });
    }
}
