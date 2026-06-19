# PrivateVault

PrivateVault is a local-first Android private media vault.

The first implementation slice focuses on the smallest useful product loop:

```text
App lock -> default board -> import or move -> remove original from public media -> local vault UI -> export/delete boundaries
```

## Current Status

- Android project skeleton created.
- Jetpack Compose and Material 3 UI shell added.
- App lock is part of the first-stage app flow.
- Default board state is modeled in pure Kotlin.
- Unit tests define the first state-model behavior.
- Real media import, Room persistence, Photo Picker, MediaStore export, and MediaStore delete requests are still the next implementation steps.

## Move/Cut Requirement

The user can choose copy or move/cut:

- Copy stores a vault copy and keeps the original in the system gallery, so two files exist.
- Move/cut copies the media into app-private storage and then requests removal of the original public media item from the system media library.

After a successful move/cut, normal gallery pickers, including share flows in apps such as WeChat that rely on public media, should no longer show that item.

If Android does not grant deletion, the app must show the state as imported but still visible outside the vault. It must not call that result a completed move.

## Security Boundary

Stage one uses Android App-specific storage plus an App lock flow. It does not claim file-level encryption. Strong file encryption and encrypted backup packages are future work.

## Build Prerequisites

- Android SDK with compile SDK 35.
- JDK compatible with the selected Android Gradle Plugin.
- Gradle or a generated Gradle wrapper.

This workspace currently does not include a Gradle wrapper. Generate one from a machine with Gradle installed, or open the project in Android Studio and sync from there.

## Useful Commands

```powershell
gradle :app:testDebugUnitTest
gradle assembleDebug
```
