package com.lokibt.emulator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.lokibt.emulator.databinding.ActivityMainBinding;
import com.lokibt.emulator.emulation.cmd.Command;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    EmulatorViewModel viewModel;

    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean bound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            bound = false;
        }
    };

    public void sayHello(View v) {
        Log.d("MAIN", "hello host: " + viewModel.host.getValue());
        if (!bound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, 1, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service
        Intent intent = new Intent();
        intent.setClassName("com.lokibt.emulator", "com.lokibt.emulator.EmulatorService");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

}
