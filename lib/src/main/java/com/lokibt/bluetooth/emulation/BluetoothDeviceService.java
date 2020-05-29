package com.lokibt.bluetooth.emulation;

import android.os.Parcel;
import android.os.Parcelable;

public class BluetoothDeviceService implements Parcelable {

    public static Parcelable.Creator<BluetoothDeviceService> CREATOR = new Parcelable.Creator<BluetoothDeviceService>() {
        @Override
        public BluetoothDeviceService createFromParcel(Parcel source) {
            BluetoothDeviceService out = new BluetoothDeviceService();
            out.tcpPort = source.readInt();
            out.uuid = source.readString();
            return out;
        }

        @Override
        public BluetoothDeviceService[] newArray(int size) {
            return new BluetoothDeviceService[size];
        }
    };
    int tcpPort;
    String uuid;

    public BluetoothDeviceService() {
    }

    public BluetoothDeviceService(String uuid, int port) {
        this.uuid = uuid;
        this.tcpPort = port;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tcpPort);
        dest.writeString(uuid);
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
