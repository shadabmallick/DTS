package com.mobile.dts.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;
import com.mobile.dts.adapter.ImageGridAdapter;
import com.mobile.dts.callbacks.ImageClickListner;
import com.mobile.dts.callbacks.ImageMovedListener;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.dialogs.AlertDialogMessage;
import com.mobile.dts.helper.DtsWidget;
import com.mobile.dts.helper.Scheduler;
import com.mobile.dts.model.DateTimeBean;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.model.PhotoDetailBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.mobile.dts.utills.Constants.CHILD;
import static com.mobile.dts.utills.Constants.HEADER;
import static com.mobile.dts.utills.Constants.alarmRequestCode;
import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.defaultTimeset;
import static com.mobile.dts.utills.Constants.deletedImageBroadcast;
import static com.mobile.dts.utills.Constants.lastViewTime;
import static com.mobile.dts.utills.Constants.newImagenotificationRequestCode;
import static com.mobile.dts.utills.Utils.getDateTime;
import static com.mobile.dts.utills.Utils.getMonthName;

public class TemproryFile  extends AppCompatActivity implements ImageClickListner, View.OnClickListener,
        AdapterView.OnItemClickListener, OnMenuItemClickListener, ImageMovedListener {
    public static String  TAG="DtsGalleryActivity";
    public static final int PICK_PHOTOS_REQUEST_CODE = 1005;
    int length,tempLength;
    public static final String PICK_PHOTOS = "PICK_PHOTOS";
    public static ArrayList<ImageBean> imageArrayList = new ArrayList();
    private final Long[] KEEP_TO_LIST_ITEMS_TIME = new Long[]{Long.valueOf((24 * 60 * 60 * 1000)), Long.valueOf((7 * 24 * 60 * 60 * 1000)),
            Long.valueOf((2 * 7 * 24 * 60 * 60 * 1000)), Long.valueOf(30) * 24 * 60 * 60 * 1000};
    private final int REQUEST_PERMISSIONS = 1001;
    private ArrayList<ImageBean> tempArrayList = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RecyclerView recyclerView;
    private ImageGridAdapter imageGridAdapter;
    private boolean isLongPressed = false;
    private LinearLayout ll_actions_layout, ll_save_actions, select_all;
    private SharedPreferences sharedpreferences, settingsPref;
    private int noOfCollumns;
    private TextView nophotosfound, tv_heading, progressbartext,tv_erase,tv_top;
    private String setTime;
    private SqlLiteHelper dtsDataBase;
    TextView tv_badge;
    private boolean isRestoreScreen = false;
    private boolean      isSortByTime=false;
    private BroadcastReceiver deletedImagebroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ArrayList<PhotoDetailBean> deletedImageList = dtsDataBase.getRestoreImageList();
                imageArrayList = getRestoreGalleryData(deletedImageList);
                imageGridAdapter.updateReceiptsList(imageArrayList);
                length=imageArrayList.size();
                Log.d(TAG, "onReceive: "+length);
            } catch (Exception e) {
            }
        }
    };
    private AlertDialogMessage alertDialogMessage;
    private Boolean isAllCheched = false, isSavedImage = false;
    private ArrayList<PhotoDetailBean> deletedImages = null;
    private ImageButton icon_home, icon_filter;
    private ContextMenuDialogFragment mMenuDialogFragment;
    private FragmentManager fragmentManager;
    private ToolTipRelativeLayout mToolTipFrameLayout;
    private ToolTipView mTipView;
    private RelativeLayout layoutforprofileimage,rel_fourth_bottom,rel_second_bottom,rel_first_bottom, rel_middle,rel_bottom,progress_rl, ll_delete, ll_save, ll_restore, ll_keep_to, ll_share, ll_dtscancel;
    private int selectedMenu = 1;
    private ArrayList<String> selectedPhotoList;
    private boolean pick_photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dts_gallery);
        initViews();
        initObjects();
        initClickListner();
        configureAlarm();
        initMenuFragment();
        tempLength=imageArrayList.size();
        Log.d("TAG", "onCreate: "+tempLength);

        pick_photos = getIntent().getAction() != null && getIntent().getAction().equals(PICK_PHOTOS);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.gv_folder);
        ll_share = findViewById(R.id.ll_share);
        ll_save = findViewById(R.id.ll_save);
        ll_keep_to = findViewById(R.id.ll_keep_to);
        ll_delete = findViewById(R.id.ll_delete);
        ll_save_actions = findViewById(R.id.ll_save_actions);
        select_all = findViewById(R.id.ll_select_all);
        ll_actions_layout = findViewById(R.id.ll_actions_layout);
        nophotosfound = findViewById(R.id.nophotosfound);
        ll_restore = findViewById(R.id.ll_restore);
        ll_dtscancel = findViewById(R.id.ll_dtscancel);
        rel_fourth_bottom = findViewById(R.id.rel_fourth_bottom);
        layoutforprofileimage = findViewById(R.id.layoutforprofileimage);
        tv_heading = findViewById(R.id.tv_heading);
        icon_home = findViewById(R.id.icon_home);
        icon_filter = findViewById(R.id.icon_filter);
        progress_rl = findViewById(R.id.progress_rl);
        progressbartext = findViewById(R.id.progressbartext);
        rel_middle = findViewById(R.id.rel_middle);
        rel_bottom=findViewById(R.id.rel_bottom);
        rel_first_bottom=findViewById(R.id.rel_first_bottom);
        tv_erase=findViewById(R.id.tv_erase);
        tv_top=findViewById(R.id.tv_top);
        tv_badge = findViewById(R.id.tv_badge1);
        rel_second_bottom = findViewById(R.id.rel_second_bottom);
        //    tv_badge.setText(length);

        layoutforprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TemproryFile.this, DtsGalleryActivity.class);
                startActivity(intent);
                finish();
            }
        });
        rel_second_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent store=new Intent(getApplicationContext(),KeepSafeMultiple.class);
                store.putExtra("images",imageArrayList);
                // Log.d(TAG, "savedButtonClick: "+imageBean.getImagePath());
                startActivity(store);
            }
        });
        rel_fourth_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent store=new Intent(getApplicationContext(),SettingsActivity.class);

                // Log.d(TAG, "savedButtonClick: "+imageBean.getImagePath());
                startActivity(store);
            }
        });
    }

    private void initObjects() {
        fragmentManager = getSupportFragmentManager();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        sharedpreferences = getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        dtsDataBase = new SqlLiteHelper(this);
        alertDialogMessage = new AlertDialogMessage(this, getResources().getString(R.string.select_image_warning));
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mToolTipFrameLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipframelayout);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getInt(Constants.galleryType) == Constants.delete) {
                isRestoreScreen = true;
            }
        }
        selectedPhotoList = new ArrayList<String>();

    }

    private void initClickListner() {
        select_all.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_save.setOnClickListener(this);
        ll_delete.setOnClickListener(this);
        ll_keep_to.setOnClickListener(this);
        ll_restore.setOnClickListener(this);
        ll_dtscancel.setOnClickListener(this);
        icon_home.setOnClickListener(this);
        icon_filter.setOnClickListener(this);
    }

    /*Show Sort media file menu*/
    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        menuParams.setAnimationDuration(50);
        menuParams.setAnimationDelay(50);

        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(TemproryFile.this);
    }

    private List<MenuObject> getMenuObjects() {
        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();

        close.setResource(R.mipmap.icon_cancel);
        MenuObject time = new MenuObject(getResources().getString(R.string.sortbytime));
        MenuObject size = new MenuObject(getResources().getString(R.string.sortbysize));



        //size.setBitmap(b);
        menuObjects.add(close);
        menuObjects.add(time);
        menuObjects.add(size);


        MenuObject time24h = null;
        MenuObject time1wk = null;
        MenuObject time2wk = null;
        MenuObject time1mo = null;
        if (isRestoreScreen) {
            time24h = new MenuObject(getResources().getString(R.string.sortby24));
            // time24h.setResource(R.mipmap.ic_24hsort);
            time1wk = new MenuObject(getResources().getString(R.string.sortby1w));
            //  time1wk.setResource(R.mipmap.ic_1wksort);
            time2wk = new MenuObject(getResources().getString(R.string.sortby2w));
            // time2wk.setResource(R.mipmap.ic_2wsort);
            time1mo = new MenuObject(getResources().getString(R.string.sortby1m));
            //  time1mo.setResource(R.mipmap.ic_1mosort);
            menuObjects.add(time24h);
            menuObjects.add(time1wk);
            menuObjects.add(time2wk);
            menuObjects.add(time1mo);
        }
        switch (selectedMenu) {
            case 1:
                time.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                time.setBgColor(Color.parseColor("#D4E6F1"));
                break;
            case 2:
                size.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                size.setBgColor(Color.parseColor("#D4E6F1"));
                break;
            case 3:
                if (time24h != null) {
                    time24h.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                    time24h.setBgColor(Color.parseColor("#D4E6F1"));
                }
                break;
            case 4:
                if (time1wk != null) {
                    time1wk.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                    time1wk.setBgColor(Color.parseColor("#D4E6F1"));
                }
                break;
            case 5:
                if (time2wk != null) {
                    time2wk.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                    time2wk.setBgColor(Color.parseColor("#D4E6F1"));
                }
                break;
            case 6:
                if (time1mo != null) {
                    time1mo.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                    time1mo.setBgColor(Color.parseColor("#D4E6F1"));
                }
                break;
            default:
                time.setMenuTextAppearanceStyle(R.style.TextViewStyle);
                time.setBgColor(Color.parseColor("#D4E6F1"));
        }
        return menuObjects;
    }

    /*Show deleted media files progress on progress bar*/
    @Override
    public void onImageMoved(final int movedCount, final int totalCount) {
        TemproryFile.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = movedCount + 1 + " out of " + totalCount;
                progressbartext.setText(text);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRestoreScreen) {
            setScreenNameFirebaseAnalytics("KeepToo Gallery Screen");
        } else {
            setScreenNameFirebaseAnalytics("Restore Media Screen");
        }
        /*Calculate column dynamically  for grid gallery*/
        noOfCollumns = Utils.calculateNoOfColumns(this);
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            } else {
                Toast.makeText(this,
                        "Please take Storage permission to run keepToo properly", Toast.LENGTH_LONG).show();
                nophotosfound.setVisibility(View.VISIBLE);
            }
        } else {
            if(selectedMenu == 1) {
                /*Used to process media files using Asynctask*/
                new TemproryFile.ProcessMedia().execute();
            }else if(selectedMenu>1){
                filerClicked(selectedMenu);
            }
        }
    }


    /*Used to configure Scheduler*/
    private void configureAlarm() {
        setTime = defaultTimeset;
        Intent intent = new Intent(TemproryFile.this, Scheduler.class);
        intent.setAction(Constants.ACTION_ALARM_RECEIVER);
//        boolean isWorking = (PendingIntent.getBroadcast(DtsGalleryActivity.this,
//                alarmRequestCode, intent, PendingIntent.FLAG_NO_CREATE) != null);
        //  if(!isWorking) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TemproryFile.this, alarmRequestCode, intent, 0);
        AlarmManager am = (AlarmManager) TemproryFile.this.getSystemService(TemproryFile.this.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance()
                .getTime().getTime() + Long.parseLong(setTime) * 1000, pendingIntent);
        //  }
        // }
    }

    private void toggleToolbarText() {
        if (!isRestoreScreen) {
            //  tv_badge.setText(length);
            tv_heading.setText(getResources().getString(R.string.gallery));
            tv_erase.setVisibility(View.GONE);
            tv_top.setVisibility(View.VISIBLE);
        } else {
            tv_erase.setVisibility(View.VISIBLE);
            tv_top.setVisibility(View.GONE);

            tv_heading.setText(getResources().getString(R.string.restore_images));
        }
    }


    /*Get Dts gallery data*/

    private ArrayList<ImageBean> getDtsGalleryData() {
        isSavedImage = true;
        ArrayList<ImageBean> sinceInstallList = new Utils().fetchFolderList(TemproryFile.this);
        String tempDate = null;
        ArrayList<String> getRestoreImages = dtsDataBase.getRestoreImages();
        if (getRestoreImages != null && getRestoreImages.size() > 0) {
            ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
            for (ImageBean imageBean : sinceInstallList) {
                if (getRestoreImages.contains(imageBean.getImagePath())) {
                    imageBeans.add(imageBean);
                }
            }
            sinceInstallList.removeAll(imageBeans);
        }
        ArrayList<String> savedImageList = dtsDataBase.getImageList(Constants.saveImage);
        ArrayList<ImageBean> dtsList = new ArrayList<ImageBean>();
        if (sinceInstallList != null && sinceInstallList.size() > 0) {
            for (ImageBean imagePath : sinceInstallList) {
                long dateTimeinMillisec = imagePath.getCreatedTimeStamp();
                ImageBean _imageBean = new ImageBean();
                _imageBean.setImagePath(imagePath.getImagePath());
                _imageBean.setChecked(false);
                if (Utils.isImageFile(imagePath.getImagePath())) {
                    _imageBean.setMediaType(0);
                } else {
                    _imageBean.setMediaType(1);
                }
                DateTimeBean dateTimeBean = getDateTime(dateTimeinMillisec);
                _imageBean.setCreatedTimeStamp(dateTimeinMillisec);
                if (dateTimeBean != null) {
                    if (dateTimeBean.getDate() != null) {
                        _imageBean.setCreatedDate(dateTimeBean.getDate());
                        if (tempDate == null || tempDate != null && !tempDate.equals(dateTimeBean.getDate())) {
                            ImageBean imgBean = new ImageBean();
                            imgBean.setCreatedDate(getMonthName(dateTimeBean.getDate()) == null ? dateTimeBean.getDate()
                                    : getMonthName(dateTimeBean.getDate()));
                            imgBean.setViewType(HEADER);
                            dtsList.add(imgBean);
                        }
                        tempDate = dateTimeBean.getDate();
                    }
                    if (dateTimeBean.getTime() != null) {
                        _imageBean.setCreatedTime(dateTimeBean.getTime());
                    }
                }
                _imageBean.setViewType(CHILD);
                _imageBean.setImageSize(imagePath.getImageSize());
                if (savedImageList.contains(imagePath.getImagePath())) {
                    _imageBean.setNew(false);
                } else {
                    _imageBean.setNew(true);
                }
                if (isLongPressed) {
                    if (selectedPhotoList.contains(imagePath.getImagePath())) {
                        _imageBean.setChecked(true);
                    } else {
                        _imageBean.setChecked(false);
                    }
                }
                dtsList.add(_imageBean);
            }
        }

        /*Set recent viewed time*/
        Date currentTime = Calendar.getInstance().getTime();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong(lastViewTime, currentTime.getTime());
        editor.commit();
        //Cancel notification and call service to remove widget
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(newImagenotificationRequestCode);
        try {
            Scheduler.count = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(TemproryFile.this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(TemproryFile.this, DtsWidget.class));
                    } else {
                        startService(new Intent(TemproryFile.this, DtsWidget.class));
                    }
                }
            } else {
                startService(new Intent(TemproryFile.this, DtsWidget.class));
            }

        } catch (Exception e) {
            Log.e("Exception", e.fillInStackTrace().toString());
        }
        return dtsList;
    }

    /*Get restore gallry data*/
    private ArrayList<ImageBean> getRestoreGalleryData(ArrayList<PhotoDetailBean> photoList) {
        registerReceiver(deletedImagebroadcastReceiver, new IntentFilter(deletedImageBroadcast));
        isRestoreScreen = true;
        ArrayList<ImageBean> deletedImages = new ArrayList<ImageBean>();
        String deleteTimeInterval = Constants.defaultDeleteTimeInterval;
        for (PhotoDetailBean photoDetailBean : photoList) {
            ImageBean _imageBean = new ImageBean();
            _imageBean.setChecked(false);
            _imageBean.setActionTime(photoDetailBean.getActionTime());
            _imageBean.setFileOriginalPath(photoDetailBean.getPhotoOriginalPath());
            _imageBean.setFileLocalPath(photoDetailBean.getPhotoLocalPath());
            String name = photoDetailBean.getPhotoPath().substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
            _imageBean.setImageName(name);
            File file = new File(photoDetailBean.getPhotoPath());
            if (file.exists()) {
                DateTimeBean dateTimeBean = getDateTime(photoDetailBean.getTakenTime());
                if (dateTimeBean != null) {
                    if (dateTimeBean.getDate() != null) {
                        _imageBean.setCreatedDate(dateTimeBean.getDate());
                    }
                    if (dateTimeBean.getTime() != null) {
                        _imageBean.setCreatedTime(dateTimeBean.getTime());
                    }
                }
                int size = Integer.parseInt(String.valueOf(file.length() / 1024));
                _imageBean.setImageSize(size);
                _imageBean.setImagePath(photoDetailBean.getPhotoPath());
                _imageBean.setSaved24(photoDetailBean.isSavedFor24Hours() == 1);
                if (_imageBean.isSaved24()) {
                    _imageBean.setKeepTime(photoDetailBean.getKeepTime());
                }
                _imageBean.setDeleted(photoDetailBean.isDeleted() == 1);
                long actionTime = _imageBean.getActionTime();
                long timerTime;
                if (_imageBean.isSaved24()) {
                    timerTime = _imageBean.getKeepTime() - (Calendar.getInstance().getTimeInMillis() - actionTime);
                } else {
                    timerTime = (actionTime + Long.parseLong(deleteTimeInterval) * 1000) - Calendar.getInstance().getTimeInMillis();
                }
                if (timerTime < 0) {
                    timerTime = Long.parseLong(Constants.defaultDeleteTimeInterval);
                }

                if (isLongPressed) {
                    if (selectedPhotoList.contains(photoDetailBean.getPhotoPath())) {
                        _imageBean.setChecked(true);
                    } else {
                        _imageBean.setChecked(false);
                    }
                }
                _imageBean.setViewType(CHILD);
                _imageBean.setRemainingTime(timerTime);
                deletedImages.add(_imageBean);
            }
        }
        Collections.sort(deletedImages, new TemproryFile.DtsComparator());
        return deletedImages;
    }


    private void setScreenNameFirebaseAnalytics(String screenName) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, this.getClass().getSimpleName());
    }


    /*Handle Dts media file long press event*/
    @Override
    public void onImageLongPressListner(int position, boolean isLongPressed) {
        this.isLongPressed = isLongPressed;
        showHideActionButton(true);
        imageArrayList.get(position).setChecked(!imageArrayList.get(position).isChecked());
        if (imageArrayList.get(position).isChecked()) {
            selectedPhotoList.add(imageArrayList.get(position).getImagePath());
        } else {
            selectedPhotoList.remove(imageArrayList.get(position).getImagePath());
        }
        imageGridAdapter.notifyDataSetChanged();
    }

    /*Handle Dts media file click event*/
    @Override
    public void onImageClickListner(int position, boolean isLongPressed) {
        if (isLongPressed) {
            imageArrayList.get(position).setChecked(!imageArrayList.get(position).isChecked());
            imageGridAdapter.notifyDataSetChanged();
            if (imageArrayList.get(position).isChecked()) {
                selectedPhotoList.add(imageArrayList.get(position).getImagePath());
            } else {
                selectedPhotoList.remove(imageArrayList.get(position).getImagePath());
            }
        } else {
            try {
                if (pick_photos) {
                    Intent intent = new Intent(this, ImageViewerActivity.class);
                    intent.putExtra(Constants.KEY_POSITION, position);
                    intent.putExtra(Constants.KEY_IS_SAVED_IMAGE, isSavedImage);
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
                    Intent intent = new Intent(this, ImageViewerActivity.class);
                    intent.putExtra(Constants.KEY_POSITION, position);
                    intent.putExtra(Constants.KEY_IS_SAVED_IMAGE, isSavedImage);
                    intent.putExtra(Constants.KEY_IS_RESTORED_IMAGE, isRestoreScreen);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            } catch (Exception e) {
                Log.e("Exception", e.fillInStackTrace().toString());

            }
        }
    }


    /*Handle beack press event*/
    @Override
    public void onBackPressed() {
        if (mTipView != null) {
            mTipView.remove();
            mTipView = null;
        }
        if (isLongPressed) {
            isLongPressed = false;
            showHideActionButton(false);
            imageGridAdapter.setIsLongPressed(isLongPressed);
            isAllCheched = false;
            clearChecked();
        } else {

            homeScreen();
        }
    }

    /*Used to clear checked media files from Dts gallery*/
    private void clearChecked() {
        for (int i = 0; i < imageArrayList.size(); i++) {
            imageArrayList.get(i).setChecked(false);
        }
        imageGridAdapter.notifyDataSetChanged();
    }

    private void makeAllChecked() {
        selectedPhotoList.clear();
        for (int i = 0; i < imageArrayList.size(); i++) {
            imageArrayList.get(i).setChecked(true);
            if(imageArrayList.get(i).getViewType() == CHILD) {
                selectedPhotoList.add(imageArrayList.get(i).getImagePath());
            }
        }

        imageGridAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_share) {
            ArrayList<Uri> files = getSelectedImageUri();
            if (files.size() > 0) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/png");
                sharingIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(Intent.createChooser(sharingIntent, "Share image using"));

            } else {
                alertDialogMessage.show();
            }
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }
        } else if (v.getId() == R.id.ll_select_all) {
            if (!isAllCheched) {
                isAllCheched = true;
                makeAllChecked();
            } else {
                isAllCheched = false;
                clearChecked();
            }
            toggleLayout();
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.ll_save) {
            if (isRestoreScreen) {
                new TemproryFile.RestoreAsyncTask().execute(R.id.ll_save);
            } else {
                crudButtonAction(R.id.ll_save);
            }
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.ll_keep_to) {
            if (mTipView == null) {
                addToolTipView();
            } else {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.ll_delete) {
            try {
                crudButtonAction(R.id.ll_delete);
            } catch (Exception e) {
            }
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.ll_restore) {
            new TemproryFile.RestoreAsyncTask().execute(R.id.ll_restore);
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.ll_dtscancel) {
            if (isLongPressed) {
                isLongPressed = false;
                showHideActionButton(false);
                imageGridAdapter.setIsLongPressed(isLongPressed);
                clearChecked();
            }
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.icon_home) {
            homeScreen();
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        } else if (v.getId() == R.id.icon_filter) {
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }
            initMenuFragment();
            mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
        } else if (v.getId() == R.id.keep24) {
            if (isRestoreScreen) {
                ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(R.id.ll_keep_to, KEEP_TO_LIST_ITEMS_TIME[0]);
                dtsDataBase.updateKeepToFromRestoreDetails(selectedImages);
                deleteRestoreButtonClick(selectedImages);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                }
                selectedMenu = 1;
                initMenuFragment();
                icon_filter.setColorFilter(Color.argb(11,193,224,1));
            } else {
                new TemproryFile.KeepToAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[0]);
            }

        } else if (v.getId() == R.id.keep2w) {
            if (isRestoreScreen) {
                ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(R.id.ll_keep_to, KEEP_TO_LIST_ITEMS_TIME[2]);
                dtsDataBase.updateKeepToFromRestoreDetails(selectedImages);
                deleteRestoreButtonClick(selectedImages);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                }
                selectedMenu = 1;
                initMenuFragment();
                icon_filter.setColorFilter(Color.argb(11,193,224,1));
            } else {
                new TemproryFile.KeepToAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[2]);
            }

        } else if (v.getId() == R.id.keep1w) {
            if (isRestoreScreen) {
                ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(R.id.ll_keep_to, KEEP_TO_LIST_ITEMS_TIME[1]);
                dtsDataBase.updateKeepToFromRestoreDetails(selectedImages);
                deleteRestoreButtonClick(selectedImages);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                }
                selectedMenu = 1;
                initMenuFragment();
                icon_filter.setColorFilter(Color.argb(11,193,224,1));
            } else {
                new TemproryFile.KeepToAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[1]);
            }

        } else if (v.getId() == R.id.keep1m) {
            if (isRestoreScreen) {
                ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(R.id.ll_keep_to, KEEP_TO_LIST_ITEMS_TIME[3]);
                dtsDataBase.updateKeepToFromRestoreDetails(selectedImages);
                deleteRestoreButtonClick(selectedImages);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                }
                selectedMenu = 1;
                initMenuFragment();
                icon_filter.setColorFilter(Color.argb(11,193,224,1));
            } else {
                new TemproryFile.KeepToAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[3]);
            }
        }

    }


    /*Show "Keep to" option menu*/
    private void addToolTipView() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tooltip, null);
        ToolTip toolTip = new ToolTip()
                .withContentView(view)
                .withColor(Color.parseColor("#00ffffff"))
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        view.findViewById(R.id.keep24).setOnClickListener(TemproryFile.this);
        view.findViewById(R.id.keep2w).setOnClickListener(TemproryFile.this);
        view.findViewById(R.id.keep1w).setOnClickListener(TemproryFile.this);
        view.findViewById(R.id.keep1m).setOnClickListener(TemproryFile.this);
        mTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.ll_keep_to));
    }

    private void homeScreen() {
        Intent intent = new Intent(TemproryFile.this, DtsGalleryActivity.class);
        startActivity(intent);
        finish();
    }

    private void toggleLayout() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_unchecked);
        String text = getResources().getString(R.string.unselect_all);
        if (!isAllCheched) {
            drawable = getResources().getDrawable(R.drawable.ic_checked);
            text = getResources().getString(R.string.select_all);
        }
        ((ImageView) findViewById(R.id.iv_select_all)).setImageDrawable(drawable);
        ((TextView) findViewById(R.id.bu_select_all)).setText(text);

    }

    /*Handle delete and save button click*/
    private void crudButtonAction(int viewID) {
        boolean isSuccess;
        ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(viewID, 0);
        isSuccess = dtsDataBase.insertOrUpdatePhotoDetails(selectedImages);
        if (isSuccess) {
            if (viewID == R.id.ll_delete) {
                if (viewID == R.id.ll_delete) {
                    if (selectedImages != null && selectedImages.size() > 0) {
                        boolean confirmdelete = settingsPref.getBoolean("confirmdelete", true);
                        if (confirmdelete) {
                            showConfrimDeleteDialog(selectedImages);
                        } else {
                            deleteRestoreButtonClick(selectedImages);
                        }

                    } else {
                        Toast.makeText(TemproryFile.this,
                                "Select atleast one image to delete", Toast.LENGTH_LONG).show();
                    }
                }

            } else if (viewID == R.id.ll_save) {
                if (isRestoreScreen) {
                    /*Need to put code if any*/
                } else {
                    savedButtonClick(selectedImages);
                }
            }
        }
    }

    /*Handle delete and restore button click*/
    private void deleteRestoreButtonClick(ArrayList<PhotoDetailBean> selectedImages) {
        if (!isRestoreScreen) {
            ArrayList<ImageBean> getPhotoImageArraylist = new ArrayList<ImageBean>();
            for (ImageBean imageBean : imageArrayList) {
                if (imageBean.getViewType() == CHILD) {
                    for (PhotoDetailBean photoDetailBean : selectedImages) {
                        if (imageBean.getImagePath().equalsIgnoreCase(photoDetailBean.getPhotoPath())) {
                            getPhotoImageArraylist.add(imageBean);
                            break;
                        }
                    }
                }
            }
            int tempNum = 0;
            imageArrayList.removeAll(getPhotoImageArraylist);
            for (int i = 0; i < imageArrayList.size(); i++) {
                ImageBean imageBean = imageArrayList.get(i);
                if (imageBean.getViewType() == HEADER) {
                    if (tempNum + 1 == i) {
                        imageArrayList.remove(imageArrayList.get(tempNum));
                        Log.d(TAG, "deleteRestoreButtonClick: "+tempNum);
                    }
                    tempNum = i;
                }
            }
        } else {
            ArrayList<PhotoDetailBean> deletedImages = dtsDataBase.getRestoreImageList();
            imageArrayList = getRestoreGalleryData(deletedImages);
        }
        showHideActionButton(false);
        clearChecked();
        imageGridAdapter.setIsLongPressed(false);
        if (isRestoreScreen) {
            imageGridAdapter.updateReceiptsList(imageArrayList);
        } else {
            if (imageArrayList.size() == 1) {
                imageArrayList.clear();
            }
        }
        if (imageArrayList.size() > 0) {
            nophotosfound.setVisibility(View.GONE);
            length=imageArrayList.size();
            Log.d("TAG", "deleteRestoreButtonClick: "+length);
        } else {
            length=imageArrayList.size();
            Log.d("TAG", "deleteRestoreButtonClick: ");
            nophotosfound.setVisibility(View.VISIBLE);
        }
    }


    /*Get selected image URI to share*/
    private ArrayList<Uri> getSelectedImageUri() {
        ArrayList<Uri> filesToSend = new ArrayList<>();
        for (int i = 0; i < imageArrayList.size(); i++) {
            if (imageArrayList.get(i).isChecked() && imageArrayList.get(i).getViewType() == CHILD) {
                Uri uri;
                try {
                    uri = FileProvider.getUriForFile(TemproryFile.this,
                            getString(R.string.file_provider_authority),
                            new File(imageArrayList.get(i).getImagePath()));
                    if (uri == null) {
                        uri = Uri.fromFile(new File(imageArrayList.get(i).getImagePath()));
                    }

                } catch (Exception e) {
                    uri = Uri.fromFile(new File(imageArrayList.get(i).getImagePath()));
                }
                filesToSend.add(uri);
            }
        }
        return filesToSend;
    }


    /*Set selected Media file detail for DB action*/
    private ArrayList<PhotoDetailBean> getSelectedImageDetail(int id, long keepTime) {
        ArrayList<PhotoDetailBean> imageList = new ArrayList<PhotoDetailBean>();
        for (int i = 0; i < imageArrayList.size(); i++) {
            if (imageArrayList.get(i).isChecked() && imageArrayList.get(i).getViewType() == CHILD ) {
                PhotoDetailBean photoDetailBean = new PhotoDetailBean();
                String imagePath = imageArrayList.get(i).getImagePath();
                photoDetailBean.setActionTime(Calendar.getInstance().getTimeInMillis());
                photoDetailBean.setPhotoPath(imagePath);
                long dateTimeinMillisec = new File(imagePath).lastModified();
                photoDetailBean.setTakenTime(dateTimeinMillisec);
                photoDetailBean.setPhotoOriginalPath(imageArrayList.get(i).getFileOriginalPath());
                photoDetailBean.setPhotoLocalPath(imageArrayList.get(i).getFileLocalPath());
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


    private void savedButtonClick(ArrayList<PhotoDetailBean> selectedImages) {
        ArrayList<ImageBean> getPhotoImageArraylist = new ArrayList<ImageBean>();
        for (ImageBean imageBean : imageArrayList) {
            for (PhotoDetailBean photoDetailBean : selectedImages) {
                if (imageBean.getImagePath() != null && imageBean.getImagePath().equalsIgnoreCase(photoDetailBean.getPhotoPath())) {
                    imageBean.setNew(false);
                    break;
                }
            }
            getPhotoImageArraylist.add(imageBean);



        }
        /*Intent store=new Intent(getApplicationContext(),KeepSafeMultiple.class);
        store.putExtra("images",imageArrayList);
       // Log.d(TAG, "savedButtonClick: "+imageBean.getImagePath());
        startActivity(store);*/
        imageArrayList.clear();
        imageArrayList = getPhotoImageArraylist;
        showHideActionButton(false);
        clearChecked();
        imageGridAdapter.setIsLongPressed(false);
        imageGridAdapter.updateReceiptsList(imageArrayList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.img_menu, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    /*Handle Bottom bar button visibility*/
    private void showHideActionButton(boolean isShow) {
        if (!isRestoreScreen) {
            ll_restore.setVisibility(View.GONE);
            if (isShow) {
                ll_save_actions.setVisibility(View.VISIBLE);
                ll_actions_layout.setVisibility(View.VISIBLE);
                ll_share.setVisibility(View.VISIBLE);
                rel_bottom.setVisibility(View.GONE);
                rel_middle.setVisibility(View.GONE);
                ;
            } else {
                ll_save_actions.setVisibility(View.GONE);
                ll_actions_layout.setVisibility(View.GONE);
                ll_share.setVisibility(View.GONE);
                rel_bottom.setVisibility(View.VISIBLE);
                rel_middle.setVisibility(View.VISIBLE);

                selectedPhotoList.clear();
            }
        } else {
            ll_delete.setVisibility(View.GONE);
            ll_save.setVisibility(View.GONE);
            if (isShow) {
                ll_save_actions.setVisibility(View.VISIBLE);
                ll_restore.setVisibility(View.VISIBLE);
                rel_bottom.setVisibility(View.GONE);
                rel_middle.setVisibility(View.GONE);

            } else {
                ll_save_actions.setVisibility(View.GONE);
                ll_restore.setVisibility(View.GONE);
                ll_restore.setVisibility(View.VISIBLE);
                rel_bottom.setVisibility(View.VISIBLE);

                rel_middle.setVisibility(View.VISIBLE);
                selectedPhotoList.clear();
            }

        }
    }


    /*Handle Confirm delete dialog box*/
    public void showConfrimDeleteDialog(final ArrayList<PhotoDetailBean> selectedImages) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(TemproryFile.this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View layout = adbInflater.inflate(R.layout.checkbox, null);
        builder.setView(layout);
        builder.setCancelable(false);
        final CheckBox dontShowAgain = (CheckBox) layout.findViewById(R.id.skip);
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this selected image(s)?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRestoreButtonClick(selectedImages);
                        if (dontShowAgain.isChecked()) {
                            SharedPreferences.Editor editor = settingsPref.edit();
                            editor.putBoolean("confirmdelete", false);
                            editor.commit();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dontShowAgain.isChecked()) {
                            SharedPreferences.Editor editor = settingsPref.edit();
                            editor.putBoolean("confirmdelete", false);
                            editor.commit();
                        }
                    }
                })
                .setIcon(R.drawable.ic_caution)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  imageArrayList = null;
        if (isRestoreScreen) {
            try {
                unregisterReceiver(deletedImagebroadcastReceiver);
            } catch (Exception e) {
            }
        }
    }

    public void reArrangeNoCollumns(int noOfCollumns) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, noOfCollumns));
        imageGridAdapter.setImageDimen(Utils.getImageDimenByColumns(this, noOfCollumns));
        imageGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    }

    @Override
    public void onMenuItemClick(View view, int position) {

        if(selectedMenu == position){
            return;
        }

        filerClicked(position);
    }

    private void filerClicked(int position){

        if(position!=0) {
            selectedMenu = position;
        }

        if (position == 1) {
            if (isRestoreScreen) {
                imageArrayList.clear();
                imageArrayList.addAll(getRestoreGalleryData(dtsDataBase.getRestoreImageList()));
                ArrayList<ImageBean> length=getRestoreGalleryData(dtsDataBase.getRestoreImageList());
                Log.d(TAG, "ImageBean: "+length.size());
                Collections.sort(imageArrayList, new TemproryFile.DtsComparator());
            } else {
                if (tempArrayList == null) {
                    return;
                } else {
                    imageArrayList.clear();
                    for (ImageBean imageBean : tempArrayList) {
                        imageArrayList.add(imageBean);
                    }
                    tempArrayList.clear();
                    tempArrayList = null;
                }
            }
            if (imageGridAdapter != null) {
                imageGridAdapter.notifyDataSetChanged();
                if (imageArrayList.size() > 0) {
                    nophotosfound.setVisibility(View.GONE);
                    length=imageArrayList.size();
                    Log.d("TAG", "filerClicked: "+length);

                } else {
                    nophotosfound.setVisibility(View.VISIBLE);
                }
            }
            icon_filter.setColorFilter(Color.argb(11,193,224,1));

        } else if (position == 2) {
            if (isRestoreScreen) {
                imageArrayList.clear();
                imageArrayList.addAll(getRestoreGalleryData(dtsDataBase.getRestoreImageList()));
                length=imageArrayList.size();
                Log.d(TAG, "filerClicked: "+length);
                Collections.sort(imageArrayList, new Comparator<ImageBean>() {
                    @Override
                    public int compare(ImageBean rhs, ImageBean lhs) {
                        return rhs.getImageSize() < lhs.getImageSize() ?
                                1 : rhs.getImageSize() > lhs.getImageSize() ?
                                -1 : 0;

                    }
                });

            } else {
                tempArrayList = new ArrayList<ImageBean>();
                Log.d(TAG, "TEMP: "+tempArrayList.size());
                ArrayList<ImageBean> sortList = new ArrayList<ImageBean>();
                for (ImageBean imageBean : imageArrayList) {
                    tempArrayList.add(imageBean);
                    if(imageBean.getViewType() == CHILD){
                        sortList.add(imageBean);
                    }
                }

                imageArrayList.clear();

                for (ImageBean imageBean : sortList) {
                    if(imageBean.getViewType() == CHILD){
                        imageArrayList.add(imageBean);
                        Log.d(TAG, "TEMP: "+imageArrayList.size());

                    }
                }

                Collections.sort(imageArrayList, new Comparator<ImageBean>() {
                    @Override
                    public int compare(ImageBean rhs, ImageBean lhs) {
                        return rhs.getImageSize() < lhs.getImageSize() ?
                                1 : rhs.getImageSize() > lhs.getImageSize() ?
                                -1 : 0;

                    }
                });



            }
            if (imageGridAdapter != null) {
                imageGridAdapter.notifyDataSetChanged();
                if (imageArrayList.size() > 0) {
                    nophotosfound.setVisibility(View.GONE);
                } else {
                    nophotosfound.setVisibility(View.VISIBLE);
                }
            }
            icon_filter.setColorFilter(Color.argb(11,193,224,1));

        } else if (position == 3) {
            imageArrayList.clear();
            imageArrayList.addAll
                    (getRestoreGalleryData(dtsDataBase.getKeepToImageList(KEEP_TO_LIST_ITEMS_TIME[0])));
            Collections.sort(imageArrayList, new TemproryFile.DtsComparator());
            if (imageGridAdapter != null) {
                imageGridAdapter.notifyDataSetChanged();
                if (imageArrayList.size() > 0) {
                    nophotosfound.setVisibility(View.GONE);
                } else {
                    nophotosfound.setVisibility(View.VISIBLE);
                }
            }
            icon_filter.setColorFilter(Color.argb(11,193,224,1));
        } else if (position == 4) {
            imageArrayList.clear();
            imageArrayList.addAll(getRestoreGalleryData
                    (dtsDataBase.getKeepToImageList(KEEP_TO_LIST_ITEMS_TIME[1])));
            Collections.sort(imageArrayList, new TemproryFile.DtsComparator());
            if (imageGridAdapter != null) {
                imageGridAdapter.notifyDataSetChanged();
                if (imageArrayList.size() > 0) {
                    nophotosfound.setVisibility(View.GONE);
                } else {
                    nophotosfound.setVisibility(View.VISIBLE);
                }
            }
            icon_filter.setColorFilter(Color.argb(11,193,224,1));
        } else if (position == 5) {
            imageArrayList.clear();
            imageArrayList.addAll(getRestoreGalleryData
                    (dtsDataBase.getKeepToImageList(KEEP_TO_LIST_ITEMS_TIME[2])));
            Collections.sort(imageArrayList, new TemproryFile.DtsComparator());
            if (imageGridAdapter != null) {
                imageGridAdapter.notifyDataSetChanged();
                if (imageArrayList.size() > 0) {
                    nophotosfound.setVisibility(View.GONE);
                } else {
                    nophotosfound.setVisibility(View.VISIBLE);
                }
            }
            icon_filter.setColorFilter(Color.argb(11,193,224,1));
        } else if (position == 6) {
            imageArrayList.clear();
            imageArrayList.addAll(getRestoreGalleryData
                    (dtsDataBase.getKeepToImageList(KEEP_TO_LIST_ITEMS_TIME[3])));
            Collections.sort(imageArrayList, new TemproryFile.DtsComparator());
            if (imageGridAdapter != null) {
                imageGridAdapter.notifyDataSetChanged();
                if (imageArrayList.size() > 0) {
                    nophotosfound.setVisibility(View.GONE);
                } else {
                    nophotosfound.setVisibility(View.VISIBLE);
                }
            }
            icon_filter.setColorFilter(Color.argb(11,193,224,1));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_PHOTOS_REQUEST_CODE:
                if (resultCode != RESULT_CANCELED) {
                    setResult(RESULT_OK, data);
                    this.finish();
                }
                break;
            default:
                break;
        }
    }

    /*Use to process Dts Gallery media files*/
    class ProcessMedia extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.getInt(Constants.galleryType) == Constants.delete) {
                deletedImages = dtsDataBase.getRestoreImageList();
            }
            if (deletedImages == null) {
                // Show Dts gallery from Dts button from Home
                imageArrayList = getDtsGalleryData();

            } else {
                //Show gallery restore image
                imageArrayList = getRestoreGalleryData(deletedImages);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            progress_rl.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object o) {
            progress_rl.setVisibility(View.GONE);


            if (imageArrayList != null && imageArrayList.size() > 0) {
                if (imageGridAdapter == null) {
                    imageGridAdapter = new ImageGridAdapter(TemproryFile.this, TemproryFile.this,
                            imageArrayList, Utils.getImageDimenByColumns(TemproryFile.this, noOfCollumns), isLongPressed);
                    GridLayoutManager manager = new GridLayoutManager
                            (TemproryFile.this, noOfCollumns);
                    recyclerView.setLayoutManager(manager);
                    manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            if (imageArrayList != null && imageArrayList.size() > 0) {
                                if (imageArrayList.get(position).getViewType() == HEADER) {
                                    length=imageArrayList.size();
                                    Log.d(TAG, "getSpanSize: "+length);
                                    tv_badge.setText(Integer.toString(length));
                                    return (noOfCollumns);
                                } else {
                                    return (1);
                                }
                            }
                            return 0;
                        }
                    });
                    recyclerView.setAdapter(imageGridAdapter);
                } else {
                    imageGridAdapter.updateReceiptsList(imageArrayList);
                }
                nophotosfound.setVisibility(View.GONE);

            } else {
                nophotosfound.setVisibility(View.VISIBLE);
            }

            toggleToolbarText();
        }
    }


    /*Use to process Keep To media files*/
    class KeepToAsyncTask extends AsyncTask<Long, Void, ArrayList<PhotoDetailBean>> {
        @Override
        protected ArrayList<PhotoDetailBean> doInBackground(Long... longs) {
            long keepToTime = longs[0];
            ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(R.id.ll_keep_to, keepToTime);
            dtsDataBase.insertOrUpdatePhotoDetails(selectedImages);
            return selectedImages;
        }

        @Override
        protected void onPreExecute() {
            progress_rl.setVisibility(View.VISIBLE);
            progressbartext.setText("Processing...");
        }


        @Override
        protected void onPostExecute(ArrayList<PhotoDetailBean> photoDetailBeans) {
            super.onPostExecute(photoDetailBeans);
            deleteRestoreButtonClick(photoDetailBeans);
            progress_rl.setVisibility(View.GONE);
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        }
    }

    /*Use to process Restore media files*/
    class RestoreAsyncTask extends AsyncTask<Integer, Void, ArrayList<PhotoDetailBean>> {
        @Override
        protected ArrayList<PhotoDetailBean> doInBackground(Integer... integers) {
            ArrayList<PhotoDetailBean> selectedImages = getSelectedImageDetail(integers[0], 0);
            dtsDataBase.insertOrUpdatePhotoRestoreDetails(selectedImages);
            return selectedImages;
        }

        @Override
        protected void onPreExecute() {
            progress_rl.setVisibility(View.VISIBLE);
            progressbartext.setText("Processing...");
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoDetailBean> photoDetailBeans) {
            super.onPostExecute(photoDetailBeans);
            deleteRestoreButtonClick(photoDetailBeans);
            progress_rl.setVisibility(View.GONE);
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
            }

        }
    }

    /*Use to sort media files*/
    class DtsComparator implements Comparator<ImageBean> {
        @Override
        public int compare(ImageBean t1, ImageBean t2) {
            return Long.compare(t1.getRemainingTime(), t2.getRemainingTime());
        }
    }
    public void moveRestoreImageGallery() {
        Intent intent = new Intent(this, DtsGalleryActivity.class);
        //   tv_badge.setText(Integer.toString(tempLength));
        intent.putExtra(Constants.galleryType, Constants.delete);
        startActivity(intent);
        finish();
    }


}
