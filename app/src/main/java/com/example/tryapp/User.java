package com.example.tryapp;

public class User {
    private String userID=null,type,locatorId,token;

    public User(String type, String locatorId, String token) {

        this.type = type;
        this.locatorId = locatorId;
        this.token = token;
    }

    public User() {
    }

    //setters and getters


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLocatorId(String locatorId) {
        this.locatorId = locatorId;
    }

    public String getLocatorId() {
        return locatorId;
    }


    public String getUserID() {
        return userID;
    }

    public String getType() {
        return type;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setType(String type) {
        this.type = type;
    }
}
