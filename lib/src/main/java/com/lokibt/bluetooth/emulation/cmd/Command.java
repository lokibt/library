package com.lokibt.bluetooth.emulation.cmd;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Command implements Runnable {
    final static String TAG = "BTEMU_CMD";
    protected Socket socket;
    protected InputStream in;
    protected OutputStream out;
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
        if (this.callback != null) {
            callback.onFinish();
        }
    }

    public Socket open() throws IOException {
        Log.d(TAG, "opening socket and streams....");
        //this.socket = new Socket("10.0.2.2", 8199);
        this.socket = new Socket("letorbi.de", 8199);
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
    }

    protected void writePreamble() throws IOException {
        sendParameter(Integer.toString(this.type.intRepr));
        sendParameter(com.lokibt.bluetooth.BluetoothAdapter.getDefaultAdapter().getAddress());
    }

    protected void sendParameter(String value) throws IOException {
        out.write((value + "\n").getBytes("UTF8"));
    }

    protected abstract void sendParameters() throws IOException;

    protected abstract void readResponse() throws IOException;
}
