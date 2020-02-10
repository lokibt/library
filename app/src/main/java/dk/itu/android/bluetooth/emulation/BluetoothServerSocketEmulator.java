package dk.itu.android.bluetooth.emulation;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.BluetoothSocket;
import dk.itu.android.bluetooth.emulation.cmd.Announce;
import dk.itu.android.bluetooth.emulation.cmd.Link;

public class BluetoothServerSocketEmulator {
	private static final String TAG = "BTEMU_SERVERSOCKET";

	private Socket socket;
	private UUID uuid;

	public BluetoothServerSocketEmulator(UUID uuid) {
		try {
			this.uuid = uuid;
			Announce addCmd = new Announce(this.uuid);
			this.socket = addCmd.open();
			new Thread(addCmd).start();
		} catch (IOException e) {
			Log.e(TAG, "Cannot create Bluetooth server socket", e);
		}
	}

	public BluetoothSocket accept() throws IOException {
		Log.d(TAG, "Waiting for an incoming connection...");
		// blocks until a connection is established
		return this.waitForConnection();
	}

	public BluetoothSocket accept(int timeout) throws IOException {
		FutureTask<BluetoothSocket> future = new FutureTask<BluetoothSocket>(new Callable<BluetoothSocket>() {
			@Override
			public BluetoothSocket call() throws IOException {
				// blocks until a connection is established
				return BluetoothServerSocketEmulator.this.waitForConnection();
			}
		});
		try {
			Log.d(TAG, "Waiting for an incoming connection for " + timeout + " seconds...");
			Executors.newFixedThreadPool(1).execute(future);
			return future.get(timeout, TimeUnit.SECONDS);
		} catch(Exception e) {
			Log.e(TAG, "Timeout while waiting for incoming connections",e);
			throw new IOException("timed out");
		}
	}

	public void close() throws IOException {
		this.socket.close();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private BluetoothSocket waitForConnection() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String btaddr = br.readLine();
		String connId = br.readLine();
		Log.i(TAG, "Incoming connection from " + btaddr + ": " + connId);
		Link linkCmd = new Link(connId);
		Socket socket = linkCmd.open();
		new Thread(linkCmd).start();
		BluetoothDevice device = BluetoothAdapterEmulator.getInstance().getRemoteDevice(btaddr.trim());
		return new BluetoothSocket(socket, device, this.uuid);
	}
}
