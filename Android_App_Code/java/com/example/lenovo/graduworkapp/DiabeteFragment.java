package com.example.lenovo.graduworkapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/4.
 */

public class DiabeteFragment extends Fragment {
    private static final String ARG_DIABETE_ID = "diabete_id";
    private static final String Sure_Making_Diabete = "SureMakingDiabete";
    private static final String Detial_Photo_Diabete = "DetaiPhotoDiabete";

    private static final int REQUEST_YESORNO = 0;

    private Diabete mDiabete;
    private TextView mDiabeteTextView;
    private Button mDiabeteMakeButton;
    private ImageView mImageView;
    private int mPhotoWidth, mPhotoHeight;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID diabeteId = (UUID) getArguments().getSerializable(ARG_DIABETE_ID);
        mDiabete = DiabeteLab.get(getActivity()).getDiabete(diabeteId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diabete, container, false);

        mDiabeteTextView = (TextView) v.findViewById(R.id.diabete_fragment_name);
        mDiabeteTextView.setText(mDiabete.getName());
        mDiabeteMakeButton = (Button) v.findViewById(R.id.diabete_fragment_making);
        mDiabeteMakeButton.setText(mDiabete.isMade() == false ? R.string.diabete_making_label : R.string.stop_making_label);
        mDiabeteMakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BluetoothController.get(getActivity()).isConnected()) {
                    //发送消息给打印机
                    FragmentManager fragmentManager = getFragmentManager();
                    SureDialogFragment makingDiabeteFragment = SureDialogFragment.newInstance(mDiabete.isMade());
                    makingDiabeteFragment.setTargetFragment(DiabeteFragment.this, REQUEST_YESORNO);
                    makingDiabeteFragment.show(fragmentManager, Sure_Making_Diabete);
                }
            }
        });



        mImageView = (ImageView) v.findViewById(R.id.diabete_fragment_photo);
        ViewTreeObserver observer = mImageView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    mPhotoWidth = mImageView.getMeasuredWidth();
                    mPhotoHeight = mImageView.getMeasuredHeight();
                    if (mDiabete.getDrawId() != 0) {
                        bitmap = PictureUtils.getScaledBitmap(getActivity(), mDiabete.getDrawId(), mPhotoWidth, mPhotoHeight);
                        mImageView.setImageBitmap(bitmap);
                    } else {
                        mImageView.setImageBitmap(null);
                    }
                }
            });
        }
        mImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDiabete.getDrawId() != 0) {
                    FragmentManager fragmentManager = getFragmentManager();
                    DetailPhotoDialogFragment detailPhotoDialogFragment = DetailPhotoDialogFragment.newInstance(mDiabete.getDrawId());
                    detailPhotoDialogFragment.show(fragmentManager, Detial_Photo_Diabete);
                }
            }
        });

        return v;
    }

    public static DiabeteFragment newInstance(UUID diabeteId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIABETE_ID, diabeteId);
        DiabeteFragment fragment = new DiabeteFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_YESORNO) {
            if (resultCode == Activity.RESULT_OK) {
                boolean makingOrNO = (boolean) data.getSerializableExtra(SureDialogFragment.EXTRA_MAKINGORNO);
                if (makingOrNO == true) {
                    if (mDiabete.isMade() == false) {
                        if (DiabeteLab.get(getActivity()).getCurrentDiabete() != null) {
                            DiabeteLab.get(getActivity()).getCurrentDiabete().setMade(false);
                        }
                        DiabeteLab.get(getActivity()).setCurrentDiabete(mDiabete);
                        mDiabete.setMade(true);
                        BluetoothController.get(getActivity()).writeToDevice(
                                'A' + String.valueOf(mDiabete.getNoId()) + 'Z' + 'A' + String.valueOf(mDiabete.getNoId()) + 'Z' +
                                        'A' + String.valueOf(mDiabete.getNoId()) + 'Z');
                    } else {
                        mDiabete.setMade(false);
                        DiabeteLab.get(getActivity()).setCurrentDiabete(null);
                        BluetoothController.get(getActivity()).writeToDevice(
                                "stop" + "stop" + "stop");
                    }
                    mDiabeteMakeButton.setText(mDiabete.isMade() == false ? R.string.diabete_making_label : R.string.stop_making_label);


                    /**
                     * 重写PagerAdapter.getItemPosition()方法
                     *返回PagerAdapter.POSITION_NONE保证调用notifyDataSetChanged刷新Fragment。
                     */
                    ViewPager viewPager = getActivity().findViewById(R.id.diabete_view_pager);
                    viewPager.getAdapter().notifyDataSetChanged();
                }
            }
        }
    }
}
