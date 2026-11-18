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


## Complete iOS path inventory for 1.0.0 → enhanced — 2026-11-17

GitHub compare reports 72 upstream commits and 160 changed paths in this range. **14 changed paths are iOS-specific** (10 `iosMain`, 4 iOS app/config/Swift). This inventory records the iOS-side decision for every changed path; common-code download and UI changes are covered in the earlier sections.

| Upstream path | Mew finding / decision |
|---|---|
| `core/storage/PlatformLocalAccountDataCleaner.ios.kt` | **ADAPT — HIGH PRIORITY**. Mew's account wipe omits download-state cancellation and unfinished-record/partial-file cleanup; see the deeper account-wipe audit above. |
| `core/ui/PlatformInsets.ios.kt` | Upstream adds a UIKit navigation-bar anchor. **SKIP exact port** while Mew uses Compose navigation chrome and a separate UIKit tab bar. |
| `features/downloads/DownloadNetworkGuard.ios.kt` | **ADAPT — HIGH PRIORITY** with common queue policy and device-level opt-in; no Mew equivalent. |
| `features/downloads/DownloadsLiveStatusPlatform.ios.kt` | **ALREADY IMPLEMENTED / Mew richer**: Mew already publishes provider, stream title, artwork, speed and ETA; keep Mew's `Waiting` state contract. |
| `features/downloads/DownloadsPlatformDownloader.ios.kt` | **INTENTIONAL PARALLEL** transport: Mew uses data delegate + `.part` + Range resume; upstream uses background download tasks + resume data. Audit lifecycle before any migration. |
| `features/downloads/DownloadsSettingsStorage.ios.kt` | **ADAPT candidate** only as part of the device-level download settings/network-policy feature; Mew has no matching platform settings store/repository. |
| `features/downloads/DownloadsStorage.ios.kt` | **ADAPT — HIGH PRIORITY** unfinished-download cleanup; Mew stores profile-scoped payloads but has no cleanup API. |
| `features/downloads/DownloadsStorageStats.ios.kt` | **ADAPT candidate**: Mew has no storage-stat actual or storage indicator. |
| `features/player/PlayerEngine.ios.kt` | **INTENTIONAL PARALLEL**: upstream's overlay subtitle state-lifetime fix does not map to Mew's direct mpv bridge style path. Reapply-style-after-load still needs verification. |
| `features/settings/SettingsConfigFileLauncher.ios.kt` | **NOT APPLICABLE currently**: Mew has no matching common settings-config transfer contract or iOS document-picker actual. Revisit if settings backup/import is introduced. |
| `iosApp/Configuration/Version.xcconfig` | **SKIP**: upstream release version/build moves from 1.0.0/157 to 1.1.0-beta.1/158; Mew intentionally owns version 0.5.1/120. |
| `iosApp/iosApp/ContentView.swift` | **ADAPT candidate / NEEDS INVESTIGATION**: Mew has a custom UIKit `UITabBar`, not upstream's SwiftUI tab hierarchy. Check iOS 26 blur without breaking Mew's Liquid Glass/native-tab preference. |
| `iosApp/iosApp/Info.plist` | Upstream adds `uk` and `zh` to its existing `CFBundleLocalizations`. Mew has no `CFBundleLocalizations` key. **NEEDS INVESTIGATION**: compare actual Mew Compose resource locales and Xcode known regions before adding a complete Mew-owned locale list. |
| `iosApp/iosApp/Player/MPVPlayerBridge.swift` | Upstream changes styled overlay cue text from `sub-text` to `sub-text/ass` with fallback. Mew's bridge has no `styled` overlay / `sub-text/ass` path and uses native mpv subtitle rendering. **INTENTIONAL PARALLEL**; do not copy the one-line change without adopting the overlay contract. |

### Additional iOS source comparison notes

- Upstream `SettingsConfigFileLauncher.ios.kt` changes the document-picker export initializer from `forExportingURLs/asCopy` to `uRL/inMode: ExportToService`. This is not a standalone bug fix to port because Mew currently lacks the settings-config export/import feature itself.
- The upstream `MPVPlayerBridge.swift` change is a subtitle overlay cue-source correction, not proof of a general MetalLayer fix. The commit subject must not substitute for reading the actual patch.
- Mew's `Info.plist` retains Mew-specific display name, URL schemes and alternate icons. Do not replace it with upstream's plist. Only the supported-localization declaration needs a separate Mew resource audit.
- Mew's iOS downloader already excludes its media directory from backup. Its richer live-status payload is a Mew advantage and must not be lost during integration.

This inventory is based on the complete 160-path compare response (14 iOS-specific paths) and direct source reads for the Mew equivalents that exist. It does not claim every common-code caller or every iOS runtime path has been audited.


## Localization parity audit — 2026-11-17

Compared Mew `Main-dev` against upstream `enhanced` for the default, French, Brazilian Portuguese, Indonesian (`id`) and legacy Indonesian (`in`) Compose resource files. Counts are unique named `string`, `plurals` and `string-array` resources; all five files on both repositories have **zero duplicate resource names** in this static check.

| Locale resource | Mew keys | Mew coverage vs Mew default | Upstream keys | Upstream coverage vs upstream default |
|---|---:|---:|---:|---:|
| Default `values` | 2,933 | 100% | 3,162 | 100% |
| French `values-fr` | 2,080 | 71% | 2,529 | 80% |
| Brazilian Portuguese `values-pt-rBR` | 1,560 | 53% | 2,953 | 93% |
| Indonesian `values-id` | 1,559 | 53% | 3,162 | 100% |
| Legacy Indonesian `values-in` | 1,559 | 53% | 3,162 | 100% |

Mew's base resource set has 2,933 keys; upstream has 3,162, with 2,189 keys shared. Relative to Mew's base, 856 French, 1,374 pt-BR and 1,374 Indonesian keys are missing from their respective locale files. Of those missing Mew translations, upstream has translations for 203 French, 812 pt-BR and 745 Indonesian keys. This gives a concrete translation backlog, but not every upstream-only key belongs in Mew: some relate to upstream-only screens/features and must be filtered by Mew key usage/callers first.

### Translation semantic checks

- Comparing shared translated keys shows 552 French, 408 pt-BR and 40 Indonesian values differ between Mew and upstream. Some are expected Mew wording/branding (e.g. Mew vs Nuvio); others are stylistic or format differences. Do not wholesale replace locale files.
- Mew `values-id` and `values-in` contain the same 1,559 resource names; the key sets are identical. The files are separate Android resource qualifiers, so treat this as a **LEGACY ALIAS / INTENTIONAL PARALLEL**, not a duplicate-key defect. Preserve both until supported locale resolution and minimum Android/resource compatibility are verified.
- Upstream commit `106e67facd528dbd923b4ec0a087404e51372337` removes two duplicated pt-BR resources from an upstream merge. Mew's current pt-BR file has no duplicate names, so that exact cleanup is **ALREADY IMPLEMENTED / NOT NEEDED**.
- Mew iOS Xcode project declares `developmentRegion = en` and `knownRegions = (en, Base)`; its Info.plist has no `CFBundleLocalizations`. Compose resources are bundled through the shared app, so do not add plist localization declarations solely to mirror upstream. Audit the generated iOS bundle/resource localization behavior and actual supported Mew locales before changing Xcode metadata.

### Decision

**ADAPT — translation completeness backlog, medium priority.** Start with Mew-owned strings that are already used by Mew screens and whose upstream translations can be safely adapted. Preserve Mew branding, product terminology, placeholders, plurals, punctuation and resource-format semantics. Exclude upstream-only feature keys until their feature is accepted. Run XML/resource validation and common/iOS/Android compilation after any translation batch. This audit made no resource-file changes.

Verification: source-level key extraction and duplicate-name scan only; no XML parser/build/lint/test/CI/iOS simulator/device run. Branch refs at audit time: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`, `main` `207ef8fa309b8061b73039b2d4e4bde4af65050e`, upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.


## Home performance and loading UX audit — 2026-11-17

Compared upstream commit `e5272769a6709d43aaecc3d7bbe8d66d5a200702` with Mew Main-dev. The upstream change is a performance-focused group: mapped/distinct state collection, smaller home addon signals, separated hero metadata loading, a retryable addon-manifest error state, wordmark preloading, Compose stability configuration, and an Android Baseline Profile/benchmark module.

### Home addon state collection — ADAPT candidate

- Mew `HomeScreen.kt` collects the full `AddonRepository.uiState` using `collectAsStateWithLifecycle()`, then derives `enabledAddons`, metadata-provider keys and readiness keys with `remember`. The state is referenced by refresh/offline-recovery paths as well as home composition.
- Upstream adds `collectMappedAsState` (map + `distinctUntilChanged`) and projects only `HomeAddonSignals`: metadata-provider key, readiness key, whether any manifest is active, whether manifests are loading, and first enabled-manifest error.
- Mew has additional semantics absent from upstream: CloudStream providers participate in `hasActiveAddons`; Mew-specific Nuvio Enhanced settings, Cloud Library, profiles and other home projections remain active. The upstream projection cannot be copied unchanged.
- Decision: **ADAPT — medium/high performance candidate**. Create a Mew-owned compact signal including all fields consumed by Mew's home and retain CloudStream state separately. Ensure the mapped signal has structural equality and no expensive work per emission. Keep refresh actions reading the current repository state to avoid stale closures. Measure recomposition/startup before and after; add tests for enabled/disabled addon, manifest refresh/error, CloudStream-only home, profile switch and offline recovery.

### Manifest failure empty state — ADAPT candidate / UI review required

- Upstream exposes `firstEnabledManifestError()` through `HomeAddonSignals` and, when there are no renderable sources, shows a retryable `HomeEmptyStateCard` or offline card. Retry refreshes network status and all addons.
- Mew's inspected home path derives active/loading state but has no equivalent `addonManifestErrorMessage` projection in `HomeScreen.kt`. Mew has CloudStream provider errors and Mew-specific source states, so upstream's single error card is not directly transferable.
- Decision: **ADAPT / NEEDS UI INVESTIGATION**. Review Mew's existing no-catalog, offline, CloudStream failure and addon refresh states before adding an error card. Avoid replacing a meaningful empty state with an addon-only error when CloudStream/server/collection content remains usable. Retry must preserve the user's enabled provider/source selection.

### Hero metadata loading separation — NEEDS INVESTIGATION

- Upstream moves the metadata fetch side effect into `HomeHeroMetaLoadEffect(items)`, separate from `rememberHomeHeroState(items)`, so settings changes used for metadata loading do not automatically make the hero presentation state depend on all metadata settings.
- Mew's checked path does not have the same `HomeHeroMetaRepository.kt` source at the upstream path, so this is not a direct file port. The Mew hero metadata owner and its settings invalidation/caching semantics must be traced before changing composition boundaries.
- Decision: **NEEDS INVESTIGATION**; preserve Mew's hero metadata, CloudStream and profile behavior. No guessed new repository/helper.

### Android Baseline Profile and Compose stability config — ADAPT candidate

- Upstream adds a dedicated `:baselineprofile` instrumentation module, benchmark build type, generated startup/steady-state profile, and Compose stability config for collection interfaces. This is Android-only; it does not improve iOS startup directly.
- Mew's inspected `settings.gradle.kts`/version catalog do not declare the baseline-profile plugin/module/dependencies. No Mew baseline-profile generation or macrobenchmark setup was found in those inspected files.
- Decision: **ADAPT — medium priority after performance baseline**. First inspect Mew's build variants, product flavors, app IDs/signing, existing Android instrumentation workflows and test navigation selectors. Reuse Mew-specific launch journeys (home, catalog, details, player) and avoid upstream package IDs or generated profiles. Profile generation requires a supported Android device/emulator; until run, report as NOT RUN. Compose stability config must be justified by compiler metrics, not copied as a blanket fix.

### Brand wordmark preload — INTENTIONAL PARALLEL

- Upstream preloads/caches a Compose resource bitmap for its own wordmark and uses a bitmap painter.
- Mew's `AppBrandWordmark.kt` deliberately renders its Mew-owned SVG through Coil's SVG decoder; its source comment explains the Compose SVG painter incompatibility on Android. Replacing it with upstream's `painterResource`/bitmap cache risks breaking the SVG asset path or branding.
- Decision: **INTENTIONAL PARALLEL**. If cold-start profiling shows wordmark decode is material, optimize through the existing Coil/resource pipeline and preserve the Mew SVG. Do not port upstream's asset/painter implementation.

### Home audit classification summary

- **ADAPT:** compact addon state projection; baseline profiles after measurement/build audit; carefully scoped retryable manifest-error UX.
- **NEEDS INVESTIGATION:** hero metadata effect/state separation and Mew-specific no-source error-state interactions.
- **INTENTIONAL PARALLEL:** Mew SVG/Coil wordmark rendering; Mew's additional CloudStream/Cloud Library/profile home behavior.
- App source changes: **0**. No builds, benchmarks, UI tests, CI or device/simulator runs were performed in this audit batch.


### iOS 26 native Liquid Glass tab-bar blur — static hierarchy review update

The earlier candidate is now narrowed by direct Mew source inspection:

- Mew `RootComposeViewController` creates a standalone `UITabBar()` (not a `UITabBarController`), pins it full-width to the view's leading/trailing/bottom anchors, and shows it only on iPhone/iOS 26+ when both `NuvioLiquidGlassNativeTabBarEnabled` and `NuvioNativeTabBarVisible` are true.
- `applyNativeTabBarAppearance()` copies `tabBar.standardAppearance`, changes icon/title colors for stacked/inline/compact layouts, then assigns the same appearance to `standardAppearance` and `scrollEdgeAppearance`. It does not clear `backgroundEffect`, `backgroundColor` or `shadowColor`.
- Upstream commit `c314dd48d622de249eebbea7e531e3f7b2e506d4` configures a transparent tab-bar appearance, clears effect/background/shadow, applies it to the bar and the tab's root `UITabBarItem`, and hides iOS 26 bottom scroll-edge effects. Its SwiftUI `toolbarBackground` modifiers and `UITabBarController` traversal do not map to Mew's standalone UIKit bar.

Decision: **ADAPT — targeted iOS 26 UI candidate, not yet implemented**. If Mew's Liquid Glass mode shows the same full-width blurred band, apply the transparent appearance inside Mew's existing `applyNativeTabBarAppearance()` only while the Liquid Glass native bar is enabled; keep the current tint/title configuration and profile-avatar overlay. Do not alter the non-Liquid-Glass mode or hide the system floating pill. The exact UIKit property interactions and visual result require an iOS 26 simulator/device check; with no Apple device available, mark **DEVICE VERIFICATION REQUIRED** and seek macOS CI/simulator coverage. No UI source changed in this pass.


### Download network guard duplicate audit — Mew existing network status is different semantics

Follow-up inspected Mew `composeApp/src/commonMain/kotlin/com/nuvio/app/core/network/NetworkStatusRepository.kt` (blob `bb270c98d0b1cba878769250ecefe1c3672dd960`). It performs HTTP reachability probes against a public endpoint and app server checks, exposing `Unknown/Checking/Online/NoInternet/ServersUnreachable`. It does not observe the OS network path, distinguish Wi-Fi from cellular, report expensive/constrained paths, or gate the download queue.

Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**, not a true duplicate. Keep user-facing internet/server reachability and download transport-cost policy separate. A new download guard may consume network reachability as an additional signal, but must not infer Wi-Fi/cellular from `NetworkStatusRepository` or replace its existing probes. On iOS use `NWPathMonitor` for path satisfaction/expensive/constrained/cellular; on Android use the platform connectivity capabilities. Queue policy should fail closed while path state is unknown if mobile data is not explicitly allowed, and must define whether an already-active transfer pauses when the path becomes expensive. This detail is now part of the high-priority network-policy adaptation plan.


### Mew Home compact-signal port — caller/side-effect audit refinement

Further caller tracing shows why the upstream HomeAddonSignals cannot replace Mew's entire addon state flow as-is:

- Mew's full enabledAddons list is used to build metaProviderKey and metaProviderReadinessKey, compute refresh state, build the catalog refresh signature, call HomeCatalogSettingsRepository.syncCatalogs(enabledAddons), and call HomeRepository.refresh(enabledAddons). There are also retry/refresh callbacks that read the latest repository state.
- The upstream compact projection only contains the five home signals needed by its own catalog/home flow; Mew has different catalog-sync side effects and additional CloudStream provider participation.
- Replacing Mew's full state with the upstream projection would risk suppressing a catalog refresh when an addon catalog/resource/transport changes but the projected signal remains equal.

Decision refinement: ADAPT the collection/recomposition boundary, not the source-of-truth data contract. Keep current AddonRepository as the source of truth. Before implementing, identify the exact fields used by buildHomeCatalogRefreshSignature and HomeCatalogSettingsRepository.syncCatalogs; split narrow UI-only signals from a catalog-refresh signature that changes for every relevant catalog/resource mutation. Trigger side effects from a distinct key and read current repository values at execution time. Preserve CloudStream registry revision and all profile/visibility dependencies. No code change until that field/caller audit and tests are complete.


### Upstream delta audit — 2026-11-17 (five commits after common ancestor)

Compared Mew `upstream-sync` at `ea7dc36d6b83689459516b0a19bec59e95b939ce` with upstream `enhanced` at `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. The merge base is `f4b7e55e31c2778827095dbdb246193e8675016f`. GitHub comparison reports five upstream-only commits; the 69 opposite-side count is Mew-only history, not 69 pending upstream commits.

- `c529863` iOS styled subtitle state across episode changes — **ALREADY IMPLEMENTED**. Mew keeps `subtitleText` in composition state and clears it through `LaunchedEffect(sourceUrl)`.
- `0b9d935` preserve local catalog choices when remote sync arrives before catalog definitions load — **ALREADY IMPLEMENTED**. Mew preserves unknown local keys while `knownKeys` is empty.
- `d703ca4` use the export-to-service document picker on iOS — **ALREADY IMPLEMENTED**. Mew uses `UIDocumentPickerModeExportToService`.
- `05e779a` improve bug-report install-method and official-app diagnostics — **ALREADY IMPLEMENTED**. Mew's issue template contains the sideloading choices and official-app comparison question.
- `b4e4464` fall back to series artwork when an episode thumbnail fails — **ALREADY IMPLEMENTED**. Mew has `rememberEpisodeImageState` and error callbacks in horizontal and list episode cards.

No source changes required for this delta. No tests, builds, CI or device/simulator verification were run. This is a source-presence/behavior comparison, not runtime verification.


## 2026-11-17 — older-history metadata and iOS detail UX audit

Compared upstream fixes against current Mew `Main-dev` source and callers:

- Upstream `c5298633914d967792c79351754ba47d54dfd242` keeps Compose subtitle text/style state alive across episode changes, clearing only the text on source change. Mew's iOS `PlayerEngine.ios.kt` has no equivalent Compose subtitle text/style state pair: it delegates subtitle rendering and style application to the native mpv bridge (`loadFileWithAudio`, `applySubtitleStyle`). **SKIP exact patch / INTENTIONAL PARALLEL**. Keep a separate bridge-level regression audit for style reapplication after `loadFileWithAudio`; do not add unused Compose state.
- Upstream `b6ae11b8be1d2b6ff702ca8163143e06c5902810` falls back from empty Simkl related titles to Trakt/TMDB. Mew's `MetaDetailsRepository.applyMoreLikeThisSource` selects authenticated Trakt or configured TMDB; no Simkl related-title source is wired into this flow. **NOT APPLICABLE as a bug-fix port**. Simkl recommendations would be a separate feature requiring product/source-setting and API audit.
- Upstream `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb` fixes MDBList watch-progress projection so episodes do not inherit show runtime. Mew's inspected `features/mdblist` module contains metadata service/settings only; no equivalent MDBList progress projection was found. **NOT APPLICABLE to current Mew integration**; don't create a parallel projection solely to copy this fix. Revisit if MDBList playback-progress sync is introduced.
- Upstream `f28929d3e3cc0d354de1dd921efc539bbf8a14cf` adds native navigation-bar button-anchor geometry for detail hero mute-button alignment. Mew `DetailHero.kt` uses status-bar top inset plus fixed trailing padding and has no anchor contract in the common screen. Mew iOS root uses a standalone `UITabBar` with its own root controller, not upstream's UINavigationController integration. **ADAPT candidate / NEEDS INVESTIGATION**: trace pushed detail navigation controller ownership, safe-area/RTL coordinate conversion and native/custom navigation modes before designing a platform anchor. Preserve current hero behavior when no native bar is present. No source change.

No source changes in this batch. Tests/build/CI/simulator/device verification: **NOT RUN**. Current refs at audit time: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; `main` `207ef8fa309b8061b73039b2d4e4bde4af65050e`; `upstream-sync` before this ledger update `b527df783859f750667417012cf1916bd50ad333`; upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.


## 2026-11-17 — iOS MPV drawable and PiP parity audit

Compared upstream iOS rendering commits with Mew's native MPV bridge:

- Upstream `839cdcde3c5c` changes render scale to `max(screen.scale, screen.nativeScale)`; Mew `iosApp/iosApp/Player/MPVPlayerBridge.swift` uses `nativeScale` only both in `viewDidLoad` and `layoutMetalLayer()`. **ADAPT candidate**: use a consistent backing-scale helper after checking simulator/device and Display Zoom behavior; preserve simulator GL path.
- Upstream `e57a5655d393` stores a preferred drawable size, restores it when MPV requests a smaller size, and recalculates the surface on app activation. Mew `MetalLayer.swift` accepts any drawable size above 1x1; `layoutMetalLayer()` tracks only `lastAppliedDrawableSize`. Mew's `enterForeground()` restores `vid=auto` and playback but does not explicitly call `layoutMetalLayer()`; `viewDidLayoutSubviews()` remains the normal layout trigger. **ADAPT candidate, playback-sensitive**: review setter threading, external surface sizing, swapchain polling and foreground lifecycle before adding a preferred-size clamp/re-layout. Test resolution changes, background/foreground, rotation, PiP transitions (if later added), and paused subtitle redraw.
- Upstream `327123ae84eb` adds Dolby Vision RPU reshaping to its custom Picture-in-Picture frame pipeline. Mew's iOS player directory contains `MPVPlayerBridge.swift`, `MetalLayer.swift`, and `NowPlayingController.swift`; the inspected bridge has no PiP frame sink/controller, Dolby Vision RPU metadata path or converter. **MISSING FEATURE / NEEDS ARCHITECTURE INVESTIGATION**, not a drop-in fix. Evaluate baseline iOS PiP support and Mew's MPVKit/VideoToolbox frame ownership first; only then assess RPU-aware conversion and fallback behavior.

Mew files inspected: `iosApp/iosApp/Player/MPVPlayerBridge.swift` (blob `d25322288f2476036d44353c52ba0d6008edf069`), `MetalLayer.swift` (blob `49fd377265e16e93a47c48dc4cd08c33a2be47ab`). Upstream commits: `839cdcde3c5c`, `e57a5655d393`, `327123ae84eb`. No source changes. iOS build, simulator, device and playback regression tests: **NOT RUN**.


## iOS Picture-in-Picture parity audit — 2026-11-17

