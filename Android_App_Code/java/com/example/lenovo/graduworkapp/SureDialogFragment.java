package com.example.lenovo.graduworkapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by Lenovo on 2018/3/6.
 */

/**
 * 建议将AlertDialog封装在DialogFragment实例中使用，当然不这样做也可以，但是不推荐，
 * 使用FragmentManager管理对话框，可以更灵活地显示对话框。
 * 另外，如果设备旋转，单独使用AlertDialog会消失，而封装在fragment中的AlertDialog不会
 * 出现该问题（旋转后，对话框会重建恢复）。
 */

public class SureDialogFragment extends DialogFragment {
    private static final String ARG_MAKINGORNO = "makingorno";
    public static final String EXTRA_MAKINGORNO = "com.example.lenovo.graduworkapp.isMakingDiabeteOrNo";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean makingOrNo = (boolean) getArguments().getSerializable(ARG_MAKINGORNO);
        return new AlertDialog.Builder(getActivity()).
                setTitle(makingOrNo == false ? R.string.sure_making_diabete : R.string.sure_stopping_diabete).
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_OK, true);
                    }
                }).
                setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED, false);
                    }
                }).
                create();

    }

    /**
     * DiabeteFragment通过调用MakingDiabeteFragment.newInstance()方法，将数据传递到对话框中
     */
    public static SureDialogFragment newInstance(boolean makingOrNo) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_MAKINGORNO, makingOrNo);
        SureDialogFragment makingDiabeteFragment = new SureDialogFragment();
        makingDiabeteFragment.setArguments(args);
        return makingDiabeteFragment;
    }

    private void sendResult(int resultCode, boolean makingOrNo) {
        if (getTargetFragment() == null) {
            return;
        } else {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_MAKINGORNO, makingOrNo);
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        }
    }
}
