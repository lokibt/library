package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Leave extends NoParamsBaseCommand {
	
	public Leave() {
		super(CommandType.LEAVE);
	}

	@Override
	protected void readResponse(InputStream in) throws IOException {
		//don't care
	}

	@Override
	protected void sendParameters(OutputStream out) throws IOException {
//		sendParameter( "tcp.address", socket.getLocalAddress().getHostAddress(), out );
	}

}