- Upstream `enhanced` contains an experimental iOS PiP subsystem: `PlayerPictureInPictureController.swift`, `PlayerPictureInPictureParking.swift`, `PictureInPictureDolbyVisionConverter.swift`, `PictureInPictureSubtitleComposer.swift`, iOS settings bridge, and a substantially extended `MetalLayer.swift`.
- The upstream Metal layer adds drawable-presentation observation, frame capture/parking, rendering suspension and retry, PiP frame submission, and synchronization around capture state. The Dolby Vision converter handles RPU-aware PiP conversion; the subtitle composer overlays subtitle text into PiP pixel buffers.
- Mew's iOS player tree has `MPVPlayerBridge.swift`, `MetalLayer.swift`, and `NowPlayingController.swift`, but no iOS PiP controller, parking pipeline, frame sink, Dolby Vision converter, subtitle composer, or iOS PiP settings bridge. Mew has `PipSubtitleScale.kt` in common code and `PlayerPictureInPictureManager.android.kt` for Android; these do not provide an iOS frame pipeline.
- Classification: **MISSING PLATFORM CAPABILITY / INTENTIONAL PARALLEL**. Mew's Android PiP implementation is platform-specific, not a duplicate of upstream iOS PiP. The upstream iOS implementation is experimental and not a safe drop-in.
- Decision: **ADAPT / NEEDS ARCHITECTURE INVESTIGATION**. Define Mew's iOS PiP contract (system PiP entry/exit, audio/background behavior, supported codecs/HDR, subtitle expectations, controls and fallback). Trace MPVKit video-output ownership and available CVPixelBuffer/VideoToolbox surfaces. Port in independently testable layers: baseline SDR PiP first, then subtitles, then Dolby Vision RPU conversion only if the source pipeline exposes reliable metadata. Preserve Mew's player bridge and do not modify Android PiP.
- Risks: pixel format/color-space conversion, HDR metadata propagation, frame timing/PTS, drawable lifetime, render-thread locking/deadlocks, app lifecycle, thermal/memory cost, subtitle layout and conversion-failure fallback. Review upstream capture/parking lifecycle before reuse.
- Mew files: `iosApp/iosApp/Player/MetalLayer.swift` blob `49fd377265e16e93a47c48dc4cd08c33a2be47ab`; `MPVPlayerBridge.swift` blob `d25322288f2476036d44353c52ba0d6008edf069`; Main-dev tree `69b4a96072b506f60dcbe538c5f6b71db3a4a464`.
- Upstream files at enhanced `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`: `MetalLayer.swift` blob `a9ae482e132401b7ed4cdaed6b5f248a9bf25e31`; `PictureInPictureSubtitleComposer.swift` blob `08eee148fba7fc693a3b87858da7398231ed6aa8`; `PictureInPictureDolbyVisionConverter.swift` blob `ae2e06a2bf1bf1bfbefcfe7db3b9f1ed3328013c`; `PlayerPictureInPictureController.swift` blob `6e4b569e8b7e5f6f91edf660d5adaaddfee0f78e`; `PlayerPictureInPictureParking.swift` blob `7942f5e9ab10cac5f8d59a5abada4de2f99f0f8f`.
- No source changes. iOS build, simulator and device verification: **NOT RUN**.


## Add-on player/library event parity audit — 2026-11-17

- Upstream commit `8f5d9bcac3c2c0535b17b26cbdff392c10b8d767` introduces/extends `features/addons/AddonEvents.kt`: player start/pause/stop events; library add/remove/watched/unwatched events; active-manifest resource/type/ID-prefix routing; transport-URL deduplication; per-transport serial queues; 10-second timeout isolation; and deduplicated library video-ID chunks of 100. It also skips before/after library and watched-state lookups when no active addon declares the library resource.
- Mew `AddonModels.kt` supports manifest resources, resource types and ID prefixes, and `ManagedAddon.isActive`; the data model can support routing. However the Main-dev tree has no dispatcher and the inspected `LibraryRepository.kt`, `WatchedRepository.kt`, and player playback actions contain no corresponding addon-event integration. Mew player tracking/scrobble events are separate and not equivalent.
- Classification: **MISSING FEATURE**, not duplicate. Addon manifest resource metadata is already present; delivery, event lifecycle and mutation hooks are missing.
- Decision: **ADAPT**, high priority. Map every mutation path: single/bulk library add/remove, watched/unwatched, playback completion, player pause/resume/stop, bulk season operations, profile changes, and local-only/sync-originated mutations. Avoid duplicate events for internal restore/sync operations unless contract requires them.
- Preserve Mew-specific semantics: profile-scoped addon registry and generation invalidation; CloudStream stream/catalog behavior; tracking-provider source and watched-history rules; server-ID exclusions; playback identity/progress semantics. Capture active profile/session generation with each queued event so stale deliveries cannot leak across profile switches.
- Queue design needs review before implementation: upstream uses unbounded event and per-addon channels keyed by transport URL. Consider bounded/coalescing behavior under offline/unreachable addons, queue cleanup when addon URLs change, ordering across profile switches, cancellation and shutdown. Preserve per-addon order without letting one slow addon block others.
- Performance optimization: skip before/after library/watched reads when no active addon supports `library`. Test disabled addons, stale manifests, duplicate transport URLs and profile transitions.
- Upstream adds tests for event URL encoding/query preservation, player IDs, 100-item chunking/deduplication, and tracking-resource detection. Mew should add equivalent unit tests plus mutation-hook/profile-generation tests.
- Mew files at Main-dev `69b4a96072b506f60dcbe538c5f6b71db3a4a464`: `AddonModels.kt` blob `2b909a765dc2b8133c97da766e05b2019db36cc4`; `AddonRepository.kt` blob `a47cb5019619b26c72d3e3a0626cbd91182e2639`; `LibraryRepository.kt` blob `990252c212dbf1754ea16b215fd9c3d5c7519651`; `WatchedRepository.kt` blob `be917a333e13767b7ac2d6b689313be786142112`; `PlayerScreenRuntimePlaybackActions.kt` blob `8717aa2b75d32a3cbd779c7bf5ea5da7186cdc27`.
- Upstream `AddonEvents.kt` blob `432839c6ca034a7897b8ef862ad6822ca59f5913` at enhanced `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- No source changes. Tests/build/CI/device checks: **NOT RUN**.


## iOS settings-backup export picker parity — 2026-11-17

- Upstream commit: `d703ca48e5ade22ceaf424451ffba05efa94e670`; changed only `composeApp/src/iosMain/kotlin/com/nuvio/app/features/settings/SettingsConfigFileLauncher.ios.kt`. It replaces `UIDocumentPickerViewController(forExportingURLs, asCopy=true)` with the URL + `UIDocumentPickerModeExportToService` initializer, matching the import picker family.
- Initial path-level search misleadingly suggested Mew lacked the feature. Caller and tree audit found the existing Mew backup flow: `NuvioEnhancedSettingsPage.kt` calls `ProfileSettingsSync.exportBackupJson/importBackupJson` through `NuvioEnhancedBackupFileBridge`; common expect, Android actual and iOS actual are all present. This is an existing equivalent capability, not a missing feature.
- Mew files on `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`: common bridge `NuvioEnhancedBackupFileBridge.kt` blob `367a9c0e5419172df34d488f120bdee2500567a1`; iOS actual `NuvioEnhancedBackupFileBridge.ios.kt` blob `bb96d98bb55457cf8fb631eb0cd530b3f3b49064`; Android actual blob `819555855ca9c8a3b430f29100507b281da93aab`; UI caller `NuvioEnhancedSettingsPage.kt` blob `0957293b59ab29f1d61f73e1561e0c0cecddcf58`.
- Semantic difference: Mew iOS import already uses `UIDocumentPickerModeImport`, but export uses `UIActivityViewController` share sheet. Upstream uses document-picker export-to-service and waits for picker delegate completion/cancellation, deleting its temporary file in either callback. Mew currently reports export success with the temporary path immediately after presenting the share sheet; the inspected code does not show equivalent completion/cancel cleanup. Do not blindly replace the share sheet: it is an intentional user-visible export path and may support destinations beyond Files.
- Classification: **SAME FEATURE / DIFFERENT UX AND COMPLETION SEMANTICS** (not a duplicate implementation bug). Decision: **ADAPT candidate**. Consider offering a document-picker save path on iOS, or retaining share sheet with a proper activity completion callback; preserve Mew backup JSON schema, profile/settings sync contract and cancellation feedback. Audit temp-file lifetime, filename, file URL ownership and iOS picker API compatibility before editing.
- Upstream common backup UI and Android launcher confirm this commit is a narrow iOS export-picker correction, not a new backup format. Upstream common UI blob `fc2646f27b4395a68741cc0d962de9a81ff542cb`; common launcher blob `2ad4be60a02a11937a8896518ecf15eb70c05c8e`; iOS launcher blob `cfa35568a8371670f8faa6b5e9e88595c7b6f34a`.
- No source changes. iOS build/simulator/device verification and backup round-trip tests: **NOT RUN**.


## Release-note upstream attribution — 2026-11-17

- Upstream commit: `647b561b01cb33e784b5b9f2181569fa65ea5f8e`; changes upstream `.github/workflows/mobile-release.yml` and `scripts/generate-release-notes.sh`. It fetches an upstream comparison ref and emits separate fork and upstream sections, resolving pull-request titles/links against the correct repository. If the upstream fetch fails, it warns and includes commits with fork changes rather than silently omitting them.
- Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464` uses `.github/workflows/android-release.yml` blob `b2c683c8add70e987c4eed59faf74677faeac641` and `scripts/generate-release-notes.sh` blob `a172a85d73c6ffd2aeb0094dff8920bb2ff4edec`. Its release workflow invokes the script only for the previous-to-current version-bump range. The script filters conventional noise and duplicate subjects, but does not fetch or separately attribute upstream commits and does not resolve PR titles/links.
- Do not replace Mew's workflow with upstream's: Mew has distinct release modes, Mew signing-secret validation, unsigned iOS IPA build/packaging, Android signing-certificate checks, release artifact handling and branch/version guards. Preserve these Mew-owned release/security contracts.
- Decision: **NEEDS INVESTIGATION / ADAPT candidate**. Define how Mew records the exact upstream source baseline represented by each release (the `upstream-sync` snapshot alone is mutable and not necessarily the revision selectively integrated into `Main-dev`). Then generate two clearly labelled sections only for upstream commits actually represented in the release, plus Mew-owned adaptation commits. Do not simply diff against current `enhanced` or list every upstream commit since the previous release; that would claim changes not integrated. Test missing upstream fetch, unrelated/diverged histories, duplicate PR/subjects, exclusions and no-upstream fallback.
- No workflow/script source changes. Release-note script tests, dry-run, build, CI and release execution: **NOT RUN**. No release created or published.


## Library release calendar cache architecture + OMDb episode ratings — 2026-11-17

### Release calendar
- Upstream current implementation in `composeApp/src/commonMain/kotlin/com/nuvio/app/features/library/LibraryScreen.kt` (blob `95bbf5986101594a5aebfc3a4d320b7fda1961b0`) centralizes work in `LibraryReleaseCalendarCache` and `LibraryReleaseSchedulePersistence`; expect storage contract is `LibraryReleaseScheduleStorage.kt` blob `417e992ba487692b87031ec8af51ee7f379d424e`.
- Upstream behavior: cache identity includes active profile, nearby-month window and library item fingerprint; persisted state includes event set, loaded month keys, build date, addon manifest signature and failed series keys. Warms previous/current/next months, incrementally fetches later months, awaits enabled addon manifests (15s timeout), bounds series metadata concurrency to 4, preserves old usable episode events during refresh, invalidates when relevant addon signature changes, retries failed series with cooldown/backoff, throttles force refresh, and avoids redundant refetch within a reuse window.
- Mew already has a release calendar/radar path, not a missing feature. `LibraryScreen.kt` blob `958c5320ad12f743df873229d0d519e48cf775b3` resolves metadata when the calendar is shown and separately for radar, stores profile-scoped details through `LibraryStorage.loadReleaseSupportPayload/saveReleaseSupportPayload`, and uses a 6-hour TTL. Mew storage contract `LibraryStorage.kt` blob ``8a13fc4a51f00facbc72bd797298e6424408315e`` exposes profile-scoped cache methods. It lacks the centralized event cache's month paging, addon-signature invalidation, failed-series state/retry and preserved-event refresh lifecycle in the inspected call path.
- Classification: **SAME FEATURE / DIFFERENT CACHE ARCHITECTURE AND COVERAGE**. Decision: **ADAPT** the central cache lifecycle and incremental event building, but preserve Mew's profile-scoped storage and existing UI/event models. Before implementation, inspect every calendar entry point, profile switch, library mutation, addon registry update, date rollover and force-refresh path; avoid duplicating the existing radar metadata cache or issuing duplicate metadata requests. Tests: cache key/profile isolation, persisted schema migration, stale date/addon invalidation, partial failure preservation, month paging, bounded concurrency, retry cooldown, empty library and cancellation.

### Episode ratings
- Upstream adds `OmdbEpisodeRatingsService.kt` blob `947a005f978d350216de81a25b6408181f173da5`, storage contract `OmdbEpisodeRatingsStorage.kt` blob `5cacf721ba7d3ab25c678e2edfdcbad1d07b1bcc`, `OmdbSettingsRepository.kt` blob `250e5d01ffd6e56b60ad7a5b9c68711f24df28d9`, and `EpisodeRatingsVisibility.kt` blob `75d9657ce37ac317c6a5eef9f0e550d3dca0cc21`. Service caches up to 400 seasons; complete season TTL 7 days, incomplete TTL 12 hours; fetches seasons concurrently; caps per-season missing episode lookups at 30 and lookup parallelism at 6; resolves IMDb from IMDb/TVDB IDs and anime tracking IDs with bounded timeouts; excludes not-yet-aired episodes; reuses cached ratings on request failure.
- Mew does not have an OMDb service/settings/storage path. Mew `MetaDetails` / `MetaVideo` blob `708b95d096aa144f387636792871aa0450bc5495` lacks `imdbId`, `tvdbId`, per-video `imdbId`, `imdbSeason`, and `imdbEpisode` fields. Mew parser blob ``258e89fe8448b5b3596ed96a7929126768a5162a`` parses generic episode `rating` but does not map those IMDb/TVDB fields. Mew's `DetailSeriesContent.kt` blob `b4b4892414800b435bb51ad3de7692c4a6eda601` already displays source-provided episode ratings and honors `showEpisodeRatings`; this is presentation support, not an equivalent OMDb enrichment service. Mew's `MetaScreenSettingsRepository` blob `5beacb96d10833f239830e8759978b8af49d9518` exposes only a boolean show/hide setting, unlike upstream's three-state show all/hide all/hide unwatched.
- Classification: **MISSING ENRICHMENT FEATURE; EXISTING DISPLAY CONTROL IS A SHARED PATTERN / DIFFERENT SEMANTICS**. Decision: **ADAPT**, optional feature. Extend parser/model compatibly, add explicit opt-in API key/settings and profile-safe persistence, integrate enrichment into Mew's base-vs-screen metadata cache lifecycle without blocking initial metadata publication, preserve source-provided ratings as fallback, and gate by aired date. Security/privacy review required for API key storage, query-string handling and log redaction. Do not add a bundled key without a separate approved secret-handling design. Verify TMDB/IMDb/TVDB ID provenance and anime episode mapping before wiring. Tests: malformed/missing IDs, TVDB resolution timeout, anime mapping, aired/unreleased filtering, cache TTL/legacy payload, partial OMDb failure, cancellation, bounded concurrency, profile switch, API key changes, and preservation of addon ratings.
- No source changes. Builds/tests/CI and iOS/Android device verification: **NOT RUN**.

## Player source fallback and next-episode lifecycle audit — 2026-11-17

- Compared upstream enhanced player source fallback against Mew Main-dev. Upstream adds PlayerSourceFallbackRun.kt (blob a81b67e327c1c28255aed23b1e1c444f8bb7b871) and PlayerScreenRuntimeSourceFallback.kt (blob 7b19a343550856c1cc43067db7674c018e0481db): bounded retry of alternative auto-selected sources, stable source-identity tracking, duplicate failed-torrent exclusion, shared stream-search reuse, Direct Debrid resolution, and preservation of playback position/subtitle/provider metadata on replacement.
- Mew already has source identity normalization and provider/binge-group constrained next-episode candidate selection, but does not track attempted identities or retry the next candidate after Direct Debrid resolution fails. **ADAPT** bounded fallback around Mew's own selector and resolver; do not replace Mew provider/binge-group ordering with upstream selection policy. Exclude manual/external selections, engine-switch conflicts and post-start playback. Reset attempt state per episode and retain Mew P2P/debrid identity semantics.
- Upstream next-episode auto-play returns the attempted-source set/count and carries it into the player when switching episodes. Mew's callback currently passes only (StreamItem, MetaVideo); no fallback history is propagated. **ADAPT** the callback contract and reset semantics. Test manual episode selection, downloaded episodes, P2P, debrid, source identity duplicates, stale requests, cancellation and episode transitions.
- Upstream settings add enable toggle, max additional sources (bounded range), and startup timeout; Android and iOS storage use ProfileScopedKey and include values in settings sync/backup. Mew has profile-scoped PlayerSettings storage and a separate existing next-episode fallback toggle, but lacks these source-failure settings and UI. Do not conflate the existing next-episode group/provider fallback setting with this distinct source-failure fallback. Add migration-safe defaults and profile-isolation/sync coverage if integrated.
- Error pipeline integration point: upstream handles expired-link refresh, source fallback, engine failover, then final error/cache cleanup. Mew currently handles expired-link refresh, engine failover, then final error. Integrate source fallback between expired-link refresh and engine failover, respecting Android engine-switch precedence and iOS behavior. Upstream UI disables conflicting options with explanatory text.
- Classification: **MISSING FEATURE / SHARED IDENTITY INFRASTRUCTURE**. Decision: **ADAPT, high priority**. No source changes; no builds/tests/CI/device checks run. Inspected refs: Main-dev 69b4a96072b506f60dcbe538c5f6b71db3a4a464, upstream enhanced b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03.


## Download subsystem audit refinement — 2026-11-17

Compared upstream batch-download commit `48cbb901bf48a641a1270c2d10bbd4260e712e68` against Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`. Upstream reference: `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.

### Batch selection and source resolution — existing Mew implementation; not a missing feature

- Mew has `EpisodeDownloadFlowSheet.kt` (blob `1b1c4c2a002a55657f59749edf1a5d279fa9697e`) wired from `MetaDetailsScreen.kt` (blob `39b63480b23da44bb55c122841fc1e56c8f50bba`). It supports movie/episode downloads, episode selection grouped by season, select-season, manual provider/quality selection, auto-select, progress, and queue-mode preference.
- `DownloadSourceResolver.kt` (blob `6bc5a45415fb18e9537aa8f7b97130ae9b6e1282`) contains `EpisodeDownloadCoordinator`. Auto-selection resolves each episode independently with up to 3 concurrent episode selections and up to 4 concurrent providers; source/provider requests have bounded timeouts. Manual selection applies one selected provider/quality to the selected episodes, matching each episode independently and persisting pending source searches when unavailable.
- Upstream `BatchDownloadCoordinator.kt` (blob `bef810f53d3d0dd088f1be92c5216fc186c03af0`) and `BatchDownloadDialog.kt` (blob `f3a52b77a9f7201a5d5b3c3c92e4ca511f2ccbfe`) use a different planner/UI: season-by-season source steps, per-episode stream candidates, manual per-episode override, and explicit already-downloaded/in-progress states.
- Classification: **INTENTIONAL PARALLEL / SHARED PATTERN, DIFFERENT UX AND PLANNING MODEL**. Decision: **ADAPT selectively**, not wholesale port. Mew's current flow is real batch functionality; preserve its custom sheet, progressive source results, auto-select and pending-search contracts. Evaluate per-episode manual override and a planner/status layer as targeted improvements.

### Important existing-download replacement risk

- Mew's episode picker derives candidates from available metadata episodes and does not read current download state. It does not mark/exclude already-downloaded or in-progress episodes.
- `DownloadsRepository.enqueueFromStream` (blob `5e5dfd0b0f6ad3959da8106c83a9a6ce7b4a86eb`) replaces an existing item with the same logical episode key; replacement cancels the old handle and removes the old local/partial file. Thus selecting an already-downloaded episode in the batch flow can replace/delete the existing downloaded file.
- Upstream's `isBatchEpisodeDownloaded` / `isBatchEpisodeUnavailable` planner helpers explicitly identify completed and in-progress items before queueing.
- Decision: **ADAPT — high priority safety/UX fix**. Feed current download state into planning, exclude completed/in-progress episodes by default, and expose an explicit re-download/replace action with confirmation. Add regression tests proving a completed file is not deleted by ordinary season selection and that explicit replacement remains possible.

### Queue admission and network policy — meaningful gap

- Mew `DownloadsModels.kt` (blob `df85a084657201d056c9f009015b427c5a26989a`) persists `DownloadStatus.Waiting` and offers `AllAtOnce` or `OneAtATime`. The repository starts every `AllAtOnce` item immediately; `OneAtATime` promotes the oldest waiting item only after no transfer is active. There are no total/per-show/concurrent-show admission limits.
- Upstream `DownloadQueuePolicy.kt` (blob `a255832eabb3f5ee79b2d043e83a4b10ac11c53f`) enforces maximum total transfers, episodes per show and concurrent shows, and starts the oldest eligible queued item.
- Mew has no download network-guard implementation in its source tree. Android uses the app's direct OkHttp downloader; iOS `DownloadsPlatformDownloader.ios.kt` (blob `1aa24cba6e141ec3acb39131805af89262087f7a`) configures URLSession with cellular, expensive and constrained access enabled. Upstream `DownloadNetworkGuard` (blob `3edc99dbdee124797179b3b1407c743afe0f1d98`) uses Android connectivity callbacks and iOS `NWPathMonitor`, with an explicit device-level opt-in for mobile data.
- Decision: **ADAPT — high priority**, retaining Mew's existing queue modes and platform downloaders. Add bounded admission and an explicit device-level mobile-data preference; unknown network state should not silently authorize metered downloads. Define active-transfer pause/resume behavior when transport changes. Keep device policy out of profile sync.
- Migration warning: Mew serializes `DownloadStatus` as an enum and `DownloadsCodec.decode` falls back to an empty payload on decode failure. Do not blindly add upstream's `Queued` enum value or copy its payload shape; design a backward-compatible status/payload migration and rollback behavior first.

### Offline metadata and storage visibility

- Mew stores a `DownloadDetailsSnapshot` on each `DownloadItem` and reconstructs offline details from the available per-item snapshots. This is an existing offline-details capability, but repeating show-level metadata across episode items can increase persisted payload size.
- Upstream `OfflineShowMetadata.kt` (blob `9146fd69234a2cff66fbdc6a9a34443bfd8bee79`) stores bounded, merged metadata once per show, separately from frequently updated download progress, and reconstructs details using stored metadata plus downloaded episodes.
- Classification: **SHARED PATTERN / DIFFERENT DATA MODEL**. Decision: **ADAPT if storage profiling supports it**; migrate from existing snapshots with a legacy fallback and preserve offline deep-link/basic-metadata behavior. Do not discard existing snapshots during migration.
- Upstream adds `DownloadsStorageStats` and `DeviceStorageBar` (blobs `42bbfa11793b4d7802da4bf7a56d2c89e334fd60` and `bddc3e8b2dad181b625e7563a3c25a079b15e474`). No equivalent storage-stat component was found in the Mew download source tree. Decision: **ADAPT — lower priority**, accounting for Mew's external-folder destination where device app-container free space is not the selected destination's free space.

### Platform lifecycle — intentional parallel, do not replace wholesale

- Mew Android uses its own coroutine/OkHttp downloader, foreground-service/live-status integration and resumable `.part` range requests. Mew iOS uses its own URLSession data-task delegate, partial-file range resume and app-background pause hook.
- Upstream's Android WorkManager/JobScheduler transfer scheduling and iOS transfer/resume-data handling have different lifecycle and persistence contracts. Classification: **INTENTIONAL PARALLEL / PLATFORM-SPECIFIC TRANSPORT**. Preserve Mew transports; adapt only isolated queue/network policy after platform lifecycle and cancellation tests. No transport replacement approved.

### Verification and boundary

- Mew `DownloadsBatchFlowTest.kt` (blob `0f0ec1bbc3121060e15fd1812f93a202de34c8d3`) currently covers quality parsing, logical episode keys and pending-search grouping; it does not test selection against completed downloads, queue admission, network changes or metadata migration.
- Upstream has dedicated `DownloadQueuePolicyTest`, `EpisodeDownloadIndexTest`, Android transfer/lifecycle tests and platform-specific paths. Mew's test suite/build/CI/device verification was **NOT RUN** in this audit.
- No application source changed. This entry refines the previous broad download audit: batch UI already exists in Mew; the highest-priority gaps are safe handling of already-downloaded episodes and bounded/network-aware queue admission.


## Generic Stremio addon tracking events — privacy and caller audit — 2026-11-17

