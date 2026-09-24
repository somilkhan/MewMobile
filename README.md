# Mew

Mew is a modern Android media client built from the NuvioMobile codebase and developed as an independent fork.

The project focuses on a clean media experience, CloudStream compatibility, external addons and plugins, optional metadata integrations, and reliable playback.

## What Mew provides

- CloudStream-compatible repositories, plugins, providers, search, details, episodes, and playback.
- External addon support rather than a bundled all-in-one provider database.
- Optional metadata integrations. TMDB is not required for CloudStream content.
- Profiles, library, playback, tracking, downloads, Live TV, and customization inherited and extended from the upstream codebase.
- Android and iOS targets with platform-specific playback and distribution support.
- An in-app updater for published Mew releases.

## CloudStream

CloudStream is a compatibility layer in Mew, not a bundled content catalog.

Users can install CloudStream repositories and plugins externally and enable the providers they want. Provider content must remain usable without TMDB.

Mew does not bundle an AIO provider database or silently aggregate third-party sources.

## Metadata

Metadata integrations are optional.

TMDB can provide metadata and enrichment when configured, but it is not a prerequisite for CloudStream providers to expose content.

## External addons

Mew supports user-installed external addons and manifests. Addons remain separate from the CloudStream provider system so users can choose which integrations they install.

## Release channels

Mew uses separate release tracks:

- **Stable** — production releases.
- **Beta** — prerelease builds for testing upcoming changes.

Release artifacts and update behavior are managed through GitHub Actions.

## Build

Clone the repository and select the development branch:

```bash
git clone https://github.com/somilkhan/MewMobile.git
cd MewMobile
git checkout feature/cloudstream-dynamic-repositories
./gradlew :androidApp:assembleFullDebug -Pnuvio.android.distribution=full
```

Credentials and private configuration belong in `local.properties` or GitHub Actions secrets. Never commit credentials, signing keys, or private configuration.

## Engineering principles

Mew is developed as a fork, so changes are intentionally separated into:

1. Product/UI changes owned by Mew.
2. CloudStream compatibility work.
3. Performance and stability improvements.
4. Upstream synchronization and compatibility maintenance.

Internal package names and compatibility identifiers may retain upstream Nuvio naming where changing them would risk existing data, integrations, or update compatibility.

## Attribution

Mew is an independent fork and is not the original NuvioMobile project.

Original project:

- NuvioMobile — https://github.com/NuvioMedia/NuvioMobile

Original code and third-party components remain subject to their respective licenses and attribution requirements.

## Legal

Mew is a client application. It does not host or distribute media content. Use the application and any third-party integrations only with content and services you are authorized to access.

See [LICENSE](LICENSE) for the project license.
