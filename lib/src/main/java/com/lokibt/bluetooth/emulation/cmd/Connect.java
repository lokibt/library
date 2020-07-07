package com.lokibt.bluetooth.emulation.cmd;

import com.lokibt.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;

public class Connect extends Command {

    private UUID uuid;
    private String address;

    public Connect(UUID uuid, BluetoothSocket btsocket) {
        super(CommandType.CONNECT, btsocket);
        this.address = btsocket.getRemoteDevice().getAddress();
        this.uuid = uuid;
    }

    @Override
    public void close() throws IOException {
        // the connection shall stay open
    }

    @Override
    protected void readResponse() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
        String line = br.readLine();
        if (line.equals("fail")) {
            throw new IOException("Error while opening Bluetooth socket for " + this.address + " " + this.uuid);
        }
    }

    @Override
    protected void sendParameters() throws IOException {
        sendParameter(this.address);
        sendParameter(this.uuid.toString());
    }
}