Compared upstream feature commit `964ecfd8d98598cd2d315cedb584fe7ef59a76fd`, reconciliation fix `d89811ac6c75a3f80c0ab424ab01a37639401d54`, and per-addon queue follow-up `8f5d9bcac3c2c0535b17b26cbdff392c10b8d767` against Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`.

### Mew capability and duplicate classification

- Mew `AddonManifest` already parses and retains `resources`, resource types and ID prefixes (`AddonModels.kt` blob `2b909a765dc2b8133c97da766e05b2019db36cc4`; parser blob `48634dcc1c4bbb019dbca908f317ca313c49df16)). `AddonRepository` manages enabled/disabled and profile-synced addon manifests (blob `a47cb5019619b26c72d3e3a0626cbd91182e2639`).
- No Mew `AddonEventDispatcher`/event delivery implementation or player/library event callsites were found in the inspected Mew addon, library, watched and player runtime files. Mew `LibraryRepository` (blob `990252c212dbf1754ea16b215fd9c3d5c7519651`) syncs local library membership with configured tracking providers/server; `WatchedRepository` (blob `be917a333e13767b7ac2d6b689313be786142112`) syncs watched state; player progress is handled through Mew's tracking/scrobble pipeline. These are **INTENTIONAL PARALLEL / DIFFERENT PROTOCOLS**, not equivalent generic Stremio addon event delivery.
- Upstream sends player start/pause/stop plus current position/duration; library add/remove; watched/unwatched, including episode video IDs in chunks of 100. Delivery is fire-and-forget GET requests to addon transport URLs. Upstream `AddonEvents.kt` now uses a serial queue per transport and 10-second send timeout; `AddonEventsTest.kt` covers URL encoding, query preservation, ID chunking and resource detection.
- Decision: **MISSING FEATURE; ADAPT ONLY WITH EXPLICIT PRIVACY APPROVAL**. No source changes. This feature transmits viewing activity and library/watched identifiers to third-party addon operators; it is not a harmless addon parser enhancement.

### Consent gap to avoid when adapting upstream

- Upstream commit `964ecfd` adds a checkbox before installing a newly entered tracking addon. However, current upstream `ManagedAddon` has no persisted consent field, server `AddonRow` stores only URL/name/enabled/sort order, and `AddonEventDispatcher.route` sends to every active addon whose manifest declares the resource.
- Therefore the install-time checkbox does not establish durable per-addon consent for already-installed or server-synced tracking addons. On upgrade/sync, an existing active tracking addon can satisfy the dispatcher's active/resource checks without a locally persisted consent record. Treat this as an upstream privacy-design gap, not behavior to copy.
- Mew adaptation must default existing and synced tracking addons to **no consent**, persist opt-in per addon (and define profile/device scope), check consent at dispatch time, and provide revocation/disable semantics. Consent text must name the data sent and that it leaves the app for the addon operator/service. Do not emit any event before consent.
- The upstream follow-up queue uses unbounded event and per-addon channels. Preserve ordering/isolation but evaluate queue bounds, stale-event expiry, disable/removal cancellation and backpressure so an unreachable addon cannot accumulate unlimited pending activity.

### Mew caller and side-effect constraints

- Mew's user library mutations are `LibraryRepository.toggleSaved`, `toggleLocalSaved`, `save`, `remove`, and membership/list operations. Server pulls and profile changes are separate synchronization paths; only confirmed local user state transitions should emit events. Compare before/after membership to avoid duplicate events when an operation is a no-op.
- Mew's explicit watched actions route through `WatchingActions` to `WatchedRepository.markWatched/unmarkWatched`. Playback completion has a distinct `markWatchedFromPlaybackCompletion` path and must not be treated as a manual watched action.
- Important Mew-specific trap: `WatchedRepository.reconcileSeriesWatchedState` currently calls the public `markWatched(seriesWatchedItem)` / `unmarkWatched(seriesWatchedItem)` when it derives the whole-series marker from episode state. If public methods begin dispatching addon events, this would incorrectly tell addons that the user manually marked/unmarked the whole series. Upstream encountered this exact regression and fixed it in `d89811a` by routing reconciliation through internal mutation paths. Mew needs an explicit event-suppression/internal path before any implementation.
- Player runtime must emit exactly one stop for the old target on episode/source changes and exit, avoid placeholder durations, resend current state after seek only when appropriate, and preserve Mew's external playback/tracking flows. No event should be generated by sync, playback-derived watched reconciliation, profile switching, or provider refresh.

### Decision and verification boundary

- **Decision: NEEDS PRODUCT APPROVAL / SECURITY REVIEW**, then ADAPT with durable per-addon consent and explicit user-action-only event boundaries. No implementation is authorized by this audit.
- Mew source inspection covered addon manifest/repository/UI, library mutations, watched mark/unmark/reconciliation, WatchingActions, and player runtime effects/actions. It did not execute integration tests or capture network traffic.
- Builds/tests/CI/device verification: **NOT RUN**. No app source changed.


## Movie end-card recommendations — upstream `07de5b27795d2fe236ca917dc5608a594947b9d0` — 2026-11-17

- Upstream adds `MetaDetailsRepository.withPreferredMoreLikeThis`, resolves the configured source for the movie end-card when the player metadata was loaded/cached under a different source, and feeds the result into `playerMoreLikeThis` while recommendations are enabled. Candidate filtering continues to exclude the current title and watched items.
- Mew `MetaDetailsRepository.kt` (blob `dcfcd22b2b86bfb3e06062d2c0303321ed56f449`) already has `applyMoreLikeThisSource` in the metadata enrichment path and records the selected source on `MetaDetails`; its detail-screen metadata source selection is therefore an existing capability.
- Mew's inspected player runtime files `PlayerScreen.kt`, `PlayerScreenContent.kt`, `PlayerScreenRuntimeEffects.kt`, `PlayerScreenRuntimeState.kt` and `PlayerScreenRuntimeUi.kt` do not contain the upstream movie-recommendation/end-card state, setting or candidate surface. There is no player end-card recommendation UI to which the upstream source-refresh helper can be directly ported.
- Classification: **SHARED PATTERN / DIFFERENT PRODUCT SURFACE**. Mew has source-aware More Like This metadata on detail pages; upstream adds a player-only end-card experience. Decision: **ADAPT only as a separate player UX feature**, after inspecting the Mew player end/next-episode UI, dismissal lifecycle, playback end timing, offline mode, recommendation setting, watched filtering and source fallback. Do not add a repository helper alone or duplicate the existing detail-page source selection.
- No source changes. Player UI tests, builds, CI and device verification: **NOT RUN**.


## Library vertical filters — full Mew data/caller audit — 2026-11-17

Upstream feature: `2da28d707c77` — remember list/type, add genre/year/watched filters and faceted counts. Compared with Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`.

### Existing Mew behavior and data availability

- Mew `LibraryScreen.kt` (blob `958c5320ad12f743df873229d0d519e48cf775b3`) already has a vertical projection, section/list selection for remote sources, type filtering, sorting, and separate downloads filters. The downloads filter is a different surface/semantic and must remain unchanged.
- Mew `LibraryDisplaySettings.kt` (blob `af31a9421807bbcabe960bf479ff5b9bde68ca1a`) currently persists only layout and sort. `selectedLibrarySectionKey` and `selectedLibraryType` live as screen `rememberSaveable` state in `LibraryScreen`; they are not stored in the profile-scoped display-settings payload. Storage uses `ProfileScopedKey` on Android/iOS (common contract blob `a1cfceaa89904ff99186a655096e743dbd741b93`, Android blob `75b026835852838ebc9258e0534b92478bccd1e3`, iOS blob `aeeef6f36996a14db748ff9eb461fa74037909bc`).
- `LibraryItem` already contains `genres` and `releaseInfo` (blob `5f4da785ec88b5d012dee23fbce1a4efeedc2a61`). Trakt mapping populates both `releaseInfo` and genres in `TraktLibraryRepository` (blob `1bdd28d4b7fa1f1ba954f5473cfc4098af89b82a`). Simkl projection populates `releaseInfo` from its year but currently does not populate genres (`SimklLibraryProjection.kt` blob `f3a44865001816c72b95b8d483bf6794370b665e`). The genre filter must therefore be data-driven and omitted/empty where a source has no genre metadata; do not fabricate genres or fetch metadata per card just to fill the facet.
- Mew already collects `WatchedRepository.uiState.watchedKeys` and `fullyWatchedSeriesKeys` in `LibraryScreen`, and uses `WatchingState.isPosterWatched` for poster state. Reuse the same watched semantics for filtering; do not create a parallel watched-state algorithm.

### Exact upstream behavior to adapt

- Upstream extends `LibraryVerticalProjection` with type counts, all-types count, genre options, year options and effective selected facets. It computes genre counts constrained by selected year and year counts constrained by selected genre; type counts are calculated after genre/year filtering; watched/unwatched is applied to final entries.
- Upstream type choices use a stable media order (`movie`, `series`, `tv`, `show`, `anime`) rather than alphabetical order and display counts. Mew's current type choices are alphabetically sorted and have no counts.
- Upstream persists selected section/list and selected type in `LibraryDisplaySettingsRepository`; genre/year/watched selections are screen state. Preserve Mew's profile-scoped storage and use backward-compatible optional fields in the existing JSON payload.
- Upstream adds a second filter row for vertical layout only. Its `LibraryFilterRow` distributes chips across available width when possible and scrolls when constrained; it adds optional `fillWidth` to `NuvioDropdownChip`. Mew's existing `LibrarySavedControls` (blob `d3a9d6ddd207ce7db0710a9be8161b7a8949d0d6`) is a single horizontally scrolling row and its shared `NuvioDropdownChip` (blob `82f4a193d24e8a45ce6c97ab9c14565218e9e7e8`) has no `fillWidth`. Add the new row without changing the existing horizontal layout; if extending the shared chip, keep the new parameter optional/default-false and audit its other callers.
- Mew's current vertical projection in `LibraryDisplaySettings.kt` filters only type and sorts; it has no genre/year/watched facets or faceted counts.

### Classification and decision

- Genre/year/watched filters and facet counts: **MISSING FEATURE** — **TAKE / ADAPT** the projection algorithm, with source-aware metadata availability.
- Persist selected list/type: **PARTIALLY IMPLEMENTED** — state exists in the screen but is not persisted in the profile-scoped display-settings repository. **TAKE / ADAPT** upstream persistence fields.
- Existing type filter/sort/layout and downloaded-content filters: **ALREADY IMPLEMENTED / INTENTIONAL PARALLEL** — retain them; extend rather than replace.
- UI row and shared dropdown width: **SHARED PATTERN / DIFFERENT LAYOUT** — **ADAPT** only for vertical saved-library controls.

### Regression tests required

Mew `LibraryDisplaySettingsTest.kt` (blob `79c4e076239c07acab5c8a02789d6fab1284a6c0`) currently covers sorting, vertical section/type projection and display-settings serialization; no genre/year/watched/facet tests exist. Add tests for genre+year intersection, reciprocal facet counts, year extraction from single years/ranges/malformed values, watched/unwatched including fully-watched series, source with no genres, type counts/order, invalidated selections after source/list changes, and legacy profile payload migration. Also verify Trakt/Simkl/local behavior and that the horizontal layout/download filters are unchanged.

No source changes. Builds/tests/CI/device verification: **NOT RUN**.


## Home recomposition and Continue Watching publication — 2026-11-17

Upstream performance merge: `e5272769a6709d43aaecc3d7bbe8d66d5a200702` (Android cold-start/home performance).

### New concrete Mew comparison: progressive Next Up publication

- Mew `HomeScreen.kt` at `Main-dev` blob `eddaf8634ff85c00db51b7ab6b5617de32e68846` resolves Continue Watching Next Up candidates in bounded batches using a semaphore and a per-batch channel. It publishes `nextUpItemsBySeries` / `processedNextUpContentIds` after each candidate result; the deferred-candidate pass also saves snapshots on each changed/conclusive result and yields between items.
- Upstream changes this to drain currently available results and coalesce state publication using a monotonic-time interval, reducing repeated Compose state emissions while keeping progressive results.
- Classification: **SHARED PATTERN / DIFFERENT BATCH AND CACHE CONTRACTS**. Decision: **ADAPT** publication coalescing only. Preserve Mew's primary-vs-deferred candidate ordering, cache generation, retained retry IDs, fallback aliases, per-profile snapshots, cancellation and maximum item count. Do not replace Mew's whole resolver loop with upstream's loop. Add deterministic tests for result ordering, cancellation, final flush, conclusive/transient results and snapshot persistence; use an injectable clock/interval for throttling tests.

### Existing addon state and hero metadata constraints reaffirmed

- Mew Home currently collects the complete `AddonRepository.uiState` in `HomeScreen` and derives enabled addons, meta-provider keys and manifest readiness from the full list. Upstream introduces `collectMappedAsState` and compact `HomeAddonSignals` to reduce unrelated recompositions.
- Earlier caller audit remains controlling: Mew's full addon list also feeds metadata readiness, catalog refresh signatures, `HomeCatalogSettingsRepository.syncCatalogs`, `HomeRepository.refresh`, and CloudStream registry participation. Decision remains **ADAPT the observation/recomposition boundary only**, preserving the complete source-of-truth list and every catalog/resource/transport/profile/visibility dependency.
- Upstream's `HomeHeroMetaLoadEffect` separates metadata loading side effects from hero-state collection. Mew hero metadata lifecycle still requires a dedicated caller/lifecycle audit before changing; do not remove current settings or hero-state dependencies based only on the upstream split.
- Baseline profile module and Compose stability configuration remain **ADAPT after measurement**, as already recorded: inspect Mew variants, app IDs, selectors, CI and compiler metrics first; do not copy generated profiles or blanket-mark mutable Kotlin collections stable.

No source changes. Builds, benchmarks, tests, CI and device verification: **NOT RUN**.


## iOS parity audit — player rendering, subtitles, navigation — 2026-11-17

### MPV drawable backing scale

- Upstream commit: `839cdcde3c5cb5124f2efb6ad5e3ff32b7549d6f` (`fix(ios): render mpv at the screen's backing scale`), path `iosApp/iosApp/Player/MPVPlayerBridge.swift`.
- Mew `Main-dev` has the same file and equivalent Metal drawable sizing in `MPVPlayerViewController`. Both initialization and `layoutMetalLayer()` currently use `screen.nativeScale` only; upstream centralizes `renderScale()` as `max(screen.scale, screen.nativeScale)` and uses it for both `contentsScale` and drawable dimensions.
- Classification: **SHARED PATTERN / SAME RENDERING CONTRACT**. Decision: **ADAPT** the small scale normalization, preserving Mew's simulator OpenGL path, deferred layout passes, externally managed view size, and drawable resize behavior. Verify external-display/window screen selection and no regression to Mew's custom surface sizing.
- Source-only candidate; no iOS device/simulator verification performed.

### Styled subtitle state across episode changes

- Upstream commit: `c5298633914d967792c79351754ba47d54dfd242` changes `subtitleText` from source-keyed Compose state to retained state with an explicit `LaunchedEffect(sourceUrl)` clear.
- Mew's `PlayerEngine.ios.kt` does not contain this overlay state. Its iOS player routes subtitle styling and track operations through the native MPV bridge; the inspected Swift bridge has no matching `subtitleText` / `updateStyledSubtitle` overlay state.
- Classification: **INTENTIONAL PARALLEL / DIFFERENT SUBTITLE PIPELINE**. Decision: **SKIP direct code port**. Keep checking subtitle style persistence, source transitions and external subtitle behavior against Mew's MPV implementation; do not add a second overlay state machine.

### iOS floating tab-bar blur

- Upstream commit: `c314dd48d622de249eebbea7e531e3f7b2e506d4` clears UIKit tab-bar and tab-item appearances and hides iOS 26 bottom scroll-edge blur while preserving the floating pill.
- Mew's `iosApp/iosApp/ContentView.swift` uses a custom UIKit tab-bar appearance and profile-avatar overlay/animation. It does not currently contain the upstream `removeBarBlur` / transparent-tab-background / bottom-edge-effect handling.
- Classification: **SHARED PATTERN / DIFFERENT NAVIGATION IMPLEMENTATION**. Decision: **NEEDS INVESTIGATION / ADAPT CANDIDATE**. Inspect the actual Mew tab presentation modes, safe-area/background layering and iOS 26 behavior before applying transparency; preserve Mew's native tab colors, profile-avatar animation and touch overlay.

### iOS settings export picker

- Upstream commit: `d703ca48e5ade22ceaf424451ffba05efa94e670` changes export to `UIDocumentPickerViewController(url:inMode: .exportToService)`, matching the import picker family.
- The upstream path `composeApp/src/iosMain/kotlin/com/nuvio/app/features/settings/SettingsConfigFileLauncher.ios.kt` is not present at that path in Mew `Main-dev`. Mew's corresponding settings backup/export caller and iOS picker contract have not yet been located.
- Decision: **NEEDS INVESTIGATION**. Do not port the isolated constructor change until Mew's actual import/export implementation and completion/copy semantics are identified.

### Dolby Vision Picture-in-Picture

- Upstream commits `327123ae84ebb3fed9b45cf3df24cc677ae6d795` and related PiP pipeline changes add RPU metadata forwarding and a Metal conversion path for Dolby Vision PiP frames.
- The inspected Mew iOS MPV bridge contains no PiP controller, RPU metadata forwarding, or `video-params/colormatrix` observer. This is not yet an app-wide absence finding; other iOS files/callers still need inventory.
- Decision: **NEEDS INVESTIGATION** as a platform capability gap. Do not transplant the converter independently of a verified Mew PiP frame lifecycle, MPVKit API and HDR fallback contract.

No application source changes. iOS build, tests, CI, simulator and device verification: **NOT RUN**.


## Catalog, artwork, provider fallback and subtitle parity — 2026-11-17

### Home catalog choices arriving before catalog definitions

- Upstream commit: `0b9d93599f7d7603e7814a99a70aa64d7c48e086`. Upstream preserves locally stored preferences when `knownPreferenceKeys()` is still empty at the time remote catalog preferences arrive.
- Mew `HomeCatalogSettingsRepository.kt` already applies `shouldPreservePreferenceAfterRemoteSync(key, remoteKeys, knownKeys)`: it preserves unmatched entries when the known-key set is empty, preserves known keys, and preserves explicit sync keys (non-collection keys with more than two colon separators). It also retains Mew's pinned-collection ordering.
- Classification: **ALREADY IMPLEMENTED / STRONGER Mew-SPECIFIC PARALLEL**. Decision: **SKIP**. Do not port upstream's simpler inline predicate over Mew's helper or remove pinned collection handling. Add/retain regression coverage for empty-known-key race, later definition arrival, remote-key replacement and collection ordering.

### Episode artwork fallback after a failed thumbnail

- Upstream commit: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. Upstream adds remembered per-card image state: try the episode thumbnail, then switch to series artwork on image load error.
- Mew `DetailSeriesContent.kt` already has `EpisodeImageState` and `rememberEpisodeImageState(thumbnail, fallbackImage)`; both horizontal and list episode cards pass `onError = { episodeImage.onError() }`. The implementation is semantically equivalent and present on `Main-dev`.
- Classification: **ALREADY IMPLEMENTED**. Decision: **SKIP**. Keep Mew's wider card/layout differences; no duplicate fallback state should be added.

### Brazilian Portuguese duplicate translations

- Upstream commit: `106e67facd528dbd923b4ec0a087404e51372337` removes duplicate definitions for `settings_playback_tunneled_playback_description` and `settings_playback_auto_skip_segments`.
- Mew `values-pt-rBR/strings.xml` currently has exactly one definition of the first key and no definition of the second key; no duplicate of either key was found.
- Classification: **ALREADY CLEAN / FALSE POSITIVE FOR Mew**. Decision: **SKIP**. Do not remove valid translation entries or add the missing auto-skip string without checking whether the corresponding setting exists in Mew.

### TorBox alternative-hash cache lookup

- Upstream merge: `f9bce1b0cfdec875eb9cfae1767cbd3d4ba3310a`, adding `resolveTorboxAliasMatches` to `LocalDebridService.kt`.
- Mew `LocalDebridService.kt` already contains `resolveTorboxAliasMatches`, normalizes hashes, retries only unresolved aliases concurrently and preserves cancellation. Mew's helper returns nullable when a retry cannot establish the alias result, explicitly preserving UNKNOWN availability rather than converting an inconclusive retry into a cache miss.
- Mew `LocalDebridServiceTest.kt` covers exact matches, single aliases, partial batch retries and failed retry remaining UNKNOWN.
- Classification: **ALREADY IMPLEMENTED / Mew HAS A STRONGER UNKNOWN-STATE CONTRACT**. Decision: **SKIP**. Do not replace Mew's nullable result with upstream's non-null map behavior.

### Next-episode auto-play source fallback

- Upstream merge: `ec726ddfd87392df0062fe03ac8cae24a2bff00a` adds a bounded source-fallback run for next-episode auto-play. It retries later eligible sources when the first debrid resolution is stale/expired/unplayable, preserves the tried-source set and attempt limit, and resets the fallback state when the user manually chooses a source or switches to a downloaded/P2P episode.
- Mew `PlayerNextEpisodeAutoPlay.kt` currently loads the next episode's streams and chooses an auto-selected candidate; `PlayerScreenRuntimeSourceActions.kt` resolves the chosen direct-debrid source and refreshes the episode streams when stale. No equivalent `PlayerSourceFallbackRun` / tried-source state was found in the inspected Mew player runtime.
- Classification: **MISSING BEHAVIOR / SHARED PLAYER FLOW**. Decision: **ADAPT candidate**. Preserve Mew's source ranking, provider/binge-group preference, current-stream reuse, download/P2P/external-source paths, retry/cache semantics, user manual selection and configured maximum fallback count. Do not copy upstream's helper until all callers and failure categories are compared. Add tests for expired first source then playable next source, limit reached, fallback disabled, duplicate identity keys, cancellation, manual source selection and next-episode reset.

### Subtitle styled-overlay parity

- Upstream commits `c46fc19a757d6acee10e870671744dbdeb959917`, `b8bbd700fda41ad55cdb90de2d60e48d10f5d0db` and follow-ups expand the styled overlay to preserve ASS/SRT colors and nine-position alignment and expose outline width; this is distinct from the later iOS subtitle-state fix.
- Mew `SubtitleAudioModels.kt` already exposes subtitle text/background/outline colors, outline enable/width, shadow, font family/size and bottom offset. The inspected Mew player does not contain upstream's `StyledSubtitleCues` / `SubtitleCuePresenter` implementation at the corresponding paths; Mew uses its own player/MPV subtitle pipeline.
- Classification: **INTENTIONAL PARALLEL / DIFFERENT RENDERING PIPELINE** with a potential fidelity gap. Decision: **NEEDS INVESTIGATION**, not a direct port. Audit Mew's Android and iOS MPV subtitle render paths, ASS override support, SRT alignment, colors, outline rendering and external subtitle behavior before changing anything. Preserve native subtitle rendering where it already supports these features; avoid double-rendering or regressions in playback/subtitle track selection.

### MDBList episode runtime projection

- Upstream commit: `a65cd8b2e42d7170d47cace44e5ebd6f03a053eb` prevents a series' runtime from being used as an individual episode duration in MDBList progress.
- The corresponding upstream `MdbListProgressProjection.kt` path and MDBList projection were not found in the inspected Mew `Main-dev` source. A full feature-tree inventory is still required before declaring MDBList absent.
- Decision: **NEEDS INVESTIGATION**. If Mew has an MDBList progress adapter elsewhere, verify movie-vs-series-vs-episode duration semantics and add a regression test; otherwise record as an upstream-only feature rather than importing a new provider integration opportunistically.

### Hero mute button alignment

