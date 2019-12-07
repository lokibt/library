package dk.itu.android.bluetooth.emulation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import dk.itu.android.bluetooth.BluetoothAdapter;
import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.emulation.cmd.Discovery;
import dk.itu.android.bluetooth.emulation.cmd.Join;
import dk.itu.android.bluetooth.emulation.cmd.Leave;
import dk.itu.android.bluetooth.emulation.cmd.CommandListener;

public class BluetoothAdapterEmulator implements CommandListener {
	private static final String TAG = "BTEMULATOR";

	private static BluetoothAdapterEmulator instance = null;

	public static BluetoothAdapterEmulator getInstance() {
		if (instance == null) {
			try {
				// Getting application context via reflection
				// https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
				Context context = (Context) Class.forName("android.app.ActivityThread")
						.getMethod("currentApplication").invoke(null, (Object[]) null);
				instance = new BluetoothAdapterEmulator(context);
			} catch (Exception e) {
				Log.e(TAG, "unable to get application context", e);
			}
		}
		return instance;
	}

	private String address;
	private Context context;
	private Activity ctrlActivity = null;
	private Set<BluetoothDevice> discoveredDevices;
	private boolean discovering = false;
	private String name;
	private int state = BluetoothAdapter.STATE_OFF;
	private Set<BluetoothDevice> bondedDevices;
	private int scanMode = BluetoothAdapter.SCAN_MODE_NONE;

	private BluetoothAdapterEmulator(Context context) {
		this.context = context;
		this.bondedDevices = new HashSet<BluetoothDevice>();
		this.discoveredDevices = new HashSet<BluetoothDevice>();
		// Generating a name will also set the address
		this.name = generateName();
	}

	public void addBondedDevice(BluetoothDevice device) {
		this.bondedDevices.add(device);
	}

	public boolean cancelDiscovery() {
		if (!isEnabled()) {
			return false;
		}
		// TODO: Actually cancel the discovery thread
		setDiscovering(false);
		return true;
	}

	public boolean disable() {
		if (this.state == BluetoothAdapter.STATE_OFF || this.state == BluetoothAdapter.STATE_TURNING_OFF) {
			return false;
		}
		setState(BluetoothAdapter.STATE_TURNING_OFF);
		if (getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			stopDiscoverable();
		} else {
			onLeaveReturned();
		}
		return true;
	}

	public boolean enable() {
		if (this.state == BluetoothAdapter.STATE_ON || this.state == BluetoothAdapter.STATE_TURNING_OFF) {
			return false;
		}
		setState(BluetoothAdapter.STATE_TURNING_ON);
		setState(BluetoothAdapter.STATE_ON);
		setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
		return true;
	}

	public String generateName(String address) {
		return "emulator-" + address.replace(":", "");
	}

	public String getAddress() {
		if (this.address == null) {
			try {
				FileInputStream fis = this.context.openFileInput("BTADDR.TXT");
				Log.d(TAG, "reading Bluetooth address");
				byte[] buf = new byte[100];
				int read = fis.read(buf);
				this.address = new String(buf, 0, read);
			} catch (Exception readException) {
				Log.d(TAG, "error while reading Bluetooth address", readException);
				try {
					Log.d(TAG, "saving Bluetooth address");
					String addr = generateAddress();
					FileOutputStream fos = this.context.openFileOutput("BTADDR.TXT", Context.MODE_PRIVATE);
					OutputStreamWriter outw = new OutputStreamWriter(fos);
					outw.write(addr);
					outw.flush();
					outw.close();
					this.address = addr;
				} catch (Exception writeException) {
					Log.e(TAG, "error while writing Bluetooth address", writeException);
				}
			}
			Log.d(TAG, "Bluetooth address is: " + this.address);
		}
		return this.address;
	}


	public Set<BluetoothDevice> getBondedDevices() {
		return this.bondedDevices;
	}

	public String getName() {
		return this.name;
	}

	public BluetoothDevice getRemoteDevice(String address) {
		if (!BluetoothAdapter.checkBluetoothAddress(address)) {
			throw new IllegalArgumentException("wrong device address");
		}
		BluetoothDevice device = null;
		for (BluetoothDevice d : discoveredDevices) {
			if (d.getAddress().equals(address)) {
				device = d;
				break;
			}
		}
		if (device == null) {
			Log.e(TAG, "Device address not found: " + address);
			Log.i(TAG, "TODO Create a device anyway");
		}
		Log.d(TAG, "Returning Bluetooth device: " + device);
		return device;
	}

	public int getScanMode() {
		return scanMode;
	}

	public int getState() {
		return this.state;
	}

