package dk.itu.android.btemu;

import dk.itu.android.bluetooth.BluetoothAdapter;
import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BTEnableControl extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //setContentView(R.layout.ctrl);

		BluetoothAdapterEmulator.getInstance().setControllerActivity(this);
	}

	@Override
	protected void onStart() {
        super.onStart();
		Intent started = getIntent();
		Log.i("BTCTRL", "start with action: "+started.getAction());
		if(started.getAction().equals(BluetoothAdapter.ACTION_REQUEST_ENABLE)) {
			BluetoothAdapter.getDefaultAdapter().enable();
		} else if(started.getAction().equals(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)) {
			//enabled discoverable
			setResult(RESULT_OK);
		}
	}
	
}
