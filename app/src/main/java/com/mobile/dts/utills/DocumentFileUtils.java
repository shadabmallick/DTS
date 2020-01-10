package com.mobile.dts.utills;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static androidx.documentfile.provider.DocumentFile.fromTreeUri;
import static com.mobile.dts.utills.Constants.SDCARDROOTDIRECTORYURI;
import static com.mobile.dts.utills.Constants.appPref;


public abstract class DocumentFileUtils {

    public static final String BASIC_MIME_TYPE = "application/octet-stream";
    private static final int BUFFER = 2048;

    @NonNull
    public static OutputStream outputStreamFor(@NonNull DocumentFile outFile, @NonNull Context context)
            throws NullPointerException, FileNotFoundException {
        OutputStream out = context.getContentResolver().openOutputStream(outFile.getUri());
        if (out == null) throw new NullPointerException("Could not open DocumentFile OutputStream");
        return out;
    }

    /**
     * Very crude check.
     *
     * @return Whether filePath and documentFile represent the same file on disk.
     */
    public static boolean areSameFile(@Nullable String filePath, DocumentFile documentFile) {
        if (filePath == null) return false;
        File file = new File(filePath);
        return file.lastModified() == documentFile.lastModified()
                && file.getName().equals(documentFile.getName());
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static boolean safAwareDelete(@NonNull Context context, @NonNull final File file) {
        if (!file.exists()) return true;
        boolean deleteSucceeded = FileUtils.delete(file);
        if (!deleteSucceeded) {
            DocumentFile safFile = findFile(context, file);
            if (safFile != null) deleteSucceeded = safFile.delete();
        }
        return deleteSucceeded && !file.exists();
    }

    @Nullable
    public static DocumentFile findFile(Context context, final File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "File must exist. Use createFile() or createDirectory() instead.");
        } else {
            return seekOrCreateTreeDocumentFile(context, file, null, false);
        }
    }

    @Nullable
    public static DocumentFile createFile(Context context, final File file, String mimeType) {
        if (file.exists()) {
            throw new IllegalArgumentException(
                    "File must not exist. Use findFile() instead.");
        } else {
            return seekOrCreateTreeDocumentFile(context, file, mimeType, true);
        }
    }

    @Nullable
    public static DocumentFile createDirectory(Context context, final File directory) {
        if (directory.exists()) {
            return null;
        } else {
            return seekOrCreateTreeDocumentFile(context, directory, null, true);
        }
    }

    /**
     * Get a DocumentFile corresponding to the given file. If the file doesn't exist, it is created.
     *
     * @param file     The file to get the DocumentFile representation of.
     * @param mimeType Only applies if shouldCreate is true. The mimeType of the file to create.
     *                 Null creates directory.
     * @return The DocumentFile representing the passed file. Null if the file or its path can't
     * be created, or found - depending on shouldCreate's value.
     */
    @Nullable
    public static DocumentFile seekOrCreateTreeDocumentFile(@NonNull Context context,
                                                            @NonNull final File file,
                                                            @Nullable String mimeType,
                                                            boolean shouldCreate) {
        String storageRoot = FileUtils.getExternalStorageRoot(file, context);
        if (storageRoot == null) return null;   // File is not on external storage
        boolean fileIsStorageRoot = false;
        String filePathRelativeToRoot = null;
        try {
            String filePath = file.getCanonicalPath();
            if (!storageRoot.equals(filePath)) {
                filePathRelativeToRoot = filePath.substring(storageRoot.length() + 1);
            } else {
                fileIsStorageRoot = true;
            }
        } catch (IOException e) {
            // log("Could not get canonical path of File while getting DocumentFile");
            return null;
        } catch (SecurityException e) {
            fileIsStorageRoot = true;
        }
        Uri docTreeUri = findStorageTreeUri(context, storageRoot);
        if (docTreeUri == null) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(appPref, Activity.MODE_PRIVATE);
            String sdcardUri = sharedpreferences.getString(SDCARDROOTDIRECTORYURI, null);
            if (sdcardUri != null) {
                docTreeUri = Uri.parse(sdcardUri);
            } else {
                return null;
            }
            // We don't have write permission for storageRoot
        }
        // Walk the granted storage tree
        DocumentFile docFile = fromTreeUri(context, docTreeUri);
        if (fileIsStorageRoot) return docFile;
        String[] filePathSegments = filePathRelativeToRoot.split("/");
        for (int i = 0; i < filePathSegments.length; i++) {
            String segment = filePathSegments[i];
            boolean isLastSegment = i == filePathSegments.length - 1;
            DocumentFile nextDocFile = docFile.findFile(segment);
            if (nextDocFile == null && shouldCreate) {
                boolean shouldCreateFile = isLastSegment && mimeType != null;
                nextDocFile = shouldCreateFile ? docFile.createFile(mimeType, segment)
                        : docFile.createDirectory(segment);
            }
            if (nextDocFile == null) {
                // If shouldCreate = true, it means that current segment is not writable
                // Otherwise we couldn't find the file we were looking for
                return null;
            } else {
                docFile = nextDocFile;
            }
        }
        return docFile;
    }

    @Nullable
    private static Uri findStorageTreeUri(Context context, String storageRoot) {
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions) {
            if (permission.isWritePermission()) {
                DocumentFile grantTree = DocumentFile.fromTreeUri(context, permission.getUri());
                List<String> storageRoots = FileUtils.getExtSdCardPaths(context);
                for (String root : storageRoots) {
                    if (areSameFile(root, grantTree)) return grantTree.getUri();
                }
            }
        }
        return null;
    }

    public static boolean moveDocument(Context context, DocumentFile fileFrom, DocumentFile fileTo) {
        if (fileTo.isDirectory() /*&& fileTo.canWrite()*/) {
            if (fileFrom.isFile()) {
                return copyDocument(context, fileFrom, fileTo);
            } else if (fileFrom.isDirectory()) {
                DocumentFile[] filesInDir = fileFrom.listFiles();
                DocumentFile filesToDir = fileTo.createDirectory(fileFrom.getName());
                if (!filesToDir.exists()) {
                    return false;
                }
                for (int i = 0; i < filesInDir.length; i++) {
                    moveDocument(context, filesInDir[i], filesToDir);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean copyDocument(Context context, DocumentFile file, DocumentFile dest) {
        if (!file.exists() || file.isDirectory()) {
            Log.v("Utils", "copyDocument: file not exist or is directory, " + file);
            return false;
        }
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        byte[] data = new byte[BUFFER];
        int read = 0;
        try {
            if (!dest.exists()) {
                dest = dest.getParentFile().createDirectory(dest.getName());
                if (!dest.exists()) {
                    return false;
                }
            }
            String mimeType = getTypeForFile(file);
            String displayName = getNameFromFilename(file.getName());
            DocumentFile destFile = dest.createFile(mimeType, displayName);
            int n = 0;
            while (destFile == null && n++ < 32) {
                String destName = displayName + " (" + n + ")";
                destFile = dest.createFile(mimeType, destName);

            }
            if (destFile == null) {
                return false;
            }
            bos = new BufferedOutputStream(getOutputStream(context, destFile));
            bis = new BufferedInputStream(getInputStream(context, file));
            while ((read = bis.read(data, 0, BUFFER)) != -1) {
                bos.write(data, 0, read);
            }
            return true;
        } catch (FileNotFoundException e) {
            // Log.e(TAG, "copyDocument: file not found, " + file);
            e.printStackTrace();
        } catch (IOException e) {
            // Log.e(TAG, "copyDocument: " + e.toString());
        } finally {
            //flush and close
            IoUtils.flushQuietly(bos);
            IoUtils.closeQuietly(bos);
            IoUtils.closeQuietly(bis);
        }
        return false;
    }


    public static OutputStream getOutputStream(Context context, DocumentFile documentFile) throws FileNotFoundException {
        return context.getContentResolver().openOutputStream(documentFile.getUri());
    }

    public static InputStream getInputStream(Context context, DocumentFile documentFile) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(documentFile.getUri());
    }


    private static String getTypeForFile(DocumentFile file) {
        if (file.isDirectory()) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }


    public static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return BASIC_MIME_TYPE;
    }


    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }
}