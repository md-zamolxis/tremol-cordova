/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tremol.androidFP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.tremol.zfplibj.FPLogger;
import com.tremol.zfplibj.ZFPLib;
import com.tremol.zfplibj.ZFPPortBlueTooth;
import com.tremol.zfplibj.ZFPPortUSB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BtService
{

    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    private static final String NAME = "BtService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//("fa87c0d0-afac-11de-8a39-0800200c9a66")

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public ZFPLib zfplib = null;
    private Resources mRes = null;

    public BtService(Context context, Handler handler)
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mRes = context.getResources();
    }

    private synchronized void setState(int state)
    {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(MainAct.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    public synchronized int getState()
    {
        return mState;
    }

    public synchronized void connect(BluetoothDevice device)
    {
        if (D)
            Log.d(TAG, "connect to: " + device);
        if (getState() == STATE_CONNECTED && mConnectThread != null)
            mConnectThread.cancel();
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
    {
        if (D)
            Log.d(TAG, "connected");
        //if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}                       
        Message msg = mHandler.obtainMessage(MainAct.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainAct.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
        zfplib = getLib(socket);
    }

    public synchronized void connectUsb(UsbDevice device, UsbDeviceConnection connection)
    {
        if (D)
            Log.d(TAG, "connect to: " + device.getDeviceName());
        if (getState() == STATE_CONNECTED && mConnectThread != null)
            mConnectThread.cancel();
        setState(STATE_CONNECTING);

        Message msg = mHandler.obtainMessage(MainAct.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        String name = DeviceListActivity.getUsbDeviceName(device, connection);
        if(TextUtils.isEmpty(name))
        {
            bundle.putString(MainAct.DEVICE_NAME, device.getDeviceName());
        }
        else
        {
            bundle.putString(MainAct.DEVICE_NAME, name);
        }
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
        zfplib = getLibUsb(device, connection);
    }



    public synchronized void stop()
    {
        if (D)
            Log.d(TAG, "stop");
        if (mConnectThread != null)
            mConnectThread.cancel();
        //mConnectThread = null;
        setState(STATE_NONE);
    }

    public synchronized void start()
    {
        setState(STATE_LISTEN);
    }

    private void connectionFailed()
    {
        setState(STATE_LISTEN);
        Message msg = mHandler.obtainMessage(MainAct.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainAct.TOAST, mRes.getString(R.string.unable_to_connect));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost()
    {
        setState(STATE_LISTEN);
        Message msg = mHandler.obtainMessage(MainAct.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainAct.TOAST, mRes.getString(R.string.dev_conn_lost));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }


    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device)
        {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try
            {
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID); // Get a BluetoothSocket for a connection with the given BluetoothDevice 
                //Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                //tmp = (BluetoothSocket) m.invoke(device, 1);  //Integer.valueOf(1)
                try
                {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
                    {
                        tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    }
                    else
                        throw e;
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "ConnectThrea--createRfcommSocketToServiceRecord() failed", e);
            }

            mmSocket = tmp;
        }

        public void run()
        {
            Log.i(TAG, "_ConnectThread.run()");
            setName("ConnectThread");
            mAdapter.cancelDiscovery();
            try
            {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
            }
            catch (IOException e)
            {
                Log.e(TAG, "_ConnectThread.mmSocket.connect()", e);
                connectionFailed();
                try
                {
                    mmSocket.close();
                }
                catch (IOException e2)
                {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                BtService.this.start();
                return;
            }
            // synchronized (BluetoothChatService.this) {
            //     mConnectThread = null;
            // }
            connected(mmSocket, mmDevice);
        }

        public void cancel()
        {
            try
            {
                mmSocket.getInputStream().close();
                mmSocket.getOutputStream().close();
                if (zfplib != null)
                    zfplib.close();
                zfplib = null;
                mmSocket.close();

            }
            catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    private ZFPLib getLibUsb(UsbDevice device, UsbDeviceConnection connection)
    {

        FPLogger logger = new FPLogger()
        {
            @Override
            public void Log(String s, boolean to_fp)
            {
                if (to_fp)
                    Log.i("TO_FP", "->:" + s);
                else
                    Log.i("FROM_FP", "<-:" + s);
            }
        };
        ZFPPortUSB m_port = new ZFPPortUSB(ZFPPortUSB.CHIP_FTDI, device, connection);

        try
        {
            m_port.setBaudrate(921600);
            m_port.open();
        }
        catch (Exception ignored2) {

            try
            {
                m_port.close();
                m_port.setBaudrate(115200);
                m_port.open();
            }
            catch (Exception e) { return null; }
        }
        ZFPLib lib = new ZFPLib(m_port, mRes.getString(R.string.charset), logger);
        try
        {
            String ver = lib.getVersion();
            ZFPLib newLib = ZfpHelper.getSpecificLib(ver, m_port, logger);
            if (MainAct.APPMODE == MainAct.APPMODE_SERVICE)
                newLib = ZfpHelper.getSpecificLib("BG", m_port, logger);
            if (newLib != null)
                return newLib;
        }
        catch (Exception e)
        {
            Log.e(TAG, "getVer", e);
        }
        return lib;

    }

    private ZFPLib getLib(BluetoothSocket socket)
    {

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try
        {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        }
        catch (IOException e)
        {
            Log.e(TAG, "temp sockets not created", e);
        }
        final InputStream  mmInStream = tmpIn;
        final OutputStream  mmOutStream = tmpOut;
        FPLogger logger = new FPLogger()
        {
            @Override
            public void Log(String s, boolean to_fp)
            {
                if (to_fp)
                    Log.i("TO_FP", "->:" + s);
                else
                    Log.i("FROM_FP", "<-:" + s);
            }
        };
        ZFPPortBlueTooth m_port = new ZFPPortBlueTooth(mmOutStream, mmInStream);
        ZFPLib lib = new ZFPLib(m_port, mRes.getString(R.string.charset), logger);
        try
        {
            String ver = lib.getVersion();
            ZFPLib newLib = ZfpHelper.getSpecificLib(ver, m_port, logger);
            if(MainAct.APPMODE == MainAct.APPMODE_SERVICE)
                newLib = ZfpHelper.getSpecificLib("BG", m_port, logger);
            if(newLib != null)
                return newLib;
        }
        catch (Exception e)
        {
            Log.e(TAG, "getVer", e);
        }
        return lib;
    }
}