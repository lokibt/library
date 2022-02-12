package com.lokibt.bluetooth.emulation.cmd;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Command implements Runnable {
    final static String TAG = "BTEMU_CMD";

    public static String host = "letorbi.de";
    public static int port = 8198;
    public static String group = "";

    Socket socket;
    InputStream in;
    OutputStream out;

    private CommandCallback callback;
    private CommandType type;

    public Command(CommandType type, CommandCallback callback) {
        this.callback = callback;
        this.type = type;
        this.in = null;
        this.out = null;
        this.socket = null;
    }

    @Override
    public void run() {
        try {
            if (this.socket == null) {
                this.open();
            }
            this.execute();
        } catch (Exception e) {
            Log.e(TAG, "error while executing command " + type.name(), e);
        } finally {
            if (this.socket != null) {
                try {
                    this.close();
                } catch (Exception e) {
                    Log.e(TAG, "error while closing socket and streams", e);
                }
            }
        }
        this.finish();
    }

    public Socket open() throws IOException {
        Log.d(TAG, "opening socket and streams to " + Command.host + ":" + Command.port);
        this.socket = new Socket(Command.host, Command.port);
        this.out = this.socket.getOutputStream();
        this.in = this.socket.getInputStream();
        return this.socket;
    }

    public void execute() throws IOException {
        Log.d(TAG, "executing command...");
        writePreamble();
        sendParameters();
        readResponse();
    }

    public void close() throws IOException {
        Log.d(TAG, "closing socket and streams....");
        this.out.close();
        this.in.close();
        this.socket.close();
        if (this.callback != null) {
            callback.onClose(this);
        }
    }

    public CommandType getType() {
        return this.type;
    }

    protected void finish() {
        if (this.callback != null) {
            callback.onFinish(this);
        }
    }

    protected void writePreamble() throws IOException {
        sendParameter(Command.group);
        sendParameter(Integer.toString(this.type.intRepr));
        sendParameter(com.lokibt.bluetooth.BluetoothAdapter.getDefaultAdapter().getAddress());
    }

    protected void sendParameter(String value) throws IOException {
        out.write((value + "\n").getBytes("UTF8"));
    }

    protected abstract void sendParameters() throws IOException;

    protected abstract void readResponse() throws IOException;
}
