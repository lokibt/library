package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class AddService extends Command {
	UUID uuid;

	public AddService(UUID uuid) {
		super(CommandType.ADDSERVICE, null);
		this.uuid = uuid;
	}

	@Override
	public void close() throws IOException {
		// the connection shall stay open
	}

	@Override
	protected void readResponse() throws IOException { }

	@Override
	protected void sendParameters() throws IOException {
		sendParameter(this.uuid.toString());
	}

}
