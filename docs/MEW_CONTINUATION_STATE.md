# MewMobile Continuation State

Updated: 2026-11-02
Repository: https://github.com/somilkhan/MewMobile
Branch: `sync/upstream`
Predecessor SHA: `c69191690e539c775ed1a8a2fb167df12c723a69`
Open PR: [#38](https://github.com/somilkhan/MewMobile/pull/38) — draft, unmerged
Phase: Phase 4 — verification and release-candidate review
Phase 5 readiness: NOT READY

The commit containing this state note is the current candidate head. Resolve its exact SHA from live `sync/upstream` branch metadata; this note records the parent rather than a self-referential SHA.

## Fresh upstream baselines
- Nuvio Mobile `cmp-rewrite` recorded integration baseline: `38295004e4d4002f5520fe4120230253b64af63d`; latest reviewed head: `46febb568348aa5e135bff0d4d5c914933aff5a0` (38 commits ahead, 0 behind).
- Nuvio Enhanced `enhanced`: `93893a444ba68876d1ac30790d235ca4e7ddb64c` (current compare identical; no newer commits observed).

## Implemented in source in this candidate
- Existing Discovery settings retain the TMDB switch. TMDB enabled gates CloudStream entry/runtime; TMDB disabled permits CloudStream. Provider load/link results and Home rows are gated/refreshed, with common policy tests.
- Player locked-state touch propagation fix and two Android host regression tests.
- Subtitle font-size stepper uses 1sp increments while retaining Mew's 12–40sp range.
- Robolectric test manifest is exposed under `src/androidHostTest/resources/AndroidManifest.xml`, referenced by classpath name, and its package is aligned to Robolectric's observed launch package `org.robolectric.default`. The unsupported `@Config(packageName = ...)` argument was removed after CI proved it does not compile.
- Android audio renderer selection excludes Dolby-named platform decoders for AC-3/E-AC-3/E-AC-3 JOC only when extension renderers are enabled, preserving platform decoders if the user disables extensions. Host policy tests added.
- Active overlapping skip intervals select the furthest safe seek target; malformed/non-finite intervals and seek-overflow targets are ignored. Common regression tests added.
- Routine CI uploads HTML/XML host-test reports only on failure with three-day retention. APK build/signing/application-ID assertion/upload remain gated to Phase 5.

## CI evidence
- Baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4`: run [40480042875](https://github.com/somilkhan/MewMobile/actions/runs/40480042875) passed host tests, Android Kotlin source compilation and branding validation; APK steps were skipped.
- `e502ae9371726e2dcdda8192d97ea55352eb22f6`: run [40938951414](https://github.com/somilkhan/MewMobile/actions/runs/40938951414) failed because Robolectric could not find `src/androidHostTest/AndroidManifest.xml` on the runtime classpath.
- `34e138b0a43a728bd9bf10fb11e8da541366832b`: run [40952751433](https://github.com/somilkhan/MewMobile/actions/runs/40952751433) ran the host suite and failed both gesture tests at activity startup. The report artifact [12931684095](https://github.com/somilkhan/MewMobile/actions/runs/40952751433) was downloaded and inspected: Robolectric resolved the classpath manifest but launched `org.robolectric.default/androidx.activity.ComponentActivity`, which did not match manifest package `com.nuvio.app.hosttest`. The assertions did not run. The attempted annotation-level package override was rejected by Kotlin compilation (`No parameter with name 'packageName' found`). The next candidate instead aligns the test manifest package with Robolectric's observed launch package; fresh CI must verify this runtime correction.
- Parent candidate `def6449e032101563e4187ce728642b6e1aace8c`: push run [40954008288](https://github.com/somilkhan/MewMobile/actions/runs/40954008288) was queued when this follow-up was prepared; no result is counted. Check the newest run for the exact current HEAD.
- No installable APK exists for the current candidate. Package-ID assertion and signing verification have not run on it.

## Remaining upstream review
- Continue selective player reliability work after CI validates this candidate: offline network-aware effect gating, local-file playback behavior, and sidecar subtitle cursor optimization need call-site/architecture review before porting.
- Enhanced's latest MPV night-mode series (`8bbdd65a` through `781157e0`) is new since the prior audit. It adds shared UI/settings, Android MPV audio filtering, iOS bridge changes, and a cross-engine button; defer until Android and iOS behavior can be verified together.
- Defer engine auto/failover, native ASS render-mode/AAR, full server-connection subsystem, and PublicMetaDB tracking/scrobbling for the documented dependency/licensing/privacy reasons in `docs/UPSTREAM_PARITY_AUDIT.md`.
- Mew's AutoSync pipeline and CloudStream update-safety policy/tests already exist; preserve them and port only compatible fixes.
- LogFox was not found in repository source; external behavior remains NOT VERIFIED / NOT FOUND IN REPO.
- Android application ID: `com.mew.mobile`; debug ID: `com.mew.mobile.debug`. iOS app bundle ID is now configured as `com.mew.mobile` and the Downloads widget as `com.mew.mobile.DownloadsWidgetExtension`; the old `nuvioenhanced` and `nuvio` URL schemes remain registered for callback compatibility. iOS/Xcode compilation, provisioning/signing, auth callback behavior, and widget embedding remain unverified. App-ID changes are install/update boundaries; do not claim data migration from prior IDs.

## Next action

Check the newest CI run associated with the exact current branch HEAD. The 2026-11-02 follow-up adds category-stable skip UI state plus offline-aware and bounded player lookups; inspect the exact-head test/build result before continuing. Then review local-file playback/container behavior and sidecar subtitle cursor performance. Do not build/upload an APK until Phase 5 is genuinely ready, and do not notify the user before every Phase 5 gate is satisfied.

## Verification labels
- IMPLEMENTED: TMDB/CloudStream policy, player gesture/subtitle fixes, Robolectric manifest-package correction, Dolby decoder policy, overlapping skip interval selection, and Mew-owned Android/iOS bundle identifiers.
- COMMITTED: this candidate commit, whose parent is `40b545461d2c9d57f6a6f18b23a87c10e9cd5143`; resolve current SHA from GitHub branch metadata.
- CI PASSED: baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4` only.
- CI PENDING: current candidate; verify exact SHA and run result. Android CI does not validate iOS bundle provisioning, URL callback routing, or widget embedding.
- BUILT / APK: none for current candidate.
- DEVICE VERIFIED: no; pending user device test.
- Working tree: no local Git checkout is available in this session; remote branch/commit state is checked through GitHub. Do not claim a clean local working tree.


## 2026-11-02 follow-up implemented in source

- Skip interval categories are shared by repository merge, active-interval runtime and skip-button auto-hide. Provider alias changes within opening/ending/recap preserve dismissal; same-category replacement intervals beginning within two seconds retain the button instead of flashing off.
- Player add-on subtitle lookup observes network state, does not mark a fetch complete while offline, retries when connectivity changes, and avoids redundant add-on lookup for local files that already have external subtitles.
- Remote skip lookup waits for usable connectivity and has a four-second timeout.
- Added common tests for skip segment alias normalization. Exact-head CI for this follow-up is not yet observed.


## 2026-11-02 local media/sidecar follow-up

- Incremental sidecar cue cursor is integrated with Mew's existing generation guard and has host regression tests for unchanged active cues, backward seek, and unsorted timelines.
- Local file/content URI media now infer container MIME from the first 4 KiB when stream type and response headers do not decide the type; extension inference remains the fallback. Host tests cover the supported signatures.
- Local `file://` subtitles can be copied into FileProvider cache without changing Mew's shared HTTP/DNS client.
- Generic "Episode N" labels are localized in detail, watched-action and stream title surfaces.
- Network monitoring is started from Home and player entry; Home foreground transitions request a delayed confirmation refresh. Player lookups retry when offline state clears without restarting for intermediate probe states.
- Exact-head CI is pending. APK build/signing and device verification remain Phase 5 gates.


## Exact-head verification note — 2026-11-02

- The network-state scope correction was verified by CI on predecessor head `8f063a2dd8474e7cab4157f305d08958b5b4f952`: workflow run [41345181764](https://github.com/somilkhan/MewMobile/actions/runs/41345181764) completed successfully. Host tests, Android Kotlin source compilation and branding validation passed; APK/package-ID/signing steps were skipped as intended.
- The next source batch is committed at `39afbe50071566fb97b45436c05e9d8df447fe75`. It adds sidecar cursor and local-container MIME regression tests, local subtitle-file support, localized generic episode titles, and explicit network-monitor startup/foreground refresh. The intermediate run for the superseded pre-amend head was cancelled during the safe duplicate-import cleanup; it is not a test failure for the corrected head.
- **Current gate:** exact-head CI for the corrected source batch must pass before any Phase 5 APK is built. No APK has been generated for this follow-up.
