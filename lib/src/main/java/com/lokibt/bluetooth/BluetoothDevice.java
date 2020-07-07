package com.lokibt.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class BluetoothDevice implements Parcelable {
    //constants
    public static final String ACTION_ACL_CONNECTED = "com.lokibt.bluetooth.device.action.ACL_CONNECTED";
    public static final String ACTION_ACL_DISCONNECGTED = "com.lokibt.bluetooth.device.action.ACL_DISCONNECTED";
    public static final String ACTION_ACL_DISCONNECT_REQUEST = "com.lokibt.bluetooth.device.action.ACL_DISCONNECT_REQUESTED";
    public static final String ACTION_BOND_STATE_CHANGED = "com.lokibt.bluetooth.device.action.BOND_STATE_CHANGED";
    public static final String ACTION_CLASS_CHANGED = "com.lokibt.bluetooth.device.action.CLASS_CHANGED";
    public static final String ACTION_FOUND = "com.lokibt.bluetooth.device.action.FOUND";
    public static final String ACTION_NAME_CHANGED = "com.lokibt.bluetooth.device.action.NAME_CHANGED";
    public static final String EXTRA_BOND_STATE = "com.lokibt.bluetooth.device.extra.BOND_STATE";
    public static final String EXTRA_CLASS = "com.lokibt.bluetooth.device.extra.CLASS";
    public static final String EXTRA_DEVICE = "com.lokibt.bluetooth.device.extra.DEVICE";
    public static final String EXTRA_NAME = "com.lokibt.bluetooth.device.extra.NAME";
    public static final String EXTRA_PREVIOUS_BOND_STATE = "com.lokibt.bluetooth.device.extra.PREVIOUS_BOND_STATE";
    public static final String EXTRA_RSSI = "com.lokibt.bluetooth.device.extra.RSSI";
    public static final int BOND_BONDED = 12;
    public static final int BOND_BONDING = 11;
    public static final int BOND_NONE = 10;
    public static final int ERROR = 0x80000000;
    private static final String TAG = "BTDEVICE";
    public static Parcelable.Creator<BluetoothDevice> CREATOR = new Parcelable.Creator<BluetoothDevice>() {
        @Override
        public BluetoothDevice createFromParcel(Parcel source) {
            BluetoothDevice device = new BluetoothDevice();
            device.addr = source.readString();
            device.tcpAddr = source.readString();
            device.name = source.readString();
            return device;
        }

        @Override
        public BluetoothDevice[] newArray(int size) {
            return new BluetoothDevice[size];
        }
    };
    private BluetoothClass btClass;
    private String addr;
    private String tcpAddr;
    private String name;

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(addr);
        out.writeString(tcpAddr);
        out.writeString(name);
    }

    public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) {
        return new BluetoothSocket(this, uuid);
    }

    public int describeContents() {
        return -1;
    }

    public boolean equals(Object o) {
        return false;
    }

    public String getAddress() {
        return addr;
    }

    public BluetoothClass getBluetoothClass() {
        return btClass;
    }

    public int getBondState() {
        return BOND_NONE;
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        return addr.hashCode();
    }

    public String toString() {
        return "BluetoothDevice " + addr + ", " + name + " -> " + tcpAddr;
    }
}
