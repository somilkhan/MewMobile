# CloudStream Port Audit — integration

> **MANDATORY HANDOFF RULE:** Before making any CloudStream port change, read this entire file, the complete `CLOUDSTREAM_UPSTREAM_COMPATIBILITY.md` and `CLOUDSTREAM_CROSS_PLATFORM_COMPATIBILITY.md` from the source branch (`Main-dev`), and the current `MEW-CONTEXT-SNAPSHOT.md` if present. Then re-check the live branch SHA and inspect the exact target files/callers. Do not repeat completed investigations or copy files blindly. Update this audit with date, time, SHA, decisions, tests, and remaining risks after each meaningful work unit.

## 1. Audit metadata

- Date/time: 22 November 2026, 01:47 IST
- Repository: `somilkhan/MewMobile`
- Target branch: `integration`
- Target base SHA: `6c2ad7937171965608d05005080b96ca19345379`
- Source branch: `Main-dev`
- Source SHA inspected: `4e98931537192e20a85fd2a248de590676aa9643`
- Stable branch: `main` — not modified
- No application source files changed in this audit entry.
- Build/tests/device verification: **NOT RUN** (remote source inspection only).

## 2. Branch and source findings

- The local Termux checkout reported branch `integration` at `6c2ad7937171965608d05005080b96ca19345379`.
- That commit exists in the GitHub repository. The online `integration` branch was created at exactly that SHA and verified.
- GitHub compare reports `integration` is 2,121 commits ahead and 720 behind `Main-dev`. This is a highly divergent upstream snapshot. **Do not merge the branches or cherry-pick broad history.**
- The inspected `Main-dev` tree contains 75 CloudStream-related entries (files and directories); the `integration` tree contains zero CloudStream-named entries.
- The local checkout had six untracked artifacts at audit time. They are not part of the branch commit/archive and were not uploaded, staged, modified, or deleted. Their contents remain uninspected.

## 3. Existing-system duplicate audit

| System | Classification | Decision |
|---|---|---|
| Stremio `AddonRepository` | INTENTIONAL PARALLEL | Keep its catalog/metadata/stream lifecycle; do not replace it. |
| Nuvio scraper `PluginRepository` | INTENTIONAL PARALLEL | Keep its scraper/provider lifecycle; CloudStream has a distinct .cs3 ABI/runtime. |
| CloudStream repository/runtime | INTENTIONAL PARALLEL | Port as a separate integration behind CloudStream-specific adapters and routes. |
| Existing Nuvio player/StreamItem pipeline | SHARED PATTERN / DIFFERENT SEMANTICS | Reuse it for resolved CloudStream streams; do not add a second player. |

## 4. Port manifest — initial decisions

| Area | Decision | Notes |
|---|---|---|
| Common models, parser, URL normalization, package inspection, SHA helper, Nuvio adapters | TAKE, then verify against target dependencies | No CloudStream equivalent exists on target. |
| Full common repository orchestration | ADAPT | Profile storage, HTTP helpers, diagnostics and coroutine APIs must match target. |
| Android Full runtime and CloudStream shims | ADAPT | Dynamic DEX execution is security-sensitive; verify embedded ABI and target Android dependencies. |
| Android Play Store disabled stubs | TAKE/ADAPT | Preserve target source-set conventions and ensure no runtime leakage. |
| iOS Full reviewed built-in adapter path | ADAPT | Arbitrary Android DEX must remain unsupported; no Apple device verification available. |
| iOS App Store stubs | TAKE/ADAPT | Keep dynamic runtime disabled for App Store distribution. |
| CloudStream settings page/section | ADAPT | Integrate with target settings/navigation rather than copying the complete settings screen blindly. |
| Home/Search/Details/Streams integration | ADAPT | Existing target callers differ; preserve target catalog/provider behavior. |
| Tests | TAKE + ADD REGRESSION TESTS | Add profile-switch and security-policy coverage. |
| Gradle, AAR, ProGuard, workflow, notices and compatibility docs | SELECTIVE TAKE | Merge only CloudStream-specific configuration; retain target-specific build and CI behavior. |

## 5. Confirmed integration findings

### Profile lifecycle — missing hook

The source branch exposes `CloudStreamRepository.onProfileChanged(profileId)`. It cancels update/refresh/main-page/link work, invalidates generations, switches profile-scoped storage, clears caches/UI state and clears the runtime.

However, the inspected `Main-dev` `ProfileRepository.selectProfile()` does not call `CloudStreamRepository.onProfileChanged(profileIndex)`. This is a confirmed lifecycle integration gap in the source branch. Add the hook during port and add a regression test proving profile A's state/runtime/results cannot leak into profile B.

### SHA verification policy — unresolved security mismatch

The install path currently computes:

```kotlin
val expectedHash = item.metadata.fileHash
val verified = expectedHash?.matches(bytes) ?: true
if (expectedHash != null) require(verified)
```

A package with no advertised hash is therefore marked verified. The cross-platform compatibility document says SHA-256 is mandatory for a verified installation. This implementation/documentation mismatch is unresolved. Do not silently change installability or verification semantics; decide and test the policy before enabling arbitrary downloaded DEX.

