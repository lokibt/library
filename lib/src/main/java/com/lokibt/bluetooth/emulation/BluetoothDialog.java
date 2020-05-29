package com.lokibt.bluetooth.emulation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lokibt.bluetooth.BluetoothAdapter;
import com.lokibt.bluetooth.R;

public class BluetoothDialog extends Activity {
    private static final String TAG = "BTDIALOG";

    private final BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();

    private Intent intent;
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_dialog);

        intent = getIntent();
        Log.d(TAG, "Bluetooth dialog created for action: " + intent.getAction());

        TextView textView = findViewById(R.id.message);
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_REQUEST_ENABLE:
                textView.setText(getResources().getText(R.string.message_enable));
                break;
            case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE:
                duration = intent.getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                textView.setText(String.format(getResources().getString(R.string.message_discoverable), duration));
                break;
        }

        emulator.setActivity(this);
    }

    public void onAllow(View view) {
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_REQUEST_ENABLE:
                if (emulator.enable()) {
                    setResult(RESULT_OK);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
                finish();
                break;
            case BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE:
                emulator.startDiscoverable(duration);
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
