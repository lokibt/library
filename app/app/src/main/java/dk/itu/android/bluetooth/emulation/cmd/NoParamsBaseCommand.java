package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;
import java.io.OutputStream;

public abstract class NoParamsBaseCommand extends BaseCommand {

	public NoParamsBaseCommand(CommandType type) {
		super(type,false);
	}
	
	@Override
	protected void sendParameters(OutputStream in) throws IOException {
		//emtpy method
	}

}
