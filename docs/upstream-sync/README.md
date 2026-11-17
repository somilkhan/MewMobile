# Mew ↔ Nuvio Enhanced upstream alignment

Last verified: 2026-11-17 (GitHub API; no local checkout)

## Mission

Keep Mew continuously aligned with `luqmanfadlli/NuvioMobile-Enhanced` across features, UI/UX, bug fixes, playback, providers, downloads, settings, performance, tests, CI, release metadata and supported platforms. Do not reduce upstream work to a handful of cherry-picked fixes.

This is a semantic port, not a blind merge. Preserve Mew branding, CloudStream compatibility, Mew player design, custom settings/profile behavior, and platform-specific implementations.

## Branch policy

- `upstream-sync`: upstream reference/snapshot and alignment ledger. Track the selected upstream source revision and record all intentional Mew overlays. Never use it as a blind merge target.
- `Main-dev`: Mew integration target after each audited batch is approved.
- `main`: stable release; do not modify or promote without explicit approval.
- Mother upstream: `luqmanfadlli/NuvioMobile-Enhanced`; inspect `enhanced`, `stable-release`, release tags and any relevant feature branches. A branch name alone does not establish release stability.

## Current upstream references (2026-11-17)

| Reference | SHA / state |
|---|---|
| Latest published stable release | `1.0.0` |
| Latest published beta | `1.1.0-beta.1` — `eb32e0479932d28f5f6fa6afeb0e21c78ad02efd` |
| Upstream `enhanced` | `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03` |
| Upstream `stable-release` | `5b8c6dc2ad3cf83846ccbe3e05e96d409f5b2ab4` |
| Mew `upstream-sync` current source-sync commit | `2017c854d6e3eeceb49ab8841548f6143034935f` (tree matches `enhanced` exactly, plus two Mew docs) |
| Mew `Main-dev` | `207ef8fa309b8061b73039b2d4e4bde4af65050e` |
| Mew `main` | `207ef8fa309b8061b73039b2d4e4bde4af65050e` |

Re-fetch every ref before acting. These values are an audit checkpoint, not permanent truth.

## What the commit counts mean

- Upstream `1.0.0` → `enhanced`: 72 commits, 160 changed paths. This is the broad post-stable-release backlog.
- Upstream `stable-release` → `enhanced`: 73 ahead / 1 behind (diverged); do not merge the branch names blindly.
- Upstream beta `1.1.0-beta.1` → `enhanced`: 8 commits, 7 changed paths.
- Mew `upstream-sync` source base `f4b7e55e31c2778827095dbdb246193e8675016f` → upstream `enhanced`: 5 commits, 5 changed paths. Two earlier post-beta paths are already in Mew's snapshot; the five remaining paths are recorded in the detailed ledger.
- Mew `Main-dev` ↔ `upstream-sync`: 2,214 ahead / 596 behind by commit graph. This reflects divergent history, not a count of missing features. Never use this number as a merge plan.
- The 72-commit range includes features, bug fixes, UI/UX, localization, tests, release workflows and platform work. It must be audited by behavior and dependency, not processed as 72 isolated cherry-picks or one giant merge.

## Audit batches

Process a few cohesive features at a time. For each batch, inspect upstream commit(s), changed files, callers, Mew equivalents, tests, dependencies, persistence/migrations, platform source sets, UI state/lifecycle and regressions.

1. **Release and CI:** release workflow, beta channel/source, store metadata, versioning, release notes and branch triggers.
2. **Player and subtitles:** source fallback, subtitle rendering/outline/placement, gesture insets, episode transitions, external playback and playback regressions.
3. **Streams and debrid:** TorBox alternative hashes, stream matching, provider compatibility, URL/auth handling and cache behavior.
4. **Addons and tracking:** Stremio tracking/library addons, event ordering, STOP/progress semantics, reconciliation and consent/privacy.
5. **Downloads:** season/batch queueing, storage management, network constraints, cleanup, Android scheduler versus Mew foreground service, iOS backup behavior.
6. **Library and details:** remembered filters, genre/year/watched filters, artwork fallback, hero/detail presentation, metadata and episode labels.
7. **Home, navigation and shared UI:** catalog sync races, RTL/LTR, gestures/insets, navigation/back behavior, responsive and TV layouts.
8. **Localization and accessibility:** locale additions, translation correctness, bidi, focus/keyboard, semantics and screen-size checks.
9. **Profiles, settings and persistence:** backup/import coverage, profile isolation, account wipe, migrations, credentials and settings defaults.
10. **Platform/build/security:** Android/iOS-specific behavior, manifests, permissions, TLS/networking, dependencies, native player, signing and CI.

The sequence is a work queue, not a claim that all categories have been fully audited. Reorder by release risk and dependency when evidence warrants.

## Decision labels

- `TAKE`: same semantics fit Mew and no fork behavior is lost.
- `ADAPT`: port the behavior into Mew's architecture/UI.
- `SKIP`: platform-irrelevant, incompatible, redundant or intentionally excluded.
- `ALREADY IMPLEMENTED`: Mew already provides equivalent behavior; prove by comparing inputs, outputs and side effects.
- `BLOCKED`: missing contract, dependency, product decision, security review or verification.

Also classify architecture similarities as TRUE DUPLICATE, INTENTIONAL PARALLEL, SHARED PATTERN / DIFFERENT SEMANTICS, COMPATIBILITY SHIM or FALSE POSITIVE.

## Integration cadence

1. Refresh and record upstream refs and current Mew refs.
2. Audit a cohesive batch and record every changed path in the ledger.
3. Decide TAKE / ADAPT / SKIP / ALREADY IMPLEMENTED / BLOCKED with reasons.
4. Review Mew UI/UX, profile/settings persistence, provider compatibility, player, download and network side effects.
5. Integrate only the approved batch into `Main-dev`, using small logical groups rather than one commit per upstream commit.
6. Run targeted tests first, then relevant build/lint/CI. Inspect actual reports. Device-only behavior remains NOT VERIFIED without a device/emulator.
7. Reconcile the resulting Mew behavior against upstream and update this ledger plus the detailed context document.
8. Promote to `main` only with separate explicit approval.

Never force-push, rewrite history, delete branches/files, publish a release, change repository settings, or merge/promote without explicit approval. Before every write, verify target branch, exact SHA and intended changed paths; use expected-SHA guards where available.

## Initial 72-commit range — themes to audit

The GitHub compare from `1.0.0` to `enhanced` reports 160 paths:
- Common Kotlin: 91 paths.
- Android main: 12 paths; Android host tests: 5 paths.
- Common resources/locales: 17 paths; common tests: 12 paths.
- iOS main: 10 paths; iOS app/config: 4 paths.
- CI/release/docs/store scripts and metadata: remaining paths.

Commit subjects identify at least these functional groups:
- Stremio player/library tracking addons and tracking-event correctness/order.
- Player next-episode source fallback and Android gesture-navigation inset behavior.
- Subtitle outline width, styled overlay color/position/formatting, subtitle cue behavior.
- Season batch downloads and storage management.
- Library remembered filters, genre/year/watched filtering.
- TorBox alternative-hash cache matching.
- RTL/LTR mixed-direction text correction.
- iOS download backup exclusion and iOS tab/navigation presentation fixes.
- MDBList episode runtime correction and detail hero mute/back-button behavior.
- Ukrainian, Simplified Chinese, Brazilian Portuguese, Hebrew, Urdu, Indonesian, French and other localization updates.
- Beta release source, prerelease metadata, release notes, store publishing and CI changes.

These are audit themes from commit subjects and changed-path statistics, not yet a complete semantic acceptance list. Read each implementation and its tests before deciding.

## Known Mew findings already recorded

See [the detailed audit ledger](../mew-context/README.md) for evidence and file-level notes. Current candidates include:
- Adapt artwork fallback after episode-image load failure.
- Adapt catalog sync protection when remote known-key list is empty.
- Adapt Android gesture-navigation inset fix only after edge-to-edge and fullscreen checks.
- Adapt subtitle outline-width normalization at repository boundaries using Mew's 1–5 contract.
- Adapt account-wipe cleanup to Mew's foreground download service, pending-source worker and partial-file lifecycle.
- Compare settings backup coverage before adding any settings export code.
- Do not replace Mew's download transport with upstream WorkManager/JobScheduler; the lifecycle contracts differ.
- Do not port iOS-only code into Android.
- Preserve Mew's existing watch-progress identity implementation where blob-level equivalence is verified.
- Keep external playback progress blocked until Mew has a reliable external-player result contract.

## Verification and reporting

For every batch report: upstream commits/tags, files inspected, Mew equivalents/callers, decisions, exact changed files, tests/build/CI and device status, unresolved risks and next batch. Mark anything not executed as NOT RUN. Never claim a feature is aligned merely because a filename or commit subject matches.

Initial status before selective integration: **UPSTREAM-SYNC TREE ALIGNED; MAIN-DEV INTEGRATION AUDIT ONLY**. This historical checkpoint predates the four documented Main-dev adaptations below.


## Full-history scale and current snapshot checkpoint

- Common ancestor of Mew `Main-dev` and the upstream source history: `f9ad843b14cd3be7fa3ddb800b6961233e0e5b56` (`bump version`).
- From that ancestor, upstream `enhanced` is **2,193 commits ahead**. Mew `Main-dev` is **596 commits ahead of the same ancestor**. This confirms the 2,000+ history concern; it is not 2,193 discrete features to cherry-pick.
- GitHub compare reports at most 300 changed-file entries for this large comparison, so its file list is not a complete audit inventory. Use full recursive trees, release checkpoints, per-area searches and semantic caller review.
- On 2026-11-17, Mew `upstream-sync` was refreshed from its previous source snapshot by copying the five exact remaining changed upstream blobs from `enhanced` into one guarded commit: `2017c854d6e3eeceb49ab8841548f6143034935f`.
- Verification: recursive source-tree comparison now finds all 2,339 upstream blobs present, **zero missing paths, zero differing upstream blobs**, and only the two Mew-owned documentation files as extras. This is exact tree parity, not identical commit history.
- `Main-dev` and `main` remain at `207ef8fa309b8061b73039b2d4e4bde4af65050e`; no app changes have been integrated there. The 2,193-commit semantic audit remains ongoing.



## Exact current Mew-vs-upstream inventory

At the shared ancestor `f9ad843b14cd3be7fa3ddb800b6961233e0e5b56`, upstream has advanced 2,193 commits and Mew Main-dev has advanced 596 commits. Recursive tree comparison gives:
- Base 1,097 blobs; upstream enhanced 2,339; Mew Main-dev 1,668.
- Upstream vs Main-dev: 1,133 upstream-only paths, 462 Mew-only paths, 500 shared identical blobs, 706 shared paths with different blobs.
- Most upstream-only paths are commonMain (617), commonTest (120), Android main (112), Android host tests (94) and iOS main (53).
- Most differing shared paths are commonMain (399), Android main (132), commonTest (58) and iOS main (36).
- Most Mew-only paths are Android main (159), commonMain (110), commonTest (69) and iOS main (23); this includes Mew-specific CloudStream, branding and platform work that must be preserved.
- The upstream release API currently lists 86 published releases through `1.1.0-beta.1`. Audit by release checkpoints and feature areas, not commit-by-commit cherry-picking.

