# MewMobile Continuation State

Updated: 2026-10-30
Repository: https://github.com/somilkhan/MewMobile
Branch: `sync/upstream`
Predecessor SHA: `34e138b0a43a728bd9bf10fb11e8da541366832b`
Open PR: [#38](https://github.com/somilkhan/MewMobile/pull/38) — draft, unmerged
Phase: Phase 4 — verification and release-candidate review
Phase 5 readiness: NOT READY

The commit containing this state note is the current candidate head. Resolve its exact SHA from the live `sync/upstream` branch metadata. This note intentionally records its parent rather than trying to embed a self-referential commit SHA.

## Fresh upstream baselines
- Nuvio Mobile `cmp-rewrite`: `53869a0279c59abdef23da2962eb2a1c80e6caa5`
- Nuvio Enhanced `enhanced`: `781157e0033db40361b662525b06126e184ffd1c` (includes the newer MPV night-mode series).

## Implemented in source in this candidate
- Existing Discovery settings retain the TMDB switch. TMDB enabled gates CloudStream entry/runtime; TMDB disabled permits CloudStream. Provider load/link results and Home rows are gated/refreshed, with common policy tests.
- Player locked-state touch propagation fix and two Android host regression tests.
- Subtitle font-size stepper uses 1sp increments while retaining Mew's 12–40sp range.
- Robolectric test manifest is now placed as an Android host-test classpath resource and referenced as `AndroidManifest.xml`; the old non-classpath manifest path is removed.
- Android audio renderer selection excludes Dolby-named platform decoders for AC-3/E-AC-3/E-AC-3 JOC only when extension renderers are enabled, preserving platform decoders if the user disables extensions. Host policy tests added.
- Active overlapping skip intervals select the furthest safe seek target; malformed/non-finite intervals and seek-overflow targets are ignored. Common regression tests added.
- Routine CI uploads HTML/XML host-test reports only on failure with three-day retention. APK build/signing/application-ID assertion/upload remain gated to Phase 5.

## CI evidence
- Baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4`: run [40480042875](https://github.com/somilkhan/MewMobile/actions/runs/40480042875) passed host tests, Android Kotlin source compilation and branding validation; APK steps were skipped.
- `e502ae9371726e2dcdda8192d97ea55352eb22f6`: run [40938951414](https://github.com/somilkhan/MewMobile/actions/runs/40938951414) failed because Robolectric could not find `src/androidHostTest/AndroidManifest.xml` on the runtime classpath. The XML report was downloaded and inspected; this was a resource-resolution failure before the gesture assertions.
- Parent candidate `34e138b0a43a728bd9bf10fb11e8da541366832b`: run [40952751433](https://github.com/somilkhan/MewMobile/actions/runs/40952751433) remained in the host-test step at last observation; it is not counted as a pass. A newer candidate is being committed with the resource-classpath correction and targeted upstream fixes; check the latest run on the exact current HEAD.
- No installable APK exists for the current candidate. Package-ID assertion and signing verification have not run on it.

## Remaining upstream review
- Continue selective player reliability work after the current candidate's CI outcome: offline network-aware effect gating, local-file playback behavior, and sidecar subtitle cursor optimization need call-site/architecture review before porting.
- Enhanced's latest MPV night-mode series (`8bbdd65a` through `781157e0`) is new since the prior audit. It adds shared UI/settings, Android MPV audio filtering, iOS bridge changes, and a cross-engine button; defer until Android and iOS behavior can be verified together.
- Defer engine auto/failover, native ASS render-mode/AAR, full server-connection subsystem, and PublicMetaDB tracking/scrobbling for the documented dependency/licensing/privacy reasons in `docs/UPSTREAM_PARITY_AUDIT.md`.
- Mew's AutoSync pipeline and CloudStream update-safety policy/tests already exist; preserve them and port only compatible fixes.
- LogFox was not found in repository source; external behavior remains NOT VERIFIED / NOT FOUND IN REPO.
- Android application ID: `com.mew.mobile`; debug ID: `com.mew.mobile.debug`. iOS bundle ID remains `com.nuvio.enhanced`; its migration needs separate signing, OAuth and widget-extension review. Android package migration does not migrate data from old IDs automatically.

## Next action
Check the newest CI run associated with the exact current branch HEAD. If host tests fail, download and inspect the failure-only report, then fix the evidenced root cause. If the tests and compile pass, continue the remaining player reliability ports and refresh the parity ledger. Do not build/upload an APK until Phase 5 is genuinely ready, and do not notify the user before every Phase 5 gate is satisfied.

## Verification labels
- IMPLEMENTED: TMDB/CloudStream policy, player gesture/subtitle fixes, Robolectric manifest classpath correction, Dolby decoder policy, overlapping skip interval selection.
- COMMITTED: this candidate commit, whose parent is `34e138b0a43a728bd9bf10fb11e8da541366832b`; resolve current SHA from GitHub branch metadata.
- CI PASSED: baseline `c26e71f3e5c86a2f4d3042d339b63bd5986f07d4` only.
- CI PENDING: current candidate; verify exact SHA and run result.
- BUILT / APK: none for current candidate.
- DEVICE VERIFIED: no; pending user device test.
- Working tree: no local Git checkout is available in this session; remote branch/commit state is checked through GitHub. Do not claim a clean local working tree.
