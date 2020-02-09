package dk.itu.android.bluetooth.emulation.cmd;

import java.util.Set;

import dk.itu.android.bluetooth.BluetoothDevice;

public interface CommandListener {
    void onJoinReturned();
    void onLeaveReturned();
    void onDiscoveryReturned(Set<BluetoothDevice> devices);
}
