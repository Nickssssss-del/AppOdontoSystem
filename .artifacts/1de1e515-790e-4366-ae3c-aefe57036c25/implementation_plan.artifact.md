# Implementation Plan - Fix Missing Launcher Icons

The project fails to build because the `mipmap/ic_launcher` and `mipmap/ic_launcher_round` resources referenced in `AndroidManifest.xml` are missing. I will create a set of default adaptive icons to resolve this build error.

## User Review Required

> [!NOTE]
> I am creating placeholder adaptive icons (vector-based). If you have specific branding assets (PNGs or SVGs), you may want to replace these later.

## Proposed Changes

### Android Resources

I will create the necessary directory structure and files for adaptive icons.

#### [NEW] [ic_launcher_background.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_background.xml)
A simple vector drawable for the background of the adaptive icon.

#### [NEW] [ic_launcher_foreground.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_foreground.xml)
A simple vector drawable for the foreground of the adaptive icon (e.g., a tooth icon or a medical cross).

#### [NEW] [ic_launcher.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
The adaptive icon definition for `ic_launcher`.

#### [NEW] [ic_launcher_round.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
The adaptive icon definition for `ic_launcher_round`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:processDebugResources` to verify that AAPT no longer reports missing resources.
- Run a full build: `./gradlew assembleDebug`.

### Manual Verification
- Deploy the app to a device or emulator and verify that a launcher icon is visible.
