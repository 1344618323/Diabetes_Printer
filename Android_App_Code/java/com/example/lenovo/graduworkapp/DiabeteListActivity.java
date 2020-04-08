package com.example.lenovo.graduworkapp;

import android.support.v4.app.Fragment;

/**
 * Created by Lenovo on 2018/3/4.
 */

public class DiabeteListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new DiabeteListFragment();
    }
}
