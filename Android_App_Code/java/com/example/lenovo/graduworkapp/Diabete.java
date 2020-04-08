package com.example.lenovo.graduworkapp;

import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/4.
 */

public class Diabete {
    private UUID mId;
    private String mName;
    private boolean mMade;
    private int mDrawId;
    private int mNoId;

    public Diabete(String string) {
        mName = string;
        mId = UUID.randomUUID();
    }

    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name){
        mName=name;
    }
    public boolean isMade() {
        return mMade;
    }

    public void setMade(boolean made) {
        mMade = made;
    }

    public void setDrawId(int id){
        mDrawId=id;
    }
    public int getDrawId(){
        return mDrawId;
    }

    public void setNoId(int id){
        mNoId=id;
    }
    public int getNoId(){
        return mNoId;
    }
}
