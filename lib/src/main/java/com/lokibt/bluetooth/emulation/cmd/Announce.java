package com.lokibt.bluetooth.emulation.cmd;

import java.io.IOException;
import java.util.UUID;

public class Announce extends Command {
    UUID uuid;

    public Announce(UUID uuid) {
        super(CommandType.ANNOUNCE, null);
        this.uuid = uuid;
    }

    @Override
    public void close() throws IOException {
        // the connection shall stay open
    }

    @Override
    protected void readResponse() throws IOException {
    }

    @Override
    protected void sendParameters() throws IOException {
        sendParameter(this.uuid.toString());
    }

}
