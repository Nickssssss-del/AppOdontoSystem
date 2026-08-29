# Fix Android Resource Linking Error (Missing ic_launcher)

The project fails to build because the launcher icons (`ic_launcher` and `ic_launcher_round`) referenced in the `AndroidManifest.xml` are missing from the `res/mipmap` folders.

## Proposed Changes

I will create a set of default adaptive launcher icons to resolve the resource linking error and allow the project to build successfully.

### Resource Files

#### [NEW] [ic_launcher_background.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_background.xml)
Create a background vector drawable using the app's primary color.

#### [NEW] [ic_launcher_foreground.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_foreground.xml)
Create a simple foreground vector drawable (placeholder).

#### [NEW] [ic_launcher.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
Define the adaptive icon.

#### [NEW] [ic_launcher_round.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
Define the round adaptive icon.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:processDebugResources` to verify that resource linking no longer fails.
- Run `./gradlew assembleDebug` to ensure the full build succeeds.