These counts measure tree paths, not features. Generated resources, vendored native code, assets, tests and build metadata need different review rules. GitHub's compare endpoint returns a capped file list for this large range; use recursive tree inventories and per-area semantic inspection for full coverage.

## Selective integration progress — 2026-11-17

Latest verified state (supersedes older branch-head checkpoints above):

- Upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Mew `upstream-sync`: `4ded43d72a0e9e31879eaa72c76d35feb4f06720`; its source snapshot remains aligned to the upstream `enhanced` tree, with Mew-owned docs as extras.
- Mew `Main-dev`: `813510f128e58aefa0b5daa4757f18502c733fa6`.
- Mew `main`: unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

### Completed adaptation

- Upstream commit: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03` — episode artwork fallback after image-load failure.
- Upstream path: `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/components/DetailSeriesContent.kt`.
- Mew equivalent inspected: same path on `Main-dev`. Mew's episode-card implementation differs substantially in layout, TV sizing, progress, ratings, download affordances and click/zoom behavior; wholesale file replacement was rejected.
- Dependency check: Mew and upstream both use Coil 3.6.3. Existing Mew `remember` / `mutableStateOf` imports support the state helper; no dependency was added.
- Decision: **ADAPT**. Added keyed fallback state and Coil `AsyncImage.onError` handling to Mew's horizontal and list episode cards. The original Mew card layouts and interactions remain intact.
- Mew commits: `edcda9a8e081143ae87aab8bbc6c48cb4ca8c4c8` (implementation), `813510f128e58aefa0b5daa4757f18502c733fa6` (callback indentation correction).
- Verification: GitHub commit diff read back; branch head confirmed. No local Gradle build, automated tests, CI run or device test was executed. Runtime fallback remains **NOT VERIFIED**.
- No equivalent image-load fallback was found in the inspected Mew detail component. Repository-wide code search returned incomplete/no usable matches, so broader duplicate status remains unproven.
- `Main-dev` only was changed. `main` and `upstream-sync` application source remain untouched.

Next: continue auditing the remaining post-1.0.0 feature groups and older release checkpoints. Do not treat this one adaptation as completion of the full upstream audit.

### Second adaptation: early catalog sync

- Upstream commit: `0b9d93599f7d7603e7814a99a70aa64d7c48e086` — preserve local catalog choices when sync arrives before catalogs load.
- Mew equivalent: `HomeCatalogSettingsRepository.applyFromRemote`. Mew already has additional hero settings, pinned collections and a richer normalization path; these are retained.
- Confirmed gap: `knownPreferenceKeys()` can be empty before `syncCatalogs` / `syncCollections` populate definitions. Mew's previous filter then dropped ordinary local preference keys before the definitions were ready.
- Decision: **ADAPT** the upstream empty-known-key guard through a small testable predicate. Remote payload keys still win; once definitions exist, known keys and explicit sync keys are retained while unrelated legacy keys are filtered.
- Changed files on `Main-dev`: `composeApp/src/commonMain/kotlin/com/nuvio/app/features/home/HomeCatalogSettingsRepository.kt`; new `composeApp/src/commonTest/kotlin/com/nuvio/app/features/home/HomeCatalogRemotePreferenceMergeTest.kt`.
- Commit: `db07c08cea6073ca8119df14eb1e41fcc58572f1`.
- Regression tests added for early sync, remote-key precedence, and normal known/explicit/legacy-key filtering. Tests/build/CI/device verification: **NOT RUN**; the tests have not been executed.
- Latest `Main-dev`: `db07c08cea6073ca8119df14eb1e41fcc58572f1`. `main` remains `207ef8fa309b8061b73039b2d4e4bde4af65050e`. `upstream-sync` remains the upstream source snapshot plus docs.

### Third adaptation: TorBox alternative-hash cache matching

- Upstream commit: `1118dfbf87de7d9e68860446ea553d48d06adcba`.
- Mew had the same cache lookup path but treated TorBox's canonical-hash response key as a miss when the requested hash was an alternative hash. `LocalDebridAvailabilityService` consumes the returned map using the original lowercased stream hash, so the mismatch could mark a cached stream `NOT_CACHED`.
- TorBox's documented batch endpoint is POST `/v1/api/torrents/checkcached`; Mew already uses the object format and Bearer auth. No endpoint, credential, transport or dependency changes were made. Reference: https://api-docs.torbox.app/
- Decision: **ADAPT** the upstream resolver and tests. Normalize/deduplicate requested hashes, preserve exact matches, map a single alias answer directly, and retry only unresolved hashes in a batch.
- Mew-specific safety improvement: if any per-hash retry fails, return an unavailable result so the caller keeps cache state **UNKNOWN** rather than incorrectly reporting **NOT_CACHED**. Cancellation continues to propagate.
- Changed files on `Main-dev`: `composeApp/src/commonMain/kotlin/com/nuvio/app/features/debrid/LocalDebridService.kt`; new `composeApp/src/commonTest/kotlin/com/nuvio/app/features/debrid/LocalDebridServiceTest.kt`.
- Commit: `dd4b6ce5d0ff26527e7b3ad482ec77a3235d6bde`.
- Regression tests added for exact matches/no extra request, single alias mapping, batch retries of missing hashes only, and retry failure remaining unknown. Tests/build/CI/device verification: **NOT RUN**.
- Remaining risk: retries for a batch containing alias responses are concurrent and not explicitly capped. Review real stream-batch sizes and provider rate-limit behavior before changing that policy.
- Latest `Main-dev`: `dd4b6ce5d0ff26527e7b3ad482ec77a3235d6bde`. `main` remains `207ef8fa309b8061b73039b2d4e4bde4af65050e`. `upstream-sync` remains the upstream source snapshot plus Mew docs.

### Fourth adaptation: subtitle outline-width normalization

- Upstream commit: `b8bbd700fda41ad55cdb90de2d60e48d10f5d0db` adds the outline-width setting and clamps loaded/saved values to `subtitleOutlineWidthRange` (1–5).
- Mew already exposes a 1–5 stepper, but `PlayerSettingsRepository.loadFromDisk()` restored persisted values without clamping and `setSubtitleStyle()` saved incoming values unchanged.
- Decision: **TAKE the repository-boundary validation, ADAPT to Mew's existing 1–5 UI contract**. Added shared min/max constants and `normalizeSubtitleOutlineWidth`; use it on load, set/persist, and the existing stepper. No player redesign or default change.- Changed files on `Main-dev`: `SubtitleAudioModels.kt`, `PlayerSettingsRepository.kt`, `SubtitleStylePanel.kt`, and `SubtitleStyleStateTest.kt`.
- Regression test covers minimum, maximum and valid values. Commit: `2d5fc48d285b4ae42a2d341cb34cd28897685f6a`.
- Tests/build/CI/device verification: **NOT RUN**. The test is committed but has not been executed.
- Latest `Main-dev`: `2d5fc48d285b4ae42a2d341cb34cd28897685f6a`; `main` remains `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

## Additional 1.0.0 → enhanced audit — 2026-11-17

GitHub compare confirms `1.0.0...enhanced` contains **72 commits**. Four behavior changes have been adapted into `Main-dev` so far (episode artwork fallback, early catalog sync preservation, TorBox alternative-hash matching, subtitle outline-width normalization). This is not a complete audit of the 72 commits.

### Player source fallback — group requires further investigation

- Upstream base feature: `8fba6c71e95e4a6d94f0ccf1998e93ef50ce1aa6`; next-episode extension: `ff7500fdd601e445982d6efb9177ec8cd14f7976`.
- Upstream adds shared `StreamSearchSessions`, a `PlayerSourceFallbackRun` state machine, runtime fallback, configurable max attempts/startup timeout, player settings/storage/UI, stream-search sharing, and next-episode fallback.
- Mew currently has no `StreamSearchSessions`, `PlayerSourceFallbackRun`, or `PlayerScreenRuntimeSourceFallback`. Its `PlayerNextEpisodeAutoPlay` selects one stream and `switchToEpisodeStream` resolves it; failed/stale debrid resolution shows a toast and refreshes streams rather than trying the next matching source.
- Mew's `PlayerStreamsRepository` and `StreamsRepository` have overlapping provider-fetch pipelines but distinct state and lifecycle: player source/episode flows versus stream-screen request tokens, auto-play overlay, manual-selection reuse and binge-group state. Classification: **INTENTIONAL PARALLEL / SHARED PATTERN, DIFFERENT SEMANTICS**; do not collapse the repositories as a quick dedupe.
- Decision: **NEEDS INVESTIGATION as one feature group**, not a partial port of the final next-episode fix. Audit settings compatibility, player engine-switch-on-error exclusivity, debrid/P2P/download paths, request cancellation, search-session ownership, source identity and screen lifecycle. Add common regression coverage and verify on device before enabling. No code changed for this group.

### MDBList episode runtime correction

- Upstream commit: `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb`; affected `MdbListProgressProjection.kt`.
- Mew has `MdbListMetadataService` and settings only; its recursive source tree has no MDBList progress projection or MDBList watch-progress sync implementation.
- Decision: **SKIP this isolated bugfix** because its target behavior does not exist in Mew. Audit the broader MDBList progress-sync feature separately before considering a port. No code changed.

### Native navigation-bar hero anchor

- Upstream commit: `f28929d3e3cc0d354de1dd921efc539bbf8a14cf`; introduces a native navigation-bar button anchor for hero mute-button placement, including UIKit frame/margin reads.
- Mew's `DetailHero` uses Compose hero padding; its iOS `PlatformInsets` exposes physical top inset only and has no native navigation-bar anchor API. Mew's Android/iOS navigation composition is therefore not equivalent to upstream's native-bar integration.
- Decision: **SKIP for current Mew navigation architecture**. Do not import UIKit anchoring code just to match filenames or padding. Revisit only if Mew adopts the same native navigation-bar button model. No code changed.

### Latest branch state

- Upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Mew `Main-dev`: `2d5fc48d285b4ae42a2d341cb34cd28897685f6a` (four selective adaptations).
- Mew `main`: unchanged at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Mew `upstream-sync`: this documentation branch; upstream application source snapshot remains unchanged.
- Tests, Gradle builds, CI and device verification remain **NOT RUN** for this session.

### Stremio tracking addons — privacy-sensitive feature group

- Upstream feature commit: `964ecfd8d98598cd2d315cedb584fe7ef59a76fd`; follow-up correctness/performance commits: `d89811ac6c75a3f80c0ab424ab01a37639401d54` and `8f5d9bcac3c2c0535b17b26cbdff392c10b8d767`.
- Upstream introduces player and library resource event delivery to installed addons, explicit install consent, ordered event delivery, video-id chunking, and guards against sync-derived series watched reconciliation being emitted as user intent.
- Mew already parses addon manifest resources and has Trakt/Simkl-style tracking providers in `LibraryRepository` and `WatchedRepository`, but the inspected Mew addon install/repository and tracking call paths do not contain an addon event dispatcher or tracking-addon consent flow.
- Classification: **MISSING FEATURE / NEEDS INVESTIGATION**. This is not a filename-level port. It transmits watch activity, playback positions and library changes to third-party addon developers/services.
- No code changed. Before implementation, require explicit approval for the feature scope and preserve a mandatory consent step. Audit all watched/library mutation paths to ensure only direct user actions emit events—not cloud sync, playback completion, derived series reconciliation or profile switching. Review per-addon ordering, queue isolation, retry/timeout, opt-out/removal, existing installed tracking addons and privacy wording.
- Required regression tests: consent gating; event URL encoding; per-addon ordering/isolation; 100-id chunking; no events for sync/reconciliation/playback-derived changes; correct start/pause/stop/seek lifecycle.

