package dk.itu.android.bluetooth.emulation.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dk.itu.android.bluetooth.emulation.BluetoothAdapterEmulator;

public class Join extends Command {

    public Join() {
           super(CommandType.JOIN, null);
    }

    @Override
    protected void readResponse() throws IOException {
        //get the emulator port number
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        BluetoothAdapterEmulator.getInstance().onJoinReturned();
    }

    @Override
    protected void sendParameters() throws IOException { }

}
