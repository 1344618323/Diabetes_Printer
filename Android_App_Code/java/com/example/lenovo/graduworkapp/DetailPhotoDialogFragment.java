package com.example.lenovo.graduworkapp;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Lenovo on 2018/3/11.
 */

public class DetailPhotoDialogFragment extends DialogFragment {
    public static final String EXTRA_PHOTO_ID= "com.example.lenovo.graduworkapp.photoid";
    private ImageView mImageView;
    private int mDrawId;

    public static DetailPhotoDialogFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PHOTO_ID, id);
        DetailPhotoDialogFragment fragment = new DetailPhotoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDrawId = (int) getArguments().getSerializable(EXTRA_PHOTO_ID);
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_photo, null);
        mImageView = (ImageView) v.findViewById(R.id.dialog_photo_fragment);
        Bitmap bitmap = PictureUtils.getScaledBitmap(getActivity(),mDrawId);
        mImageView.setImageBitmap(bitmap);
        return new AlertDialog.Builder(getActivity()).
                setView(v).
                create();
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }
}
