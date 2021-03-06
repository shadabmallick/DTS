package com.mobile.dts.helper;

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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.activity.ImageViewLoadingActivity;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.model.ImageBean;
import com.mobile.dts.model.PhotoDetailBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.DocumentFileUtils;
import com.mobile.dts.utills.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.mobile.dts.utills.Constants.alarmRequestCode;
import static com.mobile.dts.utills.Constants.defaultDeleteTimeInterval;
import static com.mobile.dts.utills.Constants.defaultTimeset;
import static com.mobile.dts.utills.Constants.deletedImageBroadcast;
import static com.mobile.dts.utills.Constants.everyThirdDaytime;
import static com.mobile.dts.utills.Constants.isBoot;
import static com.mobile.dts.utills.Constants.isRealTimeNotification;
import static com.mobile.dts.utills.Constants.newImagenotificationRequestCode;
import static com.mobile.dts.utills.Constants.saved_time;

/*This is the scheduler class. It shows widget and
notification with new images. It checks every 30 seconds for new images*/
public class Scheduler extends BroadcastReceiver {

    public static int count = 0;
    public static ArrayList<ImageBean> newImages;
    private SharedPreferences sharedpreferences, settingsPref;
    private SqlLiteHelper dtsDataBase;
    private int newCount;

    @Override
    public void onReceive(final Context context, Intent intent) {
        settingsPref = PreferenceManager.getDefaultSharedPreferences(context);
        String timeset = defaultTimeset;
        String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            sharedpreferences = context.getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
            boolean _isBoot = sharedpreferences.getBoolean(isBoot, false);
            if (_isBoot) {
                return;
            }
            boolean notification = false;
            boolean isDisbledNotification = sharedpreferences.getBoolean(Constants.isDisbledNotification, false);
            if (!isDisbledNotification) {
                boolean isRealtimeShowNotification = sharedpreferences.getBoolean(isRealTimeNotification, false);
                if (isRealtimeShowNotification) {
                    notification = true;

                } else {

                    // selected time
                    String selected_time = sharedpreferences.getString(saved_time, "");
                    System.out.println("selected_time :: " + selected_time);
                    if (!selected_time.isEmpty()){
                        notification = true;
                    }


                    if (!sharedpreferences.contains(everyThirdDaytime)) {
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date today = new Date();
                        Date todayWithZeroTime = formatter.parse(formatter.format(today));
                        long timeaveryThirdDay = todayWithZeroTime.getTime() + 2 * (24 * 60 * 60 * 1000);
                        SharedPreferences.Editor editor3 = sharedpreferences.edit();
                        editor3.putLong(everyThirdDaytime, timeaveryThirdDay);
                        editor3.commit();
                    } else {
                        long everythirdDaytime = sharedpreferences.getLong(everyThirdDaytime, new Date().getTime());
                        if (everythirdDaytime < System.currentTimeMillis()) {
                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            Date today = new Date();
                            Date todayWithZeroTime = formatter.parse(formatter.format(today));
                            long timeaveryThirdDay = todayWithZeroTime.getTime() + 2 * (24 * 60 * 60 * 1000);
                            SharedPreferences.Editor editor3 = sharedpreferences.edit();
                            editor3.putLong(everyThirdDaytime, timeaveryThirdDay);
                            editor3.commit();
                            notification = true;
                        }
                    }
                }
            }
            Intent intentcontext = new Intent(context, Scheduler.class);
            intent.setAction(Constants.ACTION_ALARM_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intentcontext, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance()
                    .getTime().getTime()
                    + Long.parseLong(timeset.toString()) * 1000, pendingIntent);
            long lastModifiedTime = sharedpreferences.getLong(Constants.lastViewTime, 0l);





            /// new work ...
            String selected_time = sharedpreferences.getString(saved_time, "");
            Log.d("Keeptoo", "selected_time :: " + selected_time);
            if (!selected_time.isEmpty()){

                SimpleDateFormat sdf =
                        new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                try {
                    Date mDate = sdf.parse(selected_time);

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());

                    Log.d("Keeptoo", "selected_time :: " +mDate.getHours());
                    Log.d("Keeptoo", "selected_time :: " +mDate.getMinutes());

                    cal.set(Calendar.HOUR_OF_DAY, mDate.getHours());
                    cal.set(Calendar.MINUTE, mDate.getMinutes());


                   // long timeInMilliseconds = mDate.getTime();
                    AlarmManager am1 = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                    am1.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY, pendingIntent);

                    System.out.println("Date in milli :: " + cal.getTimeInMillis());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            /// new work finish...





            if ((ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            } else {
                ArrayList<ImageBean> galleryList = new Utils().fetchFolderList(context);
                ArrayList<ImageBean> newImagesCount = new ArrayList<ImageBean>();
                for (ImageBean imageBean : galleryList) { // Need to remove
                    long dateTimeinMillisec = imageBean.getCreatedTimeStamp();
                    if (lastModifiedTime < dateTimeinMillisec) {
                        newImagesCount.add(imageBean);
                    }
                }
                newCount = newImagesCount.size();
                if (newCount > 0) {
                    count = newCount;
                    newImages = newImagesCount;
                }
                String deleteTimeInterval = defaultDeleteTimeInterval;
                dtsDataBase = new SqlLiteHelper(context);
                ArrayList<String> imageList = dtsDataBase.getDeletedSave24File(
                        Calendar.getInstance().getTimeInMillis() - Long.parseLong(deleteTimeInterval) * 1000, false);
                if (imageList != null && imageList.size() > 0) {
                    for (String imgPath : imageList) {
                        File fdelete = new File(imgPath);
                        if (fdelete.exists()) {
                            if (imgPath.contains(directoryPath)) {
                                if (fdelete.delete()) {
                                    dtsDataBase.deletedImage(imgPath);
                                    Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    scannerIntent.setData(Uri.fromFile(fdelete));
                                    context.sendBroadcast(scannerIntent);
                                } else {
                                    System.out.println("file not Deleted :" + imgPath);
                                }
                            } else {
                                DocumentFile targetDocument = DocumentFileUtils.seekOrCreateTreeDocumentFile(context,
                                        fdelete,
                                        null,
                                        false);
                                if (targetDocument != null) {
                                    if (targetDocument.delete()) {
                                        dtsDataBase.deletedImage(imgPath);
                                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        scannerIntent.setData(Uri.fromFile(fdelete));
                                        context.sendBroadcast(scannerIntent);
                                    }
                                }
                            }
                        } else {
                            dtsDataBase.deletedImage(imgPath);
                        }
                    }
                    context.sendBroadcast(new Intent(deletedImageBroadcast));
                }
                // Get keep file to delete
                ArrayList<PhotoDetailBean> keepImageList = dtsDataBase.getKeepFile();
                if (keepImageList != null && keepImageList.size() > 0) {
                    for (PhotoDetailBean photoDetailBean : keepImageList) {
                        long actionTime = photoDetailBean.getActionTime();
                        long keepTime = photoDetailBean.getKeepTime();
                        long currentTime = Calendar.getInstance().getTimeInMillis();
                        if (currentTime - actionTime > keepTime) {
                            File fdelete = new File(photoDetailBean.getPhotoPath());
                            if (fdelete.exists()) {
                                if (photoDetailBean.getPhotoPath().contains(directoryPath)) {
                                    if (fdelete.delete()) {
                                        dtsDataBase.deletedImage(photoDetailBean.getPhotoPath());
                                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                        scannerIntent.setData(Uri.fromFile(fdelete));
                                        context.sendBroadcast(scannerIntent);
                                    } else {
                                        System.out.println("file not Deleted :" + photoDetailBean.getPhotoPath());
                                    }

                                } else {
                                    DocumentFile targetDocument = DocumentFileUtils.seekOrCreateTreeDocumentFile(context,
                                            fdelete,
                                            null,
                                            false);
                                    if (targetDocument != null) {
                                        if (targetDocument.delete()) {
                                            dtsDataBase.deletedImage(photoDetailBean.getPhotoPath());
                                            Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                            scannerIntent.setData(Uri.fromFile(fdelete));
                                            context.sendBroadcast(scannerIntent);
                                        }
                                    }

                                }
                            } else {
                                dtsDataBase.deletedImage(photoDetailBean.getPhotoPath());
                            }
                            context.sendBroadcast(new Intent(deletedImageBroadcast));
                        }
                    }
                }

            }
            boolean overlay = false;
            String schedulingOption = settingsPref.getString("scheduling", "1003");
            System.out.println("schedulingOption :: " + schedulingOption);
            switch (schedulingOption) {
                case "1003":
                    if (newCount != 0) {
                        overlay = true;
                    }
                    break;
                case "1004":
                    if (newCount >= 5) {
                        overlay = true;
                    }
                    break;
                case "1005":
                    if (newCount >= 10) {
                        overlay = true;
                    }
                    break;
                default:
            }


            /// new work ...
            selected_time = sharedpreferences.getString(saved_time, "");
            Log.d("Keeptoo", "selected_time :: " + selected_time);
            if (!selected_time.isEmpty()){
                overlay = false;

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf =
                        new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                try {
                    //Date date1 = calendar.getTime();
                    //Date date2 = sdf.parse(selected_time);


                    Date date1 = calendar.getTime();
                    String date_s = sdf.format(date1);

                    //Date date2 = sdf.parse(selected_time);

                    Log.d("TAG s", "date_s = "+date_s);
                    Log.d("TAG s", "selected_time = "+selected_time);

                    if (selected_time.equals(date_s)){
                        overlay = true;
                    }else {

                        /*Date mDate = sdf.parse(selected_time);
                        long timeInMilliseconds = mDate.getTime();

                        Intent intentcontext1 = new Intent(context, Scheduler.class);
                        intent.setAction(Constants.ACTION_ALARM_RECEIVER);
                        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, alarmRequestCode, intentcontext1, 0);
                        AlarmManager am1 = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                        am1.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTime()
                                .getTime() + Long.parseLong("60") * 1000, pendingIntent1);*/
                    }

                   /* if (date1.equals(date2)){

                        overlay = true;

                    }else {

                        Intent intentcontext1 = new Intent(context, Scheduler.class);
                        intent.setAction(Constants.ACTION_ALARM_RECEIVER);
                        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, alarmRequestCode, intentcontext1, 0);
                        am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                        am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTime()
                                .getTime() + Long.parseLong(timeset.toString()) * 1000, pendingIntent1);

                    }*/

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else {
                overlay = true;
            }
            /// new work finish...



            if (newCount != 0) {
                if (notification) {
                    ArrayList<ImageBean> newImagesWithoutImage = new Utils().getNewImageList(context);
                    if (newImagesWithoutImage != null && newImagesWithoutImage.size() > 0) {
                        count = newImagesWithoutImage.size();
                        newImages = newImagesWithoutImage;
                    }
                    String channelID = "notify_001";
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context.getApplicationContext(), channelID);
                    Intent notificationIntent = new Intent(context.getApplicationContext(), ImageViewLoadingActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    notificationIntent.putExtra(Constants.KEY_IS_FROM_NOTIFICATION, true);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,
                            newImagenotificationRequestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    showNotification(mBuilder, contentIntent, context, "Memory optimizor", "New Images found. Do you want to manage memory?",
                            count, newImagenotificationRequestCode, channelID);

                }
                if (overlay) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(context)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(new Intent(context, DtsWidget.class));
                                } else {
                                    context.startService(new Intent(context, DtsWidget.class));
                                }
                            }
                        } else {
                            context.startService(new Intent(context, DtsWidget.class));
                        }

                    } catch (Exception e) {
                        Log.e("Exception", e.fillInStackTrace().toString());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Intent intentcontext = new Intent(context, Scheduler.class);
            intent.setAction(Constants.ACTION_ALARM_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmRequestCode, intentcontext, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTime()
                    .getTime() + Long.parseLong(timeset.toString()) * 1000, pendingIntent);
        }

    }

    /*use to show Notification with new Media files*/
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
                    "KeepToo",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);

        }
        mNotificationManager.notify(notificationRequestID, mBuilder.build());
    }
}

