package com.mobile.dts.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.mobile.dts.callbacks.ImageMovedListener;
import com.mobile.dts.model.PhotoDetailBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.FileUtils;
import com.mobile.dts.utills.Utils;

import java.io.File;
import java.util.ArrayList;

import static com.mobile.dts.activity.MainApplication.KEEPTODIRECTORYPATH;

/*Use to store database data*/
public class SqlLiteHelper extends SQLiteOpenHelper {
    //db name
    public static final String DATABASE_NAME = "dts.db";
    //db table name
    public static final String TABLE_MEDIA_LIST = "tbl_media_list";
    //db @TABLE_MEDIA_LIST column name
    public static final String MDL_PHOTO_PATH = "photo_Path";
    public static final String MDL_ACTION_TIME = "action_time";
    public static final String MDL_TIME_TAKEN = "time_taken";
    public static final String MDL_IS_SAVED = "is_saved";
    public static final String MDL_IS_SAVED_24_HOURS = "is_saved_for_24_hrs";
    public static final String MDL_IS_DELETED = "is_deleted";
    public static final String MDL_KEEP_TIME = "keep_time";
    public static final String MDL_KEEPTO_URL = "keepto_url";
    //db version
    private static final int DATABASE_VERSION = 3;

    private static final String CREATE_TABLE_MEDIA_LIST_QUERY = "CREATE  TABLE " + TABLE_MEDIA_LIST + " ( " + MDL_PHOTO_PATH + " VARCHAR PRIMARY KEY, " +
            MDL_ACTION_TIME + " INTEGER, " + MDL_TIME_TAKEN + " INTEGER, " + MDL_IS_SAVED + " INTEGER DEFAULT 0, "
            + MDL_IS_SAVED_24_HOURS + " INTEGER DEFAULT 0, " + MDL_IS_DELETED + " INTEGER DEFAULT 0, " + MDL_KEEP_TIME + " INTEGER DEFAULT 0, " + MDL_KEEPTO_URL + " VARCHAR );";
    boolean notification;
    private SQLiteDatabase database;
    private SharedPreferences settingsPref;
    private Context context;
    private ImageMovedListener imageMovedListener;

