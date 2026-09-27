# MewMobile

Mew is an Android media client built as an independent fork of NuvioMobile, with a Mew-owned product/UI layer and native CloudStream compatibility.

The project is designed around **user-installed extensions and providers** rather than a bundled all-in-one content database.

## What Mew is

Mew provides the application experience around several independently managed capabilities:

- Home, search, details, episodes, library, profiles, downloads and playback.
- Native CloudStream repository/plugin compatibility in the full Android build.
- User-installed CloudStream repositories and providers.
- External addon integrations.
- Optional metadata enrichment, including TMDB.
- Multiple playback engines and configurable playback behavior.
- Diagnostics and backup/settings tooling for troubleshooting.

Mew does **not** host or distribute media content.

## How content flows

Mew keeps the product layer separate from the extension runtime:

```text
Mew UI
  ↓
Mew application / orchestration layer
  ↓
CloudStream runtime
  ↓
CloudStream repositories
  ↓
CloudStream plugins / providers
  ↓
Search / Home / Details / Episodes
  ↓
Source extraction
  ↓
Mew playback layer
```

CloudStream is a compatibility/runtime layer, not a bundled catalog. Mew does not replace it with a parallel provider architecture.

### Metadata is optional

CloudStream providers can expose content without TMDB. Metadata services are used for enrichment and presentation where configured.

TMDB can provide richer artwork, descriptions and matching, but **TMDB is not a prerequisite for CloudStream provider content**.

## Extensions and sources

Mew supports two distinct integration concepts:

### CloudStream providers

CloudStream repositories can supply providers capable of:

- Home/catalog content
- Search
- Details
- Episodes
- Source extraction

Provider availability and behavior depend on the installed third-party repository/plugin.

### External addons

Mew also supports manifest-based addons for capabilities such as catalogs, metadata, streams and subtitles. These are separate from the CloudStream runtime.

Only install third-party repositories, plugins and addons that you trust and are authorized to use.

## Getting started

1. Install a Mew build appropriate for your platform/distribution.
2. Open **Settings → Content & Discovery**.
3. Add the extensions or CloudStream repositories you want to use.
4. Enable compatible providers.
5. Optionally configure metadata enrichment.
6. Return to Home/Search and discover content from the integrations you installed.

Mew does not assume a particular third-party provider database is installed.

## Android build

The primary Android full build is:

```bash
./gradlew :androidApp:assembleFullDebug -Pnuvio.android.distribution=full
```

The repository currently retains some `nuvio.*` Gradle/property/package identifiers for compatibility. These are implementation details and should not be treated as the public product name.

Credentials and signing configuration belong in ignored `local.properties` or GitHub Actions secrets. Never commit credentials, signing keys or private configuration.

## Release information

Android release versioning is sourced from:

```text
iosApp/Configuration/Version.xcconfig
```

At the current branch state, the configured release version is **0.4.14 (118)**.

Release workflows build and publish Mew artifacts through GitHub Actions when the required signing/configuration secrets are available.

## Project structure

Important areas include:

- `composeApp/src/commonMain` — shared application/UI code.
- `composeApp/src/fullCommonMain` — full-distribution CloudStream integration.
- `composeApp/src/androidFull` — embedded Android CloudStream runtime and platform integration.
- `androidApp` — Android application packaging, manifests and release configuration.
- `composeApp/src/commonTest` — shared tests.
- `.github/workflows` — CI/release automation.

## Development principles

Mew development is intentionally separated into:

1. **Product/UI** — Mew-owned experience and branding.
2. **CloudStream compatibility** — integration with the native CloudStream runtime.
3. **Stability/performance** — rendering, loading, playback and lifecycle reliability.
4. **Upstream compatibility** — maintaining required compatibility with inherited/open-source components.

Do not add fake provider data, synthetic metadata, hardcoded provider catalogs or a duplicate extension runtime.

## Current limitations

- Content availability depends on the repositories, plugins and addons the user installs.
- Third-party providers can fail, disappear or change independently of Mew.
- Metadata enrichment is optional and can be unavailable without affecting the underlying provider architecture.
- Some internal compatibility identifiers still use upstream Nuvio naming to preserve update/data/integration compatibility.
- Platform capabilities differ between Android distributions and other targets.

## Attribution and licenses

Mew is an independent fork. Upstream NuvioMobile code and other third-party components remain subject to their respective licenses and attribution requirements.

Upstream project:

- NuvioMobile — https://github.com/NuvioMedia/NuvioMobile

CloudStream compatibility code and the embedded runtime remain subject to their applicable upstream licenses.

See [LICENSE](LICENSE) and the in-app **Licenses & Attribution** section for project-specific attribution information.

## Legal

Mew is a client application. It does not host or distribute media content. Use Mew and any third-party integrations only with content and services you are authorized to access.
