# Upstream alignment status — 2026-11-17

## Post-beta upstream `enhanced` delta — separate from the beta baseline

- Latest published release remains `1.1.0-beta.1` (`eb32e0479932d28f5f6fa6afeb0e21c78ad02efd`). Current upstream `enhanced` HEAD is `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`, 8 commits ahead of the beta tag.
- Tree comparison: both trees contain 2,339 blob paths; no paths were added/removed, and 7 existing paths have changed blobs. This is the post-beta development delta, not a new published beta release.
- Mew `upstream-sync` currently includes two of these seven post-beta paths and their blobs match upstream `enhanced` HEAD exactly: `iosApp/iosApp/ContentView.swift` (floating tab-bar blur removal) and `store-beta.json` (beta IPA store metadata). The other five post-beta paths are not yet present in Mew `upstream-sync`.

### Post-beta decisions

| Upstream change | Mew comparison | Decision |
|---|---|---|
| `05e779a0c17861e185c09901e8be4a5f0f86ced5` — bug report template adds official-app comparison and sideload install options | Mew template already asks install method but lacks official Nuvio comparison. | **ADAPT**: add the official-app comparison question and Android-relevant install options; omit iOS-only sideload choices. |
| `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03` — episode artwork fallback after image load failure | Mew `DetailSeriesContent.kt` falls back only when thumbnail is null; no `onError` fallback state exists. | **ADAPT** both horizontal and list episode cards; preserve Mew card styling, ratings, download affordances and focus behavior. Add image-failure tests if practical. |
| `0b9d93599f7d7603e7814a99a70aa64d7c48e086` — preserve local catalog preferences when remote known-key set is empty | Mew `HomeCatalogSettingsRepository.kt` has `knownKeys` filtering but lacks the `knownKeys.isEmpty()` preservation guard. | **TAKE candidate** with regression tests for empty/known/unknown key sets and remote merge behavior. |
| `c5298633914d967792c79351754ba47d54dfd242` — reset iOS subtitle state via `LaunchedEffect(sourceUrl)` | iOS player actual only. | **SKIP for Android target**; keep in cross-platform notes if Mew's iOS source remains supported. |
| `d703ca48e5ade22ceaf424451ffba05efa94e670` — use matching iOS document-picker export API | iOS settings file launcher only. | **SKIP for Android target**; revisit only for an iOS build failure/compatibility need. |
| `c314dd48d622de249eebbea7e531e3f7b2e506d4` / merge `f4b7e55e31c2778827095dbdb246193e8675016f` — remove blur behind floating iOS tab bar | Already present in Mew `upstream-sync` with the exact upstream enhanced blob. | **ALREADY PRESENT in upstream-sync**; not a Mew-only change. |
| `803194b575dab025ed7c233507716438ad02880a` — publish beta store metadata | Already present in Mew `upstream-sync` with the exact upstream enhanced blob; iOS IPA metadata, not Android app release config. | **ALREADY PRESENT in upstream-sync**; do not treat as Mew Android release metadata. |

- These post-beta changes must stay separate from the stable-beta ledger until a newer beta tag/release is published, unless the user explicitly chooses to track `enhanced` HEAD. The two already-present paths are recorded as snapshot drift, not evidence that all post-beta work has been integrated.
- No application source changed during this post-beta comparison. Full semantic review of the two common-code candidates is still needed before any integration proposal.


## Semantic audit batch 11 — watch-progress identity and subtitle setting bounds

### Watch-progress identity

- Mew `WatchProgressIdentity.kt` and upstream beta `WatchProgressIdentity.kt` have the **same blob SHA** (`c66d53d6cfdf458927cbda71e31393dd5fd57efb`). The stable logical progress-key algorithm, opaque server-key preservation, alias handling, deterministic freshness ordering and snapshot key reconciliation are already implemented identically in Mew.
- Upstream's release-range change to `WatchProgressIdentityTest.kt` only changes the legacy completed-progress fixture from a 940 ms / 1,000 ms sample to a realistic 2,820,000 ms / 3,000,000 ms sample; the behavior is already covered in Mew's same test suite.
- Classification: **ALREADY IMPLEMENTED**. Decision: **NO CODE PORT**. Keep the existing Mew implementation; optionally align the test fixture to realistic media durations during a later test-only batch.

### Subtitle outline-width normalization

- Upstream beta changes `PlayerSettingsRepository` to clamp stored and newly set subtitle outline width to `subtitleOutlineWidthRange`.
- Mew's `SubtitleStylePanel.kt` limits user increment/decrement controls to 1–5, but `PlayerSettingsRepository.setSubtitleStyle()` persists the incoming `outlineWidth` without clamping and `loadFromDisk()` restores it without normalization. The existing `SubtitleStyleStateTest.kt` does not test out-of-range outline widths.
- Classification: **SAME SETTING / MISSING REPOSITORY-BOUNDARY VALIDATION**. Decision: **TAKE behavior, ADAPT range** to Mew's existing 1–5 UI contract (do not blindly copy upstream's range). Clamp both load and set paths and add regression tests for minimum, maximum and corrupted/out-of-range persisted values. This is a small player-settings hardening candidate; no code changed.

### Episode season label helper

- Upstream `PlayerEpisodesPanel.kt` replaces inline special/season label construction with shared `seasonLabel(season)`. Mew's panel still constructs the label inline. Before extracting a helper, search all Mew season labels and confirm identical localization/pluralization semantics across details, download and player surfaces.
- Classification: **POTENTIAL SHARED PATTERN / NEEDS DUPLICATE AUDIT**. No refactor proposed until call sites and wording are compared.

### Files inspected and limits

- Upstream/Mew `WatchProgressIdentity.kt`, `WatchProgressIdentityTest.kt`, `PlayerSettingsRepository.kt`, `PlayerEpisodesPanel.kt`; Mew `SubtitleStylePanel.kt` and `SubtitleStyleStateTest.kt`.
- No watch-progress, subtitle or player source changed. Tests/build/CI were **NOT RUN**.


## Semantic audit batch 10 — settings backup, player episode panel and search routes

### Settings config export/import

- Upstream beta adds `SettingsConfigTransfer.kt` (1,127 lines) plus platform file bridges and settings-root integration. It exports/imports multiple settings domains, including player/subtitle settings, add-ons/plugins, debrid, P2P, home/details, custom posters and other app configuration; credential inclusion is an explicit export choice.
- Mew does not have the same `SettingsConfigTransfer` implementation, but it already has a substantial parallel backup/sync contract: `ProfileSettingsSync.exportBackupJson()` / `importBackupJson()`, a versioned `MobileProfileSettingsBlob`, automatic per-profile cloud sync, and a Mew Nuvio Enhanced backup UI using `NuvioEnhancedBackupFileBridge`.
- Classification: **SHARED PATTERN / DIFFERENT COVERAGE AND DATA CONTRACT**. Decision: **ALREADY IMPLEMENTED in part; NEEDS COVERAGE COMPARISON**. Do not add a second independent backup system. Compare exported sections, add-on/plugin handling, credential opt-in/redaction, schema versioning, migrations, profile scope, failure atomicity and import confirmation. Adapt only genuinely missing sections into Mew's existing backup/sync format. Never import upstream configuration wholesale or overwrite Mew-specific keys.
- Security review required before any credential export/import changes. No backup format or settings code changed.

### Player episode panel and settings search

- Upstream and Mew both contain `PlayerEpisodesPanel.kt` with the same major entry points and episode/season panel structure, but Mew's file is longer and has fork-specific layout/styling behavior. Classification: **INTENTIONAL PARALLEL**. Preserve Mew's player panel; compare only isolated functional changes and do not replace the UI.
- Mew `SettingsSearch.kt` includes a dedicated `Downloads` target and Mew-specific settings entries. Upstream's search/settings routing is organized differently. Classification: **INTENTIONAL PARALLEL / DIFFERENT ROUTING**. Preserve Mew's download route and settings navigation; adapt only missing upstream settings entries after confirming their actual Mew destination.
- Mew `PlayerSettingsRepository.kt` already persists a large custom player configuration and subtitle style. Upstream's additional next-source fallback settings are part of the separate source-fallback candidate in batch 2; do not replace the settings repository.

### Files inspected and limits

- Upstream: `SettingsConfigTransfer.kt`, `SettingsRootPage.kt`, `PlayerEpisodesPanel.kt`, `PlayerSettingsRepository.kt`, `SettingsSearch.kt`.
- Mew: `ProfileSettingsSync.kt`, `NuvioEnhancedBackupFileBridge.kt`, `NuvioEnhancedSettingsRepository.kt`, `NuvioEnhancedSettingsPage.kt`, `PlayerEpisodesPanel.kt`, `PlayerSettingsRepository.kt`, `SettingsSearch.kt`.
- The complete upstream-vs-Mew backup section matrix is still open. No tests/build/CI were run.


## Semantic audit batch 9 — account wipe and external playback progress

### Unfinished download cleanup on account wipe

- Upstream beta changes Android and iOS `PlatformLocalAccountDataCleaner` to call `DownloadsStorage.clearUnfinishedDownloads()` and `DownloadsPlatformDownloader.clearUnfinishedTransfers()`, removing unfinished transfer state when account data is wiped.
- Mew's common `LocalAccountDataCleaner.wipe()` does not call `DownloadsRepository.clearLocalState()` or clear download storage. Mew's Android `PlatformLocalAccountDataCleaner.android.kt` also does not clear unfinished downloads/transfers. Mew's downloader interface only exposes per-file `removeFile` / `removePartialFile`; it has no equivalent bulk unfinished-transfer cleanup method.
- Mew has active download handles, a foreground service/wake lock, pending-source WorkManager jobs, persisted download metadata and partial files. Account wipe therefore needs a Mew-specific cleanup path; simply copying the upstream two calls will not compile or cover Mew's worker/service architecture.
- Classification: **MISSING CLEANUP CONTRACT / Mew-specific transfer architecture**. Decision: **ADAPT candidate, HIGH PRIORITY**. Define cleanup to stop the foreground service, cancel active download handles and pending source-search work, clear unfinished persisted items and partial transfer files, and reset in-memory download state. Preserve completed downloaded files unless the product's account-wipe policy explicitly says otherwise. Verify Android and iOS storage behavior and profile/account isolation; add tests for active, queued, pending-source and completed downloads.
- This is a source-level cleanup gap, not a claim that data has already leaked in a running build. Runtime behavior and tests are **NOT RUN**.

### External playback progress

- Upstream `ExternalPlaybackProgress.kt` records watch progress/scrobble state when an external player returns a structured `ExternalPlaybackResult`, and emits a tracking-addon STOP event when duration is known.
- Mew's external-source path in `PlayerScreenRuntimeSourceActions.openExternalSourceUrl()` only calls `args.onOpenExternalUrl(url)`; the inspected contract returns a Boolean and provides no external playback result/position callback. Mew has no `ExternalPlaybackProgress.kt` equivalent.
- Classification: **FEATURE NOT PRESENT / DIFFERENT EXTERNAL-PLAYER CONTRACT**. Decision: **SKIP for now**. Do not fabricate progress or STOP events from the handoff result; revisit only if Mew introduces a supported external-player result callback and privacy-aware tracking behavior.

### Profile lifecycle note

- Upstream release adds `BatchDownloadCoordinator.clearLocalState()` on profile change. Mew has no batch coordinator; its own `DownloadsRepository.onProfileChanged()` and pending-source worker lifecycle must be audited independently. Do not add an upstream class just to match the call.
- No account-wipe, profile, download or external-player code changed.


## Semantic audit batch 8 — Android download transport and network policy

- Upstream beta's Android download transport uses `AndroidDownloadScheduler` with WorkManager below API 34 and JobScheduler on API 34+, persisted transfer records, network constraints, retry/backoff and queue re-advance. `DownloadNetworkGuard` tracks unmetered transport state and the `allowMobileDataDownloads` setting controls whether jobs require `UNMETERED` or merely `CONNECTED`.
- Mew does **not** have upstream's `AndroidDownloadScheduler`, `DownloadNetworkGuard` or `DownloadsStorageStats` platform architecture. Mew uses `DownloadsForegroundService` + its own `DownloadsPlatformDownloader.android.kt` (535 lines), streaming/resume logic, partial files, retry policy, external-folder copy, foreground notification and wake-lock lifecycle. It also has a separate WorkManager worker for retrying pending provider source searches; that worker is not the download-transfer scheduler.
- Classification: **INTENTIONAL PARALLEL** for transfer execution and background lifecycle. Decision: **SKIP** replacing Mew's foreground-service/downloader with upstream WorkManager/JobScheduler code. The transport, persistence, pause/resume, notification, external-folder and process-restoration contracts differ.
- Network preference: Mew's inspected download paths do not expose an equivalent mobile-data opt-in setting or upstream `DownloadNetworkGuard`; Mew transfer requests can start through its current foreground service. Decision: **ADAPT candidate** for a user-controlled mobile-data policy, but design it around Mew's existing transfer lifecycle. Verify network callback behavior, pausing/resuming active streams, pending source-search network use, metered Wi-Fi/hotspot semantics, Android API levels and user-visible state before changing anything.
- Storage reporting: upstream adds `DownloadsStorageStats` and a device-storage bar; Mew supports a custom external download folder but has no matching storage-stat abstraction in the inspected paths. **ADAPT candidate** only if it can report private and user-selected document-tree storage accurately without misleading totals.
- Upstream iOS backup exclusion and iOS download platform work are not Android ports. Keep them in the cross-platform audit ledger, but do not change Mew's Android transfer implementation to match them.
- No downloader, service, scheduler, network policy or settings code changed. Android network/Doze/foreground behavior was inspected remotely only; device verification and tests are **NOT RUN**.


## Semantic audit batch 7 — Android player gesture-navigation insets

- Upstream merge commit `4128f63efcaabd42086f0fd26271556012e5c83a` fixes player timeline controls being pushed too far up under Android gesture navigation.
- Upstream `PlayerLayout.kt` adds `playerTimelineBottomInsets(metrics)`: iOS keeps `safeContent`; Android uses bottom-only `safeDrawing` plus the player slider offset. Upstream `PlayerControls.kt` applies that inset to the timeline/progress-control containers.
- Mew `PlayerLayout.kt` has no `playerTimelineBottomInsets` helper. Mew `PlayerControls.kt` applies `metrics.sliderBottomOffset` directly and does not apply bottom window insets to those controls. This is a real implementation delta in a regression-sensitive area.
- Classification: **MISSING UPSTREAM ANDROID INSET FIX / SHARED PLAYER CONTROL SURFACE**. Decision: **ADAPT candidate**, but only after verifying Mew's edge-to-edge configuration, gesture-vs-three-button navigation, immersive/fullscreen transitions, portrait/landscape, TV layout and current bottom-padding contract. Apply only the inset behavior; do not import unrelated upstream player layout or visual changes.
- Mew's player controls/layout are heavily customized and the user has previously approved the current player appearance. No visual change has been made. A real Android emulator/device check is required before calling this fixed. Tests/device verification: **NOT RUN**.