### DEX execution policy — documentation conflict

The Android Full runtime validates the package, creates a `PathClassLoader`, loads the declared class, checks `BasePlugin`, registers `PluginData`, and calls `BasePlugin.load()`. Therefore Android Full does execute downloaded DEX.

The current `CLOUDSTREAM_CROSS_PLATFORM_COMPATIBILITY.md` says arbitrary downloaded DEX is not supported and “Downloaded DEX is never executed”, while `CLOUDSTREAM_UPSTREAM_COMPATIBILITY.md` and the Android Full implementation describe execution. Reconcile the docs with the actual distribution policy before porting. iOS must not execute Android DEX.

### Package inspection

The common package inspector checks ZIP central-directory structure, entry count, total uncompressed size, manifest/DEX presence, and absolute/drive-qualified/parent-traversal entry names. Android Full performs a second ZIP read and validates the DEX magic and manifest. Preserve and test these checks; inspect additional ZIP edge cases during the security pass.

### Build and distribution

The source branch uses separate Android Full/Play Store and iOS Full/App Store source sets. Android Full includes the CloudStream runtime AAR and additional runtime dependencies. Android Play Store has disabled repository/runtime stubs. iOS Full exposes reviewed built-in adapters with no Android DEX runtime. The CloudStream workflow runs Android host tests and Android compilation; APK packaging is separately gated.

## 6. Shared integration files to adapt manually

Verified target/source counterparts include:

- `composeApp/src/commonMain/kotlin/com/nuvio/app/App.kt`
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/home/HomeRepository.kt`
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/home/HomeCatalogSettingsRepository.kt`
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/search/SearchRepository.kt`
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/MetaDetailsRepository.kt`
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/streams/StreamsRepository.kt`
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/profiles/ProfileRepository.kt`
- `composeApp/build.gradle.kts`
- `gradle/libs.versions.toml`

Do not replace these files wholesale. Their target and source SHAs differ, and the branches have extensive unrelated divergence.

## 7. Verification status and next steps

1. Finish target-side architecture/caller audit for settings routing, source-set selection, feature policy, localization, Gradle variants, and profile persistence.
2. Resolve the SHA verification and Android DEX documentation/policy mismatches before enabling downloaded code.
3. Port common CloudStream models/parser/tests in a small group; inspect diff and run targeted tests.
4. Port repository orchestration and platform storage/runtime separately.
5. Adapt App/Home/Search/Details/Streams/Profile/settings integration one subsystem at a time.
6. Add profile-switch, missing-hash, invalid-package, and disabled-distribution regression tests.
7. Run Android Full host tests and compile workflow; report iOS compile separately and do not claim device verification.
8. Inspect final diff and workflow results. No merge/promotion to `Main-dev` or `main` without separate approval.

**Current status:** Branch published; source audit started; no application source changes; implementation not yet started.


## 8. Implementation progress — common foundation

- Date/time: 22 November 2026, 01:47 IST
- Source: `Main-dev` at `4e98931537192e20a85fd2a248de590676aa9643`
- Target branch head before this update: `b7169c7141d0f916ccc269531e834137d1d1ef2a`
- Ported in two small commits, preserving the existing target history:
  - `8d963db5d513a3fea9afcab8403cb506a1297b8c` — common models, URL/parser logic, package inspection, SHA-256 helper, compatibility resolver, and four common tests.
  - `b7169c7141d0f916ccc269531e834137d1d1ef2a` — common provider/repository contracts, route ID adapter, home section helpers, search route index, and adapter test.
- Added 10 commonMain source files and 5 commonTest files. These are exact source-branch blob copies; no target-specific changes have been made inside those copied files yet.
- Verification: branch ref updates succeeded. File-level build/tests are **NOT RUN**; GitHub remote editing does not provide a local Gradle checkout or test execution.

## 9. Target model/call-site compatibility findings

- Target `HomeModels.MetaPreview` has additional poster/runtime/status/rating fields. The copied CloudStream adapter only sets fields common to both models; keep the target-only fields intact.
- Target `MetaDetails`/ `MetaVideo` have additional IDs, ratings, season poster, and enrichment fields. The copied adapter uses a compatible subset; do not replace these target models.
- Target `StreamItem` has additional YouTube/server/debrid fields. The copied adapter only populates existing common fields; preserve all target-only playback semantics.
- Target `CatalogTarget` includes `MoreLikeThis` and `Server`, but does not include `CloudStream`; target `CatalogTargetKind` also lacks `CLOUDSTREAM`.
- Target `CatalogRepository` has an exhaustive `when (request.target)` handling Addon, CollectionSource, MoreLikeThis, Server and Library. Source `Main-dev` adds a CloudStream case. **ADAPT manually**: add the CloudStream target and catalog loading case without removing or rewriting the target's MoreLikeThis/Server paths.
- Source and target `StreamModels`/ `HomeModels`/ `MetaDetailsModels` are not identical. Similar type names are not grounds for copying those files.

**Next implementation unit:** adapt `CatalogTarget` and `CatalogRepository`, then Home/Search/Details/Streams callers and profile lifecycle. Keep target-only catalog/server/player behavior.
