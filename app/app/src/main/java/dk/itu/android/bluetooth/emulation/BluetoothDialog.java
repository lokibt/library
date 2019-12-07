package dk.itu.android.bluetooth.emulation;

import dk.itu.android.bluetooth.BluetoothAdapter;
import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;
import dk.itu.android.btemu.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class BluetoothDialog extends Activity {
	private static final String TAG = "BTDIALOG";

	private final BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();

	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.ctrl);

		intent = getIntent();
		Log.d(TAG, "Bluetooth dialog created for action: " + intent.getAction());

		TextView textView = findViewById(R.id.message);
		switch(intent.getAction()) {
			case BluetoothAdapter.ACTION_REQUEST_ENABLE:
				textView.setText(getResources().getText(R.string.message_enable));
				break;
			case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE:
				textView.setText(getResources().getText(R.string.message_discoverable));
				break;
		}

		BluetoothAdapterEmulator.getInstance().setActivity(this);
	}

	public void onAllow(View view) {
		switch(intent.getAction()) {
			case BluetoothAdapter.ACTION_REQUEST_ENABLE:
				if (emulator.enable()) {
					setResult(RESULT_OK);
				}
				else {
					setResult(Activity.RESULT_CANCELED);
				}
				finish();
				break;
			case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE:
				emulator.startDiscoverable();
				break;
			default:
				Log.e(TAG, "Unknown action: " + intent.getAction());
		}
	}

	public void onDeny(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}
}