### Downloads batch / storage-management feature group

- Upstream commit: `48cbb901bf48a641a1270c2d10bbd4260e712e68` (large cross-platform feature batch).
- Mew already has a season/episode batch flow: `EpisodeDownloadFlowSheet`, `EpisodeDownloadCoordinator`, automatic per-episode source/quality selection, bounded provider/episode concurrency, pending source searches, queue modes (`AllAtOnce` / `OneAtATime`), grouped completed-show episodes, offline metadata snapshots and external-folder selection.
- Classification for the core batch-download behavior: **ALREADY IMPLEMENTED / INTENTIONAL PARALLEL**. Do not port the upstream coordinator or download queue wholesale; that would duplicate Mew's existing implementation and risk its pending-source workflow.- Remaining differences need a separate audit: upstream adds device storage statistics/bar, metered-network/Wi-Fi policy and live network guards, plus additional platform transfer/cleanup behavior. Mew's inspected download tree has no matching `DownloadNetworkGuard` or `DownloadsStoragePlatform` abstraction; its storage contract currently persists the download payload, external folder URI and episode-download settings.
- Decision: **SKIP duplicate batch behavior; NEEDS INVESTIGATION for storage/network lifecycle gaps**. Compare Android/iOS downloader scheduling, multi-profile cleanup, resume-data behavior, and mobile-data preferences before porting any platform code. No code changed.

#### Downloads batch / storage audit — refined decisions

The comparison of Mew's actual implementations against upstream commit `48cbb901bf48a641a1270c2d10bbd4260e712e68` establishes:

| Upstream area | Mew evidence | Decision |
|---|---|---|
| Season/episode batch selection and source picking | `EpisodeDownloadFlowSheet`, `EpisodeDownloadCoordinator`, selected-provider matching, auto-select, pending-source retry | **ALREADY IMPLEMENTED / INTENTIONAL PARALLEL**. Keep Mew's sheet and pending-source flow. |
| Batch source resolution | Mew resolves a chosen provider/quality for each target and queues unresolved targets for later retry; upstream uses a plan state machine, a shared two-permit source loader, and per-season source selection | **SHARED PATTERN / DIFFERENT SEMANTICS**. Compare edge cases and bounded concurrency; do not replace either coordinator wholesale. |
| Download queue | Mew supports `AllAtOnce` and `OneAtATime`; `AllAtOnce` can start every selected item, and `OneAtATime` promotes one waiting item when no download is active | **PARTIAL GAP**. Upstream adds configurable global, per-show and concurrent-show limits. Candidate: **ADAPT** into Mew's existing repository and preserve both existing modes. |
| Network policy | Mew Android/iOS downloaders use their own HTTP transfer implementations; no download-specific network guard or mobile-data preference was found in the inspected source tree | **MISSING FEATURE**. Candidate: **ADAPT** upstream's Wi-Fi/metered policy to Mew's downloader lifecycle and settings. Do not copy platform code without checking app foreground/background behavior and iOS path-monitor lifetime. |
| Device storage bar/statistics | No Mew `DownloadsStoragePlatform` or device storage stats component found. Mew supports a user-selected external folder on Android/iOS | **MISSING FEATURE**. Candidate: **ADAPT**; stats must describe the actual selected destination or clearly distinguish app storage from external-folder capacity. |
| Offline show metadata | Mew stores `DownloadDetailsSnapshot` with download records and reconstructs offline details through `findOfflineMetaDetails` / `toOfflineMetaDetails` | **ALREADY IMPLEMENTED / DIFFERENT STORAGE MODEL**. Do not add upstream's separate `OfflineMetadataRepository` without proving a missing data field or migration need. |
| Downloaded show/episode browsing | Mew's `DownloadsScreen` has root and show-content flows and episode lookup; offline detail navigation is supported | **ALREADY IMPLEMENTED / VERIFY UI PARITY**. Upstream's library download button is not evidence of a missing core download capability. |
| Profile/account cleanup | Mew downloads payload is profile-scoped, but Android account cleaner does not list `nuvio_downloads`; iOS cleaner's scoped-key list does not include download payload/external-folder/settings keys. Mew repository profile-change handling cancels active handles but does not itself perform upstream's unfinished-transfer cleanup | **CONFIRMED CLEANUP GAP; NEEDS INVESTIGATION**. Define whether account wipe should retain completed media, then adapt cleanup consistently on Android/iOS. Preserve per-profile downloads on ordinary profile switches. |
| Platform transfer scheduling/resume | Mew uses OkHttp coroutine/range-resume on Android and NSURLSession data tasks/range-resume on iOS; upstream adds Android WorkManager/JobScheduler scheduling and iOS background-session resume-data semantics | **INTENTIONAL PARALLEL / NEEDS INVESTIGATION**. These are not drop-in equivalents. Audit background execution, cancellation, resume guarantees, redirects/headers, and lifecycle before considering a scheduler migration. |

**Files inspected:** Mew `DownloadSourceResolver.kt`, `DownloadsRepository.kt`, `DownloadsModels.kt`, `DownloadsScreen.kt`, `EpisodeDownloadFlowSheet.kt`, Android/iOS `DownloadsPlatformDownloader`, Android/iOS `DownloadsStorage`, and Android/iOS `PlatformLocalAccountDataCleaner`; upstream `BatchDownloadCoordinator.kt`, `BatchDownloadQueuer.kt`, `DownloadQueuePolicy.kt`, `DownloadsStorageStats.kt`, `OfflineShowMetadata.kt`, and the platform downloader/network/storage changes in commit `48cbb901...`.

**No app code changed.** Candidate integration order: (1) clarify account-wipe contract and fix profile-safe cleanup, (2) network policy + settings, (3) queue concurrency policy, (4) storage stats/UI. Keep platform transfer architecture unchanged until a separate background/resume audit is complete. Build/tests/device verification: **NOT RUN**.

### Library vertical filters — upstream commit `2da28d707c77b0bb8d3030af8f360e67981a0994`

- Upstream adds per-profile persistence for selected list/type, genre/year/watched filters with faceted counts, normalized type ordering, and responsive filter-chip layout.
- Mew already has vertical library projection, type filtering, sorting, per-profile layout/sort settings, watched-state data, genres and release metadata on library items, plus separate downloaded-content filters. But the inspected Mew projection only filters by section/type; it has no genre/year/watched facets. Selected section/type are `rememberSaveable` screen state rather than fields in `LibraryDisplaySettingsUiState`.
- Classification: **PARTIAL GAP / SHARED PATTERN, DIFFERENT SEMANTICS**. The data inputs and base projection exist; the new filters and per-profile persistence are not implemented.
- Decision: **ADAPT** the filter logic into Mew's existing `LibraryVerticalProjection` and `LibraryDisplaySettingsRepository`; preserve Mew's local/remote source modes, provider order, watched-series semantics, downloads tab and existing screen design. Do not copy upstream `LibraryScreen` or `LibrarySavedContent` wholesale.
- UI/UX review required: filter state reset/invalid-option behavior when source/list/type changes, faceted counts, empty states, horizontal vs vertical layout, compact widths, and profile switching. Add projection tests for genre/year/watched combinations and persistence round-trip before implementation.
- No app code changed. Build/tests/device verification: **NOT RUN**.

### iOS download backup exclusion — adapted

- Upstream commit: `718b526a67654fa94a18da39aabdd7d4aa21af44`.
- Mew stores default iOS downloads in `Documents/nuvio_downloads`, which was included in iCloud/computer backups. No existing backup-exclusion attribute was found.
- Decision: **TAKE / ADAPT**. Set `NSURLIsExcludedFromBackupKey` on Mew's downloads directory once it is created. This also covers existing files in that directory. User-selected external folders are not modified.
- Main-dev commit: `a4b8ff258439f91140da2f92c60bec868fc6cec0`; file: `composeApp/src/iosMain/kotlin/com/nuvio/app/features/downloads/DownloadsPlatformDownloader.ios.kt`.
- Verification: GitHub commit diff confirms only the iOS downloader changed and the default downloads folder receives the attribute. iOS compile/device verification: **NOT RUN**.

### iOS pushed-page blur and Android player inset fixes — reviewed

- `c5e74dde1b7a7085d6e8684d5780f83faae68b38` changes upstream's UIKit `RootComposeViewController` navigation-bar appearance and iOS 26 scroll-edge effects. Mew's `iosApp/iosApp/ContentView.swift` has a different root/tab-controller lifecycle; the inspected Mew controller does not have the matching pushed `UINavigationController` navigation-bar/scroll-edge setup. **SKIP for now**; only revisit if Mew reproduces the same frosted strip on its actual iOS navigation stack.
- `d374bdf022f63ead318387c6f9ffebcdb8113497` changes upstream's `playerTimelineBottomInsets` from Android `safeContent` to `safeDrawing`. Mew's `PlayerLayout.kt` does not contain that helper or the same bottom-inset calculation; the only matching `safeContent` use found is horizontal safe padding. **FALSE POSITIVE / NOT APPLICABLE** to this Mew implementation. Do not import an unused helper or alter player layout without a reproduced device issue.
- No code changed for either item.

### CMP merge follow-up: TorBox alias matching, RTL and home sync

- Upstream commit `1118dfbf87...` and the CMP merge's `LocalDebridService.kt` change address TorBox alternative-hash responses that return a canonical hash rather than the requested alias.
- Mew `LocalDebridService` already normalizes hashes, retries only unresolved batch entries, maps single aliases directly, and returns `null` if a retry fails so availability remains **UNKNOWN** rather than a false cache miss. `LocalDebridServiceTest` covers exact hits, single aliases, selective retries and failed-retry unknown state.
- Decision: **ALREADY IMPLEMENTED**, with stricter unknown-state handling than upstream's shown helper. No port.

- Upstream `5964cdca71...` fixes mixed RTL/LTR text in Search and Watch Party by exposing a text style on the shared input and setting `TextDirection.Content`.
- Mew's shared `NuvioInputField` does not expose a text-style parameter; Search and party message/recent-search text use default direction. Decision: **ADAPT** into Mew's shared input and relevant text surfaces, with Arabic/Latin URL and message regression tests. No code changed.

- Upstream `0b9d93599f...` preserves local catalog choices when remote sync arrives before catalog definitions load. Mew's `HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync` already has the same `knownKeys.isEmpty()` safeguard. Decision: **ALREADY IMPLEMENTED**; no port.

### Current checkpoint

- Upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Mew `Main-dev`: `a4b8ff258439f91140da2f92c60bec868fc6cec0` (includes iOS download backup exclusion).
- Mew `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`, unchanged.
- `upstream-sync`: documentation-only updates; app source snapshot remains the aligned upstream tree.

### Localization coverage audit

