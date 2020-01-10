package com.mobile.dts.utills;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.mobile.dts.R;
import com.mobile.dts.database.SqlLiteHelper;
import com.mobile.dts.model.DateTimeBean;
import com.mobile.dts.model.ImageBean;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import static android.text.TextUtils.isEmpty;
import static com.mobile.dts.activity.MainApplication.KEEPTODIRECTORYPATH;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURI;
import static com.mobile.dts.utills.Constants.appPref;
import static com.mobile.dts.utills.Constants.lastViewTime;


public class Utils {
    private ArrayList<ImageBean> imageArrayList = new ArrayList<ImageBean>();
    private SharedPreferences sharedpreferences;
    private long firstLaunchtime;
    private SqlLiteHelper dtsDataBase;

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int noOfColumns = (int) Math.ceil(displayMetrics.widthPixels / context.getResources().getDimension(R.dimen.standered_image_size));
        return noOfColumns;
    }

    public static int getImageDimenByColumns(Context context, int columns) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / columns);
    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean moveFile(Context context, String inputPath, String inputFile,
                                   String outputPath, long lastmodified) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            if (inputPath.contains(path) && outputPath.contains(path)) {
                //create output directory if it doesn't exist
                File dir = new File(outputPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                    Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    scannerIntent.setData(Uri.fromFile(dir));
                    context.sendBroadcast(scannerIntent);
                }
                File from = new File(inputPath + inputFile);
                File to = new File(outputPath + inputFile);
                from.renameTo(to);
                if (true) {
                    Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    scannerIntent.setData(Uri.fromFile(from));
                    context.sendBroadcast(scannerIntent);
                    if (outputPath != KEEPTODIRECTORYPATH) {
                        Intent scannerIntentout = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scannerIntentout.setData(Uri.fromFile(to));
                        context.sendBroadcast(scannerIntentout);
                    }
                }

            } else {
                SharedPreferences sharedpreferences = context.getApplicationContext().getSharedPreferences(Constants.appPref, Activity.MODE_PRIVATE);
                File from = new File(inputPath + inputFile);
                File to = new File(outputPath);
                if (to != null) {
                    DocumentFileUtils.createDirectory(context, to);
                }
                DocumentFile fromD = DocumentFileUtils.seekOrCreateTreeDocumentFile(context,
                        from,
                        null,
                        false);
                DocumentFile toD = DocumentFileUtils.seekOrCreateTreeDocumentFile(context,
                        to,
                        null,
                        false);
                boolean status = DocumentFileUtils.moveDocument(context, fromD, toD);
                if (status) {
                    if (fromD.delete()) {
                        deleteViaContentProvider(context, fromD.getUri());
                        Intent scannerIntentout = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scannerIntentout.setData(fromD.getUri());
                        context.sendBroadcast(scannerIntentout);
                        Intent scannerIntentoutafter = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scannerIntentoutafter.setData(Uri.fromFile(from));
                        context.sendBroadcast(scannerIntentoutafter);
                        Date currentTime = Calendar.getInstance().getTime();
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putLong(lastViewTime, currentTime.getTime());
                        editor.commit();
                    }

                }


            }
            return true;

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        return false;

    }

    public static void viewUri(Context context, @NonNull String uri, @Nullable String fallbackUrl) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        try {
            i.setData(Uri.parse(uri));
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            if (isEmpty(fallbackUrl)) {
                //Logger.log(e);
                Toast.makeText(context, "Application_not_available", Toast.LENGTH_SHORT).show();
            } else {
                viewUri(context, fallbackUrl, null);
            }
        }
    }

    public static boolean deleteViaContentProvider(Context context, Uri _uri) {
        Uri uri = _uri;
        if (uri == null) {
            return false;
        }
        try {
            ContentResolver resolver = context.getContentResolver();
            // change type to image, otherwise nothing will be deleted
            ContentValues contentValues = new ContentValues();
            int media_type = 1;
            contentValues.put("media_type", media_type);
            resolver.update(uri, contentValues, null, null);
            return resolver.delete(uri, null, null) > 0;
        } catch (Throwable e) {
            return false;
        }
    }

    public static void setLanguageFromLocale(Context context) {
        String languageToLoad = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.setLanguage), "en");
        Locale myLocale = new Locale(languageToLoad);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());


    }

    /*Used to get Month name string using date to show on Dts gallery*/
    public static String getMonthName(String date) {
        try {
            String dateNumber = date.substring(0, 2);
            String month = date.substring(3, 5);
            String year = date.substring(6, 10);
            boolean isShowYear = false;
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
            sdfDate.setTimeZone(tz);
            String localTimeDateToday = sdfDate.format(new Date(System.currentTimeMillis()).getTime());
            String localdate = localTimeDateToday.substring(0, 2);
            String localmonth = localTimeDateToday.substring(3, 5);
            String localyear = localTimeDateToday.substring(6, 10);
            String localTimeDateYesterday =
                    sdfDate.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000).getTime());
            String localdateYesterday = localTimeDateYesterday.substring(0, 2);
            String localmonthYesterday = localTimeDateYesterday.substring(3, 5);
            String localyearYesterday = localTimeDateYesterday.substring(6, 10);
            if (localyear.equals(year) && localmonth.equals(month) && localdate.equals(dateNumber)) {
                return "Today";

            } else if (localyearYesterday.equals(year)
                    && localmonthYesterday.equals(month) && localdateYesterday.equals(dateNumber)) {
                return "Yesterday";
            }
            if (Integer.parseInt(localyear) > Integer.parseInt(year)) {
                isShowYear = true;
            }
            switch (month) {
                case "01":
                    month = "Jan";
                    break;
                case "02":
                    month = "Feb";
                    break;
                case "03":
                    month = "Mar";
                    break;
                case "04":
                    month = "Apr";
                    break;
                case "05":
                    month = "May";
                    break;
                case "06":
                    month = "Jun";
                    break;
                case "07":
                    month = "Jul";
                    break;
                case "08":
                    month = "Aug";
                    break;
                case "09":
                    month = "Sep";
                    break;
                case "10":
                    month = "Oct";
                    break;
                case "11":
                    month = "Nov";
                    break;
                case "12":
                    month = "Dec";
                    break;
                default:
            }
            if (dateNumber.substring(0, 1).equals("0")) {
                dateNumber = dateNumber.replace("0", "");

            }
            if (isShowYear) {
                return dateNumber + " " + month + ", " + year;


            } else {
                return dateNumber + " " + month;

            }

        } catch (Exception e) {
        }
        return null;
    }

    public static DateTimeBean getDateTime(long timeStamp) {
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

    public ArrayList<ImageBean> fetchFolderList(Context context) {
        sharedpreferences = context.getSharedPreferences(appPref, Activity.MODE_PRIVATE);
        firstLaunchtime = sharedpreferences.getLong(Constants.firstLaunchtime, 0l);
        imageArrayList.clear();
        boolean isSDcardGranted = false;
        String sdcardUri = sharedpreferences.getString(SDCARDROOTDIRECTORYURI, null);
        if (sdcardUri != null &&
                DocumentFile.fromTreeUri(context, Uri.parse(sdcardUri)).canWrite()) {
            isSDcardGranted = true;

        }
        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data;
        int column_size;
        int column_takendateImage;
        int column_takendateVideo;
        int column_mimeType;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String absolutePathOfImage = null;
        uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " and "
                + MediaStore.Images.Media.DATE_TAKEN + "> " + firstLaunchtime
                //  + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + " and "
                + MediaStore.Video.Media.DATE_TAKEN + "> " + firstLaunchtime
                //  + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                ;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATE_TAKEN,
                (MediaStore.Video.Thumbnails.DATA)
        };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri, projection, selection, null, orderBy + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_size = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE);
        column_takendateImage = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
        column_takendateVideo = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN);
        column_mimeType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
        while (cursor.moveToNext()) {
            ImageBean imageBean = new ImageBean();
            imageBean.setImagePath(cursor.getString(column_index_data));
            if (cursor.getInt(column_size) / 1024 != 0) {
                imageBean.setImageSize(cursor.getInt(column_size) / 1024);

            } else {
                imageBean.setImageSize(Integer.parseInt(String.valueOf(new File(cursor.getString(column_index_data)).length() / 1024)));
            }
            if (cursor.getString(column_mimeType).contains("video")) {
                imageBean.setCreatedTimeStamp(cursor.getLong(column_takendateVideo));

            } else {
                imageBean.setCreatedTimeStamp(cursor.getLong(column_takendateImage));

            }
            File file = new File(imageBean.getImagePath());
            if (isSDcardGranted) {
                if (file.exists()) {
                    imageArrayList.add(imageBean);
                }
            } else {
                if (file.exists() && imageBean.getImagePath().contains(path)) {
                    imageArrayList.add(imageBean);

                }
            }

        }
        ArrayList<ImageBean> telegramImages = getImagesTelegram();
        if (telegramImages != null && telegramImages.size() > 0) {
            imageArrayList.addAll(telegramImages);
        }
        ArrayList<ImageBean> whatsAppImages = getImagesWhatsApp();
        if (whatsAppImages != null && whatsAppImages.size() > 0) {
            imageArrayList.addAll(whatsAppImages);
        }
        ArrayList<ImageBean> telegramVideo = getVideoTelegram();
        if (telegramVideo != null && telegramVideo.size() > 0) {
            imageArrayList.addAll(telegramVideo);
        }
        ArrayList<ImageBean> whatsAppVideo = getVideoWhatsApp();
        if (whatsAppVideo != null && whatsAppVideo.size() > 0) {
            imageArrayList.addAll(whatsAppVideo);
        }
        Collections.sort(imageArrayList, new DtsComparator());
        Set set = new TreeSet(new Comparator<ImageBean>() {
            @Override
            public int compare(ImageBean o1, ImageBean o2) {
                if (o1.getImagePath().equalsIgnoreCase(o2.getImagePath())) {
                    return 0;
                }
                return 1;
            }


        });
        set.addAll(imageArrayList);
        ArrayList finalResult = new ArrayList(set);
        return finalResult;

    }

    public ArrayList<ImageBean> getNewImageList(Context context) {
        dtsDataBase = new SqlLiteHelper(context);
        ArrayList<ImageBean> sinceInstallList = fetchFolderList(context);
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
                if (savedImageList.contains(imagePath.getImagePath())) {
                    //    _imageBean.setNew(false);
                } else {
                    dtsList.add(imagePath);
                }


            }


        }
        return dtsList;
    }

    public ArrayList<ImageBean> getImagesTelegram() {
        ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/Telegram/Telegram Images/";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (firstLaunchtime < files[i].lastModified()) {
                        if (isImageFile(path + files[i].getName())) {
                            ImageBean imageBean = new ImageBean();
                            imageBean.setImagePath(files[i].getAbsolutePath());
                            imageBean.setImageSize((int) (files[i].length() / 1024));
                            imageBean.setCreatedTimeStamp(files[i].lastModified());
                            imageBeans.add(imageBean);

                        } else {
                        }
                    }
                }
            }
            return imageBeans;

        } catch (Exception e) {
        }
        return imageBeans;
    }

    private ArrayList<ImageBean> getImagesWhatsApp() {
        ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/WhatsApp Images/Private";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (firstLaunchtime < files[i].lastModified()) {
                        if (isImageFile(path + files[i].getName())) {
                            ImageBean imageBean = new ImageBean();
                            imageBean.setImagePath(files[i].getAbsolutePath());
                            imageBean.setImageSize((int) (files[i].length() / 1024));
                            imageBean.setCreatedTimeStamp(files[i].lastModified());
                            imageBeans.add(imageBean);

                        } else {
                        }
                    }
                }
            }
            return imageBeans;

        } catch (Exception e) {
        }
        return imageBeans;
    }

    public ArrayList<ImageBean> getVideoTelegram() {
        ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/Telegram/Telegram Video/";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (firstLaunchtime < files[i].lastModified()) {
                        if (isVideoFile(path + files[i].getName())) {
                            ImageBean imageBean = new ImageBean();
                            imageBean.setImagePath(files[i].getAbsolutePath());
                            imageBean.setImageSize((int) (files[i].length() / 1024));
                            imageBean.setCreatedTimeStamp(files[i].lastModified());
                            imageBeans.add(imageBean);

                        } else {
                        }
                    }
                }
            }
            return imageBeans;

        } catch (Exception e) {
        }
        return imageBeans;
    }

    private ArrayList<ImageBean> getVideoWhatsApp() {
        ArrayList<ImageBean> imageBeans = new ArrayList<ImageBean>();
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/WhatsApp/Media/WhatsApp Video/Private";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (firstLaunchtime < files[i].lastModified()) {
                        if (isVideoFile(path + files[i].getName())) {
                            ImageBean imageBean = new ImageBean();
                            imageBean.setImagePath(files[i].getAbsolutePath());
                            imageBean.setImageSize((int) (files[i].length() / 1024));
                            imageBean.setCreatedTimeStamp(files[i].lastModified());
                            imageBeans.add(imageBean);

                        } else {
                        }
                    }
                }
            }
            return imageBeans;

        } catch (Exception e) {
        }
        return imageBeans;
    }

    class DtsComparator implements Comparator<ImageBean> {
        @Override
        public int compare(ImageBean t1, ImageBean t2) {
            return Long.compare(t2.getCreatedTimeStamp(), t1.getCreatedTimeStamp());
        }
    }

}
