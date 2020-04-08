package com.example.lenovo.graduworkapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/5.
 */

public class DiabetePagerActivity extends FragmentActivity {
    private ViewPager mViewPager;
    private List<Diabete> mDiabetes;
    private Button mFirstPagerButton;
    private Button mLastPagerButton;

    private static final String EXTRA_DIABETE_ID = "com.example.lenovo.graduworkapp.diabete_id";

    private TextView mTempTextView;
    private SeekBar mSeekBar;
    private BluetoothController mController;

    public static Intent newIntent(Context packageContext, UUID diabeteId) {
        Intent intent = new Intent(packageContext, DiabetePagerActivity.class);
        intent.putExtra(EXTRA_DIABETE_ID, diabeteId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diabete_pager);

        mViewPager = (ViewPager) findViewById(R.id.diabete_view_pager);
        mSeekBar = (SeekBar) findViewById(R.id.diabete_pager_seek_bar);
        mSeekBar.setEnabled(false);
        mDiabetes = DiabeteLab.get(this).getDiabetes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Diabete diabete = mDiabetes.get(position);
                return DiabeteFragment.newInstance(diabete.getId());
            }

            @Override
            public int getCount() {
                return mDiabetes.size();
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return FragmentStatePagerAdapter.POSITION_NONE;
            }
        });

        UUID diabeteId = (UUID) getIntent().getSerializableExtra(EXTRA_DIABETE_ID);
        for (int i = 0; i < mDiabetes.size(); i++) {
            if (mDiabetes.get(i).getId().equals(diabeteId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mViewPager.getCurrentItem() == 0) {
                    mFirstPagerButton.setVisibility(View.INVISIBLE);
                } else {
                    mFirstPagerButton.setVisibility(View.VISIBLE);
                }
                if (mViewPager.getCurrentItem() == mViewPager.getAdapter().getCount() - 1) {
                    mLastPagerButton.setVisibility(View.INVISIBLE);
                } else {
                    mLastPagerButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mFirstPagerButton = (Button) findViewById(R.id.first_pager_button);
        mFirstPagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(0);
            }
        });
        mLastPagerButton = (Button) findViewById(R.id.last_pager_button);
        mLastPagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1);
            }
        });

        mTempTextView = (TextView) findViewById(R.id.diabete_pager_temp);

        mController = BluetoothController.get(getApplicationContext());
        mController.setOnTempChangeListener(new BluetoothController.OnTempChangeListener() {
            @Override
            public void onTempChange(float temp) {
                mSeekBar.setProgress((int) temp);
                String str = Float.toString(temp);
                mTempTextView.setText(str);
            }
        });

    }
}
