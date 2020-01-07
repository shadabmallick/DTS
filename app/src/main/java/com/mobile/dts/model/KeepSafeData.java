package com.mobile.dts.model;


public class KeepSafeData {

    private int id;
    private int folderId;
    private long entryTime;
    private String photoOriginalPath;
    private byte[] photoByte;
    private int isDeleted;



    public String getPhotoOriginalPath() {
        return photoOriginalPath;
    }

    public void setPhotoOriginalPath(String photoOriginalPath) {
        this.photoOriginalPath = photoOriginalPath;
    }


    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public byte[] getPhotoByte() {
        return photoByte;
    }

    public void setPhotoByte(byte[] photoByte) {
        this.photoByte = photoByte;
    }
}