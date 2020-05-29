package com.lokibt.bluetooth.emulation.cmd;

import com.lokibt.bluetooth.BluetoothDevice;

import java.util.Set;

public interface CommandListener {
    void onJoinReturned();

    void onLeaveReturned();

    void onDiscoveryReturned(Set<BluetoothDevice> devices);
}
