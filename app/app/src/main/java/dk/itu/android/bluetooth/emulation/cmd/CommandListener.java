package dk.itu.android.bluetooth.emulation.cmd;

import java.util.Hashtable;

import dk.itu.android.bluetooth.BluetoothDevice;

public interface CommandListener {
    void onJoinReturned();
    void onLeaveReturned();
    void onDiscoveryReturned(Hashtable<String, BluetoothDevice> devices);
}
