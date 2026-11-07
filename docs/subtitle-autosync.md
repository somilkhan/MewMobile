# Subtitle AutoSync

AutoSync compares a selected add-on subtitle with subtitle timing embedded in the active video stream. It can correct a constant offset, frame-rate drift, and local cue-group timing when the match passes confidence checks. It does not analyze audio or use speech-recognition models.

## Behavior

- **Startup:** the preferred-subtitle AutoSync setting gates automatic startup selection. When enabled, startup search may try another add-on subtitle in the same language.
- **Manual selection:** explicitly selecting an add-on subtitle requests AutoSync for that subtitle even when startup AutoSync is disabled. Manual selection does not search alternative providers.
- **Reference loading:** indexed HTTP(S) Matroska/MP4 references are used where available. Unsupported sources or missing usable reference timing fall back to the original subtitle instead of applying a speculative correction.
- **ExoPlayer:** confident corrections replace the active sidecar cue timeline without reloading the media source, preserving the playback buffer and cue styling. The extractor observer is rewrapped around Mew's libass-aware Matroska extractor when libass is enabled.
- **libmpv:** confident corrections are written to a temporary subtitle file in the app cache and replace the external subtitle track. The original subtitle remains selected when analysis or rewriting fails.
- **Cancellation:** changing subtitle, selecting an embedded track, disabling subtitles, adjusting delay manually, or leaving playback cancels stale work so an old result cannot override the newer choice.
- **Privacy:** AutoSync does not emit raw URL diagnostics. Sidecar logs redact URL credentials, query strings, and fragments.

## Automated coverage

Common tests cover constant offsets, frame-rate drift, confidence rejection, grouped cue repairs, partial subtitles, missing intros/outros, ambiguous timelines, cancellation checkpoints, reference consistency, no-fit tracking, and parsing SRT, WebVTT, ASS/SSA, and TTML. Android source/host-test CI is run without packaging an APK; device-specific playback verification is reserved for Phase 5.

## Phase 5 device checks

1. Compare an already-aligned subtitle with a known offset and a drifting subtitle for both ExoPlayer and libmpv.
2. Confirm applying a result does not rebuffer or restart playback and preserves subtitle styling.
3. Switch to another add-on subtitle, select an embedded track, disable subtitles, change delay manually, and leave playback while analysis is running; the old result must not reappear.
4. Check a video without usable embedded subtitle timing and an unsupported subtitle format; the original subtitle should remain intact and failure feedback should be clear.
5. Repeat with libass enabled and with startup AutoSync disabled but a subtitle selected manually.
