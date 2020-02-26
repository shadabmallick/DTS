package com.mobile.dts.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mobile.dts.callbacks.ImageMovedListener;
import com.mobile.dts.model.FolderData;
import com.mobile.dts.model.KeepSafeData;
import com.mobile.dts.model.PhotoDetailBean;
import com.mobile.dts.utills.Constants;
import com.mobile.dts.utills.FileUtils;
import com.mobile.dts.utills.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static com.mobile.dts.activity.ImageViewerActivity.TAG;
import static com.mobile.dts.activity.MainApplication.KEEPTODIRECTORYPATH;

/*Use to store database data*/
public class SqlLiteHelper extends SQLiteOpenHelper {
    //db name
    public static final String DATABASE_NAME = "keeptoo.db";
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

    public static final String MDL_KEEP_SAFE = "keep_safe";
    public static final String MDL_KEEP_SAFE_DELLETED = "keep_safe_delete";
    public static final String MDL_FOLDER_ID = "folder_id";
    public static final String MDL_KEEPTO_HARD_DELETE = "hard_delete";

    public static final String MDL_SAFE_ENTRY_TIME = "safe_entry_time";

    //db version
    private static final int DATABASE_VERSION = 4;

    private static final String CREATE_TABLE_MEDIA_LIST_QUERY =
            "CREATE  TABLE " + TABLE_MEDIA_LIST
                    + " ( "
                    + MDL_PHOTO_PATH + " VARCHAR PRIMARY KEY, "
                    + MDL_ACTION_TIME + " INTEGER, "
                    + MDL_TIME_TAKEN + " INTEGER, "
                    + MDL_IS_SAVED + " INTEGER DEFAULT 0, "
                    + MDL_IS_SAVED_24_HOURS + " INTEGER DEFAULT 0, "
                    + MDL_IS_DELETED + " INTEGER DEFAULT 0, "
                    + MDL_KEEP_TIME + " INTEGER DEFAULT 0, "
                    + MDL_KEEP_SAFE + " INTEGER DEFAULT 0, "
                    + MDL_KEEP_SAFE_DELLETED + " INTEGER DEFAULT 0, "
                    + MDL_FOLDER_ID + " INTEGER DEFAULT 0, "
                    + MDL_KEEPTO_HARD_DELETE + " INTEGER DEFAULT 0, "
                    + MDL_SAFE_ENTRY_TIME + " INTEGER DEFAULT 0, "
                    + MDL_KEEPTO_URL + " VARCHAR );";


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

        db.execSQL(CREATE_TABLE_FOLDER);

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

        if (!isImageExists(photoDetailBean.getPhotoPath())
                && photoDetailBean.getPhotoPath() != null &&
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

            Log.d("TAG", "insert = "+status + "***" + photoDetailBean.getPhotoPath());

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
            } else if (photoDetailBean.isSaved() == 0
                    && photoDetailBean.isSavedFor24Hours() == 0
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


            Log.d("TAG", "update = "+status);
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
                } else if (photoDetailBean.isSaved() == 0
                        && photoDetailBean.isSavedFor24Hours() == 0
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
            existingFileName = photoDetailBean.getPhotoPath()
                    .substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
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

            Log.d("TAG", "isImageExists = "+status);

        } else {

            if (photoDetailBean.isSaved() == 1) {
                contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_KEEPTO_URL, "");
            }

