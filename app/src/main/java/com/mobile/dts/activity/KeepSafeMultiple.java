package com.mobile.dts.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.mobile.dts.R;
import com.mobile.dts.adapter.FolderAdapter;
import com.mobile.dts.adapter.FolderAdapterGrid;
import com.mobile.dts.adapter.FolderAdapterImage;
import com.mobile.dts.adapter.MyRecyclerViewAdapter;
import com.mobile.dts.callbacks.ImageClickListner;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.model.FolderData;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.model.KeepSafeData;
import com.mobile.dts.model.PhotoDetailBean;
import com.mobile.dts.utills.Constants;
import com.nhaarman.supertooltips.ToolTipView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

import static com.mobile.dts.utills.Constants.CHILD;
import static com.mobile.dts.utills.Constants.appPref;

public class KeepSafeMultiple extends AppCompatActivity
        implements FolderAdapterGrid.ViewClickListener,
        FolderAdapterImage.ViewClickListener,
        ImageClickListner, View.OnClickListener ,
        FolderAdapter.ViewClickListener{
    private static final float TOTAL_VIDEO_IN_PX = 0;
    private static final int PICK_IMAGE_MULTIPLE = 7;
    private static final int PICK_IMAGE_SINGLE = 5;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    String TAG = "KeepSafeMultiple";
    RecyclerView rv_folder_images1,recyclerView, recycler_Folder,recyclerViewSubFolder;
    RelativeLayout rl_main,rel_bottom,rl_plus,rel_third_bottom,rel_fourth_bottom, layoutforprofileimage, rl_folder, rel_first_bottom;
    // ArrayList<ImageBean> sinceInstallList = new Utils().fetchFolderList(KeepSafeMultiple.this);
    private SharedPreferences sharedpreferences, settingsPref;
    Intent intent;
    ArrayList<KeepSafeData> keepSafeDataArrayList;
    private boolean isLongPressed = false;
    FolderAdapterImage folderAdapter;
    String tempDate = null;
    // Array of integers points to images stored in /res/drawable-ldpi/
    private SqlLiteHelper dtsDataBase;
    ArrayList<ImageBean> imageArrayList;
    ArrayList<KeepSafeData> imageArrayListKeepSafeData;
    MyRecyclerViewAdapter adapter;
    TextView tv_folder, tv_recent;
     int selectedFolder_id;
    private ProgressBar progress;
    private ToolTipView mTipView;
    ImageView id_add_folder, id_add_picture, id_add_cam;
    LinearLayout linear_show_folder,ll_edit_bottom,ll_export,ll_move,ll_delete,close;
    private ArrayList<String> selectedPhotoList;


    private boolean pick_photos;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keep_safe_multiple);
        progress = (ProgressBar) findViewById(R.id.progress);
                dtsDataBase = new SqlLiteHelper(this);
        sharedpreferences = getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        layoutforprofileimage = findViewById(R.id.layoutforprofileimage);
        rel_first_bottom = findViewById(R.id.rel_first_bottom);
        rel_fourth_bottom = findViewById(R.id.rel_fourth_bottom);
        rl_folder = findViewById(R.id.rl_folder);
        recyclerView = findViewById(R.id.rv_folder_images);
        rv_folder_images1 = findViewById(R.id.rv_folder_images1);
        tv_folder = findViewById(R.id.tv_folder);
        tv_recent = findViewById(R.id.tv_recent);
        id_add_cam = findViewById(R.id.id_add_cam);
        id_add_picture = findViewById(R.id.id_add_picture);
        id_add_folder = findViewById(R.id.id_add_folder);
        recycler_Folder = findViewById(R.id.recycler_folder);
        rel_third_bottom = findViewById(R.id.rel_third_bottom);
        ll_edit_bottom = findViewById(R.id.ll_edit_bottom);
        rel_bottom = findViewById(R.id.rel_bottom);
        rl_plus = findViewById(R.id.rl_plus);
        ll_export = findViewById(R.id.ll_export);
        ll_move = findViewById(R.id.ll_move);
        ll_delete = findViewById(R.id.ll_delete);
        close = findViewById(R.id.ll_magicwand);
        linear_show_folder = findViewById(R.id.linear_show_folder);
        recyclerViewSubFolder = findViewById(R.id.recycler_folder_new);
        rl_main = findViewById(R.id.rl_main);
        ArrayList<String> savedImageList = dtsDataBase.getSavedImageList();
        selectedPhotoList = new ArrayList<String>();
        imageArrayListKeepSafeData = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        rv_folder_images1.setLayoutManager(new LinearLayoutManager(this));
        recycler_Folder.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSubFolder.setLayoutManager(new LinearLayoutManager(this));

       ll_export.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

           }
       });
        ll_move.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               linear_show_folder.setVisibility(View.VISIBLE);
               linear_show_folder.setBackgroundColor(getResources().getColor(R.color.black_with_alpha));
               rl_main.setAlpha(0.7f);
               rl_main.setBackgroundColor(getResources().getColor(R.color.white_with_alpha));
               setFolderDataNew();


           }
       });
        ll_delete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

           }
       });
       close.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

           }
       });
        adapter = new MyRecyclerViewAdapter(KeepSafeMultiple.this,
                savedImageList,KeepSafeMultiple.this,isLongPressed);
        rv_folder_images1.setAdapter(adapter);

        setFolderData();
        KeepSafeData keepSafeData=new KeepSafeData();
        ImageBean _imageBean = new ImageBean();

        Log.d(TAG, "onCreate: "+selectedFolder_id);
        //  setFileData();
        layoutforprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                rl_folder.setVisibility(rl_folder.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        if (isLongPressed) {
            if (selectedPhotoList.contains(keepSafeData.getPhotoOriginalPath())) {
                String teset=keepSafeData.getPhotoOriginalPath();
                Log.d(TAG, "onCreate: "+teset);
                _imageBean.setChecked(true);
            } else {
                _imageBean.setChecked(false);
            }
        }
        rel_third_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CleanActivity.class);
                startActivity(intent);

            }
        });

        id_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_folder.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), CreateNewFolder.class);
                //tv_badge.setText(Integer.toString(tempLength));
                // intent.putExtra(Constants.galleryType, Constants.delete);
                startActivity(intent);
                finish();

            }
        });
        rel_first_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DtsGalleryActivity.class);
                //tv_badge.setText(Integer.toString(tempLength));
                intent.putExtra(Constants.galleryType, Constants.delete);
                startActivity(intent);
                finish();
            }
        });
        rel_fourth_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                //tv_badge.setText(Integer.toString(tempLength));
                // intent.putExtra(Constants.galleryType, Constants.delete);
                startActivity(intent);
                finish();
            }
        });
        id_add_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_folder.setVisibility(View.GONE);

                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_IMAGE_SINGLE);

            }
        });