### Files inspected

- Upstream/Mew `PlayerLayout.kt` and `PlayerControls.kt`; upstream Android/iOS inset branch in `playerTimelineBottomInsets`.
- This audit did not change player code or alter Mew's player UI.


## Semantic audit batch 6 — localization and mixed RTL/LTR behavior

### Locale coverage delta

- The beta tag contains `values-uk` and `values-zh` resource directories added in this release range, plus updated Urdu, Indonesian, Hebrew and Brazilian Portuguese strings. The full beta tree also contains Bengali and Croatian resources that are absent from Mew Main-dev.
- Mew's current common locale-resource directories are missing upstream's `values-bn`, `values-hr`, `values-uk`, `values-ur` and `values-zh`. Mew does already have Indonesian, Hebrew and Brazilian Portuguese resources, but their blobs differ from upstream; compare string keys and Mew-specific strings before replacing/merging translations.
- Mew `AppLanguage.kt` does not offer Bengali, Croatian, Ukrainian, Urdu or Chinese. Upstream registers those languages and includes RTL detection for Arabic, Hebrew and Urdu.
- **Confirmed Android locale configuration mismatch:** Mew's `androidApp/src/main/AndroidManifest.xml` references `@xml/locale_config`. Mew's `AppLanguage.kt` and translation resources include Arabic, Hebrew and Russian, but `composeApp/src/androidMain/res/xml/locale_config.xml` omits `ar`, `he` and `ru`. Android per-app language settings therefore do not advertise all of Mew's existing app-language choices. The config also needs to be updated alongside any new language additions.
- Classification: **MISSING UPSTREAM LOCALES + EXISTING CONFIG MISMATCH**. Decision: **ADAPT candidate** for Ukrainian/Chinese/Urdu first (current release changes), then Bengali/Croatian from the full beta baseline. Merge translation keys rather than overwriting Mew resource files; add enum entries, Android locale config and appropriate platform localization metadata together. Preserve Mew-specific translations/branding.

### Mixed RTL/LTR text

- Upstream release range includes the mixed RTL/LTR correction for Search and Watch Party and adds tests covering input direction, cursor/selection behavior, Latin tokens inside Arabic/Hebrew text, names and paragraph direction.
- Mew has different `SearchScreen.kt` and `PartyChatContent.kt` blobs and a player-only subtitle RTL fix, but the exact equivalent behavior in Search/chat has not yet been traced. Mew `AppLanguage.kt` also lacks upstream's explicit `isRightToLeft()` helper.
- Classification: **NEEDS INVESTIGATION**. Compare the upstream implementation/merge commit with Mew's input components, text styles, paragraph direction, selection/IME behavior, message sender names, accessibility and layout direction. Do not apply a global LTR/RTL modifier that breaks mixed-script input.
- Upstream's RTL test file is Android host-test coverage; Mew has no equivalent mixed-direction Search/Watch Party test in the inspected paths. Add regression tests before adapting. Tests were not run: **NOT RUN**.

### Files inspected and limits

- Upstream/Mew: `AppLanguage.kt`, Android `locale_config.xml`, Android main manifest locale-config reference, recursive common resource locale directories, plus paths for Search and Watch Party.
- Translation files were compared by directory/blob inventory only; the full translation-key diff and mixed-direction UI call-site audit remain open. No app source changed and no tests/build/device checks were run.


## Semantic audit batch 5 — release/build workflows and branch triggers

### Upstream release pipeline vs Mew release pipeline

- Upstream beta adds a stable/beta release-channel selector, prerelease publication, beta sideload-store source handling, upstream-aware release notes and release-tag metadata. Its workflow uses Nuvio-owned release properties/secrets, Nuvio artifact naming and upstream repository contributor/source URLs.
- Mew has a separate `.github/workflows/android-release.yml` with Mew signing secrets, Mew artifact/release identity, and optional unsigned iOS packaging; `mew-cloudstream-build.yml` runs Android host tests and compilation and separately gates debug APK packaging. `RELEASES.md` documents Mew signing and update compatibility.
- Classification: **INTENTIONAL PARALLEL**. Decision: **SKIP** wholesale upstream workflow/script/store integration. Preserve Mew application ID, signing certificate, Mew release tags/artifacts, CloudStream CI and stable public-release behavior. Port only generally useful validation ideas after a separate workflow review; never copy upstream signing secrets or release destinations.

### Confirmed branch-trigger mismatch in Mew CI

- Current GitHub branch refs: `Main-dev` = `207ef8fa309b8061b73039b2d4e4bde4af65050e`; `main` = `207ef8fa309b8061b73039b2d4e4bde4af65050e`; `upstream-sync` = `2629c12694a08cb58f2216380810f40a5066c51e`. No `enhanced` branch is present.
- On the current `Main-dev` workflow files:
  - `.github/workflows/android-release.yml` push trigger targets only `enhanced`.
  - `.github/workflows/mew-cloudstream-build.yml` push and pull-request triggers target only `enhanced`.
  - The other inspected workflow files are manual, issue-event or generic PR checks and do not supply the missing Main-dev build/test trigger.
- This conflicts with the approved branch model where `Main-dev` is the active development/integration target. Pushes and PRs targeting `Main-dev` will not match those two branch filters. This is a **confirmed workflow configuration mismatch**; actual workflow-run history was not checked in this pass.
- Decision: **BLOCKED / NEEDS APPROVAL TO FIX**. Propose updating the branch filters to the approved active branch model (and preserving manual release controls), but do not change CI configuration without explicit approval. Before editing, inspect all workflow triggers, required-check expectations and current branch protection/rules.

### Release-range build/dependency review

- The 157 paths changed between upstream `1.0.0` and `1.1.0-beta.1` include release workflows, store metadata and app sources/tests, but no Gradle build scripts or version-catalog files in that specific release range.
- This does not mean the full beta-vs-Main-dev build configuration is equivalent: the broader tree comparison still contains many Main-dev-only and upstream-only paths. Complete build, manifest, native-library and source-set comparison remains open.
- No workflow, Gradle, signing, release or app source files were changed. No CI run/build/test was executed during this audit.


## Semantic audit batch 4 — library filters, detail hero, MDBList

### Library filters and persistence

- Upstream commit `2da28d707c77b0bb8d3030af8f360e67981a0994` adds per-profile remembered library list/type selection and genre, year and watched/unwatched filters with faceted counts in the vertical grid.
- Mew has the same broad `LibraryDisplaySettings.kt` / `LibraryScreen.kt` / `LibraryRepository.kt` domain, but its current display settings only persist layout and sort. It does not contain upstream's `LibraryWatchedFilter`, selected section/type state, genre/year projection filters or their counters.
- Mew's library screen is not a blank older copy: it includes Mew-specific release-calendar behavior, downloaded-library views and filters, and Mew's repository includes its own tracking-provider/sync/retry behavior. The upstream screen is substantially larger and cannot be copied over.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Decision: **ADAPT candidate** for selected list/type and genre/year/watched filters, only after tracing Mew's tracking-provider list model, local vs remote sources, profile-scoped storage, watched-state lookup and release-calendar/download views. Preserve Mew's current library UI and state restoration; do not replace `LibraryScreen.kt` or `LibraryRepository.kt` wholesale.
- Upstream adds `LibraryDisplaySettingsTest.kt`; Mew has a same-domain settings test file but its coverage must be compared before adapting. Tests were not run: **NOT RUN**.

### Detail hero mute-button alignment

- Upstream commit `f28929d3e3cc0d354de1dd921efc539bbf8a14cf` introduces a native navigation-bar button anchor so the hero mute control aligns with the native back button, with Android returning no native anchor and iOS calculating the UIKit bar position.
- Mew's `DetailHero.kt` has a different layout implementation and does not expose the upstream native-anchor contract. This is primarily an iOS/native-navigation alignment change, not a generic Android feature.
- Classification: **SHARED PATTERN / DIFFERENT PLATFORM PRESENTATION**. Decision: **SKIP for the current Android target** unless an iOS/native-navigation target is explicitly in scope. Do not change Mew's hero/player UI just to match upstream geometry.

### MDBList episode runtime correction

- Upstream commit `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb` changes MDBList progress projection so show runtime is not reused as an episode duration; it adds a regression test for a 6,138-minute show runtime.
- Mew Main-dev has MDBList metadata/settings files but no `MdbListProgressProjection.kt`, MDBList watched/progress projection or equivalent path in the inspected recursive tree.
- Classification: **FALSE POSITIVE / FEATURE NOT PRESENT IN MEW** for this release item. Decision: **SKIP**; do not add an MDBList watch-progress integration solely to port this correction. Revisit only if MDBList tracking sync is added later.

### Scope and limits

- Inspected upstream/Mew `LibraryDisplaySettings.kt`, `LibraryScreen.kt`, `LibraryRepository.kt`, `DetailHero.kt`, Mew `DetailSeriesContent.kt`, Mew MDBList path inventory, and exact diffs for the three upstream commits above.
- No UI runtime, accessibility, keyboard/focus, screen-size or state-restoration verification was possible remotely. No tests/build/CI were run.


## Semantic audit batch 3 — Stremio tracking addons and TorBox cache aliases

### Stremio player/library tracking addons

- Upstream commits include `964ecfd8d98592cd2d315cedb584fe7ef59a76fd` (tracking event support), `8f5d9bcac6c2c0535b17b26cbdff392c10b8d767` (per-addon event queues), and `d89811ac6c75a3f80c0ab424ab01a37639401d54` (avoid events for derived whole-series watched reconciliation).
- Upstream adds `AddonEventDispatcher` for `player` start/pause/stop and `library` add/remove/watched/unwatched events; it filters by active addon manifest resource/type/id support, chunks video IDs to 100, queues delivery per addon, and uses a 10-second timeout. It also adds a tracking-addon consent dialog before installing manifests that declare `player` or `library` resources.
- Mew already has a Stremio addon `AddonRepository` / manifest model, but its `AddonModels.kt` has no tracking-resource classification or consent-required result and `AddonRepository.addAddon()` installs a parsed manifest directly. Mew's repository also has its own profile-generation guards and persistence/sync behavior; do not replace it with upstream's shorter repository implementation.
- Mew's CloudStream runtime/plugins are separate compatibility systems. Keep CloudStream provider/catalog behavior and Mew extension routing untouched; do not conflate CloudStream extension events with Stremio addon resources.
- Classification: **NEW UPSTREAM FEATURE / INTENTIONAL PARALLEL ADDON IMPLEMENTATION**. Decision: **ADAPT candidate**, not direct TAKE. Before integration, trace every player and watched/library call site, ensure only user actions emit events, preserve event order and profile scope, and add explicit consent.
- **Privacy blocker to resolve:** upstream's install path asks consent for newly installed tracking addons, while the dispatcher routes to every active installed addon whose current manifest declares the resource. Verify whether already-installed tracking-capable addons receive events after upgrading without a fresh consent choice; add a persisted consent/migration gate if needed. Never silently send playback position or watch/library activity to third-party addon endpoints.
- Upstream adds `AddonEventsTest.kt` for URL encoding, video-ID chunking and tracking-resource detection. No Mew equivalent event-delivery tests exist in the inspected paths. Tests were not run: **NOT RUN**.

### TorBox alternative-hash cache matching

- Upstream commit `1118dfbf87de7d9e68860446ea553d48d06adcba` adds `resolveTorboxAliasMatches()` in `LocalDebridService.kt`. TorBox may return a canonical hash for a request made with an alternative hash; upstream maps single-result answers back to the requested hash and re-requests only missing hashes in a batch.
- Mew `LocalDebridService.kt` currently lowercases the returned map keys but does not map canonical/alias answers back to the requested hash. Mew's `isCached()` then checks membership using the requested hash, so an alias response can be treated as a cache miss. The inspected Mew `DebridProviderTest.kt` checks provider capabilities, not this response-matching behavior.
- Classification: **SAME BEHAVIOR / MISSING EDGE-CASE HANDLING**. Decision: **TAKE candidate** for the small alias-matching helper and regression tests, after confirming the current TorBox response contract and tracing all callers. Preserve cancellation propagation and avoid unnecessary extra requests for exact matches.
- Upstream `LocalDebridServiceTest.kt` covers exact matches, a single aliased answer and batched missing-hash retries. Port equivalent tests into Mew's test source set before integrating.

### Files inspected and limits

- Upstream: `AddonEvents.kt`, `AddonModels.kt`, `AddonRepository.kt`, `TrackingAddonConsentDialog.kt`, `LocalDebridService.kt`, and their focused tests.
- Mew Main-dev: `AddonModels.kt`, `AddonRepository.kt`, `AddonsScreen.kt`, `LocalDebridService.kt`, and `DebridProviderTest.kt`.
- Addon event call-site audit and complete TorBox caller/response trace are still open. No application source changed; no tests/build/CI were run.


## Semantic audit batch 2 — next-episode source fallback and subtitle rendering

### Next-episode source selection

- Mew `PlayerNextEpisodeAutoPlay.kt` already has Mew-specific selection semantics: current-provider-first candidate selection, binge-group restriction/preference, manual-mode auto-selection behavior, offline short-circuiting, downloaded-episode preference and a `prepareOnly` preload path. `PlayerScreenRuntimeSourceActions.kt` owns the prepared-stream launch and episode transition.
- Upstream beta retains the general auto-play flow but adds `PlayerSourceFallbackRun.kt` and changes the selected-stream callback to carry tried-source state. The new helper retries subsequent auto-selected candidates when a source fails to resolve/play, bounded by the configured number, and keeps only the final failure for user-facing reporting. The release range also adds dedicated source-fallback tests.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Decision: **ADAPT candidate / NEEDS FAILURE-PATH INVESTIGATION**. Mew's current-provider-first and preloading behavior must not be lost. Before adapting, trace the exact point where a selected next-episode stream fails, distinguish auto-selected from user-selected/torrent/download/P2P sources, deduplicate attempted sources, verify cancellation and episode-switch reset, and preserve Mew's debrid/provider error handling. No player UI changes are implied.

### Subtitle styling and styled overlay

