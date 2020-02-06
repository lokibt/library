package dk.itu.android.bluetooth.emulation.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Join extends BaseCommand {

	public Join() {
       	super(CommandType.JOIN);
	}

	@Override
	protected void readResponse(InputStream in) throws IOException {
		//get the emulator port number
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		emulator.onJoinReturned();
	}

	@Override
	protected void sendParameters(OutputStream out) throws IOException {
		sendParameter( "tcp.address","10.0.2.2",out );
		sendParameter( "device.name", dk.itu.android.bluetooth.BluetoothAdapter.getDefaultAdapter().getName(), out);
	}

}
