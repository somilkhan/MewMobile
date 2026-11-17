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
