package dk.itu.android.btemu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import dk.itu.android.bluetooth.BluetoothAdapter;
import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.BluetoothServerSocket;
import dk.itu.android.bluetooth.BluetoothSocket;

public class Main extends Activity implements OnItemClickListener {
	static final String TAG = "BTEMU";
	static final String ITEM_KEY = "key";
	static final int REQUEST_DISCOVERABLE = 23;
	static final int REQUEST_ENABLE = 42;

	final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "Received broadcast action: " + action);
			switch (action) {
				case BluetoothAdapter.ACTION_STATE_CHANGED:
					int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
					switch (state) {
						case BluetoothAdapter.STATE_TURNING_ON:
							Log.d(TAG, "Bluetooth will be enabled");
							break;
						case BluetoothAdapter.STATE_TURNING_OFF:
							Log.d(TAG, "Bluetooth will be disabled");
							break;
						case BluetoothAdapter.STATE_ON:
							Log.d(TAG, "Bluetooth was enabled");
							enableSwitch.setChecked(true);
							break;
						case BluetoothAdapter.STATE_OFF:
							Log.d(TAG, "Bluetooth was disabled");
							enableSwitch.setChecked(false);
							break;
						default:
							Log.e(TAG, "Unknown Bluetooth state: " + state);
							break;
						}
					break;
				case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
					int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
					switch (scanMode) {
						case BluetoothAdapter.SCAN_MODE_NONE:
							Log.d(TAG, "Device is hidden");
							discoverableSwitch.setChecked(false);
							break;
						case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
							Log.d(TAG, "Device is connectable");
							discoverableSwitch.setChecked(false);
							break;
						case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
							Log.d(TAG, "Device is discoverable");
							discoverableSwitch.setChecked(true);
							break;
						default:
							Log.e(TAG, "Unknown Bluetooth scan mode: " + scanMode);
							break;
					}
					break;
				case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
					Log.d(TAG, "Bluetooth discovery started");
					break;
				case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
					Log.d(TAG, "Bluetooth discovery finished");
					break;
				case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:
					Log.d(TAG, "Bluetooth name changed to: " + intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME));
					break;
				case BluetoothDevice.ACTION_FOUND:
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Log.d(TAG, "Bluetooth device found: " + device);
					addDevice(device);
					break;
				default:
					Log.e(TAG, "Unknown Bluetooth action: " + action);
					break;
			}
		}
	};

	private BluetoothAdapter bluetoothAdapter;
	private Switch discoverableSwitch;
	private Switch enableSwitch;
	private Switch serverSwitch;
	private List<Map<String,Object>> listData = new ArrayList<Map<String,Object>>();
	private SimpleAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);

		discoverableSwitch = findViewById(R.id.discoverable_switch);
		enableSwitch = findViewById(R.id.enable_switch);
		serverSwitch = findViewById(R.id.server_switch);

		listAdapter = new SimpleAdapter(this, listData, R.layout.row, new String[]{ITEM_KEY}, new int[]{R.id.list_value});
		ListView deviceList = findViewById(R.id.device_list);
		deviceList.setAdapter(listAdapter);
		deviceList.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
		bluetoothAdapter.disable();
		unregisterReceiver(receiver);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BluetoothDevice other = (BluetoothDevice) listData.get(position).get("DEVICE");
		Log.d(TAG, "Starting client to " + other + "...");
		startClient(other);
	}

	public void onEnableClick(View v) {
    	Log.d(TAG, "ENABLE: " + enableSwitch.isChecked());
    	if (enableSwitch.isChecked()) {
			Log.d(TAG, "Enabling Bluetooth...");
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE);
		}
    	else {
			Log.d(TAG, "Disabling Bluetooth...");
			bluetoothAdapter.disable();
		}
	}

	public void onDiscoverableClick(View v) {
    	if (discoverableSwitch.isChecked()) {
			Log.d(TAG, "Making device dicoverable...");
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivityForResult(intent, REQUEST_DISCOVERABLE);
		}
    	else {
			Toast.makeText(this, "Discoverable state will be automatically revoked after 300 seconds.", Toast.LENGTH_SHORT).show();
			discoverableSwitch.setChecked(true);
		}
	}

	public void onServerClick(View v) {
		if (serverSwitch.isChecked()) {
			Log.d(TAG, "Starting server...");
			startServer();
		}
		else {
			Toast.makeText(this, "Server will be disabled after a message has been received.", Toast.LENGTH_SHORT).show();
			serverSwitch.setChecked(true);
		}
	}

	public void onDiscoveryClick(View v) {
		Log.d(TAG, "Starting discovery...");
		listData.clear();
		listAdapter.notifyDataSetChanged();
		bluetoothAdapter.startDiscovery();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private void addDevice(final BluetoothDevice device) {
		Log.d(TAG, "Adding device to list... " + device);
		Map<String,Object> item = new HashMap<String,Object>(){{
			put(ITEM_KEY, "Send message to " + device.getAddress());
			put("DEVICE", device);
		}};
		listData.add(item);
		listAdapter.notifyDataSetChanged();
	}

    private void startServer() {
    	new Thread(new Runnable() {
    		@Override
    		public void run() {
    	    	try {
    	    		Log.d(TAG, "Server accepting connection...");
    				BluetoothServerSocket server = BluetoothAdapter
    					.getDefaultAdapter()
    					.listenUsingRfcommWithServiceRecord("dk.echo", UUID.fromString("419bbc68-c365-4c5e-8793-5ebff85b908c"));
					Log.d(TAG, "Client creates client socket");
    				BluetoothSocket client = server.accept();
    				String line = new BufferedReader(new InputStreamReader(client.getInputStream())).readLine();
    	    		Log.d(TAG, "Server sends echo for line: " +  line);
    				client.getOutputStream().write( ("echoed: " + line).getBytes("UTF-8") );
    	    		Log.d(TAG, "Server is closing client and server socket");
    				client.close();
    				server.close();
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							serverSwitch.setChecked(false);
						}
					});

    			} catch (Exception e) {
    				Log.e(TAG, "Error in echo server", e);
    			}
    		}
    	}).start();
    }

    private void startClient(final BluetoothDevice other) {
    	new Thread(new Runnable() {
    		@Override
    		public void run() {
    	    	try {
    	    		Log.d(TAG, "Client creates socket");
    				BluetoothSocket s = other.createRfcommSocketToServiceRecord(UUID.fromString("419bbc68-c365-4c5e-8793-5ebff85b908c"));
    	    		Log.d(TAG, "Client connects to socket");
    	    		s.connect();
    	    		Log.d(TAG, "Client sends message");
    				s.getOutputStream().write("Hello Bluetooth :)\r\n".getBytes("UTF-8"));
    				s.getOutputStream().flush();
    				Log.d(TAG, "Client reads response");
    				final String reply = new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
    	    		Log.d(TAG, "Client is closing socket");
    				s.close();
    				runOnUiThread(new Runnable(){
    					@Override
    					public void run() {
    						((TextView)findViewById(R.id.log)).setText(reply);
    					}
    				});
    			} catch (IOException e) {
    				Log.e(TAG, "Error in echo client", e);
    			}
    		}
    	}).start();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_DISCOVERABLE:
				switch (resultCode) {
					case RESULT_OK:
						Log.d(TAG, "REQUEST_DISCOVERABLE returned: RESULT_OK");
						break;
					case RESULT_CANCELED:
						Log.d(TAG, "REQUEST_DISCOVERABLE returned: RESULT_CANCELED");
						break;
					default:
						Log.e(TAG, "REQUEST_DISCOVERABLE returned unknown result code: " + resultCode);
				}
				break;
			case REQUEST_ENABLE:
				switch (resultCode) {
					case RESULT_OK:
						Log.d(TAG, "REQUEST_ENABLE returned: RESULT_OK");
						break;
					case RESULT_CANCELED:
						Log.d(TAG, "REQUEST_ENABLE returned: RESULT_CANCELED");
						break;
					default:
						Log.e(TAG, "REQUEST_ENABLE returned unknown result code: " + resultCode);
				}
				break;
			default:
				Log.e(TAG, "Unknown request code: " + requestCode);
		}
	}
}