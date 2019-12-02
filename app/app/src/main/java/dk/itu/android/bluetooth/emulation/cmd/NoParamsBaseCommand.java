package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public abstract class NoParamsBaseCommand extends BaseCommand {

	public NoParamsBaseCommand(CommandType type) {
		super(type,false);
	}
	
	@Override
	protected void sendParameters(OutputStream in) throws IOException {
		//emtpy method
	}

}
