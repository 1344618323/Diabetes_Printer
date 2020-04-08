package com.example.lenovo.graduworkapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lenovo on 2018/3/4.
 */

public class DiabeteListFragment extends Fragment {
    private RecyclerView mDiabeteRecyclerView;
    private DiabeteAdapter mAdapter;
    private Menu mMenu;
    private boolean mBluetoothConnect;
    private boolean mSubtitleShow;
    private static final String SAVED_SUBTITLE_SHOW = "subtitle";
    private static final String BLUE_LIST = "bluelist";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diabete_list, container, false);
        mDiabeteRecyclerView = (RecyclerView) view.findViewById(R.id.diabete_recycler_view);
        mDiabeteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleShow = savedInstanceState.getBoolean(SAVED_SUBTITLE_SHOW);
        }

        updateUI();

        return view;
    }

    private class DiabeteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Diabete mDiabete;
        private TextView mNameTextView;
        private TextView mMakingTextView;
        private ImageView mImageView;
        private int mWidthHeight;

        public DiabeteHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_diabete, parent, false));

            itemView.setOnClickListener(this);
            mNameTextView = (TextView) itemView.findViewById(R.id.diabete_list_name);
            mMakingTextView = (TextView) itemView.findViewById(R.id.diabete_list_making);
            mImageView = (ImageView) itemView.findViewById(R.id.diabete_list_photo);

            mWidthHeight = PictureUtils.dp2px(getActivity().getApplicationContext(), 80);
        }

        public void bind(Diabete diabete) {
            mDiabete = diabete;
            mNameTextView.setText(mDiabete.getName());
            mMakingTextView.setText(mDiabete.isMade() == true ? "Making" : "No Making");
            Bitmap bitmap;
            if (mDiabete.getDrawId() != 0) {
                bitmap = PictureUtils.getScaledBitmap(getActivity(), mDiabete.getDrawId(), mWidthHeight, mWidthHeight);
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageView.setImageBitmap(null);
            }
            //Log.d("PictureMeasurements", "the position is " + mWidthHeight);
        }

        @Override
        public void onClick(View view) {
            Intent intent = DiabetePagerActivity.newIntent(getActivity(), mDiabete.getId());
            startActivity(intent);
        }
    }

    private class DiabeteAdapter extends RecyclerView.Adapter<DiabeteHolder> {
        private List<Diabete> mDiabetes;

        public DiabeteAdapter(List<Diabete> diabetes) {
            mDiabetes = diabetes;
        }

        @Override
        public DiabeteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new DiabeteHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(DiabeteHolder holder, int position) {
            Diabete diabete = mDiabetes.get(position);
            holder.bind(diabete);
        }

        @Override
        public int getItemCount() {
            return mDiabetes.size();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Fragment.onCreateOptionsMenu()方法是由FragmentManager负责调用的。因此，在
         * activity接收到系统的onCreateOptionsMenu方法回调请求时，我们必须明确告诉FragmentManager：
         * 其管理的fragment应接受onCreateOptionsMenu方法的调用指令，需采用
         * public void setHasOptionsMenu(boolean hasMenu)
         **/
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_diabete_list, menu);

        mMenu = menu;
        MenuItem item;
        item = (MenuItem) menu.findItem(R.id.stop_making_diabete);
        item.setVisible(DiabeteLab.get(getActivity()).getCurrentDiabete() == null ? false : true);


        updateBluetoothTitle(menu.findItem(R.id.connect_bluetooth_device));

        item = (MenuItem) menu.findItem(R.id.show_subtitile);
        if (mSubtitleShow == false) {
            item.setTitle(R.string.show_subtitle);
        } else {
            item.setTitle(R.string.hide_subtitle);
        }
    }


    /**
     * 响应菜单项选择事件
     **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_making_diabete:
                //停止打印糖人
                item.setVisible(false);
                DiabeteLab.get(getActivity()).getCurrentDiabete().setMade(false);
                Diabete diabete = DiabeteLab.get(getActivity()).getCurrentDiabete();
                BluetoothController.get(getActivity()).writeToDevice(
                        "stop" + "stop" + "stop");
                DiabeteLab.get(getActivity()).setCurrentDiabete(null);
                updateUI();
                return true;
            case R.id.connect_bluetooth_device:
                mBluetoothConnect = BluetoothController.get(getActivity()).isEnable();
                if (!mBluetoothConnect) {
                    //没有连接的话请求开启蓝牙，打开对话框
                    FragmentManager fragmentManager = getFragmentManager();
                    BlueListFragment blueListFragmen = new BlueListFragment();
                    blueListFragmen.show(fragmentManager, BLUE_LIST);
                    updateBluetoothTitle(item);
                } else {
                    //由于在执行下面的updateBluetoothTitle(item)时，
                    // 可能没来得及关蓝牙，所以直接换
                    BluetoothController.get(getActivity()).turnOff();
                    item.setIcon(R.drawable.ic_start_leak);
                    item.setTitle(R.string.connect_device_label);
                }
                //updateBluetoothTitle(item);
                return true;
            case R.id.show_subtitile:
                mSubtitleShow = !mSubtitleShow;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            case R.id.diy_diabete_tool:
                Intent intent = new Intent(getActivity(), DoItYSActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        DiabeteLab diabeteLab = DiabeteLab.get(getActivity());
        List<Diabete> diabetes = diabeteLab.getDiabetes();
        if (mAdapter == null) {
            mAdapter = new DiabeteAdapter(diabetes);
            mDiabeteRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        //如果有糖人在打印，就显示操作栏中的停止图标
        if (mMenu != null) {
            if (DiabeteLab.get(getActivity()).getCurrentDiabete() != null) {
                mMenu.findItem(R.id.stop_making_diabete).setVisible(true);
            } else {
                mMenu.findItem(R.id.stop_making_diabete).setVisible(false);
            }
            updateBluetoothTitle(mMenu.findItem(R.id.connect_bluetooth_device));
        }

        updateSubtitle();
    }

    private void updateSubtitle() {
        String subtitle;
        if (DiabeteLab.get(getActivity()).getCurrentDiabete() != null) {
            subtitle = DiabeteLab.get(getActivity()).getCurrentDiabete().getName() + " is making";
        } else {
            subtitle = "nothing is making";
        }
        if (!mSubtitleShow)
            subtitle = null;
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateBluetoothTitle(MenuItem item) {
        mBluetoothConnect = BluetoothController.get(getActivity()).isEnable();
        if (mBluetoothConnect == true) {
            item.setIcon(R.drawable.ic_stop_leak);
            item.setTitle(R.string.disconnect_device_label);
        } else {
            item.setIcon(R.drawable.ic_start_leak);
            item.setTitle(R.string.connect_device_label);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(SAVED_BLUETOOTHCONNECT, mBluetoothConnect);
        outState.putBoolean(SAVED_SUBTITLE_SHOW, mSubtitleShow);
    }
}
