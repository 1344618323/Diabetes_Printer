package com.example.lenovo.graduworkapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Lenovo on 2018/3/11.
 */

public class BluetoothController {
    private static BluetoothController sBluetoothController;
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private Set<BluetoothDevice> mBondedDevices;
    private final String BLUETOOTH_MODULE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private ConnectThread mConnectThread;
    private WriteReadThread writeReadThread;
    private boolean mIsConnecting;
    private Context mContext;

    private float mCurtemp;

    private OnTempChangeListener listener;

    public static BluetoothController get(Context context) {
        if (sBluetoothController == null) {
            sBluetoothController = new BluetoothController(context);
        }
        return sBluetoothController;
    }

    private BluetoothController(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context.getApplicationContext();
    }

    public void turnOn(Activity activity) {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
    }

    public void turnOff() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
            }
            mBluetoothAdapter.disable();
        }
    }

    public Set<BluetoothDevice> getBondedDevice() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() != false) {
            mBondedDevices = mBluetoothAdapter.getBondedDevices();
            return mBondedDevices;
        }
        return null;
    }

    public boolean isEnable() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() != false) {
            return true;
        }
        return false;
    }

    /**
     * 用于连接的内部类
     */
    public class ConnectThread extends Thread {
        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;
        private boolean isConnect;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket bluetoothSocket = null;
            mDevice = device;
            mIsConnecting = true;
            try {
                bluetoothSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_MODULE_UUID));
            } catch (IOException e) {
                mIsConnecting = false;
                e.printStackTrace();
            }
            mSocket = bluetoothSocket;
        }

        @Override
        public void run() {
            super.run();
            //取消搜索因为搜索会让连接变慢
            //mController.cancelDiscovery();
            try {
                //通过socket连接设备，这是一个阻塞操作，直到连接成功或发生异常
                mSocket.connect();
                writeReadThread = new WriteReadThread(mSocket);
                writeReadThread.start();
                isConnect = true;
                mIsConnecting = false;
            } catch (IOException e) {
                //无法连接，关闭socket并且退出
                try {
                    mSocket.close();
                    isConnect = false;
                    mIsConnecting = false;
                } catch (IOException e1) {
                    mIsConnecting = false;
                    e1.printStackTrace();
                }
            }
        }

        // 取消正在进行的链接，关闭socket
        public void cancel() {
            try {
                mSocket.close();
                isConnect = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public boolean isConnect() {
            return isConnect;
        }

    }

    /**
     * 连接成功后，用WriteReadThread收发数据
     */
    private class WriteReadThread extends Thread {
        private BluetoothSocket mSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public WriteReadThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream input = null;
            OutputStream output = null;
            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = input;
            this.outputStream = output;
        }

        public void run() {
            byte[] buff = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if (inputStream.available() == 0) {
                        continue;
                    } else {
                        sleep(150);
                    }
                    if ((bytes = inputStream.read(buff)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buff[i];
                        }
                        Message msg = new Message();
                        msg.obj = buf_data;
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(String msg) {

            //如果字符串长度小于等于100，就发一次
            if (msg.length() <= 100) {
                try {
                    byte[] bytes = msg.getBytes();
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                int scnt = (int) Math.ceil((double) (msg.length()) / 100);
                for (int cnt = 0; cnt < scnt; cnt++) {
                    String str;
                    if (cnt == 0) {
                        str = msg.substring(0, 100);
                        str = str + "ep" + scnt + "lp";

                    } else if (cnt == scnt - 1) {
                        String str2 = msg.substring(cnt * 100, msg.length());
                        str = "tp" + str2;
                    } else {
                        String str2 = msg.substring(cnt * 100, cnt * 100 + 100);
                        str = "tp" + str2;
                        str = str + "ep";
                    }
                    try {
                        byte[] bytes = str.getBytes();
                        outputStream.write(bytes);
                        sleep(150);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 取消正在进行的链接，关闭socket
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String str = new String((byte[]) msg.obj);

                    if (str.charAt(0) == 'T' && str.charAt(7) == 'Z') {
                        String str2 = str.substring(1, 7);
                        try {
                            mCurtemp = Float.parseFloat(str2);
                            tempChange(mCurtemp);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    };

    public void connect(BluetoothDevice device) {
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public void disconnect() {
        if (mConnectThread != null)
            mConnectThread.cancel();
    }

    public boolean isConnected() {
        if (mConnectThread != null) {
            return mConnectThread.isConnect();
        }
        return false;
    }

    public boolean isConnecting() {
        return mIsConnecting;
    }

    public void writeToDevice(String string) {
        if (writeReadThread != null) {
            writeReadThread.write(string);
        }
    }

    public interface OnTempChangeListener {
        public void onTempChange(float temp);

    }

    public void tempChange(float temp) {
        if (listener != null)
            listener.onTempChange(temp);
    }

    public void setOnTempChangeListener(OnTempChangeListener listener) {
        this.listener = listener;
    }
}
