# Wear Unlock

A simple Android app that enables functionality similiar to the "Trusted Device" unlock features for Android Wear that will eventually show up in Android L. Supported on Jellybean and above.

Once you have paired your Android Wear device to this app, it will lock your device with a specified password when your Android Wear device has been disconnected - and unlock your device whenever your Android Wear device is connected.

## To test against Android Wear emulator
- Install Wear Unlock to a physical device
- Start Android Wear emulator
- On device:  Pair with new device, select emulator
- On computer running emulator:  adb -s 0146914817008015 forward tcp:5601 tcp:5601  (where 0146914817008015 is the serial shown in adb devices for physical device running Wear Unlock)