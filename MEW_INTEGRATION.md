# Mew ⇄ luqman upstream integration guide

Mew Mobile tracks **luqmanfadlli/NuvioMobile-Enhanced** ("luq") as its upstream.
This file explains how the codebase is laid out so future luq releases can be merged
with small, predictable conflicts.

Last integrated release: **luq `1.1.0-beta.3`** (merge commit on `integrate/luqman-1.1.0-beta.3`).
The per-file record of decisions is in [`docs/upstream-merges/luqman-1.1.0-beta.3.md`](docs/upstream-merges/luqman-1.1.0-beta.3.md).

## Feature rule

| Situation | What we do |
|---|---|
| Feature exists only in Mew | Keep it (moved into a `mew/` file where possible) |
| Feature exists only in luq | Adopt it |
| Both have it, luq's version is newer/better | Take luq's version, keep Mew-only bits as hooks |
| Both have it, Mew's is the product identity (app shell, theme, updater, downloads, CloudStream, MyAnimeList, cinematic hero, details screen, profiles) | Keep Mew's, adapt luq callers through compat files |

## The `mew/` convention

1. **Mew-only code lives in `…/features/<feature>/mew/*.kt`**, using the *same package*
   as the upstream feature (`package com.nuvio.app.features.<feature>`). Because the
   package is the same, upstream files can call Mew code without new imports.
2. **Upstream (luq) files only get one-line hooks**, marked with a `// Mew hook` comment.
   When a luq file changes in the next release you can usually take luq's file whole
   and re-apply the few `// Mew hook` lines (`rg -n "Mew hook" <file>` lists them).
3. **Compat shims** (`Mew*Compat.kt`) provide luq APIs on top of Mew implementations,
   so luq screens compile against Mew modules (e.g. What's New on Mew's updater).
4. Private top-level names are file-scoped but class names are not: never declare the
   same top-level class/object name in an upstream file and a `mew/` file.

### Current `mew/` files

| File | Purpose |
|---|---|
| `settings/mew/MewSettingsRoot.kt`, `MewSettingsHost.kt`, `MewSettingsSearch.kt` | Mew-only settings pages inserted into luq's redesigned settings categories |
| `settings/mew/MewPlaybackSettingsExtras.kt` | Mew-only playback options shown inside luq playback sub-pages |
| `settings/mew/MewProfileInsights.kt` | Favorite people, concierge, profile background in luq profile page |
| `settings/mew/MewTrackingProviderCards.kt` | MyAnimeList (and legacy Mew AniList) tracking cards |
| `streams/mew/MewStreamSearchExtras.kt`, `MewStreamsScreenExtras.kt` | CloudStream search title, episode synopsis on the streams screen |
| `home/components/mew/MewHomeHeroSection.kt` | Mew cinematic home hero (luq `HomeHeroSection.kt` kept verbatim) |
| `downloads/mew/MewDownloadBadgeSupport.kt`, `MewDownloadSubtitles.kt` | luq download badges / subtitle lookups on Mew downloads |
| `library/mew/MewLibraryReleaseSchedule.kt` | luq release-schedule API (Upcoming, clear cache) on the Mew library |
| `player/mew/MewPlayerControlsExtras.kt`, `MewPlayerNextEpisode.kt` | device-status overlay, random-episode, prepared next episode |
| `androidMain/.../player/mew/MewPlayerEngineHelpers.android.kt` | Mew engine test helpers |
| `livetv/mew/MewLiveTvIncomingSource.kt` | Shared-link / M3U intake into luq Live TV |
| `updater/mew/MewSemanticVersion.kt`, `MewWhatsNewUpdaterCompat.kt` | luq What's New on the Mew updater (Mew release repo) |
| `com/nuvio/app/mew/MewAppShellCompat.kt` | luq app-shell helpers (`AppLoadingProfileAvatar`) for luq settings on Mew's shell |

### Hybrid files (expect conflicts here)

These were merged by hand and mix both sides; review them carefully on every merge:
`App.kt`, `HomeScreen.kt`, `HomeRepository.kt`, `TmdbMetadataService.kt`,
`MetaDetailsRepository.kt`, `details/MetaDetailsScreen.kt` (Mew), `SearchRepository.kt`,
`MainActivity.kt`, `PlayerScreenRuntimeUi.kt`, `PlayerScreenRuntimeEffects.kt`,
`PlayerControls.kt`, `AddonRepository.kt`.

## Merging the next luq release

```bash
git fetch luq --tags
git switch -c integrate/luqman-<tag> origin/enhanced
git merge --no-commit <tag>          # resolve file by file
export UPSTREAM_REF=<tag>
```

Resolve conflicts with the feature rule. For files that only carry `// Mew hook` lines,
take luq's file (`git checkout <tag> -- <file>`) and re-apply the hooks.
Then run the static checks from the repo root (no JDK needed):

```bash
python3 tools/upstream-merge/importcheck.py      # unresolved com.nuvio imports
python3 tools/upstream-merge/objcheck.py         # Object.member uses that do not exist
python3 tools/upstream-merge/xdup.py             # same-package duplicate declarations
python3 tools/upstream-merge/expectcheck.py | grep MISSING   # expect without actual
python3 tools/upstream-merge/expectsig.py        # expect/actual signature drift (heuristic)
python3 tools/upstream-merge/dupdecl.py $(git diff --name-only HEAD -- '*.kt')
python3 tools/upstream-merge/sigcheck.py <file>  # named-arg checks (heuristic)
python3 tools/upstream-merge/fix_res_imports.py $(git diff --name-only HEAD -- '*.kt')
python3 tools/upstream-merge/dedupe_imports.py  $(git diff --name-only HEAD -- '*.kt')
python3 tools/upstream-merge/merge_strings.py <strings xml files>   # key-level 3-way merge
python3 tools/upstream-merge/brand_new_strings.py                   # Nuvio -> Mew in new strings
```

Branding rules: user-visible text says **Mew**, except "upstream Nuvio", "Nuvio Supporter",
"Supporting Nuvio", "Nuvio tier" and the "Nuvio web panel". CI rejects `Discord`, `Ko-fi`,
`Mew Enhanced` and `Nuvio Enhanced` in `composeResources/*/strings.xml`.

Record each file decision in `docs/upstream-merges/luqman-<tag>.md`, commit the merge with
the luq tag as second parent, and let `mew-cloudstream-build.yml` compile it.

## Not adopted from luq (as of 1.1.0-beta.3)

- Discord Rich Presence, luq membership / profile backgrounds, luq update channels.
- luq downloads module (Mew downloads kept), luq offline download metadata.
- luq library list management, genre/year filters and release-calendar cache UI.
- luq `MetaDetailsScreen` / `DetailsDestinations` (Mew details screen kept with adapters).
- Mew in-player Live TV channels panel was replaced by luq live channels.
