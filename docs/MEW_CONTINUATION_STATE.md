# MewMobile Continuation State

Updated: 2026-10-30 (India local date; latest observed CI log time 2026-10-29 18:56:54 UTC)
Repository: https://github.com/somilkhan/MewMobile
Branch: `sync/upstream`
Predecessor SHA: `c2652ca0752be124d3a89dd4bb0e15767e9f2f08`
Open PR: [#38](https://github.com/somilkhan/MewMobile/pull/38) — draft, unmerged
Phase: Phase 4 — verification and release-candidate review
Phase 5 readiness: NOT READY

The commit containing this state file is intended to be the current branch HEAD after the import correction. Resolve the exact SHA from the branch/PR metadata.

## Fresh upstream baselines
- Nuvio Mobile `cmp-rewrite`: `53869a0279c59abdef23da2962eb2a1c80e6caa5`
- Nuvio Enhanced `enhanced`: `58d1edb895c37bc638f87fe137f3fd714b6a9a14`

## CI evidence
- Baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4`: run [40480042875](https://github.com/somilkhan/MewMobile/actions/runs/40480042875) passed Android host tests, branding validation, and Kotlin source compilation. APK packaging and upload were skipped.
- `254be18652c0817ff792fba9ac2fe2146b840a1b`: run [40932816880](https://github.com/somilkhan/MewMobile/actions/runs/40932816880) failed because the host-test source set lacked Compose UI Test and Robolectric dependencies.
- `c2652ca0752be124d3a89dd4bb0e15767e9f2f08`: run [40934466093](https://github.com/somilkhan/MewMobile/actions/runs/40934466093) failed at `:composeApp:compileAndroidHostTest`. Exact root error: `createComposeRule` was imported from the wrong package; the candidate corrects the import to `androidx.compose.ui.test.junit4.createComposeRule`. The subsequent composable-context diagnostics are cascading. The runner also emitted a separate submodule cleanup warning for `vendor/TorrServer`; this is logged for final workflow review.
- Current candidate CI remains pending until a new run completes. No APK artifact exists for the current head.

## Implemented changes
- Locked player touch propagation fix; two Android host regression tests remain enabled.
- Subtitle font-size stepper changed from 2sp to 1sp, preserving the existing 12–40sp bounds.
- TMDB/CloudStream mutual-exclusion implementation is in the candidate: the existing Discovery switch is retained; CloudStream is disabled while TMDB is enabled, and Home/runtime provider work is gated. Common policy tests cover both modes.
- PR #38 description was refreshed to remove stale status claims and show current evidence.

## Upstream disposition and remaining work
- Reviewed but deferred with reasons in `docs/UPSTREAM_PARITY_AUDIT.md`: Nuvio auto engine/failover, offline network-aware player effects, native ASS render mode/AAR, skip-segment auto-skip/controller matching, iOS subtitle/decoder changes, full server-connection subsystem (Mew has no server feature tree/policy), and Enhanced PublicMetaDB tracking/scrobbling.
- Continue source review of offline playback reliability, Dolby decoder fallback, sidecar subtitle performance, plugin install/update safety, AutoSync, and server/library scope.
- Android application ID currently reads `com.mew.mobile`, debug ID `com.mew.mobile.debug`; current routine CI skips package-ID assertion because APK packaging is gated. iOS bundle identifiers remain `com.nuvio.enhanced` and need separate migration review. Do not treat package identity as verified for the current candidate.
- Routine CI must remain tests/compile focused. APK generation/upload is reserved for Phase 5 or a concrete validation need.

## Next action
Publish the source-backed import correction with the TMDB/CloudStream candidate, verify the exact resulting commit and its CI, then continue the player reliability audit. Do not stop after a status update. Do not notify the user for device testing until all Phase 5 gates pass.

## Verification labels
- IMPLEMENTED: player touch fix, subtitle 1sp step, TMDB/CloudStream policy in candidate.
- COMMITTED: predecessor `c2652ca0752be124d3a89dd4bb0e15767e9f2f08` is published.
- CI PASSED: baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4` only.
- CI FAILED: predecessor `c2652ca0752be124d3a89dd4bb0e15767e9f2f08`, run `40934466093`, wrong Compose test import.
- CI PENDING: corrected candidate until its own workflow run completes.
- BUILT / APK: none for current candidate.
- DEVICE VERIFIED: no; pending user test.
- Working tree: no local Git checkout is available in this session. Remote state is checked via GitHub; do not claim a clean local working tree.
