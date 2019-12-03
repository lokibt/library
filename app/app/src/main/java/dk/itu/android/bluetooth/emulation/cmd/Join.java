package dk.itu.android.bluetooth.emulation.cmd;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Join extends BaseCommand {

	Activity ctrlAction;

	public Join(Activity ctrlAction) {
		super(CommandType.JOIN);
		this.ctrlAction = ctrlAction;
	}

	@Override
	protected void readResponse(InputStream in) throws IOException {
		//get the emulator port number
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String p = br.readLine();
		String name = "emulator-" + p;
		name = name.trim();
		dk.itu.android.bluetooth.BluetoothAdapter.getDefaultAdapter().setName(name);
		ctrlAction.setResult(Activity.RESULT_OK);
		ctrlAction.finish();
	}

	@Override
	protected void sendParameters(OutputStream out) throws IOException {
		sendParameter( "tcp.address","10.0.2.2",out );
		sendParameter( "device.name", dk.itu.android.bluetooth.BluetoothAdapter.getDefaultAdapter().getName(), out);
//		sendParameter( "bt.address", dk.itu.android.bluetooth.BluetoothAdapter.getDefaultAdapter().getAddress(), out );
	}

}
