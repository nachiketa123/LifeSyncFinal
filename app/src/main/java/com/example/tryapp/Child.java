package com.example.tryapp;

import java.util.ArrayList;

;

public class Child {
    private String userID,childID=null,parentID,name,mobile,email;
    private ArrayList<String> blockedAppList;

    public Child(String userID, String name, String mobile, String email) {
        this.userID = userID;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
    }

    public Child() {
    }

    //getters and setters


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getChildID() {
        return childID;
    }

    public void setChildID(String childID) {
        this.childID = childID;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getBlockedAppList() {
        return blockedAppList;
    }

    public void setBlockedAppList(ArrayList<String> blockedAppList) {
        this.blockedAppList = blockedAppList;
    }
}
