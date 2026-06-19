# Room Media Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist the private library model with Room and connect real image picking/copying/move-request behavior.

**Architecture:** Keep domain models in `core`, add Room entities/DAO/database/repository in `data`, and add Android media copying/removal in `media`. The Compose app will observe a repository-backed state holder instead of relying only on `remember`.

**Tech Stack:** Kotlin, Jetpack Compose, Room, coroutines/Flow, Android Photo Picker, MediaStore delete request.

---

### Task 1: Room Schema And Mapping

**Files:**
- Create: `app/src/main/java/com/privatevault/data/VaultEntities.kt`
- Create: `app/src/main/java/com/privatevault/data/VaultMappers.kt`
- Test: `app/src/test/java/com/privatevault/data/VaultMappersTest.kt`

- [ ] Write a failing mapper test proving libraries, movies, images, links, and tags round-trip into domain objects.
- [ ] Implement minimal entity and mapper code.
- [ ] Run `.\gradlew.bat :app:testDebugUnitTest --console=plain`.

### Task 2: DAO, Database, Repository

**Files:**
- Create: `app/src/main/java/com/privatevault/data/VaultDao.kt`
- Create: `app/src/main/java/com/privatevault/data/VaultDatabase.kt`
- Create: `app/src/main/java/com/privatevault/data/VaultRepository.kt`
- Modify: `app/build.gradle.kts`

- [ ] Add Room/coroutines dependencies and compiler plugin.
- [ ] Add DAO methods for library CRUD, movie image insertion, and state loading.
- [ ] Repository exposes `Flow<VaultAppState>` and suspend operations.
- [ ] Run unit tests and debug build.

### Task 3: Media Import

**Files:**
- Create: `app/src/main/java/com/privatevault/media/PrivateMediaImporter.kt`
- Create: `app/src/main/java/com/privatevault/media/MediaStoreOriginalRemovalGateway.kt`
- Modify: `app/src/main/java/com/privatevault/ui/PrivateVaultApp.kt`
- Modify: `app/src/main/java/com/privatevault/MainActivity.kt`

- [ ] Use Photo Picker for image/video selection.
- [ ] Copy selected content URIs into app-specific storage.
- [ ] For migration mode, request MediaStore original deletion after copy succeeds.
- [ ] Store import result and image path in Room.

### Task 4: UI Integration

**Files:**
- Modify: `app/src/main/java/com/privatevault/ui/PrivateVaultApp.kt`
- Create: `app/src/main/java/com/privatevault/ui/VaultViewModel.kt`

- [ ] Replace in-memory state mutations with ViewModel repository calls.
- [ ] Keep current back-stack behavior for details pages.
- [ ] Show pending import result text when original deletion is denied or fails.
- [ ] Run tests and `assembleDebug`.
