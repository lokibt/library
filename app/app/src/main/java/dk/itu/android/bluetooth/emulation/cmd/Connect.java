package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import dk.itu.android.bluetooth.BluetoothSocket;

public class Connect extends Command {
	private BluetoothSocket btsocket;
	private UUID uuid;

	public Connect(UUID uuid, BluetoothSocket btsocket) {
       	super(CommandType.CONNECT, btsocket);
		this.btsocket = btsocket;
		this.uuid = uuid;
	}

	@Override
	public Socket open() throws IOException {
		super.open();
		super.writePreamble();
		sendParameter(this.btsocket.getRemoteDevice().getAddress());
		sendParameter(this.uuid.toString());
		return this.socket;
	}

	@Override
	public void close() throws IOException {
		// the connection shall stay open
	}

	@Override
	protected void readResponse() throws IOException { }


	@Override
	protected void sendParameters() throws IOException {
		// parameters have already been set via open()
	}

	@Override
	protected void writePreamble() throws IOException {
		// preamble has already been set via open()
	}
}
