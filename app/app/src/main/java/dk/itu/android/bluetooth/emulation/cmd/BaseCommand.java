package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;

import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;

public abstract class BaseCommand implements Runnable {
	static final String UTF8 = "UTF8";
	static final String DEV_MACHINE_IP = "10.0.2.2";
	static final int DISCOVERY_SERVICE_PORT = 8199;
       
    protected BluetoothAdapterEmulator emulator = BluetoothAdapterEmulator.getInstance();

	CommandType type;
	Socket socket;
	InputStream in;
	OutputStream out;
	
	public BaseCommand(CommandType type) {
		this.type = type;
	}

	@Override
	public void run() {
		try {
			this.socket = new Socket(DEV_MACHINE_IP,DISCOVERY_SERVICE_PORT);
			out = socket.getOutputStream();
			in = socket.getInputStream();
			writePreamble();
			sendParameters(out);
			out.write("]".getBytes(UTF8));
			readResponse(in);
			Log.d("BTEMU_CMD", "command execution completed.");
		} catch(Exception e) {
			Log.e("BTEMU_CMD", "error while executing command " + type.name(), e);
		} finally {
			Log.d("BTEMU_CMD", "closing streams and socket.");
			try {
				out.close();
				in.close();
				socket.close();
			}
			catch (Exception e) {
				Log.e("BTEMU_CMD", e.toString());
			}
		}
	}
	
	protected void writePreamble() throws IOException {
		Log.d("BTEMU_CMD", "writing preamble for type: "+type.name());
		out.write( (Integer.toString(type.intRepr)+"\n").getBytes(UTF8) );
		//in any case, send the bt.address parameter
		sendParameter(
			"bt.address",
			dk.itu.android.bluetooth.BluetoothAdapter.getDefaultAdapter().getAddress(),
			out
		);
	}
	
	protected void sendParameter(String name, String value, OutputStream out) throws IOException {
		out.write( (name+"="+value+"\n").getBytes(UTF8) );
	}

	protected abstract void sendParameters(OutputStream out) throws IOException;
	protected abstract void readResponse(InputStream in) throws IOException;
}
