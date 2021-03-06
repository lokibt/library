package com.lokibt.bluetooth;

import com.lokibt.bluetooth.emulation.BluetoothAdapterEmulator;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BluetoothAdapter {
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
            defaultAdapter = new BluetoothAdapter();
        }
        return defaultAdapter;
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
