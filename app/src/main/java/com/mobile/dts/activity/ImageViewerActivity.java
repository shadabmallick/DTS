package com.mobile.dts.activity;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.dts.R;
import com.mobile.dts.adapter.SlidingImageAdapter;
import com.mobile.dts.callbacks.ZoomImageClickListener;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.helper.DtsWidget;
import com.mobile.dts.helper.ImageViewerPager;
import com.mobile.dts.helper.Scheduler;
import com.mobile.dts.model.DateTimeBean;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.model.PhotoDetailBean;
import com.mobile.dts.utills.Constants;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import io.fabric.sdk.android.Fabric;
import static com.mobile.dts.utills.Constants.CHILD;
import static com.mobile.dts.utills.Constants.FROMIMAGEGALLERY;
import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.defaultDeleteTimeInterval;
import static com.mobile.dts.utills.Constants.deletedImageBroadcast;
import static com.mobile.dts.utills.Constants.lastViewTime;
import static com.mobile.dts.utills.Constants.newImagenotificationRequestCode;
/*Use to show Image viewer screen(Using click on Dts gallery or Restore gallery and Using Widget click)*/
public class ImageViewerActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, ToolTipView.OnToolTipViewClickedListener, ZoomImageClickListener {

    String TAG="ImageView";
    public static int zoomedValue = -1;
    final String[] KEEP_TO_LIST_ITEMS = new String[]{"24 Hours", "48 Hours", "1 Week", "1 Month"};
    final Long[] KEEP_TO_LIST_ITEMS_TIME = new Long[]{Long.valueOf((24 * 60 * 60 * 1000)), Long.valueOf((7 * 24 * 60 * 60 * 1000)),
            Long.valueOf((2 * 7 * 24 * 60 * 60 * 1000)), Long.valueOf(30) * 24 * 60 * 60 * 1000};
    private ImageView iv_back, iv_left, iv_right, iv_new;
    private TextView tv_image_indication_text, tv_image_name, tv_date, tv_time;
    private LinearLayout ll_save_actions, stopwatchmainly;
    private SqlLiteHelper dtsDataBase;
    private ArrayList<ImageBean> imageBeanArrayList;
    private ViewPager viewPager;
    private int position;
    private SlidingImageAdapter slidingImageAdapter = null;
    private TextView stopWatch;
    private boolean isRestoredImages, isSavedImage = false, isFromNotification, isShowBottomView, isOnPauseTrue;
    private CountDownTimer countDownTimer;
    private BroadcastReceiver deletedImagebroadcastReceiver;
    private BottomSheetDialog keepToBottomSheet;
    private SharedPreferences sharedpreferences;
    private boolean isRightToLeft;
    private ToolTipRelativeLayout mToolTipFrameLayout;
    private ToolTipView mTipView = null;
    private FrameLayout mainlayout;
    private AppBarLayout appbarlayout;
    private Timer timer;
    private RelativeLayout magicimage, saveImageBtn, save24Btn, shareBtn, deleteBtn, ll_restore, progress_rl, viewpagerlayout, savekeepsafe;
    private FirebaseAnalytics mFirebaseAnalytics;
    private long mLastClickTime = 0;
    private boolean isKeepToProcessing = false;
    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);
        Fabric.with(this, new Crashlytics());
        initViews();
        initClickListner();
        removeNotification();
        isRightToLeft = getResources().getBoolean(R.bool.is_right_to_left);
        if (isRightToLeft) {
            viewPager.setRotation(180);
        }
        mToolTipFrameLayout =  findViewById(R.id.activity_main_tooltipframelayout);

        oncreateData();
    }

    private void oncreateData(){
        /*Delete or remove image from screen after delete by scheduler*/
        deletedImagebroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
                    for (ImageBean imageBean : imageBeanArrayList) {
                        File file = new File(imageBean.getImagePath());
                        if (file.exists()) {
                            imageBeans.add(imageBean);
                        }
                    }
                    imageBeanArrayList.clear();
                    imageBeanArrayList.addAll(imageBeans);
                    if (imageBeanArrayList != null) {
                        slidingImageAdapter.notifyDataSetChanged();
                    }
                    setIndicatorText();
                    if (imageBeans.size() == 0) {
                        ImageViewerActivity.this.finish();
                    } else {
                        if (imageBeanArrayList.size() > 0) {
                            int _position = 0;
                            if (imageBeanArrayList.size() == 1) {
                                _position = 0;
                            } else {
                                _position = position - 2;
                            }
                            viewPager.setCurrentItem(_position, true);
                        }
                    }
                } catch (Exception e) {
                }
            }
        };

        initObjects();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                ImageViewerActivity.this.position = position;
                setIndicatorText();
                if (isRestoredImages) {
                    File imageFile = new File(imageBeanArrayList.get(position).getImagePath());
                    if (imageFile.exists()) {
                        if (ll_save_actions.getVisibility() == View.GONE) {
                            ll_save_actions.setVisibility(View.VISIBLE);
                        }
                        setStopWatch();
                        stopwatchmainly.setVisibility(View.VISIBLE);
                        if (imageBeanArrayList.get(position).isSaved24()) {
                            deleteBtn.setVisibility(View.VISIBLE);
                        } else {
                            deleteBtn.setVisibility(View.GONE);
                        }
                    } else {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        stopWatch.setText("");
                        if (ll_save_actions.getVisibility() == View.VISIBLE) {
                            ll_save_actions.setVisibility(View.GONE);
                        }
                    }
                }
                if (mTipView != null) {
                    mTipView.setVisibility(View.GONE);
                    mTipView.remove();
                    mTipView = null;
                    findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                }
                if (!isShowBottomView) {
                    runTimer(true);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                if (zoomedValue == -2) {
                    zoomedValue = position;
                }
                if (zoomedValue != -1 && zoomedValue != position) {
                    zoomedValue = -1;
                    slidingImageAdapter.notifyDataSetChanged();
                }
                if (!isShowBottomView) {
                    runTimer(true);
                }
            }
        });

        ImageViewerPager.enabled = true;
    }
    /*Set indicator text for Media file*/
    private void setIndicatorText() {
        if (imageBeanArrayList.size() > 0) {
            String text = String.format(getResources().getString(R.string.image_indication_text), (position + 1 + ""), imageBeanArrayList.size());
            tv_image_indication_text.setText(text);
            tv_image_name.setText((imageBeanArrayList.get(position).getImageName()));
            tv_date.setText(imageBeanArrayList.get(position).getCreatedDate());
            tv_time.setText(imageBeanArrayList.get(position).getCreatedTime());
            if (imageBeanArrayList.get(position).isNew() && !isShowBottomView) {
                iv_new.setVisibility(View.GONE);
            } else {
                iv_new.setVisibility(View.GONE);
            }
        }
    }
    /*Set Stop watch for restore media*/
    private void setStopWatch() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startStopWatch();
    }
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        saveImageBtn = findViewById(R.id.saveimage);
        save24Btn = findViewById(R.id.saveimage24);
        shareBtn = findViewById(R.id.shareimage);
        deleteBtn = findViewById(R.id.deleteimage);
        iv_back = findViewById(R.id.iv_back);
        tv_image_indication_text = findViewById(R.id.tv_image_indication);
        iv_left = findViewById(R.id.iv_left);
        iv_right = findViewById(R.id.iv_right);
        iv_new = findViewById(R.id.iv_new);
        magicimage = findViewById(R.id.magicimage);
        ll_save_actions = findViewById(R.id.ll_save_actions);
        ll_restore = findViewById(R.id.ll_restore);
        tv_image_name = findViewById(R.id.tv_image_name);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        stopWatch = findViewById(R.id.stopWatch);
        stopwatchmainly = findViewById(R.id.stopwatchmainly);
        mainlayout = findViewById(R.id.mainlayout);
        appbarlayout = findViewById(R.id.appbarlayout);
        progress_rl = findViewById(R.id.progress_rl);
        viewpagerlayout = findViewById(R.id.viewpagerlayout);
        savekeepsafe = findViewById(R.id.savekeepsafe);
    }
    private void initObjects() {
        isRestoredImages = getIntent().getBooleanExtra(Constants.KEY_IS_RESTORED_IMAGE, false);
        isSavedImage = getIntent().getBooleanExtra(Constants.KEY_IS_SAVED_IMAGE, false);
        if (isSavedImage || isRestoredImages) {
            imageBeanArrayList = new ArrayList<ImageBean>();
            for (ImageBean imageBean : DtsGalleryActivity.imageArrayList) {
                imageBeanArrayList.add(imageBean);
            }
        } else {
            int deleteCount = 0;
            try {
                if (Scheduler.newImages != null && Scheduler.newImages.size() != 0) {
                    ArrayList<ImageBean> imageBeanArrayListLocal = getImageArray(Scheduler.newImages);
                    imageBeanArrayList = new ArrayList<ImageBean>();
                    for (ImageBean imageBean : imageBeanArrayListLocal) {
                        File imageFile = new File(imageBean.getImagePath());
                        if (imageFile.exists()) {
                            imageBeanArrayList.add(imageBean);
                        } else {
                            ++deleteCount;
                        }
                    }
                    if (deleteCount > 0) {
                        Toast.makeText(ImageViewerActivity.this,
                                deleteCount + " Media file(s) could be deleted by user", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent intent = new Intent(this, SplashActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }catch (Exception e){
                Log.e("Exception", e.fillInStackTrace().toString());
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        if(imageBeanArrayList==null || imageBeanArrayList!=null &&  imageBeanArrayList.size()==0){

            ImageViewerActivity.this.finish();

            return;

        }

        position = getIntent().getIntExtra(Constants.KEY_POSITION, 0);
        String imagePath = imageBeanArrayList.get(position).getImagePath();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (isRestoredImages) {
            ll_restore.setVisibility(View.VISIBLE);
            ll_save_actions.setVisibility(View.VISIBLE);
            saveImageBtn.setVisibility(View.GONE);
        } else {
            ll_restore.setVisibility(View.GONE);
            ll_save_actions.setVisibility(View.VISIBLE);
            stopwatchmainly.setVisibility(View.GONE);
        }
        isFromNotification = getIntent().getBooleanExtra(Constants.KEY_IS_FROM_NOTIFICATION, false);
        if (isFromNotification) {
            iv_back.setVisibility(View.GONE);
        }
        dtsDataBase = new SqlLiteHelper(this);
        tv_image_name.setText(imageBeanArrayList.get(position).getImageName());
        tv_date.setText(imageBeanArrayList.get(position).getCreatedDate());
        tv_time.setText(imageBeanArrayList.get(position).getCreatedTime());
        if (isSavedImage || isRestoredImages) {
            ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
            for (int i = 0; i < imageBeanArrayList.size(); i++) {
                ImageBean imageBean = imageBeanArrayList.get(i);
                if (imageBean.getViewType() == CHILD) {
                    if (imageBean.getImagePath().equals(imagePath)) {
                        position = imageBeans.size();
                    }
                    imageBeans.add(imageBean);
                }
            }
            imageBeanArrayList.clear();
            imageBeanArrayList.addAll(imageBeans);
        }
        slidingImageAdapter = new SlidingImageAdapter(this, imageBeanArrayList);
        viewPager.setAdapter(slidingImageAdapter);
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(5);
        viewPager.setCurrentItem(position, true);
        if (isRestoredImages) {
            File imageFile = new File(imageBeanArrayList.get(position).getImagePath());
            if (imageFile.exists()) {
                setStopWatch();
                stopwatchmainly.setVisibility(View.VISIBLE);
                if (imageBeanArrayList.get(position).isSaved24()) {
                    deleteBtn.setVisibility(View.VISIBLE);
                } else {
                    deleteBtn.setVisibility(View.GONE);
                }
            } else {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                stopWatch.setText("");
            }
            registerReceiver(deletedImagebroadcastReceiver, new IntentFilter(deletedImageBroadcast));
        }
        sharedpreferences = getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        setIndicatorText();
        removeNotification();
        setScreenNameFirebaseAnalytics("Image viewer screen", null);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isOnPauseTrue = false;
        isShowBottomView = true;
        slideToTop();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        removeNotification();
        isRestoredImages = intent.getBooleanExtra(Constants.KEY_IS_RESTORED_IMAGE, false);
        isSavedImage = intent.getBooleanExtra(Constants.KEY_IS_SAVED_IMAGE, false);
        if (isSavedImage || isRestoredImages) {
            // to do
        }else {
            oncreateData();
            isRestoredImages = intent.getBooleanExtra(Constants.KEY_IS_RESTORED_IMAGE, false);
            isSavedImage = intent.getBooleanExtra(Constants.KEY_IS_SAVED_IMAGE, false);
            ImageViewerActivity.this.position = 0;
            viewPager.setCurrentItem(position, true);
            clickFromWidget();
        }
    }
    private void clickFromWidget(){
        if (isSavedImage || isRestoredImages) {
            // to do
        }else if(slidingImageAdapter!=null){
            try {
                if (Scheduler.newImages != null && Scheduler.newImages.size() != 0) {
                    ArrayList<ImageBean> imageBeanArrayListLocal = getImageArray(Scheduler.newImages);
                    imageBeanArrayList.clear();
                    for (ImageBean imageBean : imageBeanArrayListLocal) {
                        File imageFile = new File(imageBean.getImagePath());
                        if (imageFile.exists()) {
                            imageBeanArrayList.add(imageBean);
                        }
                    }
                   /* if (deleteCount > 0) {
                        Toast.makeText(ImageViewerActivity.this,
                                deleteCount + " Media file(s) could be deleted by user", Toast.LENGTH_LONG).show();
                    }*/
                } else {
                    Intent intent = new Intent(this, SplashActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e("Exception", e.fillInStackTrace().toString());
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            setIndicatorText();
            slidingImageAdapter.notifyDataSetChanged();
        }
    }
    private void setScreenNameFirebaseAnalytics(String screenName, String className) {
        mFirebaseAnalytics.setCurrentScreen(this, screenName, className /* class override */);
    }
    /*Added more info to show on Image viewer*/
    private ArrayList<ImageBean> getImageArray(ArrayList<ImageBean> newImages) {
        ArrayList<ImageBean> imageBeanArrayList = new ArrayList<>();
        for (int i = 0; i < newImages.size(); i++) {
            ImageBean imageBean = new ImageBean();
            imageBean.setImagePath(newImages.get(i).getImagePath());
            imageBean.setImageSize(newImages.get(i).getImageSize());
            DateTimeBean dateTimeBean = getDateTime(newImages.get(i).getCreatedTimeStamp());
            if (dateTimeBean != null) {
                if (dateTimeBean.getDate() != null) {
                    imageBean.setCreatedDate(dateTimeBean.getDate());
                }
                imageBean.setCreatedTime(dateTimeBean.getTime());
            }
            if (dateTimeBean.getTime() != null) {
            }
            imageBeanArrayList.add(imageBean);
        }
        return imageBeanArrayList;
    }
    private DateTimeBean getDateTime(long timeStamp) {
        try {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
            sdfTime.setTimeZone(tz);
            sdfDate.setTimeZone(tz);
            String localTime = sdfTime.format(new Date(timeStamp).getTime());
            String localTimeDate = sdfDate.format(new Date(timeStamp).getTime());
            DateTimeBean dateTimeBean = new DateTimeBean();
            dateTimeBean.setTime(localTime);
            dateTimeBean.setDate(localTimeDate);
            return dateTimeBean;
        } catch (Exception e) {
        }
        return null;
    }
    private void initClickListner() {
        saveImageBtn.setOnClickListener(this);
        save24Btn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_left.setOnClickListener(this);
        iv_right.setOnClickListener(this);
        ll_restore.setOnClickListener(this);
        savekeepsafe.setOnClickListener(this);
    }
    @Override
    public void onBackPressed() {
        if(!isKeepToProcessing) {
            ImageViewerPager.enabled = true;
            if (isRestoredImages) {
                finish();
            } else if (isSavedImage) {
                finish();
            } else {
                finish();
            }
        }
    }
    @Override
    public void onClick(View v) {
        ImageViewerPager.enabled = true;
        if (v.getId() == R.id.shareimage) {
            runTimer(false);
            if (imageBeanArrayList.get(position).getImagePath() != null) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri;
                try {
                    screenshotUri = FileProvider.getUriForFile(ImageViewerActivity.this,
                            getString(R.string.file_provider_authority),
                            new File(imageBeanArrayList.get(position).getImagePath()));
                    if (screenshotUri == null) {
                        screenshotUri = Uri.fromFile(new File(imageBeanArrayList.get(position).getImagePath()));
                    }
                } catch (Exception e) {
                    screenshotUri = Uri.fromFile(new File(imageBeanArrayList.get(position).getImagePath()));
                }
                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "Share image using"));
            } else {
                Toast.makeText(this, "plaese select image to share", Toast.LENGTH_SHORT).show();
            }
            if (mTipView != null) {
                mTipView.setVisibility(View.GONE);
                mTipView.remove();
                mTipView = null;
                findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }
            runTimer(true);
        } else if (v.getId() == R.id.saveimage) {/*Handle save button click*/
            runTimer(false);
            if (isRestoredImages) {
                new SaveAsyncTask().execute();
            } else {
                crudButtonAction(R.id.saveimage, 0);
            }
            if (mTipView != null) {
                mTipView.setVisibility(View.GONE);
                mTipView.remove();
                mTipView = null;
                findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }
            runTimer(true);
        } else if (v.getId() == R.id.saveimage24) { /*Handle Keep to button click*/
            runTimer(false);
            if (mTipView == null) {
                findViewById(R.id.card_view).setVisibility(View.GONE);
                addToolTipView();
            } else {
                mTipView.remove();
                mTipView = null;
                findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }
            runTimer(true);
        } else if (v.getId() == R.id.deleteimage) { /*Handle delete button click*/
            if (SystemClock.elapsedRealtime() - mLastClickTime < 150) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            runTimer(false);
            crudButtonAction(R.id.deleteimage, 0);
            if (mTipView != null) {
                mTipView.setVisibility(View.GONE);
                mTipView.remove();
                mTipView = null;
                findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }
            runTimer(true);
        } else if (v.getId() == R.id.iv_left) {
            if (position > 0)
                position = position - 1;
            viewPager.setCurrentItem(position, true);
        } else if (v.getId() == R.id.iv_right) {
            if (position < imageBeanArrayList.size() - 1)
                position = position + 1;
            viewPager.setCurrentItem(position, true);
        } else if (v.getId() == R.id.ll_restore) {
            runTimer(false);
            new RestoreAsyncTask().execute();/*Process restore media file*/
        } else if (v.getId() == R.id.iv_back) {
            if(!isKeepToProcessing) {
                ImageViewerPager.enabled = true;
                if (isRestoredImages) {
                    finish();
                } else if (isSavedImage) {
                    finish();
                } else {
                    finish();
                }
            }
        } else if (v.getId() == R.id.keep24) {
            runTimer(false);
            if (isRestoredImages) {
                PhotoDetailBean selectedImage = getSelectedImageDetail(R.id.saveimage24, imageBeanArrayList.get(position), KEEP_TO_LIST_ITEMS_TIME[0]);
                afterKeepToinRestore(selectedImage);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                    findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                }
                runTimer(true);
            } else {
                /*Handling keep to media file if clicked from Dts gallery*/
                new KeepImageAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[0]);
            }
        } else if (v.getId() == R.id.keep2w) {
            runTimer(false);
            if (isRestoredImages) {
                PhotoDetailBean selectedImage = getSelectedImageDetail(R.id.saveimage24, imageBeanArrayList.get(position), KEEP_TO_LIST_ITEMS_TIME[2]);
                afterKeepToinRestore(selectedImage);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                    findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                }
                runTimer(true);
            } else {
                new KeepImageAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[2]);
            }
        } else if (v.getId() == R.id.keep1w) {
            runTimer(false);
            if (isRestoredImages) {
                PhotoDetailBean selectedImage = getSelectedImageDetail(R.id.saveimage24, imageBeanArrayList.get(position), KEEP_TO_LIST_ITEMS_TIME[1]);
                afterKeepToinRestore(selectedImage);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                    findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                }
                runTimer(true);
            } else {
                new KeepImageAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[1]);
            }
        } else if (v.getId() == R.id.keep1m) {
            runTimer(false);
            if (isRestoredImages) {
                PhotoDetailBean selectedImage = getSelectedImageDetail(R.id.saveimage24, imageBeanArrayList.get(position), KEEP_TO_LIST_ITEMS_TIME[3]);
                afterKeepToinRestore(selectedImage);
                if (mTipView != null) {
                    mTipView.remove();
                    mTipView = null;
                    findViewById(R.id.card_view).setVisibility(View.VISIBLE);
                }
                runTimer(true);
            } else {
                new KeepImageAsyncTask().execute(KEEP_TO_LIST_ITEMS_TIME[3]);
            }
        } else if (v.getId() == R.id.savekeepsafe){


        }
    }
    /*Handle Keep To file from restore screen*/
    private void afterKeepToinRestore(PhotoDetailBean selectedImage) {
        dtsDataBase.updateKeepToFromRestoreDetail(selectedImage);
        imageBeanArrayList.remove(position);
        if (imageBeanArrayList.size() > 0) {
            if (isRestoredImages) {
                setStopWatch();
            }
            if (imageBeanArrayList != null) {
                slidingImageAdapter.notifyDataSetChanged();
            }
            setIndicatorText();
        } else {
            super.finish();
        }
    }
    /*Show Keep To option menu*/
    private void addToolTipView() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tooltip, null);
        ToolTip toolTip = new ToolTip()
                .withContentView(view)
                .withColor(Color.parseColor("#00ffffff"))
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);
        view.findViewById(R.id.keep24).setOnClickListener(ImageViewerActivity.this);
        view.findViewById(R.id.keep1w).setOnClickListener(ImageViewerActivity.this);
        view.findViewById(R.id.keep2w).setOnClickListener(ImageViewerActivity.this);
        view.findViewById(R.id.keep1m).setOnClickListener(ImageViewerActivity.this);
        mTipView = mToolTipFrameLayout.showToolTipForView(toolTip, findViewById(R.id.saveimage24));
        mTipView.setOnToolTipViewClickedListener(this);
    }
    
    private void crudButtonAction(int viewID, long keepTime) {
        boolean isSuccess;
        if(imageBeanArrayList!=null && imageBeanArrayList.size()>0) {
            PhotoDetailBean selectedImage = getSelectedImageDetail(viewID,
                    imageBeanArrayList.get(position), keepTime);
            isSuccess = dtsDataBase.insertOrUpdatePhotoDetail(selectedImage);
            if (isSuccess) {
                if (viewID == R.id.deleteimage) {
                    if (imageBeanArrayList != null && imageBeanArrayList.size() > 0) {
                        imageBeanArrayList.remove(position);
                        slidingImageAdapter.notifyDataSetChanged();
                    }
                    if (imageBeanArrayList.size() > 0) {
                        if (isRestoredImages) {
                            setStopWatch();
                        }
                    } else {
                        if (isRestoredImages) {
                            ;
                            super.finish();
                        } else if (isSavedImage) {
                            super.finish();
                        } else {
                            if (isTaskRoot()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    super.finishAndRemoveTask();
                                } else {
                                    super.finish();
                                }
                            } else {
                                exitApplicationAndRemoveFromRecent(ImageViewerActivity.this);
                            }
                        }
                    }
                } else if (viewID == R.id.saveimage24) { // Need to remove code
                    if (isRestoredImages) {
                        if (imageBeanArrayList != null && imageBeanArrayList.size() > 0) {
                            imageBeanArrayList.remove(position);
                            slidingImageAdapter.notifyDataSetChanged();
                        }
                        if (imageBeanArrayList.size() > 0) {
                            if (isRestoredImages) {
                                setStopWatch();
                            }
                        } else {
                            if (isRestoredImages) {
                                super.finish();
                            } else if (isSavedImage) {
                                super.finish();
                            } else {
                                if (isTaskRoot()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        super.finishAndRemoveTask();
                                    } else {
                                        super.finish();
                                    }
                                } else {
                                    exitApplicationAndRemoveFromRecent(ImageViewerActivity.this);
                                }
                            }
                        }
                    } else {
                        return;
                    }
                } else if (viewID == R.id.saveimage) {
                    if (isSavedImage) {
                        imageBeanArrayList.get(position).setNew(false);
                        if (imageBeanArrayList != null && imageBeanArrayList.size() > 0) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                            slidingImageAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (imageBeanArrayList != null && imageBeanArrayList.size() > 0) {
                            imageBeanArrayList.remove(position);
                            slidingImageAdapter.notifyDataSetChanged();
                        }
                        if (imageBeanArrayList.size() > 0) {
                            if (isRestoredImages) {
                                setStopWatch();
                            }
                        } else {
                            if (isRestoredImages) {
                                super.finish();
                            } else if (isSavedImage) {
                                super.finish();
                            } else {
                                if (isTaskRoot()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        super.finishAndRemoveTask();
                                    } else {
                                        super.finish();
                                    }
                                } else {
                                    exitApplicationAndRemoveFromRecent(ImageViewerActivity.this);
                                }
                            }
                        }
                    }
                }
                setIndicatorText();
            }
        }
    }
    /*Use to exit from application*/
    public void exitApplicationAndRemoveFromRecent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(FROMIMAGEGALLERY, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
        super.onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*Use to get selected image detail for DB operations*/
    private PhotoDetailBean getSelectedImageDetail(int id, ImageBean imageBean, long keepTime) {
        PhotoDetailBean photoDetailBean = new PhotoDetailBean();
        photoDetailBean.setActionTime(Calendar.getInstance().getTimeInMillis());
        photoDetailBean.setPhotoPath(imageBean.getImagePath());
        long dateTimeinMillisec = new File(imageBean.getImagePath()).lastModified();
        photoDetailBean.setTakenTime(dateTimeinMillisec);
        photoDetailBean.setPhotoOriginalPath(imageBean.getFileOriginalPath());
        photoDetailBean.setPhotoLocalPath(imageBean.getFileLocalPath());
        if (id == R.id.saveimage) {
            photoDetailBean.setSaved(1);
            photoDetailBean.setSavedFor24Hours(0);
            photoDetailBean.setDeleted(0);
        } else if (id == R.id.saveimage24) {
            photoDetailBean.setSaved(0);
            photoDetailBean.setSavedFor24Hours(1);
            photoDetailBean.setDeleted(0);
            photoDetailBean.setKeepTime(keepTime);
        } else if (id == R.id.deleteimage) {
            photoDetailBean.setSaved(0);
            photoDetailBean.setSavedFor24Hours(0);
            photoDetailBean.setDeleted(1);
        } else if (id == R.id.ll_restore) {
            photoDetailBean.setSaved(0);
            photoDetailBean.setSavedFor24Hours(0);
            photoDetailBean.setDeleted(0);
        }
        return photoDetailBean;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        try {
            if (isRestoredImages) {
                unregisterReceiver(deletedImagebroadcastReceiver);
            }
        } catch (Exception e) {
        }
        slidingImageAdapter = null;
        iv_back = null;
        iv_left = null;
        iv_right = null;
        saveImageBtn = null;
        save24Btn = null;
        shareBtn = null;
        deleteBtn = null;
        dtsDataBase = null;
        imageBeanArrayList = null;
        viewPager = null;
    }
    /* Use to start and show stop watch on restore media file*/
    private void startStopWatch() {
        try {
            final String deleteTimeInterval = defaultDeleteTimeInterval;
            long actionTime = imageBeanArrayList.get(position).getActionTime();
            long timerTime;
            if (imageBeanArrayList.get(position).isSaved24()) {
                timerTime = imageBeanArrayList.
                        get(position).getKeepTime() - (Calendar.getInstance().getTimeInMillis() - actionTime);
            } else {
                timerTime = (actionTime + Long.parseLong(deleteTimeInterval) * 500)
                        - Calendar.getInstance().getTimeInMillis();
            }
            if (timerTime < 0) {
                timerTime = 3000;
            }
            countDownTimer = new CountDownTimer(timerTime, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = (int) (millisUntilFinished / 1000);
                    int hours = seconds / (60 * 60);
                    int tempMint = (seconds - (hours * 60 * 60));
                    int minutes = tempMint / 60;
                    seconds = tempMint - (minutes * 60);
                    int days = hours / 24;
                    if (days < 1) {
                        stopWatch.setText(String.format("%02d", hours)
                                + ":" + String.format("%02d", minutes)
                                + ":" + String.format("%02d", seconds));
                        Log.d(TAG, "onTick: " +String.format("%02d", hours) +minutes);
                    } else {
                        int temphours = (hours - (days * 24));
                        String dayStr;
                        String hoursStr;
                        if (days > 1) {
                            dayStr = "Days";
                        } else {
                            dayStr = "Day";
                        }
                        if (hours > 1) {
                            hoursStr = "Hours";
                        } else {
                            hoursStr = "Hour";
                        }
                        Log.d(TAG, "onTick: " +String.format("%02d", days) +dayStr);

                        stopWatch.setText(String.format("%02d", days) + " " + dayStr
                                + ":" + String.format("%02d", temphours) + " " + hoursStr);
                    }
                }
                public void onFinish() {
                    deleteImages(deleteTimeInterval);
                }
            }.start();
        } catch (Exception e) {
        }
    }
    /*Handle delete media file operation from stopwatch*/
    private void deleteImages(String deleteTimeInterval) {
        ArrayList<String> imageList = dtsDataBase.getDeletedSave24File(
                Calendar.getInstance().getTimeInMillis()
                        - Long.parseLong(deleteTimeInterval) * 1000, false);
        if (imageList != null && imageList.size() > 0) {
            for (String imgPath : imageList) {
                File fdelete = new File(imgPath);
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        dtsDataBase.deletedImage(imgPath);
                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scannerIntent.setData(Uri.fromFile(fdelete));
                        ImageViewerActivity.this.sendBroadcast(scannerIntent);
                        System.out.println("file Deleted :" + imgPath);
                    } else {
                        System.out.println("file not Deleted :" + imgPath);
                    }
                }
            }
            ImageViewerActivity.this.sendBroadcast(new Intent(deletedImageBroadcast));
        }
        ArrayList<PhotoDetailBean> keepImageList = dtsDataBase.getKeepFile();
        if (keepImageList != null && keepImageList.size() > 0) {
            for (PhotoDetailBean photoDetailBean : keepImageList) {
                long actionTime = photoDetailBean.getActionTime();
                long keepTime = photoDetailBean.getKeepTime();
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime - actionTime > keepTime) {
                    File fdelete = new File(photoDetailBean.getPhotoPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            dtsDataBase.deletedImage(photoDetailBean.getPhotoPath());
                            Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            scannerIntent.setData(Uri.fromFile(fdelete));
                            ImageViewerActivity.this.sendBroadcast(scannerIntent);
                            System.out.println("file Deleted :" + photoDetailBean.getPhotoPath());
                        } else {
                            System.out.println("file not Deleted :" + photoDetailBean.getPhotoPath());
                        }
                    }
                }
                ImageViewerActivity.this.sendBroadcast(new Intent(deletedImageBroadcast));
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        crudButtonAction(R.id.saveimage24, KEEP_TO_LIST_ITEMS_TIME[position]);
        keepToBottomSheet.dismiss();
    }
    /*Use to remove notification and widget if exists at the time of this screen open*/
    private void removeNotification() {
        try {
            Date currentTime = Calendar.getInstance().getTime();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putLong(lastViewTime, currentTime.getTime());
            editor.commit();
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(newImagenotificationRequestCode);
            Scheduler.count = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(ImageViewerActivity.this)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(ImageViewerActivity.this, DtsWidget.class));
                    } else {
                        startService(new Intent(ImageViewerActivity.this, DtsWidget.class));
                    }
                }
            } else {
                startService(new Intent(ImageViewerActivity.this, DtsWidget.class));
            }
        } catch (Exception e) {
            Log.e("Exception", e.fillInStackTrace().toString());
        }
    }
    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
    }
    /*Use to show and hide bottom and navigation bar to show image in full screen mode*/
    public void slideToTop() {
        TranslateAnimation animateBottom;
        TranslateAnimation animateTop;
        showandHideBottomBar();
        if (isShowBottomView) {
            isShowBottomView = false;
            animateBottom = new TranslateAnimation(0, 0, 0, 0);
            animateBottom.setDuration(500);
            animateBottom.setFillAfter(true);
            ll_save_actions.startAnimation(animateBottom);
            appbarlayout.startAnimation(animateBottom);
            save24Btn.setVisibility(View.VISIBLE);
            shareBtn.setVisibility(View.VISIBLE);
            if (!isRestoredImages) {
                deleteBtn.setVisibility(View.VISIBLE);
            } else {
                try {
                    if (imageBeanArrayList.get(position).isSaved24()) {
                        deleteBtn.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                }
            }
            if (isRestoredImages) {
                ll_restore.setVisibility(View.VISIBLE);
                saveImageBtn.setVisibility(View.GONE);
            } else {
                ll_restore.setVisibility(View.GONE);
                saveImageBtn.setVisibility(View.VISIBLE);
            }
            ll_save_actions.setVisibility(View.VISIBLE);
            appbarlayout.setVisibility(View.VISIBLE);
            tv_image_indication_text.setVisibility(View.VISIBLE);
            mainlayout.setBackgroundColor(Color.WHITE);
            if (imageBeanArrayList.size() > 0 && imageBeanArrayList.get(position).isNew()) {
                iv_new.setVisibility(View.VISIBLE);
            }
            runTimer(true);
        } else {
            isShowBottomView = true;
            animateBottom = new TranslateAnimation(0, 0, 0, ll_save_actions.getHeight());
            animateBottom.setDuration(500);
            animateBottom.setFillAfter(true);
            ll_save_actions.startAnimation(animateBottom);
            animateTop = new TranslateAnimation(0, 0, 0, -appbarlayout.getHeight());
            animateTop.setDuration(500);
            animateTop.setFillAfter(true);
            appbarlayout.startAnimation(animateTop);
            ll_save_actions = findViewById(R.id.ll_save_actions);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    tv_image_indication_text.setVisibility(View.GONE);
                    ll_save_actions.setVisibility(View.GONE);
                    save24Btn.setVisibility(View.GONE);
                    shareBtn.setVisibility(View.GONE);
                    appbarlayout.setVisibility(View.GONE);
                    if (isRestoredImages) {
                        ll_restore.setVisibility(View.GONE);
                    } else {
                        saveImageBtn.setVisibility(View.GONE);
                    }
                    deleteBtn.setVisibility(View.GONE);
                    mainlayout.setBackgroundColor(Color.BLACK);
                    iv_new.setVisibility(View.GONE);
                    if (mTipView != null) {
                        mTipView.setVisibility(View.GONE);
                        mTipView.remove();
                        mTipView = null;
                    }
                    runTimer(false);
                }
            }, 250);
        }
    }
    private void runTimer(boolean isStart) {
        if (isStart) {
            if (timer != null) {
                timer.cancel();
            }
            if (mTipView == null) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ImageViewerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isOnPauseTrue) {
                                    isShowBottomView = false;
                                    slideToTop();
                                }
                            }
                        });
                    }
                }, 6000);
            }
        } else {
            if (timer != null) {
                timer.cancel();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        isOnPauseTrue = true;
    }
    @Override
    public void zoomImageClick() {
        showandHideBottomBar();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                slideToTop();
            }
        }, 100);
    }
    /*Use code to show and hide Android navigation native bottom bar*/
    private void showandHideBottomBar() {
        if (isShowBottomView) {
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_VISIBLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            try {
                getWindow().getDecorView().setSystemUiVisibility(flags);
                getWindow().getDecorView()
                        .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                                    getWindow().getDecorView().setSystemUiVisibility(flags);
                                    int size = getSoftButtonsBarSizePort(ImageViewerActivity.this);
                                    int mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
                                    int mScreenHeight = getWindowManager().getDefaultDisplay().getHeight() + size;
                                    viewpagerlayout.setLayoutParams(new FrameLayout.LayoutParams(mScreenWidth, mScreenHeight));
                                }
                            }
                        });
            } catch (Exception e) {
            }
        } else {
            final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            try {
                getWindow().getDecorView().setSystemUiVisibility(flags);
                getWindow().getDecorView()
                        .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                                    getWindow().getDecorView().setSystemUiVisibility(flags);
                                    int size = getSoftButtonsBarSizePort(ImageViewerActivity.this);
                                    int mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
                                    int mScreenHeight = getWindowManager().getDefaultDisplay().getHeight() + size;
                                    viewpagerlayout.setLayoutParams(new FrameLayout.LayoutParams(mScreenWidth, mScreenHeight));
                                }
                            }
                        });
            } catch (Exception e) {
            }
        }
    }
    /*Use to process save image*/
    class SaveAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            PhotoDetailBean selectedImage =
                    getSelectedImageDetail(R.id.saveimage, imageBeanArrayList.get(position), 0);
            boolean status = dtsDataBase.insertOrUpdatePhotoRestoreDetail(selectedImage);
            return status;
        }
        @Override
        protected void onPreExecute() {
            progress_rl.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(Boolean status) {
            progress_rl.setVisibility(View.GONE);
            if (status) {
                imageBeanArrayList.remove(position);
                if (imageBeanArrayList.size() > 0) {
                    setStopWatch();
                    if (imageBeanArrayList != null) {
                        slidingImageAdapter.notifyDataSetChanged();
                    }
                } else {
                    ImageViewerActivity.super.finish();
                }
                setIndicatorText();
            }
        }
    }
    /*Use to process restore image*/
    class RestoreAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                PhotoDetailBean selectedImage =
                        getSelectedImageDetail(R.id.ll_restore, imageBeanArrayList.get(position), 0);
                boolean status = dtsDataBase.insertOrUpdatePhotoRestoreDetail(selectedImage);
                return status;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPreExecute() {
            progress_rl.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(Boolean status) {
            progress_rl.setVisibility(View.GONE);
            if (status) {
                imageBeanArrayList.remove(position);
                if (imageBeanArrayList.size() > 0) {
                    setStopWatch();
                    if (imageBeanArrayList != null) {
                        slidingImageAdapter.notifyDataSetChanged();
                    }
                } else {
                    ImageViewerActivity.super.finish();
                }
                setIndicatorText();
                if (imageBeanArrayList.size() > 0 && imageBeanArrayList.get(position).isSaved24()) {
                    deleteBtn.setVisibility(View.VISIBLE);
                }
            }
            if (mTipView != null) {
                mTipView.setVisibility(View.GONE);
                mTipView.remove();
                mTipView = null;
                findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }
            runTimer(true);
        }
    }
    /*Use to process Keep to image*/
    class KeepImageAsyncTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... longs) {
            isKeepToProcessing = true;
            long keepToTime = longs[0];
            crudButtonAction(R.id.saveimage24, keepToTime);
            return true;
        }
        @Override
        protected void onPreExecute() {
            progress_rl.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(Boolean status) {
            progress_rl.setVisibility(View.GONE);
            isKeepToProcessing = false;
            imageBeanArrayList.remove(position);
            if (imageBeanArrayList.size() > 0) {
                if (imageBeanArrayList != null) {
                    slidingImageAdapter.notifyDataSetChanged();
                }
            } else {
                if (isSavedImage) {
                    ImageViewerActivity.super.finish();
                } else {
                    if (isTaskRoot()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ImageViewerActivity.super.finishAndRemoveTask();
                        } else {
                            ImageViewerActivity.super.finish();
                        }
                    } else {
                        exitApplicationAndRemoveFromRecent(ImageViewerActivity.this);
                    }
                }
            }
            if (mTipView != null) {
                mTipView.remove();
                mTipView = null;
                findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }
            runTimer(true);
            setIndicatorText();
        }
    }






}