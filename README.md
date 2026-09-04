# Speed Deal

Turn timer for multiplayer card-game tables. Large countdown, whose-turn indicator (P1–P6), swipe-up settings for players and turn length, auto-advance with a clear beep when time runs out, and a big **Next** button to pass early.

Offline, no accounts, no network, no ads.

**Working title:** Speed Deal. This project does not use any trademarked board-game names, logos, or art.

## Defaults

| Setting        | Default / range |
|----------------|-----------------|
| Turn length    | **25 seconds** (min **10**, max **60**) |
| Player count   | **4** (range 2–6) |

Players and turn length live in a **swipe-up ModalBottomSheet**. The main screen shows only the countdown, current seat (P1/P2/…), and **Next**. Opening the sheet does not pause the timer; changing seconds or player count applies immediately.

## Requirements

- JDK 17+
- Android SDK (platform android-35, build-tools 35.x)
- Android device or emulator (minSdk 26)

## Build

```bash
# Point at your SDK (or use Android Studio)
echo "sdk.dir=$ANDROID_HOME" > local.properties

./gradlew assembleDebug
./gradlew assembleRelease
```

Debug APK:

- `app/build/outputs/apk/debug/app-debug.apk`
- also copied to `artifacts/speed-deal-debug.apk`

Release APK (signed with the local sideload keystore):

- `app/build/outputs/apk/release/app-release.apk`
- also copied to `artifacts/speed-deal-release.apk`

## Sideload install

```bash
adb install -r artifacts/speed-deal-debug.apk
# or
adb install -r artifacts/speed-deal-release.apk
```

## Sideload signing (local keystore)

For convenience this repo includes a **local sideload keystore** (not for Play Store upload):

| Field           | Value |
|-----------------|-------|
| File            | `keystore/speed-deal-release.jks` |
| Store password  | `speeddeal` |
| Key alias       | `speeddeal` |
| Key password    | `speeddeal` |

Create a **new** upload keystore before publishing to Google Play. Do not reuse these passwords or this keystore for production Play signing.

## Play Store path (outline)

Carl needs his own [Google Play developer account](https://play.google.com/console/signup) (~**US$25** one-time).

1. Generate a dedicated **upload key** (new keystore; keep it private; back it up).
2. In Play Console: create the app, set package `com.mcx424.speeddeal`, complete store listing (title, short/full description, screenshots, privacy policy if required).
3. Build an **AAB** with the upload key: `./gradlew bundleRelease` (after pointing `signingConfigs.release` at the upload keystore).
4. Upload the AAB to an Internal testing track, then promote to Production when ready.
5. Optionally enroll in Play App Signing so Google holds the app signing key; you keep only the upload key.

## Features

1. Turn length 10–60s (default 25s) in settings sheet
2. Large countdown for the current seat
3. At zero: tone + brief vibrate, timer resets, advances to next player
4. **Next** — same advance + reset
5. Player count 2–6 in settings sheet
6. Dark Material 3 Compose UI; screen stays on while the timer runs
7. Settings sheet does not pause the timer; value changes apply on change

## Tech

- Kotlin, Jetpack Compose, single `MainActivity`
- `applicationId`: `com.mcx424.speeddeal`
- minSdk 26, targetSdk / compileSdk 35
- Gradle Kotlin DSL
- Turn-end sound via `ToneGenerator` (no copyrighted assets)

## License

Personal / private use unless otherwise noted by the repo owner.
