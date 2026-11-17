# Mew Context Maintenance

This directory is the durable, repository-side record of Mew audit and integration context. It is intentionally separate from application source code.

## Source of truth

Use this order when facts conflict:

1. Current GitHub branch refs, commit history, repository files, and CI results.
2. This repository-side context record and approved project decisions.
3. ChatGPT Project instructions and context snapshot.
4. Earlier chat summaries.

A remembered SHA, test result, or prior assistant statement is only a lead. Re-fetch mutable facts before relying on them.

## Branch roles

- `upstream-sync`: upstream-derived reference snapshot. Do not merge its history wholesale into Mew. This document is a Mew-owned maintenance note added at the user's explicit request; it is not an upstream feature port.
- `Main-dev`: destination for reviewed, selective integration.
- `main`: stable development/release branch; leave untouched until explicit promotion approval.
- Mother upstream: `luqmanfadlli/NuvioMobile-Enhanced`, branch `enhanced`.

Before any sync or write, fetch the relevant refs and record their exact SHAs. For branch-ref updates, use an expected-SHA/lease guard. Never force-update a branch to work around a mismatch.

## Context update workflow

### 1. Inspect before editing

- Re-fetch `upstream-sync`, `Main-dev`, `main`, and mother-upstream refs as relevant.
- Record the current SHAs and compare metadata.
- Inspect the current repository-side context file and the ChatGPT Project instructions/snapshot.
- Search the repository for the behavior and its callers; do not infer from filenames alone.
- Check whether the claimed finding is already implemented under another name, source set, route, or layer.

### 2. Write only verified facts

For each audit item record:

- feature/behavior and user-visible impact;
- upstream commit and file paths;
- Main-dev equivalent, including renamed or cross-source-set implementations;
- semantic differences: inputs, outputs, side effects, lifecycle, error behavior and callers;
- dependency/API/schema changes and compatibility risks;
- UI/navigation/accessibility/theme/state-restoration impact;
- tests present and tests actually run;
- decision: `TAKE`, `ADAPT`, `SKIP`, `ALREADY IMPLEMENTED`, or `NEEDS INVESTIGATION`;
- exact next verification step.

Separate confirmed facts from hypotheses and open questions. Correct prior mistakes explicitly rather than silently carrying them forward.

### 3. Keep the audit and integration separate

- Audit first; do not edit application code during an audit-only request.
- Never wholesale-merge the divergent upstream history.
- Propose small logical integration batches for `Main-dev`, with exact files, risks and verification plan.
- Implement only after explicit user approval.
- Do not merge, force-push, rewrite history, delete branches/files, publish releases, or change project settings without explicit approval.
- Keep Mew-specific application ID, signing, provider compatibility, profile data, settings, downloads and playback behavior unless a reviewed change explicitly requires otherwise.

### 4. Verify every change

- Inspect Gradle tasks, workflows, version catalog and source sets before selecting build/test commands.
- Run the narrowest relevant tests first, then justified broader checks.
- Inspect diffs and test reports.
- A skipped, queued, or in-progress CI job is not a pass.
- Do not claim local build, device UI, test, merge, upload or code-change success without direct tool evidence.
- If no local checkout/device is available, say so plainly; remote source inspection is not runtime validation.

### 5. Maintain ChatGPT Project context

The ChatGPT Project context is a concise handoff, not a competing source of truth.

- Update it only after a meaningful milestone or before a handoff.
- Include current verified refs, audit decisions, exact files, test outcomes and remaining risks.
- Do not copy the entire repository audit into Project instructions. Keep stable working rules in Project instructions and changing progress in the context snapshot.
- Prefer updating the repository-side Markdown through GitHub on the explicitly requested branch, then read the committed file back and verify its commit/path/content.
- If a separate ChatGPT Library upload is requested, first list/read the current Library item and note its exact `library_file_id` and version. Materialize the exact current file, edit that materialized copy, verify the exported source `file_id`, upload with the expected current version, then list/read it again. Never guess IDs, reuse stale IDs, or retry blindly after Not Found.
- If Library upload tooling fails, stop repeated attempts. Report the Library item/version that remains unchanged and use the verified repository copy as the working source until the Library path is repaired.

## Duplicate/overlap audit (mandatory)

Do not call something a duplicate from filenames alone. Search by:

- same user-visible behavior under different names/screens;
- same algorithm with renamed symbols or small guard/literal changes;
- duplicate routes, settings, repositories, services, providers, adapters, models and state holders;
- repeated event handling, formatting, networking, caching, playback and error handling;
- commonMain/androidMain/iosMain equivalents and legacy/new implementations.

For each suspected overlap compare inputs, outputs, side effects, lifecycle, errors, callers and platform scope. Classify it as true duplicate, intentional parallel implementation, shared pattern with different semantics, compatibility shim, or false positive. Do not refactor intentional parallel implementations just to reduce line count.

## UI and regression checks

Before changing a screen inspect navigation entry points, state, loading/empty/error states, accessibility semantics, dark/light theme, small/large screens, back behavior, focus/keyboard and restoration.

