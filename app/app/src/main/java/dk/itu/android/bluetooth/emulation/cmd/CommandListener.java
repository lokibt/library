package dk.itu.android.bluetooth.emulation.cmd;

public interface CommandListener {
    void onJoinReturned(String name);
    void onLeaveReturned();
}
