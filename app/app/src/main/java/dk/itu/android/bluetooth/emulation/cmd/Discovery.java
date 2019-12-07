package dk.itu.android.bluetooth.emulation.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.emulation.BluetoothDeviceService;

public class Discovery extends NoParamsBaseCommand {
	final static String TAG = "BTCMD_DISCOVERY";

	public Discovery() {
		super(CommandType.DISCOVERY);
	}

	@Override
	protected void readResponse(InputStream in) throws IOException {
		Set<BluetoothDevice> devices  = new HashSet<BluetoothDevice>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = br.readLine();
		while(line != null) {
			Log.d(TAG, "line: " + line);
			String[] deviceInfo = line.trim().split("--");
			Log.d(TAG, "deviceInfo: " + TextUtils.join(", ", deviceInfo));
			Parcel device = Parcel.obtain();
			device.writeString(deviceInfo[0]);
			device.writeString(deviceInfo[1]);
			device.writeString(emulator.generateName(deviceInfo[0]));
			// Adding provided services
			ArrayList<BluetoothDeviceService> services = new ArrayList<BluetoothDeviceService>();
			if (deviceInfo.length > 2) {
				String[] serviceInfoList = deviceInfo[2].split("<><>");
				for(int i=0; i<serviceInfoList.length; i++) {
					String[] serviceInfo = serviceInfoList[i].split("<>");
					Log.d(TAG, "serviceInfo: " + TextUtils.join(", ", serviceInfo));
					services.add(new BluetoothDeviceService(serviceInfo[0], Integer.parseInt(serviceInfo[1])));
				}
			}
			device.writeParcelableArray(services.toArray(new BluetoothDeviceService[]{}), 0);
			device.setDataPosition(0);
			devices.add(BluetoothDevice.CREATOR.createFromParcel(device));
			line = br.readLine();
		}
		emulator.onDiscoveryReturned(devices);
	}

	@Override
	protected void sendParameters(OutputStream out) throws IOException {
	}

}