	public boolean isDiscovering() {
		return this.discovering;
	}

	public boolean isEnabled() {
		return this.state == BluetoothAdapter.STATE_ON;
	}

	public void setActivity(Activity ctrlActivity) {
		Log.d(TAG, "Setting controller activity " + ctrlActivity);
		this.ctrlActivity = ctrlActivity;
	}

	public boolean setName(String name) {
		if (!isEnabled()) {
			return false;
		}
		if (!this.name.equals(name)) {
			this.name = name;
			Bundle extras = new Bundle();
			extras.putString(BluetoothAdapter.EXTRA_LOCAL_NAME, name);
			sendBroadcast(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED, extras);
		}
		return true;
	}

	public void setState(int state) {
		if (this.state != state) {
			Bundle extras = new Bundle();
			extras.putInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE, this.state);
			extras.putInt(BluetoothAdapter.EXTRA_STATE, state);
			this.state = state;
			sendBroadcast(BluetoothAdapter.ACTION_STATE_CHANGED, extras);
		}
	}

	public void startDiscoverable() {
		if (!isEnabled()) {
			enable();
		}
		try {
			new Thread(new Join()).start();
		} catch (Exception e) {
			Log.e(TAG, "Cannot start Join() thread", e);
		}
	}

	public boolean startDiscovery() {
		try {
			setDiscovering(true);
			new Thread(new Discovery()).start();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Cannot start Discovery() thread", e);
			return false;
		}
	}

	public void stopDiscoverable() {
		try {
			new Thread(new Leave()).start();
		} catch (Exception e) {
			Log.e(TAG, "Cannot start Leave() thread", e);
		}
	}

	@Override
	public void onJoinReturned() {
		if (isEnabled()) {
			setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
			sendResult(Activity.RESULT_OK);
		} else {
			setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
		}
	}

	@Override
	public void onLeaveReturned() {
		if (this.state == BluetoothAdapter.STATE_TURNING_OFF || this.state == BluetoothAdapter.STATE_OFF) {
			setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
			setState(BluetoothAdapter.STATE_OFF);
		} else {
			setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
		}
	}

	@Override
	public void onDiscoveryReturned(Set<BluetoothDevice> devices) {
		for (BluetoothDevice d : devices) {
			Log.d(TAG, "Discovered device: " + d);
			Bundle extras = new Bundle();
			extras.putParcelable(BluetoothDevice.EXTRA_DEVICE, d);
			sendBroadcast(BluetoothDevice.ACTION_FOUND, extras);
		}
		this.discoveredDevices = devices;
		setDiscovering(false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private String generateAddress() {
		Log.d(TAG, "generating Bluetooth address");
		//sample: 00:11:22:AA:BB:CC
		String chars = "ABCDEF";
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		boolean f = true;
		for (int i = 0; i < 3; i++) {
			if (f) {
				f = !f;
			} else {
				sb.append(":");
			}
			int n = 0;
			do {
				n = r.nextInt(99);
			} while (n < 10);
			sb.append(n);
		}
		for (int i = 0; i < 3; i++) {
			sb.append(":");
			sb.append(chars.charAt(r.nextInt(6)));
			sb.append(chars.charAt(r.nextInt(6)));
		}
		return sb.toString();
	}

	private String generateName() {
		return generateName(getAddress());
	}

	private void sendBroadcast(String action) {
		sendBroadcast(action, null);
	}

	private void sendBroadcast(String action, Bundle extras) {
		Log.v(TAG, "Sending broadcast: " + action + "; " + extras);

		Intent intent = new Intent();
		intent.setAction(action);
		if (extras != null) {
			intent.putExtras(extras);
		}
		this.context.sendBroadcast(intent);
	}

	private void sendResult(int result) {
		Log.v(TAG, "Sending result: " + result);
		if (this.ctrlActivity == null) {
			Log.e(TAG, "trying to finish controller activity, but it is not set");
		}
		this.ctrlActivity.setResult(result);
		this.ctrlActivity.finish();
		this.ctrlActivity = null;
	}

	private void setDiscovering(boolean discovering) {
		if (this.discovering != discovering) {
			this.discovering = discovering;
			if (discovering) {
				sendBroadcast(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			} else {
				sendBroadcast(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			}
		}
	}

	private void setScanMode(int scanMode) {
		if (this.scanMode != scanMode) {
			Bundle extras = new Bundle();
			extras.putInt(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, this.scanMode);
			extras.putInt(BluetoothAdapter.EXTRA_SCAN_MODE, scanMode);
			this.scanMode = scanMode;
			sendBroadcast(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED, extras);
		}
	}
}
