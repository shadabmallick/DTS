package com.mobile.dts.utills;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import android.text.format.Formatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.net.Uri.fromFile;
import static android.os.Build.VERSION_CODES.N;
import static java.util.Collections.unmodifiableList;


public class FileUtils {
    public static final String NOMEDIA_FILE_NAME = ".nomedia";
    private static final String EXTENSION_APK = "apk";

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param path The file path or name
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String path) {
        String ext = "";
        String name = new File(path).getName();
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1) {
            ext = name.substring(i).toLowerCase();
        }
        return ext;
    }

    /**
     * @deprecated Use getUri() instead. This will intentionally crash on and rafter API 24.
     */
    @Deprecated
    private static Uri getFileUri(File fileHolder) {
        if (Build.VERSION.SDK_INT >= N) {
            throw new IllegalStateException("Tried to use File URI on a new Android version.");
        }
        return fromFile(fileHolder);
    }


    /**
     * Convert Uri into File.
     *
     * @param uri Uri to convert.
     * @return The file pointed to by the uri.
     */
    public static File getFile(Uri uri) {
        if (uri != null) {
            String filepath = uri.getPath();
            if (filepath != null) {
                return new File(filepath);
            }
        }
        return null;
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file The file whose path to get.
     * @return The first directory up from file. If file.isdirectory returns the file.
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();
                // Construct path without file name.
                String pathWithoutName = filepath.substring(0, filepath.length() - filename.length());
                if (pathWithoutName.endsWith("/")) {
                    pathWithoutName = pathWithoutName.substring(0, pathWithoutName.length() - 1);
                }
                return new File(pathWithoutName);
            }
        }
        return null;
    }

    public static String formatSize(Context context, long sizeInBytes) {
        return Formatter.formatFileSize(context, sizeInBytes);
    }

    public static long folderSize(File directory) {
        long length = 0;
        File[] files = directory.listFiles();
        if (files != null)
            for (File file : files)
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
        return length;
    }

    /**
     * @param f File which needs to be checked.
     * @return True if the file is a zip archive.
     */
    public static boolean isZipArchive(File f) {
        // Hacky but fast
        return f.isFile() && FileUtils.getExtension(f.getAbsolutePath()).equals(".zip");
    }

    /**
     * Recursively count all files in the <code>file</code>'s subtree.
     *
     * @param file The root of the tree to count.
     */
    public static int countFilesUnder(File file) {
        int fileCount = 0;
        if (!file.isDirectory()) {
            fileCount++;
        } else {
            if (file.list() != null) {
                for (File f : file.listFiles()) {
                    fileCount += countFilesUnder(f);
                }
            }
        }
        return fileCount;
    }

    public static int countFilesUnder(List<File> list) {
        int fileCount = 0;
        for (File fh : list) {
            fileCount += countFilesUnder(fh);
        }
        return fileCount;
    }

    /**
     * Native helper method, returns whether the current process has execute privilages.
     *
     * @param file File
     * @return returns True if the current process has execute permission.
     */
    public static boolean canExecute(File file) {
        return file.canExecute();
    }

    public static boolean delete(File fileOrDirectory) {
        boolean res = true;
        // Delete children if directory
        File[] children = fileOrDirectory.listFiles();
        boolean hasChildren = children != null && children.length != 0;
        if (hasChildren) {
            for (File childFile : children) {
                if (childFile.isDirectory()) {
                    res &= delete(childFile);
                } else {
                    res &= deleteFile(childFile);
                }
            }
        }
        // Delete the file itself
        res &= deleteFile(fileOrDirectory);
        return res;
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isWritable(@NonNull final File file) {
        boolean fileJustCreated = !file.exists();
        // Check by opening a stream
        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException ignored) {
            }
        } catch (FileNotFoundException ignored) {
            return false;
        }
        // If stream successful, check with Java
        boolean writable = file.canWrite();
        if (fileJustCreated) file.delete();
        return writable;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @return true If on external storage.
     */
    public static boolean isOnExternalStorage(final File file, Context context) {
        return getExternalStorageRoot(file, context) != null;
    }

    /**
     * @param file The file whose parent to look for.
     * @return A File representing the root of the external storage device that contains the file, otherwise null.
     */
    public static String getExternalStorageRoot(final File file, Context context) {
        String filePath;
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException | SecurityException e) {
            return null;
        }
        List<String> extSdPaths = getExtSdCardPaths(context);
        for (String extSdPath : extSdPaths) {
            if (filePath.startsWith(extSdPath)) return extSdPath;
        }
        return null;
    }

    /**
     * Get a list of external SD card paths.
     *
     * @return A list of external SD card paths.
     */
    @NonNull
    public static List<String> getExtSdCardPaths(Context context) {
        File[] externalStorageFilesDirs = context.getExternalFilesDirs(null);
        File primaryStorageFilesDir = context.getExternalFilesDir(null);
        List<String> externalStorageRoots = new ArrayList<>();
        for (File extFilesDir : externalStorageFilesDirs) {
            if (extFilesDir != null && !extFilesDir.equals(primaryStorageFilesDir)) {
                int rootPathEndIndex = extFilesDir.getAbsolutePath().lastIndexOf("/Android/data");
                if (rootPathEndIndex < 0) {
                    //  log("Unexpected external storage directory.");
                } else {
                    String path = extFilesDir.getAbsolutePath().substring(0, rootPathEndIndex);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        //log("Could not get canonical path for external storage. Using absolute.");
                    }
                    externalStorageRoots.add(path);
                }
            }
        }
        int rootsCount = externalStorageRoots.size();
        return unmodifiableList(externalStorageRoots);
    }


    private static boolean deleteFile(File childFile) {
        return !childFile.exists() || childFile.delete();
    }
}