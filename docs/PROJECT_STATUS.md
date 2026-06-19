# PrivateVault Project Status

## Current Goal

Build an Android local private media library app where users can organize multiple libraries and movies, copy or migrate selected images/videos into app-private storage, and clearly know whether migrated originals were removed from public media locations such as Gallery or WeChat pickers.

## Completed Work

1. Git repository initialized.
2. Initial UI design committed as `5031a21 chore: initial private vault ui design`.
3. Library management interactions committed as `e172c81 feat: add library management interactions`.
4. Room persistence layer added for libraries, movies, movie images, links, tags, and movie-tag relationships.
5. Repository/ViewModel flow added so Compose UI reads persisted app data instead of only in-memory state.
6. Library CRUD, movie creation, notes editing, favorite toggle, link editing, and tag assignment are wired through repository methods.
7. Android Photo Picker is wired from movie detail.
8. Copy mode copies selected media to app-private storage.
9. Move/cut mode copies selected media first, then asks MediaStore to remove originals.
10. Movie image domain state now preserves `originalUri` and `originalRemoved`.
11. Movie detail now warns when a move/cut import was copied but the original may still be visible in Gallery, WeChat, or other public media pickers.
12. Newly created libraries, movies, images, links, and tags now use UUIDs to avoid ID reuse after deletes.

## Verification

Last verified commands:

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

Both commands passed after the UUID and move-result fixes.

Known Gradle warnings remain:

1. `android.builtInKotlin=false` is deprecated.
2. `android.newDsl=false` is deprecated.
3. `android.enableJetifier=true` is deprecated.
4. Some dependency or plugin still uses obsolete AGP variant APIs.

These warnings do not block the current debug build, but should be cleaned up before release hardening.

## Important Decisions

1. Copy mode intentionally keeps the public original visible.
2. Move/cut mode is only considered fully complete when MediaStore deletion is confirmed.
3. If deletion is denied or unavailable, the app keeps the copied private file but must tell the user the original may still appear publicly.
4. UUIDs are preferred over count-based IDs for persisted user data.
5. Current app lock is still a non-blank passcode gate and is not real persisted authentication.

## Open Risks

1. `PrivateVaultApp.kt` is large and should be split into smaller screen/dialog files.
2. `OriginalMediaRemovalGateway` exists but is not the active abstraction used by the UI.
3. Room schema migration strategy is not defined yet.
4. File-level encryption is not implemented.
5. Import failure recovery is basic; failed copies should eventually surface clear user-facing errors.
6. Gradle warning cleanup is still pending.

## Next Goals

1. Install and manually test on the connected Android device.
2. Verify Photo Picker copy mode stores a private file and leaves the original visible.
3. Verify move/cut mode triggers the system delete confirmation and updates UI state based on the result.
4. Split `PrivateVaultApp.kt` into focused files.
5. Add movie deletion, image deletion, image ordering, and better import error handling.
6. Add real PIN storage and biometric unlock.
7. Add file-level encryption or document the exact security boundary before release.
