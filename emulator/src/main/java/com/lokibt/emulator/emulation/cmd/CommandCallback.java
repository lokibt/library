package com.lokibt.emulator.emulation.cmd;

public interface CommandCallback {
    void onClose(Command cmd);
    void onFinish(Command cmd);
}
