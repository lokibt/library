package com.lokibt.bluetooth.emulation;

import android.util.Log;

import com.lokibt.bluetooth.BluetoothDevice;
import com.lokibt.bluetooth.BluetoothSocket;
import com.lokibt.bluetooth.emulation.cmd.Announce;
import com.lokibt.bluetooth.emulation.cmd.Link;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class BluetoothServerSocketEmulator {
    private static final String TAG = "BTEMU_SERVERSOCKET";

    private static Set<Socket> openSockets = new HashSet<>();

    private Socket socket;
    private UUID uuid;

    static void closeAllOpenSockets() {
        for (Socket socket : BluetoothServerSocketEmulator.openSockets) {
            try {
                socket.close();
            }
            catch (IOException e) {
                Log.d(TAG, "Exception while closing open server socket", e);
            }
        }
        BluetoothServerSocketEmulator.openSockets.clear();
    }

    public BluetoothServerSocketEmulator(UUID uuid) {
        try {
            this.uuid = uuid;
            Announce addCmd = new Announce(this.uuid);
            this.socket = addCmd.open();
            BluetoothServerSocketEmulator.openSockets.add(this.socket);
            new Thread(addCmd).start();
        } catch (IOException e) {
            Log.e(TAG, "Cannot create Bluetooth server socket", e);
        }
    }

    public BluetoothSocket accept() throws IOException {
        Log.d(TAG, "Waiting for an incoming connection...");
        // blocks until a connection is established
        return this.waitForConnection();
    }

    public BluetoothSocket accept(int timeout) throws IOException {
        FutureTask<BluetoothSocket> future = new FutureTask<BluetoothSocket>(new Callable<BluetoothSocket>() {
            @Override
            public BluetoothSocket call() throws IOException {
                // blocks until a connection is established
                return BluetoothServerSocketEmulator.this.waitForConnection();
            }
        });
        try {
            Log.d(TAG, "Waiting for an incoming connection for " + timeout + " seconds...");
            Executors.newFixedThreadPool(1).execute(future);
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Timeout while waiting for incoming connections", e);
            throw new IOException("timed out");
        }
    }

    public void close() throws IOException {
        this.socket.close();
        BluetoothServerSocketEmulator.openSockets.remove(this.socket);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private BluetoothSocket waitForConnection() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String btaddr = br.readLine();
        String connId = br.readLine();
        if (btaddr == null || connId == null) {
            throw new IOException("\"LokiBT service closed announce connection\"");
        }
        Log.i(TAG, "Incoming connection from " + btaddr + ": " + connId);
        Link linkCmd = new Link(BluetoothAdapterEmulator.getInstance().getAddress(false), uuid, connId);
        Socket socket = linkCmd.open();
        new Thread(linkCmd).start();
        BluetoothDevice device = BluetoothAdapterEmulator.getInstance().getRemoteDevice(btaddr.trim());
        return new BluetoothSocket(socket, device, this.uuid);
    }
}
