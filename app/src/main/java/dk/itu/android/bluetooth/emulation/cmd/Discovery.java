package dk.itu.android.bluetooth.emulation.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;
import android.util.Log;

import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;
import dk.itu.android.bluetooth.emulation.BluetoothDeviceService;

public class Discovery extends Command {
    final static String TAG = "BTCMD_DISCOVERY";

    public Discovery() {
        super(CommandType.DISCOVERY,null);
    }

    @Override
    protected void readResponse() throws IOException {
        BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();
        Set<BluetoothDevice> devices  = new HashSet<BluetoothDevice>();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
        String line = br.readLine();
        while(line != null) {
            Log.d(TAG, "line: " + line);
            String[] deviceInfo = line.trim().split(",");
            Log.d(TAG, "Add device: " + deviceInfo[0]);
            Parcel device = Parcel.obtain();
            device.writeString(deviceInfo[0]);
            device.writeString("10.0.2.2"); // TODO remove me
            device.writeString(emulator.generateName(deviceInfo[0]));
            // Adding provided services
            ArrayList<BluetoothDeviceService> services = new ArrayList<BluetoothDeviceService>();
                for(int i=1; i<deviceInfo.length; i++) {
                    Log.d(TAG, "Add service: " + deviceInfo[i]);
                    services.add(new BluetoothDeviceService(deviceInfo[i], 0)); // TODO Remove zero
                }
            device.writeParcelableArray(services.toArray(new BluetoothDeviceService[]{}), 0);
            device.setDataPosition(0);
            devices.add(BluetoothDevice.CREATOR.createFromParcel(device));
            line = br.readLine();
        }
        emulator.onDiscoveryReturned(devices);
    }

    @Override
    protected void sendParameters() throws IOException {
    }

}