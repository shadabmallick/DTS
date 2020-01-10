package com.mobile.dts.saveImageEvent;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.mobile.dts.R;
import com.mobile.dts.activity.ImageViewLoadingActivity;
import com.mobile.dts.helper.DtsWidget;
import com.mobile.dts.helper.Scheduler;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;

import java.util.ArrayList;
import java.util.Calendar;

import static com.mobile.dts.utills.Constants.alarmRequestCode;
import static com.mobile.dts.utills.Constants.defaultTimeset;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;
import static com.mobile.dts.utills.Constants.newImagenotificationRequestCode;

/*This class create to get new image event*/
public class ImageJobService extends JobService {
    private final static String TAG = ImageJobService.class.toString();
    private final static int CONTENT_URI_JOB_ID = 1000;
    private ArrayList<ImageBean> newImages;
    private SharedPreferences sharedpreferences, settingsPref;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void startJob(Context context, boolean isCancel) {
        try {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (!isCancel) {
                Log.i(TAG, "startJob");
                scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(
                        CONTENT_URI_JOB_ID,
                        new ComponentName(context, ImageJobService.class));
                builder.addTriggerContentUri(
                        new JobInfo.TriggerContentUri(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
                scheduler.schedule(builder.build());
            } else {
                scheduler.cancelAll();

            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        new AsyncTask<Void, Void, Void>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected Void doInBackground(Void... parameters) {
                doProcess(params);
                return null;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onPostExecute(Void result) {
                jobFinished(params, false);
                settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                // create a new job
                startJob(getApplicationContext(), false);
                //  }
            }
        }.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void doProcess(JobParameters params) {
        if (params.getTriggeredContentAuthorities() != null) {
            if (params.getTriggeredContentUris() != null) {
                for (final Uri uri : params.getTriggeredContentUris()) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    final Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            String realPath = "";
                            try {
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                if (cursor.moveToFirst()) {
                                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                    realPath = cursor.getString(columnIndex);
                                    Log.e(TAG, realPath);
                                } else {
                                    Log.e(TAG, "error" + realPath);
                                }
                                cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "error" + "exception");
                            }
                            if (realPath != null && !realPath.isEmpty()) {
                                sharedpreferences = getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
                                settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String setTime = defaultTimeset;
                                Intent intent = new Intent(getApplicationContext(), Scheduler.class);
                                intent.setAction(Constants.ACTION_ALARM_RECEIVER);
                                boolean isWorking = (PendingIntent.getBroadcast(getApplicationContext(),
                                        alarmRequestCode, intent, PendingIntent.FLAG_NO_CREATE) != null);
                                if (!isWorking) {
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmRequestCode, intent, 0);
                                    AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
                                    am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance()
                                            .getTime().getTime() + Long.parseLong(setTime) * 1000, pendingIntent);
                                }
                                long lastModifiedTime = sharedpreferences.getLong(Constants.lastViewTime, 0l);
                                if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                                        (ContextCompat.checkSelfPermission(getApplicationContext(),
                                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                                } else {
                                    ArrayList<ImageBean> galleryList = new Utils().fetchFolderList(getApplicationContext());
                                    newImages = new ArrayList<ImageBean>();
                                    for (ImageBean imageBean : galleryList) {
                                        long dateTimeinMillisec = imageBean.getCreatedTimeStamp();
                                        if (lastModifiedTime < dateTimeinMillisec) {
                                            newImages.add(imageBean);
                                        }
                                    }
                                    int count = newImages.size();
                                    if (count > 0) {
                                        boolean overlay = false;
                                        String schedulingOption = settingsPref.getString("scheduling", "1003");
                                        switch (schedulingOption) {
                                            case "1003":
                                                if (count != 0) {
                                                    overlay = true;
                                                }
                                                break;
                                            case "1004":
                                                if (count >= 5) {
                                                    overlay = true;
                                                }
                                                break;
                                            case "1005":
                                                if (count >= 10) {
                                                    overlay = true;
                                                }
                                                break;
                                            default:
                                        }
                                        Scheduler.count = count;
                                        Scheduler.newImages = newImages;
                                        if (overlay) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                startForegroundService(new Intent(getApplicationContext(), DtsWidget.class));
                                            } else {
                                                startService(new Intent(getApplicationContext(), DtsWidget.class));
                                            }
                                        }
                                        boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
                                        if (!isDisbledNotification) {
                                            boolean notification = sharedpreferences.getBoolean(isRealTimeNotification, false);
                                            if (notification) {
                                                ArrayList<ImageBean> newImagesWithoutImage = new Utils().getNewImageList(getApplicationContext());
                                                if (newImagesWithoutImage != null && newImagesWithoutImage.size() > 0) {
                                                    Scheduler.count = newImagesWithoutImage.size();
                                                    Scheduler.newImages = newImagesWithoutImage;

                                                }
                                                String channelID = "notify_001";
                                                NotificationCompat.Builder mBuilder =
                                                        new NotificationCompat.Builder(getApplicationContext(), channelID);
                                                Intent notificationIntent = new Intent(getApplicationContext(), ImageViewLoadingActivity.class);
                                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                notificationIntent.putExtra(Constants.KEY_IS_FROM_NOTIFICATION, true);
                                                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                                                        newImagenotificationRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                showNotification(mBuilder, contentIntent, getApplicationContext(), "Memory optimizor", "New Images found. Do you want to manage memory?",
                                                        Scheduler.count, newImagenotificationRequestCode, channelID);


                                            }

                                        }
                                    }

                                }


                            }

                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        } else {
            Log.i(TAG, "no content");
        }
    }

    private void showNotification(NotificationCompat.Builder mBuilder,
                                  PendingIntent contentIntent, Context context, String title,
                                  String summary, int count, int notificationRequestID, String channelID) {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle(title);
        bigText.setSummaryText(count + " " + summary);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSmallIcon(R.mipmap.logo_launcher);
        mBuilder.setContentText(count + " " + summary);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID,
                    "KeepTooApp",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);

        }
        mNotificationManager.notify(notificationRequestID, mBuilder.build());


    }
}
