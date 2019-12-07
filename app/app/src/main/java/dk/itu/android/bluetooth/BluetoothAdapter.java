package dk.itu.android.bluetooth;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;

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
	
	private static BluetoothAdapter defaultAdapter = null;

	public static boolean checkBluetoothAddress(String addr) {
		return android.bluetooth.BluetoothAdapter.checkBluetoothAddress(addr);
	}

	public static BluetoothAdapter getDefaultAdapter() {
		if (defaultAdapter == null) {
			defaultAdapter = new BluetoothAdapter();
		}
		return defaultAdapter;
	}

	private BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();

	public boolean cancelDiscovery() {
		return emulator.cancelDiscovery();
	}

	public boolean disable(){
		return emulator.disable();
	}

	public boolean enable(){
		return emulator.enable();
	}

	public String getAddress(){
		return emulator.getAddress();
	}

	public Set<BluetoothDevice> getBondedDevices(){
		return emulator.getBondedDevices();
	}

	public String getName(){
		return emulator.getName();
	}

	public int getScanMode(){
		return emulator.getScanMode();
	}

	public int getState(){
		return emulator.getState();
	}

	public boolean isDiscovering() {
		return emulator.isDiscovering();
	}

	public boolean isEnabled() {
		return emulator.isEnabled();
	}

	public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
		return new BluetoothServerSocket(uuid);
	}

	public boolean setName(String name) {
		return emulator.setName(name);
	}

	public boolean startDiscovery() {
		return emulator.startDiscovery();
	}

	public BluetoothDevice getRemoteDevice(String address) {
		return emulator.getRemoteDevice(address);
	}
}
