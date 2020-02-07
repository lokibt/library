package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class RemoveService extends Command {
	UUID uuid;

	public RemoveService(UUID uuid) {
		super(CommandType.REMOVESERVICE, null);
		this.uuid = uuid;
	}

	@Override
	protected void readResponse() throws IOException {
	}

	@Override
	protected void sendParameters() throws IOException {
		sendParameter(uuid.toString());
	}

}