- Upstream commit: `f28929d3e3cc0d354de1dd921efc539bbf8a14cf` anchors the details-hero mute control to the native navigation bar button position on iOS, with a platform expect/actual anchor API.
- Mew `DetailHero.kt` still uses status-bar top inset and fixed trailing padding; Mew `PlatformInsets.ios.kt` has no native bar anchor equivalent in the inspected source.
- Classification: **SHARED PATTERN / DIFFERENT NAVIGATION AND HERO LAYOUT**. Decision: **INVESTIGATE / ADAPT CANDIDATE**. Audit Mew's native navigation bar variants, landscape/iPad layout, safe-area and hero mute visibility before adding a platform anchor. Keep Android behavior and Mew's existing hero chrome unchanged unless the iOS geometry requires it.

No application source changes. Tests/builds/CI/device verification: **NOT RUN**.


## Provider protocol, YouTube-only streams, and media-server parity — 2026-11-18

### Stremio `ytId`-only stream support

- Upstream `enhanced` currently at `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03` includes `StreamItem.ytId`, `youTubeIdToResolve`, `YouTubeStreamResolver` and parser/resolver regression tests (`StreamYouTubeTest`, `YouTubeStreamResolverTest`).
- Mew `Main-dev` `StreamParser.kt` reads `url`, `infoHash`, `externalUrl`, and `clientResolve`, but not `ytId`; its "must have at least one source" guard drops an otherwise valid ytId-only stream. Mew `StreamModels.kt` has no `ytId` / `youTubeIdToResolve`; `hasPlayableSource` and `isSelectableForPlayback` consequently cannot accept it. Mew `PlayerScreenRuntimeSourceActions.kt` has no YouTube stream resolver path.
- Mew does already own reusable YouTube extraction infrastructure: common `TrailerPlaybackResolver` expect, `fullCommonMain` `InAppYouTubeExtractor`, Android full and iOS full extraction platform implementations. Play Store variants deliberately return null for in-app extraction, so the adaptation must preserve external watch-page fallback for those variants.
- Classification: **MISSING PROTOCOL COMPATIBILITY / REUSABLE SHARED EXTRACTION INFRASTRUCTURE**. Decision: **TAKE / ADAPT**. Add optional `ytId` parsing/model semantics, only resolve when no direct URL/external URL/torrent/clientResolve is present, and route through the existing trailer resolver; if in-app extraction is unavailable or disabled, convert to a safe external YouTube watch URL. Preserve normal direct/torrent/debrid precedence. Add parser, model-selectability, precedence, trimmed-ID, extraction-failure and full-vs-Play-Store/iOS variant tests. Do not copy a second extractor or make YouTube extraction a prerequisite for normal streams.

### Stream fetching and request lifecycle architecture

- Mew `StreamFetchSupport.kt` is a 42 KB multi-provider adapter containing CloudStream task flattening, route/search/load conversion, and conversion of CloudStream results into Mew `StreamItem` groups. Upstream's same-path file is a 5 KB generic plugin scraper adapter; it does not contain Mew's CloudStream runtime bridge.
- Mew `StreamsRepository.kt` and `PlayerStreamsRepository.kt` have substantially expanded provider orchestration, request-token/current-request guards, cache-aware group publication and Mew-specific CloudStream/metadata paths. Upstream uses smaller active-job cancellation flows and now adds media-server stream sources.
- Classification: **INTENTIONAL PARALLEL / DIFFERENT PROVIDER AND LIFECYCLE CONTRACTS**. Decision: **KEEP Mew architecture**. Port only isolated protocol or bug fixes after comparing request identity, cancellation, cache freshness, partial results, provider grouping, CloudStream route semantics and diagnostics. Do not replace either Mew repository with the smaller upstream file.

### Jellyfin / Emby media-server providers

- Upstream has a dedicated `features/servers` architecture: `ServerProvider`, `ServerRepository`, `ServerStreams`, `ServerPlayback`, shared MediaBrowser client/provider, and separate Jellyfin/Emby provider implementations. It supports server libraries, title search/matching, candidate selection, playback preparation, track/subtitle metadata, playback reporting and optional user-state sync.
- Mew's `core/network/ServerConfiguration.kt` is the app's backend/auth configuration (official/custom backend URL and capabilities), not a Jellyfin/Emby media-server provider. Mew `Main-dev` tree has no `features/servers` or equivalent media-library provider package.
- Classification: **UPSTREAM-ONLY FEATURE / NO EQUIVALENT Mew PROVIDER**. Decision: **NEEDS PRODUCT/ARCHITECTURE INVESTIGATION**, not a small parity patch. A future adaptation must separately audit credential/token storage, server URL validation and cleartext policy, redirects/SSRF exposure, profile-scoped connection sync, library metadata authority, watch-state import/export, playback session lifecycle and iOS/Android transport behavior. Do not conflate media-server connections with Mew's existing custom backend setting.

### Next-episode source fallback — exact Mew behavior

- Upstream merge `ec726ddfd87392df0062fe03ac8cae24a2bff00a` adds a bounded `resolveWithSourceFallback` run and per-source identity tracking for auto-selected next-episode candidates.
- Mew `PlayerNextEpisodeAutoPlay.kt` selects a single stream using current-provider/binge-group preference and `StreamAutoPlaySelector`, then calls `onEpisodeStreamSelected`. Mew `PlayerScreenRuntimeSourceActions.kt` resolves direct-debrid sources asynchronously; on stale resolution it refreshes the episode streams but does not retry a ranked alternative. Mew has `playerSourceIdentityKey()` for direct, torrent and debrid identities, but no `PlayerSourceFallbackRun` or tried-source state in `PlayerScreenRuntimeState`.
- Mew's `streamAutoPlayNextEpisodeFallbackEnabled` only controls whether next-episode selection can fall beyond the preferred provider/binge-group set; it is not equivalent to upstream's playback-failure fallback. Keep those controls semantically separate.
- Classification: **MISSING FAILURE-RECOVERY BEHAVIOR / SHARED NEXT-EPISODE FLOW**. Decision: **ADAPT** after designing the candidate ordering against Mew's selector. Resolve only auto-selected candidates; preserve manual selection, downloaded/P2P/external/server sources, current-provider and binge-group preferences, direct-debrid cache/UNKNOWN semantics, cancellation, request tokens, and a strict bounded attempt count. Tests must cover expired/not-cached/error outcomes, fallback disabled, candidate identity collisions, repeated provider streams, cancellation, manual selection and episode transition. Do not retry authentication/configuration errors as if they were stale media links.

No application source changes. Tests/builds/CI/device verification: **NOT RUN**.


## Localization, RTL and iOS subtitle direction — 2026-11-18

### Locale coverage

- Mew `Main-dev` currently has Compose resource locales: ar, bg, cs, de, el, es, fr, he, hr, hu, id, in, it, ja, nb, nl, pl, pt, pt-rBR, ro, ru, sk, tr and vi.
- Upstream `enhanced` additionally ships bn (Bengali), ta (Tamil), uk (Ukrainian), ur (Urdu) and zh (Chinese). Mew already has Arabic and Hebrew resources; the missing locales are a translation-coverage gap, not proof of broken RTL handling.
- Decision: **ADAPT / TRANSLATION BACKLOG**. Before importing resource files, compare key coverage, formatting placeholders, plurals, new Mew-only settings and terminology. Do not copy upstream strings wholesale or mark untranslated English as complete localization.

### Android subtitle RTL

- Mew already has `androidMain/.../PlayerSubtitleRtlFix.kt`, with Arabic line wrapping and legacy Hebrew punctuation correction. The upstream Android implementation has the same core behavior; differences inspected are mainly annotations/formatting.
- Classification: **ALREADY IMPLEMENTED / SHARED ALGORITHM**. Decision: **SKIP code port**. Keep Mew's implementation; add focused tests for Arabic spans/line boundaries, Hebrew punctuation and timed-cue preservation because Mew's current tree has no dedicated RTL-fix test file.

### iOS external Hebrew subtitle correction

- Upstream adds `iosApp/iosApp/Player/MPVSubtitleRtlFix.swift`, which detects legacy-ordered Hebrew SRT/VTT lines using a conservative minimum-line and leading-vs-trailing punctuation threshold, then swaps punctuation runs while preserving tagged subtitle structure.
- Mew has no equivalent Swift RTL helper in its iOS source tree. Its native MPV bridge handles external subtitle loading directly; no RTL transform was found in the inspected Mew bridge.
- The upstream bridge inspected also did not contain a direct `MPVSubtitleRtlFix.fix` call, and the iOS playback test inspected did not cover Hebrew. Therefore the helper's production call-site/wiring is **NOT CONFIRMED** by this audit.
- Decision: **NEEDS INVESTIGATION**, not yet TAKE. Trace the upstream helper's actual caller and test wiring, then compare with Mew's external subtitle download/sidecar lifecycle. If wired and behavior is sound, adapt only for legacy-ordered Hebrew external files; preserve correctly authored subtitles and avoid touching embedded tracks or ASS content. Add Swift unit tests for detection threshold, punctuation/bracket swapping, tagged SRT/VTT lines, CRLF and no-op behavior.

### Floating navigation RTL

- Mew `FloatingNavigationBar.kt` already reads `LocalLayoutDirection`, maps logical indices to mirrored visual positions with `visualNavIndex` / `logicalNavIndex`, and mirrors the motion/selection geometry.
- Upstream adds `FloatingNavigationBarRtlTest` covering visual index mirroring and glass behavior. Mew's tree has no dedicated floating-navigation RTL test.
- Classification: **ALREADY IMPLEMENTED / TEST-COVERAGE GAP**. Decision: **SKIP implementation port**; adapt the upstream test intent to Mew's own navigation behavior, including selection position, tap-to-destination mapping, swipe/drag and motion cancellation under RTL.

No application source changes. Localization validation, tests/builds/CI, iOS simulator and device verification: **NOT RUN**.


## Android startup and first-home rendering — 2026-11-18

### Coordinated startup preload

- Upstream `enhanced` adds `core/startup/StartupPreloader.kt`; Android `MainActivity` holds the system splash with `setKeepOnScreenCondition { !contentReady }`, waits for `StartupPreloader.awaitReady()`, then mounts Compose content. Its preload sequence loads cached profiles, library, watched state, watch progress, continue-watching preferences, player/P2P settings, media servers, detail settings, release notifications, Live TV and a Home snapshot. It logs individual failures and total duration; the activity cancels its startup scope on destroy.
- Mew `MainActivity` initializes its Android storage/platform adapters synchronously, then immediately mounts `App()`; Mew's custom AppGate/launch UI and asynchronous auth/profile gate own the visible startup experience. No coordinated repository preloader or splash keep condition exists in Mew.
- Mew `ProfileRepository.loadCachedProfiles()` already moves SharedPreferences/JSON decoding to `Dispatchers.IO`; upstream's current implementation does not show that same explicit IO dispatch in this method. Do not regress this Mew improvement.
- Mew `HomeScreen` starts network/addon/CloudStream/collection/watch-progress initialization and triggers Home refresh when visible. Several repository `ensureLoaded()` methods have provider registration, disk reads, source selection and observer/network side effects; they are not all safe to move into a splash-blocking preload.
- Classification: **SHARED STARTUP GOAL / DIFFERENT APP-GATE AND REPOSITORY LIFECYCLE**. Decision: **ADAPT selectively**. First profile actual cold-start timings and identify only cheap, local, idempotent reads required by the initial route. Keep network/provider preparation lazy or parallel, preserve Mew's auth/profile gating and custom launch UI, and never block splash on remote provider availability. Add timeout/cancellation and per-step timing; test cold/warm launch, anonymous/authenticated accounts, profile switching, offline startup and process recreation. Upstream has no dedicated StartupPreloader unit test in the inspected tree.

### Persistent Home snapshot

- Upstream `HomeSnapshot.kt` stores a profile-keyed JSON snapshot of Home sections and hero metadata through `AppFileCache`. Startup preloads it; `HomeRepository.restoreSnapshot()` publishes cached rows immediately while a fresh catalog request continues. Snapshot targets currently serialize Addon and Server catalog targets; unsupported target kinds are skipped. The payload records `savedAtMillis`, but the inspected load path validates profile identity and decoding, not snapshot age.
- Mew `HomeRepository.kt` has in-memory `cachedSections`, `cachedCloudSections`, collection hero and localized artwork caches, plus progressive publication and request-key guards. It has no equivalent persistent Home snapshot store/restore path. Mew's Home flow includes CloudStream fallback/selection semantics absent from upstream's simpler repository.
- Classification: **MISSING PERSISTED FIRST-PAINT CACHE / INTENTIONAL HOME PIPELINE DIFFERENCE**. Decision: **ADAPT candidate**. Design a profile-scoped snapshot with explicit TTL/versioning and stale-data policy; preserve Mew's CloudStream and addon catalog identity/request keys, custom ordering, localization and hero metadata. Do not serialize unsupported target kinds or show stale private/profile data after account/profile changes. Test profile isolation, malformed/old snapshots, addon removal/renames, CloudStream provider changes, offline rendering, fresh-response replacement and cache-write cancellation.

No application source changes. Startup benchmarks, tests/builds/CI, and physical-device cold/warm launch verification: **NOT RUN**.


## DNS-over-HTTPS and Android networking resilience — 2026-11-18

### DNS resolver behavior

- Mew `Main-dev` uses OkHttp's `DnsOverHttps` implementation. `ConfiguredDnsOverHttps` rebuilds/caches the resolver instance when the selected provider changes; its resolver wrapper has no explicit per-host answer cache. It supports Google, Cloudflare, AdGuard, DNS.WATCH, Quad9, DNS.SB and Canadian Shield. `None` uses `IPv4FirstDns`.
- Upstream `enhanced` uses a custom `NuvioDns` / `DohClient` / DNS wire-message parser. It caches DNS answers by provider+hostname using returned TTL (clamped to 60–3600 seconds), queries A and AAAA, and clears the cache when provider changes. It supports System, Cloudflare, Google, AdGuard and Quad9.
- Important semantic difference: upstream falls back to system DNS when a DoH request throws `IOException`; Mew's configured DoH resolver propagates lookup failure rather than silently leaking the hostname to system DNS. Mew therefore has a stricter privacy/fail-closed behavior for selected DoH providers. Upstream treats NXDOMAIN/empty answers as unknown host, not a system fallback.
- Mew's DNS setting is persisted for Android and iOS, but the actual custom OkHttp resolver is Android-specific; upstream explicitly reports iOS DNS as unsupported and uses platform defaults there.
- Classification: **SHARED PATTERN / DIFFERENT PRIVACY AND FAILURE SEMANTICS**. Decision: **KEEP Mew resolver; ADAPT only after focused DNS benchmarks/tests**. TTL caching may reduce repeat DoH latency, but preserve Mew's selected-provider no-system-fallback guarantee unless the user explicitly chooses a fallback policy. Do not replace OkHttp's maintained DoH implementation with upstream's custom DNS packet parser without security review.
- Missing verification coverage in both inspected trees: no dedicated DNS resolver test files. Add tests for provider changes/cache invalidation, TTL expiry, A/AAAA merging, IP literals/localhost, bootstrap resolution, malformed/truncated answers, NXDOMAIN, network failure and no-system-fallback privacy behavior.

### HTTP client differences

- Upstream adds a Ktor API client with redirects disabled at both Ktor and OkHttp levels, retry-on-connection-failure disabled, explicit connect/read/request timeouts, and a bounded response-body reader (16 MiB default).
- Mew's shared Android OkHttp base is intentionally small and supplies the live configured DNS resolver; callers derive clients with `newBuilder()`. Mew also has dedicated CloudStream/provider request paths, so upstream's API client is not a drop-in replacement for those transports.
- Classification: **SHARED NETWORKING CONCERNS / DIFFERENT TRANSPORT OWNERSHIP**. Decision: **NEEDS CALL-SITE AUDIT** before porting timeout, redirect, retry or response-size policies. Trace every Mew Ktor/OkHttp/CloudStream/Supabase client and its redirect/auth/header behavior first; do not silently change provider compatibility or weaken TLS/hostname checks. Consider bounded body reads only for Mew-owned API endpoints with known response contracts.

No application source changes. DNS/network tests, benchmarks, build/CI and device verification: **NOT RUN**.

## Startup performance and Compose lifecycle audit — 2026-11-18

### Mew startup call-site findings (Main-dev `69b4a96072b506f60dcbe538c5f6b71db3a4a464`)

Inspected MainActivity.kt, App.kt, ProfileRepository.kt, PlayerSettingsRepository.kt, LibraryRepository.kt, and WatchedRepository.kt plus MainAppContent initialization call sites.

- AppGate retains Mew's launch/auth/profile-selection flow. On AuthState.Authenticated, it calls ProfileRepository.ensureLoaded(userId) from a Compose LaunchedEffect; a separate LaunchedEffect for the authenticated user also calls ensureLoaded and then pullProfiles(). The ensure call is repeated but should be idempotent; synchronous disk/decode work remains a potential main-dispatcher stall.
- ProfileRepository.loadCachedProfiles() already uses Dispatchers.IO for cached SharedPreferences/JSON loading. Preserve this existing IO path and consolidate/bridge the authenticated ensure path rather than creating a second profile cache loader.
- MainAppContent initializes repositories in Compose remember blocks before collecting their StateFlows: AddonRepository.initialize(), LibraryRepository.ensureLoaded(), NuvioEnhancedSettingsRepository.ensureLoaded(), PlayerSettingsRepository.ensureLoaded(), P2pSettingsRepository.ensureLoaded(), WatchedRepository.ensureLoaded(), and DownloadsRepository.ensureLoaded(). This is a mixed set of local restoration and lifecycle/provider activation; do not move it wholesale into a splash-blocking preloader.
- PlayerSettingsRepository.ensureLoaded() is idempotent via hasLoaded, but its first call invokes loadFromDisk() synchronously and reads a large set of player/subtitle/platform settings before publishing. Candidate for suspend/IO-backed restore or early preload, provided platform storage APIs are safe off-main and mutations cannot race the load.
- LibraryRepository.ensureLoaded() is not a pure disk restore: it registers/loads tracking providers and settings, loads profile-scoped local library data, prepares connected providers and schedules async refresh. Classification: SHARED PATTERN / DIFFERENT SEMANTICS. Split local restore from activation before preloading; retain provider preparation and refresh after active profile/source is known.
- WatchedRepository.ensureLoaded() similarly combines local disk load with tracking registry/settings initialization, effective-source activation and provider snapshot observer startup. Split restore from source activation/observer lifecycle; do not run it as an undifferentiated startup task.
- P2pSettingsRepository and DownloadsRepository are invoked from the same Compose initialization pattern, but their implementations and platform storage/lifecycle contracts were not fully inspected in this pass. Mark as NEEDS INVESTIGATION rather than assuming safe preload candidates.
- NuvioEnhancedSettingsRepository.ensureLoaded() is invoked from more than one remember block in MainAppContent; repository idempotency and duplicate state collection need a caller audit. Do not infer duplicate repository implementations from repeated call sites.

### Upstream comparison and architecture decision

Upstream StartupPreloader coordinates cached profile, library, watched, watch-progress, player/P2P settings, media-server, detail, release-notification, Live TV and Home snapshot initialization before Compose is mounted. Its current implementation is sequential and its timeout only logs; after timeout it continues awaiting the preload job. Several invoked ensure/load methods also have side effects.

Decision: **ADAPT — coordinated, non-blocking startup orchestration; do not copy upstream's sequential splash gate.**

Recommended Mew design:
1. Keep essential platform initialization and Mew's AppGate/launch UI unchanged.
2. Introduce one idempotent startup coordinator that starts independent, cheap local restores concurrently on an IO/default dispatcher, with per-task timing and bounded waiting.
3. Let Compose render immediately and observe repository StateFlows; cached profile/settings/Home state should become available progressively.
4. Separate repository local restore from profile/provider activation. Start tracking observers, provider preparation, remote sync, addon/network refresh and P2P engine work only when their owning lifecycle/profile requires them.
5. Guard every profile-scoped result with user/profile generation; cancel obsolete jobs on logout, account change, profile switch and Activity destruction. Avoid stale data publication after a switch.
6. Remove duplicate ensure calls only after confirming idempotency and all callers. A single coordinator must not become a second source of truth or duplicate repository initialization.
7. Measure cold/warm startup and first usable Home frame before/after. Add tests for offline launch, auth transitions, remembered profile, PIN-gated profile, account/profile switching, cancellation, corrupted cache and slow/failing individual tasks.

### Duplicate classification

- Profile ensure calls in two AppGate effects: **SHARED INITIALIZATION CALL / POTENTIAL REDUNDANT INVOCATION**, not duplicate implementation; verify ordering before consolidation.
- Multiple MainAppContent ensureLoaded() calls: **INTENTIONAL REPOSITORY INITIALIZATION PATTERN** where repositories have distinct state/side effects; do not merge repositories.
- Library/Watched initialization: **SHARED PATTERN / DIFFERENT SEMANTICS**; each owns distinct persistence and provider lifecycle.
- Player settings load: candidate for pure local preload, pending storage-thread-safety review.
- No evidence in this pass that Mew and upstream have identical startup architecture or that upstream's preloader is safe to transplant directly.

### Verification boundary

Read-only audit only. No application source, tests, Gradle files, workflows or branch refs were changed. Builds/tests/CI/device benchmarks: **NOT RUN**. Main-dev and upstream enhanced were not modified. upstream-sync documentation update is limited to this audit entry.
### Startup candidate safety audit — repository storage and side effects (2026-11-18)

Inspected the common repositories and Android/iOS storage actuals for Player settings, P2P settings, Downloads and Nuvio Enhanced settings.

- PlayerSettingsRepository.ensureLoaded() synchronously calls loadFromDisk() once. The loader performs many individual PlayerSettingsStorage reads and publishes one consolidated state. Android actual uses SharedPreferences; iOS actual uses NSUserDefaults. Both are synchronous key-value stores. Candidate for an asynchronous restore only after confirming Kotlin/Native threading constraints and ensuring setters/profile changes cannot race or be overwritten by a late load. Keep platform actuals and persisted keys unchanged.
- P2pSettingsRepository.ensureLoaded() is a small, pure profile-scoped key-value restore (five values, enum normalization, StateFlow publish). Android uses SharedPreferences and iOS uses NSUserDefaults. It does not start the P2P engine. Classification: SAFE PRELOAD CANDIDATE, low expected cost; include only after active profile identity is fixed, and retain lazy fallback for settings screens.
- NuvioEnhancedSettingsRepository.ensureLoaded() reads one profile-scoped JSON payload, normalizes legacy/feature IDs and publishes state. It is idempotent but has no synchronization around hasLoaded and is called from multiple MainAppContent remember blocks. Classification: POTENTIAL REDUNDANT INVOCATION / SHARED SETTINGS OWNER, not duplicate implementation. Consolidate collection and initialization at one owner; make load concurrency-safe before introducing parallel preloading.
- DownloadsRepository.ensureLoaded() is not a pure restore. loadFromDisk() decodes the persisted queue, normalizes interrupted Downloading items to Paused, resolves completed file URIs, filters missing files, notifies platform live status, may promote a waiting item, and may persist normalized state. Promotion can start a real transfer. Classification: LIFECYCLE-ACTIVE RESTORE; do not run as generic startup preload before profile/queue policy is ready. Preserve interrupted-download recovery and avoid accidental transfer starts before the app has established the selected profile and network policy.
- DownloadsStorage payload is profile-scoped on both Android SharedPreferences and iOS NSUserDefaults. The persisted queue may be sizable; measure payload size/decode cost before deciding whether to split metadata/progress or move decode off-main. Do not change serialization or cleanup semantics as part of a performance-only port.
- P2pSettingsStorage is also profile-scoped on both platforms. The engine implementation remains platform-specific (Android and iOS full/store source sets); settings restoration must not initialize or start that engine.

Refined preload tiers:
1. Before Compose: only essential platform setup and cached profile data already handled through the IO path.
2. Immediately after the active profile is known: P2P/player/Nuvio settings and other pure local state, concurrently where storage APIs and repository synchronization permit.
3. After profile activation: local Library/Watched restoration separated from provider activation; restore Home snapshot and render cached sections.
4. Lifecycle-owned/background: Downloads queue recovery, addon/provider observers, remote sync, stream/provider preparation and P2P engine work.

Decision: **ADAPT** the startup coordinator with explicit task tiers and per-repository restore/activate contracts. Do not treat all ensureLoaded() calls as equivalent. No source changes; P2P settings are a low-risk candidate, Player/Nuvio settings require concurrency guards, Downloads must remain lifecycle-gated.

