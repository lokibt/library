package com.lokibt.bluetooth.emulation.cmd;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class Link extends Command {
    private String addr;
    private String connId;
    private UUID uuid;

    public Link(String addr, UUID uuid, String connId) {
        super(CommandType.LINK, null);
        this.addr = addr;
        this.connId = connId;
        this.uuid = uuid;
    }

    @Override
    public Socket open() throws IOException {
        super.open();
        super.writePreamble();
        sendParameter(this.addr);
        sendParameter(this.uuid.toString());
        sendParameter(this.connId);
        return this.socket;
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
        // parameters have already been set via open()
    }

    @Override
    protected void writePreamble() throws IOException {
        // preamble has already been set via open()
    }
}
