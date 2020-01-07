package com.mobile.dts.model;


public class FolderData {

    private int folderId;
    private String folderName;
    private long creationTime;
    private int folderDeleted;


    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getFolderDeleted() {
        return folderDeleted;
    }

    public void setFolderDeleted(int folderDeleted) {
        this.folderDeleted = folderDeleted;
    }
}