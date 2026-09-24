<div align="center">

  <h1>MewMobile</h1>

  <p><strong>A community fork of NuvioMobile focused on a cleaner Mew experience, CloudStream compatibility, performance, and user-controlled sources.</strong></p>

  <p>
    <a href="https://github.com/somilkhan/MewMobile/releases"><img src="https://img.shields.io/github/v/release/somilkhan/MewMobile?style=for-the-badge&label=Latest%20Release" alt="Latest release" /></a>
    <a href="https://github.com/somilkhan/MewMobile/actions"><img src="https://img.shields.io/github/actions/workflow/status/somilkhan/MewMobile/mew-cloudstream-build.yml?style=for-the-badge&label=Android%20Build" alt="Android build status" /></a>
    <a href="https://github.com/somilkhan/MewMobile/blob/enhanced/LICENSE"><img src="https://img.shields.io/github/license/somilkhan/MewMobile?style=for-the-badge" alt="GPL-3.0 license" /></a>
  </p>

  <p>
    <a href="#what-is-mewmobile">What is MewMobile</a> |
    <a href="#getting-started">Getting started</a> |
    <a href="#discovery-setup">Discovery setup</a> |
    <a href="#builds">Builds</a> |
    <a href="#development">Development</a>
  </p>

</div>

## What is MewMobile

MewMobile is an independent community fork of [NuvioMobile](https://github.com/NuvioMedia/NuvioMobile).

The project keeps the upstream client architecture while focusing on:

- **Mew branding and UX** rather than the upstream product identity.
- **CloudStream compatibility** as a first-class source layer.
- **Optional TMDB enrichment** — TMDB is not required for CloudStream content to work.
- **User-controlled addons and plugins** instead of a built-in all-in-one source bundle.
- **Performance and frame pacing** across Home, navigation, Details, image loading, and background work.
- **Android-first testing** with release builds suitable for beta and stable distribution.

MewMobile does not host media or provide media itself. Users add and control their own compatible sources.

## Getting started

MewMobile is intentionally source-driven. After installation, add at least one content source before expecting Home or Search to populate.

### Recommended first setup

**1. Metadata / catalogs**

Install a compatible metadata/catalog addon from the Addons section:

- [TMDB addon configuration](https://tmdb.elfhosted.com/configure)

**2. CloudStream providers / plugins**

Open the Plugins section and add the provider repository:

- [Phisher98 Nuvio providers repository](https://raw.githubusercontent.com/phisher98/phisher-nuvio-providers/refs/heads/main/manifest.json)

**3. Enable the sources**

After installation, enable the addon/plugin and return to Home. MewMobile keeps these source layers separate:

| Layer | Purpose |
| --- | --- |
| Addons | Catalogs, metadata, streams, subtitles and other Stremio-compatible resources |
| CloudStream plugins | Provider/search/home/details/episode/playback sources through the CloudStream compatibility layer |
| TMDB enrichment | Optional metadata enrichment; not a prerequisite for CloudStream content |

> **Important:** MewMobile does not ship a built-in AIO source bundle. Add the sources you actually want to use.

## Discovery setup

The recommended onboarding flow is intentionally simple:

**Discovery → Addons → install metadata source → Plugins → install provider repository → enable sources**

The UI should explain this progressively rather than presenting users with an empty Home and no indication of what to do next.

### Useful source URLs

- TMDB addon: [configure/install page](https://tmdb.elfhosted.com/configure)
- CloudStream provider repository: [manifest.json](https://raw.githubusercontent.com/phisher98/phisher-nuvio-providers/refs/heads/main/manifest.json)

Source availability can change independently of MewMobile. A failed third-party source should not be interpreted as an MewMobile application failure without checking the source itself.

## Features

- CloudStream-compatible provider loading.
- Dynamic CloudStream plugin/repository support.
- Optional TMDB enrichment.
- Stremio-compatible addon installation.
- Search, Home, Details, episodes and playback through supported sources.
- Multiple player backends.
- Profiles, library, watch progress and tracking integrations inherited from the upstream architecture.
- Performance-focused Compose and image-loading optimizations.
- Runtime diagnostics for network, metadata, streams and plugin activity.

## Builds

MewMobile is developed with separate **beta** and **stable** distribution tracks.

### Beta

Beta builds are for testing the newest MewMobile changes before a stable release.

### Stable

Stable builds are release builds intended for regular use after validation.

Release artifacts are produced by GitHub Actions. Do not treat a CI build as proof of device-level performance; physical-device verification remains a separate validation step.

## Development

MewMobile is a Kotlin Multiplatform / Compose Multiplatform project.

### Android debug build

```bash
./gradlew :androidApp:assembleFullDebug -Pnuvio.android.distribution=full
```

### Validation

```bash
./gradlew allTests :androidApp:lintFullDebug
```

Credentials and private configuration must never be committed to Git.

For CI, keep signing material, API credentials, and other private values in **GitHub Actions repository secrets**. Public client configuration may be embedded only when it is explicitly designed to be public.

## Repository configuration

The repository's release/update configuration points to:

- Repository owner: `somilkhan`
- Repository: `MewMobile`
- Development branch: `feature/cloudstream-dynamic-repositories`
- Base branch: `enhanced`

The in-app update checker must use the MewMobile repository, not the upstream or an unrelated Enhanced fork.

## Attribution

MewMobile is an independent fork and is not an official NuvioMedia product.

Original project:

- [NuvioMobile](https://github.com/NuvioMedia/NuvioMobile)
- [NuvioMedia](https://github.com/NuvioMedia)

MewMobile retains the original project's applicable GPL-3.0 licensing and attribution requirements. Fork-specific changes are maintained in this repository's history.

## Responsible use

MewMobile is a client application. It does not host or distribute media.

Use addons, plugins, catalogs and streams only when you are authorized to access the underlying content and services. Third-party sources are independent of MewMobile and may have their own terms, availability and legal requirements.

## Contributing

When submitting a change:

1. Keep the change focused.
2. Inspect the real execution path before changing behavior.
3. Preserve CloudStream and addon compatibility.
4. Avoid hard-coded secrets.
5. Run the relevant build/tests.
6. Clearly distinguish implemented, built, tested and device-verified work.