Treat playback, providers, networking, downloads, profile/account lifecycle and settings as regression-sensitive. Treat TLS, hostname verification, secrets, cleartext policy, crypto, manifests, signing and CI publishing as security-sensitive.

## Required handoff format

Keep reports concise and factual:

- **Refs:** branch names and verified SHAs.
- **Inspected:** exact files/commits and search scope.
- **Findings:** confirmed behavior and semantic differences.
- **Decision:** take/adapt/skip/already implemented/needs investigation.
- **Changes:** exact paths and commit SHA, or explicitly “no code changes”.
- **Verification:** exact commands/checks and outcomes; distinguish not run from failed.
- **Risks / next step:** unresolved items and the smallest next action.

Never describe a partial audit as exhaustive. State API limits, missing local checkout, unavailable device tests, or uninspected call sites.


## Continued semantic audit — feature overlap and server boundaries (2026-11-17)

### Fresh refs used for this audit
- Mew `Main-dev`: `207ef8fa309b8061b73039b2d4e4bde4af65050e`.
- Mew `upstream-sync`: `1028f9a2c4ce7b6a73026eb241941a957ce2d5fb` (includes this repository-side context document commit).
- Mother upstream `enhanced`: `b4e446451f32edbdf8b1bf288d1a7a8a2bf10e03`.
- These refs were re-fetched during this audit. No application source or branch ref was changed.

### Episode shuffle
- `upstream-sync` contains `features/shuffle/EpisodeShuffleSheet.kt` (SHA `201ed9e9aaa34004d910184b93325ad3923f1dc1`) and `EpisodeShuffleRepository.kt` (SHA `0b527a0425dbabd7f367b613d378ea97977cc753`).
- The inspected `Main-dev` paths for the shuffle sheet/repository are absent (GitHub Contents API returned 404 for those exact paths). This supports the earlier finding that shuffle is a new feature in the inspected tree, not proof that every possible differently named equivalent is absent.
- Decision remains **TAKE candidate**, not approved for integration. Before implementation, trace every upstream entry point and playback handoff; verify watched/unwatched selection, repeat history, empty/failed selection, profile switch/removal, lifecycle cancellation and UI dismissal/state restoration. Port relevant unit/UI tests with the feature.

### Custom posters versus Mew poster customization
- `upstream-sync` has `core/poster/CustomPosterUrlResolver.kt` (SHA `050dd51359869fb3d67728ad480001ee357fbfe7`) and expect/actual storage contract `CustomPosterUrlStorage.kt` (SHA `943689412dd69307c91792af35413f6d30fc528e`).
- Exact custom-poster resolver/storage paths are absent from `Main-dev`. However, `Main-dev` does have `features/settings/PosterCustomizationSettingsPage.kt` (SHA `4c67989986c1a889608f2da6e745034b6b9174ae`); the similarly named upstream-sync settings page is a different blob (SHA `0d1b16a5894f8b8658d46c701450d5f591e8edae`). These are related poster-domain settings, not proof of equivalent URL-template behavior.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS**. Decision remains **ADAPT candidate**. Preserve Mew card appearance/layout controls. Before porting, trace all image consumers and storage actuals; validate schemes/hosts/redirects, placeholder escaping, cache keys, fallbacks, per-screen enablement and profile isolation.

### External media servers versus Nuvio backend configuration
- `upstream-sync` contains a separate `features/servers/ServerRepository.kt` (SHA `6e770e38514831e35092a840aff84145a7e08f7e`), `ServerStorage.kt` expect contract (SHA `bde79fe01d16fbd1d5ec5bf6644f57a8d5a25231`), `mediabrowser/MediaBrowserProvider.kt` (SHA `0765cbd53fdaa6c2cd8e046c3dcce92de5cede23`) and `core/network/ServerDiscovery.kt` (SHA `fdcb8d283ce41fb21c0ce3ae2717be36fc249d32`).
- Those exact feature paths are absent from `Main-dev`. Main-dev's `core/network/ServerConfiguration.kt` (SHA `c658108f1708961bb8d25bec21624e9ba540eee0`) configures the Nuvio backend URL/publishable key/capabilities; it is not the same feature as user-managed Jellyfin/Emby servers.
- Classification: **SHARED PATTERN / DIFFERENT SEMANTICS** (both concern server configuration, but different server roles and contracts). Decision remains **NEEDS INVESTIGATION** as a phased feature, not a drop-in replacement.
- Before any integration, inspect Android/iOS storage actuals, credential/token protection and deletion, redirects/TLS, server discovery, catalog mapping, playback/transcoding, watch-state sync, profile switching/removal and all settings/navigation callers. Keep external server credentials separate from Nuvio backend configuration and ordinary profile settings sync.

### Verification limits and next step
- This was a selective path/blob inspection using GitHub remote files. No local checkout/worktree, Gradle tasks, runtime, emulator/device, full call graph or complete test suite was available/verified.
- No application code, branch ref, merge, cherry-pick, PR, build, test, or release was changed/run.
- Continue with storage expect/actual implementations and all feature entry-point/caller searches before proposing any integration batch. No integration write without explicit approval.
