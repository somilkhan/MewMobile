# CURRENT HANDOFF — SETTINGS IA AUDIT (2026-11-20)

> Current source-of-truth for the settings audit. Older sections below are historical evidence; do not reuse their SHAs as live state. Every future agent must read this entire file before investigating or changing settings.

## Verified repository refs
- Repository: `somilkhan/MewMobile`
- Active target `Main-dev`: live branch HEAD `2b32900663f57fa61468c779602e0bb5b657a6da`.
- Latest inspected application-source HEAD: `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`. GitHub compare confirms the 14 commits after that source HEAD change only `docs/UPSTREAM_PARITY_AUDIT.md`; no application source paths changed.
- Current audit document blob: `25ca32ca9037ceb72dccbd2823daba8b2957f3cc`.
- Protected `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (unchanged).
- `upstream-sync`: `af51bbbc13cdb315a88ef597eb4d2672a60335d5` (live ref rechecked 2026-11-20 16:34 IST).
- Mother upstream `luqmanfadlli/NuvioMobile-Enhanced:enhanced`: `3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6` (live ref rechecked 2026-11-20 16:34 IST). Latest upstream delta remains the player-logo lookup commit outside settings IA. Current route-tree differences are classified in the audit; upstream-only feature implementations remain separate parity work, not empty settings placeholders.
- No `MEW-CONTEXT-SNAPSHOT.md` or `CONTEXT-SNAPSHOT.md` exists on `Main-dev`.

## Current audit status — 2026-11-20 16:34 IST
- **Static settings IA/source audit: COMPLETE.** This means source-tree, route, persistence, duplicate, dialog-contract, localization-key and upstream settings-route comparisons have been consolidated. It does not mean every upstream feature is implemented or that runtime/device behavior is verified.
- Confirmed duplicate controls: **2** — Parental Guide (Playback + Nuvio Enhanced) and TMDB enrichment toggle (Content Discovery + TMDB settings). No new TRUE DUPLICATE was found in the final dialog/persistence/navigation passes.
- Canonical decision matrix is appended near the end of this file. It records ADAPT, KEEP/ALREADY IMPLEMENTED/INTENTIONAL PARALLEL, NEEDS INVESTIGATION and SKIP decisions.
- Runtime verification remains **NOT RUN**: Android device, iOS simulator/device, app process recreation, route serialization tests, locale switching/rendering, RTL/CJK/long-string layout, TalkBack/VoiceOver, keyboard/focus and profile/provider flows.
- No application source changes, builds, tests, merges, promotions or releases were made during this audit continuation.
- **Do not create the separate settings plan MD.** The user will create it after reviewing this audit. Do not implement app changes until the user explicitly approves that plan.

## Mandatory agent read-first and deduplication protocol (2026-11-20)

**This is a required gate for every future agent/session working on settings.**
1. Read this entire file from beginning to end before inspecting or changing settings code. Do not read only this handoff block or rely on chat summaries.
2. Then verify the live `Main-dev` ref and inspect the exact source files/callers named by the latest findings. This document is the audit index, not a substitute for source verification.
3. Search this document for the target setting, page, symbol, dialog, route, persistence key, and duplicate finding before starting an investigation. Reuse existing evidence; do not repeat completed investigations unless the source SHA changed or a specific contradiction/new caller requires re-verification.
4. Maintain one canonical finding per behavior. If a new occurrence is found, append it to the existing finding instead of creating another copy. Record every candidate as TRUE DUPLICATE, INTENTIONAL PARALLEL, SHARED PATTERN / DIFFERENT SEMANTICS, COMPATIBILITY SHIM, FALSE POSITIVE, or NEEDS INVESTIGATION.
5. Before changing code, map setting → visible control → caller/route → state/repository → platform storage → policy/visibility → tests. Similar names/files are not proof of duplicate behavior.
6. Do not implement from this audit document alone. Re-read current source and get explicit user approval for application-source implementation.
7. Every meaningful MD update must include an IST timestamp, target branch, verified source HEAD SHA, previous document blob SHA used as the write guard, scope/files inspected, findings added/changed, and verification status. Read the updated MD back and verify it.
8. Never mark an audit item complete without source evidence. Mark unverified behavior/device checks explicitly as NOT VERIFIED or NOT RUN.

This protocol exists to prevent duplicate implementations, repeated audits, conflicting handoffs, and loss of earlier findings.

## Playback settings audit continuation — 2026-11-20 08:08:27 IST

**Verified source ref:** `Main-dev` at `a7f066297ba000833a981b12f8b57c26ce833f7d`  
**Document write guard:** previous `docs/UPSTREAM_PARITY_AUDIT.md` blob `c383d67f44c22a65f8afc1ed30b8073b402f9024`  
**Files inspected:** `PlaybackSettingsPage.kt`, `SettingsModels.kt`, `PlayerSettingsRepository.kt`, common/Android/iOS `PlayerSettingsStorage`, Android/iOS `P2pSettingsStorage`.

### Confirmed playback IA mapping (audit only; not implemented)

The current Mew Playback route is one page and one large composable column, with these independent sections:
- Player / interface: loading and pause overlays, Player Liquid Glass, parental guide, internal/external player preference, touch gestures, remembered brightness, hold-to-speed, skip durations, toolbar layout.
- Audio: preferred and secondary audio language.
- Subtitles: preferred/secondary language, forced/preferred-only filters, addon subtitle startup mode, AutoSync controls.
- Subtitle rendering: font size/family/import, vertical offset, bold, text/background/outline colors and outline width, Android libass controls.
- P2P: enable/consent, hide stats, torrent profile, cache size and clear-cache action; section is gated by `P2pSettingsRepository.isVisible`.
- Stream selection: reuse-last-link and cache duration; auto-play mode, regex, timeout, source scope, addon/plugin allowlists.
- Decoder/video: Android engine, libmpv output/hardware/YUV options, ExoPlayer decoder priority, DV7 mapping and tunneling; section is Android-only and options are disabled for external player or incompatible engine.
- iOS output: audio output, hardware decoder, extended dynamic range, colorspace hint, target primaries and transfer; iOS-only.
- Skip segments: intro/outro/recap, Anime Skip client ID, IntroDB submission/API key.
- Up Next: auto-next, fallback, binge-group preference/reuse, threshold mode and threshold values.

**Recommended planning map:** adapt upstream's modular Playback navigation rather than wholesale-copying its files. Split into Player/Interface, Audio, Subtitles, Video/Decoder (platform-specific controls retained), Stream Selection, Skip Segments, Up Next, and P2P where enabled. Keep iOS output options in the appropriate Video/Audio subpages. This is an IA proposal only; final placement should be checked against upstream's current page tree and the completed full-control map.

### Persistence and platform findings

- Playback controls read/write through the shared `PlayerSettingsRepository` and `PlayerSettingsStorage` expect/actual contract. Android uses SharedPreferences and iOS uses NSUserDefaults; both use `ProfileScopedKey`. Classify these as **INTENTIONAL PARALLEL** platform implementations, not duplicate repositories. Preserve profile scoping and existing keys during any UI/page split.
- P2P preferences are separate from PlayerSettings: `P2pSettingsStorage.android.kt` and `P2pSettingsStorage.ios.kt` both use `ProfileScopedKey`; this is also **INTENTIONAL PARALLEL**. P2P clear-cache is an action with live playback/cache-state gating, not a normal persisted preference.
- External-player selection disables relevant internal player, audio and subtitle controls; Android exposes subtitle-forwarding/skip-segment options, while iOS exposes an external-player app selector. Preserve these conditional rules and stored values; do not infer that hidden/disabled means deleted.
- Android decoder controls are hidden on iOS; iOS audio/video output controls are shown only on iOS. This is **INTENTIONAL PLATFORM SCOPE**, not duplicate/missing functionality.
- The generic enum picker is named `IosEnumSelectionDialog` but is also used for Android libmpv and P2P options. This is a **SHARED PATTERN / DIFFERENT SEMANTICS** naming mismatch; rename/reuse only if a later implementation plan warrants it. Do not create a second generic enum dialog.
- The player-selection dialogs repeat a common selected-card visual pattern, but options and behavior differ. Classify as **SHARED PATTERN / DIFFERENT SEMANTICS**, not confirmed duplicate controls. Accessibility selected/radio semantics and keyboard/focus behavior still need review.

### Open items (do not mark as bugs without further evidence)

- Complete the per-control map against search entries, settings storage keys, runtime callers and tests; especially distinguish settings-page controls from player-overlay/quick-settings controls.
- Investigate repository state fields that are not visibly rendered in this page (e.g. memory-safe buffer, volume boost, resize mode, and some iOS video controls). They may be owned by player UI/overlay surfaces; do not add new settings rows until callers are mapped.
- Review every Playback dialog for selection semantics, focus/keyboard behavior, scroll limits, dismissal and error handling. Most selection dialogs apply immediately and dismiss; preserve that interaction unless a concrete usability problem is found.
- The page is currently composed as one LazyList item containing a large Column. Record this as a structural/performance review point; actual scrolling/performance impact is **NOT DEVICE VERIFIED**.
- Previously recorded Parental Guide duplication is not a new finding here; retain the existing canonical duplicate record. Do not add a second duplicate entry.

**Classification summary:** Android/iOS storage pairs = INTENTIONAL PARALLEL; generic enum picker = SHARED PATTERN / DIFFERENT SEMANTICS; selected-card dialog implementations = SHARED PATTERN / DIFFERENT SEMANTICS; page-level modularization = ADAPT; hidden model fields = NEEDS INVESTIGATION. No new TRUE DUPLICATE confirmed in this continuation.

**Verification:** source/document reads only; no application source changes; builds/tests/device checks NOT RUN. Main-dev was verified at `a7f066297ba000833a981b12f8b57c26ce833f7d` immediately before this documentation write. `main` was not written.

## Latest audit refresh — 2026-11-20 08:11:36 IST

- Target branch: `Main-dev`
- Verified branch HEAD before this refresh: `866341338fe02fc56aedf6dae8fb43de04631242`
- Application source baseline inspected: `a7f066297ba000833a981b12f8b57c26ce833f7d`
- Previous audit document blob/write guard: `710cb34ee47b3cf1ce215272d141ffcfa07aa38c`
- Protected `main` rechecked: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (unchanged)
- New audit coverage: Playback sections/dialog patterns and platform storage; four playback-related settings misplaced in Nuvio Enhanced; first-pass mapping of all 13 Enhanced sections; Account/Profile Insights/Advanced/Notifications.
- Remaining: full control/search/persistence mapping for Appearance, Addons, Plugins, CloudStream, Integrations and Tracking; complete dialog copy/state/accessibility audit; localization sweep; route/back and tablet restoration migration map.
- Source changes: NONE. Builds/tests/device verification: NOT RUN.
- Mandatory read-first/deduplication protocol is active. Every next agent must read this complete document before investigating or implementing.

## Latest audit continuation — 2026-11-20 08:17:03 IST

- Target branch: `Main-dev`
- Verified branch HEAD before this handoff refresh: `fa85f0e5b00dde0e6448b5fcd00800b929dcd3df`
- Application source baseline: `a7f066297ba000833a981b12f8b57c26ce833f7d`
- Previous audit document blob / write guard: `0a9617f9f109c8fdc895fe98f73a33270a623261`
- Protected `main` rechecked: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (unchanged).
- New coverage: Integrations/Tracking/Content Discovery mapping; provider storage ownership; Plugins/CloudStream source-set variants; Appearance theme/display/layout controls; adaptive tracking picker; shared settings-row accessibility review.
- Confirmed: provider integrations remain separate state domains; tracking provider cards are intentional parallel providers; Plugins/CloudStream source-set actuals are intentional platform-flavor implementations; no new duplicate finding was added. Existing TMDB duplicate remains canonical.
- Accessibility follow-up: custom SettingsSwitchRow / picker selected-state semantics need TalkBack/VoiceOver verification; this is NEEDS ACCESSIBILITY VERIFICATION, not a confirmed defect.
- Remaining audit: delegated Addons content and detailed Enhanced feature callsites; complete dialog inventory/copy/accessibility/dismissal pass; full search-entry ↔ visible-control ↔ persistence map; localization sweep; navigation/back/tablet restoration compatibility map; NEW badge comparator test design.
- Source changes: NONE. Builds/tests/device checks: NOT RUN.

## Latest audit continuation — 2026-11-20 08:20:32 IST

- Target: `Main-dev`; verified HEAD before this refresh: `d281eaa8e32fa6e59d2343bd8957f2d75a291aec`.
- Application source baseline: `a7f066297ba000833a981b12f8b57c26ce833f7d`; compare confirms all commits since baseline changed only this audit MD.
- Previous document blob / write guard: `37cf4c204b93c887bb2b826af3654cca32c8fc9a`.
- `main` rechecked: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (untouched).
- New confirmed issues: Plugin manifest password fields are not masked; Debrid API-key dialog is also unmasked (one canonical credential-visibility finding with two affected surfaces); Debrid per-resolution/per-quality pickers show the wrong “Max Results” dialog title.
- New consolidation: split Debrid provider setup into Services → Debrid and its result filtering/formatting into Watch → Stream Selection while retaining one repository/state owner.
- Audit source changes: NONE. Builds/tests/device checks: NOT RUN.
- Remaining: complete Enhanced caller/data-source mapping; inspect all remaining dialog states/copy/validation/dismissal; finish control/search/persistence coverage; localization sweep; verify accessibility roles/selected states; route/back/tablet restoration migration map; final deduplicated migration matrix.



## Latest audit continuation — 2026-11-20 10:22:00 IST

- Target: `Main-dev`; verified HEAD before this handoff refresh: `0bcbf4f85a4ce4a6ac9efe9b27b7e09f8e7f1dff`.
- Previous document blob / write guard: `f7e96deb9c454d332dca37cf7afb1dd2c32fc595`.
- Application source baseline: `a7f066297ba000833a981b12f8b57c26ce833f7d`; compare confirms only `docs/UPSTREAM_PARITY_AUDIT.md` changed since baseline.
- Protected `main`: `207ef8fa309b8061b73039b2d4e4bde4af65050e` (unchanged).
- New verified mapping: Concierge/Smart Resume/Profile Stats/Smart Shelves/Quiet Home → Look → Home Experience; Release Radar filters/digest and Library Health → Watch → Library; Continue Watching ready badge → Watch → Continue Watching; Live TV visibility → Watch → Live TV.
- Library Health is now confirmed as a Library presentation card, not System diagnostics. Home Concierge and Library Release Radar share underlying signal data but are distinct presentations; no duplicate settings found.
- Source changes: NONE. Builds/tests/device checks: NOT RUN.
- Remaining audit: finish detailed settings control → search index → navigation route → repository/storage → feature-policy map; inspect all remaining dialog dismissal/validation/accessibility behavior; complete localization sweep; verify back-stack/tablet restoration and persisted category migration; then produce the final deduplicated settings migration plan. Do not implement until the audit and plan are complete and the user approves.

## User-approved objective
Reorganize Mew and upstream settings into one discoverable Mew settings experience. Use upstream Enhanced's five-category model as a starting point, not a wholesale copy. Preserve Mew identity, features, profiles, persisted preferences, Android/iOS differences, navigation behavior, and policy gates.

## NEW label rule
A newly added setting/page gets a small, subtle **NEW** label for **three app releases/updates inclusive**. Example: introduced in 0.5.2 → show through 0.5.4 → hide from 0.5.5. Use explicit per-setting release metadata (`introducedInVersion` + `visibleThroughVersion` or equivalent), not update-count arithmetic. It must be app-global, not profile-scoped and not dismissible. The existing profile-scoped `seenFeatureIds` / release-highlights mechanism is a different feature and must not control this badge. Use the generated app version name as the installed-version source; version comparison must safely handle unknown/unparseable values and prerelease strings.

## Additional settings mapping findings (verified at Main-dev 5d05e2a)
- **Tracking category mismatch confirmed:** `SettingsPage.TraktAuthentication` is still categorized as Account and listed in the root Account section, while `TrackingSettingsPage` contains Trakt/Simkl provider cards, sync data-source choices, watch-progress source, discovery/recommendation options, and anime tracking. This is a Services concern, not account identity. Move its category/root placement to Services in the proposed IA, while retaining the enum route name and old saved-navigation compatibility.
- **NEW badge version source confirmed:** Gradle generates `AppVersionConfig.VERSION_NAME` from `MARKETING_VERSION` in `iosApp/Configuration/Version.xcconfig`. `VersionUtils` is scoped to the updater and its parser extracts numeric semantic-version parts while ignoring prerelease suffixes; do not reuse it without dedicated prerelease/invalid-version tests. Keep badge expiry deterministic and based on the app's installed release version.
- Search currently models row-level entries as `SettingsSearchTarget.Page`; selecting a control result opens the containing page rather than revealing/focusing the exact control. Treat this as a known discoverability limitation, not a broken route; exact-control reveal would require a separate scroll/focus target contract and must be audited against lazy-list state and platform behavior.


## Audit status — refreshed 2026-11-20 13:15:18 IST
- **Progress estimate: ~87%; audit remains IN PROGRESS.** Read-only audit/documentation only; no application source changes.
- Confirmed duplicate controls remain **2** — Parental Guide (Playback + Nuvio Enhanced) and TMDB enrichment toggle (Content Discovery + TMDB settings). No new true duplicate confirmed in this continuation.
- Confirmed navigation mismatch remains **1** — Content Discovery → TMDB setup shortcut has a static Integrations parent, so Back can return to the wrong place.
- Confirmed theme contrast risk remains: white-on-accent `primary/onPrimary` pairs fall below 4.5:1 for several palettes; custom accent also hardcodes white foreground.
- Localization gaps now tracked: Advanced Logs; Plugin dialog static copy; Playback libmpv descriptions and iOS decoder Off label; CloudStream plugin-update notification copy. Keep each affected surface attached to its canonical localization finding; no duplicate issue records.
- Confirmed dialog/UX findings: Debrid API key and manifest-defined plugin secrets are unmasked (one canonical credential finding); Debrid per-resolution/per-quality pickers use the wrong title; device-auth Cancel closes but preserves a pending session; subtitle-font import failures are silently discarded; iOS font import does not validate file content like Android.
- Playback and Debrid dialog bodies have had a static interaction-contract pass. Runtime accessibility/focus/keyboard and actual platform behavior remain NOT VERIFIED.
- Notifications: Episode Release Alerts are profile-scoped; Android/iOS storage and scheduling are intentional parallel actuals. Sentry is app-global and Android-only. Account sign-out wipes local account data after attempting remote sign-out/fallback; preserve.
- Settings category restoration risk remains: tablet `selectedCategory` uses `SettingsCategory.valueOf`; add a safe legacy parser for Account, General, Enhanced, About and Advanced before category migration.
- Downloads manager remains a utility screen, not upstream Downloads preferences. Do not import upstream Downloads settings or iOS folder picker without mapping current storage/worker semantics.
- Search still targets containing pages, not exact controls. Keep utility targets (Downloads, Collections, Switch Profile, Check for Updates) distinct.
- NEW badge rule and comparator test matrix are recorded. Existing updater VersionUtils ignores prerelease precedence; do not reuse without a separate tested contract.
- Subtitle font file cleanup must not delete files until cross-profile references are understood.
- Platform variants (Android Material sheets, iOS UIKit sheets, iOS-only playback settings, engine/P2P/policy gates) must be preserved. iOS simulator/device verification not run.

## Proposed hierarchy (not implemented)
- **You:** Account, Profiles, Profile Insights, Favorite People, profile/startup preferences.
- **Look:** Appearance; Layout (Homescreen, Streams presentation, Continue Watching presentation, Poster Customization, Meta Screen/Details).
- **Watch:** Playback subpages (Player, Stream Selection, Up Next, Skip Segments, Interface, Audio, Subtitles, Video/Decoder, Buffer, P2P when supported); Downloads preferences only if separately justified; Content Discovery (Addons, Plugins/CloudStream when policy permits).
- **Services:** Integrations (AI, TMDB, MDBList, Debrid); Tracking (Trakt, Simkl, anime tracking).
- **System:** Notifications, Network/DNS, Diagnostics/Logs/Sentry, Storage/Cache, Backup/Import, About (Supporters, Licenses/Attributions, Privacy/version).
- Keep Downloads manager and Watch Party as utility shortcuts, not settings preferences.
- Keep NuvioEnhanced and old SettingsPage/category names as compatibility routes until migration and saved-state behavior are tested.

## Next audit work
1. Finish remaining page/control → search entry → route → state/repository → storage/sync → policy/platform mapping for Appearance, Addons, Plugins/CloudStream, Integrations, Tracking, Profiles and About.
2. Complete remaining dialog review outside Playback and Debrid: Addons install, Plugins/CloudStream schema dialogs, Integrations/provider auth, Tracking sync/adaptive picker, Sentry, Account and Profile sheets. Review validation, cancellation, error recovery, loading and destructive states.
3. Follow up the 1,162-key localization coverage report: verify Compose resource fallback behavior, inspect missing high-use settings keys, prioritize untranslated UI copy, and validate RTL/CJK/long-string layout. Do not assume key absence means runtime crash.
4. Finish navigation/back-stack/tablet restoration compatibility matrix, including legacy category parsing, contextual parent routes, deep links and state restoration for granular Playback subpages.
5. Consolidate all findings and upstream candidate decisions into the final deduplicated settings migration matrix. After audit sign-off, create the separate plan MD.
6. No app source implementation until the user reviews and explicitly approves that plan. Run targeted tests/builds only after approval; report iOS/device verification separately.

## Verification status
- Builds/tests: **NOT RUN** for this audit continuation.
- Android/iOS device or simulator: **NOT RUN**.
- No merges, promotions or releases.
- Reverify all branch SHAs before any future write.

---

# MewMobile Upstream Parity Audit

> **Authoritative current state — 2026-10-30:** This document contains historical audit sections below; older branch heads and status statements are not current. Use this block and the final dated sections for present status. Current upstreams are Nuvio Mobile `cmp-rewrite` `53869a0279c59abdef23da2962eb2a1c80e6caa5` and Nuvio Enhanced `enhanced` `781157e0033db40361b662525b06126e184ffd1c`. PR [#38](https://github.com/somilkhan/MewMobile/pull/38) remains draft and unmerged. Phase 5 is **NOT READY**: candidate CI must pass for the exact current head, an installable APK must be built and verified at the Phase 5 gate, and physical-device verification remains pending.

**Audit date:** 2026-10-23  
**Target:** Android-first Mew app, preserving Mew-specific CloudStream integration and existing player/profile behavior.  
**Policy:** Full relevant feature parity is the objective; implementation is dependency-aware and selective by design. Never wholesale-copy upstream files where Mew has diverged.

## Current repository state

- Mew integration branch: `sync/upstream`
- Repository hygiene: the existing `libass-android` gitlink now has its verified `.gitmodules` URL; normal CI still skips submodule checkout, but Git no longer reports a missing URL during cleanup.
- Baseline implementation audit starts at `9742b0cfdfd933039759c1700737d152500076b5`; the current follow-up stack includes subtitle shadow, PiP subtitle scaling, torrent identity fixes, configurable seek behavior, and AutoSync. AutoSync integration commit `4514a6b3d33a40d663a3254f9e83c6d2823dfe17` passed host tests and Android Kotlin source compilation in [run 39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760); audit-only follow-up `6003009e936d53d72a087819fd1872151efe8302` also passed in [run 39885320841](https://github.com/somilkhan/MewMobile/actions/runs/39885320841). The current follow-up stack routes AutoSync index requests through the shared DNS-aware client and bounds custom font imports on Android/iOS. Latest CI results are tracked in Actions; APK packaging and upload remain intentionally skipped. Device verification remains for Phase 5.
- Review PR: [#38](https://github.com/somilkhan/MewMobile/pull/38), open and draft, base `enhanced`.
- Feature candidate `e9ec8221de90b64095b6861724dacc1ba53eda28` passed host tests in [PR CI run 39869676551](https://github.com/somilkhan/MewMobile/actions/runs/39869676551). That PR run skipped APK build, package-ID assertion, signing verification, and artifact upload. A separate [push run 39869669811](https://github.com/somilkhan/MewMobile/actions/runs/39869669811) successfully built and verified a Full Debug APK with application ID `com.mew.mobile.debug`, checked signing, and uploaded `mew-cloudstream-full-debug` (archive SHA-256 `0862dcd050e346ba67e9211e5f1461eafe67ca41d775ae127ccf4717d6d919f0`; expires 2026-10-25). That artifact is from `e9ec8221...`, **not** current HEAD, so it is not a valid Phase 5 handoff. The latest source adds updater cancellation checks with partial-file cleanup on failure/cancellation, subtitle URL log redaction plus host regression tests, selected-DNS routing across core Android HTTP clients, and an Android-source compile step that does not package an APK. Current-head CI passed again after AutoSync integration in run [39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760): host tests and Android Kotlin source compilation passed. APK packaging, package-ID assertion, signing verification, and artifact upload were skipped as intended; APK generation remains deferred until the Phase 5 device-testing gate. CI cannot establish real-device behavior.
- Current package target: `com.mew.mobile.debug` for Full Debug. Package migration is a breaking install/update boundary versus old IDs; do not claim app-data migration is automatic.
- Upstream release baseline: Nuvio Mobile and Nuvio Enhanced `0.6.3-beta`. Live comparison as of 2026-10-22: Nuvio `cmp-rewrite` is 11 commits ahead of the tag (inspected head `e6c399e014939cd63dd85a3fc5d248c2bacccbe4`); Nuvio Enhanced `enhanced` is 12 commits ahead (inspected head `ae535226b1488a60d59613c1502fbb717cbb333f`). The delta includes libmpv add-on subtitle AutoSync, ExoPlayer PiP subtitle scaling, iOS PiP decoder recovery, player badges/panel insets, tracking rating-map/toggle work, and player toolbar/gesture layout changes. Assess each against Mew's architecture; do not copy whole player or settings files.

- **Fresh upstream recheck (2026-10-23):** Nuvio Mobile `cmp-rewrite` remains at `e6c399e014939cd63dd85a3fc5d248c2bacccbe4`; its comparison against the prior inspected head has no additional commits. Nuvio Enhanced `enhanced` is at `a3e885b75356c454e1c51637a48b70b83afc2781`, 19 commits after `ae535226b1488a60d59613c1502fbb717cbb333f`. The new delta includes configurable skip-back/skip-forward durations, an Android DoH resolver, an experimental native Hi10p/10-bit H.264 ExoPlayer decoder, iOS VideoToolbox recovery changes, selected-tab visibility polish, and small player/settings refinements. Mew already has the newer solid selected-tab pill fill/edge in `FloatingNavigationBar.kt`; its configurable DNS-over-HTTPS implementation is Mew-specific, so do not replace it with upstream `NuvioDns`. The settings and normalization tests were already present; follow-up parity fix makes double-tap seek accumulation use the direction-specific configured duration, with regression coverage for 5s backward and 30s forward. Mew already has `hwdec-software-fallback=300` in `iosApp/iosApp/Player/MPVPlayerBridge.swift`, so no duplicate iOS decoder option was added. The native Hi10p decoder remains a high-risk ABI/codec/runtime change and is deferred pending compatibility, licensing, packaging, and device-performance review. Upstream profile-card/theme-picker changes are visual redesigns and are not in scope.

## Already implemented on the audited Mew head (source/CI verified)

- CloudStream repository install/update operations use a repository-owned coroutine scope; reconciliation retries after plugin loads and overlapping reconciliation is skipped.
- Home catalog loading runs alongside addon catalogs and progressively scans every runnable CloudStream provider. The former 30-second global scan timeout and six-row provider cap are removed; each provider still has its own 5-second timeout and concurrency is bounded at four. A provider with a main page contributes one row, avoiding category fan-out. Follow-up fixes ensure addon catalogs do not suppress CloudStream provider rows and include Asian-drama providers in series search. CI passes; actual 81-provider behavior and memory/performance remain **not device verified**.
- CloudStream-origin titles enter the shared stream-loading pipeline; exact direct routes are preserved while eligible CloudStream providers and Nuvio addon scrapers are queried. Opaque CloudStream IDs are not sent to addon endpoints that require external IDs.
- CloudStream stream results honor the repository-grouping preference with bounded provider concurrency.
- Series/episode matching has explicit season normalization and prevents cross-season fallback. Latest head includes additional guards against unverified provider-ranked series matches and fallback to a series route when a requested episode is missing. Autoplay remains on the requested episode rather than resetting to the series default. CI passes; real-provider episode correctness remains **not device verified**.
- TMDB remains optional; the Discovery toggle was removed per maintainer direction. Generic TMDB API/collection access honors the enabled state, cache invalidation is handled, and CloudStream detail/playback routes do not require TMDB.
- Subtitle matching, series continuity, stream matching, trailer detection, and TMDB paths have host regression tests in the current PR.
- Simkl playback refresh preserves a locally newer playback row when Simkl returns a stale copy of the same title/episode immediately after a pause/stop write; rows absent from the fetched account list are still removed. The regression fixture holds library activity timestamps constant so it actually exercises playback-only refresh. Host tests and Full Debug build passed on `9742b0cfdfd933039759c1700737d152500076b5`.
- Stream-card text contrast fix is included.
- The cinematic details background now reuses the hero image painter when available, avoiding a second image request/decode; fallback image loading remains gated by deferred metadata work.
- Continuous pinch-to-zoom is ported into Mew's existing gesture pipeline for ExoPlayer and libmpv, clamped to 0.8x–2.0x, with zoom reset when the resize mode changes. ExoPlayer scales the content frame to avoid zoom escaping the video frame's black bars. Host tests and Full Debug build pass.
- Android portrait/landscape playback toggle is ported with lifecycle restoration and reset on player exit. iOS behavior remains unchanged and the toggle is hidden there. Host tests and Full Debug build pass.
- ExoPlayer PiP subtitle scaling now computes fitted video height using the full-screen subtitle view bounds and video pixel aspect ratio; it uses a bounded fallback if geometry is unavailable. Pure geometry regression tests cover wide-video letterboxing, viewport-height fit, pixel aspect ratio, and invalid dimensions. The implementation passed current-head host tests and Android Kotlin source compilation in [run 39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760); device PiP rendering is not verified.
- iOS Now Playing publication now avoids rewriting identical metadata every playback poll. Position updates are compared to the OS's expected elapsed position using monotonic uptime and a 1,500 ms drift threshold; changes to playing state, duration, or playback speed still publish immediately. Existing Mew remote-command handlers are retained. This Swift source port is not covered by Android CI and still needs an iOS/Xcode build plus device verification.

## Confirmed upstream gaps requiring assessment/porting

These are confirmed by comparing the current Mew tree with the current Nuvio Enhanced `enhanced` tree. A missing file is evidence of a likely gap, not proof by itself that the entire feature is absent; inspect call sites and settings before porting.

| Upstream area | Evidence in Enhanced | Mew status / action |
|---|---|---|
| Android Liquid Glass navigation controls and user-configurable effect/icon color | `FloatingNavigationBar.kt`, `LiquidGlassEffectSettings.kt`, `LiquidGlassNavigationSurfaces.kt`, `NavigationBarSettingsSheet.kt`, associated storage and host tests | **Navigation style ported and selectable** through the existing Settings → Layout → Navigation selector. Mew's existing styles remain available and default behavior is unchanged; `NavBarStyleTest` covers the existing style keys. Current-head CI run [39878156237](https://github.com/somilkhan/MewMobile/actions/runs/39878156237) passed host tests and Android Kotlin source compilation. Rendering, accessibility, insets, and performance remain not device verified. The upstream's full configurable effect/icon-color settings remain intentionally deferred to preserve Mew's existing settings surface; the user-requested style selector is already integrated. Rendering, accessibility, insets, and performance remain **not device verified**. |
| Player Liquid Glass with opt-in toggle | `PlayerLiquidGlass.kt`, `PlayerLiquidGlass.android.kt`, related settings and resources | **Ported:** opt-in toggle (off by default), Android video-frame capture only while enabled and controls are visible, translucent fallback on unsupported devices/iOS, and glass surfaces on existing player controls. Frame-capture performance, accessibility and rendering remain not device verified. |
| Android player orientation toggle and portrait video support | Listed in Enhanced 0.6.2-beta release notes | **Ported** to Mew Android; orientation is restored when leaving the player. CI passed; device rotation/lifecycle behavior remains **not device verified**. |
| Custom aspect-preserving pinch zoom / Fill behavior | Listed in Enhanced 0.6.2-beta release notes | **Ported/adapted** to Mew's existing gesture pipeline; ExoPlayer targets the content frame, and both ExoPlayer/libmpv implement zoom. CI passed; gesture arbitration and aspect-ratio behavior remain **not device verified**. |
| Optional libmpv libass renderer for ASS/SSA | Listed in Enhanced 0.6.2-beta release notes; Enhanced adds native C/JNI plus a substantial Kotlin subtitle stack | **Intentionally deferred for this parity pass.** It replaces a core subtitle renderer and introduces a native AAR/runtime path with licensing and distribution-obligation questions, plus substantial playback, tunneling, and subtitle regression risk. Keep Mew's existing renderer until the exact dependency/source license and redistribution terms are verified and a dedicated compatibility test plan is approved; this is not a safe selective port. |
| Libmpv add-on subtitle AutoSync | Nuvio `cb581ea78e2ff9896d1d7a39e65d450aa29d94d3` adds a cancellable coordinator and SRT/WebVTT/ASS retimer for the libmpv path | **Ported on `sync/upstream`; automated validation passed:** shared timeline retimer/parser, ExoPlayer sidecar integration, libmpv retimed-file replacement, profile-scoped preferences, cancellable subtitle HTTP, indexed reference loading, localized outcomes, and wrapper-preserving integration with Mew's libass Matroska extractor. Follow-up changes adapt the sidecar/HTTP APIs to Mew and keep explicit subtitle selection independent of the startup-only preference. Timeline matching/subtitle-format host tests and Android Kotlin source compilation passed in [run 39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760). Phase 5 must still verify actual playback synchronization on-device. |
| iOS PiP decoder recovery | Nuvio `9721a32fc42a34cb62a8db9040042b89c622fd2e` uses `hwdec-software-fallback=300` when VideoToolbox is interrupted during PiP | **Already present in Mew:** `hwdec-software-fallback=300` is configured in `iosApp/iosApp/Player/MPVPlayerBridge.swift`; no duplicate option was added. iOS build and device verification remain outstanding. |
| Player side-panel system-bar inset | Nuvio `b9fbc09c83d44d7b76d813f0c99a4c2f6c5c38af` keeps the end-side player panel clear of Android navigation bars | **Ported in current candidate:** Mew's existing `PlayerSidePanel` adds navigation-bar inset padding without replacing its layout. Final-candidate Android CI is tracked by the linked Actions run; device verification remains outstanding. |
| Configurable player skip-back/skip-forward durations | Enhanced commits `465ad56c44`, `ddb752a8c7`, and `ef5a32f657` add profile-scoped settings, playback UI, and seek behavior | **Ported:** 5/10/15/30-second choices, 10-second defaults, profile-scoped Android/iOS persistence, settings sync payload, dynamic numbered seek icons/accessibility labels, and player-control wiring. `PlayerSkipSecondsTest` covers supported and invalid values. |
| Mini-player setting tied to PiP button | Listed in Enhanced 0.6.2-beta release notes | **Assessed, not directly applicable:** Mew has Android PiP lifecycle/remote controls but no separate mini-player surface. Preserve existing PiP behavior; do not import an upstream setting without a matching Mew feature. |
| Enhanced player settings and buffering controls | Enhanced's settings model includes additional controls for loading overlay, pause overlay, seek preview, custom playback buffers, disk cache, audio passthrough and memory behavior | **Partial port:** pause-overlay visibility is user-configurable (default remains enabled), with profile-scoped Android/iOS persistence and settings sync. **Intentionally deferred:** custom buffers, disk cache, audio passthrough, and memory-behavior controls differ by ExoPlayer/libmpv and need per-engine performance, stability, and device testing; do not expose controls whose runtime semantics are not equivalent. Seek-preview frame optimization remains deferred until a reproducible accuracy issue is demonstrated. |
| Newer upstream navigation / stream-card / cinematic details refinements | Enhanced adds configurable Liquid Glass navigation, player surfaces and a cinematic stream-filter field | **Partially ported:** stream-card contrast fix, cinematic hero artwork reuse, and selectable Liquid Glass navigation are ported. Player Liquid Glass is ported as an opt-in feature; cinematic stream-filter glass remains intentionally deferred because Mew's current app-level backdrop is scoped to navigation and needs a separate architecture-aware integration. Mew's navigation integration uses the existing Settings → Layout → Navigation selector and preserves existing styles. Remaining navigation parity is the upstream effect/icon-color settings plus real-device rendering/accessibility/performance validation; do not replace Mew's navigation or settings architecture wholesale. |
| Android ExoPlayer subtitle sizing in PiP | Enhanced commit `f8dda1c821` derives subtitle size from the fitted video height outside PiP | **Already present in Mew:** `PipSubtitleScale.kt` computes fitted video height with pixel-aspect-ratio validation; `PlayerEngine.android.kt` applies fractional subtitle sizing and has host tests for letterboxing, pixel aspect ratio, and invalid geometry. Device/PiP resize behavior remains unverified. |
| Player audio-language badge pack | Enhanced commit `83e528a78e` adds a player badge surface driven by selected audio track and stream metadata | **Intentionally deferred:** this is a new player surface/settings behavior, not a correctness fix. Mew already has its own imported stream-badge rules, but the audio-track badge contract would need ExoPlayer/libmpv parity, settings integration, localization, and device-layout review before adding more controls to Mew's player. |
| Settings backup import/export | Enhanced commit `56db14744c` adds JSON backup export/import under Advanced settings | **Already present in Mew:** `NuvioEnhancedSettingsPage.kt` exposes export/import through `ProfileSettingsSync.exportBackupJson/importBackupJson`; Android and iOS file bridges are present. The current profile-scoped player settings sync-key list includes the new toolbar layout. No duplicate backup screen is needed. |
| Player end-time / time-left indicator | Enhanced commits `dece182429` and `5952638000` add end-time and remaining-time presentation around the seek bar | **Already present in Mew's own player design:** `PlayerClockEndTimeOverlay.kt` and `PlayerControls.kt` calculate end-at and remaining labels. Keep Mew's existing placement and avoid a duplicate upstream label. |
| Home hero ratings and age-rating metadata | Nuvio commit `d904677544` adds rating/age-rating rows via `HomeHeroMetaRows.kt` | **Equivalent behavior already exists in Mew's custom hero:** `HomeHeroSection.kt` renders streaming-showcase ratings and age-rating signals. The implementation is intentionally not copied component-for-component because Mew's hero layout and metadata path differ. |
| Seekbar thumbnail/frame accuracy optimization | Enhanced commit `74305199aa` optimizes AIOStreams seek-preview frame selection | **Not copied wholesale:** Mew has its own `seekpreview/SeekPreviewFrameSource.android.kt` integration and player-engine routing. Reassess only against a reproducible frame mismatch and benchmark the adapted implementation; device-level seek-preview accuracy/performance remains unverified. |
| Discord Rich Presence and iOS token/keychain login | Enhanced's post-tag branch adds a cross-platform Discord Gateway/RPC stack, Android and iOS storage/platform adapters, player runtime integration, settings UI and localized resources | **Intentionally deferred as a separate product feature.** It adds a persistent network connection, account/token lifecycle, platform-specific secure storage, reconnect behavior, and privacy/consent requirements; these are not small upstream reliability fixes. Revisit only with an explicit opt-in UX, security review, and Android/iOS lifecycle tests. No Discord token handling was added in this parity pass. |
| Binge-group startup filename/infoHash identity | Enhanced commit `1b85c6458cfb46b61849fa1a1437aa76ff6a117f` seeds filename/hash from the initial stream before the first cache write | **Adapted for Mew's route model:** `PlayerLaunch` carries initial filename/hash separately from P2P routing fields. Direct stream launch falls back from `stream.infoHash` to `stream.p2pInfoHash`; cached direct links and P2P launches carry their available metadata. Tests cover initial direct-source identity and client-resolve hash fallback without reclassifying a playable direct URL as torrent playback. Torrent launch arguments remain authoritative for P2P. Final-candidate Android CI is tracked by the linked Actions run; device verification remains pending. |
| Subtitle text-shadow control and ExoPlayer span preservation | Enhanced commits `63ce868064` and `111471f53a`; settings persistence and player rendering paths changed | **Ported in the current shadow implementation:** enable/strength controls, bounded strength normalization, profile-scoped Android/iOS persistence and settings sync, ExoPlayer cue spans that survive embedded-style stripping, Android libmpv shadow offset/background handling, and iOS libmpv shadow-offset bridging. Shadow blur strength now matches the newer Enhanced value (0.8x). Device rendering remains unverified. |
| Custom subtitle-font importer and bundled font resolution | Enhanced commit `b47732e3df` plus Android/iOS importer and resolver files | Mew has `SubtitleFontFileBridge` and platform font metadata helpers. **Safety parity port:** Android and iOS imports now enforce the upstream 30 MiB size ceiling while preserving Mew's isolated-directory/font-family behavior. Mew still accepts `.ttf`/`.otf` only; `.ttc`/`.otc`, bundled font resolution, import management and lifecycle parity remain separate review items. |
| Cinematic stream search/filter glass | Enhanced commits `13c87a32f7` and `d875e3f859` | **Intentionally deferred:** Mew's existing app backdrop is scoped to navigation; the stream list does not currently expose an equivalent backdrop/layering contract. Adding blur/material effects to a frequently updated list needs a separate architecture and performance/accessibility review. Existing filtering behavior remains unchanged. |

## Fresh upstream delta review (2026-10-23)

Comparison anchor: Enhanced `ae535226b1488a60d59613c1502fbb717cbb333f` → `a3e885b75356c454e1c51637a48b70b83afc2781` contains 19 commits. Nuvio Mobile `cmp-rewrite` remains at `e6c399e014939cd63dd85a3fc5d248c2bacccbe4`. This is a source/commit review, not a claim of complete parity.

| Upstream delta | Current Mew evidence / decision |
|---|---|
| `465ad56c44`, `ddb752a8c7`, `ef5a32f657` — profile-aware skip durations and seek wiring | **Present:** existing parity matrix and `PlayerSkipSecondsTest`; do not duplicate. |
| `1051981cd0`, `ffce85367f`, `595efc0e37`, `e08ca6db3a` — Android DoH, client routing, OkHttp migration and DNS picker | **Adapted and partially routed:** Mew already has `DnsOverHttpsSettings`, `DnsOverHttpsDns`, and `IPv4FirstDns`; a cached dynamic resolver now backs the shared OkHttp client. Selected DNS is wired into CloudStream's NiceHttp client, playback requests and subtitle-header/MIME probes, downloads, updater, subtitle cache, now-playing artwork, release notifications, YouTube trailer requests, and now AutoSync's Matroska/MP4 range-index loader (which derives its timeout-configured client from `SharedOkHttpClient.base`); addon requests already rebuild their client on provider changes. Do not copy Enhanced's differently shaped resolver blindly. `PlayerPlaybackNetworking.openConnection` still matches the upstream compatibility fallback and remains a separate review item because changing TLS/range semantics could break provider playback. |
| `42dbee2cbc`, `adaa098c0b` — per-profile in-app ratings switch; `ae535226b1` rating map | **Partial / missing delta:** Mew has rating artwork and episode rating code, but the inspected tree does not contain Enhanced's `UserRatingsRepository` or equivalent per-profile in-app rating switch. Port requires tracing rating providers and profile persistence; avoid coupling unrelated episode ratings. |
| `1a773931fa` — Hi10p / 10-bit H.264 software decoder | **Not ported intentionally:** upstream adds native C decoder/FFmpeg headers and renderer integration. This changes ABI, binary size, licensing obligations, CPU/memory load and thermal behavior. Require a license/notice audit and real-device performance evidence before considering it. |
| `2a78a1f28d` — transient iOS VideoToolbox failure handling | **Already present in Mew:** `hwdec-software-fallback=300` is configured in `iosApp/iosApp/Player/MPVPlayerBridge.swift`; no duplicate option is needed. iOS build and device verification remain outstanding. |
| `0ee95422ee`, `66a436a62a`, `35e2d28488` — episode navigator surface, seekbar padding and shadow tuning | **Selective visual port only:** preserve Mew's existing UI; check applicable common/Android rendering changes against current implementation and regression tests. |
| `b212edda42`, `28ea69390f`, `0ad834d3d9` — player gesture mapping and legacy toolbar/engine layout | **Needs compatibility review:** compare with Mew's current gestures, engine switch and controls; do not overwrite Mew controls wholesale. |
| `389255cb4e`, `ac592db4d1`, `8c9bbd1b4b`, `a3e885b753` — profile card, collapsed theme picker and Liquid Glass selected-tab visibility | **UI-only candidates:** keep existing Mew settings structure and navigation selector; assess contrast/accessibility and port only useful refinements after device rendering. |
| Nuvio Mobile PR [#2275](https://github.com/NuvioMedia/NuvioMobile/pull/2275) — libmpv add-on subtitle AutoSync | **Ported; automated validation passed.** Mew has the shared timeline loader, cue matching/retimer, profile-scoped preferences, cancellation, localized feedback, ExoPlayer sidecar timing, and libmpv subtitle-file replacement. Mew-specific sidecar/HTTP adapter mismatches are corrected; explicit user selection is independent of the startup preference. Host tests cover timeline matching and subtitle formats, and current-head Android Kotlin compilation passed in [run 39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760). Indexed HTTP(S) Matroska/MP4 references are supported; if no usable index is available, the original subtitle is retained. Real playback synchronization remains unverified until Phase 5 device testing. |
| Enhanced AutoSync V2 | **Not equivalent to libmpv AutoSync:** Enhanced's `EmbeddedSubtitleTimelineLoader` and coordinator target ExoPlayer; its docs explicitly exclude libmpv and external players. Treat it as a separate base implementation, not proof the Nuvio PR is already ported. |
| Subtitle cache log redaction | **Implemented with regression coverage:** current Mew redacts URL credentials, query and fragment, and omits exception stack traces that may contain signed URLs. Tests cover valid signed URLs and malformed input. Regression coverage includes configured double-tap seek durations, subtitle URL redaction, AutoSync cue formats, and timeline matching. Current HEAD `4514a6b3d33a40d663a3254f9e83c6d2823dfe17` passed host tests and Android Kotlin source compilation in [run 39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760). APK build, package-ID assertion, signing verification, and artifact upload remain intentionally deferred until the Phase 5 gate. |
| Discord RPC | **Not ported:** requires opt-in/privacy/lifecycle and license review before a decision. |

## Mandatory phases / completion gates

1. **Baseline audit:** pin upstream release tags and current branch heads; enumerate commits since tags and compare relevant Android/common source. Record source URLs and SHA.
2. **Parity matrix:** every relevant upstream feature/fix is marked **already present**, **port required**, **adaptation required**, **not Android-applicable**, **intentionally excluded with reason**, or **blocked**. No silent omissions.
3. **Implementation:** port in small coherent groups; retain Mew-specific CloudStream, TMDB-optional behavior, Live TV, DNS/networking, profile flows and player behavior. Add regression tests for every behavior change.
4. **Automated verification:** Android host tests, Android Kotlin source compilation, branding checks, and CI review on ordinary pushes. Build the Full Debug APK, assert application ID and verify signing only when the Phase 5 device-testing handoff is ready or the user explicitly requests a manual APK build. Recheck current upstream commits before closing the phase.
5. **Device-testing handoff (PIN USER):** only after automated checks are green and the APK artifact is available. Provide exact artifact URL, SHA-256 if available, install/update warning, test checklist, passed/failed/not-verified categories. Do not claim device verification without actual device evidence.

## Phase 5 handoff checkpoint — 2026-10-23

- The audited source head before this checkpoint was `113e51827c8b6b2231c9f057694caa23d6f82843`; current-head CI passed Android host tests and Android Kotlin source compilation. The PR remains draft and the APK was intentionally not built by routine CI.
- This checkpoint requests the dedicated Full Debug APK pipeline using the workflow's `[build-apk]` commit-message gate. The artifact is **not available or verified until that workflow run completes successfully**; do not install an artifact from an older SHA.
- APK gate must pass: host tests, Android source compilation, Mew branding check, Full Debug assembly, `com.mew.mobile.debug` application-ID assertion, signing certificate match, and artifact upload. APK generation is only for this Phase 5 handoff, not routine CI.
- Device-test order: install/update warning and launch; Settings → Layout → Navigation (all existing styles plus Liquid Glass); CloudStream repository install/update/reconciliation; verify installed provider count and progressive home scan; search/details/playback through CloudStream and addon paths; anime season/episode correctness; provider grouping; TMDB disabled; ExoPlayer/libmpv seek/zoom/orientation/PiP; external and embedded subtitles including AutoSync; profile/settings persistence; force-stop/reopen; record crashes, logcat, memory and frame drops.
- Device status remains **NOT VERIFIED** until evidence is collected on the Vivo V30 / Android 16. Package ID `com.mew.mobile.debug` differs from legacy package IDs; do not assume update compatibility or automatic data migration. Keep the PR draft and do not merge as part of the handoff.

## Known release blockers / unverified items

- Real device verification is outstanding: Vivo V30 / Android 16, CloudStream installation/reconciliation and 81-provider home scan, source playback/loading, correct anime season/episode resolution, provider grouping, TMDB-disabled operation, player gestures/orientation, subtitles, and package coexistence/data migration.
- No release-ready or full-parity claim is justified until the parity matrix is complete and the applicable automated checks pass. The current green CI verifies compilation/host tests only; it does not establish complete upstream parity or device correctness.
- Keep the PR a draft until Phase 5 readiness criteria are met; do not merge or change the default branch as part of this audit.


## Fresh upstream recheck — 2026-10-26

- **Nuvio Mobile** `cmp-rewrite` now points to `56ae37081afaff820b6073da84e711320238de36` (merge of `duo-half-open`, dated 2026-10-24), up from the previously recorded `e6c399e014939cd63dd85a3fc5d248c2bacccbe4`.
- **Nuvio Enhanced** `enhanced` now points to `a66716a4dc360559d8e2f570c70e7dc9e3fe7da6` (2026-10-25, player notch-safe padding), up from the previously recorded `a3e885b75356c454e1c51637a48b70b83afc2781`. The history now includes the `0.6.4-beta` release and additional work beyond the prior parity snapshot. A commit-count comparison against `ae535226b1488a60d59613c1502fbb717cbb333f` reports 118 commits; this is a review set, **not** 118 missing features, because it includes already-ported fixes, merge commits, translations, platform-specific changes and broad UI/native work.
- Newly reviewed areas include the Plezy-derived libass atlas renderer replacement (`97a32d1`), seekbar-thumbnail optimization (`7430519`), native software video decoder, configurable player buttons, player end-time UI, TMDB original-language titles, Discord iOS login/Keychain storage, and settings backup import/export. These are not wholesale-ported: the renderer/decoder changes alter native/runtime and licensing surfaces; Discord requires privacy/lifecycle review; UI changes must be adapted to Mew's existing screens and verified for Android.
- Enhanced's latest notch-safe padding fix targets its newer `PlayerToolbarLayout` / `PlayerTimelineDetails` structure, which is not present in Mew. It is therefore not a safe copy-paste port; reassess if/when the corresponding toolbar layout is integrated.
- **Selective reliability port made on Mew:** commit `bb6efa5717315257d56b2e5e6939a2fa50ca4fd4` adapts Enhanced's Trakt snapshot-preservation fix. Failed episode-history pages now invalidate the whole history refresh rather than publishing a partial Continue Watching snapshot; failed dropped-show pagination preserves the last complete hidden set; failed playback refresh clears the consumed activity fingerprint so a later poll retries. Added paging regression tests. CI for this current head is pending/not yet observed green; no runtime or device claim is made.
- Existing Mew policy remains: port compatible fixes selectively, preserve Mew's CloudStream/TMDB-optional/player/profile behavior, and defer high-risk native or broad UI changes until their dependencies, licenses, regression coverage and device behavior are reviewed.


## Phase 5 readiness recheck — 2026-10-26

- Candidate source head before the device-test build: `179d9a7cd264bc22da892dbb79dffd3755613579`.
- CI run [40229032370](https://github.com/somilkhan/MewMobile/actions/runs/40229032370) completed successfully on this head: Android host tests, Android Kotlin source compilation, and localized Mew branding validation passed. Routine APK packaging was correctly skipped.
- Run [40228756123](https://github.com/somilkhan/MewMobile/actions/runs/40228756123) was cancelled during Android host tests and is not counted as a pass.
- Rechecked upstream refs: Nuvio Mobile `cmp-rewrite` resolves to `56ae37081afaff820b6073da84e711320238de36`; Nuvio Enhanced `enhanced` resolves to `a66716a4dc360559d8e2f570c70e7dc9e3fe7da6`. Comparisons against those recorded SHAs are identical; no newer branch delta was exposed by this recheck.
- Latest source fix `179d9a7cd264bc22da892dbb79dffd3755613579` stops CloudStream bulk installation when the active profile changes, checking profile/generation both before each install and after the awaited result.
- **Phase 5 gate:** run the dedicated Full Debug APK build against this updated audit checkpoint; require host tests, Android source compilation, branding check, APK assembly, application ID `com.mew.mobile.debug`, signing-certificate verification, and artifact upload to pass before handing the artifact to the user. Physical-device verification remains the user's next step; do not merge PR #38.


## Continued checklist execution — 2026-10-26

- Rechecked the upstream branch deltas against the previous audit anchors. Nuvio Mobile `cmp-rewrite` is 69 commits ahead of `e6c399e014939cd63dd85a3fc5d248c2bacccbe4`, with 109 changed paths in the compare window. Nuvio Enhanced `enhanced` is 99 commits ahead of `a3e885b75356c454e1c51637a48b70b83afc2781`, with 183 changed paths. These are review windows, not counts of missing features; overlapping changes, merge commits, translations, native assets, and changes already present in Mew must be deduplicated.
- Candidate areas surfaced in the expanded compare include home-hero metadata rows and related tests, player control/layout refinements, libass renderer/binary replacement, libmpv AutoSync, seek-preview updates, settings backup/import-export, stream badges, TMDB language/title behavior, and additional Trakt paging work. Native renderer/decoder/binary changes and large UI transfers remain dependency/license/runtime-sensitive; they are not copied wholesale. Continue per-feature source/call-site review and record port/already-present/defer reasons.
- **Reliability fix added in this commit:** a failed or cancelled CloudStream auto-update check no longer leaves its six-hour in-process cooldown active. Previously the cooldown timestamp was written before repository refresh/update work, so a transient refresh or package failure could suppress another eligible check for six hours. The timestamp is now cleared on unsuccessful completion for the same profile; successful checks retain the cooldown. The cooldown decision is factored into a pure helper with regression coverage for recent checks, cleared timestamps, profile changes, elapsed cooldown, and clock rollback.
- The last full-debug workflow on `d88c0a810475130f16ddee9adf7f6d6522356b26` passed host tests, Android Kotlin source compilation, branding validation, APK assembly, package-ID verification, signing verification, and artifact upload (run [40239782624](https://github.com/somilkhan/MewMobile/actions/runs/40239782624)). That build was premature relative to completion of this checklist and is not the final Phase 5 candidate. Do not treat the artifact as the final device-test handoff.
- Current status remains: upstream review and the master checklist are in progress; physical-device verification is not established. PR #38 remains open/draft and must not be merged by this workflow.


## CloudStream package download size gate — 2026-10-26

- Source review found the .cs3 installer used `httpGetBytesWithHeaders` without overriding its default `Int.MAX_VALUE` response limit. The ZIP inspector already limited entry count and total uncompressed size, but that check happened only after the entire response had been buffered in memory.
- The installer now supplies a 50 MiB maximum response size to the platform HTTP implementation, which rejects an oversized declared Content-Length and enforces the cap while reading. The package inspector independently enforces the same maximum as defense in depth. Added unit coverage for the size validator boundary.
- This addresses the bounded-download portion of the plugin-safety checklist at source level. Runtime download/cancellation behavior and atomic replacement still require the automated workflow and device/build verification; no runtime claim is made.


## Profile-isolation race fix — 2026-10-26

- Review of the CloudStream install path found a race between checking the active profile and calling `savePackageAtomically`: Android package storage chooses its directory from a mutable active-profile field. A profile switch in that gap could route a downloaded package into the newly active profile, then fail the post-write guard while leaving the file behind.
- Added a shared profile-mutation lock around profile/state changes and the final package replacement + state persistence. The operation generation and profile guard are checked inside the same critical section. The lock also serializes AutoUpdates preference changes and local-state clearing against that commit path.
- This is a source-level concurrency fix; it still requires CI and regression/runtime verification. The broader checklist remains open.


## TMDB original-language title preference — selective upstream port

- Ported the upstream title-language preference into the existing TMDB settings page rather than creating a new screen. The preference is profile-scoped on Android/iOS, included in settings sync payloads, and defaults to the existing app-language behavior.
- TMDB enrichment and home-preview artwork cache keys now distinguish the title mode. When Original language is selected, titles prefer `original_title` / `original_name`, image requests are not restricted to the app language, and original-language artwork is preferred when available; the previous preferred-language selection remains the fallback. The preference is only enabled when TMDB enrichment is enabled and configured.
- Added unit coverage for title selection fallback and cache separation. Translation keys are present in the default resource file and all 23 locale-specific `strings.xml` files; a direct source audit checked both the title label and description keys in each locale.
- This is source-level implementation pending CI and platform verification. It does not alter the TMDB enable/disable contract or CloudStream fallback behavior.


- Follow-up for the TMDB title-language port: Home's localized-hero cache key and in-flight request key now include the title-language mode, and changing the setting invalidates/rebuilds the existing hero data through `HomeRepository` instead of leaving stale titles until a full catalog refresh. When original-title mode is selected while artwork enrichment is off, the preview path can still update the title/logo without replacing posters and backdrops against the user's artwork setting. Hidden Home state does not trigger a new metadata request during invalidation.


## Home/CloudStream timeout alignment

- The current Home caller wrapped each provider's MainPage request in a 5-second deadline while the Android CloudStream runtime's default per-provider discovery deadline was 15 seconds. This outer deadline could cancel valid providers before their own discovery budget expired, matching the LogFox timeout-mismatch concern.
- Introduced a shared 15-second discovery timeout constant used by the Android runtime and set Home's outer deadline to 20 seconds, leaving 5 seconds of orchestration headroom. Added a host test asserting the outer deadline exceeds the runtime deadline.
- This resolves the known 5s-vs-15s mismatch at source level. It does not by itself prove provider success, progressive UI rendering, or physical-device performance; those remain verification items.


## .cs3 content validation before replacement

- The previous package inspector verified ZIP structure, entry names, traversal, compressed download size, and central-directory uncompressed-size limits, but required only the *presence* of `manifest.json` and `classes.dex`. A malformed manifest or a fake/invalid DEX entry could therefore pass preflight and replace a working package.
- Android Full now streams and validates the ZIP local entries before atomic replacement: local/central-directory entry sets must match; duplicate entries are rejected; actual decompressed bytes are bounded; the manifest must parse and contain a nonblank `pluginClassName`; and `classes.dex` must have a valid DEX magic header. Android Play Store and iOS runtime stubs remain no-ops because those variants do not support Android DEX loading.
- Added common unit tests for valid, invalid, and truncated DEX headers. Full runtime/package loading and rollback behavior still require CI and device verification.


## Player orientation toggle lifecycle fix

- The Android player orientation effect previously restored the Activity orientation and reset the global portrait toggle on every disposal. An orientation request can itself recreate the Activity, so disposal during that configuration change could erase the user's toggle and force landscape again.
- The orientation preference now retains the pre-player requested orientation across configuration recreation and restores/resets it only when the Activity is actually leaving configuration change. Added a host unit test for preserving the original orientation across a simulated recreation.
- This addresses the identified lifecycle race at source level. Actual device rotation, multi-window, and OEM-specific behavior remain unverified and are reserved for the Phase 5 device pass.


## TMDB toggle / CloudStream fallback routing

- Source review found SearchRepository still searched installed CloudStream providers regardless of the TMDB toggle, and Discover always listed CloudStream catalogs when no regular add-ons were enabled. This contradicted the agreed invariant that CloudStream extensions are the fallback only while TMDB is disabled.
- Search and Discover now gate CloudStream provider use on `!TmdbSettingsRepository.snapshot().enabled`. The TMDB-enabled state is included in the search request key and SearchScreen effects observe the TMDB settings StateFlow, so toggling the option reruns search/discovery without restarting the app. Regular non-CloudStream add-ons remain available.
- Added a focused routing-policy unit test in this change batch. CI and runtime toggle behavior remain pending verification.


## CI compile diagnostics and correction — 2026-10-26

- Workflow run `40244559430` failed in `:composeApp:compileAndroidMain` with two concrete errors: the CloudStream runtime expect declaration had a one-argument `validatePackage` signature while all actual implementations and the caller used the inspection argument; and the new TMDB string accessors were referenced without importing their generated resource symbols.
- Corrected the expect signature to `validatePackage(bytes, inspection)` and added the two generated Compose resource imports. The 23 locale files were already present; missing imports, not absent translations, caused the unresolved references.
- The next current-head workflow is required to verify these corrections; the failed run is not counted as a pass.


## Add-on refresh job registry race — 2026-10-26

- `AddonRepository` runs refresh coroutines on `Dispatchers.Default`, but its per-manifest `activeRefreshJobs` map was accessed from caller threads and coroutine completion without synchronization.
- The deduplication check also used `Job.isActive`; a newly registered lazy coroutine is not active until `start()`, leaving a window where concurrent refresh requests for the same manifest could both start network requests and race version persistence.
- Added a dedicated registry lock around refresh reservation/removal and profile-switch/clear operations. The deduplication guard now treats any non-completed registered job—including a lazy job—as reserved. Cancellation occurs outside the lock to avoid running cancellation handlers while holding the registry monitor.
- Verification: current head before this change passed Android host tests and Android Kotlin source compilation in [run 40245881788](https://github.com/somilkhan/MewMobile/actions/runs/40245881788). The new concurrency change requires a fresh workflow; no result is claimed yet.


- Add-on refresh registry follow-up: the new reservation predicate is now covered by a host test that specifically models a lazy coroutine job (inactive but not completed) and a cancelled/completed job. This guards the original `isActive`-based deduplication hole; the synchronized registry implementation itself is validated by the current-head CI run.


## Additional upstream delta triage — 2026-10-26

- **Pinned Home collections:** ported the missing invariant from Enhanced's `b12f7d9aba`. Pinning had been enforced when catalogs/collections synchronized, but manual moves, reset-to-defaults, and remote settings import could leave pinned collections below ordinary catalogs. Enforcement now runs after both move APIs and after remote settings normalization. Pure ordering tests cover pinned-first behavior and preservation of relative order.
- **Home hero source changes:** Enhanced's `0546b01dee` was reviewed against Mew's `HomeRepository.publishCurrentState`. Mew already derives the visible hero directly from the currently selected hero-source preferences and cached sections; it does not retain the old visible hero while hero-source resolution is pending. The exact upstream fix is therefore already covered by Mew's current architecture and was not copied.
- **Player end-time overlay:** already present in Mew's `PlayerControls.kt` and profile-scoped `PlayerSettingsRepository`; no duplicate port needed.
- **Settings backup import/export:** already present in Mew's existing Nuvio Enhanced settings surface using `ProfileSettingsSync.exportBackupJson/importBackupJson` and the platform file bridge; no duplicate screen or backup implementation needed.
- **TMDB original-title localization:** checked both original-title label and description in all 23 locale-specific `strings.xml` files; all were present with translated text. The previous audit note claiming locale coverage was still outstanding is superseded.
- **Player toolbar customization:** ported from Enhanced commit `329a983044`, adapted to Mew's existing ten action pills (resize, speed, subtitles, subtitle sync, audio, quality, channels, sources, episodes, next episode). Mew's existing action order and all-visible behavior remain the default. The setting is profile-scoped on Android/iOS and included in settings sync/backup payloads. The existing Playback settings surface contains the reorder/show/hide dialog. Model tests cover default behavior, round-trip encoding, unknown/duplicate keys, missing entries and reordering. CI validation is pending for the current head.
- Latest completed green CI is [run 40245881788](https://github.com/somilkhan/MewMobile/actions/runs/40245881788) on pre-concurrency-fix head `c59c7f81724424b4b251cb21ce993dc0b2fcfffd`. The current post-fix candidate needs its own green host-test/source-compile run before its changes can be considered verified.


- Follow-up review also added the invariant at the end of `normalizePreferences()`, covering reset-to-defaults and any other normalization caller. Removed a duplicated no-op comparison in the ordering helper integration. The newest CI run must validate this follow-up before it is marked passed.


- Toolbar dialog review: bound the settings dialog to the repository StateFlow so reorder/show/hide edits recompose the dialog immediately instead of reading a one-time `.value` snapshot. This follow-up is included in the next CI candidate.


- Static resource review corrected the toolbar dialog's subtitle action label to use Mew's existing `compose_player_subs` resource key. The latest CI run was started before this correction, so a fresh run on the corrected head is required.


- **Toolbar settings compile review:** the reactive-state follow-up initially collected the whole `PlayerSettingsUiState` into a variable named `toolbarLayout`, but the dialog expects `PlayerToolbarLayout`. Corrected the binding to collect `playerSettings` and read `playerSettings.toolbarLayout`. A fresh CI run on this corrected head is required; earlier runs do not verify this correction.


- **Toolbar dialog compile failure diagnosis:** current-head CI exposed unresolved `DialogSurface`, `DialogButton`, `DialogButtonStyle`, and `DialogButtons` imports; those helpers are not part of Mew's UI library. Reworked the editor to use Mew's existing Material 3 `BasicAlertDialog`/`Surface`/`TextButton` pattern instead of importing nonexistent upstream dialog abstractions. The next current-head CI run must verify this correction.


## Stream badge filter cache — 2026-10-26

- Adapted Nuvio's `a0735005d3` optimization: compiled badge regex filters are cached against the immutable `StreamBadgeRules` value and rebuilt only when rules change. The cache uses atomicfu synchronization because this common code runs on Android and iOS. Added regression coverage for repeated use, changed rules, empty rules, and restoring earlier rules. Current-head CI must verify the port.


## Debrid stream metadata performance port — 2026-10-26

- Adapted Nuvio's `def203a96a`: stream facts computed for filter/sort/limit are reused by formatting instead of recalculated. Frequently used metadata regexes are reused, and token regexes are cached behind an atomicfu lock suitable for shared Android/iOS code. Added regression coverage for filtered and formatted output equivalence.
- Reviewed Nuvio Mobile's new half-open Duo/fold controls from the `duo-half-open` merge. Deferred: this is a new foldable-specific playback mode with video/control splitting, idle dimming, gestures, and platform-specific brightness/volume behavior, not a small compatibility fix. Mew's existing player layout remains intact until the feature can be tested on actual foldable hardware.
- The debrid performance port is source-level only until current-head CI passes.


- **Debrid cache source-review correction:** follow-up review found malformed generated regex declarations/interpolation in the first draft of the cache port. Corrected the static regex declarations and Kotlin interpolation before accepting CI evidence; the next run must verify the corrected head.


## Current-head verification and iOS subtitle resize adaptation — 2026-10-26

- Android CI run [40250131692](https://github.com/somilkhan/MewMobile/actions/runs/40250131692) passed on source head `9cd4e0ea2a58bd6b945adeb033b964387959692c`: Android host tests, Android Kotlin compilation, and Mew localized-resource branding validation all succeeded. APK assembly, package-ID assertion, signing verification, and artifact upload were skipped as designed before Phase 5.
- This green run validates the Mew-adapted player toolbar layout, the Mew Material 3 toolbar editor, pinned-collection ordering invariants, cached stream-badge filters, and debrid metadata/fact reuse. It does not establish iOS compilation or runtime verification.
- Adapted Nuvio Mobile's iOS player resize fix `d995e6834a`: when the drawable size changes, Mew now refreshes mpv's ASS subtitle events after committing the Metal layer layout, preventing stale subtitle overlays when a paused video is resized. The upstream's Picture-in-Picture display-layer assignment was not copied because the inspected Mew `MPVPlayerBridge.swift` has no corresponding PiP controller/API; this port is limited to the shared renderer behavior.
- **Unverified:** the iOS Swift change has not been built or device-tested. The current Android workflow cannot validate Swift code; iOS compile/runtime verification remains outstanding.


## Continue Watching dismissal reset — 2026-10-26

- Ported Enhanced commit `6fefbcdab4`'s watched-action behavior into Mew's existing `WatchedRepository`: marking an episode watched clears dismissed Next Up entries for its parent content ID.
- Fixed the associated key filter to remove both a content-level key equal to the ID and episode-specific keys with the `contentId|` prefix. This prevents stale dismissals from surviving a watched action while keeping similarly prefixed IDs (for example `show-1` versus `show-10`) isolated.
- Added pure regression tests for exact-key removal, episode-key removal, unrelated-ID preservation, and blank IDs. Current-head CI must verify this new batch; no result is claimed yet.
- Review note: Enhanced's iOS Now Playing publication-throttling implementation (`99d0e36e9a`) is already present in Mew's `NowPlayingController.swift` (published-state/time tracking and 1.5-second drift threshold), so no duplicate port was made.


## Simkl multi-season Continue Watching identity — 2026-10-26

- Adapted the Enhanced anime multi-season dedupe fix to Mew's existing `TrackingProgressProvider` abstraction. The provider-neutral hook defaults to identity, while Simkl resolves catalog IDs and anime video IDs through its existing `SimklSyncSnapshot` and respects the configured anime ID preference.
- Home completed-episode seed candidates now deduplicate by canonical identity after ranking candidates by latest activity. Cached/live Next Up items are also filtered when their next video belongs to a different currently represented canonical series, avoiding two franchise cards claiming the same upcoming episode.
- Simkl progress parent normalization now uses the episode video ID as a fallback when the parent catalog ID cannot be resolved from the current snapshot.
- Added focused tests for MAL episode video ID resolution, alias deduplication retaining the latest activity, and removing a Next Up item claimed by another active series.
- **Intentionally not ported:** Enhanced also compares TVDB IDs to detect series aliases; Mew's current `MetaDetails` model does not expose `tvdbId`, and adding a new metadata contract across all sources is a separate architecture change. The canonical Simkl IDs and video-ID mapping are the supported partial port.
- CI is required on the new head; the earlier green run does not validate this commit.


## iOS system-volume gesture feedback — 2026-10-26

- Adapted Nuvio Mobile commit `f32dce5f31`: `IOSPlayerGestureController.currentVolume()` now reads `AVAudioSession.sharedInstance().outputVolume` rather than inferring system volume from the hidden `MPVolumeView` slider. The `MPVolumeView` remains in place for the existing `setVolume()` path.
- This makes gesture feedback report the system's actual output level even when the private slider view has not been populated. The upstream Kotlin/Native imports and API usage were reviewed; **iOS compilation and device behavior are still unverified** because the available CI workflow is Android-only.
- **Upstream notch-safe padding assessment:** Enhanced's latest player padding change was not copied line-for-line. Mew's `PlayerControlsShell` already wraps `PlayerHeader` and the progress controls in a container padded by `horizontalSafePadding`, and the header also applies top `WindowInsets.safeContent`. The upstream patch targets a different nesting structure, so this specific padding delta appears covered by Mew's existing layout. Physical-device notch/cutout behavior remains a Phase 5 check.
- Current Android CI for head `180afd691a018b871be8ff2d5ccb9eb4173c6a8a` was running when this note was written. The new Swift/Kotlin-Native source change requires separate iOS build/runtime verification and is not covered by Android CI.


## TMDB original-language home hero title selection — 2026-10-26

- Adapted Enhanced commit `4f5eebc17e` to Mew's existing home-hero rendering path. In Mew, `HomeRepository` already resolves the selected TMDB title/logo into each `MetaPreview`, while the hero separately fetches base detail metadata.
- The hero previously preferred `detailMeta.name/logo` unconditionally, which could mask the selected original-language title/logo. It now prefers the TMDB-selected item title/logo only when TMDB is enabled and title language is `ORIGINAL`; the existing app-language preference still prioritizes detail metadata. Empty values retain the existing fallback behavior.
- Added common tests for both title modes, blank-value fallback, and matching logo selection. Validation is required on the new head; this is a source-level port until CI completes.


## Add-on profile-session generation follow-up — 2026-10-26

- A second source review found that refresh jobs checked only the numeric profile ID after network completion. Switching away from profile 1 and back to profile 1 could therefore make stale work appear current again even though the profile session had changed.
- Refresh jobs now retain the profile generation token through manifest-version persistence and UI application. Version writes and state updates reject stale generations; host tests cover generation mismatch even when the profile ID matches again.
- CI concurrency was also tightened so push and pull-request events for the same branch/build mode share one concurrency group instead of running duplicate Gradle host-test jobs. This does not skip validation; it prevents redundant runs.
- This follow-up requires a fresh workflow result. No CI pass is claimed here.


- **CI failure diagnosis on `b3cb493e29328defac7a33182668e7fcc76b56a9`:** Kotlin compilation rejected a suspending `getString` call inside the new synchronized profile-state critical section (`AddonRepository.kt:462`). The localized fallback message is now resolved before acquiring the lock; the next CI run must verify the correction.


## Add-on server-pull profile race follow-up — 2026-10-26

- Review found that a profile could switch after the server response's initial generation check but before the pull applied state. The previous path could then persist the old response under the new current profile and could read that profile's enabled-state map while migrating the old profile.
- Server-pull state application now rechecks the profile ID and generation while holding the repository lock, writes storage using the captured request profile ID, and starts manifest refreshes with that same generation. Local enabled-state reads are profile-explicit. A stale response is discarded rather than applied to the newly active profile.
- Fresh Android host tests and Kotlin source compilation are required to validate this fix; iOS/device behavior remains separate verification.


## Add-on mutation/server-push snapshot hardening — 2026-10-26

- The profile-session review also found that `addAddon` checked only the numeric profile ID after its network fetch, and generic server pushes read the profile ID and UI list at separate times. Rapid profile switching could therefore apply a stale add result or pair one profile's ID with another profile's rows.
- Add-on creation now captures and validates the profile generation, atomically applies/persists to the captured profile, and pushes a fixed snapshot. Generic pushes capture profile ID and add-on rows together under the repository lock. Validation is pending on the current head.


## Current-head verification after add-on profile-session fixes — 2026-10-26

- Source head `711b6846a311c2953b6b5e5de45199a911468f4a` passed GitHub Actions run [40254947274](https://github.com/somilkhan/MewMobile/actions/runs/40254947274): localized Mew branding check, Android host tests, and Android Kotlin source compilation all succeeded.
- Full Debug APK assembly, package-ID assertion, signing verification, and artifact upload were skipped as intended; Phase 5 has not been reached and no APK was generated by routine CI.
- The run includes the profile-session generation fix, stale server-pull rejection, profile-explicit persistence, and fixed-snapshot add-on pushes. It also verifies the preceding player-toolbar, TMDB title, playback, stream-badge, debrid metadata, Trakt, and iOS source adaptations only to the extent they are included in the Android host-test/source-compile tasks. iOS/Xcode and physical-device behavior remain unverified.
- PR #38 remains open, draft, and unmerged. This parity-audit update is documentation-only; the resulting new head still requires its own ordinary CI run.


## Shared-primary profile context guard — 2026-10-26

- A source review found a subtle case not covered by effective profile ID alone: switching from profile 1 to a secondary profile that reuses primary add-ons leaves the effective add-on profile ID at 1. An in-flight add-on mutation could therefore pass a numeric profile/generation check even though the active profile had changed.
- The repository now tracks the actual active profile ID separately from the effective add-on profile ID, invalidates sessions on either change, and checks the active-profile context before accepting async work. Explicit pushes retain the already-validated profile snapshot instead of being suppressed by a later profile switch. Added a regression test for the same-effective-profile case.
- Last verified head `711b6846a311c2953b6b5e5de45199a911468f4a` passed Android host tests and Android Kotlin compilation in run [40254947274](https://github.com/somilkhan/MewMobile/actions/runs/40254947274). The shared-primary context fix on this new head requires fresh CI validation.


## Initialization snapshot race guard — 2026-10-26

- A final concurrency review found a narrow interleaving where `initialize()` could read the old active profile, lose the lock to `onProfileChanged()`, then overwrite the new session using its stale snapshot. Initialization now compares the captured active profile against the live active profile again while holding the repository lock before changing session state. Fresh CI on this head is required.


## Upstream parity decision closure — 2026-10-26

- The remaining reviewed upstream deltas are now explicitly categorized as already present in Mew, adapted to Mew's existing architecture, or intentionally deferred with the concrete risk/reason recorded in the parity matrix. The native libass renderer, Discord Rich Presence/token storage, cinematic stream-filter glass, player audio-language badge surface, and engine-specific buffering controls are not silently treated as completed.
- This does not mark iOS/Xcode compilation or physical-device behavior as verified. The current head must pass its own Android host-test and Kotlin-source-compile workflow before the Phase 5 APK build is triggered.

## Phase 5 gate correction — 2026-10-27

- **Correction:** the Full Debug APK workflow was triggered before the full six-page Master Engineering Checklist was reconciled. A green routine Android CI run alone does not satisfy the Phase 5 gate. Do not treat this section as a declaration that Phase 5 is ready.
- PR #38 is open, draft, and unmerged. The current PR head at the time of review was `45f7c27713ecaf91e55770d7cb34708612a3ef4d`; the PR contains 242 changed files, so complete-diff review is still required.
- The prior verified routine CI run [40256313048](https://github.com/somilkhan/MewMobile/actions/runs/40256313048) passed Mew branding validation, Android host tests, and Android Kotlin source compilation on its then-current head `908b81f25771a49692e87463ddfc5faf9289f184`. It did not verify the newer commit or package an APK.
- APK build runs [40479343108](https://github.com/somilkhan/MewMobile/actions/runs/40479343108) and [40479349952](https://github.com/somilkhan/MewMobile/actions/runs/40479349952) were both observed in progress on commit `45f7c27713ecaf91e55770d7cb34708612a3ef4d`. They are separate push and pull-request workflow runs; at last observation, host tests were still running, APK build/package-ID/signing/artifact upload had not completed, and no artifact was available.
- Before calling Phase 5 ready, reconcile the master checklist: complete PR diff; current Nuvio Mobile and Nuvio Enhanced heads; package ID across build/manifest/CI; TMDB and CloudStream routing; LogFox provider timeout/partial-result/cancellation findings; player orientation/subtitles/seekbar; AutoSync; plugin update/install safety; settings discoverability/persistence; iOS source/build verification where available; and a per-item evidence ledger.
- Physical-device verification is pending. Keep PR #38 open and draft. Do not label the app device-verified without actual device evidence.

## Fresh upstream recheck — 2026-10-27 (Phase 5 gate remains blocked)

- Live Nuvio Mobile `NuvioMedia/NuvioMobile:cmp-rewrite` is at `61658f366f985fc7758f0942a14c1754c69aca68`, commit message `chore(store): publish 0.6.5-beta` (2026-10-27). Compared with the previously recorded SHA `56ae37081afaff820b6073da84e711320238de36`, GitHub reports 22 commits and 59 changed paths.
- Live Nuvio Enhanced `luqmanfadlli/NuvioMobile-Enhanced:enhanced` is at `313ca3d15c3a31d1d1b456fbbb09d6c31711b8b2`, also publishing `0.6.5-beta` (2026-10-27). Compared with the previously recorded SHA `a66716a4dc360559d8e2f570c70e7dc9e3fe7da6`, GitHub reports 38 commits and 71 changed paths.
- The live `sync/upstream` branch is 146 commits ahead of Mew's `enhanced` base and 0 behind by commit ancestry. This ancestry count does **not** establish source parity with either upstream: the upstream histories are distinct and the PR has 242 changed files.
- Newly surfaced candidate areas include Matroska chapter parsing/loading and player chapter navigation; foldable-device controls/layout; server-library matching and watched-state synchronization; debrid stream presentation; playback/settings and TMDB metadata adjustments; and Croatian/Bulgarian plus other translation updates. Several of these source files do not exist at the same paths in Mew, so assess architecture and existing equivalent behavior before porting rather than copying files directly.
- Initial source inspection confirms Enhanced's Matroska chapter work spans a byte-level EBML parser, bounded HTTP range loader, chapter normalization/panel/runtime integration, settings and tests. It is a multi-file player feature, not a safe one-file cherry-pick. The upstream server-library code also has new matching/index/watched-state files; its dependencies and overlap with Mew's current server integration require a separate call-site review.
- **Open actions:** compare these new deltas commit-by-commit; map each feature to Mew's existing implementation; record `port / already present / adapted / not applicable / defer / blocked` with source and test evidence; run current-head CI after any source changes; verify the package ID across Android configuration and workflow; reconcile the LogFox and plugin-update checklists. Phase 5 remains **NOT READY** until this review and the final candidate build checks are complete.


## Fresh upstream recheck — 2026-10-30

- Live Nuvio Mobile `cmp-rewrite` HEAD: `53869a0279c59abdef23da2962eb2a1c80e6caa5`. GitHub compare against the previously recorded `61658f366f985fc7758f0942a14c1754c69aca68` reports 41 commits ahead. Newly relevant areas include TV-style automatic playback-engine selection/failover with track preference preservation, offline playback reliability, and the ASS render-mode/video-effect renderer plus a modified native AAR. These are not safe blind cherry-picks: engine switching spans common/Android player state and preferences; the renderer adds native binary/runtime and distribution obligations.
- Live Nuvio Enhanced `enhanced` HEAD: `58d1edb895c37bc638f87fe137f3fd714b6a9a14`. GitHub compare against `313ca3d15c3a31d1d1b456fbbb09d6c31711b8b2` reports 17 commits ahead. Newly relevant areas include locked-player touch handling, subtitle font-size increments, skip-segment selection/auto-skip, iOS subtitle RTL punctuation and hardware-decoder recovery, configurable legacy-player buttons, and a new PublicMetaDB tracking/scrobbling subsystem.
- **Adapted in this candidate (CI pending):** Enhanced `0907a3a6cd9621d98c81c0d20ea938bdfe017220` — preserve locked-state touch propagation so small finger drift does not cancel surface taps or the controls-unlock button. Added Android host regression tests for both cases. Enhanced `d80174d6d65b03506b32b729418ef4aa0c3fbf4e` — change subtitle font-size stepper increments from 2sp to 1sp while preserving Mew's existing 12–40sp bounds.
- **Deferred with reason:** PublicMetaDB is a large new tracker/authentication/library-sync feature with account, credential, data-model, and privacy implications; it needs a separate product/security integration. Nuvio's native ASS render-mode/AAR change needs binary licensing/distribution review and engine-level playback tests. Nuvio's automatic engine failover/track carry-over is a multi-file playback state migration requiring a dedicated regression pass. Enhanced's skip-segment overlap/auto-skip and resolved-HLS/P2P controller matching are not considered present merely because Mew has a Skip Intro button; they need a separate source/call-site/test port. The new iOS subtitle punctuation and Notification Center decoder recovery require an iOS/Xcode verification route that the Android-only routine workflow does not provide.
- **Discovery requirement source check:** the candidate now retains the TMDB switch in the existing Discovery settings surface, disables the CloudStream entry while TMDB is enabled, gates Home provider selection and the full-build provider runtime, rejects provider results if TMDB is enabled during a request, and refreshes Home when the setting changes. Common policy tests cover both boolean modes. This is implemented in source but remains unverified until the current-head CI passes and the device test confirms toggle/restart behavior.
- The prior Mew head `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4` passed routine CI run [40480042875](https://github.com/somilkhan/MewMobile/actions/runs/40480042875): Android host tests, Kotlin source compilation, and localized branding validation passed; APK packaging, package-ID assertion, signing verification, and artifact upload were correctly skipped. The source changes in this follow-up require a new routine CI run on the resulting commit. No APK has been produced for this candidate.


## TMDB / CloudStream mutual-exclusion implementation — candidate follow-up

- The existing Discovery settings surface retains the TMDB switch; the CloudStream entry is disabled while TMDB is enabled and re-enabled when TMDB is disabled. No duplicate settings page was introduced.
- Added a shared policy helper and applied it to Home's CloudStream provider rows and the full-build CloudStream provider runtime. Main-page, search, load and link-resolution paths are gated; cached load/link results are not returned while TMDB mode is enabled. Provider results are rejected if TMDB becomes enabled during an in-flight provider request.
- TMDB enable/disable and API-key removal now trigger a Home refresh so stale CloudStream rows/hero items are cleared and the selected discovery mode takes effect without a forced refetch of all add-on catalogs.
- Added common unit tests for both sides of the policy. This candidate is not accepted until the preceding gesture/subtitle candidate's CI result is known and the combined commit passes routine CI.


## CI failure evidence and diagnostic improvement — 2026-10-30

- Current candidate run [40935809526](https://github.com/somilkhan/MewMobile/actions/runs/40935809526) completed the host test task in 7m03s, with 1,099 tests completed and two failures: `PlayerSurfaceGesturesTest > lockedSurfaceTapSurvivesSmallFingerDrift` and `PlayerSurfaceGesturesTest > lockedStateDoesNotCancelTheUnlockButtonTap`. Both summaries report `java.lang.RuntimeException at RoboMonitoringInstrumentation.java:102`.
- The console log does not contain the nested exception stack or XML report. The current test source already has the correct `androidx.compose.ui.test.junit4.createComposeRule` import and `@Config(sdk = [34])`; do not repeat either change or claim a cause without the report.
- Workflow correction: `.github/workflows/mew-cloudstream-build.yml` now uploads the Android host-test HTML/XML reports only when the host-test step fails. This is a short-retention diagnostic artifact, not an APK artifact, and does not upload on successful routine CI. Use it to identify the actual runtime exception before changing the test or dependencies.


## Confirmed Robolectric manifest root cause — 2026-10-30

- Failure-only report artifact for run [40937363802](https://github.com/somilkhan/MewMobile/actions/runs/40937363802), XML `TEST-com.nuvio.app.features.player.PlayerSurfaceGesturesTest.xml`, gives the exact failure: `Unable to resolve activity for Intent ... cmp=org.robolectric.default/androidx.activity.ComponentActivity`. Both gesture tests fail before executing their assertions. The stack shows `ActivityScenarioRule` trying to launch Compose's `ComponentActivity`; Robolectric has no manifest declaring it.
- The `ui-test-manifest` dependency was declared only in the `androidHostTest` source-set classpath, which does not merge its manifest into the host test's Robolectric manifest. The correction removes that ineffective declaration, adds an isolated `src/androidHostTest/AndroidManifest.xml` declaring `androidx.activity.ComponentActivity`, and points the test's `@Config` to that manifest. It does not add a launcher activity to the production app manifest.
- The report artifact and XML were inspected directly; this is the confirmed cause, not a guess. New CI must verify the test-only manifest fix.


## Checklist disposition — current source inspection

- **CloudStream update safety:** existing `CloudStreamAutoUpdatePolicyTest` and repository guards cover update cooldown, failed refresh, and plugin state/generation changes. Preserve these controls; current-head CI still needs to pass after changes.
- **AutoSync:** Mew has a dedicated subtitle cue parser, AutoSync UI/runtime actions, and tests. Preserve this Mew-specific implementation and port only compatible offline-fetch guards.
- **LogFox:** repository tree/code search found no LogFox module, symbol, or integration. External LogFox behavior remains **NOT VERIFIED / NOT FOUND IN REPO**.
- **Server watched-state sync:** Mew has library sync/reconciliation but no `features/servers` source tree and no `customServerConnectionsEnabled` feature-policy flag. Upstream `ServerWatched` depends on a broader server catalog/matcher/capability/user-state subsystem. Defer that full feature group until product scope and dependencies are reviewed.
- **Enhanced PublicMetaDB:** deferred separately because it adds account/auth/tracking/sync/data-model behavior and needs a privacy/data-retention review.


## Selective player reliability ports — 2026-10-30

- **Nuvio Mobile Dolby decoder workaround adapted:** the Android ExoPlayer renderer now filters Dolby-named platform decoders for AC-3, E-AC-3 and E-AC-3 JOC only when extension renderers are enabled, allowing the bundled extension decoder to handle affected streams while preserving the platform decoder if the user disables extensions. Added Android host policy tests. This is source-implemented; candidate CI is pending.
- **Enhanced overlapping skip intervals adapted:** Mew now chooses the active skip interval with the furthest safe seek target rather than the first active interval, and rejects malformed/non-finite ranges and overflow seek targets. Added common regression tests. Candidate CI is pending.
- **Robolectric manifest correction:** the first classpath correction fixed the missing-resource error but did not finish the test setup. Run [40952751433](https://github.com/somilkhan/MewMobile/actions/runs/40952751433), artifact [12931684095](https://github.com/somilkhan/MewMobile/actions/runs/40952751433), showed both gesture tests failed before assertions because the manifest package `com.nuvio.app.hosttest` did not match Robolectric's default `org.robolectric.default` launch package. The current candidate explicitly sets `@Config(packageName = "com.nuvio.app.hosttest")` to align the test application context with the manifest. This fix remains CI-pending.
- **Latest Enhanced delta:** `781157e0033db40361b662525b06126e184ffd1c` is six commits ahead of the previously recorded `58d1edb895c37bc638f87fe137f3fd714b6a9a14`, adding MPV night-mode UI/audio behavior across Android, common player/settings and iOS bridge. Defer this feature group until Mew's Android/iOS engine behavior, persistence and controls can be verified together; do not cherry-pick it wholesale.
- Current candidate automated verification must be checked on the exact branch head. The prior host-test run was still in progress when this follow-up was prepared; it is not evidence of a pass. Routine CI remains test/compile focused, and APK packaging/upload stays gated to Phase 5.


## Robolectric package-alignment follow-up — 2026-10-30

- Failure artifact `android-host-test-reports-40952751433` (artifact ID `12931684095`) confirms the classpath manifest is found now; the failure changed from resource lookup to an activity-component package mismatch. Both `PlayerSurfaceGesturesTest` cases fail before gesture assertions because the launch component resolves as `org.robolectric.default/androidx.activity.ComponentActivity`, while the test manifest declares package `com.nuvio.app.hosttest`.
- The follow-up sets `packageName = "com.nuvio.app.hosttest"` in the test's `@Config`. The old `src/androidHostTest/AndroidManifest.xml` file is deleted; the only test manifest is `src/androidHostTest/resources/AndroidManifest.xml`.
- The combined candidate also adds the Dolby decoder policy and overlapping skip interval selection ports described above. Run CI against the exact new head; do not count the previous run as a pass.


## Robolectric config and cross-platform Mew identity correction — 2026-10-30

- CI on the prior candidate rejected `@Config(packageName = ...)` at Kotlin compilation (`No parameter with name 'packageName' found`). That attempted annotation override is not a valid fix. The current candidate instead aligns the isolated host-test manifest package with the observed Robolectric launch package, `org.robolectric.default`; only fresh host-test results can confirm the runtime fix.
- Android app IDs remain `com.mew.mobile` (release) and `com.mew.mobile.debug` (debug). iOS bundle IDs have been aligned to `com.mew.mobile` and `com.mew.mobile.DownloadsWidgetExtension`; the URL type name is Mew-owned while legacy `nuvioenhanced` and `nuvio` URL schemes remain registered to avoid silently breaking existing callback links.
- **Not verified:** no Xcode/iOS build or physical device test has run for this identity change. Confirm signing/provisioning, authentication callbacks, widget embedding, and install/update behavior before claiming cross-platform readiness. Bundle-ID changes are install/update boundaries and do not imply automatic app-data migration.


## Nuvio Mobile follow-up audit — 2026-11-02

- Fresh compare: Mew's recorded Nuvio Mobile baseline `38295004e4d4002f5520fe4120230253b64af63d` to upstream `46febb568348aa5e135bff0d4d5c914933aff5a0` is 38 commits ahead, 0 behind, with 32 changed files. Most are localization; the player-relevant code changes were reviewed separately rather than merged as a branch.
- **Selective port — skip segment continuity:** upstream normalizes provider aliases into opening/ending/recap categories so a replacement interval from another provider does not reset the skip button's dismissed or auto-hide state. Mew now shares its existing category mapping with the button/runtime, preserves dismissal across same-category replacements, and retains a same-category replacement starting within two seconds. Common regression coverage was added for provider aliases.
- **Selective port — offline-aware player lookups:** Mew already owns and starts `NetworkStatusRepository`. Player add-on subtitle lookup now waits while offline without marking the key fetched, and retries when network state changes; local files with existing external subtitles skip redundant add-on lookup. Remote skip interval resolution is network-gated and bounded to four seconds so slow providers cannot hold the player effect indefinitely.
- **Reviewed / not ported wholesale:** upstream's larger player-runtime reorganization, automatic engine/failover changes, metadata/shuffle/party additions, and unrelated localization changes are not copied into Mew. Mew's existing runtime, stream identity, subtitle AutoSync, settings and engine behavior remain the integration boundary. Each additional feature requires a separate compatibility review.
- Nuvio Enhanced `enhanced` remains identical to recorded baseline `93893a444ba68876d1ac30790d235ca4e7ddb64c` in the current compare.
- Candidate CI before this follow-up: run [41226201898](https://github.com/somilkhan/MewMobile/actions/runs/41226201898) passed host tests, Android Kotlin source compilation and branding validation for the prior head `c69191690e539c775ed1a8a2fb167df12c723a69`. The new follow-up commit requires a fresh exact-head CI run. APK/package-ID/signing and device gates remain Phase 5 only.


## Local media and sidecar follow-up — 2026-11-02

- Ported the upstream sidecar cue cursor into Mew's generation-safe controller. Sorted timelines advance incrementally, backward seeks rewind the cursor, cue identity signatures avoid repeated cue-text hashing, and unsorted timelines use a correctness-first full-scan fallback. Regression tests cover unchanged active cues, seek rewind, and unsorted input.
- Ported local container-header detection for `file://` and `content://` media, with 4 KiB reads, a 64-entry MIME cache, extension fallback, and host tests for Matroska, MP4, MPEG-TS, AVI, FLV and OGG. Android application context is exposed only by the Android actual storage object.
- Deliberately did not retain upstream `file://` subtitle copying: add-on subtitle URLs are remote, untrusted input, and copying arbitrary local paths into a FileProvider cache could expose app-private files to an external player. Mew now accepts only absolute HTTP(S) add-on subtitle URLs at both repository parsing and cache boundaries; host tests cover allowed and rejected schemes.
- Ported generic episode-title localization ("Episode N") into Mew's existing i18n helper and the details, watched-action-sheet and stream-title surfaces. No navigation or settings UI was replaced.
- Started network monitoring from Home and player entry, with foreground refresh on Home visibility. Player subtitle/skip requests key off the offline boolean to avoid restarting on Unknown → Checking → Online transitions; parental-guide lookup waits for Online.
- Nuvio Enhanced baseline `93893a444ba68876d1ac30790d235ca4e7ddb64c` independently contains the sidecar cursor, local MIME sniffing and local subtitle-file handling; these ports preserve Mew's existing generation and network-client customizations.
- Exact-head CI for this follow-up is pending; APK packaging remains deferred to Phase 5.


## Offline-state readiness correction — 2026-11-02

- Player network-dependent lookups now wait for `NetworkCondition.Online`, rather than treating the initial `Unknown`/probe `Checking` states as online. A single boolean key avoids duplicate lookups during Unknown → Checking → Online while still cancelling offline work and retrying when connectivity returns.
- Network monitoring is explicitly started from Home and player entry; Home visibility requests the repository's delayed foreground confirmation.
- The previous exact-head CI failure was limited to missing Android/IO imports in local MIME sniffing; those imports are now present. The next exact-head run must verify the corrected source and the new host tests.


## Additional upstream localization and security follow-up — 2026-11-02

- Completed generic episode-title localization across the player episode panel and pause overlay, in addition to detail, watched-action and stream surfaces.
- Added upstream AutoSync settings/toast translations for Arabic, French and Turkish. The translation keys match Mew's existing default AutoSync resources; unrelated MDbList, party and server bundles were intentionally excluded because those product surfaces are outside this integration scope.
- Applied the upstream Arabic fallback-label correction from "الحلقة" to "حلقة".
- Player skip-interval lookups now wait for confirmed `NetworkCondition.Online`, avoiding premature "fetched" state during Unknown/Checking network probes.
- Security review rejected blind upstream `file://` subtitle copying. Add-on subtitle URLs are untrusted and forwarded to external players; Mew now restricts them to absolute HTTP(S) at both parsing and cache boundaries.
- Exact-head CI for this follow-up is pending; APK packaging remains deferred to Phase 5.


## Android player I/O and subtitle safety finalization — 2026-11-02

- Local file/content URI MIME sniffing now runs only on `Dispatchers.IO`; synchronous media-item construction no longer uses `runBlocking`. Player preparation waits for the resolved MIME type and preserves the existing stream-type/response-header/path precedence.
- Remote add-on subtitle MIME probes no longer run while the initial MediaItem is built on the UI thread. Known extensions skip probing; unknown-extension HTTP(S) sources are probed on IO with a four-request concurrency limit. Retry and explicit subtitle-selection paths also execute probes off the UI thread.
- The ExoPlayer media-item reload helper is suspendable; clear-subtitle callbacks now launch the reload coroutine rather than calling a suspend operation from a non-suspending callback.
- Added host tests for local URI classification and subtitle-probe eligibility. Existing tests also cover local container headers and HTTP(S)-only add-on subtitle URL policy.
- Exact-head routine CI passed on `b87212b85be9f0c61655f2c53f0563fda386b5d8`: [run 41349553940](https://github.com/somilkhan/MewMobile/actions/runs/41349553940). Host tests, Android Kotlin source compilation and Mew branding validation passed.
- APK assembly, application-ID assertion, signing verification and artifact upload remain reserved for the Phase 5 candidate-build gate.


## 2026-11-02 physical-device results and Watch Party disposition

### Device evidence
- Candidate `4d838a356a658bf21c9eab0d63c83124d9ba5dd4` was physically tested by the maintainer. See [DEVICE_TEST_REPORT_2026-11-02.md](DEVICE_TEST_REPORT_2026-11-02.md) for the exact APK identity, user-reported pass/fail matrix, sanitized Logcat findings and evidence limits.
- The candidate is not accepted: seek/timestamp reset, subtitles, AutoSync behavior, purple output, rotation, Raw Catalog grouping, in-player engine switching, Liquid Glass alternate layout, Watch Party and labels remain unresolved.
- Preserve previously ported Mew implementations. When a requested feature exists but fails, record `ALREADY PRESENT — REPAIR`; do not copy a second upstream implementation over it.

### Watch Party — reuse official Nuvio backend
- **Disposition: PORT (planned, not yet implemented), backend reuse approved.** Do not create a separate Mew-specific Watch Party backend if the official Nuvio backend capability is available.
- Nuvio Mobile `cmp-rewrite` head reviewed: `46febb568348aa5e135bff0d4d5c914933aff5a0`.
- Relevant upstream files: `features/party/PartyApi.kt`, `PartyRepository.kt`, `PartyChannel.kt`, plus the server configuration and Supabase Realtime client.
- The RPC contract includes create/join/preview/link-profile/heartbeat/update/leave/end/host-only/member-removal/chat-send/chat-history/clock/enabled operations under the `watch_party_*` namespace.
- Realtime uses a private `watch-party:{partyId}` channel, broadcast messages and presence. Availability is gated by `ServerCapabilities.watchParty` and the backend `watch_party_enabled` RPC. Create/join requires a signed-in, non-anonymous account.
- Mew's current Supabase client installs Auth, PostgREST and Functions but not Realtime. Mew's Gradle runtime config has an official Nuvio API fallback; confirm effective build/runtime endpoint and key configuration before enabling the feature. Do not assume endpoint compatibility merely because the fallback exists.
- Required port: Realtime dependency/client lifecycle; capability gate; validated auth; Party models/RPCs; realtime channel and presence; synchronized playback clock and drift correction; chat; host/member permissions; player and settings entry points; reconnect/late-join/leave/end/error tests.
- Status remains **NOT IMPLEMENTED** until the above integration and tests exist.

### Existing-but-broken features and no-duplicate guard
- AutoSync exists in Mew source, but the device observation is FAIL (enabled with no noticeable effect). Repair the existing controller/engine integration; do not add another AutoSync implementation.
- The Android engine preference exists in Mew settings, but the in-player runtime switch is missing. Port the Nuvio runtime switch/track carry-over into the existing player controls; do not duplicate the settings selector.
- Liquid Glass's current effect-only implementation does not meet the requirement. The toggle must switch between two distinct player layouts: default layout when off and a separate Liquid Glass player design when on.
- The current rotation control/path exists in source but the user reports it does not work. Trace and repair the existing Activity orientation request/lifecycle instead of adding a second rotation control.
- The log shows multiple successful CloudStream section payloads (including a 39-section CineStream response), while the UI exposes one Raw Catalog per extension. Investigate section-to-catalog mapping before changing plugin loading.
- TMDB service HTTP 401 responses and plugin-runtime 404s from title/IMDb identifiers used as numeric TMDB IDs are separate failure classes. Normalize media identity and verify the v3-key/v4-token contract independently.
- Repeated `valid session required for sync` responses indicate unauthenticated remote writes. Gate AddonRepository and WatchProgressRepository write paths on a validated non-anonymous session while retaining local state and retrying only when auth becomes valid.


## Unified settings information-architecture audit — 2026-11-20

### Verified baseline
- Target branch: `Main-dev`, base SHA `69b4a96072b506f60dcbe538c5f6b71db3a4a464`.
- Stable branch left untouched: `main` at `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Reference snapshot: `upstream-sync` at `af51bbbc13cdb315a88ef597eb4d2672a60335d5`.
- Mother upstream: `luqmanfadlli/NuvioMobile-Enhanced`, `enhanced` at `b23b28a878b78f5ef9252128b901155fee6eeea9`.
- No root `MEW-CONTEXT-SNAPSHOT.md` or `CONTEXT-SNAPSHOT.md` exists on this baseline. This audit is recorded in the existing parity document to avoid a duplicate handoff file.
- Documentation-only update; no application source changes, builds, tests, or device checks.

### User-approved objective
Reorganize Mew and upstream-derived settings into one coherent Mew settings experience. This is an information-architecture/discoverability task, not a wholesale upstream UI replacement. Preserve Mew identity, Mew-specific features, stored preferences, profile scoping, platform behavior, feature-policy gates, and saved navigation compatibility.

### New-setting indicator requirement
- New settings/features display a small, restrained `NEW` label beside the relevant setting/page.
- Lifetime: **3 app versions total**, counting the introduction version and the next two installed versions. Example: introduced in 0.5.2 → visible on 0.5.2, 0.5.3 and 0.5.4 → hidden from 0.5.5 onward.
- Count installed app version changes, not app launches or elapsed time. If a user skips versions, the badge remains until the configured expiry version is installed.
- Prefer centralized metadata keyed by stable setting IDs; avoid scattered hardcoded badges.
- Inspect current version/build metadata and comparison semantics before implementation. Add boundary tests for introduction version, last visible version, expiry version, skipped versions and invalid/unknown version strings.
- Apply consistently across mobile, tablet and iOS with accessible semantics and theme-safe contrast.

### Confirmed Mew settings structure
Inspected at the baseline SHA:
- `SettingsModels.kt`, `SettingsRootPage.kt`, `SettingsSearch.kt`, `SettingsScreen.kt`, `AppearanceSettingsPage.kt`, and `AdvancedSettingsPage.kt`.
- Current categories: `Account`, `General`, `Enhanced`, `About`, `Advanced`.
- `SettingsPage` already models parent relationships, but many pages still share broad `General` categorization.
- Appearance owns layout-related children (Streams, Continue Watching, Poster Customization, Homescreen, Meta Screen); Content Discovery owns Addons, Plugins and CloudStream; Integrations owns AI, TMDB, MDBList and Debrid.
- Tracking retains enum name `TraktAuthentication` for saved navigation-state compatibility. Do not rename casually.
- Root page is section-based and uses policy flags/callbacks. Search entries carry page/section/category context and route through page targets; audit each entry against actual control and policy visibility.
- Mobile/tablet rendering paths coexist in `SettingsScreen.kt`; treat as intentional layout variants unless behavior comparison proves duplication.
- Advanced currently groups startup, diagnostics/Sentry and runtime logs. Logs (view/copy/export) should be considered separately from ordinary preferences.

### Initial information-architecture assessment
- Upstream Enhanced categories `You`, `Look`, `Watch`, `Services`, `System` and granular Playback/Layout subpages are a useful reference.
- **ADAPT**, do not wholesale-copy: Account/Profile → You; Appearance/Layout → Look; Playback/Downloads/Content Discovery → Watch; Integrations/Tracking → Services; Advanced/Notifications/About/diagnostics → System/About as semantically appropriate.
- Mew-specific CloudStream, Nuvio Enhanced, AI, TMDB enrichment, MDBList, Simkl/Trakt, profile insights, custom theme/accent and diagnostics must be placed by user intent and dependencies.
- Large Playback page is a strong candidate for modular subpages, but every control, callback, persistence owner, engine/platform condition and profile scope must be mapped first.
- Existing mobile/tablet layouts and Android/iOS persistence implementations are likely intentional parallel implementations; verify equivalent semantics rather than merging source sets.

### Work still required — audit only
1. Complete a control-level inventory for Playback, Appearance/Layout, Content Discovery, Integrations/Tracking, Account/Profile, Notifications, Advanced, About and Mew Enhanced.
2. Map each setting to current page/section, proposed category/subpage, upstream equivalent, persistence repository/key, profile scope, platform availability, feature-policy gate and search target.
3. Audit dialogs/sheets for title, explanation, actions, cancel/dismiss behavior, selected state, theme tokens, contrast and platform parity.
4. Verify search results target the correct page/section/control, hidden/disabled features are not surfaced incorrectly, and back navigation works on mobile/tablet.
5. Classify similar implementations as TRUE DUPLICATE, INTENTIONAL PARALLEL, SHARED PATTERN / DIFFERENT SEMANTICS, COMPATIBILITY SHIM or FALSE POSITIVE.
6. Verify version metadata and implement/test the three-version NEW badge only after the audit and separate implementation approval.
7. Produce the proposed hierarchy and migration/compatibility plan. No application-source restructuring until separately approved.

### Current status
- Settings information-architecture audit: **IN PROGRESS**.
- Source changes: **NONE**.
- Build/tests/device verification: **NOT RUN**.
- Next: inspect upstream root/settings navigation and complete Mew Playback plus content/integration control mapping, then continue dialogs/search/platform audit.


## Settings audit findings — 2026-11-20 (continued)

### Playback: confirmed hierarchy gap
- Mew's `PlaybackSettingsPage.kt` is approximately 185 KB and currently renders **11 major sections** in one page: player controls, subtitle/audio selection, subtitle rendering/appearance, P2P, stream selection, stream auto-play, decoder, iOS audio/video output, skip segments and next-episode behavior.
- The file also owns many dialog states and engine/platform conditionals. Examples include iOS-only external-player selection and iOS output controls, Android-only external subtitle forwarding/decoder/libass options, and P2P visibility policy. These conditions are behavior, not clutter; preserve them during any split.
- Enhanced separates Playback into ten child destinations: Player, Stream Selection, Up Next, Skip Segments, Interface, Audio, Subtitles, Video, Buffer and P2P. Its `visiblePlaybackSections` explicitly hides Buffer on iOS and when libmpv is selected, and hides P2P when the feature is unavailable.
- Decision: **ADAPT** the granular navigation and section ownership into Mew's existing `SettingsPage` model. Keep Mew's controls, repositories, engine semantics and platform gates; do not replace the 185 KB file with upstream implementations.

### Mew Enhanced page: mixed-domain settings
- `NuvioEnhancedSettingsPage.kt` is approximately 91 KB and contains its own internal category navigation (`New`, `All`, `Core`, `Home`, `Player`, `System`) plus many sections spanning home/hero, player tools, detail presentation, release radar, network, backup, downloads, discovery and diagnostics.
- This page is a second navigation taxonomy inside the main settings taxonomy. It is a major source of discoverability fragmentation. Proposed direction: redistribute controls by user intent into Look/Home, Watch/Playback, Services/Integrations and System/Advanced while retaining a clearly named Mew-specific destination only for features that genuinely have no natural home.
- Confirmed **TRUE DUPLICATE**: Parental Guide appears in both Playback → Player and Nuvio Enhanced → Player Tools. Both read `PlayerSettingsRepository.uiState.showParentalGuide` and write through `PlayerSettingsRepository.setShowParentalGuide`. Retain one canonical control under Playback → Player; preserve any required feature-announcement metadata during migration. Do not duplicate the stored preference or repository.
- Other overlapping-looking controls (e.g. next-episode behavior, subtitle sync, memory-safe buffering and hero/detail settings) are **NEEDS INVESTIGATION** until state keys, callbacks and runtime effects are compared. Similar wording alone is not proof of duplication.

### Existing NEW/highlight behavior — distinct semantics
- Mew already has a compact `EnhancedNewBadge` visual using the accent color with low-opacity fill, thin accent border and label-small typography. This is a useful styling reference for the requested subtle `NEW` label.
- Current Enhanced highlights are not version-expiring: `NuvioEnhancedSettingsUiState.isNew` checks `featureHighlightsEnabled` and `seenFeatureIds`; controls call `markFeatureSeen`, and users can mark all features seen. The set is persisted in the Enhanced settings payload through Android SharedPreferences and iOS NSUserDefaults using `ProfileScopedKey`.
- The existing system also has a hardcoded `latestReleaseFeatureIds` / `previouslyReleasedFeatureIds` split. This is a feature-highlights/seen-state system, not the requested three-version expiry system.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Reuse the small badge treatment where appropriate, but do not repurpose the profile-scoped seen-state behavior as the global release-expiry mechanism without an explicit migration design.

### Search and navigation: confirmed discoverability gap
- `SettingsSearchTarget` currently supports `Page`, Downloads, Collections, Switch Profile and Check for Updates. `SettingsSearchEntry` stores a section label, but `addRow` targets only `SettingsSearchTarget.Page(page)`.
- Therefore, a result describing an individual setting can identify its page/section in text but cannot address a stable control ID or scroll/focus the exact row. Mobile routes the result to a page/callback; tablet opens the page inline. This is a confirmed page-level-only search behavior, not exact-setting deep linking.
- Proposed adaptation: add stable setting/control IDs and a target that can open the owning page and reveal/scroll to the matching control, while preserving existing page-only targets and external callback routes. Validate policy gates before reveal and keep mobile/tablet behavior equivalent.
- Preserve `SettingsPage` enum names used by `initialPageName` / `requestedPageName` and external navigation. Parent-page changes affect back navigation because `previousPage()` directly returns `parentPage`; route compatibility needs explicit coverage.

### Version source for future NEW expiry
- Gradle generates `AppVersionConfig.VERSION_NAME` and `VERSION_CODE` into common runtime config. The updater already has semantic-version parsing/comparison utilities and host tests.
- Implementation should use explicit stable-ID metadata with an explicit last-visible release (e.g. introduced 0.5.2, visible through 0.5.4, hidden at 0.5.5), rather than assuming every release increments a patch number. Test skipped releases and unknown version values. Version/build metadata source and platform propagation still require a final implementation-stage verification.

### Duplicate/platform disposition
- Android and iOS Enhanced settings storage are **INTENTIONAL PARALLEL**: both persist the same profile-scoped payload through platform-native stores. Do not merge platform adapters.
- Mobile and tablet settings navigation/rendering remain **INTENTIONAL PARALLEL**; align search targets and navigation semantics, but retain responsive presentation differences.
- No additional duplicate is confirmed by this pass beyond the Parental Guide row. All other candidates remain unclassified until state/callback/runtime comparison is complete.

### Status after this pass
- Settings audit: **IN PROGRESS**; Playback, root categories, Enhanced settings and search architecture inspected.
- Confirmed duplicate: **1** (Parental Guide control row).
- App source changes: **NONE**.
- Build/tests/device verification: **NOT RUN**.
- Next: complete control inventories for Appearance/Layout, Content Discovery, Integrations/Tracking, Account/Profile, Notifications, About and Advanced; then inspect all dialog/sheet implementations and theme contrast behavior.


## Settings audit findings — dialogs, sheets and cross-links — 2026-11-20

### Dialog/sheet consistency
- Shared baseline: `core/ui/Dialog.kt` exposes `DialogSurface` with Mew/Nuvio theme tokens for dialog surface, text, shape, border, spacing and max width. `core/ui/BottomSheet.kt` similarly centralizes sheet colors, shape and drag-handle behavior.
- **SHARED PATTERN / DIFFERENT SEMANTICS:** `StreamsSettingsPage.kt` has a private `SettingsDialogSurface` with similar token-based surface/title/padding behavior, but it is a content surface nested inside its own alert-dialog wrapper. Compare before consolidating; do not blindly replace.
- Styling drift risk: `PlayerToolbarSettingsDialog.kt`, `SimklSyncInfoDialog.kt` and several `DebridSettingsPage.kt` dialogs use direct `MaterialTheme.colorScheme` values, hardcoded 20.dp padding and 20.dp rounded corners instead of the shared dialog tokens. The Material color scheme is app-themed, so this is **not yet a confirmed contrast bug**; verify light/dark/custom accent palettes, shape consistency, width, scroll constraints and action placement before changing.
- Debrid has a local `DebridDialogSurface` reused by several template/selection dialogs. Keep its domain-specific input and selection behavior; only consolidate the visual shell if parity can be proven.
- `TrackingAdaptivePicker` intentionally presents a dialog on tablet and a bottom sheet on phone. Preserve responsive presentation while ensuring the same title, subtitle, selected/unavailable state and dismiss behavior.
- Android uses the Compose Material 3 sheet implementation; iOS uses a UIKit page sheet. These are **INTENTIONAL PARALLEL** platform implementations, not duplicates. iOS tracks one active native sheet and dismisses the previous one before presenting another; overlapping presentation/disposal ordering remains **NEEDS INVESTIGATION**.
- Dialog content audit is not complete: titles, explanatory text, destructive wording, action labels, selected state, keyboard/focus, accessibility and dismiss behavior still need per-dialog review. No dialog visual or copy bug is declared confirmed in this pass.

### Confirmed cross-link/back-navigation mismatch
- Content Discovery conditionally shows a TMDB setup shortcut when no API key is configured and routes it to `SettingsPage.TmdbEnrichment`.
- `SettingsPage.TmdbEnrichment.parentPage` is `Integrations`, and `previousPage()` returns the static parent. In the internal mobile/tablet settings navigation, opening this shortcut from Content Discovery can therefore make Back return to Integrations rather than the page the user came from.
- Keep the TMDB settings destination canonical under Integrations/Services, but either remove the duplicate shortcut or support an explicit origin-aware return route. Do not change parent mappings without covering existing deep links and saved page names.

### Appearance/Layout inventory — initial mapping
- Appearance root currently groups Theme; Display (AMOLED, Liquid Glass, auto-hide, app icon, language, navigation style); Home links (Homescreen, Collections, Continue Watching, Poster Customization); Streams; and Detail Page.
- Child pages already exist for Homescreen, Streams, Continue Watching, Poster Customization and Meta Screen. These mostly belong under Look/Layout; retain their distinct page ownership rather than flattening every control into one long page.
- Some child pages contain behavior beyond pure appearance (e.g. Continue Watching visibility/up-next/sort behavior and home catalog/source selection). Split only where intent is genuinely different; do not scatter closely related controls across categories for theoretical purity.
- Debrid is a large dedicated page (about 93 KB) with provider configuration, instant playback preparation, result management, filtering and formatting. It is a complex service configuration destination; retain it under Services/Integrations unless control-level mapping finds a strong reason to subdivide it.

### Status after dialog/cross-link pass
- Confirmed duplicate controls: **1** (Parental Guide).
- Confirmed navigation mismatch: **1** (Content Discovery → TMDB setup shortcut uses Integrations as static parent).
- Dialog theme/copy review: **IN PROGRESS**; visual drift candidates identified, no confirmed contrast failure.
- Appearance/Layout and major integration page outlines inspected; control-level mapping still incomplete.
- Source changes: **NONE**. Builds/tests/device checks: **NOT RUN**.
- Next: finish Account/Profile, Tracking, Notifications, Advanced/About and remaining integration control mapping; audit every settings dialog and verify iOS-specific visibility/lifecycle.


## Settings audit findings — Account, Tracking and System — 2026-11-20

### Account/Profile versus Services
- The root Account section currently contains profile switching, Account, and Tracking. `SettingsPage.TraktAuthentication` is categorized as `SettingsCategory.Account`, despite its page containing Trakt/Simkl service connections and tracking-source configuration.
- `TrackingSettingsPage.kt` includes tracking-service setup, Simkl manual client ID, anime-tracking display preference, library source selection, watch-progress source selection, Continue Watching window, Trakt comments, More Like This source, and anime ID preference (IMDb/MAL/Kitsu).
- Decision: **ADAPT** into a Services/Tracking destination, separate from Account/Profile. Keep authentication/session actions and account deletion in You/Account. Preserve enum name `TraktAuthentication` for saved route compatibility unless a migration is proven safe.
- `ProfileInsightsSettingsPage.kt` is a large profile-focused surface (overview, taste, favorite people and profile management actions). Treat as You/Profile rather than generic settings; complete its control/state inventory before moving anything.

### Advanced/System versus profile settings
- `AdvancedSettingsPage.kt` currently combines Remember Last Profile (a profile/startup preference), Sentry consent, runtime Logs (view/copy/export), and Continue Watching cache cleanup.
- Recommended destinations: Remember Last Profile → You/Profile or Startup; Sentry and runtime logs → System/Diagnostics; cache cleanup → System/Storage or Continue Watching maintenance. Keep destructive/cache confirmation semantics and profile-scoped cache behavior.
- The log section currently uses hardcoded English labels (Logs, View logs, Copy logs, Export logs) rather than localized string resources. This is a confirmed localization/copy consistency gap to include in the text audit.
- Search inventory check: `no View Logs / Copy Logs / Export Logs search entries found` in `SettingsSearch.kt`.
- Nuvio Enhanced separately includes Network, Backup/Import and Diagnostics sections. Map Network/Diagnostics/Backup into System rather than leaving them under a broad feature bucket.

### Other page disposition
- Notifications is focused: episode-release alert toggle plus a test-notification action. Keep under System/Notifications; preserve permission-denied and test-send states.
- About currently owns Supporters/Contributors, Privacy Policy, Licenses/Attributions and Check for Updates. This is a coherent group; keep it distinct from Advanced/System.
- Account page uses authentication state and feature-policy-gated account deletion. Keep deletion hidden unless policy permits and the account is authenticated.
- Licenses/Attributions has grouped app/data/playback attributions and iOS-specific content. Retain platform-aware attribution rather than forcing Android-only entries onto iOS.

### Updated proposed category map (still provisional)
| Destination | Current Mew content to map |
|---|---|
| You / Account & Profiles | Account status, sign out/delete, profile switch/management, profile insights, favorite people, remember-last-profile |
| Look / Appearance & Layout | Theme, accent, AMOLED, language, app icon, navigation, homescreen/layout, streams presentation, Continue Watching card presentation, poster and detail presentation |
| Watch / Playback & Content | Playback subpages, downloads, content discovery, Addons, Plugins, CloudStream, Continue Watching behavior |
| Services / Integrations & Tracking | Trakt, Simkl, anime tracking, TMDB, MDBList, Debrid, AI and other service connections |
| System / Notifications, Diagnostics & Storage | Notifications, network/DNS, Sentry/logs, cache maintenance, backup/import, diagnostics |
| About | Supporters, contributors, privacy, licenses/attributions, version/update information |

This map is provisional. Where a control spans two domains, choose one canonical owner and use a contextual link only if back navigation remains correct.

### Status after this pass
- Confirmed duplicate controls: **1** (Parental Guide).
- Confirmed navigation mismatch: **1** (Content Discovery → TMDB setup shortcut parent).
- Confirmed localization gap: **1 group** (hardcoded Advanced Logs labels).
- Tracking, Account/Profile, Notifications, Advanced and About page structures inspected.
- Source changes: **NONE**. Builds/tests/device checks: **NOT RUN**.
- Next: complete control-level audit of remaining Integrations and profile settings, inspect every dialog/copy path and theme contrast matrix, then finalize the hierarchy and migration plan.


## Additional confirmed duplicate — TMDB enrichment toggle — 2026-11-20

- `ContentDiscoverySettingsPage.kt` renders `settings_tmdb_enable_enrichment` and receives `TmdbSettingsRepository::setEnabled` from `SettingsScreen.kt`.
- `TmdbSettingsPage.kt` renders the same title/description, reads `settings.enabled`, applies the same `settings.hasApiKey` gate and calls `TmdbSettingsRepository::setEnabled`.
- Classification: **TRUE DUPLICATE** — same preference, same repository state, same setter and same enablement gate exposed on two pages.
- Proposed canonical owner: Services → Integrations → TMDB. Replace the Content Discovery toggle with a single navigation row to TMDB settings (or remove it if a separate entry already exists), and make the missing-API-key shortcut route-aware so Back returns correctly.
- Updated confirmed duplicate count: **2** — Parental Guide (Playback + Nuvio Enhanced) and TMDB enrichment enabled (Content Discovery + TMDB settings). No other exact shared setter was found in the inspected candidate page set; remaining apparent overlaps stay **NEEDS INVESTIGATION**.


## Theme accent contrast audit — 2026-11-20

### Confirmed contrast risk
- `Theme.kt` maps `palette.secondary` to Material `primary` and `palette.onSecondary` to `onPrimary`. These foreground/background pairs are consumed by Material components when they render primary-filled surfaces.
- WCAG relative-luminance contrast calculations on the current `ThemeColors.kt` palette values found multiple white-on-accent pairs below 4.5:1:
  - Amber: **2.37:1**
  - Lagoon: **2.49:1**
  - Sunset: **2.80:1**
  - Emerald: **3.30:1**
  - Messenger: **3.44:1**
  - Blossom: **3.53:1**
  - Arctic Blue: **3.62:1**
  - Ocean: **3.68:1**
  - Crimson: **4.23:1**
  - Amethyst: **4.23:1**
- Some palettes already choose dark foregrounds and pass. This is not a claim that every use of the accent color is unreadable; it is a confirmed risk for text/icons rendered on primary/accent-filled surfaces.
- Custom accent currently sets `onSecondary = Color.White`. `toAccessibleAccent()` only constrains luminance to 0.12–0.24; at the upper bound, white foreground contrast is about **3.62:1**, so the current adjustment does not guarantee 4.5:1.
- No dedicated ThemeColors/contrast regression test file was found in the inspected test tree.

### Recommended correction (not implemented)
- Calculate foreground color from actual contrast against the resolved accent and select black or white based on the higher valid ratio; if neither meets the target, adjust the accent itself or use a safer accessible foreground/background pairing.
- Apply this centrally in theme palette resolution so all Material components and Mew surfaces receive consistent values. Do not patch individual settings/dialog screens.
- Add tests for every predefined palette and custom accent boundary cases, including dark/light foreground selection and minimum contrast.
- Then visually verify accent chips, selected controls, badges, buttons, dialog actions, focus rings and navigation surfaces in Mew's supported theme set. Device rendering remains unverified.

### Status after theme pass
- Confirmed duplicate controls: **2** (Parental Guide; TMDB enrichment toggle).
- Confirmed navigation mismatch: **1** (Content Discovery → TMDB shortcut parent).
- Confirmed localization gap: **1 group** (Advanced Logs labels).
- Confirmed theme contrast risk: **multiple primary/onPrimary palette pairs**, including custom accent.
- No application source changes. Builds/tests/device verification: **NOT RUN**.
- Next: complete remaining dialog/copy review and finalize the unified settings map and safe migration strategy before requesting implementation approval.


## Downloads settings parity audit — 2026-11-20

### Upstream capability inventory
Upstream Enhanced's `DownloadsSettingsPage.kt` exposes:
- Preferred download resolution.
- Allow downloads over mobile data.
- Show/hide download action.
- Legacy download mode (Android-specific gate).
- Download location selection/reset.
- Per-category concurrency limits.

Upstream has a common `DownloadsSettingsRepository` and Android/iOS storage/location-picker implementations.

### Mew equivalent check
- Mew's root Downloads destination opens `DownloadsScreen.kt`, a download manager (active, pending-source, completed movies/shows; pause/resume/retry/delete; open directory). It is **not** a download-settings page.
- Mew has an episode-download flow sheet with a preferred-quality choice and batch source-selection behavior; this is flow-level behavior, not yet proven equivalent to upstream's global preferred-resolution setting.
- Mew's Enhanced page exposes external-folder selection only on Android. `DownloadsExternalFolderPlatform.ios.kt` currently returns success(null) from `chooseFolder`, has no selected folder and no-op clear/profile callbacks. This is not equivalent to upstream's iOS document picker.
- Mew's downloads storage includes profile-scoped download payload, external-folder URI and episode-download-settings payload. Preserve these keys and existing batch/download behavior; do not replace the repository/storage layer wholesale.
- No Mew `DownloadsSettingsPage` / `DownloadsSettingsRepository` equivalent was found in the inspected tree.

### Decision
- Upstream Downloads settings: **ADAPT / NEEDS INVESTIGATION**. First map current Mew download defaults, per-batch settings, worker/coordinator concurrency, Android storage permissions and iOS private-storage behavior. Then decide which global controls are useful and safe to expose.
- The root Downloads manager remains a utility destination, not a settings category. If global download preferences are introduced, give them a distinct Watch → Downloads settings destination rather than merging the manager screen with preferences.
- iOS download-location selection is a separate platform capability candidate; upstream's document picker is evidence of an implementation path, not proof it is safe to port into Mew's storage architecture.


## Upstream search behavior comparison — 2026-11-20

- Upstream Enhanced `SettingsSearch.kt` also models individual row results as `SettingsSearchTarget.Page(page)`; it does not currently deep-link to a specific control ID or scroll position.
- Upstream does improve Playback discovery because each playback subsection is its own `SettingsPage`, and search results can target those granular pages. Mew's single Playback page prevents that level of destination precision today.
- Therefore, exact-control deep linking is a **Mew UX improvement opportunity**, not a missing upstream feature. The first parity step is to adapt granular page destinations; exact control reveal can be added as a deliberate Mew enhancement if it can be made stable and accessible.


## Saved category-state compatibility risk — 2026-11-20

- Mew tablet settings stores the selected category as a string in `rememberSaveable` (`selectedCategory`) and immediately resolves it with `SettingsCategory.valueOf(selectedCategory)`.
- Current stored names are `Account`, `General`, `Enhanced`, `About`, and `Advanced`. Renaming/replacing the enum without a compatibility mapper can make a restored old value throw instead of falling back safely.
- Upstream Enhanced already has `settingsCategoryForName` mappings for old names (`Account` → You, `General` → Look, `About`/`Advanced` → System), but Mew's additional `Enhanced` value needs an explicit migration/fallback decision.
- `SettingsPage` restoration is more defensive (`valueOf` wrapped in `runCatching` and fallback to Root); category restoration currently is not.
- Requirement before category restructuring: add a safe parser/migration for every legacy category string, including `Enhanced`, and test valid current names, all legacy names, unknown names and process-restored state. Preserve existing `SettingsPage` enum names for external navigation/deep links; use compatibility shims for any page split/retirement.


## Mew Enhanced cross-domain redistribution map — 2026-11-20

### Proposed five-category structure
Use upstream Enhanced's five top-level categories as the baseline: **You, Look, Watch, Services, System**. Keep About as a destination under System (with Supporters/Contributors and Licenses/Attributions beneath it), rather than creating a sixth top-level category. This is a proposal pending the complete control inventory.

### Mew Enhanced settings that belong with existing domain pages
- **Player Tools → Watch / Playback:** Mew Enhanced currently contains subtitle sync menu, subtitle/audio selector style, player clock overlay, random next episode, next-episode button, Android memory-safe buffer, source pinning, background prefetch and player status overlay. These are mostly player-domain settings; place them in the relevant Playback subpages, retaining their Mew-specific state/repositories and feature gates.
- **Home/Hero Experience → Look / Homescreen:** `HomeCatalogSettingsRepository` owns hero auto-scroll and motion preview in Nuvio Enhanced, while the Homescreen page owns hero visibility, catalog type, unreleased content, catalog underline and hero source selection. These are **SHARED PATTERN / DIFFERENT SEMANTICS** under one screen domain, not proven duplicate controls. Consolidate discoverability on Homescreen without merging distinct state.
- **Detail Presentation / Details Experience → Look / Meta Screen:** Nuvio Enhanced exposes episode-card style, show-download action, trailer playback/sound and episode ratings through `MetaScreenSettingsRepository`; the Meta Screen page currently exposes background mode, tab layout, blur-unwatched and section ordering. These are separate controls for the same destination. Group them under one Details/Meta Screen destination; do not create duplicate setters.
- **Release Radar / Discovery → Watch / Content Discovery:** group user-facing discovery filters and release signals with content discovery, while keeping profile-only or system maintenance controls in their proper destinations.
- **Network → System / Network:** DNS-over-HTTPS provider and diagnostics-related network configuration should not be buried in a general Enhanced page.
- **Backup/Import → System / Backup:** keep import/export validation, file handling, profile scope and backup compatibility intact.
- **Downloads external folder → Watch / Downloads:** this is download configuration, distinct from the Downloads manager screen. Keep the Android-only capability gate until iOS support is deliberately implemented.
- **Diagnostics → System / Diagnostics:** copy diagnostics and last-crash report actions should be grouped with Sentry/runtime logs, but their different data sources and side effects must be retained.

### Duplicate audit refinement
- Exact duplicates confirmed remain **2**: Parental Guide and TMDB enrichment toggle.
- Home/Hero and Meta Screen settings are currently split across separate pages but use different settings/callbacks; classify as **SHARED PATTERN / DIFFERENT SEMANTICS**, not duplicates.
- Keep the Enhanced page as a compatibility destination while its controls are redistributed. Preserve external page names and saved state until route migration is verified.


## Proposed destination/page hierarchy — working draft — 2026-11-20

This is the clearest current mapping from the audited Mew pages to upstream Enhanced's five categories. It remains an audit proposal; no application code has been changed.

- **You**
  - Account
  - Profiles / Profile Insights / Favorite People
  - Profile/startup preferences (including Remember Last Profile)
- **Look**
  - Appearance (theme, accent, AMOLED, app icon, language, navigation)
  - Layout
    - Homescreen
    - Streams presentation
    - Continue Watching presentation
    - Poster Customization
    - Meta Screen / Details
- **Watch**
  - Playback
    - Player
    - Stream Selection
    - Up Next
    - Skip Segments
    - Interface
    - Audio
    - Subtitles
    - Video / Decoder
    - Buffer
    - P2P (only when supported)
  - Downloads preferences (separate from the Downloads manager screen)
  - Content Discovery
    - Addons
    - Plugins (policy-gated)
    - CloudStream (policy-gated)
- **Services**
  - Integrations
    - AI Assistant
    - TMDB
    - MDBList
    - Debrid
  - Tracking
    - Trakt
    - Simkl
    - Anime tracking and source preferences
- **System**
  - Notifications
  - Network / DNS
  - Diagnostics / Logs / Sentry
  - Storage / Cache maintenance
  - Backup / Import
  - About
    - Supporters / Contributors
    - Licenses / Attributions
    - Privacy / Version information

### Page-model changes implied by this draft
- Add a Layout parent page and place existing layout child pages under it; keep Appearance focused on theme/display.
- Add an About parent page under System and place Supporters/Contributors and Licenses/Attributions beneath it.
- Add focused Playback subpages while keeping the existing `Playback` enum as a compatibility entry/landing route.
- Add a Downloads preferences page only after Mew's existing download flow/storage behavior is mapped. Keep the existing Downloads manager route distinct.
- Reclassify Tracking under Services; retain `TraktAuthentication` as a legacy page name/route if required.
- Keep `NuvioEnhanced` as a compatibility destination during redistribution. Move its controls to their canonical domain pages; do not leave a second parallel category taxonomy.
- Preserve old category names through a safe migration mapper and old page names through compatibility routing. Never assume renaming an enum is a harmless cosmetic change.


## Settings dialog inventory — 2026-11-20

### Inventory from inspected call sites
- At least **40 BasicAlertDialog** call sites across Playback, Streams, Continue Watching, Debrid, Player Toolbar, Simkl info, Tracking picker, Tracking provider disconnect, Advanced, Supporters/Contributors and the Playback-triggered P2P consent dialog.
- **2 Material AlertDialog** call sites in Nuvio Enhanced backup export/import.
- **2 NuvioStatusModal** confirmation flows in Account (sign-out and delete-account).
- **6 NuvioModalBottomSheet** call sites across Appearance, Poster Customization, Profile Insights and Tracking picker.
- This is a call-site inventory, not a completed state-by-state visual/copy test.

### Visual patterns to consolidate carefully
- Playback alone owns **20** BasicAlertDialog calls. Most use the same hand-built Surface (Material `surface`, 20.dp corners, 20.dp padding, 12.dp spacing), but do not use the shared `DialogSurface` token wrapper.
- Debrid has eight BasicAlertDialog calls and its own reusable `DebridDialogSurface`; Supporters uses a different 24.dp/24.dp surface; P2P consent uses a 24.dp surface with animated background; Advanced uses token-based custom surfaces; Nuvio Enhanced uses standard Material AlertDialog; Streams has a private token-based surface.
- This is a **SHARED PATTERN / DIFFERENT SEMANTICS** finding, not a reason to collapse all dialog content into one abstraction. Centralize surface tokens/shape/width/action primitives where possible, but preserve each dialog's selection, input, consent, destructive, loading, scroll and dismiss behavior.
- The responsive Tracking picker (tablet dialog, phone sheet) and native iOS sheet are intentional platform/layout variants. Verify equivalent semantics, not identical presentation.

### Copy audit
- Hardcoded English text remains confirmed in Advanced → Logs: section title, row titles/descriptions and Copy/Export action labels. Move these to localized resources when implementation is approved.
- The other inspected dialog titles and explanatory copy predominantly use string resources; complete review of every option label, validation error, disabled reason, destructive confirmation and accessibility description is still pending.

### Contrast tie-in
- The P2P consent dialog's Enable button uses Material `primary` as its filled container and default `onPrimary` content color. This directly exercises the low-contrast palette pairs calculated in the theme audit.
- Nuvio Enhanced backup dialogs also use default Material AlertDialog buttons, which consume the same theme primary/onPrimary pair.
- Therefore the accent contrast issue is an actual affected-surface risk, not merely a theoretical palette calculation. Fix the foreground/background pairing centrally and add component-level contrast tests.

### Status
- Dialog inventory: **PARTIAL — call sites counted; per-dialog state/copy/accessibility review remains**.
- Confirmed localization gap: Advanced Logs.
- Confirmed accent contrast impact: P2P Enable and Enhanced backup dialog buttons use primary/onPrimary.
- Source changes: **NONE**. Builds/tests/device checks: **NOT RUN**.


## Platform and feature-policy audit — 2026-11-20

- Settings UI is primarily shared in `commonMain`, with responsive mobile/tablet navigation and platform conditionals. Keep the shared setting model and preserve intentional presentation differences.
- Appearance hides Navigation Bar Style on iOS (`if (!isIos)`); Playback adds explicit iOS audio/video output controls. Keep these platform-specific controls rather than forcing identical Android/iOS lists.
- Playback conditionals include engine-dependent decoder/rendering options and P2P visibility via `P2pSettingsRepository.isVisible`. Preserve the current runtime capability/policy gates when splitting Playback.
- Plugins and CloudStream entries are exposed only when `AppFeaturePolicy.pluginsEnabled`. Their SettingsPage branches intentionally fall back to Addons content when disabled; classify as **COMPATIBILITY SHIM** unless further caller audit proves otherwise. Do not expose hidden entries through search.
- Mew's Enhanced external-folder section is hidden on iOS and `DownloadsExternalFolderPlatform.ios.kt` is currently a no-op. Upstream Enhanced has an iOS document-picker implementation for Downloads settings. This is a platform capability gap to investigate, not permission to port storage behavior without reviewing Mew's private-download storage and lifecycle.
- Android Material bottom sheets and iOS UIKit page sheets are **INTENTIONAL PARALLEL**. The iOS sheet coordinator permits one active native sheet and dismisses an existing one before presenting another; overlapping presentation/disposal lifecycle remains a targeted iOS regression test.
- Source-level iOS inspection only. No iOS simulator/device verification was run.

### Policy/navigation requirements for restructuring
- Every page, search result and legacy route must honor the same feature-policy visibility.
- Preserve fallback behavior for disabled Plugins/CloudStream routes, or provide a deliberate safe redirect.
- Preserve iOS-only Playback controls and platform-specific storage behavior.
- Validate iPhone/iPad navigation and sheet dismissal on simulator/device before release; report this as NOT RUN until performed.


## Utility shortcuts and NEW badge version-source refinement — 2026-11-20

### Root utility shortcuts
- Mew's SettingsRootPage General section includes Downloads and Watch Party navigation rows. Both callbacks leave the SettingsPage model; Downloads opens `DownloadsScreen` (the manager), and Watch Party opens its feature screen. These are **utility shortcuts**, not settings preferences.
- Keep these destinations accessible if desired, but do not mix them into the new preference category taxonomy. Consider a separate utility/quick-access area outside You/Look/Watch/Services/System.
- Mew search uses a special `SettingsSearchTarget.Downloads` for the manager; upstream search uses `SettingsSearchTarget.Page(SettingsPage.Downloads)` for the actual Downloads preferences page. These targets are not semantically interchangeable.

### NEW badge implementation constraints
- Gradle generates `AppVersionConfig.VERSION_NAME` from `MARKETING_VERSION` and `VERSION_CODE` from `CURRENT_PROJECT_VERSION`.
- Existing updater `VersionUtils.parseVersionParts` extracts numeric version components and its comparison helper also supports build codes; it is not a complete SemVer prerelease comparator. Do not assume it correctly orders every beta/RC suffix.
- Recommended badge registry: stable setting ID → explicit `introducedInVersion` and `visibleThroughVersion` (or equivalent explicit expiry release). Render only when current installed `VERSION_NAME` is within that range. For example, 0.5.2 → visible through 0.5.4 → hidden from 0.5.5.
- This avoids arithmetic assumptions about patch increments and naturally expires when a user skips releases. Unknown/unparseable current or expiry version should fail safely and be covered by tests.
- Keep this release metadata app-global, not profile-scoped. Existing `seenFeatureIds` is profile-scoped and user-dismissable; it must not control the new three-version setting badge.
- Add one central registration step for every newly introduced setting/page; shared `SettingsNavigationRow` / `SettingsSwitchRow` rendering should read the registry so individual screens do not hardcode badge UI.



### Search and root discoverability follow-up — 2026-11-20
- **Search taxonomy is still legacy:** `SettingsSearchEntry` builds categories from Account / General / Enhanced / About / Advanced, rather than the proposed You / Look / Watch / Services / System taxonomy. Tracking search results are explicitly tagged Account. When the category model changes, update search metadata and labels in the same change; otherwise search will contradict the visible navigation.
- **Search omissions:** Advanced → Logs (View / Copy / Export) is present in `AdvancedSettingsPage` but has no corresponding search entries. Add a Logs result only if it opens the correct Advanced/Diagnostics destination; do not claim exact-control navigation until reveal/focus support exists.
- **Search target semantics:** `addRow` stores a page target for control-level matches. Its page/section context is descriptive only; clicking does not scroll to the matching row. Treat the current behavior as a page shortcut, and design exact-row reveal separately.
- **Root placement still reflects the old taxonomy:** Tracking appears inside the Account root section; Integrations appears inside General; Downloads manager and Watch Party are utility actions inside General. The revised root should separate You/Services/Look/Watch/System preferences from utility shortcuts. Downloads manager must remain distinct from the proposed Watch → Downloads preferences page.
- **Localization gap reconfirmed:** Advanced Logs titles, descriptions, status labels and dialog actions are hardcoded English. Include them in localization scope; also verify export completion/failure feedback, since the current row label changes only when the callback reports success.
- Classification: these are confirmed discoverability/taxonomy/localization gaps, not duplicate settings implementations. No application code changed.

### Final audit scope note
- Settings architecture and many major controls have been inspected, but the audit is **not complete** until every remaining dialog state, localization string, search entry, platform-specific control and persistence owner is mapped.
- No source implementation, build, tests or device verification has been performed.



## Playback controls currently misplaced in Nuvio Enhanced — 2026-11-20 08:09:44 IST

**Verified source ref:** `Main-dev` source baseline `a7f066297ba000833a981b12f8b57c26ce833f7d` (the intervening `46705168d8700b6c769e0ffbd534f0a078b80ce7` commit updated this audit document only).  
**Document write guard:** previous blob `573cfe5d961f9ccf0adb415c05f4199c7cf6c090`.  
**Files inspected:** `NuvioEnhancedSettingsPage.kt`, `SettingsSearch.kt`, `PlaybackSettingsPage.kt`, `PlayerSettingsRepository.kt`, `SettingsModels.kt`.

- **Android Memory Safe Buffer** is displayed inside Nuvio Enhanced → App Experience, but its state belongs to `PlayerSettingsRepository`; it is Android-only and disabled when external playback is active or Libmpv is selected. It is a Watch → Buffer / Playback Buffer candidate, not a generic Enhanced app-experience preference. Preserve the engine/player gates.
- **Random Next Episode** is displayed inside Nuvio Enhanced → Player Tools and persists through `PlayerSettingsRepository`; it is gated by `enhancedHomeFeaturesEnabled`. It belongs with Watch → Up Next / episode progression, not the generic Enhanced bucket.
- **Player Clock / End Time overlay** is displayed inside Nuvio Enhanced → Player Tools and uses `PlayerSettingsRepository`; it is a Watch → Player / Interface candidate.
- **Subtitle Sync Menu** is displayed inside Nuvio Enhanced and uses `PlayerSettingsRepository`; it is a Watch → Subtitles candidate.
- These are **MISPLACED SETTINGS / ADAPT**, not duplicate implementations. They are not currently duplicated as visible rows in `PlaybackSettingsPage.kt`. The already documented Parental Guide duplicate remains the sole playback duplicate in this group; do not add a second duplicate finding.
- Search metadata currently routes Random Next Episode to Nuvio Enhanced. The inspected search inventory did not show entries for Subtitle Sync Menu or Player Clock / End Time; verify the complete search inventory and desired inclusion before adding entries. The Memory Safe Buffer search entry is already present and points to Nuvio Enhanced. Update search targets/category metadata together with any approved page move.
- Nuvio Enhanced feature-highlighting/seen behavior is attached to these rows. Moving controls must preserve their current feature IDs and seen-state behavior unless a separately approved migration changes that contract. Do not reuse this profile-scoped seen state for the requested release-based NEW badge.

**Planning implication:** the final Watch map must include Buffer, Up Next, Player/Interface and Subtitles as destinations for these existing controls. Do not create new controls or duplicate repository state. Keep the existing `PlayerSettingsRepository` and storage keys; this is a navigation/ownership mapping, not a storage migration.

**Verification:** read-only source inspection; no source changes; builds/tests/device checks NOT RUN.



## Nuvio Enhanced settings inventory and consolidation map — 2026-11-20 08:10:17 IST

**Verified source ref:** application source baseline `a7f066297ba000833a981b12f8b57c26ce833f7d`; current branch HEAD before this doc write `33789b37f25d20e7d96d32457e2e803ff5fb1e19` (documentation-only commits since source baseline).  
**Document write guard:** previous blob `ff111df0d25c0d306ce0b8104d3493079f54f277`.  
**File inspected:** `NuvioEnhancedSettingsPage.kt`, including all section headings and visible switch/navigation row titles.

### Current inventory

The Nuvio Enhanced page contains 13 functional sections and at least 49 ordinary switch/navigation rows, plus dynamic provider lists, dialogs, platform-conditional actions and feature-policy gates. Its internal categories are `New`, `All`, `Core`, `Home`, `Player`, and `System`. This is a separate internal taxonomy layered on top of the outer Mew Settings categories; it should not remain a second disconnected settings destination after consolidation.

### First-pass destination mapping (provisional, not implementation instructions)

| Current section / control family | Proposed destination | Audit decision |
|---|---|---|
| Nuvio Experience: home features, quiet/home presentation, smart shelf | Look → Home | ADAPT; inspect each preference's effect and dependencies |
| Smart Resume / next-episode progression | Watch → Up Next / Continue Watching | ADAPT; preserve playback and catalog semantics |
| Continue Watching ready badge | Look → Home / Continue Watching | ADAPT |
| Profile Stats | You → Profile | ADAPT; verify profile ownership and visibility |
| Release Signals / Release Digest / Release Radar / Library-only Radar | Watch → Content Discovery (or a distinct discovery subpage) | NEEDS INVESTIGATION; inspect data source, user-facing surface and refresh behavior |
| Library Health | System → Diagnostics or Watch → Library | NEEDS INVESTIGATION; determine whether it is a diagnostic or library preference |
| Concierge | Services or Watch | NEEDS INVESTIGATION; inspect whether this is AI/service configuration or a viewing workflow |
| Status Bar | Look → Interface | ADAPT; preserve Android/iOS and system-bar behavior |
| Live TV | Watch → Live TV / Content Discovery | NEEDS INVESTIGATION; distinguish playback destination from service configuration |
| Player Tools: source pinning and background stream prefetch | Watch → Stream Selection / Sources | ADAPT; preserve provider ordering, network use and policy gates |
| Player status overlay / player clock | Watch → Player / Interface | ADAPT |
| Subtitle Sync Menu / subtitle selector and audio selector style | Watch → Subtitles / Audio | ADAPT; preserve current feature IDs and seen state |
| Random Next Episode / Next Episode Button | Watch → Up Next / Player Interface | ADAPT; distinguish episode-selection behavior from player control visibility |
| Memory Safe Buffer | Watch → Buffer | ADAPT; Android-only, external-player and engine gates already documented |
| Parental Guide | Watch → Player or Content Warnings | Keep one canonical control; existing TRUE DUPLICATE record remains authoritative |
| Hero Experience and Detail Presentation (hero style, video preview, overview, detail header, Nuvio Read, trailer layout, download button placement) | Look → Home / Details | ADAPT; separate appearance preferences from actual trailer playback behavior |
| Hero trailer playback and sound | Watch → Player / Preview Playback | ADAPT; preserve feature-policy and in-app playback capability checks |
| Hero/detail ratings presentation | Look → Details | ADAPT; verify this is presentation-only and not a tracking/service preference |
| Network: DNS-over-HTTPS provider | System → Network | ADAPT; preserve DNS-aware client routing and provider persistence |
| Backup / Import / Copy / Paste | System → Backup | ADAPT; preserve profile backup schema, validation and failure feedback |
| External download folder choose/change/remove | Watch → Downloads (preferences), not Downloads manager | ADAPT; Android-only today; iOS no-op/capability gap remains separately documented |
| Feature Highlights toggle | System → About / What's New | NEEDS INVESTIGATION; keep separate from the new release-version-based NEW badge |
| Diagnostics / last crash report | System → Diagnostics | ADAPT; preserve report availability policy and copy feedback |
| Community links/footer | About / Community utility, not a preference row | Keep as utility destination/action |

### Important consolidation constraints

- The `New` internal category is driven by `NuvioEnhancedSettingsRepository.isNew(...)` and `markFeatureSeen(...)`; it is profile-scoped/dismissible feature-highlighting behavior. Preserve those feature IDs and semantics during moves. It is not the user's approved three-release NEW badge.
- Current section headings are not reliable final category boundaries. Map each control by behavior, state owner, platform scope, policy and user workflow; do not move entire sections blindly.
- A number of Enhanced controls read/write repositories other than `NuvioEnhancedSettingsRepository` (including `PlayerSettingsRepository`, `HomeCatalogSettingsRepository`, `MetaScreenSettingsRepository`, and `DnsOverHttpsSettingsRepository`). The visible page is a mixed settings hub, not one coherent persistence domain. Preserve the existing owner; do not create duplicate state or migrate keys just to reorganize navigation.
- Keep utility links/actions (community links, backup file picker actions, crash-report copy) distinct from ordinary persisted preference rows.
- Search metadata must move with each setting; current search targets often point to the whole Nuvio Enhanced page, and exact-control reveal is not implemented.

**Status:** this is a first-pass inventory and destination proposal, not the final migration map. The concierge, library health, release/discovery and Live TV semantics still need caller/data-source review. No source changes; builds/tests/device checks NOT RUN.



## Account, Profile, Notifications and Advanced control audit — 2026-11-20 08:10:56 IST

**Verified source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`; branch HEAD before this doc write `b92c18405761c5a0d382e9d971238d7444dbd9d5`.  
**Document write guard:** `325cda25ab8c12d7b12b24c8294568768fcb69b3`.  
**Files inspected:** `AccountSettingsPage.kt`, `ProfileInsightsSettingsPage.kt`, `AdvancedSettingsPage.kt`, `NotificationsSettingsPage.kt`, `SettingsModels.kt`, `SettingsScreen.kt`.

- **Account:** authentication status/email, sign-out and optional account deletion are grouped in one page. Delete is gated by `AppFeaturePolicy.accountDeletionEnabled` and authenticated state. Sign-out and deletion use separate confirmation modals; deletion exposes a busy state, blocks dismissal while active, and reports failure inline. This is correctly mapped to You → Account; preserve destructive confirmation and policy behavior.
- **Profile Insights:** the large profile dashboard contains overview/taste insights, watch progress, library and favorite-person surfaces. It is not simply an account-preferences form. Keep it as a distinct You → Profile Insights destination; favorite people remains a child route. Preserve active-profile scoping and callbacks into details/continue-watching.
- **Advanced:** currently mixes startup preference (remember last profile), Sentry consent, runtime diagnostics/logs, and Continue Watching cache clear. Candidate split is System → Startup, Diagnostics and Storage/Cache. Do not merge the cache action with the Downloads manager or profile settings.
- **Notifications:** episode-release alerts preference and a separate test-notification action/card. The test action is enabled only when a valid target exists and sending/loading state permits; status/error/permission messages are shown. Keep it under System → Notifications and preserve permission/status feedback.
- **Theme contrast impact expanded:** Notifications' test button explicitly uses `MaterialTheme.colorScheme.primary` with `onPrimary`, so the previously documented low-contrast palette pairs affect this surface too. Add it to the same canonical central contrast fix/test list; do not create a separate per-screen color patch.
- **Logs localization finding is already canonical:** Advanced → Logs still has hardcoded English section/row/dialog text. Do not duplicate the finding; include its localization and export-feedback requirements in the existing Advanced Logs item.
- **Profile Insights and Account use distinct page semantics:** do not treat their repeated profile/account references or favorite-person links as duplicate settings controls.

**Classification:** Account page grouping = ALREADY LOGICAL; Profile Insights dashboard = INTENTIONAL PARALLEL destination from Account preferences; Advanced multi-domain page = ADAPT into System subpages; Notifications test action = utility action within Notifications, not a persisted preference; contrast issue = existing central theme defect with one additional confirmed consumer.

**Verification:** source reads only; no application source changes; builds/tests/device checks NOT RUN.



## Integrations, Tracking, Content Discovery and source-set audit — 2026-11-20 08:15:04 IST

**Target:** `Main-dev`  
**Verified branch HEAD before this documentation write:** `8f5820b4d03900f138946ae568493e428a35e303`  
**Application source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`; GitHub compare confirms the five intervening commits changed only `docs/UPSTREAM_PARITY_AUDIT.md`.  
**Previous document blob / write guard:** `54812ba9037a1be905cfe6a3be5b495bc0fc48b2`.  
**Files inspected:** `IntegrationsSettingsPage.kt`, `TrackingSettingsPage.kt`, `TrackingProviderCards.kt`, `ContentDiscoverySettingsPage.kt`, `TmdbSettingsPage.kt`, `MdbListSettingsPage.kt`, `DebridSettingsPage.kt`, `AiAssistantSettingsPage.kt`, `AddonsSettingsPage.kt`, Plugins/CloudStream expect/actual pages, `SettingsModels.kt`, `SettingsSearch.kt`, `SettingsScreen.kt`, and Trakt/TMDB/MDBList/Debrid/AI settings repositories/storage contracts.

### Integrations → Services mapping

- The existing Integrations root is a navigation-only hub with four distinct service pages: AI Assistant, TMDB Enrichment, MDBList Ratings, and Debrid. Map these to **Services → AI**, **Services → Metadata (TMDB/MDBList)**, and **Services → Debrid**. Do not collapse the service pages into one giant form.
- AI Assistant owns provider selection, enable/web-search switches, provider-specific API keys and model names through `AiAssistantSettingsRepository`. Its Android storage uses `ProfileScopedKey`; preserve per-profile values and key ownership. Web-search toggle is not equivalent to an app-wide search preference.
- TMDB owns API key, enable state, language/title-language, trailer/artwork/details/credits/episode/release/collection modules. `TmdbSettingsRepository` invalidates metadata and notifies Home on relevant changes. Keep the existing repository and invalidation behavior.
- MDBList owns a separate API key, enable state and independent rating-provider switches. API-key changes clear MDBList metadata cache. It is a separate service from TMDB, not a duplicate despite similar credential/module UI.
- Debrid owns provider credentials, resolver selection, cloud-library enablement, instant-playback preparation limits, stream result/sort/quality/HDR/Dolby Vision/codec filters and display templates. This is not merely a provider connection toggle; keep it as a dedicated Services destination or a clearly structured Debrid subpage group.
- TMDB, MDBList, Debrid and AI have separate repository/storage contracts and profile-change reload paths. Their similar credential controls are **SHARED PATTERN / DIFFERENT SEMANTICS**, not evidence for merging persistence or provider models. Android storage uses profile-scoped keys; platform actuals exist. Preserve sync payload contracts where present (TMDB/MDBList/Debrid).
- The existing TMDB enable switch also appears in Content Discovery and is the already-confirmed **TRUE DUPLICATE** recorded in the canonical duplicate inventory. Do not create a second duplicate finding. If consolidation removes the shortcut, keep the one canonical toggle and a navigation link to TMDB configuration; if both remain, they must be intentionally presented as one shared state/control, not two independent switches.

### Tracking → Services mapping

- Tracking page has provider connection cards for Trakt, Simkl, AniList and MyAnimeList; additional conditional Simkl manual-client-ID entry; data-source choices for Library and Watch Progress; Trakt-only viewing/discovery controls (continue-watching cap, comments, More Like This source); anime ID preference; and anime tracking presentation toggle.
- The four provider cards share visual/card patterns but have different authentication, sync and data semantics. Classify as **INTENTIONAL PARALLEL provider integrations** with **SHARED PATTERN / DIFFERENT SEMANTICS** presentation—not duplicate provider implementations. Preserve each provider's independent auth, sync, error and loading states.
- Tracking data-source controls use connected-provider availability and effective fallback calculations. Selecting Watch Progress calls `WatchProgressSourceCoordinator.selectSource(profileId = ProfileRepository.activeProfileId, ...)`; refresh failures have an inline retry action. Preserve active-profile scoping, fallback messaging and asynchronous refresh lifecycle.
- Trakt viewing/discovery options are only shown while Trakt is connected. Simkl manual client ID is conditionally shown when credentials are not configured or a manual ID exists. Anime ID options are shown when Simkl is connected. These are intentional visibility gates; moving rows must preserve them.
- Anime pencil visibility is stored through `MetaScreenSettingsRepository`, even though its UI currently appears under Tracking. It controls a Meta Screen presentation affordance, not provider authentication. **ADAPT** it to Look → Details / Meta Screen, while keeping the existing repository/key and avoiding a second toggle.
- `TraktSettingsRepository` persists a combined settings payload and reloads on profile change; Android/iOS storage implementations use `ProfileScopedKey`. Keep the route enum name `TraktAuthentication` for saved-navigation compatibility, but its new category should be Services → Tracking. The earlier category mismatch finding remains canonical.
- Search currently gives Tracking a legacy Account category/root page placement. Update search category, root placement and the same route's category together when implementation is approved; do not rename the enum route or invalidate saved navigation.

### Addons, Plugins and CloudStream source-set findings

- `AddonsSettingsPage.kt` delegates to `AddonsSettingsPageContent`; it is an adapter, not a second independent Addons implementation. Audit the delegated content owner before editing its controls.
- Plugins and CloudStream settings use common expect declarations with actuals in `fullCommonMain`, `androidPlaystore` and `iosAppStore`. Full source sets delegate to the plugin settings content / CloudStream section; Play Store and iOS App Store actuals are no-op. These are **INTENTIONAL PARALLEL / PLATFORM-FLAVOR IMPLEMENTATIONS**, not duplicate screens.
- Both entries are gated by `AppFeaturePolicy.pluginsEnabled`; search receives the same policy input. When policy disables the route, SettingsScreen falls back from Plugins/CloudStream page rendering to Addons content as a compatibility shim. Preserve the policy and fallback semantics or replace them with an explicit safe redirect only after direct-route/saved-state tests.
- CloudStream entry is also disabled when TMDB enrichment is enabled in the Content Discovery page. Preserve and explain this interaction; do not silently remove it while reorganizing categories.
- Content Discovery is a source/addon hub, not the same as the Integrations hub. Proposed placement: Watch → Content Discovery, with Addons and policy-permitted Plugins/CloudStream as children. TMDB configuration belongs under Services, with only one canonical enable control.

### Consolidation decisions

| Existing control/page | Proposed destination | Decision |
|---|---|---|
| AI Assistant | Services → AI | ADAPT; preserve provider keys/models and profile scope |
| TMDB | Services → Metadata → TMDB | ADAPT; keep one enable control; preserve invalidation |
| MDBList | Services → Metadata → MDBList | ADAPT; distinct provider/cache |
| Debrid | Services → Debrid | ADAPT; retain resolver/stream preferences and sync schema |
| Trakt / Simkl / AniList / MyAnimeList | Services → Tracking | ADAPT; independent providers, shared card pattern |
| Tracking library/watch-progress source | Services → Tracking → Data Sources | ADAPT; preserve coordinator/fallback behavior |
| Anime pencil toggle | Look → Details / Meta Screen | ADAPT; existing MetaScreenSettingsRepository owner |
| Addons | Watch → Content Discovery → Addons | ADAPT; inspect delegated content owner |
| Plugins / CloudStream | Watch → Content Discovery | ADAPT; preserve source-set actuals, policy gate and fallback |
| TMDB enable switch on Content Discovery | Services → Metadata → TMDB | Existing TRUE DUPLICATE; do not add another duplicate record |

**Verification:** source reads only; source diff since baseline is documentation-only; no app source changes; builds/tests/device checks NOT RUN. iOS actual files were located, but no iOS simulator/device behavior was verified.



## Appearance / Look controls and selection-sheet audit — 2026-11-20 08:16:09 IST

**Target:** `Main-dev`  
**Verified branch HEAD before write:** `9d5959ebaf7b8648ac05df8313586c2fdf886c09`  
**Application source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`; source compare remains documentation-only.  
**Previous document blob / write guard:** `ef9c739e0dce387891b3352c66dddef51a16c7c0`.  
**Files inspected:** `AppearanceSettingsPage.kt`, `ThemeSettingsRepository` call sites, `SettingsModels.kt`, plus navigation targets `HomescreenSettingsPage.kt`, `StreamsSettingsPage.kt`, `PosterCustomizationSettingsPage.kt`, `MetaScreenSettingsPage.kt`.

### Appearance page breakdown

- **Theme:** classic and enhanced palette selection plus custom theme with two persisted custom accent colors. Theme selection uses `ThemeSettingsRepository`; custom first/second colors are independently persisted. Preserve the two-color model and existing theme IDs; upstream's single/gradient editor is not a drop-in replacement. Existing theme contrast risk remains a central ThemeColors/Material color-scheme issue, not a reason to add per-screen overrides.
- **Display:** AMOLED black; native Liquid Glass tab-bar enablement and auto-hide; app icon; app language; Android-only navigation-bar style.
- Liquid Glass rows are hidden unless `liquidGlassNativeTabBarSupported`; auto-hide is disabled unless Liquid Glass is enabled. These are intentional capability/dependency gates, not missing controls.
- App icon selection is disabled while an icon change is pending; opening the picker first clears the prior failure notice, and dismissal clears failure UI. Preserve pending/error behavior.
- App language and navigation-bar style sheets apply the chosen value immediately, show a selected check with `cd_selected` accessibility text, and dismiss after selection. Both use `NuvioModalBottomSheet` and a scrollable `LazyColumn`.
- Navigation-bar style is intentionally hidden on iOS. Do not treat this as a parity defect.
- **Home / layout links:** Homescreen, Collections, Continue Watching, Poster Customization, Streams, and Meta Screen are navigation destinations, not settings duplicated in Appearance. Place them under Look → Layout (with distinct subpages) rather than retaining them as a second copy of controls on the Appearance page.
- **Custom color editor:** preset swatches expose color names through semantics; selected swatches show a check. Saturation/value and hue pickers update colors live; hex input accepts up to seven characters and applies only parseable colors. Keyboard/focus and screen-reader operation of the custom picker still need accessibility verification; source inspection alone does not establish usability.

### Dialog / component classifications

- Language and navigation-style sheets are **SHARED PATTERN / DIFFERENT SEMANTICS**: common sheet/dismissal/selected-check pattern, different option sets and state. Reuse existing sheet primitives; do not add another picker abstraction.
- Theme chips and custom accent controls are part of the same ThemeSettingsRepository domain, not duplicate theme persistence.
- iOS hiding of navigation-bar style and capability-gated Liquid Glass are **INTENTIONAL PLATFORM/CAPABILITY SCOPE**.
- No new TRUE DUPLICATE confirmed in this Appearance pass.

**Planning note:** final Look hierarchy should distinguish Theme, Interface/Display, Home, Layout and Details. Keep theme editing centralized, move navigation destinations into subpages, and preserve current repository fields. Do not copy upstream theme persistence or collapse custom accent colors without a separately approved migration.

**Verification:** source-level review only; no app source changes; no accessibility/device checks or builds/tests run.



## Shared settings-row accessibility and picker audit — 2026-11-20 08:17:03 IST

**Target:** `Main-dev`  
**Verified branch HEAD before write:** `2802152e499bc40dd66982317d4ed41c65276498`  
**Application source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d` (source changes since baseline remain none).  
**Previous document blob / write guard:** `df64786d3f94de8facb8f7d5a47fd05846f64397`.  
**Files inspected:** `SettingsComponents.kt`, `TrackingAdaptivePicker.kt`, `TrackingSettingsPage.kt`, `AppearanceSettingsPage.kt`.

### Shared row semantics — NEEDS ACCESSIBILITY VERIFICATION

- `SettingsSwitchRow` uses a clickable parent row to invert `checked`; the visible switch is a custom-drawn `Box` track/thumb. The inspected implementation does not explicitly attach `Role.Switch`, `toggleable` semantics, or state description to the row/visual switch.
- `SettingsNavigationRow` uses `clickable` and a custom focus indicator; no explicit role is set in the inspected implementation.
- This is a code-level accessibility review finding, **not yet a confirmed screen-reader defect**. Verify TalkBack, VoiceOver and keyboard focus on Android/iOS before choosing the fix. Do not add semantics blindly if the Compose clickable semantics already produce the intended role/state in the supported versions.
- Selected Tracking picker options are represented by a trailing check icon passed into `SettingsNavigationRow`; the generic row does not accept an explicit selected parameter. Confirm whether selected state is announced to screen readers. If not, add selected semantics to the picker option row rather than changing every navigation row.

### Tracking adaptive picker

- One shared `TrackingAdaptivePicker` chooses tablet `BasicAlertDialog` vs mobile `NuvioModalBottomSheet`; both render the same title/subtitle/options content. This is an **INTENTIONAL RESPONSIVE PARALLEL**, not duplicate picker logic.
- Option lists support disabled entries and explanatory unavailable reasons; content is scroll-limited to 520dp. Selection applies immediately and dismisses; tablet also exposes Close. Preserve this behavior and ensure long translated labels/options remain scrollable.
- The picker uses shared Nuvio dialog tokens on tablet and shared bottom-sheet primitives on mobile. It is a useful reference pattern for other settings pickers; do not create a second adaptive picker abstraction.

**Verification:** static source inspection only; TalkBack/VoiceOver, keyboard/focus, device and simulator checks NOT RUN. No source changes.


## Addons / Plugins dialog findings — 2026-11-20 08:18:04 IST

**Branch:** `Main-dev`; verified HEAD `9dec2c844102258484e7bff84000cfe03ad78e90`.  
**Source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d` (compare confirms only this MD changed since baseline).  
**Write guard:** prior doc blob `a61400c65238ee6fbb96acb6ff6bd661bb509e84`.  
**Inspected:** `AddonsScreen.kt`, `PluginsSettingsScreen.kt`, `PluginSettingsDialog.kt`, `CloudStreamSettingsSection.kt`, `Components.kt`.

- Addons settings route and standalone Addons screen both render the same `AddonsSettingsPageContent` owner. This is a shared entry point, not duplicate UI logic. It owns add/install, overview, refresh, enable/disable, ordering, configure URL, delete and version-change dismissal.
- Addon install uses one localized `NuvioStatusModal` for Checking/Success/Error; busy state prevents dismissal. Preserve repository validation and per-addon lifecycle.
- **CONFIRMED CREDENTIAL-VISIBILITY DEFECT (canonical finding; two occurrences):** (1) `PluginSettingsDialog` reads manifest `isPassword` but never uses it; `NuvioInputField` has no password transformation and renders a plain `OutlinedTextField`, so fields marked `isPassword: true` appear as visible text. (2) `DebridApiKeyDialog` also renders provider API keys through a plain `OutlinedTextField` with no password transformation/reveal control. This differs from TMDB, MDBList and AI pages, which use `SettingsSecretTextField` with `PasswordVisualTransformation` and an explicit reveal/hide affordance. A future approved fix should reuse/extend that secure field pattern, preserve stored values/JSON keys and avoid logging secrets. Do not create separate duplicate issue records for each occurrence.
- **CONFIRMED LOCALIZATION GAP:** plugin dialog title suffix `Settings`, fallback `Select option`, and `Cancel` / `Save` are hardcoded English. Static copy should use localized resources; manifest labels/descriptions remain dynamic external content.
- Plugin dialog uses Material `Dialog` + `Card` and Material primary/secondary colors rather than shared Nuvio dialog tokens. Classify as **SHARED PATTERN / DIFFERENT SEMANTICS with visual drift**; adapt the surface without changing schema-driven fields or save/cancel behavior.
- Malformed `layoutJson` and stored JSON fall back to empty arrays/objects. This avoids a crash but can silently show an empty form; mark **NEEDS INVESTIGATION** before deciding whether visible error feedback is compatible.
- CloudStream is a separate repository/catalog management workflow (repository add/edit, search/filter, install/update/bulk install, pinning, auto-updates and security-warning acceptance). Keep it distinct from Addons and Plugins; these are **INTENTIONAL PARALLEL ecosystems** with different models/lifecycles.
- CloudStream's security-warning acceptance must remain a gate. App-store source-set actuals remain no-op as previously documented.

**Verification:** source inspection only; no source changes, builds, tests, credential runtime tests or device checks. This adds no duplicate record.


## Debrid page subcategory split — 2026-11-20 08:18:51 IST

**Branch:** `Main-dev`; verified HEAD `fac907367a8f6118adee7feab69e4a495490c0d7`.  
**Source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`.  
**Previous document blob / write guard:** `945744b954a7b3a6f54893c9f602a1d4673f9556`.  
**File inspected:** `DebridSettingsPage.kt` (124 referenced localized resources) and `DebridSettingsRepository.kt`.

- Current Debrid page combines service setup and stream-selection behavior in one destination.
- **Services → Debrid:** provider credentials/device authorization, enable state, resolver selection, cloud-library enablement.
- **Watch → Stream Selection:** instant-playback preparation and stream count; result management (max results, sorting, per-resolution/per-quality limits, size range); advanced matching rules (preferred/required/excluded resolutions, quality, visual/audio tags, channels, encodes, languages and release groups); name/description templates.
- These are not duplicate settings. They share `DebridSettingsRepository` and `DebridSettingsStorage`; split navigation/UI only and preserve all existing keys, profile scope, sync payload fields, provider authorization lifecycle and formatting defaults.
- Keep the existing template reset dialog and learn-more external link attached to the formatting group. Validate reset confirmation/copy and long rule lists during the dialog pass.

**Decision:** ADAPT into Services → Debrid and Watch → Stream Selection subgroups; no repository split or second copy of stream preferences. **Verification:** source inspection only; no code changes/builds/tests/device checks.


## Debrid result-picker dialog copy mismatch — 2026-11-20 08:19:39 IST

**Branch:** `Main-dev`; verified HEAD `a0202e7d13bb2c2aaea65cc08e3267c0d5ced6cd`.  
**Source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`.  
**Previous document blob / write guard:** `f4a79358ca91a273ad46039d5d60aa9d1245ce86`.  
**File inspected:** `DebridSettingsPage.kt`.

- Result Management exposes distinct rows/resources for Max Results, Per Resolution Limit and Per Quality Limit.
- In `DebridStreamPreferenceDialog`, all three picker branches currently pass `settings_debrid_max_results` as the dialog title. Therefore the Per Resolution Limit and Per Quality Limit rows open a dialog titled “Max Results” despite representing different values.
- **CONFIRMED COPY/CONTEXT BUG:** each picker should use its matching localized title resource. No state or storage behavior should change.
- The page has a local shared `DebridDialogSurface` and shared option-row helpers used by its picker/device-auth/template dialogs. This is a page-local shared pattern, not a duplicate of the global settings dialog.
- Dialog count: seven `BasicAlertDialog` call sites across choice, text-list, template, prepare-count, device-auth and API-key workflows. A full per-dialog action/dismissal/error pass remains open.

**Decision:** ADAPT dialog titles to the matching existing resources during approved implementation. **Verification:** source inspection only; no source changes/builds/tests/device checks.


### Credential visibility cross-check — 2026-11-20 08:20:32 IST

The existing canonical credential-visibility finding above now includes the Debrid API-key dialog as a second confirmed occurrence. Comparison files: `DebridSettingsPage.kt`, `SettingsSecretTextField.kt`, `TmdbSettingsPage.kt`, `MdbListSettingsPage.kt`, `AiAssistantSettingsPage.kt`. TMDB/MDBList/AI already use masked input with a reveal/hide action; Debrid and manifest-defined plugin secrets do not. This is one shared behavior-class finding with two affected surfaces, not two independent duplicate records. Source inspection only; no source changes or runtime secret-display tests.


## Mew Enhanced caller-level audit — Concierge, Release Radar, Library Health and Live TV — 2026-11-20 10:19:00 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `961d59323452e16a9c403ae3f076b996d6546a5d`  
**Application source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d` (all changes since baseline remain documentation-only).  
**Previous document blob / write guard:** `7dc3a42d6e9878597326e1a1ad2c690feb843fbf`.  
**Upstream enhanced ref checked:** `luqmanfadlli/NuvioMobile-Enhanced` branch `enhanced`, `b23b28a878b78f5ef9252128b901155fee6eeea9`.  
**Files inspected:** `NuvioEnhancedSettingsPage.kt`, `NuvioEnhancedSettingsRepository.kt`, `HomePremiumSections.kt`, `LiveTvScreen.kt`, `HomescreenSettingsPage.kt`.

### Concierge and home experience

- `nuvioConciergeEnabled` is a Home experience feature gate, not an external service connection or independent destination. It gates Concierge-dependent Smart Resume, Release Signals and Profile Stats rows in the Enhanced settings page. The actual UI surface is `HomeConciergeSection` in `HomePremiumSections.kt`. **Decision: ADAPT** its controls into Look → Home Experience (or Home → Concierge subsection); do not put it under Services.
- `smartResumeEnabled`, `releaseRadarHomeSignalsEnabled` and `profileStatsEnabled` are dependent Home experience options. Keep their enablement relationship to `enhancedHomeFeaturesEnabled && nuvioConciergeEnabled`; do not show them as independent service integrations.
- `showContinueWatchingReadyBadge` is a Continue Watching presentation preference and belongs with Watch → Continue Watching, not Concierge configuration. It shares the Enhanced settings repository only because the current Enhanced page aggregates unrelated settings; this is **SHARED REPOSITORY / DIFFERENT SEMANTICS**, not duplicate state.
- `smartShelvesEnabled` and `quietHomeModeEnabled` change Home composition/visibility. Place them under Look → Home Experience. Preserve the quiet-home dependency that disables Smart Shelves while quiet mode is enabled.
- `releaseRadarDigestEnabled` controls digest presentation within the Home Release Radar surface (`HomeReleaseRadarSection(showDigest)`). It is not a separate notification subscription or standalone destination. Place under Look → Home Experience → Release Radar; retain its dependency on enhanced-home master enablement.
- `libraryHealthEnabled` is exposed as a Premium Labs switch, but its runtime owner is confirmed: `LibraryEnhancedSections.kt` builds `LibraryHealthReport` from library items and Release Radar signals, then renders `LibraryHealthCard` only when `enhancedHomeFeaturesEnabled && libraryHealthEnabled`. **Decision: ADAPT to Watch → Library → Library Health** (or a Library presentation subsection), not System → Diagnostics and not Services. Preserve its current dependency on the enhanced-home master flag.
- `releaseRadarLibraryOnly`, `releaseRadarWindowDays` and `releaseRadarContentFilter` are filters for the same Home Release Radar feed; the source page groups them under Release Radar. Place under Look → Home Experience → Release Radar, not general Content Discovery settings. Preserve allowed values, clamp (7..45 days), and category semantics.
- The Release Radar digest counts (today/week/profile) are computed from the same list in `HomePremiumSections.kt`; do not create another digest model/page while reorganizing settings.

### Live TV

- `liveTvEnabled` is a feature-visibility/availability switch under current Enhanced App Experience settings. Actual Live TV workflow lives in `LiveTvScreen.kt` and manages M3U, Stalker and Xtream sources, playlists, EPG, favorites and channel playback.
- **Decision: ADAPT** the visibility switch to Watch → Live TV (or Watch → Content Sources → Live TV) near the destination it gates. Keep source credentials and playlist configuration within the Live TV screen workflow; do not create a second settings page duplicating those controls.
- Live TV is an intentional distinct viewing/source workflow, not an Integrations provider card. Preserve profile-scoped storage and platform-specific file-picker/EPG implementations.

### Duplicate and persistence guard

- `NuvioEnhancedSettingsRepository` currently aggregates feature flags alongside `PlayerSettingsRepository`, `MetaScreenSettingsRepository` and other domain owners. Existing setter calls show several controls already write to those domain repositories (e.g. parental guide and detail-hero settings); preserve the domain owner rather than moving persistence into a new consolidated settings repository.
- No new TRUE DUPLICATE was established in this pass. The Continue Watching ready badge and Home Concierge controls are separate semantics even though their controls share one source page. The Release Radar digest and filters share one feed but represent presentation vs filtering options, not duplicates.
- The prior proposed destinations for Concierge, Release Digest and Live TV are refined by these caller findings. Library Health remains open until its runtime caller is identified.

**Verification:** static source inspection only; no source changes, builds/tests, Android device or iOS simulator checks. Upstream tree/ref was verified, but upstream file-content comparison for this specific settings page was unavailable in the current fetch path; no claim of full upstream semantic diff is made.


### Library caller verification refinement — 2026-11-20 10:22:00 IST

**Additional files inspected:** `LibraryEnhancedSections.kt`, `HomePremiumModels.kt`, `HomePremiumSections.kt`.

- Confirmed Library Health is a Library-screen presentation card, not an OS/app diagnostic. `LibraryEnhancedSections.kt` computes its report from current library items, filtered Release Radar signals and the current date; the card is gated by enhanced-home master enablement plus `libraryHealthEnabled`.
- The same Library enhanced section renders `HomeReleaseRadarSection` and passes `releaseRadarDigestEnabled` as `showDigest`. Thus Release Radar filters and digest directly affect the Library surface. Concierge also consumes Release Radar-derived items on Home; this is shared underlying data with distinct presentations, not duplicate UI.
- Refined placement: **Watch → Library** owns Library Health and Release Radar presentation/filter controls; **Look → Home Experience → Concierge** owns Concierge, Smart Resume, Profile Stats, Smart Shelves and Quiet Home behavior. Keep a single persisted value for each setting even where the data feeds more than one screen.
- `showContinueWatchingReadyBadge` remains Watch → Continue Watching. `liveTvEnabled` remains Watch → Live TV.
- **Classification:** Library Health card vs system diagnostics = FALSE POSITIVE; Home Concierge vs Library Release Radar = SHARED DATA / DIFFERENT PRESENTATIONS; no new duplicate settings identified.

**Verification:** static source inspection only; no source changes, builds/tests or device checks. This refinement supersedes the earlier NEEDS INVESTIGATION note for Library Health.


## Settings search and navigation re-verification — 2026-11-20 12:08:00 IST

**Target:** `Main-dev`  
**Verified branch HEAD before write:** `e04529427007fbd8aee0378f5ee47b26fab66c83`  
**Previous audit document blob / write guard:** `c14b1a610265d0d4cb08f4c3eb968c53865629ff`  
**Application source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`; compare reports 17 commits ahead, 0 behind, with exactly one changed path: `docs/UPSTREAM_PARITY_AUDIT.md`. No application source changed since this baseline.  
**Live refs rechecked:** `main` = `207ef8fa309b8061b73039b2d4e4bde4af65050e` (untouched); `upstream-sync` = `af51bbbc13cdb315a88ef597eb4d2672a60335d5`; mother upstream `enhanced` = `375d93d90864d6b0774d8ab6f58c72d63d10d2af`. Upstream advanced from the prior audit anchor; its new delta has NOT yet been compared.

**Files re-read:** `SettingsSearch.kt`, `SettingsModels.kt`, `SettingsScreen.kt`, plus the full `docs/UPSTREAM_PARITY_AUDIT.md`.

### Verified search behavior and category mapping

- `SettingsSearchTarget` still supports page targets plus Downloads, Collections, Switch Profile and Check for Updates. There is no stable control-ID/scroll target. `addRow()` deliberately creates `SettingsSearchTarget.Page(page)`; a row-level search result therefore opens its owning page but does not reveal/focus the matched control. This confirms the existing canonical search discoverability finding; **no duplicate finding added**.
- Search category labels are still sourced from the legacy `SettingsCategory.Account/General/Enhanced/About/Advanced` enum. Tracking search entries are explicitly categorized as Account, while the page model also assigns `SettingsPage.TraktAuthentication` to Account. This confirms the existing Tracking → Services mismatch in both navigation metadata and search metadata.
- Search page/row entries for Plugins and CloudStream are conditional on `pluginsEnabled`; the runtime route also checks `isEnabledByPolicy()` and falls back to Addons content when disabled. Preserve this policy and compatibility behavior when changing categories or adding granular Playback pages.
- The root search has dedicated utility targets for Downloads, Collections, profile switching and update checks. These are distinct from preference-page targets and must remain so during taxonomy changes.
- The TMDB duplicate remains visible in the current search/control map: Content Discovery exposes the same enable state as the canonical TMDB page. Retain the already-recorded single canonical duplicate entry.

### Verified navigation/restoration risk

- Tablet navigation stores `selectedCategory` using `rememberSaveable` and immediately calls `SettingsCategory.valueOf(selectedCategory)`; it has no safe fallback for removed/renamed category strings. Existing names are Account, General, Enhanced, About and Advanced. This confirms the existing migration blocker: implement a legacy-name parser (including Enhanced) and tests before replacing category values.
- `SettingsPage.previousPage()` is exactly the static `parentPage`. A contextual shortcut to TMDB from Content Discovery cannot return to its origin while the page parent remains Integrations. The existing route mismatch is therefore confirmed at the navigation-model level; any fix must preserve direct links and saved enum names.
- Tablet inline navigation sets the selected category from `page.category`; a future page split must assign category and parent coherently, or the sidebar and Back header can disagree.

### Classification / decisions

- Search row → containing page: **CURRENT LIMITATION / ALREADY DOCUMENTED**, not a broken route and not an upstream parity gap (upstream also uses page targets).
- Tracking in Account: **CONFIRMED TAXONOMY MISMATCH / ADAPT** to Services while retaining the `TraktAuthentication` route name.
- Plugins/CloudStream no-op or Addons fallback across policy/source sets: **COMPATIBILITY SHIM / INTENTIONAL PLATFORM-FLAVOR IMPLEMENTATION**; preserve.
- Downloads manager utility target vs future Downloads preferences page: **SHARED LABEL / DIFFERENT SEMANTICS**; do not merge.
- Category restore via `valueOf`: **CONFIRMED COMPATIBILITY RISK**; safe legacy mapping is a precondition to category migration.

**Verification:** GitHub source/document reads and commit comparison only. No application source changes, builds, tests, TalkBack/VoiceOver, Android device or iOS simulator checks. Upstream branch head was refreshed, but its delta audit remains open.


## Fresh Enhanced delta — Home hero source selection and pending-state continuity — 2026-11-20 12:16:00 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `337bb5c2b0d792155e1fc1fee7f3176735a7530f`  
**Previous audit document blob / write guard:** `296d73907eb00f406ebb988beda28402b704ac38`  
**Mew source files inspected:** `HomeCatalogSettingsRepository.kt`, `HomeRepository.kt`.  
**Upstream range:** `b23b28a878b78f5ef9252128b901155fee6eeea9..375d93d90864d6b0774d8ab6f58c72d63d10d2af` (2 commits; compare lists exactly these two Home files).

### Upstream commits and semantic comparison

- `f326d11ecf88597597f2349ca384369b84169951` — `fix(home): pick default hero sources from the catalog order until the user chooses`.
- `375d93d90864d6b0774d8ab6f58c72d63d10d2af` — `fix(home): replace startup hero items that do not come from the chosen hero sources`.

**1) Explicit user choice marker — ADAPT**

- Upstream adds nullable `heroSourcesChosen` to its stored Home catalog payload and a runtime boolean. It is set on the first explicit hero-source change, reset on profile change/reset, and inferred for old payloads from whether any non-collection hero source was already enabled.
- Before an explicit choice, upstream chooses up to two enabled catalogs in catalog order; if none are enabled, it falls back to the first catalogs. After an explicit choice, it preserves that choice and does not automatically select newly appearing catalogs.
- Mew's `normalizePreferences()` currently defaults missing catalog preferences to `heroSourceEnabled = true` until the two-source limit is reached. It does not store an explicit-choice marker. Consequently, after a user deliberately disables all selected hero sources, a newly discovered catalog can be auto-selected during normalization. The current default also chooses from catalog order without first preferring enabled catalogs.
- **Decision: ADAPT** the explicit-choice state into Mew's existing `HomeCatalogSettingsRepository` payload and lifecycle. Preserve Mew's existing profile-scoped storage, legacy list/object payload decoding, catalog ordering, two-source limit, remote sync contract and existing reset semantics. The marker must be backward-compatible and must not be copied as a new sync field without proving cross-device semantics. Verify how old payloads with zero selected hero sources should be interpreted before finalizing migration behavior.

**2) Pending hero continuity / chosen-source correctness — ADAPT**

- Upstream passes `keepVisibleHeroWhilePending` and `heroSourcesResolved` into its publish path. While hero-source resolution is pending, it can retain the visible hero only when those items still belong to the selected source set (or no replacement candidates have arrived); once sources resolve it publishes from the chosen set.
- Mew's `HomeRepository.publishCurrentState(isLoading, requestKey)` currently derives hero items directly from cached selected catalog sections and immediately publishes the result. It has no equivalent `heroSourcesResolved` or selected-source membership guard. Mew also has a distinct CloudStream fallback and collection-hero fallback; those must remain intact.
- **Decision: ADAPT after focused lifecycle review.** Preserve Mew's CloudStream fallback, collection fallback, localized-artwork cache, request-generation guards and current source refresh behavior. Do not copy upstream's full HomeRepository implementation. Add tests for source change while loading, stale hero from a removed source, no available selected-source items, and fallback order.

### Duplicate / ownership classification

- `HomeCatalogSettingsRepository` and `HomeRepository` remain the existing canonical Mew owners. This is **ALREADY IMPLEMENTED BASE + REQUIRED ADAPTATION**, not a reason to add a parallel repository or settings control.
- The upstream marker is a persistence-compatibility refinement, not a duplicate setting.
- The pending-state guard is a shared Home rendering concern with Mew-specific fallback semantics; classify as **SHARED PATTERN / DIFFERENT SEMANTICS** and adapt behavior, not source files wholesale.

**Verification:** upstream commit range and exact changed paths verified; Mew/upstream source bodies compared. No application source changes. Tests/build/device behavior NOT RUN. This candidate remains an audit/plan item only; implementation still requires the approved settings plan and explicit user authorization.


## Debrid dialog inventory count reconciliation — 2026-11-20 12:20:00 IST

**Target:** `Main-dev`; verified HEAD before write `8b991059e6d2fd67d37757ab07dbf8832e1145d3`.  
**Previous audit document blob / write guard:** `27857d3f6325329eb82c42dde77852eab2d20ae8`.  
**Source:** `DebridSettingsPage.kt`, current blob `30776a3b384f5cb888817cb5f8579bae033f6431`.

- Recounted actual `BasicAlertDialog` call sites: **7**, at lines 619, 705, 1076, 1110, 1163, 1572 and 1774.
- Their roles are: prepare-count selection; multiline template/text editor; single-choice picker; multi-choice picker; release-group text-list editor; device authorization/connect/disconnect; API-key entry/validation.
- This corrects the later inventory sentence that said Debrid had eight `BasicAlertDialog` calls. The earlier seven-call count was correct. The total dialog inventory remains a partial call-site count, not a complete state/copy/accessibility audit.
- The API-key entry remains part of the existing canonical credential-visibility defect; do not add another issue. The wrong Max Results title remains the separate confirmed copy bug.
- Source read only; no app source changes, tests, builds or device checks.


## Debrid dialog behavior and dismissal audit — 2026-11-20 13:05:51 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `902e546df95344fb0ead0c65f69c3ee1dbf63f4d`  
**Previous audit document blob / write guard:** `9d61fa83395381b98df272173cca7ac524de6a13`  
**Source file / blob:** `DebridSettingsPage.kt` / `30776a3b384f5cb888817cb5f8579bae033f6431`.  
**Reviewed:** all seven Debrid dialog call sites; detailed template, picker, multi-choice, release-group, device authorization and API-key flows.

### Confirmed interaction contracts (preserve)

- **Single-choice / integer pickers:** selection commits immediately and dismisses. No separate Save is expected; do not convert these to draft/Save dialogs without a product reason.
- **Multi-choice pickers:** selections are staged in a local draft; Save commits; Clear empties the draft but does not persist until Save; outside/back dismissal discards draft. Preferred lists intentionally normalize an empty saved list back to their default order, while required/excluded lists can remain empty. Preserve this per-field difference.
- **Release-group editor:** edits a draft; Clear empties it; Save splits on newline/comma, trims whitespace, removes blanks and deduplicates. Dismissal without Save discards edits.
- **Template editor:** Reset changes only the draft to the default; Save persists it; Cancel/outside/back discards the draft. Reset is not an immediate destructive action.
- **Prepare-stream count:** option selection is immediate; warning text is shown in the dialog. No new issue confirmed in this pass.
- **API-key editor:** Save validates the trimmed value before persistence; failed/blank validation shows a localized generic invalid-key message; Clear explicitly stores an empty credential and dismisses. Cancel/outside/back dismisses without saving the draft. The field is still plain text (already part of the canonical credential-visibility finding).
- **Device authorization:** the pending session is persisted and reused on reopening; dismissal cancels the active composable polling effect but does not clear the pending session. Success/expiry/failure/unsupported/retry/disconnect clear the pending session as appropriate. This supports resumable device auth, but the visible Cancel label only closes the dialog; it does not cancel the pending authorization.

### Copy / UX follow-up

- **NEEDS COPY REVIEW:** the device authorization dialog's Cancel button preserves a pending session and polling can resume on reopen. Consider “Close” / “Continue later” wording or an explicit cancel-authorization action if true cancellation is desired. Do not clear the session silently; preserve current resumability unless a deliberate behavior change is approved.
- No new state-loss or validation defect confirmed in the inspected picker/template flows.
- Existing API-key masking and Max Results title findings remain canonical; not duplicated here.
- Accessibility semantics (checkbox/radio state, focus return, keyboard behavior, touch target and screen-reader announcement) remain **NOT VERIFIED** without platform runtime testing.

**Verification:** GitHub source reads only; no application source changes, tests, builds, TalkBack/VoiceOver or device checks. This is a behavior-contract audit, not an implementation.


## Playback dialog commit, validation and dismissal audit — 2026-11-20 13:05:51 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `d6da9153b95e217ec65542fcfacdef701d9862ee`  
**Previous audit document blob / write guard:** `dd473260ab96fe7f4b2cdfb80f0fce142546987a`  
**Files inspected:** `PlaybackSettingsPage.kt` (20 dialog call sites; detailed review of provider allowlists, regex editor, IntroDB key and threshold-mode picker); `SkipIntroRepository.kt`; `SkipIntroApi.kt`.

### Verified dialog contracts

- Provider allowlist selection is a staged draft, but **every dismissal path (outside tap, Back, or dialog close)** calls `onSelectionSaved` before dismissing. The UI explicitly displays “Save & close”; there is no Cancel action. Preserve this auto-save-on-close contract unless product UX is intentionally changed. Empty selection means “All”; selected names are intersected with currently available items before showing the dialog, preventing stale provider names from being selected.
- Regex editor stages changes; presets only update the draft; Clear empties the draft; Save trims and validates regex syntax before invoking the parent save callback. Invalid syntax keeps the dialog open with a localized error. Empty regex is accepted and saved as no custom filter. Dismissal without Save discards the draft. Runtime matching semantics and catastrophic-regex/performance behavior were not tested.
- IntroDB API key uses the shared `SettingsSecretTextField` (masked/reveal pattern), unlike the already-recorded unmasked Debrid/plugin credential surfaces. Save of a new non-empty key is blocked while `SkipIntroApi.verifyIntroDbApiKey` checks it; the API helper catches request failures and returns false. Cancel/outside/Back are blocked while verification is active. An unchanged key simply closes; blank key explicitly clears through the parent callback.
- IntroDB key verification sends a POST to the configured IntroDB `/submit` endpoint with an empty JSON body and Bearer token; API helper treats 400 (auth passed but body invalid), 200 and 201 as valid, 401/403 as invalid, and all other/throwing outcomes as invalid. This is current behavior; no endpoint integration test was run.
- Next Episode Threshold Mode commits immediately through the parent callback, which also closes the dialog. The helper's selected-state rendering and parent callback are consistent; no issue found.
- Dialog count in `PlaybackSettingsPage.kt`: 20 `BasicAlertDialog` call sites. This count is a call-site inventory, not a complete accessibility/runtime verification.

### Classification / remaining checks

- Provider allowlist auto-save-on-dismiss: **INTENTIONAL INTERACTION CONTRACT**, not a missing Cancel bug; preserve “Save & close” explanation.
- Regex draft/validation: **CURRENT BEHAVIOR VERIFIED**, runtime matching and pathological-pattern performance **NOT VERIFIED**.
- IntroDB secret masking vs Debrid/plugin plain text: **SHARED PATTERN / DIFFERENT IMPLEMENTATION**; add IntroDB as a positive reference to the existing single canonical credential-visibility finding, not a duplicate issue.
- Threshold-mode picker: **NO DEFECT FOUND**; immediate save and close are coherent.
- Remaining Playback dialogs still need a complete per-control/copy/accessibility pass, including keyboard/focus and selected-state announcements. No TalkBack/VoiceOver/device verification.

**Verification:** static GitHub source reads only; no source changes, tests, builds or device checks. `SkipIntroApi.verifyIntroDbApiKey` catches exceptions and maps them to false; this conclusion is based on the inspected helper source.


## Playback selection-dialog and localization cross-check — 2026-11-20 13:05:51 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `69e69d83419da255447b1c127de997816d666408`  
**Previous audit document blob / write guard:** `b37867b768da1970a936a8fad7ddf15fd0b21e02`  
**Files inspected:** `PlaybackSettingsPage.kt`, `PlayerModels.kt`, `IosVideoSettingsModal.kt`.

### Selection behavior and common pattern

- Player preference, external-player app, language, reuse-cache duration, decoder priority, playback engine, iOS enum, skip-seconds, hold-to-speed, libass render type, subtitle startup mode, subtitle font and subtitle color dialogs are immediate-selection patterns: parent callbacks update the setting and close the dialog. Their repeated selected-card UI is **SHARED PATTERN / DIFFERENT SEMANTICS**, not duplicate settings or duplicate dialog behavior.
- `NextEpisodeThresholdModeDialog` also commits and closes through its parent callback; no defect found.
- The generic enum picker name `IosEnumSelectionDialog` remains a naming mismatch because Android libmpv and P2P settings also use it. This is the existing shared-helper finding; do not create another picker.
- Many custom pickers show selected state through a tinted Surface and check icon rather than explicit RadioButton/Checkbox controls. This remains an accessibility verification item, not a confirmed defect without TalkBack/VoiceOver testing.

### New localization findings

- **CONFIRMED LOCALIZATION GAP:** `AndroidLibmpvVideoOutput` stores user-facing English descriptions directly in `PlayerModels.kt` (“Modern libmpv renderer…” / “Compatibility renderer…”), and `PlaybackSettingsPage.kt` passes `it.description` straight into the generic enum dialog. These descriptions should be localized in the approved text pass. Keep the technical renderer identifiers/values stable.
- **CONFIRMED LOCALIZATION CONSISTENCY BUG:** `IosHardwareDecoderMode.localizedLabel()` already maps the `Off` option to `Res.string.player_ios_hardware_decoder_off`, but the Playback settings dialog currently passes `it.label` instead of the existing localized helper. Reuse `localizedLabel()`; do not add a duplicate resource/helper.
- `IosVideoOutputPreset.localizedLabel()` and `localizedDescription()` are already used by `IosVideoSettingsModal.kt`; preserve this implementation and avoid adding another localization path.
- Technical labels such as ExoPlayer, libmpv, VideoToolbox, BT.709, Display P3 and codec/renderer identifiers should be reviewed for product terminology rather than mechanically translated. Do not treat every enum literal as a localization defect.
- Existing Advanced Logs and Plugin dialog localization findings remain separate canonical findings; this adds Playback-specific occurrences, not duplicate entries for those screens.

**Decision:** ADAPT the existing localization helper for iOS decoder Off and move libmpv descriptions into string resources. No source edits made. Accessibility and runtime localization layout remain NOT VERIFIED on Android/iOS devices.


## Notifications settings control-to-storage and platform audit — 2026-11-20 13:07:39 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `96a943247cd6df7d2ed0cfd310686a35b7631e69`  
**Previous audit document blob / write guard:** `8a3bd8585a2b2d29789ee42fbe0ac25ee40dd7ac`  
**Files inspected:** `NotificationsSettingsPage.kt`, `EpisodeReleaseNotificationsRepository.kt`, `EpisodeReleaseNotificationsModels.kt`, common/Android/iOS notification platform and storage expect/actual files.

### Canonical setting map

- The only user preference currently exposed on the Notifications page is **Episode Release Alerts**. Its switch calls `EpisodeReleaseNotificationsRepository.setEnabled`; it is disabled while repository loading is active.
- The repository requests platform notification authorization before enabling. Denial leaves the setting disabled, clears scheduled count, and exposes a localized permission message. If permission is later revoked externally, repository refresh clears pending schedules while retaining the user's enabled preference and reports the permission problem.
- The test notification button is intentionally still available when permission is missing: its action requests authorization again and then sends a test notification. Do not disable it solely because `permissionGranted == false`.
- Test target selection prefers a followed series, then falls back to the first library item. This is test-only behavior; it does not alter tracked-show eligibility.
- Followed shows are reconciled from the active profile's Library state. The stored payload contains enabled state and followed-show metadata, including the date followed; episode requests are built from release dates not earlier than the followed date. Request IDs include profile identity.
- `EpisodeReleaseNotificationsStorage` is profile-scoped on Android SharedPreferences and iOS NSUserDefaults. Platform scheduled-request identifiers are also profile-scoped. Android WorkManager and iOS UserNotifications implementations are **INTENTIONAL PARALLEL** actuals, not duplicate repositories. Preserve profile switching, cancellation and scheduled-ID bookkeeping.
- The common platform contract also exposes `showCloudStreamPluginUpdates`; this is an automatic plugin-update notification path, not another user-facing preference on the current Notifications page. Do not add a second toggle without tracing its caller and product policy.

### New localization follow-up

- **CONFIRMED LOCALIZATION GAP:** Android and iOS `showCloudStreamPluginUpdates` actuals hardcode “CloudStream extensions updated” and their message/body formatting. Localize these through shared resources, preserving pluralization and the platform's notification-channel semantics. Episode-release notification channel title/description and episode bodies already use localized resources.
- Existing Advanced Logs hardcoded copy is a separate canonical localization finding.

**Classification:** Android/iOS storage and notification scheduling = **INTENTIONAL PARALLEL**; automatic CloudStream update notification vs episode-release alerts = **SHARED NOTIFICATION INFRASTRUCTURE / DIFFERENT SEMANTICS**. No duplicate setting identified.

**Verification:** static source reads only. No notification permission prompts, background worker execution, iOS notification scheduling, app lifecycle or device tests were run.


## Account and Advanced settings persistence map — 2026-11-20 13:08:06 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `91c1ce508392cd0d00ba1ca2c4c8bf484bf04610`  
**Previous audit document blob / write guard:** `f3cb52d7a53a9fd2774025cd0c074b0ed449e788`  
**Files inspected:** `AccountSettingsPage.kt`, `AuthRepository.kt`, `AdvancedSettingsPage.kt`, `ProfileRepository.kt`, `SentrySettingsRepository.kt`, `SentrySettingsStorage.kt` and Android/iOS actuals.

### Account

- Account page exposes auth status/email, Sign Out, and conditional Delete Account.
- Delete Account is shown only when `AppFeaturePolicy.accountDeletionEnabled` and the current auth state is authenticated. Confirmation is required; the dialog blocks dismissal while deletion is in progress.
- Sign Out confirms, then calls `AuthRepository.signOut()`. The repository attempts remote sign-out (except anonymous sessions), falls back to clearing the Supabase session on remote failure, wipes local account data and clears the local data-owner marker; it then publishes Unauthenticated. A failure is still surfaced after cleanup attempts. Preserve this local-data cleanup and fallback behavior; do not turn sign-out into a profile-only action.
- Delete Account invokes the `delete-account` Supabase function, signs out, and then wipes local data / clears owner state. Failure is exposed in the page. Keep the feature-policy gate and confirmation.
- Account is a user/auth identity surface; Profiles and Profile Insights remain separate profile-management/analytics surfaces. No duplicate account/profile control found in this pass.

### Advanced

- **Remember Last Profile** writes through `ProfileRepository.setRememberLastProfileEnabled` into the stored profile payload's user-level preference; it is not scoped to the active profile. Keep under You/Profiles or You/Startup, not System.
- **Sentry Reports** exists only when `SentrySettingsRepository.isSupported`. Android reports support and stores the preference in app SharedPreferences; iOS reports unsupported and hides the control. The value is app-global (not ProfileScopedKey). The switch opens a confirmation/explanation dialog and only writes on confirm. This is **INTENTIONAL PLATFORM SCOPE**, not a missing iOS toggle or duplicate.
- **Logs** is diagnostics content and remains System → Diagnostics. Its hardcoded English labels/dialog actions are already the canonical Advanced Logs localization finding; do not duplicate it.
- **Clear Continue Watching Cache** clears the active profile's `ContinueWatchingEnrichmentCache`, then requests a local clear and forced server snapshot refresh through `WatchProgressRepository`. It is an action, not a persistent setting; retain a completion state and do not move it into a profile preference list.

**Classification:** Account auth vs Profiles = **DIFFERENT SEMANTICS**; Sentry Android/iOS implementations = **INTENTIONAL PLATFORM SCOPE**; clear-cache action vs persisted preferences = **FALSE POSITIVE** for duplicate setting. No new duplicate confirmed.

**Verification:** static source reads only; no auth/account deletion requests, Sentry runtime initialization, profile switch, cache clearing, tests, builds or device checks were run.


## NEW badge version-comparison test design — 2026-11-20 13:08:37 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `ee7e61f0763630f9cc464f72c98e5b7aef51f1a2`  
**Previous audit document blob / write guard:** `d9494f60eebb8500bc7305864627a1c2ca153eeb`  
**Files inspected:** `composeApp/build.gradle.kts`, `AppUpdater.kt`, `AppUpdaterVersionUtilsTest.kt`; existing NEW-label rule above.

### Source and compatibility findings

- `composeApp/build.gradle.kts` generates `AppVersionConfig.VERSION_NAME` and `VERSION_CODE`; `VERSION_NAME` is sourced from `MARKETING_VERSION` in `iosApp/Configuration/Version.xcconfig`. This is the installed app version source for the badge.
- Existing `VersionUtils` is private to the updater. Its semantic-version regex extracts numeric version parts and ignores prerelease suffixes; its current tests cover release/build-code ordering, not SemVer prerelease precedence or invalid inputs. **Do not reuse it** for NEW expiry without extending its contract and tests; prefer a small pure comparator scoped to the badge feature to avoid changing updater behavior.
- Badge lifetime remains exactly three app releases inclusive, e.g. introduced in 0.5.2 → visible in 0.5.2, 0.5.3 and 0.5.4 → hidden from 0.5.5. Metadata is explicit and app-global; it is not dismissible and must not use profile-scoped `seenFeatureIds`.

### Required unit-test matrix before implementation

1. Inclusive lower and upper bounds: show at introduced version and visible-through version.
2. Adjacent stable releases: show on the middle release; hide one release before and one after the interval.
3. Patch/minor/major ordering: compare `0.5.9 < 0.5.10 < 0.6.0 < 1.0.0` numerically, not lexicographically.
4. Prerelease ordering: `0.5.2-alpha.1 < 0.5.2-alpha.2 < 0.5.2-beta.1 < 0.5.2-rc.1 < 0.5.2`; numeric prerelease identifiers compare numerically (`rc.2 < rc.10`).
5. Build metadata does not change precedence (`0.5.2+build.10` equals `0.5.2+build.11` for expiry).
6. Unknown/blank/unparseable installed version fails closed (do not show stale badges); malformed metadata is rejected and does not crash settings rendering.
7. Reversed interval (`visibleThroughVersion < introducedInVersion`) is rejected.
8. App-global behavior: changing active profile does not alter badge visibility; restarting/reopening settings does not mark it seen or dismiss it.
9. Multiple new settings: each stable setting ID resolves its own metadata; no badge leaks to unrelated rows or duplicate routes.
10. Release metadata is compared against the installed `VERSION_NAME`, not a remote latest-release tag, build code, profile version or updater cache.

**Status:** test design only; no comparator or tests implemented. This remains a plan prerequisite, not a new duplicate setting or existing feature.


## Subtitle font import platform-parity and error handling — 2026-11-20 13:09:14 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `c6b28b918f6af2169bf6f71eeec901819909f876`  
**Previous audit document blob / write guard:** `328a93df34753ed01aec869bc34f3fe6c978ee17`  
**Files inspected:** `SubtitleFontFileBridge.kt`, Android/iOS `SubtitleFontFileBridge` actuals, `PlaybackSettingsPage.kt` caller, `PlayerModels.kt`.

### Confirmed behavior and gaps

- Both platforms enforce the shared 30 MB import limit and save each font in a unique directory, avoiding renderer collisions between imports with the same embedded family.
- Android reads the selected document and validates it with `Typeface.createFromFile` before accepting the path. iOS accepts `public.font` and broad `public.data` picker types, checks non-empty/size, but does not parse/validate that the bytes are a usable TTF/OTF font before storing the path. **NEEDS INVESTIGATION / PLATFORM PARITY GAP:** confirm renderer failure behavior on iOS and add equivalent validation if a safe cross-platform parser/native API is available.
- Android derives the displayed font name from the embedded family when possible; iOS uses the selected filename. This is a presentation difference; preserve technical font file identity and avoid rewriting stored paths as part of settings IA.
- The Playback page caller handles only `result.onSuccess`; it closes the font dialog for both success and failure and discards `result.exceptionOrNull()`. Thus oversized, unreadable, invalid or storage-failure errors from either platform have no user-facing feedback. **CONFIRMED ERROR-FEEDBACK GAP**; cancellation may close silently, but genuine import failures should be surfaced distinctly.
- Clearing the custom font resets the active profile's family/name/path preference but does not delete the imported file. Do not add eager file deletion: a font path could still be referenced by another profile. Any cleanup requires reference tracking across profiles and migration-safe storage review.
- Android uses app-internal files storage; iOS stores imports under Documents. This is an intentional platform storage difference unless lifecycle/backup behavior demonstrates a problem. The saved path is part of Player settings and must remain profile-scoped.

### Classification / decision

- Android/iOS font import actuals: **INTENTIONAL PARALLEL** with a **NEEDS INVESTIGATION** validation mismatch.
- Ignored import failure result: **CONFIRMED UX ERROR-HANDLING GAP**, not a duplicate dialog.
- Font file cleanup: **NEEDS INVESTIGATION**; do not delete files until cross-profile references are understood.
- Decision: **ADAPT** error presentation and investigate iOS validation as a separate implementation item; no new settings repository or font-picker abstraction.

**Verification:** source inspection only; no real font imports, malformed-font tests, iOS simulator, Android device or rendering checks were run.


## Upstream Downloads settings parity — 2026-11-20 13:11:47 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `6076c51ac34fc3c2958b82d9251f0778bdb511d5`  
**Previous audit document blob / write guard:** `82fb17ac44dbf646c6d8b6a7d96041b4dd38ab83`  
**Upstream source files:** `DownloadsSettingsPage.kt`, `DownloadsSettingsRepository.kt`, `DownloadsSettingsStorage.android.kt`, `DownloadsSettingsStorage.ios.kt`, `DownloadsPlatformDownloader.ios.kt`, `DownloadsRepository.kt`.  
**Mew source files:** `EpisodeDownloadFlowSheet.kt`, `DownloadSourceResolver.kt`, `DownloadsRepository.kt`, `DownloadsStorage.kt` and Android/iOS actuals for storage, external folder and platform downloader.

### Semantic comparison

| Upstream setting | Mew current behavior | Decision |
|---|---|---|
| Preferred resolution | Mew already has per-profile `EpisodeDownloadSettings.preferredQuality` (Best, 2160, 1080, 720, 480, 360) inside the batch flow sheet; used by source auto-selection. Upstream has a device-level 720/1080/2160 preference. | **SHARED PATTERN / DIFFERENT SCOPE** — retain Mew quality choices and profile semantics; do not add a second preferred-quality setting. |
| Concurrency limits (total, concurrent shows, episodes/show) | Mew supports per-item `AllAtOnce` or `OneAtATime`; `AllAtOnce` starts each item immediately. Source discovery is bounded (4 providers, 3 episode selections), but active file transfers have no configurable global/show/episode cap in the inspected repository. | **ADAPT candidate** — add limits to the existing queue admission/promotion state machine only if approved; preserve per-item queueMode and pending source queue. Tests must cover simultaneous batches, resume, pause, replacement, profile switch and worker callbacks. |
| Allow mobile data | Mew Android downloader has no metered-network guard in the inspected path. Mew iOS default NSURLSession config sets cellular, expensive and constrained network access to true. Upstream has a device-level allow-mobile-data setting and network guard. | **ADAPT candidate / HIGH REGRESSION RISK** — wire a device-level network policy into existing Android/iOS downloader and queue behavior. Do not silently change current always-allowed behavior; test transport changes, in-flight requests and resume semantics. |
| Show download button | Mew exposes the Episode Download Flow from detail UI; no equivalent preference or visibility gate found in the inspected caller path. | **NEEDS PRODUCT DECISION** — if adopted, gate the existing detail action, do not add a second download entry point. |
| Download location | Mew Android downloader can copy app-private completed files to an optional persisted tree URI; `DownloadsExternalFolderPlatform` has Android chooser/storage, but no chooser call site was found in the inspected settings/DownloadsScreen/flow. Mew iOS external-folder actual is a no-op and downloads into `Documents/nuvio_downloads`. Mew folder URI storage is profile-scoped. Upstream uses a device-level location URI and iOS security-scoped bookmarks. | **NEEDS INVESTIGATION** — locate/verify the Mew Android chooser caller and decide device vs profile scope before any UI port. iOS needs bookmark lifecycle, access renewal and file operations; never copy only the picker UI. |
| iOS legacy downloads toggle | Mew iOS currently uses a default NSURLSession data-task implementation with app-level pause handling. Upstream defaults to a background NSURLSession download coordinator and exposes a legacy-mode toggle to select the older data-task path. | **NEEDS INVESTIGATION / iOS PARITY CANDIDATE** — compare background completion, resume data, app termination, subtitle sidecar fetch, progress and destination semantics. No iOS device/simulator verification available; do not port toggle or downloader wholesale. |

### Storage / duplicate classification

- Upstream download preferences are explicitly **device-level**, stored in Android SharedPreferences / iOS NSUserDefaults without profile keys.
- Mew queue state, external folder URI and episode-download settings are stored through `DownloadsStorage` using `ProfileScopedKey` on both Android and iOS. Do not change that scope implicitly.
- Mew `preferredQuality` is not a duplicate of upstream preferred resolution: it drives per-profile batch auto-selection with more granular values. QueueMode is also distinct from upstream concurrency limits: one-at-a-time vs all-at-once is a per-item batch policy, not a configurable global/show cap.
- Mew's bounded source-discovery semaphores (4 providers / 3 episode selections) are not active download concurrency controls. Do not mistake them for equivalent settings.
- Existing Mew batch-download implementation and tests remain canonical. Do not copy upstream `DownloadsRepository` or replace the existing coordinator.
- Upstream iOS folder bookmarks and background download coordinator are separate platform capabilities, not duplicate Mew implementations.

**Overall decision:** **SKIP wholesale port**. Selectively adapt concurrency/network policy only after explicit product approval and regression test plan; investigate download location and iOS background-download parity separately. No app source changes made.

**Verification:** source comparison only; no download tasks, metered-network transitions, storage picker, external folder write, app-background/termination or iOS runtime tests were run.


## Live TV settings and model parity — 2026-11-20 13:12:39 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `ca4c18420f18999d43b7e0b724117749fbd789ea`  
**Previous audit document blob / write guard:** `38c0dd3ba79f0c5925f9f7832b3388dfae7fae62`  
**Files inspected:** Mew `LiveTvScreen.kt`, `LiveTvRepository.kt`, `LiveTvModels.kt`, `LiveTvStorage.kt`, Enhanced `LiveTvSettingsPage.kt`, and Enhanced `SettingsModels.kt`.

### Current Mew behavior

- Mew has one active Live TV source at a time: M3U URL, local M3U file, Stalker credentials or Xtream credentials. `LiveTvRepository` persists one source type/URL/local playlist payload and the Stalker/Xtream settings; the screen's gear opens an in-screen provider configuration surface.
- Mew supports EPG parsing/loading from EPG URLs discovered in the active M3U source, cached EPG data and guide display. The inspected Mew settings screen does not expose a manual multi-source EPG manager or an EPG auto-detect preference.
- Mew's existing `liveTvEnabled` flag in Nuvio Enhanced is the feature/visibility gate; `LiveTvRepository.setFeatureEnabled` responds to that gate. Do not add a second equivalent enable toggle.
- Mew's Xtream/Stalker credential inputs are masked with `PasswordVisualTransformation`. Preserve this; do not regress secret handling when moving configuration UI.

### Enhanced delta

- Enhanced exposes a dedicated Live TV settings route and adds navigation visibility, reload channels/EPG actions, provider settings, multiple configurable M3U URL/local playlists with per-playlist enable/edit/remove, and manual EPG sources with per-source enable/remove plus auto-detect.
- Enhanced `SettingsPage.LiveTv` is under Services → Integrations. For Mew IA, the natural home is Watch → Live TV because it is a playback destination, while credentials and source management remain within the Live TV workflow.
- This is not a simple dialog/page duplicate: Mew's one-active-source model and Enhanced's multi-playlist model have different persistence and playback semantics. Mew does not currently contain `LiveTvPlaylist` / `LiveTvEpgSource` model collections in its inspected model.

### Decisions

- Existing Mew feature gate: **ALREADY IMPLEMENTED / DIFFERENT OWNER**; keep one toggle, move it from Enhanced to Watch → Live TV in the proposed IA without duplicating state.
- Dedicated settings destination: **ADAPT** only if it improves navigation; preserve the existing in-screen gear as a compatibility entry to the same settings surface.
- Multiple playlists: **NEEDS INVESTIGATION / FEATURE PARITY CANDIDATE**; requires a data-model/storage migration and source-selection policy, not a UI-only port.
- Manual EPG source manager/auto-detect: **NEEDS INVESTIGATION / FEATURE PARITY CANDIDATE**; preserve current M3U-derived EPG behavior and cache semantics while comparing explicit source precedence and profile scope.
- Reload controls: **ADAPT** only after confirming current Mew refresh lifecycle and loading cancellation; do not duplicate the existing refresh action.

**Classification:** existing Mew single-source setup vs Enhanced playlist collection = **SHARED PATTERN / DIFFERENT SEMANTICS**; current feature gate vs upstream navigation visibility = **SAME USER-FACING PURPOSE / DIFFERENT STATE OWNER**. No TRUE DUPLICATE found.

**Verification:** static source reads only. No M3U imports, EPG refresh, playlist migration, credentials, playback or device/iOS runtime checks were run.


## Current Enhanced route-tree delta — 2026-11-20 13:12:59 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `4baac9058c1199ed232c7a49676331c1d6354475`  
**Previous audit document blob / write guard:** `529fc9d3335bddb8326cc321dd42d98961eef352`  
**Files compared:** current Enhanced and Mew `SettingsModels.kt` / `SettingsRootPage.kt`; upstream route page files for Media Servers, OMDB, Discord RPC, Live TV and What's New; Mew repository tree search for corresponding feature paths.

### Route hierarchy to adapt

- Enhanced categories are You, Look, Watch, Services, System. Mew categories remain Account, General, Enhanced, About, Advanced. Adopt the five-category taxonomy as an IA proposal, but map old category names with a safe parser before any persisted tablet category migration.
- Enhanced introduces a Layout parent for Homescreen, Streams, Continue Watching, Poster Customization and Meta Screen. Mew currently places those pages under Appearance. **ADAPT** the parent route, preserve existing child page enum names and saved state.
- Enhanced introduces an About parent for Supporters, Licenses and What's New. Mew has Supporters/Licenses routes directly under Root and no What's New feature implementation in the source tree. **ADAPT** About grouping for existing Mew pages; **SKIP** adding a What's New route until its data/content and release-highlight relationship are designed.
- Enhanced splits Playback into Player, Stream Selection, Up Next, Skip Segments, Interface, Audio, Subtitles, Video, Buffer and P2P subpages. Mew currently has one Playback page with those sections. **ADAPT** page structure using Mew's existing PlayerSettingsRepository/P2P owners; preserve platform gates, route names and all keys.
- Enhanced nests Addons/Plugins/Media Servers under Content Discovery. Mew has Addons/Plugins/CloudStream; CloudStream remains a distinct Mew integration. No Mew Media Server provider/repository code found.
- Enhanced adds Debug Logs as an Advanced child; Mew already has logs inside Advanced. **ADAPT** only as a route split over the existing diagnostics owner.
- Enhanced has a Downloads settings route. Mew's Downloads manager is a utility screen and download preferences are partly embedded in EpisodeDownloadFlowSheet; see the dedicated semantic comparison above. Do not conflate the manager route with preferences.

### Upstream-only feature routes absent from Mew source tree

- **Media Servers (Plex/Jellyfin-style provider repository and per-server settings): SKIP for this settings IA pass.** No corresponding Mew feature/repository/model path was found. Feature parity requires a separate provider/auth/security audit.
- **OMDB integration settings: SKIP for this IA pass.** No Mew OMDB page/repository/model path found; do not insert an empty placeholder into Integrations.
- **Discord RPC: SKIP for this IA pass.** Mew has a Discord brand asset but no RPC feature/repository path. Do not infer feature presence from the asset.
- **What's New: SKIP for this IA pass.** Enhanced has a dedicated repository/screen; Mew has no corresponding feature path. Do not repurpose the NEW badge or profile-scoped release-highlight mechanism as a substitute.
- **Live TV:** Mew already has a full single-source workflow and an Enhanced feature gate; its settings/model differences are documented in the Live TV parity section above. Do not treat it as a missing feature or duplicate.

**Classification:** route parent additions = **ADAPT**; upstream-only feature pages without Mew feature owners = **SKIP** for this task, not confirmed duplicate settings. This route-tree comparison is current to Enhanced `375d93d90864d6b0774d8ab6f58c72d63d10d2af`; a complete implementation/dependency audit of skipped upstream features is outside this settings IA scope.

**Verification:** GitHub tree/source reads only; no feature source imported, no builds/tests/device checks.


## Localization resource coverage spot-check — 2026-11-20 13:14:12 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `9a2324151b781cbde602d2a26cde2caa709b8316`  
**Previous audit document blob / write guard:** `e2e2281b333ca64cc6d2f4cb308e1575b0f555fa`  
**Scope:** all 24 `commonMain/composeResources/values*/strings.xml` files (23 locale folders plus default `values`); selected existing settings resource keys checked for literal presence.

### Key coverage results

| Resource key | Files containing key |
|---|---:|
| `player_ios_hardware_decoder_off` | 17/24 |
| `settings_playback_render_type` | 23/24 |
| `settings_debrid_max_results` | 17/24 |
| `settings_advanced_sentry_reports` | 12/24 |
| `settings_notifications_episode_release_alerts` | 24/24 |

- The missing-key sets for `player_ios_hardware_decoder_off` and `settings_debrid_max_results` match in this sample: `values-de`, `values-he`, `values-id`, `values-in`, `values-nb`, `values-pt-rBR`, `values-tr`.
- `settings_playback_render_type` is missing from `values-he` only.
- `settings_advanced_sentry_reports` appears in only half the resource files; verify whether this is intentionally inherited/fallback or untranslated before changing resource files.
- Notifications Episode Release Alerts key is present in all 24 files; this is a positive coverage sample, not proof that the entire Notifications page is fully translated.
- Upstream-only keys such as `live_tv_settings_epg_auto_title` and `downloads_allow_mobile_data_title` are not Mew resources and are not counted as Mew localization gaps.

**Classification:** resource-key absence = **LOCALIZATION COVERAGE GAP / NEEDS FALLBACK VERIFICATION**; hardcoded English UI text remains a separate canonical issue. Do not add duplicate findings for each locale. The spot-check is not an exhaustive audit of every settings key or translation quality.

**Verification:** GitHub source reads of all 24 resource files; no localization source changes, app rendering, locale switching or device tests.


## Expanded localization coverage across all settings resources — 2026-11-20 13:14:55 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `c1a53c386b5ff468073a72b75630526f1b26eb6e`  
**Previous audit document blob / write guard:** `6e3ded08ce84bc285896dbf9e52a36b96537f179`  
**Method:** compared every `settings_`, `compose_settings`, `player_`, `downloads_`, `live_tv_` and `sentry_` string/plural/array key in default `values/strings.xml` against each of the 23 locale files. Baseline set: **1,162 keys**.

| Locale folder | Missing keys | Coverage |
|---|---:|---:|
| ar | 286 | 75.4% |
| bg | 215 | 81.5% |
| cs | 381 | 67.2% |
| de | 714 | 38.6% |
| el | 222 | 80.9% |
| es | 364 | 68.7% |
| fr | 341 | 70.7% |
| he | 948 | 18.4% |
| hu | 327 | 71.9% |
| id | 596 | 48.7% |
| in | 596 | 48.7% |
| it | 358 | 69.2% |
| ja | 383 | 67.0% |
| nb | 620 | 46.6% |
| nl | 362 | 68.8% |
| pl | 317 | 72.7% |
| pt-rBR | 610 | 47.5% |
| pt | 377 | 67.6% |
| ro | 362 | 68.8% |
| ru | 286 | 75.4% |
| sk | 381 | 67.2% |
| tr | 528 | 54.6% |
| vi | 329 | 71.7% |

- The default `values/strings.xml` contains all 1,162 baseline keys. Missing keys in locale folders are **translation coverage gaps**, not missing default resources.
- Coverage varies significantly. Hebrew (18.4%) and German (38.6%) are the largest gaps in this key subset; this should be prioritized before a settings taxonomy release.
- The duplicated `id` / `in` folders have identical counts but are separate resource directories; do not delete or consolidate either without locale configuration and build verification.
- The counts are key-presence coverage only. They do not measure translation quality, stale wording, parameter correctness, plural correctness, text overflow, or runtime fallback. Compose resource fallback behavior remains **NOT VERIFIED** in this audit.
- This comparison is broader than the earlier 5-key spot-check; retain the earlier spot-check as a historical sample, but use this section for current coverage totals.

**Decision:** localization must be a dedicated implementation workstream in the final plan, not a small copy-only cleanup. Keep default English strings complete; add/update translated keys consistently across all locale files and test layout/overflow on representative scripts (RTL, CJK, long German strings).

**Verification:** GitHub reads of the default file and all 23 locale files; no resource edits, app build, locale switching or device rendering tests.


## Tracking, addon consent and account-flow parity — 2026-11-20 15:27 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `a0f1dc0d1d1f9df1e7832604681baac5c6ddfe57`  
**Previous audit document blob / write guard:** `5279070fcab6855e59b8257f37112b723ebbddd2`  
**Application source baseline:** `a7f066297ba000833a981b12f8b57c26ce833f7d`; compare confirms only this audit MD changed since baseline.  
**Upstream Enhanced ref:** `3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6`.  
**Files inspected:** Mew `AddonModels.kt`, `AddonRepository.kt`, `AddonsScreen.kt`, `TrackingProvider.kt`, `TrackingSources.kt`, `TrackingSettingsPage.kt`, `TrackingProviderCards.kt`, `TrackingSettings.kt`, `PlayerSettingsRepository.kt`, `AccountSettingsPage.kt`, `AuthRepository.kt`, `SettingsModels.kt`, `SettingsRootPage.kt`, `Routes.kt`, `NuvioNavigator.kt`; corresponding Enhanced Addon consent/model/repository/screen, Tracking provider/settings/repository, Player settings and Account page files.

### Addon tracking-consent gate — CONFIRMED PRIVACY / SECURITY PARITY GAP

- Enhanced defines `AddonManifest.isTrackingAddon` when the manifest exposes either the `player` or `library` resource. `AddonRepository.addAddon()` returns `TrackingConsentRequired` instead of installing immediately; the UI requests consent, and `TrackingAddonConsentDialog` requires the checkbox before enabling Install. The explicit continuation calls `installWithTrackingConsent()`.
- Mew's `AddonManifest` has no equivalent tracking-resource classification or consent-required result. `AddonRepository.addAddon()` validates/parses the manifest and immediately persists/installs it; `AddonsScreen` handles only Success/Error. No tracking-consent dialog or gate exists in the inspected Mew source tree.
- **Decision: ADAPT / HIGH PRIORITY.** Port the consent behavior into Mew's existing add-on install transaction, not the upstream repository wholesale. Preserve Mew's profile-session/generation guards, primary-addons profile restrictions, URL normalization, manifest validation, redacted logging, server sync and install-state behavior. Consent must be bound to the exact manifest/transport URL being installed; re-check profile/session and duplicate state when consent is accepted. Do not silently install on dialog dismissal.
- **Classification:** Mew add-on validation and Enhanced consent are shared install pipeline with different privacy safeguards, not duplicate dialogs. The missing consent gate is a confirmed gap. Existing installed add-ons/migration and consent persistence policy require a deliberate decision; the inspected upstream flow asks at install time and does not establish a long-term blanket permission.

### Tracking provider and capability delta — SHARED PATTERN / DIFFERENT SEMANTICS

- Mew `TrackingProviderId` currently contains TRAKT, SIMKL, ANILIST and MY_ANIME_LIST. Mew's tracking data-source picker currently considers only connected Trakt and Simkl. AniList and MyAnimeList have separate anime-tracking cards/settings and should not be treated as interchangeable with the current library/progress source selectors.
- Enhanced's provider IDs are TRAKT, SIMKL, MDBLIST, ANILIST, WETRAKR and PUBLICMETADB; it no longer has the Mew `MY_ANIME_LIST` provider ID. Enhanced adds MDBList, WeTrakr and PublicMetaDB provider cards and includes their authenticated providers in library/progress source selection.
- Mew has an MDBList integration/settings surface, but the inspected Mew tracking registry has no MDBLIST/WETRAKR/PUBLICMETADB IDs or corresponding tracking provider adapters. No source evidence supports simply mapping the existing MDBList API-key integration to a library/progress provider.
- **Decision: ADAPT candidates / NEEDS INVESTIGATION.** Preserve Mew's MyAnimeList support and serialized provider IDs. Assess MDBList, WeTrakr and PublicMetaDB one provider at a time: API/auth model, supported capabilities, profile-scoped credentials, sync direction, paging/rate limits, conflict policy, tests and platform support. Do not replace the enum or migrate stored provider IDs wholesale.
- Enhanced also adds `TrackingCapability.RATINGS`, a `TrackingRatingProvider` registry and `UserRatingsRepository` for profile-scoped 1–10 ratings. Mew has no corresponding ratings package/registry capability in the inspected tree. Treat in-app cross-provider ratings as a separate feature candidate, not a setting-only port.
- Enhanced adds `privateViewingEnabled` to Player settings and exposes it under Tracking → Privacy. Mew's current `PlayerSettingsRepository.kt` does not contain that state and the inspected tracking/scrobble settings have no private-viewing control. **NEEDS INVESTIGATION:** trace every local history, scrobble, provider sync, analytics and profile write path before adopting a privacy mode. A UI toggle without complete write suppression would be misleading. Keep this separate from existing parental-guide and tracking source controls.
- **Duplicate audit:** no new TRUE DUPLICATE found. Mew's MyAnimeList vs Enhanced's new provider set is an intentional capability difference; Mew's MDBList integration configuration is not proof of a tracking adapter.

### Account anonymous-to-account label — NEEDS INVESTIGATION

- Enhanced Account UI detects anonymous guest state and changes the primary action/copy to “Sign in with account”. However, its confirmation callback still calls `AuthRepository.signOut()`; the inspected AuthRepository in both Mew and Enhanced exposes sign-up/sign-in methods separately and no explicit anonymous-session link/upgrade method.
- Mew's Account page always labels this action “Sign Out” and calls the same sign-out flow. The sign-out path clears anonymous local identity and local account data, so the Enhanced wording must not be copied as a standalone UI change.
- **Decision: NEEDS INVESTIGATION.** Trace the root auth-screen transition and guest-data retention contract before changing Mew's copy or behavior. Do not assume that `signOut()` converts or links a guest account.

### Current upstream movement

- Enhanced advanced one commit after the previous audit anchor: `375d93d90864d6b0774d8ab6f58c72d63d10d2af..3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6`.
- The commit changes `StreamDestination.kt` and `PlayerScreen.kt` to look up a missing title logo during playback. This is outside settings IA; **SKIP for this pass** and record it as a separate player metadata-parity candidate. No Mew port or behavior claim was made.

### Status and remaining work

- **Audit progress estimate: ~90%; still IN PROGRESS.** This is a scope estimate, not a test-completion percentage.
- Confirmed duplicate controls remain **2** (Parental Guide and TMDB enrichment); confirmed contextual navigation/back mismatch remains **1**. No new true duplicate was added.
- Newly recorded high-priority privacy gap: addon tracking-resource consent before install.
- Remaining audit work: finish Plugins/CloudStream dialog and settings persistence review; verify Compose resource fallback behavior and representative locale rendering; complete contextual navigation/back-stack/deep-link restoration checks; consolidate all candidate decisions into one deduplicated migration matrix.
- No application source changes. Builds, tests, Android device checks and iOS simulator/device checks are **NOT RUN** in this audit continuation.

**Verification:** source-only GitHub reads and branch/commit comparisons. No app code or protected branch modified.


## Plugin / CloudStream dialogs and localization refinement — 2026-11-20 15:27 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `32f4d90da93fa4b1cd4879e33f0fbfa6dedad3d3`  
**Previous audit document blob / write guard:** `af00c3758f9d43a54fd7edd13d41cf06ca298a26`  
**Files inspected:** `fullCommonMain/.../PluginSettingsDialog.kt`, `PluginsSettingsScreen.kt`, `CloudStreamSettingsSection.kt`, `PluginRepository.kt`; common `PluginModels.kt`, `PluginRepository.kt`, `SettingsScreen.kt`, `SettingsModels.kt`, `Routes.kt`, `NuvioNavigator.kt`; source-set wrappers under commonMain/androidPlaystore/iosAppStore.

### Plugin settings dialog — existing canonical findings re-confirmed

- The actual dialog is in `fullCommonMain`; commonMain and store-specific `PluginsSettingsPage.kt` files are thin route/screen wrappers. These are **COMPATIBILITY SHIMS / INTENTIONAL SOURCE-SET SPLITS**, not duplicate implementations. Keep one shared dialog/content owner and preserve platform feature-policy fallbacks.
- `PluginSettingsDialog` reads manifest `isPassword` but does not apply password transformation or reveal/hide semantics to the input. This is another confirmation of the existing canonical credential-visibility finding; do not create another issue.
- The dialog title suffix and Save/fallback labels are hardcoded English. Keep these attached to the existing canonical localization gap.
- Malformed layout JSON falls back to an empty array; malformed saved settings JSON falls back to an empty object. The user receives no parse/error indication. This confirms the existing **NEEDS INVESTIGATION** finding; determine whether the plugin schema is invalid, storage is corrupt or a version migration is required before deciding whether empty defaults are safe. Do not silently overwrite the original payload on dialog open.
- Plugin settings are saved through the existing `PluginStorage.saveScraperSettings` call. Preserve existing JSON keys/types and profile/source-set storage behavior; do not introduce a parallel settings store.

### CloudStream settings — security gate and locale coverage

- CloudStream settings are implemented in `fullCommonMain/CloudStreamSettingsSection.kt`; the shared `CloudStreamRepository` API owns state and exposes `acceptSecurityWarning()`. The section hides the warning after acceptance and gates bulk installation on `securityWarningAccepted`; per-plugin install paths also retain their security/compatibility gates. Preserve these safeguards.
- The settings section's `CloudStreamSettingsCopy` is a hand-authored copy table selected by `selectedAppLanguage == TURKISH`. It supplies English for every other app language and Turkish only for Turkish. This includes repository controls, search/filter labels, bulk-install feedback, package verification, compatibility errors and security-warning copy.
- **Localization gap refinement:** add the entire CloudStream settings copy table to the existing canonical localization workstream. Do not create one issue per string. Replace with resource-backed translations as a dedicated localization task; preserve technical security wording and ensure warnings remain visible in every locale.
- No CloudStream modal-dialog family was found in the inspected settings section; most configuration and feedback are inline cards/messages. Do not invent a dialog consolidation task for this screen.
- CloudStream's separate repository/package model, security acknowledgement, package verification, platform compatibility and auto-update lifecycle remain distinct from generic Addons/Plugins. **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS**; do not merge the surfaces or bypass the warning.

### Navigation compatibility re-check

- `SettingsScreen` safely handles unknown `initialPageName`, saved `currentPage` and requested page names with `runCatching { SettingsPage.valueOf(...) }` and Root fallback/rejection.
- Tablet-only `selectedCategory` remains different: `SettingsCategory.valueOf(selectedCategory)` is called without a safe fallback. This confirms the existing category migration risk; preserve legacy names Account, General, Enhanced, About and Advanced through a tested parser before replacing the taxonomy.
- `SettingsPage.previousPage()` remains a static `parentPage`; the previously recorded Content Discovery → TMDB shortcut still cannot express a contextual origin. No second navigation bug or duplicate route was established in this re-check.
- `SettingsPageRoute` stores a page name string; retain existing enum names and external route behavior while reorganizing the page tree. `NuvioNavigator`'s external-back handoff is a navigation compatibility contract, not a second settings stack.

### Classification and status

- Plugin wrappers: **COMPATIBILITY SHIMS / INTENTIONAL PARALLEL**.
- Plugin password field: **CONFIRMED SECURITY GAP**, already part of canonical credential finding.
- Plugin JSON fallback: **NEEDS INVESTIGATION**, existing canonical finding.
- CloudStream vs generic Addons/Plugins: **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS**.
- CloudStream locale table: **LOCALIZATION COVERAGE GAP**, append to existing localization workstream.
- Tablet category parser: **CONFIRMED COMPATIBILITY RISK**, existing canonical finding.
- No new TRUE DUPLICATE found.

**Verification:** static source and route reads only. No source edits, builds/tests, locale switching, Android accessibility/device checks or iOS runtime checks.


## App language, theme persistence and profile boundary — 2026-11-20 16:14:00 IST

**Target:** `Main-dev`  
**Verified source HEAD before write:** `126546069ca2ad254fe00fdb7a5a085e10f38788`  
**Previous audit document blob / write guard:** `d680a5542278231715c4740c710db9ce62c5bfe5`  
**Files inspected:** `AppLanguage.kt`, `ThemeSettingsRepository.kt`, common `ThemeSettingsStorage.kt`, Android/iOS `ThemeSettingsStorage` actuals, `ProfileScopedKey.kt`, `ProfileRepository.kt`, Android/iOS `DeviceLanguagePreferences`, `PlayerLanguagePreferences.kt`, `PlayerLanguagePreferencesTest.kt`, `AppUrlBridge.kt`, `AppUrlBridgeTest.kt`, `SettingsScreen.kt`, `ProfileSettingsSync.kt`.

### Global app language vs profile-scoped appearance — CONFIRMED SEMANTIC BOUNDARY

- `AppLanguage` exposes 23 selectable language codes plus `DEVICE`; `fromCode()` falls back to `DEVICE` for unknown/null values.
- Theme, custom theme colors, AMOLED, Liquid Glass native tab bar, Liquid Glass auto-hide and navbar style are stored using `ProfileScopedKey.of(...)`. `ProfileRepository.selectProfile()` calls `ThemeSettingsRepository.onProfileChanged()`, which reloads these values for the newly active profile.
- App language deliberately uses the unscoped `selected_app_language` key on both Android and iOS. Both platform actuals first read that global key, then fall back to the old profile-scoped key and migrate it to the global key. Changing profile therefore does not intentionally change the app language.
- Android applies language through `AppCompatDelegate.setApplicationLocales`; iOS updates `AppleLanguages` in `NSUserDefaults`. These are **INTENTIONAL PARALLEL / PLATFORM IMPLEMENTATIONS**, not duplicate storage logic to consolidate into one platform implementation.
- The theme sync payload exports profile-scoped appearance keys (theme, custom colors, AMOLED, Liquid Glass and navbar style), but does not export the global app-language key. This is consistent with the global/profile boundary; do not accidentally make app language profile-specific or sync it as a profile preference during settings IA work.
- `ThemeSettingsRepository.clearLocalState()` resets in-memory defaults and native tab bridge state; profile switching instead reloads profile-scoped persisted values. Preserve the distinction between clearing state and switching profiles.

### Player subtitle language — separate preference domain

- `PlayerLanguagePreferences.kt` defines a much broader language option list and audio/subtitle sentinels (`default`, `device`, `original`, `none`, `forced`). Its device-language resolver has Android and iOS actuals and its normalization/filtering helpers have common unit tests.
- This is **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS**, not duplication of the 23-language app UI locale selector. App language controls interface localization; player preferences choose audio/subtitle tracks and filter addon subtitles. Do not merge their models or lists merely because both use language names/codes.

### Navigation / test coverage boundary

- `AppUrlBridgeTest` covers representative meta, addon-install, provider, reserved-auth and invalid Stremio deep-link parsing. `PlayerLanguagePreferencesTest` covers common language normalization and addon subtitle filtering.
- These tests do not establish settings route restoration, contextual back behavior, saved category migration, or end-to-end deep-link navigation after app recreation. Keep the existing contextual-back and tablet category parser findings open; broader navigation restoration coverage remains **NEEDS INVESTIGATION**.
- No evidence in this inspection establishes a dedicated `ThemeSettingsRepository` unit-test suite. Do not claim theme/profile persistence is tested merely because the player-language and deep-link helpers have tests.

### Classification and decision

- App locale vs player audio/subtitle language: **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS**.
- Android vs iOS language/storage actuals: **INTENTIONAL PARALLEL / PLATFORM IMPLEMENTATIONS**.
- Profile-scoped appearance vs global app language: **SHARED SETTINGS REPOSITORY / DIFFERENT PERSISTENCE SCOPE**; preserve current semantics.
- No new TRUE DUPLICATE found. No application source change recommended from this audit finding alone.

**Verification:** GitHub source reads at the recorded HEAD; compare confirms the only commit after the previously recorded HEAD changed this audit document. No builds, unit tests, locale switching, profile switching on device, Android device checks or iOS runtime checks were run.



## Profile settings sync boundaries and independent sync pipeline — 2026-11-20 16:18 IST

**Target:** `Main-dev`  
**Verified branch HEAD before write:** `cc21dae63c4668b5e18aa687bd5f1213ca628d51`  
**Previous audit document blob / write guard:** `5376d3c3fab6f0ef69d5705285352f38d24ba5ea`  
**Application source audit HEAD:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0` (the commits after this source HEAD through the current branch HEAD are audit-document-only; re-check before implementation).  
**Files inspected:** `core/sync/ProfileSettingsSync.kt`, `features/profiles/ProfileRepository.kt`, `core/storage/ProfileScopedKey.kt`, `features/library/LibraryDisplaySettings.kt`, `features/library/LibraryDisplaySettingsStorage.kt`, `features/tracking/TrackingSettings.kt`, `features/home/HomeCatalogSettingsRepository.kt`, `features/home/HomeCatalogSettingsSyncService.kt`, `features/anime/AnimeTrackingSettingsRepository.kt`, `features/anime/AnimeTrackingAuthStorage.kt`.

### General profile-settings sync — explicit allowlist, not repository auto-discovery

- `ProfileSettingsSync` owns a fixed set of 21 observed/exported feature groups. The export/apply contract is explicit in `MobileProfileSettingsBlob`; it is not derived from every repository that responds to `ProfileRepository.selectProfile()`.
- The sync payload intentionally strips selected profile credentials before upload and preserves local credentials when applying a remote payload for player, debrid, TMDB, MDBList and AI assistant settings. Preserve this security boundary; do not replace the allowlist with broad storage serialization.
- Global app language remains outside this profile payload; see the preceding canonical finding.
- `TrackingSettingsRepository` is a facade/typealias over the existing `TraktSettingsRepository`, and the general profile payload already exports the Trakt settings payload. This is a **COMPATIBILITY FACADE / SHARED PERSISTENCE**, not a second tracking-settings store.
- `LibraryDisplaySettingsRepository` has its own local storage adapter and reloads on profile changes, but its payload is absent from `ProfileSettingsSync`. The inspected code does not establish that it is meant to roam between devices. **NEEDS INVESTIGATION / PRODUCT DECISION:** decide whether library layout and sort are device-local presentation preferences or profile-cloud preferences before adding them to sync. Do not silently change sync behavior as part of category reorganization.
- `AnimeTrackingSettingsRepository` registers as a `TrackingProfileStore` and persists provider preferences/mappings through `AnimeTrackingAuthStorage`; these settings are not in the general profile-settings blob. Verify the Android/iOS storage scope and any dedicated provider sync before treating this as a missing feature. **NEEDS INVESTIGATION**, not a confirmed sync defect.

### Home catalog sync — separate intentional pipeline

- Home catalog settings have a dedicated `HomeCatalogSettingsSyncService`, separate from the general `ProfileSettingsSync` blob. It uses its own pull/push RPCs, shared payload shape, legacy platform compatibility constants, initial-pull token keyed by user/profile, and a push gate that waits for initial pull completion.
- The general profile blob does not contain the home catalog payload; this is **INTENTIONAL PARALLEL / DIFFERENT SYNC DOMAIN**, not a duplicate implementation. Do not merge the two sync services or remove legacy compatibility handling without auditing server RPC/schema behavior and cross-client migration.
- Home catalog preferences include catalog ordering, enabled state, custom titles and hero-source controls. The current dedicated sync payload is narrower than the full local repository state; confirm field-by-field compatibility before extending it. No change recommended from this inspection alone.

### Profile switch and concurrency boundary

- `ProfileRepository.selectProfile()` fans out `onProfileChanged()` calls across feature repositories, then clears/reloads dependent caches. This lifecycle fan-out is distinct from cloud sync registration and should remain explicit until every repository has a verified lifecycle contract.
- `ProfileSettingsSync.pull(profileId)` checks the active profile before and after the network fetch, then applies the remote blob through a sequence of repository/storage writes. The active-profile check occurs immediately before the multi-repository apply, not atomically with the entire apply sequence. **NEEDS INVESTIGATION:** determine whether profile switching can interleave with remote apply on real call paths; do not label it a confirmed race without lifecycle/coroutine evidence or a reproducer.
- The sync observer debounces local state changes and rechecks the active profile around export/push. Preserve these guards and the remote-apply suppression signature when reorganizing settings.

### Classification and decision

- General profile settings blob vs dedicated home catalog sync: **INTENTIONAL PARALLEL / DIFFERENT SYNC DOMAINS**.
- Tracking settings facade over Trakt repository: **COMPATIBILITY FACADE / SHARED PERSISTENCE**.
- Library display preferences absent from cloud blob: **NEEDS INVESTIGATION / DEVICE-LOCAL VS PROFILE-CLOUD PRODUCT DECISION**.
- Anime tracking preferences absent from general blob: **NEEDS INVESTIGATION**, verify platform storage and dedicated provider sync.
- Remote blob apply vs concurrent profile switch: **NEEDS INVESTIGATION**, no confirmed defect.
- No new TRUE DUPLICATE found.

**Verification:** static GitHub source reads at the recorded application source HEAD; current branch and audit blob were rechecked immediately before this documentation write. No app source edits, database/RPC schema audit, build, tests, device profile switching or multi-device sync verification were performed.


## Profile-scoped local storage: anime tracking and library display — 2026-11-20 16:31 IST

**Target:** `Main-dev`  
**Verified branch HEAD before write:** `6854e339345cf8db6a28e4d7587532004d0754c3`  
**Previous audit document blob / write guard:** `486e16173871ddcfdd5744bad37614a028410807`  
**Files inspected:** `core/storage/ProfileScopedKey.kt`, `features/anime/AnimeTrackingSettingsRepository.kt`, `features/anime/AnimeTrackingAuthStorage.kt`, Android/iOS actual storage implementations, `features/library/LibraryDisplaySettings.kt`, Android/iOS library storage actuals, `features/profiles/ProfileRepository.kt`.

### Anime tracking settings and credential storage

- `AnimeTrackingSettingsRepository` implements `TrackingProfileStore`, registers with `TrackingProviderRegistry`, and reloads through the registry's profile-change lifecycle. Its preferences and manual media mappings are serialized into `AnimeTrackingAuthStorage.saveSettings()`.
- Both Android and iOS store the settings and non-secret provider metadata under `ProfileScopedKey`, which appends the active profile ID. The preference/mapping payload is therefore profile-scoped locally.
- Android encrypts provider secrets using AES/GCM with a key held in Android Keystore; iOS stores provider secrets in Keychain. This is an **INTENTIONAL PLATFORM IMPLEMENTATION** split, not duplicated code to consolidate.
- Android profile removal clears the profile-scoped settings, metadata and provider secret keys. iOS removes the profile-scoped settings/metadata and profile-specific Keychain secrets. Preserve platform-specific secure storage and cleanup semantics.
- The repository is not included in the general `ProfileSettingsSync` export/apply allowlist, and the inspected source tree contains no dedicated AnimeTracking settings sync service. **NEEDS INVESTIGATION / PRODUCT DECISION:** decide whether anime provider preferences and manual mappings are intentionally device-local or should roam with profile cloud settings. Do not sync access/refresh tokens, OAuth state or PKCE verifiers as part of this decision; credentials remain local secure-storage material.

### Library display preferences

- `LibraryDisplaySettingsRepository` stores layout mode and sort option as a serialized payload. Android SharedPreferences and iOS NSUserDefaults both use the same profile-scoped key contract.
- `ProfileRepository.selectProfile()` explicitly invokes `LibraryDisplaySettingsRepository.onProfileChanged()`, confirming profile-specific local presentation state.
- The payload is absent from the general profile cloud-settings blob. No dedicated library-display sync service was found in the inspected tree. **NEEDS INVESTIGATION / PRODUCT DECISION:** determine whether layout/sort should remain device-local per profile or sync across devices. Do not silently add it to cloud sync during settings IA work.

### Duplicate / compatibility classification

- Android SharedPreferences vs iOS NSUserDefaults/Keychain/Android Keystore: **INTENTIONAL PARALLEL** platform storage implementations.
- Anime tracking preference repository vs general TrackingSettings facade: **SHARED PATTERN / DIFFERENT SEMANTICS**; anime provider-specific scrobble preferences and manual mappings are not the Trakt-backed library/watch-progress source preference facade.
- ProfileScopedKey: **SHARED PATTERN**, one common key contract used by platform storage adapters.
- No confirmed duplicate or sync defect identified.

**Verification:** static GitHub reads at `6854e339345cf8db6a28e4d7587532004d0754c3`; Android/iOS storage actuals and profile lifecycle were inspected. No secrets were retrieved. No cloud schema/RPC behavior, live provider sync, build, tests, device profile deletion, or multi-device behavior was verified.


## Settings navigation restoration and profile-sync concurrency recheck — 2026-11-20 16:23 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 73f81d4efd127484e83dab932ab52ea8d45b32ba  
**Previous audit document blob / write guard:** 067a76fbdebc85a9bc48d55fb55a2b0734954567  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0 (the current Main-dev tip is docs-only after this source baseline).  
**Files inspected:** features/settings/SettingsScreen.kt, features/settings/SettingsModels.kt, navigation/NuvioNavigator.kt, core/sync/ProfileSettingsSync.kt, features/profiles/ProfileRepository.kt, commonTest/features/profiles/ProfileSelectionRoutingTest.kt, and the commonTest tree inventory.

### Settings navigation state and restoration

- SettingsScreen parses initialPageName and its saveable currentPage using runCatching { SettingsPage.valueOf(...) }, falls back to Root for unknown or policy-disabled pages, and keys the saved page state by initialPageName. Keep this defensive behavior and preserve enum route names while reorganizing the visible taxonomy.
- SettingsPage.previousPage() is still a direct read of the enum static parentPage. It cannot retain the page from which a shortcut opened a destination. This re-confirms the existing canonical Content Discovery → TMDB contextual-back mismatch; no second back-stack implementation or new duplicate route was found.
- NuvioNavigator owns the app-level Navigation 3 back stack and delegates root back to its external callback when the stack has one entry. The settings screen internal page state and the app-level route stack are separate layers with an explicit external-navigation handoff. **COMPATIBILITY CONTRACT:** retain that handoff; do not replace it with a second navigator or assume that changing SettingsPage parents alone updates app-level routes.
- Tablet/TV sidebar category state remains rememberSaveable as a category-name string and is immediately decoded with SettingsCategory.valueOf(selectedCategory). Unknown/renamed persisted names can throw before the existing page fallback is reached. This re-confirms the canonical category-migration risk. Add a safe parser/mapping for legacy Account, General, Enhanced, About and Advanced names before changing category identifiers.
- TraktAuthentication remains categorized as Account in the enum, although the Tracking page owns provider cards, sync source selection and tracking preferences. Keep the previously recorded Services placement recommendation; retain the enum route name for saved-state compatibility.

### Test coverage and verification boundary

- The inspected commonTest tree contains no settings-navigation test for saved page restoration, unknown page/category values, contextual back, requested-page handoff, or tablet category migration.
- ProfileSelectionRoutingTest tests profile-card selection callbacks only; it does not test settings routes or settings restoration. Existing deep-link parser tests likewise do not prove settings navigation after app recreation.
- **Decision:** add pure parser/legacy-name tests and route/back restoration tests in the eventual implementation plan. Runtime process recreation, Android back handling, tablet layout and iOS navigation remain **NOT DEVICE VERIFIED**.
- Compose resource default-locale fallback and translated text rendering remain **NOT VERIFIED**; static key coverage does not establish runtime fallback or layout quality. Keep this as a release verification task, not a claim of missing default strings.

### Profile settings sync apply vs profile switch — race status refined

- ProfileSettingsSync.pull(profileId) is serialized with its own syncMutex; it checks the active profile before export, after network fetch and immediately before applying the remote payload.
- applyRemoteBlob() is synchronous and performs sequential storage/repository writes without a suspension point in the inspected implementation. ProfileRepository.selectProfile() does not acquire the sync mutex and synchronously fans out profile-change callbacks.
- **NEEDS INVESTIGATION, not a confirmed race:** the current source does not prove that profile selection can interleave with the non-suspending apply on the same dispatcher, nor does it prove all callers use one dispatcher. Do not label this a data-corruption bug or add locking without tracing actual pull launch/call contexts and demonstrating an interleaving. Preserve the active-profile guards and sync mutex.
- No new duplicate implementation found. Settings internal navigation vs app Navigation 3 is an intentional layered handoff, not duplicate navigation.

**Classification:** static parent/contextual back = existing CONFIRMED NAVIGATION LIMITATION; tablet category decoding = existing CONFIRMED COMPATIBILITY RISK; settings navigation test coverage = TEST GAP / NEEDS VERIFICATION; sync apply/profile switch = NEEDS INVESTIGATION; app navigator handoff = INTENTIONAL LAYERING.

**Verification:** GitHub source reads at application HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0; live Main-dev and main refs checked before this write. No app source changes, builds, tests, Android/iOS runtime checks or locale-rendering checks performed.

## Localization key coverage sweep — 2026-11-20 16:26 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 394e5bf73e6c9579cd1a39e45db27e0f957c76d5  
**Previous audit document blob / write guard:** 5a8222d737b55f5275fe53ae53eda0d6339fe6f3  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Files inspected:** commonMain/composeResources/values/strings.xml; all 23 locale strings.xml files under values-{ar,bg,cs,de,el,es,fr,he,hu,id,in,it,ja,nb,nl,pl,pt-rBR,pt,ro,ru,sk,tr,vi}; features/settings/AppLanguage.kt.

### Key coverage (static XML inventory; not a translation-quality or runtime test)

The default strings.xml contains 2,930 string entries. For a settings-relevant key inventory, prefixes were grouped into five overlapping-but-explicit domains: settings pages/category (892 keys), tracking/anime (158), addons/plugins (97), playback/player/P2P (340), and appearance/theme (103). Counts below are missing keys in each locale-specific strings.xml compared with the default resource file. A missing locale entry is not proof of a blank UI; actual Compose resource fallback and rendered output were NOT runtime-verified.

| Locale | Settings /892 | Tracking & anime /158 | Addons & plugins /97 | Playback /340 | Appearance /103 |
|---|---:|---:|---:|---:|---:|
| ar | 201 | 142 | 3 | 97 | 58 |
| bg | 87 | 78 | 12 | 70 | 5 |
| cs | 232 | 142 | 12 | 99 | 58 |
| de | 520 | 142 | 62 | 203 | 62 |
| el | 94 | 78 | 12 | 70 | 29 |
| es | 226 | 142 | 12 | 95 | 58 |
| fr | 209 | 118 | 12 | 97 | 58 |
| he | 743 | 157 | 65 | 285 | 81 |
| hu | 195 | 142 | 12 | 95 | 51 |
| id | 402 | 142 | 61 | 182 | 60 |
| in | 402 | 142 | 61 | 182 | 60 |
| it | 226 | 142 | 12 | 95 | 81 |
| ja | 234 | 142 | 12 | 101 | 58 |
| nb | 426 | 142 | 62 | 198 | 60 |
| nl | 228 | 142 | 12 | 99 | 82 |
| pl | 185 | 142 | 12 | 93 | 45 |
| pt | 228 | 71 | 12 | 106 | 59 |
| pt-rBR | 416 | 71 | 62 | 189 | 60 |
| ro | 228 | 142 | 12 | 99 | 82 |
| ru | 201 | 142 | 3 | 97 | 58 |
| sk | 232 | 142 | 12 | 99 | 58 |
| tr | 388 | 142 | 52 | 197 | 58 |
| vi | 195 | 142 | 12 | 97 | 51 |

### Canonical localization findings

- Tracking/anime is the most consistent gap: 142 of 158 default keys are absent from most locale files (Portuguese files are missing 71; French 118; Arabic and most others 142). This includes provider, mapping, scrobble and anime-tracking UI copy. Keep this as one localization workstream; do not create per-screen duplicate findings.
- Settings and Playback are also uneven. German is missing 520/892 settings keys and 203/340 playback keys; Hebrew 743/892 and 285/340; Norwegian 426/892 and 198/340; Turkish 388/892 and 197/340. Bulgarian and Greek have much stronger coverage in these groups. These are static key-presence counts, not a judgement of translation correctness.
- Addons/plugins coverage is comparatively complete in Arabic, Czech, Greek, Spanish, French, Hungarian, Italian, Japanese, Dutch, Polish, Portuguese and Romanian (12 or fewer missing), but has larger gaps in German (62), Hebrew (65), Indonesian (61), Norwegian (62), Turkish (52), and Brazilian Portuguese (62).
- Appearance/theme has 5–82 missing keys depending on locale. Several locales lack the newer card-depth, app-icon, navbar and Liquid Glass strings; keep these attached to the existing canonical localization gap.

### Legacy Indonesian locale alias — NEEDS INVESTIGATION

- AppLanguage exposes Indonesian as code id; it does not expose in. Both values-id/strings.xml and values-in/strings.xml exist and each contains 1,557 string keys. They are not exact duplicates: 13 key values differ.
- In values-in, 12 download-setting entries (including labels for download mode, quality and status) contain Hindi text while the corresponding values-id entries are Indonesian. One Liquid Glass description differs between the two Indonesian variants.
- Classification: **NEEDS INVESTIGATION / LEGACY LOCALE ALIAS**, not a confirmed duplicate and not safe to delete from this audit. Determine whether values-in is retained for deprecated device locale tags, generated compatibility, or is an orphan before any cleanup. Do not alter AppLanguage codes or locale files as part of settings IA without an explicit migration decision.

**Decision:** carry the locale coverage table and the id/in alias discrepancy into the user's later settings plan. Prioritize a resource-backed translation pass for tracking/anime, playback and the large settings surfaces. Preserve the default English resource as the reference key set; add a translation-completeness check to CI or tests only after inspecting existing build conventions.

**Verification:** static GitHub reads at application source HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0; all 23 locale string files and AppLanguage enum inspected. No translation edits, XML parser/build validation, app locale switching, fallback rendering, Android/iOS device verification, builds or tests performed. Protected main was rechecked and remains unchanged.

## Plugin settings persistence and Addons / Plugins / CloudStream interaction-contract recheck — 2026-11-20 16:26 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 8ec8404a3b7de8c6ba818f9ce2c6648c3257ec5a  
**Previous audit document blob / write guard:** 6dcd423f9b48f08284f34bab9c85a0b6e082d161  
**Mew application source HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Mother upstream enhanced HEAD:** 3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6  
**Files inspected:** Mew fullCommonMain PluginSettingsDialog.kt, PluginsSettingsScreen.kt, PluginRepository.kt, PluginRuntime.kt, Android/iOS PluginPlatform storage actuals, commonMain AddonsScreen.kt and AddonRepository.kt, CloudStreamSettingsSection.kt, PluginPersistenceTest.kt; matching upstream PluginSettingsDialog.kt, PluginRuntime.kt and Android/iOS PluginPlatform actuals.

### Scraper configuration scope — matches upstream; do not re-key casually

- Mew Android and iOS PluginStorage both store scraper configuration under settings_${scraperId}; neither includes active profile ID. PluginRuntime reads that same global key and passes its JSON map into scraper execution. The runtime API does not receive a profile ID for scraper configuration.
- Plugin repository metadata and cached scraper source code are profile-scoped by profileId; scraper configuration is a separate app-global key. PluginPersistenceTest covers metadata/source-code persistence and migration, not per-profile settings isolation.
- The current Enhanced upstream Android/iOS PluginStorage and PluginRuntime use the same settings_${scraperId} key and profile-independent runtime lookup. This is **ALREADY IMPLEMENTED / UPSTREAM PARITY** and **INTENTIONAL DIFFERENT PERSISTENCE SCOPE** inside the plugin subsystem, not a Mew-only regression or a confirmed duplicate.
- Preserve this behavior during settings IA work. If product requirements later demand per-profile plugin configuration, treat it as a data migration and runtime-contract change: include profile ID in settings reads/writes, handle legacy global values, define copy/migration behavior and add cross-profile tests. Do not change it as incidental cleanup.

### Plugin settings dialog interaction contract

- PluginSettingsDialog parses layout JSON as an array and saved settings JSON as an object; either parse failure silently falls back to an empty structure. This remains the canonical **NEEDS INVESTIGATION** data-loss/diagnostic finding. Do not overwrite source payload merely by opening the dialog.
- Text/select/toggle controls mutate an in-memory map. Cancel and Dialog onDismissRequest close without calling save; Save serializes the map to PluginStorage and then dismisses. No explicit schema validation or required-field enforcement is present in the inspected dialog.
- The manifest isPassword flag is read for text fields but is not used to switch input transformation or reveal/hide behavior. This remains the existing canonical credential-visibility/security finding; do not create a duplicate record.
- Dialog title suffix and Cancel/Save copy are hardcoded English in the existing implementation; attach to the canonical localization gap. The dialog is scrollable; runtime keyboard/focus, screen-reader labels, narrow-screen sizing and dismissal behavior remain NOT DEVICE VERIFIED.

### Addons and CloudStream interaction contract

- AddonsScreen uses one shared NuvioStatusModal state for Checking, Success and Error. While Checking, its confirm action is disabled by the busy-state guard; success/error close through the confirm action. Install remains delegated to AddonRepository.addAddon(). The missing upstream tracking-consent classification/confirmation is the already-recorded HIGH priority privacy finding; no second consent finding is created.
- CloudStreamSettingsSection has no modal dialog family in the inspected screen. Repository add/edit/refresh/remove, search/filter and plugin install/bulk-install feedback are inline cards/messages. The security acknowledgement is a persistent explicit state; bulk install and per-plugin install actions are gated by securityWarningAccepted. Preserve these gates and the distinction from generic Addons/Plugins.
- Addons, generic Plugins and CloudStream remain **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS** (separate repository models, install/security/update lifecycles), not a consolidation target. Existing thin page wrappers remain compatibility/navigation layers.
- No new TRUE DUPLICATE or new dialog defect was confirmed in this recheck. The existing JSON fallback, password visibility, hardcoded copy and addon tracking-consent findings remain canonical.

**Classification:** global scraper settings vs profile-scoped plugin metadata/code = **INTENTIONAL DIFFERENT PERSISTENCE SCOPE / UPSTREAM PARITY**; PluginSettingsDialog = existing canonical validation/credential/localization findings; Addons vs Plugins vs CloudStream = **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS**; no new duplicate.

**Verification:** static GitHub source reads at Mew source HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0 and Enhanced HEAD 3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6. No code changes, schema changes, runtime plugin execution, settings isolation tests, builds, tests or device checks performed. Protected main rechecked and unchanged.

## Settings route restoration and add-on deep-link consent caller recheck — 2026-11-20 16:29 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 367daf7ce5db493e9624766a5f6683f7e2f6006e  
**Previous audit document blob / write guard:** 828c59bf871ed3ec402cbfd98cf07f7e107653d7  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Files inspected:** App.kt, RootTabHost.kt, navigation/Routes.kt, navigation/NuvioNavigator.kt, features/settings/SettingsScreen.kt, features/settings/SettingsModels.kt, core/deeplink/AppUrlBridge.kt and AppDeepLinkRepository callsites, AddonsScreen.kt, AddonRepository.kt.

### Navigation restoration and handoff — static behavior clarified

- MainAppContent creates the Navigation 3 back stack with rememberNavBackStack(navigationSavedStateConfiguration, initialRoute). SettingsPageRoute is serializable and explicitly registered in the NavKey polymorphic serializers module. NavDisplay uses rememberSaveableStateHolderNavEntryDecorator, so each route entry owns saveable Compose state while it remains in the back stack.
- SettingsPageRoute stores pageName as a String, not a SettingsPage enum. The entry passes it to SettingsScreen(initialPageName); unknown/renamed values are defensively mapped to Root. Preserve old SettingsPage enum names or add explicit aliases/migration before renaming pages/categories, otherwise restored old route entries can silently land on Root.
- Native-navigation mode pushes each SettingsPageRoute and SettingsScreen delegates Back to the guarded app navigator callback. In the non-native/tablet in-screen mode, page changes remain internal and Back uses SettingsPage.previousPage() static parentPage. Therefore the previously recorded Content Discovery → TMDB contextual-back mismatch is specifically an internal/tablet navigation limitation; do not replace the app-level Navigation 3 stack or create another navigator to fix it.
- RootTabHost wraps tab state in a SaveableStateProvider and keys the host by profileId. This makes tab UI state profile-bound and resets it when profileId changes; treat that as existing lifecycle behavior, not a duplicate settings stack. Tablet selectedCategory still has the separately recorded unsafe valueOf migration risk.
- AppDeepLinkRepository handles AddonInstall by selecting the Settings tab, navigating to AddonsSettingsRoute, then directly calling AddonRepository.addAddon(manifestUrl) and showing a toast. The AddonsScreen also calls AddonRepository.addAddon(requestedUrl). This is an additional concrete caller of the same install transaction.

### Addon consent finding — callsite coverage expanded

- The confirmed missing tracking-resource consent gate affects both the AddonsScreen manual-add path and the App.kt AddonInstall deep-link path. A UI-only consent dialog added inside AddonsScreen would leave the deep-link path unprotected.
- **Implementation constraint for the later approved plan:** centralize consent-required classification/result and exact-manifest acceptance in the shared install transaction or a shared coordinator used by every caller. The deep-link path must pause and request consent rather than silently installing; preserve profile/session/duplicate checks and do not mark the deep link consumed before the consent decision and install outcome are safely handled.
- This is an extension of the existing canonical HIGH priority privacy finding, not a second addon-consent issue.

### Test coverage status

- Static route registration and saveable route-entry behavior are confirmed by source inspection. No test covers SettingsPageRoute serialization/restoration, invalid legacy page names, tablet category restoration, native vs tablet Back behavior, or AddonInstall deep-link consent routing.
- AppUrlBridge parser tests establish parsing only; they do not establish the end-to-end AppDeepLinkRepository → route → install UI/consent flow. Add route serialization/legacy alias and consent caller tests to the eventual implementation plan.
- **Runtime status:** process recreation, native iOS SwiftUI navigation handoff, Android back handling, tablet/TV restoration, RTL/locale rendering and actual device behavior remain NOT VERIFIED.

**Classification:** Navigation 3 route stack vs SettingsScreen internal page state = **INTENTIONAL LAYERING / DIFFERENT MODES**, not duplicate; SettingsPageRoute pageName compatibility = **COMPATIBILITY CONTRACT**; static parent back = existing **CONFIRMED INTERNAL/TABLET LIMITATION**; AddonInstall deep link bypass risk = existing **CONFIRMED PRIVACY GAP / SHARED CALLER COVERAGE**; no new TRUE DUPLICATE.

**Verification:** static GitHub source reads at application HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0. No code changes, route serialization tests, process recreation, device/simulator checks, builds or tests performed. Protected main rechecked and unchanged.


## Tracking, Account, Profile and Diagnostics dialog interaction pass — 2026-11-20 16:30 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 6b88f6b7ce2112dcf64898cecc612a8f80da4342  
**Previous audit document blob / write guard:** f8a1d3a69307d622c44d0cdcf94ed786959fadb1  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Files inspected:** TrackingProviderCards.kt, TrackingSettingsPage.kt, TrackingAdaptivePicker.kt, SimklSyncInfoDialog.kt, AccountSettingsPage.kt, ProfileEditScreen.kt, PinEntryDialog.kt, AdvancedSettingsPage.kt, SettingsSecretTextField.kt, AiAssistantSettingsPage.kt, MdbListSettingsPage.kt, TmdbSettingsPage.kt, DebridSettingsPage.kt.

### Tracking and provider authorization
- Trakt/Simkl/anime cards share TrackingConnectionCard with provider-specific copy/callbacks. Awaiting-approval exposes Resume and Cancel; connected state exposes Sync where supported and a destructive Disconnect confirmation. Browser-launch failure is surfaced inline. **SHARED PATTERN / DIFFERENT PROVIDER SEMANTICS**, not duplicate provider cards.
- TrackingDisconnectDialog uses generic Disconnect action text and provider-specific confirmation title/body. Cancel/outside dismissal leave the connection intact; confirmation invokes the provider callback.
- TrackingAdaptivePicker is shared: phone uses NuvioModalBottomSheet with animated dismissal; tablet uses BasicAlertDialog with explicit Close. Selection applies immediately and dismisses. The selected option is visually marked by a check icon through SettingsNavigationRow; explicit radio/selected accessibility semantics remain NOT VERIFIED on TalkBack/VoiceOver.
- Simkl manual client ID uses SettingsSecretTextField and explicit Save. SimklSyncInfoDialog catches browser-open failures and displays an inline error.
- No new tracking dialog duplicate or confirmed destructive-action defect.

### Account and profile dialogs
- Account sign-out requires confirmation and starts AuthRepository.signOut() after closing the modal. Account deletion has a busy state, blocks dismissal while deleting, and reports failure inline. Preserve guest-data cleanup; do not change sign-out wording until the anonymous-account flow is resolved.
- Profile deletion requires confirmation. PIN setup/removal share PinEntryDialog; failed verification clears the entered PIN and shows retry/lockout messaging with haptic feedback. Profile GIF search handles loading, empty, no-results, not-configured and failure states; selection updates the draft avatar URL.
- Profile save errors are inline. Delete, PIN setup/clear and GIF search are separate workflows; do not merge into one generic profile dialog. Focus restoration, keypad accessibility, long-name layout and in-flight cancellation remain NOT DEVICE VERIFIED. No new defect confirmed.

### Diagnostics and integration credential visibility
- Sentry enable/disable uses an explanatory confirmation dialog. Logs use a separate view dialog with Copy/Export. Log text has no bounded scroll container in the inspected source; long-log/narrow-screen layout is NEEDS DEVICE VERIFICATION, not a confirmed overflow.
- AI Assistant, MDBList and TMDB API-key inputs use SettingsSecretTextField, which applies PasswordVisualTransformation and a show/hide control. The existing credential-visibility finding is limited to the Debrid API-key dialog and manifest-defined Plugin text fields.
- Advanced Logs headings/descriptions and several actions remain hardcoded English; attach to the existing localization finding.

**Classification:** Tracking cards = SHARED PATTERN / DIFFERENT SEMANTICS; adaptive picker = INTENTIONAL PLATFORM PRESENTATION; account/profile workflows = INTENTIONAL PARALLEL ACTION FLOWS; AI/TMDB/MDBList masking = ALREADY IMPLEMENTED; log layout = NEEDS DEVICE VERIFICATION. No new TRUE DUPLICATE or confirmed dialog defect.

**Verification:** static source reads at application HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0. No provider auth, PIN attempts, Sentry toggle, log export, locale rendering, accessibility/device checks, builds or tests performed. Protected main rechecked and unchanged.



## Appearance and Enhanced backup dialogs — static interaction pass — 2026-11-20 16:32 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 7a21bb7dcca3703ad95ffea8ca887b4b0b64fc93  
**Previous audit document blob / write guard:** 298cb8875eaca16341bbaaf9129c134ba48077aa  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Files inspected:** AppearanceSettingsPage.kt, AppIconPicker.kt, NuvioEnhancedSettingsPage.kt, AppIconRepository.kt, AppIconPlatform actuals.

- Appearance language and navbar-style sheets apply the selected option immediately, show a selected check with localized accessibility description, and dismiss through the shared animated sheet helper. AppIconPicker uses phone sheet/tablet dialog presentation, exposes selected semantics, disables choices while a change is pending, surfaces change failure, and requires a confirmation modal on Android where the platform requires it.
- Custom accent controls are a pointer-driven HSV canvas with hex parsing; keyboard/screen-reader equivalent control and actual narrow-screen interaction remain NOT DEVICE VERIFIED. Keep this attached to the existing shared settings accessibility/runtime review; no new confirmed defect.
- Nuvio Enhanced backup export, copy, file import and paste-import are separate entry paths. Backup preview is scrollable; paste import validates through ProfileSettingsSync.importBackupJson, shows an inline error, and clears the draft on successful import or dismissal. File import/export failures are surfaced through toast feedback. Preserve the credential-stripping/local-credential-preservation contract in ProfileSettingsSync; do not implement a second backup store.
- App icon selection, language selection, navbar style selection and backup/import are distinct actions, not duplicate settings dialogs. No new TRUE DUPLICATE or confirmed modal defect found.

**Classification:** phone/tablet icon picker = INTENTIONAL PLATFORM PRESENTATION; language/navbar selectors = SHARED SHEET PATTERN / DIFFERENT SETTINGS; backup preview/import = DISTINCT BACKUP WORKFLOWS; custom color canvas accessibility = NEEDS DEVICE VERIFICATION.

**Verification:** static source reads at application HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0. No app icon changes, locale changes, backup import/export, color-picker interaction, accessibility/device checks, builds or tests performed. Protected main rechecked and unchanged.



## Compose resource fallback behavior — documentation verified, runtime pending — 2026-11-20 16:32 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** f412a58c44a186e06f9bb1c53a6d40078aef6d46  
**Previous audit document blob / write guard:** 4716f13fdd858ca1b6594993d30ae87ef37a65c2  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Files inspected:** gradle/libs.versions.toml (Compose Multiplatform 1.12.0, Kotlin 2.4.10), default and all 23 localized strings.xml files, AppLanguage.kt.

- JetBrains/Kotlin Multiplatform resource setup documentation states that when a resource with the requested qualifier is unavailable, the default resource without a qualifier is used. Therefore a missing key in values-xx/strings.xml should fall back to its corresponding default values/strings.xml entry rather than render blank, provided the default key exists. The default file was confirmed to contain all 2,930 base string entries used in the comparison.
- Official references: https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html (qualifier fallback); https://kotlinlang.org/docs/multiplatform/compose-localize-strings.html (localized resource directory/key workflow).
- This corrects the earlier audit wording: fallback behavior is **DOCUMENTED EXPECTED BEHAVIOR**, not wholly unknown. Actual runtime rendering on this repository's Compose 1.12.0 Android/iOS targets, app-specific language override behavior, RTL direction, plural/placeholder formatting and layout overflow remain **NOT DEVICE VERIFIED**.
- Missing localized keys remain translation coverage gaps, not missing base resources. Keep the 23-locale key-count matrix as the canonical static coverage inventory. Prioritize tracking/anime, settings and playback copy; do not add duplicate resource keys or remove legacy locale files without a migration review.

**Classification:** missing localized key with default counterpart = **EXPECTED DEFAULT-RESOURCE FALLBACK / TRANSLATION GAP**, not a blank-string bug; values-in alias discrepancy remains separate NEEDS INVESTIGATION.

**Verification:** repository version catalog and all locale XML files read at source HEAD 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0; official Kotlin Multiplatform docs checked. No build, app locale switching, RTL/locale rendering or device/simulator test performed. Protected main rechecked and unchanged.


## Audit consolidation — canonical findings and decision matrix — 2026-11-20 16:33 IST

**Target:** Main-dev  
**Verified branch HEAD before write:** 0ece7a5970b429217bfd3a74acff413dcf779045  
**Previous audit document blob / write guard:** 02dc456bc53f32a23458b3d7e85e3b430e6c8602  
**Application source audit HEAD:** 8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0  
**Scope:** consolidate existing canonical findings only; no new application behavior proposed beyond decisions already recorded above.

### Confirmed duplicates — exactly two
| Finding | Classification | Required handling |
|---|---|---|
| Parental Guide control appears in Playback and Nuvio Enhanced | TRUE DUPLICATE | One canonical setting/control owner; preserve current behavior and all callers during UI move |
| TMDB enrichment enabled toggle appears in Content Discovery and TMDB settings | TRUE DUPLICATE | One canonical setting/control owner; preserve API-key gating and enable-after-config flow |

No other TRUE DUPLICATE was confirmed. Similar cards, source-set actuals, separate sync domains, and platform-specific sheets remain classified as intentional or semantically different in their canonical sections.

### Confirmed ADAPT / fix candidates
| Priority | Candidate | Evidence-based constraint |
|---|---|---|
| HIGH / privacy | Addon tracking-resource consent | Gate both AddonsScreen manual install and App.kt AddonInstall deep link through the same install transaction; bind consent to exact manifest and re-check profile/session/duplicates |
| HIGH / security UX | Mask Debrid API-key and manifest-defined Plugin secret fields | Keep existing SettingsSecretTextField behavior for AI/TMDB/MDBList; do not broaden the finding to already-masked integrations |
| HIGH / compatibility | Safe SettingsCategory legacy parser | Preserve Account, General, Enhanced, About, Advanced saved names before category migration |
| MEDIUM / navigation | Contextual Back for Content Discovery → TMDB in internal/tablet mode | Preserve Navigation 3 app stack; native route mode already pushes SettingsPageRoute entries |
| MEDIUM / navigation | Stable SettingsPageRoute pageName compatibility | Keep existing enum names or explicit aliases for saved route restoration |
| MEDIUM / UX | Debrid per-resolution/per-quality picker titles | Use the correct title for each picker; do not alter selection/persistence semantics |
| MEDIUM / localization | Translate settings/tracking/anime/playback/appearance copy | Default strings remain the reference; missing locale keys fall back to default resources according to Compose Multiplatform documentation |
| MEDIUM / accessibility | Explicit selected/radio semantics for shared picker rows | Verify TalkBack/VoiceOver before changing shared SettingsNavigationRow contracts |
| MEDIUM / parity | Validate iOS subtitle-font import content like Android | Confirm safe parser/API and renderer failure behavior before porting; preserve current font file references |
| MEDIUM / UX | Surface subtitle-font import failures instead of silently discarding them | Preserve cancellation and profile references; do not delete font files without reference audit |
| IA | Adapt You / Look / Watch / Services / System taxonomy | Preserve Mew identity, feature policy, storage scope, route names and platform-only options |
| IA | Add Layout and About parents; split Playback into logical subpages | Reuse existing repositories and keys; do not create parallel state holders |
| IA | Split Advanced Logs into a route only if useful | Keep RuntimeDiagnostics as the single diagnostics owner |
| IA | Add subtle NEW badge with release metadata | Three app releases inclusive; app-global, non-dismissible; do not reuse profile-scoped seenFeatureIds |

### KEEP / ALREADY IMPLEMENTED / INTENTIONAL PARALLEL
- Android/iOS storage actuals for theme, player, P2P, anime tracking, library display and notifications are intentional platform implementations; preserve platform storage and cleanup behavior.
- App language is global; appearance settings are profile-scoped. Player audio/subtitle language is a separate preference domain.
- General ProfileSettingsSync uses an explicit allowlist and credential stripping/preservation; HomeCatalogSettingsSyncService remains a separate intentional sync domain.
- Anime tracking and Library display settings are locally profile-scoped but absent from the general cloud blob; do not add them to cloud sync without product decisions.
- Plugin scraper configuration is app-global under settings_${scraperId} in both Mew and Enhanced; plugin metadata/source code are profile-scoped. This matches upstream and must not be re-keyed incidentally.
- Generic Addons, generic Plugins and CloudStream are intentional parallel systems with different install/security/update lifecycles.
- Tracking provider cards share a component but retain provider-specific semantics; app locale and player track-language preferences remain separate.
- Downloads manager is a utility route, not proof that upstream Downloads preferences should be imported.

### NEEDS INVESTIGATION / PRODUCT DECISION
- Whether Library layout/sort and Anime tracking preferences/mappings should remain device-local per profile or sync across devices; credentials/OAuth/PKCE material stays local secure storage.
- Whether ProfileSettingsSync remote apply can interleave with profile switching; current source does not prove a race.
- Private viewing mode requires tracing every history/scrobble/provider/analytics/profile write path before any UI toggle.
- Anonymous guest-to-account wording requires an auth linking/upgrade and guest-data retention contract; do not copy upstream wording alone.
- Malformed Plugin layout/settings JSON fallback needs schema/version/corrupt-storage analysis before adding recovery or rewriting payloads.
- Legacy values-in locale alias: AppLanguage exposes id; values-in has 13 differing entries, including 12 Hindi download labels. Determine whether it is a deprecated locale alias or orphan before cleanup.
- Android external download-folder chooser callsite/profile scope and iOS security-scoped bookmark/background NSURLSession semantics.
- Upstream multiple IPTV playlists/manual EPG manager and other feature-level candidates remain separate data-model/provider audits, not settings-only ports.
- Theme contrast white-on-accent risk, custom color accessibility, and runtime dialog/layout/focus behavior require tests/device evidence.

### SKIP for this settings IA pass
- Upstream-only Media Servers, OMDB, Discord RPC and What's New: no corresponding Mew feature owner found; do not add empty placeholders or infer features from branding assets.
- Latest Enhanced player-logo lookup commit: outside settings IA; retain as a separate player metadata parity candidate.
- Do not wholesale-copy upstream settings files, replace Mew provider IDs, merge intentional sync pipelines, or refactor platform actuals solely to reduce file count.

### Required verification after the user approves implementation scope
- Tests: legacy category/page-name parser and route restoration; internal/tablet contextual Back; Addon consent across manual and deep-link callers; NEW release-version comparison including prerelease/invalid values; plugin schema fallback and password masking; profile/global settings scope and sync boundaries; accessibility selected semantics.
- Runtime: Android and iOS locale switching; RTL Arabic/Hebrew; CJK; long German strings; app process recreation; phone/tablet/TV layouts; keyboard/focus and screen-reader behavior; profile switch/delete; provider auth and Addon consent; iOS-specific sheets, font import and downloads.
- Builds, CI and device results must be run and reported separately. None of these verification steps were performed during this read-only audit.

**Consolidation status:** canonical findings grouped; no new application source change. The user's separate settings plan MD is intentionally not created.


## Broader upstream feature parity audit — first feature batch (2026-11-20 16:48:58 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `7891379ff8d41ab69d46164107816d6e5749597f`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0` (the commits after this baseline were documentation-only; no app-source paths changed)  
**Upstream snapshot:** `upstream-sync` `af51bbbc13cdb315a88ef597eb4d2672a60335d5`  
**Mother upstream:** `luqmanfadlli/NuvioMobile-Enhanced:enhanced` `3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6`  
**Previous audit document blob / write guard:** `abe3729753f7abadd4178d9d6315dad92ac6d532`  
**Scope:** first broader feature-parity pass across Library, Streams, Downloads, Media Servers, watch thresholds and Home row placement. Static source/tree comparison only.

### Branch divergence and audit boundary

- Comparing the common-history refs reports **69 commits ahead on mother upstream and 167 commits ahead on the Mew/upstream-sync side**, with merge base `f4b7e55e31c2778827095dbdb246193e8675016f`. The comparison exposes 140 changed paths in the returned file list.
- This is a heavily divergent history, not a safe merge candidate. Review behavior/file groups and preserve Mew-specific commits; never merge the branch histories wholesale.
- Live refs were rechecked immediately before this document write. `main` remained `207ef8fa309b8061b73039b2d4e4bde4af65050e`.

### First feature decisions

| Upstream area | Mew evidence and semantic comparison | Initial decision |
|---|---|---|
| Library genre/year/watched filters | Upstream `LibraryScreen.kt` adds genre, year and watched filter state and applies it to visible library sections. Mew's `LibraryScreen.kt` has download-list All/Movies/Shows filters and sort/layout controls; Mew's `LibraryItem` already carries genres and the screen reads watched state for badges, but the main library has no matching genre/year/watched filter controls. | **ADAPT** the filter behavior using Mew's existing Library models, WatchedRepository and display settings. Do not add a second library repository. Decide separately whether filter selections remain screen-local or become persisted preferences. |
| Smart Streams / device-aware ranking | Upstream adds a distinct SmartStream ranking/settings/device stack: instant availability, resolution/device decoder/HDR/memory suitability, preferred audio language, bitrate/data targets, filters and optional ranked auto-play. Mew has an established `StreamsRepository`, stream request-generation/lifecycle handling and `StreamSourcePreferencesRepository`, but no equivalent SmartStream module in its source tree. | **ADAPT / NEEDS INVESTIGATION.** Treat as a new ranking/presentation layer over Mew's stream lifecycle, not a replacement for StreamsRepository, provider loading, CloudStream, Debrid or existing source preferences. Audit cancellation, stale requests, fallback, external player, device capabilities and profile/global persistence before implementation. |
| Media Servers (Plex, Silo, Emby/Jellyfin) | Mother upstream has a dedicated server subsystem (55 paths in the feature tree), including account clients, providers, catalog mapping, playback, watch state, sync, storage, settings sign-in flows and provider tests. Mew has no `features/servers/` source tree at this baseline. The earlier settings audit's SKIP applied only to settings IA/placeholders, not to full feature parity. | **NEEDS INVESTIGATION / PRODUCT SCOPE.** This is a substantial new integration, not a settings-only port. Audit provider/API licensing, auth/token storage, server discovery, LAN/WAN addresses, playback headers, catalog/watch sync, profile cleanup, privacy and iOS support before deciding TAKE/ADAPT/SKIP. |
| Downloads redesign and transfer lifecycle | Upstream has a redesigned download UI, `BatchDownloadCoordinator`, queue/transfer worker and storage-stat components. Mew has a different pipeline: `EpisodeDownloadCoordinator`, `DownloadSourceResolver`, pending-source-search worker/notifications and choice sheet, its own `DownloadsRepository`, plus `DownloadsBatchFlowTest`. Both expose a Downloads screen but their queue and source-discovery lifecycles are not interchangeable. | **INTENTIONAL PARALLEL / DIFFERENT SEMANTICS.** Do not copy upstream coordinator, worker or repository wholesale. Continue a UI/UX and lifecycle comparison; adapt isolated display affordances only where they preserve Mew's pending-source, foreground-service, external-folder and iOS bookmark behavior. |
| Watched threshold | Upstream adds `WatchedThresholdSettings` and Android/iOS storage actuals to control when playback counts as watched. Mew has separate WatchedRepository and WatchProgress domains but no equivalent threshold settings module in its source tree. | **ADAPT candidate / NEEDS INVESTIGATION.** Trace every playback completion, manual watched action, resume/progress write, Trakt/Simkl/AniList/MyAnimeList sync, profile and server path before adopting. Preserve current provider-specific policies and avoid UI-only preference. |
| Home row pinning/hidden rows | Upstream adds pinned catalog rows and a hidden-rows section. Mew already has home/catalog preference ownership and a `HomePinnedCollectionOrderTest`; collection ordering is not proof of equivalent catalog-row placement or hidden-row behavior. | **NEEDS INVESTIGATION.** Compare row identity, catalog reorder, selected hero sources, collections, profile/global scope and cloud-sync contract. Do not duplicate existing collection pinning or accidentally sync device-local placement. |
| Preferred audio stream ranking | Upstream adds preferred-audio-language ranking and later incorporates language into SmartStream scoring. Mew already stores preferred audio language in player preferences; no equivalent ranking behavior was established in this first pass. | **Fold into the Smart Streams audit**, not a separate implementation. Verify stream metadata language parsing and unknown-language fallback. |
| Missing title-logo lookup | Latest upstream commit adds a playback-time title-logo lookup when metadata has no logo. Mew has existing metadata enrichment and player presentation paths, but the equivalent caller/lookup behavior has not yet been fully traced. | **NEEDS INVESTIGATION**, separate player-metadata candidate; not a settings task. |

### Files and evidence inspected in this pass

- Mew: `LibraryScreen.kt`, `LibraryModels.kt`, `LibraryRepository.kt`, `DownloadsScreen.kt`, `DownloadsRepository.kt`, `StreamsRepository.kt`, source-tree inventories and tests.
- Mother upstream: corresponding Library/Downloads screens; `SmartStreamRanking.kt`, `SmartStreamSettings.kt`, `BatchDownloadCoordinator.kt`, `WatchedThresholdSettings.kt`, server subsystem tree and relevant commit/file comparison.
- Source-tree counts are discovery evidence only; path presence/absence is not treated as proof of semantic equivalence.

### Next audit batch

1. Complete Smart Streams vs Mew stream loading, source ranking, playback fallback and device capability mapping.
2. Compare Downloads transfer/queue state, persistence, Android workers, iOS file movement/bookmarks and UI behavior.
3. Trace watched-threshold semantics through all watch/progress/provider callers.
4. Inspect Home catalog row placement vs Mew collection ordering and sync.
5. Continue Media Servers feasibility/security audit and player title-logo lookup.
6. Continue remaining upstream-only commits and classify each affected behavior/file group.

**Verification:** read-only GitHub source/tree/commit inspection. No application source changes, builds, tests, merges, promotions or releases. Android/iOS runtime and device checks remain **NOT RUN**.


## Smart Streams semantic comparison — Mew AutoPlay compatibility (2026-11-20 16:50:39 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `f48579352809e358c602af795a3f5b39b5ab25ac`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `88c674b00cedc282179f1a3bd19c43c36dbbc3a0`  
**Files inspected:** Mew `StreamsRepository.kt`, `StreamsScreen.kt`, `StreamModels.kt`, `StreamAutoPlaySelector.kt`, `StreamAutoPlayModels.kt`, `PlayerSettingsRepository.kt`, Android/iOS `PlayerDeviceStatus` actuals; upstream `SmartStreamRanking.kt`, `SmartStreamSettings.kt`, `SmartStreamQuality.kt`, `SmartStreamDevice.kt`, `SmartStreamControls.kt`.

### Confirmed Mew behavior that Smart Streams must preserve

- Mew already has a complete AutoPlay policy domain: `MANUAL`, `FIRST_STREAM`, `REGEX_MATCH`; source scope (`ALL_SOURCES`, installed addons only, enabled plugins only); addon/plugin allowlists; regex matching; persisted binge-group preference, filename and info-hash identity; and readiness checks for direct/debrid streams.
- `StreamsRepository` owns request identity and cancellation: it reuses unchanged requests, increments `activeRequestGeneration`, cancels the prior job, rejects stale-generation results, and manages cache enrichment, timeout and manual-picker fallback. This is a regression-sensitive lifecycle contract.
- AutoPlay is not simply “choose the highest-scoring stream”: it applies source eligibility and episode identity first, distinguishes ready vs pending Debrid candidates, and preserves existing selection order/policy.
- Mew's `StreamItem` already carries structured `resolution`, `quality`, `hdr`, `codec`, cache status and local Debrid-resolution flags. Avoid parsing the same metadata into a second competing model when the existing structured fields are reliable; inspect provider population/normalization before relying on them.
- Preferred and secondary audio language are already stored by `PlayerSettingsRepository`. Do not create another preference store for Smart Streams language ranking.
- Mew's `PlayerDeviceStatus` abstraction currently exposes time, battery and network type; its Android/iOS actuals do not provide the upstream Smart device profile (display/video lines, decoder/HDR capability and memory tier). Upstream's `SmartStreamDevice` uses a separate capability model and memory information. Capability detection therefore needs a platform-specific audit, not a blind port.

### Refined decision

**ADAPT the ranking/filtering behavior; do not TAKE the upstream AutoPlay implementation wholesale.** A compatible design must:

1. Keep Mew's AutoPlay mode, regex, addon/plugin source allowlists, binge-group identity, Debrid readiness, request-generation cancellation, timeout and manual fallback as the authoritative eligibility/lifecycle policy.
2. Apply Smart ranking only to eligible candidates and define deterministic behavior for unknown resolution/codec/HDR/language/size/cache metadata.
3. Reuse Mew's preferred/secondary audio-language settings and existing StreamItem fields where semantically valid; do not add duplicate persisted controls or models.
4. Add or extend Android/iOS device-capability providers only after checking available APIs, permissions, fallback values and testability. Do not infer hardware decode support from screen resolution alone.
5. Decide explicitly whether Smart ranking is a manual sort mode, an optional ranking assist within existing AutoPlay modes, or a new user-visible mode. Do not introduce a second independent AutoPlay toggle that conflicts with Mew's current settings.
6. Add tests for stale requests, provider-order stability, source allowlists, regex, binge-group identity, pending/ready Debrid candidates, unknown metadata, preferred language and device-profile fallbacks.

**Classification:** Mew AutoPlay vs upstream Smart AutoPlay = **SHARED PATTERN / DIFFERENT SEMANTICS**; Mew stream request lifecycle = **KEEP**; structured stream metadata and player language settings = **ALREADY IMPLEMENTED / REUSE**; Smart device profile and quality ranking = **ADAPT / NEEDS INVESTIGATION**.

**Verification:** static source reads only. No ranking code, settings, models, tests, builds, playback or device behavior changed or run.


## Media Servers — authentication, credential storage and sync security pass (2026-11-20 16:51:31 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `366f8858225b416363e87ea3eec2db628e59d179`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `751a17c63291f3c2f906846de98cc34e96aa5b41`  
**Files inspected:** upstream `ServerStorage.kt` and Android/iOS actuals, `ServerRepository.kt`, `ServerSync.kt`, `PlexAccountClient.kt`, `SiloClient.kt`; Mew source-tree absence of a `features/servers/` subsystem confirmed.

### Confirmed upstream contracts

- Android local server credentials are stored in app-private SharedPreferences encrypted with AES/GCM using an Android Keystore key; key-specific AAD is applied. Failed decryption removes the unreadable entry. iOS uses the Keychain generic-password APIs.
- Server repository state is profile-scoped: it tracks the loaded profile, clears in-memory tokens on profile changes, deletes per-profile credential references on profile removal, and uses a generation guard when connection work completes.
- Token renewal is serialized per connection and only replaces a token if the same profile is still loaded and the previous token still matches.
- **Security-sensitive sync contract:** `SyncedServer` contains a `token` field. `ServerRepository.syncSnapshot()` obtains each stored credential, converts it with provider `tokenForAccount()` where applicable, and includes the resulting token in the server sync snapshot. Applying remote sync writes the chosen token back into local credential storage. Local encryption therefore does not, by itself, establish protection of tokens while stored or processed by the remote sync backend.
- Plex discovery may construct local `http://` addresses from local server connections; the source comments state unencrypted addresses are only used on the home network. The reachability path filters to HTTPS or local addresses and checks that the responding machine ID matches the expected server. This needs transport/network-policy review before any port.
- Silo's client defaults to disallowing redirects; its API error mapping and bounded response handling are provider-specific and must be preserved.
- The server subsystem is not just sign-in UI: provider account clients, per-server tokens, libraries, watch-state import, catalog metadata, server playback/streams, sync, connection renewal and profile cleanup are coupled.

### Decision and mandatory gates

**NEEDS INVESTIGATION / SECURITY GATE — do not port yet.**

1. Establish the server-sync backend contract: TLS, authentication/authorization, per-profile access isolation, database/log/backup encryption, retention, export/delete semantics and whether tokens are stored as plaintext or protected server-side.
2. Determine whether sync should contain provider tokens at all, or use a separate device authorization/re-link flow. Provider-specific `tokenForAccount` and renewal semantics require review.
3. Verify credential revocation/sign-out behavior, profile switch/delete races, server removal, failed decryption, key loss and account switching.
4. Audit local HTTP policy, DNS rebinding/SSRF defenses, redirect handling, address probing timeouts, private-network access and header/token redaction. Never weaken TLS or hostname verification to make servers connect.
5. Review Plex/Silo/Emby/Jellyfin provider terms/API requirements and iOS support before choosing implementation scope.
6. Add tests for token sync isolation, profile-generation races, token renewal, remote merge, credential cleanup, redirect policy and local/remote address selection before runtime integration.

**Correction to prior settings-only SKIP:** Media Servers remain SKIP only as empty settings placeholders in the settings IA pass. For full feature parity they are an upstream-only feature candidate requiring product approval and this security audit; no final TAKE/ADAPT decision is made.

**Verification:** read-only upstream source inspection. No server feature or credentials were added to Mew; no app source changes, tests, builds or runtime/network/device checks performed.


## Downloads transfer and persistence semantic comparison (2026-11-20 16:52:35 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `02de2bae702e99cd492bdd44779be93dc48b3c23`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `4c9cca84dbb2cd574ebb36fbe8d6e2b08723b5e6`  
**Files inspected:** Mew `DownloadsModels.kt`, `DownloadSourceResolver.kt`, `EpisodeDownloadFlowSheet.kt`, `DownloadsStorage.kt` and Android/iOS actuals, `DownloadsPlatformDownloader.android.kt`, `DownloadsForegroundService.kt`; upstream `BatchDownloadCoordinator.kt`, `DownloadsSettingsRepository.kt`, `AndroidDownloadScheduler.kt`, `AndroidDownloadTransfer.kt`, `DownloadsTransferWorker.kt`, iOS `DownloadsPlatformDownloader.ios.kt`.

### Confirmed semantic differences

- Mew's `DownloadSourceResolver.kt` contains `EpisodeDownloadCoordinator` with `enqueue`, `enqueueAuto` and `retryPending`; it resolves addon, plugin, CloudStream and Debrid source candidates and manages pending source searches. Mew also has `PendingEpisodeDownload` state, retry/expiry/attempt metadata, manual-choice prompting and Android background pending-search notification/worker paths.
- Upstream's `BatchDownloadCoordinator` is a season/episode planning state machine: it tracks planned episode states, season selection, select-unwatched, source-selection progression, per-episode stream overrides and queue-plan results. This is not the same responsibility as Mew's source-discovery coordinator. The shared “batch download” label is not proof of a duplicate.
- Mew's Android file transfer uses its shared OkHttp client, temporary `.part` file, byte-range resume, retry policy, external-folder handling and a foreground service/wake lock. Mew's iOS implementation uses a background `NSURLSession` coordinator and has security-scoped folder access/bookmark behavior in its platform layer.
- Upstream Android transfer has a separate persisted transfer store and scheduler using JobScheduler on newer Android and WorkManager fallback, with generation checks and a transfer worker. Upstream iOS has a background-session path plus a separate legacy direct-to-folder mode.
- **Persistence scope differs:** Mew's Android and iOS `DownloadsStorage` actuals apply `ProfileScopedKey` to the download payload, external-folder URI and episode-download settings. Upstream `DownloadsSettingsRepository` explicitly describes its preferences (mobile-data allowance, download location, preferred resolution, concurrency and legacy iOS mode) as device-level and not synced across profiles/devices. Changing Mew's scope to upstream's device-global scope would alter existing profile behavior.
- Both implementations handle redirects in their download transport. Preserve Mew's existing shared-client/DNS/security policy and explicitly audit redirect target validation and credential-header forwarding before any transfer changes.

### Decision

**KEEP Mew's current download architecture; ADAPT only isolated UX/settings behavior after product and platform review.** Do not copy upstream `BatchDownloadCoordinator`, Android scheduler/store/worker or iOS legacy mode as replacements. Preserve Mew's pending source-search lifecycle, profile-scoped state, external folder handling, Android foreground-service behavior and iOS background-session/bookmark contract.

### Follow-up questions for the implementation plan

1. Should download settings remain profile-scoped in Mew, or should any specific preference (for example mobile-data permission) become device-global? Do not migrate the whole settings object implicitly.
2. Should Mew expose upstream-style preferred resolution, concurrency limits or watched-count summaries? Map them to existing Mew settings and download queue behavior before adding controls.
3. Is upstream's iOS legacy direct-write mode needed for a Mew-specific failure case? Compare security-scoped bookmark lifetime, move-vs-copy behavior, background completion and data-loss recovery first.
4. Audit source URL redirects and sensitive source headers across addon/plugin/CloudStream/Debrid downloads; test host changes, range resume, 401/403/416, interrupted responses, external-folder failure and cancellation.

**Classification:** Mew source discovery vs upstream batch plan = **SHARED PATTERN / DIFFERENT SEMANTICS**; Android/iOS transfer engines = **INTENTIONAL PARALLEL / PLATFORM LIFECYCLE DIFFERENCES**; Mew profile-scoped download state = **KEEP**; upstream device-level preferences = **NEEDS PRODUCT DECISION**.

**Verification:** static source reads only. No download, worker, queue, persistence or transfer code changed; no tests, builds, file I/O or device/network checks run.


## Watched threshold and short-placeholder behavior comparison (2026-11-20 16:53:47 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `9bbe1459989e702320e755154dd9ce5baf4b84bb`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `e1ada6974a5795e4d01f922f81fcd68b2dd1e59f`  
**Files inspected:** Mew `WatchingPolicies.kt`, `WatchingActions.kt`, `WatchedRepository.kt`, `WatchProgressRepository.kt`; upstream equivalents plus `WatchedThresholdSettings.kt` and Android/iOS storage actuals.

### Confirmed behavior delta

- Mew already owns the shared watch policy in `WatchingPolicies.kt`; its completion threshold is a fixed `0.90` fraction. `isProgressComplete()` accepts `isEnded` before checking duration, then marks complete at 90% for positive durations.
- Upstream moves the threshold into `WatchedThresholdSettings`, with choices 80/85/90/95/99 and a default of 90. The setting is app-global/device-local in both inspected actuals (Android SharedPreferences, iOS NSUserDefaults; no profile-scoped key).
- Upstream also adds `isShortPlaceholderDuration()`: positive durations from 1 ms through 120,999 ms are treated as error/placeholder clips. It blocks both progress storage and completion, including the `isEnded` path. Zero/negative duration is intentionally not classified as a short placeholder and continues through the normal policy.
- Mew's `shouldStoreProgress()` currently stores any position at or above 1,000 ms without checking duration, and Mew has no equivalent short-placeholder guard in the inspected policy.
- Mew's `WatchingActions.onProgressEntryUpdated()` converts completed progress into a watched mark; WatchedRepository then handles profile/source selection and provider/Nuvio sync. Manual poster/episode/season watched actions also clear progress and reconcile series state. A policy change therefore affects more than a player badge: it can change persisted watched markers and tracker sync.

### Decision

**ADAPT within Mew's existing WatchingPolicies owner; do not add a parallel watched/progress policy.**

- Candidate A: configurable completion threshold using the upstream option set and a clearly defined app-global scope (or an explicitly approved Mew-specific scope). Reuse the current policy function so every caller observes the same value.
- Candidate B: short-placeholder guard in both `shouldStoreProgress` and `isProgressComplete`, with exact boundary tests at 0, 1, 120,999, 121,000 ms and `isEnded=true`.
- Before implementation, trace all Android/iOS player progress callbacks, external-player completion, manual watched actions, resume prompts, downloads/offline playback and each connected tracking provider. Verify whether any provider-specific completion threshold or remote progress source should override local policy.
- Add regression tests for 80/85/90/95/99%, ended streams, unknown/zero duration, short placeholder clips, normal episodes, stale profile generations and provider sync side effects.
- Keep the existing WatchedRepository and WatchProgressRepository separation, profile-scoped records, source selection and sync policies. Do not move watched/progress data to app-global storage merely because the threshold preference is device-global upstream.

**Classification:** shared `WatchingPolicies` = **ALREADY IMPLEMENTED / SINGLE OWNER**; configurable threshold = **ADAPT**; short-placeholder guard = **ADAPT candidate**; watch/progress persistence and provider sync = **KEEP / REGRESSION-SENSITIVE**.

**Verification:** static source reads only. No watch policy, preference, persistence, provider sync, tests, builds or playback behavior changed or run.


## Home row placement, hidden rows and episode shuffle comparison (2026-11-20 16:54:55 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `b196d9e7fd36bd8d696f528339409de11e0653aa`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `b26ae4e01edb7eb703736ab4d601573e88d2b0e5`  
**Files inspected:** Mew/upstream `HomeCatalogSettingsRepository.kt`, `HomeCatalogSettingsSyncService.kt`, `HomeScreen.kt`, `HomescreenSettingsPage.kt`; Mew `HomePinnedCollectionOrderTest.kt`; upstream `HomeEpisodeShuffle.kt` and associated source-tree inventory.

### Row placement and hidden rows

- Mew already has a single `HomeCatalogSettingsRepository` owner for catalog ordering, enabled/hidden state, custom titles, hero-source selection and collection ordering. Its `HomeCatalogSettingsItem.isPinnedToTop` is derived from collection metadata; the Mew regression test explicitly covers **pinned collections** remaining first after manual reorder.
- Mew's persisted home preference model does not contain a general catalog `rowPlacement` field. Its settings screen has no collapsible hidden-rows section; disabled items are excluded from the visible Home rows.
- Upstream adds `HomeCatalogRowPlacement` (`NORMAL`, `PINNED_TOP`, `PINNED_BOTTOM`) for catalog rows and collections, preserves collection pinning, and adds a collapsible Hidden Rows section to Homescreen settings. Reordering is constrained within placement groups.
- Upstream's row-placement setter uses `pushRemote = false`, and the shared sync payload does not serialize `rowPlacement`. This is deliberately device-local. Mew's existing home catalog sync is profile-scoped and syncs catalog ordering/enabled/title/hero-related preferences through its own service; do not accidentally add device-local row placement to that cloud payload.

**Decision: ADAPT.** Extend the existing Mew HomeCatalogSettingsRepository and Homescreen settings UI if approved; do not create another home settings store. Preserve the existing collection `isPinnedToTop` source and migrate only with explicit compatibility mapping. Add top/bottom placement, hidden-row disclosure and within-group reorder tests. Keep new row placement local/device-only unless product explicitly chooses otherwise.

### Home episode shuffle

- Upstream adds `HomeEpisodeShuffle.kt` integration backed by a separate `features/shuffle` repository/profile model. It selects eligible episodes for Home continue-watching using per-profile settings, watched state, progress, metadata and visit/session context.
- No corresponding `features/shuffle` implementation exists in Mew's inspected source tree. Mew's Home screen already has its own Continue Watching, upcoming and hidden-content projection logic; this is not an equivalent shuffle feature.

**Decision: NEEDS PRODUCT REVIEW / ADAPT candidate.** Treat episode shuffle as a new feature, not a duplicate of Mew's continue-watching rows. Before implementation, inspect the complete upstream shuffle domain (profile settings, persistence, watched inclusion, repeat/avoid history, release eligibility, resume precedence, UI entry points and tests) and map it onto Mew's WatchProgress/Watched sources. Preserve Mew's profile-generation and provider-owned history behavior; do not add a second watch-progress store.

**Classification:** Mew pinned collections vs upstream row placement = **SHARED PATTERN / DIFFERENT SEMANTICS**; HomeCatalogSettingsRepository and sync service = **KEEP / SINGLE OWNER**; row placement and hidden-row disclosure = **ADAPT**; episode shuffle = **NEW FEATURE / NEEDS PRODUCT REVIEW**.

**Verification:** static source reads and source-tree search only. No home UI, persistence, sync or shuffle code changed; no tests, builds or device checks run.


## iOS bundled CJK subtitle-font optimization applicability check (2026-11-20 16:55:41 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `48c774739e20012f623424ba94b30fe258d765e2`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `ee712b0ea020fd9c290be9fe07f0371144a7ca43`  
**Files inspected:** upstream commit `e5000dc8c91e2035b6154e8328f74e2e6eefa7d9` patch; Mew/upstream `PlayerEngine.ios.kt`, `MPVPlayerBridge.swift` and complete source-tree font asset inventories.

- The upstream fix addresses a specific bundled iOS `NotoSans-Regular.ttf` / `subfont.ttf` CJK font (~16 MB) being pointed to from the MPV config directory and reloaded during subtitle switches. It copies the font into a dedicated caches directory and sets `config-dir` separately from `sub-fonts-dir`.
- Mew's inspected iOS source tree does **not** contain the upstream `iosApp/iosApp/Resources/Fonts/NotoSans-Regular.ttf` or `subfont.ttf` assets, and its `MPVPlayerBridge.swift` does not contain the upstream `configureBundledSubtitleFont()` path. Mew passes the selected custom subtitle font directory from `PlayerEngine.ios.kt` when applying subtitle style.
- Therefore this exact bundled-font patch is **not directly applicable** to Mew's current asset/runtime setup. Do not port the copy/cache helper or add a 16 MB bundled font as a speculative fix.
- Keep the separately recorded iOS custom subtitle-font import validation/error-surfacing candidate. If runtime profiling later shows imported custom CJK fonts being reloaded on every switch, investigate that Mew-specific path with memory measurements and tests before changing it.

**Decision:** **SKIP this exact upstream commit for Mew** (false-positive applicability at current source baseline); retain custom-font import/performance as a separate **NEEDS INVESTIGATION** item only if evidence appears.

**Verification:** source/asset tree inspection only. No iOS build, simulator, memory profiling or subtitle playback test was run.


## iOS styled-subtitle episode-transition fix applicability check (2026-11-20 16:56:41 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `3668d03709e160323ba9f1590ce410dc38aea382`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `a63d221b8674a6cf2a67097de7e4748a6ce9d2c8`  
**Upstream commit inspected:** `c5298633914d967792c79351754ba47d54dfd242`  
**Files compared:** Mew/upstream `PlayerEngine.ios.kt`, Mew `MPVPlayerBridge.swift`.

- The upstream fix changes the lifecycle of a Compose-managed styled subtitle text overlay: keep `subtitleText` state across `sourceUrl` changes, then explicitly clear it in `LaunchedEffect(sourceUrl)`. This prevents the styled overlay's state from being recreated/reset incorrectly while switching episodes.
- Mew's inspected iOS player does not contain this `subtitleText` state, `setSubtitleTextHandler` bridge callback or `StyledSubtitleOverlay` path. Mew applies subtitle styling through MPV's native subtitle style APIs; its Swift bridge has no equivalent styled text overlay implementation at this baseline.
- This is therefore not a matching Mew defect or a safe two-line port. Do not add an unused state/overlay path or refactor Mew's native subtitle renderer to imitate upstream.

**Decision:** **SKIP this exact upstream fix for Mew** (different iOS subtitle rendering architecture). Keep the existing Mew subtitle-font import validation and playback-style regression candidates separate.

**Verification:** static source comparison only. No iOS episode transition, subtitle style, build, simulator or device test performed.


## Trakt library refresh queue safety comparison (2026-11-20 16:57:24 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `fcbd7bffa17dbbda9670f8142e4fb65347dd61bb`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `1ab53cac07ceb7ce9dfd734dddeb51e49a7f7109`  
**Upstream commit inspected:** `1e41574929e2c85286d7b0eac4c0488a405f0088`  
**Files compared:** Mew/upstream `TraktLibraryRepository.kt`.

- Mew's `refresh(force)` acquires `refreshMutex` before checking cache age/loading state and keeps the mutex while fetching the snapshot. Concurrent `ensureFresh()` / forced refresh callers therefore wait on the mutex; cache deduplication happens only after each waiter acquires it.
- Upstream adds a separate `refreshJobMutex` and a tracked `refreshJob`. If an automatic refresh is already active, another non-forced caller returns instead of queueing; forced callers join the existing job. A single `refreshOnce()` still owns the actual serialized work.
- The upstream change addresses heap growth when an HTTP request remains suspended while many callers accumulate as mutex waiters. Mew's current source has the same basic waiter-queue shape, but this pass has not yet established whether Mew's HTTP transport enforces a sufficiently strict timeout or how many independent UI/background callers can trigger refresh concurrently.

**Decision: ADAPT candidate / NEEDS INVESTIGATION — potential queue-growth risk, not yet a confirmed production bug.**

1. Trace every Mew caller of `ensureFresh()`, `refreshNow()`, `getMembershipSnapshot()` and `preloadListTabsAsync()` on the current Main-dev source.
2. Inspect `httpRequestRaw` transport timeout/cancellation behavior for Trakt calls; do not assume upstream's network conditions exactly match Mew.
3. If the risk remains, adapt single-flight job ownership: automatic callers should not accumulate behind a long-running request; forced callers should join or intentionally supersede it. Preserve profile changes, cancellation, optimistic membership updates and error publication.
4. Add tests for many concurrent automatic callers, force-vs-automatic behavior, hung/timeout requests, cancellation, profile switch during refresh and cache TTL behavior.

**Classification:** Mew mutex serialization = **KEEP as single refresh owner**; upstream tracked refresh job = **ADAPT candidate**; unbounded waiter growth = **NEEDS INVESTIGATION**, not a confirmed bug until timeout and callsite audit completes.

**Verification:** static source/commit inspection only. No Trakt API calls, concurrency tests, builds or device checks run.


## Episode-rating alignment and cast filmography parity (2026-11-20 16:58:31 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `233c5eb9c6e4ea3ae5d598ea52d51cae638fb6be`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `edaa16e1128ae5ef8d453bb962300594d760e073`  
**Upstream commits inspected:** `97fbfa90565125859f973feff7a5740e8e647928`, `547972efd9fbfbde60e06cf83ae9e2d0c97fd8f4`.

### Episode ratings mapped to addon episode numbering

- Mew's `ImdbEpisodeRatingsRepository` converts external ratings directly into a `(season, episode) -> rating` map using the provider's numbering. `MetaDetailsScreen` displays those ratings against addon-provided `MetaVideo.season/episode` values. No `EpisodeRatingsAlignment` helper or addon-air-date input was found in Mew.
- Upstream adds `AddonEpisodeAirDate` / `ExternalEpisodeRating` models and aligns external ratings to addon numbering when season sets differ: same-season layouts keep provider numbering; otherwise aired episode totals are paired in airing order when counts match, or grouped by air date (including a one-day tolerance) when counts differ. Season-zero specials retain their original numbering. The repository now caches external episode records rather than a pre-mapped number map.
- This specifically addresses anime/addon catalogs that collapse multiple provider seasons into fewer addon seasons. It is not safe to remap all series indiscriminately.

**Decision: ADAPT** in the existing `ImdbEpisodeRatingsRepository` and details screen. Preserve current cache/in-flight deduplication and IMDb-primary/TMDB-fallback behavior. Add upstream's alignment algorithm only when season sets differ; keep specials stable. Tests should cover same season layouts, merged seasons, equal/different aired totals, missing/invalid dates, future episodes, specials, duplicate numbers, missing ratings and empty addon metadata. Review whether TMDB-native episode ratings need separate handling; do not silently remap those through the IMDb provider path.

### Cast filmography talk-show guest filtering

- Mew's `TmdbMetadataService.mapPersonTvCreditsFromCast()` currently keeps TV cast credits when a poster/backdrop exists, sorts by vote average and maps them into filmography. Its `TmdbPersonCreditCast` DTO does not parse `genre_ids`; there is no Talk-show guest filter.
- Upstream parses `genre_ids` and filters TMDB Talk genre ID 10767 from cast filmography unless the character/credit string identifies the person as a host/co-host. Crew credits remain a separate mapping path.
- This prevents guest appearances on talk shows from displacing actual film/series credits while retaining hosts.

**Decision: ADAPT** the focused filter in Mew's existing `TmdbMetadataService`; add `genre_ids` to the DTO and unit tests for guest, host, co-host, null character, non-talk genres and missing genre IDs. Keep movie credits and crew credits unchanged. Confirm TMDB's genre/character data shape before shipping.

**Classification:** episode ratings direct numbering = **CURRENT LIMITATION / ADAPT**; existing ratings cache and fallback = **KEEP**; cast filmography filter = **ADAPT**; no new repository or parallel metadata service required.

**Verification:** static source and upstream commit/test inspection only. No API calls, metadata UI checks, tests, builds or device checks run.


## Home hero-source selection and stale-startup item parity (2026-11-20 17:00:00 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `6b569fccfb291bf5f7ee908b5ea662d3b76232c2`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `ac692142c3e162ea9f27786d2e3394b922e23640`  
**Upstream commits inspected:** `f326d11ecf88597597f2349ca384369b84169951`, `375d93d90864d6b0774d8ab6f58c72d63d10d2af`  
**Files compared:** Mew/upstream `HomeCatalogSettingsRepository.kt`, `HomeRepository.kt`.

### Default hero-source selection

- Mew's `normalizePreferences()` defaults a newly discovered catalog to `heroSourceEnabled=true` while fewer than two sources are selected. There is no `heroSourcesChosen` marker in Mew's stored settings payload or repository state.
- If a user explicitly turns off all hero sources, the persisted rows can all be false. When a new catalog is discovered, its missing preference is treated as a default-true source and can become selected automatically. This means a new addon/catalog can override the user's explicit empty selection.
- Upstream adds a persisted `heroSourcesChosen` flag. Until the user first changes hero-source selection, defaults are selected from enabled catalogs in catalog order; after the first explicit choice (including disabling a source), later catalog additions do not silently reselect defaults.

**Decision: ADAPT.** Add an explicit-choice marker to Mew's existing HomeCatalogSettingsRepository and local stored payload. Keep its current two-source limit and catalog-order behavior. Decide deliberately whether the marker belongs in profile cloud sync; do not add it to the shared sync schema by accident. Add tests for first install, explicit all-off, one/two chosen sources, newly added catalog, reorder, remote pull and legacy payload migration.

### Startup hero items from stale sources

- Upstream's HomeRepository fix separates `heroSourceItems` from the randomized display list and retains previously visible hero items during a pending refresh only when those items still belong to the selected source set.
- Mew's HomeRepository builds catalog hero items from current `heroSourceEnabled` preferences and has a keep-visible/refresh lifecycle, but the inspected publish path has no equivalent `visibleHeroItems.allComeFrom(heroSourceItems)` guard. This is a candidate stale-hero transition issue; exact timing must be confirmed through state/lifecycle tests.
- Mew also has CloudStream and collection hero fallbacks that differ from upstream's catalog-only hero source list. Preserve those Mew-specific fallbacks when adapting the source-membership guard.

**Decision: ADAPT candidate.** Add a source-membership guard to Mew's existing hero publication flow only after testing source changes while provider refresh is pending. Do not remove CloudStream/collection fallback or change hero randomization unnecessarily.

**Classification:** new catalog auto-selected after explicit all-off = **CONFIRMED SOURCE-LEVEL DEFAULTING GAP / ADAPT**; stale visible hero during pending refresh = **NEEDS INVESTIGATION / ADAPT candidate**; HomeCatalogSettingsRepository and HomeRepository = **KEEP / SINGLE OWNERS**.

**Verification:** static source/commit inspection only. No Home UI, addon install, preference migration, cloud sync, tests, builds or device checks run.


## Player-time title-logo fallback lookup comparison (2026-11-20 17:00:49 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `e712d1609ac931802488f7fd4d23c54365997156`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `e2b679325194b6a81b093d950a7c2b15c6ddbb02`  
**Upstream commit inspected:** `3f9c50c122c9c1d63bf3f061e65bd2cb17967fb6`  
**Files inspected:** upstream `StreamDestination.kt`, `PlayerScreen.kt`; Mew `PlayerScreen.kt`, `MetaDetailsRepository.kt`.

- Upstream fills a missing player title logo in both StreamDestination and PlayerScreen: first use the launch logo, then cached metadata via `MetaDetailsRepository.peek()`, then a lightweight metadata lookup; the resolved logo is passed to player/loading UI and cached-stream paths. It skips unsupported types such as live TV/channel.
- Mew's `PlayerScreen` currently forwards the supplied `logo` directly; no equivalent player-time logo lookup was found. Mew's existing `MetaDetailsRepository` already provides `peek()` and `fetchBase()` (base metadata without optional enrichment), so a second metadata repository is unnecessary.
- Mew's content type identifiers and playback entry points differ from upstream. The upstream skip set (`tv`, `channel`) must not be copied without mapping Mew's actual canonical types and verifying parent metadata IDs for local downloads, direct links, movies, series and live channels.

**Decision: ADAPT candidate.** Reuse `MetaDetailsRepository.peek/fetchBase` at the narrowest shared player-entry point. Avoid network lookup when a launch logo or cached logo exists; do not fetch optional enrichment solely for a logo; skip unsupported/live content types; guard asynchronous completion against player/metadata identity changes. Ensure the result reaches every playback path without duplicate lookups. Add tests for supplied/cached/missing logos, type exclusions, request cancellation and episode-vs-parent metadata IDs.

**Classification:** metadata repository = **KEEP / SINGLE OWNER**; player-time logo fallback = **ADAPT**; no new metadata service or parallel cache.

**Verification:** static source/commit inspection only. No playback route, network lookup, tests, builds or device checks run.


## Player skip/next overlay placement vs end-time anchor (2026-11-20 17:01:34 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `ef41e9ed33f87ca944e52e40d0cf1f943cb14117`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `85537f14f5072d14c97d8f22f3b0e6c2041c07cd`  
**Upstream commit inspected:** `b23b28a878b78f5ef9252128b901155fee6eeea9`  
**Files compared:** Mew/upstream `PlayerControls.kt`, `PlayerPlaybackOverlays.kt`, `PlayerScreenRuntimeUi.kt`; Mew `PlayerClockEndTimeOverlay.kt`, `NextEpisodeCard.kt`, `SkipIntroButton.kt`.

- Mew renders Skip Intro at bottom-start and Next Episode at bottom-end using the same overlay-bottom padding. Mew's clock end-time display is rendered from the progress-control area; there is no shared anchor that exposes its actual bottom/right coordinates to playback overlays.
- Upstream adds `PlayerEndTimeAnchor` to the progress/end-time row, measures its root-relative bottom/right position, moves Skip Intro to the bottom-right, and stacks Next Episode / movie recommendation cards above it. It also measures skip-button height so the cards do not overlap.
- Mew has a Next Episode card but no matching movie-recommendation card in the inspected overlay. Do not copy an unrelated recommendation feature as part of this layout fix.

**Decision: ADAPT the anchor/stacking behavior only.** Reuse Mew's existing clock/end-time control and Next Episode/Skip Intro components. Preserve Mew's player lock behavior, control visibility, safe-area padding and skip dismissal state. Test phone/tablet/TV, portrait/landscape, RTL, narrow screens, end-time hidden, skip hidden/dismissed, locked controls and large text before shipping.

**Classification:** end-time control and skip/next components = **ALREADY IMPLEMENTED / REUSE**; anchor-based non-overlap layout = **ADAPT**; upstream movie-recommendation card = **NOT APPLICABLE / SKIP** for this pass.

**Verification:** static source/commit inspection only. No player UI, layout, accessibility, tests, builds or device checks run.


## iOS MPV Metal backing-scale comparison (2026-11-20 17:02:44 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `310cdfb646f099b22ecebc90be32e6bb3dc3cade`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `604473e74848f3059c074100562aeb606a39a4d7`  
**Upstream commit inspected:** `839cdcde3c5cb5124f2efb6ad5e3ff32b7549d6f`  
**Files compared:** Mew/upstream `iosApp/iosApp/Player/MPVPlayerBridge.swift`.

- Mew initializes `metalLayer.contentsScale` and computes `drawableSize` from `view.window?.screen.nativeScale ?? UIScreen.main.nativeScale` in `layoutMetalLayer()`.
- Upstream replaces the native-scale-only calculation with `renderScale()`, resolving the active window screen (fallback to main screen) and returning `max(screen.scale, screen.nativeScale)`. It uses the same resolved scale for both layer contents scale and drawable size.
- Mew's Metal layer and layout lifecycle differ from the current upstream bridge (Mew has its own `layoutMetalLayer()`, drawable-size tracking and render-context behavior). The scale correction itself is a small compatible candidate; do not replace the bridge.

**Decision: ADAPT** the scale calculation in Mew's existing Metal-layer initialization/layout helper. Keep Mew's drawable-size change detection, swapchain update and subtitle/render lifecycle unchanged. Verify on iPhone/iPad, external display/windowed modes and any supported scaled-display configuration; check memory/performance and screenshot sharpness. This remains **NOT DEVICE VERIFIED** because no Apple device/simulator run was performed.

**Classification:** Metal layer lifecycle = **KEEP / Mew-specific implementation**; nativeScale-only calculation = **ADAPT candidate**.

**Verification:** static source/commit inspection only. No Swift change, iOS build, simulator, external display or playback test run.


## iOS backup share-sheet lifecycle and iPad presentation (2026-11-20 17:03:46 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `87eda9fa9a5131d7c6d17b71b5c1a5f88785141b`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `91c4480394f0978c97b144bffd950d7249852761`  
**Upstream commits inspected:** `875de58db03ab7bf390920e7ed27722e82f2ab71`, `d703ca48e5ade22ceaf424451ffba05efa94e670`  
**Files inspected:** Mew `NuvioEnhancedBackupFileBridge.ios.kt`, `NuvioEnhancedSettingsPage.kt`; upstream `SettingsConfigFileLauncher.ios.kt`.

- Mew already uses `UIActivityViewController` to share its temporary backup JSON, so the core share-sheet behavior is **ALREADY IMPLEMENTED**; do not add another export path or replace it with a different picker family.
- Mew presents the activity controller without configuring `popoverPresentationController`. Upstream explicitly anchors the iPad popover to the presenter view's center and disables arrow directions. The missing anchor is a platform compatibility risk for iPad presentation and should be adapted.
- Mew writes the backup to `NSTemporaryDirectory()` and returns success immediately after presenting the controller. It does not attach a completion handler to remove the temporary file after share/cancel. Upstream's share-sheet completion handler removes the temporary file and only invokes its saved callback when the user completes the activity.
- Mew's UI currently shows a “backup file ready” toast on successful file creation/presentation. Preserve that meaning or update the callback contract deliberately; do not report that a destination save completed before the share sheet returns.

**Decision: ADAPT the missing lifecycle/presentation safeguards** inside Mew's existing `NuvioEnhancedBackupFileBridge`:
1. Set a valid popover source view/rect for iPad.
2. Remove the temporary backup file in the activity completion handler on both success and cancellation.
3. Decide whether callback success means “file prepared/presented” or “share activity completed”; keep toast wording aligned.
4. Handle presenter dismissal/cancellation and ensure callback/delegate state is released exactly once.

**Classification:** share-sheet export = **ALREADY IMPLEMENTED**; iPad popover anchor = **ADAPT**; temp-file cleanup/completion semantics = **ADAPT**; existing backup serialization/import validation = **KEEP**.

**Verification:** static source inspection only. No iPad/iPhone simulator, share destination, cancellation, temp-file cleanup or build test run.


## iOS floating/native tab-bar blur fix applicability (2026-11-20 17:03:46 IST)

**Target:** `Main-dev`  
**Verified target HEAD before write:** `41b469c22630422076fc32c50aff0a5f2916c990`  
**Application-source baseline:** `8a36dcede748ba6bb35d2a49c1ffd062bf7efbd0`  
**Previous audit document blob / write guard:** `8c050020076daf6cf77b5027956296a37b039bc2`  
**Upstream commit inspected:** `c314dd48d622de249eebbea7e531e3f7b2e506d4`  
**Files compared:** Mew/upstream `iosApp/iosApp/ContentView.swift`.

- Upstream's fix targets SwiftUI/native tab navigation: clear `UITabBarAppearance` background/effect and the tab item's own appearance, hide iOS 26 bottom scroll-edge effects, and apply SwiftUI `toolbarBackground(.hidden, for: .tabBar)`. It preserves the floating tab pill.
- Mew uses a custom UIKit `UITabBar` inside `RootComposeViewController`, with its own visibility/profile-touch overlay and native-tab configuration. `applyNativeTabBarAppearance()` updates item colors but starts from a copy of the default appearance; it does not explicitly clear the background effect/color. Mew does not use the same SwiftUI `NativeNavContentView` structure as upstream.
- The UIKit transparent-background portion may be relevant, but applying the full upstream patch could remove an intentionally designed material or conflict with Mew's custom native-tab lifecycle and Liquid Glass settings.

**Decision: NEEDS DEVICE INVESTIGATION / ADAPT candidate.** Inspect Mew's actual iOS tab appearance in Static, Auto-hide and Morphed modes on iOS 26; determine whether a full-width blur band is present and whether the floating pill remains intact. If confirmed, adapt the UIKit appearance and bottom-edge effect only in Mew's custom tab-bar owner. Do not copy upstream SwiftUI modifiers or replace Mew's tab navigation architecture.

**Classification:** Mew custom UIKit tab bar vs upstream SwiftUI/native tab integration = **INTENTIONAL PARALLEL / DIFFERENT ARCHITECTURE**; transparent background behavior = **NEEDS DEVICE VERIFICATION**.

**Verification:** static source comparison only. No iOS simulator/device, Liquid Glass mode, tab visibility, scroll-edge or accessibility test run.
