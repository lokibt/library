package com.lokibt.bluetooth.emulation.cmd;

import android.util.Log;

import com.lokibt.bluetooth.emulation.BluetoothAdapterEmulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class Join extends Command {
    private int duration;
    private Timer timer;

    public Join(CommandCallback callback, int duration) {
        super(CommandType.JOIN, callback);
        this.duration = duration;
    }

    @Override
    public void close() {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Join.this.closeImmediately();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error while closing JOIN socket via timer: " + e);
                }
            }
        }, duration * 1000);
    }

    public void closeImmediately() throws IOException {
        timer.cancel();
        super.close();
    }

    @Override
    protected void readResponse() throws IOException {
    }

    @Override
    protected void sendParameters() throws IOException {
    }

}
