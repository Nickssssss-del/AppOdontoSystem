# Fix missing launcher icons error

The project fails to build because `ic_launcher` and `ic_launcher_round` resources are referenced in `AndroidManifest.xml` but do not exist in the `res/mipmap` directories. I will add default adaptive icons to resolve this.

## Proposed Changes

### [App Resources]

#### [NEW] [ic_launcher_background.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/values/ic_launcher_background.xml)
Create a background color resource for the adaptive icon.

#### [NEW] [ic_launcher_foreground.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/drawable/ic_launcher_foreground.xml)
Create a simple vector foreground for the adaptive icon.

#### [NEW] [ic_launcher.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
Define the adaptive icon.

#### [NEW] [ic_launcher_round.xml](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
Define the round adaptive icon.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the resource linking error is resolved and the project builds successfully.