- Upstream beta adds `StyledSubtitleCues.kt` and expands `StyledSubtitle.kt`: parse mpv ASS text into annotated cue blocks, preserve bold/italic/underline, file colors and all nine alignment positions, and render a Compose overlay using the user's font/outline style. It also adds subtitle outline-width controls and aligns the overlay with native ExoPlayer/mpv rendering.
- Mew does not have upstream's `StyledSubtitle.kt` / `StyledSubtitleCues.kt` paths. Mew already has a substantial but different subtitle subsystem: `SubtitleStylePanel.kt` (font size/family/import, outline and sync controls), `EnhancedSubtitleModal.kt`, `PlayerSubtitleCueParser.kt` (SRT/WebVTT/ASS/TTML parsing for cue/sync workflows), subtitle selection/repository code, Android subtitle utilities, sidecar subtitle handling and RTL fixes.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Decision: **NEEDS INVESTIGATION**; do not paste the upstream overlay into Mew. Trace Mew's actual ExoPlayer and libmpv render paths, custom-font handoff, ASS styling, cue updates, external/sidecar subtitles, RTL, PiP, TV and accessibility. A second overlay could double-render or conflict with Mew's native renderer.
- Mew's existing `PlayerNextEpisodeAutoPlayPolicyTest`, subtitle parser/style/selection tests and Android host tests are related coverage, not substitutes for upstream's new fallback/cue tests. Tests/builds were not run: **NOT RUN**.

### Files inspected in this batch

- Upstream: `PlayerNextEpisodeAutoPlay.kt`, `PlayerSourceFallbackRun.kt`, `StyledSubtitle.kt`, `StyledSubtitleCues.kt`, Android `SubtitleCuePresenter.kt`, and related release-range tests.
- Mew Main-dev: `PlayerNextEpisodeAutoPlay.kt`, `PlayerScreenRuntimeSourceActions.kt`, `SubtitleStylePanel.kt`, `EnhancedSubtitleModal.kt`, `PlayerSubtitleCueParser.kt`, `PlayerSubtitleUtils.kt`, plus the player/subtitle source-set path inventory.
- No app source changed. This is a remote source audit only; runtime rendering, emulator/device behavior and Gradle tests remain unverified.


## Semantic audit batch 1 — downloads and season batch flow

**Scope compared:** upstream beta `1.1.0-beta.1` against Mew `Main-dev`; common download repository/screen, upstream batch planner/dialog/queuer/settings, Mew episode source resolver/sheet/pending-source model, details entry point, and available download tests.

### Confirmed Mew behavior — do not overwrite

- Mew already has a substantial independent download/source-selection workflow:
  - `DownloadSourceResolver.kt` resolves sources across Mew Addons and CloudStream providers, supports direct-debrid resolution, preferred quality and Auto Select, and can persist provider/quality identity for a temporarily unavailable source.
  - `EpisodeDownloadFlowSheet.kt` is opened from `MetaDetailsScreen.kt` and supports manual source selection and automatic multi-episode source resolution.
  - `PendingEpisodeDownload` and `PendingSourceChoiceSheet.kt` retain source-search work without persisting signed URLs or request headers; Android has background worker/notification entry points.
  - `DownloadsRepository.kt` supports `AllAtOnce` / `OneAtATime`, a `Waiting` queue state, source-search persistence, local details snapshots and profile reload.
- This is not a filename match or a simple port target. Mew's CloudStream/provider lookup, pending source retry and choice UI are Mew-specific behavior that must remain intact.

### Upstream beta behavior

- Upstream adds `BatchDownloadCoordinator`, `BatchDownloadDialog` and `BatchDownloadQueuer`: select released episodes across seasons, plan sources by season, allow per-episode source override, then resolve direct/debrid streams and enqueue the chosen set.
- Upstream's download scheduler adds global/per-show concurrency limits, queued-item promotion, network/mobile-data policy, foreground re-advance and platform network guards.
- Upstream adds device-storage reporting, season-grouped/offline downloaded-show metadata, and broader download-management UI/settings.
- Upstream's `DownloadsRepository` and Mew's repository have different queue states and lifecycle strategies. Upstream uses `Queued` plus policy-driven promotion; Mew uses `Waiting` and the persisted per-batch `DownloadQueueMode`. Do not transplant queue code without reconciling status migration, pause/resume, background transfer restoration, profile changes, network changes and ordering.

### Semantic classifications and decisions

- Mew episode source resolution vs upstream batch planner: **SHARED PATTERN / DIFFERENT SEMANTICS**. **ADAPT candidate**, not wholesale TAKE. Mew already handles multi-episode selection/source discovery, but upstream's per-season planning, source override and selected-episode presentation may offer behavior to compare. Trace all action entry points and preserve Mew's provider/pending-search semantics.
- Mew `DownloadsRepository` vs upstream `DownloadsRepository`: **INTENTIONAL PARALLEL / DIFFERENT QUEUE CONTRACTS**. **SKIP** direct replacement. Consider individual scheduler ideas only after explicit design and migration review.
- Offline metadata: Mew stores a `DownloadDetailsSnapshot` per download and exposes `findOfflineMetaDetails`; upstream uses `OfflineMetadataRepository` and merges show/episode metadata. **SHARED PATTERN / DIFFERENT DATA MODEL**. Do not add a second offline metadata store without proving a missing case.
- Network policy/settings: upstream has user-configurable mobile-data and concurrency controls; Mew's batch queue mode is not equivalent. **NEEDS INVESTIGATION**; check Mew's existing download preferences, Android foreground service, iOS downloader, and profile-scoped persistence before proposing any addition.
- Download settings UI: upstream exposes preferred resolution, concurrency, mobile-data, download-button and storage-location settings. Mew's source-selection sheet stores preferred quality/Auto Select/queue mode. **SHARED PATTERN / DIFFERENT SEMANTICS**; avoid duplicate controls and preserve existing Mew settings UX.

### Tests and gaps

- Mew `DownloadsBatchFlowTest.kt` currently contains quality-key, logical-content-key and pending-search grouping tests. It does not demonstrate end-to-end batch planning, source override, queue scheduling or profile/network lifecycle.
- Upstream adds `DownloadQueuePolicyTest`, `EpisodeDownloadIndexTest`, and `StreamSourcePrefillSelectorTest` in the release range. Their existence does not prove Mew compatibility.
- Source tests in both repositories were inspected by path only; no Gradle tests/build/CI were run. **NOT RUN**.
- Follow-up before any integration proposal: compare Android/iOS downloader actuals, Mew foreground-service and WorkManager behavior, profile cleanup, offline metadata lifecycle, storage-location handling and all download action routes. Add regression tests for any adapted queue/source-planning behavior.


## Release-range comparison — 1.0.0 to 1.1.0-beta.1

- Compared upstream tag `1.0.0` (`6c2ad7937171965608d05005080b96ca19345379`) to `1.1.0-beta.1` (`eb32e0479932d28f5f6fa6afeb0e21c78ad02efd`).
- GitHub compare reports **64 commits, 157 changed paths, 16,621 additions and 1,522 deletions** across the range. The recursive tree and compare response were not truncated.
- Main-dev comparison for those 157 release-range paths: **78 paths absent**, **79 paths present but with a different blob**, and **0 paths byte-identical to the beta version**. This is a path-level triage only; it does not prove missing behavior or semantic incompatibility.
- Change concentration: 146 of 157 paths are under `composeApp`; the rest cover release workflows, README, iOS host/configuration, release scripts and store metadata.
- Main feature clusters represented in the changed paths: season batch downloads/storage management; library filters and profile-persisted display settings; subtitle styled-overlay/cue rendering; Stremio player/library tracking addons and consent; next-episode source fallback; TorBox alternative-hash cache handling; mixed RTL/LTR text; iOS download backup/network behavior; locale additions/updates; release channel and beta-store workflow.
- The release range also contains follow-up fixes and test additions not visible as separate headline bullets. Review commits and their complete diffs, not just the release notes.
- **No semantic decisions assigned yet** for these 157 paths. Do not treat path absence as automatic TAKE or blob difference as automatic SKIP. Search Mew's renamed equivalents, call sites, platform actuals, tests and persistence before deciding.
- Next audit batches: (1) downloads and storage lifecycle, (2) player/subtitle/source fallback, (3) addons/debrid/provider behavior, (4) library/details/UI, (5) localization and accessibility/RTL, (6) iOS platform differences, (7) CI/release/store/dependency changes.


> **Primary mission:** keep Mew aligned with the releases and complete behavior of `luqmanfadlli/NuvioMobile-Enhanced`. This is not a feature-picking exercise. Audit the complete upstream release surface—application code, UI/UX, player, providers, networking, settings, profiles, persistence, platform source sets, dependencies, build configuration, tests and release workflows—while preserving Mew-specific behavior. The stable-beta release is the comparison baseline; the moving `enhanced` branch is a freshness reference, not an automatic integration target.

## Latest upstream baseline

