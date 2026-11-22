# Android VNC integration audit — GIMP Sakura Candy

Status: BLOCKED — missing upstream FreeRDP Android module; no runtime changes made.

## Verified source state

- UserLAndLibrary pins `remote-desktop-clients` at `0047fd1a35c50fe00de73f97b167ec68c8e12347`.
- Root `settings.gradle` includes `:bVNC`, `:remoteClientLib`, and `:pubkeyGenerator`, but not the FreeRDP Android module.
- The pinned upstream `remote-desktop-clients/settings.gradle` includes `:remoteClientLib:jni:libs:deps:FreeRDP:client:Android:Studio:freeRDPCore`.
- Both upstream `bVNC/build.gradle` and `remoteClientLib/build.gradle` depend on that module.
- The pinned upstream Git tree does **not** contain a corresponding `FreeRDP/.../freeRDPCore` project directory or nested Git submodule.
- The Android `:app` module depends on `:bVNC`, so removing VNC would change the actual application.

## Reproduced failure

GitHub Actions run 44908839521 failed during `:app:help` while evaluating `:bVNC`:
`Project with path ':remoteClientLib:jni:libs:deps:FreeRDP:client:Android:Studio:freeRDPCore' could not be found`.
The Android `AssetDownloaderTest` suite was skipped, not failed.

## Required next action

Locate a legitimate compatible source or binary artifact for the exact FreeRDP Android module and establish its license, provenance, Android Gradle compatibility, and native ABI support. Only then restore the project mapping in an isolated development branch and run `:app:help` followed by the downloader regression tests. Do not add a placeholder module or delete bVNC dependencies to force CI green.

## Independently verified checkpoint

Run 44992651521 passed 11 standalone production filename parser checks. This verifies filename routing, **not** Android archive extraction, VNC launch, or on-device GIMP 3 startup.

Do not update the parent GIMP-SakuraCandy submodule gitlink, merge the feature branch, or enable `RELEASE_VERIFIED` until full integration verification succeeds.
