package com.mobile.dts.saveImageEvent;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.mobile.dts.R;
import com.mobile.dts.activity.ImageViewLoadingActivity;
import com.mobile.dts.helper.DtsWidget;
import com.mobile.dts.helper.Scheduler;
import com.mobile.dts.model.DateTimeBean;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.mobile.dts.utills.Constants.alarmRequestCode;
import static com.mobile.dts.utills.Constants.defaultTimeset;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;
import static com.mobile.dts.utills.Constants.newImagenotificationRequestCode;

/*This class create to get Camera broadcast */
public class CameraBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = CameraBroadcastReceiver.class.toString();
    private ArrayList<ImageBean> newImages;
    private SharedPreferences sharedpreferences, settingsPref;

    public static void register(Context context) {
        Log.i(TAG, "register");
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.hardware.Camera.ACTION_NEW_PICTURE);
        filter.addAction("android.hardware.action.NEW_PICTURE");
        try {
            filter.addDataType("image/*");
        } catch (Exception e) {
            Log.e(TAG, "Fail: + " + e.getMessage());
        }
        CameraBroadcastReceiver receiver = new CameraBroadcastReceiver();
        context.registerReceiver(receiver, filter);
    }

    @Override
    public void onReceive(Context context, final Intent _intent) {
        sharedpreferences = context.getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(context);
        long lastModifiedTime = sharedpreferences.getLong(Constants.lastViewTime, 0l);
        if ((ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
        } else {
            ArrayList<ImageBean> galleryList = new Utils().fetchFolderList(context);
            newImages = new ArrayList<ImageBean>();
            for (ImageBean imagePath : galleryList) {
                long dateTimeinMillisec = imagePath.getCreatedTimeStamp();
                if (lastModifiedTime < dateTimeinMillisec) {
                    newImages.add(imagePath);
                }
            }
            int count = newImages.size();
            if (count > 0) {
                String setTime = defaultTimeset;
                Intent intent = new Intent(context, Scheduler.class);
                intent.setAction(Constants.ACTION_ALARM_RECEIVER);
                boolean isWorking = (PendingIntent.getBroadcast(context,
                        alarmRequestCode, intent, PendingIntent.FLAG_NO_CREATE) != null);
                if (!isWorking) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intent, 0);
                    AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                    am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance()
                            .getTime().getTime() + Long.parseLong(setTime) * 1000, pendingIntent);
                }
                Scheduler.count = count;
                Scheduler.newImages = newImages;
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
                if (overlay) {
                    context.startService(new Intent(context, DtsWidget.class));
                }
                boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
                if (!isDisbledNotification) {
                    boolean notification = sharedpreferences.getBoolean(isRealTimeNotification, false);
                    if (notification) {
                        ArrayList<ImageBean> newImagesWithoutImage = new Utils().getNewImageList(context);
                        if (newImagesWithoutImage != null && newImagesWithoutImage.size() > 0) {
                            Scheduler.count = newImagesWithoutImage.size();
                            Scheduler.newImages = newImagesWithoutImage;

                        }
                        String channelID = "notify_001";
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context.getApplicationContext(), channelID);
                        Intent notificationIntent = new Intent(context.getApplicationContext(), ImageViewLoadingActivity.class);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        notificationIntent.putExtra(Constants.KEY_IS_FROM_NOTIFICATION, true);
                        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(),
                                newImagenotificationRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        showNotification(mBuilder, contentIntent, context.getApplicationContext(), "Memory optimizor", "New Images found. Do you want to manage memory?",
                                Scheduler.count, newImagenotificationRequestCode, channelID);


                    }
                }

            }

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
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentText(count + " " + summary);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID,
                    "DtsApp",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);

        }
        mNotificationManager.notify(notificationRequestID, mBuilder.build());


    }

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
                if (dateTimeBean.getTime() != null) {
                    imageBean.setCreatedTime(dateTimeBean.getTime());
                }
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
}
