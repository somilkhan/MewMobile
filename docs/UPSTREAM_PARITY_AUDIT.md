# MewMobile Upstream Parity Audit

**Audit date:** 2026-10-20  
**Target:** Android-first Mew app, preserving Mew-specific CloudStream integration and existing player/profile behavior.  
**Policy:** Full relevant feature parity is the objective; implementation is dependency-aware and selective by design. Never wholesale-copy upstream files where Mew has diverged.

## Current repository state

- Mew integration branch: `sync/upstream`
- Current audited head: `78bba4b8edefb0687b6f374e52ec2f19207ea7d3`
- Review PR: [#38](https://github.com/somilkhan/MewMobile/pull/38), open and draft, base `enhanced`.
- Latest verified CI run: [39320138740](https://github.com/somilkhan/MewMobile/actions/runs/39320138740), conclusion **success** on the current code head. Its `android-full-debug` job passed Android host tests, Full Debug APK build, Mew branding resource check, application ID assertion, APK signing verification and artifact upload.
- Current package target: `com.mew.mobile.debug` for Full Debug. Package migration is a breaking install/update boundary versus old IDs; do not claim app-data migration is automatic.
- Upstream release baseline: Nuvio Mobile `0.6.2-beta` (published 2026-10-18) and Nuvio Enhanced `0.6.2-beta` (published 2026-10-19). Enhanced's `enhanced` branch has 14 commits after that tag as of this audit; Nuvio's `cmp-rewrite` has one release-publication commit after its tag.

## Already implemented on the audited Mew head (source/CI verified)

- CloudStream repository install/update operations use a repository-owned coroutine scope; reconciliation retries after plugin loads and overlapping reconciliation is skipped.
- Home catalog loading runs alongside addon catalogs and progressively scans every runnable CloudStream provider. The former 30-second global scan timeout and six-row provider cap are removed; each provider still has its own 5-second timeout and concurrency is bounded at four. A provider with a main page contributes one row, avoiding category fan-out. CI passes; actual 81-provider behavior and memory/performance remain **not device verified**.
- CloudStream-origin titles enter the shared stream-loading pipeline; exact direct routes are preserved while eligible CloudStream providers and Nuvio addon scrapers are queried. Opaque CloudStream IDs are not sent to addon endpoints that require external IDs.
- CloudStream stream results honor the repository-grouping preference with bounded provider concurrency.
- Series/episode matching has explicit season normalization and prevents cross-season fallback. Latest head includes additional guards against unverified provider-ranked series matches and fallback to a series route when a requested episode is missing. CI passes; real-provider episode correctness remains **not device verified**.
- TMDB remains optional; the Discovery toggle was removed per maintainer direction. Generic TMDB API/collection access honors the enabled state, cache invalidation is handled, and CloudStream detail/playback routes do not require TMDB.
- Subtitle matching, series continuity, stream matching, trailer detection, and TMDB paths have host regression tests in the current PR.
- Stream-card text contrast fix is included.
- The cinematic details background now reuses the hero image painter when available, avoiding a second image request/decode; fallback image loading remains gated by deferred metadata work.
- Continuous pinch-to-zoom is ported into Mew's existing gesture pipeline for ExoPlayer and libmpv, clamped to 0.8x–2.0x, with zoom reset when the resize mode changes. ExoPlayer scales the content frame to avoid zoom escaping the video frame's black bars. Host tests and Full Debug build pass.
- Android portrait/landscape playback toggle is ported with lifecycle restoration and reset on player exit. iOS behavior remains unchanged and the toggle is hidden there. Host tests and Full Debug build pass.

## Confirmed upstream gaps requiring assessment/porting

These are confirmed by comparing the current Mew tree with the current Nuvio Enhanced `enhanced` tree. A missing file is evidence of a likely gap, not proof by itself that the entire feature is absent; inspect call sites and settings before porting.

| Upstream area | Evidence in Enhanced | Mew status / action |
|---|---|---|
| Android Liquid Glass navigation controls and user-configurable effect/icon color | `FloatingNavigationBar.kt`, `LiquidGlassEffectSettings.kt`, `LiquidGlassNavigationSurfaces.kt`, `NavigationBarSettingsSheet.kt`, associated storage and host tests | Not present as the same implementation in Mew. Audit dependencies, accessibility, device performance, and existing Mew navigation before a scoped port. |
| Player Liquid Glass with opt-out | `PlayerLiquidGlass.kt`, `PlayerLiquidGlass.android.kt`, related settings and resources | Those source files are absent in Mew. Assess as a distinct player UI feature; preserve low-overhead fallback. |
| Android player orientation toggle and portrait video support | Listed in Enhanced 0.6.2-beta release notes | **Ported** to Mew Android; orientation is restored when leaving the player. CI passed; device rotation/lifecycle behavior remains **not device verified**. |
| Custom aspect-preserving pinch zoom / Fill behavior | Listed in Enhanced 0.6.2-beta release notes | **Ported/adapted** to Mew's existing gesture pipeline; ExoPlayer targets the content frame, and both ExoPlayer/libmpv implement zoom. CI passed; gesture arbitration and aspect-ratio behavior remain **not device verified**. |
| Optional libmpv libass renderer for ASS/SSA | Listed in Enhanced 0.6.2-beta release notes; Enhanced adds native C/JNI plus a substantial Kotlin subtitle stack | **Not ported yet.** This is a large native/runtime integration (multiple C and Kotlin files); the upstream AIOStreams port is AGPL-3.0 while Mew is GPL-3.0. Perform source/license and distribution-obligation review, then port with subtitle regression tests. |
| Mini-player setting tied to PiP button | Listed in Enhanced 0.6.2-beta release notes | Inspect Mew's PiP manager and player lifecycle; do not remove Mew's existing behavior. |
| Enhanced player settings and buffering controls | Enhanced's settings model includes additional controls for loading overlay, pause overlay, seek preview, custom playback buffers, disk cache, audio passthrough and memory behavior | Large model/storage/UI divergence. Build a field-by-field compatibility matrix and migrate only relevant Android features with defaults and persistence tests; do not copy the upstream settings repository wholesale. |
| Newer upstream navigation / stream-card / cinematic details refinements | Enhanced adds configurable Liquid Glass navigation, player surfaces and a cinematic stream-filter field | **Partially ported:** stream-card contrast fix was already present; cinematic hero artwork reuse is now ported. Liquid Glass navigation/player/filter remain open. The current Mew toolchain is Compose Multiplatform 1.11.1 / Kotlin 2.3.0 / Coil 3.5.0-beta01, while Enhanced 0.6.2 uses 1.12.0 / 2.4.10 / 3.6.3 plus `kyant-backdrop` 2.0.1. Do a controlled dependency migration before integrating the glass surfaces. |
| Upstream watch-party chat, anime ID mapping, and translations | Present in the 0.6.2-beta release notes; Enhanced includes platform-specific work | **Open parity audit:** compare watch-party chat/fixes, Simkl watched-series completion, Trakt/MDblist/WeTrakr anime ID mapping, binge-group filename/infoHash matching, prefetch fixes, iOS external-player scheme probing, and pt-BR/Italian/Greek translations. Port applicable work; use adapters for dependency/architecture divergence. |

## Mandatory phases / completion gates

1. **Baseline audit:** pin upstream release tags and current branch heads; enumerate commits since tags and compare relevant Android/common source. Record source URLs and SHA.
2. **Parity matrix:** every relevant upstream feature/fix is marked **already present**, **port required**, **adaptation required**, **not Android-applicable**, **intentionally excluded with reason**, or **blocked**. No silent omissions.
3. **Implementation:** port in small coherent groups; retain Mew-specific CloudStream, TMDB-optional behavior, Live TV, DNS/networking, profile flows and player behavior. Add regression tests for every behavior change.
4. **Automated verification:** Android host tests, Full Debug APK build, branding and application-ID assertions, APK signature verification, review CI. Recheck current upstream commits before closing the phase.
5. **Device-testing handoff (PIN USER):** only after automated checks are green and the APK artifact is available. Provide exact artifact URL, SHA-256 if available, install/update warning, test checklist, passed/failed/not-verified categories. Do not claim device verification without actual device evidence.

## Known release blockers / unverified items

- Real device verification is outstanding: Vivo V30 / Android 16, CloudStream installation/reconciliation and 81-provider home scan, source playback/loading, correct anime season/episode resolution, provider grouping, TMDB-disabled operation, player gestures/orientation, subtitles, and package coexistence/data migration.
- No release-ready or full-parity claim is justified until the parity matrix is complete and the applicable automated checks pass. The current green CI verifies compilation/host tests only; it does not establish complete upstream parity or device correctness.
- Keep the PR a draft until Phase 5 readiness criteria are met; do not merge or change the default branch as part of this audit.
