package com.example.tryapp;

import java.util.ArrayList;

public class Parent {
    private String parentID = null,name,email,mobile,userID;
    private ArrayList<String> childID;

    public Parent(String userID, String name, String mobile, String email) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public Parent() {
    }

    //getters and setters


    public ArrayList<String> getChildID() {
        return childID;
    }

    public void setChildID(ArrayList<String> childID) {
        this.childID = childID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
