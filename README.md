## Pa$$$afe
Simple Android app that can save your passwords.
### Summary
The application allows to create records with a login, password and description, view them and delete them. All records are stored in an encrypted database and are protected by a common password that use to access to the application.
### Key Features
- DB records are encrypted by the AES 256 algorithm
- Secret key stored using Android keystore system
- Application access password is not saved on the device
- Application access password can be changed
- The screen brightness reduction button hides the secret data
### Working with application
![PaSSSafe working](https://user-images.githubusercontent.com/100992310/163045223-b920ef6f-d030-439e-8e22-80638f781507.gif)
### References
- Navigation in app: https://developer.android.com/guide/navigation/navigation-getting-started
- Fragments: https://developer.android.com/guide/fragments
- Room database: https://developer.android.com/training/data-storage/room/
- Keystore system: https://developer.android.com/training/articles/keystore#ExtractionPrevention