- Upstream adds Bengali, Croatian, Urdu, Ukrainian and Simplified Chinese UI locale packages and registers language options/Android locale configuration. Mew currently has no resource directories for those five locales and does not expose them in `AppLanguage`.
- Mew's default strings already contain the five language display-name keys, so the missing work is the locale resources, `AppLanguage` entries and Android `locale_config.xml` registrations. Mew already has Arabic and Russian translations and `AppLanguage` entries, but its Android `locale_config.xml` currently omits `ar` and `ru`.
- Key-count comparison (string/plural/array resource names): Mew English base has 2,933 keys. Existing Mew Hebrew has 782 (26.7%) and Brazilian Portuguese has 1,560 (53.2%). Upstream's locale files contain translations for 2,121 Mew-base keys in Hebrew/Chinese/Ukrainian/Bengali/Croatian and 2,115 in Urdu; pt-BR contains 2,169 Mew-base keys. The upstream locale files do not cover all 812 Mew-only keys, so replacing Mew resource files wholesale would drop Mew-specific translations.
- Decision: **ADAPT** the five upstream locale packages by merging only matching keys into Mew's resource namespace, preserve all Mew-specific strings and use default English fallback for untranslated Mew-only keys. Update `AppLanguage` and Android locale config, including the existing Arabic/Russian registration omission. Review RTL layout separately; do not assume adding Urdu/Hebrew/Arabic resources alone fixes mixed-direction text.
- Add resource-key parity checks and verify Compose resource generation plus Android per-app language settings. No app code changed in this audit; builds/tests **NOT RUN**.

### Mixed RTL/LTR text — adapted

- Upstream commit: `5964cdca71c0cc6c4892af88f76083b165c6e55c`.
- Mew's shared `NuvioInputField` hard-coded its text style, preventing callers from requesting content-aware paragraph direction. Search, recent-search labels, party sender names, chat bubbles and composer did not explicitly use `TextDirection.Content`.
- Decision: **TAKE / ADAPT**. Added an optional `textStyle` parameter with the existing typography as its default, then applied content-aware direction only to Search and Watch Party text surfaces. Unmodified callers retain the previous default style.
- Main-dev commit: `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; files: `core/ui/Components.kt`, `features/search/SearchScreen.kt`, `features/party/PartyChatContent.kt`.
- Verification: GitHub diff reviewed; combined status returned no status checks. Tests/build/device checks: **NOT RUN**. Mixed Arabic/Hebrew + Latin text and IME editing still need UI regression verification.

### Release workflow / beta-channel comparison

- Upstream commits `511ebd5f17`, `ac2021de3a`, `8ac35a18cd`, `647b561b01` and `b829dc2522` evolve a cross-platform stable/beta release workflow, separate beta sideload source, plain version tags, pre-release flags, and release notes split between fork and upstream commits.
- Mew intentionally has a separate Android-first `android-release.yml` with optional unsigned iOS packaging, Mew signing secrets, `Version.xcconfig` version metadata, `CHANGELOG.md`, and Mew-specific release-note generation. Upstream's iOS store source, beta store JSON and workflow should **not** be copied wholesale.
- **Release trigger mismatch:** the Mew workflow's `push.branches` contains `enhanced`, but the live repository branch list contains only `Main-dev`, `main` and `upstream-sync`. Automatic push builds therefore do not trigger on the active development branch.
- **Release safety gap:** `Validate release state` checks that dispatch is from a branch and checks tags/version-bump state, but does not restrict `RELEASE_BRANCH` to an approved release branch. A manual `draft`/`publish` dispatch can target an unintended branch unless external repository protections prevent it.
- Decision: **INTENTIONAL PARALLEL release architecture; NEEDS INVESTIGATION for Mew trigger/branch guard**. Do not alter workflows, publish, tag, or change repository settings in this audit. Before any release-workflow edit, explicitly settle whether release builds are allowed from `Main-dev` or only `main`, how beta versions/tags are represented, and whether Mew needs an upstream-vs-fork release-note split.
- Live evidence: repository default branch is `main`; `android-release.yml` exists on both `main` and `Main-dev`; current branches are `Main-dev`, `main`, `upstream-sync`. Main-dev at audit: `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; main: `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- No workflow code changed; no release was created. Workflow execution/build/CI checks: **NOT RUN**.

## Library filters + mixed-direction text audit — 2026-11-17

### Library filters (upstream `2da28d707c77b0bb8d3030af8f360e67981a0994`)

Mew `Main-dev` currently has vertical/horizontal layouts, source-aware sorting, list/type selection, genre and release metadata on `LibraryItem`, and watched badge state. It does **not** have the upstream vertical-grid Genre, Year or Watched filters, facet counts, or persistent selected list/type.

Important architecture differences:
- Mew's `LibraryDisplaySettingsStorage` is already profile-scoped on Android and iOS. Extend its existing JSON schema; do not add a second preference store.
- Mew currently holds selected list/type in `rememberSaveable` screen-local state; upstream moves these into profile-scoped display settings.
- Mew has LOCAL/TRAKT/SIMKL sources, not MDBList library source; do not port MDBList-only release-sort behavior from newer upstream code.
- Mew's `LibraryScreen` contains Mew-specific cloud library, downloads, release calendar/radar, and profile logic. Upstream's 154 KB screen is not a safe replacement for Mew's 111 KB screen.
- Mew `LibraryItem` already exposes `genres` and `releaseInfo`; watched state is available from `WatchedRepository` and fully-watched series keys.

Decision: **TAKE / ADAPT**, as a bounded vertical-grid filter feature. Implement pure projection/facet logic and tests first; then add the filter row and persist selected list/type through the existing profile-scoped settings repository. Preserve Mew's sort behavior, Cloud/Downloads modes and release-calendar/radar state. Need explicit decisions for watched matching (movie/series keys and episode-derived fully-watched series) and facet-count semantics before UI wiring. No app code changed in this audit.

### Mixed RTL/LTR Search and Watch Party text

- Upstream commit: `5964cdca71c0`.
- Mew commit: `69b4a96072b506f60dcbe538c5f6b71db3a4a464`.
- Mew already adds `TextDirection.Content` to search input/recent query, watch-party message metadata/body/composer and exposes a configurable input text style.
- Upstream additionally makes `MessageRow` and `SearchRecentRow` internal and adds Robolectric/Compose host tests covering mixed RTL/LTR text, selection/editing, and paragraph direction. Mew's patch has no equivalent targeted host test and keeps those functions private.
- Decision: **ALREADY IMPLEMENTED** for runtime behavior; **ADAPT** the regression tests when test infrastructure permits. No duplicate UI changes needed.

### Branch checkpoint

At this audit: `Main-dev` = `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; `main` = `207ef8fa309b8061b73039b2d4e4bde4af65050e`; upstream `enhanced` = `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. The `1.0.0...enhanced` range still contains 72 commits; this is a feature-group audit, not a completed review of every commit. Tests/build/CI/device verification: **NOT RUN** in this pass.

## iOS UI / subtitle / backup picker follow-up audit — 2026-11-17

### Floating tab-bar blur — upstream `c314dd48d622`

Mew's `iosApp/iosApp/ContentView.swift` owns a custom native tab bar and applies tint/title styling by copying its existing `UITabBarAppearance`. It does not currently clear the tab-bar background effect or hide iOS 26 bottom scroll-edge effects. Upstream's UIKit fix targets a different root controller/tab-bar composition and its own floating pill. Mew's actual visual result has not been reproduced/verified on device.

Decision: **NEEDS INVESTIGATION**, not a blind UIKit port. Verify iOS 26 Static/Auto-hide tab modes and Mew's custom tab appearance first; preserve its icon/title styling and tab interaction.

### Styled subtitle retention after next episode — upstream `c5298633914d`

Upstream changes its iOS player-local subtitle text state from `remember(sourceUrl)` to a stable remembered state reset by `LaunchedEffect(sourceUrl)`, retaining the style state across source/episode recomposition. Mew's `PlayerEngine.ios.kt` does not contain that `subtitleText` / `subtitleStyle` state pair; it routes subtitle selection through a different player/bridge implementation. The inspected Mew file therefore is not a safe patch target.

Decision: **NEEDS INVESTIGATION** in Mew's actual iOS styled-subtitle overlay/bridge lifecycle. No code changed; do not modify Android/libmpv subtitle behavior based on this iOS-specific patch.

### iOS backup export picker — upstream `d703ca48e5ad`

Upstream changes export from `UIDocumentPickerViewController(forExportingURLs:asCopy:)` to `UIDocumentPickerModeExportToService`. Mew's backup bridge does not use either picker for export: it writes a temporary JSON file and presents `UIActivityViewController`; import uses `UIDocumentPickerModeImport`.

Decision: **FALSE POSITIVE / DIFFERENT UX**. Preserve Mew's share-sheet export unless a separate bug is reproduced. No code changed.

No tests/build/CI/device verification run for these audit-only decisions.

## Post-snapshot upstream follow-up — 2026-11-17

Compared the source snapshot base `4ded43d72a0e9e31879eaa72c76d35feb4f06720` with upstream `enhanced` at `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. GitHub reports five upstream-ahead commits and 31 commits on the opposite side of the divergent histories; the compare reports five changed paths. This is a semantic audit only, not a merge plan.

| Upstream commit | Finding in Mew | Decision |
|---|---|---|
| `05e779a0c178` — bug-report install method and official-app baseline | Mew's bug template already asks for install method and supports Android release/preview/CI/source builds. It does not ask whether the same issue reproduces in the official Nuvio app. That question can help distinguish upstream-originated bugs from Mew-only regressions, but should be worded for Mew's support context. | **ADAPT** template question only; no app behavior change. |
| `d703ca48e5ad` — iOS export picker mode | Mew's backup export uses a temporary file and `UIActivityViewController`, not the upstream document-picker export API. | **FALSE POSITIVE / DIFFERENT UX**; keep Mew share-sheet export. |
| `0b9d93599f7d` — preserve catalog choices during early remote sync | Mew `HomeCatalogSettingsRepository.shouldPreservePreferenceAfterRemoteSync` already preserves local keys while `knownKeys` is empty, while still giving remote keys precedence. Verified in the inspected Main-dev source. | **ALREADY IMPLEMENTED**; no duplicate patch. |
| `c5298633914d` — retain styled iOS subtitles across episode changes | Mew's inspected Main-dev `PlayerEngine.ios.kt` uses a different subtitle/bridge flow and does not contain the upstream `subtitleText` / `subtitleStyle` state pair. Locate the actual Mew styled-overlay state owner and compare source-change lifecycle before any change. | **NEEDS INVESTIGATION**; do not transplant the iOS-only state patch or touch Android/libmpv subtitles. |
| `b4e446451f32` — episode artwork fallback after image load failure | Mew Main-dev `DetailSeriesContent.kt` already contains `rememberEpisodeImageState` and uses its URL for episode cards. This behavior was previously adapted in Main-dev. | **ALREADY IMPLEMENTED**; do not duplicate. |

### Verification and branch safety

- Upstream `enhanced` latest inspected commit: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- Mew `upstream-sync` was verified at documentation commit `0d35721389075a5e962120037180547f2f13c807` before this documentation update.
- GitHub compare responses for `Main-dev` were inconsistent across adjacent reads (one response reported `69b4a960...`; another showed `a4b8ff258...` as its ancestor). Treat Main-dev's exact branch head as **UNRESOLVED** until a fresh ref read; do not write to it based on the stale checkpoint.
- No app source, workflow, release, or branch promotion changed in this batch. Tests/build/CI/device verification: **NOT RUN**.
- Next audit: locate the actual Main-dev iOS styled-subtitle overlay/state owner, then continue the remaining localization, provider compatibility, playback/subtitle and UI-regression batches. Also review the bug-template baseline question as a small documentation/support improvement.

