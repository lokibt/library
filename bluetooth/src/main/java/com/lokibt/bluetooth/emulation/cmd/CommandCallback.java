package com.lokibt.bluetooth.emulation.cmd;

public interface CommandCallback {
    void onClose(Command cmd);
    void onFinish(Command cmd);
}
