package com.lokibt.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.lokibt.bluetooth.emulation.BluetoothAdapterEmulator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BluetoothAdapter {
    private static final String TAG = "BTADAPTER";

    public static final String ACTION_DISCOVERY_FINISHED = "com.lokibt.bluetooth.adapter.action.DISCOVERY_FINISHED";
    public static final String ACTION_DISCOVERY_STARTED = "com.lokibt.bluetooth.adapter.action.DISCOVERY_STARTED";
    public static final String ACTION_LOCAL_NAME_CHANGED = "com.lokibt.bluetooth.adapter.action.LOCAL_NAME_CHANGED";
    public static final String ACTION_REQUEST_DISCOVERABLE = "com.lokibt.bluetooth.adapter.action.REQUEST_DISCOVERABLE";
    public static final String ACTION_REQUEST_ENABLE = "com.lokibt.bluetooth.adapter.action.REQUEST_ENABLE";
    public static final String ACTION_SCAN_MODE_CHANGED = "com.lokibt.bluetooth.adapter.action.SCAN_MODE_CHANGED";
    public static final String ACTION_STATE_CHANGED = "com.lokibt.bluetooth.adapter.action.STATE_CHANGED";
    public static final int ERROR = -2147483648;
    public static final String EXTRA_DISCOVERABLE_DURATION = "com.lokibt.bluetooth.adapter.extra.DISCOVERABLE_DURATION";
    public static final String EXTRA_LOCAL_NAME = "com.lokibt.bluetooth.adapter.extra.LOCAL_NAME";
    public static final String EXTRA_PREVIOUS_SCAN_MODE = "com.lokibt.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE";
    public static final String EXTRA_PREVIOUS_STATE = "com.lokibt.bluetooth.adapter.extra.PREVIOUS_STATE";
    public static final String EXTRA_SCAN_MODE = "com.lokibt.bluetooth.adapter.extra.SCAN_MODE";
    public static final String EXTRA_STATE = "com.lokibt.bluetooth.adapter.extra.STATE";
    public static final String EXTRA_LOKIBT_HOST = "com.lokibt.bluetooth.adapter.extra.LOKIBT_HOST";
    public static final String EXTRA_LOKIBT_PORT = "com.lokibt.bluetooth.adapter.extra.LOKIBT_PORT";
    public static final String EXTRA_LOKIBT_GROUP = "com.lokibt.bluetooth.adapter.extra.LOKIBT_GROUP";
    public static final int SCAN_MODE_CONNECTABLE = 21;
    public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;
    public static final int SCAN_MODE_NONE = 20;
    public static final int STATE_OFF = 10;
    public static final int STATE_ON = 12;
    public static final int STATE_TURNING_OFF = 13;
    public static final int STATE_TURNING_ON = 11;

    private static BluetoothAdapter defaultAdapter = null;
    private BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();

    public static boolean checkBluetoothAddress(String addr) {
        return android.bluetooth.BluetoothAdapter.checkBluetoothAddress(addr);
    }

    public static BluetoothAdapter getDefaultAdapter() {
        if (defaultAdapter == null) {
            try {
                // Getting application context via reflection
                // https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
                Context context = (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
                defaultAdapter = new BluetoothAdapter(context);
            } catch (Exception e) {
                Log.e(TAG, "unable to get application context", e);
            }
        }
        return defaultAdapter;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            Log.d(TAG, "data received: " + bundle.getString("data"));
        }
    };

    private Messenger service = null;
    private Messenger client = new Messenger(handler);

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothAdapter.this.service = new Messenger(service);
            // send a message for testing
            Message msg = Message.obtain(handler);
            Bundle bundle = new Bundle();
            bundle.putString("data", "hello from library");
            msg.setData(bundle);
            msg.what = 1;
            msg.replyTo = client;
            try {
                BluetoothAdapter.this.service.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "unable to communicate with service", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            BluetoothAdapter.this.service = null;
        }
    };

    private BluetoothAdapter(Context context) {
        Intent intent = new Intent("LOKIBT_BIND_SERVICE");
        intent.setPackage("com.lokibt.companion");
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public boolean cancelDiscovery() {
        return emulator.cancelDiscovery();
    }

    public boolean disable() {
        return emulator.disable();
    }

    public boolean enable() {
        return emulator.enable();
    }

    public String getAddress() {
        return emulator.getAddress(true);
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return emulator.getBondedDevices();
    }

    public String getName() {
        return emulator.getName();
    }

    public int getScanMode() {
        return emulator.getScanMode();
    }

    public int getState() {
        return emulator.getState();
    }

    public boolean isDiscovering() {
        return emulator.isDiscovering();
    }

    public boolean isEnabled() {
        return emulator.isEnabled();
    }

    public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return emulator.listenUsingRfcommWithServiceRecord(name, uuid);
    }

    public boolean setName(String name) {
        return emulator.setName(name);
    }

    public boolean startDiscovery() {
        return emulator.startDiscovery();
    }

    public BluetoothDevice getRemoteDevice(String address) {
        return emulator.getRemoteDevice(address);
    }
}
