package dk.itu.android.bluetooth.emulation.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import dk.itu.android.bluetooth.BluetoothDevice;

public class Discovery extends NoParamsBaseCommand {
	final static String TAG = "BTCMD_DISCOVERY";

	public Discovery() {
		super(CommandType.DISCOVERY);
	}
	
//	@Override
//	protected void readResponse(InputStream in) throws IOException {
//		String all = readAll(in);
//		Log.i("DISCOVERY_CMD", "all response: "+all);
//		for(String line : all.split("\r\n")) {
//			String[] parts = line.trim().split("|");
//			dk.itu.android.bluetooth.BluetoothDevice d = new BluetoothDevice(parts[0], parts[1]);
//			
//			if(parts[3].length()>0) {
//				String[] sParts = parts[3].split("<>");
//				for(String p : sParts) {
//					String[] s = p.split("|");
//					d.addService(s[0], Integer.parseInt(s[1]));
//				}
//			}
//			devices.add(d);
//		}
//		if(withDevices != null) {
//			withDevices.devices(devices);
//		}
//
//	}

	@Override
	protected void readResponse(InputStream in) throws IOException {
		ArrayList<BluetoothDevice> devices = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = br.readLine();
		while(line != null) {
			Log.d(TAG, "line: " + line);
			String[] deviceInfo = line.trim().split("--");
			Log.d(TAG, "deviceInfo: " + TextUtils.join(", ", deviceInfo));
			BluetoothDevice device = new BluetoothDevice(deviceInfo[0], deviceInfo[1], emulator.generateName(deviceInfo[0]));
			// Adding provided services
			if (deviceInfo.length > 2) {
				String[] serviceInfoList = deviceInfo[2].split("<><>");
				for(int i=0; i<serviceInfoList.length; i++) {
					String[] serviceInfo = serviceInfoList[i].split("<>");
					Log.d(TAG, "serviceInfo: " + TextUtils.join(", ", serviceInfo));
					device.addService(serviceInfo[0], Integer.parseInt(serviceInfo[1]));
				}
			}
			devices.add(device);
			line = br.readLine();
		}
		emulator.onDiscoveryReturned(devices);
	}

	@Override
	protected void sendParameters(OutputStream out) throws IOException {
	}

}