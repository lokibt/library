package com.lokibt.bluetooth.emulation.cmd;

import java.util.Set;

import com.lokibt.bluetooth.BluetoothDevice;

public interface CommandListener {
    void onJoinReturned();
    void onLeaveReturned();
    void onDiscoveryReturned(Set<BluetoothDevice> devices);
}
