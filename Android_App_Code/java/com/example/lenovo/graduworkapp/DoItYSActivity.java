package com.example.lenovo.graduworkapp;

import android.support.v4.app.Fragment;

public class DoItYSActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return DoItYSFragment.newInstance();
    }
}
