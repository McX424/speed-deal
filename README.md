# Speed Deal

Single repeating turn timer for card-game tables. Huge countdown, a clear beep + short vibration when time runs out, then the next turn starts automatically. Big **Start/Stop** and **Reset** buttons are always on screen.

Offline, no accounts, no network, no ads. Personal use only (not for any app store).

## Defaults

| Setting        | Default / range |
|----------------|-----------------|
| Turn length    | **25 seconds** (min **10**, max **60**) |

Turn length lives in a **ModalBottomSheet** opened from the small "Turn length · 25s" row under the buttons. The main screen shows the header, the countdown with its status (READY / TURN n / PAUSED / TIME UP) and a thin progress bar, then **Start/Stop** and **Reset**. A fresh open is idle (no auto-start). Opening the sheet does not pause the timer; changing seconds applies immediately.

## Controls

| Button       | Behaviour |
|--------------|-----------|
| **Start/Stop** | Toggles. Idle or paused → START counts down. Running → STOP pauses. Stopping during TIME UP cancels the auto-restart and loads a fresh turn. |
| **Reset**    | Returns the timer to the selected turn length. If it is running, the next turn starts at once (one tap to pass the turn). If it is stopped, it stays stopped. |

At zero: tone + vibrate, **TIME UP** pulses (muted red) for 2 s, then the next turn starts automatically.

## End-of-turn warning

The countdown (and its progress bar) warms up smoothly as the turn runs out: **#ECECEC → amber #E0A84E → muted red #E5534B**.

| | |
|---|---|
| Warming starts | `max(25% of turn, 10 s)` left, capped at the turn length (25 s turn → 10 s, 60 s turn → 15 s) |
| Pure amber | 40% of the way through the ramp (8 s left on a 25 s turn) |
| Full red | 5 s left, held until TIME UP |
| Pulse | final 5 s: gentle ~1 Hz breathe (scale 1.00→1.04, alpha 1.00→0.85, eased), drawn with `graphicsLayer` |

The colour follows a per-frame interpolated remaining time (no once-a-second steps) and is read only in draw lambdas, so nothing recomposes or relayouts per frame. Digits are tabular, so nothing shifts.

- **Reset / new turn** (including the automatic next turn after TIME UP): back to white, no pulse, on the next frame.
- **Paused**: holds the current colour, dimmed; no pulse.
- **Reduce motion** (system *Animator duration scale* off, or Accessibility *Remove animations*): no pulse and no TIME UP flash; the colour ramp stays.

## Header logo (personal builds only)

The header can show the **Monopoly Deal** logo with **SPEED** underneath. The logo is a Hasbro trademark, so it is **not** in this public repo:

- Personal builds put the image at `app/src/main/res/drawable-nodpi/logo_monopoly_deal.png`. That path is in `.gitignore`.
- The app looks the drawable up at runtime. If it is missing (as in this repo), the header shows a plain **SPEED / DEAL** text wordmark instead, so the public repo always builds.
- Gradle prints `Speed Deal: personal logo FOUND` or `... no personal logo ...` at configuration time, so you can tell which kind of APK you are building.
- **Never commit or publish** an APK built with the logo (no GitHub releases, no `artifacts/`).

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

Debug APK: `app/build/outputs/apk/debug/app-debug.apk` (installs as a separate app, `com.mcx424.speeddeal.debug`).

Release APK (signed with the local sideload keystore): `app/build/outputs/apk/release/app-release.apk`.

The committed `artifacts/speed-deal-release.apk` is a **logo-free** release build (text wordmark header).

## Screenshot tests

```bash
./gradlew recordRoborazziDebug   # Robolectric render at 411x914dp @ 420dpi (~1080x2400)
```

Writes `idle`, `running`, `paused`, `timeup`, `warn-amber`, `warn-red` and `warning-frames/` PNGs to `app/build/outputs/roborazzi/` (git-ignored). If the personal logo is present they include it, so don't commit them.

## Sideload install

```bash
adb install -r artifacts/speed-deal-release.apk
```

Release builds are all signed with the same keystore, so `-r` updates an existing install in place and keeps settings.

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

1. Single repeating turn timer, 10–60 s (default 25 s), set in a bottom sheet
2. Huge bold countdown with tabular figures, thin progress bar, status label
3. Fresh open: idle (no auto-start)
4. **Start/Stop** toggles; **Reset** restores the turn length (and starts the next turn if running)
5. At zero: tone + brief vibrate, TIME UP pulse, then the next turn starts automatically
6. Start/Stop and Reset always visible (idle, running, paused, time-up), 84 dp tall
7. Grok-style dark theme: #0A0A0A background, #121212 surfaces, #262626 borders, #ECECEC / #9A9A9A text, cool-grey #E4E8EE highlights; dark status and navigation bars
8. Screen stays on while the timer runs
9. End-of-turn warning: smooth white → amber → red ramp and a gentle final-5 s pulse (respects reduce motion)

## Tech

- Kotlin, Jetpack Compose, single `MainActivity`
- `applicationId`: `com.mcx424.speeddeal`
- minSdk 26, targetSdk / compileSdk 35
- Gradle Kotlin DSL
- Turn-end sound via `ToneGenerator`
- Optional header logo resolved at runtime (`Resources.getIdentifier`) so it can stay out of git

## License

Personal / private use unless otherwise noted by the repo owner.
