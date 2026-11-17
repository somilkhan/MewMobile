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

### Fresh refs used for this audit
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
