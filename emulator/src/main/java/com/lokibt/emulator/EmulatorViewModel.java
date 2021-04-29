package com.lokibt.emulator;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lokibt.emulator.emulation.cmd.Command;

public class EmulatorViewModel extends ViewModel {
    public MutableLiveData<String> host = new MutableLiveData<>();
    public MutableLiveData<String> port = new MutableLiveData<>();
    public MutableLiveData<String> group = new MutableLiveData<>();

    public EmulatorViewModel() {
        host.setValue(Command.host);
        port.setValue("" + Command.port);
        group.setValue(Command.group);
    }
}
