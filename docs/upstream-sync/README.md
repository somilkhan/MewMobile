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
- Decision: **TAKE the repository-boundary validation, ADAPT to Mew's existing 1–5 UI contract**. Added shared min/max constants and `normalizeSubtitleOutlineWidth`; use it on load, set/persist, and the existing stepper. No player redesign or default change.
- Changed files on `Main-dev`: `SubtitleAudioModels.kt`, `PlayerSettingsRepository.kt`, `SubtitleStylePanel.kt`, and `SubtitleStyleStateTest.kt`.
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
