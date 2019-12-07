package dk.itu.android.bluetooth;

import java.io.IOException;
import java.util.UUID;

import dk.itu.android.bluetooth.emulation.BluetoothServerSocketEmulator;

public class BluetoothServerSocket {
	private BluetoothServerSocketEmulator emulator;
	
	protected BluetoothServerSocket(UUID uuid) {
		this.emulator = new BluetoothServerSocketEmulator(uuid);
	}

	public BluetoothSocket accept() throws IOException {
		return emulator.accept();
	}

	public BluetoothSocket accept(int timeout) throws IOException {
		return emulator.accept(timeout);
	}

	public void close() throws IOException {
		emulator.close();
	}
}
