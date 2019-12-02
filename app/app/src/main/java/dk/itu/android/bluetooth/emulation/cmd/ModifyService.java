package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ModifyService extends BaseCommand {

	int port;
	String uuid;
	boolean added;
	public ModifyService(String uuid, int port, boolean added) {
		super(CommandType.MODIFYSERVICE);
		this.uuid = uuid;
		this.port = port;
		this.added = added;
	}

	@Override
	protected void readResponse(InputStream in) throws IOException {
	}

	@Override
	protected void sendParameters(OutputStream out) throws IOException {
		sendParameter("type", added ? "added" : "removed", out);
		sendParameter("tcp.port", port+"",out);
		sendParameter("service.uuid",uuid,out);
	}

}