## Expanded alignment mandate — product, architecture, quality and UI/UX parity

The objective is **full maturity alignment**, not merely importing missing features. Audit upstream improvements even when Mew already has a feature with the same name. An existing Mew implementation is not automatically equivalent, safer, more complete or better architected.

For every meaningful upstream change, inspect the behavior and compare it with Mew across these dimensions:

1. **Bug fixes and reliability:** identify the original failure, affected callers/platforms, root cause, edge cases and regression coverage. Check whether Mew has the same bug, a related variant, or already has a stronger fix. Port the underlying fix where relevant, not just the changed lines.
2. **Architecture and engineering maturity:** compare boundaries, ownership, state machines, data flow, concurrency, cancellation, lifecycle, error handling, caching, persistence, migrations, testability, observability and dependency choices. Look for simpler, safer, more maintainable or more scalable upstream designs. Do not preserve a weaker Mew design solely because it is already present; equally, do not replace a sound Mew design merely because upstream renamed or reorganized it.
3. **New and improved features:** audit capability, completeness, settings, defaults, profile behavior, platform parity, accessibility, offline behavior and failure states. A feature may exist in Mew but still have meaningful upstream improvements worth adapting.
4. **UI/UX and visual quality:** compare actual screens and user flows, not only composable names. Inspect hierarchy, spacing, typography, icons, motion, transitions, navigation/back behavior, gestures, sheets/dialogs, loading/empty/error states, responsive layouts, TV/remote behavior, keyboard/focus, accessibility and state restoration. Identify upstream designs that are clearer, more consistent, more polished or easier to use. Also identify where Mew's design is intentionally better and must remain.
5. **Performance and resource use:** compare startup, recomposition, scrolling, image loading, memory, network requests, database work, background execution, battery and large-library behavior where evidence exists.
6. **Security, privacy and compatibility:** inspect auth/token handling, TLS, redirects, storage, backup, permissions, account/profile isolation, provider contracts, data loss risks and backwards compatibility.
7. **Tests and delivery quality:** compare unit/host/instrumentation tests, fixtures, CI, build configuration, release validation and migration coverage. Upstream tests may expose gaps even when runtime behavior already exists in Mew.

### Required comparison record

For each candidate or cohesive change group, record:

- Upstream commit/release, changed paths and the user-visible or internal problem it addresses.
- Upstream implementation contract: inputs, outputs, state ownership, side effects, lifecycle, failure behavior and platform scope.
- Mew implementation and all relevant callers, shared components, routes, repositories, source sets and tests.
- Semantic differences, not just file or symbol differences.
- Whether upstream is demonstrably better, Mew is better, both have gaps, or the implementations are equivalent.
- User impact, engineering benefit, regression/security/data-migration risk, dependencies and verification plan.
- Decision: **TAKE**, **ADAPT**, **IMPROVE MEW**, **ALREADY IMPLEMENTED (EQUIVALENT OR BETTER — WITH EVIDENCE)**, **KEEP MEW DESIGN**, **SKIP**, or **NEEDS INVESTIGATION**.

Do not label something ALREADY IMPLEMENTED just because a similarly named class, screen or setting exists. Demonstrate parity across behavior, lifecycle, errors, persistence and platform coverage. If upstream is more mature, record the exact improvement and make an adaptation candidate even if Mew's basic feature already works.

### UI/UX parity is a first-class audit

For every upstream UI/UX change, inspect screenshots/previews where available and trace the complete interaction. Compare the relevant Mew screen and shared design system. Record:
- What changed visually and behaviorally, and which user problem it solves.
- Whether the design is objectively clearer or more usable, rather than merely different.
- Reusable components/tokens and consistency with Mew's existing visual language.
- Android, iOS, compact phone, large-screen/tablet and TV implications where supported.
- Accessibility, localization/RTL, focus, keyboard, scrolling, insets, gestures and navigation regressions.
- Whether to adopt, adapt, selectively improve Mew, or retain Mew's current design, with reasons.

Preserve Mew branding and intentional product choices, but do not use them as an excuse to reject genuinely better upstream UX. Do not blindly copy upstream screens or redesign unrelated areas. Ship cohesive, reviewed UI batches and verify screenshots/device behavior where possible.

### Full-history audit method

The 2,000+ commit range must be covered systematically. Walk release/tag checkpoints and commit ranges, group related changes by behavior and subsystem, and maintain a path/feature coverage ledger so changes are not silently skipped. Use commit subjects and changed paths only to discover candidates; inspect source, callers, tests and relevant history before deciding. For large or capped GitHub comparisons, use recursive tree inventories and smaller per-release/per-area comparisons. Track reviewed, deferred, blocked and not-yet-reviewed ranges separately. Do not report a percentage as complete unless the denominator and coverage evidence are explicit.

Audit both directions: upstream improvements that Mew lacks, and Mew improvements/overlays that should be retained or considered upstream-worthy. A parity audit is not an instruction to make Mew identical where Mew's behavior is intentionally better.

### Integration priority

Prioritize by user impact and risk: correctness/data safety/security; playback, subtitles and provider reliability; core navigation and high-traffic UI; performance and lifecycle; feature completeness; visual polish and lower-impact cleanup. Architecture improvements should be evaluated for measurable reliability/maintenance benefit and migration cost, not novelty.

No integration is automatic. First complete the semantic and UI/UX review, record the decision, then implement the smallest coherent change in the approved Mew development branch. Add regression tests where practical, inspect the final diff, and report exact verification. Preserve Mew-specific behavior unless a reviewed change explicitly improves it without unacceptable regressions.


## Player source fallback + stream-loading architecture audit — 2026-11-17

### Next-episode source fallback — upstream merge `ec726ddfd87392df0062fe03ac8cae24a2bff00a`

Upstream's next-episode auto-play previously resolved only the initially auto-selected stream. If a debrid result was expired, no longer cached or failed to resolve, it showed the failure and refreshed the stream list without trying the next eligible auto-selection candidate. The upstream fix introduces a reusable `resolveWithSourceFallback` runner, tracks source identity keys and attempts, passes the tried-source state into the episode switch, and restarts the fallback state for the new episode.

Mew Main-dev evidence:
- `PlayerNextEpisodeAutoPlay.kt` blob `f8019dc32d54db9d0052f4c0ec5b92a929d3c8cf`: resolves the selected stream once; on a failed debrid resolution it reports the error and forces a refresh. It does not pass tried-source metadata to the episode switch.
- `PlayerScreenRuntimeState.kt` blob `d9d236b1f76f271cc82cc0cd6583f4a5fbed95c6`: no equivalent fallback identity/attempt/job state.
- `PlayerSettingsRepository.kt` blob `3526a8a18e0d34e77c8ae9dfae1592ecdd73b216`: Mew has `streamAutoPlayNextEpisodeFallbackEnabled`, but not upstream's distinct `streamAutoPlayFallbackOnFailure` / max-source controls. Mew's existing next-episode fallback setting controls binge-group/provider selection; it is not equivalent to retrying another source after a debrid resolve failure.
- Upstream adds `PlayerSourceFallbackRunTest` and `PlayerSourceFallbackTest`. Coverage includes source identity de-duplication, attempt limits, disabled/manual/user-picked exclusions, engine-switch exclusion, no fallback after playback starts, and state reset across episode switches.

Decision: **ADAPT — high priority reliability fix**. Port the behavior contract, not the files wholesale. Mew's candidate selection must include its CloudStream, addon/plugin and server/P2P sources correctly; preserve Mew's existing binge-group preference, playback-engine switching, resume position, manual selection and download paths. Carry tried-source metadata only for an auto-selected stream, exclude duplicate torrent/info-hash identities, stop after the configured bound, and surface only the final failure. Add focused host tests for Mew's source identity and episode-switch lifecycle before enabling the behavior. No source code changed in this audit.

### Stream-source loader extraction — upstream architecture comparison

Upstream separates source discovery/target resolution and provider execution into `StreamSourceLoader.kt` (blob `440f67bef2e016db8c19d323e52964a406936fb7`), with `StreamsRepository.kt` at blob `2a96a0fa30033e8ca2d6519d27c3344fadd11a5b`. Mew's `StreamsRepository.kt` is blob `18dd4ddb2c4aa90128bd9a179ed487a4a507d331` and is substantially larger because it also owns Mew CloudStream discovery/route handling, P2P/server sources, diagnostics, request-generation protection and additional stream presentation/selection behavior.

Upstream's loader uses structured concurrency (`supervisorScope`), indexed result collection, a timeout and cancellation of unfinished source tasks. This is a useful separation pattern, but file size alone does not prove better architecture and the upstream loader is not a drop-in replacement for Mew's richer source graph.

Decision: **NEEDS INVESTIGATION / ADAPT candidate**. Trace every Mew caller and source type; compare cancellation, request-generation guards, cache behavior, ordering, timeouts, late-result handling, CloudStream routing, P2P/server behavior and error presentation. If extraction improves ownership/testability, split Mew's existing logic behind a Mew-aware loader without duplicating providers or changing source ordering/compatibility. No source code changed.

### Fresh range and branch evidence

- Upstream `1.0.0...enhanced`: 72 commits, 160 changed paths.
- Upstream `1.1.0-beta.1...enhanced`: 8 commits, 7 changed paths.
- Mew `upstream-sync` documentation head before this entry: `8c392a80c565c80884b7c10152344ba20e9e4fdd`.
- Mew `upstream-sync...Main-dev`: diverged; Main-dev is 603 commits ahead and 2,238 behind by commit graph, with merge base `f9ad843b14cd3be7fa3ddb800b6961233e0e5b56`. These are history counts, not missing-feature counts or a merge plan.
- The Mew branch source blobs listed above were fetched from the `Main-dev` ref. GitHub compare output did not expose a fresh Main-dev head SHA; do not use the historical head checkpoint for a write.
- No app source, tests, workflow, release or branch promotion changed. Builds/tests/CI/device verification: **NOT RUN**.


## Further upstream quality and feature triage — 2026-11-17

### TorBox alternative-hash cache responses — upstream `1118dfbf87de7d9e68860446ea553d48d06adcba`

Mew Main-dev already contains the upstream alias-resolution behavior in `features/debrid/LocalDebridService.kt` (blob `b4c39c6c402ce6d688c62260ae409c8a2536f16d`) and regression tests in `features/debrid/LocalDebridServiceTest.kt` (blob `8795235768f3ea482e80ff33aefc287b86d6f095`). It normalizes/deduplicates requested hashes, maps single alias responses to the requested hash, and retries only unresolved batch entries concurrently. Mew adds a stricter safety rule: if any unresolved retry fails, return `null` (availability unknown) rather than returning a partial map that could be interpreted as a cache miss.

Decision: **ALREADY IMPLEMENTED — Mew behavior is stricter; preserve it.** Tests exist for exact match, single alias, missing batch retry and failed retry/unknown availability. Test execution was not performed.

### Season batch downloads and storage management — upstream `48cbb901bf48a641a1270c2d10bbd4260e712e68`

