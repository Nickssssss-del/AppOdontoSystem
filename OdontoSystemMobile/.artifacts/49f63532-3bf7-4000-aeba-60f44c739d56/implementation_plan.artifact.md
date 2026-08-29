# Implementation Plan - Fix Gradle Sync Error (Incompatible Gradle Version)

The project is currently failing to sync because it uses Gradle 9.7.1, which is incompatible with the Android Gradle Plugin (AGP) 8.2.0 and Kotlin Gradle Plugin (KGP) 1.9.20. Specifically, Gradle 9.0 removed the `HasConvention` class which these plugins rely on.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle-wrapper.properties](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/gradle/wrapper/gradle-wrapper.properties)
- Downgrade `distributionUrl` from Gradle `9.7.1` to `8.7`. This version is stable and compatible with AGP 8.2.0.

#### [MODIFY] [build.gradle](file:///C:/Users/DeLaC/.gemini/antigravity/scratch/OdontoSystemMobile/build.gradle)
- Update the `clean` task to use the modern `layout.buildDirectory` instead of the deprecated `buildDir` property (optional but recommended for Gradle 8.x).

## Verification Plan

### Automated Tests
- Run `gradlew help` to verify that the build configuration is now valid.
- Trigger a Gradle Sync via the IDE to ensure the "MultipleBuildFailures" error is resolved.

### Manual Verification
- Verify that the `app` module is correctly recognized by the IDE after a successful sync.
