# MewMobile — Physical Device Test Report

**Date received:** 2026-11-02  
**Repository:** https://github.com/somilkhan/MewMobile  
**Tested build:** `sync/upstream` candidate `4d838a356a658bf21c9eab0d63c83124d9ba5dd4` (`4d838a3`)  
**APK:** Full Debug artifact `mew-cloudstream-full-debug`, artifact ID `13101531350`  
**Package:** `com.mew.mobile.debug`  
**APK SHA-256:** `ec29b1507181e4ec23c56a295e90f3b81beb45019fb42bd09c624be9fc24607f`  
**Device log:** `02_11-04-36-11_811.log`  
**Log SHA-256:** `b8345f8b507bf73cf3ae4002dd22316ccac9847c644497acda204475634854c0`  
**Log size:** 3,574,178 bytes; 23,310 lines  
**Device:** vivo V2318T / V2318, SDK 36, arm64  
**Log interval:** 2026-11-01 23:06:11 UTC – 2026-11-02 00:11:45 UTC

> The raw device log is intentionally not committed. It contains credential-like values in request URLs (including a TMDB API-key query parameter) and device/runtime details. Keep the original private; redact query keys, bearer tokens, cookies, account identifiers and signed media URLs before sharing another copy. The key's owner is not established from this capture; if it belongs to the maintainer, rotate it.

## 1. User-reported device results

These are the maintainer's hands-on observations for this candidate, not inferences from Logcat.

| Area | Result | Observation |
|---|---|---|
| App launch | PASS | App opens to Home. |
| Jelly bottom navigation | PASS | Elastic/animated selection pill is visible and moves between tabs. |
| CloudStream extension install/load | PASS | Installed extensions load and appear in the app. |
| CloudStream playback | PARTIAL | Streams start and play; seek/skip is unreliable, tapping a timeline timestamp can restart at 00:00, and subtitle rendering is mixed. |
| CloudStream catalog presentation | FAIL | Only one Raw Catalog is visible per extension; investigate section mapping/presentation. |
| TMDB setting persistence | PASS | Enable state remains enabled after leaving and reopening Settings. |
| TMDB metadata | FAIL | Metadata does not appear after entering the long key and short token. |
| TMDB / CloudStream routing | FAIL / REQUIREMENT RECONCILIATION | The user previously observed CloudStream navigation being blocked while TMDB was enabled. Current product requirement is independent provider operation; do not impose mutual exclusion. |
| Subtitle Auto Sync | FAIL | Enabled with a supported language, but no noticeable synchronization effect. |
| CloudStream auto-update | NOT TESTABLE | No extension update was available; source/tests must validate the update path. |
| Watch Party | MISSING | No player action or settings entry is available. |
| Player rotation | FAIL | User reports the in-player rotation control does not work. |
| Purple video output | FAIL | Purple screen still occurs. |
| In-player playback-engine switch | MISSING | Engine setting exists, but the player does not expose the Nuvio-style runtime switch. |
| Liquid Glass player | FAIL / DESIGN MISMATCH | Current toggle/effect is not the requested alternate player. Enabling it must switch from the default player layout to a distinct Liquid Glass player design; disabling it must restore the default layout. |
| New player/stream labels | MISSING | The expected newly ported labels/badges are not visible. Identify exact upstream components before porting; do not duplicate existing stream-card badges. |

## 2. Logcat findings

### Crash and responsiveness
- No `FATAL EXCEPTION`, fatal native signal, or ANR marker was found in this capture. This is not a general crash-free guarantee.
- 18 Choreographer skipped-frame events were recorded; the largest was 108 frames. Additional bursts included 97 and 106 frames. This is a measurable responsiveness issue to profile, not proof of one specific main-thread cause.
- 323 `Surface doesn't have any previously queued frames` warnings and 82 DMA-BUF allocation warnings were recorded. These are relevant to rendering investigation but do not alone prove the cause of the purple screen.

### Authenticated backend sync
- 121 backend responses reported `Unauthorized: valid session required for sync` (PostgREST code `28000`).
- 117 failed watch-progress scrobble pushes and 3 failed add-on sync pushes are explicitly logged; at least one additional matching authorization response is present in the capture.
- Source review found that `WatchProgressRepository.pushScrobbleToServer` and `AddonRepository.pushToServer` can issue writes without first requiring a valid non-anonymous remote session. Watch-progress pull already has an auth guard, but the write paths do not consistently mirror it.
- Fix the root cause: keep local progress/add-on state authoritative while signed out or anonymous, suppress futile remote writes/retry storms, and flush eligible pending changes only after a validated authenticated session is available. Do not discard local changes.