This is a broad cross-platform feature set, not one isolated UI patch: season batch selection/quality, stream prefill, queue overview, storage statistics, network policy, offline show metadata, transfer resume/cleanup, multi-profile behavior, downloaded-show grouping and library integration. Mew Main-dev already has a substantial download domain: `DownloadsRepository.kt` (blob `5e5dfd0b0f6ad3959da8106c83a9a6ce7b4a86eb`) and `DownloadsScreen.kt` (blob `33bf5d80e3c5bfbab7d677a86200a1afa801fa46`) include active/completed items, pending source searches, show grouping and episode display.

Decision: **NEEDS INVESTIGATION — high priority feature/architecture audit.** Compare the full lifecycle and data contract before porting: queue admission/concurrency, source prefill, quality selection, cancellation, Wi-Fi/mobile-data guards, resume semantics, profile cleanup, offline metadata, storage accounting, platform worker behavior and UI states. Do not replace Mew's download repository or queue wholesale. Split this large upstream change into independently reviewable behavior groups.

### Home recomposition and startup performance — upstream `e5272769a6709d43aaecc3d7bbe8d66d5a200702`

Upstream adds Baseline Profile generation/artifacts, Compose stability configuration, mapped state helpers and Home/AppGate recomposition changes. Mew Main-dev `HomeScreen.kt` is a large Mew-specific composition with CloudStream, Cloud Library, profiles and provider-derived state; this is not a safe file replacement.

Decision: **NEEDS INVESTIGATION — performance candidate.** Compare the individual state/recomposition changes and stability annotations against Mew's actual state holders and callers. Require benchmark/profile evidence before adding Baseline Profile build infrastructure or changing dependencies. Preserve Mew's cloud/provider state semantics.

### Per-addon tracking event queues — upstream `8f5d9bcac3c2c0535b17b26cbdff392c10b8d767`

Upstream moves from one serial event-delivery loop to a queue per addon, preserving order within each addon while preventing one slow/unreachable addon from delaying other addons. It also avoids library/watched before-after lookups when no enabled addon declares the library resource, and adds URL/chunking tests.

Decision: **NEEDS INVESTIGATION.** The Mew Main-dev addon event dispatcher source path/callers were not conclusively located in this pass, so no equivalence or duplication claim is made. Locate the actual event owner and compare ordering guarantees, duplicate transport URLs, queue lifetime/bounds, timeout/cancellation, addon enable/disable behavior, event loss/backpressure and watched/library sync side effects before adapting.

### End-of-movie recommendation source — upstream `07de5b27795d2fe236ca917dc5608a594947b9d0`

Upstream resolves the configured More Like This source for movie end-card recommendations rather than always using the metadata object's default list.

Decision: **NEEDS INVESTIGATION.** Trace Mew's recommendation source settings, player end-card state and metadata cache/fallback semantics. Verify whether the selected provider/source is already applied at playback end; a similar recommendation list is not proof of equivalent source selection.

### Audit boundaries

This pass inspected the above upstream changes and Mew Main-dev files cited in each section. It did not complete the full download lifecycle, Home recomposition, addon event or end-card recommendation audits. No app source or tests changed. Builds/tests/CI/device verification: **NOT RUN**.


### Batch-download audit refinement — Mew primitives confirmed

A deeper read of Mew Main-dev `DownloadsModels.kt` (blob `df85a084657201d056c9f009015b427c5a26989a`) and `DownloadsRepository.kt` (blob `5e5dfd0b0f6ad3959da8106c83a9a6ce7b4a86eb`) narrows the previous finding:

- Mew already models `DownloadQueueMode.AllAtOnce/OneAtATime`, pending per-episode source searches, persisted download details snapshots and offline metadata reconstruction (`DownloadItem.toOfflineMetaDetails`).
- Mew repository exposes `enqueueFromStream`, pending source-search lifecycle, pause/resume/retry/cancel and offline metadata lookup. This is meaningful existing queue/domain behavior and must be retained.
- The upstream `BatchDownloadCoordinator.kt`, `BatchDownloadDialog.kt`, `DeviceStorageBar.kt`, `DownloadQueuePolicy.kt` and `OfflineShowMetadata.kt` have no same-path Mew equivalents on Main-dev. Mew's `DetailSeriesContent.kt` has per-episode download lookup/badges, but no season batch selection/source-plan dialog was found in the inspected file.

Refined decision: **ADAPT — UI/orchestration gap; architecture is partially implemented already.** Do not port upstream storage models or queue primitives wholesale. Build any batch-season flow on Mew's existing per-episode enqueue and pending-source-search contracts, and compare storage/network/profile cleanup separately. The earlier entry's broad **NEEDS INVESTIGATION** remains for the full cross-platform lifecycle; this refinement only establishes the missing batch UI/coordinator versus existing Mew primitives.


## Addon tracking events and download lifecycle — further comparison (2026-11-17)

### Addon player/library event delivery — upstream `8f5d9bcac3c2c0535b17b26cbdff392c10b8d767`

Upstream adds `AddonEventDispatcher` in `features/addons/AddonEvents.kt`. It routes player and library events to active manifests that declare the resource, deduplicates recipients by transport URL, preserves delivery order per transport using a dedicated queue, and lets slow addons fail independently under a 10-second timeout. Library video IDs are filtered, deduplicated and chunked in groups of 100. It also avoids before/after watched/library membership reads when no active addon declares the library resource.

Mew Main-dev evidence:
- `features/addons/AddonModels.kt` defines `AddonManifest.resources`, resource types/id prefixes and `ManagedAddon.isActive`; the domain can express the same manifest capability.
- `features/addons/AddonRepository.kt` owns profile-scoped addon installation, enabled state, manifest refresh and lifecycle. It is not an event-delivery queue.
- The inspected `features/library/LibraryRepository.kt` uses Mew's local library and tracking-provider membership model; its save/remove operations persist, publish and push to the configured server. No addon event dispatch was present in the inspected call paths.
- The inspected `features/watched/WatchedRepository.kt` persists watched state and syncs with tracking providers; its mark/unmark paths did not dispatch addon library events.
- There is no same-path `features/addons/AddonEvents.kt` implementation on Main-dev. Code-search results were empty, which is not a complete proof that no equivalent exists elsewhere.

Decision: **ADAPT — feature gap, subject to full caller audit.** Add delivery through Mew's active manifest/resource contract, retaining profile-generation safety and tracking-provider semantics. Before implementation, inspect every library/watched mutation entry point (including playback completion, bulk season actions, profile changes and server-backed membership changes), define which mutations should emit, and test URL encoding, 100-ID chunking, duplicate transport URLs, per-addon ordering, timeout isolation, addon disable/removal and cancellation. Avoid blocking local persistence on network delivery. Do not copy upstream's dispatcher wholesale until Mew's lifecycle/queue ownership is settled.

### Season batch downloads, queue policy and offline metadata — upstream `48cbb901bf48a641a1270c2d10bbd4260e712e68`

The upstream change is a coordinated download subsystem, not just a dialog:
- `BatchDownloadCoordinator.kt` builds a season/episode plan, excludes already downloaded/in-progress episodes, loads candidate streams with a shared semaphore (two concurrent source lookups) and a 30-second lookup timeout, and supports per-episode source override.
- `BatchDownloadDialog.kt` provides season chips, select-all/select-unwatched/deselect controls, episode readiness states, quality/size display and source override.
- `DownloadQueuePolicy.kt` enforces total-download, per-show and concurrent-show limits plus current network policy, then starts the oldest eligible queued item.
- `DownloadNetworkGuard` exposes transport state; Android uses `ConnectivityManager.NetworkCallback`, iOS uses `NWPathMonitor`. The setting to allow mobile data is device-level, not profile-synced.
- `OfflineShowMetadata.kt` stores bounded offline title/episode/cast metadata separately from frequently changing transfer progress.
- Platform download changes add queued status, network-aware scheduling, storage stats, unfinished-transfer cleanup, and platform-specific resume behavior. Android and iOS deliberately differ: Android range transfers can retain partial bytes; iOS resume data retains the cellular rule under which the transfer started.

Mew Main-dev evidence:
- `DownloadsModels.kt` has `DownloadQueueMode.AllAtOnce/OneAtATime`, persisted pending source searches, per-item details snapshots and offline metadata reconstruction. These are real existing contracts and must be preserved.
- `DownloadsRepository.enqueueFromStream` admits all-at-once downloads immediately, or starts a one-at-a-time item only when no transfer is active. `promoteWaitingDownload` starts the oldest waiting item after completion/failure. This is a basic serial queue, not upstream's bounded multi-dimensional admission policy.
- Mew's per-item `DownloadDetailsSnapshot` and `toOfflineMetaDetails` provide offline reconstruction, but the inspected model/repository do not establish upstream's separately persisted, merged, bounded per-show metadata store.
- Mew's inspected download screen already covers active/completed items, pending source searches, retry/pause/resume/cancel and grouped completed episodes. It is not equivalent to upstream's season planning/source selection flow.
- The upstream files `BatchDownloadCoordinator.kt`, `DownloadQueuePolicy.kt`, `DownloadNetworkGuard.kt`, `OfflineShowMetadata.kt` and `BatchDownloadDialog.kt` have no same-path Mew Main-dev equivalents. Attempts to fetch those exact Mew paths returned 404.
- Mew's existing downloader is platform-specific and has its own foreground/task lifecycle. Upstream's Android WorkManager/JobScheduler and iOS background URLSession changes must not be transplanted as a shared transport replacement.

Decision: **ADAPT — high-priority feature and policy gap; preserve Mew's download transport and persisted models.** A safe port should be split into:
1. Season planning/dialog and source prefill, reusing Mew's `enqueueFromStream` and pending-source-search flow.
2. Bounded queue admission, after deciding whether to retain the existing one-at-a-time option and how to represent queued state without breaking stored payload compatibility.
3. Device-level network policy with Android/iOS transport monitoring and explicit unknown/offline behavior.
4. Offline metadata store only if Mew's per-item snapshots prove insufficient for downloaded-show navigation and metadata updates.
5. Storage statistics and transfer cleanup, separately reviewed against Mew's chosen download directory, SAF/document URI support, partial files, profile isolation and platform lifecycle.

Do not implement this group as one wholesale port. Add policy/selector tests first; test migration of existing persisted download statuses and verify transfer cancellation/resume separately. UI review must cover the details-page entry point, season selection, no-source/partial-ready states, queue overview, storage display, mobile-data warning, accessibility and TV/large-screen behavior.

### Verification boundary

This comparison inspected upstream commit diffs and the named upstream/Mew source files on the refs recorded in this ledger. It did not complete all library/watched mutation callers, all downloader platform lifecycle paths, or the full batch-download UI review. No app source/tests/workflows changed in this batch. Builds/tests/CI/device verification: **NOT RUN**.


### Next-episode fallback — Mew source-identity and selector refinement

A deeper Main-dev read refines the earlier gap assessment:

