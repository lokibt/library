package com.lokibt.emulator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.lokibt.emulator.databinding.ActivityMainBinding;
import com.lokibt.emulator.emulation.cmd.Command;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();
    ActivityMainBinding binding;
    EmulatorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EmulatorViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);

        binding.setViewModel(viewModel);
        setContentView(binding.getRoot());

        viewModel.host.observe(
            this, new Observer<String>() {
                @Override
                public void onChanged(String value) {
                    Log.d("MAIN", "host changed: " + value);
                    Command.host = value;
                }
            });

        viewModel.port.observe(
            this, new Observer<String>() {
                @Override
                public void onChanged(String value) {
                    Log.d("MAIN", "port changed: " + value);
                    Command.port = Integer.parseInt(value);
                }
            });


        viewModel.group.observe(
            this, new Observer<String>() {
                @Override
                public void onChanged(String value) {
                    Log.d("MAIN", "group changed: " + value);
                    Command.group = value;
                }
            });
    }
}
