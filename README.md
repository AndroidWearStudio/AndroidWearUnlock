# Work In Progress

## To Do
- Start WearUnlockService once user has configured a password
- Start WearUnlockService on boot if application is enabled and a password is set
- Remove all Pebble stuff
- Remove MainActivity
- Add persistent notification (low priority) to prevent WearUnlockService from being killed.
- Improve onboarding for Android Wear
-- Select from a connected peer (could be more than one wearable peer available!)
- Launcher icon

** To test against Android Wear emulator
- Install Wear Unlock to a physical device
- Start Android Wear emulator
- On device:  Pair with new device, select emulator
- On computer running emulator:  adb -s 0146914817008015 forward tcp:5601 tcp:5601  (where 0146914817008015 is the serial shown in adb devices for physical device running Wear Unlock)