- Mew already implements `StreamItem.playerSourceIdentityKey()` in `PlayerScreenRuntimeSourceActions.kt`. It derives identity from normalized P2P info-hash/file index, client-resolve metadata, video hash, direct URL, then a metadata fallback. `PlayerScreenRuntime` also tracks `activeSourceIdentityKey`. Preserve this Mew-aware identity implementation; do not add a duplicate helper.
- Upstream's identity helper additionally recognizes its `serverTarget` identity. Mew's server preparation is a separate path (`prepareServerForPlayer`); verify the server source identity contract before extending Mew's key.
- Mew's `StreamAutoPlaySelector.evaluateAutoPlayStream()` already returns an ordered `readyStreams` list after source-scope, addon/plugin filters, regex, exact-episode filename matching and binge-group preference. However, next-episode selection currently calls `selectAutoPlayStream()` and retains only the first candidate.
- Mew `PlayerScreenRuntime` has active source identity and next-episode preparation/search jobs, but no fallback tried-key set, bounded attempt counter, auto-selected-source marker or source-fallback job. Mew settings only expose `streamAutoPlayNextEpisodeFallbackEnabled` for binge-group/provider selection; no separate source-retry enable/max-source/startup-timeout settings were found in `PlayerSettingsRepository.kt`.
- The current next-episode path resolves the one selected source through `DirectDebridPlaybackResolver.resolveToPlayableStream`; on failure it surfaces the result and force-refreshes streams. It does not walk the selector's remaining eligible candidates.

Refined decision: **ADAPT — keep the existing Mew identity helper and ordered selector contract; add only the missing bounded retry state/runner and episode-switch propagation.** Candidate iteration must preserve Mew's current-provider-first and preferred-binge-group behavior, exact target episode matching, selected addon/plugin and regex constraints, and P2P/debrid eligibility. Exclude manual/user-picked streams, external playback and engine-switch paths; reset tried-source state on episode changes; prevent duplicate identities and cap attempts. The upstream runtime startup-timeout/error fallback is a separate, broader integration and needs its own review of Mew's playback error callbacks before porting.

No source code changed. Tests/build/CI/device verification: **NOT RUN**.


## Home performance and movie end-card source — further triage (2026-11-17)

### Home recomposition / startup — upstream `e5272769a6709d43aaecc3d7bbe8d66d5a200702`

Upstream combines several changes with different risk/benefit:
- `collectMappedAsState` maps state flows and applies `distinctUntilChanged`, so consumers subscribe to a smaller derived value rather than the full repository state.
- `HomeScreen` derives compact `HomeAddonSignals` (metadata-provider key/readiness, active-addon state and manifest error) instead of collecting the entire `AddonsUiState`. It also separates hero metadata loading from the composable state calculation.
- `MainAppContent` maps metadata settings to the one navigation flag it needs; `AppGate` preloads the brand wordmark.
- Android adds a Baseline Profile/benchmark module, generated profiles, stability configuration and supporting Gradle dependencies.

Mew Main-dev evidence:
- `features/home/HomeScreen.kt` (blob `eddaf8634ff85c00db51b7ab6b5617de32e68846`) currently collects the full `AddonRepository.uiState` with `collectAsStateWithLifecycle`, then derives enabled addons and manifests using `remember`. The full state is used for offline refresh, manual refresh, retry, provider readiness and home catalog refresh. This is a plausible recomposition optimization candidate.
- Mew's Home screen is substantially more fork-specific: it also collects CloudStream plugin state, Cloud Library, profile-scoped watch progress, tracking sources, smart shelves, server/library state and additional Mew-specific home sections. Do not replace it with upstream's file.
- Upstream's hero loading split is not a direct same-path port: Mew's inspected HomeScreen does not expose the same `rememberHomeHeroState` / `HomeHeroMetaLoadEffect` flow.
- Mew's inspected `gradle/libs.versions.toml` contains no Baseline Profile or benchmark plugin/version entries. No performance profile or benchmark evidence was collected for Mew in this audit.

Decision:
- **ADAPT candidate:** derive a compact, immutable Mew home-addon signal state with distinct equality, while preserving CloudStream/Cloud Library and all existing refresh/provider semantics. First inspect every consumer and add tests for signal equality/readiness transitions.
- **NEEDS INVESTIGATION:** hero loading/state ownership and metadata-settings collection across Mew's actual navigation root. The upstream wordmark preload is low-risk in principle but must be checked against Mew's actual branding/resource lifecycle.
- **NEEDS INVESTIGATION / evidence required:** Baseline Profile and stability configuration. Do not add a benchmark module or dependencies solely because upstream has them; first establish Mew's current build variants, supported Android min SDK, benchmark journey reliability and measurable startup/scroll benefit.

### Movie end-card recommendation source — upstream `07de5b27795d2fe236ca917dc5608a594947b9d0`

Upstream adds `MetaDetailsRepository.withPreferredMoreLikeThis` and re-resolves the configured recommendation source when movie playback metadata is bound. The end-card candidate list prefers this freshly selected list over the metadata object's original `moreLikeThis` value. This addresses stale/default recommendations when the preferred source differs from the metadata's initial source.

Mew Main-dev evidence:
- `features/details/MetaDetailsRepository.kt` already has `applyMoreLikeThisSource`, which consults current Trakt authentication, tracking settings and TMDB settings during metadata enrichment. It can choose Trakt-related items or retain/clear TMDB recommendations based on the current configuration.
- `MetaDetails` already carries `moreLikeThisSource` and `moreLikeThis`.
- The Mew player end-card candidate/effect owner was not conclusively located in this pass, so it is not proven that the player re-applies the currently selected source at playback end or that settings changes during playback are reflected.

Decision: **NEEDS INVESTIGATION — likely partial parity.** Trace the Mew movie end-card composable/state owner, when its metadata snapshot is captured, and whether settings/auth changes invalidate or refresh the recommendation list. If the player can retain stale recommendations after source settings change, adapt a lightweight current-source resolver with cancellation and fallback behavior. Do not duplicate metadata enrichment if the player already consumes a freshly enriched snapshot.

### Verification boundary

Inspected the upstream commit diffs and Mew Main-dev HomeScreen, MetaDetailsRepository, MetaDetails model, PlayerScreenRuntime state/effects and Gradle version catalog. No benchmark, build, test, CI or device run was performed. No app source changed.


## Newer upstream bug-fix parity pass — 2026-11-17

### Episode artwork fallback after image-load failure — upstream `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`

Upstream changed both horizontal and list episode cards to retain an image-load error state and fall back from the episode thumbnail to series background/poster.

Mew Main-dev `features/details/components/DetailSeriesContent.kt` already contains `rememberEpisodeImageState(thumbnail, fallbackImage)`, passes `onError` from both episode card variants, and switches to the series fallback after the primary image fails.

Decision: **ALREADY IMPLEMENTED.** The Mew equivalent is present in both UI variants. Preserve it; no duplicate change needed. Runtime/device behavior not tested in this audit.

### Home catalog sync-before-load race — upstream `0b9d93599f7d7603e7814a99a70aa64d7c48e086`

Upstream preserves local preferences when the remote sync arrives before the known catalog keys are loaded, by treating an empty known-key set as a temporary incomplete catalog list.

Mew Main-dev `features/home/HomeCatalogSettingsRepository.kt` already contains the same guard in its preference merge: `knownKeys.isEmpty() || key in knownKeys || key.requiresExplicitSyncKey()`.

Decision: **ALREADY IMPLEMENTED.** Mew already protects the exact startup race. Do not re-port.

### iOS styled subtitles after next-episode transition — upstream `c5298633914d967792c79351754ba47d54dfd242`

Upstream fixes a Compose state-lifetime bug by keeping `subtitleText` across source URL changes and explicitly clearing it in `LaunchedEffect(sourceUrl)`, while leaving `subtitleStyle` intact.

Mew Main-dev `iosMain/features/player/PlayerEngine.ios.kt` has a materially different player bridge/engine implementation: the inspected file has no matching `subtitleText` / `subtitleStyle` Compose state and configures the bridge through source-keyed effects. This is not a same-line port.

Decision: **INTENTIONAL PARALLEL / NEEDS INVESTIGATION.** Trace subtitle style ownership in Mew's iOS bridge and the common player settings/state. Reproduce the upstream scenario (styled subtitle enabled, next episode/source changes) on iOS before deciding whether Mew has the same bug. Do not add a second subtitle state holder.

### iOS floating tab bar blur — upstream `c314dd48d622de249eebbea7e531e3f7b2e506d4`

Upstream consolidates top/bottom native bar appearance cleanup and clears the iOS 26 scroll-edge blur behind the floating tab pill, without changing the pill itself.

Mew's `iosApp/iosApp/ContentView.swift` does not expose the same `removeBarBlur` / UIKit tab-bar appearance helper in the inspected source. Mew's native navigation integration and tab implementation differ.

Decision: **NEEDS INVESTIGATION — iOS-only visual parity.** Inspect the actual Mew tab controller hierarchy and iOS 26 appearance on device/simulator before porting UIKit/SwiftUI appearance changes. Avoid copying upstream's native bar patch into a different host hierarchy. Device verification not performed.

### MDBList episode duration projection — upstream `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb`

Upstream prevents an episode from inheriting the parent show's runtime when creating an MDBList watch-progress projection; only movie entries may use the media-level runtime fallback. A regression test covers a show runtime incorrectly assigned to an episode.

The exact `features/mdblist/MdbListProgressProjection.kt` path does not exist on Mew Main-dev (404), so equivalent projection behavior has not been located.

Decision: **NEEDS INVESTIGATION — data correctness.** Find Mew's MDBList progress mapping and verify episode duration is sourced only from episode runtime or known episode metadata, never the show's aggregate runtime. Add a regression test with a deliberately large show runtime and a missing episode runtime. No conclusion about current Mew correctness yet.

### Detail hero mute/back-button alignment — upstream `f28929d3e3cc0d354de1dd921efc539bbf8a14cf`

Upstream adds a native navigation-bar button anchor from UIKit to align the hero mute control with the actual back button, including physical insets and RTL-aware trailing geometry. Android returns no native anchor; the Compose fallback remains.

Mew Main-dev `features/details/components/DetailHero.kt` currently uses status-bar inset plus fixed end padding for the mute control and has no `platformNativeBarButtonAnchor` contract in `core/ui/PlatformInsets.kt`.

Decision: **NEEDS INVESTIGATION — targeted iOS UI polish.** Inspect Mew's custom/native navigation modes, RTL layout and safe-area geometry on device/simulator. If the mute control visibly misaligns with the native back button, adapt an anchor to Mew's actual navigation host. Do not add an unused expect/actual API.

### Simkl related-title fallback and MDBList duration — upstream `b6ae11b8be1d2b6ff702ca8163143e06c5902810`

Upstream keeps trying Trakt/TMDB when Simkl returns no related titles rather than returning an empty Simkl selection. Mew's `MetaDetailsRepository.applyMoreLikeThisSource` already contains the source selection/enrichment policy, but this specific empty-Simkl fallback path was not fully traced in the inspected range.

Decision: **NEEDS INVESTIGATION.** Test empty Simkl response, Simkl API failure, non-empty Simkl response, and configured TMDB/Trakt fallback in Mew. Preserve configured-source priority and avoid duplicate metadata requests.

### Localization cleanup — upstream `106e67facd528dbd923b4ec0a087404e51372337`

Upstream removes two duplicated Brazilian Portuguese string resources introduced by a merge. Mew's resource path/content was not compared in this pass.

Decision: **NEEDS INVESTIGATION — low risk, broad audit.** Compare resource keys across all Mew locale files and check duplicate XML names/build resource validation before cleanup. Do not remove strings based only on upstream's duplicate state.

