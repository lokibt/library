package com.lokibt.bluetooth.emulation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.lokibt.bluetooth.BluetoothAdapter;
import com.lokibt.bluetooth.BluetoothDevice;
import com.lokibt.bluetooth.BluetoothServerSocket;
import com.lokibt.bluetooth.BluetoothSocket;
import com.lokibt.bluetooth.emulation.cmd.Command;
import com.lokibt.bluetooth.emulation.cmd.CommandCallback;
import com.lokibt.bluetooth.emulation.cmd.CommandType;
import com.lokibt.bluetooth.emulation.cmd.Discovery;
import com.lokibt.bluetooth.emulation.cmd.Join;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class BluetoothAdapterEmulator implements CommandCallback {
    private static final String TAG = "BTEMULATOR";

    private static BluetoothAdapterEmulator instance = null;

    private String address;
    private Context context;
    private Activity ctrlActivity = null;
    private Set<BluetoothDevice> discoveredDevices;
    private String name;
    private int state = BluetoothAdapter.STATE_OFF;
    private Set<BluetoothDevice> bondedDevices;
    private int scanMode = BluetoothAdapter.SCAN_MODE_NONE;

    private Join join;
    private Discovery discoveryCmd;

    private BluetoothAdapterEmulator(Context context) {
        this.context = context;
        this.bondedDevices = new HashSet<BluetoothDevice>();
        this.discoveredDevices = new HashSet<BluetoothDevice>();
        // Generating a name will also set the address
        this.name = generateName(getAddress(false));
    }

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

    public void addBondedDevice(BluetoothDevice device) {
        this.bondedDevices.add(device);
    }

    public boolean cancelDiscovery() {
        checkAdminPermission();
        if (!isEnabled()) {
            return false;
        }
        try {
            if (this.discoveryCmd != null)
                this.discoveryCmd.stop();
            return true;
        }
        catch(IOException e) {
            Log.e(TAG, "Unable to cancel discovery", e);
            return false;
        }
    }

    public boolean disable() {
        checkAdminPermission();
        if (this.state == BluetoothAdapter.STATE_OFF || this.state == BluetoothAdapter.STATE_TURNING_OFF) {
            return false;
        }
        cancelDiscovery();
        stopDiscoverable();
        BluetoothServerSocketEmulator.closeAllOpenSockets();
        BluetoothSocket.closeAllOpenSockets();
        setState(BluetoothAdapter.STATE_TURNING_OFF);
        setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
        setState(BluetoothAdapter.STATE_OFF);
        return true;
    }

    public boolean enable() {
        checkAdminPermission();
        if (this.state == BluetoothAdapter.STATE_ON || this.state == BluetoothAdapter.STATE_TURNING_OFF) {
            return false;
        }
        setState(BluetoothAdapter.STATE_TURNING_ON);
        setState(BluetoothAdapter.STATE_ON);
        setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
        return true;
    }

    public String generateName(String address) {
        return "lokibt-" + address.replace(":", "");
    }

    public String getAddress(boolean strict) {
        if (strict) {
            checkPermission();
        }
        if (this.address == null) {
            this.address = generateAddress();
            Log.d(TAG, "Bluetooth address is: " + this.address);
        }
        return this.address;
    }


    public Set<BluetoothDevice> getBondedDevices() {
        checkPermission();
        return this.bondedDevices;
    }

    public String getName() {
        checkPermission();
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
            Log.d(TAG, "Device address not found: " + address);
            Parcel parcel = Parcel.obtain();
            parcel.writeString(address);
            parcel.writeString(this.generateName(address));
            parcel.setDataPosition(0);
            device = BluetoothDevice.CREATOR.createFromParcel(parcel);
        }
        Log.d(TAG, "Returning Bluetooth device: " + device);
        return device;
    }

    public int getScanMode() {
        checkPermission();
        return scanMode;
    }

    public int getState() {
        checkPermission();
        return this.state;
    }

    public boolean isDiscovering() {
        checkPermission();
        return this.discoveryCmd != null;
    }

    public boolean isEnabled() {
        checkPermission();
        return this.state == BluetoothAdapter.STATE_ON;
    }

    public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        checkPermission();
        checkEnabled();
        return new BluetoothServerSocket(uuid);
    }

    public boolean setName(String name) {
        checkAdminPermission();
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
        checkAdminPermission();
        if (!isEnabled()) {
            return false;
        }
        try {
            this.discoveredDevices = new HashSet<>();
            this.discoveryCmd = new Discovery(this);
            new Thread(this.discoveryCmd).start();
            sendBroadcast(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Cannot start Discovery() thread", e);
            return false;
        }
    }

    public void addDiscoveredDevice(BluetoothDevice device) {
        // Return if device is already in list
        String addr = device.getAddress();
        for(BluetoothDevice d : this.discoveredDevices) {
            Log.d(TAG, addr + " == " + d.getAddress());
            if(d != null && d.getAddress().equals(addr)) {
                return;
            }
        }
        // Add device to list
        Log.d(TAG, "Discovered device: " + device);
        Bundle extras = new Bundle();
        extras.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
        sendBroadcast(BluetoothDevice.ACTION_FOUND, extras);
        this.discoveredDevices.add(device);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onFinish(Command cmd) {
        switch (cmd.getType()) {
            case DISCOVERY:
                Log.d(TAG, "DISCOVERY finished");
                this.discoveryCmd = null;
                sendBroadcast(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                break;
            case JOIN:
                Log.d(TAG, "JOIN finished");
                if (isEnabled()) {
                    setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    sendResult(Activity.RESULT_OK);
                } else {
                    setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
                }
        }
    }

    @Override
    public void onClose(Command cmd) {
        if (cmd.getType() == CommandType.JOIN) {
            Log.d(TAG, "JOIN closed");
            this.join = null;
            if (this.state == BluetoothAdapter.STATE_TURNING_OFF || this.state == BluetoothAdapter.STATE_OFF) {
                setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
                setState(BluetoothAdapter.STATE_OFF);
            } else {
                setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void setActivity(Activity ctrlActivity) {
        Log.d(TAG, "Setting controller activity " + ctrlActivity);
        this.ctrlActivity = ctrlActivity;
    }

    void startDiscoverable(int duration) {
        if (!isEnabled()) {
            enable();
        }
        try {
            this.join = new Join(this, duration);
            new Thread(this.join).start();
        } catch (Exception e) {
            Log.e(TAG, "Error while starting JOIN thread", e);
            this.join = null;
        }
    }

    void stopDiscoverable() {
        try {
            if (this.join != null) {
                this.join.closeImmediately();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while closing JOIN socket", e);
            this.join = null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkEnabled() throws IOException {
        if (!isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            throw new IOException("Bluetooth is not enabled");
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
            Log.e(TAG, "Missing permission: BLUETOOTH");
            throw new SecurityException();
        }
    }

    private void checkAdminPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
            Log.e(TAG, "Missing permission: BLUETOOTH_ADMIN");
            throw new SecurityException();
        }
    }

    private String generateAddress() {
        Log.d(TAG, "generating Bluetooth address");
        long seed = Utils.getIPSeed() | Utils.getDeviceSeed(context);
        String chars = "ABCDEF";
        StringBuffer sb = new StringBuffer();
        Random r = new Random(seed);
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

    private void setScanMode(int scanMode) {
        if (this.scanMode != scanMode) {
            Bundle extras = new Bundle();
            extras.putInt(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, this.scanMode);
            extras.putInt(BluetoothAdapter.EXTRA_SCAN_MODE, scanMode);
            this.scanMode = scanMode;
            sendBroadcast(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED, extras);
        }
    }

    private void setState(int state) {
        if (this.state != state) {
            Bundle extras = new Bundle();
            extras.putInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE, this.state);
            extras.putInt(BluetoothAdapter.EXTRA_STATE, state);
            this.state = state;
            sendBroadcast(BluetoothAdapter.ACTION_STATE_CHANGED, extras);
        }
    }
}