- Latest published beta release: [`1.1.0-beta.1`](https://github.com/luqmanfadlli/NuvioMobile-Enhanced/releases/tag/1.1.0-beta.1), published `2026-11-16T12:13:03Z`, marked prerelease.
- Exact tag commit: `eb32e0479932d28f5f6fa6afeb0e21c78ad02efd`.
- Upstream beta source tree: `8778a465da5a01077118a5be985a4d42eac1b452` (2,339 blob paths; recursive tree not truncated).
- Upstream `enhanced` branch: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Upstream `stable-release` branch: `5b8c6dc2ad3cf83846ccbe3e05e96d409f5b2ab4`. This is a separate branch ref; do not substitute it for the explicitly published beta tag without checking its release policy.
- The beta release notes contain 7 Nuvio Enhanced-specific items and 14 inherited NuvioMobile items. These are an index, not the complete audit: inspect the full tag tree and commits/diffs, including unmentioned dependency/build/config changes.

## Verified Mew refs and source-tree comparison

- Mew `upstream-sync` source snapshot base: upstream commit `f4b7e55e31c2778827095dbdb246193e8675016f`; later branch commits in this audit update only `docs/mew-context/README.md`. Verify the live branch head before every write.
- Mew `Main-dev`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Mew `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (same head as Main-dev; leave untouched).
- Recursive tree comparison of beta tag vs Mew `upstream-sync`: beta has 2,339 blobs; Mew has 2,340 including its context document. All 2,339 upstream paths are present; no upstream paths are removed. The two differing source/metadata paths are:
  - `iosApp/iosApp/ContentView.swift`: Mew's blob exactly matches upstream `enhanced` HEAD, including the post-beta removal of the blurred band behind the floating tab bar.
  - `store-beta.json`: Mew's blob exactly matches upstream `enhanced` HEAD, including the published `1.1.0-beta.1` AltStore/SideStore entry.
  - `docs/mew-context/README.md`: Mew-owned documentation overlay.
- **Correction to the earlier audit note:** those two paths are not verified Mew-specific overlays. Their exact blob SHAs match upstream `enhanced` HEAD. The current `upstream-sync` tree is a mixed snapshot: it includes the beta tag source plus these two post-beta upstream paths, but does not include the other five post-beta `enhanced` changes listed below. This is a tree-level result, not a build/runtime validation.
- Recursive tree comparison of beta tag vs `Main-dev`: Main-dev has 1,668 blobs; 706 same-path files have different blob contents, 1,133 beta paths are absent from Main-dev, and 462 Main-dev paths are absent from the beta tree. These counts include source, assets, vendored/native files and build/configuration files; they are not a feature count or a merge recommendation. Main-dev is substantially divergent and needs a structured path/behavior audit before integration.

## Release 1.1.0-beta.1 audit ledger

Release notes enumerate the following changes to trace through the tag's actual commits/files and compare semantically with Mew:

**Nuvio Enhanced layer**
- iOS navigation-controller extension import for bar anchor.
- Remove duplicated Brazilian Portuguese strings.
- MDBList: do not use show runtime as episode length.
- Details hero mute button mirrored on back button.
- Indonesian localization update.
- iOS pushed-page top blurred-strip removal.
- Download whole seasons and manage storage.

**Inherited NuvioMobile layer**
- Library remembers list/type and adds genre, year and watched filters.
- Android gesture-navigation player bottom-controls positioning fix.
- TorBox alternative-hash cache matching.
- Seconds-unit spacing localization.
- Subtitle outline width and styled-overlay/native-renderer matching.
- High-fidelity Brazilian Portuguese localization.
- Ukrainian localization.
- Stremio player and library-tracking addon support.
- Fallback from next episode's auto-selected source.
- Keep iOS downloads out of iCloud/computer backups.
- Hebrew localization update.
- Mixed RTL/LTR text correction in Search and Watch Party.
- Simplified Chinese localization.
- Urdu episode-shuffle localization.

**Audit rule:** every item above must be traced to implementation, callers, tests and platform scope; then search for renamed/equivalent Mew behavior. Also inspect every other beta-vs-Main-dev tree delta, including dependencies, Gradle/version catalog, manifests, native libraries, workflows, assets and release metadata. Do not assume release notes cover all meaningful changes.

## Decisions and write boundaries

- Classify each candidate: `TAKE`, `ADAPT`, `SKIP`, `ALREADY IMPLEMENTED`, or `BLOCKED`.
- Duplicate classifications: `TRUE DUPLICATE`, `INTENTIONAL PARALLEL`, `SHARED PATTERN / DIFFERENT SEMANTICS`, `COMPATIBILITY SHIM`, or `FALSE POSITIVE`.
- Compare behavior and contracts, not filenames: inputs/outputs, side effects, lifecycle, persistence/migrations, errors, callers, source sets, UI/navigation/accessibility and dependency/API impact.
- Keep `upstream-sync` as the upstream reference snapshot plus clearly marked Mew documentation/overlays. `Main-dev` is the selective integration target. `main` remains untouched.
- Audit first. Do not change application code, merge, cherry-pick, force-push, rewrite history, publish or promote without explicit approval. Any approved write must verify the target branch and exact current SHA immediately beforehand and read back the result.
- Integrate in small logical batches only after the complete impact and regression review. Preserve Mew branding, CloudStream compatibility, player UI, provider behavior, profile/settings data and platform-specific work unless a reviewed change requires otherwise.

## Current audit progress and verification

- **Baseline identification: complete.** Latest published beta tag, exact commit and source tree are verified.
- **Tree alignment: complete.** `upstream-sync` matches the beta source tree except for the documented iOS host and beta-store overlays plus this Mew-owned context file.
- **Full semantic release audit: in progress; not yet complete.** Tree counts do not establish that every behavior, caller, dependency or test has been reviewed.
- **Main-dev integration assessment: in progress.** The large tree divergence requires grouped comparison and duplicate/call-site review; no automatic merge is justified.
- Remote GitHub source/tree inspection only. No local checkout, Gradle build, tests, CI run, emulator or device verification was performed for this baseline comparison. Tests are **NOT RUN**.
- No application source was changed in this update. Only this context Markdown is being updated on `upstream-sync`.

## Next execution order

1. Trace every 1.1.0-beta.1 release-note item to exact commits/files and audit all changed files in the release range.
2. Compare the complete beta source tree against Main-dev in logical groups: build/dependencies/manifests; common core/data; providers/addons; player/subtitles/downloads; UI/navigation; persistence/profiles/settings; Android/iOS actuals; assets/localization; CI/release.
3. Search Mew for equivalent behavior under different names/routes and audit callers/source sets before assigning decisions.
4. Record each group in this ledger with upstream path/commit, Mew equivalent, semantic delta, risk, decision, tests and verification status.
5. Produce a prioritized integration plan for Main-dev. Do not implement until the user explicitly approves the proposed batch.

---

# Mew Context Maintenance

This directory is the durable, repository-side record of Mew audit and integration context. It is intentionally separate from application source code.

## Source of truth

Use this order when facts conflict:

1. Current GitHub branch refs, commit history, repository files, and CI results.
2. This repository-side context record and approved project decisions.
3. ChatGPT Project instructions and context snapshot.
4. Earlier chat summaries.

A remembered SHA, test result, or prior assistant statement is only a lead. Re-fetch mutable facts before relying on them.

## Branch roles

- `upstream-sync`: upstream-derived reference snapshot. Do not merge its history wholesale into Mew. This document is a Mew-owned maintenance note added at the user's explicit request; it is not an upstream feature port.
- `Main-dev`: destination for reviewed, selective integration.
- `main`: stable development/release branch; leave untouched until explicit promotion approval.
- Mother upstream: `luqmanfadlli/NuvioMobile-Enhanced`, branch `enhanced`.

Before any sync or write, fetch the relevant refs and record their exact SHAs. For branch-ref updates, use an expected-SHA/lease guard. Never force-update a branch to work around a mismatch.

## Context update workflow

### 1. Inspect before editing

- Re-fetch `upstream-sync`, `Main-dev`, `main`, and mother-upstream refs as relevant.
- Record the current SHAs and compare metadata.
- Inspect the current repository-side context file and the ChatGPT Project instructions/snapshot.
- Search the repository for the behavior and its callers; do not infer from filenames alone.
- Check whether the claimed finding is already implemented under another name, source set, route, or layer.

### 2. Write only verified facts

For each audit item record:

- feature/behavior and user-visible impact;
- upstream commit and file paths;
- Main-dev equivalent, including renamed or cross-source-set implementations;
- semantic differences: inputs, outputs, side effects, lifecycle, error behavior and callers;
- dependency/API/schema changes and compatibility risks;
- UI/navigation/accessibility/theme/state-restoration impact;
- tests present and tests actually run;
- decision: `TAKE`, `ADAPT`, `SKIP`, `ALREADY IMPLEMENTED`, or `NEEDS INVESTIGATION`;
- exact next verification step.

Separate confirmed facts from hypotheses and open questions. Correct prior mistakes explicitly rather than silently carrying them forward.

### 3. Keep the audit and integration separate

- Audit first; do not edit application code during an audit-only request.
- Never wholesale-merge the divergent upstream history.
- Propose small logical integration batches for `Main-dev`, with exact files, risks and verification plan.
- Implement only after explicit user approval.
- Do not merge, force-push, rewrite history, delete branches/files, publish releases, or change project settings without explicit approval.
- Keep Mew-specific application ID, signing, provider compatibility, profile data, settings, downloads and playback behavior unless a reviewed change explicitly requires otherwise.

### 4. Verify every change

- Inspect Gradle tasks, workflows, version catalog and source sets before selecting build/test commands.
- Run the narrowest relevant tests first, then justified broader checks.
- Inspect diffs and test reports.
- A skipped, queued, or in-progress CI job is not a pass.
- Do not claim local build, device UI, test, merge, upload or code-change success without direct tool evidence.
- If no local checkout/device is available, say so plainly; remote source inspection is not runtime validation.

### 5. Maintain ChatGPT Project context

The ChatGPT Project context is a concise handoff, not a competing source of truth.

- Update it only after a meaningful milestone or before a handoff.
- Include current verified refs, audit decisions, exact files, test outcomes and remaining risks.
- Do not copy the entire repository audit into Project instructions. Keep stable working rules in Project instructions and changing progress in the context snapshot.
- Prefer updating the repository-side Markdown through GitHub on the explicitly requested branch, then read the committed file back and verify its commit/path/content.
- If a separate ChatGPT Library upload is requested, first list/read the current Library item and note its exact `library_file_id` and version. Materialize the exact current file, edit that materialized copy, verify the exported source `file_id`, upload with the expected current version, then list/read it again. Never guess IDs, reuse stale IDs, or retry blindly after Not Found.
- If Library upload tooling fails, stop repeated attempts. Report the Library item/version that remains unchanged and use the verified repository copy as the working source until the Library path is repaired.

## Duplicate/overlap audit (mandatory)

Do not call something a duplicate from filenames alone. Search by:

- same user-visible behavior under different names/screens;
- same algorithm with renamed symbols or small guard/literal changes;
- duplicate routes, settings, repositories, services, providers, adapters, models and state holders;
- repeated event handling, formatting, networking, caching, playback and error handling;
- commonMain/androidMain/iosMain equivalents and legacy/new implementations.

For each suspected overlap compare inputs, outputs, side effects, lifecycle, errors, callers and platform scope. Classify it as true duplicate, intentional parallel implementation, shared pattern with different semantics, compatibility shim, or false positive. Do not refactor intentional parallel implementations just to reduce line count.

## UI and regression checks

Before changing a screen inspect navigation entry points, state, loading/empty/error states, accessibility semantics, dark/light theme, small/large screens, back behavior, focus/keyboard and restoration.

Treat playback, providers, networking, downloads, profile/account lifecycle and settings as regression-sensitive. Treat TLS, hostname verification, secrets, cleartext policy, crypto, manifests, signing and CI publishing as security-sensitive.

## Required handoff format

Keep reports concise and factual:

- **Refs:** branch names and verified SHAs.
- **Inspected:** exact files/commits and search scope.
- **Findings:** confirmed behavior and semantic differences.
- **Decision:** take/adapt/skip/already implemented/needs investigation.
- **Changes:** exact paths and commit SHA, or explicitly “no code changes”.
- **Verification:** exact commands/checks and outcomes; distinguish not run from failed.
- **Risks / next step:** unresolved items and the smallest next action.

Never describe a partial audit as exhaustive. State API limits, missing local checkout, unavailable device tests, or uninspected call sites.


## Continued semantic audit — feature overlap and server boundaries (2026-11-17)

### Historical refs used for the initial audit segment

These refs describe the earlier inspection state; the latest verified refs and follow-up findings are recorded at the end of this document.
- Mew `Main-dev`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Mew `upstream-sync`: `1028f9a2c4ce7b6a73026eb241941a957ce2d5fb` (includes this repository-side context document commit).
- Mother upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- These refs were re-fetched during this audit. No application source or branch ref was changed.

### Episode shuffle
- `upstream-sync` contains `features/shuffle/EpisodeShuffleSheet.kt` (SHA `201ed9e9aaa34004d910184b93325ad3923f1dc1`) and `EpisodeShuffleRepository.kt` (SHA `0b527a0425dbabd7f367b613d378ea97977cc753`).
- The inspected `Main-dev` paths for the shuffle sheet/repository are absent (GitHub Contents API returned 404 for those exact paths). This supports the earlier finding that shuffle is a new feature in the inspected tree, not proof that every possible differently named equivalent is absent.
- Decision remains **TAKE candidate**, not approved for integration. Before implementation, trace every upstream entry point and playback handoff; verify watched/unwatched selection, repeat history, empty/failed selection, profile switch/removal, lifecycle cancellation and UI dismissal/state restoration. Port relevant unit/UI tests with the feature.

### Custom posters versus Mew poster customization
- `upstream-sync` has `core/poster/CustomPosterUrlResolver.kt` (SHA `050dd51359869fb3d67728ad480001ee357fbfe7`) and expect/actual storage contract `CustomPosterUrlStorage.kt` (SHA `943689412dd69307c91792af35413f6d30fc528e`).
- Exact custom-poster resolver/storage paths are absent from `Main-dev`. However, `Main-dev` does have `features/settings/PosterCustomizationSettingsPage.kt` (SHA `4c67989986c1a889608f2da6e745034b6b9174ae`); the similarly named upstream-sync settings page is a different blob (SHA `0d1b16a5894f8b8658d46c701450d5f591e8edae`). These are related poster-domain settings, not proof of equivalent URL-template behavior.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Decision remains **ADAPT candidate**. Preserve Mew card appearance/layout controls. Before porting, trace all image consumers and storage actuals; validate schemes/hosts/redirects, placeholder escaping, cache keys, fallbacks, per-screen enablement and profile isolation.

### External media servers versus Nuvio backend configuration
- `upstream-sync` contains a separate `features/servers/ServerRepository.kt` (SHA `6e770e38514831e35092a840aff84145a7e08f7e`), `ServerStorage.kt` expect contract (SHA `bde79fe01d16fbd1d5ec5bf6644f57a8d5a25231`), `mediabrowser/MediaBrowserProvider.kt` (SHA `0765cbd53fdaa6c2cd8e046c3dcce92de5cede23`) and `core/network/ServerDiscovery.kt` (SHA `fdcb8d283ce41fb21c0ce3ae2717be36fc249d32`).
- Those exact feature paths are absent from `Main-dev`. Main-dev's `core/network/ServerConfiguration.kt` (SHA `c658108f1708961bb8d25bec21624e9ba540eee0`) configures the Nuvio backend URL/publishable key/capabilities; it is not the same feature as user-managed Jellyfin/Emby servers.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS** (both concern server configuration, but different server roles and contracts). Decision remains **NEEDS INVESTIGATION** as a phased feature, not a drop-in replacement.
- Before any integration, inspect Android/iOS storage actuals, credential/token protection and deletion, redirects/TLS, server discovery, catalog mapping, playback/transcoding, watch-state sync, profile switching/removal and all settings/navigation callers. Keep external server credentials separate from Nuvio backend configuration and ordinary profile settings sync.

### Verification limits and next step
- This was a selective path/blob inspection using GitHub remote files. No local checkout/worktree, Gradle tasks, runtime, emulator/device, full call graph or complete test suite was available/verified.
- No application code, branch ref, merge, cherry-pick, PR, build, test, or release was changed/run.
- Continue with storage expect/actual implementations and all feature entry-point/caller searches before proposing any integration batch. No integration write without explicit approval.


### Storage and profile-lifecycle follow-up

- Upstream server repository is profile-aware: it tracks `loadedProfileId`, clears its in-memory token map on profile load/change, and exposes `onProfileChanged()`, `clearLocalState()`, and `removeProfile(profileId)`. Profile removal deletes credential references and that profile's connection/sync records; connection completion checks a generation/profile guard before persisting. This is a useful lifecycle design to preserve if the feature is adapted.
- Android `ServerStorage.android.kt` stores encrypted values in private SharedPreferences using AES-GCM with an Android Keystore key and key/key-alias AAD. iOS `ServerStorage.ios.kt` uses Keychain generic-password items scoped to a service; newly added items use `AfterFirstUnlockThisDeviceOnly`. This is materially different from ordinary profile preference storage. Before integration, audit key invalidation/recovery, logout/profile deletion, error propagation and whether tokens enter any sync/export path.
- Custom poster pattern and enabled-screen preferences use `ProfileScopedKey.of(...)` on both Android SharedPreferences and iOS NSUserDefaults. This confirms profile-scoped persistence for these settings; it does not establish that image cache entries are profile-scoped or that URL targets are safe.
- Resolver inspection shows placeholder parsing/caching and RPDB-family fallback behavior. URL scheme/authority validation, redirect policy and final image-request enforcement were not established by this resolver-only inspection; inspect the image loader/interceptor and every poster call site before taking it.
- Classification: server credentials = **INTENTIONAL PLATFORM PARALLEL** (Android Keystore AES-GCM vs iOS Keychain); custom poster storage = **INTENTIONAL PLATFORM PARALLEL** (SharedPreferences vs NSUserDefaults), both sharing profile-scoped semantics. Do not consolidate platform actuals into common storage.


## Caller, profile-isolation and security audit — 2026-11-17 (continued)

### Current verified refs
- Mew `upstream-sync`: `0c605a21c4a15b4b3113ad2ca94016f2e1c6bea8`.
- Mew `Main-dev`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Mew `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (same head as Main-dev; left untouched).
- Mother `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Re-fetched immediately before this context update. The repository-side context file is on `upstream-sync`; its commits are documentation-only and mean the branch is not a byte-for-byte upstream mirror.

### Episode Shuffle — caller and lifecycle trace
- Main entry points in upstream: `features/details/MetaDetailsScreen.kt` (SHA `b06a0cfc4d2c3d63d5744d8a943045e9680224af`) opens `EpisodeShuffleSheet` and uses shuffle for the series primary action; `features/home/HomeScreen.kt` (SHA `13c4b20a6dab55559628ba3366f4a7836c2ec2ae`) calls `rememberShuffleHomeItems`; `features/home/HomeEpisodeShuffle.kt` (SHA `7a69e17e3a403b44bf1325f98bf712fbe8e9ccd2`) resolves metadata, watched/progress state and home shuffle targets.
- Core behavior is split across `EpisodeShuffle.kt`, `EpisodeShuffleRepository.kt`, `RandomEpisodePicker.kt`, `EpisodeShuffleSheet.kt`, `ShuffleBadge.kt`, and `ShuffleEpisodeState.kt`; it is not a screen-only feature. It tracks profile/content/surface/filter sessions, preserves selection history across metadata refresh, excludes unavailable/future/unreleased episodes and respects watched/partial progress.
- Android storage is initialized in `MainActivity.kt` and uses profile-indexed SharedPreferences; iOS uses profile-indexed NSUserDefaults. Account wipe includes the shuffle storage key on both platforms and calls `EpisodeShuffleRepository.clearLocalState()`.
- **Confirmed profile-deletion gap:** `ProfileRepository.createProfile()` chooses the lowest free index in `1..MAX_PROFILES`. `deleteProfile()` removes server credentials and subtitle-device preferences, but does not remove the deleted profile's shuffle payload. `EpisodeShuffleStorage` exposes only load/save; there is no remove-profile operation. Reusing a deleted profile index can therefore restore that previous profile's shuffle settings.
- Tests exist: `EpisodeShuffleTest.kt`, `RandomEpisodePickerTest.kt`, and `ShuffleProjectionTest.kt`. They cover selection cycles, no immediate repeats, watched/progress filtering, refresh stability, per-profile session histories and home projection. They do not establish storage cleanup on profile deletion/index reuse.
- Decision: **ADAPT**. Add profile-specific storage deletion and repository cleanup, wire it into both anonymous and authenticated profile deletion, and add a profile-index-reuse regression test before integration. Preserve the existing home/details/playback semantics and UI.

### Custom Posters — actual consumers, fallback and cleanup
- The settings page is `features/settings/PosterCustomizationSettingsPage.kt` (SHA `0d1b16a5894f8b8658d46c701450d5f591e8edae`); it is exposed from Settings and provides a URL-template field plus per-screen toggles. `CustomPosterUrlRepository` stores the pattern and enabled-screen state. `CustomPosterOverlay.kt` provides overlays for MetaPreview, LibraryItem, ContinueWatchingItem, MetaDetails, TMDB entity rails and person credits.
- Home Continue Watching applies the custom poster overlay in `HomeScreen.kt`. Poster rendering uses `NuvioPosterCard` / `posterImageModel` and `PosterGrid` to attach the original artwork as a Coil fallback; `CustomPosterFallbackInterceptor` retries the original image when a custom URL fails. Android's application ImageLoader and the common iOS app loader register the interceptor.
- Resolver tests cover ID extraction, placeholder expansion, optional/pipe placeholders, shapes and RPDB-family fallback; overlay tests cover replacement/restoration and shape behavior. These tests do not establish safe URL policy or account/profile cleanup.
- **Confirmed profile-deletion gap:** custom poster settings are stored under `ProfileScopedKey` keys, but `ProfileRepository.deleteProfile()` does not remove those keys. Because profile creation reuses the lowest free index, a new profile can inherit the deleted profile's custom poster pattern and screen toggles.
- **Confirmed account-wipe gap on both platforms:** Android `PlatformLocalAccountDataCleaner.preferenceNames` omits the `nuvio_custom_poster_url` preference file. iOS `profileScopedBaseKeys` omits `custom_poster_url_pattern` and `custom_poster_enabled_screens`. The common `LocalAccountDataCleaner` also does not call `CustomPosterUrlRepository.clearLocalState()`, despite that reset method existing. Thus old custom-poster settings can survive account wipe; in-memory state can also remain loaded until a later profile reload.
- The settings editor accepts arbitrary pattern text; resolver inspection shows placeholder substitution, not final URL scheme/host validation. The final Coil request boundary, redirect policy and local-network target handling were not proven safe by this audit. This is a required review item, not a confirmed exploit.
- Decision: **ADAPT**. Preserve Mew's existing poster sizing/layout controls; add profile deletion and account-wipe cleanup on Android+iOS, reset in-memory repository state, validate the final resolved URL at the image-request boundary, and add profile-reuse/account-wipe regression tests.

### External Media Servers — entry points and security boundary
- Settings route is Content Discovery → Media Servers → server detail. Upstream files include `MediaServersSettingsPage.kt`, `MediaServerSettingsPage.kt`, and `ServerSignInSheet.kt`; Jellyfin/Emby provider logic lives in `features/servers/mediabrowser/MediaBrowserProvider.kt`.
- Android credential persistence uses AES-GCM with Android Keystore; iOS uses Keychain generic-password items. Repository profile switching clears the in-memory token cache; profile deletion calls `ServerRepository.removeProfile()` to delete connection records and referenced credentials.
- **Cloud-sync security dependency:** `SyncedServer` serializes the external server access token as `token`; `MediaServerSync` sends that payload through Supabase RPC `sync_push_media_servers` and retrieves it through `sync_pull_media_servers`. Local encryption does not protect the separate cloud copy. This client-source audit did not verify the RPC implementation, database/RLS policies, server-side encryption/retention or account/profile authorization.
- Discovery accepts HTTP and HTTPS input, rejects user-info in the authority, bounds the response and rejects an HTTPS-to-HTTP downgrade based on the final response URL. MediaBrowser sign-in performs a public-info redirect retry without sending the token on that first public request. Full Ktor client redirect behavior, arbitrary server URL handling, playback URL/token exposure and backend RPC policy remain to be audited.
- Some playback delivery URLs include the server token as an `api_key` query parameter, as required by the provider API. Before taking this feature, trace where those URLs can be logged, cached, persisted, surfaced to external players or included in diagnostics.
- Decision: **NEEDS INVESTIGATION** as a separate feature batch. Do not conflate it with Mew's Nuvio backend configuration. Verify backend RPC/RLS and credential lifecycle, Ktor redirect/TLS policy, playback URL handling and server discovery before proposing integration.

### Main-dev comparison and verification limits
- The exact upstream paths for shuffle, custom-poster URL resolver/storage and external media-server management are absent from the inspected `Main-dev` tree. Mew's existing poster customization page and Nuvio backend configuration are related domains but different semantics.
- GitHub remote source/tree inspection only. No local checkout/worktree, Gradle build, tests, CI run, emulator or device verification was available or performed. The tests listed above are present in source but **NOT RUN**.
- No application source, branch ref, merge, cherry-pick, PR or release was changed. Only this context Markdown is being updated.
- Next: audit the MediaBrowser HTTP client/redirect configuration and all custom-poster consumer call sites; then prepare a small integration plan with profile/account cleanup and regression tests. No code integration without explicit approval.


### Verified call-site map and MediaBrowser redirect follow-up

- All six custom-poster screen toggles have concrete consumers in the inspected upstream source:
  - `HOME`: `HomeRepository.kt` (SHA `243e57e5ebcec79496af759b939d826d688c8867`) applies/reapplies the Home pattern.
  - `CONTINUE_WATCHING`: `HomeScreen.kt` (SHA `13c4b20a6dab55559628ba3366f4a7836c2ec2ae`).
  - `COLLECTIONS`: `FolderDetailRepository.kt` (SHA `f5e17b4e2842262206399d96f96cb2c908cb3974`).
  - `LIBRARY`: `LibraryScreen.kt` (SHA `95bbf5986101594a5aebfc3a4d320b7fda1961b0`).
  - `SEARCH`: `SearchRepository.kt` (SHA `182e630896fc10353127c97ee3b27f8dec671f25`).
  - `DETAILS`: `MetaDetailsRepository.kt` (SHA `9112378db6933cc69f0adc9ca42c5a0a84f947c0`), `TmdbEntityBrowseScreen.kt` (SHA `b527f9dc01ab8f4be918c6027feea6a56640f475`) and `PersonDetailScreen.kt` (SHA `79fa2f62a8f158f09a4278e0c4d5bba6ec2d0f87`).
- Therefore the six settings toggles are not dead controls; earlier checks of only the screen composables were insufficient because several transformations live in repositories. Keep this classification as **FALSE POSITIVE** for suspected unused toggles.

### MediaBrowser HTTP redirect behavior — partially verified
- Emby and Jellyfin providers default to `createApiHttpClient()`. Android and iOS actual clients both set `followRedirects = false`; Android's OkHttp engine also sets `followRedirects(false)` and `followSslRedirects(false)`. The shared Android base client only adds `NuvioDns`.
- `MediaBrowserProvider.publicInfo()` handles a 3xx response manually and retries the public-info request with `token = null`. Authenticated API requests do not automatically follow redirects through these clients. This reduces the risk of forwarding the authorization header to a redirect target.
- `normalizeServerAddress()` accepts HTTP and HTTPS and defaults an address without a scheme to `http://`. This may be needed for LAN servers, but credentials and playback requests can consequently use cleartext HTTP if the user supplies/accepts it. Do not silently disable HTTP; require a separate compatibility/security decision and consider clear warning/confirmation.
- Playback URLs can contain the server token in an `api_key` query parameter. Audit player URL logging, diagnostics, history/cache and external-player handoff before integration.
- Still unverified: `httpRequestRaw` transport/redirect policy used by `ServerDiscoveryService`; backend RPC/RLS/retention for cloud-synced server tokens; and final custom-poster request/redirect restrictions. The prior NEEDS INVESTIGATION decision for Media Servers remains unchanged.


### Raw HTTP platform parity — confirmed resource-limit mismatch

- Common expect contract `AddonPlatform.kt` (SHA `c0e30fcebfc3a1b473fa852c42f5e2c029ad6b6b`) exposes `httpRequestRaw(..., followRedirects, maxResponseBodyBytes, ...)`.
- Android actual `AddonPlatform.android.kt` (SHA `ee184e07d075a3687b154c94f9a13def7b0fb153`) honors `followRedirects`: it uses the shared client when true and clones it with both redirect flags false when false. It streams at most the requested body limit using `readAtMostBytes`.
- iOS actual `AddonPlatform.ios.kt` (SHA `c556d5b8fd3c15d95724114583098ef4c0224206883`) does not use the `followRedirects` argument at all. It reads the entire response into `ByteArray` first, then truncates to `maxResponseBodyBytes`. Thus the flag is not enforced on iOS and the size limit bounds returned data but not memory consumed while downloading.
- Verified caller: `ServerDiscoveryService.discover()` requests a 64 KiB + 1 byte cap and checks the returned body size; it relies on the default redirect behavior. On iOS, an oversized response can still be fully buffered before the service rejects the truncated result. This is a real cross-platform resource-bound mismatch, not just a theoretical call-site concern.
- Decision: **ADAPT / FIX BEFORE PORTING SERVER DISCOVERY**. Implement streaming bounded reads on iOS and honor `followRedirects` in the iOS actual. Add platform-level tests or a testable bounded-reader abstraction covering oversized responses, redirect disabled/enabled, cancellation and final response URL. Search all callers before changing the shared contract.


### Main-dev profile-index comparison
- Main-dev `ProfileRepository.kt` (SHA `750e1b49e32df98effd5c4bd6a7be2e94a86f2ee`) also allocates the lowest free profile index in `1..MAX_PROFILES`. Its current `deleteProfile()` path removes the profile and PIN cache but has no generic per-feature storage cleanup hook in the inspected section.
- This is an existing architectural constraint, not a reason to copy incomplete cleanup from upstream. Any newly ported profile-indexed store (shuffle/custom posters/server data) must explicitly remove its index on deletion and reset in-memory state when the active profile changes. Keep this scoped to the feature integration; do not silently refactor unrelated Mew profile stores.
- Main-dev account wipe currently clears its known repositories and platform storage through `LocalAccountDataCleaner`; it has no custom-poster/shuffle/server feature to clean yet. New storage keys must be added to both platform wipe implementations as part of their respective feature ports.


### Stream-fetch duplicate audit and Mew adaptation boundary

- Upstream has two separately consumed stream flows:
  - `StreamsRepository` feeds the dedicated stream-selection screen. It owns request tokens, loading placeholders, downloaded/embedded groups, empty states and auto-play decisions.
  - `PlayerStreamsRepository` explicitly documents that it owns separate state for the in-player source and episode panels so it does not interfere with the main `StreamsRepository`. It also has separate cancellation, filters, pause-search-on-playback and cleanup behavior.
- Both upstream flows call `ServerStreams.sources(...)` and merge server sources alongside addon/plugin sources. This shared behavior is **INTENTIONAL PARALLEL** at the repository/UI-state level, not a reason to collapse the two flows.
- Main-dev has the same two separate entry points, but both contain substantial Mew CloudStream-specific behavior:
  - `features/streams/StreamsRepository.kt` SHA `18dd4ddb2c4aa90128bd9a179ed487a4a507d331` includes CloudStream route parsing, registry revision/cache keys, provider groups, direct-route resolution and bounded provider concurrency.
  - `features/player/PlayerStreamsRepository.kt` SHA `b5d26cd370bb531a6ebf5f5d5b83e886acd85bec` has separate player-panel state and CloudStream route/provider resolution with its own concurrency.
- Main-dev has no `ServerStreams`, `ServerPlayback` or `PlayerScreenRuntimeServerTracks.kt` equivalent at the inspected paths. Server integration must be adapted into both Mew flows without dropping CloudStream route handling, plugin pause semantics, concurrency, filters, request generations or the separate UI states.
- Upstream player integration is in the player runtime rather than the engine:
  - `PlayerScreenRuntimeSourceActions.prepareServerForPlayer()` calls `ServerPlayback.prepare()` for streams requiring server preparation.
  - `PlayerScreenRuntimeServerTracks.kt` supplies server audio/subtitle track state, switching/restart and session stop.
  - `PlayerScreenRuntimePlaybackActions.kt` deliberately excludes server-source URLs from `lastSourceUrl`, reducing persistence of token-bearing playback URLs.
  - `ServerPlayback` also tracks active sessions by playback URL and reports playback lifecycle events.
- The upstream `StreamSourceLoader.kt` contains a third `resolveTargets/fetchAddonGroup/loadStreams` path with overlapping addon/plugin/server target resolution. No caller was identified in the inspected `StreamsRepository`, `StreamsScreen`, `PlayerStreamsRepository`, `PlayerScreenContent`, `PlayerScreenRuntimeUi`, `PlayerScreenRuntimeSourceActions`, `StreamSearchSessions`, `StreamAutoPlaySelector` or `SeriesPlaybackResolver`; no corresponding test file was found. Classification: **NEEDS INVESTIGATION / POSSIBLE ORPHAN**, not a confirmed duplicate. Do not port or refactor it until repo-wide references and history are checked.
- Decision: **ADAPT** the server-source/provider/playback behavior into Mew's existing two flows. Do not replace Mew's stream repositories, do not introduce `StreamSourceLoader` by assumption, and keep the player UI/engine unchanged unless a verified server-playback requirement proves otherwise. Add regression coverage for CloudStream + addon + plugin + server coexistence in both flows.


### Follow-up audit — upstream freshness, profile cleanup and platform contracts (2026-11-17)

#### Current verified refs and upstream snapshot comparison
- Mew `Main-dev`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (tree `db06d0a44367f7881460317f0d7a7233ed6bc115`).
- Mew `main`: same SHA as `Main-dev`; neither branch was changed.
- Mew `upstream-sync`: `80b263bc983c75b41153509b434c34bb43eba5fb` (tree `72c33f51c9fd82e8ef5378d8868f3d851a00d8c3`).
- Mother upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03` (tree `519923a50e2b8c3f226bcafa4e46c6160b8095d3`).
- The app-code snapshot on `upstream-sync` is based on upstream commit `f4b7e55e31c2778827095dbdb246193e8675016f` (2026-11-17 01:19 UTC); subsequent Mew commits on that branch in this audit were docs-only.
- Comparing that source tree with current mother-upstream: 2,339 source blobs on each side; 2,334 identical and exactly 5 changed. `upstream-sync` additionally contains the Mew-owned `docs/mew-context/README.md`. The five changed paths are listed below. This is a freshness delta, not permission to overwrite the branch.

#### Five upstream changes newer than the current upstream-sync snapshot

1. **Bug report template** — upstream commit `05e779a0c17861e185c09901e8be4a5f0f86ced5`, `.github/ISSUE_TEMPLATE/bug_report.yml`.
   - Adds iOS sideloading choices and asks whether the official app has the same bug.
   - Mew's current template still lists Android/iOS store and TestFlight options. Decision: **ADAPT** only the useful upstream-comparison question and Android-relevant install methods; do not copy iOS-only choices into an Android-only report flow.

2. **Episode artwork fallback** — upstream commit `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`, `DetailSeriesContent.kt`.
   - Upstream adds `rememberEpisodeImageState(thumbnail, fallbackImage)` and switches to series artwork after the episode thumbnail fails, in both episode-card layouts.
   - Main-dev `DetailSeriesContent.kt` (`e4c7a22a8e561bfcb4d33a3ef647e25a5e666783`) still uses `video.thumbnail ?: fallbackImage` in both layouts and has no equivalent image-error state.
   - Decision: **ADAPT** the fallback behavior into both Mew layouts without changing their design. Add a regression test for primary success, primary failure/fallback success, no fallback and fallback failure.

3. **Home catalog sync race** — upstream commit `0b9d93599f7d7603e7814a99a70aa64d7c48e086`, `HomeCatalogSettingsRepository.kt`.
   - Upstream preserves local preferences when `knownKeys.isEmpty()`, covering remote sync arriving before catalog definitions load.
   - Main-dev (`6ca42fffc3348c62b337310cbf3ee1d949cc407d`) still uses `key !in remoteKeys && (key in knownKeys || key.requiresExplicitSyncKey())`; it lacks the empty-known-keys guard.
   - Decision: **ADAPT** the guard after adding a repository-level test for remote sync before definitions load. Preserve Mew's snapshot store, normalization and sync behavior.

4. **iOS subtitle state reset** — upstream commit `c5298633914d967792c79351754ba47d54dfd242`, `PlayerEngine.ios.kt`.
   - Upstream changes `remember(sourceUrl)` to an unkeyed state plus `LaunchedEffect(sourceUrl)` reset so styled subtitles survive episode switching correctly.
   - Main-dev's iOS engine (`288d8efa1115f395f934e6c9a3bbe64dab610819`) does not contain the same `subtitleText` state.
   - Decision: **SKIP** as a direct port; revisit only if Mew adopts the same iOS subtitle overlay architecture.

5. **iOS settings export picker** — upstream commit `d703ca48e5ade22ceaf424451ffba05efa94e670`, `SettingsConfigFileLauncher.ios.kt`.
   - Upstream switches to the export-to-service picker initializer.
   - The exact iOS launcher path is absent from Main-dev (GitHub Contents API 404).
   - Decision: **SKIP** for the current Android fork; do not create an iOS implementation solely to match upstream.

#### Confirmed profile-cleanup blockers in upstream feature candidates

**Custom poster URLs**
- `CustomPosterUrlStorage` stores `custom_poster_url_pattern_<profileId>` and `custom_poster_enabled_screens_<profileId>` using `ProfileScopedKey`.
- `ProfileRepository.selectProfile()` reloads the repository, but both anonymous and authenticated `deleteProfile()` paths only remove server and subtitle-device profile data; they do not remove custom-poster keys.
- `CustomPosterUrlRepository` has `clearLocalState()` but no per-profile removal method. Upstream `LocalAccountDataCleaner` does not call it.
- Android account wipe does not include the `nuvio_custom_poster_url` SharedPreferences file. iOS account wipe does not include either custom-poster key in its profile-scoped key list.
- Result: profile-index reuse can inherit a deleted profile's custom-poster settings; account wipe can leave both persisted settings and in-memory repository state. This is a confirmed cleanup gap.
- Decision: **ADAPT / BLOCKER BEFORE INTEGRATION**. Add per-profile removal and call it in both profile-deletion paths; clear repository state on account wipe; add Android and iOS storage keys to account-wipe cleanup; test profile-index reuse and sign-out/account wipe.

**Episode shuffle**
- `EpisodeShuffleStorage` persists `episode_shuffle_<profileId>`. Profile switching reloads state; account wipe clears repository state and the Android/iOS platform cleaners include the `episode_shuffle` storage.
- Neither `EpisodeShuffleRepository` nor `ProfileRepository.deleteProfile()` removes the deleted profile's shuffle key.
- Result: deleting a profile and reusing its index can restore the previous profile's shuffle availability and per-show choices.
- Decision: **ADAPT / BLOCKER BEFORE INTEGRATION**. Add `removeProfile(profileId)` to the storage contract and both platform actuals; invoke it in anonymous and authenticated deletion paths; test index reuse.

#### HTTP platform contract audit — Main-dev
- Common contract `AddonPlatform.kt` (`27507ac7723d9a6209917e86d1fe0fba738685f3`) exposes `followRedirects`, `maxResponseBodyBytes` and optional `bodyBytes`.
- Android actual (`44272e80eca597427cc9d91594a904d51665b349`) streams raw response data through `readAtMostBytes` and applies redirect flags, but builds request payloads from the text `body` and ignores `bodyBytes`.
- iOS actual (`cd61266920ed871e29b5629564750d6134c7c0a0`) materializes the full raw response as `ByteArray` before truncating it, ignores `followRedirects`, and ignores `bodyBytes`. Its `httpGetBytesWithHeaders` path also materializes the full response before checking the configured size limit.
- Verified caller: Main-dev `TraktLibraryRepository.kt` (`1bdd28d4b7fa1f1ba954f5473cfc4098af89b82a`) uses string request bodies and `followRedirects = true`; no `bodyBytes` use was found in that file. GitHub code search returned no usable global call-site results, so the absence of other callers is **NOT VERIFIED**.
- Upstream Android's raw request implementation supports `bodyBytes ?: body`; upstream iOS has the same full-buffering and ignored-flag limitations as Mew iOS. Do not blindly copy one platform actual into the other.
- Decision: **ADAPT / NEEDS CALLER AUDIT**. Fix iOS streaming bounds and redirect parity; decide whether the binary request-body parameter is still required, then implement it consistently on both platforms. Add platform tests for oversized bodies, redirect enabled/disabled, cancellation and byte-body requests.

#### Custom poster URL request boundary
- The settings page saves the supplied pattern after trimming; no URL validation is performed there. The resolver substitutes IDs and returns the resulting URL without enforcing an HTTP(S) scheme or allowed host.
- The Coil image-loader registers `CustomPosterFallbackInterceptor`; poster requests carry the original-art fallback through `memoryCacheKeyExtras`. This fallback handles load errors but is not URL validation.
- Decision: **ADAPT / SECURITY REVIEW** before integration. Validate the final resolved URL at the image-request boundary, define HTTP/HTTPS and local-network policy, and test redirects/fallback behavior. The pattern is user-configured; this audit does not establish an exploitable remote attack.

#### Media-server discovery security boundary
- `ServerDiscoveryPolicy` rejects an HTTPS discovery request that finishes on HTTP, but `normalizeBackendUrl()` independently accepts both HTTP and HTTPS for the `backend_url` supplied inside the discovery document.
- Therefore an HTTPS discovery response can still configure an HTTP backend endpoint. Authentication and subsequent API traffic may then use cleartext unless the user is explicitly warned/required to opt in.
- `MediaServerSync` serializes server tokens in the `token` field and sends them through Supabase RPCs `sync_push_media_servers` / `sync_pull_media_servers`. No corresponding SQL/RPC definitions or RLS policy are present in the inspected repository tree, so cloud-side token authorization, encryption and retention remain **UNVERIFIED**.
- Decision: **NEEDS INVESTIGATION / SECURITY BLOCKER**. Inspect backend RPC/RLS separately; define explicit HTTP opt-in while preserving legitimate LAN-server compatibility; trace token-bearing playback URLs through logs, persistence and external-player handoff.

#### Verification and remaining work
- GitHub remote source/tree inspection only. A local clone attempt failed because the environment could not resolve `github.com`; no local working tree was created.
- No Gradle build, unit/instrumentation tests, CI run, emulator or device verification was performed. Source tests cited in this document are **NOT RUN**.
- No application source, `Main-dev`, or `main` was changed. This update changes only this audit Markdown on `upstream-sync`.
- Next: complete the remaining security/caller trace for server discovery, custom-poster image requests and token-bearing playback URLs; then consolidate a small `Main-dev` integration matrix. Do not port application code until explicitly approved.


### Server boundary follow-up — trust UI and token handling (2026-11-17)

#### Custom backend HTTP warning and UI reachability
- `ServerConfiguration.isSecure` is derived from whether `backendUrl` starts with HTTPS. `ServerDiscoveryPolicy.normalizeBackendUrl()` accepts both HTTP and HTTPS, so a valid HTTPS discovery document can still advertise an HTTP backend endpoint.
- Upstream contains a `ServerTrustDialog` that explicitly warns:
  - private-network HTTP is unencrypted and may be readable by other devices on the network;
  - public-host HTTP may expose credentials and synced data in transit;
  - the server operator can receive account credentials and synced data.
  The dialog requires an explicit “I trust this server” confirmation.
- Important limitation: a call-site for `ServerConnectionSheet`, `ServerTrustDialog` or `ServerConnectionController` was not found in the inspected `App.kt`, `SettingsScreen.kt`, `SettingsRootPage.kt` or `AccountSettingsPage.kt`. GitHub code search returned no usable matches. The warning component exists, but whether the custom-backend flow is reachable from the shipped UI is **NOT VERIFIED**.
- Build policy: custom server connections are enabled in upstream `androidFull` and `iosFull`, but disabled in `androidPlaystore` and `iosAppStore`.
- Decision remains **NEEDS INVESTIGATION / POSSIBLE ORPHAN**. Do not rely on the warning as an effective mitigation until its caller and shipped build variant are verified. If the flow is integrated, preserve the explicit trust review and make sure the displayed URL is the actual backend URL.

#### External media-server token exposure — mitigations observed
- Emby/Jellyfin API requests send the token in the MediaBrowser authorization header. API clients disable automatic redirects; the public-info redirect retry is made without a token.
- Direct-play and external-subtitle delivery URLs append the provider-required `api_key` query parameter. The token-bearing URL is therefore a real data flow, not just a theoretical concern.
- Observed mitigations:
  - `InAppLogger.redactUrl()` removes query strings, fragments and URL user-info before logging.
  - player runtime logging uses that redactor for stream/playback URLs;
  - server sources are excluded from `lastSourceUrl`, avoiding persistence in the normal last-source field;
  - Sentry's Android HTTP breadcrumb interceptor strips query and fragment;
  - `ServerPlayback` logs labels, event types and positions rather than the playback URL;
  - `MediaBrowserClient` maps transport failures to generic server errors rather than exposing the request URL.
- Still unverified: external-player handoff for server streams, third-party crash/HTTP instrumentation outside the inspected paths, and whether all future diagnostics preserve query redaction.
- Decision: **ADAPT WITH SECURITY GUARDS** if ported. Keep tokens out of logs, diagnostics, persisted history and external-player handoff unless that handoff is explicitly designed and disclosed. Add tests for URL redaction and server-source persistence exclusion.

#### Updated security disposition
- HTTP custom-backend support is not silently accepted by the discovery policy, but the backend URL can be HTTP; a trust-warning component exists, while its call-site remains unverified.
- Media-server playback query tokens are required by the provider protocol; current inspected app logging/persistence paths have redaction or exclusion safeguards, but external handoff remains open.
- No backend SQL/RPC/RLS definitions were found in this repository, so cloud-synced server-token authorization, encryption and retention remain **UNVERIFIED**.
- No application source changes or runtime tests were made during this follow-up.

## Full upstream backlog — first functional comparison wave

Scope: upstream stable tag `1.0.0` → `enhanced` HEAD (`b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`): **72 commits / 160 changed paths**. This is a broad post-stable release range, not 2,000 individual feature ports. The range includes 91 common Kotlin paths, 12 Android main paths, 10 iOS main paths, 17 resource paths, 17 test paths and release/CI/docs metadata. Every category remains in scope.

### Batch A — feature parity candidates

| Upstream feature/change | Main-dev evidence | Classification / decision |
|---|---|---|
| TorBox alternative-hash cache matching (`1118dfbf87de7d9e68860446ea553d48d06adcba`) | Mew `LocalDebridService.kt` (83 lines) returns TorBox results keyed by returned hash and has no alias-resolution fallback. Upstream adds normalized request hashes, canonical/alias resolution and selective single-hash retries; it adds focused tests. | **MISSING BEHAVIOR — ADAPT, HIGH PRIORITY.** Port the alias resolution into Mew's existing service and tests. Preserve cancellation propagation, avoid redundant requests, and verify batch/single hash semantics. |
| Season batch downloads + storage management (`48cbb901bf48a641a1270c2d10bbd4260e712e68`) | Mew `DownloadsRepository.kt` supports individual enqueue, queue modes and pending source searches. It has no matching upstream-style season selection/source-planning coordinator or batch dialog. `DownloadsBatchFlowTest.kt` tests quality keys and pending-source grouping; it is not a season batch downloader. | **FEATURE GAP — ADAPT, LARGE FEATURE.** Build on Mew's existing enqueue/queue/service lifecycle; do not import upstream scheduler or replace Mew foreground downloads. Preserve profile cancellation, source lookup limits, completed-download detection and per-episode metadata. |
| Library remembered filters: genre/year/watched + selected section/type (`2da28d707c77b0bb8d3030af8f360e67981a0994`) | Mew `LibraryDisplaySettings.kt` persists layout and sort settings. `LibrarySavedContent.kt` currently offers list/type/sort controls; `LibraryScreen.kt` already has Mew-specific downloads and release-calendar surfaces, but no matching saved-library genre/year/watched filter state or projection. | **FEATURE GAP — ADAPT.** Add filters to Mew's existing saved-library UI and persistence, not by replacing its screen. Define behavior across local, Trakt/Simkl and profile changes; add projection/persistence tests. |
| Stremio player/library tracking addons, consent and event queue (`964ecfd8...`, related event commits) | Mew `AddonRepository.kt` handles installed addon manifests and `stremio://` URL normalization, but the inspected tree has no upstream-equivalent `AddonEvents` dispatcher or `TrackingAddonConsentDialog`. Mew's CloudStream compatibility and existing tracking providers are separate systems. | **NEEDS INVESTIGATION → ADAPT candidate.** Treat as a distinct addon capability, not a duplicate of CloudStream extensions. Review manifest resource support, event URL construction, install flow, consent persistence, event ordering/retries, profile scope and privacy before implementation. |
| Playback error → next source fallback (`ff7500fd...`) | Mew `PlayerNextEpisodeAutoPlay.kt` selects a source for the *next episode*. Mew `PlayerScreenRuntimeEffects.kt` has `tryRefreshCredentialedSourceAfterError`, which refreshes the current credentialed source; inspected code does not show upstream's general failed-source cycling across installed addons. | **SHARED PATTERN / DIFFERENT SEMANTICS — ADAPT candidate.** Do not confuse next-episode selection or same-source credential refresh with current-playback source fallback. Audit error routing, stream loading completion, source identity, engine-switch behavior, retry caps and Mew player UI before porting. |
| Android gesture-navigation timeline inset (`4128f63e...`) | Mew `PlayerLayout.kt` lacks upstream `playerTimelineBottomInsets`; Mew controls apply slider offset without the equivalent bottom safe-drawing inset. | **ADAPT candidate.** Already documented in semantic audit batch 7; test edge-to-edge, fullscreen, three-button navigation, landscape and TV before integration. |
| Episode artwork fallback after image failure (`b4e44645...`) | Mew detail cards fall back when thumbnail is absent, but inspected code has no image-load error fallback state. | **ADAPT candidate.** Both card layouts; preserve Mew card design, rating and download affordances. |
| Empty known-key catalog sync guard (`0b9d9359...`) | Mew repository lacks the upstream empty-key preservation guard. | **TAKE candidate** with sync race regression tests. |

### Batch B — intentional differences and already-present work

- **TorBox cloud-library DTO mapping:** Mew and upstream `TorboxCloudLibraryProviderApi.kt` have the exact same blob SHA `463832f2e61e0dd59f7cfccda56a483113563bd0`; its tests also match exactly. This is already implemented. The alternative-hash fix is in the separate `LocalDebridService.kt` and is not covered by that equality.
- **Library screen:** Mew's library is a deliberate parallel implementation with its own downloads view, cloud-library surface and release calendar. Upstream genre/year/watched filters are a behavior gap, not a reason to replace Mew's 2,584-line screen with upstream's 3,612-line screen.
- **Downloads:** Mew's foreground service, resumable transfer, queue and pending-source worker remain an intentional parallel architecture. Upstream season batch planning can be adapted above Mew's queue; the upstream WorkManager/JobScheduler transfer backend must not replace Mew's.
- **Player:** Mew's next-episode autoplay, credential refresh, external-player handoff and player presentation have different contracts. Compare behavior at the call sites; do not classify similarly named fallback paths as equivalent.
- **Watch progress identity:** previously verified exact blob equality with upstream; no port required.
- **iOS-only changes:** iOS subtitle reset, document picker and backup exclusion are not Android ports. Keep them in the cross-platform audit ledger and assess separately if Mew's iOS target is still in scope.
- **Localization:** locale additions and translation corrections are a separate batch. Check resource key coverage, generated resources, RTL/BiDi rendering, locale configuration and Mew-specific strings before copying locale files.

### Next functional batches

1. Finish Batch A call-site and dependency audit, including source-fallback error entry points, addon install flow and library profile persistence.
2. Audit all remaining changed paths in the 72-commit release range, especially subtitle cue rendering, downloads storage/network policy, CI/release metadata and iOS-specific behavior.
3. Expand beyond the 72-commit range: compare the older stable-beta/release history and current feature branches by feature area. The 72-commit range is the first manageable wave, not the full upstream history.
4. Only after the semantic inventory, prepare small approved integration groups for `Main-dev`. No code has been ported in this audit.

Tests/build/device verification for this comparison wave: **NOT RUN**. Findings are based on remote source/tree inspection only.


## Upstream snapshot refresh — exact tree parity checkpoint

- On 2026-11-17, refreshed Mew `upstream-sync` from `d00db9bd774d2d395ea2d2c9f09548e2c47d08d1` to `2017c854d6e3eeceb49ab8841548f6143034935f`.
- Source target: upstream `enhanced` commit `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Updated five source paths in one commit: `.github/ISSUE_TEMPLATE/bug_report.yml`, `DetailSeriesContent.kt`, `HomeCatalogSettingsRepository.kt`, iOS `PlayerEngine.ios.kt`, and iOS `SettingsConfigFileLauncher.ios.kt`.
- Guard: verified `upstream-sync` expected head before the ref update; non-force ref update succeeded.
- Read-back tree verification: 2,339 upstream blobs present, zero missing, zero differing; only `docs/mew-context/README.md` and `docs/upstream-sync/README.md` are Mew-owned extras. This establishes source-tree parity with `enhanced` HEAD, not commit-history identity.
- `Main-dev` and `main` were not changed. This was an upstream snapshot refresh only, not feature integration into Mew's app branch.
- Tests/build/CI/device verification: **NOT RUN**. The five files were copied as the exact upstream snapshot; Mew runtime compatibility remains to be assessed in the integration audit.

## Full upstream history scale

- Common ancestor: `f9ad843b14cd3be7fa3ddb800b6961233e0e5b56`.
- Upstream `enhanced` is 2,193 commits ahead of that ancestor; Mew `Main-dev` is 596 commits ahead of the same ancestor. This is the true large-scale alignment backlog.
- The large GitHub compare file listing is capped at 300 entries; it is not a complete changed-file audit. Continue through release checkpoints, full tree inventories and semantic batches, not one giant merge or one cherry-pick per commit.



## Full-tree alignment inventory — common ancestor to current heads

Reference point: common ancestor `f9ad843b14cd3be7fa3ddb800b6961233e0e5b56`, tree `ebba9d7f3c7f2d7dda239f5fb10224a34a49c5fc`. Upstream `enhanced` tree `519923a50e2b8c3f226bcafa4e46c6160b8095d3`; Mew `Main-dev` tree `db06d0a44367f7881460317f0d7a7233ed6bc115`.

- Base: 1,097 blobs.
- Upstream enhanced: 2,339 blobs; since base: 1,255 added, 13 removed, 583 modified.
- Mew Main-dev: 1,668 blobs; since base: 573 added, 2 removed, 396 modified.
- Direct tree comparison (current Mew Main-dev vs enhanced): **1,133 upstream-only paths**, **462 Mew-only paths**, **500 shared identical paths**, **706 shared paths with different content**.
- Largest upstream-only groups: commonMain 617, commonTest 120, androidMain 112, androidHostTest 94, iosMain 53, Android app/native and assets after that.
- Largest shared-but-different groups: commonMain 399, androidMain 132, commonTest 58, iosMain 36, then full/platform-specific source sets.
- Largest Mew-only groups: androidMain 159, commonMain 110, commonTest 69, iosMain 23, Android full source 17, Mew branding/assets and CloudStream support.
- These are **path/blob counts, not feature counts**. Some paths are generated resources, vendored native code, binary assets, tests, release metadata or platform-only files. Every candidate still needs semantic review and duplicate classification.

## Release checkpoints

- GitHub returned 86 published release entries for the upstream repository (current page, 2026-11-17), from the 0.1.x/0.2.x/0.3.x/0.4.x/0.5.x/0.6.x lines through stable `1.0.0` and beta `1.1.0-beta.1`.
- Use release tags as audit checkpoints, but include commits between tags and branch-only work. A tag's `prerelease` flag and release notes must be checked; historical beta-named releases may not have the GitHub prerelease flag set.
- The first 72-commit post-`1.0.0` range is only the most recent manageable wave. It does not cover the full 2,193-commit ancestry delta.

## 2026-11-17 — episode artwork fallback integration

**Upstream source:** `luqmanfadlli/NuvioMobile-Enhanced` `enhanced`, commit `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.

**Mew comparison:** `DetailSeriesContent.kt` on `Main-dev` had thumbnail-null fallback (`video.thumbnail ?: fallbackImage`) in both horizontal and list episode cards, but no Coil image-load error callback. The shared fallback is passed from `meta.background ?: meta.poster`. The Mew file is not a safe wholesale replacement: it contains fork-specific TV layout sizing, episode progress, rating, download and click/zoom behavior.

**Duplicate audit:** no same-behavior helper in the inspected detail component. The two card layouts are intentional parallel presentations (not duplicate UI); both need the same fallback behavior. No dependency added; Mew and upstream version catalogs both specify Coil 3.6.3.

**Decision:** `ADAPT`. Added a keyed `rememberEpisodeImageState` helper and `AsyncImage.onError` callbacks to both Mew card variants. Primary thumbnail failure switches to series artwork; Mew presentation and interactions are preserved.

**Changed file:** `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/components/DetailSeriesContent.kt`.

**Commits:** implementation `edcda9a8e081143ae87aab8bbc6c48cb4ca8c4c8`; callback indentation correction `813510f128e58aefa0b5daa4757f18502c733fa6`.

**Verification:** GitHub diff and new branch head read back. Gradle build/tests, CI and device testing: **NOT RUN**. Runtime image-failure fallback: **NOT VERIFIED**. Repository-wide duplicate search was incomplete, so do not claim a complete global duplicate audit.

**Branch safety:** `Main-dev` now points to `813510f128e58aefa0b5daa4757f18502c733fa6`; `main` remains `207ef8fa309b8061b73039b2d4e4bde4af65050e`; `upstream-sync` remains the upstream source snapshot plus Mew documentation.

## 2026-11-17 — early catalog sync preference preservation

**Upstream source:** commit `0b9d93599f7d7603e7814a99a70aa64d7c48e086`, changing `HomeCatalogSettingsRepository.applyFromRemote` so an empty known-key set does not cause local catalog choices to be discarded.

**Mew comparison:** same repository and method on `Main-dev`. Mew additionally syncs hero auto-scroll and underline settings, preserves hero-source state, and enforces pinned collections after normalization. These behaviors remain intact. Mew's definitions and collection definitions start empty and are populated later by `syncCatalogs` / `syncCollections`, so an early remote sync can encounter an empty `knownPreferenceKeys()`.

**Decision:** `ADAPT`. Extracted the key-preservation predicate as an internal test seam and added the upstream empty-known-key condition. Remote keys still replace matching local keys. Once known keys are available, existing known-key and explicit-sync-key behavior remains; unrelated legacy keys are still filtered.

**Changed files:** `composeApp/src/commonMain/kotlin/com/nuvio/app/features/home/HomeCatalogSettingsRepository.kt`; `composeApp/src/commonTest/kotlin/com/nuvio/app/features/home/HomeCatalogRemotePreferenceMergeTest.kt`.

**Regression coverage added:** empty known keys preserve local choices; remote payload keys take precedence; known keys and explicit sync keys survive while unrelated legacy keys do not. Tests, Gradle build, CI and device checks: **NOT RUN**. The added tests validate the predicate, not the full repository lifecycle.

**Commit:** `db07c08cea6073ca8119df14eb1e41fcc58572f1`. `Main-dev` is now at this SHA; `main` remains unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

## 2026-11-17 — TorBox alternative-hash cache matching

**Upstream source:** `luqmanfadlli/NuvioMobile-Enhanced` commit `1118dfbf87de7d9e68860446ea553d48d06adcba`. It adds alias resolution in `LocalDebridService.kt` and tests exact, single-alias and batch behavior.

**Mew comparison:** Mew's `LocalDebridService.kt` used the same TorBox `checkcached?format=object` endpoint and lowercased returned map keys, but did not normalize the request list in the service or map canonical alias keys back to requested hashes. `LocalDebridAvailabilityService.annotateCachedAvailability` looks up each stream by its requested lowercased hash, so alias responses could be misclassified as `NOT_CACHED`. The service's `isCached` path has the same key-membership dependency.

**API/transport review:** Mew's existing TorBox client already POSTs the batch to `/v1/api/torrents/checkcached?format=object` with Bearer auth and normalizes request hashes. The adaptation reuses that client for single-hash retries; no credential, URL, transport, redirect or TLS behavior changed. TorBox API reference: https://api-docs.torbox.app/

**Decision:** `ADAPT`. Normalize/deduplicate requested hashes in `checkTorboxCached`, separate request decoding into `requestTorboxCached`, map single alias answers to the requested key, and retry only unresolved hashes after a batch contains a canonical key not present in the requested list. Existing Premiumize behavior is unchanged.

**Mew-specific failure safety:** Upstream's helper treats a failed per-hash retry like an absent cache entry. Mew's caller maps absent entries to `NOT_CACHED`, so this could create a false negative during a transient retry failure. Mew's resolver returns null if any retry fails; `LocalDebridAvailabilityService` then retains `UNKNOWN` status. `CancellationException` is rethrown.

**Duplicate audit:** no equivalent alias resolver or service-level regression tests existed in Mew's inspected debrid source/test paths. `LocalDebridAvailabilityService` is the shared caller for batch and single-hash availability. No duplicate implementation was added.

**Changed files:** `composeApp/src/commonMain/kotlin/com/nuvio/app/features/debrid/LocalDebridService.kt`; `composeApp/src/commonTest/kotlin/com/nuvio/app/features/debrid/LocalDebridServiceTest.kt`.

**Tests added:** exact matches do not retry; single alias maps to requested hash; only missing batch hashes are retried; failed retry returns null so caller can retain UNKNOWN. Gradle tests/build, CI and device checks: **NOT RUN**.

**Remaining risk:** fallback retries are concurrent and not capped. TorBox documents POST batch checks as supporting large batches, but this does not establish a per-account rate limit for concurrent single-hash retries. Reassess if real batch sizes or provider throttling show a problem.

**Commit:** `dd4b6ce5d0ff26527e7b3ad482ec77a3235d6bde`. `Main-dev` is at this SHA; `main` remains unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

## 2026-11-17 — subtitle outline-width normalization

**Upstream source:** `b8bbd700fda41ad55cdb90de2d60e48d10f5d0db` adds subtitle outline-width control and repository-boundary clamping to upstream's 1–5 range.

**Mew comparison:** Mew already presents outline width in a 1–5 stepper, but `PlayerSettingsRepository.loadFromDisk()` restored persisted width without normalization and `setSubtitleStyle()` accepted/persisted out-of-range values. This could occur through corrupted or imported settings even though normal UI increments/decrements are bounded.

**Decision:** take the validation behavior and adapt it to Mew's existing 1–5 contract. Added shared min/max constants and `normalizeSubtitleOutlineWidth`; apply it to storage load, incoming style updates and the existing stepper. Default remains 1. No subtitle renderer, playback engine or visual design changed.

**Changed files:** `composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/SubtitleAudioModels.kt`, `PlayerSettingsRepository.kt`, `SubtitleStylePanel.kt`; `composeApp/src/commonTest/kotlin/com/nuvio/app/features/player/SubtitleStyleStateTest.kt`.

**Regression coverage added:** `Int.MIN_VALUE` clamps to 1, `Int.MAX_VALUE` clamps to 5, and an in-range value is preserved. Tests, Gradle build, CI and device checks: **NOT RUN**.

**Commit:** `2d5fc48d285b4ae42a2d341cb34cd28897685f6a`. `Main-dev` is at this SHA; `main` remains unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

## 2026-11-17 — additional upstream feature-group decisions

### Source fallback and shared search sessions

Upstream commits `8fba6c71e95e4a6d94f0ccf1998e93ef50ce1aa6` and `ff7500fdd601e445982d6efb9177ec8cd14f7976` implement the base source-fallback system and next-episode continuation. Mew has no equivalent `PlayerSourceFallbackRun`, runtime fallback, or shared `StreamSearchSessions`. Mew's next-episode auto-play currently selects a stream and passes it to the player; a failed/stale debrid resolution refreshes the episode sources instead of trying another matching source.

Mew's `PlayerStreamsRepository` and `StreamsRepository` share a provider-fetch pattern, but their state/lifecycle differ (player source/episode flows versus stream-screen request tokens, auto-play overlay, manual-selection reuse and binge-group behavior). Classification: **INTENTIONAL PARALLEL / SHARED PATTERN, DIFFERENT SEMANTICS**. Do not merge them as a superficial dedupe. Upstream's shared search session layer is a separate integration opportunity.

Decision: **NEEDS INVESTIGATION as a grouped player feature**. The port needs settings/storage/UI, shared search-session lifecycle, source identity, retry limits/timeouts, engine-switch exclusivity, and correct handling of direct debrid, P2P, downloads, manual source selection, cancellation and next-episode transitions. No code changed. Device verification is required.

### MDBList runtime bugfix

Upstream `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb` prevents a show runtime from being used as an episode duration in `MdbListProgressProjection`. Mew's tree contains metadata enrichment/settings but no MDBList watch-progress projection or sync engine. Decision: **SKIP this isolated fix**; investigate the larger missing sync feature separately.

### Native navigation bar anchor for detail hero

Upstream `f28929d3e3cc0d354de1dd921efc539bbf8a14cf` aligns the detail hero mute button with the native navigation-bar button using platform-specific UIKit frame/margin reads. Mew's hero uses Compose padding and Mew's iOS insets expose only the physical top inset; no matching native bar anchor exists. Decision: **SKIP** for the current Mew navigation model. No code changed.

### Range checkpoint

The `1.0.0...enhanced` compare contains 72 commits. Four changes are integrated into `Main-dev`; the rest are not yet fully audited. `Main-dev` is `2d5fc48d285b4ae42a2d341cb34cd28897685f6a`; `main` remains `207ef8fa309b8061b73039b2d4e4bde4af65050e`. Tests/build/CI/device verification: **NOT RUN**.

## 2026-11-17 — Stremio tracking-addon feature audit

Upstream commits:
- `964ecfd8d98598cd2d315cedb584fe7ef59a76fd`: Stremio player/library resource events and tracking-addon installation consent.
- `d89811ac6c75a3f80c0ab424ab01a37639401d54`: prevents derived series-watched reconciliation from emitting a user-intent event.
- `8f5d9bcac3c2c0535b17b26cbdff392c10b8d767`: per-addon event queues, ordering/isolation, and no-addon fast paths.

Mew's `AddonManifest` already carries manifest resource declarations. Mew's library/watched repositories integrate existing tracking providers, but the inspected addon installation, library and watched paths have no Stremio addon event dispatcher or tracking-addon consent flow. This is a new capability, not an equivalent implementation.

Classification: **MISSING FEATURE / NEEDS INVESTIGATION**. It shares tracking data sources with existing Trakt/Simkl functionality, but has a different destination, protocol and consent boundary. It sends viewing activity, playback positions and library changes to third-party addon developers/services.

No code changed. Do not implement without explicit approval for the feature scope. Any port must preserve mandatory install consent, per-addon ordered delivery, event chunking, and strict suppression of events caused by cloud sync, playback completion, derived series reconciliation or profile switching. Required tests include consent, URL encoding, queue ordering/isolation, 100-ID chunking, and lifecycle correctness for start/pause/stop/seek.

## 2026-11-17 — batch downloads and storage-management audit

Upstream commit `48cbb901bf48a641a1270c2d10bbd4260e712e68` adds season batch downloads, source prefill, queue policies, storage statistics, metered-network guards, offline metadata and platform cleanup/transfer changes.

Mew already has `EpisodeDownloadFlowSheet`, `EpisodeDownloadCoordinator`, automatic per-episode source and quality selection, bounded provider/episode concurrency, pending source searches, `AllAtOnce` / `OneAtATime` queue modes, grouped downloaded-show episodes, offline detail snapshots and external-folder selection. Mew's batch flow has its own tests and a different pending-source lifecycle.

Classification: **ALREADY IMPLEMENTED / INTENTIONAL PARALLEL** for the core batch-download feature. Do not copy the upstream batch coordinator or replace Mew's repository.

Potential gaps: the inspected Mew download tree has no dedicated `DownloadNetworkGuard` or `DownloadsStoragePlatform` / device-storage-statistics abstraction. Mew's storage contract currently persists the download payload, external-folder URI and episode-download settings. The upstream batch also changes network policy, platform transfer scheduling, resume behavior, and multi-profile cleanup.

Decision: **SKIP duplicate batch behavior; NEEDS INVESTIGATION for storage/network lifecycle gaps**. Compare actual Android/iOS scheduling and cleanup semantics before any platform integration. No code changed.

### Downloads batch/storage audit — detailed result

For upstream commit `48cbb901bf48a641a1270c2d10bbd4260e712e68`, Mew already has its own batch download flow, auto source selection, provider/quality matching, pending-source retry, grouped show downloads and offline detail reconstruction. Its data model stores a `DownloadDetailsSnapshot` per download instead of upstream's separate offline metadata store. These are equivalent user-facing capabilities with different persistence/lifecycle semantics; do not duplicate or wholesale replace them.

Actual gaps found:
- Mew supports only `AllAtOnce` / `OneAtATime`; upstream adds configurable global/per-show/concurrent-show admission limits.
- Mew has no download-specific Wi-Fi/metered-network guard or mobile-data setting.
- Mew has no device storage statistics/bar; external-folder destinations require careful capacity semantics.
- Android account wipe omits the `nuvio_downloads` preferences file. iOS account wipe omits the downloads profile-scoped keys. Mew downloads are profile-scoped, so ordinary profile switching must not erase another profile's data; account wipe cleanup must be handled separately and deliberately.
- Mew's Android OkHttp and iOS NSURLSession data-task/range-resume downloaders are materially different from upstream's WorkManager/JobScheduler and background NSURLSession transfer architecture. Do not transplant these implementations without a dedicated lifecycle/resume/security audit.

Decisions: **ALREADY IMPLEMENTED / INTENTIONAL PARALLEL** for core batch/offline metadata/grouped download behavior; **ADAPT** for queue limits, network policy and storage stats; **NEEDS INVESTIGATION** for account-wipe cleanup and platform background scheduling. No app code changed. Build/tests/device verification: **NOT RUN**.

### Library filter feature audit — 2026-11-17

Upstream commit `2da28d707c77b0bb8d3030af8f360e67981a0994` adds persistent selected list/type and genre, year, watched/unwatched filters with faceted counts.

Mew's vertical library projection already handles section/type selection, sorting and deduplication; library items expose genre/release data and the screen already observes watched state. However, the current projection has no genre/year/watched filters, and selected list/type live in `rememberSaveable` screen state rather than `LibraryDisplaySettingsUiState` persisted per profile. Mew's downloaded-content filters are separate and should remain so.

Decision: **ADAPT** the filter projection and per-profile persistence into Mew's existing library architecture. Preserve provider ordering, local vs remote tracking source behavior, Mew's watched-series semantics, downloaded-content filters and UI. Do not port upstream screen files wholesale. No code changed. Tests/build/device verification: **NOT RUN**.

### iOS download backup exclusion — 2026-11-17

Upstream commit `718b526a67654fa94a18da39aabdd7d4aa21af44` excludes the default downloads directory from iCloud and computer backups. Mew's default iOS path is also `Documents/nuvio_downloads` and previously lacked the exclusion attribute.

**Adapted on Main-dev:** commit `a4b8ff258439f91140da2f92c60bec868fc6cec0` adds `NSURLIsExcludedFromBackupKey` on the default downloads directory, once created. Existing files in that directory inherit the folder setting; user-selected external folders are unchanged.

GitHub diff verified. iOS compilation and device verification: **NOT RUN**. Main remains unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

### iOS pushed-page blur and Android player inset audit

Upstream `c5e74dde1b7a7085d6e8684d5780f83faae68b38` modifies its UIKit pushed-page navigation-bar appearance and iOS 26 scroll-edge blur. Mew's iOS root controller has a different tab/navigation lifecycle and no matching upstream navigation-bar/scroll-edge integration in the inspected file. Decision: **SKIP unless reproduced on Mew's navigation stack**.

Upstream `d374bdf022f63ead318387c6f9ffebcdb8113497` adjusts the bottom inset helper `playerTimelineBottomInsets`; Mew does not have that helper or equivalent calculation in `PlayerLayout.kt`. The existing `safeContent` call is for horizontal safe padding. Classification: **FALSE POSITIVE / NOT APPLICABLE**. No player layout change.

## 2026-11-17 — CMP follow-up decisions

- TorBox alternative-hash handling is already present in Mew `LocalDebridService`; its tests cover exact matches, single alias mapping, selective retries, and preserving unknown availability when retry fails. Decision: **ALREADY IMPLEMENTED**.
- Mixed RTL/LTR Search and Watch Party handling is not present in Mew's shared input/chat text components. Decision: **ADAPT** optional input text styling and `TextDirection.Content` with mixed Arabic/Latin regression tests.
- The home-catalog remote-sync race safeguard is already present in Mew's `shouldPreservePreferenceAfterRemoteSync`. Decision: **ALREADY IMPLEMENTED**.

Current refs: upstream `enhanced` = `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`; Mew `Main-dev` = `a4b8ff258439f91140da2f92c60bec868fc6cec0`; Mew `main` = `207ef8fa309b8061b73039b2d4e4bde4af65050e`. No app code changed in this audit. Tests/build/CI/device verification: **NOT RUN**.

## 2026-11-17 — localization parity audit

Mew currently has no Bengali, Croatian, Urdu, Ukrainian or Simplified Chinese resource directories, and those languages are absent from its `AppLanguage` enum. The five language display-name keys already exist in Mew's default resource file. Mew already has Arabic and Russian translations and enum entries, but Android `locale_config.xml` omits `ar` and `ru`.

Resource key counts: Mew default English 2,933; Hebrew 782; pt-BR 1,560. Upstream locale files map to 2,121 existing Mew English keys for Hebrew/Chinese/Ukrainian/Bengali/Croatian, 2,115 for Urdu and 2,169 for pt-BR. The Mew-only keys need to be retained and fall back to English; do not replace Mew's locale XML files wholesale.

Decision: **ADAPT** the five locale packages by merging only matching resource keys, adding language enum entries and Android locale registrations, and preserving Mew-specific strings. Also register existing Arabic/Russian resources in Android locale config. Verify RTL behavior separately. Resource generation/build/tests: **NOT RUN**.

### Mixed RTL/LTR text — 2026-11-17

Adapted upstream commit `5964cdca71c0cc6c4892af88f76083b165c6e55c` into Mew's shared input/search/chat UI.

Changed `NuvioInputField` to accept an optional `TextStyle` while retaining its existing typography as the default. Search input/recent searches and Watch Party sender names, message bubbles and composer now use `TextDirection.Content`. Existing callers that omit the new argument retain prior behavior.

Commit: `69b4a96072b506f60dcbe538c5f6b71db3a4a464`. GitHub diff reviewed; no status checks were reported. Tests/build/device checks: **NOT RUN**; mixed-script IME editing needs UI verification. `main` remains unchanged.

## 2026-11-17 — release workflow safety audit

Upstream stable/beta release changes were compared with Mew's Android-first `android-release.yml`. Mew has a distinct release process (Mew signing secrets, optional unsigned iOS packaging, `Version.xcconfig`, changelog, custom release notes), so upstream's mobile release workflow and store-source automation are not direct ports.

Two issues need a separately approved release-workflow task:
1. Mew's push trigger still targets `enhanced`, but the live branch list is only `Main-dev`, `main`, `upstream-sync`.
2. Manual draft/publish validation checks branch ref type but does not enforce an approved branch allowlist.

Do not change workflow triggers/guards or publish until release branch policy is explicitly settled. Current default branch: `main`; `Main-dev`: `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`. No release created; workflow/build/CI tests: **NOT RUN**.

## 2026-11-17 — Library filter and RTL/LTR audit

Upstream Library commit `2da28d707c77b0bb8d3030af8f360e67981a0994`: Mew already has vertical/horizontal library layouts, sorting, list/type controls, genre/release metadata and watched indicators. Missing are vertical Genre/Year/Watched filters, facet counts and persistent selected list/type. Mew's Android/iOS storage is already profile-scoped; extend that schema rather than introducing a second store. Its LibraryScreen has Cloud Library, Downloads, release calendar/radar and other Mew-specific flows, so no wholesale upstream screen replacement. Decision: **TAKE / ADAPT** as a bounded feature group; no app code changed.

Upstream mixed RTL/LTR commit `5964cdca71c0` is already substantially implemented by Mew commit `69b4a96072b506f60dcbe538c5f6b71db3a4a464`. Mew applies content-direction text styling to Search and Watch Party; upstream adds focused host regression tests. Decision: **ALREADY IMPLEMENTED** runtime behavior; test coverage remains an adaptation opportunity.

Branches: `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; `main` unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`. Tests/build/CI/device checks: **NOT RUN**.

## 2026-11-17 — iOS UI / subtitle / backup picker follow-up

- Upstream `c314dd48d622` clears the floating tab-bar blur. Mew has a custom native tab bar with copied/tinted appearance; no device reproduction of the same bottom blur. **NEEDS INVESTIGATION**, not a blind port.
- Upstream `c5298633914d` retains iOS subtitle state across source changes. Mew's iOS engine file does not have the corresponding subtitle text/style state pair and uses a different bridge path. **NEEDS INVESTIGATION** in Mew's actual styled subtitle overlay.
- Upstream `d703ca48e5ad` changes document-picker export. Mew exports backups using a temporary file + `UIActivityViewController` share sheet, so this is **FALSE POSITIVE / DIFFERENT UX**.

No app code changed. iOS device checks and tests: **NOT RUN**.
