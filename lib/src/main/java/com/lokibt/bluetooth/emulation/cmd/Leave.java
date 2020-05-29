package com.lokibt.bluetooth.emulation.cmd;

import com.lokibt.bluetooth.emulation.BluetoothAdapterEmulator;

import java.io.IOException;

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
