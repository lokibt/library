package com.lokibt.bluetooth.emulation.cmd;

import android.os.Parcel;
import android.util.Log;

import com.lokibt.bluetooth.BluetoothDevice;
import com.lokibt.bluetooth.emulation.BluetoothAdapterEmulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class Discovery extends Command {
    private final static String TAG = "BTCMD_DISCOVERY";

    public Discovery(CommandCallback callback) {
        super(CommandType.DISCOVERY, callback);
    }

    @Override
    protected void readResponse() throws IOException {
        BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.in));

        try {
            while (true) {
                String line = br.readLine();
                Log.d(TAG, "line: " + line);
                String[] deviceInfo = line.trim().split(",");
                Log.d(TAG, "Discovered device: " + deviceInfo[0]);
                Parcel device = Parcel.obtain();
                device.writeString(deviceInfo[0]);
                device.writeString(emulator.generateName(deviceInfo[0]));
                device.setDataPosition(0);
                emulator.addDiscoveredDevice(BluetoothDevice.CREATOR.createFromParcel(device));
            }
        }
        catch (SocketException e) {
            String msg = e.getMessage();
            if (msg.equals("Socket closed"))
                Log.d(TAG, msg);
            else
                throw e;
        }
    }

    @Override
    public void close() throws IOException {
        // the connection will be closed via stop()
    }

    public void stop() throws IOException {
        super.close();
    }

    @Override
    protected void sendParameters() throws IOException {
    }

}
