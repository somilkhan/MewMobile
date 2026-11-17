# Upstream alignment status — 2026-11-17

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

- Mew `upstream-sync`: `3ca5433681fcdf53763c31f5e995801800e99b8f`; tree `a57827fbb45db1c25ce054fa3c7858a8f61efa69`.
- Mew `Main-dev`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Mew `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (same head as Main-dev; leave untouched).
- Recursive tree comparison of beta tag vs `upstream-sync`: beta has 2,339 blobs; upstream-sync has 2,340. No upstream paths are missing and no upstream paths were removed. The only differing existing paths are:
  - `iosApp/iosApp/ContentView.swift`: Mew retains its fork-specific iOS host/navigation/tab-bar presentation changes. Preserve these deliberately; review the full file diff before future syncs.
  - `store-beta.json`: Mew adds the `1.1.0-beta.1` beta-store version entry and upstream release metadata.
  - `docs/mew-context/README.md`: Mew-owned documentation overlay, not upstream application code.
- Therefore the current `upstream-sync` source snapshot is already effectively aligned to the exact `1.1.0-beta.1` tag, with the two intentional Mew overlays above. This is a tree-level result, not a build/runtime validation.
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