Verification boundary: source inspection only. No timing benchmark, build, tests, CI or device verification. No app source changes.
### iOS foreground/background lifecycle parity — 2026-11-18

Inspected Mew and upstream common lifecycle contracts, iOS and Android actuals, and the Mew App.kt consumer.

- Mew common contract exposes Flow<Unit>; Android emits on ProcessLifecycleOwner.ON_START, while iOS emits only UIApplicationDidBecomeActiveNotification. The iOS callback observer is correctly removed in awaitClose.
- Upstream expands the contract to Flow<AppVisibility> with explicit Foreground and Background states. Its iOS actual emits the current UIApplication.sharedApplication.applicationState immediately, then observes both UIApplicationDidBecomeActiveNotification and UIApplicationWillResignActiveNotification, applying distinctUntilChanged(). Its Android implementation is a separate platform actual and must be checked before treating this as a shared drop-in.
- Mew's inspected App.kt consumer only reacts to foreground events: it requests a network-status refresh and retries authenticated device-session registration. It does not currently consume a background event. Therefore, the current one-way contract is sufficient for this caller; changing it now would widen API scope without a demonstrated Mew behavior requirement.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Upstream has a richer app-visibility contract; Mew currently uses a foreground-refresh signal. Not a duplicate implementation.
- Decision: **ADAPT LATER / NEEDS CALLER REVIEW**, not an immediate port. If Mew adds background-sensitive behavior (for example pausing eligible sync/expensive work), introduce an explicit visibility state and migrate all platform actuals and consumers together. Do not infer that iOS should cancel downloads or playback merely because the app resigns active; background audio, PiP, downloads and native player lifecycles have separate policies.
- iOS app entry differs too: Mew's MainViewController() initializes crash diagnostics and CloudStream platform storage before mounting App(); upstream's controller factory additionally wires native profile switcher/biometric and auto-sync setup for its richer native navigation entry points. These are intentional product/platform integrations, not safe startup boilerplate to copy. Preserve Mew's initialization ordering and inspect each native controller caller before any change.

Verification boundary: read-only source inspection. GitHub code-search returned no useful indexed matches, so caller scope was verified through the recursive tree and direct source reads; no claim of exhaustive repository-wide usage beyond inspected files. No app source changes, builds, tests, CI or iOS device verification.
### iOS background download architecture — 2026-11-18

Compared Mew's iOS downloader, AppDelegate callbacks and lifecycle behavior with upstream's iOS downloader.

- Mew uses a coroutine-owned NSURLSessionDataTask and writes response chunks into a .part file. On UIApplicationDidEnterBackground, its AppDelegate calls pauseDownloadsForAppBackground(), which delegates to DownloadsRepository.pauseActiveDownloads(). Its partial-file/range-request flow can resume from the current .part byte count when the app later starts the transfer again.
- Upstream uses a dedicated background NSURLSessionDownloadTask coordinator (identifier com.nuvio.app.downloads), registers/recreates the session when iOS delivers background-session events, and has pauseDownloadsForAppBackground() as a no-op. The OS can continue eligible transfers while the app is suspended/terminated and relaunch the app for completion handling. The upstream downloader also explicitly tracks subtitle-preparation jobs per download and cancels them with the owning download.
- Both projects retain the UIApplicationDelegate background-session completion-handler entry point, but Mew's inspected handler only stores the callback; upstream additionally ensures its background session is created. This is a material contract difference, not a cosmetic implementation detail.
- Classification: **INTENTIONAL PARALLEL / DIFFERENT LIFECYCLE GUARANTEES**. Mew's foreground-pause + .part/range-resume policy is simpler and predictable while the app is active; upstream's OS-managed background session provides a stronger background-completion capability and different transfer lifecycle.
- Decision: **ADAPT — HIGH-PRIORITY INVESTIGATION**, not a direct file port. Evaluate whether Mew should support background completion on iOS, including user expectation, App Store behavior, background-session identifier migration, existing active-task reconciliation, callback delivery after process relaunch, partial-file cleanup, range/resume semantics, cellular/Wi-Fi policy, auth headers/cookies, cancellation, queue ordering, and Live Activity updates. Preserve Mew's download queue/UI/profile isolation and current explicit pause policy unless a tested replacement is ready.
- Important: upstream's background NSURLSessionDownloadTask path has different persistence and resume semantics from Mew's NSURLSessionDataTask writing to a .part file. Do not combine the implementations or simply change pauseDownloadsForAppBackground() to no-op; that would risk losing Mew's established pause/recovery guarantees.
- The iOS AppDelegate's applicationDidEnterBackground callback is a specific download policy. It should not be replaced by a generic Compose AppVisibility event unless lifecycle ordering and background execution behavior are proven equivalent.

Verification boundary: source inspection only. Compared the downloader entry points and lifecycle callbacks; did not execute iOS builds, background transfer tests, device/simulator tests or App Store background-session validation. No application source changes.
### iOS native player, PiP and audio-session parity — 2026-11-18

Compared Mew and upstream iOS Compose player effects, Kotlin player bridge, Swift MPV bridge, Metal layer, Now Playing controller, app delegate and upstream native PiP/audio-session components.

- **Picture-in-Picture:** Mew's PlayerPlatformEffects.ios.kt implements ManagePlayerPictureInPicture() as a no-op and rememberIsInPictureInPicture() as false. Mew's inspected MPVPlayerBridge.swift and MetalLayer.swift have no AVPictureInPictureController/sample-buffer sink/parking integration. No Mew iOS PiP implementation was found in the inspected iOS player paths.
- Upstream also leaves the shared Compose ManagePlayerPictureInPicture() hook as a no-op; its actual PiP is a native Swift subsystem: PlayerPictureInPictureController uses AVPictureInPictureController with AVSampleBufferDisplayLayer and AVPictureInPictureSampleBufferPlaybackDelegate, receives frames from MetalLayer, handles scene activation and inline handoff, and coordinates with PlayerPictureInPictureParking to detach/re-attach the player route while browsing. It also has subtitle composition and a Dolby Vision conversion path. Therefore this is not a Compose-level feature or a direct port of PlayerPlatformEffects.
- **Classification:** Mew iOS PiP is **MISSING** relative to upstream. Upstream implementation is a substantial native subsystem and has navigation/lifecycle coupling. **Decision: ADAPT as a dedicated project, not a small sync patch.** First inventory Mew's UIKit/Compose navigation host, player route ownership, Metal render output and player teardown before porting. Preserve Mew's player UI and route behavior; don't copy upstream parking/navigation types wholesale. Treat HDR/Dolby Vision conversion and subtitle composition as separate stages with visual/performance tests.
- **Now Playing:** Mew already has PlayerNowPlayingController with metadata/artwork caching, remote command targets, playback-position drift throttling and cleanup. Upstream has substantially the same architecture; it adds a force-refresh option to syncPlayback() and ties republishing to audio-session reconfiguration. Classification: **ALREADY IMPLEMENTED / SHARED PATTERN**, with small upstream hardening candidates. Adapt only if a concrete missed state/metadata refresh is reproduced; do not duplicate another Now Playing owner.
- **Audio session:** Mew's MPVPlayerBridge directly sets AVAudioSession category .playback, mode .moviePlayback and activates it, then deactivates during teardown. Upstream extracts this into PlayerAudioSessionController, observes AVAudioSession interruptions and CXCallObserver, mixes with active calls, selects .longFormVideo route-sharing policy when not mixing, and re-publishes cached Now Playing state after reconfiguration. Classification: **SHARED PATTERN / DIFFERENT LIFECYCLE AND CALL-MIXING SEMANTICS**. Decision: **ADAPT selectively** after deciding Mew's call-interruption and call-mixing policy; don't blindly adopt mixing behavior because it changes audio privacy/UX and route-sharing behavior.
- **Orientation and controls:** Mew currently locks iOS player to landscape and reports orientation toggle unsupported. Upstream adds reference-counted landscape lock plus portrait/landscape preference notifications and supports the toggle on iPhone. This is an intentional UX choice, not a correctness fix. Decision: **INVESTIGATE UX PARITY** separately; preserve Mew behavior until the intended iPhone rotation control is approved.
- External-player launchers are intentionally platform-specific and not equivalent to native MPV/PiP. Mew's Infuse, VLC, Outplayer and VidHub URL-scheme support should remain separate from any native PiP work.

Verification boundary: source-level comparison only. GitHub code-search indexing returned no useful matches, so conclusions are limited to the inspected iOS source paths and recursive trees. No iOS build, simulator/device playback, PiP, call-interruption, HDR, subtitle or navigation restoration tests were run. No application source changes.

## UI/UX parity audit — navigation, home, search, details and library — 2026-11-18

Compared the current Mew `Main-dev` UI implementations against upstream `enhanced` at the verified refs above. This is source-level review, not screenshot/device validation.

### Navigation and tab lifecycle

- `core/ui/FloatingNavigationBar.kt` is identical between the inspected branches (686 lines). Mew already contains the same JellyMotion selection, RTL visual-index mapping, liquid-glass selection surface, label animation, glow and scroll-bounce behavior. Classification: **ALREADY IMPLEMENTED**. Do not port a duplicate navigation bar.
- `RootTabHost.kt` is semantically identical in the inspected implementation: visited-tab retention, SaveableStateHolder, child LifecycleOwner capped at RESUMED/CREATED, focus clearing, inactive pointer consumption and semantics suppression. Classification: **ALREADY IMPLEMENTED**.
- Upstream's `SettingsScreen.kt` additionally gates page rendering and back handling on `screenActive` / `isSelectedTab`, and uses `ScreenActivityEffect` for requested-page effects. Mew has tab lifecycle owners through RootTabHost but its inspected SettingsScreen call sites use ordinary LaunchedEffect and root-action policy. This is a **SHARED PATTERN / DIFFERENT SCREEN-LIFECYCLE WIRING** candidate: audit all settings entry points and nested pages before adapting; avoid suppressing deep-link/requested-page handling when a tab is inactive.

### Home hero and search

- Mew's `HomeHeroSection` is a substantial fork-specific implementation with selectable hero display modes, poster-art and streaming-showcase variants, rating/metadata controls, trailer enrichment and user-configurable presentation. Upstream's current hero adds distinct native-tab/vertical-bar anchoring, artwork sizing behavior for portrait/landscape/tablet, and trailer lifecycle/position handling. Classification: **INTENTIONAL PARALLEL / DIFFERENT PRODUCT UX**. Preserve Mew's hero modes and settings; selectively evaluate upstream's responsive sizing and native navigation anchoring on iOS/iPad/Android tablets. Do not replace the file.
- Mew's `SearchScreen.kt` keeps the search field visible in a sticky header and renders discover content beneath it. Upstream uses a collapsible search field with an explicit search action, focus-driven recent-search visibility, a separate discover-filter row, native-tab auto-hide, and refresh behavior that retries addon manifest failures before refreshing discovery. These are meaningful UX and failure-state differences, not duplicate code. Decision: **INVESTIGATE / ADAPT selectively**. Test keyboard/focus, back dismissal, scroll position, discover filter persistence, addon failure/retry, accessibility labels and native-tab overlap before choosing whether Mew should adopt the collapsible interaction. Preserve Mew's existing query/discover behavior unless parity is explicitly chosen.
- Mew's `HomeScreen.kt` is longer and contains additional CloudStream provider selection, profile-aware continue-watching enrichment/cache behavior and Mew-specific home sections. Upstream's screen uses a different lifecycle helper and scroll-stop behavior. Classification: **INTENTIONAL PARALLEL / DIFFERENT DATA AND LIFECYCLE SEMANTICS**. Compare the screen-active effects and stale-scroll handling separately; do not transplant HomeScreen wholesale.

### Details and library presentation

- Upstream `MetaDetailsScreen.kt` adds an episode-poster zoom anchor/backdrop-source transition and explicitly stops the hero trailer before navigation/play actions. Mew has its own action sheets, trailer popup, request/error/offline handling and hero scroll/header behavior, but the inspected code does not contain the same episode zoom-anchor transition. Decision: **ADAPT candidate** for the visual transition only after checking Mew's poster components, shared-element/zoom primitives, dialog dismissal, navigation back and TV/tablet interactions. Trailer-stop calls are a separate lifecycle correctness candidate; verify every Mew navigation/play action path before porting.
- Upstream `DetailHero.kt` accounts for the native navigation bar's physical anchor/inset; Mew uses status-bar insets and custom hero chrome placement. This overlaps the earlier hero mute-alignment finding. Decision: **ADAPT platform-aware anchoring** only where a native tab bar is actually present; keep Mew's custom UIKit/profile-avatar layout and avoid applying the upstream offset on Android or non-native navigation layouts.
- Upstream `LibraryScreen.kt` has explicit genre/year/watched filters, provider-order fallback, downloaded-mode section projection and active-screen scroll/network effects. Mew's version has additional release-calendar/radar enrichment, download filters and custom vertical projection but its inspected vertical projection does not expose the upstream genre/year/watched filter state. Classification: **SHARED PATTERN / DIFFERENT SEMANTICS** with a confirmed UI capability gap for genre/year/watched filtering (also recorded in the Library batch). Decision: **ADAPT** filters into Mew's existing projection and display settings rather than replacing its release calendar, cloud/download modes or sorting. Preserve saved filter state and provider ordering.
- Upstream's `TabletDetailHero.kt` is an additional responsive detail presentation path absent from Mew's inspected tree. This is **NEEDS INVESTIGATION**, not automatically a missing feature: verify Mew's tablet breakpoint and existing `DetailHero`/series layout before deciding whether a separate tablet composition is warranted.

### Duplicate audit classification

- FloatingNavigationBar: **ALREADY IMPLEMENTED** (same implementation).
- RootTabHost: **ALREADY IMPLEMENTED** (same lifecycle/state retention behavior).
- Home hero: **INTENTIONAL PARALLEL** (Mew adds independent hero modes/settings).
- HomeScreen: **INTENTIONAL PARALLEL** (Mew-specific CloudStream/profile-aware behavior).
- SearchScreen: **SHARED PATTERN / DIFFERENT UX** (collapsible vs persistent search).
- Details: **SHARED PATTERN / DIFFERENT INTERACTION** (episode zoom and trailer teardown).
- Library: **SHARED PATTERN / DIFFERENT SEMANTICS** (Mew release calendar/download/cloud features; upstream genre/year/watched filters).

### Verification boundary and next work

Read-only source inspection on GitHub. Inspected paired commonMain files: `RootTabHost.kt`, `FloatingNavigationBar.kt`, `HomeScreen.kt`, `HomeHeroSection.kt`, `SearchScreen.kt`, `MetaDetailsScreen.kt`, `DetailHero.kt`, `LibraryScreen.kt`, plus settings screen call sites and the recursive source trees. No source files changed. No screenshot comparison, accessibility run, Gradle build, tests, CI, emulator or device verification. Next UI/UX pass: inspect poster/card components and action sheets/dialogs, TV and tablet layout branches, settings page hierarchy, keyboard/back behavior, and platform-native tab-bar integration; then reconcile all candidates against Mew's existing components and tests.


### Action-sheet and poster interaction audit — 2026-11-18

Compared Mew and upstream common UI components for continue-watching actions, episode watched actions, poster watched overlays and the shared bottom-sheet primitives.

- `PosterActionSheet.kt` is effectively identical in the inspected source: the watched badge/overlay components and accessibility description are aligned. Classification: **ALREADY IMPLEMENTED**.
- Mew's `NuvioContinueWatchingActionSheet` and `EpisodeWatchedActionSheet` intentionally use `NuvioMediaActionOverlay`: cinematic artwork-backed action panels with Mew-specific width, spacing and custom action rows. Upstream uses `NuvioModalBottomSheet`, shared Material bottom-sheet state, drag/dismiss animation and reusable action rows/dividers. Classification: **INTENTIONAL PARALLEL / DIFFERENT PRESENTATION AND DISMISSAL CONTRACT**. Do not replace Mew's overlay with upstream's bottom sheet merely to match filenames; first test touch outside, swipe/back dismissal, focus/keyboard, accessibility traversal, safe-area padding, landscape/TV sizing and animation completion.
- Mew's continue-watching action sheet exposes details, manual play, start from beginning and remove. Upstream additionally exposes `onMarkWatched`. This is a **POTENTIAL ACTION PARITY GAP**, not yet a confirmed missing app capability: trace all Home/Library callers and check whether marking watched is intentionally available elsewhere or whether it should be added to this context menu. If added, use Mew's existing watched/progress repository action and preserve dismiss ordering.
- Mew's episode action sheet includes download and anime tracking actions, and its current callback contract differs from upstream. Upstream includes an episode user-rating row/callback, while Mew's inspected sheet does not. This is another **NEEDS CALLER / PRODUCT INVESTIGATION** item: verify whether Mew exposes episode rating through another UI and whether the rating model/provider supports the same semantics. Do not add a duplicate rating path.
- Mew's action sheets use artwork fallbacks and custom content descriptions. Preserve these while evaluating any upstream shared-row or dismissal improvements. The upstream sheet-state coroutine dismissal must not be copied into the Mew overlay unless its animation and callback lifecycle match.

### Duplicate classification

- Poster watched badge/overlay: **ALREADY IMPLEMENTED**.
- Continue-watching / episode action presentation: **INTENTIONAL PARALLEL** (Mew media-action overlay versus upstream modal bottom sheet).
- Mark-watched and episode-rating actions: **NEEDS INVESTIGATION** until all callers and alternate entry points are checked.
- Download and anime-tracking actions: Mew-specific additions; preserve.

Verification boundary: read-only source inspection of the paired components and shared bottom-sheet primitive. No interaction tests, accessibility tests, screenshots, build, CI or device verification. No source changes.


## Expanded UI/UX and experimental-feature audit — glass, backdrops, transitions, native iOS — 2026-11-18

This pass expanded beyond screen-name matching and searched the recursive source trees for glass, backdrop, blur, transitions, tablet/TV, native-tab, and experimental feature implementations.

### Liquid Glass core and customization

- Mew already has the core `core/ui/glass/LiquidGlass.kt`, `LiquidGlassEffectSettings.kt`, and `LiquidGlassNavigationSurfaces.kt`. The settings model and navigation-surface file were byte-identical at the inspected refs; Mew is not missing Liquid Glass wholesale.
- Mew's `LiquidGlass.kt` is 365 lines vs upstream's 426. The inspected upstream adds `staticFromBackdrop` handling in `ambientLiquidGlass` and a `StaticBackdropGlassElement/Node` that paints a captured `NuvioBackdrop` when live liquid-glass support is unavailable. Mew's ambient helper falls back to its configured solid/brush fallback only. Classification: **SHARED PATTERN / DIFFERENT PLATFORM FALLBACK**. Decision: **ADAPT candidate**; first check Mew's Android/iOS/desktop support gates and whether a static artwork backdrop is available on unsupported OS versions. Preserve Mew's current fallback colors and ensure no stale/incorrect backdrop capture.
- Mew and upstream `PlayerLiquidGlass.kt` share the same video-frame backdrop architecture; Mew's file is 173 lines vs upstream's 167, with Mew-side additions rather than a missing implementation. Classification: **ALREADY IMPLEMENTED / Mew extension**. Platform actuals and player capture lifecycle still need a separate platform-by-platform audit.
- The shared effect-settings model is byte-identical, including corner radius, blur, refraction, chromatic aberration, depth, tint opacity and opt-in effects for tab icons/floating bar. Do not re-port the model.

### Upstream-only app glass experience

- Upstream adds `core/ui/background/AppBackground.kt`, `AppBackgroundSettings.kt`, platform storage/picker actuals, and `features/settings/AppBackgroundSettingsSection.kt`. This provides configurable app-wide local-photo or URL artwork backgrounds, a glass enable switch, and Dark/Balanced/Vivid presets. Mew has profile backgrounds, `ProfileMeshBackground`, and Liquid Glass navigation surfaces, but no equivalent app-wide background configuration/repository/setting in the inspected source tree. Classification: **UPSTREAM-ONLY FEATURE / NEEDS PRODUCT AND PRIVACY REVIEW**. Decision: **INVESTIGATE**; evaluate image persistence, URL loading/SSRF-safe image policy, profile scoping, cache limits, offline behavior, licensing and performance before adoption. Do not conflate profile avatar/background customization with app-wide glass wallpaper.
- Upstream `NavigationBarSettingsSheet.kt` exposes live preview, nav-bar style/position, icon color, glow and two independently adjustable glass effect profiles (floating bar and selection lens), with draft subscriptions scoped to the sheet and synchronous persistence on dismissal. Mew has the same underlying glass settings data model and navigation surfaces, but the inspected Mew source tree does not contain this upstream settings sheet path. Classification: **PARTIAL IMPLEMENTATION / SETTINGS UX PARITY GAP**. Decision: **INVESTIGATE** actual Mew settings entry points and persistence before porting controls; ensure unsupported devices do not expose nonfunctional refraction sliders and retain existing Mew navigation styles.

### Artwork-aware backdrops and focus crop

- Upstream adds `ArtworkBackdrop.kt` (320px decode, crossfade and backdrop-control color adaptation) and `BackdropFocus.kt` (saliency profile used to bias wide-art crop toward salient content), with Android/iOS actuals and common tests. Mew has `DynamicArtworkBackground`, `ProfileMeshBackground`, and `BackdropPaletteImage`, but no same-named artwork-backdrop/focus modules in the inspected tree. Classification: **NEEDS SEMANTIC COMPARISON**; likely complementary rather than duplicate. Decision: **INVESTIGATE / ADAPT** after tracing Mew hero, detail, profile and glass backdrop callers. Potential benefits: reduce repeated image decode, improve text/control contrast over art, and prevent faces/titles being cropped off-center. Preserve Mew's current dominant-color and profile background behavior.
- Upstream has a focused `ArtworkBackdropTest` and `BackdropFocusTest`; Mew's equivalent tests were not found in the inspected tree. Tests are part of the candidate, not optional decoration.

### Navigation and poster transitions

- Upstream adds `NativeTabBarScrollEffect.kt`, `NativeTabBridge` platform actuals, and `iosApp/NuvioGlassTabBar.swift` (336 lines). Swift tab bar supports off/static/auto-hide/morphed modes, measured selection geometry, native glass rendering, and scroll-responsive behavior. Mew has `NativeTabBridge` and `FloatingNavigationBar`, but no `NuvioGlassTabBar.swift` or matching native-tab scroll-effect file. Classification: **UPSTREAM-ONLY PLATFORM IMPLEMENTATION**. Decision: **ADAPT candidate for iOS 26+ only**, with OS availability guards and Compose fallback; inspect Mew's current UIKit tab bar/profile-avatar bridge and navigation/back semantics before any replacement. Do not remove Mew's Android floating bar or assume native Swift glass is available on older iOS.
- Upstream adds `PosterOpenMotion.kt` and `PosterNavigationMotion/Transition.kt`, with motion tests, to create a shared-element-like poster-to-detail opening transition. Mew has `PosterZoomActionOverlay` and `CastSharedTransition`, but no same-named poster navigation motion module. Classification: **NEEDS INVESTIGATION** (possible partial overlap). Decision: **ADAPT candidate** only after comparing current poster zoom overlay, navigation route ownership, interrupted transition handling, and restoration when back navigation returns.
- Upstream adds `TabletDetailHero.kt` (486 lines) as a tablet-specific detail hero path. Mew has TV layout profile and streams tablet layout, but no matching detail tablet hero in the inspected tree. Classification: **POTENTIAL RESPONSIVE UI GAP**. Decision: **INVESTIGATE** Mew detail breakpoint/layout first; porting requires keyboard/focus, split-screen, landscape, and large-window validation.

### Experimental player feature

- Upstream includes `IosExperimentalPictureInPictureSettings` common and iOS actual storage, persisting a profile-scoped opt-in for a single-primary-renderer PiP path. Android reports availability false. This is distinct from the upstream native PiP controller already audited; it is an experimental renderer mode, not a general PiP enable switch. Mew's inspected tree has no matching setting/storage files. Classification: **UPSTREAM-ONLY EXPERIMENT**. Decision: **SKIP for now / NEEDS INVESTIGATION** until Mew's own PiP renderer exists and the single-renderer tradeoffs are documented; never expose a dead experimental toggle.