### Mew verification boundary

Mew Main-dev was checked for episode artwork fallback, home catalog merge, iOS player subtitle state, native tab-bar appearance and hero mute anchoring. MDBList projection path and complete Simkl fallback call chain remain unlocated/unverified. No app source changed. Tests/build/CI/device verification: **NOT RUN**.


## iOS parity audit — static source review (2026-11-17)

Scope: compare upstream `enhanced` at `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03` with Mew `Main-dev` at `69b4a96072b506f60dcbe538c5f6b71db3a4a464`. Mew has no Apple device available for user-side testing, so this pass prioritizes source-level semantic review and marks simulator/device-only behavior explicitly. No iOS build or simulator run was performed.

### iOS subtitle state after episode/source transition — upstream `c5298633914d967792c79351754ba47d54dfd242`

- Upstream fix changes `subtitleText` from `remember(sourceUrl)` to stable `remember`, clears it explicitly in `LaunchedEffect(sourceUrl)`, and keeps `subtitleStyle` alive. This is specific to its Compose-rendered `StyledSubtitleOverlay` and subtitle-text callback.
- Mew `composeApp/src/iosMain/kotlin/com/nuvio/app/features/player/PlayerEngine.ios.kt` (blob `288d8efa1115f395f934e6c9a3bbe64dab610819`) has no `subtitleText`, `subtitleStyle` Compose state, subtitle-text handler or `StyledSubtitleOverlay`. It applies style directly through `bridge.applySubtitleStyle(...)` and loads source changes through `bridge.loadFileWithAudio(...)`.
- Decision: **INTENTIONAL PARALLEL / NEEDS INVESTIGATION**. Do not copy the overlay state patch. Trace whether the player runtime reapplies the persisted style after source load and whether mpv resets subtitle options on `loadFileWithAudio`; add a source-transition regression test at the bridge/state boundary if testable. Simulator/device visual confirmation remains required, but is not a blocker for static fixes.

### iOS 26 tab-bar blur — upstream `c314dd48d622de249eebbea7e531e3f7b2e506d4`

- Upstream's `removeBarBlur` configures both navigation and tab bar appearances, hides iOS 26 scroll-edge blur, and adds SwiftUI `toolbarBackground(.hidden, for: .tabBar)` to its SwiftUI `TabView` pages.
- Mew `iosApp/iosApp/ContentView.swift` (blob `10b53e63a9d31f30c5a4ac0b0590af929a9380a9`) uses a custom `RootComposeViewController` with a manually embedded UIKit `UITabBar`, not the same SwiftUI `TabView`/tab-controller hierarchy. Mew's `applyNativeTabBarAppearance()` copies the current system appearance and changes icon/title tints; it does not explicitly clear the background effect.
- Decision: **ADAPT candidate / NEEDS INVESTIGATION**. SwiftUI toolbar modifiers are inapplicable. Review Mew's Liquid Glass/native-tab preference and whether its custom UIKit bar actually produces the unwanted full-width blur on iOS 26. If so, adapt a transparent `UITabBarAppearance` in Mew's existing appearance path without changing the selected tab style or profile-avatar overlay. Do not blindly remove blur from every mode.

### Detail hero native back-button anchor — upstream `f28929d3e3cc0d354de1dd921efc539bbf8a14cf`

- Upstream reads the native `UINavigationBar` geometry through `LocalUIViewController` and uses it to align the hero mute button.
- Mew's `RootComposeViewController` embeds the Compose `MainViewController` and manages a separate UIKit tab bar; Mew `DetailHero.kt` uses Compose status-bar insets and its own navigation chrome. No matching native navigation-bar anchor contract was found in Mew's iOS inset actual.
- Decision: **SKIP this exact implementation** while Mew uses Compose navigation chrome. Revisit only if Mew adds a UIKit navigation bar around detail routes. Do not add an unused expect/actual API.

### iOS download network policy — upstream batch-download commit `48cbb901bf48a641a1270c2d10bbd4260e712e68`

- Upstream adds common `DownloadNetworkGuard` and queue policy plus iOS `NWPathMonitor`. The monitor initially reports unavailable, then allows the queue only when the path is satisfied and not expensive; device-level `allowMobileDataDownloads` defaults to false and is stored outside profile sync.
- Mew has no matching common/iOS `DownloadNetworkGuard`, no `isDownloadNetworkAllowed` policy and no device-level mobile-data download setting in the inspected download repository/models. `DownloadsRepository` starts waiting items through its own `promoteWaitingDownload` queue without network gating. Mew's iOS downloader explicitly sets `allowsCellularAccess = true`, `allowsExpensiveNetworkAccess = true` and `allowsConstrainedNetworkAccess = true` on its default URLSession configuration.
- Decision: **ADAPT — high-priority data-safety and parity gap**. Add a device-level opt-in and platform network monitor, then gate new queue admissions while preserving Mew's `DownloadQueueMode`, `DownloadStatus.Waiting`, persisted queue ordering and current transfer semantics. The shared expect/actual policy needs an Android implementation too; do not add an iOS-only switch that leaves common queue behavior inconsistent. Test unknown/no-network, Wi-Fi, cellular, explicit mobile-data opt-in, transitions while queued, and transitions while active.

### iOS downloader lifecycle — intentional transport difference

- Mew `DownloadsPlatformDownloader.ios.kt` (blob `1aa24cba6e141ec3acb39131805af89262087f7a`) uses `NSURLSessionDataDelegate`, streams bytes into a `.part` file, resumes with HTTP Range requests, and calls `DownloadsRepository.pauseActiveDownloads()` when the app backgrounds. It uses a default URLSession, not an NSURLSession background download task.
- Upstream uses a background URLSession with `NSURLSessionDownloadTask`, stores resume data, and reapplies the cellular rule to resumed tasks. These have different callback, cancellation, partial-file, background execution and resume contracts.
- Mew already excludes its downloads directory from iCloud/computer backups using `NSURLIsExcludedFromBackupKey` via `excludeFromBackupOnce`.
- Decision: **INTENTIONAL PARALLEL** for the transport; **ADAPT / NEEDS INVESTIGATION** for background-transfer capability. Preserve Mew's range-resume and `.part` lifecycle unless a separately designed migration proves safe. Audit profile changes, cancellation, app termination, HTTP 200/206/416, signed headers, and background relaunch before considering background URLSession adoption. Backup exclusion is **ALREADY IMPLEMENTED**; do not duplicate it.

### iOS storage statistics and download status surface

- Upstream adds `DownloadsStorageStats.ios.kt` using `NSFileManager.attributesOfFileSystemForPath` to expose total/free storage, with a common storage-stats contract and UI bar.
- Mew has no matching `DownloadsStorageStats` / `DownloadsStoragePlatform` source or storage indicator in the inspected `DownloadsScreen.kt`.
- Decision: **ADAPT — low-risk feature gap**, after the common contract and Android actual are inspected. Treat filesystem-stat failures as unknown/zero without blocking downloads; avoid showing misleading capacity when the API fails.
- Mew's iOS live-status payload already carries provider, stream title, artwork, speed and ETA in addition to progress; upstream's corresponding payload is simpler. Preserve Mew's richer status surface and its `Waiting` status semantics.

### MDBList and Simkl correctness fixes — applicability refined

- Upstream `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb` fixes MDBList watch-progress projection so an episode cannot inherit a show's runtime. Mew's inspected `MdbListMetadataService.kt` only fetches/merges external ratings; Mew's `MetaDetailsRepository.kt` uses that ratings enrichment and no MDBList progress-projection path was found. Decision: **NOT APPLICABLE to current Mew behavior**; do not create a projection solely to port this guard. Revisit if MDBList watch-progress sync is introduced.
- Upstream `b6ae11b8be1d2b6ff702ca8163143e06c5902810` falls back from empty Simkl related titles to Trakt/TMDB. Mew's inspected `MetaDetailsRepository.kt` has Trakt/TMDB related-title selection but no Simkl source path. Decision: **NOT APPLICABLE to current Mew behavior**; preserve existing Trakt/TMDB priority and revisit if Simkl recommendations are added.

### iOS batch audit verification boundary

Inspected upstream commit diffs and Mew Main-dev iOS player, insets, hero, native tab host, download repository/models, iOS downloader, iOS storage and account cleaner. This is a targeted source audit, not a complete iOS audit. The account-wipe call chain, subtitle style reapplication after source load, iOS native-tab visual mode, background-session feasibility, and storage-stat common/Android contracts remain open. No app source changed. iOS build, tests, CI, simulator and device verification: **NOT RUN**.

### Latest verified refs for this audit entry

- Mew `Main-dev`: `69b4a96072b506f60dcbe538c5f6b71db3a4a464`.
- Mew `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (unchanged).
- Mew `upstream-sync` before this documentation update: `8fd054a1a036c0a18d33f0304583796e429980cf`.
- Upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- App source changes: **0**. Documentation-only update on `upstream-sync`.


### iOS download account-wipe parity — deeper caller audit (2026-11-17)

This follow-up closes part of the earlier account-wipe investigation:

- Upstream commit `48cbb901bf48a641a1270c2d10bbd4260e712e68` adds `DownloadsRepository.clearLocalState()` to the common account wipe and `DownloadsStorage.clearUnfinishedDownloads()` to the iOS platform cleaner. The iOS storage implementation iterates all profile-scoped download payloads, removes each unfinished item's `.part` file through `DownloadsPlatformDownloader.removePartialFile`, and rewrites the payload with completed items only. The common clear cancels active handles and resets in-memory download state without writing to the now-reset active profile.
- Mew Main-dev `LocalAccountDataCleaner.wipe()` does **not** call `DownloadsRepository.clearLocalState()`; its common imports/call list contain no download cleanup. Mew `PlatformLocalAccountDataCleaner.ios.kt` does not clear download payloads or partial files. Its key lists omit `downloads_payload`; Mew `DownloadsStorage.ios.kt` stores profile-scoped `downloads_payload` and has no `clearUnfinishedDownloads` operation.
- Mew's Android platform account cleaner also does not clear the downloads SharedPreferences; the common account wipe omission therefore affects both platforms, not only iOS.
- Mew's iOS downloader already exposes `removePartialFile(destinationFileName)`, so the platform has the primitive needed to delete unfinished `.part` files. The common repository `clearLocalState()` already cancels active handles and resets in-memory state, but no account-wipe caller invokes it.

Decision: **ADAPT — HIGH PRIORITY account-wipe/data-isolation regression.** Port the common in-memory cancellation and a profile-wide storage cleanup, including pending source-search records and partial files. Preserve completed offline media only if that is the intended product policy; before adopting upstream's keep-completed behavior, review Mew's completed item payload for retained source URLs/headers and account-bound metadata. Do not simply delete the entire downloads directory or clear all profile payloads without deciding whether completed local media should remain. Add tests for active-handle cancellation, all-profile unfinished record removal, pending-search removal, completed-download retention policy and partial-file cleanup on Android/iOS.

Verification boundary: static call-chain/source inspection only. No account-wipe runtime test, iOS simulator, Android device, build or CI run. This finding supersedes the earlier note that the account-wipe call chain was wholly open; the missing cleanup call is confirmed, while the completed-download retention policy remains open.
