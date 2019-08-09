package com.mobile.dts.model;

import android.os.Parcel;
import android.os.Parcelable;


public class ImageBean implements Parcelable {

    public static final Creator<ImageBean> CREATOR = new Creator<ImageBean>() {
        @Override
        public ImageBean createFromParcel(Parcel in) {
            return new ImageBean(in);
        }

        @Override
        public ImageBean[] newArray(int size) {
            return new ImageBean[size];
        }
    };
    private String imagePath;
    private boolean isChecked = false;
    private boolean isRecent = false;
    private int imageSize;
    private String imageName;
    private boolean isSaved24 = false;
    private boolean isDeleted = false;
    private long keepTime;
    private long createdTimeStamp;
    private String createdDate;
    private String createdTime;
    private long actionTime;
    private long remainingTime;
    private boolean isNew;
    private int mediaType;
    private String fileOriginalPath;
    private String fileLocalPath;
    private int viewType;

    public ImageBean() {
        ;
    }

    protected ImageBean(Parcel in) {
        imagePath = in.readString();
        isChecked = in.readByte() != 0;
        isRecent = in.readByte() != 0;
        imageSize = in.readInt();
        imageName = in.readString();
        isSaved24 = in.readByte() != 0;
        isDeleted = in.readByte() != 0;
        keepTime = in.readLong();
        createdTimeStamp = in.readLong();
        createdDate = in.readString();
        createdTime = in.readString();
        actionTime = in.readLong();
        remainingTime = in.readLong();
        isNew = in.readByte() != 0;
        mediaType = in.readInt();
        fileOriginalPath = in.readString();
        fileLocalPath = in.readString();
        viewType = in.readInt();
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getFileOriginalPath() {
        return fileOriginalPath;
    }

    public void setFileOriginalPath(String fileOriginalPath) {
        this.fileOriginalPath = fileOriginalPath;
    }

    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imagePath);
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeByte((byte) (isRecent ? 1 : 0));
        dest.writeInt(imageSize);
        dest.writeString(imageName);
        dest.writeByte((byte) (isSaved24 ? 1 : 0));
        dest.writeByte((byte) (isDeleted ? 1 : 0));
        dest.writeLong(keepTime);
        dest.writeLong(createdTimeStamp);
        dest.writeString(createdDate);
        dest.writeString(createdTime);
        dest.writeLong(actionTime);
        dest.writeLong(remainingTime);
        dest.writeByte((byte) (isNew ? 1 : 0));
        dest.writeInt(mediaType);
        dest.writeString(fileOriginalPath);
        dest.writeString(fileLocalPath);
        dest.writeInt(viewType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public long getKeepTime() {
        return keepTime;
    }

    public void setKeepTime(long keepTime) {
        this.keepTime = keepTime;
    }

    public long getActionTime() {
        return actionTime;
    }

    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }


    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }


    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public long getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(long createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public boolean isSaved24() {
        return isSaved24;
    }

    public void setSaved24(boolean saved24) {
        isSaved24 = saved24;
    }

    public boolean isRecent() {
        return isRecent;
    }

    public void setRecent(boolean recent) {
        isRecent = recent;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getImageSize() {
        return imageSize;
    }

    public void setImageSize(int imageSize) {
        this.imageSize = imageSize;
    }
}

