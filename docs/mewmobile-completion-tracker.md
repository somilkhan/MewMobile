# MewMobile Completion Tracker
Persistent source of truth across chats. Resume first unchecked item; update this file after every meaningful task/handoff. Never assume code, CI, or device verification.

Status distinctions: **DONE** (required evidence recorded), **IN PROGRESS**, **BLOCKED** (reason + next action), **REMAINING**. Implementation ≠ CI ≠ device verified.

## A — Device blockers
- [x] **A1 Seek/timestamp reset:** timeline tap seeks target, not 00:00; trace timeline→seek dispatch→player/engine; regression tests.
- [x] **A2 Subtitles:** track discovery/selection/switching/render/timing; local, remote, CloudStream paths.
- [x] **A3 AutoSync no effect:** candidate aggregation, late-addon retry, persisted add-on restoration repaired + regression tests; exact-head CI passed. Device playback retest pending.
- [x] **A4 CloudStream Raw Catalog:** all provider sections now render as Home rows; collision-safe section keys + regression; exact-head CI passed. Device confirmation pending.
- [ ] **A5 Purple video:** root-cause surface/decoder/color/HDR/Dolby Vision/ExoPlayer/libmpv first.
- [ ] **A6 Rotation:** trace existing control through Activity/config lifecycle; both orientations and persistence.
- [ ] **A7 TMDB metadata:** v3 key/v4 token auth, response/cache and physical device retest.

## B — Player/features
- [ ] **B1** In-player engine switch; preserve playback state as feasible.
- [ ] **B2** Liquid Glass OFF=default player; ON=distinct alternate layout (preference centralized, layout incomplete).
- [ ] **B3** Player/stream labels, source-review and selective upstream port.
- [ ] **B4 Watch Party: reuse Nuvio backend; auth/capabilities, RPC/realtime/presence, clock/drift, chat/permissions, UI/player/tests. Not just a button.

## C — Regression/stability
- [ ] **C1** CloudStream install/update/reconcile/restart/errors.
- [ ] **C2** Sync/offline: auth gates, local dirty state/tombstones, post-login flush, no unauthorized loops.
- [ ] **C3** Episode identity/matching, seasons/episodes, ambiguous matches/autoplay guard.
- [ ] **C4** Subtitle/AutoSync/player lifecycle, source changes, rotation, background, engine switch.
- [ ] **C5** Profile measured performance: skipped frames, catalog responsiveness, memory/rendering warnings.

## D — Acceptance/release
- [ ] **D1** CI green exact final SHA (host tests, Android compile, branding/relevant checks).
- [ ] **D2** Full Debug APK at device gate only; routine CI skips artifacts.
- [ ] **D3** package/signing/install-update boundary: debug com.mew.mobile.debug, release com.mew.mobile.
- [ ] **D4** Device test exact APK/SHA; record each flow.
- [ ] **D5** Fix regressions and rerun affected tests/device checks.
- [ ] **D6** iOS build/signing/provisioning/runtime status.
- [ ] **D7** PR #38 final review/merge only after acceptance; remain draft/unmerged meanwhile.

## Guardrails
1. Inspect branch/head/status, files/callers/tests, project instructions and upstream refs before edits.
2. Selective upstream ports; preserve Mew brand and existing settings/navigation/player architecture. If present-but-broken: **REPAIR EXISTING**, never duplicate.
3. Root cause → coherent fix → regression tests → build/tests → adjacent regression scan.
4. Group commits; no commit spam; preserve unrelated changes.
5. No APK build/upload in routine CI; D2 only.
6. Report commands/results honestly; compile is not device verification.
7. TMDB and CloudStream remain independently usable; no mutual exclusion.
8. Redact credentials/tokens/cookies/account IDs/signed URLs; never commit raw sensitive Logcat.
9. Handoffs update status, evidence, blockers, and **NEXT EXACT TASK**.

