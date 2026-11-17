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

Current status: **UPSTREAM-SYNC TREE ALIGNED; MAIN-DEV INTEGRATION AUDIT ONLY**. The five-file snapshot refresh changes only `upstream-sync`; no application code has been integrated into `Main-dev` or `main`.


## Full-history scale and current snapshot checkpoint

- Common ancestor of Mew `Main-dev` and the upstream source history: `f9ad843b14cd3be7fa3ddb800b6961233e0e5b56` (`bump version`).
- From that ancestor, upstream `enhanced` is **2,193 commits ahead**. Mew `Main-dev` is **596 commits ahead of the same ancestor**. This confirms the 2,000+ history concern; it is not 2,193 discrete features to cherry-pick.
- GitHub compare reports at most 300 changed-file entries for this large comparison, so its file list is not a complete audit inventory. Use full recursive trees, release checkpoints, per-area searches and semantic caller review.
- On 2026-11-17, Mew `upstream-sync` was refreshed from its previous source snapshot by copying the five exact remaining changed upstream blobs from `enhanced` into one guarded commit: `2017c854d6e3eeceb49ab8841548f6143034935f`.
- Verification: recursive source-tree comparison now finds all 2,339 upstream blobs present, **zero missing paths, zero differing upstream blobs**, and only the two Mew-owned documentation files as extras. This is exact tree parity, not identical commit history.
- `Main-dev` and `main` remain at `207ef8fa309b8061b73039b2d4e4bde4af65050e`; no app changes have been integrated there. The 2,193-commit semantic audit remains ongoing.

