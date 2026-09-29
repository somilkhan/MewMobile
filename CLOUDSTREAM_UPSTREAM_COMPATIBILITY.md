# Mew upstream compatibility map

This document records the current provenance boundary for inherited and Mew-specific CloudStream work.

## Source layers

| Layer | Role | Rule |
| --- | --- | --- |
| UPSTREAM | reCloudStream CloudStream runtime/API contracts | Match the current public ABI and lifecycle where Mew embeds/implements it. |
| NUVIO | Official NuvioMobile application architecture | Prefer existing Nuvio orchestration, playback, storage and UI behavior instead of recreating it. |
| ENHANCED | NuvioMobile-Enhanced Android CloudStream compatibility implementation | Reuse proven compatibility surfaces and document intentional deviations. |
| MEW | Product/UI layer and Android dynamic-repository bridge | Keep Mew-specific behavior behind the CloudStream adapter boundary. |

## Current Mew boundary

The CloudStream integration is intentionally split into:

1. Common CloudStream models, repository parsing, package inspection, compatibility and Nuvio conversion.
2. Full-distribution CloudStream orchestration.
3. Android full runtime integration for `.cs3` execution.
4. Android CloudStream compatibility shims under `com.lagradost.cloudstream3`.
5. Mew playback/orchestration outside the CloudStream runtime.

The app-facing code must not become dependent on provider-specific implementation details.

## Intentional Mew deltas

### Dynamic repository bridge

Mew's Android `RepositoryManager` implements the native repository ABI while Mew remains the owner of repository UI/download state. This is required because downloaded CloudStream extensions can inspect the native repository registry.

### Plugin lifecycle

Mew registers `PluginData` with `PluginManager` before invoking `BasePlugin.load()`. This matches the current CloudStream lifecycle ordering and supports extensions that inspect the plugin registry during initialization.

A plugin with zero `MainAPI` providers is not automatically treated as failed. Repository/bootstrap plugins may register repositories asynchronously.

### Repository bootstrap

Mew reconciles native repository registrations after plugin load and performs a bounded background reconciliation window for asynchronous bootstrap plugins.

### Link loading

Mew treats `ExtractorLink` callbacks as authoritative output. A provider may return `false` after emitting links, and a timeout may return already-emitted links instead of discarding them.

### Host-owned state

Mew owns installation, persistence, repository UI and plugin package lifecycle. The CloudStream compatibility surface must not silently create a second provider catalog or mutate Mew-owned state outside the adapter boundary.

## Non-goals

- Do not copy arbitrary provider implementations into Mew.
- Do not invent CloudStream lifecycle APIs.
- Do not make TMDB a prerequisite for CloudStream providers.
- Do not add fake provider/content IDs or a parallel provider abstraction.
- Do not change provider timeouts globally without provider/runtime evidence.
- Do not rename technical `nuvio.*`/CloudStream identifiers merely for branding.

## Change gate

For every substantial CloudStream change:

1. Inspect current CloudStream source.
2. Inspect the current official NuvioMobile source.
3. Inspect the current Enhanced implementation when applicable.
4. Identify the proven behavior being reused.
5. Make the smallest Mew-specific delta.
6. Add/adjust tests where the behavior is locally testable.
7. Build and run the relevant CI workflow.
8. Device-verify runtime behavior before calling it verified.
9. Record any intentional divergence here or next to the implementation.

## Evidence labels

Use these labels in engineering handoffs:

- `UPSTREAM` — directly matches/reuses current CloudStream behavior.
- `NUVIO` — inherited/adapted from official NuvioMobile.
- `ENHANCED` — inherited/adapted from NuvioMobile-Enhanced.
- `MEW` — intentional Mew-specific behavior.
- `NOT VERIFIED` — not backed by a build, CI result, or device evidence.