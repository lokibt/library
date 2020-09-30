package com.lokibt.bluetooth;

import android.util.Log;

import com.lokibt.bluetooth.emulation.BluetoothServerSocketEmulator;
import com.lokibt.bluetooth.emulation.cmd.Command;
import com.lokibt.bluetooth.emulation.cmd.CommandCallback;
import com.lokibt.bluetooth.emulation.cmd.Connect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BluetoothSocket implements CommandCallback {
    private static final String TAG = "BTSOCKET";

    private static Set<Socket> openSockets = new HashSet<>();

    private Socket socket;
    private UUID uuid;
    private BluetoothDevice remote;
    private ByteArrayOutputStream outBuffer;

    public static void closeAllOpenSockets() {
        for (Socket socket : BluetoothSocket.openSockets) {
            try {
                socket.close();
            }
            catch (IOException e) {
                Log.d(TAG, "Exception while closing open socket", e);
            }
        }
        BluetoothSocket.openSockets.clear();
    }

    protected BluetoothSocket(BluetoothDevice device, UUID uuid) {
        this.socket = null;
        this.remote = device;
        this.uuid = uuid;
        this.outBuffer = null;
    }

    public BluetoothSocket(Socket socket, BluetoothDevice device, UUID uuid) {
        this.socket = socket;
        BluetoothSocket.openSockets.add(this.socket);
        this.remote = device;
        this.uuid = uuid;
        this.outBuffer = null;
    }

    public void close() throws IOException {
        this.socket.close();
        BluetoothSocket.openSockets.remove(this.socket);
    }

    public void connect() throws IOException {
        Log.d(TAG, "Connecting to " + this.uuid.toString() + " on " + this.remote.getAddress());
        Connect connectCmd = new Connect(this.uuid, this);
        this.socket = connectCmd.open();
        BluetoothSocket.openSockets.add(this.socket);
        connectCmd.execute();
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

    public void onClose(Command cmd) {
    }

    public Socket _getRawSocket() throws IOException {
        return socket;
    }

    @Override
    public void onFinish(Command cmd) {
        Log.d(TAG, "connected :)");
    }
}
