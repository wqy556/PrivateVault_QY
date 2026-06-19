# PrivateVault Stage One Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first Android/Compose project slice for PrivateVault: app lock entry, local-first navigation shell, default board model, and the first-stage import/move semantics.

**Architecture:** Keep stage one small and local. Pure Kotlin state models live outside Compose so they can be tested without Android runtime, while Compose screens consume those models through a thin app shell. The move/cut flow must model two outcomes: moved, where the original public MediaStore item was removed, and imported-but-visible, where deletion was denied or failed.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose, Material 3, JUnit.

---

### Task 1: Project Skeleton

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add Gradle project files**

Create a single-module Android app using package `com.privatevault`.

- [ ] **Step 2: Verify tooling**

Run: `gradle --version` or `.\gradlew.bat --version`

Expected: local machine reports a Gradle installation or wrapper availability.

### Task 2: State Model Test First

**Files:**
- Create: `app/src/test/java/com/privatevault/core/VaultAppStateTest.kt`
- Create: `app/src/main/java/com/privatevault/core/VaultAppState.kt`

- [ ] **Step 1: Write failing tests**

Test that a fresh app starts locked, contains the default board, unlocks with a non-blank passcode, and relocks when returning from background.

- [ ] **Step 2: Run tests and confirm RED**

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: tests fail because `VaultAppState` does not exist yet.

- [ ] **Step 3: Implement minimal state model**

Add immutable state classes and reducer-like methods for unlock, lock, import placeholder, and board selection.

- [ ] **Step 4: Run tests and confirm GREEN**

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: tests pass.

### Task 3: Compose UI Shell

**Files:**
- Create: `app/src/main/java/com/privatevault/MainActivity.kt`
- Create: `app/src/main/java/com/privatevault/ui/PrivateVaultApp.kt`
- Create: `app/src/main/java/com/privatevault/ui/theme/PrivateVaultTheme.kt`

- [ ] **Step 1: Implement app entry**

Create `MainActivity` using Compose and `PrivateVaultTheme`.

- [ ] **Step 2: Implement screens**

Create locked entry, home, boards, import, and settings screens with Material 3 components and accessible labels.

- [ ] **Step 3: Build**

Run: `.\gradlew.bat assembleDebug`

Expected: debug APK builds when Android SDK, JDK, and Gradle are installed.

### Task 4: Documentation Verification

**Files:**
- Modify: `设计.md`
- Create: `README.md`

- [ ] **Step 1: Confirm design wording**

Ensure the doc states that stage one uses App-specific storage plus App lock, not file-level encryption.

- [ ] **Step 2: Add README**

Document local-first scope, build prerequisites, and current implementation status.

### Task 5: Move/Cut MediaStore Boundary

**Files:**
- Create: `app/src/main/java/com/privatevault/core/ImportMode.kt`
- Create: `app/src/main/java/com/privatevault/core/ImportResult.kt`
- Create: `app/src/main/java/com/privatevault/media/OriginalMediaRemovalGateway.kt`
- Test: `app/src/test/java/com/privatevault/core/ImportResultTest.kt`

- [ ] **Step 1: Write failing tests**

Test that copy-only import stays externally visible, while move/cut is complete only after original removal succeeds.

- [ ] **Step 2: Implement pure result model**

Add `ImportMode.CopyOnly`, `ImportMode.MoveAndHideOriginal`, and result states for copied, moved, and copied but original still visible.

- [ ] **Step 3: Add Android gateway interface**

Define an interface that later wraps `MediaStore.createDeleteRequest` on Android 11+ and recoverable delete flows on older scoped-storage devices.
