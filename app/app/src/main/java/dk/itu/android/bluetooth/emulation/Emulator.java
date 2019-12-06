package dk.itu.android.bluetooth.emulation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Hashtable;
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
import dk.itu.android.bluetooth.emulation.cmd.ModifyService;
import dk.itu.android.bluetooth.emulation.cmd.CommandListener;

public class Emulator implements CommandListener {
	private static final String TAG = "BTEMULATOR";

	private static Emulator instance = null;

	public static Emulator getInstance() {
		if (instance == null) {
			try {
				// Getting application context via reflection
				// https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
				Context context = (Context) Class.forName("android.app.ActivityThread")
						.getMethod("currentApplication").invoke(null, (Object[]) null);
				instance = new Emulator(context);
			} catch (Exception e) {
				Log.e(TAG, "unable to get application context", e);
			}
		}
		return instance;
	}

	private String address;
	private Context context;
	private Activity ctrlActivity = null;
	private Hashtable<String, BluetoothDevice> devices;
	private boolean discovering = false;
	private String name;
	private int state = BluetoothAdapter.STATE_OFF;

	Emulator(Context context) {
		this.context = context;
		// Generating a name will also set the address
		this.name = generateName();
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
		if (this.state == BluetoothAdapter.STATE_OFF) {
			return false;
		}
		try {
			this.setState(BluetoothAdapter.STATE_TURNING_OFF);
			new Thread(new Leave()).start();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Cannot start Leave() thread", e);
			return false;
		}
	}

	public boolean enable() {
		if (this.state == BluetoothAdapter.STATE_ON) {
			return false;
		}
		try {
			this.setState(BluetoothAdapter.STATE_TURNING_ON);
			new Thread(new Join()).start();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Cannot start Join() thread", e);
			return false;
		}
	}

	public String getAddress() {
		if (this.address == null) {
			try {
				FileInputStream fis = this.context.openFileInput("BTADDR.TXT");
				Log.d(TAG, "reading Bluetooth address");
				byte[] buf = new byte[100];
				int read = fis.read(buf);
				this.address = new String(buf, 0, read);
			}
			catch (Exception readException) {
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
				}
				catch (Exception writeException) {
					Log.e(TAG, "error while writing Bluetooth address", writeException);
				}
			}
			Log.d(TAG, "Bluetooth address is: " + this.address);
		}
		return this.address;
	}

	public String getName() {
		return this.name;
	}

	public BluetoothDevice getRemoteDevice(String address) {
		if(!BluetoothAdapter.checkBluetoothAddress(address)) {
			throw new IllegalArgumentException("wrong device address");
		}
		BluetoothDevice device = null;
		if (devices.containsKey(address)) {
			device = devices.get(address);
		}
		else {
			// TODO Create a device anyway
			Log.e(TAG, "Device address not found: " + address);
		}
		Log.d(TAG, "Returning Bluetooth device: " + device);
		return device;
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

	public void setControllerActivity(Activity ctrlActivity) {
		Log.d(TAG, "etting controller activity " + ctrlActivity);
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

	public boolean startDiscovery() {
		if(!isEnabled()) {
			return false;
		}
		try {
			setDiscovering(true);
			new Thread(new Discovery()).start();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Cannot start Discovery() thread", e);
			return false;
		}
	}

	@Override
	public void onJoinReturned() {
		this.setState(BluetoothAdapter.STATE_ON);
		sendResult(Activity.RESULT_OK);
	}

	@Override
	public void onLeaveReturned() {
		this.setState(BluetoothAdapter.STATE_OFF);
	}

	@Override
	public void onDiscoveryReturned(Hashtable<String, BluetoothDevice> devices) {
		if (devices.isEmpty()) {
			Log.d(TAG, "Discovered no Bluetooth devices");
		}
		else {
			Set<String> keys = devices.keySet();
			for (String key : keys) {
				BluetoothDevice device = devices.get(key);
				Log.d(TAG, "Discovered device: " + device);
				Bundle extras = new Bundle();
				extras.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
				sendBroadcast(BluetoothDevice.ACTION_FOUND, extras);
			}
		}
		this.devices = devices;
		setDiscovering(false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private String generateName() {
		return generateName(getAddress());
	}

	// should be private once device instaces are created on demand
	public String generateName(String address) {
		return "emulator-" + address.replace(":", "");
	}

	private String generateAddress() {
		Log.d(TAG, "generating Bluetooth address");
		//sample: 00:11:22:AA:BB:CC
		String chars = "ABCDEF";
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		boolean f = true;
		for(int i = 0; i < 3; i++) {
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
		for(int i = 0; i<3;i++) {
			sb.append(":");
			sb.append(chars.charAt(r.nextInt(6)));
			sb.append(chars.charAt(r.nextInt(6)));
		}
		return sb.toString();
	}

	private void sendBroadcast(String action) {
		sendBroadcast(action, null);
	}

	private void sendBroadcast(String action, Bundle extras) {
		Log.v(TAG, "Sending broadcast: " + action);
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

	private void setState(int state) {
		if (this.state != state) {
			this.state = state;
			sendBroadcast(BluetoothAdapter.ACTION_STATE_CHANGED);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

//	public BluetoothDevice lookupIP( String ipAddr ) {
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		FutureTask<List<BluetoothDevice>> future = 
//			new FutureTask<List<BluetoothDevice>>(new Callable<List<BluetoothDevice>>(){
//			@Override
//			public List<BluetoothDevice> call() throws Exception {
//				Discovery d = new Discovery();
//				d.run();
//				return d.getDevices();
//			}
//		});
//		executor.execute(future);
//		try {
//			List<BluetoothDevice> devices = future.get();
//			for(BluetoothDevice d : devices) {
//				if(d.getTcpAddr().equals(ipAddr))
//					return d;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.e(TAG, "cannot retrieve btdevices", e);
//		}
//		
//		return null;
//	}
	
	public void addService( String uuid, int port ) {
		modifyService(uuid,port,true);
	}
	public void removeService( String uuid, int port ) {
		modifyService(uuid,port,false);
	}
	protected void modifyService( String uuid, int port, boolean add ) {
		try {
			new ModifyService(uuid,port,add).run();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "cannot modify service", e);
		}
	}
}
