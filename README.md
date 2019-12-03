# Android Bluetooth Simulator

## What is it

It's a tcp-based implementation of part of the android bluetooth API.
As for now, you can communicate between different emulators using the RFComm protocol, you can start a discovery phase and enable/disable the bluetooth.

What you need to do in order to use the simulator instead of the android API, is to change the import from `android.bluetooth` to `dk.itu.android.bluetooth` (and also add the `INTERNET` permission in the android manifest file).

There is a slight modification to the code, in order to use the simulator:

 - call `BluetoothAdapter.SetContext(this);` at some point (the `onCreate` method is fine) in your activity/service.

## How do I use it?

You will have to follow some steps:

 - download everything in this repository
 - compile and install into at least 2 android emulators the Android application fonr in */app*
 - compile the *btsim-server* and run it.
     - there is an already precompiled jar in *dist*
     - execute `java -cp btsimserver.jar dk.itu.btemu.Server --help` to see some options
     - if you don't have the `adb` command in your path, you will need to set the `adb.path` variable

You need to install the application because it will handle the *system activities* of the bluetooth, like switching on/off the radio and start a discovery.

Then you can create a new android project.
My preferred workflow, at this point, is:

 - add the necessary permissions for the bluetooth AND for internet access (required by the bt simulator).
 - add the *btsim.jar* to the libraries of the project
 - add the call to `BluetoothAdapter.SetContext(this);` in the `onCreate` method of the activity
 - start using the bluetooth API, importing the `dk.itu.android.bluetooth` version
 - set up two launch configurations for the project (one for each android emulator)

Then, depending on your needs, when you want to deploy the application on an actual device, you will need to delete or comment the `SetContext` call (as this will not compile, since it is not part of the android bluetooth api, but just a custom call for the simulator) and delete and re-import all the bluetooth stuff (this time using the `android.bluetooth` classes).

I hope this will be useful for somebody, I know that it implements just a subset of the API and it is not possible to put into play different devices than the emulators themselves, but until we got something in the android emulator itself, this is what we got :D

## Adding dummy devices

You can add dummy devices to the server, so that you can check if the discovery phase works. This involves communicating directly with the bluetooth emulator server, which accept text-based command.

First of all, fire up a terminal and telnet to localhost, port 8199:

	telnet localhost 8199

when the terminal prompt, use the following syntax:

	0]tcp.address=10.0.2.2&not.android.emulator=true&bt.address=<address>&device.name=<name>]

e.g. :

	0]tcp.address=10.0.2.2&not.android.emulator=true&bt.address=00:11:22:AA:BB:CC&device.name=Dummy1]

In general, the bluetooth emulator server accepts commands on the form:

	<command.identifier>]<param.name>=<param.value>&...]

Another useful command is the discovery one, if you want to know which devices are listed in the server, telnet and enter:

	2]]

then the list of the devices and relative registered services will appear.
