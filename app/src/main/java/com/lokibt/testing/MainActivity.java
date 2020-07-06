package com.lokibt.testing;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lokibt.bluetooth.BluetoothAdapter;
import com.lokibt.bluetooth.BluetoothDevice;
import com.lokibt.bluetooth.BluetoothServerSocket;
import com.lokibt.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static final String TAG = "BTEMU";
    static final String ITEM_KEY = "key";
    static final int REQUEST_DISCOVERABLE = 23;
    static final int REQUEST_ENABLE = 42;

    private boolean runServer = false;

    private BluetoothAdapter bluetoothAdapter;
    private Switch discoverableSwitch;
    private Switch discoverySwitch;
    private Switch enableSwitch;
    private Switch serverSwitch;
    private List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
    private SimpleAdapter listAdapter;

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
                            logToView("Bluetooth will be enabled");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            logToView("Bluetooth will be disabled");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            logToView("Bluetooth was enabled");
                            enableSwitch.setChecked(true);
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            logToView("Bluetooth was disabled");
                            enableSwitch.setChecked(false);
                            break;
                        default:
                            logErrorToView("Unknown Bluetooth state: " + state);
                            break;
                    }
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
                    switch (scanMode) {
                        case BluetoothAdapter.SCAN_MODE_NONE:
                            logToView("Device is hidden");
                            discoverableSwitch.setChecked(false);
                            break;
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            logToView("Device is connectable");
                            discoverableSwitch.setChecked(false);
                            break;
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            logToView("Device is discoverable");
                            discoverableSwitch.setChecked(true);
                            break;
                        default:
                            logErrorToView("Unknown Bluetooth scan mode: " + scanMode);
                            break;
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    logToView("Bluetooth discovery started");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    logToView("Bluetooth discovery finished");
                    break;
                case BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED:
                    logToView("Bluetooth name changed to: " + intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME));
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    logToView("Bluetooth device found: " + device);
                    addDevice(device);
                    break;
                default:
                    logErrorToView("Unknown Bluetooth action: " + action);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        discoverableSwitch = findViewById(R.id.discoverable_switch);
        discoverySwitch = findViewById(R.id.discovery_switch);
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
        startClient(other);
    }

    public void onEnableClick(View v) {
        if (enableSwitch.isChecked()) {
            logToView("Enabling Bluetooth...");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Comment the following two lines to use the official web-service
            intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_HOST, "10.0.2.2");
            intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_PORT, 8198);
            // Uncomment the following two lines to test device grouping
            //intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_GROUP, "com.lokibt.testing");
            startActivityForResult(intent, REQUEST_ENABLE);
        } else {
            discoverableSwitch.setChecked(false);
            serverSwitch.setChecked(false);
            logToView("Disabling Bluetooth...");
            bluetoothAdapter.disable();
        }
    }

    public void onDiscoverableClick(View v) {
        if (discoverableSwitch.isChecked()) {
            enableSwitch.setChecked(true);
            logToView("Making device discoverable...");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
            // Comment the following two lines to use the official web-service
            intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_HOST, "10.0.2.2");
            intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_PORT, 8198);
            // Uncomment the following two lines to test device grouping
            //intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_GROUP, "com.lokibt.testing");
            startActivityForResult(intent, REQUEST_DISCOVERABLE);
        } else {
            discoverableSwitch.setChecked(true);
            Toast.makeText(this, "Discoverable state will be automatically revoked after 300 seconds.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onServerClick(View v) {
        if (serverSwitch.isChecked()) {
            runServer = true;
            startServer();
        } else {
            runServer = false;
            Toast.makeText(this, "Server will be disabled after the next message has been received.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onDiscoveryClick(View v) {
        logToView("Starting discovery...");
        if (discoverySwitch.isChecked()) {
            if (bluetoothAdapter.startDiscovery()) {
                listData.clear();
                listAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Bluetooth needs to be enabled first", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (bluetoothAdapter.cancelDiscovery()) {
                listData.clear();
                listAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Problem while cancelling the Bluetooth discovery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothServerSocket listenSocket = null;
                BluetoothSocket dataSocket = null;
                try {
                    logToView("Starting server...");
                    listenSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("dk.echo", UUID.fromString("419bbc68-c365-4c5e-8793-5ebff85b908c"));
                    Log.d(TAG, "Waiting for client connections...");
                    while (runServer) {
                        try {
                            dataSocket = listenSocket.accept();
                            InputStream inStream = dataSocket.getInputStream();
                            OutputStream outStream = dataSocket.getOutputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));

                            String line = reader.readLine();
                            logToView("Message received: " + line);

                            writer.write("1 Echo of: \"" + line + "\"\n");
                            writer.flush();

                            line = reader.readLine();
                            logToView("Message received: " + line);

                            writer.write("2 Echo of: \"" + line + "\"\n");
                            writer.flush();

                            writer.write("It was nice talking to you.\n");
                            writer.flush();

                            writer.write("Goodbye!\n");
                            writer.flush();
                        } catch (Exception e) {
                            logErrorToView("Exception during data exchange", e);
                        } finally {
                            try {
                                Log.d(TAG, "Closing data socket");
                                if (dataSocket != null) {
                                    dataSocket.close();
                                }
                            } catch (Exception e2) {
                                logErrorToView("Exception when closing data socket", e2);
                            }
                        }
                    }
                } catch (Exception e) {
                    logErrorToView("Exception during server loop", e);
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            serverSwitch.setChecked(false);
                        }
                    });
                    try {
                        Log.d(TAG, "Closing listen socket");
                        if (listenSocket != null) {
                            listenSocket.close();
                        }
                        logToView("Server stopped");
                    } catch (Exception e2) {
                        logErrorToView("Exception when closing listen socket", e2);
                    }
                }
            }
        }).start();
    }

    private void startClient(final BluetoothDevice other) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothSocket socket = null;
                try {
                    logToView("Sending messages to: " + other);
                    socket = other.createRfcommSocketToServiceRecord(UUID.fromString("419bbc68-c365-4c5e-8793-5ebff85b908c"));
                    Log.d(TAG, "Connecting to client socket");
                    socket.connect();
                    InputStream inStream = socket.getInputStream();
                    OutputStream outStream = socket.getOutputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));

                    /*String reply = reader.readLine();
                    logToView("Received reply: " + reply);

                    reply = reader.readLine();
                    logToView("Received reply: " + reply);*/

                    Log.d(TAG, "Writing message...");
                    writer.write("1 Hello Bluetooth :)\n");
                    writer.flush();
                    Log.d(TAG, "Writing message done");

                    String reply = reader.readLine();
                    logToView("Received reply: " + reply);

                    Log.d(TAG, "Writing message...");
                    writer.write("2 Hello Bluetooth :)\n");
                    writer.flush();
                    Log.d(TAG, "Writing message done");

                    reply = reader.readLine();
                    logToView("Received reply: " + reply);

                    reply = reader.readLine();
                    logToView("Received reply: " + reply);

                    reply = reader.readLine();
                    logToView("Received reply: " + reply);

                    Log.d(TAG, "Closing client socket");
                    socket.close();
                } catch (IOException e) {
                    logErrorToView("Exception in client", e);
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            serverSwitch.setChecked(false);
                        }
                    });
                    try {
                        Log.d(TAG, "Closing client socket");
                        if (socket != null) {
                            socket.close();
                        }
                        logToView("Client stopped");
                    } catch (Exception e2) {
                        logErrorToView("Exception when stopping client", e2);
                    }
                }
            }
        }).start();
    }

    private void addDevice(final BluetoothDevice device) {
        Log.d(TAG, "Adding device to list... " + device);
        Map<String, Object> item = new HashMap<String, Object>() {{
            put(ITEM_KEY, "Send message to " + device.getAddress());
            put("DEVICE", device);
        }};
        listData.add(item);
        listAdapter.notifyDataSetChanged();
    }

    private void logToView(final String str) {
        Log.i(TAG, str);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView log = findViewById(R.id.log);
                log.setText(str + "\n" + log.getText());
            }
        });
    }

    private void logErrorToView(String str) {
        logErrorToView(str, null);
    }

    private void logErrorToView(final String str, Exception e) {
        Log.e(TAG, str, e);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView log = findViewById(R.id.log);
                log.setText("ERROR " + str + "\n" + log.getText());
            }
        });
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
