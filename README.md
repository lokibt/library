# Loki BT Library - Bluetooth for the Android emulator

Loki BT allows Android developers to use Bluetooth in the Android emulator that ships with Android Studio by emulating Bluetooth over TCP/IP. Loki consists of a web-service and an Android library. This is the repo of the library, which is licensed as open-source under the GPL version 3.

*Loki BT is still an alpha! Its core functionality is working, but there are still some parts of Android's Bluetooth API missing. Apart from that there could still be undiscovered bugs and breaking interface changes are likely to happen, so it is not recommended to use it in production projects, yet.*

## Installation

To use Loki BT to your project you just have to add the JitPack repository and the Loki BT dependency.

Add the JitPack repository to your project's *build.gradle* file:

```gradle
allprojects { // not buildscript!
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the Loki BT dependency to your modules's *build.gradle* file (e.g */app/build.gradle*):

```gradle
dependencies {
    // ...
    implementation 'com.github.lokibt:library:master-SNAPSHOT'
}
```

## Credits

The original version was written by Francesco Zanitti, who [published it on GitHub](https://github.com/cheng81/Android-Bluetooth-Simulator/). However, his repo seems to be abandoned since 2010 and most of the original code has been replaced or rewritten by now.

----

Copyright 2020 Torben Haase \<[https://pixelsvsbytes.com](https://pixelsvsbytes.com)>
