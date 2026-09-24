# MewMobile

MewMobile is a modern Android media client built around flexible external integrations, with CloudStream compatibility as a core source layer.

MewMobile does not bundle an all-in-one provider system or host media. You choose the addons, plugins, repositories, metadata services, and playback integrations you want to use.

## Features

- CloudStream provider compatibility and dynamic repositories.
- External Stremio-compatible addon support.
- Optional TMDB metadata/catalog enrichment.
- Search, Home/catalog browsing, Details, episodes, and playback through supported integrations.
- External plugin and repository architecture.
- Multiple playback integrations where supported by the current build.
- Android-focused performance and diagnostics work.

## Architecture

```
MewMobile
├── UI
├── Metadata
├── CloudStream compatibility
├── External addons
├── External repositories / plugins
└── Playback
```

Integrations remain external. MewMobile does not silently bundle or inject third-party providers.

### TMDB vs CloudStream

**TMDB addon**

Optional metadata and catalog enrichment. Installing TMDB is not required for CloudStream providers to work.

**CloudStream plugins / repositories**

Provider and source integrations exposed through MewMobile's CloudStream compatibility layer. They can provide content independently of TMDB.

**Stremio/Nuvio addons**

External addon integrations. They remain separate from the CloudStream plugin system.

## Getting started

On first launch, MewMobile explains the integration model and points you to Discovery.

Typical setup:

1. Open **Discovery → Addons** when you want metadata/catalog integrations.
2. Open **Discovery → Plugins** when you want provider repositories.
3. Open **Discovery → CloudStream** to manage CloudStream repositories and extensions.
4. Return to Home after enabling the integrations you want.

### Optional TMDB metadata

TMDB manifest:

https://tmdb.elfhosted.com/manifest.json

### CloudStream provider repository

Provider repository manifest:

https://raw.githubusercontent.com/phisher98/phisher-nuvio-providers/refs/heads/main/manifest.json

These are external sources. Their availability and contents are independent of MewMobile.

## No built-in AIO

MewMobile intentionally does not include:

- a bundled AIO provider;
- a hidden provider database;
- automatic third-party provider injection;
- a mandatory TMDB dependency.

The application provides the client and compatibility layers; users choose their external integrations.

## Development

MewMobile is a Kotlin Multiplatform / Compose Multiplatform project.

### Android debug

```bash
./gradlew :androidApp:assembleFullDebug -Pnuvio.android.distribution=full
```

### Android release

```bash
./gradlew :androidApp:assembleFullRelease
```

### Validation

```bash
./gradlew allTests :androidApp:lintFullDebug
```

Keep private credentials, signing material, and API secrets out of Git.

## Builds and releases

MewMobile uses separate beta and stable release tracks.

**Beta**

- Prerelease builds for validation.
- Intended for testing current changes.
- Must not overwrite or replace stable releases.

**Stable**

- Production release track.
- Manually validated before publication.
- Signed APKs include the universal artifact and ABI variants produced by the Android build.

Release infrastructure also validates the Android signing certificate so an incorrectly signed APK cannot silently replace an existing installation.

## Updates

The in-app updater is intended to consume releases from:

https://github.com/somilkhan/MewMobile

Release asset naming and channel filtering must remain compatible with the updater implementation.

## Security

CI-only credentials belong in GitHub Actions repository secrets.

Never commit:

- release keystores or passwords;
- private API credentials;
- access tokens;
- signing keys;
- decoded CI `local.properties`.

An Android application cannot keep a secret private once it is compiled into an APK. Credentials that genuinely require server-side secrecy must be handled by a trusted backend boundary.

## Credits

MewMobile is maintained independently and retains required upstream attribution.

Upstream project:

https://github.com/NuvioMedia/NuvioMobile

MewMobile preserves applicable upstream licenses and attribution requirements. See the repository license for the authoritative terms.

## Responsible use

MewMobile is a client application and does not host or distribute media. Use external addons, plugins, catalogs, and streams only when you are authorized to access the underlying services and content.

