package dk.itu.android.bluetooth.emulation.cmd;

import java.io.IOException;

import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;

public class Leave extends Command {
    
    public Leave() {
        super(CommandType.LEAVE, null);
    }

    @Override
    protected void readResponse() throws IOException {
        //don't care about response data
        BluetoothAdapterEmulator.getInstance().onLeaveReturned();
    }

    @Override
    protected void sendParameters() throws IOException {
    }

}
