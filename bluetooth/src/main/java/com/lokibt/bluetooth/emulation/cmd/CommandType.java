package com.lokibt.bluetooth.emulation.cmd;

public enum CommandType {
    JOIN(0),
    LEAVE(1),
    DISCOVERY(2),
    ANNOUNCE(3),
    CONNECT(4),
    LINK(5);
    
    int intRepr;

    CommandType(int intRepr) {
        this.intRepr = intRepr;
    }
}