## Baseline (2026-11-02)
Device candidate 4d838a356a658bf21c9eab0d63c83124d9ba5dd4 tested, not accepted. CI candidate build run 41350265383 passed; routine run 41350269421 passed with APK steps skipped (not proof for later commits). Evidence: [device report](DEVICE_TEST_REPORT_2026-11-02.md), [parity audit](UPSTREAM_PARITY_AUDIT.md), [PR #38](https://github.com/somilkhan/MewMobile/pull/38). Previously merged: sync auth/write guard, TMDB v3/v4 credentials, plugin TMDB ID resolution, centralized Liquid Glass preference (device retest pending). Watch Party NOT IMPLEMENTED; Nuvio backend reuse decided. iOS unverified.

## Handoff log
| Date | Item | Evidence/status | Next exact task |
|---|---|---|---|
| 2026-11-02 | Tracker initialized | Device report + PR #38 baseline | **A1: inspect timeline/seek source and reproduce reset; add regression test** |
| 2026-11-02 | A1 seek fix attempt | Based on device log's repeated `Cannot seek in this stream` and mpv's documented guidance, enabled libmpv `force-seekable=yes` in Android `initOptions` (before `mpv.init`), with option error logging. This lets mpv attempt cache/range seeks rather than preemptively rejecting timeline seeks. Source commit `075142707fc81ea6faac5f7481faec0b392f40fa` on `sync/upstream`. This is a targeted implementation, NOT yet built/tested/device-verified; force-seekable cannot overcome a server that truly lacks range support outside cache. | Trigger Android compile + relevant host tests/CI, inspect option return/log, then device test timeline forward/backward at several positions and confirm no 00:00 reset; adjust based on evidence. |

| 2026-11-02 | A2 subtitle race fix | Implemented at sync/upstream HEAD `a6c5e5b8868ff1a3957a9c069a43262822925c4d`: pending embedded selection waits for actual TEXT groups and requested index; if missing at READY, drops pending to prevent stale cross-item selection. Tests added for delayed availability and off immediate. CI run 41492270870 pending; no passing build/test/device yet. | Inspect exact-head CI/test reports; then continue local/remote/CloudStream, sidecar timing, libmpv and lifecycle regression audit. |

| 2026-11-02 | A2 continued: source audit + exact-head CI | `sync/upstream` HEAD `e259133ab755b247e82750442345fa480bfad8b7`. [CI run 41493375989](https://github.com/somilkhan/MewMobile/actions/runs/41493375989) exact SHA SUCCESS: host tests BUILD SUCCESSFUL (6m05s), Android Kotlin compile BUILD SUCCESSFUL; APK steps skipped. Track enumeration/selection use consistent flattened TEXT indices. Sidecar delay is applied once (positive delays shift cue lookup earlier, negative later, floor at zero); Exo uses renderer playback clock; libmpv native `sub-delay`. External→embedded clears external config and queues track selection until text tracks exist; external attach preserves position/audio and resolves MIME; sidecar passes addon headers without media reload. CloudStream subtitle metadata/header propagation covered by existing test. Log shows Exo text enabled and track selected but doesn't prove visible cues; no confirmed rendering root cause. | Continue A2 edge-case audit and prepare device acceptance checklist; device verification outstanding. No unrelated phase work. |
| 2026-11-02 | A2 upstream subtitle-label parity | Selectively ported Nuvio Enhanced's recent Media3 cue/codec naming fix into Mew's existing `CustomDefaultTrackNameProvider`: Media3 cue tracks no longer display internal MIME as codec text; codec strings resolve to readable labels. Added Android host regression tests for Media3 cues, HEVC codec strings, and SRT/VTT naming. Source commit `237e3f01ce0a72b64596ee392263edd955a4f71f`. CI queued for exact SHA; not verified yet. | Check exact-SHA host tests and Android compile, then continue A2 edge-case review. |
| 2026-11-02 | A3 AutoSync upstream parity audit | Reviewed Nuvio Enhanced `enhanced` AutoSync tree against Mew `sync/upstream`: core matcher is effectively equivalent (Mew uses its own `AutoSyncSubtitleCue` abstraction); Mew's selected-only policy intentionally permits explicit subtitle AutoSync while startup preference is OFF. Confirmed persistence initialization in `MainActivity`, existing settings rows, Exo coordinator/sidecar path, libmpv coordinator/rewrite path, and runtime startup binding. Exact Mew SHA `237e3f01ce0a72b64596ee392263edd955a4f71f` CI run [41495690019](https://github.com/somilkhan/MewMobile/actions/runs/41495690019) **SUCCESS**: Android host tests + Android Kotlin compile passed; APK build/package/sign/upload skipped. This is source/CI evidence, not runtime/device proof; no fix claimed yet. | Continue A3 root-cause trace: inspect subtitle selection callbacks and coordinator/controller forwarding/cancellation, then make a minimal regression-backed repair on the active branch; compare any later Enhanced autosync commits before porting. Device verification remains Phase 5. |

| 2026-11-02 | A3 Enhanced parity gap identified | Direct source comparison of Enhanced `PlayerRuntimeAutoSync.kt` vs Mew confirms coordinator/controller wrappers and explicit selected-subtitle AutoSync are present in both. Concrete missing parity: Enhanced candidate refresh merges repository add-on subtitles with current stream/external subtitles (including header-bearing candidates), while Mew's `currentAutoSyncCandidates()` only maps `addonSubtitles`; Mew runtime also lacks Enhanced's candidate refresh effect keyed to `externalSubtitles`/repository updates. Therefore alternative-subtitle search may not see stream subtitles, even though manual selected sync works. This is a confirmed source difference, not yet a proven user-facing failure. | Port the candidate merge/update narrowly using Mew's `StreamSubtitle` model (URL/language/name/headers), verify repository lifecycle and duplicate URL precedence, add common tests for candidate merge and source updates; then exact-SHA CI. No device verification yet. |

| 2026-11-02 | A3 candidate parity implementation | Implemented on `sync/upstream` SHA `765d7d8f955c646468f4cecf5f551f8138bbfd49`: AutoSync candidate inventory now merges current add-on and active stream subtitles, deduplicates by URL with add-on metadata precedence, and refreshes when either source list changes. Added 3 common tests (both sources, duplicate URL, empty lists). One coherent commit; no APK artifacts. CI run [41498944570](https://github.com/somilkhan/MewMobile/actions/runs/41498944570) was initially cancelled before steps; job rerun attempt 2 requested, pending at handoff. Implementation is not verified until rerun completes. |

| 2026-11-02 | A3 late-addon retry | Ported Nuvio Enhanced's retry guard to Mew `BindAutoSyncRuntimeEffects": when playback is ready, AutoSync startup is enabled, addon lookup completed with candidates, automatic selection previously ran but found no track, and user has not explicitly selected a subtitle, reset selection-applied and rescan tracks. Commit `d7af1c4fad731b937707a72986a8407b4bd945f4". CI run 41498944570 attempt 2 is still in progress at host tests; not verified. | Check exact-SHA CI; add policy-level regression tests; continue restored-subtitle and repository candidate parity audit.

| 2026-11-03 | A3 late-addon retry regression tests | Extracted retry guard into pure `shouldRetryPreferredSubtitleAfterAddonLoad`; added 10 guard assertions covering AutoSync/controller, playback readiness, preference, addon load/count, prior selection, and manual lock. Commit `5c3da38ae5363ce8fd6f9bedbcc92ea5dcb8db0c`. |
| 2026-11-03 | A3 restored add-on preference | Ported Nuvio's descriptive add-on preference restore: resolve against current episode's fresh subtitle list by language/provider/label (never reuse stale URL), and invoke startup AutoSync for restored URL. Added two regression tests. Commit `45494ca74b33d1ead90830f20e4c0ce2bf4d99c2`. CI run `41501808847` on exact SHA currently in progress; no APK. |

| 2026-11-03 | A3 asynchronous restore lifecycle | Added a runtime effect to retry persisted add-on preference resolution when the current episode's add-on list/loading state changes; restored add-on choice now sets manual-selection lock so late automatic selection cannot replace it. Commit `af6ebfa50ca3ee6a39750d54cf6de9c7ff36c9d3` plus `37aa336c70c94305ab0fb148fcfc6ca098a728d1`. Exact-head CI queued; awaiting host tests. |

| 2026-11-03 | A3 restored-addon lifecycle verified | Exact current PR head `37aa336c70c94305ab0fb148fcfc6ca098a728d1` passed Android host tests, Android Kotlin compile, debug build and package checks in [CI run 41502437495](https://github.com/somilkhan/MewMobile/actions/runs/41502437495). APK artifact remains intentionally skipped. This validates source/build only; no device test. A3 AutoSync parity work is ready for Phase 5 device verification, pending final PR/rebase checks. |

| 2026-11-03 | A3 AutoSync parity source/CI acceptance | Final candidate/late-addon/restored-addon selection work is on PR #38 head `37aa336c70c94305ab0fb148fcfc6ca098a728d1`; exact SHA CI [41502437495](https://github.com/somilkhan/MewMobile/actions/runs/41502437495) SUCCESS (host tests, Android compile, debug/package verification). Source/CI accepted; real playback synchronization remains device-unverified. |
| 2026-11-03 | A4 CloudStream Raw Catalog repair | Root cause confirmed: Home transforms all provider categories into `HomeCatalogSection` objects but `loadCloudSections` appended only `result.sections.first()`. Changed it to append every section; removed category hash from keys (collision-prone) in favor of full provider/category identity; added key regression assertions. Commit `f5f46b596e917b546dd70b2b202d12bed31e2c35` plus test import correction `dd631b2f1aace03fdae28fdc71385004b97109e7`. Exact-head CI [41508922731](https://github.com/somilkhan/MewMobile/actions/runs/41508922731) running host tests. |

| 2026-11-03 | A5 purple video evidence review | Private device log shows ExoPlayer `c2.qti.avc.decoder` initialized for AVC 1920×1080, BT.709 standard/SDR transfer, and a first frame rendered; a later READY/isPlaying event occurred. It does not identify which playback session was purple or capture a rendered frame/color pipeline. libmpv reports libplacebo without LittleCMS, but that warning alone is not causal proof. **No speculative decoder/renderer change**; next diagnostic: reproduce with same stream, compare Exo vs libmpv and hardware decode ON/OFF, capture a frame/color metadata at failure. |

| 2026-11-03 | A4 CloudStream Raw Catalog CI accepted | Exact PR head `dd631b2f1aace03fdae28fdc71385004b97109e7`, [CI 41508922731](https://github.com/somilkhan/MewMobile/actions/runs/41508922731) SUCCESS: Android host tests and Android Kotlin compile passed; APK/package/sign/upload correctly skipped (routine CI). All extension-returned categories now map to separate Home rows with collision-safe keys. **A4 source + CI DONE; physical-device confirmation pending.** |

## Phase 5 focused device gate — candidate `dd631b2f1aace03fdae28fdc71385004b97109e7`
- Source changes through A4 accepted by exact-head CI [41508922731](https://github.com/somilkhan/MewMobile/actions/runs/41508922731): host tests + Android Kotlin compile PASS; no APK generated in routine run.
- Generate one full Debug APK now for focused device verification (commit marker `[build-apk]`); don't build/upload APKs on routine CI.
- Device checklist: (1) CloudStream provider with many sections (CineStream: verify all 39 categories are visible and each opens); (2) AutoSync startup preferred language, late addon arrival, explicit selected addon with startup preference OFF, persisted addon on episode reload; (3) internal, addon and stream subtitle display/switch/delay; (4) timeline forward/backward seek and seek-bar taps on a known seekable stream; (5) rotate portrait↔landscape repeatedly and exit restore; (6) same purple-video sample under Exo/libmpv and hardware decode ON/OFF; (7) TMDB v3 API key and v4 token separately, metadata plus IMDb/TMDB IDs; (8) confirm TMDB and CloudStream remain independently available.
- Do not claim device pass from CI. Capture APK SHA, install/update result, and per-flow pass/fail; raw logs must be redacted.
