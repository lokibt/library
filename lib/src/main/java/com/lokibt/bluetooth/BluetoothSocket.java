package com.lokibt.bluetooth;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import com.lokibt.bluetooth.emulation.cmd.CommandCallback;
import com.lokibt.bluetooth.emulation.cmd.Connect;

public class BluetoothSocket implements CommandCallback {
    private static final String TAG = "BTSOCKET";

    private Socket socket;
    private UUID uuid;
    private BluetoothDevice remote;
    private ByteArrayOutputStream outBuffer;
    
    protected BluetoothSocket(BluetoothDevice device, UUID uuid) {
        this.socket = null;
        this.remote = device;
        this.uuid = uuid;
        this.outBuffer = null;
    }
    public BluetoothSocket(Socket socket, BluetoothDevice device, UUID uuid) {
        this.socket = socket;
        this.remote = device;
        this.uuid = uuid;
        this.outBuffer = null;
    }
    
    public void close() throws IOException {
        if(socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }

    public void connect() throws IOException {
        if(socket == null) {
            Log.d(TAG, "Connecting to " + this.uuid.toString() + " on " + this.remote.getAddress());
            Connect connectCmd = new Connect(this.uuid, this);
            this.socket = connectCmd.open();
            new Thread(connectCmd).start();
        }
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public BluetoothDevice getRemoteDevice() {
        return remote;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onFinish() {
        Log.d(TAG, "connected :)");
    }
}
