package dk.itu.android.bluetooth.emulation.cmd;

import java.util.ArrayList;

import dk.itu.android.bluetooth.BluetoothDevice;

public interface CommandListener {
    void onJoinReturned();
    void onLeaveReturned();
    void onDiscoveryReturned(ArrayList<BluetoothDevice> devices);
}
