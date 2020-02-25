package com.mobile.dts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile.dts.R;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.model.FolderData;

import java.io.File;
import java.util.Calendar;


public class CreateNewFolder extends AppCompatActivity {
    String TAG="CreateNewFolder";
    EditText edt_folder;
    private SqlLiteHelper dtsDataBase;
    TextView tv_ok;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_folder);
        edt_folder=findViewById(R.id.edt_folder);
        tv_ok=findViewById(R.id.tv_ok);

        dtsDataBase = new SqlLiteHelper(this);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if (edt_folder.getText().toString().trim().length() == 0){
                    Toast.makeText(CreateNewFolder.this, "Enter folder name",
                            Toast.LENGTH_SHORT).show();
                }else {

                    FolderData folderData = new FolderData();
                    folderData.setFolderName(edt_folder.getText().toString());

                    Calendar calendar = Calendar.getInstance();
                    folderData.setCreationTime(calendar.getTimeInMillis());

                    dtsDataBase.insertFolder(folderData);
                    Toast.makeText(CreateNewFolder.this, "Folder Created Succssfully",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), KeepSafeMultiple.class);
                    //tv_badge.setText(Integer.toString(tempLength));
                    // intent.putExtra(Constants.galleryType, Constants.delete);
                    startActivity(intent);
                    finish();

                }
            }
        });





    }
}