    public SqlLiteHelper(Context _context) {
        super(_context, DATABASE_NAME, null, DATABASE_VERSION);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(_context);
        notification = true;
        context = _context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEDIA_LIST_QUERY);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 3) {
            db.execSQL("ALTER TABLE " + TABLE_MEDIA_LIST + " ADD COLUMN " + MDL_KEEPTO_URL + " VARCHAR");
        }

    }

    /*Use to insert or update media file info(Only single media file info update)*/
    public boolean insertOrUpdatePhotoDetail(PhotoDetailBean photoDetailBean) {
        database = this.getWritableDatabase();
        boolean status = false;
        String destinationDirectory = null;
        String existingFileName = null;
        if (photoDetailBean.isSavedFor24Hours() == 1 && notification) {
            existingFileName = photoDetailBean.getPhotoPath().substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
            String existingFileDirrectory = photoDetailBean.getPhotoPath().substring(0, photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
            if (existingFileDirrectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                destinationDirectory = KEEPTODIRECTORYPATH;
            } else {
                File directory = new File(FileUtils.getExternalStorageRoot(new File(photoDetailBean.getPhotoPath()), context) + "/.dir/");
                if (directory != null) {
                    destinationDirectory = directory.getAbsolutePath() + "/";
                }

            }
            status = Utils.moveFile(context, existingFileDirrectory, existingFileName, destinationDirectory, photoDetailBean.getTakenTime());

        }
        ContentValues contentValues = new ContentValues();
        if (photoDetailBean.isDeleted() == 1 && photoDetailBean.getPhotoPath().contains(KEEPTODIRECTORYPATH)
                && photoDetailBean.getPhotoOriginalPath() != null) {
            photoDetailBean.setPhotoPath(photoDetailBean.getPhotoOriginalPath());

        }
        if (!isImageExists(photoDetailBean.getPhotoPath()) && photoDetailBean.getPhotoPath() != null &&
                !photoDetailBean.getPhotoPath().contains(KEEPTODIRECTORYPATH)) {
            contentValues.put(MDL_PHOTO_PATH, photoDetailBean.getPhotoPath());
            contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
            contentValues.put(MDL_TIME_TAKEN, photoDetailBean.getTakenTime());
            contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
            contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
            contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
            contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
            if (photoDetailBean.isSavedFor24Hours() == 1 && notification
                    && status && destinationDirectory != null) {
                contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
            }
            long id = database.insert(TABLE_MEDIA_LIST, null, contentValues);
            contentValues.clear();
        } else {
            if (photoDetailBean.isSaved() == 1) {
                contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_KEEPTO_URL, "");
            } else if (photoDetailBean.isSavedFor24Hours() == 1) {
                contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                if (notification
                        && status && destinationDirectory != null) {
                    contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
                }
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 0);
                //contentValues.put(MDL_IS_SAVED,0);
            } else if (photoDetailBean.isDeleted() == 1) {
                contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                contentValues.put(MDL_IS_SAVED, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                //  contentValues.put(MDL_KEEPTO_URL, "");
            } else if (photoDetailBean.isSaved() == 0 && photoDetailBean.isSavedFor24Hours() == 0
                    && photoDetailBean.isDeleted() == 0) {
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 1);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_KEEPTO_URL, "");
            }
            database.update(TABLE_MEDIA_LIST, contentValues,
                    MDL_PHOTO_PATH + " =" + "'" + photoDetailBean.getPhotoPath() + "'", null);
            contentValues.clear();
        }
        return true;

    }

    /*Use to insert or update media files info*/
    public boolean insertOrUpdatePhotoDetails(ArrayList<PhotoDetailBean> photoDetailBeanArrayList) {
        try {
            imageMovedListener = (ImageMovedListener) context;
        } catch (Exception e) {
        }
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < photoDetailBeanArrayList.size(); i++) {
            PhotoDetailBean photoDetailBean = photoDetailBeanArrayList.get(i);
            boolean status = false;
            String destinationDirectory = null;
            String existingFileName = null;
            // For keep to file
            if (photoDetailBean.isSavedFor24Hours() == 1 && notification) {
                existingFileName = photoDetailBean.getPhotoPath().substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
                String existingFileDirrectory = photoDetailBean.getPhotoPath().substring(0, photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
                if (existingFileDirrectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    destinationDirectory = KEEPTODIRECTORYPATH;

                } else {
                    File directory = new File(FileUtils.getExternalStorageRoot(new File(photoDetailBean.getPhotoPath()), context) + "/.dir/");
                    if (directory != null) {
                        destinationDirectory = directory.getAbsolutePath() + "/";
                    }

                }
                status = Utils.moveFile(context, existingFileDirrectory, existingFileName, destinationDirectory, photoDetailBean.getTakenTime());
            }
            if (!isImageExists(photoDetailBean.getPhotoPath())) {
                contentValues.put(MDL_PHOTO_PATH, photoDetailBean.getPhotoPath());
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                contentValues.put(MDL_TIME_TAKEN, photoDetailBean.getTakenTime());
                contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                if (photoDetailBean.isSavedFor24Hours() == 1 && notification
                        && status && destinationDirectory != null) {
                    contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
                    if (imageMovedListener != null) {
                        if (destinationDirectory != null && !destinationDirectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                            imageMovedListener.onImageMoved(i, photoDetailBeanArrayList.size());

                        }

                    }
                }
                long id = database.insert(TABLE_MEDIA_LIST, null, contentValues);
                contentValues.clear();
            } else {
                if (photoDetailBean.isSaved() == 1) {
                    contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                    contentValues.put(MDL_ACTION_TIME, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_KEEPTO_URL, "");
                } else if (photoDetailBean.isSavedFor24Hours() == 1) {
                    contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                    contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                    contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                    if (notification
                            && status && destinationDirectory != null) {
                        contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
                        if (imageMovedListener != null) {
                            if (destinationDirectory != null && !destinationDirectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                                imageMovedListener.onImageMoved(i, photoDetailBeanArrayList.size());

                            }

                        }
                    }
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_IS_SAVED, 0);
                } else if (photoDetailBean.isDeleted() == 1) {
                    contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                    contentValues.put(MDL_IS_SAVED, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                } else if (photoDetailBean.isSaved() == 0 && photoDetailBean.isSavedFor24Hours() == 0
                        && photoDetailBean.isDeleted() == 0) {
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_IS_SAVED, 1);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_ACTION_TIME, 0);
                    contentValues.put(MDL_KEEPTO_URL, "");
                }
                database.update(TABLE_MEDIA_LIST, contentValues,
                        MDL_PHOTO_PATH + " =" + "'" + photoDetailBean.getPhotoPath() + "'", null);
                contentValues.clear();
            }
        }
        return true;

    }


    public boolean insertOrUpdatePhotoRestoreDetail(PhotoDetailBean photoDetailBean) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        boolean status = false;
        String destinationDirectory = null;
        String existingFileName = null;
        if (photoDetailBean.getPhotoLocalPath() != null &&
                !photoDetailBean.getPhotoLocalPath().isEmpty()) {
            existingFileName = photoDetailBean.getPhotoPath().substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
            String existingFileDirrectory = KEEPTODIRECTORYPATH;
            destinationDirectory = photoDetailBean.getPhotoOriginalPath().substring(0, photoDetailBean.getPhotoOriginalPath().lastIndexOf("/") + 1);
            if (destinationDirectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                existingFileDirrectory = KEEPTODIRECTORYPATH;

            } else {
                File directory = new File(FileUtils.getExternalStorageRoot(new File(photoDetailBean.getPhotoPath()), context) + "/.dir/");
                if (directory != null) {
                    existingFileDirrectory = directory.getAbsolutePath() + "/";
                }

            }
            status = Utils.moveFile(context, existingFileDirrectory, existingFileName, destinationDirectory, photoDetailBean.getTakenTime());

        } else {
            status = true;
        }
        // if media not exists then execute
        if (!isImageExists(photoDetailBean.getPhotoOriginalPath())) {
            contentValues.put(MDL_PHOTO_PATH, photoDetailBean.getPhotoPath());
            contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
            contentValues.put(MDL_TIME_TAKEN, photoDetailBean.getTakenTime());
            contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
            contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
            contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
            contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
            long id = database.insert(TABLE_MEDIA_LIST, null, contentValues);
            contentValues.clear();
        } else {
            if (photoDetailBean.isSaved() == 1) {
                contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_KEEPTO_URL, "");
            } else if (photoDetailBean.isSavedFor24Hours() == 1) {
                contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                if (notification
                        && status && destinationDirectory != null) {
                    contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
                }
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 0);
            } else if (photoDetailBean.isDeleted() == 1) {
                contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                contentValues.put(MDL_IS_SAVED, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
            } else if (photoDetailBean.isSaved() == 0 && photoDetailBean.isSavedFor24Hours() == 0
                    && photoDetailBean.isDeleted() == 0) {
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 1);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_KEEPTO_URL, "");
            }
            if (status) {
                database.update(TABLE_MEDIA_LIST, contentValues,
                        MDL_PHOTO_PATH + " =" + "'" + photoDetailBean.getPhotoOriginalPath() + "'", null);
            }
            contentValues.clear();
        }
        return true;

    }


    /*Use to insert or Keep To media file data(Restore)*/
    public boolean updateKeepToFromRestoreDetail(PhotoDetailBean photoDetailBean) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (photoDetailBean.isSavedFor24Hours() == 1) {
            contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
            contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
            contentValues.put(MDL_IS_DELETED, 0);
            contentValues.put(MDL_IS_SAVED, 0);
            contentValues.put(MDL_IS_SAVED_24_HOURS, 1);

        }
        database.update(TABLE_MEDIA_LIST, contentValues,
                MDL_PHOTO_PATH + " =" + "'" + photoDetailBean.getPhotoOriginalPath() + "'", null);
        contentValues.clear();
        return true;

    }

    /*Use to insert or Keep To media files data(Restore)*/
    public boolean updateKeepToFromRestoreDetails(ArrayList<PhotoDetailBean> photoDetailBeans) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for (PhotoDetailBean photoDetailBean : photoDetailBeans) {
            if (photoDetailBean.isSavedFor24Hours() == 1) {
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 1);

            }
            database.update(TABLE_MEDIA_LIST, contentValues,
                    MDL_PHOTO_PATH + " =" + "'" + photoDetailBean.getPhotoOriginalPath() + "'", null);
            contentValues.clear();
        }
        return true;

    }


    /*Use to insert or update Restore media files data*/
    public boolean insertOrUpdatePhotoRestoreDetails(ArrayList<PhotoDetailBean> photoDetailBeanArrayList) {
        database = this.getWritableDatabase();
        try {
            imageMovedListener = (ImageMovedListener) context;
        } catch (Exception e) {
        }
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < photoDetailBeanArrayList.size(); i++) {
            PhotoDetailBean photoDetailBean = photoDetailBeanArrayList.get(i);
            boolean status = false;
            String destinationDirectory = null;
            String existingFileName = null;
            String existingFileDirrectory = null;
            if (photoDetailBean.getPhotoLocalPath() != null &&
                    !photoDetailBean.getPhotoLocalPath().isEmpty()) {
                existingFileName = photoDetailBean.getPhotoPath().substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
                existingFileDirrectory = KEEPTODIRECTORYPATH;
                destinationDirectory = photoDetailBean.getPhotoOriginalPath().substring(0, photoDetailBean.getPhotoOriginalPath().lastIndexOf("/") + 1);
                if (destinationDirectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    existingFileDirrectory = KEEPTODIRECTORYPATH;

                } else {
                    File directory = new File(FileUtils.getExternalStorageRoot(new File(photoDetailBean.getPhotoPath()), context) + "/.dir/");
                    if (directory != null) {
                        existingFileDirrectory = directory.getAbsolutePath() + "/";
                    }

                }
                ;
                status = Utils.moveFile(context, existingFileDirrectory, existingFileName, destinationDirectory, photoDetailBean.getTakenTime());

            } else {
                status = true;
            }
            // if media not exists then execute
            if (!isImageExists(photoDetailBean.getPhotoOriginalPath())) {
                contentValues.put(MDL_PHOTO_PATH, photoDetailBean.getPhotoPath());
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                contentValues.put(MDL_TIME_TAKEN, photoDetailBean.getTakenTime());
                contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                long id = database.insert(TABLE_MEDIA_LIST, null, contentValues);
                contentValues.clear();
            } else {
                if (photoDetailBean.isSaved() == 1) {
                    contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                    contentValues.put(MDL_ACTION_TIME, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_KEEPTO_URL, "");
                    if (imageMovedListener != null) {
                        if (existingFileDirrectory != null && !existingFileDirrectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                            imageMovedListener.onImageMoved(i, photoDetailBeanArrayList.size());

                        }

                    }

                } else if (photoDetailBean.isSavedFor24Hours() == 1) {
                    contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                    contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                    contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                    if (notification
                            && status && destinationDirectory != null) {
                        contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);


                    }
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_IS_SAVED, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 1);
                    //contentValues.put(MDL_IS_SAVED,0);
                } else if (photoDetailBean.isDeleted() == 1) {
                    contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                    contentValues.put(MDL_IS_SAVED, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                    // contentValues.put(MDL_KEEPTO_URL, "");
                } else if (photoDetailBean.isSaved() == 0 && photoDetailBean.isSavedFor24Hours() == 0
                        && photoDetailBean.isDeleted() == 0) {
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_IS_SAVED, 1);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_ACTION_TIME, 0);
                    contentValues.put(MDL_KEEPTO_URL, "");
                    if (imageMovedListener != null) {
                        if (existingFileDirrectory != null && !existingFileDirrectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                            imageMovedListener.onImageMoved(i, photoDetailBeanArrayList.size());

                        }

                    }

                }
                if (status) {
                    database.update(TABLE_MEDIA_LIST, contentValues,
                            MDL_PHOTO_PATH + " =" + "'" + photoDetailBean.getPhotoOriginalPath() + "'", null);
                }
                contentValues.clear();
            }
        }
        return true;

    }


    // Use to get data accroding to action type
    public ArrayList<String> getImageList(int actionType) {
        database = this.getReadableDatabase();
        ArrayList<String> imageBeans = new ArrayList<String>();
        String columnName = null;
        if (Constants.saveImage == actionType) {
            columnName = MDL_IS_SAVED;

        } else if (Constants.saveImage24 == actionType) {
            columnName = MDL_IS_SAVED_24_HOURS;

        } else if (Constants.delete == actionType) {
            columnName = MDL_IS_DELETED;
        }
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST + " WHERE " + columnName + " = " + 1 + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                imageBeans.add(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageBeans;
    }

    /*Use to get Restore media file list*/
    public ArrayList<PhotoDetailBean> getRestoreImageList() {
        database = this.getReadableDatabase();
        ArrayList<PhotoDetailBean> imageBeans = new ArrayList<PhotoDetailBean>();
        String deleteColumnName = MDL_IS_DELETED;
        String isSave24column = MDL_IS_SAVED_24_HOURS;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST + " WHERE " + isSave24column + " = " + 1 + " or +" + deleteColumnName + " = " + 1 + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                PhotoDetailBean imageBean = new PhotoDetailBean();
                String keeptoPath = (cursor.getString(cursor.getColumnIndex(MDL_KEEPTO_URL)));
                if (keeptoPath != null && !keeptoPath.isEmpty()) {
                    imageBean.setPhotoPath(keeptoPath);
                } else {
                    imageBean.setPhotoPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                }
                imageBean.setPhotoOriginalPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                imageBean.setPhotoLocalPath(keeptoPath == null ? "" : keeptoPath);
                imageBean.setActionTime(cursor.getLong(cursor.getColumnIndex(MDL_ACTION_TIME)));
                imageBean.setSavedFor24Hours(cursor.getInt(cursor.getColumnIndex(MDL_IS_SAVED_24_HOURS)));
                imageBean.setKeepTime(cursor.getLong(cursor.getColumnIndex(MDL_KEEP_TIME)));
                imageBean.setDeleted(cursor.getInt(cursor.getColumnIndex(MDL_IS_DELETED)));
                imageBean.setTakenTime(cursor.getLong(cursor.getColumnIndex(MDL_TIME_TAKEN)));
                imageBeans.add(imageBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageBeans;
    }

    /*Use to get Keep to Media file list*/
    public ArrayList<PhotoDetailBean> getKeepToImageList(long keeptime) {
        database = this.getReadableDatabase();
        ArrayList<PhotoDetailBean> imageBeans = new ArrayList<PhotoDetailBean>();
        String isSave24column = MDL_IS_SAVED_24_HOURS;
        String KeepToColumn = MDL_KEEP_TIME;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST + " WHERE " + isSave24column + " = " + 1 + " and +" + KeepToColumn + " = " + keeptime + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                PhotoDetailBean imageBean = new PhotoDetailBean();
                String keeptoPath = (cursor.getString(cursor.getColumnIndex(MDL_KEEPTO_URL)));
                if (keeptoPath != null && !keeptoPath.isEmpty()) {
                    imageBean.setPhotoPath(keeptoPath);
                } else {
                    imageBean.setPhotoPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                }
                imageBean.setPhotoOriginalPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                imageBean.setPhotoLocalPath(keeptoPath == null ? "" : keeptoPath);
                imageBean.setActionTime(cursor.getLong(cursor.getColumnIndex(MDL_ACTION_TIME)));
                imageBean.setSavedFor24Hours(cursor.getInt(cursor.getColumnIndex(MDL_IS_SAVED_24_HOURS)));
                imageBean.setKeepTime(cursor.getLong(cursor.getColumnIndex(MDL_KEEP_TIME)));
                imageBean.setDeleted(cursor.getInt(cursor.getColumnIndex(MDL_IS_DELETED)));
                imageBean.setTakenTime(cursor.getLong(cursor.getColumnIndex(MDL_TIME_TAKEN)));
                imageBeans.add(imageBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageBeans;
    }

    // Use to get Restore media file
    public ArrayList<String> getRestoreImages() {
        database = this.getReadableDatabase();
        ArrayList<String> imageBeans = new ArrayList<String>();
        String deleteColumnName = MDL_IS_DELETED;
        String isSave24column = MDL_IS_SAVED_24_HOURS;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST + " WHERE " + isSave24column + " = " + 1 + " or +" + deleteColumnName + " = " + 1 + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                imageBeans.add(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageBeans;
    }


    // Use to get Keep To images for deletion
    public ArrayList<String> getDeletedSave24File(long timestamp, boolean isSave24File) {
        database = this.getReadableDatabase();
        ArrayList<String> imageBeans = new ArrayList<String>();
        String columnName;
        if (isSave24File) {
            columnName = MDL_IS_SAVED_24_HOURS;

        } else {
            columnName = MDL_IS_DELETED;

        }
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST + " WHERE " + columnName + " = " + 1 + " and  " + MDL_ACTION_TIME + "<" + timestamp + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String keeptoPath = (cursor.getString(cursor.getColumnIndex(MDL_KEEPTO_URL)));
                if (keeptoPath != null && !keeptoPath.isEmpty()) {
                    imageBeans.add(keeptoPath);
                } else {
                    imageBeans.add(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageBeans;
    }

    /*use to get Keep to media files*/
    public ArrayList<PhotoDetailBean> getKeepFile() {
        database = this.getReadableDatabase();
        ArrayList<PhotoDetailBean> imageBeans = new ArrayList<PhotoDetailBean>();
        String columnName = MDL_IS_SAVED_24_HOURS;
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST + " WHERE " + columnName + " = " + 1 + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                PhotoDetailBean photoDetailBean = new PhotoDetailBean();
                String keeptoPath = (cursor.getString(cursor.getColumnIndex(MDL_KEEPTO_URL)));
                if (keeptoPath != null && !keeptoPath.isEmpty()) {
                    photoDetailBean.setPhotoPath(keeptoPath);
                } else {
                    photoDetailBean.setPhotoPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                }
                photoDetailBean.setKeepTime(cursor.getLong(cursor.getColumnIndex(MDL_KEEP_TIME)));
                photoDetailBean.setActionTime(cursor.getLong(cursor.getColumnIndex(MDL_ACTION_TIME)));
                imageBeans.add(photoDetailBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imageBeans;
    }

    /*Use to delete media file*/
    public boolean deletedImage(String imagePath) {
        database = this.getWritableDatabase();
        return database.delete(TABLE_MEDIA_LIST, MDL_PHOTO_PATH + "=" + "'" + imagePath + "'" + " or " + MDL_KEEPTO_URL + "=" + "'" + imagePath + "'", null) > 0;

    }

    /*Use to check Media file exists or not*/
    public boolean isImageExists(String imagePath) {
        Cursor cursor = database.rawQuery("SELECT " + MDL_PHOTO_PATH + " FROM " + TABLE_MEDIA_LIST +
                " WHERE " + MDL_PHOTO_PATH + " = " + "'" + imagePath + "'", null);
        if (cursor != null && cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
