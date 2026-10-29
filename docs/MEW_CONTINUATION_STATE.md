# MewMobile Continuation State

Updated: 2026-10-30 UTC
Repository: https://github.com/somilkhan/MewMobile
Branch: `sync/upstream`
Predecessor SHA: `254be18652c0817ff792fba9ac2fe2146b840a1b`
Open PR: [#38](https://github.com/somilkhan/MewMobile/pull/38) — open, draft, unmerged
Current phase: Phase 4 — verification and release-candidate review
Phase 5 readiness: NOT READY

The commit containing this state file is intended to become the branch HEAD after the predecessor's failure is corrected. Resolve the branch HEAD from Git metadata for the exact current SHA.

## Upstream baselines (freshly checked)
- Nuvio Mobile `NuvioMedia/NuvioMobile:cmp-rewrite`: `53869a0279c59abdef23da2962eb2a1c80e6caa5`
- Nuvio Enhanced `luqmanfadlli/NuvioMobile-Enhanced:enhanced`: `58d1edb895c37bc638f87fe137f3fd714b6a9a14`

## CI and artifact status
- Baseline commit `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4`: run [40480042875](https://github.com/somilkhan/MewMobile/actions/runs/40480042875) completed successfully. Branding validation, Android host tests and Kotlin source compilation passed. APK assembly, package-ID assertion, signing and artifact upload were skipped as intended.
- Predecessor `254be18652c0817ff792fba9ac2fe2146b840a1b`: run [40932816880](https://github.com/somilkhan/MewMobile/actions/runs/40932816880) failed at `:composeApp:compileAndroidHostTest`. Root cause: the newly added gesture test imported Compose UI testing and Robolectric, but Mew's host-test source set did not declare those dependencies. The candidate adds the missing dependencies; new CI must verify the correction.
- No APK artifact exists for the current candidate. Do not trigger packaging until Phase 5.

## Implemented in the predecessor
- Locked player touch propagation fix; the two regression tests remain and this candidate adds the missing test dependencies.
- Subtitle font-size stepper changed from 2sp to 1sp; existing 12–40sp bounds retained.
- The predecessor SHA is `254be18652c0817ff792fba9ac2fe2146b840a1b`.

## Current candidate changes
- Add a shared TMDB/CloudStream discovery policy.
- When TMDB is enabled, CloudStream is removed from Home discovery, provider search/load/link APIs reject or suppress provider work, cached load/link results are not returned, and the existing Discovery settings row is disabled. When TMDB is disabled, CloudStream becomes available again.
- Refresh Home when TMDB is toggled or its API key removal disables it, clearing stale CloudStream rows and hero items.
- Add common tests for both sides of the policy and add the missing host-test dependencies for the gesture regression tests.

## Remaining work and blockers
- Verify the candidate's CI; if any check fails, inspect the exact failing output before further edits.
- Complete the remaining source-by-source upstream dispositions in `docs/UPSTREAM_PARITY_AUDIT.md`. Reviewed but deferred: engine auto/failover, offline playback network-awareness, native ASS render mode/AAR, skip-segment auto-skip and resolved HLS/P2P controller matching, iOS subtitle/decoder changes, and PublicMetaDB tracking/scrobbling. Reasons are recorded in the audit.
- Audit Android package ID `com.mew.mobile` and debug ID `com.mew.mobile.debug`. iOS identifiers remain `com.nuvio.enhanced`; changing them requires separate signing/OAuth/app-update review.
- Review LogFox behavior, plugin install/update safety, AutoSync, player regressions, and server/library behavior against the master checklist.
- Keep routine CI limited to tests/compile; APK build/upload remains gated to device-test/release readiness.

## Next executable action
Verify that the live branch HEAD is still `254be18652c0817ff792fba9ac2fe2146b840a1b`, publish this corrected candidate as a single coherent commit, then poll the new CI run for the exact resulting SHA. Continue implementation and source review after verification; do not hand off for physical-device testing until all Phase 5 gates pass.

## Verification state
- IMPLEMENTED: locked-touch source fix, subtitle 1sp step, TMDB/CloudStream policy and tests in candidate tree.
- COMMITTED: predecessor `254be18652c0817ff792fba9ac2fe2146b840a1b` is published.
- CI PASSED: baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4` only.
- CI FAILED: predecessor `254be18652c0817ff792fba9ac2fe2146b840a1b`, run `40932816880`, due missing test dependencies.
- CI PENDING: corrected candidate until its new workflow run completes.
- BUILT / APK: no candidate APK.
- DEVICE VERIFIED: no; pending user device test.
- Working tree: no local Git checkout is available in this session; remote refs and commits are checked through GitHub. Do not claim a clean local working tree.
