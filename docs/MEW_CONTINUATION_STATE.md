# MewMobile Continuation State

Updated: 2026-10-30
Repository: https://github.com/somilkhan/MewMobile
Branch: `sync/upstream`
Predecessor SHA: `f2a7d9e1441b792e4e32c34b6e3923753104c635`
Open PR: [#38](https://github.com/somilkhan/MewMobile/pull/38) — draft, unmerged
Phase: Phase 4 — verification and release-candidate review
Phase 5 readiness: NOT READY

The commit containing this file is the current branch head after adding failure-only host-test report upload. Resolve its exact SHA from GitHub metadata.

## Fresh upstream baselines
- Nuvio Mobile `cmp-rewrite`: `53869a0279c59abdef23da2962eb2a1c80e6caa5`
- Nuvio Enhanced `enhanced`: `58d1edb895c37bc638f87fe137f3fd714b6a9a14`

## CI evidence
- Baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4`: run [40480042875](https://github.com/somilkhan/MewMobile/actions/runs/40480042875) passed Android host tests, branding validation, and Kotlin source compilation. APK packaging/upload were skipped.
- `254be18652c0817ff792fba9ac2fe2146b840a1b`: run [40932816880](https://github.com/somilkhan/MewMobile/actions/runs/40932816880) failed at test-source compilation because Compose UI Test and Robolectric dependencies were missing.
- `c2652ca0752be124d3a89dd4bb0e15767e9f2f08`: run [40934466093](https://github.com/somilkhan/MewMobile/actions/runs/40934466093) failed at test-source compilation because `createComposeRule` was imported from the wrong package. The import was corrected in the next commit.
- `f2a7d9e1441b792e4e32c34b6e3923753104c635`: run [40935809526](https://github.com/somilkhan/MewMobile/actions/runs/40935809526) executed 1,099 tests in 7m03s; the two new gesture tests failed at runtime with `RuntimeException at RoboMonitoringInstrumentation.java:102`. The correct import and `@Config(sdk = [34])` are already present. The console did not expose the underlying exception.
- This commit adds a failure-only upload of the host-test HTML/XML report. Current-head CI is pending. If the tests fail again, download the report artifact and inspect the full nested exception before editing the test.
- No APK artifact exists for the current head. Package-ID assertion and signing verification have not run.

## Implemented in source, awaiting current CI
- Locked player touch propagation fix and two Android host regression tests.
- Subtitle font-size stepper changed from 2sp to 1sp, preserving the existing 12–40sp bounds.
- TMDB/CloudStream mutual-exclusion policy: existing Discovery switch retained; CloudStream entry and provider runtime disabled while TMDB is enabled; Home sections refresh when the mode changes; common policy tests cover both modes.
- Workflow now uploads Android host-test reports only on host-test failure, with three-day retention. Routine successful CI still does not upload APKs.

## Upstream disposition and remaining work
- Continue selective player ports after the current test suite is diagnosable: offline network-backed effect gating, Dolby decoder fallback filtering, and overlapping skip-interval selection.
- Defer engine auto/failover, native ASS render-mode/AAR, iOS subtitle/decoder changes, full server-connection subsystem, and Enhanced PublicMetaDB tracking/scrobbling; reasons are in `docs/UPSTREAM_PARITY_AUDIT.md`.
- Mew's AutoSync pipeline and CloudStream update-safety policy/tests already exist; preserve them and port only compatible fixes.
- LogFox was not found in repository source; external behavior remains NOT VERIFIED / NOT FOUND IN REPO.
- Android application ID currently reads `com.mew.mobile`, debug ID `com.mew.mobile.debug`. Routine CI skips package-ID assertion. iOS identifiers remain `com.nuvio.enhanced` and require separate review.
- Keep routine CI focused on tests/compile. APK packaging/upload remains gated to Phase 5.

## Next action
Poll the new CI run for the commit containing this state file. If the host tests fail, retrieve the `android-host-test-reports` artifact, read the XML/HTML stack trace, and correct the actual runtime cause. If they pass, continue the planned player reliability ports and upstream disposition review. Do not stop after reporting status and do not notify for device testing until every Phase 5 gate is satisfied.

## Verification labels
- IMPLEMENTED: player touch fix, subtitle 1sp step, TMDB/CloudStream policy in source.
- COMMITTED: predecessor `f2a7d9e1441b792e4e32c34b6e3923753104c635`.
- CI PASSED: baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4` only.
- CI FAILED: `f2a7d9e1441b792e4e32c34b6e3923753104c635`, run `40935809526`, two Robolectric runtime failures.
- CI PENDING: current commit until its own workflow run completes.
- BUILT / APK: none for current head.
- DEVICE VERIFIED: no; pending user device test.
- Working tree: no local Git checkout is available in this session; remote branch/commit state is checked through GitHub. Do not claim a clean local working tree.