/*
        id_add_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_folder.setVisibility(View.GONE);

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);

            }
        });
*/
        id_add_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_folder.setVisibility(View.GONE);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });


    }
    @Override
    public void onImageLongPressListner(int position, boolean isLongPressed) {
        this.isLongPressed = isLongPressed;
        Log.d(TAG, "onImageLongPressListner: "+isLongPressed);
        Log.d(TAG, "onImageLongPressListner: "+position);
        rel_bottom.setVisibility(View.GONE);
        rl_plus.setVisibility(View.GONE);
        ll_edit_bottom.setVisibility(View.VISIBLE);
        keepSafeDataArrayList.get(position).isShowCheckbox();

        selectedPhotoList.add(keepSafeDataArrayList.get(position).getPhotoOriginalPath());
        Log.d(TAG, "onImageLongPressListner: "+ keepSafeDataArrayList.get(position).getPhotoOriginalPath());
        Log.d(TAG, "onImageLongPressListner: "+  keepSafeDataArrayList.get(position).isShowCheckbox());

      //  showHideActionButton(true);
        keepSafeDataArrayList.get(position).setShowCheckbox(!keepSafeDataArrayList.get(position).isShowCheckbox());
        if (keepSafeDataArrayList.get(position).isShowCheckbox()) {
            selectedPhotoList.add(keepSafeDataArrayList.get(position).getPhotoOriginalPath());
        } else {
            selectedPhotoList.remove(keepSafeDataArrayList.get(position).getPhotoOriginalPath());
        }
        folderAdapter.notifyDataSetChanged();
    }
    @Override
    public void onImageClickListner(int position, boolean isLongPressed) {
        if (isLongPressed) {
            keepSafeDataArrayList.get(position).setShowCheckbox(!keepSafeDataArrayList.get(position).isShowCheckbox());
            folderAdapter.notifyDataSetChanged();
            if (keepSafeDataArrayList.get(position).isShowCheckbox()) {
                selectedPhotoList.add(keepSafeDataArrayList.get(position).getPhotoOriginalPath());
            } else {
                selectedPhotoList.remove(keepSafeDataArrayList.get(position).getPhotoOriginalPath());
            }
        } else {
            try {
                if (pick_photos) {
                    Intent intent = new Intent(this, ViewImageFromSafe.class);
                    intent.putExtra(Constants.KEY_POSITION, position);
                    intent.putExtra("entry_time",keepSafeDataArrayList.get(position).getEntryTime());
                    intent.putExtra("path",keepSafeDataArrayList.get(position).getPhotoOriginalPath());
                    intent.putExtra("Id",keepSafeDataArrayList.get(position).getId());
                  //  intent.putExtra(Constants.KEY_IS_SAVED_IMAGE, isSavedImage);
                    Context c = getApplicationContext();
                    boolean allowMultiple = false;
                    if (c instanceof Activity) {
                        Activity a = (Activity) c;
                        allowMultiple = a.getIntent()
                                .getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    }
                    intent.setAction(DtsGalleryActivity.PICK_PHOTOS);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
                    ActivityOptionsCompat options;
                    Activity context = this;
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(context);
                    context.startActivityForResult(intent,
                            DtsGalleryActivity.PICK_PHOTOS_REQUEST_CODE, options.toBundle());
                } else {
                    Intent intent = new Intent(this, ViewImageFromSafe.class);
                    intent.putExtra(Constants.KEY_POSITION, position);
                    intent.putExtra("entry_time",keepSafeDataArrayList.get(position).getEntryTime());
                    intent.putExtra("path",keepSafeDataArrayList.get(position).getPhotoOriginalPath());
                    intent.putExtra("Id",keepSafeDataArrayList.get(position).getId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            } catch (Exception e) {
                Log.e("Exception", e.fillInStackTrace().toString());

            }
        }
    }

    private void setFolderData() {

        ArrayList<FolderData> folderDataArrayList = dtsDataBase.getAllFolder();


          Collections.reverse(folderDataArrayList);

        if (folderDataArrayList.size() == 0) {
            tv_folder.setVisibility(View.GONE);
            tv_recent.setVisibility(View.GONE);
            Toast.makeText(this, "No Folder Found", Toast.LENGTH_SHORT).show();

        } else {
            selectedFolder_id = folderDataArrayList.get(0).getFolderId();

            Log.d(TAG, "setFolderData: " + selectedFolder_id);

            FolderAdapterGrid folderAdapter = new FolderAdapterGrid(KeepSafeMultiple.this,
                    folderDataArrayList);

            recycler_Folder.setAdapter(folderAdapter);
            folderAdapter.setViewClickListener(this);
            setFileData(selectedFolder_id);

           // new AsyncTaskLoadImage().execute(String.valueOf(selectedFolder_id));
        }
    }
    private void setFolderDataNew() {

        ArrayList<FolderData> folderDataArrayList = dtsDataBase.getAllFolder();

        FolderAdapter folderAdapter = new FolderAdapter(KeepSafeMultiple.this,
                folderDataArrayList);

        recyclerViewSubFolder.setAdapter(folderAdapter);
        folderAdapter.setViewClickListener(this);

           // new AsyncTaskLoadImage().execute(String.valueOf(selectedFolder_id));
        }



    @Override
    public void onImageClicked(int clickEvent, int folderId) {


      //  Log.d(TAG, "onImageClicked: " +keepSafeDataArrayList.get(clickEvent).isShowCheckbox());
        if(keepSafeDataArrayList.size()>0 && keepSafeDataArrayList.get(clickEvent).isShowCheckbox()){
            keepSafeDataArrayList.get(clickEvent).setShowCheckbox(false);
            Log.d(TAG, "onImageClicked: "+ keepSafeDataArrayList.get(clickEvent).getPhotoOriginalPath());
            ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(121, 0);

            for (int i = 0; i < selectedImages.size(); i++){

                PhotoDetailBean photoDetailBean = selectedImages.get(i);

                dtsDataBase.updateFolderId(folderId, photoDetailBean.getPhotoPath());

            }


            Toast.makeText(getApplicationContext(),"Image saved",Toast.LENGTH_SHORT).show();
            linear_show_folder.setVisibility(View.GONE);



            if (isLongPressed) {
                isLongPressed = false;

                folderAdapter.setIsLongPressed(isLongPressed);

                rel_bottom.setVisibility(View.VISIBLE);
                rl_plus.setVisibility(View.VISIBLE);
                ll_edit_bottom.setVisibility(View.GONE);
                setFileData(selectedFolder_id);
            }
        }
        else {
            setFileData(folderId);
            selectedFolder_id=folderId;
            Log.d(TAG, "onImageClicked: " + folderId);
        }
      //  keepSafeDataArrayList.get(clickEvent).getPhotoOriginalPath();
      //  new AsyncTaskLoadImage().execute(String.valueOf(folderId));

    }

    private ArrayList<PhotoDetailBean> getSelectedImageDetail(int id, long keepTime) {
        ArrayList<PhotoDetailBean> imageList = new ArrayList<PhotoDetailBean>();
        for (int i = 0; i < keepSafeDataArrayList.size(); i++) {
            if (keepSafeDataArrayList.get(i).isShowCheckbox() ) {
                PhotoDetailBean photoDetailBean = new PhotoDetailBean();
                String imagePath = keepSafeDataArrayList.get(i).getPhotoOriginalPath();
                photoDetailBean.setActionTime(Calendar.getInstance().getTimeInMillis());
                photoDetailBean.setPhotoPath(imagePath);
                long dateTimeinMillisec = new File(imagePath).lastModified();
                photoDetailBean.setTakenTime(dateTimeinMillisec);
                photoDetailBean.setPhotoOriginalPath(keepSafeDataArrayList.get(i).getPhotoOriginalPath());
                photoDetailBean.setPhotoLocalPath(keepSafeDataArrayList.get(i).getPhotoOriginalPath());
                if (id == R.id.ll_save) {
                    photoDetailBean.setSaved(1);
                    photoDetailBean.setSavedFor24Hours(0);
                    photoDetailBean.setDeleted(0);
                } else if (id == R.id.ll_keep_to) {
                    photoDetailBean.setSaved(0);
                    photoDetailBean.setSavedFor24Hours(1);
                    photoDetailBean.setDeleted(0);
                    photoDetailBean.setKeepTime(keepTime);
                } else if (id == R.id.ll_delete) {
                    photoDetailBean.setSaved(0);
                    photoDetailBean.setSavedFor24Hours(0);
                    photoDetailBean.setDeleted(1);
                } else if (id == R.id.ll_restore) {
                    photoDetailBean.setSaved(0);
                    photoDetailBean.setSavedFor24Hours(0);
                    photoDetailBean.setDeleted(0);
                }

                imageList.add(photoDetailBean);
            }
        }
        return imageList;
    }

    private void setFileData(int folder_id) {

      //  ArrayList<KeepSafeData> keepSafeDataArrayList = dtsDataBase.getFolderWiseImage(folder_id);
        keepSafeDataArrayList =
                dtsDataBase.getFolderWiseImages(String.valueOf(folder_id));

        folderAdapter = new FolderAdapterImage(KeepSafeMultiple.this,
                keepSafeDataArrayList,KeepSafeMultiple.this,isLongPressed);

        recyclerView.setAdapter(folderAdapter);
        folderAdapter.setViewClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_MULTIPLE) {

            Log.d(TAG, "++data" + data.getClipData().getItemCount());// Get count of image here.
            Log.d(TAG, "++count" + data.getClipData().getItemCount());

            for (int i = 0; i < data.getClipData().getItemCount(); i++){

                Uri selectedImage = data.getClipData().getItemAt(i).getUri();//As of now use static position 0 use as per itemcount.

                Bitmap bitmap = null;
                //        Uri selectedImage1 = data.getData();
                try {
                   // bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                   // ByteArrayOutputStream stream = new ByteArrayOutputStream();
                   // bitmap.compress(Bitmap.CompressFormat.PNG, 20, stream);
                   // byte[] byteArray = stream.toByteArray();
                   // Log.d(TAG, "onActivityResult: "+byteArray.length);
                   // setImageToTable(byteArray);
                   // bitmap.recycle();
                   String abc= getRealPathFromURI_API19(this,selectedImage);
                    Log.d(TAG, "onActivityResult: "+abc);
                    File auxFile = new File(selectedImage.toString());

                    Log.d(TAG, "auxFile" + auxFile.getAbsolutePath());
                    Log.d(TAG, "auxFile" + selectedImage.toString());
                     String file11=auxFile.getAbsolutePath();


                    setImageToTable(abc);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("+++ clipdate" + selectedImage);

            }

       /* ImageView imageView = (ImageView) findViewById(R.id.imgView);
        imageView.setImageBitmap(bitmap);*/
            //        }
        }

        if (requestCode == PICK_IMAGE_SINGLE) {
            Uri filePath = data.getData();
            Log.d(TAG, "onActivityResult: "+filePath);
          //  Uri selectedImage = data.;//As of now use static position 0 use as per itemcount.
            String abc= getRealPathFromURI_API19(this,filePath);
            Log.d(TAG, "onActivityResult: "+abc);
            Bitmap img= null;
            setImageToTable(abc);
            /*try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.PNG, 20, stream);
                byte[] byteArray = stream.toByteArray();
                setImageToTable(byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }*/


            if(img!= null) {
                Log.d(TAG, "onActivityResult: "+img);
            }

        }

        if (requestCode == CAMERA_REQUEST) {

            Bitmap image = (Bitmap) data.getExtras().get("data");
            Uri filePath = getImageUri(getApplicationContext(),image);
            Log.d(TAG, "onActivityResult: "+filePath);
          //  Uri selectedImage = data.;//As of now use static position 0 use as per itemcount.
            String abc= getRealPathFromUri(this,filePath);
            Log.d(TAG, "onActivityResult: "+abc);
            Bitmap img= null;
            setImageToTable(abc);
            /*try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.PNG, 20, stream);
                byte[] byteArray = stream.toByteArray();
                setImageToTable(byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }*/


            if(img!= null) {
                Log.d(TAG, "onActivityResult: "+img);
            }

        }


    }

    public void setImageToTable(String SelectedImage){

        KeepSafeData keepSafeData = new KeepSafeData();
        keepSafeData.setFolderId(selectedFolder_id);
        Calendar calendar = Calendar.getInstance();
        keepSafeData.setEntryTime(calendar.getTimeInMillis());
        keepSafeData.setPhotoOriginalPath(SelectedImage);

       // keepSafeData.setPhotoByte(getByteArrayFromFile(SelectedImage));
        Log.d(TAG, "setImageToTable: "+SelectedImage);

        dtsDataBase.updateFolderId(selectedFolder_id, SelectedImage);

       // dtsDataBase.insertToKeepSafe(keepSafeData);
    }

    public static byte[] getByteArrayFromFile(String filePath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];
            for (int readNum; (readNum = fis.read(b)) != -1; ) {
                bos.write(b, 0, readNum);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("mylog", e.toString());
        }
        return null;
    }
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setFolderData();
    }

    @Override
    public void onClick(View v) {

    }


    public class AsyncTaskLoadImage extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;
        ArrayList<KeepSafeData> keepSafeDataArrayList;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(KeepSafeMultiple.this,
                    "", "Loading...");
        }

        @Override
        protected String doInBackground(String... params) {

           /* keepSafeDataArrayList = dtsDataBase
                    .getFolderWiseImages(Integer.parseInt(params[0]));*/

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Log.d(TAG, "onPostExecute: "+result);
            setAdapterData(keepSafeDataArrayList);

        }

    }

    private void setAdapterData(ArrayList<KeepSafeData> keepSafeDataArrayList){
        folderAdapter = new FolderAdapterImage(KeepSafeMultiple.this,
                keepSafeDataArrayList,KeepSafeMultiple.this,isLongPressed);
        recyclerView.setAdapter(folderAdapter);
        folderAdapter.setViewClickListener(this);

    }
    @Override
    public void onBackPressed() {
        if (mTipView != null) {
            mTipView.remove();
            mTipView = null;
            ll_edit_bottom.setVisibility(View.GONE);

        }
        if (isLongPressed) {
            isLongPressed = false;

            folderAdapter.setIsLongPressed(isLongPressed);
            // isAllCheched = false;
            // linear_show_folder.setVisibility(View.GONE);
            rel_bottom.setVisibility(View.VISIBLE);
            rl_plus.setVisibility(View.VISIBLE);
            ll_edit_bottom.setVisibility(View.GONE);
            linear_show_folder.setVisibility(View.GONE);

        } else {

            Intent codeagain=new Intent(getApplicationContext(),DtsGalleryActivity.class);
            startActivity(codeagain);

        }


        }




}




