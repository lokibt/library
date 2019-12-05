package dk.itu.android.bluetooth;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import dk.itu.android.bluetooth.emulation.Emulator;
import dk.itu.android.bluetooth.emulation.cmd.Discovery;

public class BluetoothAdapter {
	public static final String ACTION_DISCOVERY_FINISHED = "dk.android.bluetooth.adapter.action.DISCOVERY_FINISHED";
	public static final String ACTION_DISCOVERY_STARTED = "dk.android.bluetooth.adapter.action.DISCOVERY_STARTED";
	public static final String ACTION_LOCAL_NAME_CHANGED = "dk.android.bluetooth.adapter.action.LOCAL_NAME_CHANGED";
	public static final String ACTION_REQUEST_DISCOVERABLE = "dk.android.bluetooth.adapter.action.REQUEST_DISCOVERABLE";
	public static final String ACTION_REQUEST_ENABLE = "dk.android.bluetooth.adapter.action.REQUEST_ENABLE";
	public static final String ACTION_SCAN_MODE_CHANGED = "dk.android.bluetooth.adapter.action.SCAN_MODE_CHANGED";
	public static final String ACTION_STATE_CHANGED = "dk.android.bluetooth.adapter.action.STATE_CHANGED";
	public static final int ERROR = -2147483648;
	public static final String EXTRA_DISCOVERABLE_DURATION = "dk.android.bluetooth.adapter.extra.DISCOVERABLE_DURATION";
	public static final String EXTRA_LOCAL_NAME = "dk.android.bluetooth.adapter.extra.LOCAL_NAME";
	public static final String EXTRA_PREVIOUS_SCAN_MODE = "dk.android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE";
	public static final String EXTRA_PREVIOUS_STATE = "dk.android.bluetooth.adapter.extra.PREVIOUS_STATE";
	public static final String EXTRA_SCAN_MODE = "dk.android.bluetooth.adapter.extra.SCAN_MODE";
	public static final String EXTRA_STATE = "dk.android.bluetooth.adapter.extra.STATE";

	public static final int SCAN_MODE_CONNECTABLE = 21;
	public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;
	public static final int SCAN_MODE_NONE = 20;
	public static final int STATE_OFF = 10;
	public static final int STATE_ON = 12;
	public static final int STATE_TURNING_OFF = 13;
	public static final int STATE_TURNING_ON = 11;
	
	private static final BluetoothAdapter defaultAdapter = new BluetoothAdapter();
	private static Context context = null;

	public static boolean checkBluetoothAddress(String addr) {
		return android.bluetooth.BluetoothAdapter.checkBluetoothAddress(addr);
	}
	public static BluetoothAdapter getDefaultAdapter() {
		return defaultAdapter;
	}

	public static void setContext(Context c) {
		if(context != null) return;
		context=c;
	}

	private Emulator emulator = Emulator.instance();
	private boolean discovering = false;
	private Set<BluetoothDevice> bonded = new HashSet<BluetoothDevice>();
	private int scanMode = android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;

	public boolean disable(){
		return emulator.disable();
	}

	public boolean enable(){
		return emulator.enable();
	}

	public String getAddress(){
		return emulator.getAddress();
	}

	public String getName(){
		return emulator.getName();
	}

	public boolean isEnabled() {
		return emulator.isEnabled();
	}

	public boolean setName(String name) {
		return emulator.setName(name);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean cancelDiscovery() {
		return false;
	}

	public Set<BluetoothDevice> getBondedDevices(){
		Set<BluetoothDevice> out = null;
		synchronized(this.bonded) {
			out = new HashSet<BluetoothDevice>(bonded);
		}
		return out;
	}

	public BluetoothDevice getRemoteDevice(String address) {
		if(!checkBluetoothAddress(address))
			throw new IllegalArgumentException("wrong device address");
		return emulator.lookupBT(address);//new BluetoothDevice(address);
	}
	public int getScanMode() {
		return scanMode;
	}
	public boolean isDiscovering() {
		return discovering;
	}
	public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid)
	throws IOException {
		
		//chhoose a random tcp port
		int port = choosePort();
		
		BluetoothServerSocket out = new BluetoothServerSocket(emulator,uuid.toString(),port);
		
//		emulator.addService(uuid.toString(), port);
		
		return out;
	}

	public boolean startDiscovery() {
		if(discovering)
			return false;
		
		discovering = true;
		Discovery.WithDevices wd = new Discovery.WithDevices() {
			@Override
			public void devices(List<BluetoothDevice> devices) {
				Intent intent = new Intent(ACTION_DISCOVERY_STARTED);
				context.sendBroadcast(intent);
				for(BluetoothDevice d : devices) {
					intent = new Intent();
					intent.setAction(BluetoothDevice.ACTION_FOUND);
					intent.putExtra(BluetoothDevice.EXTRA_DEVICE, d);
					intent.putExtra(BluetoothDevice.EXTRA_CLASS, d.getBluetoothClass());
					context.sendBroadcast(intent);
				}
				intent = new Intent(ACTION_DISCOVERY_FINISHED);
				context.sendBroadcast(intent);
				discovering = false;
			}
		};
		emulator.asyncDiscovery(wd);
		return true;
	}
	
	/////////////////////////////

	static int _curPort = 8123;
	private int choosePort() {
		_curPort++;
		return _curPort;
	}
}
