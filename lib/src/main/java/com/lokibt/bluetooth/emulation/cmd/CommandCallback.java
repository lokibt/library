package com.lokibt.bluetooth.emulation.cmd;

public interface CommandCallback {
    void onFinish(Command cmd);
    void onClose(Command cmd);
}