### Duplicate and integration summary

- LiquidGlass settings model and navigation surfaces: **ALREADY IMPLEMENTED**.
- LiquidGlass unsupported-platform static backdrop fallback: **ADAPT candidate**.
- App-wide glass wallpaper/presets: **UPSTREAM-ONLY / INVESTIGATE**.
- Navigation glass live-preview/tuning settings: **PARTIAL IMPLEMENTATION / INVESTIGATE**.
- Player video-frame glass: **ALREADY IMPLEMENTED / Mew extension**.
- Artwork backdrop and saliency crop: **NEEDS SEMANTIC COMPARISON / ADAPT candidate**.
- Native iOS glass tab bar and scroll behavior: **UPSTREAM-ONLY / ADAPT candidate**.
- Poster-open motion: **NEEDS INVESTIGATION**.
- Tablet detail hero: **POTENTIAL RESPONSIVE UI GAP**.
- Single-primary-renderer PiP experiment: **SKIP pending native PiP architecture**.

Verification boundary: recursive tree and source inspection at the verified SHAs above. No source changes, screenshots, accessibility validation, build, tests, CI, simulator or device checks. This is a newly expanded UI audit, not a claim that all UI/UX surfaces have been exhausted. Next pass should inventory every upstream-only UI feature and settings page, then inspect each caller and platform actual, including Android TV/remote focus, iPad/macOS window sizing, iOS native bar lifecycle, and all Liquid Glass effect presets/feature gates.


## Appearance, settings surfaces, dialogs and sheets — expanded comparison (2026-11-18)

Comparison target: Mew `Main-dev` at `69b4a96072b506f60dcbe538c5f6b71db3a4a464` versus upstream `enhanced` at `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. The `upstream-sync` branch is a source snapshot, not Mew's active UI implementation; comparing it to itself would hide Mew-specific gaps. Rechecked all three branch SHAs and the audit document blob immediately before this update.

### Screenshot-derived behavior contract

The supplied upstream Appearance screenshots show two independent glass controls and a conditional configuration group:

- **Glass UI (Experimental):** turns cards, sheets and dialogs into frosted-glass surfaces over the app background. Enabling it reveals **Background** and **Glass style** choices (the screenshot shows Default background and Vivid style).
- **Liquid glass effects:** independently controls live glass on the navigation bar, sheets, player controls and cinematic screens. The screenshot explicitly describes different behavior when Glass UI is enabled: live effects default off and Glass UI uses lighter static glass; with Glass UI disabled, turning live effects off leaves solid surfaces.
- **AMOLED Black** is a separate display preference and must not be coupled to either glass setting.
- The second screenshot demonstrates that Glass UI can be enabled while live Liquid Glass effects remain enabled; the settings must not collapse these into one boolean.

This is a concrete behavior contract from the supplied screenshots, not a claim that every screen already follows it.

### Main-dev versus upstream source findings

- Mew `AppearanceSettingsPage.kt` already exposes Mew's Liquid Glass-related controls (native tab-bar support, auto-hide-on-scroll, navigation-bar style) and its own richer appearance settings. Upstream `AppearanceSettingsPage.kt` adds the app-background settings entry and an availability-gated Liquid Glass effects control. Mew does not currently expose the upstream app-wide Glass UI/background/preset configuration through this page.
- Upstream adds `core/ui/background/AppBackground.kt`, `AppBackgroundSettings.kt`, Android/iOS platform implementations and `features/settings/AppBackgroundSettingsSection.kt`. The app-background host composes the selected image behind the app, computes blurred artwork slices and glass styling, adjusts surface tokens/typography for image backgrounds, and tracks when Glass UI is active. Mew's `Main-dev` tree has no matching app-background package or settings section. Mew's profile background/mesh artwork is not equivalent: it is profile-scoped artwork, not the app-wide background and glass configuration shown in the screenshots.
- Upstream `DialogSurface` adds `.appGlassBackdrop(tokens.shapes.dialog)`; Mew's matching `DialogSurface` has no equivalent app-background glass modifier. Therefore the shared dialog primitive itself does not currently inherit the screenshot's frosted-dialog behavior in Mew.
- Upstream `NuvioModalBottomSheet` adds a shared glass-surface pipeline: app-background glass backdrop, live Liquid Glass/backdrop capture when supported, and fallback surface treatment. Mew's matching `BottomSheet.kt` is substantially smaller and retains its native-sheet/platform branch, but lacks the upstream app-glass and live-backdrop composition. Do not replace Mew's native iOS sheet path blindly; adapt the visual surface layer around the existing platform behavior.
- Upstream `NavigationBarSettingsSheet.kt` exists only on the upstream side of this comparison. Mew already has related navigation settings and a preview, but this is not evidence of full parity for the live glass material controls, effect tuning and draft/commit behavior. Treat as a settings UX gap to investigate, not as a request to copy the entire sheet.
- Shared `SettingsComponents.kt`, `SettingsRootPage.kt`, and `SettingsScreen.kt` differ substantially. Mew has fork-specific profile card/watch-party navigation, TV layout/focus handling, and Mew settings organization. Upstream has its own glass-aware settings group treatment, screen-active lifecycle gates and additional pages. Classification: **INTENTIONAL PARALLEL / SHARED PATTERN, DIFFERENT SEMANTICS**. Preserve Mew navigation, profile and TV behavior while selectively aligning visual primitives and lifecycle correctness.

### Decisions

| Area | Classification | Decision |
|---|---|---|
| Glass UI master switch + background + preset | UPSTREAM-ONLY FEATURE | **ADAPT** into Mew Appearance after repository/storage/profile scope and image-source safety review |
| Independent live Liquid Glass effects switch | SHARED PATTERN / DIFFERENT SEMANTICS | **ADAPT** exact dependency/default behavior; keep Mew native-tab and auto-hide controls |
| Dialog glass surface | SHARED COMPONENT / VISUAL GAP | **ADAPT** at shared `DialogSurface` level so all callers receive consistent behavior; audit exceptions and contrast |
| Bottom-sheet glass surface | SHARED COMPONENT / PLATFORM DIFFERENCE | **ADAPT** surface composition without replacing Mew native iOS/Android dismissal and lifecycle behavior |
| App-wide background image/picker/URL | UPSTREAM-ONLY FEATURE | **INVESTIGATE** local/remote source validation, cache bounds, offline fallback, profile scope, privacy and performance |
| Navigation glass tuning sheet | PARTIAL IMPLEMENTATION | **INVESTIGATE** Mew's existing preview/settings callers and persistence before deciding which controls are missing |
| Mew settings root/profile/TV structure | INTENTIONAL PARALLEL | **KEEP** Mew-specific behavior; align shared row/group styling only where semantics match |

### Required follow-up audit before any implementation

1. Trace every `DialogSurface`, `NuvioModalBottomSheet`, custom dialog and custom sheet caller; classify which should inherit Glass UI and which need solid/opaque surfaces (PIN, destructive confirmation, auth, player, downloads, provider selection).
2. Trace settings repository/storage and profile synchronization for AMOLED, live glass, Glass UI, background source and preset. Establish defaults, migration behavior and cross-device sync policy; never assume a screenshot's toggle default is the persisted default on every platform.
3. Compare all Appearance subpages, settings groups/rows, previews, sheets, pickers and confirmation dialogs for typography, spacing, grouping, separators, touch targets, focus, accessibility semantics, scrolling and dismissal.
4. Inspect Android/iOS actuals and OS capability gates for blur/render effects, static backdrop fallback, image picker, URL loading, and native sheet behavior. Include low-memory, reduced-motion, high-contrast and unsupported-device fallbacks.
5. Validate background image URL policy (scheme/host/redirect/size limits), decode dimensions, cache eviction, profile switching, offline behavior and sensitive local-photo handling before accepting the feature.
6. Build a complete dialog/sheet inventory rather than assuming shared primitives cover every custom overlay.

Verification boundary: source/tree inspection only. No code changes, builds, tests, screenshots, accessibility checks, simulator runs or device verification. This batch identifies concrete UI parity gaps; it does not authorize or perform a redesign.


## Settings information architecture and dialog inventory — 2026-11-18

Compared Mew `Main-dev` (`69b4a96072b506f60dcbe538c5f6b71db3a4a464`) with upstream `enhanced` (`b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`). The source snapshot branch was rechecked and is not the comparison target.

### Settings navigation structure

- Mew categories are **Account / General / Enhanced / About / Advanced**. Upstream categories are **You / Look / Watch / Services / System**.
- Mew's root places Appearance, Content Discovery, Downloads, Playback, Integrations, Notifications under General; Account and Tracking are under Account; Nuvio Enhanced is its own root category; About and Advanced are separate.
- Upstream separates visual/layout preferences (Look), playback/download/content behavior (Watch), integrations (Services), personal/profile settings (You), and system/about/advanced (System).
- Mew has an Appearance page that directly contains Home, Streams, Continue Watching, Poster Customization and Meta Screen links. Upstream has a distinct **Layout** page as the parent for Streams, Continue Watching, Poster Customization, Homescreen and Meta Screen, while Appearance focuses on theme/display/app icon/language and app background.
- Mew's Appearance file is ~41 KB and contains many Mew-specific settings/custom theme controls; upstream's corresponding file is ~10 KB and delegates app background to its own section. This is not a simple missing-file comparison.
- Mew's PlaybackSettingsPage is ~185 KB as one source file. Upstream's SettingsPage model exposes separate playback subpages: Player, Stream Selection, Up Next, Skip Segments, Interface, Audio, Subtitles, Video, Buffer and P2P. This is a strong navigation/discoverability candidate, but first check whether Mew's monolithic page already has internal grouping/state and what navigation/persistence compatibility would be affected.
- Mew's SettingsModels uses five categories; upstream's uses five different semantic groups. Both have parent-page navigation and tablet inline support. Do not rename enum values casually: Mew explicitly retains a legacy enum name for saved navigation-state compatibility.

### Inventory counts (path-level, commonMain Kotlin only)

- Settings-related source files: Mew has 24 matching page/root/full-screen files; upstream has 30. Counts include shared helper files and do not equal user-visible page counts.
- Files with names matching Dialog / Sheet / Modal / Picker: Mew 27; upstream 44. These are filename inventory counts only, not a claim that every dialog is missing in Mew. Many are intentional feature differences and need caller/behavior comparison.
- Mew-only overlay examples include `ContinueWatchingActionSheet`, `EpisodeDownloadFlowSheet`, `PendingSourceChoiceSheet`, `EnhancedSubtitleModal`, `NuvioAudioTrackModal`, and `MediaActionOverlayDialogProperties`.
- Upstream-only overlay examples include `ServerConnectionDialogs`, `DownloadsQueueDialog`, `LibraryListManagementDialog`, `NavigationBarSettingsSheet`, `PlaybackSettingsDialogs`, `PlaybackStreamDialogs`, `TrackingDeviceCodeSheet`, and `ProfileBackgroundPicker`. Treat each as a candidate to trace, not an automatic port.

### Dialog and sheet primitives

- Mew's `DialogSurface` and upstream's `DialogSurface` share the same public API and composition pattern. Upstream adds the app-glass backdrop and uses the theme's subtle border token; Mew uses its border token directly. **SHARED PATTERN / DIFFERENT SURFACE EFFECT**. Adapt the glass layer only after classifying sensitive/contrast-critical dialogs.
- Mew's `NuvioModalBottomSheet` is ~4.8 KB and routes to Mew's native bottom-sheet implementation when supported. Upstream's is ~9 KB and adds a reusable backdrop host/surface, live glass capture and app-background integration. **SHARED PATTERN / DIFFERENT PLATFORM IMPLEMENTATION**. Preserve Mew native sheet dismissal, gesture, keyboard, focus and iOS presentation semantics; adapt the material pipeline around it.
- Central primitives do not cover all overlays. Several feature-specific files implement their own modal/sheet surfaces; a complete caller inventory must include direct `BasicAlertDialog`, `AlertDialog`, `ModalBottomSheet`, custom Dialog and popup usage, not only references to `DialogSurface`.

### Initial IA recommendations (audit proposals, not implementation)

1. Evaluate grouping settings as **You / Appearance / Playback / Services / System** (or equivalent clear Mew wording). Keep Mew-only profile, addon, CloudStream, membership, Live TV and feature settings visible in the correct group; do not force upstream's labels if they are unclear for Mew.
2. Split **Appearance** from **Layout/Home presentation**. Appearance should own theme, color, AMOLED, glass, background, app icon and language. Layout should own home composition, poster/card presentation, detail layout, continue-watching presentation, collection and stream display controls. Confirm each setting's actual effect before moving it.
3. Split the large Playback page into focused subpages or sections comparable to upstream's Player / Stream / Up Next / Skip / Interface / Audio / Subtitles / Video / Buffer / P2P. Keep current values, profile scoping, deep links, and saved navigation-state compatibility.
4. Rewrite setting titles/descriptions to answer: what changes, where it applies, whether it is experimental, platform/OS limits, and whether restart is required. Remove vague/duplicated copy and ensure labels match actual behavior. Resource-key/source audit is still pending.
5. Standardize dialogs/sheets with consistent title/body/action order, destructive emphasis, max width, padding, corner radius, safe areas, scroll limits, keyboard handling, accessibility labels, loading/error/empty states, and dismissal behavior. Use shared primitives where semantics match; do not force PIN/player/native platform surfaces into a generic modal.
6. Build a settings glossary and map every persisted setting to one canonical page/section; no setting should appear twice under different names unless it is an intentional shortcut.

### Classification and next inspection

- Settings category/layout differences: **INTENTIONAL PARALLEL / PRODUCT IA GAP** — **ADAPT selectively** after full setting-to-repository/caller mapping.
- Playback monolith versus upstream subpages: **SHARED PATTERN / DIFFERENT INFORMATION ARCHITECTURE** — **INVESTIGATE then ADAPT** without changing stored keys.
- DialogSurface: **ALREADY IMPLEMENTED shared primitive / visual parity gap** — **ADAPT** glass-aware surface and contrast policy.
- BottomSheet: **SHARED PATTERN / DIFFERENT PLATFORM SEMANTICS** — **ADAPT** material treatment only; retain native Mew path.
- Named overlay differences: **NEEDS CALLER/SEMANTIC AUDIT** — no bulk copy.

Verification boundary: GitHub source/tree inspection only. No app source modifications, builds, tests, screenshots, accessibility checks, simulator or device validation. Resource-string copy audit, full setting ownership map, all custom modal call sites, and cross-platform dismissal/focus testing remain outstanding.


## Settings copy and ownership audit — confirmed labels and playback grouping (2026-11-18)

### Confirmed text/semantic mismatches in Mew Main-dev

- The root Appearance row uses `compose_settings_page_appearance` as its title and `compose_settings_root_appearance_description` as its description. The resource value for that title is **"Layout"**, while the description says **"Home, streams, collections, and detail pages"**. The destination `SettingsPage.Appearance` opens a page that contains both Theme and Display (AMOLED, iOS native tab-bar option, app icon, language, nav-bar style) plus Home, Streams and Detail Page groups. This is a concrete title/description/contents mismatch: it is neither a pure Appearance page nor a pure Layout page. **ADAPT** after choosing the canonical split and mapping every destination.
- `settings_appearance_liquid_glass` is rendered as **"Liquid Glass"**, but its description is **"Use the native iPhone tab bar on iOS 26 and later."** The state/callback names are `liquidGlassNativeTabBarEnabled` and `onLiquidGlassNativeTabBarToggle`, and the option is shown only when `liquidGlassNativeTabBarSupported`. It is therefore a native iOS tab-bar preference, not the app-wide live Liquid Glass effects toggle in the supplied upstream screenshot. **RENAME the visible label** to an accurate term such as "Native iOS tab bar" (final copy to be decided), and keep the distinct glass rendering control separate. Do not reuse this key for the new Glass UI feature.
- The setting `settings_appearance_liquid_glass_auto_hide` is dependent on the native iOS tab-bar toggle (`enabled = liquidGlassNativeTabBarEnabled`). Its copy should explicitly say it auto-hides the native iOS tab bar while scrolling; don't imply it affects all glass surfaces.
- Root Appearance description currently describes only layout-related destinations and omits theme/display controls that are on the same page. This makes the landing-row summary incomplete even before the proposed split.

### Playback page correction

- Mew's `PlaybackSettingsPage.kt` is large (~185 KB), but it is **not one undifferentiated list**. It already has nine named groups in the inspected source: Player, Subtitle Audio, Subtitle Rendering, P2P, Stream Selection, Stream Auto Play, Decoder, Skip Segments and Next Episode.
- Therefore the previous suggestion to split it into upstream-style subpages is only a **UX/discoverability option**, not a confirmed missing architecture. Preserve these nine semantic groups and their persisted setting ownership. First evaluate a compact in-page index/anchors, collapsible groups, search deep links or subpages only if navigation and accessibility benefit; do not fragment it solely to match upstream.
- Upstream has more granular playback pages, but Mew's current group structure is meaningful and should be compared by behavior/caller, not file size.

### Copy audit finding

- `AdvancedSettingsPage.kt` has hardcoded English diagnostics labels/body strings ("Logs", "View logs", "Copy logs", "Export logs", "Logs copied", "Export started") rather than generated string resources. **ADAPT** into localized resources and check all supported locales/fallback behavior.
- Resource copy needs a systematic key-to-visible-control pass: verify title, description, current value, dialog title/body, action labels, disabled-state explanation, platform limits and restart requirement against the actual callback and persistence behavior. Avoid cosmetic wording changes that conceal different semantics.

### Ownership map corrections

- The proposed future IA should distinguish **Appearance** (theme, palette, AMOLED, glass, app background, icon, language, visual effects) from **Layout** (home hero/catalog composition, collections, Continue Watching presentation, posters, streams list/badges, detail-page layout). The current Mew page combines both.
- Native iOS tab-bar selection/auto-hide is a platform navigation preference, not a generic Appearance > Liquid Glass setting. Place under Navigation or platform-specific Appearance, with availability explanation.
- Playback remains a single page with nine established groups pending usability review; do not mark it as missing subpages or force an upstream structure.

Verification: source and resource inspection only. No app code changes, resource edits, builds, tests, locale validation, screenshots, accessibility or device checks.


## Dialog/sheet implementation paths — direct primitive bypass audit (2026-11-18)

Inspected Mew `Main-dev` common-source overlay implementations, beyond filename inventory.

### Confirmed implementation paths

- Shared `DialogSurface` wraps Material3 `BasicAlertDialog` and provides a reusable content surface.
- Shared `NuvioModalBottomSheet` selects Mew's native platform sheet when supported, otherwise Material3 `ModalBottomSheet`.
- Several important dialogs bypass `DialogSurface` and call `BasicAlertDialog` directly: `PinEntryDialog`, `P2pConsentDialog`, `AppIconPickerDialog`, `PlayerToolbarSettingsDialog`, `SimklSyncInfoDialog`, `TrackingAdaptivePicker` dialog mode, and `TrackingListPickerDialog`.
- `AppIconPicker` has separate Android confirmation-dialog and bottom-sheet paths. `TrackingAdaptivePicker` has dialog and sheet paths. These are intentional adaptive presentations, not duplicate screens by default.
- `EpisodeDownloadFlowSheet` is a large feature-specific multi-stage flow (~75 KB); it must be audited as a workflow with source/episode selection, queue and error states, not treated as a generic sheet to replace.
- `PendingSourceChoiceSheet` uses the shared `NuvioModalBottomSheet`, but its content and source-selection actions remain feature-specific.

### Glass UI coverage implication

A glass modifier added only to `DialogSurface` will not automatically reach the direct-`BasicAlertDialog` implementations. A complete Glass UI pass must explicitly decide whether each bypass should:
- migrate to a shared dialog shell,
- use a shared glass surface slot while retaining custom content, or
- remain intentionally opaque/solid (for example, security-sensitive PIN entry or high-contrast consent/confirmation).

Do not force every overlay into one visual treatment. Player track/subtitle menus, PIN/auth/consent, destructive confirmation, download workflow, source selection and informational dialogs have different interaction and contrast requirements.

### Dialog audit classification

| Surface | Classification | Next decision |
|---|---|---|
| `DialogSurface` shared shell | SHARED PATTERN | Add opt-in/automatic glass surface support after contrast review |
| Direct `BasicAlertDialog` callers | INTENTIONAL PARALLEL / BYPASS | Inventory caller semantics; migrate only where it improves consistency |
| Native bottom sheet | INTENTIONAL PLATFORM PATH | Preserve dismissal, focus, keyboard and safe-area behavior |
| App icon adaptive picker | INTENTIONAL PARALLEL | Keep Android confirmation vs platform sheet distinction |
| Tracking adaptive picker | INTENTIONAL PARALLEL | Keep size/platform adaptive presentation |
| Episode download flow | SHARED PRIMITIVES / DIFFERENT SEMANTICS | Keep dedicated multi-step flow; align shared chrome only |
| Pending source choice | FEATURE-SPECIFIC SHARED SHEET | Glass-compatible shell candidate; retain source-choice behavior |

### Remaining work

- Search all source sets (Android/iOS/desktop) for direct Material3 and Compose dialog/sheet APIs, custom `Popup`/overlay surfaces, and SwiftUI/UIKit presentation controllers. GitHub code search did not return indexed results for the targeted symbol queries, so the commonMain inventory is not yet exhaustive.
- Inspect actual dialog dimensions, shape/elevation/border, actions order, text hierarchy, scroll constraints, focus restoration, back/outside dismissal, accessibility semantics and tablet/TV behavior.
- Trace each dialog's caller and lifecycle (loading, errors, cancellation, save/commit) before changing its shared implementation.
- Build a shared visual contract with explicit variants (glass, opaque, security, player, picker) rather than one mandatory universal dialog.

Verification: commonMain source inspection only. No source modifications, builds, tests, UI captures, accessibility or device checks.


## Cross-platform settings feature parity — downloads, Live TV, servers and backup (2026-11-18)

Compared the current `Main-dev` and upstream `enhanced` trees across commonMain, androidMain and iosMain. Absence below means no equivalent settings page/file was found in the inspected tree; it does not prove the underlying feature is absent.

### Upstream settings pages without a Mew page counterpart found

- **Downloads settings:** upstream has `DownloadsSettingsPage.kt`, `DownloadsSettingsRepository.kt`, Android/iOS storage actuals and platform download-location pickers. The page includes preferred resolution, allow-mobile-data, download-button visibility, download location and concurrency controls. Mew has a Downloads root destination and a Downloads feature/queue, but no corresponding `features/settings/DownloadsSettingsPage.kt` or platform settings storage/picker was found. Mew's Nuvio Enhanced settings include a download external-folder option; that is not yet proven equivalent to the upstream download preferences. **Classification: PARTIAL / NEEDS SEMANTIC AUDIT. Decision: INVESTIGATE then ADAPT only missing preferences.** Check existing queue defaults, external-folder setting, file URI grants, scoped storage, iOS document picker, profile scope and background transfer behavior before adding anything.
- **Live TV settings:** upstream has a substantial `LiveTvSettingsPage.kt` for playlist/provider credentials, EPG sources, navigation and manual reload/status. No same-purpose Mew settings page was found in the settings tree. Do not infer Live TV itself is absent; inspect Mew feature availability, route and provider configuration before deciding. **Classification: NEEDS INVESTIGATION. Decision: VERIFY feature parity and ownership.**
- **Media server settings:** upstream has `MediaServersSettingsPage.kt` plus individual server connection/sign-in flows. Mew's inspected settings tree has no corresponding page. Earlier architecture audit found Mew's custom backend configuration is not equivalent to upstream's Jellyfin/Emby-style media-server provider system. **Classification: UPSTREAM-ONLY / DIFFERENT BACKEND CONTRACT. Decision: PRODUCT + SECURITY INVESTIGATION, not a settings-page port.** Review auth token storage, TLS/hostname policy, server discovery, profile scope and playback/source semantics before any adoption.
- **Settings configuration backup/restore:** upstream has `SettingsConfigTransfer.kt` (~78 KB), `SettingsConfigBackupSection.kt`, platform file launchers and import/export across multiple settings domains. Mew has `NuvioEnhancedBackupFileBridge` and backup/import UI inside `NuvioEnhancedSettingsPage.kt`; this is specifically a Nuvio Enhanced settings backup flow, not evidence of full-app settings transfer parity. **Classification: SHARED PATTERN / DIFFERENT SCOPE. Decision: INVESTIGATE.** Compare exact exported keys, schema/versioning, sensitive credential opt-in, profile scoping, addon/plugin secrets, migrations, partial import errors and rollback before extending. Never replace Mew's narrower backup with upstream's transfer wholesale.

### Cross-platform implementation observations

- Upstream download-location selection uses platform-specific Android and iOS implementations. The iOS implementation holds a strong delegate reference while the UIKit document picker is active; cancellation and selection callbacks are handled explicitly. This is a useful lifecycle detail if Mew adapts the feature.
- Mew already has platform-specific native bottom-sheet actuals for Android and iOS and a common expect contract. Preserve those platform presentation paths while aligning shared visual surfaces; do not replace native presentation with a common Material sheet solely for appearance parity.
- Settings pages are commonMain, but platform-specific storage, document pickers and native sheet actuals are part of the user-visible behavior. The audit must compare all three together.

### Next verification required

1. Trace Mew Downloads root row to its destination and identify every persisted download preference across settings/repository/storage.
2. Search Mew Live TV routes, providers and settings/configuration entry points; determine whether there is an actual parity gap or a different feature contract.
3. Compare Mew server integrations and custom backend config against upstream media-server provider semantics and security requirements.
4. Diff the actual Mew Nuvio Enhanced backup payload against upstream SettingsConfigTransfer; classify every setting domain and sensitive field.
5. Inspect Android/iOS file picker lifecycle, URI permissions, cancellation and persistence; test only after any implementation is separately approved.

Verification: recursive tree and selected source inspection on GitHub. No application code changes, builds, tests, screenshots, simulator or device checks. No assumption that a missing page file proves the underlying feature is missing.


## Settings ownership map — root destinations and persisted-state domains (2026-11-18)

### Current root destinations and likely ownership

Observed directly in `SettingsRootPage.kt`:

- **Account:** Profile switch, Account, Tracking.
- **Mew Enhanced:** Nuvio Enhanced settings and Telegram/community link.
- **General:** Appearance, Content Discovery, Downloads, Playback, Watch Party, Integrations, Notifications.
- **About:** Supporters/Contributors, Privacy Policy, Licenses, optional update/debug entries.
- **Advanced:** Advanced/diagnostics.

This is a root destination inventory, not a final proposed IA.

### Misplaced or ambiguous destinations to resolve

- **Tracking** currently sits under Account and uses an Account category in `SettingsModels.kt`, but its page is a third-party service connection/sync surface (Trakt/Simkl/anime tracking). Candidate move to **Services / Integrations**; preserve profile ownership and account-specific authentication. Confirm if any account/profile summary depends on this location.
- **Downloads** is a first-class General root row, while the upstream has a dedicated download preferences page and Mew's queue/recovery is a separate lifecycle feature. Candidate **Playback & Downloads** or **Storage & Downloads** grouping. Do not merge queue controls with storage/location policy without checking their different lifecycle semantics.
- **Watch Party** is a root General row but is a distinct feature/service, not a general appearance or app preference. Candidate **Services / Social**. Check account/session setup, room hosting, network and playback requirements.
- **Content Discovery** owns Addons, Plugins and CloudStream child pages. These are provider/catalog/source configuration, not merely visual discovery preferences. Candidate **Sources & Catalogs** or **Services**, with separate content-display preferences under Layout.
- **Integrations** owns AI Assistant, TMDB enrichment, MDBList ratings and Debrid. This mixes metadata enrichment, AI, ratings and stream-source/debrid configuration. Candidate split into **Services** (Debrid/provider connections) and **Personalization/Metadata** (TMDB/MDBList/AI) only if the root becomes easier to scan; avoid excessive top-level groups.
- **Nuvio Enhanced** is a Mew-specific group with profile-scoped feature flags/settings and its own backup. Preserve as an identifiable Mew feature area, but audit whether individual settings belong in canonical Appearance/Playback/Services destinations or remain in one advanced feature hub. Do not hide its unique toggles during reorganization.
- **Notifications** has its own root page; likely System/Notifications. Verify notification channels and platform permission behavior before copy/layout changes.
- **Advanced** contains diagnostics/log export. Consider System > Diagnostics rather than a catch-all Advanced bucket; preserve debug-only gating and data export behavior.
- **Privacy Policy** currently lives in About. A separate **Privacy** group would be justified only if there are actual controls for analytics, telemetry, data retention or permissions—not just a policy link.

### Persisted-state domains found in selected repositories

- `ThemeSettingsRepository`: selected theme, custom theme colors, AMOLED mode, native iOS tab-bar enable/auto-hide, app language, navigation-bar style. The two `liquidGlassNativeTabBar` values are specifically iOS navigation controls, not general glass-effect settings.
- `PlayerSettingsRepository`: a broad profile of player UI, gestures, external player, preferred audio/subtitle languages and style, subtitle startup mode, stream reuse, Android decoder/MPV options, stream auto-play and source selection, intro/anime skip, next-episode behavior, subtitle renderer, iOS video/HDR/colorspace/audio output and image adjustments. Platform-specific values should be hidden or explained when unsupported; don't flatten every advanced codec knob into beginner-level controls.
- `HomeCatalogSettingsRepository`: home hero behavior, catalog visibility/order/title, collections, pinned ordering, provider selection and per-catalog hero-source toggles. This belongs under **Layout > Home**, not generic Appearance.
- `MetaScreenSettingsRepository`: detail-screen section visibility/order/tab grouping, background mode, cinematic background, hero trailer/sound, random episode, episode card style, blur unwatched, ratings/download/anime actions. This belongs under **Layout > Details**.
- `CollectionMobileSettingsRepository`: collection/folder GIF overrides; candidate **Layout > Collections**.
- `StreamBadgeSettingsRepository`: stream badge rules, file-size badge, addon logo and placement. Candidate **Playback > Stream list** or **Layout > Streams** depending whether it changes source information or just presentation. Keep rule engine and badge presentation as separate ownership concepts.
- Additional repositories exist for AI, Anime Tracking, Debrid, MDBList, Sentry, TMDB and Trakt; full field-by-field inventory remains pending.

### Recommended destination map (provisional)

| Destination | Mew settings/domains |
|---|---|
| You & Profiles | Account, profiles, profile-specific preferences |
| Appearance | Theme, custom colors, AMOLED, app-wide glass/background, icon, language |
| Layout | Home/catalogs, Collections, Continue Watching, Posters, Streams presentation, Details |
| Playback | Player controls, audio/subtitles, decoder, stream selection/autoplay, skip/next episode, player-specific glass |
| Sources & Services | Addons, Plugins, CloudStream, Debrid, Trakt/Simkl/anime tracking, media server investigation, Watch Party |
| Personalization | TMDB, MDBList, AI Assistant (only if a separate grouping improves discoverability) |
| System | Downloads/storage, Notifications, Diagnostics, advanced options |
| About | Support, privacy policy, licenses, update information |

This map is a proposal for testing against all settings, not a committed design. Keep Mew Enhanced features accessible and avoid duplicating the same persisted setting in two places.

### Duplicate/overlap audit rules for next pass

- Similar options can affect different layers: e.g. player glass vs app-wide glass; native iOS tab bar vs Liquid Glass surface; stream badges vs stream selection. Do not merge them based on naming.
- Check whether each setting has one canonical persisted key, one owner repository, one UI control (or intentional shortcut), and a clear platform capability gate.
- Compare default value, profile scope, migration/legacy key, setter side effects and consumers before moving a control.
- Record unsupported platform values explicitly; don't show dead toggles or silently reset cross-platform preferences.

Verification: current Main-dev source and repository inspection; selected settings models/repositories only. Full settings-repository inventory, all consumers, translations, migration keys and UI-state persistence still pending. No source changes, builds, tests or device checks.


## Mew Enhanced settings hub — overlapping controls and canonical ownership (2026-11-18)

Inspected `NuvioEnhancedSettingsPage.kt` (~90.8 KB) and `NuvioEnhancedSettingsRepository.kt` against the ordinary settings pages and their repositories.

### Confirmed overlap / duplicate exposure

The Mew Enhanced hub contains sections for Nuvio Experience, Premium Labs, App Experience, Player Tools, Hero Experience, Detail Presentation, Release Radar, Details Experience, Network, Backup/Import, Downloads, Discovery and Diagnostics. This is a much broader surface than a small independent feature-settings page.

Several controls are backed by the same canonical repository as ordinary settings, so the Enhanced hub is also an alternate control surface:

- **Player Tools** exposes parental guide, subtitle sync, player clock, random next episode and Android memory-safe buffer, all represented in `PlayerSettingsRepository` and the ordinary Playback settings domain. It also exposes player status overlay and subtitle/audio selector styles.
- **Hero Experience** exposes hero display mode/artwork source, poster-art and streaming-showcase modes, preview/sound, metadata/rating/overview, hero refresh haptics, auto-scroll and motion preview. These overlap the normal Home/Homescreen and appearance/layout domains.
- **Detail Presentation / Details Experience** exposes cinematic detail header/content mode, episode-card layout, trailer playback/sound and episode ratings, overlapping the MetaScreen settings domain.
- **Downloads** in the hub exposes external-folder selection/removal; this is not automatically equivalent to full download policy preferences such as mobile data, concurrency, preferred resolution or default location.
- **Diagnostics** in the hub coexists with the separate Advanced diagnostics/log page.

### Classification

- Same persisted player preference reachable from Playback and Enhanced: **SHARED PATTERN / SAME SETTING, MULTIPLE ENTRY POINTS**. Not necessarily a storage duplicate, but risks duplicate controls, inconsistent descriptions and discoverability.
- Same home/detail preference reachable from Layout-related pages and Enhanced hub: **SHARED PATTERN / SAME SETTING, MULTIPLE ENTRY POINTS**.
- Mew Enhanced-only experiments (smart shelves, release digest, quiet home, library health, background prefetch, source pinning, hero variants, etc.): **INTENTIONAL PARALLEL / MEW-SPECIFIC FEATURE HUB**, not candidates for deletion.
- Enhanced external-folder setting vs general download policies: **FALSE POSITIVE if treated as equivalent**; keep semantics separate pending exact behavior comparison.
- Enhanced Diagnostics vs Advanced logs: **POTENTIAL DUPLICATE DESTINATION**, inspect whether both expose same logs or distinct data before consolidation.

### IA recommendation

Use a canonical-owner model:

1. Every preference gets one primary location based on what it changes (Appearance, Layout, Playback, Sources/Services, Downloads, System).
2. The Enhanced hub should either contain only Mew-specific experimental features, or retain clearly marked shortcuts/curated labs while linking to the canonical setting. It must not quietly maintain a second independent control or divergent description.
3. If a setting remains intentionally reachable from two pages, both entries must bind to the same repository property/setter, use the same localized title/description/value, and be documented as a shortcut.
4. Keep feature announcement/seen-state metadata separate from preference persistence. Do not delete feature flags or reset profile preferences during IA cleanup.
5. Group experimental features by effect and maturity: stable Mew enhancements vs Labs/experimental; add concise risk/availability notes and explicit reset/default behavior.

### Settings text audit priority

- Highest: controls with ambiguous names ("Liquid Glass", "Enhanced", "Smart Resume", "Background Stream Prefetch", "Quiet Home", "Library Health", "Release Radar") need descriptions that state exact effect and scope.
- Player controls: distinguish playback behavior, stream-source selection, UI appearance and decoder compatibility; explain Android/iOS-only controls and restart requirements.
- Home/detail controls: explain whether each option changes layout, metadata, artwork, network requests, video preview, sound or haptics.
- Security/network/backup: state what leaves device, what credentials are included, what server/provider is contacted and how restore conflicts are handled.

Verification: selected repository/state and UI inspection only. Same repository-backed setting overlap is confirmed at domain level; every individual callback/key and visible string pair still needs line-by-line comparison. No source edits, builds, tests or device checks.


## Correction: Enhanced hub overlap requires per-control classification (2026-11-18)

A line-by-line follow-up narrows the earlier broad statement that Player Tools, Hero Experience and Detail Experience are duplicates of ordinary settings pages.

### Confirmed shared backing repository, but not duplicate visible control

- Subtitle sync menu, player clock end-time and random-next-episode are read from and written to `PlayerSettingsRepository` in `NuvioEnhancedSettingsPage.kt`. A scan of `PlaybackSettingsPage.kt` found none of these setting properties/callbacks. They are **not currently confirmed duplicate controls** in the ordinary Playback page. Their correct canonical destination can still be Playback, but the existing Enhanced hub may be their only visible control.
- Android memory-safe buffer is also bound to `PlayerSettingsRepository` in the Enhanced hub; the playback page file receives decoder-related settings as parameters, but the property itself was not found in its source. Need trace the caller and any separate Android advanced controls before classifying.
- Subtitle/audio selector style values use `NuvioEnhancedSettingsRepository`, not `PlayerSettingsRepository`; these are selector UI presentation preferences, not the preferred subtitle/audio language or subtitle renderer settings. **SHARED PATTERN / DIFFERENT SEMANTICS**, not a duplicate.
- Hero display mode, artwork source, poster-art hero and streaming-showcase hero use `NuvioEnhancedSettingsRepository`. `HomeCatalogSettingsRepository` separately controls catalog visibility/order and hero-source eligibility, plus home auto-scroll/motion. These settings affect related home surfaces but are **different persisted settings and different behavior**, not duplicates.
- Hero auto-scroll/motion values in `HomeCatalogSettingsRepository` are not the same as hero display/artwork mode in the Enhanced repository. Avoid merging them.
- Trailer playback/sound and episode ratings in Enhanced use `MetaScreenSettingsRepository`. The ordinary MetaScreen settings page file contains no direct property/callback matches in the scanned source; the Appearance page links to the Meta Screen page. This is **shared detail-screen ownership, but duplicate UI is not confirmed**.
- Cinematic detail header / Nuvio Read state is stored in `NuvioEnhancedSettingsRepository` and has mutually exclusive normalization (`nuvioReadEnabled` is disabled when cinematic header is enabled). It is not interchangeable with `MetaScreenSettingsRepository.cinematicBackground` / background mode. **SHARED PATTERN / DIFFERENT SEMANTICS**.
- Enhanced parental guide writes `PlayerSettingsRepository.setShowParentalGuide`; it is a playback behavior preference. Verify whether the normal Playback page exposes the same setter elsewhere before moving it.

### Diagnostics are two different data sources

- Enhanced Diagnostics uses `CrashDiagnostics` for diagnostics copy and crash context.
- Advanced > Logs uses `RuntimeDiagnostics.snapshotText()` and includes recent runtime/CloudStream logs; its export writes `mew-diagnostics.txt`.
- These are **SHARED PATTERN / DIFFERENT SEMANTICS**, not proven duplicate controls. A future System > Diagnostics page may unify the presentation while preserving separate data sections and exports. Do not merge or discard either diagnostic source.

### Corrected decision

- Previous broad wording "same preference reachable from Playback and Enhanced" was too strong. For the listed Enhanced Player Tools controls, current source evidence confirms shared PlayerSettings repository ownership but does **not** confirm a second visible Playback control.
- Previous broad wording "Hero/Detail controls duplicated in Layout" was also too strong. Current source evidence confirms domain relation and repository ownership, but no second visible control for those exact keys was found in the inspected canonical pages.
- Treat the Enhanced hub as a possible **secondary feature index**. Proposed redesign: keep Mew-specific feature discovery/highlights in Enhanced, but have each setting either (a) live in one canonical page with a deep link from Enhanced, or (b) remain exclusively in Enhanced if it is truly a Labs/feature-specific control. Do not maintain two independently editable copies.
- Before any move, trace the root route, all callers, all UI state collectors, exact setter, persisted key, profile reset path, migration and feature announcement state.

Verification: inspected exact Mew source on Main-dev at the SHAs recorded in this audit; searched current Playback, Appearance, Home and Meta settings pages for the exact properties. This narrows but does not complete the caller audit. No source changes, builds, tests or device checks.


## Settings audit continuation — persistence and duplicate-control correction (2026-11-18)

Verified against Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`, upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`, and `upstream-sync` `a09deaf20a09537dcd8bb6cb6dfb26858b6515b7`.

### Confirmed duplicate visible control: Content Warnings

The previous per-control correction said the parental-guide control's second visible location was not confirmed. That conclusion was incomplete.

- `PlaybackSettingsPage.kt` renders a switch using `settings_playback_parental_guide`, `settings_playback_parental_guide_description`, `autoPlayPlayerSettings.showParentalGuide`, and `PlayerSettingsRepository.setShowParentalGuide`.
- `NuvioEnhancedSettingsPage.kt` renders the same switch with the same title, description, state property and setter.
- This is a **TRUE DUPLICATE / MULTIPLE EDITABLE ENTRY POINTS**. Both write the same canonical preference, so there is no separate-value conflict, but the user sees the same setting twice in different IA locations.
- Decision: **ADAPT**. Keep one canonical control in Playback (content/playback behavior); in Enhanced, either remove the editable switch and provide a shortcut/deep link, or keep a clearly marked shortcut. Preserve the feature highlight/seen-state behavior independently if the feature announcement still needs it. Do not reset the preference.

### Profile persistence and sync

- `PlayerSettingsRepository.onProfileChanged()` reloads its state; `clearLocalState()` restores defaults. The Android and iOS player storage implementations use `ProfileScopedKey` for preference keys.
- Player settings include a sync payload contract (`exportToSyncPayload` / `replaceFromSyncPayload`); the Android and iOS implementations list the same persisted keys for sync. The parental-guide key is included.
- `NuvioEnhancedSettingsRepository` serializes a version-tolerant JSON payload (`ignoreUnknownKeys = true`, `encodeDefaults = true`) and normalizes mutually exclusive Cinematic Detail Header / Nuvio Read state, plus feature-seen defaults.
- Android Enhanced storage uses SharedPreferences payload key `enhanced_settings_payload`; iOS uses NSUserDefaults with the same logical key. Both wrap it with `ProfileScopedKey`. Profile change resets in-memory state and reloads the active profile payload.
- Enhanced feature IDs, `seenFeatureIds`, `discordWelcomeSeen`, and `featureHighlightsEnabled` are announcement/discovery state, not the same thing as the feature preferences themselves. Keep them separate during any settings IA move or reset flow.
- No migration or key rename is proposed. Profile isolation, backup/import behavior and cross-platform payload parity still need a full field-by-field audit.

### Confirmed label/copy defects

- The root Appearance destination resource resolves to **“Layout”**, but its root description says “Home, streams, collections, and detail pages”; the destination also includes Theme and Display groups. This is a title/description/contents mismatch. **ADAPT**: define the canonical distinction between Appearance and Layout before renaming or splitting routes.
- The setting label `settings_appearance_liquid_glass` is **“Liquid Glass”**, while its description explicitly says **“Use the native iPhone tab bar on iOS 26 and later.”** This is not the app-wide Liquid Glass effect; it is a native iOS tab-bar mode. **ADAPT** the label and IA to “Native iOS tab bar” (or equivalent), and keep the actual glass-effect controls separately named.
- `AdvancedSettingsPage.kt` hardcodes English diagnostics strings (“Logs”, “View logs”, “Copy logs”, “Logs copied”, “Export started”, “Export logs”, “Runtime logs”). **ADAPT** to localized resources and verify locale fallback. This is a confirmed localization gap, not a missing diagnostics feature.

### Settings dialog surface audit — new evidence

- Shared `DialogSurface` wraps Material3 `BasicAlertDialog` and supplies the common dialog surface tokens (width, shape, background, border, padding and text hierarchy).
- `PlaybackSettingsPage.kt` itself contains numerous direct `BasicAlertDialog` call sites (at least 18 in the inspected file), in addition to the separately identified direct-dialog components. A one-place glass/surface change to `DialogSurface` therefore cannot update all settings dialogs.
- Decision: **NEEDS INVESTIGATION**. Inventory each dialog's purpose, sizing, actions, dismissal policy, scroll/focus behavior, platform-specific presentation and accessibility before consolidating. Security/PIN/consent dialogs and player/picker dialogs may require distinct visual variants; do not force all into one glass treatment.

### Verification limits

Read-only source inspection only. No source implementation changes, builds, tests, CI, simulator or device checks. Current branch refs were re-fetched before this documentation update. `Main-dev` and `main` remain untouched.


## Backup/import parity and native presentation audit (2026-11-18)

Source refs rechecked before documentation update: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`; current `upstream-sync` `1ddb4f5364f802ddc0cb0311aaa5353f5f99c5b3`.

