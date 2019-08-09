package com.mobile.dts.model;

import java.io.Serializable;


public class UserBean implements Serializable {
    static UserBean userBean;
    public String userName = "";
    public String lastName = "";
    public String password = "";
    public String emailId = "";
    public String mobileNumber = "";
    public String uniqueId = "";
    public String userId;
    public String accountType = "0";
    public String verified;
    public String countryCode = "";
    public String otp;
    public String picUrl = "";
    public boolean isSuccess;

    public static UserBean getObect() {
        if (userBean == null) {
            userBean = new UserBean();
        }
        return userBean;
    }

    public static void resetData() {
        userBean = null;
    }
}
