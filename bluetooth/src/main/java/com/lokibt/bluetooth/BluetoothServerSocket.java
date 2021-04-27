package com.lokibt.bluetooth;

import com.lokibt.bluetooth.emulation.BluetoothServerSocketEmulator;

import java.io.IOException;
import java.util.UUID;

public class BluetoothServerSocket {
    private BluetoothServerSocketEmulator emulator;

    public BluetoothServerSocket(UUID uuid) {
        this.emulator = new BluetoothServerSocketEmulator(uuid);
    }

    public BluetoothSocket accept() throws IOException {
        return emulator.accept();
    }

    public BluetoothSocket accept(int timeout) throws IOException {
        return emulator.accept(timeout);
    }

    public void close() throws IOException {
        emulator.close();
    }
}