### Backup/import is not equivalent to upstream Settings Config Transfer

**Mew current behavior**
- Enhanced settings offers file export, file import, copy JSON, and paste JSON.
- The payload comes from `ProfileSettingsSync.exportBackupJson()`, which serializes `MobileProfileSettingsBlob` — the profile settings sync schema — rather than a dedicated versioned backup format.
- Exported sections include theme, poster/card styles, player, stream badges, Debrid, TMDB, MDBList, details, collections, Continue Watching preferences, Trakt, notifications, Enhanced settings and AI Assistant.
- Export explicitly strips profile credentials from Player, Debrid, TMDB, MDBList and AI Assistant using `withoutProfileCredentials`. Import preserves the current local credentials for those features via `preservingLocalProfileCredentials`.
- Import decodes the blob and applies it through `applyRemoteBlob`; there is no explicit user confirmation step for file import in the inspected path. The manual paste dialog accepts raw text and invokes the same import.
- No dedicated backup format/version/app-version/export timestamp or explicit sensitive-data opt-in was found in this Mew backup path.

**Upstream behavior**
- `SettingsConfigTransfer` uses a dedicated `nuvio_settings_config` format with version, app version, export timestamp, and `includes_sensitive_data` metadata.
- Export supports explicit opt-in for credentials. Sensitive sections such as Live TV/addons/plugins are conditionally included.
- Import validates format/version, asks for confirmation (with distinct copy when sensitive data is included), applies sections individually, and reports full/partial/failure outcomes.
- Upstream covers additional domains absent from the inspected Mew profile blob, including subtitle auto-sync, custom posters, OMDb, library display, downloads/general settings, P2P, Discord RPC, Live TV, addons and plugins.
- Upstream import deliberately preserves local dismissed Continue Watching keys rather than restoring those transient dismissals.

**Classification: SHARED PATTERN / DIFFERENT SEMANTICS; significant parity gap. Decision: ADAPT, not wholesale replace.**
- Mew's profile sync blob is designed for profile cloud sync and credential preservation. It should not silently be treated as a complete portable configuration backup.
- Create a dedicated Mew backup schema/version and explicit sensitive-data policy only after defining exactly which profile settings, app-global settings, credentials, addons/plugins, provider state and transient state should transfer.
- Preserve Mew's profile-scoped keys, local credential safety, Mew-only settings and current sync behavior. Add confirmation, schema validation, partial-import reporting and migrations where appropriate.
- Audit whether current Mew backup is incomplete by product intent or by omission before porting upstream's additional sections. Do not import credentials by default.

### Native sheet/dialog behavior

- Mew's shared `NuvioModalBottomSheet` selects the iOS native UIKit page sheet when native sheets are enabled and the glass UI is inactive; otherwise it uses the Compose Material3 modal sheet path.
- iOS native path uses `UIModalPresentationPageSheet`, medium + large detents for non-full-height sheets, large-only for full-height, configurable grabber, and disables automatic scroll-to-expand. It retains the presentation delegate and invokes the current dismiss callback.
- Android native sheet actual in the inspected source is a no-op; the shared wrapper falls through to Material3 modal sheet on Android. Therefore “native sheet on both platforms” is not established by the shared API name. Keep Android Material sheet behavior unless a concrete Android native presentation is intentionally designed.
- Mew `DialogSurface` uses app glass backdrop + dialog tokens; direct `AlertDialog`/ `BasicAlertDialog` components bypass it. PIN entry is a security-sensitive opaque Material surface; P2P consent has its own soft/animated surface and scrollable consent body. These are intentional presentation variants, not candidates for blind unification.
- Backup import currently uses standard Material `AlertDialog` for paste; upstream uses `DialogSurface` for the import confirmation. The larger gap is confirmation/format semantics, not simply dialog styling.

