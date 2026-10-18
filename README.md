# Block 432

A tiny Android app that rejects incoming calls from the 432 area code unless the number is saved in your contacts. Works on Android 10+ (Galaxy S26 Ultra included).

## Build the APK (no computer setup needed)
1. Create a new GitHub repository and upload everything in this folder.
2. Open the repo's **Actions** tab — the "Build APK" job runs automatically (~3 min).
3. Open the finished run and download **Block432-apk** → unzip → `app-debug.apk`.

Or open the folder in Android Studio and press Run.

## Install and set up
1. Open `app-debug.apk` on the phone and allow "Install unknown apps" when asked.
2. Open **Block 432** → allow Contacts → choose Block 432 as the **Caller ID & spam app** (call screening app).
3. Done. The toggle turns blocking on/off; the screen shows how many calls were blocked.

## Notes
- Blocked calls are rejected silently but still appear in your call log.
- Only one call screening app can be active; choosing this one replaces the default spam filter in that slot.
- If Contacts access is ever revoked, the app lets every call through rather than risk blocking a saved contact.
