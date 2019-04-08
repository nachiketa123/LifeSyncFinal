package com.example.tryapp;

public class SOS {
    private String SOSID=null, TO_ID, FROM_ID, TO_TYPE, FROM_TYPE, MESSAGE;

    public SOS() {

    }

    public SOS(String fromID, String toID, String fromType, String toType, String msg) {
        this.TO_ID = toID;
        this.FROM_ID = fromID;
        this.TO_TYPE = toType;
        this.FROM_TYPE = fromType;
        this.MESSAGE = msg;
    }


    //getters and setters
    public void setSOSID(String SOSID) {
        this.SOSID = SOSID;
    }

    public void setTO_ID(String TO_ID) {
        this.TO_ID = TO_ID;
    }

    public void setFROM_ID(String FROM_ID) {
        this.FROM_ID = FROM_ID;
    }

    public void setTO_TYPE(String TO_TYPE) {
        this.TO_TYPE = TO_TYPE;
    }

    public void setFROM_TYPE(String FROM_TYPE) {
        this.FROM_TYPE = FROM_TYPE;
    }

    public void setMESSAGE(String MESSAGE) {
        this.MESSAGE = MESSAGE;
    }

    public String getSOSID() {
        return SOSID;
    }

    public String getTO_ID() {
        return TO_ID;
    }

    public String getFROM_ID() {
        return FROM_ID;
    }

    public String getTO_TYPE() {
        return TO_TYPE;
    }

    public String getFROM_TYPE() {
        return FROM_TYPE;
    }

    public String getMESSAGE() {
        return MESSAGE;
    }
}

