package com.mobile.dts.model;


public class PhotoDetailBean {
    private String photoPath;
    private long actionTime;
    private long takenTime;
    private long keepTime;
    private int isSaved ,isSavedFor24Hours ,isDeleted;
    private String photoOriginalPath;
    private String photoLocalPath;


    public String getPhotoLocalPath() {
        return photoLocalPath;
    }

    public void setPhotoLocalPath(String photoLocalPath) {
        this.photoLocalPath = photoLocalPath;
    }

    public String getPhotoOriginalPath() {
        return photoOriginalPath;
    }

    public void setPhotoOriginalPath(String photoOriginalPath) {
        this.photoOriginalPath = photoOriginalPath;
    }






    public long getKeepTime() {
        return keepTime;
    }

    public void setKeepTime(long keepTime) {
        this.keepTime = keepTime;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public long getActionTime() {
        return actionTime;
    }

    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }

    public long getTakenTime() {
        return takenTime;
    }

    public void setTakenTime(long takenTime) {
        this.takenTime = takenTime;
    }

    public int isSaved() {
        return isSaved;
    }

    public void setSaved(int saved) {
        isSaved = saved;
    }

    public int isSavedFor24Hours() {
        return isSavedFor24Hours;
    }

    public void setSavedFor24Hours(int savedFor24Hours) {
        isSavedFor24Hours = savedFor24Hours;
    }

    public int isDeleted() {
        return isDeleted;
    }

    public void setDeleted(int deleted) {
        isDeleted = deleted;
    }


}