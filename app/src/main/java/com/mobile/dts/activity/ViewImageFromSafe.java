package com.mobile.dts.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.mobile.dts.R;
import com.mobile.dts.adapter.FolderAdapter;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.helper.Function;
import com.mobile.dts.model.FolderData;
import com.mobile.dts.model.KeepSafeData;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;

import static com.mobile.dts.activity.DtsGalleryActivity.getByteArrayFromFile;


public class ViewImageFromSafe extends AppCompatActivity implements   FolderAdapter.ViewClickListener {
    String TAG="ViewImageFromSafe";
    String extraStr;
    int id;
    private SqlLiteHelper dtsDataBase;
    long path;
    ImageView GalleryPreviewImg,img_back,iimg_share;
    CropImageView mCropView;
    LinearLayout ll_magicwand,ll_restore,ll_delete,ll_move,ll_undo,ll_save,ll_save_undo,linear_show_folder;
    TextView image_modified;
    File imageFile;
    private RecyclerView recycler_folder;

    private SaveCallback mSaveCallback=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clean_image_restore);
        GalleryPreviewImg=findViewById(R.id.GalleryPreviewImg);
        ll_magicwand=findViewById(R.id.ll_magicwand);
        ll_undo=findViewById(R.id.ll_undo);
        ll_save_undo=findViewById(R.id.ll_save_undo);
        ll_save=findViewById(R.id.ll_save);
        mCropView =  findViewById(R.id.cropImageView);
        linear_show_folder =  findViewById(R.id.linear_show_folder);
        image_modified =  findViewById(R.id.img_modified_date);
        recycler_folder = findViewById(R.id.recycler_folder);
        ll_delete = findViewById(R.id.ll_delete);
        ll_move = findViewById(R.id.ll_move);
        iimg_share = findViewById(R.id.iimg_share);
        img_back = findViewById(R.id.img_back);
        ll_restore = findViewById(R.id.ll_restore);
        dtsDataBase = new SqlLiteHelper(this);
        recycler_folder.setLayoutManager(new LinearLayoutManager(this));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            extraStr = bundle.getString("path");
             path= bundle.getLong("entry_time");
             id= bundle.getInt("Id");

            Log.d(TAG, "onCreate: "+extraStr);
            Log.d(TAG, "onCreate: "+path);


        }
        image_modified.setText(Function.converToTime(path));
        File imgFile = new  File(extraStr);
        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView myImage =  findViewById(R.id.GalleryPreviewImg);

            myImage.setImageBitmap(myBitmap);

        }
        final Uri uri =  Uri.fromFile(new File(extraStr));
        imageFile = new File(extraStr);

        Log.d(TAG, "onCreate: "+uri);
        ll_magicwand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.setImageUriAsync(uri);
                mCropView.setVisibility(View.VISIBLE);
                GalleryPreviewImg.setVisibility(View.GONE);
                ll_save_undo.setVisibility(View.VISIBLE);
            }
        });
        ll_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.setVisibility(View.GONE);
                GalleryPreviewImg.setVisibility(View.VISIBLE);
                ll_save_undo.setVisibility(View.GONE);
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        iimg_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri;
                try {
                    screenshotUri = FileProvider.getUriForFile(ViewImageFromSafe.this,
                            getString(R.string.file_provider_authority),
                            new File(extraStr));
                    if (screenshotUri == null) {
                        screenshotUri = Uri.fromFile(new File(extraStr));
                    }
                } catch (Exception e) {
                    screenshotUri = Uri.fromFile(new File(extraStr));
                }
                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "Share image using"));
            }
        });
        ll_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri;
                try {
                    screenshotUri = FileProvider.getUriForFile(ViewImageFromSafe.this,
                            getString(R.string.file_provider_authority),
                            new File(extraStr));
                    if (screenshotUri == null) {
                        screenshotUri = Uri.fromFile(new File(extraStr));
                    }
                } catch (Exception e) {
                    screenshotUri = Uri.fromFile(new File(extraStr));
                }
                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "Share image using"));
            }
        });
        ll_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFolderData();
                linear_show_folder.setVisibility(View.VISIBLE);
                GalleryPreviewImg.setVisibility(View.GONE);

            }
        });
        ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

           dtsDataBase.deleteKeepSafeImage(extraStr);
           finish();
            }
        });
        ll_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                Bitmap cropped = mCropView.getCroppedImage();
                mCropView.setImageBitmap(cropped);

                if (cropped != null) {
                    try {
                        // build directory
                        if (imageFile.exists()){
                            imageFile.delete();
                        }

                        FileOutputStream fos = new FileOutputStream(imageFile);
                        cropped.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //   imageBeanArrayList.clear();

                //  File imageFile = new File(imageBeanArrayList.get(position).getImagePath());
                mCropView.setVisibility(View.GONE);
                GalleryPreviewImg.setVisibility(View.VISIBLE);
                ll_save_undo.setVisibility(View.GONE);


            }
        });
    }


    private void setFolderData(){

        ArrayList<FolderData> folderDataArrayList = dtsDataBase.getAllFolder();

        FolderAdapter folderAdapter = new FolderAdapter(ViewImageFromSafe.this,
                folderDataArrayList);

        recycler_folder.setAdapter(folderAdapter);
        folderAdapter.setViewClickListener(this);

    }
    @Override
    public void onImageClicked(int clickEvent, int folderId) {

        if (clickEvent == 0){

            KeepSafeData keepSafeData = new KeepSafeData();
            keepSafeData.setFolderId(folderId);
            Calendar calendar = Calendar.getInstance();

            keepSafeData.setEntryTime(calendar.getTimeInMillis());
            keepSafeData.setPhotoOriginalPath(extraStr);
            Log.d(TAG, "onImageClicked: "+extraStr);

            // keepSafeData.setPhotoByte(getByteArrayFromFile(selected_image));

            //dtsDataBase.insertToKeepSafe(keepSafeData);


            dtsDataBase.updateFolderId(folderId, extraStr);
            Toast.makeText(getApplicationContext(),"Image saved",Toast.LENGTH_SHORT).show();
            linear_show_folder.setVisibility(View.GONE);

        }else if (clickEvent == 1){

          //  deleteFolderDialog(folderId);

        }


    }

}
