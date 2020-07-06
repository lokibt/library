package com.lokibt.bluetooth.emulation.cmd;

import android.os.Parcel;
import android.util.Log;

import com.lokibt.bluetooth.BluetoothDevice;
import com.lokibt.bluetooth.emulation.BluetoothAdapterEmulator;
import com.lokibt.bluetooth.emulation.BluetoothDeviceService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Discovery extends Command {
    private final static String TAG = "BTCMD_DISCOVERY";

    private boolean shallStop = false;

    public Set<BluetoothDevice> devices = new HashSet<BluetoothDevice>();

    public Discovery(CommandCallback callback) {
        super(CommandType.DISCOVERY, callback);
    }

    @Override
    protected void readResponse() throws IOException {
        BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.in));

        while (!shallStop) {
            if (this.in.available() > 0) {
                String line = br.readLine();
                Log.d(TAG, "line: " + line);
                String[] deviceInfo = line.trim().split(",");
                Log.d(TAG, "Discovered device: " + deviceInfo[0]);
                // Adding provided services
                ArrayList<BluetoothDeviceService> services = new ArrayList<BluetoothDeviceService>();
                for (int i = 1; i < deviceInfo.length; i++) {
                    Log.d(TAG, "...with service: " + deviceInfo[i]);
                    services.add(new BluetoothDeviceService(deviceInfo[i], 0)); // TODO Remove zero
                }
                Parcel device = Parcel.obtain();
                device.writeString(deviceInfo[0]);
                device.writeString("10.0.2.2"); // TODO remove me
                device.writeString(emulator.generateName(deviceInfo[0]));
                device.writeParcelableArray(services.toArray(new BluetoothDeviceService[]{}), 0);
                device.setDataPosition(0);
                emulator.addDiscoveredDevice(BluetoothDevice.CREATOR.createFromParcel(device));
            }
        }
    }

    public void stop() {
        this.shallStop = true;
    }

    @Override
    protected void sendParameters() throws IOException {
    }

}
