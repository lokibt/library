package com.lokibt.emulator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class EmulatorService extends Service {
  private final String TAG = this.getClass().getName();
  /**
   * Command to the service to display a message
   */
  static final int MSG_SAY_HELLO = 1;

  /**
   * Handler of incoming messages from clients.
   */
  static class IncomingHandler extends Handler {
    private final String TAG = this.getClass().getName();
    private EmulatorService service;

    IncomingHandler(EmulatorService service) {
      this.service = service;
    }

    @Override
    public void handleMessage(Message msg) {
      Bundle bundle = msg.getData();
      Log.d(TAG, "data received: " + bundle.getString("data"));
      switch (msg.what) {
        case MSG_SAY_HELLO:
          service.sayHello();
          break;
        default:
          super.handleMessage(msg);
      }

      if (msg.replyTo != null) {
        Message replyMsg = Message.obtain(this);
        Bundle replyBundle = new Bundle();
        replyBundle.putString("data", "Hello from service");
        replyMsg.setData(replyBundle);
        try {
          msg.replyTo.send(replyMsg);
        } catch (RemoteException e) {
          Log.e(TAG, "unable to communicate with service", e);
        }
      }
    }
  }

  /**
   * Target we publish for clients to send messages to IncomingHandler.
   */
  Messenger mMessenger;

  /**
   * When binding to the service, we return an interface to our messenger
   * for sending messages to the service.
   */
  @Override
  public IBinder onBind(Intent intent) {
    Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();

    int port = intent.getIntExtra("port", 0);
    if (port != 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket socket = null;
                    try {
                        Log.d(TAG, "trying to connect...");
                        Log.d(TAG, "port: " + port);
                        socket = new Socket("127.0.0.1", port);
                        Log.d(TAG, "connected");
                    } catch (Exception e) {
                        Log.e(TAG, "error while connecting...", e);
                    }
                    try {
                        socket.close();
                    } catch (Exception e) {
                        Log.e(TAG, "unable to close socket", e);
                    }
                }
            }).start();
    }

    mMessenger = new Messenger(new IncomingHandler(this));
    return mMessenger.getBinder();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d("btcompanion", "Service created");
  }

  void sayHello() {
      Toast.makeText(this, "hellow!", Toast.LENGTH_SHORT).show();
  }
}