### Dialog audit status

Inventory is still incomplete. Next inspect the remaining direct dialog call sites, each picker mode, platform dismiss/back/outside behavior, focus restoration, keyboard handling, screen-reader labels, TV/tablet width and scroll constraints. Do not declare a global dialog redesign complete until each call site is classified by intent and risk.

### Verification limits

Read-only source inspection. No source code changes, build/test/CI, iOS simulator, Android emulator or physical-device validation. Main-dev and main remain untouched.


## Backup file bridge and import confirmation audit (2026-11-18)

Verified refs before documentation update: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`; `upstream-sync` `87f17fb5bdf435c801602156329d0efdee9e382a`. Existing audit document blob `f6964e0bd7625e2dbf2a9d2a2f9cf8094075f023`.

### Platform bridge findings

**Android — `NuvioEnhancedBackupFileBridge.android.kt`**
- Uses deprecated `startActivityForResult` with request codes and process-memory pending callbacks/payload.
- Export uses `ACTION_CREATE_DOCUMENT`, writes through ContentResolver; import uses `ACTION_OPEN_DOCUMENT` and requests read permission.
- Import advertises `*/*` plus JSON/text/octet-stream MIME types, then reads the complete stream with `readText()`; no visible input-size bound or streaming validation in this bridge.
- Pending callback state is not durable across process death. Activity recreation/result routing should be verified before changing the bridge; do not assume ordinary rotation loses the callback because the object is process-scoped.

**iOS — `NuvioEnhancedBackupFileBridge.ios.kt`**
- Export writes a temporary JSON file and presents `UIActivityViewController`; callback reports success immediately after presentation, before the user completes or cancels the share/save operation. The returned temporary path therefore means “share UI presented”, not “export completed”.
- Import uses `UIDocumentPickerViewController` and correctly brackets file reading with security-scoped resource access.
- Reads the full selected file into memory and decodes it as UTF-8 without a visible size bound in this bridge.

### Import flow comparison

- Mew's file-import callback invokes `importBackupPayload` directly. The manual-paste path uses an edit/import dialog, but the selected-file path does not show a separate parsed-preview/confirmation stage.
- Upstream `SettingsConfigBackupSection` parses first, rejects invalid format with feedback, then shows a confirmation dialog with different warning copy when sensitive data is included; only confirmation calls `SettingsConfigTransfer.startImport`.
- The Mew bridge is a platform adapter, not a duplicate of upstream's transfer engine. **Classification: SHARED PATTERN / DIFFERENT SEMANTICS. Decision: ADAPT.**
- Keep Android SAF and iOS document-picker/share-sheet implementations, but make export completion semantics honest, bound/validate imported input before parsing, and route both file and pasted imports through one validated review/confirmation flow.
- Preserve Mew's existing credential-stripping/local-credential-preservation behavior until a dedicated portable backup schema and sensitive-data policy are designed. Never treat the upstream sensitive-data toggle as permission to export Mew credentials without an explicit product decision.

### Remaining verification

- Trace Android activity-result dispatch and test recreation/process-death behavior before replacing the legacy result API.
- Inspect iOS share completion/cancellation callback handling and temporary-file lifetime.
- Inspect parser limits, malformed/oversized input feedback, and whether import can partially mutate state before failure.
- No source changes, build/test/CI, simulator/emulator or physical-device checks were performed.


## Profile deletion, lock cleanup, and profile-switch audit (2026-11-18)

Verified source refs: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. Prior `upstream-sync` head `51822a06e2d98fa259ca3ba5b29c82893db53ab6`; document blob before this update `87740e2658ee4089e6fe547178e008f58a732d75`.

### Profile deletion parity gap

**Mew — `ProfileRepository.deleteProfile`**
- Anonymous deletion removes the profile's cached PIN payload, updates the in-memory profile list/active profile, and persists.
- Authenticated deletion calls `sync_delete_profile_data` and then `pullProfiles()`; no explicit device-local cleanup is visible in this method.
- The Mew profile editor exposes PIN setup/removal and delete confirmation. No biometric-auth UI or `ProfileBiometricAuth` implementation appears in the inspected Mew common/android/iOS profile source sets.

**Upstream — same repository and editor**
- Both anonymous and authenticated deletion explicitly remove the profile's local PIN cache, disable profile biometric credentials, remove per-profile server state, and remove subtitle-device preferences.
- Authenticated deletion performs local cleanup after remote deletion succeeds and before refreshing profiles; cleanup failures are logged separately rather than silently changing remote deletion outcome.
- Upstream editor also provides biometric enable/disable for the primary profile, gated by PIN verification, with explicit failure feedback.
- Upstream `ensureLoaded(userId)` clears stale-account PIN payloads and disables biometric credentials associated with the previous account when the cached profile snapshot belongs to a different user. Mew's corresponding path resets in-memory profile state but does not show the same local credential cleanup.

**Classification / decision**
- This is not a cosmetic UI difference. It concerns device-local security artifacts and profile-scoped data cleanup. **Classification: SHARED PATTERN / DIFFERENT SEMANTICS. Decision: ADAPT after auditing Mew's actual profile-scoped storage inventory.**
- Audit every Mew profile-indexed store before adding deletion cleanup; identify which keys are local-only, remotely synchronized, shared-device preferences, or intentionally retained. Avoid deleting global state or another profile's data.
- Evaluate biometric auth as a distinct opt-in feature, not an automatic port. Preserve PIN fallback and platform capability/error behavior.
- Inspect Mew's `clearPin` path separately against upstream's cleanup guarantees; do not infer parity from the matching API name.

### Profile switching

- Both implementations perform broad profile-change invalidation across watched state, tracking, library, watch progress, addons/plugins, player/settings, home, metadata, search, collections, downloads, and notifications.
- Upstream additionally switches custom poster settings, pinned stream sources, shuffle settings, batch-download coordinator state, server repository state, and profile settings sync. Mew includes several Mew-specific repositories (Enhanced settings, AI assistant, external download folder, Live TV) that upstream does not have.
- **Classification: INTENTIONAL PARALLEL with selective missing invalidations.** Do not replace either switch pipeline wholesale. Verify each Mew profile-scoped repository and add only missing invalidation where semantics match.
- Profile editor delete confirmation exists in both. Upstream marks the modal destructive explicitly; Mew uses the same status-modal component without the explicit destructive flag. Review the shared component's visual/action semantics before changing it.

### Reset/defaults audit status

- Repository-wide GitHub code searches for generic reset identifiers returned no indexed matches; this is not proof that reset flows do not exist.
- A complete reset inventory remains open: settings reset, profile reset/delete, sign-out/account change cleanup, app-data reset, and per-feature clear actions must be traced through callers and platform storage.
- No source changes, build/test/CI, simulator/emulator or physical-device checks were performed.


### PIN removal cleanup detail

A focused comparison of `clearPin` and `clearPinWithPassword` found an additional robustness gap:
- Mew clears the remote PIN, calls `pullProfiles()`, then removes the local PIN cache. It has no biometric artifact to remove in its current implementation.
- Upstream separates remote success from local cleanup, re-checks account identity before touching profile-indexed storage, performs cleanup in a non-cancellable IO section, and reports local cleanup failure without falsely reporting that the remote PIN removal failed. It also disables biometric credentials.
- Mew's `runCatching` wrappers also catch `CancellationException` unless explicitly rethrown; upstream explicitly rethrows cancellation in these security-sensitive flows.
- **Decision: ADAPT.** Make remote PIN removal authoritative, cleanup account-scoped local artifacts safely even during cancellation, and distinguish “PIN removed” from “local cleanup incomplete”. Keep the Mew PIN-only design unless biometric auth is separately approved.


## Account-wide cleanup and sign-out audit (2026-11-18)

Verified source refs: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`. Mew `upstream-sync` before this documentation update: `7c81843f3bc4d37b6712d7c1681129703b94ca39`.

### Account cleanup is present, but Mew's coverage is narrower

Mew already has `LocalAccountDataCleaner.wipe()`; it clears profile sync/credential sync state, watch progress, watched/library data, addon/plugin state, settings repositories, caches and platform preference stores. This is not a missing subsystem.

Compared with upstream's cleaner, the inspected Mew implementation does not explicitly:
- Remove all profile PIN cache payloads or perform account-bound biometric cleanup (Mew currently has no biometric implementation).
- Clear `DownloadsRepository` and `BatchDownloadCoordinator` in-memory/local state, or clear unfinished platform download transfers in the Android platform cleaner.
- Clear `ServerRepository` local state.
- Clear `EpisodeShuffleRepository` local state.
- Clear `MemberAccessRepository` local state (upstream-only feature; confirm whether Mew has an equivalent before treating as a gap).

The Android platform preference lists also differ. Upstream includes shuffle, library release schedule, provider-specific auth/sync stores, member access, and download-transfer cleanup; Mew has its own Trakt/Simkl and plugin stores. Do not copy the upstream list wholesale: map each preference name to an existing Mew feature and its actual persistence contract first.

**Classification:** SHARED PATTERN / DIFFERENT SEMANTICS. **Decision: ADAPT selectively** after tracing Mew's download service, pending-source worker, partial-file lifecycle, server persistence and profile scoping. Preserve Mew-specific storage and do not clear global preferences merely because upstream clears a similarly named store.

### Invalid-session cleanup is a separate gap from explicit sign-out

- Mew's explicit `signOut()` calls `LocalAccountDataCleaner.wipe()`, then clears its local-data owner marker.
- Mew's account-owner handoff (`claimLocalAccountData(userId)`) wipes local account data when a different authenticated user claims the device.
- However, Mew's inspected `clearLocalSessionAfterRemoteInvalidation()` clears anonymous auth, cancels sync, clears profile/provider sync state and marks the session unauthenticated; it does **not** call the full local account cleaner. The `NotAuthenticated` session-status branch also sets unauthenticated state without an equivalent full wipe.
- Upstream routes invalid-session/lost-session cleanup through its broader reset path, including account-local cleanup.

This means Mew can retain account-scoped local caches/settings after a remote session becomes invalid, until a later explicit sign-out or a different account claims local data. Confirm intended offline behavior and ensure cleanup is bound to the invalidated user ID before adapting; avoid wiping data belonging to a newly authenticated account during asynchronous session transitions.

**Decision: ADAPT** the account-identity-aware lost-session cleanup, with cancellation/error handling and account-owner guards. Do not clear local data merely because transient remote validation is unavailable.

### Verification limits

Read-only GitHub source inspection. No source changes, build/test/CI, simulator/emulator or physical-device checks. Mew `Main-dev` and `main` remain untouched.


## Download account-wipe and settings-reset audit correction (2026-11-18)

Verified refs: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`; `upstream-sync` before this update `cb7932bdb469b054efd0f7de4b3bd0c5e59491aa`. Prior doc blob: `a296b5bdd9c1ee05c043259b7b9b30cc0c6b368e`.

### Correction: download repository cleanup exists

The earlier account-cleanup comparison missed Mew's `DownloadsRepository.clearLocalState()`. It cancels registered active task handles, clears the in-memory UI state, resets the loaded flag and notifies the live-status platform. This is a real Mew implementation and must be preserved.

However, the account cleaner does not currently call it. The previous claim that Mew has no download in-memory cleanup was incorrect; the precise gap is **wiring the existing cleanup into account invalidation**, plus persistent/platform cleanup.

### Confirmed account data retention in download persistence

- Android stores download payload, external-folder URI and episode-download settings in SharedPreferences file `nuvio_downloads`, with profile-scoped keys. The Android `PlatformLocalAccountDataCleaner.preferenceNames` list does not include `nuvio_downloads`.
- iOS stores the same three values in profile-scoped NSUserDefaults keys. Its account cleaner's `profileScopedBaseKeys` list does not include `downloads_payload`, `external_folder_uri` or `episode_download_settings`.
- Therefore both platform cleaners leave download metadata/settings behind on account wipe, including profile-indexed download queue records and selected external-folder URI. A later account reusing the same profile index can encounter stale metadata/settings.
- Android media files live under `filesDir/downloads`; the platform account cleaner does not remove this directory or its `.part` files.
- iOS media files live under `Documents/nuvio_downloads` and are explicitly excluded from device backups. The platform account cleaner does not remove this directory either.
- `DownloadsRepository.clearLocalState()` cancels in-memory handles but does not itself erase persisted payload, completed media or partial files. Normal per-download cancellation removes the item file and its `.part`; account wipe has no equivalent bulk lifecycle in the inspected path.

**Classification:** SHARED PATTERN / DIFFERENT SEMANTICS. **Decision: ADAPT, high priority.** Integrate existing `DownloadsRepository.clearLocalState()` into account cleanup, then define a safe account-wipe contract for profile-scoped queue metadata, external-folder permission/URI, active transfers, partial files and completed downloads. Do not delete user media silently until product intent is explicit; distinguish “forget account queue” from “delete downloaded media”. Ensure pending-source worker jobs cannot repopulate state after wipe. Do not introduce upstream WorkManager/JobScheduler architecture into Mew's foreground/coroutine downloader.

### Server and shuffle follow-up

The Mew tree at the verified Main-dev SHA has no upstream-equivalent `features/servers/ServerRepository` or `features/shuffle/EpisodeShuffleRepository` files. The server configuration files in Mew's `core/network` are not evidence of the same Jellyfin/Emby account state; upstream's media-server repository and shuffle state are upstream-only features in this comparison. Classify as **FALSE POSITIVE for direct missing-cleanup calls** until Mew introduces equivalent features. Do not add placeholder cleanup APIs.

### Settings reset/defaults — current evidence

- Mew's Settings root has no visible global “Reset all settings” route/action in the inspected implementation.
- Repositories expose `clearLocalState()` primarily for account wipe/profile lifecycle, not as a user-facing restore-defaults operation. For example, `ThemeSettingsRepository.clearLocalState()` resets in-memory values and native-tab bridge state; it is not itself a persisted user reset flow.
- Advanced settings does expose a targeted “clear Continue Watching cache” action. Debrid repository also has a narrower `resetStreamTemplates` operation; this is not a global settings reset.
- Exact GitHub code searches for “Reset to defaults”, “Restore defaults”, and “Reset settings” returned no indexed results. Search indexing is incomplete, so absence of other per-feature reset actions is **not proven**.
- **Decision: NEEDS INVESTIGATION** for a full settings reset inventory. Trace per-page actions and repository/storage reset APIs, then decide whether users need per-section restore defaults, profile-only reset, or app-wide reset. Any future reset must clearly distinguish profile-scoped preferences, global UI preferences, credentials, feature-announcement state, downloaded files and account data.

### Verification limits

Read-only GitHub source inspection. No app source changes, builds, tests, CI, simulator/emulator or device checks. `Main-dev` and `main` remain untouched.


## Home-row placement parity and pending-download worker cleanup audit (2026-11-18)

Verified refs before documentation update: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `017bdaf0d63a2e3d9f2817d9febed43eae6ef5bd`; `upstream-sync` before update `24fa4886cb19b856ac03f5dd98cf0fc03c96be8d`. Existing README blob: `81b365784c2a1cef70bbcc5b6c2d03d92232cf42`.

### New upstream feature: pin rows and hidden rows

Upstream commit `017bdaf0d63a` adds home-row placement and hidden-row management across `HomeCatalogSettingsRepository`, `HomeRepository`, `HomescreenSettingsPage`, and `HomescreenCatalogRow`, with repository tests and localized labels.

- Per-row/collection placement supports default, pinned top, and pinned bottom. Existing collection-pinned-to-top behavior is retained.
- Hidden catalogs/collections move into a separate expandable section; hidden rows can be reordered independently without changing visibility or visible-row order.
- Drag reorder is constrained to the same placement section; attempts to cross pinned boundaries are rejected with feedback.
- Placement is stored in the existing platform home-catalog payload (Android SharedPreferences / iOS NSUserDefaults) under the profile-scoped key. It is intentionally excluded from the profile sync payload; remote sync updates preserve the local placement preference.
- Upstream tests cover top/bottom ordering, hidden-list reordering, placement exclusion from sync, and preservation during remote settings application.

Mew already has catalog visibility, ordering, custom titles, collection pinning and a reset-to-defaults action. Its current item/preference models and settings UI have no equivalent row-placement field, pin-top/bottom controls or hidden-row section. Existing platform storage is already profile scoped, so this can be added without replacing the storage abstraction.

**Classification:** SHARED PATTERN / DIFFERENT SEMANTICS. **Decision: ADAPT.** Port the placement model and ordering constraints into Mew's existing repository; keep placement local/profile-scoped rather than adding it to the shared sync contract. Preserve Mew's hero-source selection, custom titles, catalog definitions, collections and reset behavior. Add common tests for placement ordering, hidden-row reorder isolation, remote-sync preservation and profile switching. Audit reset semantics so “reset catalog layout” resets placement as well as existing ordering/visibility where product intent says it should.

### Pending-source worker lifecycle and account wipe

- Android schedules each pending episode-source search as unique WorkManager work with a network-connected constraint and a delay derived from `nextAttemptAtEpochMs`. Individual search removal/status changes call `cancelUniqueWork`.
- The worker initializes addon, plugin, debrid, profile, download, external-folder, downloader and live-status storage before retrying the search.
- Pending searches are serialized in the same `DownloadsStorage` payload as download queue items. The model stores provider identity and retry/expiry metadata, but no explicit account/user identity snapshot.
- Mew's `LocalAccountDataCleaner.wipe()` does not call `DownloadsRepository.clearLocalState()`, does not clear the Android `nuvio_downloads` preference file, and does not cancel pending-source WorkManager jobs. The existing repository clear method cancels active download task handles only; it does not cancel pending-source jobs.
- Consequently, account wipe can leave persisted pending searches and scheduled Android work behind. A worker may execute after the wipe and re-read profile-scoped storage. Account/profile ownership must be checked before retrying or mutating queue state; cancellation alone is insufficient if persisted work can be recreated.
- iOS pending-source scheduling/cancellation/notification adapter is currently a no-op, so the Android WorkManager lifecycle is not shared cross-platform.
- On repository load, expired or exhausted pending searches are filtered from the in-memory state and persisted normalization; this is a useful stale-entry guard but does not itself cancel already-enqueued WorkManager jobs.

**Classification:** SHARED PATTERN / DIFFERENT SEMANTICS. **Decision: ADAPT, high priority with download cleanup.** Define a single account-wipe sequence that prevents new work, cancels all pending-source work, cancels active handles, clears pending and queue metadata under the correct account/profile owner, then reinitializes only after the new owner is established. Keep completed media deletion separate and explicit. Add tests for account switch/wipe racing a running worker, stale work after process restart, expiry, and cancellation. Do not introduce a new scheduler architecture; retain Mew's WorkManager foreground/coroutine split.

**Investigation limitation:** the tracked source tree contains the Android worker's call to `EpisodeDownloadCoordinator.retryPending(searchId)`, but the corresponding coordinator declaration was not located in the recursive tree or indexed source search. Do not infer that the worker retry implementation is verified until the symbol is resolved and its callers/account guards are inspected. No build was run to determine whether this is generated, supplied elsewhere, or a compile-time issue.

### Verification limits

Read-only GitHub inspection. No app source changes, builds, tests, CI, simulator/emulator or physical-device checks. `Main-dev` and `main` remain untouched.


## Settings dialog and shared glass-surface audit (2026-11-18)

Verified refs before documentation update: Mew `Main-dev` `69b4a96072b506f60dcbe538c5f6b71db3a4a464`; upstream `enhanced` `017bdaf0d63a2e3d9f2817d9febed43eae6ef5bd`; `upstream-sync` before update `a04afce709fc631a17217fcf10a3e62ad4015add`. Existing README blob: `d8af5ca7f7f6d259126df0503647c66380c2c533`.

### Important correction: shared DialogSurface glass treatment

The earlier audit note said Mew's `DialogSurface` includes the app glass backdrop. Re-reading the current `Main-dev` source disproves that: Mew's `core/ui/Dialog.kt` applies dialog tokens, shape, border and solid `surfaceDialog`, but does **not** call `appGlassBackdrop`. Upstream's corresponding `Dialog.kt` adds `appGlassBackdrop(tokens.shapes.dialog)` to the dialog surface and uses its surface-edge treatment. This is a real shared-component parity gap, not merely a call-site difference.

**Decision: ADAPT.** Port the backdrop treatment into Mew's shared component only after checking Mew's own glass enablement/fallback and backdrop lifecycle. Preserve Mew dialog tokens, max width, button layout, accessibility and opaque/security-sensitive exceptions. Do not assume every dialog should be transparent/glassy.

### Settings dialog inventory — partial, not a blanket duplicate

Read-only scan of current Mew settings pages confirms several presentation paths:

- `PlaybackSettingsPage.kt` contains many independent `BasicAlertDialog` pickers (player preference, language, decoder, playback engine, subtitle font/color, autoplay provider/regex, etc.). Several construct legacy Material color-scheme surfaces with fixed 20dp/12dp styling rather than the shared Nuvio dialog tokens. These are not automatically duplicates: their inputs, selection behavior, platform-specific options and content differ, but their presentation layer is inconsistent.
- `DebridSettingsPage.kt` has a large family of `BasicAlertDialog` pickers and a local `DebridDialogSurface`. It is a page-local shared surface pattern, but its choice models and stream-template editors have distinct semantics. Compare/migrate the shell, not the picker behavior wholesale.
- `StreamsSettingsPage.kt` defines a second local `SettingsDialogSurface` with title + tokenized surface. It overlaps the core `DialogSurface` presentation contract and is a candidate for consolidation after checking its call sites and content sizing.
- `TrackingAdaptivePicker.kt` deliberately uses a bottom sheet on phones and a dialog on tablets. Preserve this adaptive interaction; the tablet dialog currently builds its own surface rather than using the shared component.
- `AdvancedSettingsPage.kt` has direct `BasicAlertDialog` implementations for runtime logs and Sentry consent. The logs dialog uses hardcoded English labels/actions and an unbounded text column in the inspected implementation; Sentry consent has a separate explanatory/confirmation flow. Keep consent semantics distinct; improve logs scrolling/localization and shared shell.
- `NuvioEnhancedSettingsPage.kt` uses Material `AlertDialog` for backup export payload and pasted import. This bypasses the Nuvio dialog surface; import also remains without a parsed preview/confirmation stage as recorded in the backup audit.
- `TrackingListPickerDialog.kt` is another direct BasicAlertDialog with custom list/loading/error/save states; it should be evaluated against the adaptive tracking picker rather than presumed equivalent.

**Classification:** mixed. Local dialog shells are **SHARED PATTERN / DIFFERENT SEMANTICS**; `StreamsSettingsPage.SettingsDialogSurface` is a likely **TRUE DUPLICATE PRESENTATION ABSTRACTION** of the core shell, subject to call-site review. The selection/picker bodies are not duplicates.

### UI/UX risks and next checks

- Do not mass-replace every `BasicAlertDialog`: first compare dismissal/back/outside behavior, DialogProperties, focus/keyboard, scroll constraints, selected-value commit timing, destructive actions, loading/disabled states, screen-reader semantics and tablet/TV sizing.
- Mew has an established adaptive sheet pattern; preserve it where phone/tablet interaction differs.
- Security-sensitive PIN entry and P2P consent remain intentional special surfaces; keep them opaque/consent-focused where required.
- Continue the remaining settings and feature-dialog inventory (profiles, addons, tracking, player, download, metadata, provider setup, pickers) and compare corresponding upstream callers before proposing a consolidated component migration.

### Verification limits

Source-only inspection of the listed shared/settings components. Full dialog call-site inventory remains incomplete. No source changes, build/test/CI, simulator/emulator or device checks. `Main-dev` and `main` remain untouched.
