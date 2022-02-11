package com.lokibt.bluetooth.emulation;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Utils {
    private static final String TAG = "DeviceInfo";

    public static long getIPSeed() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        Log.d(TAG, "found IP address: " + sAddr);
                        if (sAddr.indexOf(':') < 0) { // IPv4
                            return Long.decode(sAddr.replace(".", ""));
                        }
                        else { //IPv6
                            // drop ip6 zone suffix if necessary
                            int zoneDelim = sAddr.indexOf('%');
                            if (zoneDelim > 0)
                                sAddr = sAddr.substring(0, zoneDelim);
                            // stripping to 56 bits since decode cannot handle 64bit values :/
                            return Long.decode("0x" + sAddr.substring(7).replace(":", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while generating IP seed, using default", e);
        }
        return Long.decode("0xCAFFEEBABE");
    }

    public static long getDeviceSeed(Context context) {
        String seedStr;
        try {
            FileInputStream fis = context.openFileInput("BTSEED.TXT");
            Log.d(TAG, "reading device seed");
            byte[] buf = new byte[100];
            int read = fis.read(buf);
            seedStr = new String(buf, 0, read);
            fis.close();
        } catch (Exception readException) {
            Log.d(TAG, "error while reading address", readException);
            try {
                Log.d(TAG, "generating device seed");
                Random r = new Random();
                // stripping to 56 bits since decode cannot handle 64bit values :/
                seedStr = "0x" + Long.toHexString(r.nextLong()).substring(1);
                FileOutputStream fos = context.openFileOutput("BTSEED.TXT", Context.MODE_PRIVATE);
                fos.write(seedStr.getBytes());
                fos.close();
            } catch (Exception writeException) {
                Log.e(TAG, "exception while generating device seed, using default", writeException);
                seedStr = "0xCAFFEEBABE";
            }
        }
        return Long.decode(seedStr);
    }
}
