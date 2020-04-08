package com.example.lenovo.graduworkapp;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class DoItYSFragment extends Fragment {
    private TextView mTextView;
    private Button mSendButton;
    private Button mStopButton;
    private DIYDrawingView mDIYDrawingView;
    private Point mViewWH;
    private ArrayList<Point> mDrawList;
    private TextView mTempTextView;
    private SeekBar mTempSeekBar;
    private BluetoothController mController;

    public static DoItYSFragment newInstance() {
        return new DoItYSFragment();
    }

    public DoItYSFragment() {
        mDrawList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diy_diabete, container, false);
        mTextView = (TextView) v.findViewById(R.id.diy_textView);
        mSendButton = (Button) v.findViewById(R.id.diy_button);

        if (BluetoothController.get(getActivity()).isConnected()) {
            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //发送路径
                    String sendPath = "path" + mDrawList.size();
                    if (mDrawList.size() > 0) {
                        for (Point point : mDrawList) {
                            sendPath = sendPath + "x" + point.x + "y" + point.y;
                        }
                        sendPath = sendPath + "end";
                        DiabeteLab.get(getActivity()).setCurrentDiabeteDIYID();
                        BluetoothController.get(getActivity()).writeToDevice(sendPath);
                        mStopButton.setEnabled(true);
                        mStopButton.setOnClickListener(new StopOnClickListener(mStopButton));
                    }
                }
            });
        } else {
            mSendButton.setEnabled(false);
        }

        mStopButton = (Button) v.findViewById(R.id.stop_draw_button);

        if (BluetoothController.get(getActivity()).isConnected()
                && DiabeteLab.get(getActivity()).getCurrentDiabete() != null) {
            mStopButton.setOnClickListener(new StopOnClickListener(mStopButton));
        } else {
            mStopButton.setEnabled(false);
        }

        mDIYDrawingView = (DIYDrawingView) v.findViewById(R.id.DIYDrawingView);

        ViewTreeObserver observer = mDIYDrawingView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mDIYDrawingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mDIYDrawingView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                    mViewWH = new Point(mDIYDrawingView.getMeasuredWidth(), mDIYDrawingView.getMeasuredHeight());
                }
            });
        }
        mDIYDrawingView.setOnCxnListener(new DIYDrawingView.OnDrawListener() {
            @Override
            public void onDraw(DIYDrawingView view, int linesum) {
                float radio = Math.min(150.0f / mViewWH.x, 200.0f / mViewWH.y);
                Point point = new Point((int) (view.getCurrentPoint().x * radio), (int) (view.getCurrentPoint().y * radio));

                collectPoint(point, linesum);

                String str = mDrawList.size() >= 50 ? " Too long to draw!" : "";
                mTextView.setText("CurrentPoint:(" + String.valueOf(point.x) + "," + String.valueOf(point.y) +
                        "), PointSum:" + String.valueOf(mDrawList.size()) + str);
            }
        });


        mTempTextView = (TextView) v.findViewById(R.id.diy_temp_textView);
        mTempSeekBar = (SeekBar) v.findViewById(R.id.diy_seekBar);
        mTempSeekBar.setEnabled(false);
        mController = BluetoothController.get(getActivity());
        mController.setOnTempChangeListener(new BluetoothController.OnTempChangeListener() {
            @Override
            public void onTempChange(float temp) {
                mTempSeekBar.setProgress((int)temp);
                String str = Float.toString(temp);
                mTempTextView.setText(str);
            }
        });

        return v;
    }

    private void collectPoint(Point point, int linesum) {
        if (linesum == 1) {
            mDrawList.clear();
        }
        if (mDrawList.size() >= 50)
            return;
        if (mDrawList.size() == 0)
            mDrawList.add(point);
        else if ((Math.pow(point.x - mDrawList.get(mDrawList.size() - 1).x, 2) +
                Math.pow(point.y - mDrawList.get(mDrawList.size() - 1).y, 2) >= 100)
                && (point.x <= 150 && point.x >= 0 && point.y <= 200 && point.y >= 0)) {
            mDrawList.add(point);
        }
    }

    private class StopOnClickListener implements View.OnClickListener {
        private Button mButton;

        public StopOnClickListener(Button button) {
            mButton = button;
        }

        public void onClick(View v) {
            String stopDiy = "stopstopstop";
            DiabeteLab.get(getActivity()).getCurrentDiabete().setMade(false);
            DiabeteLab.get(getActivity()).setCurrentDiabete(null);
            BluetoothController.get(getActivity()).writeToDevice(stopDiy);
            mButton.setEnabled(false);
        }
    }
}