            else if (photoDetailBean.isSavedFor24Hours() == 1) {
                contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                if (notification
                        && status && destinationDirectory != null) {
                    contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
                }
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 0);
            }

            else if (photoDetailBean.isDeleted() == 1) {
                contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                contentValues.put(MDL_IS_SAVED, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
            }

            else if (photoDetailBean.isSaved() == 0
                    && photoDetailBean.isSavedFor24Hours() == 0
                    && photoDetailBean.isDeleted() == 0) {
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 1);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_KEEPTO_URL, "");
            }

            Log.d("TAG", "db update status = "+status);
            if (status) {
                database.update(TABLE_MEDIA_LIST, contentValues,
                        MDL_PHOTO_PATH + " ="
                                + "'" + photoDetailBean.getPhotoOriginalPath()
                                + "'", null);
            }
            contentValues.clear();
        }
        return true;

    }

    public boolean updateForUndoImage(PhotoDetailBean photoDetailBean) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        boolean status = false;
        String destinationDirectory = null;
        String existingFileName = null;
        if (photoDetailBean.getPhotoLocalPath() != null &&
                !photoDetailBean.getPhotoLocalPath().isEmpty()) {
            existingFileName = photoDetailBean.getPhotoPath()
                    .substring(photoDetailBean.getPhotoPath().lastIndexOf("/") + 1);
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
        Log.d(TAG, "undo : "+isImageExists(photoDetailBean.getPhotoPath()));
        // if media not exists then execute
        Log.d("TAG", "insert = "+status + "***" + photoDetailBean.getPhotoPath());

        if (!isImageExists(photoDetailBean.getPhotoPath())) {
            contentValues.put(MDL_PHOTO_PATH, photoDetailBean.getPhotoPath());
            contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
            contentValues.put(MDL_TIME_TAKEN, photoDetailBean.getTakenTime());
            contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
            contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
            contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
            contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
            long id = database.insert(TABLE_MEDIA_LIST, null, contentValues);
            contentValues.clear();

            Log.d("TAG", "isImageExists = "+status);

        } else {

            if (photoDetailBean.isSaved() == 1) {
                contentValues.put(MDL_IS_SAVED, photoDetailBean.isSaved());
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_KEEPTO_URL, "");

                Log.d("TAG", "update 1 = "+status);
            }

            else if (photoDetailBean.isSavedFor24Hours() == 1) {
                contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                if (notification
                        && status && destinationDirectory != null) {
                    contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);
                }
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 0);

                Log.d("TAG", "update 2 = "+status);
            }

            else if (photoDetailBean.isDeleted() == 1) {
                contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                contentValues.put(MDL_IS_SAVED, 0);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());

                Log.d("TAG", "update 3 = "+status);
            }

            else if (photoDetailBean.isSaved() == 0
                    && photoDetailBean.isSavedFor24Hours() == 0
                    && photoDetailBean.isDeleted() == 0) {
                contentValues.put(MDL_IS_DELETED, 0);
                contentValues.put(MDL_IS_SAVED, 1);
                contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                contentValues.put(MDL_ACTION_TIME, 0);
                contentValues.put(MDL_KEEPTO_URL, "");

                Log.d("TAG", "update 4 = "+status);
            }

            Log.d("TAG", "db update status = "+status);
            if (status) {
                database.update(TABLE_MEDIA_LIST, contentValues,
                        MDL_PHOTO_PATH + " ="
                                + "'" + photoDetailBean.getPhotoPath()
                                + "'", null);
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
            e.printStackTrace();
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

                }

                else if (photoDetailBean.isSavedFor24Hours() == 1) {
                    contentValues.put(MDL_IS_SAVED_24_HOURS, photoDetailBean.isSavedFor24Hours());
                    contentValues.put(MDL_KEEP_TIME, photoDetailBean.getKeepTime());
                    contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                    if (notification
                            && status
                            && destinationDirectory != null) {
                        contentValues.put(MDL_KEEPTO_URL, destinationDirectory + existingFileName);

                    }
                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_IS_SAVED, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 1);
                    //contentValues.put(MDL_IS_SAVED,0);
                }

                else if (photoDetailBean.isDeleted() == 1) {
                    contentValues.put(MDL_IS_DELETED, photoDetailBean.isDeleted());
                    contentValues.put(MDL_IS_SAVED, 0);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_ACTION_TIME, photoDetailBean.getActionTime());
                    // contentValues.put(MDL_KEEPTO_URL, "");
                }

                else if (photoDetailBean.isSaved() == 0
                        && photoDetailBean.isSavedFor24Hours() == 0
                        && photoDetailBean.isDeleted() == 0) {

                    contentValues.put(MDL_IS_DELETED, 0);
                    contentValues.put(MDL_IS_SAVED, 1);
                    contentValues.put(MDL_IS_SAVED_24_HOURS, 0);
                    contentValues.put(MDL_ACTION_TIME, 0);
                    contentValues.put(MDL_KEEPTO_URL, "");

                    if (imageMovedListener != null) {
                        if (existingFileDirrectory != null
                                && !existingFileDirrectory.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                            imageMovedListener.onImageMoved(i, photoDetailBeanArrayList.size());

                        }
                    }
                }
                if (status) {
                    database.update(TABLE_MEDIA_LIST, contentValues,
                            MDL_PHOTO_PATH + " ="
                                    + "'" + photoDetailBean.getPhotoOriginalPath()
                                    + "'", null);
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

        Log.d("DB", "imageBeans : "+imageBeans);

        return imageBeans;
    }


    public ArrayList<String> getSavedImageList() {
        database = this.getReadableDatabase();
        ArrayList<String> imageBeans = new ArrayList<String>();
        String columnName = MDL_IS_SAVED;

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
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST
                + " WHERE " + isSave24column + " = " + 1
                + " or +" + deleteColumnName + " = " + 1
                + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
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
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST
                + " WHERE " + isSave24column + " = " + 1
                + " and +" + KeepToColumn + " = " + keeptime
                + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
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
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_MEDIA_LIST
                + " WHERE " + isSave24column + " = " + 1
                + " or +" + deleteColumnName + " = " + 1
                + " ORDER BY " + MDL_TIME_TAKEN + " DESC", null);
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

    public void queryData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public void insertData(String name, String price, byte[] image){
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO FOOD VALUES (NULL, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1, name);

        statement.bindBlob(2, image);

        statement.executeInsert();
    }

    public void updateData(String name, String price, byte[] image, int id) {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "UPDATE FOOD SET name = ?, image = ? WHERE id = ?";
        SQLiteStatement statement = database.compileStatement(sql);

        statement.bindString(1, name);

        statement.bindBlob(2, image);
        statement.bindDouble(4, (double)id);

        statement.execute();
        database.close();
    }

    public  void deleteData(int id) {
        SQLiteDatabase database = getWritableDatabase();

        String sql = "DELETE FROM FOOD WHERE id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1, (double)id);

        statement.execute();
        database.close();
    }

    public Cursor getData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql, null);
    }





    //////////////// NEW WORK ... 6-Jan-2020

    public static final String TABLE_FOLDER = "table_folder";
    public static final String FOLDER_ID = "folder_id";
    public static final String FOLDER_NAME = "folder_name";
    public static final String FOLDER_CREATION_TIME = "folder_creation_time";
    public static final String FOLDER_IS_DELETED = "folder_is_deleted";

    private static final String CREATE_TABLE_FOLDER = "CREATE TABLE " + TABLE_FOLDER
            + " ( " + FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FOLDER_CREATION_TIME + " INTEGER, "
            + FOLDER_IS_DELETED + " INTEGER DEFAULT 0, "
            + FOLDER_NAME + " VARCHAR );";

    public void insertFolder(FolderData folderData){
        database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FOLDER_NAME, folderData.getFolderName());
        contentValues.put(FOLDER_CREATION_TIME, Calendar.getInstance().getTimeInMillis());

        database.insert(TABLE_FOLDER, null, contentValues);
        contentValues.clear();

    }

    public void deleteFolder(int folder_id){

        database = this.getWritableDatabase();

        String selection = FOLDER_ID + " =? ";
        String[] selectionArgs = { String.valueOf(folder_id) };

        database.delete(TABLE_FOLDER, selection, selectionArgs);

    }

    public ArrayList<FolderData> getAllFolder(){
        database = this.getReadableDatabase();

        ArrayList<FolderData> list = new ArrayList<>();

        String rawQuery = "SELECT * FROM " + TABLE_FOLDER
                + " WHERE " + FOLDER_IS_DELETED + " = " + 0
                + " ORDER BY " + FOLDER_CREATION_TIME + " DESC";

        Cursor cursor = database.rawQuery(rawQuery, null);

        if (cursor.moveToFirst()) {
            do {

                FolderData folderData = new FolderData();
                folderData.setFolderId(cursor.getInt(cursor.getColumnIndex(FOLDER_ID)));
                folderData.setFolderName(cursor.getString(cursor.getColumnIndex(FOLDER_NAME)));

                list.add(folderData);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }


    public void updateFolderId(int folder_id, String imagePath) {

        if (isImageExists(imagePath)) {

            database = this.getWritableDatabase();

            String selection = MDL_PHOTO_PATH + " =? ";
            String[] selectionArgs = { imagePath };

            ContentValues contentValues = new ContentValues();
            contentValues.put(MDL_FOLDER_ID, folder_id);
            contentValues.put(MDL_SAFE_ENTRY_TIME, Calendar.getInstance().getTimeInMillis());

            database.update(TABLE_MEDIA_LIST, contentValues, selection, selectionArgs);
            //contentValues.clear();

            Log.d("DB", "TABLE_MEDIA_LIST insert: ");
            Log.d("DB", "TABLE_MEDIA_LIST folder_id: "+folder_id);

        }else {

            ContentValues contentValues = new ContentValues();
            contentValues.put(MDL_PHOTO_PATH, imagePath);
            contentValues.put(MDL_FOLDER_ID, folder_id);
            contentValues.put(MDL_SAFE_ENTRY_TIME, Calendar.getInstance().getTimeInMillis());

            database.insert(TABLE_MEDIA_LIST, null, contentValues);

        }


    }



    public ArrayList<KeepSafeData> getFolderWiseImages(String folder_id) {

        ArrayList<KeepSafeData> arrayList = new ArrayList<>();

        database = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEDIA_LIST
                + " WHERE " + MDL_FOLDER_ID + " = " + folder_id
                + " ORDER BY " + MDL_SAFE_ENTRY_TIME + " DESC";

        //Log.d("DB", "query : "+query);

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {

                KeepSafeData keepSafeData = new KeepSafeData();
                keepSafeData.setFolderId(cursor.getInt(cursor.getColumnIndex(MDL_FOLDER_ID)));
                keepSafeData.setPhotoOriginalPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                keepSafeData.setEntryTime(cursor.getInt(cursor.getColumnIndex(MDL_SAFE_ENTRY_TIME)));

                arrayList.add(keepSafeData);

            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d("DB", "arrayList size: "+arrayList.size());

        return arrayList;
    }



    public void deleteKeepSafeImage(String imagePath){
        database = this.getWritableDatabase();

        String selection = MDL_PHOTO_PATH + " =? ";
        String[] selectionArgs = { imagePath };

        database.delete(TABLE_MEDIA_LIST, selection, selectionArgs);
    }









    //////////////////////////////////////////////////


    public static final String TABLE_KEEP_SAFE = "table_keep_safe";
    public static final String SAFE_ID = "safe_id";

    public static final String SAFE_BYTE_DATA = "safe_byte_data";

    private static final String CREATE_TABLE_KEEP_SAFE = "CREATE TABLE " + TABLE_KEEP_SAFE
            + " ( " + SAFE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FOLDER_ID + " INTEGER, "
            + MDL_PHOTO_PATH + " VARCHAR, "
            + MDL_SAFE_ENTRY_TIME + " INTEGER, "
            + MDL_IS_DELETED + " INTEGER DEFAULT 0, "
            + SAFE_BYTE_DATA + " BLOB );";

    public void insertToKeepSafe(KeepSafeData keepSafeData){
        database = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(FOLDER_ID, keepSafeData.getFolderId());
        contentValues.put(MDL_PHOTO_PATH, keepSafeData.getPhotoOriginalPath());
        contentValues.put(MDL_SAFE_ENTRY_TIME, keepSafeData.getEntryTime());
        contentValues.put(SAFE_BYTE_DATA, keepSafeData.getPhotoByte());
        contentValues.put(MDL_IS_DELETED, 0);


        database.insert(TABLE_KEEP_SAFE, null, contentValues);
        contentValues.clear();

        Log.d("TAG", "insert keep safe " +keepSafeData.getFolderId());
        Log.d("TAG", "insert keep safe " +keepSafeData.getPhotoOriginalPath());
        Log.d("TAG", "insert keep safe " +keepSafeData.getEntryTime());
        Log.d("TAG", "insert keep safe " +keepSafeData.getPhotoByte());

    }


    public ArrayList<KeepSafeData> getFolderWiseImage(int folder_id){
        database = this.getReadableDatabase();

        ArrayList<KeepSafeData> arrayList = new ArrayList<>();

        String rawQuery = "SELECT * FROM " + TABLE_KEEP_SAFE
                + " WHERE " + FOLDER_ID + " = " + folder_id
                + " AND " + MDL_IS_DELETED + " = " + 0
                + " ORDER BY " + MDL_SAFE_ENTRY_TIME + " DESC";

        Cursor cursor = database.rawQuery(rawQuery, null);

        if (cursor.moveToFirst()) {
            do {

                KeepSafeData keepSafeData = new KeepSafeData();
                keepSafeData.setId(cursor.getInt(cursor.getColumnIndex(SAFE_ID)));
                keepSafeData.setFolderId(cursor.getInt(cursor.getColumnIndex(FOLDER_ID)));
                keepSafeData.setPhotoOriginalPath(cursor.getString(cursor.getColumnIndex(MDL_PHOTO_PATH)));
                keepSafeData.setPhotoByte(cursor.getBlob(cursor.getColumnIndex(SAFE_BYTE_DATA)));
                keepSafeData.setEntryTime(cursor.getInt(cursor.getColumnIndex(MDL_SAFE_ENTRY_TIME)));

                arrayList.add(keepSafeData);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return arrayList;
    }




}
