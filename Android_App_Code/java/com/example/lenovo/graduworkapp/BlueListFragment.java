package com.example.lenovo.graduworkapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by Lenovo on 2018/3/11.
 */

public class BlueListFragment extends DialogFragment {
    private RecyclerView mBlueRecyclerView;
    private Set<BluetoothDevice> mBondedDevice;
    private BlueDeviceAdapter mAdapter;
    private BluetoothStateListener mListener;
    private List<BluetoothDevice> mDeviceList;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BluetoothController.get(getActivity()).turnOn(getActivity());
        // 蓝牙开状态接收器
        mListener = new BluetoothStateListener();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        getActivity().registerReceiver(mListener, filter);
        getActivity().registerReceiver(mListener, filter2);


        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_bluetooth_list, null);
        mBlueRecyclerView = (RecyclerView) v.findViewById(R.id.bluelist_recycler_view);
        mBlueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return new AlertDialog.Builder(getActivity()).
                setView(v).
                create();
    }

    private class BlueDeviceHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private BluetoothDevice mDevice;
        private TextView mNameText;
        private TextView mAddressText;

        public BlueDeviceHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_bluetooth, parent, false));
            mNameText = (TextView) itemView.findViewById(R.id.blue_list_name);
            mAddressText = (TextView) itemView.findViewById(R.id.blue_list_address);
            itemView.setOnClickListener(this);
        }

        public void bind(BluetoothDevice device) {
            mDevice = device;
            mNameText.setText(mDevice.getName());
            mAddressText.setText(mDevice.getAddress());
        }

        @Override
        public void onClick(View view) {
            //连接设备
            if (mDevice.getName().equals("HCCXN") && BluetoothController.get(getActivity()).isConnecting() == false) {
                Toast.makeText(getActivity().getApplicationContext(), "start connect",
                        Toast.LENGTH_SHORT).show();
                BluetoothController.get(getActivity()).connect(mDevice);
            }
            //dismiss();
        }
    }

    private class BlueDeviceAdapter extends RecyclerView.Adapter<BlueDeviceHolder> {
        private List<BluetoothDevice> deviceList;

        public BlueDeviceAdapter(List<BluetoothDevice> list) {
            deviceList = list;
        }

        @Override
        public BlueDeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new BlueDeviceHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(BlueDeviceHolder holder, int position) {
            BluetoothDevice device = deviceList.get(position);
            holder.bind(device);
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }

    private void updateUI() {
        if (mAdapter == null) {
            mBondedDevice = BluetoothController.get(getActivity()).getBondedDevice();
            if (mBondedDevice == null)
                return;
            mDeviceList = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : mBondedDevice) {
                mDeviceList.add(device);
            }
            mAdapter = new BlueDeviceAdapter(mDeviceList);
            mBlueRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class BluetoothStateListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Toast.makeText(getActivity().getApplicationContext(), "connect successful",
                        Toast.LENGTH_SHORT).show();
                dismiss();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        updateUI();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mListener);
        super.onDestroy();
    }
}
