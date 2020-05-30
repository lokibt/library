# Loki BT Library - Bluetooth for the Android emulator

Loki BT allows Android developers to use Bluetooth in the Android emulator that ships with Android Studio by emulating Bluetooth over TCP/IP. Loki consists of a web-service and an Android library. This is the repo of the library, which is licensed as open-source under the GPL version 3.

*Loki BT is still an alpha! Its core functionality is working, but there are still some parts of Android's Bluetooth API missing. Apart from that there could still be undiscovered bugs and breaking interface changes are likely to happen, so it is not recommended to use it in production projects, yet.*

## Installation

To use Loki BT in your project you just have to add the JitPack repository and the Loki BT dependency.

Add the JitPack repository to your project's build-file (*/build.gradle*):

```gradle
allprojects { // not buildscript!
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the Loki BT dependency to your modules's build-file (e.g */app/build.gradle*):

```gradle
dependencies {
    // ...
    implementation 'com.github.lokibt:library:master-SNAPSHOT'
}
```
## Usage

Just use `import com.lokibt.bluetooth.*` instead of `import android.bluetooth.*` to import the Bluetooth classes.

Once you are using the Loki BT classes, you just have to start your app in two emulators and they will be able to discover each other and exchange data via Bluetooth. Of course also different apps that use Loki BT can communicate with each other as long as the emulators run on the same host-system. See [Device groups](#device-groups), if you want to connect to emulators on other systems.

Loki BT aims to be interface compatible with Android's Bluetooth API, so the information in [the official Bluetooth overview for Android](https://developer.android.com/guide/topics/connectivity/bluetooth) and the [Reference of the android.bluetooth package](https://developer.android.com/reference/android/bluetooth/package-summary) also apply to Loki BT. However, the Loki BT implementation is still incomplete, see [Limitations](#limitations) for details.

### Device groups

Loki makes all Android emulators on the same host-system visible to each other by default. You have to set a device group, if you want to connect to emulators on other hosts or want to hide some emulators from other emulators on the same host.

To add an emulator to a device group, you just have to add the name of the device group as an extra to the [REQUEST_ENABLE](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_REQUEST_ENABLE) or [REQUEST_DISCOVERABLE](https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_REQUEST_DISCOVERABLE) intent:

```Java
Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
intent.putExtra(BluetoothAdapter.EXTRA_LOKIBT_GROUP, "com.mydomain.MY_DEVICE_GROUP_NAME");
startActivityForResult(intent, MY_REQUEST_CODE);
```
Please note that the device group names are not checked for uniqueness. It is in your responsibility to prevent naming-collisions with device groups of others. We therefore recommend to prefix your group name with the name of a domain you own.

### Testing on real hardware

If you want to test your app with the Bluetooth hardware on a real device, you have to revert all import statements from `import com.lokibt.bluetooth.*` back to `import android.bluetooth.*`. This also definitely recommended before releasing the app ;)

We plan to make our code aware of real hardware and release builds, so that no extras actions would be required by you.

## Limitations

Re-implementing Android's Bluetooth API is a big task, therefore we concentrate on the core functionality of Bluetooth, sending and receiving Data over a RFCOMM socket, right now. All examples on [the Bluetooth Overview page](https://developer.android.com/guide/topics/connectivity/bluetooth) except for the one about using profiles should work.

The support for **Pairing** is just a stub with no real functionality so far. We plan to add full pairing support as soon as possible.

Everything related to **Bluetooth profiles** has not been implemented at all. We want to support profiles, but not before the core functionality has been fully implemented.

### Class implementation status

* `BluetoothAdapter`: partly implemented
* `BluetoothDevice`: partly implemented
* `BluetoothSocket`: partly implemented
* `BluetoothServerSocket`: partly implemented
* `BluetoothClass`: partly implemented
* `BluetoothProfile`: not implemented
* `BluetoothHeadset`: not implemented
* `BluetoothA2dp`: not implemented
* `BluetoothHealth`: not implemented
* `BluetoothHealthCallback`: not implemented
* `BluetoothHealthAppConfiguration`: not implemented
* `BluetoothProfile.ServiceListener`: not implemented

## Credits

The original version was written by Francesco Zanitti, who [published it on GitHub](https://github.com/cheng81/Android-Bluetooth-Simulator/). However, the repo seems to be abandoned since 2010 and most of the original code has been replaced or rewritten by now.

----

Copyright 2020 Torben Haase \<[https://pixelsvsbytes.com](https://pixelsvsbytes.com)>
