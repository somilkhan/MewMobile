# MewMobile Upstream Parity Audit

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
| Optional libmpv libass renderer for ASS/SSA | Listed in Enhanced 0.6.2-beta release notes; Enhanced adds native C/JNI plus a substantial Kotlin subtitle stack | **Not ported yet.** This is a large native/runtime integration (multiple C and Kotlin files); the upstream AIOStreams port is AGPL-3.0 while Mew is GPL-3.0. Perform source/license and distribution-obligation review, then port with subtitle regression tests. |
| Libmpv add-on subtitle AutoSync | Nuvio `cb581ea78e2ff9896d1d7a39e65d450aa29d94d3` adds a cancellable coordinator and SRT/WebVTT/ASS retimer for the libmpv path | **Ported on `sync/upstream`; automated validation passed:** shared timeline retimer/parser, ExoPlayer sidecar integration, libmpv retimed-file replacement, profile-scoped preferences, cancellable subtitle HTTP, indexed reference loading, localized outcomes, and wrapper-preserving integration with Mew's libass Matroska extractor. Follow-up changes adapt the sidecar/HTTP APIs to Mew and keep explicit subtitle selection independent of the startup-only preference. Timeline matching/subtitle-format host tests and Android Kotlin source compilation passed in [run 39884536760](https://github.com/somilkhan/MewMobile/actions/runs/39884536760). Phase 5 must still verify actual playback synchronization on-device. |
| iOS PiP decoder recovery | Nuvio `9721a32fc42a34cb62a8db9040042b89c622fd2e` uses `hwdec-software-fallback=300` when VideoToolbox is interrupted during PiP | **Already present in Mew:** `hwdec-software-fallback=300` is configured in `iosApp/iosApp/Player/MPVPlayerBridge.swift`; no duplicate option was added. iOS build and device verification remain outstanding. |
| Player side-panel system-bar inset | Nuvio `b9fbc09c83d44d7b76d813f0c99a4c2f6c5c38af` keeps the end-side player panel clear of Android navigation bars | **Ported in current candidate:** Mew's existing `PlayerSidePanel` adds navigation-bar inset padding without replacing its layout. Final-candidate Android CI is tracked by the linked Actions run; device verification remains outstanding. |
| Configurable player skip-back/skip-forward durations | Enhanced commits `465ad56c44`, `ddb752a8c7`, and `ef5a32f657` add profile-scoped settings, playback UI, and seek behavior | **Ported:** 5/10/15/30-second choices, 10-second defaults, profile-scoped Android/iOS persistence, settings sync payload, dynamic numbered seek icons/accessibility labels, and player-control wiring. `PlayerSkipSecondsTest` covers supported and invalid values. |
| Mini-player setting tied to PiP button | Listed in Enhanced 0.6.2-beta release notes | **Assessed, not directly applicable:** Mew has Android PiP lifecycle/remote controls but no separate mini-player surface. Preserve existing PiP behavior; do not import an upstream setting without a matching Mew feature. |
| Enhanced player settings and buffering controls | Enhanced's settings model includes additional controls for loading overlay, pause overlay, seek preview, custom playback buffers, disk cache, audio passthrough and memory behavior | **Partial port:** pause-overlay visibility is user-configurable (default remains enabled), with profile-scoped Android/iOS persistence and settings sync. Seek-preview thumbnails, custom buffers, disk cache, audio passthrough and memory-behavior controls remain deferred pending per-engine compatibility/performance review. |
| Newer upstream navigation / stream-card / cinematic details refinements | Enhanced adds configurable Liquid Glass navigation, player surfaces and a cinematic stream-filter field | **Partially ported:** stream-card contrast fix, cinematic hero artwork reuse, and selectable Liquid Glass navigation are ported. Player Liquid Glass is ported as an opt-in feature; cinematic stream-filter glass remains intentionally deferred because Mew's current app-level backdrop is scoped to navigation and needs a separate architecture-aware integration. Mew's navigation integration uses the existing Settings → Layout → Navigation selector and preserves existing styles. Remaining navigation parity is the upstream effect/icon-color settings plus real-device rendering/accessibility/performance validation; do not replace Mew's navigation or settings architecture wholesale. |
| Settings backup import/export | Enhanced commit `56db14744c` adds JSON backup export/import under Advanced settings | **Already present in Mew:** `NuvioEnhancedSettingsPage.kt` exposes export/import through `ProfileSettingsSync.exportBackupJson/importBackupJson`; Android and iOS file bridges are present. The current profile-scoped player settings sync-key list includes the new toolbar layout. No duplicate backup screen is needed. |
| Player end-time / time-left indicator | Enhanced commits `dece182429` and `5952638000` add end-time and remaining-time presentation around the seek bar | **Already present in Mew's own player design:** `PlayerClockEndTimeOverlay.kt` and `PlayerControls.kt` calculate end-at and remaining labels. Keep Mew's existing placement and avoid a duplicate upstream label. |
| Home hero ratings and age-rating metadata | Nuvio commit `d904677544` adds rating/age-rating rows via `HomeHeroMetaRows.kt` | **Equivalent behavior already exists in Mew's custom hero:** `HomeHeroSection.kt` renders streaming-showcase ratings and age-rating signals. The implementation is intentionally not copied component-for-component because Mew's hero layout and metadata path differ. |
| Seekbar thumbnail/frame accuracy optimization | Enhanced commit `74305199aa` optimizes AIOStreams seek-preview frame selection | **Not copied wholesale:** Mew has its own `seekpreview/SeekPreviewFrameSource.android.kt` integration and player-engine routing. Reassess only against a reproducible frame mismatch and benchmark the adapted implementation; device-level seek-preview accuracy/performance remains unverified. |
| Discord Rich Presence | Enhanced's post-tag branch adds a cross-platform Discord Gateway/RPC stack, Android and iOS storage/platform adapters, player runtime integration, settings UI and localized resources | **Not ported; assessment required.** This is a new integration rather than a small bug fix. Evaluate Android lifecycle/socket behavior, privacy and opt-in defaults, platform scope, dependency/license impact, and Mew's existing player runtime before deciding whether to port. Do not copy the cross-platform implementation wholesale. |
| Binge-group startup filename/infoHash identity | Enhanced commit `1b85c6458cfb46b61849fa1a1437aa76ff6a117f` seeds filename/hash from the initial stream before the first cache write | **Adapted for Mew's route model:** `PlayerLaunch` carries initial filename/hash separately from P2P routing fields. Direct stream launch falls back from `stream.infoHash` to `stream.p2pInfoHash`; cached direct links and P2P launches carry their available metadata. Tests cover initial direct-source identity and client-resolve hash fallback without reclassifying a playable direct URL as torrent playback. Torrent launch arguments remain authoritative for P2P. Final-candidate Android CI is tracked by the linked Actions run; device verification remains pending. |
| Subtitle text-shadow control and ExoPlayer span preservation | Enhanced commits `63ce868064` and `111471f53a`; settings persistence and player rendering paths changed | **Ported in the current shadow implementation:** enable/strength controls, bounded strength normalization, profile-scoped Android/iOS persistence and settings sync, ExoPlayer cue spans that survive embedded-style stripping, Android libmpv shadow offset/background handling, and iOS libmpv shadow-offset bridging. Shadow blur strength now matches the newer Enhanced value (0.8x). Device rendering remains unverified. |
| Custom subtitle-font importer and bundled font resolution | Enhanced commit `b47732e3df` plus Android/iOS importer and resolver files | Mew has `SubtitleFontFileBridge` and platform font metadata helpers. **Safety parity port:** Android and iOS imports now enforce the upstream 30 MiB size ceiling while preserving Mew's isolated-directory/font-family behavior. Mew still accepts `.ttf`/`.otf` only; `.ttc`/`.otc`, bundled font resolution, import management and lifecycle parity remain separate review items. |
| Cinematic stream search/filter glass | Enhanced commits `13c87a32f7` and `d875e3f859` | Not present as matching upstream UI in Mew. Assess as a separate UI port after Liquid Glass navigation foundation; preserve current stream filtering behavior and low-overhead fallback. |

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
