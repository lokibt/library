package dk.itu.android.bluetooth.emulation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.emulation.cmd.Discovery;
import dk.itu.android.bluetooth.emulation.cmd.Join;
import dk.itu.android.bluetooth.emulation.cmd.Leave;
import dk.itu.android.bluetooth.emulation.cmd.ModifyService;

public class Emulator {
	private static final String TAG = "BTEMULATOR";

	private static Emulator _instance = null;

	public static Emulator instance() {
		if (_instance == null) {
			try {
				// Getting application context via reflection
				// https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
				Context context = (Context) Class.forName("android.app.ActivityThread")
						.getMethod("currentApplication").invoke(null, (Object[]) null);
				_instance = new Emulator(context);
			} catch (Exception e) {
				Log.e(TAG, "unable to get application context", e);
			}
		}
		return _instance;
	}

	private Context context;
	private Activity ctrlActivity = null;

	private String address = null;
	private boolean enabled = false;
	private String name = "local";

	Emulator(Context context) {
		this.context = context;
	}

	public void setControllerActivity(Activity ctrlActivity) {
		Log.d(TAG, "setting controller activity " + ctrlActivity);
		this.ctrlActivity = ctrlActivity;
	}

	public String getAddress() {
		if (this.address == null) {
			try {
				FileInputStream fis = this.context.openFileInput("BTADDR.TXT");
				Log.d(TAG, "reading Bluetooth address: " + this.address);
				byte[] buf = new byte[100];
				int read = fis.read(buf);
				this.address = new String(buf, 0, read);
			}
			catch (Exception readException) {
				Log.d(TAG, "error while reading Bluetooth address", readException);
				try {
					String addr = generateAddress();
					Log.d(TAG, "saving Bluetooth address: " + this.address);
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
		}
		return this.address;
	}

	public String getName() {
		return this.name;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean disable() {
		Log.d(TAG, "disabling Bluetooth");
		leave();
		this.enabled = false;
		return true;
	}


	public boolean enable() {
		Log.d(TAG, "enabling Bluetooth");
		join();
		this.enabled = true;
		return true;
	}

	public boolean setName(String name) {
		this.name = name;
		return true;
	}


	public void sendBroadcast(String action) {
		try {
			Log.d(TAG, "sendBroadcast in appContext: " + this.context);
			Intent intent = new Intent();
			intent.setAction(action);
			this.context.sendBroadcast(intent);
		}
		catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	public boolean finishController(int result) {
		if (this.ctrlActivity == null) {
			Log.d(TAG, "trying to finish controller, but no activity set");
			return false;
		}
		this.ctrlActivity.setResult(result);
		this.ctrlActivity.finish();
		this.ctrlActivity = null;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

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
	
	public void join() {
		try {
			new Thread(new Join()).start();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "cannot join", e);
		}
	}
	public void leave() {
		try {
			new Thread(new Leave()).start();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "cannot leave", e);
		}
	}
	
	public void asyncDiscovery( Discovery.WithDevices wd ) {
		Discovery d;
		try {
			d = new Discovery();
			d.setWithDevices(wd);
			new Thread(d).start();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "cannot async discovery", e);
		}
	}
	
	public void discovery( Discovery.WithDevices wd ) {
		Discovery d;
		try {
			d = new Discovery();
			d.setWithDevices(wd);
			new Thread(d).start();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "cannot discovery", e);
		}
	}
	
	public BluetoothDevice lookupBT( String btAddr ) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		FutureTask<List<BluetoothDevice>> future = 
			new FutureTask<List<BluetoothDevice>>(new Callable<List<BluetoothDevice>>(){
			@Override
			public List<BluetoothDevice> call() throws Exception {
				Discovery d = new Discovery();
				new Thread(d).start();
				return d.getDevices();
			}
		});
		executor.execute(future);
		try {
			List<BluetoothDevice> devices = future.get();
			for(BluetoothDevice d : devices) {
				Log.i(TAG, "check btaddr: "+d.getAddr() + " == " + btAddr + "?");
				if(d.getAddr().equals(btAddr)) {
					Log.i(TAG, "btAddr match, return device");
					return d;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "cannot retrieve btdevices", e);
		}
		Log.i(TAG, "btAddr " + btAddr + " not found! return null!");
		return null;
	}
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