### TMDB identity and credentials
- Mew's `TmdbService` recorded 10 HTTP 401 responses for `find/{imdbId}` and `tv/{id}/external_ids`. The service currently builds requests using the `api_key` query parameter. A v4 read-access token is not interchangeable with a v3 API key in that request shape; verify the settings field contract and support only the credential format actually documented by the UI/API.
- Multiple plugin-runtime requests used a title (for example, `Mirzapur`) or an IMDb-style ID as the path segment of TMDB `/movie/{id}` or `/tv/{id}` requests, returning 404. Those path segments require canonical numeric TMDB IDs.
- Trace the media identity passed through the Mew-to-plugin bridge. Normalize valid `tmdb:` numeric IDs and resolve IMDb IDs through `TmdbService.ensureTmdbId`; do not pass display titles or raw IMDb IDs as TMDB IDs. Add tests for movie, TV, IMDb, title-only and missing-ID cases.
- Treat the 401 credential failure and the 404 wrong-identifier failure as separate issues; do not mask one with the other.

### CloudStream
- The log confirms successful catalog responses from five extensions at least once: CineStream (39 sections / 1,381 items), BollyFlix (4 / 81), MoviesDrive (6 / 148), Moviesmod (4 / 81), and VegaMovies (5 / 101).
- Successful stream-link responses include CineStream (124 links / 1 subtitle), MoviesDrive (31 and 12 links), and Rogmovies (22 links). One CineStream request later timed out after partial link/subtitle output.
- Other provider calls fail with external DNS resolution failures, connection resets, HTTP errors and provider-specific parse errors. Classify those individually; do not convert provider-site outages into global CloudStream failures.
- The fact that CineStream reports 39 sections while the UI exposes one Raw Catalog points toward a grouping/mapping/rendering issue after provider response parsing. Trace the catalog-to-Home mapping before changing plugin execution.

### Playback, source selection and orientation
- Two ExoPlayer source failures are logged. One reports loading finished before preparation completed; another reports `UnrecognizedInputFormatException` because no available extractor could read the response. The first is followed by a logged fallback to libmpv. Capture the selected URL's response headers/content type and source classification to determine whether this is a wrong MIME/source-type decision or an invalid provider response.
- The log includes mpv surface attach/detach and unavailable video-parameter property reads. It does not contain enough evidence to identify the purple-screen color pipeline or decoder as the root cause.
- Seven `ROTATION_90` observations occur in the log. That proves the Android window rotated during the capture, but it does not prove the in-player toggle callback or requested orientation path worked. Keep the user's reported rotation failure open and trace the button-to-Activity request lifecycle.

## 3. Watch Party integration decision

**Decision: reuse the official Nuvio backend contract; do not create a separate Mew-hosted Watch Party database/API.**

Reviewed Nuvio Mobile source at `cmp-rewrite` SHA `46febb568348aa5e135bff0d4d5c914933aff5a0`:
- `PartyApi` calls the backend RPCs `watch_party_create`, `watch_party_join`, `watch_party_preview`, `watch_party_link_profile`, `watch_party_heartbeat`, `watch_party_update`, `watch_party_leave`, `watch_party_end`, `watch_party_set_host_only`, `watch_party_remove_member`, `watch_party_send_message`, `watch_party_list_messages`, `watch_party_clock`, and `watch_party_enabled`.
- `PartyChannel` uses a private Supabase Realtime channel named `watch-party:{partyId}`, with broadcast events and presence.
- `PartyRepository` checks the active server's `capabilities.watchParty`, refreshes the server-side enabled flag, and requires a signed-in, non-anonymous account for create/join.
- Nuvio's Supabase client installs Realtime. Mew's current `SupabaseProvider` installs Auth, PostgREST and Functions but not Realtime. Mew's Gradle runtime configuration has an official Nuvio API fallback; the effective endpoint/key must still be verified against the official server configuration at build/runtime.
- Integration prerequisites: verify official backend capability/RPC availability; add the Realtime dependency/client module and lifecycle cleanup; enforce validated non-anonymous auth; port models, RPC adapters, channel/presence, clock/synchronization, chat, error handling and player/settings entry points; add tests for reconnect, host/member permissions, late join, drift correction, leave/end, profile changes and server-disabled behavior.
- Status: **DECIDED / NOT IMPLEMENTED**. Do not add only a UI button and call Watch Party complete.

## 4. Remediation order

1. Stop unauthenticated sync write storms without losing local state; add auth-gating and deferred-flush regression tests.
2. Fix TMDB credential contract and canonical media-ID normalization at the plugin bridge.
3. Reproduce and repair seek/timestamp reset, subtitle selection/rendering and AutoSync behavior.
4. Trace the purple-screen path across ExoPlayer/libmpv selection, surface lifecycle, decoder, color format and Dolby Vision handling; use source-backed diagnostics.
5. Repair player rotation lifecycle and verify both orientations.
6. Implement the Nuvio-compatible in-player engine switch, distinct Liquid Glass player layout, and missing labels after source parity review.
7. Port Watch Party against the official Nuvio backend contract after auth/realtime prerequisites are proven.
8. Fix CloudStream section/catalog presentation and profile performance based on measured traces.

## 5. Evidence limits

- User-reported PASS/FAIL results are attributed to the physical-device test session; the log is supporting evidence, not a replacement for those observations.
- This report does not prove a source fix, a new build, CI after remediation, or device verification of a future build.
- Raw Logcat is not stored in this public repository because of credential-like URL parameters. Use the SHA-256 above to identify the private original.
