# Fix Android Resource Linking Error (Missing ic_launcher)

The project is failing to build because the `ic_launcher` and `ic_launcher_round` resources referenced in `AndroidManifest.xml` are missing from the `res/mipmap` directories.

## Proposed Changes

### [app] Resource Components

I will create basic adaptive launcher icons to resolve the build error. Since the `minSdk` is 26, we can use adaptive icons in the `mipmap-anydpi-v26` configuration.

#### [NEW] [ic_launcher.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
- Define an adaptive icon using a background color and a foreground vector.

#### [NEW] [ic_launcher_round.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
- Define a round adaptive icon (usually identical to the main one for adaptive icons).

#### [NEW] [ic_launcher_foreground.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_foreground.xml)
- A simple vector drawable to serve as the icon's foreground.

#### [NEW] [ic_launcher_background.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_background.xml)
- A simple color or vector drawable for the background. (Alternatively, I can use `@color/primary` directly in the adaptive icon).

## Verification Plan

### Automated Tests
- Run `./gradlew :app:processDebugResources` to verify that resource linking now succeeds.
- Run a full build: `./gradlew assembleDebug`.
