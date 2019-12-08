package dk.itu.android.bluetooth.emulation;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import dk.itu.android.bluetooth.BluetoothDevice;
import dk.itu.android.bluetooth.BluetoothSocket;
import dk.itu.android.bluetooth.emulation.cmd.ModifyService;

public class BluetoothServerSocketEmulator {
	private static final String TAG = "BTEMU_SERVERSOCKET";

	private static int nextPort = 8124;

	private ServerSocket socket;
	private int port;
	private UUID uuid;

	public BluetoothServerSocketEmulator(UUID uuid) {
		this.port = getNextPort();
		this.uuid = uuid;
		try {
			this.socket = new ServerSocket(port);
			addService(uuid, port);
		} catch (IOException e) {
			Log.e(TAG, "Cannot create server socket", e);
		}
	}

	public BluetoothSocket accept() throws IOException {
		Log.d(TAG, "Waiting for incoming connections on port " + socket.getInetAddress() + "; " + socket.getLocalSocketAddress() + ";" + socket.getLocalPort());
		// blocks until a connection is established
		Socket s = socket.accept();
		return createBTSocket(s);
	}

	public BluetoothSocket accept(int timeout) throws IOException {
		FutureTask<Socket> future = new FutureTask<Socket>(new Callable<Socket>() {
			@Override
			public Socket call() throws Exception {
				// blocks until a connection is established
				return socket.accept();
			}
		});
		try {
			Log.d(TAG, "Waiting for incoming connections for " + timeout + " seconds...");
			Executors.newFixedThreadPool(1).execute(future);
			Socket s = future.get(timeout, TimeUnit.SECONDS);
			return createBTSocket(s);
		} catch(Exception e) {
			Log.e(TAG, "Timeout while waiting for incoming connections",e);
			throw new IOException("timed out");
		}
	}

	public void close() throws IOException {
		removeService(this.uuid, this.port);
		socket.close();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private BluetoothSocket createBTSocket(Socket s) throws IOException {
		InputStream is = s.getInputStream();
		Log.i(TAG, "creating btsocket, reading btaddr...");
		int read;
		byte[] buf = new byte[25];

		int idx = 0;
		do {
			read = is.read();
			buf[idx] = (byte)read;
			idx++;
		} while( '\n' != read );

		String btaddr = new String(buf,0,idx-1);

		Log.i(TAG, "received btaddr: " + btaddr);
		BluetoothDevice d = BluetoothAdapterEmulator.getInstance().getRemoteDevice(btaddr.trim());
		return new BluetoothSocket(s, d);
	}

	private void addService(UUID uuid, int port ) {
		modifyService(uuid, port, true);
	}

	private void removeService(UUID uuid, int port ) {
		modifyService(uuid, port, false);
	}

	private void modifyService(UUID uuid, int port, boolean add ) {
		try {
			new Thread(new ModifyService(uuid.toString(), port, add)).start();
		} catch (Exception e) {
			Log.e(TAG, "Cannot start ModifyService() thread", e);
		}
	}

	private int getNextPort() {
		return nextPort++;
	}
}
